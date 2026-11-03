# Bernardo Leris System

API REST para gestão de estoque e operações de pequenos comércios. Ela oferece autenticação, cadastros, vendas, movimentações de estoque, alertas e indicadores.

## Recursos

- Autenticação com JWT e refresh token;
- controle de acesso por papel (`ROLE_OWNER`, `ROLE_ADMIN` e `ROLE_OPERATOR`);
- produtos, categorias, marcas, fornecedores, clientes e endereços;
- vendas com itens, descontos, status e formas de pagamento;
- movimentações de estoque e prevenção de saldo negativo;
- alertas de estoque e analytics de vendas;
- documentação OpenAPI e migrations versionadas com Flyway.

## Tecnologia

Java 21, Spring Boot 4, Spring Security, Spring Data JPA, Flyway, MySQL 8, Maven, Docker e Docker Compose.

## Executar com Docker

Crie um arquivo `.env` na raiz com as variáveis abaixo. Não versione esse arquivo.

```env
DB_PASSWORD=uma_senha_segura
JWT_SECRET=uma_chave_secreta_longa
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
```

Suba os serviços:

```bash
docker compose up --build
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- MySQL: `localhost:3307` (banco `stock_db`)

## Testes

```bash
mvn test
```

Os testes de integração usam Testcontainers e exigem Docker em execução.

## Estado de multiempresa

O banco e a autenticação já possuem empresas e vínculos de acesso, e o JWT carrega a empresa ativa. As permissões também são derivadas do vínculo ativo, permitindo papéis diferentes para a mesma pessoa em empresas distintas. O escopo por empresa já está aplicado aos módulos operacionais e aos indicadores, e os `company_id` são obrigatórios no banco. Ainda faltam testes de integração completos e a revisão dos endpoints administrativos; portanto, o isolamento entre empresas ainda não deve ser considerado concluído.

O dono da empresa gerencia a equipe em `/company/members`: cria login e senha, define o papel, altera o papel e desativa acessos. O sistema preserva pelo menos um dono ativo por empresa.

## Empresas e equipe

Após autenticar, use o access token para:

- `GET /auth/companies`: listar as empresas disponíveis e o papel em cada uma;
- `POST /auth/switch-company`: trocar a empresa ativa e receber um novo access token;
- `GET /company/members`: listar membros da empresa ativa;
- `POST /company/members`: criar uma nova conta e associá-la à empresa ativa;
- `POST /company/members/existing`: associar uma conta existente à empresa ativa;
- `PUT /company/members/{membershipId}/role`: alterar o papel de um membro;
- `DELETE /company/members/{membershipId}`: desativar o vínculo do membro.

Exemplo de troca de empresa:

```json
{
  "companyId": 2
}
```

Ao renovar o token, envie `companyId` junto do refresh token para manter a empresa desejada.

## Estrutura principal

```text
src/main/java/br/com/system
├── controllers     # endpoints HTTP
├── services        # regras de negócio
├── repository      # acesso a dados
├── model           # entidades JPA
├── security        # JWT e autorização
└── config          # configuração da aplicação e OpenAPI

src/main/resources/db/migration
└── V1 ... V4       # schema e evolução do banco com Flyway
```
