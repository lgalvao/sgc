<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial"/>
    <template v-else>
      <CadastroAcoesHeader
          :acao-principal-cadastro="acaoPrincipalCadastro"
          :cod-subprocesso="codigoSubprocesso"
          :loading-validacao="loadingValidacao"
          :mostrar-devolver-cadastro="mostrarDevolverCadastro"
          :mostrar-disponibilizar-cadastro="mostrarDisponibilizarCadastro"
          :mostrar-importar-atividades="mostrarImportarAtividades"
          :permissoes="permissoesUI"
          :pode-visualizar-impacto="podeVisualizarImpacto"
          :unidade="unidade"
          @disponibilizar="disponibilizarCadastro"
          @abrir-historico="abrirModalHistorico"
          @abrir-devolver="abrirModalDevolverAnalise"
          @abrir-validar="abrirModalValidarAnalise"
          @abrir-impacto="abrirModalImpacto"
          @abrir-importar="mostrarModalImportar = true"
      />

      <div v-if="mostrarControlesEdicaoCadastro && isRevisao" class="mt-3 mb-2">
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
          <BSpinner small/>
        </div>
      </div>

      <BAlert
          v-if="erroGlobalFormatado"
          :key="erroTick"
          :model-value="true"
          data-testid="alerta-erro-global"
          dismissible
          no-fade
          show
          variant="danger"
          @dismissed="erroGlobal = null"
      >
        {{ erroGlobalFormatado.mensagem }}
      </BAlert>

      <AppAlert
          v-if="notificacao"
          :dispensavel="notificacao.dispensavel"
          :mensagem="notificacao.mensagem"
          :variante="notificacao.variante"
          @dismissed="clear()"
      />

      <CadAtividadeForm
          v-if="mostrarControlesEdicaoCadastro"
          ref="atividadeFormRef"
          v-model="novaAtividade"
          :disabled="!codigoSubprocesso || !habilitarEditarCadastro"
          :erro="erroNovaAtividade"
          :loading="loadingAdicionar"
          @submit="handleAdicionarAtividade"
      />

      <EmptyState
          v-if="atividades?.length === 0"
          :description="mostrarControlesEdicaoCadastro ? TEXTOS.atividades.EMPTY_DESCRIPTION : TEXTOS.treeTable.EMPTY_DESCRIPTION"
          :title="mostrarControlesEdicaoCadastro ? TEXTOS.atividades.EMPTY_TITLE : TEXTOS.treeTable.EMPTY_TITLE"
          data-testid="cad-atividades-empty-state"
          icon="bi-list-check"
      />

      <div
          v-for="atividade in atividadesOrdenadas"
          :key="atividade.codigo"
          :ref="el => setAtividadeRef(atividade.codigo, el)"
      >
        <AtividadeItem
            :atividade="atividade"
            :erro-validacao="obterErroParaAtividade(atividade.codigo)"
            :habilitar-edicao="habilitarEditarCadastro"
            :pode-editar="mostrarControlesEdicaoCadastro"
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
          :erro-fluxo="erroFluxoCadastro"
          :historico-analises="historicoAnalises"
          :impactos="impactos"
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
          :erro-observacao-devolucao="mensagemErroObservacaoDevolucao"
          :observacao-validacao="observacaoValidacao"
          @importar="handleImportAtividades"
          @confirmar-devolucao-analise="confirmarDevolucaoAnalise"
          @confirmar-disponibilizacao="confirmarDisponibilizacao"
          @confirmar-remocao="confirmarRemocao"
          @confirmar-validacao-analise="confirmarValidacaoAnalise"
          @fechar-impacto="fecharModalImpacto"
          @update:mostrar-modal-confirmacao="mostrarModalConfirmacao = $event"
          @update:mostrar-modal-confirmacao-remocao="mostrarModalConfirmacaoRemocao = $event"
          @update:mostrar-modal-devolver-analise="mostrarModalDevolverAnalise = $event"
          @update:mostrar-modal-historico="mostrarModalHistorico = $event"
          @update:mostrar-modal-importar="mostrarModalImportar = $event"
          @update:mostrar-modal-validar-analise="mostrarModalValidarAnalise = $event"
          @update:observacao-devolucao="observacaoDevolucao = $event"
          @update:observacao-validacao="observacaoValidacao = $event"
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
import {usePerfilStore} from "@/stores/perfil";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useAcesso} from "@/composables/acesso";
import {useCadastroAtividadesMutacoes} from "@/composables/useCadastroAtividadesMutacoes";
import {useCadastroRevisaoSemMudancas} from "@/composables/useCadastroRevisaoSemMudancas";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useCadastroOrquestracao} from "@/composables/useCadastroOrquestracao";
import {useCadastroAnaliseFluxo} from "@/views/cadastroAnaliseFluxo";
import {useCadastroDisponibilizacao} from "@/views/cadastroDisponibilizacao";
import {
  type Atividade,
  type AtividadeOperacaoResponse,
  Perfil,
  type PermissoesSubprocesso,
  TipoProcesso
} from "@/types/tipos";
import {calcularAssinaturaCadastro} from "@/utils/formatters";
import {normalizarPermissoesSubprocesso} from "@/utils/permissoesSubprocesso";
import {listarAnalisesCadastro} from "@/services/analiseService";
import {TEXTOS} from "@/constants/textos";
import {extrairTextoPlanoHtml} from "@/utils/textoFormatado";

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
  codSubprocesso?: number;
}>();

const atividades = ref<Atividade[]>([]);

