<template>
  <div>
    <BFormGroup class="mb-3" label-for="descricao">
      <template #label>
        Descrição <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <BFormInput
          id="descricao"
          ref="inputDescricaoRef"
          aria-required="true"
          :model-value="modelValue.descricao"
          :state="fieldErrors.descricao ? false : null"
          data-testid="inp-processo-descricao"
          placeholder="Descreva o processo"
          type="text"
          @update:model-value="(val) => updateField('descricao', String(val))"
      />
      <BFormInvalidFeedback :state="fieldErrors.descricao ? false : null">
        {{ fieldErrors.descricao }}
      </BFormInvalidFeedback>
    </BFormGroup>

    <BFormGroup class="mb-3" label-for="tipo">
      <template #label>
        Tipo <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <BFormSelect
          id="tipo"
          ref="selectTipoRef"
          aria-required="true"
          :disabled="isEdit"
          :model-value="modelValue.tipo"
          :options="tipoOptions"
          :state="isEdit ? null : (fieldErrors.tipo ? false : null)"
          data-testid="sel-processo-tipo"
          @update:model-value="updateField('tipo', $event as any)"
      >
        <template #first>
          <BFormSelectOption :value="null" disabled>-- Selecione o tipo --</BFormSelectOption>
        </template>
      </BFormSelect>
      <BFormInvalidFeedback :state="isEdit ? null : (fieldErrors.tipo ? false : null)">
        {{ fieldErrors.tipo }}
      </BFormInvalidFeedback>
    </BFormGroup>

    <BFormGroup class="mb-3">
      <template #label>
        Unidades participantes <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <div
          ref="containerUnidadesRef"
          :class="{ 'border-danger': fieldErrors.unidades }"
          class="border rounded p-3 container-arvore"
          data-testid="container-processo-unidades"
          tabindex="-1"
      >
        <ArvoreUnidades
            v-if="!isLoadingUnidades"
            :model-value="modelValue.unidadesSelecionadas"
            :unidades="unidades"
            @update:model-value="updateField('unidadesSelecionadas', $event)"
        />
        <div v-else class="text-center py-3">
          <BSpinner
              aria-hidden="true"
              class="me-2"
              small
          />
          Carregando unidades...
        </div>
      </div>
      <BFormInvalidFeedback :state="fieldErrors.unidades ? false : null" class="d-block">
        {{ fieldErrors.unidades }}
      </BFormInvalidFeedback>
    </BFormGroup>

    <BFormGroup
        class="mb-3"
        description="Prazo para conclusão da primeira etapa"
        label-for="dataLimite">

      <template #label>
        Data limite <span aria-hidden="true" class="text-danger">*</span>
      </template>

      <InputData
          id="dataLimite"
          ref="inputDataLimiteRef"
          :model-value="modelValue.dataLimite"
          :state="fieldErrors.dataLimite ? false : null"
          data-testid="inp-processo-data-limite"
          max="2099-12-31"
          :min="obterAmanhaFormatado()"
          :required="true"
          @update:model-value="(val) => updateField('dataLimite', String(val))"
      />

      <BFormInvalidFeedback :state="fieldErrors.dataLimite ? false : null">
        {{ fieldErrors.dataLimite }}
      </BFormInvalidFeedback>
    </BFormGroup>
  </div>
</template>

<script lang="ts" setup>
import {
  BFormGroup,
  BFormInput,
  BFormInvalidFeedback,
  BFormSelect,
  BFormSelectOption,
  BSpinner
} from "bootstrap-vue-next";
import {nextTick, ref, watch} from "vue";
import ArvoreUnidades from "@/components/unidade/ArvoreUnidades.vue";
import InputData from "@/components/comum/InputData.vue";
import type {Unidade} from "@/types/tipos";
import {TipoProcesso} from "@/types/tipos";
import {useValidacao} from "@/composables/useValidacao";
import {obterAmanhaFormatado} from "@/utils/dateUtils";

interface ProcessoFormData {
  descricao: string;
  tipo: TipoProcesso | null;
  unidadesSelecionadas: number[];
  dataLimite: string;
}

interface FieldErrors {
  descricao?: string;
  tipo?: string;
  unidades?: string;
  dataLimite?: string;
}

const props = defineProps<{
  modelValue: ProcessoFormData;
  fieldErrors: FieldErrors;
  unidades: Unidade[];
  isLoadingUnidades: boolean;
  isEdit?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: ProcessoFormData];
}>();

const inputDescricaoRef = ref<InstanceType<typeof BFormInput> | null>(null);
const selectTipoRef = ref<any>(null);
const inputDataLimiteRef = ref<InstanceType<typeof InputData> | null>(null);
const containerUnidadesRef = ref<HTMLElement | null>(null);
const {obterPrimeiroCampoComErro, possuiErros} = useValidacao();

const tipoOptions = [
  {value: TipoProcesso.MAPEAMENTO, text: 'Mapeamento'},
  {value: TipoProcesso.REVISAO, text: 'Revisão'},
  {value: TipoProcesso.DIAGNOSTICO, text: 'Diagnóstico'}
];

function updateField<K extends keyof ProcessoFormData>(field: K, value: ProcessoFormData[K]) {
  emit('update:modelValue', {
    ...props.modelValue,
    [field]: value
  });
}

function focarPrimeiroErro() {
  const primeiroCampo = obterPrimeiroCampoComErro(props.fieldErrors);
  // Se for edição, ignoramos erros no campo 'tipo' para foco
  if (props.isEdit && primeiroCampo === "tipo") {
    // Tenta focar no próximo campo com erro se houver
    return;
  }

  switch (primeiroCampo) {
    case "descricao":
      inputDescricaoRef.value?.$el?.focus?.();
      return;
    case "tipo": {
      const select = selectTipoRef.value?.$el?.querySelector?.("select");
      select?.focus?.();
      return;
    }
    case "unidades":
      containerUnidadesRef.value?.focus?.();
      return;
    case "dataLimite":
      inputDataLimiteRef.value?.focus?.();
      return;
    default:
      return;
  }
}

defineExpose({inputDescricaoRef, focarPrimeiroErro});
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

/* Esconde o ícone de calendário nativo do navegador para evitar duplicidade */
:deep(input[type="date"]::-webkit-calendar-picker-indicator) {
  display: none !important;
  -webkit-appearance: none;
}

/* Garante altura mínima para o input para evitar cortes no ícone */
:deep(input[type="date"]) {
  min-height: calc(1.5em + 0.75rem + 2px);
}

/* Ajustes para o container da árvore de unidades no celular */
.container-arvore {
  overflow-x: hidden;
}

@media (max-width: 576px) {
  .container-arvore {
    padding: 0.75rem !important;
  }
}
</style>
