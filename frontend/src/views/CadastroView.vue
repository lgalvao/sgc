<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial" />
    <template v-else>
      <CadastroAcoesHeader
          :unidade="unidade"
          :cod-subprocesso="codSubprocesso"
          :permissoes="permissoesUI"
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
        :key="erroGlobalFormatado.message"
        :model-value="true"
        no-fade
        show
        variant="danger"
        dismissible
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
        ref="atividadeFormRef"
        v-model="novaAtividade"
        :disabled="!codSubprocesso || !habilitarEditarCadastro"
        :erro="erroNovaAtividade"
        :loading="loadingAdicionar"
        @submit="handleAdicionarAtividade"
    />

    <EmptyState
        v-if="atividades?.length === 0"
        :description="TEXTOS.atividades.EMPTY_DESCRIPTION"
        data-testid="cad-atividades-empty-state"
        icon="bi-list-check"
        :title="TEXTOS.atividades.EMPTY_TITLE"
    />

    <div
        v-for="(atividade, idx) in atividades"
        :key="atividade.codigo || idx"
        :ref="el => setAtividadeRef(atividade.codigo, el)"
    >
      <AtividadeItem
          :atividade="atividade"
          :erro-validacao="obterErroParaAtividade(atividade.codigo)"
          :pode-editar="podeEditarCadastro"
          :habilitar-edicao="habilitarEditarCadastro"
          @atualizar-atividade="(desc: string) => salvarEdicaoAtividade(atividade.codigo, desc)"
          @remover-atividade="() => removerAtividade(idx)"
          @adicionar-conhecimento="(desc: string) => adicionarConhecimento(idx, desc)"
          @atualizar-conhecimento="(idC: number, desc: string) => salvarEdicaoConhecimento(atividade.codigo, idC, desc)"
          @remover-conhecimento="(idC: number) => removerConhecimento(idx, idC)"
      />
    </div>

    <ImportarAtividadesModal
        :cod-subprocesso-destino="codSubprocesso"
        :mostrar="mostrarModalImportar"
        @fechar="mostrarModalImportar = false"
        @importar="handleImportAtividades"
    />

    <ImpactoMapaModal
        v-if="codSubprocesso"
        :impacto="impactos"
        :loading="loadingImpacto"
        :mostrar="mostrarModalImpacto"
        @fechar="fecharModalImpacto"
    />

    <ConfirmacaoDisponibilizacaoModal
        :is-revisao="isRevisao"
        :loading="loadingDisponibilizacao"
        :mostrar="mostrarModalConfirmacao"
        :erro="fluxoSubprocesso.lastError?.value?.message"
        @confirmar="confirmarDisponibilizacao"
        @fechar="mostrarModalConfirmacao = false"
    />

    <HistoricoAnaliseModal
        :historico="historicoAnalises"
        :mostrar="mostrarModalHistorico"
        @fechar="mostrarModalHistorico = false"
    />

      <ModalConfirmacao
          v-model="mostrarModalConfirmacaoRemocao"
          :loading="loadingRemocao"
          :mensagem="dadosRemocao?.tipo === 'atividade' ? TEXTOS.atividades.MODAL_REMOVER_ATIVIDADE_TEXTO : TEXTOS.atividades.MODAL_REMOVER_CONHECIMENTO_TEXTO"
          :ok-title="TEXTOS.comum.BOTAO_REMOVER"
          :titulo="dadosRemocao?.tipo === 'atividade' ? TEXTOS.atividades.MODAL_REMOVER_ATIVIDADE_TITULO : TEXTOS.atividades.MODAL_REMOVER_CONHECIMENTO_TITULO"
          variant="danger"
          @confirmar="confirmarRemocao"
      />

      <ModalAceiteCadastro
          v-model="mostrarModalValidarAnalise"
          v-model:observacao="observacaoValidacao"
          :loading="loadingAnaliseCadastro"
          :acao="acaoPrincipalCadastro"
          :erro="fluxoSubprocesso.lastError?.value?.message"
          @confirmar="confirmarValidacaoAnalise"
      />

      <ModalDevolucaoCadastro
          v-model="mostrarModalDevolverAnalise"
          v-model:observacao="observacaoDevolucao"
          :loading="loadingDevolucaoAnalise"
          :is-revisao="isRevisao"
          :erro="fluxoSubprocesso.lastError?.value?.message"
          @confirmar="confirmarDevolucaoAnalise"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BFormCheckbox,
  BFormGroup,
  BFormInvalidFeedback,
  BFormTextarea,
  BSpinner
} from "bootstrap-vue-next";
import AppAlert from "@/components/comum/AppAlert.vue";
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/atividades/ImportarAtividadesModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/mapa/ConfirmacaoDisponibilizacaoModal.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CadAtividadeForm from "@/components/atividades/CadAtividadeForm.vue";
import AtividadeItem from "@/components/atividades/AtividadeItem.vue";
import CadastroAcoesHeader from "@/components/cadastro/CadastroAcoesHeader.vue";
import ModalAceiteCadastro from "@/components/cadastro/ModalAceiteCadastro.vue";
import ModalDevolucaoCadastro from "@/components/cadastro/ModalDevolucaoCadastro.vue";
import {useAtividadeForm} from "@/composables/useAtividadeForm";
import {useFluxoSubprocesso} from "@/composables/useFluxoSubprocesso";
import {useImpactoMapaModal} from "@/composables/useImpactoMapaModal";
import {useMapas} from "@/composables/useMapas";
import {useNotification} from "@/composables/useNotification";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useToastStore} from "@/stores/toast";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {useAcesso} from "@/composables/useAcesso";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {
  type AceitarCadastroRequest,
  type PermissoesSubprocesso,
  type Analise,
  type Atividade,
  type AtividadeOperacaoResponse,
  type ContextoCadastroAtividadesSubprocesso,
  type CriarConhecimentoRequest,
  type DevolverCadastroRequest,
  type ErroValidacao,
  type HomologarCadastroRequest,
  SituacaoSubprocesso,
  TipoProcesso,
  type Unidade
} from "@/types/tipos";
import logger from "@/utils/logger";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import * as atividadeService from "@/services/atividadeService";
import {listarAnalisesCadastro} from "@/services/analiseService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {TEXTOS} from "@/constants/textos";

