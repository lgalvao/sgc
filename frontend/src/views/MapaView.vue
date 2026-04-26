<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial" />
    <template v-else>
      <PageHeader :title="TEXTOS.mapa.TITULO_TECNICO">
        <template #default>
          <div class="fs-5" data-testid="subprocesso-header__txt-header-unidade">
            {{ unidade?.sigla }}
          </div>
        </template>

        <template #actions>
          <div class="d-flex gap-2">
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
                data-testid="btn-mapa-historico"
                variant="outline-secondary"
                @click="verHistorico"
            >
              {{ TEXTOS.mapa.BOTAO_HISTORICO_ANALISE }}
            </BButton>

            <BButton
                v-if="podeAnalisar"
                data-testid="btn-mapa-devolver"
                :disabled="!habilitarDevolverMapa"
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
                v-if="acaoPrincipalMapa?.mostrar"
                data-testid="btn-mapa-homologar-aceite"
                :disabled="!acaoPrincipalMapa.habilitar"
                variant="success"
                @click="abrirModalAceitar"
            >
              {{ acaoPrincipalMapa.rotuloBotao }}
            </BButton>
          </div>

          <div v-if="podeVisualizarImpacto || podeEditarMapa || podeDisponibilizarMapa" class="d-flex gap-2 ms-3 ps-3 border-start">
            <LoadingButton
                v-if="podeVisualizarImpacto"
                :loading="loadingImpacto"
                data-testid="cad-mapa__btn-impactos-mapa"
                icon="arrow-right-circle"
                :text="TEXTOS.mapa.BOTAO_IMPACTO"
                variant="outline-secondary"
                @click="abrirModalImpacto"
            />
            <BButton
                v-if="podeEditarMapa"
                :disabled="!habilitarEditarMapa"
                data-testid="btn-abrir-criar-competencia"
                variant="outline-primary"
                @click="abrirModalCriarLimpo"
            >
              <i aria-hidden="true" class="bi bi-plus-lg me-1"/> {{ TEXTOS.mapa.BOTAO_CRIAR }}
            </BButton>
            <BButton
                v-if="podeDisponibilizarMapa"
                :disabled="loadingDisponibilizacao"
                data-testid="btn-cad-mapa-disponibilizar"
                variant="success"
                @click="abrirModalDisponibilizar"
            >
              {{ TEXTOS.mapa.BOTAO_DISPONIBILIZAR }}
            </BButton>
          </div>
        </template>
      </PageHeader>

      <BAlert
          v-if="erroMapaExibido"
          :key="erroMapaExibido"
          :model-value="true"
          no-fade
          show
          variant="danger"
          dismissible
          @dismissed="limparErroMapa"
      >
        {{ erroMapaExibido }}
      </BAlert>

      <div v-if="unidade">
        <div v-if="modoSomenteLeitura" class="mb-4 mt-3">
          <MapaSomenteLeitura :mapa="mapaSomenteLeitura" />
        </div>

        <template v-else>
          <div v-if="competencias.length === 0" class="mb-4 mt-3">
            <EmptyState
                :title="TEXTOS.mapa.EMPTY_TITLE"
                :description="TEXTOS.mapa.EMPTY_DESCRIPTION"
                class="mb-0"
                icon="bi-journal-plus"
            />
          </div>

          <div v-else class="mb-4 mt-3">
            <CompetenciaCard
                v-for="comp in competencias"
                :key="comp.codigo"
                :atividades="atividades"
                :competencia="comp"
                :pode-editar="podeEditarMapa"
                @editar="iniciarEdicaoCompetencia"
                @excluir="excluirCompetencia"
                @remover-atividade="(competenciaId, codAtividade) => removerAtividadeAssociada(competenciaId, codAtividade)"
            />
          </div>
        </template>
      </div>

      <div v-else>
        <p>{{ TEXTOS.mapa.UNIDADE_NAO_ENCONTRADA }}</p>
      </div>

      <template v-if="!modoSomenteLeitura">
        <CriarCompetenciaModal
            :atividades="atividades"
            :competencia-para-editar="competenciaSendoEditada"
            :field-errors="fieldErrors"
            :loading="loadingCompetencia"
            :mostrar="mostrarModalCriarNovaCompetencia"
            @fechar="fecharModalCriarNovaCompetencia"
            @salvar="adicionarCompetenciaEFecharModal"
        />

        <DisponibilizarMapaModal
            :field-errors="fieldErrors"
            :loading="loadingDisponibilizacao"
            :mostrar="mostrarModalDisponibilizar"
            :notificacao="notificacaoDisponibilizacao"
            :ultima-data-limite-subprocesso="subprocesso?.ultimaDataLimiteSubprocesso"
            @disponibilizar="disponibilizarMapa"
            @fechar="fecharModalDisponibilizar"
        />

        <ModalConfirmacao
            v-model="mostrarModalExcluirCompetencia"
            :loading="loadingExclusao"
            :mensagem="TEXTOS.mapa.EXCLUSAO_CONFIRMACAO(competenciaParaExcluir?.descricao || '')"
            data-testid="mdl-excluir-competencia"
            test-codigo-confirmar="btn-confirmar-exclusao-competencia"
            :titulo="TEXTOS.mapa.EXCLUSAO_TITULO"
            variant="danger"
            @confirmar="confirmarExclusaoCompetencia"
        />
      </template>

      <AceitarMapaModal
          :loading="isLoading"
          :homologacao="acaoPrincipalMapa?.codigo === 'HOMOLOGAR'"
          :mostrar-modal="mostrarModalAceitar"
          @fechar-modal="fecharModalAceitar"
          @confirmar-aceitacao="confirmarAceitacao"
      />

      <ModalPadrao
          v-model="mostrarModalVerSugestoes"
          :mostrar-botao-acao="false"
          test-codigo-cancelar="btn-ver-sugestoes-mapa-fechar"
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
          v-model="mostrarModalSugestoes"
          :auto-close="false"
          :loading="isLoading"
          :ok-title="TEXTOS.comum.BOTAO_APRESENTAR"
          test-codigo-cancelar="btn-sugestoes-mapa-cancelar"
          test-codigo-confirmar="btn-sugestoes-mapa-confirmar"
          titulo="Apresentar sugestões"
          variant="success"
          @confirmar="handleConfirmarSugestoes"
          @shown="() => sugestoesTextareaRef?.$el?.focus()"
      >
        <BFormGroup
            label-for="sugestoesTextarea"
            :state="mensagemErroSugestoes ? false : null"
            class="mb-3"
        >
          <template #label>
            Sugestões para o mapa de competências: <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <BFormTextarea
              id="sugestoesTextarea"
              ref="sugestoesTextareaRef"
              v-model="sugestoes"
              aria-required="true"
              :state="mensagemErroSugestoes ? false : null"
              data-testid="inp-sugestoes-mapa-texto"
              rows="5"
          />
          <BFormInvalidFeedback :state="mensagemErroSugestoes ? false : null">
            {{ mensagemErroSugestoes }}
          </BFormInvalidFeedback>
        </BFormGroup>
      </ModalConfirmacao>

      <ModalConfirmacao
          v-model="mostrarModalValidar"
          :loading="isLoading"
          :ok-title="TEXTOS.comum.BOTAO_VALIDAR"
          test-codigo-cancelar="btn-validar-mapa-cancelar"
          test-codigo-confirmar="btn-validar-mapa-confirmar"
          titulo="Validação de mapa"
          variant="success"
          @confirmar="confirmarValidacao"
      >
        <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
      </ModalConfirmacao>

      <ModalConfirmacao
          v-model="mostrarModalDevolucao"
          :auto-close="false"
          :loading="isLoading"
          :ok-title="TEXTOS.mapa.BOTAO_DEVOLVER"
          test-codigo-cancelar="btn-devolucao-mapa-cancelar"
          test-codigo-confirmar="btn-devolucao-mapa-confirmar"
          titulo="Devolver mapa"
          variant="danger"
          @confirmar="handleConfirmarDevolucao"
          @shown="() => observacaoDevolucaoRef?.$el?.focus()"
      >
        <p>Confirma a devolução da validação do mapa para ajustes?</p>
        <BFormGroup
            label-for="observacaoDevolucao"
            :state="mensagemErroDevolucao ? false : null"
            class="mb-3"
        >
          <template #label>
            Observação: <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <BFormTextarea
              id="observacaoDevolucao"
              ref="observacaoDevolucaoRef"
              v-model="observacaoDevolucao"
              :state="mensagemErroDevolucao ? false : null"
              data-testid="inp-devolucao-mapa-obs"
              placeholder="Digite observações sobre a devolução..."
              rows="3"
          />
          <BFormInvalidFeedback :state="mensagemErroDevolucao ? false : null">
            {{ mensagemErroDevolucao }}
          </BFormInvalidFeedback>
        </BFormGroup>
      </ModalConfirmacao>

      <ImpactoMapaModal
          v-if="codSubprocesso"
          :impacto="impactos"
          :loading="loadingImpacto"
          :mostrar="mostrarModalImpacto"
          @fechar="fecharModalImpacto"
      />

      <HistoricoAnaliseModal
          :historico="historicoAnalise"
          :mostrar="mostrarModalHistorico"
          @fechar="fecharModalHistorico"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormGroup, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CompetenciaCard from "@/components/mapa/CompetenciaCard.vue";
