<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.mapa.TITULO_TECNICO">
      <template #default>
        <div class="fs-5" data-testid="subprocesso-header__txt-header-unidade">
          {{ unidade?.sigla }}
        </div>
      </template>
      <template #actions>
        <BButton
            v-if="podeValidar"
            data-testid="btn-mapa-sugestoes"
            variant="outline-secondary"
            @click="abrirModalSugestoes"
        >
          {{ TEXTOS.mapa.BOTAO_SUGESTOES }}
        </BButton>

        <BButton
            v-if="podeVerSugestoes"
            data-testid="btn-mapa-ver-sugestoes"
            variant="outline-secondary"
            @click="verSugestoes"
        >
          {{ TEXTOS.mapa.BOTAO_VER_SUGESTOES }}
        </BButton>

        <BButton
            v-if="(podeValidar && temHistoricoAnalise) || podeAnalisar"
            :data-testid="podeAnalisar ? 'btn-mapa-historico-gestor' : 'btn-mapa-historico'"
            variant="outline-secondary"
            @click="verHistorico"
        >
          {{ TEXTOS.mapa.BOTAO_HISTORICO_ANALISE }}
        </BButton>

        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-devolver"
            :disabled="!habilitarAnalisarMapa"
            variant="secondary"
            @click="abrirModalDevolucao"
        >
          {{ TEXTOS.mapa.BOTAO_DEVOLVER }}
        </BButton>

        <BButton
            v-if="podeValidar"
            data-testid="btn-mapa-validar"
            :disabled="!habilitarValidar"
            variant="success"
            @click="abrirModalValidar"
        >
          {{ TEXTOS.mapa.BOTAO_VALIDAR }}
        </BButton>

        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-homologar-aceite"
            :disabled="!habilitarAnalisarMapa"
            variant="success"
            @click="abrirModalAceitar"
        >
          {{ podeHomologarMapa ? TEXTOS.mapa.LABEL_HOMOLOGAR : TEXTOS.mapa.LABEL_REGISTRAR_ACEITE }}
        </BButton>
      </template>
    </PageHeader>

    <div v-if="unidade">
      <div class="mb-5 d-flex align-items-center">
        <div class="fs-5" data-testid="txt-header-unidade">
          {{ unidade.sigla }}
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
                  <BBadge
                      v-for="c in atividade.conhecimentos"
                      :key="c.codigo"
                      variant="light"
                      class="bg-white text-dark border fw-normal py-1 px-2"
                      data-testid="txt-conhecimento-item"
                  >
                    <i aria-hidden="true" class="bi bi-book me-1 text-info"/>
                    {{ c.descricao }}
                  </BBadge>
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
        :ok-disabled="!sugestoes.trim()"
        ok-title="Confirmar"
        test-id-cancelar="btn-sugestoes-mapa-cancelar"
        test-id-confirmar="btn-sugestoes-mapa-confirmar"
        titulo="Apresentar sugestões"
        @confirmar="confirmarSugestoes"
        @shown="() => sugestoesTextareaRef?.$el?.focus()"
    >
      <BFormGroup
          label="Sugestões para o mapa de competências:"
          label-for="sugestoesTextarea"
          class="mb-3"
      >
        <BFormTextarea
            id="sugestoesTextarea"
            ref="sugestoesTextareaRef"
            v-model="sugestoes"
            aria-required="true"
            data-testid="inp-sugestoes-mapa-texto"
            rows="5"
        />
      </BFormGroup>
    </ModalConfirmacao>

    <ModalPadrao
        v-model="mostrarModalVerSugestoes"
        :mostrar-botao-acao="false"
        test-id-cancelar="btn-ver-sugestoes-mapa-fechar"
        texto-cancelar="Fechar"
        titulo="Sugestões"
        @fechar="fecharModalVerSugestoes"
    >
      <BFormGroup
          label="Sugestões registradas para o mapa de competências:"
          label-for="sugestoesVisualizacao"
          class="mb-3"
      >
        <BFormTextarea
            id="sugestoesVisualizacao"
            v-model="sugestoesVisualizacao"
            data-testid="txt-ver-sugestoes-mapa"
            readonly
            rows="5"
        />
      </BFormGroup>
    </ModalPadrao>

    <ModalConfirmacao
        v-model="mostrarModalValidar"
        :loading="isLoading"
        ok-title="Validar"
        test-id-cancelar="btn-validar-mapa-cancelar"
        test-id-confirmar="btn-validar-mapa-confirmar"
        titulo="Validação de mapa"
        variant="success"
        @confirmar="confirmarValidacao"
    >
      <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
    </ModalConfirmacao>

    <ModalConfirmacao
        v-model="mostrarModalDevolucao"
        :loading="isLoading"
        :ok-disabled="!observacaoDevolucao.trim()"
        :ok-title="TEXTOS.mapa.BOTAO_DEVOLVER"
        test-id-cancelar="btn-devolucao-mapa-cancelar"
        test-id-confirmar="btn-devolucao-mapa-confirmar"
        titulo="Devolver mapa"
        variant="danger"
        @confirmar="confirmarDevolucao"
        @shown="() => observacaoDevolucaoRef?.$el?.focus()"
    >
      <p>Confirma a devolução da validação do mapa para ajustes?</p>
      <BFormGroup
          label="Observação:"
          label-for="observacaoDevolucao"
          class="mb-3"
      >
        <BFormTextarea
            id="observacaoDevolucao"
            ref="observacaoDevolucaoRef"
            v-model="observacaoDevolucao"
            data-testid="inp-devolucao-mapa-obs"
            placeholder="Digite observações sobre a devolução..."
            rows="3"
        />
      </BFormGroup>
    </ModalConfirmacao>

    <HistoricoAnaliseModal
        :historico="historicoAnalise"
        :mostrar="mostrarModalHistorico"
        @fechar="fecharModalHistorico"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BBadge, BButton, BCard, BFormGroup, BFormTextarea} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import EmptyState from "@/components/comum/EmptyState.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import AceitarMapaModal from "@/components/mapa/AceitarMapaModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import {useProcessos} from "@/composables/useProcessos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import {usePerfil} from "@/composables/usePerfil";
