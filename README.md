# Sistema de Gestão de Estoque

API REST para apoiar pequenos comércios no controle de estoque, produtos, clientes, fornecedores, vendas, movimentações e alertas.

O sistema possui autenticação por JWT, controle de acesso por papéis e suporte a múltiplas empresas, mantendo os dados operacionais isolados pela empresa ativa.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Security e JWT
- Spring Data JPA / Hibernate
- MySQL 8
- Flyway
- Maven
- Docker e Docker Compose
- JUnit 5, Rest Assured e Testcontainers
- OpenAPI / Swagger

## Executar

Crie o arquivo `.env` na raiz do projeto:

```env
DB_PASSWORD=uma_senha_segura
JWT_SECRET=uma_chave_secreta_longa
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
```

Inicie a aplicação:

```bash
docker compose up --build
```

- API: `http://localhost:8080`
- Documentação: `http://localhost:8080/swagger-ui.html`

## Testes

```bash
mvn test
```

Os testes de integração utilizam MySQL com Testcontainers e exigem Docker Desktop em execução.