import MapaSomenteLeitura from "@/components/mapa/MapaSomenteLeitura.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import AceitarMapaModal from "@/components/mapa/AceitarMapaModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import {computed, defineAsyncComponent, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {usePerfil} from "@/composables/usePerfil";
import {useAcesso} from "@/composables/useAcesso";
import {useFluxoMapa} from "@/composables/useFluxoMapa";
import {useFormErrors} from '@/composables/useFormErrors';
import {useImpactoMapaModal} from "@/composables/useImpactoMapaModal";
import {useMapaAcoesAnalise} from "@/composables/useMapaAcoesAnalise";
import {useMapas} from "@/composables/useMapas";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {diagnosticarCarregamentoContextoSubprocessoInicial} from "@/composables/useContextoSubprocesso";
import {obterMapaVisualizacao, obterSugestoesMapa} from "@/services/subprocessoService";
import {listarAnalisesCadastro} from "@/services/analiseService";
import {apresentarSugestoes as apresentarSugestoesService} from "@/services/processoService";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import logger from "@/utils/logger";
import type {
  Analise,
  Atividade,
  Competencia,
  MapaCompleto,
  MapaVisualizacao,
  SalvarCompetenciaRequest,
  Unidade
} from "@/types/tipos";
import type {NormalizedError} from "@/utils/apiError";
import {normalizeError} from "@/utils/apiError";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

// Lazy loading de componentes pesados ou modais
const ImpactoMapaModal = defineAsyncComponent(() => import("@/components/mapa/ImpactoMapaModal.vue"));
const CriarCompetenciaModal = defineAsyncComponent(() => import("@/components/mapa/CriarCompetenciaModal.vue"));
const DisponibilizarMapaModal = defineAsyncComponent(() => import("@/components/mapa/DisponibilizarMapaModal.vue"));

const props = defineProps<{ codProcesso: number; sigla: string; codSubprocesso?: number }>();
const router = useRouter();
const mapasStore = useMapas();
const fluxoMapa = useFluxoMapa();
const {mapaCompleto, impactoMapa: impactos, erro: erroMapa} = mapasStore;
const {notify} = useNotification();
const toastStore = useToastStore();
const subprocessoStore = useSubprocessoStore();
const {invalidarCachesSubprocesso, limparEstadoSubprocessoAtual} = useInvalidacaoNavegacao();
const subprocesso = computed(() => subprocessoStore.contextoEdicao?.detalhes ?? null);
usePerfil();

const codProcesso = computed(() => Number(props.codProcesso));
const siglaUnidade = computed(() => String(props.sigla));

const {
  podeVisualizarImpacto,
  podeValidarMapa,
  podeEditarMapa,
  podeDisponibilizarMapa,
  habilitarEditarMapa,
  habilitarValidarMapa,
  podeAnalisarMapa,
  podeVerSugestoes: podeMostrarVerSugestoes,
  habilitarDevolverMapa,
  acaoPrincipalMapa
} = useAcesso(subprocesso);

const podeAnalisar = computed(() => {
  return (
      Boolean(acaoPrincipalMapa.value?.mostrar) ||
      podeAnalisarMapa.value
  );
});
const podeVerSugestoes = computed(() => podeMostrarVerSugestoes.value);
const podeValidar = computed(() => podeValidarMapa?.value ?? false);
const habilitarValidar = computed(() => habilitarValidarMapa?.value ?? false);
const modoSomenteLeitura = computed(() => !podeEditarMapa.value);

const unidade = ref<Unidade | null>(null);
const codSubprocesso = ref<number | null>(null);
const carregandoInicial = ref(true);
const mapaSomenteLeitura = ref<MapaVisualizacao | null>(null);

const analisesCadastro = ref<Analise[]>([]);
const historicoAnalise = computed(() => analisesCadastro.value || []);
const temHistoricoAnalise = computed(() => historicoAnalise.value.length > 0);
const mostrarModalSugestoes = ref(false);
const mostrarModalVerSugestoes = ref(false);
const mostrarModalHistorico = ref(false);
const sugestoes = ref("");
const sugestoesVisualizacao = ref("");

const sugestoesTextareaRef = ref<InstanceType<typeof BFormTextarea> | null>(null);
const observacaoDevolucaoRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const mensagemErroDevolucao = computed(() => {
  return deveExibirErro(!observacaoDevolucao.value.trim()) ? "A justificativa é obrigatória para a devolução." : "";
});
const mensagemErroSugestoes = computed(() => {
  return deveExibirErro(!sugestoes.value.trim()) ? "As sugestões são obrigatórias." : "";
});

async function concluirAcaoPainel(mensagem: string, fecharModal: () => void) {
  fecharModal();
  toastStore.setPending(mensagem);
  invalidarCachesSubprocesso({incluirPainel: true});
  await router.push({name: "Painel"});
}

const {
  mostrarModalAceitar,
  mostrarModalValidar,
  mostrarModalDevolucao,
  observacaoDevolucao,
  isLoading,
  confirmarAceitacao,
  confirmarValidacao,
  confirmarDevolucao,
  abrirModalAceitar,
  fecharModalAceitar,
  abrirModalValidar,
  abrirModalDevolucao: abrirModalDevolucaoBase,
} = useMapaAcoesAnalise({
  codSubprocesso,
  acaoPrincipalMapa,
  concluirAcaoPainel,
  notify,
});

function abrirModalDevolucao() {
  resetarValidacao();
  abrirModalDevolucaoBase();
}

async function handleConfirmarDevolucao() {
  if (!validarSubmissao(!!observacaoDevolucao.value.trim())) {
    await focarPrimeiroErroInvalido();
    return;
  }
  await confirmarDevolucao();
}

async function sincronizarSugestoesMapa(): Promise<string> {
  if (!codSubprocesso.value) {
    return "";
  }
  return await obterSugestoesMapa(codSubprocesso.value);
}

async function carregarSugestoesParaVisualizacao() {
  try {
    sugestoesVisualizacao.value = await sincronizarSugestoesMapa();
  } catch (error) {
    logger.error(error);
    notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
  }
}

function verSugestoes() {
  mostrarModalVerSugestoes.value = true;
  sugestoesVisualizacao.value = "";
  void carregarSugestoesParaVisualizacao();
}

function fecharModalVerSugestoes() {
  mostrarModalVerSugestoes.value = false;
  sugestoesVisualizacao.value = "";
}

async function carregarSugestoesParaEdicao() {
  try {
    sugestoes.value = await sincronizarSugestoesMapa();
  } catch (error) {
    logger.error(error);
    notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
  }
}

function abrirModalSugestoes() {
  resetarValidacao();
  mostrarModalSugestoes.value = true;
  void carregarSugestoesParaEdicao();
}

function fecharModalSugestoes() {
  mostrarModalSugestoes.value = false;
  sugestoes.value = "";
  resetarValidacao();
}

async function handleConfirmarSugestoes() {
  if (!validarSubmissao(!!sugestoes.value.trim())) {
    await focarPrimeiroErroInvalido();
    return;
  }

  if (!codSubprocesso.value) return;

  try {
    isLoading.value = true;
    await apresentarSugestoesService(codSubprocesso.value, {sugestoes: sugestoes.value});
    await concluirAcaoPainel(TEXTOS.sucesso.MAPA_SUBMETIDO_COM_SUGESTOES, fecharModalSugestoes);
  } catch (error) {
    logger.error(error);
    notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
  } finally {
    isLoading.value = false;
  }
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
  void abrirModalHistorico();
}

const {
  mostrarModalImpacto,
  loadingImpacto,
  abrirModalImpacto,
  fecharModalImpacto,
} = useImpactoMapaModal(codSubprocesso, (codigo) => mapasStore.buscarImpactoMapa(codigo));

async function carregarContextoEdicao(codigo: number) {
  const data = await subprocessoStore.garantirContextoEdicao(codigo);
  if (!data) {
    return null;
  }

  atividades.value = data.mapa.atividades;
  unidade.value = data.unidade;

  return data;
}

async function carregarContextoInicial() {
  if (typeof props.codSubprocesso === "number") {
    const contexto = await subprocessoStore.garantirContextoEdicao(props.codSubprocesso, false);
    if (!contexto) {
      if (subprocessoStore.erroIntegracaoContexto) {
        notify(subprocessoStore.erroIntegracaoContexto.message, 'danger');
      } else {
        notify('Falha grave ao resolver subprocesso para o mapa. A ocorrência deve ser auditada.', 'danger');
      }
      return null;
    }

    codSubprocesso.value = contexto.detalhes.codigo;
    atividades.value = contexto.mapa.atividades;
    unidade.value = contexto.unidade;
    return contexto;
  }

  const diagnostico = await diagnosticarCarregamentoContextoSubprocessoInicial({
    codProcesso: codProcesso.value,
    siglaUnidade: siglaUnidade.value,
    store: subprocessoStore,
  });

  if (diagnostico.tipo === 'erroIntegracao') {
    notify(diagnostico.erro.message, 'danger');
    return null;
  }

  if (diagnostico.tipo === 'cancelado') {
    return null;
  }

  if (diagnostico.tipo === 'ausencia') {
    notify('Falha grave ao resolver subprocesso para o mapa. A ocorrência deve ser auditada.', 'danger');
    return null;
  }

  codSubprocesso.value = diagnostico.resultado.codigo;
  atividades.value = diagnostico.resultado.contexto.mapa.atividades;
  unidade.value = diagnostico.resultado.contexto.unidade;

  return diagnostico.resultado.contexto;
}

async function executarComSubprocesso(
    callback: (codigoSubprocesso: number) => Promise<void>
) {
  const codigoSubprocesso = codSubprocesso.value;
  if (!codigoSubprocesso) return;
  await callback(codigoSubprocesso);
}

function sincronizarMapa(mapaAtualizado: MapaCompleto | null | undefined) {
  if (mapaAtualizado) {
    mapasStore.mapaCompleto.value = mapaAtualizado;
    if (subprocessoStore.contextoEdicao?.detalhes.codigo === codSubprocesso.value) {
      subprocessoStore.contextoEdicao.mapa = mapaAtualizado;
    }
  }
}

async function carregarMapaSomenteLeitura(codigo: number) {
  mapaSomenteLeitura.value = await obterMapaVisualizacao(codigo);
  mapasStore.mapaCompleto.value = null;
  atividades.value = [];
}

async function carregarMapaInicial(codigo: number, contextoInicial?: Awaited<ReturnType<typeof carregarContextoEdicao>> | null) {
  if (modoSomenteLeitura.value) {
    await carregarMapaSomenteLeitura(codigo);
    return;
  }

  mapaSomenteLeitura.value = null;
  const data = contextoInicial ?? await carregarContextoEdicao(codigo);
  if (data?.mapa) {
    sincronizarMapa(data.mapa);
    return;
  }

  await mapasStore.buscarMapaCompleto(codigo);
}

onMounted(async () => {
  try {
    limparEstadoSubprocessoAtual();
    const contextoInicial = await carregarContextoInicial();
    if (!codSubprocesso.value) {
      return;
    }
    await carregarMapaInicial(codSubprocesso.value, contextoInicial);
  } catch (erro) {
    notify(normalizeError(erro).message, 'danger');
  } finally {
    carregandoInicial.value = false;
  }
});

const atividades = ref<Atividade[]>([]);

const competencias = computed(() => mapaCompleto.value?.competencias || []);
const codigosAtividadesAssociadas = computed(() => {
  return new Set(
      competencias.value.flatMap((competencia) =>
          (competencia.atividades || []).map((atividade) => atividade.codigo)
      )
  );
});
const atividadesSemCompetencia = computed(() => {
  if (atividades.value.length === 0) {
    return [];
  }

  return atividades.value.filter((atividade) => !codigosAtividadesAssociadas.value.has(atividade.codigo));
});

const existeCompetenciaSemAtividade = computed(() => {
  return competencias.value.some((competencia) => (competencia.atividades?.length ?? 0) === 0);
});

const associacoesMapaValidas = computed(() => {
  return !existeCompetenciaSemAtividade.value && atividadesSemCompetencia.value.length === 0;
});

const podeConfirmarDisponibilizacao = computed(() => {
  return (
      competencias.value.length > 0
      && associacoesMapaValidas.value
  );
});
const competenciaSendoEditada = ref<Competencia | null>(null);

const mostrarModalCriarNovaCompetencia = ref(false);
const mostrarModalDisponibilizar = ref(false);
const mostrarModalExcluirCompetencia = ref(false);
const competenciaParaExcluir = ref<Competencia | null>(null);
const notificacaoDisponibilizacao = ref("");
const erroValidacaoMapa = ref("");
const loadingCompetencia = ref(false);
const loadingDisponibilizacao = ref(false);
const loadingExclusao = ref(false);

const {errors: fieldErrors, setFromNormalizedError, clearErrors} = useFormErrors([
  'descricao',
  'atividades',
  'atividadesCodigos',
  'dataLimite',
  'observacoes',
  'generic'
]);
const erroMapaExibido = computed(() => erroValidacaoMapa.value || erroMapa.value);

function limparErroMapa() {
  erroValidacaoMapa.value = "";
  erroMapa.value = null;
}

function handleErrors(store: { lastError: unknown }) {
  setFromNormalizedError(store.lastError as NormalizedError | null);
  if (fieldErrors.value.atividadesCodigos) fieldErrors.value.atividades = fieldErrors.value.atividadesCodigos;
}

function abrirModalCriarNovaCompetencia(competenciaParaEditar: Competencia | null = null) {
  mostrarModalCriarNovaCompetencia.value = true;
  clearErrors();
  fluxoMapa.clearError();
  competenciaSendoEditada.value = competenciaParaEditar;
}

function abrirModalCriarLimpo() {
  abrirModalCriarNovaCompetencia();
}

function fecharModalCriarNovaCompetencia() {
  mostrarModalCriarNovaCompetencia.value = false;
  clearErrors();
}

function iniciarEdicaoCompetencia(competencia: Competencia) {
  abrirModalCriarNovaCompetencia(competencia);
}

function abrirModalDisponibilizar() {
  erroValidacaoMapa.value = "";
  if (!podeConfirmarDisponibilizacao.value) {
    if (competencias.value.length === 0) {
      erroValidacaoMapa.value = TEXTOS.mapa.ERRO_MAPA_SEM_COMPETENCIAS;
    } else if (existeCompetenciaSemAtividade.value) {
      erroValidacaoMapa.value = TEXTOS.mapa.ERRO_COMPETENCIA_SEM_ATIVIDADE;
    } else if (atividadesSemCompetencia.value.length > 0) {
      erroValidacaoMapa.value = TEXTOS.mapa.ERRO_ATIVIDADES_SEM_COMPETENCIA;
    }
    return;
  }

  mostrarModalDisponibilizar.value = true;
  clearErrors();
}

async function adicionarCompetenciaEFecharModal(dados: { descricao: string; atividadesSelecionadas: number[] }) {
  if (loadingCompetencia.value) return;

  await executarComSubprocesso(async (codigoSubprocesso) => {
    const request: SalvarCompetenciaRequest = {
      descricao: dados.descricao,
      atividadesCodigos: dados.atividadesSelecionadas,
    };

    loadingCompetencia.value = true;
    try {
      if (competenciaSendoEditada.value) {
        sincronizarMapa(await fluxoMapa.atualizarCompetencia(codigoSubprocesso, competenciaSendoEditada.value.codigo, request));
      } else {
        sincronizarMapa(await fluxoMapa.adicionarCompetencia(codigoSubprocesso, request));
      }

      fecharModalCriarNovaCompetencia();
    } catch {
      handleErrors(fluxoMapa);
    } finally {
      loadingCompetencia.value = false;
    }
  });
}

function excluirCompetencia(codigo: number) {
  const competencia = competencias.value.find((comp) => comp.codigo === codigo);
  if (competencia) {
    competenciaParaExcluir.value = competencia;
    mostrarModalExcluirCompetencia.value = true;
  }
}

async function confirmarExclusaoCompetencia() {
  const competencia = competenciaParaExcluir.value;
  if (!competencia) return;

  await executarComSubprocesso(async (codigoSubprocesso) => {
    loadingExclusao.value = true;
    try {
      sincronizarMapa(await fluxoMapa.removerCompetencia(codigoSubprocesso, competencia.codigo));
      fecharModalExcluirCompetencia();
    } catch {
      handleErrors(fluxoMapa);
    } finally {
      loadingExclusao.value = false;
    }
  });
}

function fecharModalExcluirCompetencia() {
  mostrarModalExcluirCompetencia.value = false;
  competenciaParaExcluir.value = null;
}

async function removerAtividadeAssociada(competenciaId: number, codAtividade: number) {
  const competencia = competencias.value.find((comp) => comp.codigo === competenciaId);
  if (!competencia) return;

  await executarComSubprocesso(async (codigoSubprocesso) => {
    const atividadesCodigos = (competencia.atividades || []).map((atividade) => atividade.codigo)
        .filter((codigoAtividade) => codigoAtividade !== codAtividade);
    const request: SalvarCompetenciaRequest = {
      descricao: competencia.descricao,
      atividadesCodigos,
    };

    sincronizarMapa(await fluxoMapa.atualizarCompetencia(
        codigoSubprocesso,
        competencia.codigo,
        request,
    ));
  });
}

async function disponibilizarMapa(payload: { dataLimite: string; observacoes: string }) {
  await executarComSubprocesso(async (codigoSubprocesso) => {
    fluxoMapa.clearError();
    loadingDisponibilizacao.value = true;

    try {
      await fluxoMapa.disponibilizarMapa(codigoSubprocesso, payload);
      fecharModalDisponibilizar();
      toastStore.setPending(TEXTOS.sucesso.MAPA_DISPONIBILIZADO);
      invalidarCachesSubprocesso({incluirPainel: true});
      await router.push({name: "Painel"});
    } catch {
      handleErrors(fluxoMapa);
    } finally {
      loadingDisponibilizacao.value = false;
    }
  });
}

function fecharModalDisponibilizar() {
  mostrarModalDisponibilizar.value = false;
  notificacaoDisponibilizacao.value = "";
  clearErrors();
}

defineExpose({
  ImpactoMapaModal,
  CriarCompetenciaModal,
  DisponibilizarMapaModal,
  podeVisualizarImpacto,
  podeEditarMapa,
  podeDisponibilizarMapa,
  podeConfirmarDisponibilizacao,
  existeCompetenciaSemAtividade,
  associacoesMapaValidas,
  unidade,
  competencias,
  mapaSomenteLeitura,
  modoSomenteLeitura,
  atividades,
  atividadesSemCompetencia,
  impactoMapa: impactos,
  mostrarModalImpacto,
  loadingImpacto,
  loadingCompetencia,
  abrirModalImpacto,
  abrirModalCriarLimpo,
  iniciarEdicaoCompetencia,
  excluirAtividade: confirmarExclusaoCompetencia,
  removerAtividadeAssociada,
  abrirModalDisponibilizar,
  disponibilizarMapa,
  fecharModalDisponibilizar,
  fecharModalImpacto,
  fecharModalCriarNovaCompetencia,
  adicionarCompetenciaEFecharModal,
  confirmarExclusaoCompetencia,
  fecharModalExcluirCompetencia,
  excluirCompetencia
});
</script>
