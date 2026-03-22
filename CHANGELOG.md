# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased] - 1.0.0-SNAPSHOT

### Added
- Spring Boot 3.4.4 auto-configuration for Morphium (`spring.morphium.*` properties)
- Jakarta Data 1.0 repository support via JDK dynamic proxies
  - `CrudRepository<T,K>` and `MorphiumRepository<T,K>`
  - Query derivation: `findBy*`, `countBy*`, `existsBy*`, `deleteBy*`
  - JDQL via `@Query` annotation
  - `@Find` / `@Delete` with `@By` parameter binding
  - Pagination (`Page<T>`, `CursoredPage<T>`, `PageRequest`)
  - Sorting (`Sort<T>`, `Order<T>`, `@OrderBy`)
  - Stream and async (`Stream<T>`, `CompletionStage<T>`) return types
- `@EnableMorphiumRepositories` annotation for repository scanning
- `@MorphiumTransactional` AOP aspect for declarative transactions
- Actuator health indicator (`/actuator/health` with Morphium connection details)
- `@MorphiumTest` composite test annotation (InMemDriver, no MongoDB required)
- Connection retry logic with linear backoff for transient failures
- SSL/TLS support via `spring.morphium.ssl.*` properties
