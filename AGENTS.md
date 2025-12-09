# Guia para Agentes de Desenvolvimento

Este documento estabelece diretrizes e boas práticas para agentes de desenvolvimento que trabalham no projeto SGC. O objetivo é garantir consistência, eficiência e alinhamento com as convenções do projeto.

## 1. Conhecimento do Projeto

É fundamental que o agente se familiarize com a estrutura e as especificidades de cada módulo do projeto antes de iniciar qualquer tarefa.

- **Visão Geral:** Consulte o `README.md` na raiz do repositório.
- **Backend:** Cada pacote principal em `backend/src/main/java/sgc/` possui um `README.md` detalhando suas responsabilidades, arquitetura e componentes.
- **Frontend:** Cada diretório em `frontend/src/` (components, views, stores, etc.) também contém um `README.md`.

## 2. Regras Gerais de Desenvolvimento

### 2.1. Idioma

- **Português Brasileiro:** Todo o sistema, incluindo nomes de variáveis, métodos, classes, mensagens de erro, logs, comentários e documentação, deve estar em Português Brasileiro.

### 2.2. Convenções de Nomenclatura

- **Classes:** PascalCase (ex: `UsuarioService`).
- **Métodos e Variáveis:** camelCase (ex: `buscarPorCodigo`).
- **Exceções:** Prefixo `Erro` (ex: `ErroEntidadeNaoEncontrada`).
- **Repositórios JPA:** Sufixo `Repo` (ex: `SubprocessoRepo`).
- **Serviços:** Sufixo `Service` (ex: `MapaService`).
- **Controladores:** Sufixo `Controller` (ex: `ProcessoController`).
- **Testes:** Sufixo `Test` (ex: `MapaServiceTest`).

## 3. Backend (Java com Spring Boot)

### 3.1. Arquitetura

- **Service Facade:** Cada módulo (pacote) deve ter um serviço principal (Fachada) que orquestra a lógica de negócio e delega para serviços especializados. O Controller deve interagir apenas com essa fachada.
- **DTOs:** Nunca exponha entidades JPA diretamente nos Controllers. Utilize DTOs (`dto/`) e Mappers (`MapStruct`).
- **Pacote Comum:** Utilize o pacote `sgc.comum` para exceções (`ErroApi`), configurações e utilitários compartilhados.

### 3.2. API REST

- **Verbos HTTP:**
  - `GET`: Para consultas.
  - `POST`: Para criação e, por convenção deste projeto, também para **atualização** e **exclusão** (usando sufixos na URL, ex: `/api/recurso/{id}/atualizar`, `/api/recurso/{id}/excluir`).
- **Erros:** Lance exceções da hierarquia de `sgc.comum.erros`. O `RestExceptionHandler` as converterá automaticamente para respostas JSON padronizadas.

### 3.3. Testes

- Utilize **JUnit 5** e **Mockito**.
- Evite criar dados de teste manualmente se puder usar os builders ou factories existentes.

## 4. Frontend (Vue.js com TypeScript)

### 4.1. Arquitetura

- Siga o fluxo: `View (Página)` -> `Store (Pinia)` -> `Service (Axios)` -> `API`.
- **Componentes:** Devem ser "burros" (apresentacionais), recebendo dados via `props` e emitindo eventos via `emits`. Consulte `frontend/src/components/README.md`.
- **Views:** Responsáveis por conectar as Stores aos Componentes.

### 4.2. Tecnologias

- **UI:** Utilize componentes da biblioteca **BootstrapVueNext** (`BButton`, `BModal`, etc.) em vez de HTML/Bootstrap puro quando possível.
- **Estado:** Utilize **Pinia** com "Setup Stores".
- **Roteamento:** Vue Router modularizado.

## 5. Testes e Qualidade

Antes de submeter alterações no **frontend**, execute (dentro de `frontend`):

- **`npm run typecheck`**: Verifica tipagem TypeScript.
- **`npm run lint`**: Verifica estilo de código.

Antes de submeter alterações no **backend**, execute (na raiz):

- **`./gradlew :backend:test`**: Testes unitários/integração em Junit.

## 6. Testes E2E (Playwright)

Os testes de ponta a ponta (E2E) validam o funcionamento integrado do Frontend e Backend.
