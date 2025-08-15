# MyDI — Lightweight Java Dependency Injection Container

MyDI is a lightweight, annotation-driven dependency injection container inspired by Spring.
It supports **component scanning**, **bean creation**, **constructor injection**, **field injection**, **singleton/prototype scopes**, and **lifecycle callbacks** like `@PostConstruct`.

---

## Features

- **Annotation-based DI**
    - `@Component` — Marks a class as a managed component.
    - `@Bean` — Defines factory methods for producing beans.
    - `@Inject` — Marks fields for field-based dependency injection.
    - `@Singleton` — Declares a singleton-scoped bean.
    - `@PostConstruct` — Lifecycle callback after initialization.

- **Injection types**
    1. **Bean method injection** — Uses a `@Bean`-annotated method inside of `@Config`-annotated class to create an instance.
    2. **Constructor injection** — Uses an `@Autowired` (or primary) constructor.
    3. **Field injection** — Sets fields annotated with `@Inject`.

- **Interface resolution**
    - Automatic detection and resolution of interface implementations.
    - Supports multiple implementations with a primary marker.

- **Singleton caching**
    - Caches instances for singleton-scoped components.
    - Prototype-scoped beans are created each time.

- **Post-construction lifecycle hooks**
    - Executes methods annotated with `@PostConstruct` after injection.

---

### `DIContainer`
The **core container** responsible for:
- Scanning packages for components (`ClassPathScanner`)
- Extracting metadata (`MetadataExtractor`)
- Storing **component metadata** (`ComponentMetadata`) and **bean metadata** (`BeanMetadata`)
- Creating and caching instances
- Calling lifecycle methods

#### Main methods:
- `scanAndProcessComponents(...)` — Scans the target package for components and beans, builds internal metadata maps.
- `createUsingBean(...)` — Creates objects from `@Bean` methods.
- `createUsingFieldInjection(...)` — Creates objects and injects fields.
- `createUsingConstructorInjection(...)` — Creates objects using constructor injection.
- `createInstance(...)` — Attempts all creation methods in order until one succeeds.

### Example Usage

```java
@Component
@Singleton
public class MyService {

    @Inject
    private MyRepository repository;

    @PostConstruct
    public void init() {
        System.out.println("MyService initialized!");
    }
}

@Component
public class MyRepository { }

public class Main {
    public static void main(String[] args) {
        DIContainer container = new DIContainer();
        container.scanAndProcessComponents(Main.class);

        MyService service = (MyService) container.createInstance(MyService.class);
    }
}