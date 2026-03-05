<template>
  <LayoutPadrao>
    <PageHeader title="Mapa de competências técnicas">
      <template #actions>
        <BButton
            v-if="podeValidar"
            data-testid="btn-mapa-sugestoes"
            title="Apresentar sugestões"
            variant="outline-warning"
            @click="abrirModalSugestoes"
        >
          Apresentar sugestões
        </BButton>
        <BButton
            v-if="podeValidar"
            data-testid="btn-mapa-validar"
            title="Validar mapa"
            variant="outline-success"
            @click="abrirModalValidar"
        >
          Validar
        </BButton>

        <BButton
            v-if="(podeValidar && temHistoricoAnalise) || podeAnalisar"
            :data-testid="podeAnalisar ? 'btn-mapa-historico-gestor' : 'btn-mapa-historico'"
            title="Histórico de análise"
            variant="outline-secondary"
            @click="verHistorico"
        >
          Histórico de análise
        </BButton>

        <BButton
            v-if="podeAnalisar"
            v-show="podeVerSugestoes"
            data-testid="btn-mapa-ver-sugestoes"
            title="Ver sugestões"
            variant="outline-info"
            @click="verSugestoes"
        >
          Ver sugestões
        </BButton>
        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-devolver"
            title="Devolver para ajustes"
            variant="outline-danger"
            @click="abrirModalDevolucao"
        >
          Devolver para ajustes
        </BButton>
        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-homologar-aceite"
            title="Aceitar"
            variant="outline-success"
            @click="abrirModalAceitar"
        >
          {{ podeHomologarMapa ? 'Homologar' : 'Registrar aceite' }}
        </BButton>
      </template>
    </PageHeader>

    <div v-if="unidade">
      <div class="mb-5 d-flex align-items-center">
        <div
            class="fs-5"
            data-testid="txt-header-unidade"
        >
          {{ unidade.sigla }} - {{ unidade.nome }}
        </div>
      </div>

      <div class="mb-4 mt-3">
        <EmptyState
            v-if="!mapa || mapa.competencias.length === 0"
            description="Este mapa ainda não possui competências registradas."
            icon="bi-journal-x"
            title="Nenhuma competência cadastrada"
        />
        <BCard
            v-for="comp in mapa?.competencias"
            :key="comp.codigo"
            class="mb-3 shadow-sm border-0 border-start border-4 border-primary"
        >
          <div class="d-flex justify-content-between align-items-start mb-2">
            <h5 class="mb-0" data-testid="vis-mapa__txt-competencia-descricao">
              {{ comp.descricao }}
            </h5>
          </div>
          <div v-if="comp.atividades && comp.atividades.length > 0" class="mt-3">
            <div
                v-for="atividade in comp.atividades"
                :key="atividade.codigo"
                class="mb-3 p-2 bg-light rounded"
            >
              <div class="d-flex align-items-baseline">
                <i aria-hidden="true" class="bi bi-gear-fill me-2 text-secondary small"/>
                <p class="atividade-associada-descricao mb-1 fw-medium">{{ atividade.descricao }}</p>
              </div>
              <div v-if="atividade.conhecimentos && atividade.conhecimentos.length > 0" class="ms-4 mt-2">
                <div class="d-flex flex-wrap gap-2">
                  <span
                      v-for="c in atividade.conhecimentos"
                      :key="c.codigo"
                      class="badge bg-white text-dark border fw-normal py-1 px-2"
                      data-testid="txt-conhecimento-item"
                  >
                    <i aria-hidden="true" class="bi bi-book me-1 text-info"/>
                    {{ c.descricao }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </BCard>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <AceitarMapaModal
        :loading="isLoading"
        :mostrar-modal="mostrarModalAceitar"
        :perfil="perfilSelecionado || undefined"
        @fechar-modal="fecharModalAceitar"
        @confirmar-aceitacao="confirmarAceitacao"
    />

    <ModalConfirmacao
        v-model="mostrarModalSugestoes"
        :loading="isLoading"
        ok-title="Confirmar"
        test-id-cancelar="btn-sugestoes-mapa-cancelar"
        test-id-confirmar="btn-sugestoes-mapa-confirmar"
        titulo="Apresentar Sugestões"
        @confirmar="confirmarSugestoes"
        @shown="() => sugestoesTextareaRef?.$el?.focus()"
    >
      <div class="mb-3">
        <label
            class="form-label"
            for="sugestoesTextarea"
        >Sugestões para o mapa de competências:</label>
        <BFormTextarea
            id="sugestoesTextarea"
            ref="sugestoesTextareaRef"
            v-model="sugestoes"
            data-testid="inp-sugestoes-mapa-texto"
            placeholder="Digite suas sugestões para o mapa de competências..."
            rows="5"
        />
      </div>
    </ModalConfirmacao>

    <BModal
        v-model="mostrarModalVerSugestoes"
        :fade="false"
        centered
        hide-footer
        title="Sugestões"
    >
      <div class="mb-3">
        <label
            class="form-label"
            for="sugestoesVisualizacao"
        >Sugestões registradas para o mapa de competências:</label>
        <BFormTextarea
            id="sugestoesVisualizacao"
            v-model="sugestoesVisualizacao"
            data-testid="txt-ver-sugestoes-mapa"
            readonly
            rows="5"
        />
      </div>
      <template #footer>
        <BButton
            data-testid="btn-ver-sugestoes-mapa-fechar"
            variant="secondary"
            @click="fecharModalVerSugestoes"
        >
          Fechar
        </BButton>
      </template>
    </BModal>

    <ModalConfirmacao
        v-model="mostrarModalValidar"
        :loading="isLoading"
        ok-title="Validar"
        test-id-cancelar="btn-validar-mapa-cancelar"
        test-id-confirmar="btn-validar-mapa-confirmar"
        titulo="Validar Mapa de Competências"
        variant="success"
        @confirmar="confirmarValidacao"
    >
      <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
    </ModalConfirmacao>

    <ModalConfirmacao
        v-model="mostrarModalDevolucao"
        :loading="isLoading"
        :ok-disabled="!observacaoDevolucao.trim()"
        ok-title="Confirmar"
        test-id-cancelar="btn-devolucao-mapa-cancelar"
        test-id-confirmar="btn-devolucao-mapa-confirmar"
        titulo="Devolução"
        variant="danger"
        @confirmar="confirmarDevolucao"
        @shown="() => observacaoDevolucaoRef?.$el?.focus()"
    >
      <p>Confirma a devolução da validação do mapa para ajustes?</p>
      <div class="mb-3">
        <label
            class="form-label"
            for="observacaoDevolucao"
        >Observação:</label>
        <BFormTextarea
            id="observacaoDevolucao"
            ref="observacaoDevolucaoRef"
            v-model="observacaoDevolucao"
            data-testid="inp-devolucao-mapa-obs"
            placeholder="Digite observações sobre a devolução..."
            rows="3"
        />
      </div>
    </ModalConfirmacao>

    <HistoricoAnaliseModal
        :historico="historicoAnalise"
        :mostrar="mostrarModalHistorico"
        @fechar="fecharModalHistorico"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BCard, BFormTextarea, BModal} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {storeToRefs} from "pinia";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import EmptyState from "@/components/comum/EmptyState.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import AceitarMapaModal from "@/components/mapa/AceitarMapaModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import {useMapasStore} from "@/stores/mapas";
