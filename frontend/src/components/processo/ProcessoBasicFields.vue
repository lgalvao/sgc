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
          :model-value="descricao"
          :state="erroDescricao ? false : null"
          data-testid="inp-processo-descricao"
          placeholder="Descreva o processo"
          type="text"
          @update:model-value="(val) => $emit('update:descricao', String(val))"
      />
      <BFormInvalidFeedback :state="erroDescricao ? false : null">
        {{ erroDescricao }}
      </BFormInvalidFeedback>
    </BFormGroup>

    <BFormGroup class="mb-3" label-for="tipo">
      <template #label>
        Tipo <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <BFormSelect
          id="tipo"
          aria-required="true"
          :disabled="isEdit"
          :model-value="tipo"
          :options="tipoOptions"
          :state="isEdit ? null : (erroTipo ? false : null)"
          data-testid="sel-processo-tipo"
          @update:model-value="$emit('update:tipo', $event as TipoProcesso)"
      >
        <template #first>
          <BFormSelectOption :value="null" disabled>-- Selecione o tipo --</BFormSelectOption>
        </template>
      </BFormSelect>
      <BFormInvalidFeedback :state="isEdit ? null : (erroTipo ? false : null)">
        {{ erroTipo }}
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
import {ref} from "vue";
import {TipoProcesso} from "@/types/tipos";

defineProps<{
  descricao: string;
  tipo: TipoProcesso | null;
  erroDescricao?: string;
  erroTipo?: string;
  isEdit?: boolean;
}>();

defineEmits<{
  'update:descricao': [value: string];
  'update:tipo': [value: TipoProcesso | null];
}>();

const inputDescricaoRef = ref<InstanceType<typeof BFormInput> | null>(null);

const tipoOptions = [
  {value: TipoProcesso.MAPEAMENTO, text: 'Mapeamento'},
  {value: TipoProcesso.REVISAO, text: 'Revisão'},
  {value: TipoProcesso.DIAGNOSTICO, text: 'Diagnóstico'}
];

defineExpose({
  focarDescricao: () => inputDescricaoRef.value?.$el?.focus?.()
});
</script>
