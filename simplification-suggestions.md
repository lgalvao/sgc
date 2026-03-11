# Simplification Suggestions for SGC

This document outlines strategies for simplifying the SGC codebase, reducing overengineering, and minimizing fragmentation. These recommendations are specifically tailored for a small-scale intranet application expected to handle at most 5-10 simultaneous users. Guidelines typically applied to highly-scalable, multilayered, or super-modularized systems are intentionally avoided to prioritize maintainability, readability, and development speed.

## Architectural Priorities

1. **Monolithic Architecture**: Maintain a cohesive monolith. Avoid splitting the application into microservices or fragmented Gradle subprojects.
2. **Procedural Services**: Favor procedural code in Services over complex design patterns (e.g., Strategy, Command, Visitor) unless the complexity is strictly necessary for the business logic.
3. **Concrete Classes**: Avoid single-implementation interfaces. If an interface is only implemented by one class, remove the interface and use the concrete class directly.
4. **Direct Controller-to-Service Communication**: Remove redundant intermediary layers.

## Backend Simplifications

1. **Remove Redundant Facades**:
   - Facade classes (e.g., `OrganizacaoFacade`, `ProcessoFacade`, `SubprocessoFacade`, `MapaFacade`, `AtividadeFacade`, `UsuarioFacade`, `AlertaFacade`, `RelatorioFacade`, `LoginFacade`, `PainelFacade`) that merely delegate calls from Controllers to Services add unnecessary boilerplate.
   - **Action**: Inject Services directly into Controllers.

2. **Consolidate Business Logic**:
   - Move business validations and logic out of Controllers.
   - **Action**: Controllers should be thin, focusing solely on HTTP request/response handling, routing, and basic input validation (via annotations). All business rules and complex validations must reside in the Service layer.

3. **Simplify DTO Mapping**:
   - Avoid creating separate DTOs for simple read operations where the entity structure is sufficient and does not expose sensitive information.
   - **Action**: Return entities directly for simple queries, minimizing the need for mapping boilerplate. Only use DTOs when necessary to shape the response for specific UI needs, aggregate data, or hide sensitive fields.

## Frontend Simplifications

1. **Minimize Pinia State Management**:
   - Many Pinia stores currently act as unnecessary pass-throughs, merely caching API data without managing complex global state. Examples include `mapas.ts`, `atividades.ts`, `subprocessos.ts`, `processos.ts`, and `usuarios.ts`.
   - **Action**: Remove these pass-through stores. Replace them with standard Vue `refs` or composables that call Services directly.
   - **Action**: Reserve Pinia exclusively for true global state (e.g., user authentication, session data, UI notifications/toasts).

2. **Eliminate Redundant Wrapper Components**:
   - Avoid creating frontend components that only proxy props and events to underlying components (e.g., wrapping a simple BootstrapVueNext component without adding substantial value or specific logic).
   - **Action**: Use the base components directly or consolidate the logic into fewer, more meaningful components.

## Development Practices

- **Avoid Overengineering**: Always evaluate if a pattern or abstraction is necessary for the current scale and requirements. If it only serves "future-proofing" for scenarios unlikely to occur in this specific intranet context, remove it.
- **Code Cleanliness**: Remove conversational AI artifacts, obvious comments that just repeat code logic, redundant Javadoc/JSDoc tags, and visual separator lines. The code should be self-documenting as much as possible.

### Avoid Over-layered Architecture
- **Do not use Hexagonal/Onion Architecture**: For a small intranet application, strictly segregating domain models from frameworks is unnecessary and counterproductive. It is perfectly acceptable and encouraged to use Spring Data JPA annotations (`@Entity`, `@Table`, etc.) directly on domain models.
- **Direct Repository Access**: For simple CRUD operations with no additional business logic, Controllers can safely inject and use Spring Data Repositories directly, skipping the Service layer entirely to reduce boilerplate.

### Simplify Object Creation
- **Avoid Builders and Factories for Simple Objects**: Remove classes like `PdfFactory` or `ProcessoDetalheBuilder` if they merely instantiate objects or assemble them straightforwardly. Use constructors or static factory methods on the entity/DTO itself.

### Frontend Component Granularity
- **Avoid Over-modularization**: Do not create highly granular Vue components for trivial UI elements (e.g., creating a dedicated component for a single standard button or input field) unless it encapsulates significant reusable logic or specific styling that cannot be achieved with standard BootstrapVueNext components or utility classes.
