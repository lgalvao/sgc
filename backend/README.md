# Backend do Sistema de Gestão de Competências (SGC)

## Visão Geral

Este diretório contém o código do backend do SGC. Ele fornece uma API REST para consumo pelo frontend. A arquitetura é organizada em pacotes representando domínios específicos.

## 🏗️ Arquitetura e Stack

A aplicação segue uma arquitetura **Modular Monolith** construída com:

* **Java 21**: Linguagem base.
* **Spring Boot 4.0.1**: Framework de aplicação (GA).
* **Hibernate/JPA**: Persistência de dados.
* **Oracle**: Banco de dados de produção.
* **H2 Database**: Banco de dados em memória para testes e desenvolvimento local rápido.
* **Gradle**: Ferramenta de build e gerenciamento de dependências.

## 📦 Módulos Principais

O código está organizado em `src/main/java/sgc/` com os seguintes módulos principais:

* **`processo`**: Orquestrador central de fluxos de alto nível.
* **`subprocesso`**: Máquina de estados para gerenciamento de tarefas por unidade.
* **`mapa`**: Núcleo do domínio (Mapas, Competências, Atividades, Conhecimentos).
* **`usuario`**: Gestão de usuários, perfis e autenticação.
* **`unidade`**: Modelagem da estrutura organizacional.
* **`alerta` / `notificacao`**: Módulos reativos para comunicação com o usuário.
* **`analise`**: Auditoria e histórico de revisões.

## 🚀 Como Executar

A partir da raiz do projeto, execute:

```bash
cd backend
./gradlew bootRun -Dspring.profiles.active=e2e
```

A API do backend estará disponível em `http://localhost:10000`.

### Perfis do Spring

O sistema utiliza perfis para configurar o comportamento do ambiente:

* `default`/`local`: Usa banco H2 em memória. Ideal para desenvolvimento.
* `prod`: Configurado para Oracle.
* `test`: Ativado durante a execução de testes unitários/integração.
* `e2e`: Ativa endpoints auxiliares para testes end-to-end (reset de banco, fixtures).

## 🧪 Testes

### Execução

O projeto suporta a execução granular de testes através de tarefas Gradle específicas:

*   **Todos os Testes** (Padrão):
    ```bash
    ./gradlew test
    ```
    Executa tanto testes unitários quanto de integração.

*   **Apenas Unitários** (Rápido, exclui tag `integration`):
    ```bash
    ./gradlew unitTest
    ```

*   **Apenas Integração** (Filtra tag `integration`):
    ```bash
    ./gradlew integrationTest
    ```

### Estrutura de Testes

Os testes estão localizados em `src/test/java/sgc/`:

* **`integracao/`**: Testes de integração cobrindo os Casos de Uso (CDU-XX).
* **`[pacote]/`**: Testes unitários específicos de cada módulo.
* **`architecture/`**: Testes ArchUnit garantindo a integridade arquitetural.

### Status Atual

* ✅ **1078/1078 testes passando (100%)**
* ✅ **Cobertura de código: 95.1%**
* ✅ **Testes de segurança: 95%+ cobertura**
* ✅ **Testes E2E: Implementados com Playwright**

## 🏛️ Arquitetura Detalhada

### Padrões Arquiteturais

O sistema segue uma arquitetura em camadas com padrões bem definidos:

#### 1. Facade Pattern

Todos os módulos principais expõem uma **Facade** como ponto de entrada único:

```java
// Controllers interagem APENAS com Facades
@RestController
public class SubprocessoController {
    private final SubprocessoFacade facade;  // ✅ Correto
    
    @PostMapping("/{id}/disponibilizar")
    public void disponibilizar(@PathVariable Long id) {
        facade.disponibilizarCadastro(id, getCurrentUser());
    }
}
```

**Facades Implementadas:**
* ✅ `ProcessoFacade` - Operações de processos
* ✅ `SubprocessoFacade` - Operações de subprocessos  
* ✅ `AtividadeFacade` - Operações de atividades
* 🟡 `MapaService` - Atua como facade implícita

**Services Especializados** são package-private e usados apenas pelas Facades:
* `SubprocessoCadastroWorkflowService`
* `SubprocessoMapaWorkflowService`
* `SubprocessoService` (CRUD)
* `SubprocessoContextoService`
* etc.

#### 2. Security in Layers (3 Camadas)

