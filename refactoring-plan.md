# Plano de Refatoração do Frontend SGC

**Versão:** 1.0  
**Data:** 07 de dezembro de 2025  
**Objetivo:** Remover lógica de negócio, validações complexas, filtragens e ordenações desnecessárias do frontend, transferindo essas responsabilidades para o backend, mantendo o frontend como um client otimizado e limpo.

---

## Sumário Executivo

Após análise detalhada dos 14 views e 23 componentes Vue.js, identificamos múltiplas áreas onde o frontend contém lógica que deveria estar no backend. Este documento detalha todas as mudanças necessárias para criar uma separação clara de responsabilidades entre frontend e backend, garantindo que:

1. **Backend:** Gerencia regras de negócio, validações, filtragens complexas, ordenações e cálculos
2. **Frontend:** Foca em apresentação, interação do usuário e comunicação com a API

**Total de Linhas Analisadas:** ~8.065 linhas em componentes e views  
**Áreas Críticas Identificadas:** 47 pontos de melhoria

---

## 1. Validações de Negócio no Frontend

### 1.1. CadAtividades.vue

**Problema Atual:**
- **Linhas 631-635:** Validação de atividades sem conhecimento feita no frontend
- **Linhas 669-697:** Lógica complexa de validação de situação antes de disponibilizar
- **Linhas 683-694:** Validação e montagem de mensagens de erro no frontend

```typescript
// Código atual (frontend)
function validarAtividades(): Atividade[] {
  return atividades.value.filter(
    (atividade) => atividade.conhecimentos.length === 0,
  );
}

function disponibilizarCadastro() {
  // Validação de situação
  if (!sub || sub.situacaoSubprocesso !== situacaoEsperada) {
    feedbackStore.show("Ação não permitida", ...);
    return;
  }
  // Validação de atividades sem conhecimento
  atividadesSemConhecimento.value = validarAtividades();
  if (atividadesSemConhecimento.value.length > 0) {
    // Monta mensagem de erro no frontend
    const atividadesDescricoes = atividadesSemConhecimento.value
      .map((a) => `- ${a.descricao}`)
      .join("\n");
    feedbackStore.show("Atividades Incompletas", ...);
    return;
  }
}
```

**Refatoração Recomendada:**

1. **Criar endpoint no backend:** `POST /api/subprocessos/{id}/disponibilizar-cadastro`
2. **Backend deve:**
   - Validar situação do subprocesso
   - Validar completude das atividades (todas devem ter conhecimentos)
   - Retornar erro HTTP 400 com detalhes estruturados se validação falhar
   - Executar a disponibilização se tudo estiver OK

```typescript
// Código refatorado (frontend)
async function disponibilizarCadastro() {
  if (!codSubrocesso.value) return;
  
  try {
    await subprocessosStore.disponibilizarCadastro(codSubrocesso.value);
    await router.push("/painel");
  } catch (error) {
    // O backend retorna erro estruturado com detalhes
    // O interceptor do Axios já trata e exibe
  }
}
```

**Backend esperado:**
```java
// Response de erro estruturado
{
  "status": 400,
  "message": "Cadastro não pode ser disponibilizado",
  "details": {
    "atividadesSemConhecimento": [
      {"codigo": 1, "descricao": "Desenvolver APIs"},
      {"codigo": 2, "descricao": "Fazer testes"}
    ],
    "situacaoAtual": "CADASTRO_EM_ANDAMENTO",
    "situacaoEsperada": "CADASTRO_EM_ANDAMENTO"
  }
}
```

**Arquivos Afetados:**
- `frontend/src/views/CadAtividades.vue`
- `frontend/src/stores/subprocessos.ts`
- `frontend/src/services/subprocessoService.ts`
- `backend/src/main/java/sgc/subprocesso/` (novo endpoint)

---

### 1.2. CadMapa.vue

**Problema Atual:**
- **Linhas 452-453:** Validação de campos obrigatórios no frontend
- **Sem validação de negócio:** Permite criar competência sem atividades associadas

```typescript
// Código atual
async function adicionarCompetenciaEFecharModal() {
  if (
    !novaCompetencia.value.descricao ||
    atividadesSelecionadas.value.length === 0
  ) return;
  // ... continua
}
```

**Refatoração Recomendada:**

1. **Endpoint existente:** `POST /api/mapas/{id}/competencias`
2. **Backend deve validar:**
   - Descrição não vazia
   - Pelo menos uma atividade associada
   - Atividades pertencem ao mapa correto
   - Não há duplicação de competências (se aplicável)

```typescript
// Código refatorado (frontend) - simplificado
async function adicionarCompetenciaEFecharModal() {
  const competencia: Competencia = {
    codigo: competenciaSendoEditada.value?.codigo ?? undefined,
    descricao: novaCompetencia.value.descricao,
    atividadesAssociadas: atividadesSelecionadas.value,
  };

  try {
    if (competenciaSendoEditada.value) {
      await mapasStore.atualizarCompetencia(codSubrocesso.value, competencia);
    } else {
      await mapasStore.adicionarCompetencia(codSubrocesso.value, competencia);
    }
    await mapasStore.buscarMapaCompleto(codSubrocesso.value);
    fecharModal();
  } catch {
    // Erro tratado pelo interceptor
  }
}
```

