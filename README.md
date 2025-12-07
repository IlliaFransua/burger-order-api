# 🍔 burger-order-api

This project is a RESTful API for managing burger orders, built with **Spring Boot**.

## 🛠️ Quick Start

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/IlliaFransua/burger-order-api
   cd ./burger-order-api
   ```

2. **Database Configuration:**

   Open the file `src/main/resources/application-dev.properties` and replace the placeholders with your PostgreSQL credentials

   ```bash
   # Example: spring.datasource.url=jdbc:postgresql://localhost:5432/burger_order_db_dev
   spring.datasource.url=DB_URL
   spring.datasource.username=DB_USERNAME
   spring.datasource.password=DB_PASSWORD
   ```

   > Note: The application is configured to use the dev profile by default

3. **Run Tests:**

   ```bash
   mvn test
   ```

4. **Run the Application:**

   ```bash
   mvn spring-boot:run
   ```

After starting, you can interact with the API (for example, using Postman or cURL) through endpoints.

---

## 📦 Core Technologies (Short List)

* **Java 21**
* **Spring Boot 3.5.8**
* **Spring Boot Starter Web**
* **Spring Boot Starter Data JPA** (Hibernate)
* **PostgreSQL**
* **Liquibase** (Database Schema Migration)
* **Spring Boot Starter Security**
* **Spring Boot Starter Validation** (DTO Input Validation)
* **MapStruct 1.6.3** (DTO ↔ Entity Mapping)
* **Lombok**
* **Jackson Dataformats (CSV, JSR310)**
* **Spring Boot Starter Test** and **AssertJ**
* **Spring Security Test**
* **Spring Boot DevTools**

---

# 🌐 Endpoints

> When mentioning something like "Accepts an `OrderRequest` object and saves it", it means that OrderRequest will be passed in the request body in JSON format, then automatically converted to an OrderRequest class object by Spring MVC methods (Jackson).

## BurgerController

| Action                      | Endpoint           | HTTP Method | Purpose                                                                                                             |
|:----------------------------|:-------------------|:------------|:--------------------------------------------------------------------------------------------------------------------|
| **Get list** of all burgers | `/api/burger`      | `GET`       | Returns a full list of all burgers. (Note: There's a *TODO* in the code to change this to `StreamingResponseBody`). |
| **Create** a new burger     | `/api/burger`      | `POST`      | Accepts a `BurgerRequest` object and saves it. Returns 201 CREATED.                                                 |
| **Update** burger data      | `/api/burger/{id}` | `PUT`       | Updates a burger record by ID using `BurgerRequest`.                                                                |
| **Delete** a burger         | `/api/burger/{id}` | `DELETE`    | Deletes a burger record by ID. Returns 204 No Content.                                                              |

## OrderController

| Action                        | Endpoint             | HTTP Method | Purpose                                                                                                                            |
|:------------------------------|:---------------------|:------------|:-----------------------------------------------------------------------------------------------------------------------------------|
| **Create** a new Order record | `/api/order`         | `POST`      | Accepts an `OrderRequest` object and saves it. Returns 201 CREATED.                                                                |
| **Get** Order details         | `/api/order/{id}`    | `GET`       | Returns an `OrderResponse` object by the specified ID.                                                                             |
| **Update** Order data         | `/api/order/{id}`    | `PUT`       | Updates an Order record by ID using `OrderRequest`.                                                                                |
| **Delete** an Order record    | `/api/order/{id}`    | `DELETE`    | Deletes a record by ID. Returns 204 No Content.                                                                                    |
| **Get list (Pagination)**     | `/api/order/_list`   | `POST`      | Returns a page of `OrderResponse` records with sorting/page size options.                                                          |
| **Download report (CSV)**     | `/api/order/_report` | `POST`      | Generates a CSV report based on filter criteria (`FilterCriteriaRequest`) and sends it as a data stream (`StreamingResponseBody`). |
| **Upload files**              | `/api/order/upload`  | `POST`      | Accepts a binary data stream (CSV file) and processes it, returning upload statistics (`UploadStatsResponse`).                     |

---

## 🚨 GlobalExceptionHandler

| Exception Handled            | HTTP Response Code            | Purpose of Code                                                                                                                                                             | Logging                                             |
|:-----------------------------|:------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------|
| `DuplicateResourceException` | **409 CONFLICT**              | Indicates that the request cannot be completed due to a **conflict with the current state** of the resource (for example, trying to create a resource that already exists). | `WARN`, logs a message about the duplicate attempt. |
| `NotFoundResourceException`  | **404 NOT FOUND**             | Indicates that the requested resource **does not exist** or cannot be found.                                                                                                | `WARN`, logs URI and exception details.             |
| `TechnicalFailureException`  | **500 INTERNAL SERVER ERROR** | Indicates a **critical server error** that is not related to incorrect user input (for example, I/O error, database failure).                                               | `ERROR`, logs full exception stack trace.           |

---

# 💼 Services (Business Layer)

## BurgerService

| Method               | Purpose                            | Input Parameters                      | Returns                | Logic and Key Points                                                                                                                                                    | Exceptions                                                                                               |
|:---------------------|:-----------------------------------|:--------------------------------------|:-----------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------|
| **`createBurger`**   | Creates a new burger record in DB. | `BurgerRequest` (new burger data)     | `BurgerResponse`       | 1. **Checks for duplicates** by name (`existsByName`). 2. Maps DTO to Entity. 3. Saves to repository.                                                                   | `DuplicateResourceException` (if name already exists)                                                    |
| **`findAllBurgers`** | Returns a list of all burgers.     | None                                  | `List<BurgerResponse>` | 1. Retrieves all records (`findAll()`). 2. Maps to DTO list.                                                                                                            | None                                                                                                     |
| **`updateBurger`**   | Updates an existing burger by ID.  | `Long id`, `BurgerRequest` (new data) | `BurgerResponse`       | 1. **Finds** burger by ID. 2. **Checks for duplicates** by new name (if name was changed). 3. Uses mapper to update Entity with data from DTO. 4. Saves updated Entity. | `NotFoundResourceException` (if ID not found), `DuplicateResourceException` (if new name already exists) |
| **`deleteBurger`**   | Deletes a burger by ID.            | `Long id`                             | `void`                 | 1. **Finds** burger by ID. 2. Deletes the found object.                                                                                                                 | `NotFoundResourceException` (if ID not found)                                                            |

## OrderService

| Method                       | Purpose                                           | Input Parameters                        | Returns               | Logic and Key Points                                                                                                                                                                                                                                                     | Exceptions                                                                              |
|:-----------------------------|:--------------------------------------------------|:----------------------------------------|:----------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------|
| **`createOrder`**            | Creates a new order.                              | `OrderRequest`                          | `OrderResponse`       | 1. **Validates** all burger IDs (`findAndValidateBurgers`). 2. Maps DTO to Entity, sets found burgers and creation time. 3. Saves to repository.                                                                                                                         | `NotFoundResourceException` (if burger(s) not found)                                    |
| **`findOrder`**              | Returns an order by ID.                           | `Long id`                               | `OrderResponse`       | 1. **Finds** order (`findById` with `EntityGraph` for burgers). 2. Throws exception if not found.                                                                                                                                                                        | `NotFoundResourceException` (if ID not found)                                           |
| **`updateOrder`**            | Updates an order (burger list).                   | `Long id`, `OrderRequest`               | `OrderResponse`       | 1. **Validates** new burger IDs. 2. **Finds** existing order. 3. Sets new burger list.                                                                                                                                                                                   | `NotFoundResourceException` (if order ID or burger IDs not found)                       |
| **`deleteOrder`**            | Deletes an order by ID.                           | `Long id`                               | `void`                | 1. **Finds** order. 2. Deletes it.                                                                                                                                                                                                                                       | `NotFoundResourceException` (if ID not found)                                           |
| **`getPaginatedOrders`**     | Returns a page of orders.                         | `Pageable`                              | `Page<OrderResponse>` | Uses repository to get a page (`findAll(pageable)`) and maps content to DTO.                                                                                                                                                                                             | None                                                                                    |
| **`generateReport`**         | Generates a CSV report and writes it to a stream. | `FilterCriteriaRequest`, `OutputStream` | `void`                | 1. Uses **Jackson CsvMapper** for serialization. 2. **Streams** orders from database (`orderRepository.findOrdersByFilter`). 3. For each order, formats burger list to string and writes it to `OutputStream`.                                                           | `TechnicalFailureException` (I/O errors during generation/writing)                      |
| **`uploadOrders`**           | Imports new orders from input stream (CSV file).  | `InputStream`                           | `UploadStatsResponse` | 1. Uses **Jackson CsvMapper** (`orderReader`) to read DTOs iteratively (`OrderRequest`). 2. In a loop, calls **`createOrder`** for each record. 3. Handles parsing or business logic errors (throws `RuntimeException`) and keeps count of successful/failed operations. | `TechnicalFailureException` (if failure occurs during initialization/reading of stream) |
| **`findAndValidateBurgers`** | **Private method.** Finds and validates burgers.  | `List<Long> requestBurgerIds`           | `List<Burger>`        | Checks if the number of found burgers matches the number of requested IDs.                                                                                                                                                                                               | `NotFoundResourceException` (if any burger not found)                                   |

---

# 📊 Entities

## Burger

> This entity represents the `burger` table and contains information about an individual item.

| Field           | Data Type (Java) | JPA/Validation Annotations                               | Purpose and Constraints                                                           |
|:----------------|:-----------------|:---------------------------------------------------------|:----------------------------------------------------------------------------------|
| **`id`**        | `Long`           | `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)` | **Primary key.** Unique burger identifier, value is auto-generated.               |
| **`name`**      | `String`         | `@NotBlank`, `@Size(min = 5)`                            | **Name** of the burger. Cannot be empty and must be at least 5 characters.        |
| **`unitPrice`** | `BigDecimal`     | `@NotNull`                                               | **Price** per unit. Uses `BigDecimal` for precise representation of money values. |

## Order

> This entity represents the `ORDERS` table and contains information about an order, as well as a "many-to-many" relationship with burgers.

| Field           | Data Type (Java) | JPA/Validation Annotations                               | Purpose and Relationship                                                                                                                               |
|:----------------|:-----------------|:---------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`id`**        | `Long`           | `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)` | **Primary key.** Unique order identifier, value is auto-generated.                                                                                     |
| **`createdAt`** | `Instant`        | `@NotNull`                                               | **Creation time** of the order. Uses `Instant` to store moment in time in UTC.                                                                         |
| **`burgers`**   | `List<Burger>`   | `@ManyToMany`, `@NotEmpty`                               | **Relationship.** Order contains a list of burgers. This is a **many-to-many** relationship (`ManyToMany`) that requires an intermediate (join) table. |

---

# 🗄️ Repository (Only Custom Methods)

## BurgerRepository

| Method              | Returns        | Purpose                                                                         | JPA/Spring Data Logic                                                                                                                   |
|:--------------------|:---------------|:--------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------|
| **`existsByName`**  | `boolean`      | Checks if a `Burger` record with the specified **name** exists in the database. | Spring Data JPA automatically generates SQL query `SELECT CASE WHEN COUNT(id) > 0 THEN true ELSE false END FROM burger WHERE name = ?`. |
| **`findAllByIdIn`** | `List<Burger>` | Finds and returns a list of all `Burger` whose IDs are in the provided list.    | Spring Data JPA generates SQL query `SELECT * FROM burger WHERE id IN (?)`.                                                             |

## OrderRepository

| Method                   | Returns           | Purpose                                                                             | JPA/Spring Data Logic and Features                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|:-------------------------|:------------------|:------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`findById`**           | `Optional<Order>` | Finds an order by ID.                                                               | **Optimization (`@EntityGraph`)**: Overrides the standard `findById` method to use **`EntityGraph`**. This ensures that related entities (the `burgers` collection) are **loaded immediately** together with the order itself, avoiding the "N+1 select problem".                                                                                                                                                                                                                                                                                  |
| **`findOrdersByFilter`** | `Stream<Order>`   | Performs flexible filtering of orders by creation time range and/or by burger name. | **Native query (`@Query`, `nativeQuery = true`)**: Uses a complex native SQL query (PostgreSQL) for: 1. **Dynamic filtering**: Conditions `(CAST(:param AS type) IS NULL OR ...)` allow ignoring parameters if they are passed as `null`. 2. **Search**: Uses `LOWER(b.name) LIKE LOWER(CONCAT('%', ..., '%'))` for case-insensitive search by part of burger name. 3. **Streaming**: Returning `Stream<Order>` allows processing large results efficiently without loading them all into memory at once (critical for report generation feature). |

---

# 🔄 Mapper

## BurgerMapper

| Method                        | Input Type                | Output Type                  | Purpose                                                                | Key Features                                                                                           |
|:------------------------------|:--------------------------|:-----------------------------|:-----------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------|
| **`toBurger`**                | `BurgerRequest` (DTO)     | `Burger` (Entity)            | Converts input data for creating a new entity.                         | -                                                                                                      |
| **`toResponse`**              | `Burger` (Entity)         | `BurgerResponse` (DTO)       | Converts entity from DB to response object.                            | -                                                                                                      |
| **`toResponseList`**          | `List<Burger>` (Entity)   | `List<BurgerResponse>` (DTO) | Converts list of entities to list of DTOs.                             | -                                                                                                      |
| **`updateBurgerFromRequest`** | `BurgerRequest`, `Burger` | `void`                       | **Updates** existing entity (`Burger`) with data from `BurgerRequest`. | Uses `@MappingTarget` to indicate the object that needs to be changed (mutated), not create a new one. |

## OrderMapper

| Method               | Input Type             | Output Type                 | Purpose                                                                  |
|:---------------------|:-----------------------|:----------------------------|:-------------------------------------------------------------------------|
| **`toOrder`**        | `OrderRequest` (DTO)   | `Order` (Entity)            | Converts input data from client (DTO) to entity ready for saving in DB.  |
| **`toResponse`**     | `Order` (Entity)       | `OrderResponse` (DTO)       | Converts entity from DB to response object returned to client.           |
| **`toResponseList`** | `List<Order>` (Entity) | `List<OrderResponse>` (DTO) | Converts list of entities (for example, for pagination) to list of DTOs. |