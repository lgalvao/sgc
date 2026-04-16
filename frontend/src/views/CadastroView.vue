<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial" />
    <template v-else>
      <PageHeader :title="TEXTOS.atividades.TITULO">
      <template #default>
        <span v-if="unidade" class="fw-bold" data-testid="subprocesso-header__txt-header-unidade">{{ unidade.sigla }}</span>
      </template>
      <template #actions>
        <BButton
            v-if="codSubprocesso && podeVisualizarImpacto"
            data-testid="cad-atividades__btn-impactos-mapa-edicao"
            variant="outline-secondary"
            @click="abrirModalImpacto"
        >
          <i aria-hidden="true" class="bi bi-arrow-right-circle me-1"/> {{ TEXTOS.atividades.BOTAO_IMPACTO }}
        </BButton>
        <BButton
            v-if="codSubprocesso && podeEditarCadastro"
            :disabled="!habilitarEditarCadastro"
            data-testid="btn-cad-atividades-historico"
            variant="outline-secondary"
            @click="abrirModalHistorico"
        >
          <i aria-hidden="true" class="bi bi-clock-history me-1"/> {{ TEXTOS.atividades.BOTAO_HISTORICO_ANALISE }}
        </BButton>
        <BButton
            v-if="codSubprocesso && podeEditarCadastro"
            :disabled="!habilitarEditarCadastro"
            data-testid="btn-cad-atividades-importar"
            variant="outline-secondary"
            @click="mostrarModalImportar = true"
        >
          <i aria-hidden="true" class="bi bi-arrow-down-circle me-1"/> {{ TEXTOS.atividades.BOTAO_IMPORTAR }}
        </BButton>
        <LoadingButton
            v-if="codSubprocesso && (podeDisponibilizarCadastro || podeEditarCadastro)"
            :disabled="botaoDisponibilizarDesabilitado"
            :loading="loadingValidacao"
            data-testid="btn-cad-atividades-disponibilizar"
            icon="check-lg"
            :loading-text="TEXTOS.atividades.BOTAO_DISPONIBILIZANDO"
            :text="TEXTOS.atividades.BOTAO_DISPONIBILIZAR"
            variant="success"
            @click="disponibilizarCadastro"
        />
      </template>
    </PageHeader>

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
        :model-value="true"
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
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormCheckbox, BSpinner} from "bootstrap-vue-next";
import AppAlert from "@/components/comum/AppAlert.vue";
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/atividades/ImportarAtividadesModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/mapa/ConfirmacaoDisponibilizacaoModal.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CadAtividadeForm from "@/components/atividades/CadAtividadeForm.vue";
import AtividadeItem from "@/components/atividades/AtividadeItem.vue";
import {useAtividadeForm} from "@/composables/useAtividadeForm";
import {useFluxoSubprocesso} from "@/composables/useFluxoSubprocesso";
import {useImpactoMapaModal} from "@/composables/useImpactoMapaModal";
import {useSubprocessos} from "@/composables/useSubprocessos";
import {useMapas} from "@/composables/useMapas";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {useAcesso} from "@/composables/useAcesso";
import {
  type Analise,
  type Atividade,
  type AtividadeOperacaoResponse,
  type ContextoCadastroAtividadesSubprocesso,
  type CriarConhecimentoRequest,
  type ErroValidacao,
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
}>();

const router = useRouter();
const route = useRoute();
const subprocessosStore = useSubprocessos();
const mapasStore = useMapas();
const fluxoSubprocesso = useFluxoSubprocesso();
const {notify, notificacao, clear} = useNotification();
const toastStore = useToastStore();
const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();
const {impactoMapa: impactos} = mapasStore;
const codSubprocesso = ref<number | null>(null);
const codMapa = ref<number | null>(null);
const carregandoInicial = ref(true);
const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
const unidade = ref<Unidade | null>(null);
const acesso = useAcesso(subprocesso);
const {podeEditarCadastro, podeVisualizarImpacto, habilitarEditarCadastro} = acesso;
const podeDisponibilizarCadastro = computed(() => acesso.podeDisponibilizarCadastro?.value ?? false);
const habilitarDisponibilizarCadastro = computed(() => acesso.habilitarDisponibilizarCadastro?.value ?? false);
const isRevisao = computed(() => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO);


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