import {useAcesso} from "@/composables/useAcesso";
import logger from "@/utils/logger";
import {listarAnalisesCadastro} from "@/services/analiseService";
import {obterMapaVisualizacao} from "@/services/mapaService";
import {buscarUnidadePorSigla as buscarUnidadeServico} from "@/services/unidadeService";
import type {Analise, MapaVisualizacao, Unidade} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

const route = useRoute();
const router = useRouter();
const processos = useProcessos();
const subprocessosStore = useSubprocessosStore();
const {notify} = useNotification();
const toastStore = useToastStore();
const {perfilSelecionado, isAdmin} = usePerfil();
const mapa = ref<MapaVisualizacao | null>(null);
const analisesCadastro = ref<Analise[]>([]);

const sigla = computed(() => route.params.siglaUnidade as string);
const codProcesso = computed(() => Number(route.params.codProcesso));

const unidade = ref<Unidade | null>(null);

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
  if (!processos.processoDetalhe.value) return null;
  return buscarUnidadeRecursivo(processos.processoDetalhe.value.unidades, sigla.value);
});

const codSubprocesso = computed(() => subprocesso.value?.codSubprocesso);

const {
  podeValidarMapa,
  podeAceitarMapa,
  podeDevolverMapa,
  podeHomologarMapa,
  podeVerSugestoes: podeMostrarVerSugestoes,
  habilitarAceitarMapa,
  habilitarDevolverMapa,
  habilitarHomologarMapa,
  habilitarValidarMapa
} = useAcesso(computed(() => subprocessosStore.subprocessoDetalhe));

const podeValidar = computed(() => podeValidarMapa.value);
const habilitarValidar = computed(() => habilitarValidarMapa.value);
const podeAnalisar = computed(() => {
  return (
      podeAceitarMapa.value ||
      podeDevolverMapa.value ||
      podeHomologarMapa.value
  );
});
const habilitarAnalisarMapa = computed(() => {
  return (
      habilitarAceitarMapa.value ||
      habilitarDevolverMapa.value ||
      habilitarHomologarMapa.value
  );
});
const podeVerSugestoes = computed(() => podeMostrarVerSugestoes.value);

const historicoAnalise = computed(() => {
  return analisesCadastro.value || [];
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
  if (!codSubprocesso.value || !sugestoes.value.trim()) return;
  isLoading.value = true;
  try {
    await processos.apresentarSugestoes(codSubprocesso.value, {
      sugestoes: sugestoes.value,
    });
    fecharModalSugestoes();
    toastStore.setPending(TEXTOS.sucesso.MAPA_SUBMETIDO_COM_SUGESTOES);
    await router.push({name: "Painel"});
  } catch {
    notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
  } finally {
    isLoading.value = false;
  }
}

async function confirmarValidacao() {
  if (!codSubprocesso.value) return;
  isLoading.value = true;
  try {
    await processos.validarMapa(codSubprocesso.value);
    fecharModalValidar();
    toastStore.setPending(TEXTOS.sucesso.MAPA_VALIDADO_SUBMETIDO);
    await router.push({name: "Painel"});
  } catch {
    notify(TEXTOS.mapa.ERRO_VALIDAR, 'danger');
  } finally {
    isLoading.value = false;
  }
}

async function confirmarAceitacao(observacao = "") {
  if (!codSubprocesso.value) return;
  isLoading.value = true;
  const isHomologacao = podeHomologarMapa.value || isAdmin.value;

  try {
    if (isHomologacao) {
      await processos.homologarValidacao(codSubprocesso.value, {texto: observacao});
    } else {
      await processos.aceitarValidacao(codSubprocesso.value, {texto: observacao});
    }
    fecharModalAceitar();
    toastStore.setPending(
        isHomologacao
            ? TEXTOS.mapa.SUCESSO_HOMOLOGACAO
            : TEXTOS.sucesso.ACEITE_REGISTRADO,
    );
    await router.push({name: "Painel"});
  } catch (error) {
    logger.error(error);
    notify(TEXTOS.comum.ERRO_OPERACAO, 'danger');
  } finally {
    isLoading.value = false;
  }
}

async function confirmarDevolucao() {
  if (!codSubprocesso.value) return;
  isLoading.value = true;
  try {
    await processos.devolverValidacao(codSubprocesso.value, {
      justificativa: observacaoDevolucao.value,
    });
    fecharModalDevolucao();
    toastStore.setPending(TEXTOS.sucesso.DEVOLUCAO_REALIZADA);
    await router.push({name: "Painel"});
  } catch (error) {
    logger.error(error);
    notify(TEXTOS.mapa.ERRO_DEVOLVER, 'danger');
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
  sugestoes.value = mapa.value?.sugestoes ?? "";
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
    analisesCadastro.value = await listarAnalisesCadastro(codSubprocesso.value);
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
  const response = await buscarUnidadeServico(sigla.value);
  unidade.value = response as Unidade;
  await processos.buscarProcessoDetalhe(codProcesso.value);
  if (codSubprocesso.value) {
    await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value);
    mapa.value = await obterMapaVisualizacao(codSubprocesso.value);
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
