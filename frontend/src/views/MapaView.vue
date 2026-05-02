<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial" />
    <template v-else>
      <MapaAcoesHeader
          :codigo-subprocesso="codigoSubprocesso"
          :habilitar-acao-principal-mapa="habilitarAcaoPrincipalMapa"
          :habilitar-apresentar-sugestoes="habilitarApresentarSugestoes"
          :habilitar-devolver-mapa="habilitarDevolverMapa"
          :habilitar-disponibilizar-mapa="habilitarDisponibilizarMapa"
          :habilitar-validar-mapa="habilitarValidarMapa"
          :loading-disponibilizacao="loadingDisponibilizacao"
          :loading-impacto="loadingImpacto"
          :loading-sugestoes-visualizacao="loadingSugestoesVisualizacao"
          :mostrar-acao-principal-mapa="mostrarAcaoPrincipalMapa"
          :mostrar-apresentar-sugestoes="mostrarApresentarSugestoes"
          :mostrar-devolver-mapa="mostrarDevolverMapa"
          :mostrar-disponibilizar-mapa="mostrarDisponibilizarMapa"
          :mostrar-validar-mapa="mostrarValidarMapa"
          :pode-ver-sugestoes="podeVerSugestoes"
          :pode-visualizar-impacto="podeVisualizarImpacto"
          :rotulo-acao-principal-mapa="rotuloAcaoPrincipalMapa"
          :unidade="unidade"
          :usar-menu-acoes-mapa="usarMenuAcoesMapa"
          @abrir-acao-principal="abrirModalAceitar"
          @abrir-devolver="abrirModalDevolucao"
          @abrir-disponibilizar="abrirModalDisponibilizar"
          @abrir-historico="verHistorico"
          @abrir-impacto="abrirModalImpacto"
          @abrir-sugestoes="abrirModalSugestoes"
          @abrir-validar="abrirModalValidar"
          @ver-sugestoes="verSugestoes"
      />

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

      <MapaFluxoModais
          :atividades="atividades"
          :carregando-fluxo-mapa="carregandoFluxoMapa"
          :codigo-subprocesso="codigoSubprocesso"
          :competencia-para-excluir="competenciaParaExcluir"
          :competencia-sendo-editada="competenciaSendoEditada"
          :field-errors="fieldErrors"
          :historico-analise="historicoAnalise"
          :homologacao="acaoPrincipalMapa?.codigo === 'HOMOLOGAR'"
          :impactos="impactos"
          :is-chefe="isChefe"
          :loading-competencia="loadingCompetencia"
          :loading-exclusao="loadingExclusao"
          :loading-impacto="loadingImpacto"
          :loading-disponibilizacao="loadingDisponibilizacao"
          :loading-sugestoes-envio="loadingSugestoesEnvio"
          :mensagem-erro-devolucao="mensagemErroDevolucao"
          :mensagem-erro-sugestoes="mensagemErroSugestoes"
          :modo-somente-leitura="modoSomenteLeitura"
          :mostrar-modal-aceitar="mostrarModalAceitar"
          :mostrar-modal-criar-nova-competencia="mostrarModalCriarNovaCompetencia"
          :mostrar-modal-devolucao="mostrarModalDevolucao"
          :mostrar-modal-disponibilizar="mostrarModalDisponibilizar"
          :mostrar-modal-excluir-competencia="mostrarModalExcluirCompetencia"
          :mostrar-modal-historico="mostrarModalHistorico"
          :mostrar-modal-impacto="mostrarModalImpacto"
          :mostrar-modal-sugestoes="mostrarModalSugestoes"
          :mostrar-modal-validar="mostrarModalValidar"
          :mostrar-modal-ver-sugestoes="mostrarModalVerSugestoes"
          :notificacao-disponibilizacao="notificacaoDisponibilizacao"
          :observacao-devolucao="observacaoDevolucao"
          :sugestoes="sugestoes"
          :sugestoes-visualizacao="sugestoesVisualizacao"
          :ultima-data-limite-subprocesso="subprocesso?.ultimaDataLimiteSubprocesso"
          @confirmar-aceitacao="confirmarAceitacao"
          @confirmar-devolucao="handleConfirmarDevolucao"
          @confirmar-exclusao-competencia="confirmarExclusaoCompetencia"
          @confirmar-sugestoes="handleConfirmarSugestoes"
          @confirmar-validacao="confirmarValidacao"
          @disponibilizar="disponibilizarMapa"
          @fechar-aceite="fecharModalAceitar"
          @fechar-criar-competencia="fecharModalCriarNovaCompetencia"
          @fechar-disponibilizar="fecharModalDisponibilizar"
          @fechar-historico="fecharModalHistorico"
          @fechar-impacto="fecharModalImpacto"
          @fechar-ver-sugestoes="fecharModalVerSugestoes"
          @salvar-competencia="adicionarCompetenciaEFecharModal"
          @update:mostrarModalDevolucao="mostrarModalDevolucao = $event"
          @update:mostrarModalExcluirCompetencia="mostrarModalExcluirCompetencia = $event"
          @update:mostrarModalSugestoes="mostrarModalSugestoes = $event"
          @update:mostrarModalValidar="mostrarModalValidar = $event"
          @update:mostrarModalVerSugestoes="mostrarModalVerSugestoes = $event"
          @update:observacaoDevolucao="observacaoDevolucao = $event"
          @update:sugestoes="sugestoes = $event"
          @update:sugestoesVisualizacao="sugestoesVisualizacao = $event"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import EmptyState from "@/components/comum/EmptyState.vue";
