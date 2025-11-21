<template>
  <div class="arvore-unidades">
    <template
      v-for="unidade in unidadesFiltradas"
      :key="unidade.sigla"
    >
      <div class="form-check">
        <BFormCheckbox
          :id="`chk-${unidade.sigla}`"
          v-model="unidadesSelecionadasLocal"
          :value="unidade.codigo"
          :indeterminate="getEstadoSelecao(unidade) === 'indeterminate'"
          :disabled="!unidade.isElegivel"
          :data-testid="`chk-${unidade.sigla}`"
        >
          <label
            :for="`chk-${unidade.sigla}`"
            class="form-check-label ms-2"
            :class="{ 'text-muted': !unidade.isElegivel }"
          >
            <strong>{{ unidade.sigla }}</strong> - {{ unidade.nome }}
            <span
              v-if="!unidade.isElegivel"
              class="badge bg-warning text-dark ms-2"
            >
              Não elegível
            </span>
          </label>
        </BFormCheckbox>
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
            <BFormCheckbox
              :id="`chk-${filha.sigla}`"
              v-model="unidadesSelecionadasLocal"
              :value="filha.codigo"
              :indeterminate="getEstadoSelecao(filha) === 'indeterminate'"
              :disabled="!filha.isElegivel"
            >
              <label
                :for="`chk-${filha.sigla}`"
                class="form-check-label ms-2"
                :class="{ 'text-muted': !filha.isElegivel }"
              >
                <strong>{{ filha.sigla }}</strong> - {{ filha.nome }}
                <span
                  v-if="!filha.isElegivel"
                  class="badge bg-warning text-dark ms-2"
                >
                  Não elegível
                </span>
              </label>
            </BFormCheckbox>
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
                <BFormCheckbox
                  :id="`chk-${neta.sigla}`"
                  v-model="unidadesSelecionadasLocal"
                  :value="neta.codigo"
                  :disabled="!neta.isElegivel"
                >
                  <label
                    :for="`chk-${neta.sigla}`"
                    class="form-check-label ms-2"
                    :class="{ 'text-muted': !neta.isElegivel }"
                  >
                    <strong>{{ neta.sigla }}</strong> - {{ neta.nome }}
                    <span
                      v-if="!neta.isElegivel"
                      class="badge bg-warning text-dark ms-2"
                    >
                      Não elegível
                    </span>
                  </label>
                </BFormCheckbox>
              </div>
            </template>
          </div>
        </template>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import {BFormCheckbox} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import type {Unidade} from "@/types/tipos";
import logger from "@/utils/logger";

interface Props {
  unidades: Unidade[];
  modelValue: number[];
  filtrarPor?: (unidade: Unidade) => boolean;
}

const props = withDefaults(defineProps<Props>(), {
  filtrarPor: () => true,
});

const emit = defineEmits<(e: "update:modelValue", value: number[]) => void>();

const unidadesSelecionadasLocal = ref<number[]>([...props.modelValue]);

logger.debug("[DEBUG ArvoreUnidades] Initial modelValue:", props.modelValue);
logger.debug(
    "[DEBUG ArvoreUnidades] Initial unidadesSelecionadasLocal:",
    unidadesSelecionadasLocal.value,
);
logger.debug("[DEBUG ArvoreUnidades] Initial props.unidades:", props.unidades);

// Filtrar unidades pela função customizada
const unidadesFiltradas = computed(() => {
  return props.unidades.filter(props.filtrarPor);
});

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
function getEstadoSelecao(unidade: Unidade): boolean | "indeterminate" {
  const selfSelected = isChecked(unidade.codigo);

  if (isFolha(unidade)) {
    return selfSelected;
  }

  const subunidades = getTodasSubunidades(unidade);
  if (subunidades.length === 0) {
    return selfSelected;
  }
  const selecionadas = subunidades.filter((codigo) => isChecked(codigo)).length;

  if (selecionadas === 0 && !selfSelected) {
    return false;
  }
  if (selecionadas === subunidades.length && selfSelected) {
    return true;
  }

  // INTEROPERACIONAL marcada sem todas filhas → mostrar como marcada (não indeterminate)
  if (unidade.tipo === "INTEROPERACIONAL" && selfSelected) {
    return true;
  }

  return "indeterminate";
}

watch(
    () => props.modelValue,
    (novoValor) => {
      logger.debug("[DEBUG ArvoreUnidades] modelValue changed:", novoValor);
      if (
          JSON.stringify(novoValor.sort()) !==
          JSON.stringify(unidadesSelecionadasLocal.value.sort())
      ) {
        unidadesSelecionadasLocal.value = [...novoValor];
        logger.debug(
            "[DEBUG ArvoreUnidades] unidadesSelecionadasLocal updated:",
            unidadesSelecionadasLocal.value,
        );
      }
    },
    {deep: true},
);

// Watch para reagir a mudanças internas e emitir para o pai
watch(
    unidadesSelecionadasLocal,
    (novoValor) => {
      logger.debug(
          "[DEBUG ArvoreUnidades] unidadesSelecionadasLocal changed (internal):",
          novoValor,
      );
      emit("update:modelValue", novoValor);
    },
    {deep: true},
);
</script>

<style scoped>
.arvore-unidades .form-check-input:indeterminate {
  background-color: #6c757d;
  border-color: #6c757d;
}
</style>