import {useUnidadesStore} from "@/stores/unidades";
import {useProcessosStore} from "@/stores/processos";
import {useAnalisesStore} from "@/stores/analises";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useFeedbackStore} from "@/stores/feedback";
import {usePerfil} from "@/composables/usePerfil";
import {useAcesso} from "@/composables/useAcesso";
import logger from "@/utils/logger";

const route = useRoute();
const router = useRouter();
const unidadesStore = useUnidadesStore();
const mapaStore = useMapasStore();
const processosStore = useProcessosStore();
const analisesStore = useAnalisesStore();
const subprocessosStore = useSubprocessosStore();
const feedbackStore = useFeedbackStore();
const {perfilSelecionado} = usePerfil();
const {mapaVisualizacao: mapa} = storeToRefs(mapaStore);

const sigla = computed(() => route.params.siglaUnidade as string);
const codProcesso = computed(() => Number(route.params.codProcesso));

const unidade = computed(() => unidadesStore.unidade);

function buscarUnidadeRecursivo(unidades: any[], siglaAlvo: string): any | null {
  for (const u of unidades) {
    if (u.sigla === siglaAlvo) return u;
    if (u.filhos && u.filhos.length > 0) {
      const encontrada = buscarUnidadeRecursivo(u.filhos, siglaAlvo);
      if (encontrada) return encontrada;
    }
  }
  return null;
}

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return buscarUnidadeRecursivo(processosStore.processoDetalhe.unidades, sigla.value);
});

