# Sistema de Gestão de Estoque

Aplicação de gestão de estoque para pequenos comércios, composta por uma API REST e um frontend web.

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

## Estrutura

```text
backend/             API Spring Boot, migrations e testes
frontend/            Aplicação web
docs/                Documentação de arquitetura e domínio
docker-compose.yml   Infraestrutura local
```

O frontend será mantido em `frontend/` como um projeto independente. A base visual e os componentes reutilizáveis serão implementados em código, sem depender de Figma.

### Desenvolvimento do frontend

```bash
cd frontend
npm install
npm run dev
```

Use `npm run build` para gerar a versão de produção e `npm run lint` para verificar o código.

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
cd backend
mvn test
```

Os testes de integração utilizam MySQL com Testcontainers e exigem Docker Desktop em execução.
