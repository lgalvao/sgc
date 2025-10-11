# SGC (Sistema de Gestão de Competências) - Agent Guide

This document provides a high-level overview of the SGC backend, its architecture, and key design patterns.

## 1. Project Overview

SGC is a Spring Boot application designed to manage organizational competencies. It orchestrates complex workflows involving multiple organizational units to create, review, and approve "Competency Maps".

## 2. Core Architecture

The application follows a **modular, service-oriented, and event-driven architecture**. Each Java package in `sgc.*` represents a distinct business domain or module.

### Key Architectural Principles:

- **Service-Oriented:** Business logic is encapsulated in specialized services (e.g., `ProcessoService`, `ImpactoMapaService`). This promotes separation of concerns and reusability.
- **Event-Driven:** The `processo` module acts as a central orchestrator that publishes domain events (e.g., `EventoProcessoIniciado`). Other modules, like `alerta` and `notificacao`, subscribe to these events to perform actions (like sending notifications) without being tightly coupled to the orchestrator.
- **Data Transfer Objects (DTOs):** The API layer (`*Controle.java` classes) uses DTOs extensively to decouple the web interface from the internal JPA entity models. This enhances security and flexibility.
- **Transactional Integrity:** Complex operations, especially in the `processo` and `mapa` services, are wrapped in `@Transactional` blocks to ensure atomicity. If any part of the operation fails, the entire transaction is rolled back, maintaining data consistency.

## 3. Key Modules (Packages)

- **`processo`**: The main orchestrator. It manages high-level initiatives (`Processo` entity), such as a company-wide competency mapping. It initiates workflows, creates `Subprocessos` for each participating unit, and publishes events to trigger alerts and notifications. It also uses a **Snapshot** pattern (`UnidadeProcesso` entity) to preserve the state of units at the start of a process for historical integrity.

- **`subprocesso`**: The workflow engine. It acts as a **State Machine** for the tasks assigned to individual units. It manages the detailed lifecycle of a task (e.g., `PENDENTE`, `EM_ELABORACAO`, `HOMOLOGADO`) and creates a detailed **Audit Trail** for every action using the `Movimentacao` entity.

- **`mapa`**: Manages the core artifact, the "Mapa de Competências". This module contains complex business logic for creating, saving, validating, and copying maps. It features specialized services like `CopiaMapaService` for cloning maps and `ImpactoMapaService` for analyzing the impact of changes.

- **`unidade`**: Defines the organizational hierarchy (`Unidade` entity). It is primarily a data model package, serving as the "source of truth" for the organizational structure. Its data is intended to be synchronized from an external system via the `sgrh` module.

- **`competencia`, `atividade`, `conhecimento`**: These packages manage the building blocks of a competency map. `Atividade` represents a task, `Conhecimento` a skill needed for that task, and `Competencia` groups them together.

- **`analise`**: Captures justifications and observations (`AnaliseCadastro`, `AnaliseValidacao`) made during the review and approval stages of the workflow.

- **`alerta` & `notificacao`**: These are listener modules. They subscribe to events published by `ProcessoService` to create in-system alerts and send emails, respectively. This demonstrates the decoupled nature of the architecture.

- **`sgrh`**: An integration layer for an external Human Resources system. It is designed to be read-only and is responsible for populating user and organizational unit data. It is currently implemented with mock data.

- **`comum`**: A cross-cutting module containing shared components like base entities, global configurations (`WebConfig`), and a custom exception hierarchy for standardized error handling.

## 4. Development & Testing

- **Language:** The entire codebase, including comments and documentation, is in **Brazilian Portuguese**.
- **Naming Conventions:**
  - Controllers end with `Controle` (e.g., `ProcessoControle.java`).
  - Services often have an interface (`*Service`) and an implementation (`*ServiceImpl`).
- **Testing:**
  - The project uses JUnit 5, Mockito, and AssertJ.
  - Integration tests (`@SpringBootTest`) are critical for verifying the complex, transactional workflows.
  - Pay close attention to test data setup to avoid persistence-related exceptions (e.g., `TransientPropertyValueException`). Ensure parent entities are saved before child entities that reference them.
  - The in-memory H2 database is used for the test scope.
- **Build Tool:** Gradle is used for dependency management and running tasks. The wrapper script (`./gradlew`) is located in the `/app` directory.