const orquestracao = useCadastroOrquestracao(props, atividades);
const carregandoInicial = orquestracao.carregandoInicial;
const codigoSubprocesso = orquestracao.codigoSubprocesso;
const atividadesSnapshotInicial = orquestracao.atividadesSnapshotInicial;
const unidade = orquestracao.unidade;
const codMapa = orquestracao.codMapa;
const carregarContextoInicial = orquestracao.carregarContextoInicial;
const processarRespostaLocal = orquestracao.processarRespostaLocal;

const subprocessoStore = useSubprocessoStore();
const perfilStore = usePerfilStore();
const mapasStore = useMapas(codigoSubprocesso);
const fluxoSubprocesso = useFluxoSubprocesso();
const {notify, notificacao, clear} = useNotification();
const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
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
const esconderEdicaoCadastroParaChefe = computed(() =>
    perfilStore.perfilSelecionado === Perfil.CHEFE
    && podeEditarCadastro.value
    && !habilitarEditarCadastro.value
);
const mostrarControlesEdicaoCadastro = computed(() => podeEditarCadastro.value && !esconderEdicaoCadastroParaChefe.value);

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


const situacaoAtual = computed(() => subprocesso.value?.situacao);
const {
  disponibilizacaoSemMudancas,
  checkboxSemMudancasDesabilitado,
  loadingInicioRevisao,
  sincronizarDisponibilizacaoSemMudancasInicial
} = useCadastroRevisaoSemMudancas({
  codigoSubprocesso,
  isRevisao,
  situacaoAtual,
  houveAlteracaoCadastro,
  fluxoSubprocesso
});

const {executarComTratamentoDeErros, ultimoErro} = useErrorHandler();

const {novaAtividade, loadingAdicionar, adicionarAtividade: adicionarAtividadeAction} = useAtividadeForm();

watch(novaAtividade, (valorAtual, valorAnterior) => {
  if (valorAtual !== valorAnterior && erroNovaAtividade.value) {
    erroNovaAtividade.value = null;
  }
});

watch(assinaturaCadastroAtual, (valorAtual, valorAnterior) => {
  if (valorAtual !== valorAnterior && (errosValidacao.value.length > 0 || erroGlobal.value)) {
    limparErrosValidacao();
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
} = useImpactoMapaModal(codigoSubprocesso, (codigo) => mapasStore.carregarImpacto(codigo));

const atividadeRefs = new Map<number, Element>();
const atividadeFormRef = ref<InstanceType<typeof CadAtividadeForm> | null>(null);

const {
  erroNovaAtividade,
  dadosRemocao,
  loadingRemocao,
  mostrarModalConfirmacaoRemocao,
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
  executarComTratamentoDeErros,
  ultimoErro,
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

const {
  erroGlobal,
  erroTick,
  errosValidacao,
  loadingValidacao,
  loadingDisponibilizacao,
  limparErrosValidacao,
  disponibilizarCadastro,
  confirmarDisponibilizacao,
  obterErroParaAtividade,
} = useCadastroDisponibilizacao({
  atividades,
  codigoSubprocesso,
  situacaoAtual,
  isRevisao,
  houveAlteracaoCadastro,
  disponibilizacaoSemMudancas,
  mostrarModalConfirmacao,
  scrollParaPrimeiroErro,
  validarCadastro: fluxoSubprocesso.validarCadastro,
  disponibilizarCadastroFluxo: fluxoSubprocesso.disponibilizarCadastro,
  disponibilizarRevisaoCadastroFluxo: fluxoSubprocesso.disponibilizarRevisaoCadastro,
});

const erroGlobalFormatado = computed(() =>
    erroGlobal.value ? {mensagem: erroGlobal.value} : null
);
const erroCampoObservacaoDevolucao = computed(() => {
  const erros = fluxoSubprocesso.ultimoErro.value?.erros;
  if (!erros) return "";
  return erros.find((erro) =>
      ["justificativa", "texto", "observacoes"].includes(erro.campo || "")
  )?.mensagem || "";
});
const erroFluxoCadastro = computed(() =>
    fluxoSubprocesso.ultimoErro.value?.tipo === "validacao"
        ? undefined
        : fluxoSubprocesso.ultimoErro.value?.mensagem
);
const mensagemErroObservacaoDevolucao = computed(() =>
    erroCampoObservacaoDevolucao.value
        || (deveExibirErro(!extrairTextoPlanoHtml(observacaoDevolucao.value))
            ? TEXTOS.atividades.ERRO_DEVOLUCAO_JUSTIFICATIVA
            : "")
);

const {
  historicoAnalises,
  loadingAnaliseCadastro,
  loadingDevolucaoAnalise,
  observacaoValidacao,
  observacaoDevolucao,
  abrirModalHistorico,
  abrirModalValidarAnalise,
  abrirModalDevolverAnalise,
  confirmarValidacaoAnalise,
  confirmarDevolucaoAnalise,
} = useCadastroAnaliseFluxo({
  codigoSubprocesso,
  codProcesso: props.codProcesso,
  sigla: props.sigla,
  isRevisao,
  acaoPrincipalCadastro,
  mostrarModalHistorico,
  mostrarModalValidarAnalise,
  mostrarModalDevolverAnalise,
  resetarValidacao,
  validarSubmissao,
  focarPrimeiroErroInvalido,
  listarAnalisesCadastro,
  homologarCadastro: fluxoSubprocesso.homologarCadastro,
  homologarRevisaoCadastro: fluxoSubprocesso.homologarRevisaoCadastro,
  aceitarCadastro: fluxoSubprocesso.aceitarCadastro,
  aceitarRevisaoCadastro: fluxoSubprocesso.aceitarRevisaoCadastro,
  devolverCadastro: fluxoSubprocesso.devolverCadastro,
  devolverRevisaoCadastro: fluxoSubprocesso.devolverRevisaoCadastro,
});

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
