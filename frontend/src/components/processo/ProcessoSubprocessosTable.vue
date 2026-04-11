<template>
  <TreeTable
      :columns="colunas"
      :data="mapeamentoHierarquia"
      :title="TEXTOS.subprocesso.DETALHE_UNIDADES_TITULO"
      @row-click="emitirCliqueLinha"
  />
</template>

<script lang="ts" setup>
import {computed} from "vue";
import TreeTable from "@/components/comum/TreeTable.vue";
import type {UnidadeParticipante} from "@/types/tipos";
import {formatDate, formatSituacaoSubprocesso} from "@/utils/formatters";
import {TEXTOS} from "@/constants/textos";

type LinhaSubprocessoArvore = {
  codigo: number;
  codSubprocesso: number;
  unidadeAtual: string;
  sigla: string;
  situacao: string;
  situacaoTooltip: string;
  dataLimite: string;
  children: LinhaSubprocessoArvore[];
  expanded: true;
  clickable: boolean;
};

const props = defineProps<{
  participantesHierarquia: UnidadeParticipante[];
}>();

const emit = defineEmits<{
  'row-click': [item: LinhaSubprocessoArvore];
}>();

const colunas = [
  {key: "unidadeAtual", label: TEXTOS.subprocesso.COLUNA_UNIDADE, width: "55%"},
  {key: "situacao", label: TEXTOS.subprocesso.COLUNA_SITUACAO, width: "22%"},
  {key: "dataLimite", label: TEXTOS.subprocesso.COLUNA_DATA_LIMITE, width: "23%"},
];

const mapeamentoHierarquia = computed(() => {
  return mapUnidades(props.participantesHierarquia);
});

function mapUnidades(unidades: UnidadeParticipante[]): LinhaSubprocessoArvore[] {
  return unidades.map(u => ({
    codigo: u.codUnidade,
    codSubprocesso: u.codSubprocesso,
    unidadeAtual: `${u.sigla} - ${u.nome}`,
    sigla: u.sigla,
    situacao: formatSituacaoSubprocesso(u.situacaoSubprocesso),
    situacaoTooltip: formatSituacaoSubprocesso(u.situacaoSubprocesso),
    dataLimite: formatDate(u.dataLimite, false),
    children: u.filhos ? mapUnidades(u.filhos) : [],
    expanded: true,
    clickable: u.codSubprocesso > 0
  }));
}

function emitirCliqueLinha(item: unknown) {
  emit('row-click', item as LinhaSubprocessoArvore);
}
</script>
