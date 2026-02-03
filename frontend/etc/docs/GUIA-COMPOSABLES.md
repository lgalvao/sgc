# 🧩 Guia de Extração de Lógica para Composables - Frontend

**Data:** 2026-01-31  
**Status:** Diretrizes para Refatoração de Views

---

## 🎯 Objetivo

Este documento fornece diretrizes para extrair lógica complexa de views Vue para composables reutilizáveis, mantendo views como componentes de apresentação.

---

## 📋 Princípios

### Views Devem Ser "Burras" (Presentational)

**Responsabilidades de Views:**
- ✅ Renderizar UI baseada em estado
- ✅ Capturar eventos de usuário
- ✅ Orquestrar composables e stores
- ❌ Conter lógica de negócio complexa
- ❌ Transformações ou cálculos complexos
- ❌ Gerenciamento de estado local complexo

### Composables São "Inteligentes" (Smart)

**Responsabilidades de Composables:**
- ✅ Lógica reutilizável entre componentes
- ✅ Gerenciamento de estado local
- ✅ Transformações e cálculos
- ✅ Orquestração de múltiplas stores
- ✅ Side effects (watch, lifecycle hooks)

---

## 🔍 Identificando Lógica para Extrair

### Sinais de Alerta (Code Smells)

1. **View com 200+ linhas no `<script>`**
2. **Múltiplas funções `computed` complexas**
3. **Lógica duplicada em outras views**
4. **Muitos `ref` e `reactive` no setup**
5. **Transformações de dados complexas**
6. **Múltiplos `watch` e `watchEffect`**

### Exemplo: View com Lógica Complexa

```vue
<!-- ❌ ANTI-PADRÃO: ProcessoView.vue -->
<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useProcessosStore } from '@/stores/processos';
import type { UnidadeParticipante } from '@/types/tipos';

// 150+ linhas de lógica complexa aqui!
const route = useRoute();
const processosStore = useProcessosStore();

const mostrarModalBloco = ref(false);
const acaoBloco = ref<'aceitar' | 'homologar' | 'disponibilizar'>('aceitar');
const unidadesSelecionadas = ref<number[]>([]);

const codProcesso = computed(() => Number(route.params.codProcesso));

const unidadesElegiveis = computed(() => {
  const all = flattenTree(processo.value?.unidades || [], 'filhos');
  
  if (acaoBloco.value === 'aceitar') {
    return all.filter(u => 
      u.situacao === 'CADASTRO_DISPONIBILIZADO' ||
      u.situacao === 'MAPA_VALIDADO'
    );
  } else if (acaoBloco.value === 'homologar') {
    return all.filter(u => u.situacao === 'CADASTRO_DISPONIBILIZADO');
  } else {
    return all.filter(u => u.situacao === 'MAPA_CRIADO');
  }
});

watch(codProcesso, async (novo) => {
  if (novo) {
    await processosStore.buscarContextoCompleto(novo);
  }
});

onMounted(async () => {
  if (codProcesso.value) {
    await processosStore.buscarContextoCompleto(codProcesso.value);
  }
});

function abrirModal(acao: 'aceitar' | 'homologar' | 'disponibilizar') {
  acaoBloco.value = acao;
  mostrarModalBloco.value = true;
}

async function executarAcao() {
  await processosStore.executarAcaoBloco(
    acaoBloco.value,
    unidadesSelecionadas.value
  );
  mostrarModalBloco.value = false;
}

// ... mais 100 linhas de lógica
</script>
```

---

## ✅ Solução: Extrair para Composable

### Passo 1: Criar Composable Focado

```typescript
// composables/useProcessoAcoesBloco.ts
import { ref, computed, type Ref } from 'vue';
import type { UnidadeParticipante } from '@/types/tipos';
import { flattenTree } from '@/utils/treeUtils';

export type AcaoBloco = 'aceitar' | 'homologar' | 'disponibilizar';

export function useProcessoAcoesBloco(
  unidades: Ref<UnidadeParticipante[]>
) {
  const mostrarModal = ref(false);
  const acaoAtual = ref<AcaoBloco>('aceitar');
  const unidadesSelecionadas = ref<number[]>([]);
  
  const unidadesElegiveis = computed(() => {
    const todas = flattenTree(unidades.value, 'filhos');
    
    const filtros = {
      aceitar: (u: UnidadeParticipante) => 
        u.situacao === 'CADASTRO_DISPONIBILIZADO' ||
        u.situacao === 'MAPA_VALIDADO',
      
      homologar: (u: UnidadeParticipante) => 
        u.situacao === 'CADASTRO_DISPONIBILIZADO',
      
      disponibilizar: (u: UnidadeParticipante) => 
        u.situacao === 'MAPA_CRIADO'
    };
    
    return todas.filter(filtros[acaoAtual.value]);
  });
  
  function abrirModal(acao: AcaoBloco) {
    acaoAtual.value = acao;
    mostrarModal.value = true;
    // Pré-seleciona todas as unidades elegíveis
    unidadesSelecionadas.value = unidadesElegiveis.value.map(u => u.codigo);
  }
  
  function fecharModal() {
    mostrarModal.value = false;
    unidadesSelecionadas.value = [];
  }
  
  return {
    // Estado
    mostrarModal,
    acaoAtual,
    unidadesSelecionadas,
    
    // Computeds
    unidadesElegiveis,
    
    // Ações
    abrirModal,
    fecharModal
  };
}
```

