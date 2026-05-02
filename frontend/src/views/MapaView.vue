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
            <LoadingButton
                v-if="podeVerSugestoes"
                :loading="loadingSugestoesVisualizacao"
                data-testid="btn-mapa-ver-sugestoes"
                loading-text="Carregando..."
                text="Ver sugestões"
                variant="outline-secondary"
                @click="verSugestoes"
            >
              {{ TEXTOS.mapa.BOTAO_VER_SUGESTOES }}
            </LoadingButton>

            <BButton
                v-if="codigoSubprocesso"
                data-testid="btn-mapa-historico"
                variant="outline-secondary"
                @click="verHistorico"
            >
              {{ TEXTOS.mapa.BOTAO_HISTORICO_ANALISE }}
            </BButton>

            <LoadingButton
                v-if="podeVisualizarImpacto"
                :loading="loadingImpacto"
                data-testid="cad-mapa__btn-impactos-mapa"
                icon="arrow-right-circle"
                :text="TEXTOS.mapa.BOTAO_IMPACTO"
                variant="outline-secondary"
                @click="abrirModalImpacto"
            />

            <BDropdown
                v-if="usarMenuAcoesMapa"
                data-testid="btn-mapa-acoes"
                :text="TEXTOS.mapa.BOTAO_ACOES"
                toggle-class="text-nowrap"
                variant="success"
            >
              <BDropdownItemButton
                  v-if="mostrarApresentarSugestoes"
                  data-testid="btn-mapa-acao-sugestoes"
                  :disabled="!habilitarApresentarSugestoes"
                  @click="abrirModalSugestoes"
              >
                {{ TEXTOS.mapa.BOTAO_SUGESTOES }}
              </BDropdownItemButton>
              <BDropdownItemButton
                  v-if="mostrarValidarMapa"
                  data-testid="btn-mapa-acao-validar"
                  :disabled="!habilitarValidar"
                  @click="abrirModalValidar"
              >
                {{ TEXTOS.mapa.BOTAO_VALIDAR }}
              </BDropdownItemButton>
              <BDropdownItemButton
                  v-if="mostrarDisponibilizarMapa"
                  data-testid="btn-mapa-acao-disponibilizar"
                  :disabled="!habilitarDisponibilizarMapa || loadingDisponibilizacao"
                  @click="abrirModalDisponibilizar"
              >
                {{ TEXTOS.mapa.BOTAO_DISPONIBILIZAR }}
              </BDropdownItemButton>
              <BDropdownItemButton
                  v-if="mostrarAcaoDevolverMapa"
                  data-testid="btn-mapa-acao-devolver"
                  :disabled="!habilitarDevolverMapa"
                  @click="abrirModalDevolucao"
              >
                {{ TEXTOS.mapa.BOTAO_DEVOLVER }}
              </BDropdownItemButton>
              <BDropdownItemButton
                  v-if="mostrarAcaoPrincipalMapa"
                  data-testid="btn-mapa-acao-homologar-aceite"
                  :disabled="!habilitarAcaoPrincipalMapa"
                  @click="abrirModalAceitar"
              >
                {{ rotuloAcaoPrincipalMapa }}
              </BDropdownItemButton>
            </BDropdown>
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
          <div class="mb-3 mt-3">
            <BButton
                :disabled="!habilitarEditarMapa"
                data-testid="btn-abrir-criar-competencia"
                variant="outline-primary"
                @click="abrirModalCriarLimpo"
            >
              <i aria-hidden="true" class="bi bi-plus-lg me-1"/> {{ TEXTOS.mapa.BOTAO_CRIAR }}
            </BButton>
          </div>

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
                @excluir="(codigo) => excluirCompetencia(codigo)"
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
          :loading="carregandoFluxoMapa"
          :homologacao="acaoPrincipalMapa?.codigo === 'HOMOLOGAR'"
          :mostrar-modal="mostrarModalAceitar"
          @fechar-modal="fecharModalAceitar"
          @confirmar-aceitacao="(observacao) => confirmarAceitacao(observacao)"
      />

      <ModalPadrao
          v-model="mostrarModalVerSugestoes"
          :mostrar-botao-acao="false"
          test-codigo-cancelar="btn-ver-sugestoes-mapa-fechar"
          texto-cancelar="Fechar"
          titulo="Sugestões sobre o mapa"
          @fechar="fecharModalVerSugestoes"
      >
        <BFormGroup class="mb-3">
          <template #label>
            Sugestões registradas para o mapa de competências:
          </template>

          <div
              v-if="!isChefe"
              data-testid="txt-ver-sugestoes-mapa-texto"
              class="border rounded p-3 bg-body-tertiary white-space-pre-line"
          >
            {{ sugestoesVisualizacao }}
          </div>

          <BFormTextarea
              v-else
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
          :loading="loadingSugestoesEnvio"
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
          :loading="carregandoFluxoMapa"
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
          :loading="carregandoFluxoMapa"
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
          v-if="codigoSubprocesso"
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
import {
  BAlert,
  BButton,
  BDropdown,
  BDropdownItemButton,
  BFormGroup,
  BFormInvalidFeedback,
  BFormTextarea
} from "bootstrap-vue-next";
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
import {useMapaCompetenciasMutacoes} from "@/composables/useMapaCompetenciasMutacoes";
import {useImpactoMapaModal} from "@/composables/useImpactoMapaModal";
import {useMapas} from "@/composables/useMapas";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {apresentarSugestoes, obterSugestoesMapa} from "@/services/subprocessoService";
import {listarAnalisesCadastro} from "@/services/analiseService";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useMapaOrquestracao} from "@/composables/useMapaOrquestracao";
import logger from "@/utils/logger";
import {normalizeError} from "@/utils/apiError";
import {Perfil} from "@/types/tipos";
import type {
  Analise,
  MapaCompleto,
  MapaVisualizacao,
} from "@/types/tipos";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

