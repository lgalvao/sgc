<template>
  <TreeTable
      :columns="colunas"
      :data="mapeamentoHierarquia"
      title="Unidades participantes"
      @row-click="$emit('row-click', $event)"
  />
</template>

<script lang="ts" setup>
import {computed} from "vue";
import TreeTable from "@/components/comum/TreeTable.vue";
import type {UnidadeParticipante} from "@/types/tipos";
import {formatDate, formatSituacaoSubprocesso} from "@/utils/formatters";

const props = defineProps<{
  participantesHierarquia: UnidadeParticipante[];
}>();

defineEmits<{
  'row-click': [item: any];
}>();

const colunas = [
  {key: "unidadeAtual", label: "Unidade", width: "40%"},
  {key: "situacao", label: "Situação", width: "30%"},
  {key: "dataLimite", label: "Data Limite", width: "30%"},
];

const mapeamentoHierarquia = computed(() => {
  return mapUnidades(props.participantesHierarquia);
});

function mapUnidades(unidades: UnidadeParticipante[]): any[] {
  if (!unidades) return [];
  return unidades.map(u => ({
    codigo: u.codUnidade,
    unidadeAtual: `${u.sigla} - ${u.nome}`,
    sigla: u.sigla,
    situacao: formatSituacaoSubprocesso(u.situacaoSubprocesso),
    dataLimite: formatDate(u.dataLimite, false),
    children: u.filhos ? mapUnidades(u.filhos) : [],
    expanded: true,
    clickable: true
  }));
}
</script>
