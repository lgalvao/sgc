# Análise de Melhorias do Frontend - SGC

## Sumário Executivo

Este documento apresenta uma análise profunda do frontend do projeto SGC, identificando vestígios de quando era um protótipo e propondo melhorias para simplificar, modernizar e transferir responsabilidades adequadas para o backend.

**Principais Achados:**
- ✅ Boa arquitetura modular com separação de responsabilidades
- ⚠️ Complexidade desnecessária em composables compostos
- ⚠️ Lógica de negócio e validação que deveria estar no backend
- ⚠️ Duplicação de código entre stores e composables
- ⚠️ Formatação e transformação de dados que poderia ser feita pelo backend
- ⚠️ Estado distribuído sem fonte única de verdade

---

## 1. Problemas Identificados

### 1.1. Complexidade Excessiva nos Composables

**Problema:** Composables orquestradores que agregam 5-6 outros composables, criando dependências complexas e difíceis de testar.

**Exemplos:**

#### `useCadAtividadesLogic.ts` (73 linhas)
```typescript
export function useCadAtividadesLogic(props) {
    const state = useCadAtividadesState();
    const modais = useCadAtividadesModais();
    const validacao = useCadAtividadesValidacao();
    const crud = useCadAtividadesCrud(adicionarAtividadeAction);
    // ... orquestra 5 composables diferentes
}
```

**Arquivos afetados:**
- `/frontend/src/composables/useCadAtividadesLogic.ts` (73 linhas)
- `/frontend/src/composables/useCadAtividadesState.ts` (75 linhas)
- `/frontend/src/composables/useCadAtividadesCrud.ts` (137 linhas)
- `/frontend/src/composables/useCadAtividadesModais.ts` (64 linhas)
- `/frontend/src/composables/useCadAtividadesValidacao.ts` (136 linhas)
- `/frontend/src/composables/useVisMapaLogic.ts` (66 linhas)
- `/frontend/src/composables/useVisMapaCrud.ts` (117 linhas)
- `/frontend/src/composables/useVisMapaModais.ts` (95 linhas)
- `/frontend/src/composables/useVisMapaState.ts` (74 linhas)
- `/frontend/src/composables/useVisAtividadesLogic.ts` (64 linhas)
- `/frontend/src/composables/useVisAtividadesCrud.ts` (107 linhas)
- `/frontend/src/composables/useVisAtividadesModais.ts` (83 linhas)
- `/frontend/src/composables/useVisAtividadesState.ts` (99 linhas)

**Total:** ~1.190 linhas em 13 composables interconectados

**Impacto:**
- ❌ Dificulta manutenção e testes
- ❌ Cria acoplamento desnecessário
- ❌ Duplica lógica entre diferentes "logic" composables

---

### 1.2. Lógica de Negócio no Frontend

**Problema:** Validações, regras de negócio e formatações complexas que deveriam ser responsabilidade do backend.

#### 1.2.1. Validações Complexas no Frontend

**Arquivo:** `/frontend/src/composables/useCadAtividadesValidacao.ts` (136 linhas)

```typescript
// Validação complexa que deveria estar no backend
async function validarCadastro() {
    errosValidacao.value = {};
    const errosAPI = await subprocessosStore.validarCadastro(codSubprocesso);
    
    if (errosAPI && errosAPI.length > 0) {
        errosAPI.forEach((erro: any) => {
            if (erro.codigo) {
                if (!errosValidacao.value[erro.codigo]) {
                    errosValidacao.value[erro.codigo] = [];
                }
                errosValidacao.value[erro.codigo].push(erro.mensagem);
            }
        });
        return false;
    }
    return true;
}
```

**Outros exemplos:**
- Validação de email e senha em `/frontend/src/utils/validators.ts`
- Validação de datas futuras em `/frontend/src/utils/dateUtils.ts`
- Validação de CPF (formatação) em `/frontend/src/utils/formatters.ts`

**Impacto:**
- ❌ Duplicação de lógica (validação no front + backend)
- ❌ Risco de inconsistência entre validações
- ❌ Mais código para manter

---

#### 1.2.2. Formatação de Dados que Deveria Ser Backend

**Arquivo:** `/frontend/src/utils/formatters.ts` (50 linhas)

```typescript
export function formatarSituacaoProcesso(situacao: SituacaoProcesso | string): string {
    switch (situacao) {
        case SituacaoProcesso.CRIADO: return "Criado";
        case SituacaoProcesso.FINALIZADO: return "Finalizado";
        case SituacaoProcesso.EM_ANDAMENTO: return "Em andamento";
        default: return situacao;
    }
}

export function formatarTipoProcesso(tipo: TipoProcesso | string): string {
    switch (tipo) {
        case TipoProcesso.MAPEAMENTO: return "Mapeamento";
        case TipoProcesso.REVISAO: return "Revisão";
        case TipoProcesso.DIAGNOSTICO: return "Diagnóstico";
        default: return tipo;
    }
}
```

**Arquivo:** `/frontend/src/utils/statusUtils.ts` (42 linhas)

```typescript
export function situacaoLabel(situacao?: string | null): string {
    const backendLabels: Record<string, string> = {
        NAO_INICIADO: "Não iniciado",
        MAPEAMENTO_CADASTRO_EM_ANDAMENTO: "Cadastro em andamento",
        MAPEAMENTO_CADASTRO_DISPONIBILIZADO: "Cadastro disponibilizado",
        // ... 30+ mapeamentos manuais
    };
    // ...
}
```

**Problema:** O backend envia enums/códigos e o frontend precisa traduzi-los manualmente. Isso deveria vir do backend já formatado ou com labels associados.

**Impacto:**
- ❌ Duplicação de lógica de apresentação
- ❌ Manutenção em dois lugares quando adicionar novos status
- ❌ Risco de inconsistência nas traduções

---

#### 1.2.3. Transformação de Dados (Mappers)

