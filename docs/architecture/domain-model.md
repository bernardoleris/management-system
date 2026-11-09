# Modelo de domínio

Este diagrama é uma visão de manutenção das relações principais do backend. Ele prioriza a empresa ativa, segurança e fluxos operacionais; campos e DTOs ficam fora para manter a leitura útil durante o crescimento do sistema.

```mermaid
classDiagram
    class Company
    class UserEntity
    class Administrator
    class CompanyMembership {
        CompanyRole role
        boolean active
    }
    class RefreshToken

    class Client
    class Address
    class Category
    class Brand
    class Supplier
    class Product
    class Sale
    class SaleItem
    class StockMovement
    class StockMovementItem
    class Alert

    UserEntity "1" <-- "0..1" Administrator : credential
    UserEntity "1" <-- "0..*" CompanyMembership : member
    Company "1" <-- "0..*" CompanyMembership : membership
    Administrator "1" <-- "0..*" RefreshToken : owns

    Company "1" <-- "0..*" Client : scopes
    Company "1" <-- "0..*" Address : scopes
    Client "1" --> "1" UserEntity : profile
    Client "1" --> "0..1" Address : address

    Company "1" <-- "0..*" Category : scopes
    Company "1" <-- "0..*" Brand : scopes
    Company "1" <-- "0..*" Supplier : scopes
    Company "1" <-- "0..*" Product : scopes
    Product "0..*" --> "1" Category : category
    Product "0..*" --> "1" Brand : brand
    Product "0..*" --> "0..1" Supplier : supplier

    Company "1" <-- "0..*" Sale : scopes
    Sale "0..*" --> "1" Administrator : createdBy
    Sale "0..*" --> "0..1" Client : client
    Sale "1" *-- "0..*" SaleItem : items
    SaleItem "0..*" --> "1" Product : product

    Company "1" <-- "0..*" StockMovement : scopes
    StockMovement "0..*" --> "1" Administrator : createdBy
    StockMovement "0..*" --> "0..1" Supplier : supplier
    StockMovement "0..*" --> "0..1" Sale : generatedFrom
    StockMovement "1" *-- "0..*" StockMovementItem : items
    StockMovementItem "0..*" --> "1" Product : product

    Company "1" <-- "0..*" Alert : scopes
    Alert "0..*" --> "1" Product : product
    Alert "0..*" --> "0..1" Administrator : assignedTo
```

## Regras de leitura

- `CompanyMembership` é a fonte do papel ativo do usuário na empresa; o JWT carrega `user_id`, `company_id` e `membership_id`.
- Entidades operacionais são sempre delimitadas por `Company`; relações entre empresas devem ser rejeitadas pelos serviços.
- Apenas vendas concluídas originam movimentação de estoque do tipo `SALE`.
- `UserEntity` é compartilhado como identidade, mas clientes e vínculos de empresa são delimitados pela empresa atual.
