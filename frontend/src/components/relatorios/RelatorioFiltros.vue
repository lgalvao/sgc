<template>
  <BRow class="mb-4">
    <BCol md="4">
      <BFormGroup label="Filtrar por Tipo" label-for="filtro-tipo">
        <BFormSelect
            id="filtro-tipo"
            :model-value="tipo"
            :options="opcoesTipo"
            data-testid="filtro-tipo"
            aria-label="Filtrar por Tipo"
            @update:model-value="$emit('update:tipo', $event as string)"
        />
      </BFormGroup>
    </BCol>
    <BCol md="4">
      <BFormGroup label="Data Início" label-for="filtro-data-inicio">
        <BFormInput
            id="filtro-data-inicio"
            :model-value="dataInicio"
            data-testid="filtro-data-inicio"
            type="date"
            aria-label="Data Início"
            @update:model-value="$emit('update:dataInicio', $event as string)"
        />
      </BFormGroup>
    </BCol>
    <BCol md="4">
      <BFormGroup label="Data Fim" label-for="filtro-data-fim">
        <BFormInput
            id="filtro-data-fim"
            :model-value="dataFim"
            data-testid="filtro-data-fim"
            type="date"
            aria-label="Data Fim"
            @update:model-value="$emit('update:dataFim', $event as string)"
        />
      </BFormGroup>
    </BCol>
  </BRow>
</template>

<script lang="ts" setup>
import {BCol, BFormGroup, BFormInput, BFormSelect, BRow} from "bootstrap-vue-next";
import {TipoProcesso} from "@/types/tipos";

defineProps<{
  tipo: string;
  dataInicio: string;
  dataFim: string;
}>();

defineEmits<{
  (e: 'update:tipo', value: string): void;
  (e: 'update:dataInicio', value: string): void;
  (e: 'update:dataFim', value: string): void;
}>();

const opcoesTipo = [
  { value: "", text: "Todos os Tipos" },
  { value: TipoProcesso.MAPEAMENTO, text: "Mapeamento" },
  { value: TipoProcesso.REVISAO, text: "Revisão" },
  { value: TipoProcesso.DIAGNOSTICO, text: "Diagnóstico" },
];

defineExpose({ opcoesTipo });
</script>