### Passo 2: Usar Composable na View

```vue
<!-- ✅ PADRÃO CORRETO: ProcessoView.vue -->
<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useProcessosStore } from '@/stores/processos';
import { useProcessoAcoesBloco } from '@/composables/useProcessoAcoesBloco';

const route = useRoute();
const processosStore = useProcessosStore();
const { processoDetalhe } = storeToRefs(processosStore);

const codProcesso = computed(() => Number(route.params.codProcesso));

// Composable gerencia toda a lógica de ações em bloco
const acoesBloco = useProcessoAcoesBloco(
  computed(() => processoDetalhe.value?.unidades || [])
);

onMounted(async () => {
  if (codProcesso.value) {
    await processosStore.buscarContextoCompleto(codProcesso.value);
  }
});

async function executarAcaoBloco() {
  await processosStore.executarAcaoBloco(
    acoesBloco.acaoAtual.value,
    acoesBloco.unidadesSelecionadas.value
  );
  acoesBloco.fecharModal();
}
</script>

<template>
  <div>
    <BButton @click="acoesBloco.abrirModal('aceitar')">
      Aceitar em Bloco
    </BButton>
    
    <ModalAcoesBloco
      v-model="acoesBloco.mostrarModal.value"
      :unidades="acoesBloco.unidadesElegiveis.value"
      v-model:selecionadas="acoesBloco.unidadesSelecionadas.value"
      @confirmar="executarAcaoBloco"
    />
  </div>
</template>
```

---

## 📐 Tipos de Composables

### 1. State Management Composable

Gerencia estado local complexo.

```typescript
// useFormularioProcesso.ts
export function useFormularioProcesso() {
  const form = reactive({
    descricao: '',
    tipo: 'MAPEAMENTO',
    unidades: [] as number[]
  });
  
  const erros = ref<Record<string, string>>({});
  
  function validar() {
    erros.value = {};
    
    if (!form.descricao) {
      erros.value.descricao = 'Descrição é obrigatória';
    }
    
    if (form.unidades.length === 0) {
      erros.value.unidades = 'Selecione ao menos uma unidade';
    }
    
    return Object.keys(erros.value).length === 0;
  }
  
  function resetar() {
    form.descricao = '';
    form.tipo = 'MAPEAMENTO';
    form.unidades = [];
    erros.value = {};
  }
  
  return {
    form,
    erros,
    validar,
    resetar
  };
}
```

### 2. Business Logic Composable

Encapsula cálculos e transformações.

```typescript
// useCalculoGaps.ts
export function useCalculoGaps(diagnostico: Ref<DiagnosticoDto>) {
  const gapsIdentificados = computed(() => {
    return diagnostico.value.avaliacoes.filter(a => 
      a.gap !== null && a.gap > 0
    ).length;
  });
  
  const importanciaMedia = computed(() => {
    const avaliacoes = diagnostico.value.avaliacoes;
    const soma = avaliacoes.reduce((acc, a) => 
      acc + parseImportancia(a.importancia), 0
    );
    return soma / avaliacoes.length;
  });
  
  const dominioMedio = computed(() => {
    const avaliacoes = diagnostico.value.avaliacoes;
    const soma = avaliacoes.reduce((acc, a) => 
      acc + parseDominio(a.dominio), 0
    );
    return soma / avaliacoes.length;
  });
  
  const competenciasCriticas = computed(() => {
    return diagnostico.value.avaliacoes
      .filter(a => a.gap && a.gap >= 3)
      .map(a => a.competenciaDescricao);
  });
  
  return {
    gapsIdentificados,
    importanciaMedia,
    dominioMedio,
    competenciasCriticas
  };
}
```

### 3. API Orchestration Composable

Orquestra múltiplas stores/services.