type DadosRemocao = { tipo: "atividade" | "conhecimento"; index: number; conhecimentoCodigo?: number } | null;
const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
  codSubprocesso?: number;
}>();

const router = useRouter();
const subprocessoStore = useSubprocessoStore();
const mapasStore = useMapas();
const fluxoSubprocesso = useFluxoSubprocesso();
const {notify, notificacao, clear} = useNotification();
const toastStore = useToastStore();
const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();
const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();
const {impactoMapa: impactos} = mapasStore;
const codSubprocesso = ref<number | null>(null);
const codMapa = ref<number | null>(null);
const carregandoInicial = ref(true);
const subprocesso = computed(() => subprocessoStore.contextoCadastro?.detalhes ?? null);
const unidade = ref<Unidade | null>(null);
const acesso = useAcesso(subprocesso);
const {
  podeEditarCadastro,
  podeVisualizarImpacto,
  podeDevolverCadastro,
  habilitarEditarCadastro,
  habilitarDevolverCadastro,
  podeDisponibilizarCadastro,
  acaoPrincipalCadastro
} = acesso;
const isRevisao = computed(() => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO);
const permissoesUI = computed<PermissoesSubprocesso>(() => ({
  ...(subprocesso.value?.permissoes || {}),
  podeEditarCadastro: podeEditarCadastro?.value ?? false,
  podeDisponibilizarCadastro: podeDisponibilizarCadastro?.value ?? false,
  podeDevolverCadastro: podeDevolverCadastro?.value ?? false,
  habilitarEditarCadastro: habilitarEditarCadastro?.value ?? false,
  habilitarDevolverCadastro: habilitarDevolverCadastro?.value ?? false,
} as PermissoesSubprocesso));


