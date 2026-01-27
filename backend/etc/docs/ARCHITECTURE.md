# Arquitetura do Sistema SGC

**Versão:** 2.0  
**Data:** 2026-01-10  
**Status:** Atualizado após refatoração de segurança (100% testes passando)

---

## 📋 Visão Geral

Sistema de Gestão de Competências construído com:

- **Backend:** Spring Boot 4 + Java 21
- **Frontend:** Vue 3.5 + TypeScript
- **Banco:** PostgreSQL
- **Segurança:** Spring Security + AccessControlService centralizado

---

## 🏗️ Arquitetura em Camadas

```
FRONTEND (Vue 3)
├── Views (componentes inteligentes)
├── Stores (Pinia - estado global)
├── Services (chamadas HTTP)
└── Components (apresentacionais)
        ↓ HTTP/REST
BACKEND (Spring Boot 4)
├── Controllers (@PreAuthorize básico)
├── AccessControlService (autorização detalhada)
├── Service Facades (orquestração)
├── Services Especializados (lógica de negócio)
├── Repositories (Spring Data JPA)
└── Entidades JPA
        ↓
PostgreSQL Database
```

---

## 🎯 Princípios Arquiteturais

### 1. Facade Pattern

Controllers interagem APENAS com Facades. Services especializados são package-private.

**Facades Implementadas:**

- ✅ `ProcessoFacade`
- ✅ `SubprocessoFacade`
- ✅ `AtividadeFacade`
- 🟡 `MapaService` (atua como facade implícita)

### 2. Fail-Safe Security

```
Camada 1: @PreAuthorize (autenticação + role básica)
Camada 2: AccessControlService (autorização detalhada)
Camada 3: Services (lógica SEM verificações de acesso)
```

### 3. Domain Events

Desacoplamento via Spring Events (6 eventos atuais, meta: 23).

### 4. DTOs Obrigatórios

NUNCA expor entidades JPA. Sempre usar DTOs + Mappers.

---

## 📦 Módulos Principais

### `processo`

- **Facade:** `ProcessoFacade` ✅
- **Função:** Gerencia ciclo de vida de processos

### `subprocesso`

- **Facade:** `SubprocessoFacade` ✅
- **Services:** 12 services (oportunidade de consolidação para ~6)
- **Função:** Gerencia subprocessos por unidade

### `mapa`

- **Facade:** `MapaService` (atua como facade) 🟡
- **Facade:** `AtividadeFacade` ✅
- **Função:** Gerencia mapas de competências

### `organizacao`

- **Services:** `UsuarioFacade`, `UnidadeFacade`
- **Função:** Estrutura organizacional

### `seguranca`

- **Service:** `AccessControlService` ✅
- **Políticas:** `SubprocessoAccessPolicy`, `ProcessoAccessPolicy`, etc.
- **Função:** Controle de acesso centralizado

---

## �� Segurança

### Fluxo de Autorização

```
1. Controller: @PreAuthorize("hasRole('CHEFE')")
2. Service: accessControlService.verificarPermissao(usuario, ACAO, recurso)
3. AccessPolicy: verifica perfil + situação + hierarquia
4. AccessAuditService: registra decisão
5. Service: executa lógica de negócio
```

### Políticas por Recurso

Cada tipo de recurso tem uma `AccessPolicy<T>` que define:

- Perfis permitidos
- Situações permitidas
- Requisitos hierárquicos

---

## 🔄 Padrões de Projeto

| Padrão         | Aplicação              | Exemplo                                 |
|----------------|------------------------|-----------------------------------------|
| **Facade**     | Ponto de entrada único | `SubprocessoFacade`                     |
| **Strategy**   | EnumMaps vs if/else    | `SITUACAO_MAPA.get(tipo)`               |
| **Repository** | Spring Data JPA        | `SubprocessoRepo extends JpaRepository` |
| **Observer**   | Spring Events          | `@EventListener`                        |
| **Builder**    | Lombok @Builder        | `DTO.builder().campo(valor).build()`    |

---

## 📝 Convenções

### Nomenclatura

- **Backend:** Classes `PascalCase`, métodos `camelCase`
- **Frontend:** Componentes `PascalCase`, arquivos TS `camelCase`
- **Sufixos:** `Controller`, `Service`, `Facade`, `Repo`, `Dto`, `Mapper`

### Idioma

**TUDO em Português Brasileiro:** código, comentários, mensagens, documentação.

### Identificadores

**SEMPRE** use `codigo` em vez de `id`.

### REST API

```
GET  /api/processos           - Listar
POST /api/processos           - Criar
POST /api/processos/{id}/atualizar   - Atualizar
POST /api/processos/{id}/excluir     - Excluir
POST /api/processos/{id}/iniciar     - Workflow action
```

---

## 🎯 Oportunidades de Melhoria

### Sprint 2: Consolidar Services - Subprocesso

- **Atual:** 12 services
- **Meta:** ~6 services (50% redução)
- **Ações:**
    - Consolidar `SubprocessoCadastroWorkflowService` + `SubprocessoMapaWorkflowService`
    - Mover lógica de `SubprocessoContextoService` para `SubprocessoFacade`
    - Tornar services especializados `package-private`

### Sprint 3: MapaFacade Explícita

- Renomear `MapaService` → `MapaFacade`
- Consolidar services especializados

### Sprint 5: Eventos de Domínio

- **Atual:** 6 eventos
- **Meta:** 23 eventos completos
- Refatorar comunicação síncrona para assíncrona

---

## 📚 Referências

- [Backend Patterns](/etc/regras/backend-padroes.md)
- [Frontend Patterns](/etc/regras/frontend-padroes.md)
- [Security Refactoring](/etc/docsdocs/SECURITY-REFACTORING-COMPLETE.md)
- [AGENTS.md](/AGENTS.md)

---

**Mantido por:** GitHub Copilot AI Agent  
**Última atualização:** 2026-01-10

