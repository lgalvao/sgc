# Guia de Implementação - Simplificação SGC v2

**Objetivo:** Guia prático para implementar as simplificações recomendadas  
**Base:** LEIA-ME-COMPLEXIDADE-V2.md  
**Foco:** Fases 1 e 2 (conservadora, 15 dias)

---

## 📋 Pré-requisitos

Antes de começar:
- [ ] Ler LEIA-ME-COMPLEXIDADE-V2.md completo
- [ ] Aprovar roadmap Fases 1 + 2 com o time
- [ ] Garantir cobertura de testes atual (~70%+)
- [ ] Criar branch `feature/simplificacao-v2`
- [ ] Configurar CI para rodar testes a cada commit

---

## 🟢 FASE 1: Quick Wins (5 dias, BAIXO risco)

### 1.1. Consolidar Stores Frontend - processos (4 horas)

**Objetivo:** Mesclar processos/{core,workflow,context}.ts em processos.ts único

**Arquivos afetados:**
- `frontend/src/stores/processos.ts` (modificar)
- `frontend/src/stores/processos/core.ts` (remover)
- `frontend/src/stores/processos/workflow.ts` (remover)
- `frontend/src/stores/processos/context.ts` (remover)

**Passos:**

```bash
# 1. Backup
cp -r frontend/src/stores/processos frontend/src/stores/processos.backup

# 2. Criar store consolidado
cat frontend/src/stores/processos/core.ts \
    frontend/src/stores/processos/workflow.ts \
    frontend/src/stores/processos/context.ts > /tmp/merged.ts

# 3. Editar processos.ts manualmente
# - Remover agregador
# - Consolidar estado de core + workflow + context
# - Mesclar funções
# - Manter todos os métodos públicos
```

**Código de exemplo:**
```typescript
// frontend/src/stores/processos.ts (DEPOIS)
export const useProcessosStore = defineStore("processos", () => {
    // Estado consolidado
    const processosPainel = ref<Processo[]>([]);
    const processoDetalhe = ref<Processo | null>(null);
    const subprocessosElegiveis = ref<Subprocesso[]>([]);
    const lastError = ref<string | null>(null);
    const isLoading = ref(false);
    
    // CRUD (antes em core.ts)
    async function buscarProcessosPainel(filtros?: ProcessoFiltro) {
        isLoading.value = true;
        lastError.value = null;
        try {
            processosPainel.value = await processosApi.buscarPainel(filtros);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }
    
    // Workflow (antes em workflow.ts)
    async function iniciarProcesso(codigo: number) {
        // ... implementação
    }
    
    // Context (antes em context.ts)
    async function buscarSubprocessosElegiveis(codigo: number) {
        // ... implementação
    }
    
    return {
        // Estado
        processosPainel,
        processoDetalhe,
        subprocessosElegiveis,
        lastError,
        isLoading,
        
        // Ações
        buscarProcessosPainel,
        iniciarProcesso,
        buscarSubprocessosElegiveis,
        // ... todas as outras
    };
});
```

**Atualizar imports:**
```bash
# Buscar e substituir em todos os componentes
grep -r "useProcessosCoreStore\|useProcessosWorkflowStore\|useProcessosContextStore" \
     frontend/src/views frontend/src/components

# Substituir por useProcessosStore
```

**Testes:**
```bash
cd frontend
npm run test:unit -- processos
npm run typecheck
```

**Risco:** BAIXO  
**Reversível:** Sim (restaurar backup)  
**Ganho:** -3 arquivos, estado mais simples

---

### 1.2. Eliminar Composables View-Specific (1 dia)

**Objetivo:** Mover lógica de composables view-specific para as Views

**Anti-padrão identificado:**
```typescript
// ❌ composables/useProcessoView.ts
export function useProcessoView() {
    const store = useProcessosStore();
    const route = useRoute();
    const processo = computed(() => store.processoDetalhe);
    
    onMounted(() => store.buscarProcessoDetalhe(route.params.id));
    
    return { processo };
}
```

