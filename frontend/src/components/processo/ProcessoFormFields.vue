<template>
  <div>
    <BFormGroup class="mb-3" label-for="descricao">
      <template #label>
        Descrição <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <BFormInput
          id="descricao"
          ref="inputDescricaoRef"
          :model-value="modelValue.descricao"
          :state="fieldErrors.descricao ? false : null"
          data-testid="inp-processo-descricao"
          placeholder="Descreva o processo"
          required
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
          :disabled="isEdit"
          :model-value="modelValue.tipo"
          :options="tipoOptions"
          :state="isEdit ? null : (fieldErrors.tipo ? false : null)"
          data-testid="sel-processo-tipo"
          required
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
          <span class="spinner-border spinner-border-sm me-2"/>
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

      <BInputGroup>
        <BFormInput
            id="dataLimite"
            ref="inputDataLimiteRef"
            :model-value="modelValue.dataLimite"
            :state="fieldErrors.dataLimite ? false : null"
            data-testid="inp-processo-data-limite"
            max="2099-12-31"
            min="2000-01-01"
            required
            type="date"
            @update:model-value="(val) => updateField('dataLimite', String(val))"
        />
        <BInputGroupText class="cursor-pointer" @click="abrirCalendario">
          <i class="bi bi-calendar-event"></i>
        </BInputGroupText>
      </BInputGroup>

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
  BInputGroup,
  BInputGroupText
} from "bootstrap-vue-next";
import {nextTick, ref, watch} from "vue";
import ArvoreUnidades from "@/components/unidade/ArvoreUnidades.vue";
import type {Unidade} from "@/types/tipos";
import {TipoProcesso} from "@/types/tipos";
import {useValidacao} from "@/composables/useValidacao";

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
const inputDataLimiteRef = ref<InstanceType<typeof BFormInput> | null>(null);
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

function abrirCalendario() {
  if (inputDataLimiteRef.value?.$el) {
    const input = inputDataLimiteRef.value.$el as HTMLInputElement;
    if (typeof input.showPicker === 'function') {
      input.showPicker();
    } else {
      input.focus();
    }
  }
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
      inputDataLimiteRef.value?.$el?.focus?.();
      return;
    default:
      return;
  }
}

watch(
    () => [
      props.fieldErrors.descricao,
      props.fieldErrors.tipo,
      props.fieldErrors.unidades,
      props.fieldErrors.dataLimite
    ],
    async (erros) => {
      const errosFiltrados = {
        descricao: erros[0],
        tipo: props.isEdit ? undefined : erros[1], // Ignora erro de tipo na edição
        unidades: erros[2],
        dataLimite: erros[3]
      };

      if (possuiErros(errosFiltrados)) {
        await nextTick();
        focarPrimeiroErro();
      }
    },
    {immediate: true}
);

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

@media (max-width: 575.98px) {
  .container-arvore {
    padding: 0.75rem !important;
  }
}
</style>
