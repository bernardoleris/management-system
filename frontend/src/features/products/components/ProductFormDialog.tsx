import { useState, type FormEvent } from 'react'
import { ApiError, jsonBody } from '../../../api/http'
import type { NamedOption, Product, ProductRequest, SupplierOption } from '../../../api/contracts'
import { useAuth } from '../../../auth/AuthContext'
import { Dialog, DialogFooter, DialogHeader } from '../../../shared/ui/Dialog'
import styles from '../ProductsPage.module.css'

interface Props {
  product: Product | null
  categories: NamedOption[]
  brands: NamedOption[]
  suppliers: SupplierOption[]
  canEditReferences: boolean
  referencesError: string
  suppliersError: string
  loadingReferences: boolean
  loadingSuppliers: boolean
  onClose: () => void
  onRetryReferences: () => void
  onSaved: (product: Product) => void
}

function cx(...classNames: Array<string | false | undefined>) {
  return classNames.flatMap((className) => className ? className.split(/\s+/).map((name) => styles[name] ?? name) : []).join(' ')
}

export function ProductFormDialog({ product, categories, brands, suppliers, canEditReferences, referencesError, suppliersError, loadingReferences, loadingSuppliers, onClose, onRetryReferences, onSaved }: Props) {
  const { request } = useAuth()
  const [name, setName] = useState(product?.name ?? '')
  const [description, setDescription] = useState(product?.description ?? '')
  const [barcode, setBarcode] = useState(product?.barcode ?? '')
  const [purchasePrice, setPurchasePrice] = useState(product ? String(product.purchasePrice) : '')
  const [salePrice, setSalePrice] = useState(product ? String(product.salePrice) : '')
  const [quantity, setQuantity] = useState(product ? String(product.quantity) : '0')
  const [categoryId, setCategoryId] = useState(product ? String(product.categoryId) : '')
  const [brandId, setBrandId] = useState(product ? String(product.brandId) : '')
  const [supplierId, setSupplierId] = useState(product?.supplierId ? String(product.supplierId) : '')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSaving(true)

    const payload: ProductRequest = {
      name: name.trim(),
      description: description.trim() || null,
      barcode: barcode.trim() || null,
      purchasePrice: Number(purchasePrice),
      salePrice: Number(salePrice),
      quantity: Number(quantity),
      categoryId: Number(categoryId),
      brandId: Number(brandId),
      supplierId: supplierId ? Number(supplierId) : null,
    }

    try {
      const latestProduct = product ? await request<Product>(`/products/${product.id}`) : null
      const saved = await request<Product>(product ? `/products/${product.id}` : '/products', {
        method: product ? 'PUT' : 'POST',
        body: jsonBody({ ...payload, quantity: latestProduct?.quantity ?? payload.quantity }),
      })
      onSaved(saved)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível salvar o produto. Tente novamente.')
    } finally {
      setSaving(false)
    }
  }

  return (
      <Dialog labelledBy="product-form-title" onClose={onClose} closeOnEscape={!saving}>
        <DialogHeader>
          <div>
            <span className={cx('eyebrow')}>CATÁLOGO</span>
            <h2 id="product-form-title">{product ? 'Editar produto' : 'Novo produto'}</h2>
            <p>{product ? 'Atualize os dados do produto.' : 'Cadastre os dados que identificam e precificam o produto.'}</p>
          </div>
          <button className={cx('icon-button')} type="button" aria-label="Fechar formulário" onClick={onClose}>×</button>
        </DialogHeader>

        <form className={cx('product-form')} onSubmit={(event) => void handleSubmit(event)}>
          {loadingReferences && <div className={cx('product-form-message')} role="status">Carregando categorias e marcas…</div>}
          {referencesError && <div className={cx('form-alert')} role="alert">{referencesError}<button type="button" onClick={onRetryReferences}>Tentar novamente</button></div>}
          {!loadingReferences && !referencesError && canEditReferences && (!categories.length || !brands.length) && <div className={cx('product-form-message')} role="status">É necessário cadastrar ao menos uma categoria e uma marca antes de salvar este produto.</div>}
          {suppliersError && <div className={cx('product-form-message')} role="status">Fornecedores indisponíveis no momento. O produto pode ser salvo sem fornecedor.</div>}
          {error && <div className={cx('form-alert')} role="alert">{error}</div>}

          <div className={cx('product-form-grid')}>
            <label className={cx('product-form-wide')}>Nome do produto
              <input autoFocus required maxLength={100} value={name} onChange={(event) => setName(event.target.value)} placeholder="Ex.: Café em grãos 500 g" />
            </label>
            <label>Código de barras
              <input maxLength={50} value={barcode} onChange={(event) => setBarcode(event.target.value)} placeholder="Opcional" />
            </label>
            {canEditReferences ? <label>Fornecedor
              <select value={supplierId} onChange={(event) => setSupplierId(event.target.value)} disabled={loadingSuppliers}>
                <option value="">{loadingSuppliers ? 'Carregando fornecedores…' : 'Sem fornecedor'}</option>
                {product?.supplierId && !suppliers.some((supplier) => supplier.id === product.supplierId) && <option value={product.supplierId}>{product.supplierTradeName || 'Fornecedor atual'} {suppliersError ? '(indisponível)' : '(inativo)'}</option>}
                {suppliers.map((supplier) => <option value={supplier.id} key={supplier.id}>{supplier.tradeName}</option>)}
              </select>
            </label> : <div className={cx('product-form-readonly')}><span>Fornecedor</span><strong>{product?.supplierTradeName || 'Sem fornecedor'}</strong></div>}
            {canEditReferences ? <label>Categoria
              <select required value={categoryId} onChange={(event) => setCategoryId(event.target.value)} disabled={loadingReferences || !categories.length}>
                <option value="">Selecione uma categoria</option>
                {categories.map((category) => <option value={category.id} key={category.id}>{category.name}</option>)}
              </select>
            </label> : <div className={cx('product-form-readonly')}><span>Categoria</span><strong>{product?.categoryName || '—'}</strong></div>}
            {canEditReferences ? <label>Marca
              <select required value={brandId} onChange={(event) => setBrandId(event.target.value)} disabled={loadingReferences || !brands.length}>
                <option value="">Selecione uma marca</option>
                {brands.map((brand) => <option value={brand.id} key={brand.id}>{brand.name}</option>)}
              </select>
            </label> : <div className={cx('product-form-readonly')}><span>Marca</span><strong>{product?.brandName || '—'}</strong></div>}
            <label>Preço de compra
              <span className={cx('product-money-input')}><span>R$</span><input required type="number" min="0" step="0.01" value={purchasePrice} onChange={(event) => setPurchasePrice(event.target.value)} placeholder="0,00" /></span>
            </label>
            <label>Preço de venda
              <span className={cx('product-money-input')}><span>R$</span><input required type="number" min="0.01" step="0.01" value={salePrice} onChange={(event) => setSalePrice(event.target.value)} placeholder="0,00" /></span>
            </label>
            {product ? (
              <div className={cx('product-stock-preserved')}><span>Estoque atual</span><strong>{product.quantity} unid.</strong><small>O estoque será preservado nesta edição.</small></div>
            ) : (
              <label>Estoque inicial
                <input required type="number" min="0" step="1" value={quantity} onChange={(event) => setQuantity(event.target.value)} />
              </label>
            )}
            <label className={cx('product-form-wide')}>Descrição
              <textarea maxLength={255} rows={3} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Detalhes opcionais do produto" />
            </label>
          </div>

          <DialogFooter>
            <button className={cx('button-secondary')} type="button" onClick={onClose} disabled={saving}>Cancelar</button>
            <button className={cx('button-primary')} type="submit" disabled={saving || loadingReferences || Boolean(referencesError) || (canEditReferences && (!categories.length || !brands.length))}>
              {saving ? 'Salvando…' : product ? 'Salvar alterações' : 'Cadastrar produto'}
            </button>
          </DialogFooter>
        </form>
      </Dialog>
  )
}
