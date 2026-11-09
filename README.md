# Sistema de Gestão de Estoque

Aplicação de gestão de estoque para pequenos comércios, composta por uma API REST e um frontend web. O sistema usa JWT, controle de acesso por papéis e suporte a múltiplas empresas, com dados operacionais isolados pela empresa ativa.

## Tecnologias

- Java 21, Spring Boot 4, Spring Security, Spring Data JPA e Hibernate
- MySQL 8 e Flyway
- React, TypeScript e Vite
- Docker Compose e OpenAPI / Swagger

## Executar localmente

Crie o `.env` na raiz:

```env
DB_PASSWORD=uma_senha_segura
JWT_SECRET=uma_chave_secreta_longa
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
```

Inicie os serviços:

```bash
docker compose up --build
```

- API e Swagger: `http://localhost:8080/swagger-ui.html`
- Mailpit local: `http://localhost:8025`
- Frontend: inicie com `cd frontend`, `npm install` e `npm run dev`; por padrão, fica em `http://localhost:3000`.

## Cadastro de empresa e proprietário

O cadastro é feito pela tela `http://localhost:3000/register` ou pela rota pública `POST /auth/register` no Swagger. Informe nome da empresa, dados do proprietário, login e senha. A API cria a empresa, o usuário, o administrador e o vínculo de proprietário como pendentes, e envia uma confirmação para o e-mail informado. Em desenvolvimento, abra o Mailpit em `http://localhost:8025` e acesse o link de confirmação. A empresa e o acesso são ativados após a confirmação; então é possível entrar usando login ou e-mail e a senha escolhida.

O cadastro não cria planos, cobranças ou usuários de demonstração. O proprietário pode adicionar membros pela rota autenticada `POST /company/members`; o convite por e-mail será implementado depois.

Para outro servidor SMTP, configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_AUTH`, `MAIL_STARTTLS`, `MAIL_FROM` e `APP_FRONTEND_URL`. Use HTTPS para o frontend em produção.

## Desenvolvimento do frontend

```bash
cd frontend
npm install
npm run dev
npm run build
```

## Testes

```bash
cd backend
mvn test
```

Os testes de integração usam MySQL com Testcontainers e exigem Docker Desktop em execução.
