<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando && relatorio.length === 0"/>

    <template v-else>
      <PageHeader :title="TEXTOS_RELATORIOS.GAPS_DIAGNOSTICO">
        <template #actions>
          <BButton to="/relatorios" variant="outline-secondary">
            <i class="bi bi-arrow-left me-1"/> Voltar
          </BButton>
        </template>
      </PageHeader>

      <RelatorioDiagnosticoFiltros
          :carregando="carregando"
          :cod-processo-selecionado="codProcessoSelecionado"
          :opcoes-processos="opcoesProcessos"
          :pode-gerar="podeGerar"
          :unidades-disponiveis="unidadesDisponiveis"
          :unidades-selecionadas="unidadesSelecionadas"
          @exportar="exportarPdf"
          @gerar="gerarRelatorio"
          @update:cod-processo-selecionado="atualizarProcessoSelecionado"
          @update:unidades-selecionadas="unidadesSelecionadas = $event"/>

      <RelatorioDiagnosticoGapsResultado
          v-if="relatorio.length > 0"
          :campos="campos"
          :itens="relatorio"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import RelatorioDiagnosticoGapsResultado from "@/components/relatorios/RelatorioDiagnosticoGapsResultado.vue";
import RelatorioDiagnosticoFiltros from "@/components/relatorios/RelatorioDiagnosticoFiltros.vue";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {useRelatorioAndamentoTela} from "@/composables/useRelatorioAndamentoTela";
import {useNotification} from "@/composables/useNotification";
import {useRelatoriosStore} from "@/stores/relatorios";
import {buscarContextoCompleto} from "@/services/processo";
import {type ProcessoResumo, TipoProcesso, type Unidade, type UnidadeParticipante} from "@/types/tipos";
import {normalizarErro} from "@/utils/apiError";
import {useAsyncAction} from "@/composables/useAsyncAction";

const relatoriosStore = useRelatoriosStore();
const {processosDisponiveis, carregarProcessos} = useRelatorioAndamentoTela();
const {notify} = useNotification();
const acaoRelatorio = useAsyncAction();

const codProcessoSelecionado = ref<number | null>(null);
const unidadesSelecionadas = ref<number[]>([]);
const unidadesDisponiveis = ref<Unidade[]>([]);

const relatorio = computed(() => relatoriosStore.relatorioGapsDiagnostico);
const carregando = acaoRelatorio.carregando;
const processosDiagnostico = computed(() => processosDisponiveis.value.filter((processo: ProcessoResumo) => processo.tipo === TipoProcesso.DIAGNOSTICO));
const opcoesProcessos = computed(() => [
  {value: null, text: TEXTOS_RELATORIOS.SELECIONE},
  ...processosDiagnostico.value.map(processo => ({value: processo.codigo, text: processo.descricao}))
]);
const podeGerar = computed(() => !!codProcessoSelecionado.value && unidadesSelecionadas.value.length > 0);

const campos = [
  {key: "competenciaDescricao", label: "Competência"},
  {key: "mediaGap", label: "Gap médio"},
  {key: "totalAvaliacoesConsideradas", label: "Avaliações consideradas"},
];

function mapearUnidadesParticipantes(unidades: UnidadeParticipante[]): Unidade[] {
  return unidades.map(unidade => ({
    codigo: unidade.codUnidade,
    nome: unidade.nome,
    sigla: unidade.sigla,
    unidadeSuperiorCodigo: unidade.codUnidadeSuperior,
    filhas: mapearUnidadesParticipantes(unidade.filhos ?? []),
    isElegivel: unidade.codSubprocesso != null,
  }));
}

function obterMensagemErro(error: unknown, mensagemPadrao: string) {
  const erro = normalizarErro(error);
  return erro.mensagem || mensagemPadrao;
}

async function atualizarProcessoSelecionado(codigoProcesso: number | null) {
  codProcessoSelecionado.value = codigoProcesso;
  unidadesSelecionadas.value = [];
  unidadesDisponiveis.value = [];
  relatoriosStore.limparRelatorio();

  if (!codigoProcesso) {
    return;
  }

  await acaoRelatorio.executar(
      () => buscarContextoCompleto(codigoProcesso),
      TEXTOS_RELATORIOS.ERRO_CARREGAR_PROCESSO,
      {
        relancarErro: false,
        aoSucesso: (processo) => {
          unidadesDisponiveis.value = mapearUnidadesParticipantes(processo.unidades);
        },
        aoOcorrerErro: (_erro, causa) => {
          notify(obterMensagemErro(causa, TEXTOS_RELATORIOS.ERRO_CARREGAR_PROCESSO), "danger");
        },
      },
  );
}

async function gerarRelatorio() {
  if (!podeGerar.value || !codProcessoSelecionado.value) {
    return;
  }
  await acaoRelatorio.executar(
      () => relatoriosStore.buscarRelatorioGapsDiagnostico(codProcessoSelecionado.value!, unidadesSelecionadas.value),
      TEXTOS_RELATORIOS.ERRO_BUSCA,
      {
        relancarErro: false,
        aoOcorrerErro: (_erro, causa) => {
          notify(obterMensagemErro(causa, TEXTOS_RELATORIOS.ERRO_BUSCA), "danger");
        },
      },
  );
}

async function exportarPdf() {
  if (!podeGerar.value || !codProcessoSelecionado.value) {
    return;
  }
  await acaoRelatorio.executar(
      () => relatoriosStore.exportarRelatorioGapsDiagnosticoPdf(codProcessoSelecionado.value!, unidadesSelecionadas.value),
      TEXTOS_RELATORIOS.ERRO_EXPORTAR,
      {
        relancarErro: false,
        aoOcorrerErro: (_erro, causa) => {
          notify(obterMensagemErro(causa, TEXTOS_RELATORIOS.ERRO_EXPORTAR), "danger");
        },
      },
  );
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
  carregarProcessos();
});
</script>