**Arquivos Afetados:**
- `frontend/src/views/CadMapa.vue`
- `backend/src/main/java/sgc/mapa/dto/` (validações Bean Validation)

---

### 1.3. CadProcesso.vue

**Problema Atual:**
- **Linhas 296-315:** Validação complexa de dados no frontend antes de salvar
- **Linhas 302-305:** Filtragem de unidades elegíveis no frontend
- **Linhas 376-380:** Mesma filtragem duplicada em outro método

```typescript
// Código atual
async function salvarProcesso() {
  if (!descricao.value) {
    mostrarAlerta('danger', "Dados incompletos", "Preencha a descrição.");
    return;
  }

  // Filtragem no frontend
  const unidadesFiltradas = unidadesSelecionadas.value.filter(id => {
    const unidade = findUnidadeById(id, unidadesStore.unidades);
    return unidade && unidade.isElegivel;
  });

  if (unidadesFiltradas.length === 0) {
    mostrarAlerta('danger', "Dados incompletos", "Pelo menos uma unidade...");
    return;
  }
  // ... mais validações
}
```

**Refatoração Recomendada:**

1. **Backend deve:**
   - Validar campos obrigatórios (descrição, tipo, data limite, unidades)
   - Validar elegibilidade das unidades internamente
   - Validar data limite (não pode ser passada)
   - Retornar erro estruturado com campo específico que falhou

2. **Frontend deve:**
   - Apenas enviar os dados
   - Tratar erros genericamente

```typescript
// Código refatorado
async function salvarProcesso() {
  try {
    const request: CriarProcessoRequest = {
      descricao: descricao.value,
      tipo: tipo.value as TipoProcesso,
      dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
      unidades: unidadesSelecionadas.value, // Backend filtra elegíveis
    };
    
    if (processoEditando.value) {
      await processosStore.atualizarProcesso(processoEditando.value.codigo, request);
    } else {
      await processosStore.criarProcesso(request);
    }
    
    await router.push("/painel");
  } catch {
    // Erro tratado automaticamente
  }
}
```

**Backend esperado:**
```java
// DTO com validações
public class CriarProcessoRequest {
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
    
    @NotNull(message = "Tipo é obrigatório")
    private TipoProcesso tipo;
    
    @NotNull(message = "Data limite é obrigatória")
    @FutureOrPresent(message = "Data limite deve ser presente ou futura")
    private LocalDateTime dataLimiteEtapa1;
    
    @NotEmpty(message = "Pelo menos uma unidade deve ser selecionada")
    private List<Integer> unidades;
}

// No serviço
public Processo criarProcesso(CriarProcessoRequest request) {
    // Filtra apenas unidades elegíveis
    List<Integer> unidadesElegiveis = filtrarUnidadesElegiveis(
        request.getUnidades(), 
        request.getTipo()
    );
    
    if (unidadesElegiveis.isEmpty()) {
        throw new ErroDadosInvalidos(
            "Nenhuma unidade elegível foi selecionada para o tipo de processo " + request.getTipo()
        );
    }
    // ... continua
}
```

**Arquivos Afetados:**
- `frontend/src/views/CadProcesso.vue`
- `frontend/src/stores/processos.ts`
- `backend/src/main/java/sgc/processo/dto/CriarProcessoRequest.java`
- `backend/src/main/java/sgc/processo/ProcessoService.java`

---

### 1.4. DiagnosticoEquipe.vue

**Problema Atual:**
- **Linhas 225-230:** Validação de avaliações pendentes no frontend
- **Linha 176-186:** Inicialização com valores padrão no frontend (domínio 3, importância 3)
- **TODO na linha 243:** Comentário indica que falta implementação no backend

```typescript
// Código atual
const avaliacoesPendentes = computed(() => {
  return competencias.value.filter((comp) => {
    const aval = avaliacoes.value[comp.codigo];
    return !aval || aval.importancia === 0 || aval.dominio === 0;
  });
});

function confirmarFinalizacao() {
  // TODO: Implementar chamada real ao backend para finalizar diagnóstico
  feedbackStore.show("Diagnóstico finalizado", ...);
  router.push("/painel");
}
```

**Refatoração Recomendada:**

1. **Criar endpoints:**
   - `POST /api/diagnosticos/{subprocessoId}/avaliacoes` - Salvar avaliações individuais
   - `POST /api/diagnosticos/{subprocessoId}/finalizar` - Finalizar diagnóstico
   - `GET /api/diagnosticos/{subprocessoId}/avaliacoes` - Buscar avaliações existentes

2. **Backend deve:**
   - Validar que todas as competências foram avaliadas antes de finalizar
   - Validar que valores de importância e domínio estão entre 1-5
   - Calcular automaticamente gaps e criticidades
   - Gerar alertas e notificações automaticamente