const houveAlteracaoCadastro = computed(() => calcularAssinaturaCadastro(atividades.value) !== atividadesSnapshotInicial.value);
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

const botaoDisponibilizarDesabilitado = computed(() => {
  if (isRevisao.value) {
    return !habilitarDisponibilizar.value;
  }
  return !habilitarDisponibilizarCadastro.value || !habilitarDisponibilizar.value;
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

const mostrarModalImportar = ref(false);
const mostrarModalConfirmacao = ref(false);
const mostrarModalHistorico = ref(false);
const mostrarModalConfirmacaoRemocao = ref(false);
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
const errosValidacao = ref<ErroValidacao[]>([]);
const erroGlobal = ref<string | null>(null);
const atividadeRefs = new Map<number, Element>();
let timeoutLimparErros: ReturnType<typeof setTimeout> | null = null;

function timeoutLimpezaErros() {
  if (timeoutLimparErros) clearTimeout(timeoutLimparErros);
  timeoutLimparErros = setTimeout(() => {
    errosValidacao.value = [];
    erroGlobal.value = null;
  }, 6000);
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
  subprocessosStore.atualizarStatusLocal({
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
  const codProcessoRef = Number(props.codProcesso);
  const codigoQuery = Number(route.query.codSubprocesso);
  const data = Number.isFinite(codigoQuery) && codigoQuery > 0
      ? await subprocessosStore.buscarContextoCadastroAtividades(codigoQuery)
      : await subprocessosStore.buscarContextoCadastroAtividadesPorProcessoEUnidade(codProcessoRef, props.sigla);
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

async function handleImportAtividades(aviso?: string) {
  mostrarModalImportar.value = false;
  clear();
  await nextTick();
  if (aviso) {
    notify(TEXTOS.atividades.AVISO_IMPORTACAO_DUPLICATAS, 'warning');
  } else {
    notify(TEXTOS.atividades.SUCESSO_IMPORTACAO, 'success');
  }
  const codigoSubprocesso = codSubprocesso.value;
  if (codigoSubprocesso !== null) {
    await withErrorHandling(async () => {
      const data = await subprocessosStore.buscarContextoCadastroAtividades(codigoSubprocesso);
      if (data) {
        sincronizarEstadoInicialContexto(data);
      }
    });
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
  const situacaoAtualCadastro = subprocesso.value?.situacao;
  const situacaoReferencia = isRevisao.value
      ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
      : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

  if (!situacaoAtualCadastro || situacaoAtualCadastro !== situacaoReferencia) {
    notify(TEXTOS.comum.ACAO_NAO_PERMITIDA_SITUACAO(formatSituacaoSubprocesso(situacaoReferencia)), 'danger');
    return;
  }

  if (codSubprocesso.value) {
    loadingValidacao.value = true;
    errosValidacao.value = [];
    erroGlobal.value = null;
    try {
      const resultado = await fluxoSubprocesso.validarCadastro(codSubprocesso.value);
      if (resultado?.valido) {
        mostrarModalConfirmacao.value = true;
      } else if (resultado) {
        errosValidacao.value = resultado.erros;

        const erroSemAtividade = resultado.erros.find((e) => !e.atividadeCodigo);
        if (erroSemAtividade) {
          erroGlobal.value = erroSemAtividade.mensagem;
        }

        await nextTick();
        scrollParaPrimeiroErro();
        timeoutLimpezaErros();
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

  let sucesso: boolean;
  loadingDisponibilizacao.value = true;
  try {
    if (isRevisao.value) {
      sucesso = await fluxoSubprocesso.disponibilizarRevisaoCadastro(codSubprocesso.value);
    } else {
      sucesso = await fluxoSubprocesso.disponibilizarCadastro(codSubprocesso.value);
    }
  } finally {
    loadingDisponibilizacao.value = false;
  }

  mostrarModalConfirmacao.value = false;
  if (sucesso) {
    const msg = isRevisao.value
        ? TEXTOS.sucesso.REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA
        : TEXTOS.sucesso.CADASTRO_ATIVIDADES_DISPONIBILIZADO;
    toastStore.setPending(msg);
    invalidarCachesSubprocesso();
    await router.push("/painel");
  }
}

async function abrirModalHistorico() {
  if (codSubprocesso.value) {
    analisesCadastro.value = await listarAnalisesCadastro(codSubprocesso.value);
  }
  mostrarModalHistorico.value = true;
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