**Arquivos:**
- `/frontend/src/mappers/atividades.ts` (66 linhas)
- `/frontend/src/mappers/mapas.ts` (77 linhas)
- `/frontend/src/mappers/processos.ts` (27 linhas)
- `/frontend/src/mappers/sgrh.ts` (97 linhas)
- `/frontend/src/mappers/unidades.ts` (59 linhas)
- `/frontend/src/mappers/usuarios.ts` (40 linhas)

**Total:** ~366 linhas de código de mapeamento

**Exemplo:** `/frontend/src/mappers/sgrh.ts`

```typescript
export function mapPerfilUnidadeToFrontend(perfilUnidadeDto: PerfilUnidadeDto): PerfilUnidade {
    return {
        perfil: perfilUnidadeDto.perfil as Perfil,
        unidade: {
            codigo: perfilUnidadeDto.unidade.codigo,
            nome: perfilUnidadeDto.unidade.nome,
            sigla: perfilUnidadeDto.unidade.sigla,
        },
        siglaUnidade: perfilUnidadeDto.siglaUnidade,
    };
}
```

**Problema:** Muitos mappers fazem transformações triviais (renomeação de campos, aninhamento/desaninhamento). O backend deveria retornar os dados já no formato esperado pelo frontend.

**Impacto:**
- ❌ Código boilerplate desnecessário
- ❌ Performance (transformações em runtime)
- ❌ Manutenção duplicada quando mudar estrutura de dados

---

### 1.3. Duplicação de Estado e Lógica

**Problema:** Estado e lógica duplicados entre Stores e Composables.

#### 1.3.1. Estado Distribuído

**Stores:**
- `processos.ts` (79 linhas) + `/processos/context.ts` (47 linhas) + `/processos/core.ts` (102 linhas) + `/processos/workflow.ts` (167 linhas) = 395 linhas
- `subprocessos.ts` (263 linhas)
- `atividades.ts` (153 linhas)
- `mapas.ts` (152 linhas)

**Composables de Estado:**
- `useCadAtividadesState.ts` (75 linhas)
- `useVisMapaState.ts` (74 linhas)
- `useVisAtividadesState.ts` (99 linhas)

**Problema:** 
- Stores Pinia já gerenciam estado global
- Composables de estado (`*State.ts`) criam camada adicional que frequentemente apenas reexporta dados das stores
- Duplicação de computeds e refs

**Exemplo:** `useCadAtividadesState.ts`

```typescript
export function useCadAtividadesState() {
    const subprocessosStore = useSubprocessosStore();
    const router = useRouter();
    
    // Apenas reexporta dados da store com computeds adicionais
    const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
    const nomeUnidade = computed(() => subprocessosStore.subprocessoDetalhe?.nomeUnidade);
    // ... mais 10 computeds similares
}
```

**Impacto:**
- ❌ Indireção desnecessária
- ❌ Confusão sobre fonte única de verdade
- ❌ Mais código para manter

---

### 1.4. Gerenciamento de Modais Complexo

**Problema:** Composables dedicados apenas para controlar visibilidade de modais.

**Arquivos:**
- `/frontend/src/composables/useCadAtividadesModais.ts` (64 linhas)
- `/frontend/src/composables/useVisMapaModais.ts` (95 linhas)
- `/frontend/src/composables/useVisAtividadesModais.ts` (83 linhas)
- `/frontend/src/composables/useModalManager.ts` (126 linhas)

**Total:** ~368 linhas dedicadas a gerenciar modais

**Exemplo:** `useCadAtividadesModais.ts`

```typescript
export function useCadAtividadesModais() {
    const mostrarModalImpacto = ref(false);
    const mostrarModalImportar = ref(false);
    const mostrarModalConfirmacao = ref(false);
    const mostrarModalHistorico = ref(false);
    const mostrarModalConfirmacaoRemocao = ref(false);
    
    // 10+ funções para abrir/fechar modais
    const abrirModalImpacto = () => { /* ... */ };
    const fecharModalImpacto = () => { mostrarModalImpacto.value = false; };
    // ...
}
```

**Problema:**
- A maioria das funções é trivial (apenas toggle de um ref booleano)
- Poderia ser simplificado com um único `useModalManager` genérico

**Impacto:**
- ❌ Código boilerplate excessivo
- ❌ Padrão inconsistente (alguns usam ref direto, outros composable)

---

### 1.5. Utilitários que Poderiam Ser Backend

**Problema:** Funções utilitárias que duplicam lógica ou poderiam ser feitas pelo backend.

#### 1.5.1. Utilitários de Data

**Arquivo:** `/frontend/src/utils/dateUtils.ts` (100 linhas)

```typescript
export function parseDate(dateInput: DateInput): Date | null {
    // Parsing complexo com múltiplos formatos
    // ISO Date/DateTime, DD/MM/YYYY, numeric string...
}

export function formatDateBR(date: DateInput, pattern = "dd/MM/yyyy"): string {
    // Formatação para pt-BR
}

export function isDateValidAndFuture(date: DateInput): boolean {
    // Validação de data futura
}
```

**Problema:** 
- Backend deveria retornar datas em formato ISO padrão
- Frontend deveria apenas formatar para exibição (não fazer parsing complexo)
- Validações de regra de negócio (data futura) deveriam estar no backend

---

#### 1.5.2. Utilitários de CSV

**Arquivo:** `/frontend/src/utils/csv.ts` (60 linhas)

```typescript
export function exportToCSV(data: any[], filename: string, columns?: string[]) {
    // Geração de CSV no frontend
}
```

**Problema:** Exportação de CSV deveria ser endpoint no backend (melhor performance, acesso a dados completos, formatação consistente).

---

### 1.6. Views Grandes e Monolíticas

**Problema:** Views com muita lógica e tamanho excessivo.

**Arquivos maiores:**
- `CadProcesso.vue` (460 linhas)
- `CadMapa.vue` (382 linhas)
- `ConfiguracoesView.vue` (346 linhas)
- `ProcessoView.vue` (324 linhas)
- `VisMapa.vue` (312 linhas)
- `RelatoriosView.vue` (296 linhas)
- `CadAtividades.vue` (273 linhas)
- `LoginView.vue` (271 linhas)