```typescript
// Código refatorado
// Salvar avaliações incrementalmente
async function salvarAvaliacao(competenciaId: number) {
  try {
    await diagnosticoService.salvarAvaliacao(codSubprocesso.value, {
      competenciaId,
      importancia: avaliacoes.value[competenciaId].importancia,
      dominio: avaliacoes.value[competenciaId].dominio,
      observacoes: avaliacoes.value[competenciaId].observacoes,
    });
  } catch {
    // Erro tratado
  }
}

// Finalizar
async function confirmarFinalizacao() {
  try {
    await diagnosticoService.finalizarDiagnostico(codSubprocesso.value);
    await router.push("/painel");
  } catch (error) {
    // Backend retorna erro se há avaliações pendentes
  }
}
```

**Arquivos Afetados:**
- `frontend/src/views/DiagnosticoEquipe.vue`
- `frontend/src/services/` (novo `diagnosticoService.ts`)
- `backend/src/main/java/sgc/` (novo módulo `diagnostico/`)

---

### 1.5. OcupacoesCriticas.vue

**Problema Atual:**
- **Linhas 258-262:** Validação de formulário no frontend
- **Linhas 238-307:** Toda a lógica de gerenciamento de ocupações no frontend (sem backend)
- **Dados não persistidos:** Ocupações críticas são apenas locais

```typescript
// Código atual - tudo no frontend
const ocupacoesCriticas = ref<Array<{
  nome: string;
  descricao: string;
  nivelCriticidade: number;
  competenciasCriticas: string[];
}>>([]);

function adicionarOcupacao() {
  if (!novaOcupacao.value.nome.trim() || !novaOcupacao.value.descricao.trim()) {
    feedbackStore.show("Dados incompletos", ...);
    return;
  }
  ocupacoesCriticas.value.push({ ... }); // Apenas local!
}
```

**Refatoração Recomendada:**

1. **Criar endpoints:**
   - `GET /api/diagnosticos/{subprocessoId}/ocupacoes-criticas` - Listar
   - `POST /api/diagnosticos/{subprocessoId}/ocupacoes-criticas` - Criar
   - `DELETE /api/diagnosticos/{subprocessoId}/ocupacoes-criticas/{id}` - Remover
   - `POST /api/diagnosticos/{subprocessoId}/ocupacoes-criticas/finalizar` - Finalizar

2. **Backend deve:**
   - Persistir ocupações críticas no banco
   - Validar dados (nome, descrição obrigatórios, criticidade 1-5)
   - Relacionar com competências do mapa
   - Gerar relatórios baseados nas ocupações

```typescript
// Código refatorado
const ocupacoesCriticas = computed(() => 
  diagnosticoStore.ocupacoesCriticas
);

async function adicionarOcupacao() {
  try {
    await diagnosticoService.criarOcupacaoCritica(codSubprocesso.value, {
      nome: novaOcupacao.value.nome,
      descricao: novaOcupacao.value.descricao,
      nivelCriticidade: novaOcupacao.value.nivelCriticidade,
      competenciasCriticas: novaOcupacao.value.competenciasCriticas,
    });
    
    // Recarrega lista
    await diagnosticoStore.buscarOcupacoesCriticas(codSubprocesso.value);
    limparFormulario();
  } catch {
    // Erro tratado
  }
}
```

**Arquivos Afetados:**
- `frontend/src/views/OcupacoesCriticas.vue`
- `frontend/src/stores/` (novo `diagnostico.ts`)
- `frontend/src/services/diagnosticoService.ts`
- `backend/src/main/java/sgc/diagnostico/` (novo módulo completo)

---

## 2. Filtragens e Ordenações Complexas

### 2.1. ArvoreUnidades.vue

**Problema Atual:**
- **Linhas 134-147:** Filtragem complexa para ocultar SEDOC no frontend
- **Linhas 150-181:** Lógica complexa de tri-state checkbox no frontend
- **Linhas 218-267:** Algoritmo de seleção hierárquica com regras especiais para INTEROPERACIONAL

```typescript
// Código atual - lógica complexa no frontend
const unidadesExibidas = computed(() => {
  const filtradas = props.unidades.filter(props.filtrarPor);
  const lista: Unidade[] = [];
  
  for (const u of filtradas) {
    // Oculta SEDOC mas mostra filhas
    if (u.sigla === 'SEDOC' || u.codigo === 1) {
      if (u.filhas) lista.push(...u.filhas);
    } else {
      lista.push(u);
    }
  }
  return lista;
});

function toggle(unidade: Unidade, checked: boolean) {
  // Algoritmo complexo de 40+ linhas para gerenciar seleção hierárquica
  const newSelection = new Set(unidadesSelecionadasLocal.value);
  const idsToToggle = [unidade.codigo, ...getTodasSubunidades(unidade)];
  
  if (checked) {
    idsToToggle.forEach(id => {
      const unidadeParaAdicionar = findUnidadeById(id);
      if (unidadeParaAdicionar?.isElegivel) {
        newSelection.add(id);
      }
    });
  } else {
    idsToToggle.forEach(id => newSelection.delete(id));
  }
  
  updateAncestors(unidade, newSelection); // Mais 25 linhas de lógica
  // ...
}
```