const atividades = ref<Atividade[]>([]);
const atividadesSnapshotInicial = ref<string>('[]');
const disponibilizacaoSemMudancas = ref(false);

function calcularAssinaturaCadastro(lista: Atividade[]): string {
  return lista
      .map(atividade => {
        const descricao = (atividade.descricao || '').trim();
        const conhecimentos = (atividade.conhecimentos || [])
            .map(conhecimento => (conhecimento.descricao || '').trim())
            .sort()
            .join('\u0001');
        return `${descricao}\u0002${conhecimentos}`;
      })
      .sort((a, b) => a.localeCompare(b))
      .join('\u0003');
}

const assinaturaCadastroAtual = computed(() => calcularAssinaturaCadastro(atividades.value));
const houveAlteracaoCadastro = computed(() => assinaturaCadastroAtual.value !== atividadesSnapshotInicial.value);
const checkboxSemMudancasDesabilitado = computed(() => loadingInicioRevisao.value || houveAlteracaoCadastro.value);

const habilitarDisponibilizar = computed(() => {
  const cadastroValido = atividades.value.length > 0
      && atividades.value.every(a => a.conhecimentos && a.conhecimentos.length > 0);

  if (!cadastroValido) {
    return false;
  }

  if (!isRevisao.value) {
    return true;
  }

  return houveAlteracaoCadastro.value || (
      disponibilizacaoSemMudancas.value
      && situacaoAtual.value === SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
  );
});



const analisesCadastro = ref<Analise[]>([]);

const situacaoAtual = computed(() => subprocesso.value?.situacao);
const precisaIniciarRevisao = computed(() =>
    isRevisao.value &&
    situacaoAtual.value === SituacaoSubprocesso.NAO_INICIADO
);

async function iniciarRevisaoSeNecessario() {
  if (!precisaIniciarRevisao.value || !codSubprocesso.value || loadingInicioRevisao.value) return;

  loadingInicioRevisao.value = true;
  try {
    const sucesso = await fluxoSubprocesso.iniciarRevisaoCadastro(codSubprocesso.value);
    if (!sucesso) {
      logger.error('Falha ao iniciar revisão do cadastro');
    }
  } finally {
    loadingInicioRevisao.value = false;
  }
}

async function cancelarInicioRevisaoSeNecessario() {
  if (situacaoAtual.value !== SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
      || !codSubprocesso.value
      || loadingInicioRevisao.value
      || houveAlteracaoCadastro.value) return;

  loadingInicioRevisao.value = true;
  try {
    const sucesso = await fluxoSubprocesso.cancelarInicioRevisaoCadastro(codSubprocesso.value);
    if (!sucesso) {
      logger.error('Falha ao cancelar início da revisão do cadastro');
    }
  } finally {
    loadingInicioRevisao.value = false;
  }
}

let ignorarAlteracaoCheckboxSemMudancas = false;

function atualizarCheckboxSemMudancasSilenciosamente(valor: boolean) {
  ignorarAlteracaoCheckboxSemMudancas = true;
  disponibilizacaoSemMudancas.value = valor;
  queueMicrotask(() => {
    ignorarAlteracaoCheckboxSemMudancas = false;
  });
}

watch(disponibilizacaoSemMudancas, async (marcado) => {
  if (ignorarAlteracaoCheckboxSemMudancas) {
    return;
  }

  if (marcado) {
    if (!precisaIniciarRevisao.value) return;

    await iniciarRevisaoSeNecessario();
    if (situacaoAtual.value !== SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO) {
      atualizarCheckboxSemMudancasSilenciosamente(false);
    }
    return;
  }

  if (houveAlteracaoCadastro.value || situacaoAtual.value !== SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO) {
    return;
  }

  await cancelarInicioRevisaoSeNecessario();
  const situacaoAposCancelamento = subprocesso.value?.situacao;
  if (situacaoAposCancelamento !== SituacaoSubprocesso.NAO_INICIADO) {
    atualizarCheckboxSemMudancasSilenciosamente(true);
  }
});

