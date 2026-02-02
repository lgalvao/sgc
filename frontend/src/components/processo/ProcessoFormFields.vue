<template>
  <div>
    <BFormGroup class="mb-3" label-for="descricao">
      <template #label>
        Descrição <span class="text-danger" aria-hidden="true">*</span>
      </template>
      <BFormInput
          id="descricao"
          ref="inputDescricaoRef"
          :model-value="modelValue.descricao"
          :state="fieldErrors.descricao ? false : null"
          data-testid="inp-processo-descricao"
          placeholder="Descreva o processo"
          type="text"
          required
          @update:model-value="(val) => updateField('descricao', String(val))"
      />
      <BFormInvalidFeedback :state="fieldErrors.descricao ? false : null">
        {{ fieldErrors.descricao }}
      </BFormInvalidFeedback>
    </BFormGroup>

    <BFormGroup class="mb-3" label-for="tipo">
      <template #label>
        Tipo <span class="text-danger" aria-hidden="true">*</span>
      </template>
      <BFormSelect
          id="tipo"
          :model-value="modelValue.tipo"
          :options="tipoOptions"
          :state="fieldErrors.tipo ? false : null"
          data-testid="sel-processo-tipo"
          required
          @update:model-value="updateField('tipo', $event)"
      >
        <template #first>
          <BFormSelectOption :value="null" disabled>-- Selecione o tipo --</BFormSelectOption>
        </template>
      </BFormSelect>
      <BFormInvalidFeedback :state="fieldErrors.tipo ? false : null">
        {{ fieldErrors.tipo }}
      </BFormInvalidFeedback>
    </BFormGroup>

    <BFormGroup class="mb-3">
      <template #label>
        Unidades participantes <span class="text-danger" aria-hidden="true">*</span>
      </template>
      <div class="border rounded p-3" :class="{ 'border-danger': fieldErrors.unidades }">
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
        description="Prazo para conclusão da primeira etapa (Mapeamento/Revisão)."
        label-for="dataLimite"
    >
      <template #label>
        Data limite <span class="text-danger" aria-hidden="true">*</span>
      </template>
      <BFormInput
          id="dataLimite"
          :model-value="modelValue.dataLimite"
          :state="fieldErrors.dataLimite ? false : null"
          data-testid="inp-processo-data-limite"
          type="date"
          required
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
  BFormSelectOption
} from "bootstrap-vue-next";
import { ref } from "vue";
import ArvoreUnidades from "@/components/ArvoreUnidades.vue";
import { TipoProcesso } from "@/types/tipos";
import type { Unidade } from "@/types/tipos";

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
}>();

const emit = defineEmits<{
  'update:modelValue': [value: ProcessoFormData];
}>();

const inputDescricaoRef = ref<InstanceType<typeof BFormInput> | null>(null);

const tipoOptions = [
  { value: TipoProcesso.MAPEAMENTO, text: 'Mapeamento' },
  { value: TipoProcesso.REVISAO, text: 'Revisão' },
  { value: TipoProcesso.DIAGNOSTICO, text: 'Diagnóstico' }
];

function updateField<K extends keyof ProcessoFormData>(field: K, value: ProcessoFormData[K]) {
  emit('update:modelValue', {
    ...props.modelValue,
    [field]: value
  });
}

defineExpose({ inputDescricaoRef });
</script>
