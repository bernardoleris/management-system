import { useEffect, useMemo, useState } from 'react'
import { ApiError } from '../../api/http'
import type { NamedOption, Page, Product, SupplierOption } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import { Dialog, DialogConfirmationIcon, DialogFooter, DialogHeader } from '../../shared/ui/Dialog'
import { ProductFormDialog } from './components/ProductFormDialog'
import styles from './ProductsPage.module.css'

type FilterKind = '' | 'category' | 'brand' | 'supplier'
type ProductStatus = 'active' | 'all'

const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const date = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' })

function cx(...classNames: Array<string | false | undefined>) {
  return classNames.flatMap((className) => className ? className.split(/\s+/).map((name) => styles[name] ?? name) : []).join(' ')
}

async function loadAllSuppliers(request: ReturnType<typeof useAuth>['request']) {
  const firstPage = await request<Page<SupplierOption>>('/suppliers/active?page=0&size=100&sort=tradeName,asc')
  const remainingPages = await Promise.all(Array.from({ length: Math.max(firstPage.totalPages - 1, 0) }, (_, index) =>
    request<Page<SupplierOption>>(`/suppliers/active?page=${index + 1}&size=100&sort=tradeName,asc`),
  ))
  return [...firstPage.content, ...remainingPages.flatMap((page) => page.content)]
}

