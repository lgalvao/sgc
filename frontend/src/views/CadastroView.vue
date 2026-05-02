<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial" />
    <template v-else>
      <CadastroAcoesHeader
          :unidade="unidade"
          :cod-subprocesso="codigoSubprocesso"
          :permissoes="permissoesUI"
          :mostrar-devolver-cadastro="mostrarDevolverCadastro"
          :mostrar-importar-atividades="mostrarImportarAtividades"
          :mostrar-disponibilizar-cadastro="mostrarDisponibilizarCadastro"
          :acao-principal-cadastro="acaoPrincipalCadastro"
          :loading-validacao="loadingValidacao"
          :pode-visualizar-impacto="podeVisualizarImpacto"
          @abrir-historico="abrirModalHistorico"
          @abrir-devolver="abrirModalDevolverAnalise"
          @abrir-validar="abrirModalValidarAnalise"
          @abrir-impacto="abrirModalImpacto"
          @abrir-importar="mostrarModalImportar = true"
          @disponibilizar="disponibilizarCadastro"
      />

    <div v-if="podeEditarCadastro && isRevisao" class="mt-3 mb-2">
      <BFormCheckbox
          v-model="disponibilizacaoSemMudancas"
          :disabled="checkboxSemMudancasDesabilitado"
          data-testid="chk-disponibilizacao-sem-mudancas"
      >
        {{ TEXTOS.atividades.CHECKBOX_DISPONIBILIZACAO_SEM_MUDANCAS }}
      </BFormCheckbox>
      <div
          v-if="loadingInicioRevisao"
          class="d-inline-flex align-items-center mt-1"
          data-testid="cad-atividades__spinner-iniciando-revisao"
      >
        <BSpinner small />
      </div>
    </div>

    <BAlert
        v-if="erroGlobalFormatado"
        :key="erroTick"
        :model-value="true"
        no-fade
        show
        variant="danger"
        dismissible
        data-testid="alerta-erro-global"
        @dismissed="erroGlobal = null"
    >
      {{ erroGlobalFormatado.message }}
    </BAlert>

    <AppAlert
        v-if="notificacao"
        :message="notificacao.message"
        :variant="notificacao.variant"
        :dismissible="notificacao.dismissible ?? true"
        @dismissed="clear()"
    />

    <CadAtividadeForm
        v-if="podeEditarCadastro"
        ref="atividadeFormRef"
        v-model="novaAtividade"
        :disabled="!codigoSubprocesso || !habilitarEditarCadastro"
        :erro="erroNovaAtividade"
        :loading="loadingAdicionar"
        @submit="handleAdicionarAtividade"
    />

    <EmptyState
        v-if="atividades?.length === 0"
        :description="podeEditarCadastro ? TEXTOS.atividades.EMPTY_DESCRIPTION : TEXTOS.treeTable.EMPTY_DESCRIPTION"
        data-testid="cad-atividades-empty-state"
        icon="bi-list-check"
        :title="podeEditarCadastro ? TEXTOS.atividades.EMPTY_TITLE : TEXTOS.treeTable.EMPTY_TITLE"
    />

    <div
        v-for="atividade in atividadesOrdenadas"
        :key="atividade.codigo"
        :ref="el => setAtividadeRef(atividade.codigo, el)"
    >
      <AtividadeItem
          :atividade="atividade"
          :erro-validacao="obterErroParaAtividade(atividade.codigo)"
          :pode-editar="podeEditarCadastro"
          :habilitar-edicao="habilitarEditarCadastro"
          @atualizar-atividade="(desc: string) => salvarEdicaoAtividade(atividade.codigo, desc)"
          @remover-atividade="() => removerAtividade(atividade.codigo)"
          @adicionar-conhecimento="(desc: string) => adicionarConhecimento(atividade.codigo, desc)"
          @atualizar-conhecimento="(idC: number, desc: string) => salvarEdicaoConhecimento(atividade.codigo, idC, desc)"
          @remover-conhecimento="(idC: number) => removerConhecimento(atividade.codigo, idC)"
      />
    </div>

      <CadastroFluxoModais
          :acao-principal-cadastro="acaoPrincipalCadastro"
          :codigo-subprocesso="codigoSubprocesso"
          :dados-remocao="dadosRemocao"
          :erro-fluxo="fluxoSubprocesso.lastError?.value?.message"
          :historico-analises="historicoAnalises"
          :impactos="impactos ?? null"
          :is-revisao="isRevisao"
          :loading-analise-cadastro="loadingAnaliseCadastro"
          :loading-devolucao-analise="loadingDevolucaoAnalise"
          :loading-disponibilizacao="loadingDisponibilizacao"
          :loading-impacto="loadingImpacto"
          :loading-remocao="loadingRemocao"
          :mostrar-modal-confirmacao="mostrarModalConfirmacao"
          :mostrar-modal-confirmacao-remocao="mostrarModalConfirmacaoRemocao"
          :mostrar-modal-devolver-analise="mostrarModalDevolverAnalise"
          :mostrar-modal-historico="mostrarModalHistorico"
          :mostrar-modal-impacto="mostrarModalImpacto"
          :mostrar-modal-importar="mostrarModalImportar"
          :mostrar-modal-validar-analise="mostrarModalValidarAnalise"
          :observacao-devolucao="observacaoDevolucao"
          :observacao-validacao="observacaoValidacao"
          @confirmar-devolucao-analise="confirmarDevolucaoAnalise"
          @confirmar-disponibilizacao="confirmarDisponibilizacao"
          @confirmar-remocao="confirmarRemocao"
          @confirmar-validacao-analise="confirmarValidacaoAnalise"
          @fechar-impacto="fecharModalImpacto"
          @importar="handleImportAtividades"
          @update:mostrarModalConfirmacao="mostrarModalConfirmacao = $event"
          @update:mostrarModalConfirmacaoRemocao="mostrarModalConfirmacaoRemocao = $event"
          @update:mostrarModalDevolverAnalise="mostrarModalDevolverAnalise = $event"
          @update:mostrarModalHistorico="mostrarModalHistorico = $event"
          @update:mostrarModalImportar="mostrarModalImportar = $event"
          @update:mostrarModalValidarAnalise="mostrarModalValidarAnalise = $event"
          @update:observacaoDevolucao="observacaoDevolucao = $event"
          @update:observacaoValidacao="observacaoValidacao = $event"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BFormCheckbox, BSpinner} from "bootstrap-vue-next";
