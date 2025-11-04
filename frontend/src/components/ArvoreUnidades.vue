<template>
  <div class="arvore-unidades">
    <template
      v-for="unidade in unidadesFiltradas"
      :key="unidade.sigla"
    >
      <div
        v-if="temFilhasElegiveis(unidade)"
        class="form-check"
      >
        <input
          :id="`chk-${unidade.sigla}`"
          v-model="unidadesSelecionadasLocal"
          :value="unidade.codigo"
          class="form-check-input"
          type="checkbox"
          :indeterminate.prop="getEstadoSelecao(unidade) === 'indeterminate'"
          :disabled="isUnidadeDesabilitada(unidade.codigo)"
          :data-testid="`chk-${unidade.sigla}`"
        >
        <label
          :for="`chk-${unidade.sigla}`"
          class="form-check-label ms-2"
          :class="{ 'text-muted': isUnidadeDesabilitada(unidade.codigo) }"
        >
          <strong>{{ unidade.sigla }}</strong> - {{ unidade.nome }}
          <span v-if="isUnidadeDesabilitada(unidade.codigo)" class="badge bg-warning text-dark ms-2">
            Não elegível
          </span>
        </label>
      </div>

      <div
        v-if="unidade.filhas && unidade.filhas.length"
        class="ms-4"
      >
        <template
          v-for="filha in unidade.filhas"
          :key="filha.sigla"
        >
          <div
            v-if="temFilhasElegiveis(filha)"
            class="form-check"
          >
            <input
              :id="`chk-${filha.sigla}`"
              v-model="unidadesSelecionadasLocal"
              :value="filha.codigo"
              class="form-check-input"
              type="checkbox"
              :indeterminate.prop="getEstadoSelecao(filha) === 'indeterminate'"
              :disabled="isUnidadeDesabilitada(filha.codigo)"
            >
            <label
              :for="`chk-${filha.sigla}`"
              class="form-check-label ms-2"
              :class="{ 'text-muted': isUnidadeDesabilitada(filha.codigo) }"
            >
              <strong>{{ filha.sigla }}</strong> - {{ filha.nome }}
              <span v-if="isUnidadeDesabilitada(filha.codigo)" class="badge bg-warning text-dark ms-2">
                Não elegível
              </span>
            </label>
          </div>

          <div
            v-if="filha.filhas && filha.filhas.length"
            class="ms-4"
          >
            <template
              v-for="neta in filha.filhas"
              :key="neta.sigla"
            >
              <div
                v-if="temFilhasElegiveis(neta)"
                class="form-check"
              >
                <input
                  :id="`chk-${neta.sigla}`"
                  v-model="unidadesSelecionadasLocal"
                  :value="neta.codigo"
                  class="form-check-input"
                  type="checkbox"
                  :disabled="isUnidadeDesabilitada(neta.codigo)"
                >
                <label
                  :for="`chk-${neta.sigla}`"
                  class="form-check-label ms-2"
                  :class="{ 'text-muted': isUnidadeDesabilitada(neta.codigo) }"
                >
                  <strong>{{ neta.sigla }}</strong> - {{ neta.nome }}
                  <span v-if="isUnidadeDesabilitada(neta.codigo)" class="badge bg-warning text-dark ms-2">
                    Não elegível
                  </span>
                </label>
              </div>
            </template>
          </div>
        </template>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import {computed, ref, watch} from 'vue';
import type {Unidade} from '../types';

interface Props {
  unidades: Unidade[];
  modelValue: number[];
  desabilitadas?: number[];
  filtrarPor?: (unidade: Unidade) => boolean;
}

const props = withDefaults(defineProps<Props>(), {
  desabilitadas: () => [],
  filtrarPor: () => true
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: number[]): void;
}>();

const unidadesSelecionadasLocal = ref<number[]>([...props.modelValue]);
const processandoSelecao = ref(false);

// Filtrar unidades pela função customizada
const unidadesFiltradas = computed(() => {
  return props.unidades.filter(props.filtrarPor);
});

// Verifica se unidade está desabilitada
function isUnidadeDesabilitada(codigo: number): boolean {
  return props.desabilitadas.includes(codigo);
}

// Verifica se unidade ou suas descendentes são elegíveis
function temFilhasElegiveis(unidade: Unidade): boolean {
  if (!props.filtrarPor(unidade)) {
    // Unidade não é elegível, mas verificar filhas
    if (unidade.filhas && unidade.filhas.length > 0) {
      return unidade.filhas.some(f => temFilhasElegiveis(f));
    }
    return false;
  }
  return true;
}

// Verifica se é folha (sem filhas)
function isFolha(unidade: Unidade): boolean {
  return !unidade.filhas || unidade.filhas.length === 0;
}

// Obtém todas subunidades recursivamente
function getTodasSubunidades(unidade: Unidade): number[] {
  const result: number[] = [];
  if (unidade.filhas) {
    for (const filha of unidade.filhas) {
      result.push(filha.codigo);
      if (filha.filhas) {
        result.push(...getTodasSubunidades(filha));
      }
    }
  }
  return result;
}

// Verifica se está marcada
function isChecked(codigo: number): boolean {
  return unidadesSelecionadasLocal.value.includes(codigo);
}