**Refatoração Recomendada:**

1. **Backend deve fornecer:**
   - Endpoint: `GET /api/unidades/arvore?tipoProcesso={tipo}&ocultarRaiz=true`
   - Árvore já filtrada (sem SEDOC)
   - Flag `isElegivel` já calculada por tipo de processo
   - Metadados para simplificar a UI

2. **Frontend deve:**
   - Manter lógica visual de tri-state (é UI, não negócio)
   - Simplificar algoritmo de seleção
   - Remover filtragem manual

```typescript
// Código refatorado - simplificado
const unidadesExibidas = computed(() => {
  // Backend já retorna sem SEDOC
  return unidadesStore.arvoreUnidadesFiltrada;
});

function toggle(unidade: Unidade, checked: boolean) {
  // Lógica simplificada - apenas visual
  const newSelection = new Set(unidadesSelecionadasLocal.value);
  
  if (checked) {
    adicionarUnidadeEFilhas(unidade, newSelection);
  } else {
    removerUnidadeEFilhas(unidade, newSelection);
  }
  
  unidadesSelecionadasLocal.value = Array.from(newSelection);
}
```

**Nota:** A lógica de tri-state checkbox pode permanecer no frontend, pois é uma questão de UX/apresentação. O que deve ir para o backend são as regras de elegibilidade e filtros de negócio.

**Arquivos Afetados:**
- `frontend/src/components/ArvoreUnidades.vue`
- `frontend/src/stores/unidades.ts`
- `backend/src/main/java/sgc/unidade/UnidadeController.java`

---

### 2.2. TabelaProcessos.vue

**Problema Atual:**
- **Linhas 78-94:** Formatação de enums no frontend
- **Linhas 69-71:** Controle de ordenação no frontend

```typescript
// Código atual
function formatarSituacao(situacao: string): string {
  const mapa: Record<string, string> = {
    EM_ANDAMENTO: "Em Andamento",
    FINALIZADO: "Finalizado",
    CRIADO: "Criado",
  };
  return mapa[situacao] || situacao;
}

function formatarTipo(tipo: string): string {
  const mapa: Record<string, string> = {
    MAPEAMENTO: "Mapeamento",
    REVISAO: "Revisão",
    DIAGNOSTICO: "Diagnóstico",
  };
  return mapa[tipo] || tipo;
}
```

**Refatoração Recomendada:**

1. **Backend deve:**
   - Retornar DTOs com campos já formatados para exibição
   - Exemplo: `situacaoLabel`, `tipoLabel`
   - Suportar ordenação via query params: `?sortBy=descricao&sortOrder=asc`

```typescript
// DTO do backend
interface ProcessoResumoDTO {
  codigo: number;
  descricao: string;
  tipo: "MAPEAMENTO" | "REVISAO" | "DIAGNOSTICO";
  tipoLabel: string; // "Mapeamento", "Revisão", "Diagnóstico"
  situacao: "CRIADO" | "EM_ANDAMENTO" | "FINALIZADO";
  situacaoLabel: string; // "Criado", "Em Andamento", "Finalizado"
  unidadesParticipantes: number;
  dataFinalizacao?: string;
}
```

```typescript
// Frontend simplificado
<template #cell(situacao)="data">
  {{ data.item.situacaoLabel }}
</template>

<template #cell(tipo)="data">
  {{ data.item.tipoLabel }}
</template>
```

**Alternativa:** Manter formatação no frontend usando constantes centralizadas, pois é apresentação. Mas ordenação deve ser backend.

**Arquivos Afetados:**
- `frontend/src/components/TabelaProcessos.vue`
- `backend/src/main/java/sgc/processo/dto/ProcessoResumoDTO.java`

---

### 2.3. ProcessoView.vue

**Problema Atual:**
- **Linhas 117-140:** Formatação complexa de dados hierárquicos no frontend
- **Linha 106-111:** Definição de colunas e larguras no frontend (poderia vir do backend)

```typescript
// Código atual
function formatarDadosParaArvore(dados: UnidadeParticipante[]): TreeTableItem[] {
  if (!dados) return [];
  return dados.map((item) => ({
    id: item.codUnidade,
    nome: `${item.sigla} - ${item.nome}`,
    situacao: item.situacaoSubprocesso || "Não iniciado",
    dataLimite: formatarData(item.dataLimite || null),
    unidadeAtual: item.sigla,
    clickable: true,
    expanded: true,
    children: item.filhos ? formatarDadosParaArvore(item.filhos) : [],
  }));
}
```

**Refatoração Recomendada:**

Backend já retorna estrutura hierárquica, mas pode melhorar:

1. **Backend deve:**
   - Incluir campos formatados: `dataLimiteFormatada`, `nomeCompleto`
   - Incluir flags: `isClickable`, `isExpanded` (baseado em regras)

