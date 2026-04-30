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
      <div class="d-flex flex-column gap-3">
        <BFormGroup label-for="select-processo">
          <template #label>
            {{ TEXTOS.relatorios.LABEL_SELECIONE_PROCESSO }} <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <BFormSelect
              id="select-processo"
              v-model="codProcessoSelecionado"
              :options="opcoesProcessos"
              data-testid="select-processo-andamento"
          />
        </BFormGroup>

        <div class="d-flex flex-wrap gap-2">
          <BButton
              :disabled="carregando || !codProcessoSelecionado"
              variant="success"
              data-testid="btn-gerar-andamento"
              @click="gerarRelatorio"
          >
            <BSpinner v-if="carregando" small class="me-1"/>
            <i v-else class="bi bi-search me-1"/>
            {{ TEXTOS.relatorios.BOTAO_GERAR }}
          </BButton>
          <BButton
              :disabled="carregando || !codProcessoSelecionado"
              variant="outline-danger"
              data-testid="btn-exportar-andamento"
              @click="exportarPdf"
          >
            <i class="bi bi-file-earmark-pdf me-1"/>
            PDF
          </BButton>
        </div>
      </div>
    </BCard>

    <div v-if="carregando && relatorioAndamento.length === 0" class="text-center py-5">
      <BSpinner variant="primary"/>
    </div>

    <template v-else>
      <div v-if="relatorioAndamento.length > 0" class="d-flex flex-column gap-3">
        <BCard
            v-for="(item, index) in linhasRelatorioAndamento"
            :key="index"
            no-body
            class="relatorio-andamento__card shadow-sm"
            data-testid="card-resultado-andamento"
        >
          <BCardBody>
            <BCardTitle class="mb-3 relatorio-andamento__cabecalho">
              <span class="relatorio-andamento__titulo">{{ item.siglaUnidade }}</span>
              <span class="relatorio-andamento__subtitulo">{{ item.nomeUnidade }}</span>
            </BCardTitle>

            <div class="relatorio-andamento__secao-geral mb-4">
              <div class="relatorio-andamento__grid-geral">
                <div class="relatorio-andamento__info-item">
                  <span class="relatorio-andamento__info-label">Situação</span>
                  <span class="relatorio-andamento__info-valor fw-bold text-dark">{{ item.situacaoAtual }}</span>
                </div>
                <div class="relatorio-andamento__info-item">
                  <span class="relatorio-andamento__info-label">Localização</span>
                  <span class="relatorio-andamento__info-valor fw-bold text-dark">{{ item.localizacao }}</span>
                </div>
                <div class="relatorio-andamento__info-item">
                  <span class="relatorio-andamento__info-label">Última movimentação</span>
                  <span class="relatorio-andamento__info-valor fw-bold text-dark">{{ item.dataUltimaMovimentacao }}</span>
                </div>
              </div>
            </div>

            <div class="relatorio-andamento__etapas mb-4">
              <div class="row">
                <div class="col-md-6 mb-3 mb-md-0">
                  <div class="relatorio-andamento__etapa-card h-100">
                    <h6 class="relatorio-andamento__etapa-titulo">ETAPA 1: CADASTRO</h6>
                    <div class="d-flex gap-4">
                      <div class="relatorio-andamento__info-item">
                        <span class="relatorio-andamento__info-label">Data limite</span>
                        <span class="relatorio-andamento__info-valor">{{ item.dataLimiteEtapa1 }}</span>
                      </div>
                      <div class="relatorio-andamento__info-item">
                        <span class="relatorio-andamento__info-label">Conclusão</span>
                        <span class="relatorio-andamento__info-valor">{{ item.dataFimEtapa1 }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="relatorio-andamento__etapa-card h-100">
                    <h6 class="relatorio-andamento__etapa-titulo">ETAPA 2: MAPA</h6>
                    <div class="d-flex gap-4">
                      <div class="relatorio-andamento__info-item">
                        <span class="relatorio-andamento__info-label">Data limite</span>
                        <span class="relatorio-andamento__info-valor">
                          {{ item.dataLimiteEtapa2 }}
                          <small v-if="item.mostraPrazoAjustado" class="text-muted fst-italic fw-normal">(Prazo ajustado)</small>
                        </span>
                      </div>
                      <div class="relatorio-andamento__info-item">
                        <span class="relatorio-andamento__info-label">Conclusão</span>
                        <span class="relatorio-andamento__info-valor">{{ item.dataFimEtapa2 }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="relatorio-andamento__responsaveis pt-3 border-top">
              <div class="row">
                <div class="col-md-6 mb-2 mb-md-0">
                  <span class="relatorio-andamento__info-label">Titular</span>
                  <span class="relatorio-andamento__info-valor fw-bold">{{ item.titular }}</span>
                </div>
                <div v-if="item.titular !== item.responsavel" class="col-md-6">
                  <span class="relatorio-andamento__info-label">Responsável atual</span>
                  <span class="relatorio-andamento__info-valor fw-bold">{{ item.responsavel }}</span>
                </div>
              </div>
            </div>
          </BCardBody>
        </BCard>
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {
  BButton,
  BCard,
  BCardBody,
  BCardTitle,
  BFormGroup,
  BFormSelect,
  BSpinner
} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
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
  if (!codProcessoSelecionado.value) {
    return;
  }

  carregando.value = true;
  await relatoriosStore.buscarRelatorioAndamento(codProcessoSelecionado.value!);
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

<style scoped>
.relatorio-andamento__card {
  border: 1px solid #c8d1dc;
  background: #fff;
  border-radius: 0.75rem;
  overflow: hidden;
}

.relatorio-andamento__cabecalho {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  padding-bottom: 0.85rem;
  margin-bottom: 1.2rem;
  border-bottom: 1px solid #d7dee8;
}

.relatorio-andamento__titulo {
  color: #16365f;
  font-size: 1.45rem;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.relatorio-andamento__subtitulo {
  color: #4b5d73;
  font-size: 0.95rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.relatorio-andamento__grid-geral {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.relatorio-andamento__info-item {
  display: flex;
  flex-direction: column;
}

.relatorio-andamento__info-label {
  color: #6b7280;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.025em;
  margin-bottom: 0.15rem;
}

.relatorio-andamento__info-valor {
  color: #1f2937;
  font-size: 0.95rem;
}

.relatorio-andamento__etapa-card {
  padding: 1rem;
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 0.6rem;
}

.relatorio-andamento__etapa-titulo {
  color: #1d3557;
  font-size: 0.9rem;
  font-weight: 700;
  margin-bottom: 0.75rem;
  letter-spacing: 0.02em;
}

@media (max-width: 768px) {
  .relatorio-andamento__titulo {
    font-size: 1.2rem;
  }
}
</style>
