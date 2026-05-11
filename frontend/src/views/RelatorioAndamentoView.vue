<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando && relatorioAndamento.length === 0"/>

    <template v-else>
      <PageHeader :title="TEXTOS.relatorios.ANDAMENTO_PROCESSO">
        <template #actions>
          <BButton to="/relatorios" variant="outline-secondary">
            <i class="bi bi-arrow-left me-1"/> Voltar
          </BButton>
        </template>
      </PageHeader>

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
import {useRelatoriosStore} from "@/stores/relatorios";
import {TEXTOS} from "@/constants/textos";
import * as painelService from "@/services/painelService";
import type {ProcessoResumo} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";
import {formatarDataBR, formatarDataHoraBR} from "@/utils/date";

const relatoriosStore = useRelatoriosStore();
const {notify} = useNotification();

const codProcessoSelecionado = ref<number | null>(null);
const processosDisponiveis = ref<ProcessoResumo[]>([]);
const carregando = ref(false);

const relatorioAndamento = computed(() => relatoriosStore.relatorioAndamento);


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
  {value: null, text: TEXTOS.relatorios.SELECIONE},
  ...processosDisponiveis.value.map(p => ({value: p.codigo, text: p.descricao}))
]);

async function carregarProcessos() {
  try {
    const response = await painelService.listarProcessos({page: 0, size: 100});
    processosDisponiveis.value = response?.content ?? [];
  } catch {
    notify("Erro ao carregar processos", "danger");
  }
}

async function gerarRelatorio() {
  if (!codProcessoSelecionado.value) {
    return;
  }

  try {
    carregando.value = true;
    await relatoriosStore.buscarRelatorioAndamento(codProcessoSelecionado.value);
  } catch {
    // O erro já é normalizado na store; a view só precisa encerrar o estado de carregamento.
  } finally {
    carregando.value = false;
  }

  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
  }
}

async function exportarPdf() {
  if (!codProcessoSelecionado.value) return;

  try {
    carregando.value = true;
    await relatoriosStore.exportarAndamentoPdf(codProcessoSelecionado.value);
  } catch {
    // O erro já é normalizado na store; a view só precisa encerrar o estado de carregamento.
  } finally {
    carregando.value = false;
  }

  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_EXPORTAR, "danger");
  }
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
  carregarProcessos();
});
</script>