```typescript
// DTO melhorado do backend
interface UnidadeParticipanteDTO {
  codUnidade: number;
  sigla: string;
  nome: string;
  nomeCompleto: string; // "SIGLA - Nome"
  situacaoSubprocesso: string;
  situacaoLabel: string; // "Cadastro em andamento"
  dataLimite: string; // ISO
  dataLimiteFormatada: string; // "01/12/2025"
  isClickable: boolean; // Baseado em permissões
  filhos: UnidadeParticipanteDTO[];
}
```

```typescript
// Frontend simplificado
function formatarDadosParaArvore(dados: UnidadeParticipanteDTO[]): TreeTableItem[] {
  return dados.map(item => ({
    id: item.codUnidade,
    nome: item.nomeCompleto,
    situacao: item.situacaoLabel,
    dataLimite: item.dataLimiteFormatada,
    unidadeAtual: item.sigla,
    clickable: item.isClickable,
    expanded: true,
    children: formatarDadosParaArvore(item.filhos),
  }));
}
```

**Arquivos Afetados:**
- `frontend/src/views/ProcessoView.vue`
- `backend/src/main/java/sgc/processo/dto/UnidadeParticipanteDTO.java`

---

## 3. Lógica de Negócio e Regras Complexas

### 3.1. ImpactoMapaModal.vue

**Problema Atual:**
- **Linhas 183-194:** Formatação de tipo de impacto no frontend (deveria vir do backend)
- **Modal carrega dados mas backend já faz o cálculo** - OK, mas pode melhorar resposta

```typescript
// Código atual
function formatTipoImpacto(tipo: TipoImpactoCompetencia): string {
  switch (tipo) {
    case TipoImpactoCompetencia.ATIVIDADE_REMOVIDA:
      return "Atividade Removida";
    case TipoImpactoCompetencia.ATIVIDADE_ALTERADA:
      return "Atividade Alterada";
    // ...
  }
}
```

**Refatoração Recomendada:**

Backend já calcula impactos, mas deve retornar labels:

```typescript
// DTO melhorado
interface ImpactoMapaDTO {
  temImpactos: boolean;
  atividadesInseridas: AtividadeImpactoDTO[];
  atividadesRemovidas: AtividadeImpactoDTO[];
  atividadesAlteradas: AtividadeImpactoDTO[];
  competenciasImpactadas: CompetenciaImpactadaDTO[];
}

interface CompetenciaImpactadaDTO {
  codigo: number;
  descricao: string;
  tipoImpacto: "ATIVIDADE_REMOVIDA" | "ATIVIDADE_ALTERADA" | "IMPACTO_GENERICO";
  tipoImpactoLabel: string; // "Atividade Removida"
  atividadesAfetadas: string[];
}
```

**Arquivos Afetados:**
- `frontend/src/components/ImpactoMapaModal.vue`
- `backend/src/main/java/sgc/mapa/dto/ImpactoMapaDTO.java`

---

### 3.2. utils/index.ts

**Problema Atual:**
- **Linhas 78-153:** Parser complexo de datas em múltiplos formatos
- **Linhas 17-56:** Mapeamentos de situações e labels (duplica backend)

```typescript
// Código atual - 75+ linhas de lógica de parsing de datas
export function parseDate(dateInput: string | number | Date | null | undefined): Date | null {
  // Múltiplos formatos: ISO, timestamps, DD/MM/YYYY, etc.
  // Lógica complexa de validação e conversão
  // ...
}

// Mapeamentos duplicados
const backendLabels: Record<string, string> = {
  NAO_INICIADO: "Não iniciado",
  MAPEAMENTO_CADASTRO_EM_ANDAMENTO: "Cadastro em andamento",
  // ... 30+ linhas
};
```

**Refatoração Recomendada:**

1. **Backend deve:**
   - SEMPRE retornar datas em ISO 8601
   - SEMPRE incluir campos formatados quando necessário: `dataFormatada`, `dataHoraFormatada`
   - Incluir labels junto com enums

2. **Frontend deve:**
   - Usar `parseDate` apenas para inputs do usuário
   - Remover mapeamentos (usar DTOs do backend)
   - Simplificar utilitários de data

```typescript
// utils/index.ts refatorado - 80% menor
export function formatDateBR(isoDate: string): string {
  if (!isoDate) return "Não informado";
  return new Date(isoDate).toLocaleDateString("pt-BR");
}

export function formatDateForInput(isoDate: string): string {
  if (!isoDate) return "";
  return isoDate.split('T')[0]; // YYYY-MM-DD
}

// Mapeamentos removidos - usar DTOs do backend
```

**Arquivos Afetados:**
- `frontend/src/utils/index.ts`
- Todos os DTOs do backend (adicionar campos `*Label`, `*Formatada`)

---

### 3.3. Stores - Lógica de Transformação

**Problema Atual (Geral):**
Muitas stores fazem transformações e cálculos que poderiam vir do backend:

- `mapas.ts`: Cálculos de impacto (já no backend, mas pode melhorar)
- `processos.ts`: Filtragens de subprocessos elegíveis
- `atividades.ts`: Indexação por subprocesso (pode ser otimizada)