import MapaAcoesHeader from "@/components/mapa/MapaAcoesHeader.vue";
import MapaFluxoModais from "@/components/mapa/MapaFluxoModais.vue";
import CompetenciaCard from "@/components/mapa/CompetenciaCard.vue";
import MapaSomenteLeitura from "@/components/mapa/MapaSomenteLeitura.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import {computed, onMounted, ref} from "vue";
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
import {listarAnalisesCadastro} from "@/services/analiseService";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useMapaOrquestracao} from "@/composables/useMapaOrquestracao";
import {useMapaSugestoes} from "@/composables/useMapaSugestoes";
import logger from "@/utils/logger";
import {normalizeError} from "@/utils/apiError";
import {Perfil} from "@/types/tipos";
import type {
  Analise,
  MapaCompleto,
} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{ codProcesso: number | string; sigla: string; codSubprocesso?: number }>();
const router = useRouter();
const fluxoMapa = useFluxoMapa();
const carregandoFluxoMapa = fluxoMapa.carregando;
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
  podeVerSugestoes,
  habilitarDevolverMapa,
  acaoPrincipalMapa
} = useAcesso(subprocesso);

const usarMenuAcoesMapa = computed(() => {
  return mostrarApresentarSugestoes.value
      || mostrarValidarMapa.value
      || mostrarDevolverMapa.value
      || Boolean(acaoPrincipalMapa.value?.mostrar)
      || mostrarDisponibilizarMapa.value;
});
const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);
const modoSomenteLeitura = computed(() => !podeEditarMapa.value);
const mostrarAcaoPrincipalMapa = computed(() => Boolean(acaoPrincipalMapa.value?.mostrar));
const habilitarAcaoPrincipalMapa = computed(() => acaoPrincipalMapa.value?.habilitar ?? false);
const rotuloAcaoPrincipalMapa = computed(() => acaoPrincipalMapa.value?.rotuloBotao ?? TEXTOS.mapa.LABEL_HOMOLOGAR);

const {
  carregandoInicial,
  codigoSubprocesso,
  unidade,
  carregarContextoInicial,
} = useMapaOrquestracao(props);

const mapasStore = useMapas(codigoSubprocesso);
const {impactoMapa: impactos, erro: erroMapa} = mapasStore;

const atividades = computed(() => mapasStore.mapaCompleto.value?.atividades ?? []);
const competencias = computed(() => mapasStore.mapaCompleto.value?.competencias ?? []);
const mapaSomenteLeitura = computed(() => mapasStore.mapaCompleto.value);

const mostrarModalAceitar = ref(false);
const mostrarModalValidar = ref(false);
const mostrarModalDevolucao = ref(false);
const observacaoDevolucao = ref("");

const analisesCadastro = ref<Analise[]>([]);
const historicoAnalise = computed(() => analisesCadastro.value);
const mostrarModalHistorico = ref(false);

const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const {
  sugestoes,
  sugestoesVisualizacao,
  loadingSugestoesVisualizacao,
  loadingSugestoesEnvio,
  mostrarModalSugestoes,
  mostrarModalVerSugestoes,
  verSugestoes,
  fecharModalVerSugestoes,
  abrirModalSugestoes,
  handleConfirmarSugestoes,
  sincronizarSugestoesMapa,
  carregarSugestoesParaVisualizacao,
  carregarSugestoesParaEdicao
} = useMapaSugestoes({
  codigoSubprocesso,
  notify,
  concluirAcaoPainel,
  validarSubmissao,
  focarPrimeiroErroInvalido,
  resetarValidacao
});

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
  const codSubprocessoAtual = codigoSubprocesso.value;
  if (mapaAtualizado && codSubprocessoAtual) {
    mapasStore.definirMapaCompleto(codSubprocessoAtual, mapaAtualizado);
    if (subprocessoStore.contextoEdicao?.detalhes.codigo === codSubprocessoAtual) {
      subprocessoStore.contextoEdicao.mapa = mapaAtualizado;
    }
  }
}

onMounted(async () => {
  const sucesso = await carregarContextoInicial();
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
      aplicarErroNormalizado(normalizeError(error) as ReturnType<typeof normalizeError>);
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
  mostrarModalAceitar,
  mostrarModalValidar,
  mostrarModalDevolucao,
  mostrarModalVerSugestoes,
  mostrarModalSugestoes,
  loadingImpacto,
  loadingCompetencia,
  loadingExclusao,
  loadingDisponibilizacao,
  notificacaoDisponibilizacao,
  sugestoes,
  sugestoesVisualizacao,
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