**Composables a eliminar:**
- `composables/useProcessoView.ts`
- `composables/useUnidadeView.ts`
- `composables/useVisAtividades.ts`
- `composables/useVisMapa.ts`
- `composables/useAtividadeForm.ts`
- `composables/useProcessoForm.ts`
- `composables/useCadAtividades.ts`
- `composables/useModalManager.ts` (substituir por useModal genérico)
- `composables/useLoadingManager.ts` (usar reactive do Vue)
- `composables/useApi.ts` (desnecessário)

**Exemplo de migração:**

```vue
<!-- ANTES: ProcessoView.vue -->
<script setup lang="ts">
import { useProcessoView } from '@/composables/useProcessoView';
const { processo } = useProcessoView();
</script>

<!-- DEPOIS: ProcessoView.vue -->
<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useProcessosStore } from '@/stores/processos';

const store = useProcessosStore();
const route = useRoute();

const processo = computed(() => store.processoDetalhe);

onMounted(async () => {
    await store.buscarProcessoDetalhe(Number(route.params.id));
});
</script>
```

**Composables GENÉRICOS a criar/manter:**

```typescript
// composables/useModal.ts (GENÉRICO)
export function useModal(initialState = false) {
    const isOpen = ref(initialState);
    
    function open() {
        isOpen.value = true;
    }
    
    function close() {
        isOpen.value = false;
    }
    
    function toggle() {
        isOpen.value = !isOpen.value;
    }
    
    return { isOpen, open, close, toggle };
}

// composables/useForm.ts (GENÉRICO)
export function useForm<T>(initialData: T, validationRules?: ValidationRules<T>) {
    const formData = reactive<T>({ ...initialData });
    const errors = ref<Record<string, string>>({});
    const isSubmitting = ref(false);
    
    async function submit(onSubmit: (data: T) => Promise<void>) {
        // ... validação e submit genérico
    }
    
    function reset() {
        Object.assign(formData, initialData);
        errors.value = {};
    }
    
    return { formData, errors, isSubmitting, submit, reset };
}
```

**Testes:**
```bash
npm run test:unit
npm run typecheck
```

**Risco:** BAIXO  
**Ganho:** -10 arquivos, lógica mais clara nas Views

---

### 1.3. Consolidar OrganizacaoServices (2 dias)

**Objetivo:** Reduzir 9 services para 3

**Estado atual:**
```
organizacao/service/
├── AdministradorService.java (52 LOC)
├── HierarquiaService.java (60 LOC)
├── UnidadeConsultaService.java (40 LOC)
├── UnidadeHierarquiaService.java (253 LOC)
├── UnidadeMapaService.java (64 LOC)
├── UnidadeResponsavelService.java (187 LOC)
├── UsuarioConsultaService.java (51 LOC)
├── UsuarioPerfilService.java (32 LOC)
└── ValidadorDadosOrgService.java (170 LOC)
```

**Estado desejado:**
```
organizacao/service/
├── OrganizacaoService.java (~300 LOC)
├── GestaoUsuariosService.java (~200 LOC)
└── ResponsabilidadeService.java (~100 LOC = renomear UnidadeResponsavelService)
```

**Passo a passo:**

**Etapa 1: Criar OrganizacaoService**