**Total:** 8 views com 250+ linhas = ~2.664 linhas

**Problema:**
- Views muito grandes são difíceis de manter
- Misturam template, lógica de apresentação e orquestração
- Deveriam ser quebradas em componentes menores

**Exemplo:** `CadProcesso.vue` tem 460 linhas incluindo:
- Formulário complexo com múltiplos campos
- Validação inline
- Gestão de árvore de unidades
- Alerta de erros
- Lógica de submit

**Deveria ser quebrado em:**
- `ProcessoFormFields.vue` - Campos do formulário
- `ProcessoUnidadesSelector.vue` - Seleção de unidades
- `ProcessoFormErrors.vue` - Exibição de erros
- `CadProcesso.vue` - Orquestração (ficaria com ~100 linhas)

---

### 1.7. Gerenciamento de Loading Complexo

**Arquivo:** `/frontend/src/composables/useLoadingManager.ts` (171 linhas)

```typescript
export function useLoadingManager(names: string[]): LoadingManager {
    const states: Record<string, Ref<boolean>> = {};
    names.forEach(name => { states[name] = ref(false); });
    
    // 7 métodos para gerenciar loading
    const start = (name: string) => { /* ... */ };
    const stop = (name: string) => { /* ... */ };
    const isLoading = (name: string) => { /* ... */ };
    const anyLoading = computed(() => { /* ... */ });
    const stopAll = () => { /* ... */ };
    const withLoading = async <T>(name: string, fn: () => Promise<T>) => { /* ... */ };
}

export function useSingleLoading(initialValue = false) {
    // Versão simplificada com 50+ linhas
}
```

**Problema:**
- Over-engineering para algo que poderia ser um simples `ref<boolean>`
- A maioria dos usos só precisa de um único loading state
- O pattern `withLoading` poderia ser uma função standalone

**Impacto:**
- ❌ Complexidade desnecessária
- ❌ 171 linhas para algo que poderia ser 20 linhas

---

### 1.8. Código de Testes com Helpers Complexos

**Problema:** Infraestrutura de testes também herdou complexidade do protótipo.

**Arquivos:**
- `/frontend/src/test-utils/componentTestHelpers.ts`
- `/frontend/src/test-utils/serviceTestHelpers.ts`
- `/frontend/src/test-utils/storeTestHelpers.ts`
- `/frontend/src/test-utils/helpers.ts`
- `/frontend/src/test-utils/a11yTestHelpers.ts`
- `/frontend/src/test-utils/uiHelpers.ts`

**Problema:** Muitos helpers de teste que poderiam ser simplificados com melhores padrões de teste ou uso de bibliotecas consolidadas.

---

## 2. Oportunidades de Melhoria no Backend

### 2.1. Backend Poderia Retornar Dados Formatados

**Oportunidade:** O backend já tem 76 DTOs. Deveria incluir campos formatados para evitar lógica de apresentação no frontend.

**Exemplo:**

#### Atual (Backend → Frontend)
```java
// Backend
public class SubprocessoDetalheResponse {
    private Long codigo;
    private String situacao; // "MAPEAMENTO_CADASTRO_EM_ANDAMENTO"
    private LocalDate dataLimite; // "2024-12-31"
}
```

```typescript
// Frontend precisa traduzir
const label = situacaoLabel(subprocesso.situacao); // "Cadastro em andamento"
const dataFormatada = formatDateBR(subprocesso.dataLimite); // "31/12/2024"
```

#### Proposto
```java
// Backend
public class SubprocessoDetalheResponse {
    private Long codigo;
    private String situacao; // "MAPEAMENTO_CADASTRO_EM_ANDAMENTO"
    private String situacaoLabel; // "Cadastro em andamento" ✅
    private LocalDate dataLimite;
    private String dataLimiteFormatada; // "31/12/2024" ✅
}
```

```typescript
// Frontend apenas usa
<span>{{ subprocesso.situacaoLabel }}</span>
<span>{{ subprocesso.dataLimiteFormatada }}</span>
```

---

### 2.2. Endpoints para Exportação

**Oportunidade:** Adicionar endpoints REST para exportação de relatórios.

**Atual:** Frontend gera CSV com `/frontend/src/utils/csv.ts`

**Proposto:**
```java
@GetMapping("/api/relatorios/{tipo}/export")
public ResponseEntity<byte[]> exportarRelatorio(@PathVariable String tipo) {
    byte[] csvData = relatorioService.gerarCSV(tipo);
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=relatorio.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(csvData);
}
```

---

### 2.3. Validação Centralizada

**Oportunidade:** Backend já tem Bean Validation. Deveria retornar erros estruturados e frontend apenas exibir.

**Atual:** Frontend valida + Backend valida = duplicação

**Proposto:**
- Backend faz TODA a validação
- Retorna erros estruturados por campo
- Frontend apenas exibe (sem lógica de validação)

---

## 3. Plano de Ação Detalhado

### 3.1. FASE 1: Simplificação Imediata (Sem Backend)

#### 3.1.1. Consolidar Composables Fragmentados

**Objetivo:** Reduzir 13 composables interconectados para 3-4 composables coesos.

**Passos:**

1. **Eliminar composables de estado (`*State.ts`)**
   - Mover computeds diretamente para composables de lógica ou views
   - Usar stores diretamente
   - **Economia:** ~248 linhas

2. **Simplificar gerenciamento de modais**
   - Usar refs booleanos diretamente nas views
   - Ou criar um único `useModal(name)` genérico de 20 linhas
   - Eliminar `useCadAtividadesModais`, `useVisMapaModais`, `useVisAtividadesModais`
   - **Economia:** ~242 linhas

3. **Consolidar lógica de CRUD**
   - Mesclar `*Logic` + `*Crud` em um único composable
   - Exemplo: `useCadAtividades.ts` (em vez de `useCadAtividadesLogic` + `useCadAtividadesCrud`)
   - **Economia:** ~200 linhas (redução de duplicação)

**Resultado Esperado:**
- De 13 composables (1.190 linhas) para 4-5 composables (~600 linhas)
- **Redução: ~590 linhas (~50%)**

