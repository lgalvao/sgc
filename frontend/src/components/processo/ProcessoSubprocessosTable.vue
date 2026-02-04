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
import TreeTable from "@/components/TreeTableView.vue";
import type {UnidadeParticipante} from "@/types/tipos";

const props = defineProps<{
  participantesHierarquia: UnidadeParticipante[];
}>();

defineEmits<{
  'row-click': [item: any];
}>();

const colunas = [
  { key: "unidadeAtual", label: "Unidade", width: "40%" },
  { key: "situacaoLabel", label: "Situação", width: "30%" },
  { key: "dataLimiteFormatada", label: "Data Limite", width: "30%" },
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
    situacaoLabel: u.situacaoLabel || u.situacaoSubprocesso,
    dataLimiteFormatada: u.dataLimiteFormatada || u.dataLimite,
    children: u.filhos ? mapUnidades(u.filhos) : [],
    expanded: true,
    clickable: true
  }));
}
</script>
