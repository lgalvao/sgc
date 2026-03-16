<template>
  <TreeTable
      :columns="colunas"
      :data="mapeamentoHierarquia"
      :title="TEXTOS.subprocesso.DETALHE_UNIDADES_TITULO"
      @row-click="$emit('row-click', $event)"
  />
</template>

<script lang="ts" setup>
import {computed} from "vue";
import TreeTable from "@/components/comum/TreeTable.vue";
import type {UnidadeParticipante} from "@/types/tipos";
import {formatDate, formatSituacaoSubprocesso} from "@/utils/formatters";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  participantesHierarquia: UnidadeParticipante[];
}>();

defineEmits<{
  'row-click': [item: any];
}>();

const colunas = [
  {key: "unidadeAtual", label: TEXTOS.subprocesso.COLUNA_UNIDADE, width: "40%"},
  {key: "situacao", label: TEXTOS.subprocesso.COLUNA_SITUACAO, width: "30%"},
  {key: "dataLimite", label: TEXTOS.subprocesso.COLUNA_DATA_LIMITE, width: "30%"},
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
    situacaoTooltip: formatSituacaoSubprocesso(u.situacaoSubprocesso),
    dataLimite: formatDate(u.dataLimite, false),
    children: u.filhos ? mapUnidades(u.filhos) : [],
    expanded: true,
    clickable: true
  }));
}
</script>
