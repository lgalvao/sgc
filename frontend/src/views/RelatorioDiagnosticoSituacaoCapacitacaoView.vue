<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando && relatorio.length === 0"/>

    <template v-else>
      <PageHeader :title="TEXTOS_RELATORIOS.SITUACAO_CAPACITACAO_DIAGNOSTICO">
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

      <div v-if="relatorio.length > 0" class="d-flex flex-column gap-3">
        <BCard
            v-for="item in relatorio"
            :key="item.codigoUnidade"
            class="shadow-sm"
            data-testid="card-relatorio-situacao-capacitacao-diagnostico"
        >
          <BCardTitle class="mb-1 relatorio-diagnostico__titulo">{{ item.siglaUnidade }}</BCardTitle>
          <BCardText class="text-muted mb-3">{{ item.nomeUnidade }}</BCardText>

          <BTable
              :fields="campos"
              :items="item.competencias"
              bordered
              responsive
              small
          />
        </BCard>
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton, BCard, BCardText, BCardTitle, BTable} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import RelatorioDiagnosticoFiltros from "@/components/relatorios/RelatorioDiagnosticoFiltros.vue";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {useRelatorioAndamentoTela} from "@/composables/useRelatorioAndamentoTela";
import {useNotification} from "@/composables/useNotification";
import {useRelatoriosStore} from "@/stores/relatorios";
import {buscarContextoCompleto} from "@/services/processo";
import {type ProcessoResumo, TipoProcesso, type Unidade, type UnidadeParticipante} from "@/types/tipos";
import {normalizarErro} from "@/utils/apiError";

const relatoriosStore = useRelatoriosStore();
const {processosDisponiveis, carregarProcessos} = useRelatorioAndamentoTela();
const {notify} = useNotification();

const codProcessoSelecionado = ref<number | null>(null);
const unidadesSelecionadas = ref<number[]>([]);
const unidadesDisponiveis = ref<Unidade[]>([]);
const carregando = ref(false);

const relatorio = computed(() => relatoriosStore.relatorioSituacaoCapacitacaoDiagnostico);
const processosDiagnostico = computed(() => processosDisponiveis.value.filter((processo: ProcessoResumo) => processo.tipo === TipoProcesso.DIAGNOSTICO));
const opcoesProcessos = computed(() => [
  {value: null, text: TEXTOS_RELATORIOS.SELECIONE},
  ...processosDiagnostico.value.map(processo => ({value: processo.codigo, text: processo.descricao}))
]);
const podeGerar = computed(() => !!codProcessoSelecionado.value && unidadesSelecionadas.value.length > 0);

const campos = [
  {key: "competenciaDescricao", label: "Competência"},
  {key: "totalNaoSeAplica", label: "NA"},
  {key: "totalACapacitar", label: "AC"},
  {key: "totalEmCapacitacao", label: "EC"},
  {key: "totalCapacitado", label: "C"},
  {key: "totalInstrutor", label: "I"},
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

  carregando.value = true;
  try {
    const processo = await buscarContextoCompleto(codigoProcesso);
    unidadesDisponiveis.value = mapearUnidadesParticipantes(processo.unidades);
  } catch (error) {
    notify(obterMensagemErro(error, TEXTOS_RELATORIOS.ERRO_CARREGAR_PROCESSO), "danger");
  } finally {
    carregando.value = false;
  }
}

async function gerarRelatorio() {
  if (!podeGerar.value || !codProcessoSelecionado.value) {
    return;
  }
  carregando.value = true;
  try {
    await relatoriosStore.buscarRelatorioSituacaoCapacitacaoDiagnostico(codProcessoSelecionado.value, unidadesSelecionadas.value);
  } catch (error) {
    notify(obterMensagemErro(error, TEXTOS_RELATORIOS.ERRO_BUSCA), "danger");
  } finally {
    carregando.value = false;
  }
}

async function exportarPdf() {
  if (!podeGerar.value || !codProcessoSelecionado.value) {
    return;
  }
  carregando.value = true;
  try {
    await relatoriosStore.exportarRelatorioSituacaoCapacitacaoDiagnosticoPdf(codProcessoSelecionado.value, unidadesSelecionadas.value);
  } catch (error) {
    notify(obterMensagemErro(error, TEXTOS_RELATORIOS.ERRO_EXPORTAR), "danger");
  } finally {
    carregando.value = false;
  }
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
  carregarProcessos();
});
</script>

<style scoped>
.relatorio-diagnostico__titulo {
  color: var(--bs-heading-color);
  font-size: 1.05rem;
}
</style>