const codSubprocesso = computed(() => subprocesso.value?.codSubprocesso);

const {
  podeValidarMapa,
  podeAceitarMapa,
  podeDevolverMapa,
  podeHomologarMapa,
  podeApresentarSugestoes
} = useAcesso(computed(() => subprocessosStore.subprocessoDetalhe));

const podeValidar = computed(() => podeValidarMapa.value);
const podeAnalisar = computed(() => {
  return (
      podeAceitarMapa.value ||
      podeDevolverMapa.value ||
      podeHomologarMapa.value
  );
});
const podeVerSugestoes = computed(() => podeApresentarSugestoes.value);

const historicoAnalise = computed(() => {
  return analisesStore.analisesCadastro || [];
});

const temHistoricoAnalise = computed(() => historicoAnalise.value.length > 0);

const mostrarModalAceitar = ref(false);
const mostrarModalSugestoes = ref(false);
const mostrarModalVerSugestoes = ref(false);
const mostrarModalValidar = ref(false);
const mostrarModalDevolucao = ref(false);
const mostrarModalHistorico = ref(false);
const sugestoes = ref("");
const sugestoesVisualizacao = ref("");
const observacaoDevolucao = ref("");

const isLoading = ref(false);

// Refs para foco
const sugestoesTextareaRef = ref<InstanceType<typeof BFormTextarea> | null>(null);
const observacaoDevolucaoRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

async function confirmarSugestoes() {
  if (!codSubprocesso.value) return;
  isLoading.value = true;
  try {
    await processosStore.apresentarSugestoes(codSubprocesso.value, {
      sugestoes: sugestoes.value,
    });
    fecharModalSugestoes();
    feedbackStore.show(
        "Sugestões apresentadas",
        "Sugestões submetidas para análise da unidade superior",
        "success"
    );
    await router.push({name: "Painel"});
  } catch {
    feedbackStore.show("Erro ao apresentar sugestões", "Ocorreu um erro. Tente novamente.", "danger");
  } finally {
    isLoading.value = false;
  }
}

async function confirmarValidacao() {
  if (!codSubprocesso.value) return;
  isLoading.value = true;
  try {
    await processosStore.validarMapa(codSubprocesso.value);
    fecharModalValidar();
    feedbackStore.show("Mapa validado", "Mapa validado e submetido para análise da unidade superior", "success");
    await router.push({name: "Painel"});
  } catch {
    feedbackStore.show("Erro ao validar mapa", "Ocorreu um erro. Tente novamente.", "danger");
  } finally {
    isLoading.value = false;
  }
}

