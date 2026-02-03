# Stack Tecnológico: SGC - Sistema de Gestão de Competências

## Backend
- **Linguagem:** Java 21
- **Framework Principal:** Spring Boot 4.0.2
- **Gerenciamento de Dependências:** Gradle 9.3.0
- **Persistência:** Spring Data JPA / Hibernate 7
- **Mapeamento de Objetos:** MapStruct 1.6.3
- **Utilidades:** 
  - Lombok 1.18.42
  - Jackson 3
  - JSpecify (para null-safety)
- **Segurança:** Spring Security, JJWT (JSON Web Token)

## Frontend
- **Framework:** Vue.js 3.5
- **Linguagem:** TypeScript 5.9.3
- **Build Tool:** Vite 7.3.1
- **Gerenciamento de Estado:** Pinia 3.0.4
- **UI Components:** BootstrapVueNext e Bootstrap 5.3.8
- **Validação:** Zod 4.3.6

## Banco de Dados
- **Produção:** Oracle
- **Desenvolvimento/Testes:** H2 (In-memory)

## Qualidade e Testes
- **Backend:** 
  - JUnit 6
  - Mockito
  - AssertJ 3
  - ArchUnit (Regras de Arquitetura)
  - Jacoco (Cobertura de Código)
  - Pitest (Testes de Mutação)
- **Frontend:** 
  - Vitest (Testes Unitários)
  - Vue Test Utils
- **End-to-End (E2E):** 
  - Playwright 1.57.0