---

#### 3.1.2. Simplificar useLoadingManager

**Objetivo:** Reduzir complexidade de 171 linhas para ~20 linhas.

**Implementação:**

```typescript
// De 171 linhas para:
export function useLoading() {
    const isLoading = ref(false);
    const withLoading = async <T>(fn: () => Promise<T>): Promise<T> => {
        try {
            isLoading.value = true;
            return await fn();
        } finally {
            isLoading.value = false;
        }
    };
    return { isLoading, withLoading };
}
```

**Resultado:**
- **Redução: ~150 linhas**

---

#### 3.1.3. Quebrar Views Grandes

**Objetivo:** Reduzir views de 250+ linhas para <150 linhas cada.

**Estratégia:**

Para cada view grande:
1. Extrair seções repetitivas em componentes
2. Separar formulários complexos em subcomponentes
3. Isolar modais em componentes dedicados

**Exemplo: CadProcesso.vue (460 linhas → ~150 linhas)**

Extrair:
- `ProcessoForm.vue` (campos do formulário) - ~150 linhas
- `UnidadeTreeSelector.vue` (árvore de seleção) - ~100 linhas
- `FormErrorAlert.vue` (alerta de erros) - ~30 linhas
- `CadProcesso.vue` (orquestração) - ~150 linhas

**Resultado Esperado:**
- 8 views (2.664 linhas) → ~1.200 linhas (após extração de componentes)
- **Redução: ~1.464 linhas (~55%)**

---

### 3.2. FASE 2: Integração com Backend (Mudanças no Backend + Frontend)

#### 3.2.1. Backend: Adicionar Campos Formatados nos DTOs

**Backend - Exemplo:**

```java
// ProcessoDetalheResponse.java
public class ProcessoDetalheResponse {
    private Long codigo;
    private String descricao;
    
    // Campos existentes
    private String tipo; // "MAPEAMENTO"
    private LocalDate dataLimite;
    private String situacao;
    
    // NOVOS campos formatados
    private String tipoLabel; // "Mapeamento"
    private String dataLimiteFormatada; // "31/12/2024"
    private String situacaoLabel; // "Em andamento"
    
    // Getters/Setters/Builder
}
```

**Service Layer:**
```java
@Service
public class ProcessoService {
    public ProcessoDetalheResponse buscarDetalhe(Long codigo) {
        Processo processo = repository.findById(codigo);
        return ProcessoDetalheResponse.builder()
            .codigo(processo.getCodigo())
            .tipo(processo.getTipo().name())
            .tipoLabel(processo.getTipo().getLabel()) // ✅
            .dataLimite(processo.getDataLimite())
            .dataLimiteFormatada(formatarData(processo.getDataLimite())) // ✅
            .situacao(processo.getSituacao().name())
            .situacaoLabel(processo.getSituacao().getLabel()) // ✅
            .build();
    }
    
    private String formatarData(LocalDate data) {
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
```

**Enum com Labels:**
```java
public enum TipoProcesso {
    MAPEAMENTO("Mapeamento"),
    REVISAO("Revisão"),
    DIAGNOSTICO("Diagnóstico");
    
    private final String label;
    
    TipoProcesso(String label) { this.label = label; }
    
    public String getLabel() { return label; }
}
```

**Frontend - Simplificação:**

Antes:
```typescript
// Precisava formatar
const tipoFormatado = formatarTipoProcesso(processo.tipo);
const dataFormatada = formatDateBR(processo.dataLimite);
const situacaoFormatada = situacaoLabel(processo.situacao);
```

Depois:
```vue
<template>
  <span>{{ processo.tipoLabel }}</span>
  <span>{{ processo.dataLimiteFormatada }}</span>
  <span>{{ processo.situacaoLabel }}</span>
</template>
```

**Arquivos a Modificar:**

Backend:
- `ProcessoDetalheResponse.java`
- `SubprocessoDetalheResponse.java`
- `AtividadeVisualizacaoDto.java`
- `MapaCompletoDto.java`
- Enums: `TipoProcesso`, `SituacaoProcesso`, `SituacaoSubprocesso`

Frontend (Deletar/Simplificar):
- `utils/formatters.ts` (50 linhas → 0 linhas)
- `utils/statusUtils.ts` (42 linhas → 0 linhas)
- `utils/dateUtils.ts` (100 linhas → 30 linhas - manter apenas parseDate para inputs)

**Resultado:**
- **Redução Frontend: ~162 linhas**
- **Adição Backend: ~50 linhas** (enums + DTOs)
- **Ganho líquido: +112 linhas eliminadas no frontend**

---

#### 3.2.2. Backend: Endpoint de Exportação de CSV

**Backend:**

```java
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {
    
    @GetMapping("/{tipo}/export")
    public ResponseEntity<byte[]> exportarCSV(@PathVariable String tipo) {
        byte[] csv = relatorioService.gerarCSV(tipo);
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=" + tipo + ".csv")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csv);
    }
}

@Service
public class RelatorioService {
    public byte[] gerarCSV(String tipo) {
        List<Map<String, String>> dados = buscarDados(tipo);
        return CSVWriter.write(dados);
    }
}
```

**Frontend - Simplificação:**

Antes:
```typescript
// utils/csv.ts - 60 linhas de geração manual
export function exportToCSV(data: any[], filename: string) {
    const csv = generateCSV(data);
    const blob = new Blob([csv], { type: 'text/csv' });
    downloadBlob(blob, filename);
}
```