// Lazy loading de componentes pesados ou modais
const ImpactoMapaModal = defineAsyncComponent(() => import("@/components/mapa/ImpactoMapaModal.vue"));
const CriarCompetenciaModal = defineAsyncComponent(() => import("@/components/mapa/CriarCompetenciaModal.vue"));
const DisponibilizarMapaModal = defineAsyncComponent(() => import("@/components/mapa/DisponibilizarMapaModal.vue"));

const props = defineProps<{ codProcesso: number | string; sigla: string; codSubprocesso?: number }>();
const router = useRouter();
const mapasStore = useMapas();
const fluxoMapa = useFluxoMapa();
const carregandoFluxoMapa = fluxoMapa.carregando;
const {impactoMapa: impactos, erro: erroMapa} = mapasStore;
const {notify} = useNotification();
const toastStore = useToastStore();
const subprocessoStore = useSubprocessoStore();
const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();
const subprocesso = computed(() => subprocessoStore.contextoEdicao?.detalhes ?? null);
const {perfilSelecionado} = usePerfil();

const {
  podeVisualizarImpacto,
  podeEditarMapa,
  mostrarValidarMapa,
  mostrarApresentarSugestoes,
  mostrarDisponibilizarMapa,
  mostrarDevolverMapa,
  habilitarApresentarSugestoes,
  habilitarDisponibilizarMapa,
  habilitarEditarMapa,
  habilitarValidarMapa,
  podeVerSugestoes: podeMostrarVerSugestoes,
  habilitarDevolverMapa,
  acaoPrincipalMapa
} = useAcesso(subprocesso);

const usarMenuAcoesMapa = computed(() => {
  return mostrarApresentarSugestoes.value
      || mostrarValidarMapa.value
      || mostrarAcaoDevolverMapa.value
      || Boolean(acaoPrincipalMapa.value?.mostrar)
      || mostrarDisponibilizarMapa.value;
});
const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);
const podeVerSugestoes = computed(() => podeMostrarVerSugestoes.value);
const habilitarValidar = computed(() => habilitarValidarMapa?.value ?? false);
const modoSomenteLeitura = computed(() => !podeEditarMapa.value);
const mostrarAcaoDevolverMapa = computed(() => mostrarDevolverMapa.value);
const mostrarAcaoPrincipalMapa = computed(() => Boolean(acaoPrincipalMapa.value?.mostrar));
const habilitarAcaoPrincipalMapa = computed(() => acaoPrincipalMapa.value?.habilitar ?? false);
const rotuloAcaoPrincipalMapa = computed(() => acaoPrincipalMapa.value?.rotuloBotao ?? TEXTOS.mapa.LABEL_HOMOLOGAR);

const atividades = computed(() => mapasStore.mapaCompleto.value?.atividades ?? []);
const competencias = computed(() => mapasStore.mapaCompleto.value?.competencias ?? []);
const mapaSomenteLeitura = ref<MapaVisualizacao | null>(null);
const mostrarModalAceitar = ref(false);
const mostrarModalValidar = ref(false);
const mostrarModalDevolucao = ref(false);
const observacaoDevolucao = ref("");
const loadingSugestoesEnvio = ref(false);

const {
  carregandoInicial,
  codigoSubprocesso,
  unidade,
  carregarContextoInicial,
} = useMapaOrquestracao(props, mapaSomenteLeitura);

const analisesCadastro = ref<Analise[]>([]);
const historicoAnalise = computed(() => analisesCadastro.value || []);
const mostrarModalSugestoes = ref(false);
const mostrarModalVerSugestoes = ref(false);
const mostrarModalHistorico = ref(false);
const sugestoes = ref("");
const sugestoesVisualizacao = ref("");
const loadingSugestoesVisualizacao = ref(false);

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

