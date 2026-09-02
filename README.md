# 🗂️ Stock Management System

> A web-based inventory management system designed for small retail businesses.

---

## 📌 About

The **Stock Management System** is a REST API built with **Spring Boot**, aimed at helping small store owners take control of their business operations. The goal is to provide a simple, reliable, and professional tool to manage products, sales, customers, and suppliers — all in one place.

The system was designed with real-world small business needs in mind: intuitive workflows, clear stock alerts, sales tracking, and analytics to help owners make better decisions.

---

## 🎯 Goals

- Give store owners real-time visibility over their inventory
- Track sales and associate them with customers
- Alert when a product is running low on stock
- Provide business analytics (best-selling products, busiest days, customer insights)
- Support multiple access levels for owners and employees

---

## 🧩 Core Modules

| Module | Description |
|---|---|
| **Products** | Register and manage products with pricing, stock quantity, category, and brand |
| **Categories & Brands** | Organize products for better management |
| **Suppliers** | Track product suppliers and link them to stock entries |
| **Clients** | Register customers and track their purchase history |
| **Sales** | Record sales with multiple items, payment methods, and optional discounts |
| **Stock Movements** | Full history of stock entries, exits, adjustments, and sales |
| **Alerts** | Custom low-stock alerts configured per product |
| **Analytics** | Insights on sales performance, customer behavior, and stock health |

---

## 🔐 Access Levels

| Role | Who | Access |
|---|---|---|
| `ROLE_OWNER` | System owner | Full access across the entire platform |
| `ROLE_ADMIN` | Store owner | Full access to their own store's data |
| `ROLE_OPERATOR` | Store employee | Basic operations: sales, stock queries, alerts |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Security | Spring Security + JWT + Refresh Token |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Build Tool | Maven |
| Containerization | Docker + Docker Compose |


---

## 🚀 Running with Docker

Make sure you have **Docker** and **Docker Compose** installed.

1. Clone the repository
```bash
git clone https://github.com/your-username/stock-management-system.git
cd stock-management-system
```

2. Create a `.env` file in the root of the project
```env
DB_URL=jdbc:mysql://db:3306/stock_db?useTimezone=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_secret_key
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
```

3. Run the application
```bash
docker compose up --build
```

4. Access the API documentation: http://localhost:8080/swagger-ui.html


---

## 🚧 Project Status

| Feature | Status |
|---|---|
| Core REST API | ✅ Done |
| Authentication (JWT + Refresh Token) | ✅ Done |
| Role-based access control | ✅ Done |
| Input validation | ✅ Done |
| Exception handling | ✅ Done |
| Pagination | ✅ Done |
| API Documentation (Swagger) | ✅ Done |
| Analytics | ✅ Done |
| Docker | ✅ Done |
| Unit and integration tests | ⬜ Pending |
| Flyway migrations | ⬜ Pending |