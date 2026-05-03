<template>
  <div>
    <ProcessoBasicFields
        ref="basicFieldsRef"
        :descricao="modelValue.descricao"
        :erro-descricao="fieldErrors.descricao"
        :erro-tipo="fieldErrors.tipo"
        :is-edit="isEdit"
        :tipo="modelValue.tipo"
        @update:descricao="updateField('descricao', $event)"
        @update:tipo="updateField('tipo', $event)"
    />

    <ProcessoUnidadesField
        ref="unidadesFieldRef"
        :erro="fieldErrors.unidades"
        :is-loading="isLoadingUnidades"
        :model-value="modelValue.unidadesSelecionadas"
        :unidades="unidades"
        @update:model-value="updateField('unidadesSelecionadas', $event)"
    />

    <ProcessoDeadlineField
        ref="deadlineFieldRef"
        :erro="fieldErrors.dataLimite"
        :model-value="modelValue.dataLimite"
        @update:model-value="updateField('dataLimite', $event)"
    />
  </div>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import ProcessoBasicFields from "./ProcessoBasicFields.vue";
import ProcessoUnidadesField from "./ProcessoUnidadesField.vue";
import ProcessoDeadlineField from "./ProcessoDeadlineField.vue";
import type {Unidade} from "@/types/tipos";
import {TipoProcesso} from "@/types/tipos";

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

const basicFieldsRef = ref<InstanceType<typeof ProcessoBasicFields> | null>(null);
const unidadesFieldRef = ref<InstanceType<typeof ProcessoUnidadesField> | null>(null);
const deadlineFieldRef = ref<InstanceType<typeof ProcessoDeadlineField> | null>(null);

function updateField<K extends keyof ProcessoFormData>(field: K, value: ProcessoFormData[K]) {
  emit('update:modelValue', {
    ...props.modelValue,
    [field]: value
  });
}

function focarPrimeiroErro() {
  const erros = props.fieldErrors;
  if (erros.descricao) {
    basicFieldsRef.value?.focarDescricao();
  } else if (!props.isEdit && erros.tipo) {
    // Tipo foca via seletor interno se necessário, ou deixamos para o focarPrimeiroErroInvalido global
  } else if (erros.unidades) {
    unidadesFieldRef.value?.focar();
  } else if (erros.dataLimite) {
    deadlineFieldRef.value?.focar();
  }
}

defineExpose({
  focarDescricao: () => basicFieldsRef.value?.focarDescricao(),
  focarPrimeiroErro
});
</script>