O controle de acesso segue uma arquitetura em 3 camadas:

```
CAMADA 1: HTTP (Controllers)
┌─────────────────────────────────────────┐
│ @PreAuthorize("hasRole('CHEFE')")      │
│ - Verificação de autenticação          │
│ - Verificação básica de role           │
└─────────────────────────────────────────┘
                 ↓
CAMADA 2: AUTORIZAÇÃO (AccessControlService)
┌─────────────────────────────────────────┐
│ accessControlService.verificarPermissao │
│ - Verifica role necessária              │
│ - Verifica ownership (unidade)          │
│ - Verifica hierarquia                   │
│ - Verifica estado do recurso            │
│ - Audita decisão                        │
└─────────────────────────────────────────┘
                 ↓
CAMADA 3: LÓGICA DE NEGÓCIO (Services)
┌─────────────────────────────────────────┐
│ Services executam lógica                │
│ - SEM verificações de acesso            │
│ - Confiam que Camada 2 validou          │
└─────────────────────────────────────────┘
```

**Componentes de Segurança:**
* `AccessControlService` - Ponto central de autorização
* `AccessPolicy<T>` - Políticas específicas por recurso
* `HierarchyService` - Hierarquia de unidades
* `AccessAuditService` - Auditoria automática

#### 3. Domain Events

Comunicação assíncrona entre módulos via Spring Events:

```java
// Publicação
eventPublisher.publishEvent(new EventoProcessoIniciado(codigo));

// Observação
@EventListener
public void onProcessoIniciado(EventoProcessoIniciado evento) {
    // Reage sem acoplamento direto
}
```

**Estado Atual:** 6 eventos implementados (meta: 23 completos)

#### 4. Data Transfer Objects (DTOs)

**Regra:** NUNCA expor entidades JPA diretamente.

```java
// ✅ BOM: Retorna DTO
@GetMapping("/{id}")
public SubprocessoDto obter(@PathVariable Long id) {
    return facade.obterDto(id);
}

// ❌ RUIM: Expõe entidade JPA
@GetMapping("/{id}")
public Subprocesso obter(@PathVariable Long id) {
    return repository.findById(id).get();
}
```

### Fluxo de Dados

#### Leitura (Query)
```
User Request → Controller → Facade → Service → Repository 
→ JPA Entity → Mapper → DTO → HTTP Response
```

#### Escrita (Command)
```
User Request + DTO → Controller (@Valid) → Facade
→ AccessControlService (autoriza)
→ Service (valida + executa) → Repository → JPA Entity
→ EventPublisher (opcional) → DTO → HTTP Response
```

### Módulos Detalhados

#### `sgc.processo`
* **Facade:** `ProcessoFacade` ✅
* **Responsabilidade:** Gerencia ciclo de vida de processos (MAPEAMENTO ou REVISÃO)
* **Entidades:** `Processo`, `SituacaoProcesso`, `TipoProcesso`
* **Services:** `ProcessoConsultaService`, etc.

#### `sgc.subprocesso`
* **Facade:** `SubprocessoFacade` ✅
* **Responsabilidade:** Gerencia subprocessos vinculados a processos e unidades
* **Entidades:** `Subprocesso`, `SituacaoSubprocesso`, `TransicaoSubprocesso`
* **Services:** 12 services (oportunidade de consolidação para ~6)
  * `SubprocessoCadastroWorkflowService` - Workflow de cadastro
  * `SubprocessoMapaWorkflowService` - Workflow de mapa
  * `SubprocessoService` - CRUD básico
  * `SubprocessoContextoService` - Contexto de edição
  * `SubprocessoTransicaoService` - Registro de transições
  * `SubprocessoPermissaoCalculator` - Cálculo de permissões
  * `SubprocessoEmailService` - Envio de emails
  * + 4 em `decomposed/`

#### `sgc.mapa`
* **Facade:** `MapaService` (atua como facade) 🟡, `AtividadeFacade` ✅
* **Responsabilidade:** Gerencia mapas de competências
* **Entidades:** `Mapa`, `Competencia`, `Atividade`, `Conhecimento`
* **Services:** `CompetenciaService`, `ConhecimentoService`, `MapaSalvamentoService`, etc.