function abrirModalAceitar() {
  mostrarModalAceitar.value = true;
}

function fecharModalAceitar() {
  mostrarModalAceitar.value = false;
}

function abrirModalValidar() {
  mostrarModalValidar.value = true;
}

function fecharModalValidar() {
  mostrarModalValidar.value = false;
}

function abrirModalDevolucao() {
  resetarValidacao();
  mostrarModalDevolucao.value = true;
}

function fecharModalDevolucao() {
  mostrarModalDevolucao.value = false;
  observacaoDevolucao.value = "";
}

async function confirmarValidacao() {
  if (!codigoSubprocesso.value) return;

  try {
    await fluxoMapa.validarMapa(codigoSubprocesso.value);
    await concluirAcaoPainel(TEXTOS.sucesso.MAPA_VALIDADO_SUBMETIDO, fecharModalValidar);
  } catch {
    notify(TEXTOS.mapa.ERRO_VALIDAR, 'danger');
  }
}

async function confirmarAceitacao(observacao = "") {
  if (!codigoSubprocesso.value) return;

  const acao = acaoPrincipalMapa.value;
  if (!acao) return;

  try {
    if (acao.codigo === "HOMOLOGAR") {
      await fluxoMapa.homologarMapa(codigoSubprocesso.value, {observacao});
    } else {
      await fluxoMapa.aceitarMapa(codigoSubprocesso.value, {observacao});
    }
    await concluirAcaoPainel(acao.mensagemSucesso, fecharModalAceitar);
  } catch {
    notify(TEXTOS.comum.ERRO_OPERACAO, 'danger');
  }
}

async function handleConfirmarDevolucao() {
  if (!validarSubmissao(!!observacaoDevolucao.value.trim())) {
    await focarPrimeiroErroInvalido();
    return;
  }

  if (!codigoSubprocesso.value) return;

  try {
    await fluxoMapa.devolverMapa(codigoSubprocesso.value, {
      justificativa: observacaoDevolucao.value,
    });
    await concluirAcaoPainel(TEXTOS.sucesso.DEVOLUCAO_REALIZADA, fecharModalDevolucao);
  } catch {
    notify(TEXTOS.mapa.ERRO_DEVOLVER, 'danger');
  }
}

async function sincronizarSugestoesMapa(): Promise<string> {
  if (!codigoSubprocesso.value) {
    return "";
  }
  return await obterSugestoesMapa(codigoSubprocesso.value);
}

async function carregarSugestoesParaVisualizacao() {
  try {
    sugestoesVisualizacao.value = await sincronizarSugestoesMapa();
    return true;
  } catch (error) {
    logger.error(error);
    notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
    return false;
  }
}

async function verSugestoes() {
  if (loadingSugestoesVisualizacao.value) {
    return;
  }

  sugestoesVisualizacao.value = "";
  loadingSugestoesVisualizacao.value = true;

  try {
    const carregou = await carregarSugestoesParaVisualizacao();
    if (carregou) {
      mostrarModalVerSugestoes.value = true;
    }
  } finally {
    loadingSugestoesVisualizacao.value = false;
  }
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

  if (!codigoSubprocesso.value) return;

  try {
    loadingSugestoesEnvio.value = true;
    await apresentarSugestoes(codigoSubprocesso.value, {sugestoes: sugestoes.value});
    await concluirAcaoPainel(TEXTOS.sucesso.MAPA_SUBMETIDO_COM_SUGESTOES, fecharModalSugestoes);
  } catch (error) {
    logger.error(error);
    notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
  } finally {
    loadingSugestoesEnvio.value = false;
  }
}