watch(houveAlteracaoCadastro, (alterou) => {
  if (alterou && disponibilizacaoSemMudancas.value) {
    atualizarCheckboxSemMudancasSilenciosamente(false);
  }
});

const {withErrorHandling, lastError} = useErrorHandler();

const historicoAnalises = computed(() => {
  return analisesCadastro.value || [];
});

const {novaAtividade, loadingAdicionar, adicionarAtividade: adicionarAtividadeAction} = useAtividadeForm();
const erroNovaAtividade = ref<string | null>(null);

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

const mostrarModalImportar = ref(false);
const mostrarModalConfirmacao = ref(false);
const mostrarModalHistorico = ref(false);
const mostrarModalConfirmacaoRemocao = ref(false);
const mostrarModalValidarAnalise = ref(false);
const mostrarModalDevolverAnalise = ref(false);
const dadosRemocao = ref<DadosRemocao>(null);
const {
  mostrarModalImpacto,
  loadingImpacto,
  abrirModalImpacto,
  fecharModalImpacto,
} = useImpactoMapaModal(codSubprocesso, (codigo) => mapasStore.buscarImpactoMapa(codigo));

const loadingValidacao = ref(false);
const loadingDisponibilizacao = ref(false);
const loadingRemocao = ref(false);
const loadingInicioRevisao = ref(false);
const loadingAnaliseCadastro = ref(false);
const loadingDevolucaoAnalise = ref(false);
const errosValidacao = ref<ErroValidacao[]>([]);
const erroGlobal = ref<string | null>(null);
const observacaoValidacao = ref("");
const observacaoDevolucao = ref("");
const atividadeRefs = new Map<number, Element>();
let timeoutLimparErros: ReturnType<typeof setTimeout> | null = null;

const estadoObservacaoDevolucao = computed(() => {
  return deveExibirErro(!observacaoDevolucao.value.trim()) ? false : null;
});

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

function registrarErrosValidacaoCadastro(erros: ErroValidacao[]) {
  errosValidacao.value = erros;
  erroGlobal.value = erros.find((erro) => !erro.atividadeCodigo)?.mensagem ?? null;
}

function obterSituacaoReferenciaDisponibilizacao(): SituacaoSubprocesso {
  return isRevisao.value
      ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
      : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
}