```java
// organizacao/service/OrganizacaoService.java
@Service
@RequiredArgsConstructor
public class OrganizacaoService {
    
    private final UnidadeRepo unidadeRepo;
    private final VwUnidadeRepo vwUnidadeRepo;
    private final ValidadorDadosOrgService validador; // Manter temporariamente
    
    // === Consultas (antes em UnidadeConsultaService) ===
    
    public VwUnidade buscarPorCodigo(Long codigo) {
        return vwUnidadeRepo.findById(codigo)
            .orElseThrow(() -> new ErroNegocio("Unidade não encontrada"));
    }
    
    public List<VwUnidade> buscarTodas() {
        return vwUnidadeRepo.findAll();
    }
    
    public List<VwUnidade> buscarPorTipo(TipoUnidade tipo) {
        return vwUnidadeRepo.findByTipo(tipo);
    }
    
    // === Hierarquia (antes em UnidadeHierarquiaService) ===
    
    public UnidadeHierarquia montarArvore() {
        var raiz = buscarUnidadeRaiz();
        return montarArvoreRecursiva(raiz);
    }
    
    public List<VwUnidade> buscarSubordinadas(Long codigoUnidadeSuperior) {
        return vwUnidadeRepo.findByUnidadeSuperiorCodigo(codigoUnidadeSuperior);
    }
    
    // === Verificação de subordinação (antes em HierarquiaService) ===
    
    public boolean isSubordinada(Long codigoUnidade, Long codigoPossibleSuperior) {
        var unidade = buscarPorCodigo(codigoUnidade);
        while (unidade.unidadeSuperiorCodigo() != null) {
            if (unidade.unidadeSuperiorCodigo().equals(codigoPossibleSuperior)) {
                return true;
            }
            unidade = buscarPorCodigo(unidade.unidadeSuperiorCodigo());
        }
        return false;
    }
    
    public List<Long> buscarCodigosHierarquia(Long codigoUnidade) {
        List<Long> codigos = new ArrayList<>();
        var unidade = buscarPorCodigo(codigoUnidade);
        codigos.add(unidade.codigo());
        
        while (unidade.unidadeSuperiorCodigo() != null) {
            unidade = buscarPorCodigo(unidade.unidadeSuperiorCodigo());
            codigos.add(unidade.codigo());
        }
        
        return codigos;
    }
    
    // === Mapas (antes em UnidadeMapaService) ===
    
    public MapaCompetencia buscarMapaVigente(Long codigoUnidade) {
        // ... implementação
    }
    
    // === Métodos privados ===
    
    private VwUnidade buscarUnidadeRaiz() {
        return buscarPorCodigo(UnidadeRaiz.CODIGO);
    }
    
    private UnidadeHierarquia montarArvoreRecursiva(VwUnidade unidade) {
        var subordinadas = buscarSubordinadas(unidade.codigo());
        var filhos = subordinadas.stream()
            .map(this::montarArvoreRecursiva)
            .toList();
        return new UnidadeHierarquia(unidade, filhos);
    }
}
```

**Etapa 2: Criar GestaoUsuariosService**

```java
// organizacao/service/GestaoUsuariosService.java
@Service
@RequiredArgsConstructor
public class GestaoUsuariosService {
    
    private final UsuarioRepo usuarioRepo;
    private final VwUsuarioRepo vwUsuarioRepo;
    private final VwUsuarioPerfilUnidadeRepo vwUsuarioPerfilUnidadeRepo;
    private final AdministradorRepo administradorRepo;
    
    // === Consultas de usuários (antes em UsuarioConsultaService) ===
    
    public VwUsuario buscarPorTitulo(String titulo) {
        return vwUsuarioRepo.findById(titulo)
            .orElseThrow(() -> new ErroNegocio("Usuário não encontrado"));
    }
    
    public List<VwUsuario> buscarPorUnidade(Long codigoUnidade) {
        return vwUsuarioRepo.findByUnidadeCompCodigo(codigoUnidade);
    }
    
    // === Perfis (antes em UsuarioPerfilService) ===
    
    public List<GrantedAuthority> carregarAuthorities(String titulo) {
        var perfisUnidades = vwUsuarioPerfilUnidadeRepo
            .findByUsuarioTitulo(titulo);
        
        return perfisUnidades.stream()
            .map(pu -> new SimpleGrantedAuthority("ROLE_" + pu.perfil()))
            .collect(Collectors.toList());
    }
    
    public List<PerfilUnidade> buscarPerfisUnidades(String titulo) {
        return vwUsuarioPerfilUnidadeRepo.findByUsuarioTitulo(titulo)
            .stream()
            .map(pu -> new PerfilUnidade(pu.perfil(), pu.unidadeCodigo()))
            .toList();
    }
    
    // === Administradores (antes em AdministradorService) ===
    
    public void adicionarAdministrador(String titulo) {
        if (administradorRepo.existsById(titulo)) {
            throw new ErroNegocio("Usuário já é administrador");
        }
        
        var usuario = buscarPorTitulo(titulo); // Validar existência
        var admin = new Administrador(titulo);
        administradorRepo.save(admin);
    }
    
    public void removerAdministrador(String titulo) {
        administradorRepo.deleteById(titulo);
    }
    
    public List<VwUsuario> listarAdministradores() {
        var titulosAdmins = administradorRepo.findAll()
            .stream()
            .map(Administrador::getUsuarioTitulo)
            .toList();
        
        return vwUsuarioRepo.findAllById(titulosAdmins);
    }
}
```

