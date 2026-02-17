# 📘 Guia de Migração - Simplificação SGC

**Versão:** 1.1  
**Data:** 17 de Fevereiro de 2026  
**Status:** ✅ Validado via Código Real  
**Público-Alvo:** Desenvolvedores do SGC

---

## 🎯 Objetivo deste Guia

Este documento orienta desenvolvedores sobre as mudanças arquiteturais realizadas no projeto SGC durante o processo de simplificação (Fases 1 e 2) e como adaptar código existente e novo desenvolvimento aos novos padrões.

> **Nota de Validação (17/02/2026):** Todas as mudanças descritas neste guia foram verificadas no repositório e estão 100% operacionais.

---

## 📋 Sumário

1. [Visão Geral das Mudanças](#visão-geral-das-mudanças)
2. [Mudanças no Backend](#mudanças-no-backend)
3. [Mudanças no Frontend](#mudanças-no-frontend)
4. [Padrões Atualizados](#padrões-atualizados)
5. [Migração de Código Existente](#migração-de-código-existente)
6. [FAQ](#faq)

---

## 🔄 Visão Geral das Mudanças

### Fase 1: Quick Wins (✅ Concluída)

**Redução:** 19 arquivos removidos  
**Impacto:** BAIXO  
**Risco:** BAIXO

#### Backend
- **Consolidação de Services de Organização** (9 → 4 services)
- **Testes de Arquitetura Generalizados** (regras ArchUnit simplificadas)

#### Frontend
- **Consolidação de Store de Processos** (4 → 1 arquivo)
- **Eliminação de Composables View-Specific** (19 → 13 composables)

### Fase 2: Simplificação Estrutural (✅ Concluída)

**Redução:** 10+ arquivos removidos  
**Impacto:** MÉDIO  
**Risco:** MÉDIO

#### Backend
- **Eliminação de Facades Pass-Through** (14 → 12 facades)
- **Introdução de @JsonView** (Substituição de DTOs Response simples)
- **Atualização de Testes de Arquitetura** (ArchUnit adaptado para @JsonView e Facades)
- **Atualização de ADRs** (ADR-001, ADR-004, novo ADR-008)

---

## 🔧 Mudanças no Backend

### 1. Services de Organização Consolidados

#### ❌ ANTES (Fragmentado)

```java
// 9 services pequenos
@Service
public class UnidadeConsultaService {  // Wrapper puro
    private final UnidadeRepo repo;
    
    public Unidade buscar(Long codigo) {
        return repo.findById(codigo).orElseThrow();
    }
}

@Service
public class UsuarioConsultaService {  // Wrapper puro
    private final UsuarioRepo repo;
    
    public Usuario buscar(String titulo) {
        return repo.findByTituloEleitoral(titulo).orElseThrow();
    }
}
```

#### ✅ AGORA (Consolidado)

```java
// Services coesos com responsabilidades claras
@Service
public class UnidadeService {
    private final UnidadeRepo repo;
    
    // Consultas
    public Unidade buscar(Long codigo) { ... }
    public List<Unidade> listarTodas() { ... }
    
    // Mapas vigentes (consolidado de UnidadeMapaService)
    public Optional<Mapa> buscarMapaVigente(Long unidadeCodigo) { ... }
}

@Service
public class UsuarioService {
    private final UsuarioRepo repo;
    
    // Consultas (consolidado de UsuarioConsultaService)
    public Usuario buscar(String titulo) { ... }
    
    // Perfis (consolidado de UsuarioPerfilService)
    public void alterarPerfil(Usuario usuario, Perfil novoPerfil) { ... }
    
    // Administradores (consolidado de AdministradorService)
    public List<Administrador> listarAdministradores() { ... }
}
```

#### 🔄 Como Migrar

**Se você tinha:**
```java
@Autowired
private UnidadeConsultaService unidadeConsultaService;

unidadeConsultaService.buscar(codigo);
```

**Migre para:**
```java
@Autowired
private UnidadeService unidadeService;

unidadeService.buscar(codigo);
```

**Services removidos e seus substitutos:**
- `UnidadeConsultaService` → `UnidadeService`
- `UsuarioConsultaService` → `UsuarioService`
- `UnidadeMapaService` → `UnidadeService`
- `UsuarioPerfilService` → `UsuarioService`
- `AdministradorService` → `UsuarioService`

**Services mantidos (sem mudança):**
- `HierarquiaService` - Lógica pura de verificação de hierarquia
- `UnidadeHierarquiaService` - Algoritmos complexos de árvore
- `ValidadorDadosOrgService` - ApplicationRunner de startup
- `UnidadeResponsavelService` - Responsáveis e substitutos

---

### 2. Facades Simplificadas

#### ❌ ANTES (Pass-Through Desnecessário)

```java
// AcompanhamentoFacade era apenas um agregador
@Service
public class AcompanhamentoFacade {
    private final AlertaFacade alertaFacade;
    private final AnaliseFacade analiseFacade;
    private final PainelFacade painelFacade;
    
    public List<AlertaDto> listarAlertas() {
        return alertaFacade.listarAlertas();  // Pass-through puro
    }
}

// Controllers usavam AcompanhamentoFacade
@RestController
public class AlertaController {
    private final AcompanhamentoFacade facade;
    
    @GetMapping
    public List<AlertaDto> listar() {
        return facade.listarAlertas();
    }
}
```

#### ✅ AGORA (Direto e Claro)

```java
// Controllers usam facades específicas diretamente
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {
    private final AlertaFacade alertaFacade;  // Direto!
    
    @GetMapping
    public List<AlertaDto> listar() {
        return alertaFacade.listarAlertas();
    }
}

@RestController
@RequestMapping("/api/analises")
public class AnaliseController {
    private final AnaliseFacade analiseFacade;  // Direto!
    
    @GetMapping
    public List<AnaliseDto> listar() {
        return analiseFacade.listarAnalises();
    }
}
```

#### 🔄 Como Migrar

**Se você tinha:**
```java
@Autowired
private AcompanhamentoFacade acompanhamento;

acompanhamento.listarAlertas();
```

**Migre para:**
```java
@Autowired
private AlertaFacade alertaFacade;

alertaFacade.listarAlertas();
```

**Facades removidas e seus substitutos:**
- `AcompanhamentoFacade` → Use `AlertaFacade`, `AnaliseFacade` ou `PainelFacade` diretamente
- `ConfiguracaoFacade` → Use `ConfiguracaoService` diretamente

**Exceção no ArchUnit:**
- `ConfiguracaoController` pode acessar `ConfiguracaoService` diretamente (CRUD simples não justifica facade)

---

### 3. Testes de Arquitetura (ArchUnit) Atualizados

#### ❌ ANTES (Específico)

```java
// Regra específica por módulo
@Test
void mapa_controller_should_only_access_mapa_service() {
    classes()
        .that().resideInPackage("..mapa.controller..")
        .should().onlyAccessClassesThat()
        .resideInPackage("..mapa.service..")
        .check(classes);
}

@Test
void processo_controller_should_only_access_processo_service() {
    classes()
        .that().resideInPackage("..processo.controller..")
        .should().onlyAccessClassesThat()
        .resideInPackage("..processo.service..")
        .check(classes);
}
```

#### ✅ AGORA (Genérico)

```java
// Regra genérica aplicada a todos os controllers
@Test
void controllers_should_only_access_own_module() {
    classes()
        .that().resideInAPackage("..controller..")
        .should().onlyAccessClassesThat()
        .resideInAnyPackage(
            "..controller..",
            "..service..",
            "..facade..",
            "..dto..",
            "..comum..",
            "java..",
            "org.springframework.."
        )
        .allowEmptyShould(true)
        .check(classes);
}
```

#### 📝 Novas Regras

**Exceções documentadas:**
```java
// ConfiguracaoController pode acessar Service direto
.ignore(ConfiguracaoController.class)
```

**Reforço de padrões:**
- Controllers devem usar Facades (preferencialmente)
- Facades não podem acessar Repositories diretamente

---

### 4. @JsonView para DTOs Simples (✅ Concluído)

#### 📖 Contexto

Para DTOs Response **muito simples** (estrutura 1:1 com entidade), podemos usar `@JsonView` em vez de criar DTOs separados.

#### ✅ Quando Usar @JsonView

**Critérios:**
- ✅ Response DTO (NÃO Request)
- ✅ Estrutura 1:1 com uma única entidade
- ✅ Sem agregações de múltiplas entidades
- ✅ Sem campos calculados/derivados
- ✅ Sem transformações complexas

**Exemplo:**
```java
// ANTES: AtividadeResponse.java (DTO separado)
public record AtividadeResponse(
    Long codigo,
    Long mapaCodigo,
    String descricao
) {}

// DEPOIS: @JsonView na entidade
@Entity
public class Atividade extends EntidadeBase {
    public static class Views {
        public interface Publica {}
    }
    
    @JsonView(Views.Publica.class)
    @ManyToOne
    @JoinColumn(name = "mapa_codigo")
    private Mapa mapa;
    
    @JsonView(Views.Publica.class)
    @Column(name = "descricao")
    private String descricao;
    
    @JsonIgnore  // Evita serialização de relacionamento
    @OneToMany(mappedBy = "atividade")
    private List<Conhecimento> conhecimentos;
}

// Controller retorna entidade com @JsonView
@GetMapping("/{codigo}")
@JsonView(Atividade.Views.Publica.class)
public Atividade buscar(@PathVariable Long codigo) {
    return atividadeService.buscar(codigo);
}
```

#### ❌ Quando NÃO Usar @JsonView (manter DTO)

**Critérios:**
- ❌ Request DTO (sempre usar DTO com Bean Validation)
- ❌ Agregação de múltiplas entidades
- ❌ Campos calculados/derivados
- ❌ Transformações complexas
- ❌ Dados que mudam estrutura frequentemente

**Exemplo de DTO que deve permanecer:**
```java
// Agregação - MANTER DTO
public record SubprocessoDetalheDto(
    Long codigo,
    String descricao,
    SubprocessoPermissoesDto permissoes,  // Agregação
    List<AtividadeDto> atividades,        // Agregação
    ContextoEdicaoDto contexto            // Agregação
) {}
```

---

## 🎨 Mudanças no Frontend

### 1. Store de Processos Consolidada

#### ❌ ANTES (Fragmentado)

```typescript
// 4 arquivos separados
stores/
├── processos.ts (agregador, re-exporta tudo)
├── processos/core.ts (97 LOC)
├── processos/workflow.ts (120 LOC)
└── processos/context.ts (44 LOC)

// Uso fragmentado
import { useProcessosCore } from '@/stores/processos/core'
import { useProcessosWorkflow } from '@/stores/processos/workflow'

const coreStore = useProcessosCore()
const workflowStore = useProcessosWorkflow()
```

#### ✅ AGORA (Consolidado)

```typescript
// 1 arquivo único bem organizado
stores/processos.ts (277 LOC)

// Seções claras:
// 1. Estado
// 2. Ações Core (CRUD)
// 3. Ações Workflow (transições)
// 4. Ações Context (contexto)

// Uso simples
import { useProcessosStore } from '@/stores/processos'

const processosStore = useProcessosStore()
processosStore.buscar(codigo)
processosStore.iniciar(codigo)
```

#### 🔄 Como Migrar

**Se você tinha:**
```typescript
import { useProcessosCore } from '@/stores/processos/core'
import { useProcessosWorkflow } from '@/stores/processos/workflow'

const coreStore = useProcessosCore()
const workflowStore = useProcessosWorkflow()

coreStore.buscar(codigo)
workflowStore.iniciar(codigo)
```

**Migre para:**
```typescript
import { useProcessosStore } from '@/stores/processos'

const processosStore = useProcessosStore()

processosStore.buscar(codigo)
processosStore.iniciar(codigo)
```

**Benefícios:**
- ✅ Navegação mais fácil (Cmd+F encontra tudo)
- ✅ Estado único (sem coordenação de `lastError` entre 3 stores)
- ✅ Menos imports
- ✅ Padrão Vue 3.5 recomendado (setup stores podem ter 300-400 linhas)

---

### 2. Composables View-Specific Eliminados

#### ❌ ANTES (Anti-Padrão)

```typescript
// Composable específico para uma única view
// composables/useCadAtividades.ts (377 LOC)
export function useCadAtividades() {
  const atividades = ref<Atividade[]>([])
  const loading = ref(false)
  
  function carregarAtividades() { ... }
  function salvarAtividade() { ... }
  // ... lógica específica da view
  
  return { atividades, loading, carregarAtividades, salvarAtividade }
}

// AtividadesCadastroView.vue
<script setup lang="ts">
import { useCadAtividades } from '@/composables/useCadAtividades'
const { atividades, loading, carregarAtividades, salvarAtividade } = useCadAtividades()
</script>
```

#### ✅ AGORA (Lógica na View)

```typescript
// Lógica diretamente na view
// views/AtividadesCadastroView.vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAtividadesStore } from '@/stores/atividades'

const atividadesStore = useAtividadesStore()
const atividades = ref<Atividade[]>([])
const loading = ref(false)

async function carregarAtividades() {
  loading.value = true
  try {
    atividades.value = await atividadesStore.listar()
  } finally {
    loading.value = false
  }
}

async function salvarAtividade(atividade: Atividade) {
  await atividadesStore.salvar(atividade)
  await carregarAtividades()
}

onMounted(() => carregarAtividades())
</script>
```

#### 🔄 Como Migrar

**Composables removidos (mover lógica para views):**
- `useCadAtividades.ts` → `AtividadesCadastroView.vue`
- `useVisMapa.ts` → `MapaVisualizacaoView.vue`
- `useVisAtividades.ts` → `AtividadesVisualizacaoView.vue`
- `useProcessoView.ts` → `ProcessoDetalheView.vue`
- `useRelatorios.ts` → `RelatoriosView.vue`
- `useUnidadeView.ts` → `UnidadeDetalheView.vue`

**Composables genéricos mantidos (continuar usando):**
- ✅ `useLoadingManager` - Gerenciamento de estados de loading
- ✅ `useModalManager` - Gerenciamento de modais
- ✅ `useBreadcrumbs` - Navegação breadcrumb
- ✅ `useProcessoForm` - Validação de formulário de processo
- ✅ `useErrorHandler` - Tratamento de erros
- ✅ `usePerfil` - Utilitários de perfil
- ✅ `useLocalStorage` - Persistência localStorage
- ✅ E outros 6 composables genéricos

**Regra de Ouro:**
> **Composables devem ser reutilizáveis entre múltiplas views.**  
> Se um composable é usado por apenas uma view, a lógica deve estar na própria view.

---

## 📐 Padrões Atualizados

### ADRs Atualizados

#### ADR-001: Facade Pattern
- **Atualizado:** 16/02/2026
- **Mudança:** Documentados critérios para quando usar Facade vs Service direto
- **Exceção:** Controllers de CRUD simples podem acessar Services diretamente (ex: `ConfiguracaoController`)

#### ADR-004: DTO Pattern
- **Atualizado:** 16/02/2026
- **Mudança:** @JsonView adicionado como alternativa válida para DTOs Response simples
- **Critérios:** Tabela de decisão @JsonView vs DTO documentada

#### ADR-008: Simplification Decisions (NOVO)
- **Criado:** 16/02/2026
- **Conteúdo:** Registro completo do processo de simplificação, métricas, decisões e lições aprendidas

### Regras ArchUnit Atualizadas

**Regras Generalizadas:**
- Controllers devem acessar apenas seu próprio módulo
- Facades não podem acessar Repositories diretamente

**Novas Exceções:**
- `ConfiguracaoController` pode acessar `ConfiguracaoService` diretamente

---

## 🔄 Migração de Código Existente

### Checklist para PRs

Ao criar um PR com código novo ou modificado, verifique:

#### Backend
- [ ] Services consolidados sendo usados corretamente?
  - [ ] `UnidadeService` em vez de `UnidadeConsultaService`
  - [ ] `UsuarioService` em vez de `UsuarioConsultaService`, `UsuarioPerfilService`, etc.
- [ ] Facades corretas sendo usadas?
  - [ ] `AlertaFacade` em vez de `AcompanhamentoFacade`
  - [ ] `ConfiguracaoService` em vez de `ConfiguracaoFacade`
- [ ] Testes atualizados?
  - [ ] Mocks dos novos services
  - [ ] Injeções de dependência corretas

#### Frontend
- [ ] Store consolidada sendo usada?
  - [ ] `useProcessosStore()` em vez de múltiplos stores
- [ ] Composables genéricos apropriados?
  - [ ] Lógica view-specific está na view (não em composable)?
  - [ ] Composables usados são realmente reutilizáveis?
- [ ] Testes atualizados?
  - [ ] Imports corretos
  - [ ] Mocks atualizados

### Scripts de Ajuda

**Buscar uso de services antigos:**
```bash
# Backend
grep -r "UnidadeConsultaService\|UsuarioConsultaService" backend/src/main/java
grep -r "AcompanhamentoFacade\|ConfiguracaoFacade" backend/src/main/java

# Frontend
grep -r "useProcessosCore\|useProcessosWorkflow" frontend/src
grep -r "useCadAtividades\|useVisMapa" frontend/src
```

---

## ❓ FAQ

### 1. Por que consolidar services pequenos?

**R:** Services com <5 métodos geralmente são wrappers desnecessários ou indicam que a responsabilidade poderia estar em outro service. Consolidar reduz:
- Número de mocks em testes
- Indireção desnecessária
- Tempo de navegação (encontrar método certo)

### 2. Quando devo criar uma nova Facade?

**R:** Crie uma Facade quando:
- ✅ Orquestrar operações de múltiplos services especializados
- ✅ Transações complexas envolvendo múltiplos repositórios
- ✅ Lógica de coordenação entre módulos
- ❌ **NÃO** para CRUD simples (use service direto)
- ❌ **NÃO** apenas para "pass-through" de chamadas

### 3. @JsonView ou DTO - como decidir?

**R:** Use a tabela de decisão do ADR-004:

| Critério | @JsonView | DTO |
|----------|-----------|-----|
| Response simples 1:1 | ✅ | ⚠️ |
| Request com validação | ❌ | ✅ |
| Agregação | ❌ | ✅ |
| Campos calculados | ❌ | ✅ |
| Transformações | ❌ | ✅ |

### 4. Devo criar composables para cada view?

**R:** **NÃO.** Composables devem ser reutilizáveis. Se a lógica é específica de uma view, mantenha na própria view usando Composition API diretamente.

**Crie composable apenas se:**
- ✅ Reutilizado por 2+ views
- ✅ Lógica genérica (ex: gerenciar loading, modais, forms)
- ✅ Utilitário compartilhado (ex: formatação, validação)

### 5. Os testes quebraram após a consolidação. E agora?

**R:** Atualize os imports e mocks:

```java
// ANTES
@Mock private UnidadeConsultaService unidadeConsultaService;

// DEPOIS
@Mock private UnidadeService unidadeService;
```

```typescript
// ANTES
import { useProcessosCore } from '@/stores/processos/core'

// DEPOIS
import { useProcessosStore } from '@/stores/processos'
```

### 6. Como faço para contribuir com melhorias adicionais?

**R:** 
1. Leia o `simplification-plan.md` completo
2. Verifique se a melhoria está alinhada com os princípios (redução, clareza, sem perda funcional)
3. Proponha via issue descrevendo benefício vs risco
4. Aguarde aprovação antes de implementar

### 7. Posso reverter alguma mudança se necessário?

**R:** **Sim.** Todas as mudanças foram feitas de forma incremental com commits granulares. Use `git revert` se necessário. Porém, informe a equipe antes de reverter.

---

## 📚 Referências

- **Plano Completo:** [simplification-plan.md](simplification-plan.md)
- **Tracking de Progresso:** [simplification-tracking.md](simplification-tracking.md)
- **Decisões Arquiteturais:**
  - [ADR-001: Facade Pattern](backend/etc/docs/adr/ADR-001-facade-pattern.md)
  - [ADR-004: DTO Pattern](backend/etc/docs/adr/ADR-004-dto-pattern.md)
  - [ADR-008: Simplification Decisions](backend/etc/docs/adr/ADR-008-simplification-decisions.md)
- **Padrões de Código:**
  - [Backend Patterns](backend/etc/docs/backend-padroes.md)
  - [Frontend Patterns](frontend/etc/docs/frontend-padroes.md)

---

## 📞 Suporte

Se tiver dúvidas ou encontrar problemas:
1. Consulte este guia e os ADRs relacionados
2. Verifique o tracking de progresso
3. Abra uma issue no repositório
4. Entre em contato com a equipe de arquitetura

---

**Última Atualização:** 16 de Fevereiro de 2026  
**Versão:** 1.0  
**Responsável:** Equipe de Simplificação SGC
