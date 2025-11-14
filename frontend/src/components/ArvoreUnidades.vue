<template>
  <div class="arvore-unidades">
    <template
      v-for="unidade in unidadesFiltradas"
      :key="unidade.sigla"
    >
      <div class="form-check">
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
          <span
            v-if="isUnidadeDesabilitada(unidade.codigo)"
            class="badge bg-warning text-dark ms-2"
          >
            Não elegível
          </span>
        </label>
      </div>

      <!-- Mostrar filhas se a unidade tem filhas (mesmo que a unidade pai não seja) -->
      <div
        v-if="unidade.filhas && unidade.filhas.length"
        class="ms-4"
      >
        <template
          v-for="filha in unidade.filhas"
          :key="filha.sigla"
        >
          <div class="form-check">
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
              <span
                v-if="isUnidadeDesabilitada(filha.codigo)"
                class="badge bg-warning text-dark ms-2"
              >
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
              <div class="form-check">
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
                  <span
                    v-if="isUnidadeDesabilitada(neta.codigo)"
                    class="badge bg-warning text-dark ms-2"
                  >
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
import type {Unidade} from '@/types/tipos';

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

console.log('[DEBUG ArvoreUnidades] Initial modelValue:', props.modelValue);
console.log('[DEBUG ArvoreUnidades] Initial unidadesSelecionadasLocal:', unidadesSelecionadasLocal.value);
console.log('[DEBUG ArvoreUnidades] Initial props.unidades:', props.unidades);

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

watch(() => props.modelValue, (novoValor) => {
  console.log('[DEBUG ArvoreUnidades] modelValue changed:', novoValor);
  if (JSON.stringify(novoValor.sort()) !== JSON.stringify(unidadesSelecionadasLocal.value.sort())) {
    unidadesSelecionadasLocal.value = [...novoValor];
    console.log('[DEBUG ArvoreUnidades] unidadesSelecionadasLocal updated:', unidadesSelecionadasLocal.value);
  }
}, { deep: true });

// Watch para reagir a mudanças internas e emitir para o pai
watch(unidadesSelecionadasLocal, (novoValor) => {
  console.log('[DEBUG ArvoreUnidades] unidadesSelecionadasLocal changed (internal):', novoValor);
  emit('update:modelValue', novoValor);
}, { deep: true });
</script>

<style scoped>
.arvore-unidades .form-check-input:indeterminate {
  background-color: #6c757d;
  border-color: #6c757d;
}
</style>