**Etapa 3: Renomear UnidadeResponsavelService**

```bash
# Renomear arquivo
mv organizacao/service/UnidadeResponsavelService.java \
   organizacao/service/ResponsabilidadeService.java

# Renomear classe no arquivo
sed -i 's/UnidadeResponsavelService/ResponsabilidadeService/g' \
   organizacao/service/ResponsabilidadeService.java
```

**Etapa 4: Atualizar Facades**

```java
// organizacao/facade/UnidadeFacade.java
@Service
@RequiredArgsConstructor
public class UnidadeFacade {
    
    // ANTES
    // private final UnidadeConsultaService unidadeConsulta;
    // private final UnidadeHierarquiaService unidadeHierarquia;
    // private final HierarquiaService hierarquia;
    // private final UnidadeMapaService unidadeMapa;
    
    // DEPOIS
    private final OrganizacaoService organizacaoService;
    private final ResponsabilidadeService responsabilidadeService;
    
    public UnidadeHierarquia buscarArvoreUnidades() {
        return organizacaoService.montarArvore();
    }
    
    public boolean isSubordinada(Long codigo, Long superior) {
        return organizacaoService.isSubordinada(codigo, superior);
    }
    
    // ... outros métodos
}
```

**Etapa 5: Remover services antigos**

```bash
rm organizacao/service/AdministradorService.java
rm organizacao/service/HierarquiaService.java
rm organizacao/service/UnidadeConsultaService.java
rm organizacao/service/UnidadeHierarquiaService.java
rm organizacao/service/UnidadeMapaService.java
rm organizacao/service/UsuarioConsultaService.java
rm organizacao/service/UsuarioPerfilService.java
```

**Etapa 6: Migrar testes**

```bash
# Consolidar testes
cat organizacao/service/UnidadeConsultaServiceTest.java \
    organizacao/service/UnidadeHierarquiaServiceTest.java \
    organizacao/service/HierarquiaServiceTest.java \
    > organizacao/service/OrganizacaoServiceTest.java.new

# Editar manualmente para remover duplicatas
```

**Testes:**
```bash
./gradlew :backend:test --tests "*organizacao*"
./gradlew :backend:test --tests "*UnidadeFacade*"
```

**Risco:** MÉDIO  
**Reversível:** Sim (com trabalho)  
**Ganho:** -6 arquivos, +coesão

---

## 🟡 FASE 2: Simplificação Estrutural (10 dias, MÉDIO risco)

### 2.1. Remover Facades Pass-Through (2 dias)

**Facades a eliminar:**
- AlertaFacade → AlertaService
- AnaliseFacade → AnaliseService
- ConfiguracaoFacade → ConfiguracaoService

**Exemplo: AlertaFacade**