```typescript
// useCarregamentoProcesso.ts
export function useCarregamentoProcesso(codigo: Ref<number>) {
  const processosStore = useProcessosStore();
  const subprocessosStore = useSubprocessosStore();
  const mapasStore = useMapasStore();
  
  const loading = useSingleLoading();
  
  async function carregar() {
    await loading.withLoading(async () => {
      await Promise.all([
        processosStore.buscarProcessoDetalhe(codigo.value),
        subprocessosStore.buscarPorProcesso(codigo.value),
        mapasStore.buscarResumo(codigo.value)
      ]);
    });
  }
  
  // Auto-carregar quando código muda
  watch(codigo, carregar, { immediate: true });
  
  return {
    isLoading: loading.isLoading,
    recarregar: carregar
  };
}
```

### 4. Modal/Dialog Composable

Gerencia estado de modais.

```typescript
// useModalConfirmacao.ts
export function useModalConfirmacao() {
  const mostrar = ref(false);
  const titulo = ref('');
  const mensagem = ref('');
  const confirmCallback = ref<(() => void) | null>(null);
  
  function abrir(opts: {
    titulo: string;
    mensagem: string;
    onConfirmar: () => void;
  }) {
    titulo.value = opts.titulo;
    mensagem.value = opts.mensagem;
    confirmCallback.value = opts.onConfirmar;
    mostrar.value = true;
  }
  
  function confirmar() {
    if (confirmCallback.value) {
      confirmCallback.value();
    }
    fechar();
  }
  
  function fechar() {
    mostrar.value = false;
    confirmCallback.value = null;
  }
  
  return {
    mostrar,
    titulo,
    mensagem,
    abrir,
    confirmar,
    fechar
  };
}
```

---

## 🎯 Checklist de Refatoração

Ao extrair lógica de uma view:

- [ ] Identifique blocos de lógica relacionados
- [ ] Agrupe por responsabilidade (state, business logic, API, etc.)
- [ ] Crie composable com interface clara (params e retorno)
- [ ] Use TypeScript para tipos de parâmetros e retorno
- [ ] Documente o composable (propósito e uso)
- [ ] Teste o composable isoladamente (unit test)
- [ ] Refatore a view para usar o composable
- [ ] Verifique se outros componentes podem reutilizar

---

## 📊 Exemplo Completo: Antes e Depois

### Antes: View com 320 linhas

```vue
<!-- CadProcesso.vue - 320 linhas -->
<script setup lang="ts">
// 200+ linhas de lógica complexa
const form = reactive({ ... });
const validar = () => { ... };
const carregarUnidades = async () => { ... };
const salvar = async () => { ... };
// ... muito mais código
</script>
```

### Depois: View + 3 Composables

```vue
<!-- CadProcesso.vue - 80 linhas -->
<script setup lang="ts">
import { useFormularioProcesso } from '@/composables/useFormularioProcesso';
import { useCarregamentoUnidades } from '@/composables/useCarregamentoUnidades';
import { useSalvarProcesso } from '@/composables/useSalvarProcesso';

const formulario = useFormularioProcesso();
const unidades = useCarregamentoUnidades();
const { salvar, isLoading } = useSalvarProcesso(formulario.form);
</script>

<template>
  <FormularioProcesso
    v-model="formulario.form"
    :erros="formulario.erros"
    :unidades="unidades.disponiveis"
    :loading="isLoading"
    @salvar="salvar"
  />
</template>
```

---

## 🚀 Benefícios

- ✅ **Testabilidade:** Composables podem ser testados isoladamente
- ✅ **Reutilização:** Lógica compartilhada entre views
- ✅ **Manutenção:** Mudanças centralizadas em um lugar
- ✅ **Legibilidade:** Views mais simples e focadas em apresentação
- ✅ **Separação de Concerns:** Lógica separada de renderização

---

## 📚 Views Candidatas à Refatoração

Baseado no tamanho e complexidade:

1. **CadProcesso.vue** (449 linhas) - Alta prioridade
2. **CadMapa.vue** (382 linhas) - Alta prioridade
3. **ConfiguracoesView.vue** (346 linhas) - Média prioridade
4. **ProcessoView.vue** (324 linhas) - Média prioridade
5. **VisMapa.vue** (312 linhas) - Já tem composables parcialmente
6. **CadAtividades.vue** (273 linhas) - Já tem composables parcialmente
7. **VisAtividades.vue** (246 linhas) - Já tem composables parcialmente
8. **UnidadeView.vue** (225 linhas) - Baixa prioridade

---

**Última Atualização:** 2026-01-31  
**Referências:**
- [Vue 3 Composables Guide](https://vuejs.org/guide/reusability/composables.html)
- [Composition API FAQ](https://vuejs.org/guide/extras/composition-api-faq.html)