Depois:
```typescript
// services/relatorioService.ts
export async function exportarRelatorio(tipo: string) {
    const response = await api.get(`/relatorios/${tipo}/export`, {
        responseType: 'blob'
    });
    const url = URL.createObjectURL(response.data);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${tipo}.csv`;
    a.click();
}
```

**Arquivos a Modificar:**

Backend (Criar):
- `RelatorioController.java` (+30 linhas)
- `RelatorioService.java` (+80 linhas)
- `CSVWriter.java` (utilitário, +50 linhas)

Frontend (Deletar/Simplificar):
- `utils/csv.ts` (60 linhas → 0 linhas)
- `services/relatorioService.ts` (atualizar método de exportação)

**Resultado:**
- **Redução Frontend: 60 linhas**
- **Adição Backend: 160 linhas**
- **Benefícios:**
  - ✅ CSV gerado no servidor (melhor performance)
  - ✅ Acesso a dados completos sem limitações do frontend
  - ✅ Formatação consistente

---

#### 3.2.3. Backend: Validação Centralizada com Erros Estruturados

**Backend:**

```java
// GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex
    ) {
        Map<String, List<String>> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = error.getDefaultMessage();
            errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
        });
        
        return ResponseEntity.badRequest().body(
            new ValidationErrorResponse("Erros de validação", errors)
        );
    }
}

// ValidationErrorResponse.java
public class ValidationErrorResponse {
    private String mensagem;
    private Map<String, List<String>> errosPorCampo;
    // getters/setters
}

// CadastroRequest.java
public class CadastroAtividadeRequest {
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 5, max = 500, message = "Descrição deve ter entre 5 e 500 caracteres")
    private String descricao;
    
    @NotNull(message = "Código do subprocesso é obrigatório")
    private Long codigoSubprocesso;
    
    // Validações customizadas
    @ValidDataFutura(message = "Data deve ser futura")
    private LocalDate dataLimite;
}
```

**Frontend - Simplificação:**

Antes:
```typescript
// useCadAtividadesValidacao.ts - 136 linhas de lógica complexa
const errosValidacao = ref<Record<number, string[]>>({});

async function validarCadastro() {
    errosValidacao.value = {};
    const errosAPI = await subprocessosStore.validarCadastro(codSubprocesso);
    
    if (errosAPI && errosAPI.length > 0) {
        errosAPI.forEach((erro: any) => {
            if (erro.codigo) {
                if (!errosValidacao.value[erro.codigo]) {
                    errosValidacao.value[erro.codigo] = [];
                }
                errosValidacao.value[erro.codigo].push(erro.mensagem);
            }
        });
        return false;
    }
    return true;
}
```

Depois:
```typescript
// composables/useFormValidation.ts - 30 linhas genéricas
export function useFormValidation() {
    const fieldErrors = ref<Record<string, string[]>>({});
    
    function handleValidationError(error: any) {
        if (error.response?.data?.errosPorCampo) {
            fieldErrors.value = error.response.data.errosPorCampo;
        }
    }
    
    function clearError(field: string) {
        delete fieldErrors.value[field];
    }
    
    return { fieldErrors, handleValidationError, clearError };
}
```

**Arquivos a Modificar:**

Backend (Criar/Atualizar):
- `GlobalExceptionHandler.java` (+40 linhas)
- `ValidationErrorResponse.java` (+20 linhas)
- `ValidDataFutura.java` (validator customizado, +30 linhas)
- Adicionar `@Valid` em todos os `@RequestBody`

Frontend (Deletar/Simplificar):
- `composables/useCadAtividadesValidacao.ts` (136 linhas → 0 linhas, mover para genérico)
- `composables/useFormValidation.ts` (criar genérico, 30 linhas)
- `utils/validators.ts` (20 linhas → 0 linhas)

**Resultado:**
- **Redução Frontend: ~126 linhas**
- **Adição Backend: ~90 linhas**
- **Ganho: +36 linhas eliminadas**
- **Benefícios:**
  - ✅ Validação única e consistente
  - ✅ Erros estruturados e padronizados
  - ✅ Menos duplicação

---

#### 3.2.4. Eliminar Mappers Triviais

**Problema:** Muitos mappers fazem transformações triviais que poderiam ser evitadas com DTOs bem estruturados.

**Exemplo Atual:**

```typescript
// mappers/sgrh.ts
export function mapPerfilUnidadeToFrontend(dto: PerfilUnidadeDto): PerfilUnidade {
    return {
        perfil: dto.perfil as Perfil,
        unidade: {
            codigo: dto.unidade.codigo,
            nome: dto.unidade.nome,
            sigla: dto.unidade.sigla,
        },
        siglaUnidade: dto.siglaUnidade,
    };
}
```

**Solução:**

1. **Backend retorna DTOs já no formato esperado**
2. **Frontend usa interfaces que correspondem aos DTOs** (sem transformação)

```typescript
// types/dtos.ts (correspondência 1:1 com backend)
export interface PerfilUnidadeDto {
    perfil: Perfil;
    unidade: {
        codigo: number;
        nome: string;
        sigla: string;
    };
    siglaUnidade: string;
}

// Sem mapeamento! Usa diretamente:
const perfilUnidade: PerfilUnidadeDto = await api.get('/perfis');
```

**Arquivos a Revisar:**

- `mappers/sgrh.ts` (97 linhas) - Verificar quais podem ser eliminados
- `mappers/usuarios.ts` (40 linhas)
- `mappers/unidades.ts` (59 linhas)
- `mappers/processos.ts` (27 linhas)

**Manter apenas mappers que fazem transformações complexas:**
- `mappers/atividades.ts` (se houver lógica não-trivial)
- `mappers/mapas.ts` (se houver lógica não-trivial)

**Resultado Esperado:**
- **Redução: ~150-200 linhas** (eliminando mappers triviais)

---

### 3.3. FASE 3: Modernização e Otimização

#### 3.3.1. Migrar de BootstrapVue para Biblioteca Moderna

**Contexto:** Projeto usa BootstrapVueNext (port community-driven do BootstrapVue para Vue 3).

**Problemas:**
- Suporte limitado
- Bundle size grande
- Componentes verbosos

**Opções:**

1. **PrimeVue** (recomendado)
   - Componentes ricos e modernos
   - Tree component nativo (importante para unidades/processos)
   - Tema customizável
   - Excelente documentação

2. **Vuetify**
   - Material Design
   - Componentes completos
   - Grande comunidade

3. **Naive UI**
   - Leve e performático
   - TypeScript first
   - Componentes bonitos

**Recomendação: PrimeVue**

Motivos:
- ✅ Tree components nativos (usado em unidades)
- ✅ Melhor bundle size que BootstrapVue
- ✅ Componentes de formulário robustos
- ✅ Tema TRE customizável

**Impacto:**
- Refatoração de ~26 componentes
- Simplificação de markup (componentes PrimeVue são mais concisos)
- **Estimativa: -500 linhas de código** (markup simplificado)

---

#### 3.3.2. Implementar Virtual Scrolling para Listas Grandes

**Problema:** Componentes como `TabelaProcessos` podem ter performance issues com muitos registros.

**Solução:**

Usar `vue-virtual-scroller` ou componente de tabela da biblioteca de UI (PrimeVue tem DataTable com virtual scrolling).

**Exemplo:**
```vue
<template>
  <DataTable 
    :value="processos" 
    :virtualScrollerOptions="{ itemSize: 50 }"
    scrollable
  >
    <!-- columns -->
  </DataTable>