**ANTES:**
```java
// Controller → Facade → Service
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {
    private final AlertaFacade alertaFacade;
    
    @GetMapping
    public List<AlertaDto> buscar(Authentication auth) {
        var usuario = (UsuarioAutenticado) auth.getPrincipal();
        return alertaFacade.buscarAlertas(usuario.getTitulo());
    }
}

// AlertaFacade.java (pass-through!)
@Service
@RequiredArgsConstructor
public class AlertaFacade {
    private final AlertaService alertaService;
    
    public List<AlertaDto> buscarAlertas(String titulo) {
        return alertaService.buscarAlertas(titulo); // ← Delegação pura!
    }
}
```

**DEPOIS:**
```java
// Controller → Service (direto)
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {
    private final AlertaService alertaService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<AlertaDto> buscar(Authentication auth) {
        var usuario = (UsuarioAutenticado) auth.getPrincipal();
        return alertaService.buscarAlertas(usuario.getTitulo());
    }
}

// AlertaFacade.java → DELETAR
```

**Passos:**
1. Identificar todos os Controllers que usam a Facade
2. Substituir injeção de Facade por Service
3. Atualizar chamadas de métodos
4. Deletar Facade
5. Rodar testes

**Repetir para:** AnaliseFacade, ConfiguracaoFacade

**Risco:** BAIXO-MÉDIO  
**Ganho:** -3 facades (~260 LOC)

---

### 2.2. Introduzir @JsonView (3 dias) ⚠️

**ATENÇÃO:** Esta mudança requer mais cuidado!

**Objetivo:** Substituir DTOs simples por @JsonView

**Identificar DTOs candidatos:**
```bash
# DTOs que SÃO candidatos (estrutura duplicada):
grep -r "class.*Response.*{" backend/src/main/java/sgc --include="*.java" | \
  grep -v "Aggregated\|Combined\|Transformed"

# DTOs que NÃO SÃO candidatos (transformação real):
# - ProcessoComSubprocessosResponse (agregação)
# - MapaEstatisticasResponse (cálculo)
# - RelatorioConsolidadoResponse (transformação)
```

**Exemplo de conversão:**

**ANTES:**
```java
// Processo.java (Entity)
@Entity
public class Processo {
    @Id
    private Long codigo;
    private String nome;
    private TipoProcesso tipo;
    private Situacao situacao;
    @Column(name = "observacoes_internas")
    private String observacoesInternas; // ← Campo sensível
}

// ProcessoResponse.java (DTO)
public record ProcessoResponse(
    Long codigo,
    String nome,
    String tipo,
    String situacao
) {}

// ProcessoMapper.java (MapStruct)
@Mapper
public interface ProcessoMapper {
    ProcessoResponse toResponse(Processo processo);
}

// Controller
@GetMapping("/{codigo}")
public ProcessoResponse buscar(@PathVariable Long codigo) {
    var processo = processoService.buscar(codigo);
    return processoMapper.toResponse(processo); // ← Mapeamento manual
}
```

**DEPOIS:**
```java
// Processo.java (Entity com @JsonView)
@Entity
public class Processo {
    
    // Definir views
    public static class Views {
        public interface Public {}
        public interface Admin extends Public {}
    }
    
    @Id
    @JsonView(Views.Public.class)
    private Long codigo;
    
    @JsonView(Views.Public.class)
    private String nome;
    
    @JsonView(Views.Public.class)
    private TipoProcesso tipo;
    
    @JsonView(Views.Public.class)
    private Situacao situacao;
    
    @Column(name = "observacoes_internas")
    @JsonView(Views.Admin.class) // ← Só ADMIN vê
    private String observacoesInternas;
}

// ProcessoResponse.java → DELETAR
// ProcessoMapper.java → DELETAR

// Controller (retorna Entity)
@GetMapping("/{codigo}")
@JsonView(Processo.Views.Public.class) // ← Define view
public Processo buscar(@PathVariable Long codigo) {
    return processoService.buscar(codigo); // ← Retorna entity direto!
}

// Para Admin
@GetMapping("/admin/{codigo}")
@PreAuthorize("hasRole('ADMIN')")
@JsonView(Processo.Views.Admin.class) // ← Admin vê tudo
public Processo buscarComDetalhes(@PathVariable Long codigo) {
    return processoService.buscar(codigo);
}
```