function obterErroPreValidacaoDisponibilizacao(): string | null {
  if (habilitarDisponibilizar.value) {
    return null;
  }

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

function aplicarResultadoValidacaoCadastro(valido: boolean, erros: ErroValidacao[]) {
  if (valido) {
    mostrarModalConfirmacao.value = true;
    return;
  }

  registrarErrosValidacaoCadastro(erros);
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

type RespostaLocalCadastro = Pick<AtividadeOperacaoResponse, "subprocesso" | "permissoes" | "atividadesAtualizadas">;
type AcaoAtualizacaoCadastro = () => Promise<RespostaLocalCadastro>;

function processarRespostaLocal(response: RespostaLocalCadastro) {
  atividades.value = response.atividadesAtualizadas;
  subprocessoStore.atualizarStatusLocal({
    ...response.subprocesso,
    permissoes: response.permissoes
  });
}

function sincronizarEstadoInicialContexto(data: ContextoCadastroAtividadesSubprocesso) {
  processarRespostaLocal({
    subprocesso: {
      codigo: data.detalhes.codigo,
      situacao: data.detalhes.situacao,
    },
    permissoes: data.detalhes.permissoes,
    atividadesAtualizadas: data.atividadesDisponiveis,
  });
  atividadesSnapshotInicial.value = data.assinaturaCadastroReferencia ?? calcularAssinaturaCadastro(data.atividadesDisponiveis);
  atualizarCheckboxSemMudancasSilenciosamente(
      data.detalhes.tipoProcesso === TipoProcesso.REVISAO
      && data.detalhes.situacao === SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
      && !houveAlteracaoCadastro.value
  );
  unidade.value = data.unidade;
  codMapa.value = data.mapa.codigo;
}

async function executarAtualizacaoCadastro(
    acao: AcaoAtualizacaoCadastro,
    mensagemErro: string,
) {
  try {
    await withErrorHandling(async () => {
      const response = await acao();
      processarRespostaLocal(response);
    });
    return true;
  } catch {
    notify(mensagemErro, "danger");
    return false;
  }
}

async function carregarContextoInicial() {
  const data = typeof props.codSubprocesso === "number"
      ? await subprocessoStore.garantirContextoCadastroAtividades(props.codSubprocesso, false)
      : await subprocessoStore.garantirContextoCadastroAtividadesPorProcessoEUnidade(
          Number(props.codProcesso),
          props.sigla,
          false,
      );
  if (!data) {
    logger.error("ERRO: Subprocesso não encontrado!");
    return;
  }

  codSubprocesso.value = data.detalhes.codigo;
  sincronizarEstadoInicialContexto(data);
}

async function adicionarAtividade(): Promise<boolean> {
  if (codMapa.value && codSubprocesso.value) {
    try {
      const response = await withErrorHandling(() => adicionarAtividadeAction(codSubprocesso.value!, codMapa.value!));
      if (response) {
        processarRespostaLocal(response);
        erroNovaAtividade.value = null;
        return true;
      }
      return false;
    } catch {
      erroNovaAtividade.value = lastError.value?.message || TEXTOS.atividades.ERRO_ADICIONAR;
      return false;
    }
  }
  return false;
}

function removerAtividade(idx: number) {
  if (!codSubprocesso.value) return;
  dadosRemocao.value = {tipo: "atividade", index: idx};
  mostrarModalConfirmacaoRemocao.value = true;
}

async function confirmarRemocao() {
  if (!dadosRemocao.value || !codSubprocesso.value || loadingRemocao.value) return;

  const {tipo, index, conhecimentoCodigo} = dadosRemocao.value;

  loadingRemocao.value = true;
  try {
    if (tipo === "atividade") {
      const atividadeRemovida = atividades.value[index];
      await withErrorHandling(async () => {
        const response = await atividadeService.excluirAtividade(atividadeRemovida.codigo);
        processarRespostaLocal(response);
      });
    } else if (tipo === "conhecimento" && conhecimentoCodigo !== undefined) {
      const atividade = atividades.value[index];
      await withErrorHandling(async () => {
        const response = await atividadeService.excluirConhecimento(atividade.codigo, conhecimentoCodigo);
        processarRespostaLocal(response);
      });
    }
    mostrarModalConfirmacaoRemocao.value = false;
    dadosRemocao.value = null;
  } catch (e: unknown) {
    const err = lastError.value?.message || (e as Error).message;
    notify(err || TEXTOS.atividades.ERRO_REMOVER, 'danger');
    mostrarModalConfirmacaoRemocao.value = false;
  } finally {
    loadingRemocao.value = false;
  }
}

async function salvarEdicaoAtividade(codigo: number, descricao: string) {
  if (descricao.trim() && codSubprocesso.value) {
    const atividadeOriginal = atividades.value.find((a) => a.codigo === codigo);
    if (atividadeOriginal) {
      const descricaoAtualizada = descricao.trim();
      await executarAtualizacaoCadastro(
          () => atividadeService.atualizarAtividade(codigo, {
            ...atividadeOriginal,
            descricao: descricaoAtualizada,
          }),
          TEXTOS.atividades.ERRO_SALVAR_ATIVIDADE,
      );
    }
  }
}

async function adicionarConhecimento(idx: number, descricao: string) {
  if (!codSubprocesso.value) return;
  const atividade = atividades.value[idx];
  if (descricao.trim()) {
    const request: CriarConhecimentoRequest = {
      descricao: descricao.trim(),
    };
    await executarAtualizacaoCadastro(
        () => atividadeService.criarConhecimento(atividade.codigo, request),
        TEXTOS.atividades.ERRO_ADICIONAR_CONHECIMENTO,
    );
  }
}

function removerConhecimento(idx: number, conhecimentoCodigo?: number) {
  if (!codSubprocesso.value) return;
  dadosRemocao.value = {tipo: "conhecimento", index: idx, conhecimentoCodigo};
  mostrarModalConfirmacaoRemocao.value = true;
}

async function salvarEdicaoConhecimento(atividadeCodigo: number, conhecimentoCodigo: number, descricao: string) {
  if (!codSubprocesso.value) return;

  if (descricao.trim()) {
    const descricaoAtualizada = descricao.trim();
    await executarAtualizacaoCadastro(
        () => atividadeService.atualizarConhecimento(
            atividadeCodigo,
            conhecimentoCodigo,
            {
              codigo: conhecimentoCodigo,
              descricao: descricaoAtualizada,
            },
        ),
        TEXTOS.atividades.ERRO_ATUALIZAR_CONHECIMENTO,
    );
  }
}

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

  const situacaoAtualCadastro = subprocesso.value?.situacao;
  const situacaoReferencia = obterSituacaoReferenciaDisponibilizacao();
  const erroPreValidacao = obterErroPreValidacaoDisponibilizacao();

  if (erroPreValidacao) {
    limparErrosValidacaoCadastro();
    erroGlobal.value = erroPreValidacao;
    return;
  }

  if (!situacaoAtualCadastro || situacaoAtualCadastro !== situacaoReferencia) {
    notify(TEXTOS.comum.ACAO_NAO_PERMITIDA_SITUACAO(formatSituacaoSubprocesso(situacaoReferencia)), 'danger');
    return;
  }

  if (codSubprocesso.value) {
    loadingValidacao.value = true;
    limparErrosValidacaoCadastro();
    try {
      const resultado = await fluxoSubprocesso.validarCadastro(codSubprocesso.value);
      if (resultado) {
        aplicarResultadoValidacaoCadastro(resultado.valido, resultado.erros);
        if (!resultado.valido) {
          await nextTick();
          scrollParaPrimeiroErro();
          timeoutLimpezaErros();
        }
      }
    } catch {
      // O withErrorHandling já notificou o erro se necessário ou ele será exibido via erroGlobal
    } finally {
      loadingValidacao.value = false;
    }
  }
}

async function confirmarDisponibilizacao() {
  if (!codSubprocesso.value || loadingDisponibilizacao.value) return;

  loadingDisponibilizacao.value = true;
  try {
    await fluxoSubprocesso.disponibilizarCadastro(codSubprocesso.value, isRevisao.value);
  } finally {
    loadingDisponibilizacao.value = false;
  }
  mostrarModalConfirmacao.value = false;
}

async function abrirModalHistorico() {
  if (codSubprocesso.value) {
    analisesCadastro.value = await listarAnalisesCadastro(codSubprocesso.value);
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
  if (!codSubprocesso.value) return;

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

      sucesso = await fluxoSubprocesso.homologarCadastro(codSubprocesso.value, req, isRevisao.value, {
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
    sucesso = await fluxoSubprocesso.aceitarCadastro(codSubprocesso.value, req, isRevisao.value, {
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

  if (!codSubprocesso.value) return;

  loadingDevolucaoAnalise.value = true;
  try {
    const req: DevolverCadastroRequest = {observacoes: observacaoDevolucao.value};
    const sucesso = await fluxoSubprocesso.devolverCadastro(codSubprocesso.value, req, isRevisao.value);

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
  try {
    await carregarContextoInicial();
  } finally {
    carregandoInicial.value = false;
  }
});

watch(() => atividades.value?.length, (newLen, oldLen) => {
  if (newLen === 0 && oldLen === undefined) {
    nextTick(() => atividadeFormRef.value?.inputRef?.$el?.focus());
  }
}, {immediate: true});
</script>