import AppAlert from "@/components/comum/AppAlert.vue";
import {computed, nextTick, onMounted, reactive, ref, toRefs, watch} from "vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CadAtividadeForm from "@/components/atividades/CadAtividadeForm.vue";
import AtividadeItem from "@/components/atividades/AtividadeItem.vue";
import CadastroAcoesHeader from "@/components/cadastro/CadastroAcoesHeader.vue";
import CadastroFluxoModais from "@/components/cadastro/CadastroFluxoModais.vue";
import {useAtividadeForm} from "@/composables/useAtividadeForm";
import {useFluxoSubprocesso} from "@/composables/useFluxoSubprocesso";
import {useImpactoMapaModal} from "@/composables/useImpactoMapaModal";
import {useMapas} from "@/composables/useMapas";
import {useNotification} from "@/composables/useNotification";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useAcesso} from "@/composables/useAcesso";
import {useCadastroAtividadesMutacoes} from "@/composables/useCadastroAtividadesMutacoes";
import {useCadastroRevisaoSemMudancas} from "@/composables/useCadastroRevisaoSemMudancas";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useCadastroOrquestracao} from "@/composables/useCadastroOrquestracao";
import {
  type AceitarCadastroRequest,
  type Analise,
  type Atividade,
  type AtividadeOperacaoResponse,
  type DevolverCadastroRequest,
  type ErroValidacao,
  type HomologarCadastroRequest,
  type PermissoesSubprocesso,
  SituacaoSubprocesso,
  TipoProcesso
} from "@/types/tipos";
import logger from "@/utils/logger";
import {calcularAssinaturaCadastro, formatSituacaoSubprocesso} from "@/utils/formatters";
import {normalizeError} from "@/utils/apiError";
import {normalizarPermissoesSubprocesso} from "@/utils/permissoesSubprocesso";
import {listarAnalisesCadastro} from "@/services/analiseService";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
  codSubprocesso?: number;
}>();