async function abrirModalHistorico() {
  if (codigoSubprocesso.value) {
    analisesCadastro.value = await listarAnalisesCadastro(codigoSubprocesso.value);
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
} = useImpactoMapaModal(codigoSubprocesso, (codigo) => mapasStore.buscarImpactoMapa(codigo));

async function executarComSubprocesso(
    callback: (id: number) => Promise<void>
) {
  const codSubp = codigoSubprocesso.value;
  if (!codSubp) return;
  await callback(codSubp);
}

function sincronizarMapa(mapaAtualizado: MapaCompleto | null | undefined) {
  if (mapaAtualizado) {
    mapasStore.mapaCompleto.value = mapaAtualizado;
    if (subprocessoStore.contextoEdicao?.detalhes.codigo === codigoSubprocesso.value) {
      subprocessoStore.contextoEdicao.mapa = mapaAtualizado;
    }
  }
}

onMounted(async () => {
  const sucesso = await carregarContextoInicial(podeEditarMapa);
  if (!sucesso) {
    if (subprocessoStore.erroIntegracaoContexto) {
      notify(subprocessoStore.erroIntegracaoContexto.message, 'danger');
    } else {
      notify('Falha grave ao resolver subprocesso para o mapa. A ocorrência deve ser auditada.', 'danger');
    }
  }
});

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
const mostrarModalDisponibilizar = ref(false);
const notificacaoDisponibilizacao = ref("");
const erroValidacaoMapa = ref("");
const loadingDisponibilizacao = ref(false);

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

function sincronizarErrosAtividades() {
  if (fieldErrors.value.atividadesCodigos) {
    fieldErrors.value.atividades = fieldErrors.value.atividadesCodigos;
  }
}

function aplicarErroNormalizado(error: ReturnType<typeof normalizeError> | null) {
  setFromNormalizedError(error);
  sincronizarErrosAtividades();
}

function handleErrors(store: { lastError: unknown }) {
  aplicarErroNormalizado(store.lastError as ReturnType<typeof normalizeError> | null);
}

const {
  competenciaSendoEditada,
  mostrarModalCriarNovaCompetencia,
  mostrarModalExcluirCompetencia,
  competenciaParaExcluir,
  loadingCompetencia,
  loadingExclusao,
  abrirModalCriarLimpo,
  fecharModalCriarNovaCompetencia,
  iniciarEdicaoCompetencia,
  adicionarCompetenciaEFecharModal,
  excluirCompetencia,
  confirmarExclusaoCompetencia,
  removerAtividadeAssociada,
  fecharModalExcluirCompetencia,
} = useMapaCompetenciasMutacoes({
  codigoSubprocesso,
  competencias,
  fluxoMapa,
  notify,
  clearErrors,
  aplicarErroNormalizado,
  sincronizarMapa,
});

function obterMensagemErroChecklistDisponibilizacao() {
  if (competencias.value.length === 0) {
    return TEXTOS.mapa.ERRO_MAPA_SEM_COMPETENCIAS;
  }

  if (existeCompetenciaSemAtividade.value) {
    return TEXTOS.mapa.ERRO_COMPETENCIA_SEM_ATIVIDADE;
  }

  if (atividadesSemCompetencia.value.length > 0) {
    return TEXTOS.mapa.ERRO_ATIVIDADES_SEM_COMPETENCIA;
  }

  return "";
}

function abrirModalDisponibilizar() {
  erroValidacaoMapa.value = "";
  if (!podeConfirmarDisponibilizacao.value) {
    erroValidacaoMapa.value = obterMensagemErroChecklistDisponibilizacao();
    return;
  }

  mostrarModalDisponibilizar.value = true;
  clearErrors();
}

async function disponibilizarMapa(request: { dataLimite: string; observacoes: string }) {
  if (loadingDisponibilizacao.value) return;

  await executarComSubprocesso(async (codigoSubprocesso) => {
    loadingDisponibilizacao.value = true;
    try {
      await fluxoMapa.disponibilizarMapa(codigoSubprocesso, request);
      await concluirAcaoPainel(TEXTOS.sucesso.MAPA_DISPONIBILIZADO, fecharModalDisponibilizar);
    } catch (error) {
      logger.error(error);
      aplicarErroNormalizado(fluxoMapa.lastError.value as ReturnType<typeof normalizeError> | null);
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

// Expose para testes
defineExpose({
  codigoSubprocesso,
  mostrarModalImpacto,
  mostrarModalCriarNovaCompetencia,
  mostrarModalExcluirCompetencia,
  mostrarModalDisponibilizar,
  loadingImpacto,
  loadingCompetencia,
  loadingExclusao,
  loadingDisponibilizacao,
  notificacaoDisponibilizacao,
  atividades,
  competencias,
  competenciaSendoEditada,
  competenciaParaExcluir,
  handleErrors,
  fieldErrors,
  atividadesSemCompetencia,
  existeCompetenciaSemAtividade,
  associacoesMapaValidas,
  obterMensagemErroChecklistDisponibilizacao,
  unidade,
  abrirModalImpacto,
  fecharModalImpacto,
  abrirModalCriarLimpo,
  abrirModalAceitar,
  fecharModalAceitar,
  abrirModalValidar,
  fecharModalValidar,
  abrirModalDevolucao,
  fecharModalDevolucao,
  sincronizarSugestoesMapa,
  carregarSugestoesParaVisualizacao,
  carregarSugestoesParaEdicao,
  verSugestoes,
  fecharModalVerSugestoes,
  handleConfirmarSugestoes,
  fecharModalHistorico,
  sincronizarMapa,
  adicionarCompetenciaEFecharModal,
  excluirCompetencia,
  confirmarExclusaoCompetencia,
  disponibilizarMapa,
  removerAtividadeAssociada,
  fecharModalExcluirCompetencia,
  fecharModalDisponibilizar,
  abrirModalDisponibilizar,
  fecharModalCriarNovaCompetencia,
  iniciarEdicaoCompetencia,
});
</script>
