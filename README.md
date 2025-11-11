
# Bank Cards Management System

## Описание
Веб-приложение для управления банковскими картами, реализованное на Java Spring Boot.  
Приложение поддерживает регистрацию пользователей, аутентификацию через JWT, ролевую модель (ADMIN / USER), работу с банковскими картами и переводы между ними.  
Система использует PostgreSQL и Liquibase для миграций базы данных. Проект полностью контейнеризирован с помощью Docker Compose.

---

## Запуск проекта

### Вариант 1. Через Docker
1. Убедитесь, что установлены **Docker** и **Docker Compose**.
2. В корне проекта выполните:
   ```bash
   docker compose up --build


3. Приложение будет доступно по адресу:

   ```
   http://localhost:8080/swagger-ui.html
   ```

### Вариант 2. Локально (без Docker)

1. Установите PostgreSQL и создайте базу данных `bank_db`.
2. Проверьте настройки в `src/main/resources/application.yml`:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/bank_db
       username: postgres
       password: admin
   ```
3. Выполните команду:

   ```bash
   mvn spring-boot:run
   ```

---

## Роли и доступ

| Роль      | Возможности                                                                         |
| --------- | ----------------------------------------------------------------------------------- |
| **ADMIN** | Управляет пользователями и картами, может блокировать, активировать и удалять карты |
| **USER**  | Просматривает свои карты, делает переводы, запрашивает блокировку, смотрит баланс   |

---

## Функционал

* Регистрация и вход через JWT
* CRUD-операции для карт
* Переводы между своими картами
* Маскирование номеров карт
* Ролевой доступ (Spring Security)
* Миграции через Liquibase
* Документация через Swagger UI
* Запуск в Docker Compose

---

## Основные API эндпоинты

Аутентификация:

```
POST /auth/register — регистрация
POST /auth/login — вход
```

Работа с картами:

```
GET /api/v1/cards — получить список карт
POST /api/v1/cards — создать карту
PATCH /api/v1/cards/{id}/block — заблокировать карту
PATCH /api/v1/cards/{id}/activate — активировать карту
DELETE /api/v1/cards/{id} — удалить карту
```

Переводы:

```
POST /api/v1/cards/transfer — перевод между своими картами
```

Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

## Используемые технологии

* Java 17
* Spring Boot 3
* Spring Security + JWT
* Spring Data JPA
* PostgreSQL
* Liquibase
* Docker / Docker Compose
* Swagger / OpenAPI

---

## Миграции

Миграции выполняются автоматически при старте приложения.
Основной файл миграций:

```
src/main/resources/db/migration/db.changelog-master.yaml
```

---

## Автор

**Имя:** Nikita Soldatov



