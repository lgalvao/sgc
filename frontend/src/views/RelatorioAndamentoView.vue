<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.relatorios.ANDAMENTO_PROCESSO">
      <template #actions>
        <BButton variant="outline-secondary" to="/relatorios">
          <i class="bi bi-arrow-left me-1"/> Voltar
        </BButton>
      </template>
    </PageHeader>

    <BCard class="mb-4">
      <BRow class="align-items-end">
        <BCol md="6">
          <BFormGroup :label="TEXTOS.relatorios.LABEL_SELECIONE_PROCESSO" label-for="select-processo">
            <BFormSelect
                id="select-processo"
                v-model="codProcessoSelecionado"
                :options="opcoesProcessos"
                data-testid="select-processo-andamento"
            />
          </BFormGroup>
        </BCol>
        <BCol md="auto">
          <BButton
              :disabled="!codProcessoSelecionado || carregando"
              variant="primary"
              data-testid="btn-gerar-andamento"
              @click="gerarRelatorio"
          >
            <BSpinner v-if="carregando" small class="me-1"/>
            <i v-else class="bi bi-search me-1"/>
            {{ TEXTOS.relatorios.BOTAO_GERAR }}
          </BButton>
        </BCol>
        <BCol v-if="relatorioAndamento.length > 0" md="auto">
          <BButton variant="outline-success" data-testid="btn-exportar-andamento" @click="exportarPdf">
            <i class="bi bi-file-earmark-pdf me-1"/>
            {{ TEXTOS.relatorios.BOTAO_PDF }}
          </BButton>
        </BCol>
      </BRow>
    </BCard>

    <div v-if="carregando && relatorioAndamento.length === 0" class="text-center py-5">
      <BSpinner variant="primary"/>
    </div>

    <template v-else>
      <div v-if="relatorioAndamento.length > 0" class="d-flex flex-column gap-3">
        <BCard v-for="(item, index) in linhasRelatorioAndamento" :key="index" no-body class="border-start border-4 border-secondary shadow-sm" data-testid="card-resultado-andamento">
          <BCardBody>
            <BCardTitle class="mb-3">
              <span class="fw-bold text-primary">{{ item.siglaUnidade }} - {{ item.nomeUnidade }}</span>
            </BCardTitle>
            
            <div class="row mb-3 pb-3 border-bottom">
              <div class="col-md-6 col-lg-4 mb-2 mb-md-0">
                <span class="text-muted d-block small">Situação</span>
                <span class="fw-bold text-dark">{{ item.situacaoAtual }}</span>
              </div>
              <div class="col-md-6 col-lg-4 mb-2 mb-md-0">
                <span class="text-muted d-block small">Localização</span>
                <span class="fw-bold text-dark">{{ item.localizacao }}</span>
              </div>
              <div class="col-md-12 col-lg-4">
                <span class="text-muted d-block small">Última movimentação</span>
                <span class="fw-bold text-dark">{{ item.dataUltimaMovimentacao }}</span>
              </div>
            </div>

            <div class="row mb-4">
              <div class="col-md-6 mb-3 mb-md-0">
                <h6 class="fw-bold text-primary mb-2">ETAPA 1: CADASTRO</h6>
                <div class="d-flex gap-4">
                  <div>
                    <span class="text-muted d-block small">Data limite</span>
                    <span class="fw-bold">{{ item.dataLimiteEtapa1 }}</span>
                  </div>
                  <div>
                    <span class="text-muted d-block small">Conclusão</span>
                    <span class="fw-bold">{{ item.dataFimEtapa1 }}</span>
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <h6 class="fw-bold text-primary mb-2">ETAPA 2: MAPA</h6>
                <div class="d-flex gap-4">
                  <div>
                    <span class="text-muted d-block small">Data limite</span>
                    <span class="fw-bold">
                      {{ item.dataLimiteEtapa2 }}
                      <span v-if="item.mostraPrazoAjustado" class="text-muted fst-italic fw-normal">(Prazo ajustado)</span>
                    </span>
                  </div>
                  <div>
                    <span class="text-muted d-block small">Conclusão</span>
                    <span class="fw-bold">{{ item.dataFimEtapa2 }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="row">
              <div class="col-md-6">
                <span class="text-muted d-block small">Titular</span>
                <span class="fw-bold">{{ item.titular }}</span>
              </div>
              <div class="col-md-6" v-if="item.titular !== item.responsavel">
                <span class="text-muted d-block small">Responsável atual</span>
                <span class="fw-bold">{{ item.responsavel }}</span>
              </div>
            </div>
          </BCardBody>
        </BCard>
      </div>
      <EmptyState
          v-else-if="codProcessoSelecionado && !carregando"
          :title="TEXTOS.relatorios.EMPTY_TITLE"
          :description="TEXTOS.relatorios.EMPTY_DESCRIPTION"
          icon="bi-table"
          data-testid="empty-state-andamento"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton, BCard, BCardBody, BCardTitle, BCol, BFormGroup, BFormSelect, BRow, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useRelatoriosStore} from "@/stores/relatorios";
import {TEXTOS} from "@/constants/textos";
import * as painelService from "@/services/painelService";
import type {ProcessoResumo} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";
import {formatDateBR, formatDateTimeBR} from "@/utils/dateUtils";

const relatoriosStore = useRelatoriosStore();
const {notify} = useNotification();

const codProcessoSelecionado = ref<number | null>(null);
const processosDisponiveis = ref<ProcessoResumo[]>([]);
const carregando = ref(false);

const relatorioAndamento = computed(() => relatoriosStore.relatorioAndamento);

const linhasRelatorioAndamento = computed(() => relatorioAndamento.value.map(item => {
  const dt1 = item.dataLimiteEtapa1;
  const dt2 = item.dataLimiteEtapa2;
  const dt1Formatada = dt1 ? formatDateBR(dt1) : '-';
  const dt2Formatada = dt2 ? formatDateBR(dt2) : '-';
  const mostraPrazoAjustado = dt2 && dt1 && dt2Formatada !== dt1Formatada;

  return {
    ...item,
    localizacao: item.localizacao || '-',
    dataLimiteEtapa1: dt1Formatada,
    dataLimiteEtapa2: dt2Formatada,
    dataFimEtapa1: item.dataFimEtapa1 ? formatDateBR(item.dataFimEtapa1) : '-',
    dataFimEtapa2: item.dataFimEtapa2 ? formatDateBR(item.dataFimEtapa2) : '-',
    dataUltimaMovimentacao: item.dataUltimaMovimentacao ? formatDateTimeBR(item.dataUltimaMovimentacao) : '-',
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
  if (!codProcessoSelecionado.value) return;
  carregando.value = true;
  await relatoriosStore.buscarRelatorioAndamento(codProcessoSelecionado.value);
  carregando.value = false;
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
  }
}

async function exportarPdf() {
  if (!codProcessoSelecionado.value) return;
  await relatoriosStore.exportarAndamentoPdf(codProcessoSelecionado.value);
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_EXPORTAR, "danger");
  }
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
  carregarProcessos();
});
</script>
