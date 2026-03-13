# Backend do Sistema de Gestão de Competências (SGC)

## Visão geral

Este diretório contém o código do backend do SGC. Ele fornece uma API REST para consumo pelo frontend. A arquitetura é
organizada em pacotes representando domínios específicos.

## 🏗️ Arquitetura e Stack

A aplicação segue uma arquitetura **Modular monolith** construída com:

* **Java 21**: Linguagem base.
* **Spring Boot 4.0.3**: Framework de aplicação (GA).
* **Hibernate/JPA**: Persistência de dados.
* **Oracle**: Banco de dados de produção.
* **H2 Database**: Banco de dados em memória para testes e desenvolvimento local rápido.
* **Gradle**: Ferramenta de build e gerenciamento de dependências.

## 📦 Módulos principais

O código está organizado em `src/main/java/sgc/` com os seguintes módulos principais:

* **`processo`**: Orquestrador central de fluxos de alto nível.
* **`subprocesso`**: Máquina de estados para gerenciamento de tarefas por unidade.
* **`mapa`**: Núcleo do domínio (Mapas, Competências, Atividades, Conhecimentos).
* **`usuario`**: Gestão de usuários, perfis e autenticação.
* **`unidade`**: Modelagem da estrutura organizacional.
* **`alerta` / `notificacao`**: Módulos reativos para comunicação com o usuário.
* **`analise`**: Auditoria e histórico de revisões.

## 🚀 Como executar

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

* **Todos os Testes** (Padrão):
  ```bash
  ./gradlew test
  ```
  Executa tanto testes unitários quanto de integração.

* **Apenas unitários** (Rápido, exclui tag `integration`):
  ```bash
  ./gradlew unitTest
  ```

* **Apenas integração** (Filtra tag `integration`):
  ```bash
  ./gradlew integrationTest
  ```

### Estrutura de Testes

Os testes estão localizados em `src/test/java/sgc/`:

* **`integracao/`**: Testes de integração cobrindo os Casos de Uso (CDU-XX).
* **`[pacote]/`**: Testes unitários específicos de cada módulo.
* **`architecture/`**: Testes ArchUnit garantindo a integridade arquitetural.

## 🏛️ Arquitetura detalhada

### Padrões arquiteturais

O sistema segue uma arquitetura em camadas com padrões bem definidos:

#### 1. Facade pattern

Todos os módulos principais expõem uma **Facade** como ponto de entrada único:

```java
// Controllers interagem APENAS com Facades
@RestController
public class SubprocessoController {
    private final SubprocessoFacade facade;  // ✅ Correto
    
    @PostMapping("/{codigo}/disponibilizar")
    public void disponibilizar(@PathVariable Long id) {
        facade.disponibilizarCadastro(id, getCurrentUser());
    }
}
```

**Services especializados** são package-private e usados apenas pelas Facades:

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

#### 3. Domain events

Comunicação assíncrona entre módulos via Spring events:

```java
// Publicação
eventPublisher.publishEvent(new EventoProcessoIniciado(codigo));

// Observação
@EventListener
public void onProcessoIniciado(EventoProcessoIniciado evento) {
    // Reage sem acoplamento direto
}
```

#### 4. Data transfer objects (DTOs)

**Regra:** NUNCA expor entidades JPA diretamente.

```java
// ✅ BOM: Retorna DTO
@GetMapping("/{codigo}")
public SubprocessoDto obter(@PathVariable Long id) {
    return facade.obterDto(id);
}

// ❌ RUIM: Expõe entidade JPA
@GetMapping("/{codigo}")
public Subprocesso obter(@PathVariable Long id) {
    return repository.findById(id).get();
}
```

### Fluxo de Dados

#### Leitura (Query)

```
User request → Controller → Facade → Service → Repository 
→ JPA Entity → Mapper → DTO → HTTP Response
```

#### Escrita (Command)

```
User request + DTO → Controller (@Valid) → Facade
→ AccessControlService (autoriza)
→ Service (valida + executa) → Repository → JPA Entity
→ EventPublisher (opcional) → DTO → HTTP Response
```

### Módulos detalhados

#### `sgc.processo`

* **Facade:** `ProcessoFacade`
* **Responsabilidade:** Gerencia ciclo de vida de processos (MAPEAMENTO ou REVISÃO)
* **Entidades:** `Processo`, `SituacaoProcesso`, `TipoProcesso`
* **Services:** `ProcessoConsultaService`, etc.

#### `sgc.subprocesso`

* **Facade:** `SubprocessoFacade`
* **Responsabilidade:** Gerencia subprocessos vinculados a processos e unidades
* **Entidades:** `Subprocesso`, `SituacaoSubprocesso`, `TransicaoSubprocesso`
* **Services:** `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoService` (CRUD),
  `SubprocessoContextoService`

#### `sgc.mapa`

* **Facade:** `MapaService` (atua como facade), `AtividadeFacade`
* **Responsabilidade:** Gerencia mapas de competências
* **Entidades:** `Mapa`, `Competencia`, `Atividade`, `Conhecimento`
* **Services:** `CompetenciaService`, `ConhecimentoService`, `MapaSalvamentoService`, etc.

#### `sgc.organizacao`

* **Services:** `UsuarioFacade`, `OrganizacaoFacade`
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
    * `LoginFacade`, `JwtService`, `ConfigSeguranca`

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

#### Sufixos obrigatórios

* Controllers: `{Entidade}Controller`
* Facades: `{Entidade}Facade`
* Services: `{Entidade}Service`
* Repositories: `{Entidade}Repo`
* DTOs: `{Entidade}Dto`
* Mappers: `{Entidade}Mapper`
* Exceções: `Erro{TipoErro}`

#### Idioma

**TUDO em Português brasileiro:**

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
private final Long id;
@PathVariable Long id
```

#### REST API (Não-Padrão)

```
GET  /api/processos           - Listar
GET  /api/processos/{codigo}      - Obter
POST /api/processos           - Criar
POST /api/processos/{codigo}/atualizar   - Atualizar
POST /api/processos/{codigo}/excluir     - Excluir
POST /api/processos/{codigo}/iniciar     - Workflow action
```

## 📚 Documentação adicional

* [Backend patterns](/etc/docs/backend-padroes.md) - Padrões e convenções