</template>
```

**Benefício:**
- ✅ Renderiza apenas itens visíveis
- ✅ Melhor performance com grandes listas

---

#### 3.3.3. Lazy Loading de Rotas

**Problema:** Todas as views são carregadas no bundle principal.

**Solução:**

```typescript
// router/index.ts
const routes = [
    {
        path: '/processo/:id',
        component: () => import('@/views/ProcessoView.vue'), // lazy
    },
    {
        path: '/cadastro/processo',
        component: () => import('@/views/CadProcesso.vue'), // lazy
    },
    // ...
];
```

**Benefício:**
- ✅ Bundle inicial menor
- ✅ Carrega views sob demanda
- **Estimativa: Redução de 30-40% no bundle inicial**

---

## 4. Cronograma de Implementação

### Sprint 1-2: Fase 1 (Simplificação Imediata)
**Duração:** 2 semanas  
**Objetivo:** Reduzir complexidade sem tocar no backend

- [ ] Consolidar composables (useCadAtividades*, useVisMapa*, useVisAtividades*)
- [ ] Simplificar useLoadingManager
- [ ] Quebrar views grandes (CadProcesso, CadMapa, ProcessoView, etc)
- [ ] Revisar e limpar código morto
- [ ] Atualizar testes

**Resultado Esperado:** ~1.200 linhas removidas

---

### Sprint 3-4: Fase 2.1 (Backend: Campos Formatados)
**Duração:** 2 semanas  
**Objetivo:** Backend retorna dados formatados

**Backend:**
- [ ] Adicionar labels em enums (TipoProcesso, SituacaoProcesso, etc)
- [ ] Atualizar DTOs com campos formatados (*Label, *Formatada)
- [ ] Atualizar Services para popular novos campos
- [ ] Testes de integração

**Frontend:**
- [ ] Remover utils/formatters.ts
- [ ] Remover utils/statusUtils.ts
- [ ] Simplificar utils/dateUtils.ts
- [ ] Atualizar templates para usar campos formatados
- [ ] Atualizar testes

**Resultado Esperado:** ~162 linhas removidas no frontend

---

### Sprint 5: Fase 2.2 (Backend: Exportação CSV)
**Duração:** 1 semana  
**Objetivo:** Backend gera exportações

**Backend:**
- [ ] Criar RelatorioController
- [ ] Criar RelatorioService
- [ ] Criar CSVWriter utility
- [ ] Endpoints para cada tipo de relatório
- [ ] Testes

**Frontend:**
- [ ] Remover utils/csv.ts
- [ ] Atualizar relatorioService para usar endpoints
- [ ] Atualizar views de relatórios
- [ ] Testes

**Resultado Esperado:** ~60 linhas removidas no frontend

---

### Sprint 6-7: Fase 2.3 (Backend: Validação Centralizada)
**Duração:** 2 semanas  
**Objetivo:** Validação apenas no backend

**Backend:**
- [ ] Criar GlobalExceptionHandler
- [ ] Criar ValidationErrorResponse
- [ ] Adicionar @Valid em todos os endpoints
- [ ] Criar validadores customizados (@ValidDataFutura, etc)
- [ ] Testes

**Frontend:**
- [ ] Criar useFormValidation genérico
- [ ] Remover useCadAtividadesValidacao e similares
- [ ] Remover utils/validators.ts
- [ ] Atualizar formulários para usar validação genérica
- [ ] Testes

**Resultado Esperado:** ~126 linhas removidas no frontend

---

### Sprint 8: Fase 2.4 (Eliminar Mappers Triviais)
**Duração:** 1 semana  
**Objetivo:** Alinhar interfaces frontend com DTOs backend

**Backend:**
- [ ] Revisar DTOs para eliminar necessidade de mapeamento
- [ ] Ajustar estrutura de dados se necessário

**Frontend:**
- [ ] Revisar mappers/*
- [ ] Eliminar mappers triviais
- [ ] Alinhar types/dtos.ts com backend
- [ ] Atualizar imports
- [ ] Testes

**Resultado Esperado:** ~150-200 linhas removidas

---

### Sprint 9-12: Fase 3 (Modernização)
**Duração:** 4 semanas  
**Objetivo:** Atualizar stack técnico

**Tarefas:**
- [ ] Avaliar e decidir biblioteca de UI (PrimeVue recomendado)
- [ ] Setup PrimeVue/tema
- [ ] Migrar componentes incrementalmente
- [ ] Implementar lazy loading de rotas
- [ ] Implementar virtual scrolling em listas grandes
- [ ] Otimizar bundle
- [ ] Testes de regressão
- [ ] Documentação

**Resultado Esperado:** 
- ~500 linhas de markup simplificado
- Bundle 30-40% menor
- Melhor performance

---

## 5. Métricas de Sucesso

### Redução de Código

| Fase | Linhas Removidas | % Redução |
|------|-----------------|-----------|
| Fase 1: Simplificação | ~1.200 | ~20% |
| Fase 2.1: Formatação Backend | ~162 | ~3% |
| Fase 2.2: CSV Backend | ~60 | ~1% |
| Fase 2.3: Validação Backend | ~126 | ~2% |
| Fase 2.4: Mappers | ~150 | ~2.5% |
| Fase 3: Modernização | ~500 | ~8% |
| **TOTAL** | **~2.198** | **~35%** |

### Qualidade de Código

**Antes:**
- 6.000+ linhas de código frontend (estimativa)
- 13 composables interconectados
- 366 linhas de mappers
- 8 views com 250+ linhas
- Duplicação de validação (frontend + backend)

**Depois:**
- ~4.000 linhas de código frontend
- 4-5 composables coesos
- ~150 linhas de mappers (apenas complexos)
- Views com <150 linhas
- Validação centralizada no backend

### Performance

**Bundle Size:**
- Atual: ~500KB (estimativa)
- Meta: ~300KB (-40%)

**Tempo de Carregamento:**
- Atual: ~2s
- Meta: ~1.2s (-40%)

---

## 6. Riscos e Mitigações

### Risco 1: Quebra de Compatibilidade com Backend

**Mitigação:**
- Fazer mudanças incrementais
- Manter versionamento de API
- Criar testes de integração end-to-end
- Deploy coordenado de backend + frontend

### Risco 2: Regressão em Funcionalidades

**Mitigação:**
- Manter cobertura de testes >90%
- Executar testes e2e antes de cada merge
- Revisão de código rigorosa
- QA manual de funcionalidades críticas

### Risco 3: Curva de Aprendizado com Nova Biblioteca UI

**Mitigação:**
- Migração gradual (componente por componente)
- Documentação interna
- Treinamento da equipe
- Manter BootstrapVue em paralelo durante transição

---

## 7. Conclusão

O frontend do SGC, apesar de bem arquitetado, carrega vestígios significativos de sua origem como protótipo. As principais oportunidades de melhoria incluem:

1. **Simplificação de Composables:** Reduzir complexidade e indireção desnecessária
2. **Transferência de Responsabilidades:** Mover formatação, validação e transformação para o backend
3. **Modernização:** Atualizar biblioteca de UI e otimizar bundle
4. **Componentização:** Quebrar views grandes em componentes menores e reutilizáveis

**Impacto Total Estimado:**
- ✅ **~35% de redução de código** (~2.200 linhas)
- ✅ **40% de redução no bundle size**
- ✅ **Melhoria significativa em manutenibilidade**
- ✅ **Eliminação de duplicação de lógica**
- ✅ **Centralização de responsabilidades**

A implementação gradual proposta permite realizar melhorias contínuas sem grandes riscos, com entregas de valor a cada sprint.

---

## 8. Anexos

### Anexo A: Inventário de Arquivos por Categoria

#### Composables (25 arquivos)
```
useApi.ts
useAtividadeForm.ts
useBreadcrumbs.ts (142 linhas)
useCadAtividadesCrud.ts (137 linhas) ⚠️
useCadAtividadesLogic.ts (73 linhas) ⚠️
useCadAtividadesModais.ts (64 linhas) ⚠️
useCadAtividadesState.ts (75 linhas) ⚠️
useCadAtividadesValidacao.ts (136 linhas) ⚠️
useErrorHandler.ts
useFormErrors.ts
useLoadingManager.ts (171 linhas) ⚠️
useLocalStorage.ts (71 linhas)
useModalManager.ts (126 linhas)
usePerfil.ts (51 linhas)
useProcessoForm.ts (91 linhas)
useVisAtividadesCrud.ts (107 linhas) ⚠️
useVisAtividadesLogic.ts (64 linhas) ⚠️
useVisAtividadesModais.ts (83 linhas) ⚠️
useVisAtividadesState.ts (99 linhas) ⚠️
useVisMapaCrud.ts (117 linhas) ⚠️
useVisMapaLogic.ts (66 linhas) ⚠️
useVisMapaModais.ts (95 linhas) ⚠️
useVisMapaState.ts (74 linhas) ⚠️
```

⚠️ = Candidato a simplificação/consolidação

#### Utils (11 arquivos)
```
apiError.ts (complexidade média)
apiUtils.ts
csv.ts (60 linhas) ⚠️ Mover para backend
dateUtils.ts (100 linhas) ⚠️ Simplificar
formatters.ts (50 linhas) ⚠️ Remover (backend faz)
index.ts
logger.ts
statusUtils.ts (42 linhas) ⚠️ Remover (backend faz)
styleUtils.ts
treeUtils.ts
validators.ts (19 linhas) ⚠️ Remover (backend valida)
```

#### Mappers (7 arquivos, 384 linhas)
```
alertas.ts (18 linhas)
atividades.ts (66 linhas) - Revisar
mapas.ts (77 linhas) - Revisar
processos.ts (27 linhas) ⚠️ Eliminar
sgrh.ts (97 linhas) ⚠️ Eliminar
unidades.ts (59 linhas) ⚠️ Eliminar
usuarios.ts (40 linhas) ⚠️ Eliminar
```

#### Stores (14 arquivos, ~1.816 linhas)
```
alertas.ts (69 linhas)
analises.ts (56 linhas)
atividades.ts (153 linhas)
atribuicoes.ts (93 linhas)
configuracoes.ts (75 linhas)
diagnosticos.ts (125 linhas)
feedback.ts (60 linhas)
mapas.ts (152 linhas)
perfil.ts (163 linhas)
processos.ts (79 linhas)
  ├── processos/context.ts (47 linhas)
  ├── processos/core.ts (102 linhas)
  └── processos/workflow.ts (167 linhas)