**Testes necessários:**
```java
// ProcessoControllerTest.java
@Test
void buscar_deveRetornarApenasPublicFields() throws Exception {
    var processo = new Processo(1L, "Processo 1", MAPEAMENTO, CRIADO);
    processo.setObservacoesInternas("Confidencial");
    
    when(processoService.buscar(1L)).thenReturn(processo);
    
    mockMvc.perform(get("/api/processos/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Processo 1"))
        .andExpect(jsonPath("$.observacoesInternas").doesNotExist()); // ← Não expõe!
}

@Test
@WithMockUser(roles = "ADMIN")
void buscarComDetalhes_adminDeveVerTudo() throws Exception {
    var processo = new Processo(1L, "Processo 1", MAPEAMENTO, CRIADO);
    processo.setObservacoesInternas("Confidencial");
    
    when(processoService.buscar(1L)).thenReturn(processo);
    
    mockMvc.perform(get("/api/processos/admin/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.observacoesInternas").value("Confidencial")); // ← Admin vê!
}
```

**Migrar gradualmente:**
1. Processo (10 DTOs → @JsonView)
2. Subprocesso (15 DTOs → @JsonView)
3. Atividade (8 DTOs → @JsonView)
4. Competencia (5 DTOs → @JsonView)

**Risco:** MÉDIO-ALTO (serialização é crítica)  
**Ganho:** -15 DTOs (~750 LOC)

---

### 2.3. Consolidar SubprocessoServices (2 dias)

**Objetivo:** 8 services → 3 services

**Ver exemplo detalhado em LEIA-ME-COMPLEXIDADE-V2.md seção 2️⃣**

**Risco:** MÉDIO  
**Ganho:** -5 services

---

## ✅ Checklist Pós-Implementação

Após cada fase:

- [ ] Todos os testes passam (`./gradlew test`, `npm run test:unit`)
- [ ] Cobertura de testes ≥ anterior
- [ ] Build sem warnings (`./gradlew build`, `npm run build`)
- [ ] Typecheck OK (`npm run typecheck`)
- [ ] Lint OK (`npm run lint`)
- [ ] Code review aprovado
- [ ] Documentação atualizada (package-info.java, README)
- [ ] ADR criado se mudança arquitetural significativa

---

## 📊 Métricas de Sucesso

Medir ANTES e DEPOIS de cada fase:

```bash
# Contar arquivos
find backend/src/main/java/sgc -name "*.java" | wc -l
find frontend/src -name "*.ts" -o -name "*.vue" | wc -l

# Linhas de código
cloc backend/src/main/java/sgc
cloc frontend/src

# Tempo de build
time ./gradlew build
time npm run build

# Tempo de testes
time ./gradlew test
time npm run test:unit
```

**Expectativas:**
- Fase 1: -19 arquivos, -15% LOC em módulos afetados
- Fase 2: -23 arquivos, -20% LOC adicional
- Total: -42 arquivos (~15% do total)

---

## 🚨 Troubleshooting

**Problema:** Testes quebram após consolidação
- **Causa:** Mocks apontam para services antigos
- **Solução:** Atualizar `@Mock` e `when()` para novos services

**Problema:** @JsonView não funciona
- **Causa:** `@EnableWebMvc` pode desabilitar
- **Solução:** Verificar `WebMvcConfigurer`, adicionar `ObjectMapper` configurado

**Problema:** Performance degradou
- **Causa:** N+1 queries após consolidação
- **Solução:** Adicionar `@EntityGraph` ou `JOIN FETCH`

---

**Dúvidas?** Consulte LEIA-ME-COMPLEXIDADE-V2.md ou abra issue no GitHub.

**Boa sorte! 🚀**
