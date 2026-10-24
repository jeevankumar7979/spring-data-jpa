# One-to-Many Relationship

A Spring Boot learning application demonstrating Spring Data JPA with PostgreSQL. It models students and their addresses, then exposes endpoints for searching, paging, sorting, and creating or updating students.

## Technology

- Java 21
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Gradle

## Requirements and setup

1. Install Java 21 and PostgreSQL.
2. Create or select a PostgreSQL database and configure the connection in `src/main/resources/application.yaml`. The checked-in configuration targets `localhost:5432/postgres` and uses the `jeevankumar` database user; provide a password if your PostgreSQL account requires one. Do not commit real credentials.
3. Start the application:

   ```bash
   ./gradlew bootRun
   ```

   On Windows, use `gradlew.bat bootRun`.

Hibernate's `ddl-auto: update` setting updates the schema to match the entities, and `show-sql: true` prints SQL statements to the application log. `update` is useful for this learning project; production deployments should use managed schema migrations instead.

## Entity relationship

`Student` and `Address` are JPA entities mapped to the `student` and `address` tables. Both IDs use `@GeneratedValue(strategy = GenerationType.IDENTITY)`, so PostgreSQL assigns IDs when rows are inserted. Leave IDs unset when creating new entities.

The relationship is bidirectional:

- `Student.addresses` is the inverse, one-to-many side. `mappedBy = "student"` points to the `student` property on `Address`.
- `Address.student` is the owning, many-to-one side. Its `@JoinColumn(name = "student_id")` maps the foreign-key column in `address`.
- `cascade = CascadeType.ALL` on `Student.addresses` propagates JPA operations (including persist and remove) from a student to its addresses.

JPA cascades apply when operations go through the persistence context, such as `studentRepository.deleteById(id)`. They do **not** cause PostgreSQL to cascade deletes issued directly in a SQL client. For direct SQL deletes, delete dependent address rows first or define the foreign key with `ON DELETE CASCADE`.

## Fetching behavior

The current mapping explicitly sets `Student.addresses` to `FetchType.EAGER`, and `Address.student` to `FetchType.EAGER`.

- **EAGER** means the association must be available when the entity is returned. Hibernate may use a join or additional SQL queries to load it; the fetch type does not guarantee one particular SQL statement.
- **LAZY** means the association can be loaded when first accessed. For a one-to-many collection, this is generally the preferred default when callers do not always need the related rows. Access it while the persistence context is open, or arrange an explicit fetch strategy.

`FetchTypeDemo` runs at application startup. It inserts a student named `Fetch type demo` with two addresses (`Bangalore` and `Chennai`), clears the persistence context, reloads the student, and accesses the address collection. This generates sample rows **on every application start** and does not remove them. Keep this in mind when inspecting the database; disable the demo before using the app outside local learning.

When eager fetching is enabled, Hibernate may log a query like:

```sql
select s.id, s.name, a.student_id, a.id, a.city_name
from student s
left join address a on s.id = a.student_id
where s.id = ?
```

The left join keeps a matching student in the result even when it has no addresses; the address columns are then `NULL`. A student with multiple addresses can appear in multiple SQL result rows, which Hibernate assembles into one student with a collection of addresses.

## HTTP API

The base URL is `http://localhost:8080/api/v1/students`.

### List students with pagination and sorting

```http
GET /api/v1/students?page=0&size=2&sortBy=name&ascending=false
```

All parameters are optional:

| Parameter | Default | Meaning |
|---|---:|---|
| `page` | `0` | Zero-based page index |
| `size` | `10` | Maximum number of results on a page |
| `sortBy` | `id` | Student entity property to sort by, such as `id` or `name` |
| `ascending` | `true` | `true` for ascending order; `false` for descending |

The response is a Spring Data `Page<Student>` containing the page content and pagination metadata.

### Find students by name and address city

```http
GET /api/v1/students?name=Jeevan&cityName=Bangalore
```

Both query parameters are required. The controller uses the presence of `name` and `cityName` to route this request to the search method rather than the paginated list method.

The repository method executes a native SQL query:

```sql
SELECT DISTINCT s.*
FROM student s
JOIN address a ON a.student_id = s.id
WHERE s.name = :name
  AND a.city_name = :cityName
```

The inner join returns students that have a matching address in the requested city. `DISTINCT` prevents duplicate student rows if more than one address matches.

### Create or update a student (upsert-style)

```http
POST /api/v1/students/upsert
Content-Type: application/json
```

Create a student by omitting `id`:

```json
{
  "name": "Jeevan"
}
```

Update the name of an existing student by including its ID:

```json
{
  "id": 6,
  "name": "Jeevan Kumar"
}
```

The service checks a supplied ID. If it exists, it updates that student's name. If no ID is supplied, it creates a student. If a supplied ID does not exist, the service clears it and creates a new student with a database-generated ID. This endpoint only updates the student's name; it does not reconcile address changes from the request body.

This is an application-defined upsert policy, not an atomic database upsert. In a concurrent production system, add suitable uniqueness constraints and use a database-native upsert or another concurrency-safe strategy as appropriate. `POST` is used because the endpoint can create a resource whose ID is assigned by the server.

## Spring Data JPA concepts shown

- `JpaRepository<Student, Long>` supplies common CRUD methods, including `save`, `findById`, `deleteById`, and paginated `findAll`.
- The service layer (`StudentService`) keeps persistence decisions out of the controller and accepts a `Pageable` for list operations.
- `@Transactional` on the upsert service method groups its lookup and update/create logic in a transaction.
- `@Query(nativeQuery = true)` allows a repository method to execute SQL directly against PostgreSQL table and column names. `@Param` binds method arguments to named query parameters.
- `PageRequest` and `Sort` translate HTTP query parameters into Spring Data pagination and ordering.
- `@RequestParam` reads search and paging criteria from a URL query string; `@RequestBody` deserializes the JSON request body.
- HTTP methods communicate intent: `GET` reads data without changing it; `POST` submits data that may create or change state.

## Project layout

```text
src/main/java/com/jeevankumar/onetomanyrelationship/
├── AddressRepository.java
├── FetchTypeDemo.java
├── OneToManyRelationshipApplication.java
├── StudentRepository.java
├── StudentService.java
└── entity/
    ├── Address.java
    └── Student.java
```