**Refatoração Recomendada:**

1. **Stores devem:**
   - Ser cache simples de dados do backend
   - Gerenciar estado local de UI (modais abertos, loading, etc.)
   - Chamar services e armazenar respostas

2. **Não devem:**
   - Fazer cálculos complexos
   - Filtrar dados de negócio (backend deve retornar filtrado)
   - Transformar enums (backend deve retornar labels)

**Exemplo - processos.ts:**

```typescript
// Atual - store busca e depois frontend filtra
const subprocessosElegiveis = computed(() => {
  return state.listaSubprocessosElegiveis || [];
});

// Refatorado - backend já retorna filtrados
async function buscarSubprocessosElegiveis(codProcesso: number) {
  state.listaSubprocessosElegiveis = await processoService
    .buscarSubprocessosElegiveis(codProcesso);
}
```

**Arquivos Afetados:**
- `frontend/src/stores/*.ts` (revisar todos)
- Múltiplos endpoints backend (adicionar filtros via query params)

---

## 4. Permissões e Autorizações

### 4.1. SubprocessoView.vue e ProcessoView.vue

**Problema Atual:**
- **ProcessoView.vue linhas 142-166:** Lógica de permissão de navegação no frontend
- **SubprocessoView.vue linha 96-100:** Validação de permissões localmente

```typescript
// Código atual - ProcessoView.vue
function abrirDetalhesUnidade(item: any) {
  if (item && item.clickable) {
    const perfilUsuario = perfilStore.perfilSelecionado;
    if (perfilUsuario === "ADMIN" || perfilUsuario === "GESTOR") {
      router.push({ ... });
    } else if (
      (perfilUsuario === "CHEFE" || perfilUsuario === "SERVIDOR") &&
      perfilStore.unidadeSelecionada === item.id
    ) {
      router.push({ ... });
    }
  }
}
```

**Refatoração Recomendada:**

1. **Backend deve:**
   - Incluir permissões em cada DTO: `podeVisualizar`, `podeEditar`, `podeNavegar`
   - Validar permissões em TODOS os endpoints (não confiar no frontend)

2. **Frontend deve:**
   - Usar flags do backend para habilitar/desabilitar UI
   - Router guards podem verificar permissões gerais

```typescript
// Frontend refatorado
function abrirDetalhesUnidade(item: UnidadeTreeItem) {
  if (item.permissoes.podeNavegar) {
    router.push({
      name: "Subprocesso",
      params: {
        codProcesso: codProcesso.value,
        siglaUnidade: item.unidadeAtual,
      },
    });
  }
}
```

```java
// Backend - UnidadeParticipanteDTO
public class UnidadeParticipanteDTO {
    // ... campos existentes
    
    private PermissoesUnidade permissoes;
    
    public static class PermissoesUnidade {
        private boolean podeNavegar;
        private boolean podeEditar;
        private boolean podeVisualizar;
        
        // Calculado baseado em perfil do usuário e situação
        public static PermissoesUnidade calcular(
            Perfil perfil, 
            SituacaoSubprocesso situacao,
            Integer unidadeUsuario,
            Integer unidadeTarget
        ) {
            // Lógica centralizada de permissões
        }
    }
}
```

**Arquivos Afetados:**
- `frontend/src/views/ProcessoView.vue`
- `frontend/src/views/SubprocessoView.vue`
- Todos os DTOs backend (adicionar objeto `permissoes`)
- `backend/src/main/java/sgc/comum/` (novo `PermissaoHelper.java`)

---

## 5. Formatação e Apresentação (Zona Cinzenta)

Alguns itens estão na "zona cinzenta" entre frontend e backend. Recomendações:

### 5.1. Formatação de Datas e Números

**Recomendação:** Backend retorna ISO 8601, frontend formata para exibição usando `Intl`

**Motivo:** Internacionalização futura, locale do navegador

**Exceção:** Para relatórios e exports, backend pode formatar

### 5.2. Badges e Classes CSS

**Recomendação:** Frontend mantém mapeamento de situação → classe CSS

**Motivo:** É puramente apresentação/tema visual

**Implementação:**
```typescript
// frontend/src/constants/situacoes.ts - OK manter
export const CLASSES_BADGE_SITUACAO = {
  NAO_INICIADO: "bg-secondary",
  EM_ANDAMENTO: "bg-primary",
  // ...
};
```

### 5.3. Ordenação de Tabelas

**Recomendação:** Backend implementa ordenação via query params, frontend chama endpoint

**Implementação:**
```typescript
// Frontend
async function ordenar(campo: string) {
  await processosStore.buscarProcessos({
    sortBy: campo,
    sortOrder: ordem.value,
  });
}

// Backend
@GetMapping
public Page<ProcessoResumo> listar(
    @RequestParam(required = false) String sortBy,
    @RequestParam(required = false) String sortOrder,
    Pageable pageable
) {
    // Aplica ordenação
}
```

---

## 6. Casos Especiais e TODOs Pendentes

### 6.1. TODOs Encontrados no Código

