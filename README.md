# Spring Boot Morphium Starter

[![Build](https://github.com/Bardioc1977/spring-boot-morphium/actions/workflows/build.yml/badge.svg)](https://github.com/Bardioc1977/spring-boot-morphium/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://adoptium.net)
[![Jakarta Data](https://img.shields.io/badge/Jakarta%20Data-1.0-green)](https://jakarta.ee/specifications/data/1.0/)

A [Spring Boot](https://spring.io/projects/spring-boot) auto-configuration for
[Morphium](https://github.com/sboesebeck/morphium), an actively maintained MongoDB ORM
for Java -- with full **Jakarta Data 1.0** repository support.

> **Companion project:** See [quarkus-morphium](https://github.com/Bardioc1977/quarkus-morphium)
> for Quarkus integration with the same Jakarta Data feature set.

---

## Features

- **Auto-configuration** -- `Morphium` bean created from `spring.morphium.*` properties
- **Jakarta Data repositories** -- `@Repository` interfaces with JDK dynamic proxies (runtime)
- **Query derivation** -- `findBy*`, `countBy*`, `existsBy*`, `deleteBy*` with And/Or, Between, In, Like, etc.
- **JDQL** -- `@Query("WHERE status = :s ORDER BY name")` Jakarta Data Query Language
- **@Find / @Delete** -- explicit field binding via `@By` parameters
- **Transactions** -- `@MorphiumTransactional` with AOP-based commit/rollback
- **Actuator health** -- Morphium connection status in `/actuator/health`
- **Test support** -- `@MorphiumTest` composite annotation with InMemDriver (no MongoDB needed)
- **MorphiumRepository** -- escape hatch for `distinct()`, `query()`, `morphium()` access

---

## Prerequisites

| Dependency | Minimum version |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.x |
| Morphium | 6.2.2 ([sboesebeck/morphium](https://github.com/sboesebeck/morphium)) |

## Installation

Add the starter to your `pom.xml`:

```xml
<dependency>
    <groupId>de.caluga</groupId>
    <artifactId>spring-boot-morphium-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> **Note:** Until published to Maven Central, build locally:
> ```bash
> git clone https://github.com/Bardioc1977/spring-boot-morphium.git
> cd spring-boot-morphium
> mvn install -DskipTests
> ```

## Quick Start

### 1. Configure

```properties
# application.properties
spring.morphium.database=my-database
spring.morphium.hosts=localhost:27017
```

### 2. Define an entity

```java
@Entity(collectionName = "products")
public class Product {
    @Id private MorphiumId id;
    private String name;
    private double price;
    private String category;

    // getters, setters, constructors
}
```

### 3. Create a repository

```java
@Repository
public interface ProductRepository extends MorphiumRepository<Product, MorphiumId> {

    List<Product> findByCategory(String category);

    List<Product> findByPriceGreaterThan(double minPrice);

    long countByCategory(String category);

    @Query("WHERE category = :cat AND price > :minPrice ORDER BY price")
    List<Product> findExpensive(@Param("cat") String category,
                                @Param("minPrice") double minPrice);
}
```

### 4. Enable and inject

```java
@SpringBootApplication
@EnableMorphiumRepositories
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

```java
@Service
public class ProductService {

    @Autowired ProductRepository products;

    public List<Product> findExpensive(double minPrice) {
        return products.findByPriceGreaterThan(minPrice);
    }
}
```

---

## Jakarta Data Repository Support

| Feature | Details |
|---------|---------|
| **CRUD** | `CrudRepository<T,K>`, `MorphiumRepository<T,K>` -- save, insert, update, delete, findById, findAll |
| **Query derivation** | `findBy`, `countBy`, `existsBy`, `deleteBy` with operators: Equals, Not, GreaterThan, LessThan, Between, In, NotIn, Like, StartsWith, EndsWith, Null, NotNull, True, False -- combined with And/Or |
| **@Find + @By** | Explicit field binding via parameter annotations |
| **@Query (JDQL)** | Jakarta Data Query Language with WHERE, ORDER BY, named parameters, BETWEEN, IN, LIKE, IS NULL, NOT, GROUP BY, HAVING, aggregates |
| **@OrderBy** | Static sort annotation on query methods |
| **Pagination** | `Page<T>`, `PageRequest`, `CursoredPage<T>` (keyset pagination) |
| **Sorting** | `Sort<T>`, `Order<T>` as method parameters |
| **Stream** | `Stream<T>` return type for large result sets |
| **Async** | `CompletionStage<T>` return type for non-blocking operations |

### MorphiumRepository -- The Escape Hatch

`MorphiumRepository<T,K>` extends `CrudRepository` with Morphium-specific operations:

```java
// Distinct values for a field
List<Object> categories = products.distinct("category");

// Direct access to the Morphium API
products.morphium().inc(product, "stock", 5);

// Create a typed Morphium Query
Query<Product> q = products.query();
q.f("price").gt(100).f("category").eq("electronics");
```

---

## Configuration Reference

| Property | Default | Description |
|---|---|---|
| `spring.morphium.database` | *(required)* | MongoDB database name |
| `spring.morphium.hosts` | `localhost:27017` | Comma-separated `host:port` list |
| `spring.morphium.username` | -- | MongoDB username |
| `spring.morphium.password` | -- | MongoDB password |
| `spring.morphium.auth-database` | `admin` | Authentication database |
| `spring.morphium.atlas-url` | -- | MongoDB Atlas SRV URL (overrides `hosts`) |
| `spring.morphium.replica-set-name` | -- | Replica set name (required for transactions) |
| `spring.morphium.read-preference` | `primary` | Read preference |
| `spring.morphium.max-connections` | `250` | Connection pool size |
| `spring.morphium.driver-name` | `PooledDriver` | `PooledDriver` (production) or `InMemDriver` (tests) |
| `spring.morphium.connect-retries` | `5` | Connection retry attempts on transient failures |
| `spring.morphium.index-check` | `CREATE_ON_STARTUP` | `CREATE_ON_STARTUP`, `WARN_ON_STARTUP`, `CREATE_ON_WRITE_NEW_COL`, `NO_CHECK` |
| `spring.morphium.cache.global-valid-time` | `5000` | Cache TTL in milliseconds |
| `spring.morphium.cache.read-cache-enabled` | `true` | Enable query result cache |
| `spring.morphium.ssl.enabled` | `false` | Enable TLS |
| `spring.morphium.ssl.keystore-path` | -- | Keystore path (JKS/PKCS12) |
| `spring.morphium.ssl.keystore-password` | -- | Keystore password |

## Transactions

Requires a MongoDB replica set or Atlas.

```java
@Service
public class OrderService {

    @Autowired Morphium morphium;

    @MorphiumTransactional
    public void placeOrder(Order order, Payment payment) {
        morphium.store(order);
        morphium.store(payment);
        // auto-commit on success, auto-rollback on exception
    }
}
```

## Actuator Health

When `spring-boot-actuator` is on the classpath, a Morphium health indicator is
automatically registered at `/actuator/health`:

```json
{
  "status": "UP",
  "components": {
    "morphium": {
      "status": "UP",
      "details": {
        "database": "my-database",
        "driver": "PooledDriver",
        "replicaSet": true,
        "replicaSetName": "rs0"
      }
    }
  }
}
```

## Testing

### Option A: InMemDriver (no MongoDB required)

```properties
# src/test/resources/application-test.properties
spring.morphium.database=test
spring.morphium.driver-name=InMemDriver
```

```java
@SpringBootTest
@ActiveProfiles("test")
@EnableMorphiumRepositories
class ProductRepositoryTest {

    @Autowired ProductRepository repository;

    @Test
    void shouldFindByCategory() {
        repository.save(new Product("Widget", 9.99, "tools"));

        var results = repository.findByCategory("tools");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Widget");
    }
}
```

### Option B: @MorphiumTest annotation

The `spring-boot-morphium-test` module provides a composite annotation:

```xml
<dependency>
    <groupId>de.caluga</groupId>
    <artifactId>spring-boot-morphium-test</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

```java
@MorphiumTest
@EnableMorphiumRepositories
class ProductRepositoryTest {

    @Autowired ProductRepository repository;

    @Test
    void shouldFindByCategory() {
        // InMemDriver is auto-configured
    }
}
```

## Module Structure

```
spring-boot-morphium/
  spring-boot-morphium-autoconfigure/   Auto-configuration, repository proxy, AOP, health
  spring-boot-morphium-starter/         Dependency-only POM (pull this in your app)
  spring-boot-morphium-test/            @MorphiumTest annotation for test support
```

## Architecture

This starter uses **JDK dynamic proxies** at runtime (the standard Spring Data pattern),
in contrast to the [quarkus-morphium](https://github.com/Bardioc1977/quarkus-morphium)
extension which uses **Gizmo bytecode generation** at build time.

Both share the same query engine via the
[morphium-jakarta-data](https://github.com/Bardioc1977/morphium-jakarta-data) module --
a framework-agnostic library containing all Jakarta Data query derivation, JDQL parsing,
pagination, and CRUD logic.

```
morphium (core ODM)
  └── morphium-jakarta-data (shared Jakarta Data runtime)
        ├── spring-boot-morphium (this project, JDK proxies)
        └── quarkus-morphium (Gizmo bytecode, build-time)
```

## Building from Source

```bash
# Requires morphium 6.2.2-SNAPSHOT and morphium-jakarta-data 1.0.0-SNAPSHOT
# in your local Maven repository

mvn clean install

# Run tests only
mvn test -pl spring-boot-morphium-autoconfigure
```

## Related Projects

- [Morphium](https://github.com/sboesebeck/morphium) -- the underlying MongoDB ORM
- [morphium-jakarta-data](https://github.com/Bardioc1977/morphium-jakarta-data) -- shared Jakarta Data runtime
- [quarkus-morphium](https://github.com/Bardioc1977/quarkus-morphium) -- Quarkus CDI extension (same Jakarta Data features)
- [quarkus-morphium-showcase](https://github.com/Bardioc1977/quarkus-morphium-showcase) -- interactive demo
- [Jakarta Data 1.0](https://jakarta.ee/specifications/data/1.0/) -- the specification

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md).

## License

[Apache License 2.0](LICENSE)
