<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoProcessos || (carregando && relatorioAndamento.length === 0)"/>

    <template v-else>
      <PageHeader :title="TEXTOS_RELATORIOS.ANDAMENTO_PROCESSO">
        <template #actions>
          <BButton to="/relatorios" variant="outline-secondary">
            <i class="bi bi-arrow-left me-1"/> Voltar
          </BButton>
        </template>
      </PageHeader>

      <EmptyState
          v-if="processosMapeamentoRevisao.length === 0"
          title="Não há processos de mapeamento ou revisão em andamento."
          icon="bi-bar-chart-steps"
      />

      <template v-else>
        <RelatorioAndamentoFiltros
            :carregando="carregando"
            :cod-processo-selecionado="codProcessoSelecionado"
            :opcoes-processos="opcoesProcessos"
            @exportar="exportarPdf"
            @gerar="gerarRelatorio"
            @update:cod-processo-selecionado="codProcessoSelecionado = $event"/>

        <div v-if="relatorioAndamento.length > 0" class="d-flex flex-column gap-3">
          <RelatorioAndamentoCard
              v-for="(item, index) in linhasRelatorioAndamento"
              :key="index"
              :item="item"/>
        </div>
      </template>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton} from "bootstrap-vue-next";
import RelatorioAndamentoCard from "@/components/relatorios/RelatorioAndamentoCard.vue";
import RelatorioAndamentoFiltros from "@/components/relatorios/RelatorioAndamentoFiltros.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useRelatoriosStore} from "@/stores/relatorios";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {useRelatorioAndamentoTela} from "@/composables/useRelatorioAndamentoTela";
import {useNotification} from "@/composables/useNotification";
import {formatarDataBR, formatarDataHoraBR} from "@/utils/date";
import {normalizarErro} from "@/utils/apiError";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {TipoProcesso, type ProcessoResumo} from "@/types/tipos";

const relatoriosStore = useRelatoriosStore();
const {notify} = useNotification();
const {processosDisponiveis, carregarProcessos} = useRelatorioAndamentoTela();
const acaoRelatorio = useAsyncAction();

const codProcessoSelecionado = ref<number | null>(null);
const carregandoProcessos = ref(true);

const relatorioAndamento = computed(() => relatoriosStore.relatorioAndamento);
const carregando = acaoRelatorio.carregando;
const processosMapeamentoRevisao = computed(() => processosDisponiveis.value.filter((processo: ProcessoResumo) => processo.tipo !== TipoProcesso.DIAGNOSTICO));

const linhasRelatorioAndamento = computed(() => relatorioAndamento.value.map(item => {
  const dt1 = item.dataLimiteEtapa1;
  const dt2 = item.dataLimiteEtapa2;
  const dt1Formatada = dt1 ? formatarDataBR(dt1) : '-';
  const dt2Formatada = dt2 ? formatarDataBR(dt2) : '-';
  const mostraPrazoAjustado = Boolean(dt2 && dt1 && dt2Formatada !== dt1Formatada);

  return {
    ...item,
    localizacao: item.localizacao || '-',
    dataLimiteEtapa1: dt1Formatada,
    dataLimiteEtapa2: dt2Formatada,
    dataFimEtapa1: item.dataFimEtapa1 ? formatarDataBR(item.dataFimEtapa1) : '-',
    dataFimEtapa2: item.dataFimEtapa2 ? formatarDataBR(item.dataFimEtapa2) : '-',
    dataUltimaMovimentacao: item.dataUltimaMovimentacao ? formatarDataHoraBR(item.dataUltimaMovimentacao) : '-',
    mostraPrazoAjustado
  };
}));

const opcoesProcessos = computed(() => [
  {value: null, text: TEXTOS_RELATORIOS.SELECIONE},
  ...processosMapeamentoRevisao.value.map(p => ({value: p.codigo, text: p.descricao}))
]);

function obterMensagemErroRelatorio(error: unknown, mensagemPadrao: string) {
  const erroNormalizado = normalizarErro(error);
  if (erroNormalizado.tipo === 'inesperado' && !erroNormalizado.status) {
    return mensagemPadrao;
  }
  return erroNormalizado.mensagem || mensagemPadrao;
}

async function gerarRelatorio() {
  if (!codProcessoSelecionado.value) return;
  await acaoRelatorio.executar(
      () => relatoriosStore.buscarRelatorioAndamento(codProcessoSelecionado.value!),
      TEXTOS_RELATORIOS.ERRO_BUSCA,
      {
        relancarErro: false,
        aoOcorrerErro: (_erro, causa) => {
          notify(obterMensagemErroRelatorio(causa, TEXTOS_RELATORIOS.ERRO_BUSCA), "danger");
        },
      },
  );
}

async function exportarPdf() {
  if (!codProcessoSelecionado.value) return;
  await acaoRelatorio.executar(
      () => relatoriosStore.exportarAndamentoPdf(codProcessoSelecionado.value!),
      TEXTOS_RELATORIOS.ERRO_EXPORTAR,
      {
        relancarErro: false,
        aoOcorrerErro: (_erro, causa) => {
          notify(obterMensagemErroRelatorio(causa, TEXTOS_RELATORIOS.ERRO_EXPORTAR), "danger");
        },
      },
  );
}

onMounted(async () => {
  relatoriosStore.limparRelatorio();
  try {
    await carregarProcessos();
  } finally {
    carregandoProcessos.value = false;
  }
});
</script>