1. **DiagnosticoEquipe.vue linha 243:**
```typescript
// TODO: Implementar chamada real ao backend para finalizar diagnóstico
```
**Ação:** Criar endpoint `POST /api/diagnosticos/{id}/finalizar`

2. **OcupacoesCriticas.vue linha 298:**
```typescript
// TODO: Implementar chamada real ao backend para finalizar identificação
```
**Ação:** Criar endpoint `POST /api/diagnosticos/{id}/ocupacoes-criticas/finalizar`

3. **CadMapa.vue linha 536:**
```typescript
// TODO: Adicionar redirecionamento para o painel
```
**Ação:** Adicionar `router.push('/painel')` após sucesso

### 6.2. Módulo Diagnóstico Inexistente

**Observação:** As views `DiagnosticoEquipe.vue` e `OcupacoesCriticas.vue` não têm backend correspondente.

**Ação Requerida:**
1. Criar módulo completo: `backend/src/main/java/sgc/diagnostico/`
2. Entidades: `Diagnostico`, `AvaliacaoCompetencia`, `OcupacaoCritica`
3. Repositórios, Serviços, Controllers, DTOs
4. Integração com módulo `mapa` e `subprocesso`

**Arquivos a Criar:**
- `backend/src/main/java/sgc/diagnostico/Diagnostico.java`
- `backend/src/main/java/sgc/diagnostico/DiagnosticoService.java`
- `backend/src/main/java/sgc/diagnostico/DiagnosticoController.java`
- `backend/src/main/java/sgc/diagnostico/dto/AvaliacaoDTO.java`
- `backend/src/main/java/sgc/diagnostico/dto/OcupacaoCriticaDTO.java`

---

## 7. Resumo de Endpoints Necessários

### Novos Endpoints

| Endpoint | Método | Descrição | Prioridade |
|----------|--------|-----------|------------|
| `/api/diagnosticos/{id}/avaliacoes` | POST | Salvar avaliação de competência | Alta |
| `/api/diagnosticos/{id}/avaliacoes` | GET | Buscar avaliações existentes | Alta |
| `/api/diagnosticos/{id}/finalizar` | POST | Finalizar diagnóstico | Alta |
| `/api/diagnosticos/{id}/ocupacoes-criticas` | GET | Listar ocupações críticas | Alta |
| `/api/diagnosticos/{id}/ocupacoes-criticas` | POST | Criar ocupação crítica | Alta |
| `/api/diagnosticos/{id}/ocupacoes-criticas/{ocupacaoId}` | DELETE | Remover ocupação crítica | Alta |
| `/api/diagnosticos/{id}/ocupacoes-criticas/finalizar` | POST | Finalizar identificação | Alta |
| `/api/subprocessos/{id}/disponibilizar-cadastro` | POST | Disponibilizar cadastro (com validações) | Alta |
| `/api/unidades/arvore` | GET | Árvore filtrada com flags de elegibilidade | Média |
| `/api/processos?sortBy=&sortOrder=` | GET | Listar com ordenação | Média |

### Endpoints a Melhorar

| Endpoint Atual | Melhoria Necessária | Prioridade |
|----------------|---------------------|------------|
| `POST /api/mapas/{id}/competencias` | Adicionar validações completas | Alta |
| `POST /api/processos` | Validar elegibilidade de unidades | Alta |
| `GET /api/processos/{id}` | Incluir permissões calculadas | Alta |
| `GET /api/processos/{id}/subprocessos-elegiveis` | Já existe, documentar melhor | Baixa |
| `GET /api/mapas/{id}/impacto` | Incluir labels formatados | Média |

---

## 8. Plano de Implementação Sugerido

### Fase 1: Validações Críticas (2-3 semanas)

**Objetivo:** Mover validações de negócio para backend

1. Implementar validações em `CadAtividades.vue` → Backend
2. Implementar validações em `CadProcesso.vue` → Backend
3. Implementar validações em `CadMapa.vue` → Backend
4. Atualizar DTOs com Bean Validation
5. Criar testes unitários para validações

**Entregáveis:**
- Endpoints de cadastro validam dados completamente
- Frontend simplificado (remove 200+ linhas de validação)
- Mensagens de erro estruturadas

### Fase 2: Módulo Diagnóstico (3-4 semanas)

**Objetivo:** Criar backend para funcionalidades de diagnóstico

1. Criar entidades e repositórios
2. Criar serviços de negócio
3. Criar controllers e DTOs
4. Implementar endpoints para avaliações
5. Implementar endpoints para ocupações críticas
6. Integrar com sistema de notificações e alertas
7. Criar testes E2E

**Entregáveis:**
- Módulo `diagnostico` completo
- Views funcionando com persistência real
- Relatórios de diagnóstico

### Fase 3: Otimização de DTOs (2 semanas)

**Objetivo:** Enriquecer DTOs com dados formatados

1. Adicionar campos `*Label` em todos os enums
2. Adicionar campos `*Formatada` em datas
3. Adicionar objeto `permissoes` em DTOs principais
4. Remover mapeamentos do frontend
5. Simplificar `utils/index.ts`