subprocessos.ts (263 linhas)
unidades.ts (122 linhas)
usuarios.ts (90 linhas)
```

#### Views (19 arquivos, ~5.978 linhas)
```
AutoavaliacaoDiagnostico.vue (197 linhas)
CadAtividades.vue (273 linhas) ⚠️
CadAtribuicao.vue (183 linhas)
CadMapa.vue (382 linhas) ⚠️
CadProcesso.vue (460 linhas) ⚠️
ConclusaoDiagnostico.vue (120 linhas)
ConfiguracoesView.vue (346 linhas) ⚠️
HistoricoView.vue (105 linhas)
LoginView.vue (271 linhas) ⚠️
MonitoramentoDiagnostico.vue (175 linhas)
OcupacoesCriticasDiagnostico.vue (173 linhas)
PainelView.vue (143 linhas)
ProcessoView.vue (324 linhas) ⚠️
RelatoriosView.vue (296 linhas) ⚠️
SubprocessoView.vue (219 linhas)
UnidadeView.vue (253 linhas) ⚠️
VisAtividades.vue (246 linhas) ⚠️
VisMapa.vue (312 linhas) ⚠️
```

⚠️ = Views com 250+ linhas que devem ser quebradas

---

### Anexo B: Checklist de Implementação

#### Fase 1: Simplificação

- [ ] Consolidar `useCadAtividades*` em um único composable
- [ ] Consolidar `useVisMapa*` em um único composable
- [ ] Consolidar `useVisAtividades*` em um único composable
- [ ] Simplificar `useLoadingManager` para versão minimalista
- [ ] Quebrar `CadProcesso.vue` em subcomponentes
- [ ] Quebrar `CadMapa.vue` em subcomponentes
- [ ] Quebrar `ConfiguracoesView.vue` em subcomponentes
- [ ] Quebrar `ProcessoView.vue` em subcomponentes
- [ ] Quebrar `VisMapa.vue` em subcomponentes
- [ ] Quebrar `RelatoriosView.vue` em subcomponentes
- [ ] Quebrar `UnidadeView.vue` em subcomponentes
- [ ] Quebrar `VisAtividades.vue` em subcomponentes
- [ ] Executar testes e garantir 90%+ cobertura
- [ ] Code review completo

#### Fase 2.1: Formatação no Backend

- [ ] Backend: Adicionar getLabel() em TipoProcesso enum
- [ ] Backend: Adicionar getLabel() em SituacaoProcesso enum
- [ ] Backend: Adicionar getLabel() em SituacaoSubprocesso enum
- [ ] Backend: Atualizar ProcessoDetalheResponse com campos *Label
- [ ] Backend: Atualizar SubprocessoDetalheResponse com campos *Label
- [ ] Backend: Criar utility DateFormatter para pt-BR
- [ ] Backend: Atualizar DTOs com campos *Formatada para datas
- [ ] Backend: Testes unitários de formatação
- [ ] Backend: Testes de integração dos endpoints
- [ ] Frontend: Remover utils/formatters.ts
- [ ] Frontend: Remover utils/statusUtils.ts
- [ ] Frontend: Simplificar utils/dateUtils.ts
- [ ] Frontend: Atualizar templates para usar *Label e *Formatada
- [ ] Frontend: Atualizar types/dtos.ts
- [ ] Frontend: Atualizar testes

#### Fase 2.2: Exportação CSV no Backend

- [ ] Backend: Criar RelatorioController
- [ ] Backend: Criar RelatorioService com lógica de geração
- [ ] Backend: Criar CSVWriter utility
- [ ] Backend: Endpoint GET /api/relatorios/processos/export
- [ ] Backend: Endpoint GET /api/relatorios/atividades/export
- [ ] Backend: Endpoint GET /api/relatorios/diagnosticos/export
- [ ] Backend: Testes de endpoints
- [ ] Frontend: Remover utils/csv.ts
- [ ] Frontend: Atualizar relatorioService com novos métodos
- [ ] Frontend: Atualizar RelatoriosView.vue
- [ ] Frontend: Testes

#### Fase 2.3: Validação no Backend

- [ ] Backend: Criar GlobalExceptionHandler
- [ ] Backend: Criar ValidationErrorResponse DTO
- [ ] Backend: Criar @ValidDataFutura annotation
- [ ] Backend: Adicionar @Valid em ProcessoController endpoints
- [ ] Backend: Adicionar @Valid em SubprocessoController endpoints
- [ ] Backend: Adicionar @Valid em AtividadeController endpoints
- [ ] Backend: Bean Validation em todos os *Request DTOs
- [ ] Backend: Testes de validação
- [ ] Frontend: Criar useFormValidation genérico
- [ ] Frontend: Remover useCadAtividadesValidacao.ts
- [ ] Frontend: Remover utils/validators.ts
- [ ] Frontend: Atualizar formulários
- [ ] Frontend: Testes

#### Fase 2.4: Eliminar Mappers

- [ ] Backend: Revisar ProcessoDetalheResponse
- [ ] Backend: Revisar SubprocessoDetalheResponse
- [ ] Backend: Revisar AtividadeVisualizacaoDto
- [ ] Backend: Alinhar estrutura de dados com frontend
- [ ] Frontend: Eliminar mappers/processos.ts
- [ ] Frontend: Eliminar mappers/sgrh.ts
- [ ] Frontend: Eliminar mappers/unidades.ts
- [ ] Frontend: Eliminar mappers/usuarios.ts
- [ ] Frontend: Revisar mappers/atividades.ts (manter se complexo)
- [ ] Frontend: Revisar mappers/mapas.ts (manter se complexo)
- [ ] Frontend: Atualizar types/dtos.ts
- [ ] Frontend: Atualizar imports
- [ ] Frontend: Testes

#### Fase 3: Modernização

- [ ] Decisão: Escolher biblioteca UI (PrimeVue recomendado)
- [ ] Setup PrimeVue e tema TRE
- [ ] Migrar FormInput/FormSelect
- [ ] Migrar Buttons/Alerts
- [ ] Migrar Tables/DataTables
- [ ] Migrar Modals
- [ ] Migrar ArvoreUnidades para PrimeVue Tree
- [ ] Implementar lazy loading de rotas
- [ ] Implementar virtual scrolling em tabelas grandes
- [ ] Otimizar imports (tree shaking)
- [ ] Análise de bundle size
- [ ] Testes de regressão completos
- [ ] Testes de performance
- [ ] Documentação de migração

---

**Documento criado em:** 2026-02-02  
**Autor:** Análise Automatizada SGC  
**Versão:** 1.0