#### `sgc.organizacao`
* **Services:** `UsuarioService`, `UnidadeService`
* **Responsabilidade:** Estrutura organizacional (usuários, unidades, perfis)
* **Entidades:** `Usuario`, `Unidade`, `Perfil`

#### `sgc.seguranca`
* **Pacote:** `sgc.seguranca.acesso` - Controle de acesso centralizado
* **Componentes:**
  * `AccessControlService` - Serviço central
  * `AccessPolicy<T>` - Interface de políticas
  * `SubprocessoAccessPolicy`, `ProcessoAccessPolicy`, etc.
  * `HierarchyService` - Hierarquia de unidades
  * `AccessAuditService` - Auditoria
* **Pacote:** `sgc.seguranca.login` - Autenticação
  * `LoginService`, `JwtService`, `ConfigSeguranca`

#### `sgc.analise`
* **Service:** `AnaliseService`
* **Responsabilidade:** Auditoria de análises durante workflows
* **Entidades:** `Analise`, `TipoAnalise`, `TipoAcaoAnalise`

#### `sgc.notificacao` e `sgc.alerta`
* **Services:** `NotificacaoEmailService`, `AlertaFacade`
* **Responsabilidade:** Comunicação reativa com usuários
* **Integração:** Reage a eventos de domínio

### Convenções de Código

#### Nomenclatura
* **Classes:** `PascalCase`
* **Métodos:** `camelCase`
* **Constantes:** `UPPER_SNAKE_CASE`
* **Packages:** `lowercase`

#### Sufixos Obrigatórios
* Controllers: `{Entidade}Controller`
* Facades: `{Entidade}Facade`
* Services: `{Entidade}Service`
* Repositories: `{Entidade}Repo`
* DTOs: `{Entidade}Dto`
* Mappers: `{Entidade}Mapper`
* Exceções: `Erro{TipoErro}`

#### Idioma
**TUDO em Português Brasileiro:**
* Código (variáveis, métodos, classes)
* Comentários
* Mensagens de erro
* Documentação

#### Identificadores
**SEMPRE** use `codigo` em vez de `id`:
```java
// ✅ BOM
private Long codigo;
@PathVariable Long codigo

// ❌ RUIM
private Long id;
@PathVariable Long id
```

#### REST API (Não-Padrão)
```
GET  /api/processos           - Listar
GET  /api/processos/{id}      - Obter
POST /api/processos           - Criar
POST /api/processos/{id}/atualizar   - Atualizar
POST /api/processos/{id}/excluir     - Excluir
POST /api/processos/{id}/iniciar     - Workflow action
```

**Justificativa:** Simplicidade para operações de workflow complexas.

## 📊 Métricas de Qualidade

* **Testes:** 1078/1078 passando (100%)
* **Cobertura:** 95.1% (18.791/19.752 instruções)
* **Checkstyle:** 169 violações em 81 arquivos (baseline)
* **Services em subprocesso:** 12 (oportunidade: consolidar para ~6)
* **Facades implementadas:** 4 (ProcessoFacade, SubprocessoFacade, AtividadeFacade, MapaService)
* **Eventos de domínio:** 6 implementados (meta: 23)
* **Null-safety:** @NullMarked em todos os pacotes

## 🎯 Oportunidades de Melhoria

### Consolidação de Services (Subprocesso)
* **Atual:** 12 services
* **Meta:** ~6 services (50% redução)
* **Ações:**
  * Consolidar `SubprocessoCadastroWorkflowService` + `SubprocessoMapaWorkflowService`
  * Mover lógica de `SubprocessoContextoService` para `SubprocessoFacade`
  * Tornar services especializados `package-private`

### MapaFacade Explícita
* Renomear `MapaService` → `MapaFacade` para consistência
* Consolidar services especializados

### Eventos de Domínio Completos
* **Atual:** 6 eventos
* **Meta:** 23 eventos completos
* Refatorar comunicação síncrona para assíncrona

## 📚 Documentação Adicional

* [Arquitetura Completa](/docs/ARCHITECTURE.md) - Visão detalhada do sistema
* [Backend Patterns](/regras/backend-padroes.md) - Padrões e convenções
* [Security Refactoring](/SECURITY-REFACTORING.md) - Refatoração de segurança (completa)
* [Refactoring Plan](/refactoring-plan.md) - Plano de refatoração arquitetural

---

**Última atualização:** 2026-01-10  
**Versão:** 2.0 (pós-refatoração de segurança)