// Obtém estado de seleção (true, false ou 'indeterminate')
function getEstadoSelecao(unidade: Unidade): boolean | 'indeterminate' {
  const selfSelected = isChecked(unidade.codigo);

  if (isFolha(unidade)) {
    return selfSelected;
  }

  const subunidades = getTodasSubunidades(unidade);
  if (subunidades.length === 0) {
    return selfSelected;
  }
  const selecionadas = subunidades.filter(codigo => isChecked(codigo)).length;

  if (selecionadas === 0 && !selfSelected) {
    return false;
  }
  if (selecionadas === subunidades.length && selfSelected) {
    return true;
  }

  // INTEROPERACIONAL marcada sem todas filhas → mostrar como marcada (não indeterminate)
  if (unidade.tipo === 'INTEROPERACIONAL' && selfSelected) {
    return true;
  }

  return 'indeterminate';
}

// Encontra unidade no array recursivamente
function encontrarUnidade(codigo: number, unidades: Unidade[]): Unidade | null {
  for (const unidade of unidades) {
    if (unidade.codigo === codigo) {
      return unidade;
    }
    if (unidade.filhas) {
      const encontrada = encontrarUnidade(codigo, unidade.filhas);
      if (encontrada) return encontrada;
    }
  }
  return null;
}

// Watch para processar mudanças de seleção (lógica hierárquica)
watch(unidadesSelecionadasLocal, (novoValor, valorAnterior) => {
  if (processandoSelecao.value) return;

  try {
    processandoSelecao.value = true;

    let novaSelecao = [...novoValor];
    const selecionadasAnteriores = valorAnterior || [];

    const adicionadas = novoValor.filter(c => !selecionadasAnteriores.includes(c));
    const removidas = selecionadasAnteriores.filter(c => !novoValor.includes(c));

    // Processar adicionadas - marcar todas filhas
    if (adicionadas.length === 1) {
      const codigo = adicionadas[0];
      const unidade = encontrarUnidade(codigo, props.unidades);
      if (unidade && !isFolha(unidade)) {
        const subunidades = getTodasSubunidades(unidade);
        for (const sub of subunidades) {
          if (!novaSelecao.includes(sub)) {
            novaSelecao.push(sub);
          }
        }
      }
    }

    // Processar removidas - desmarcar todas filhas
    if (removidas.length === 1) {
      const codigo = removidas[0];
      const unidade = encontrarUnidade(codigo, props.unidades);
      if (unidade && !isFolha(unidade)) {
        const subunidades = getTodasSubunidades(unidade);
        novaSelecao = novaSelecao.filter(c => !subunidades.includes(c));
      }
    }

    // Verificar pais - propagar seleção para cima
    function verificarPais(unidadesArray: Unidade[]) {
      for (const unidade of unidadesArray) {
        if (unidade.filhas && unidade.filhas.length > 0) {
          verificarPais(unidade.filhas);
        }
      }

      for (const unidade of unidadesArray) {
        if (!isFolha(unidade)) {
          const filhasDirectas = unidade.filhas?.map(f => f.codigo) || [];

          const selecionadas = filhasDirectas.filter(s => novaSelecao.includes(s)).length;
          const total = filhasDirectas.length;

          if (selecionadas === total && total > 0) {
            if (!novaSelecao.includes(unidade.codigo)) {
              novaSelecao.push(unidade.codigo);
            }
          } else if (selecionadas === 0) {
            // INTEROPERACIONAL que estava explicitamente marcada antes pode permanecer
            const estaExplicitamenteMarcada = selecionadasAnteriores.includes(unidade.codigo);
            if (unidade.tipo === 'INTEROPERACIONAL' && estaExplicitamenteMarcada) {
              // Manter marcada
            } else {
              novaSelecao = novaSelecao.filter(c => c !== unidade.codigo);
            }
          } else {
            // INTEROPERACIONAL que estava explicitamente marcada antes pode permanecer
            const estaExplicitamenteMarcada = selecionadasAnteriores.includes(unidade.codigo);
            if (unidade.tipo === 'INTEROPERACIONAL' && estaExplicitamenteMarcada) {
              // Manter marcada
            } else {
              novaSelecao = novaSelecao.filter(c => c !== unidade.codigo);
            }
          }
        }
      }
    }

    verificarPais(props.unidades);

    // Atualizar local apenas se mudou
    const selecaoAtualSorted = JSON.stringify([...novoValor].sort());
    const novaSelecaoSorted = JSON.stringify([...novaSelecao].sort());

    if (selecaoAtualSorted !== novaSelecaoSorted) {
      unidadesSelecionadasLocal.value = novaSelecao;
    }

    // Emitir para o pai apenas se diferente do modelValue recebido
    const modelValueSorted = JSON.stringify([...props.modelValue].sort());
    if (novaSelecaoSorted !== modelValueSorted) {
      emit('update:modelValue', novaSelecao);
    }
  } finally {
    processandoSelecao.value = false;
  }
}, { deep: true });

// Watch para sincronizar com mudanças externas do modelValue
watch(() => props.modelValue, (novoValor) => {
  if (processandoSelecao.value) return;

  const localSorted = JSON.stringify([...unidadesSelecionadasLocal.value].sort());
  const propsSorted = JSON.stringify([...novoValor].sort());

  if (localSorted !== propsSorted) {
    unidadesSelecionadasLocal.value = [...novoValor];
  }
}, { deep: true });
</script>

<style scoped>
.arvore-unidades .form-check-input:indeterminate {
  background-color: #6c757d;
  border-color: #6c757d;
}
</style>