async function confirmarAceitacao() {
  if (!codSubprocesso.value) return;
  isLoading.value = true;
  const isHomologacao = podeHomologarMapa.value || perfilSelecionado.value === "ADMIN";

  try {
    if (isHomologacao) {
      await processosStore.homologarValidacao(codSubprocesso.value);
    } else {
      await processosStore.aceitarValidacao(codSubprocesso.value);
    }
    fecharModalAceitar();
    feedbackStore.show(
        "Sucesso",
        isHomologacao ? "Homologação efetivada" : "Aceite registrado",
        "success",
    );
    await router.push({name: "Painel"});
  } catch (error) {
    logger.error(error);
    feedbackStore.show("Erro", "Erro ao realizar a operação.", "danger");
  } finally {
    isLoading.value = false;
  }
}

async function confirmarDevolucao() {
  if (!codSubprocesso.value) return;
  isLoading.value = true;
  try {
    await processosStore.devolverValidacao(codSubprocesso.value, {
      justificativa: observacaoDevolucao.value,
    });
    fecharModalDevolucao();
    feedbackStore.show("Sucesso", "Devolução realizada", "success");
    await router.push({name: "Painel"});
  } catch (error) {
    logger.error(error);
    feedbackStore.show("Erro", "Erro ao devolver.", "danger");
  } finally {
    isLoading.value = false;
  }
}

function abrirModalAceitar() {
  mostrarModalAceitar.value = true;
}

function fecharModalAceitar() {
  mostrarModalAceitar.value = false;
}

function abrirModalSugestoes() {
  mostrarModalSugestoes.value = true;
}

function fecharModalSugestoes() {
  mostrarModalSugestoes.value = false;
  sugestoes.value = "";
}

function verSugestoes() {
  sugestoesVisualizacao.value = mapa.value?.sugestoes || "Nenhuma sugestão registrada.";
  mostrarModalVerSugestoes.value = true;
}

function fecharModalVerSugestoes() {
  mostrarModalVerSugestoes.value = false;
  sugestoesVisualizacao.value = "";
}

function abrirModalValidar() {
  mostrarModalValidar.value = true;
}

function fecharModalValidar() {
  mostrarModalValidar.value = false;
}

function abrirModalDevolucao() {
  mostrarModalDevolucao.value = true;
}

function fecharModalDevolucao() {
  mostrarModalDevolucao.value = false;
  observacaoDevolucao.value = "";
}

async function abrirModalHistorico() {
  if (codSubprocesso.value) {
    await analisesStore.carregarHistorico(codSubprocesso.value);
  }
  mostrarModalHistorico.value = true;
}

function fecharModalHistorico() {
  mostrarModalHistorico.value = false;
}

function verHistorico() {
  abrirModalHistorico();
}

onMounted(async () => {
  await unidadesStore.buscarUnidade(sigla.value);
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  if (codSubprocesso.value) {
    await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value);
    await mapaStore.buscarMapaVisualizacao(codSubprocesso.value);
  }
});

defineExpose({
  perfilSelecionado,
  mapa,
  unidade,
  podeValidar,
  podeAnalisar,
  podeVerSugestoes,
  temHistoricoAnalise,
  historicoAnalise,
  mostrarModalAceitar,
  mostrarModalSugestoes,
  mostrarModalVerSugestoes,
  mostrarModalValidar,
  mostrarModalDevolucao,
  mostrarModalHistorico,
  sugestoes,
  sugestoesVisualizacao,
  observacaoDevolucao,
  isLoading,
  confirmarSugestoes,
  confirmarValidacao,
  confirmarAceitacao,
  confirmarDevolucao,
  abrirModalAceitar,
  fecharModalAceitar,
  abrirModalSugestoes,
  verSugestoes,
  fecharModalVerSugestoes,
  abrirModalValidar,
  abrirModalDevolucao,
  abrirModalHistorico,
  fecharModalHistorico,
  verHistorico,
  sugestoesTextareaRef,
  observacaoDevolucaoRef
});
</script>