const atividades = ref<Atividade[]>([]);

const {
  carregandoInicial,
  codigoSubprocesso,
  atividadesSnapshotInicial,
  unidade,
  codMapa,
  carregarContextoInicial,
  processarRespostaLocal
} = useCadastroOrquestracao(props, atividades);

const subprocessoStore = useSubprocessoStore();
const mapasStore = useMapas(codigoSubprocesso);
const fluxoSubprocesso = useFluxoSubprocesso();
const {notify, notificacao, clear} = useNotification();
const {
  validarSubmissao,
  resetarValidacao,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();
const {impactoMapa: impactos} = mapasStore;

const subprocesso = computed(() => subprocessoStore.contextoCadastro?.detalhes ?? null);
const acesso = useAcesso(subprocesso);
const {
  podeEditarCadastro,
  podeVisualizarImpacto,
  podeDevolverCadastro,
  mostrarDevolverCadastro,
  mostrarImportarAtividades,
  mostrarDisponibilizarCadastro,
  habilitarEditarCadastro,
  habilitarDevolverCadastro,
  podeDisponibilizarCadastro,
  acaoPrincipalCadastro
} = acesso;

const isRevisao = computed(() => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO);
const permissoesUI = computed<PermissoesSubprocesso>(() => ({
  ...normalizarPermissoesSubprocesso(subprocesso.value?.permissoes),
  podeEditarCadastro: podeEditarCadastro.value,
  podeDisponibilizarCadastro: podeDisponibilizarCadastro.value,
  podeDevolverCadastro: podeDevolverCadastro.value,
  habilitarEditarCadastro: habilitarEditarCadastro.value,
  habilitarDevolverCadastro: habilitarDevolverCadastro.value,
}));



const assinaturaCadastroAtual = computed(() => calcularAssinaturaCadastro(atividades.value));
const houveAlteracaoCadastro = computed(() => assinaturaCadastroAtual.value !== atividadesSnapshotInicial.value);

const atividadesOrdenadas = computed(() => {
  return [...atividades.value].sort((a, b) => (b.codigo || 0) - (a.codigo || 0));
});

const habilitarDisponibilizar = computed(() => {
  const cadastroCompleto = atividades.value.length > 0 &&
      atividades.value.every((atividade) => atividade.conhecimentos && atividade.conhecimentos.length > 0);

  if (isRevisao.value) {
    return cadastroCompleto && (houveAlteracaoCadastro.value || disponibilizacaoSemMudancas.value);
  }

  return cadastroCompleto;
});



const analisesCadastro = ref<Analise[]>([]);

const situacaoAtual = computed(() => subprocesso.value?.situacao);
const {
  disponibilizacaoSemMudancas,
  checkboxSemMudancasDesabilitado,
  precisaIniciarRevisao,
  loadingInicioRevisao,
  iniciarRevisaoSeNecessario,
  cancelarInicioRevisaoSeNecessario,
  sincronizarDisponibilizacaoSemMudancasInicial
} = useCadastroRevisaoSemMudancas({
  codigoSubprocesso,
  isRevisao,
  situacaoAtual,
  houveAlteracaoCadastro,
  fluxoSubprocesso
});

const {withErrorHandling, lastError} = useErrorHandler();

const historicoAnalises = computed(() => {
  return analisesCadastro.value;
});

const {novaAtividade, loadingAdicionar, adicionarAtividade: adicionarAtividadeAction} = useAtividadeForm();

watch(novaAtividade, (valorAtual, valorAnterior) => {
  if (valorAtual !== valorAnterior && erroNovaAtividade.value) {
    erroNovaAtividade.value = null;
  }
});

watch(assinaturaCadastroAtual, (valorAtual, valorAnterior) => {
  if (valorAtual !== valorAnterior && (errosValidacao.value.length > 0 || erroGlobal.value)) {
    limparErrosValidacaoCadastro();
  }
});

const estadoModais = reactive({
  mostrarModalImportar: false,
  mostrarModalConfirmacao: false,
  mostrarModalHistorico: false,
  mostrarModalValidarAnalise: false,
  mostrarModalDevolverAnalise: false,
});
const {
  mostrarModalImportar,
  mostrarModalConfirmacao,
  mostrarModalHistorico,
  mostrarModalValidarAnalise,
  mostrarModalDevolverAnalise,
} = toRefs(estadoModais);
const {
  mostrarModalImpacto,
  loadingImpacto,
  abrirModalImpacto,
  fecharModalImpacto,
} = useImpactoMapaModal(codigoSubprocesso, (codigo) => mapasStore.buscarImpactoMapa(codigo));

const loadingValidacao = ref(false);
const loadingDisponibilizacao = ref(false);
const loadingAnaliseCadastro = ref(false);
const loadingDevolucaoAnalise = ref(false);
const errosValidacao = ref<ErroValidacao[]>([]);
const erroGlobal = ref<string | null>(null);
const erroTick = ref(0);
const observacaoValidacao = ref("");
const observacaoDevolucao = ref("");
const atividadeRefs = new Map<number, Element>();
let timeoutLimparErros: ReturnType<typeof setTimeout> | null = null;


function timeoutLimpezaErros() {
  limparTimeoutErrosCadastro();
  timeoutLimparErros = setTimeout(() => {
    limparErrosValidacaoCadastro();
  }, 6000);
}

function limparTimeoutErrosCadastro() {
  if (timeoutLimparErros) {
    clearTimeout(timeoutLimparErros);
    timeoutLimparErros = null;
  }
}

function limparErrosValidacaoCadastro() {
  limparTimeoutErrosCadastro();
  errosValidacao.value = [];
  erroGlobal.value = null;
}

async function registrarErrosValidacaoCadastro(erros: ErroValidacao[]) {
  // Força reatividade limpando e incrementando tick para garantir que o alerta reapareça se for o mesmo erro
  limparErrosValidacaoCadastro();
  erroTick.value++;

  await nextTick();
  errosValidacao.value = erros;
  const msg = erros.find((erro) => !erro.atividadeCodigo)?.mensagem ?? null;
  erroGlobal.value = msg;
}

function obterSituacaoReferenciaDisponibilizacao(): SituacaoSubprocesso {
  return isRevisao.value
      ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
      : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
}

function obterErroPreValidacaoDisponibilizacao(): string | null {
  const cadastroIncompleto = atividades.value.length === 0
      || atividades.value.some((atividade) => !atividade.conhecimentos || atividade.conhecimentos.length === 0);

  if (cadastroIncompleto) {
    return TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO;
  }

  if (isRevisao.value && !houveAlteracaoCadastro.value && !disponibilizacaoSemMudancas.value) {
    return TEXTOS.atividades.ERRO_REVISAO_SEM_ALTERACAO;
  }

  return null;
}

async function aplicarResultadoValidacaoCadastro(valido: boolean, erros: ErroValidacao[]) {
  if (valido) {
    mostrarModalConfirmacao.value = true;
    return;
  }

  await registrarErrosValidacaoCadastro(erros);
}

const mapaErros = computed(() => {
  const mapa = new Map<number, string>();
  errosValidacao.value.forEach((erro) => {
    if (erro.atividadeCodigo) {
      const atividade = atividades.value.find(a => a.codigo === erro.atividadeCodigo);
      if (!atividade || !atividade.conhecimentos || atividade.conhecimentos.length === 0) {
        mapa.set(erro.atividadeCodigo, erro.mensagem);
      }
    }
  });
  return mapa;
});

const atividadeFormRef = ref<InstanceType<typeof CadAtividadeForm> | null>(null);
const erroGlobalFormatado = computed(() =>
    erroGlobal.value ? {message: erroGlobal.value} : null
);

const {
  erroNovaAtividade,
  dadosRemocao,
  loadingRemocao,
  mostrarModalConfirmacaoRemocao,
  executarAtualizacaoCadastro,
  adicionarAtividade,
  removerAtividade,
  confirmarRemocao,
  salvarEdicaoAtividade,
  adicionarConhecimento,
  removerConhecimento,
  salvarEdicaoConhecimento
} = useCadastroAtividadesMutacoes({
  atividades,
  codigoSubprocesso,
  codMapa,
  withErrorHandling,
  lastError,
  notify,
  processarRespostaLocal,
  adicionarAtividadeAction
});

async function handleImportAtividades(resultado: AtividadeOperacaoResponse) {
  mostrarModalImportar.value = false;
  clear();
  await nextTick();
  processarRespostaLocal(resultado);
  if (resultado.aviso) {
    notify(TEXTOS.atividades.AVISO_IMPORTACAO_DUPLICATAS, 'warning');
  } else {
    notify(TEXTOS.atividades.SUCESSO_IMPORTACAO, 'success');
  }
}

function obterErroParaAtividade(atividadeCodigo: number): string | undefined {
  return mapaErros.value.get(atividadeCodigo);
}

function setAtividadeRef(atividadeCodigo: number, el: unknown) {
  if (el && el instanceof Element) {
    atividadeRefs.set(atividadeCodigo, el);
  }
}

function scrollParaPrimeiroErro() {
  if (errosValidacao.value.length > 0 && errosValidacao.value[0].atividadeCodigo) {
    const primeiraAtividadeComErro = atividadeRefs.get(errosValidacao.value[0].atividadeCodigo);
    if (primeiraAtividadeComErro) {
      primeiraAtividadeComErro.scrollIntoView({
        behavior: "instant" as ScrollBehavior,
        block: "center",
      });
    }
  }
}

async function disponibilizarCadastro() {
  if (loadingValidacao.value) return;

  // Limpa erros anteriores antes de começar nova tentativa
  limparErrosValidacaoCadastro();

  const situacaoAtualCadastro = subprocesso.value?.situacao;
  const situacaoReferencia = obterSituacaoReferenciaDisponibilizacao();
  const erroPreValidacao = obterErroPreValidacaoDisponibilizacao();
  if (erroPreValidacao) {
    await registrarErrosValidacaoCadastro([{ tipo: "PRE_VALIDACAO", mensagem: erroPreValidacao }]);
    return;
  }

  if (!situacaoAtualCadastro || situacaoAtualCadastro !== situacaoReferencia) {
    erroGlobal.value = TEXTOS.comum.ACAO_NAO_PERMITIDA_SITUACAO(formatSituacaoSubprocesso(situacaoReferencia));
    return;
  }

  if (!codigoSubprocesso.value) {
    erroGlobal.value = "Identificador do subprocesso não encontrado. Recarregue a página.";
    return;
  }

  loadingValidacao.value = true;
  
  try {
    const resultado = await fluxoSubprocesso.validarCadastro(codigoSubprocesso.value);
    if (resultado) {
      await aplicarResultadoValidacaoCadastro(resultado.valido, resultado.erros);
      if (!resultado.valido) {
        await nextTick();
        try {
          scrollParaPrimeiroErro();
        } catch (domError) {
          logger.warn('Falha ao executar scroll para erro', domError);
        }
      }
    } else {
      erroGlobal.value = "Não foi possível obter o resultado da validação. Tente novamente.";
    }
  } catch (e) {
    erroGlobal.value = normalizeError(e).message;
  } finally {
    loadingValidacao.value = false;
  }
}

async function confirmarDisponibilizacao() {
  if (!codigoSubprocesso.value || loadingDisponibilizacao.value) return;

  loadingDisponibilizacao.value = true;
  try {
    await fluxoSubprocesso.disponibilizarCadastro(codigoSubprocesso.value, isRevisao.value);
  } finally {
    loadingDisponibilizacao.value = false;
  }
  mostrarModalConfirmacao.value = false;
}

async function abrirModalHistorico() {
  if (codigoSubprocesso.value) {
    analisesCadastro.value = await listarAnalisesCadastro(codigoSubprocesso.value);
  }
  mostrarModalHistorico.value = true;
}

function abrirModalValidarAnalise() {
  mostrarModalValidarAnalise.value = true;
}

function fecharModalValidarAnalise() {
  mostrarModalValidarAnalise.value = false;
  observacaoValidacao.value = "";
}

function abrirModalDevolverAnalise() {
  resetarValidacao();
  mostrarModalDevolverAnalise.value = true;
}

function fecharModalDevolverAnalise() {
  mostrarModalDevolverAnalise.value = false;
  observacaoDevolucao.value = "";
  resetarValidacao();
}

async function confirmarValidacaoAnalise() {
  if (!codigoSubprocesso.value) return;

  const acao = acaoPrincipalCadastro.value;
  if (!acao) return;

  loadingAnaliseCadastro.value = true;
  try {
    let sucesso: boolean;

    if (acao.codigo === "HOMOLOGAR") {
      const req: HomologarCadastroRequest = {observacoes: observacaoValidacao.value};
      const paramsRedirecionamento = acao.redirecionarParaPainel
          ? undefined
          : {
            name: "Subprocesso",
            params: {codProcesso: props.codProcesso, siglaUnidade: props.sigla},
          };

      sucesso = await fluxoSubprocesso.homologarCadastro(codigoSubprocesso.value, req, isRevisao.value, {
        mensagemSucesso: acao.mensagemSucesso,
        redirecionarParaPainel: acao.redirecionarParaPainel,
        redirecionarPara: paramsRedirecionamento
      });

      if (sucesso) {
        fecharModalValidarAnalise();
      }
      return;
    }

    const req: AceitarCadastroRequest = {observacoes: observacaoValidacao.value};
    sucesso = await fluxoSubprocesso.aceitarCadastro(codigoSubprocesso.value, req, isRevisao.value, {
      mensagemSucesso: acao.mensagemSucesso
    });

    if (sucesso) {
      fecharModalValidarAnalise();
    }
  } finally {
    loadingAnaliseCadastro.value = false;
  }
}

async function confirmarDevolucaoAnalise() {
  if (!validarSubmissao(!!observacaoDevolucao.value.trim())) {
    void focarPrimeiroErroInvalido();
    return;
  }

  if (!codigoSubprocesso.value) return;

  loadingDevolucaoAnalise.value = true;
  try {
    const req: DevolverCadastroRequest = {observacoes: observacaoDevolucao.value};
    const sucesso = await fluxoSubprocesso.devolverCadastro(codigoSubprocesso.value, req, isRevisao.value);

    if (sucesso) {
      fecharModalDevolverAnalise();
    }
  } finally {
    loadingDevolucaoAnalise.value = false;
  }
}

async function handleAdicionarAtividade() {
  const sucesso = await adicionarAtividade();
  await nextTick();
  if (sucesso || erroNovaAtividade.value) atividadeFormRef.value?.inputRef?.$el?.focus();
}

onMounted(async () => {
  await carregarContextoInicial();

  sincronizarDisponibilizacaoSemMudancasInicial();
});

watch(() => atividades.value?.length, (newLen, oldLen) => {
  if (podeEditarCadastro.value && newLen === 0 && oldLen === undefined) {
    nextTick(() => atividadeFormRef.value?.inputRef?.$el?.focus());
  }
}, {immediate: true});

</script>