**Entregáveis:**
- DTOs autocontidos
- Frontend 30% mais simples
- Menos duplicação de código

### Fase 4: Filtragens e Ordenações (1-2 semanas)

**Objetivo:** Backend fornece dados filtrados e ordenados

1. Adicionar query params de ordenação
2. Melhorar endpoint de árvore de unidades
3. Adicionar paginação onde necessário
4. Otimizar queries do banco

**Entregáveis:**
- APIs REST completas com suporte a sort/filter
- Performance melhorada
- Frontend apenas consome dados

### Fase 5: Refatoração de Stores (1 semana)

**Objetivo:** Simplificar stores

1. Remover computeds complexos
2. Mover lógica para services
3. Padronizar estrutura de stores
4. Atualizar testes

**Entregáveis:**
- Stores 40% mais simples
- Código mais manutenível
- Melhor separação de responsabilidades

---

## 9. Métricas de Sucesso

### Redução de Código Frontend

**Objetivo:** Reduzir 25-35% do código de lógica de negócio no frontend

| Arquivo | Linhas Atuais | Linhas Estimadas Pós-Refatoração | Redução |
|---------|---------------|----------------------------------|---------|
| CadAtividades.vue | 724 | ~550 | 24% |
| CadMapa.vue | 546 | ~400 | 27% |
| CadProcesso.vue | 436 | ~300 | 31% |
| DiagnosticoEquipe.vue | 262 | ~180 | 31% |
| OcupacoesCriticas.vue | 307 | ~200 | 35% |
| ArvoreUnidades.vue | 305 | ~220 | 28% |
| utils/index.ts | 235 | ~80 | 66% |
| **Total Estimado** | **~2.800** | **~1.930** | **~31%** |

### Aumento de Cobertura Backend

**Objetivo:** Garantir 80%+ cobertura de testes em novos módulos

- Validações: 100% cobertura
- Módulo diagnóstico: 85%+ cobertura
- DTOs e mappers: 90%+ cobertura

### Melhoria de Performance

**Objetivo:** Reduzir chamadas de API desnecessárias

- Menos re-fetches (dados vêm completos)
- Paginação implementada onde necessário
- Cache de dados formatados

---

## 10. Riscos e Mitigações

### Risco 1: Quebra de Funcionalidades Existentes

**Probabilidade:** Média  
**Impacto:** Alto

**Mitigação:**
- Implementar mudanças incrementalmente
- Manter testes E2E passando a cada fase
- Feature flags para novas implementações
- Testes de regressão extensivos

### Risco 2: Incompatibilidade de DTOs

**Probabilidade:** Baixa  
**Impacto:** Médio

**Mitigação:**
- Versionar endpoints se necessário
- Manter backward compatibility temporariamente
- Documentar breaking changes claramente

### Risco 3: Aumento de Complexidade Backend

**Probabilidade:** Média  
**Impacto:** Médio

**Mitigação:**
- Seguir arquitetura em camadas rigorosamente
- Documentar regras de negócio
- Code reviews obrigatórias
- Testes unitários abrangentes

---

## 11. Checklist de Implementação

### Para Cada View/Component Refatorado

- [ ] Identificar validações de negócio
- [ ] Identificar filtragens/ordenações complexas
- [ ] Identificar mapeamentos duplicados
- [ ] Criar/atualizar endpoints backend necessários
- [ ] Criar/atualizar DTOs com validações
- [ ] Implementar testes unitários backend
- [ ] Atualizar services frontend
- [ ] Simplificar componente/view
- [ ] Atualizar testes E2E
- [ ] Documentar mudanças
- [ ] Code review
- [ ] Testar em ambiente de staging

### Para Cada Novo Endpoint

- [ ] Especificar contrato (request/response)
- [ ] Implementar validações
- [ ] Implementar autorização
- [ ] Criar testes unitários
- [ ] Criar testes de integração
- [ ] Documentar no Swagger/OpenAPI
- [ ] Atualizar collection Postman/Insomnia

---

## 12. Conclusão

Este plano de refatoração visa transformar o frontend SGC de um protótipo com lógica de negócio misturada para uma aplicação production-ready com separação clara de responsabilidades.

**Benefícios Esperados:**

1. **Manutenibilidade:** Código frontend 30% mais simples e focado em UI
2. **Consistência:** Regras de negócio centralizadas no backend
3. **Performance:** Menos processamento no cliente, dados otimizados
4. **Segurança:** Validações no servidor, não contornáveis
5. **Testabilidade:** Lógica de negócio 100% testável no backend
6. **Escalabilidade:** Fácil adicionar novos clientes (mobile, API pública)

**Esforço Total Estimado:** 9-12 semanas (2-3 sprints de 3-4 semanas)

**Priorização:** Começar pela Fase 1 (validações críticas) pois tem maior impacto na segurança e qualidade.

---

**Documento elaborado por:** GitHub Copilot  
**Data de elaboração:** 07 de dezembro de 2025  
**Versão:** 1.0  
**Status:** Proposta para revisão