export function ProductsPage() {
  const { request, session } = useAuth()
  const canManageReferences = session?.role === 'COMPANY_OWNER' || session?.role === 'ADMIN'
  const canEditProducts = session?.role === 'COMPANY_OWNER' || session?.role === 'ADMIN' || session?.role === 'OPERATOR'
  const [page, setPage] = useState<Page<Product> | null>(null)
  const [pageNumber, setPageNumber] = useState(0)
  const [status, setStatus] = useState<ProductStatus>('active')
  const [filterKind, setFilterKind] = useState<FilterKind>('')
  const [filterId, setFilterId] = useState('')
  const [categories, setCategories] = useState<NamedOption[]>([])
  const [brands, setBrands] = useState<NamedOption[]>([])
  const [suppliers, setSuppliers] = useState<SupplierOption[]>([])
  const [referencesError, setReferencesError] = useState('')
  const [suppliersError, setSuppliersError] = useState('')
  const [loadingReferences, setLoadingReferences] = useState(false)
  const [loadingSuppliers, setLoadingSuppliers] = useState(false)
  const [referencesRetry, setReferencesRetry] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [createBlockMessage, setCreateBlockMessage] = useState('')
  const [reloadToken, setReloadToken] = useState(0)
  const [formProduct, setFormProduct] = useState<Product | null | undefined>(undefined)
  const [detailsProduct, setDetailsProduct] = useState<Product | null>(null)
  const [productToDeactivate, setProductToDeactivate] = useState<Product | null>(null)
  const [changingProductId, setChangingProductId] = useState<number | null>(null)

  useEffect(() => {
    let current = true
    setLoading(true)
    setError('')

    const query = `?page=${pageNumber}&size=20&sort=name,asc`
    const path = filterId && filterKind
      ? `/products/${filterKind}/${filterId}${query}`
      : `/products${status === 'active' ? '/active' : ''}${query}`

    request<Page<Product>>(path)
      .then((result) => {
        if (!current) return
        setPage(result)
        if (result.content.length === 0 && pageNumber > 0 && !result.first) setPageNumber(result.number - 1)
      })
      .catch((reason: unknown) => {
        if (current) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os produtos.')
      })
      .finally(() => { if (current) setLoading(false) })

    return () => { current = false }
  }, [filterId, filterKind, pageNumber, reloadToken, request, session?.companyId, status])

  useEffect(() => {
    if (!canManageReferences) return
    let current = true
    setLoadingReferences(true)
    setReferencesError('')
    Promise.all([
      request<NamedOption[]>('/categories'),
      request<NamedOption[]>('/brands'),
    ])
      .then(([nextCategories, nextBrands]) => {
        if (!current) return
        setCategories(nextCategories)
        setBrands(nextBrands)
      })
      .catch((reason: unknown) => {
        if (current) setReferencesError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar categorias e marcas.')
      })
      .finally(() => { if (current) setLoadingReferences(false) })

    setLoadingSuppliers(true)
    setSuppliersError('')
    loadAllSuppliers(request)
      .then((nextSuppliers) => { if (current) setSuppliers(nextSuppliers) })
      .catch((reason: unknown) => {
        if (!current) return
        setSuppliers([])
        setSuppliersError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar fornecedores.')
      })
      .finally(() => { if (current) setLoadingSuppliers(false) })

    return () => { current = false }
  }, [canManageReferences, referencesRetry, request, session?.companyId])

  const filterOptions = useMemo(() => {
    if (filterKind === 'category') return categories.map(({ id, name }) => ({ id, name }))
    if (filterKind === 'brand') return brands.map(({ id, name }) => ({ id, name }))
    if (filterKind === 'supplier') return suppliers.map(({ id, tradeName }) => ({ id, name: tradeName }))
    return []
  }, [brands, categories, filterKind, suppliers])

  const filteredLabel = useMemo(() => {
    if (!filterId) return ''
    return filterOptions.find((option) => String(option.id) === filterId)?.name ?? ''
  }, [filterId, filterOptions])

  function changeStatus(nextStatus: ProductStatus) {
    setStatus(nextStatus)
    setFilterKind('')
    setFilterId('')
    setPageNumber(0)
  }

  function changeFilterKind(nextKind: FilterKind) {
    setFilterKind(nextKind)
    setFilterId('')
    setPageNumber(0)
    if (nextKind) setStatus('active')
  }

  function openNewProductForm() {
    setCreateBlockMessage('')
    if (loadingReferences) {
      setCreateBlockMessage('Aguarde o carregamento das categorias e marcas antes de cadastrar um produto.')
      return
    }
    if (referencesError) {
      setCreateBlockMessage('Não foi possível conferir as categorias e marcas. Tente carregá-las novamente antes de cadastrar um produto.')
      return
    }
    if (!categories.length || !brands.length) {
      const missing = [!categories.length && 'categoria', !brands.length && 'marca'].filter(Boolean).join(' e ')
      setCreateBlockMessage(`Não é possível cadastrar um produto sem ${missing}. Cadastre ${missing} e tente novamente.`)
      return
    }
    setNotice('')
    setFormProduct(null)
  }

  async function toggleProduct(product: Product) {
    setChangingProductId(product.id)
    setError('')
    setNotice('')
    try {
      await request<void>(`/products/${product.id}/toggle-active`, { method: 'PATCH' })
      setProductToDeactivate(null)
      setNotice(product.active ? 'Produto desativado.' : 'Produto reativado.')
      setPageNumber(0)
      setReloadToken((value) => value + 1)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível atualizar o status do produto.')
      setProductToDeactivate(null)
    } finally {
      setChangingProductId(null)
    }
  }

  function productActions(product: Product) {
    return (
      <div className={cx('product-actions')}>
        <button className={cx('text-action')} type="button" onClick={() => setDetailsProduct(product)}>Detalhes</button>
        {canEditProducts && <>
          <button className={cx('text-action')} type="button" onClick={() => setFormProduct(product)}>Editar</button>
          <button
            className={cx('text-action', product.active && 'text-action-muted')}
            type="button"
            disabled={changingProductId === product.id}
            onClick={() => product.active ? setProductToDeactivate(product) : void toggleProduct(product)}
          >
            {changingProductId === product.id ? 'Atualizando…' : product.active ? 'Desativar' : 'Reativar'}
          </button>
        </>}
      </div>
    )
  }

  const showingFrom = page?.totalElements ? page.number * page.size + 1 : 0
  const showingTo = page ? Math.min(page.number * page.size + page.content.length, page.totalElements) : 0

  return (
    <section className={cx('page-section products-page')}>
      <div className={cx('page-heading products-heading')}>
        <div>
          <span className={cx('eyebrow')}>CATÁLOGO</span>
          <h1>Produtos</h1>
          <p>Consulte preços, estoque e dados dos produtos da empresa ativa.</p>
        </div>
        {canManageReferences && <button className={cx('button-primary products-add-button')} type="button" onClick={openNewProductForm}>
          <span aria-hidden="true">＋</span> Novo produto
        </button>}
      </div>

      {notice && <div className={cx('form-success products-notice')} role="status">{notice}</div>}
      {createBlockMessage && <div className={cx('products-create-block')} role="alert">{createBlockMessage}<button type="button" onClick={() => setCreateBlockMessage('')} aria-label="Fechar aviso">×</button></div>}
      {error && <div className={cx('form-alert page-alert')} role="alert">{error}<button type="button" onClick={() => setReloadToken((value) => value + 1)}>Tentar novamente</button></div>}
      {referencesError && canManageReferences && <div className={cx('form-alert products-reference-alert')} role="alert">Não foi possível carregar categorias e marcas: {referencesError}<button type="button" onClick={() => setReferencesRetry((value) => value + 1)}>Tentar novamente</button></div>}
      {suppliersError && canManageReferences && <div className={cx('products-reference-note')} role="status">Fornecedores não foram carregados. Você ainda pode cadastrar o produto sem fornecedor.</div>}
      {canManageReferences && !loadingReferences && !referencesError && (!categories.length || !brands.length) && <div className={cx('products-reference-note')} role="status">O cadastro exige ao menos uma categoria e uma marca. Cadastre esses dados antes de criar produtos.</div>}

      <div className={cx('data-card products-card')}>
        <div className={cx('data-card-header products-card-header')}>
          <div><h2>Catálogo de produtos</h2><p>{loading ? 'Carregando catálogo…' : `${page?.totalElements ?? 0} ${page?.totalElements === 1 ? 'produto' : 'produtos'}`}</p></div>
          <button className={cx('button-secondary products-refresh')} type="button" onClick={() => setReloadToken((value) => value + 1)} disabled={loading}>↻ Atualizar</button>
        </div>

        <div className={cx('products-filters')} aria-label="Filtros do catálogo">
          <label>Status
            <select value={status} onChange={(event) => changeStatus(event.target.value as ProductStatus)}>
              <option value="active">Produtos ativos</option>
              <option value="all">Todos os produtos</option>
            </select>
          </label>
          {canManageReferences && <><label>Filtrar por
            <select value={filterKind} onChange={(event) => changeFilterKind(event.target.value as FilterKind)}>
              <option value="">Sem filtro</option>
              <option value="category">Categoria</option>
              <option value="brand">Marca</option>
              <option value="supplier">Fornecedor</option>
            </select>
          </label>
          {filterKind && <label className={cx('products-filter-value')}>{filterKind === 'category' ? 'Categoria' : filterKind === 'brand' ? 'Marca' : 'Fornecedor'}
            <select value={filterId} onChange={(event) => { setFilterId(event.target.value); setPageNumber(0) }} disabled={loadingReferences || filterOptions.length === 0}>
              <option value="">Selecione</option>
              {filterOptions.map((option) => <option value={option.id} key={option.id}>{option.name}</option>)}
            </select>
          </label>}
          <span className={cx('products-filter-note')}>Os filtros por categoria, marca e fornecedor mostram produtos ativos.</span>
          </>}
          {!canManageReferences && <span className={cx('products-filter-note')}>Para cadastrar produtos, solicite ao proprietário ou administrador uma categoria e uma marca.</span>}
        </div>

        {loading ? <div className={cx('products-state')} role="status"><span className={cx('products-loading-mark')} />Carregando produtos…</div>
          : page?.content.length ? <>
            <div className={cx('products-table-wrap')}>
              <table className={cx('products-table')}>
                <thead><tr><th>Produto</th><th>Categoria</th><th>Marca / fornecedor</th><th>Estoque</th><th>Preço de venda</th><th>Status</th><th><span className={cx('sr-only')}>Ações</span></th></tr></thead>
                <tbody>{page.content.map((product) => <tr key={product.id}>
                  <td><div className={cx('product-name-cell')}><strong>{product.name}</strong><small>{product.barcode || 'Sem código de barras'}</small></div></td>
                  <td>{product.categoryName}</td>
                  <td><div className={cx('product-name-cell')}><span>{product.brandName}</span><small>{product.supplierTradeName || 'Sem fornecedor'}</small></div></td>
                  <td><span className={cx('product-stock', product.quantity === 0 && 'product-stock-empty')}>{product.quantity} unid.</span></td>
                  <td className={cx('product-price')}>{money.format(product.salePrice)}</td>
                  <td><span className={cx('product-status', product.active ? 'product-status-active' : 'product-status-inactive')}>{product.active ? 'Ativo' : 'Inativo'}</span></td>
                  <td>{productActions(product)}</td>
                </tr>)}</tbody>
              </table>
            </div>

            <div className={cx('product-cards')}>
              {page.content.map((product) => <article className={cx('product-card')} key={product.id}>
                <div className={cx('product-card-top')}><div className={cx('product-name-cell')}><strong>{product.name}</strong><small>{product.barcode || 'Sem código de barras'}</small></div><span className={cx('product-status', product.active ? 'product-status-active' : 'product-status-inactive')}>{product.active ? 'Ativo' : 'Inativo'}</span></div>
                <div className={cx('product-card-meta')}><span>{product.categoryName}</span><span>{product.brandName}</span></div>
                <div className={cx('product-card-bottom')}><span className={cx('product-stock', product.quantity === 0 && 'product-stock-empty')}>{product.quantity} unid. em estoque</span><strong className={cx('product-price')}>{money.format(product.salePrice)}</strong></div>
                {productActions(product)}
              </article>)}
            </div>

            <div className={cx('products-pagination')}>
              <span>Exibindo {showingFrom}–{showingTo} de {page.totalElements}</span>
              <div><span>Página {page.number + 1} de {Math.max(page.totalPages, 1)}</span>
                <button className={cx('button-secondary')} type="button" disabled={page.first || loading} onClick={() => setPageNumber((current) => Math.max(0, current - 1))}>Anterior</button>
                <button className={cx('button-secondary')} type="button" disabled={page.last || loading} onClick={() => setPageNumber((current) => current + 1)}>Próxima</button>
              </div>
            </div>
          </>
          : <div className={cx('products-state products-empty-state')}>
            <span className={cx('products-empty-icon')} aria-hidden="true">▦</span>
            <h3>{error ? 'Não foi possível exibir o catálogo' : filteredLabel ? 'Nenhum produto encontrado' : 'Nenhum produto cadastrado'}</h3>
            <p>{error ? 'Tente carregar os produtos novamente.' : filteredLabel ? `Não há produtos ativos para “${filteredLabel}”.` : 'Cadastre o primeiro produto para começar seu catálogo.'}</p>
            {canManageReferences && !error && !filteredLabel && <button className={cx('button-primary products-add-button')} type="button" onClick={openNewProductForm}>Cadastrar produto</button>}
          </div>}
      </div>

      {formProduct !== undefined && <ProductFormDialog
        key={formProduct?.id ?? 'new-product'}
        product={formProduct}
        categories={categories}
        brands={brands}
        suppliers={suppliers}
        canEditReferences={canManageReferences}
        referencesError={referencesError}
        loadingReferences={loadingReferences}
        loadingSuppliers={loadingSuppliers}
        suppliersError={suppliersError}
        onRetryReferences={() => setReferencesRetry((value) => value + 1)}
        onClose={() => setFormProduct(undefined)}
        onSaved={(saved) => {
          setFormProduct(undefined)
          setNotice(saved.active ? 'Produto salvo com sucesso.' : 'Produto salvo como inativo.')
          setPageNumber(0)
          setFilterKind('')
          setFilterId('')
          setStatus('active')
          setReloadToken((value) => value + 1)
        }}
      />}

      {detailsProduct && <Dialog labelledBy="product-details-title" variant="details" onClose={() => setDetailsProduct(null)}>
          <DialogHeader>
            <div><span className={cx('eyebrow')}>DETALHES DO PRODUTO</span><h2 id="product-details-title">{detailsProduct.name}</h2><span className={cx('product-status', detailsProduct.active ? 'product-status-active' : 'product-status-inactive')}>{detailsProduct.active ? 'Ativo' : 'Inativo'}</span></div>
            <button className={cx('icon-button')} type="button" aria-label="Fechar detalhes" onClick={() => setDetailsProduct(null)}>×</button>
          </DialogHeader>
          <dl className={cx('product-details-grid')}>
            <div><dt>Categoria</dt><dd>{detailsProduct.categoryName}</dd></div>
            <div><dt>Marca</dt><dd>{detailsProduct.brandName}</dd></div>
            <div><dt>Fornecedor</dt><dd>{detailsProduct.supplierTradeName || 'Não informado'}</dd></div>
            <div><dt>Código de barras</dt><dd>{detailsProduct.barcode || 'Não informado'}</dd></div>
            <div><dt>Estoque</dt><dd>{detailsProduct.quantity} unidades</dd></div>
            <div><dt>Preço de compra</dt><dd>{money.format(detailsProduct.purchasePrice)}</dd></div>
            <div><dt>Preço de venda</dt><dd>{money.format(detailsProduct.salePrice)}</dd></div>
            <div><dt>Cadastrado em</dt><dd>{detailsProduct.createdAt ? date.format(new Date(detailsProduct.createdAt)) : '—'}</dd></div>
            <div className={cx('product-detail-description')}><dt>Descrição</dt><dd>{detailsProduct.description || 'Sem descrição.'}</dd></div>
          </dl>
          <DialogFooter>
            <button className={cx('button-secondary')} type="button" onClick={() => setDetailsProduct(null)}>Fechar</button>
            {canEditProducts && <button className={cx('button-primary')} type="button" onClick={() => { setFormProduct(detailsProduct); setDetailsProduct(null) }}>Editar produto</button>}
          </DialogFooter>
      </Dialog>}

      {productToDeactivate && <Dialog labelledBy="deactivate-title" describedBy="deactivate-description" role="alertdialog" variant="confirmation" onClose={() => setProductToDeactivate(null)} closeOnBackdrop={false}>
          <DialogConfirmationIcon>!</DialogConfirmationIcon>
          <h2 id="deactivate-title">Desativar produto?</h2>
          <p id="deactivate-description"><strong>{productToDeactivate.name}</strong> deixará de aparecer nas novas vendas. Você pode reativá-lo depois.</p>
          <DialogFooter><button className={cx('button-secondary')} type="button" onClick={() => setProductToDeactivate(null)}>Cancelar</button><button className={cx('button-primary product-deactivate-confirm')} type="button" disabled={changingProductId === productToDeactivate.id} onClick={() => void toggleProduct(productToDeactivate)}>{changingProductId === productToDeactivate.id ? 'Desativando…' : 'Desativar produto'}</button></DialogFooter>
      </Dialog>}
    </section>
  )
}
