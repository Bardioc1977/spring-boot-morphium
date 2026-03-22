# Contributing to Spring Boot Morphium

Thank you for your interest in contributing! This document provides guidelines and
information for contributors.

## How to Contribute

### Reporting Bugs

- Use [GitHub Issues](https://github.com/Bardioc1977/spring-boot-morphium/issues) to report bugs
- Include the Spring Boot version, Morphium version, and Java version
- Provide a minimal reproducer if possible
- Describe the expected and actual behavior

### Suggesting Features

- Open a [GitHub Issue](https://github.com/Bardioc1977/spring-boot-morphium/issues) describing the feature
- Explain the motivation and use case
- Discuss the proposed approach before starting implementation

### Submitting Pull Requests

1. Fork the repository and create a feature branch from `main`
2. Make your changes following the code conventions below
3. Add or update tests as appropriate
4. Update documentation if your change affects user-facing behavior
5. Ensure `mvn verify` passes locally
6. Open a pull request against `main`

## Development Setup

### Prerequisites

- JDK 21+
- Maven 3.9+

### Building from Source

```bash
# Build dependencies first (SNAPSHOT versions)
# 1. morphium 6.2.2-SNAPSHOT
# 2. morphium-jakarta-data 1.0.0-SNAPSHOT

# Then build this project
git clone https://github.com/Bardioc1977/spring-boot-morphium.git
cd spring-boot-morphium
mvn verify
```

### Running Tests

```bash
# All tests (uses InMemDriver, no Docker needed)
mvn test

# Only the autoconfigure module tests
mvn test -pl spring-boot-morphium-autoconfigure
```

## Code Conventions

- Java 21+ features are welcome (records, sealed classes, pattern matching, etc.)
- Follow existing code style (4-space indentation, no tabs)
- No `sun.*` or `jdk.internal.*` imports

## License

By contributing to this project, you agree that your contributions will be licensed
under the [Apache License 2.0](LICENSE).
