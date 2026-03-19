<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.atividades.TITULO">
      <template #default>
        <div class="d-flex align-items-center gap-2">
          <BBadge
              v-if="subprocesso"
              :variant="badgeVariant(subprocesso.situacao)"
              class="fs-6"
              data-testid="cad-atividades__txt-badge-situacao"
          >{{ formatSituacaoSubprocesso(subprocesso.situacao) }}
          </BBadge>
        </div>
      </template>
      <template #actions>
        <BButton
            v-if="codSubprocesso && podeVerImpacto"
            data-testid="cad-atividades__btn-impactos-mapa-edicao"
            variant="outline-secondary"
            @click="abrirModalImpacto"
        >
          <i aria-hidden="true" class="bi bi-arrow-right-circle me-1"/> {{ TEXTOS.atividades.BOTAO_IMPACTO }}
        </BButton>
        <BButton
            v-if="codSubprocesso && isChefe"
            :disabled="!habilitarEditarCadastro"
            data-testid="btn-cad-atividades-historico"
            variant="outline-secondary"
            @click="abrirModalHistorico"
        >
          <i aria-hidden="true" class="bi bi-clock-history me-1"/> {{ TEXTOS.atividades.BOTAO_HISTORICO_ANALISE }}
        </BButton>
        <BButton
            v-if="codSubprocesso && isChefe"
            :disabled="!habilitarEditarCadastro"
            data-testid="btn-cad-atividades-importar"
            variant="outline-secondary"
            @click="mostrarModalImportar = true"
        >
          <i aria-hidden="true" class="bi bi-upload me-1"/> {{ TEXTOS.atividades.BOTAO_IMPORTAR }}
        </BButton>

        <LoadingButton
            v-if="isChefe"
            :disabled="!habilitarDisponibilizarCadastro || !habilitarDisponibilizar"
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
    >
    </EmptyState>

    <div
        v-for="(atividade, idx) in atividades"
        :key="atividade.codigo || idx"
        :ref="el => setAtividadeRef(atividade.codigo, el)"
    >
      <AtividadeItem
          :atividade="atividade"
          :erro-validacao="obterErroParaAtividade(atividade.codigo)"
          :pode-editar="podeEditarCadastro"
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
        :mensagem="dadosRemocao?.tipo === 'atividade' ? TEXTOS.atividades.MODAL_REMOVER_ATIVIDADE_TEXTO : TEXTOS.atividades.MODAL_REMOVER_CONHECIMENTO_TEXTO"
        :titulo="dadosRemocao?.tipo === 'atividade' ? TEXTOS.atividades.MODAL_REMOVER_ATIVIDADE_TITULO : TEXTOS.atividades.MODAL_REMOVER_CONHECIMENTO_TITULO"
        variant="danger"
        @confirmar="confirmarRemocao"
    />

  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BBadge, BButton} from "bootstrap-vue-next";
import AppAlert from "@/components/comum/AppAlert.vue";
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import {storeToRefs} from "pinia";
import {badgeClass} from "@/utils";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/atividades/ImportarAtividadesModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/mapa/ConfirmacaoDisponibilizacaoModal.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CadAtividadeForm from "@/components/atividades/CadAtividadeForm.vue";
import AtividadeItem from "@/components/atividades/AtividadeItem.vue";
import {useAtividadeForm} from "@/composables/useAtividadeForm";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useMapasStore} from "@/stores/mapas";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import {usePerfil} from "@/composables/usePerfil";
import {useAcesso} from "@/composables/useAcesso";
import {
  type Analise,
  type Atividade,
  type Conhecimento,
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

function badgeVariant(situacao: SituacaoSubprocesso): any {
  const cls = badgeClass(situacao);
  const match = cls.match(/bg-(\w+)/);
  return match ? match[1] : 'secondary';
}

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
}>();

const router = useRouter();
const subprocessosStore = useSubprocessosStore();
const mapasStore = useMapasStore();
const {notify, notificacao, clear} = useNotification();
const toastStore = useToastStore();
const {impactoMapa: impactos} = storeToRefs(mapasStore);

const perfil = usePerfil();
const isChefe = computed(() => perfil.isChefe.value);
const codSubprocesso = ref<number | null>(null);

const codMapa = computed(() => mapasStore.mapaCompleto?.codigo || null);
const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
const unidade = ref<Unidade | null>(null);
const {
  podeEditarCadastro,
  podeVisualizarImpacto,
  habilitarEditarCadastro,
  habilitarDisponibilizarCadastro
} = useAcesso(subprocesso);
const isRevisao = computed(() => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO);
const podeVerImpacto = computed(() => podeVisualizarImpacto.value);

const atividades = ref<Atividade[]>([]);

const habilitarDisponibilizar = computed(() => {
  if (atividades.value.length === 0) return false;
  return atividades.value.every(a => a.conhecimentos && a.conhecimentos.length > 0);
});

const analisesCadastro = ref<Analise[]>([]);

const {withErrorHandling, lastError} = useErrorHandler();

const historicoAnalises = computed(() => {
  return analisesCadastro.value || [];
});

const {novaAtividade, loadingAdicionar, adicionarAtividade: adicionarAtividadeAction} = useAtividadeForm();
const erroNovaAtividade = ref<string | null>(null);

const mostrarModalImpacto = ref(false);
const mostrarModalImportar = ref(false);
const mostrarModalConfirmacao = ref(false);
const mostrarModalHistorico = ref(false);
const mostrarModalConfirmacaoRemocao = ref(false);
const dadosRemocao = ref<DadosRemocao>(null);
const loadingImpacto = ref(false);

const loadingValidacao = ref(false);
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

function processarRespostaLocal(response: any) {
  if (response?.atividadesAtualizadas) {
    atividades.value = response.atividadesAtualizadas;
  }
  if (response?.subprocesso) {
    subprocessosStore.atualizarStatusLocal({
      ...response.subprocesso,
      permissoes: response.permissoes
    });
  }
}

function sincronizarEstadoInicialContexto(data: any) {
  const subprocessoContexto = data?.detalhes?.subprocesso ?? data?.subprocesso;
  const permissoesContexto = data?.detalhes?.permissoes ?? data?.permissoes;

  if (!subprocessoContexto || !permissoesContexto) {
    return;
  }

  processarRespostaLocal({
    subprocesso: subprocessoContexto,
    permissoes: permissoesContexto
  });
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
  if (!dadosRemocao.value || !codSubprocesso.value) return;

  const {tipo, index, conhecimentoCodigo} = dadosRemocao.value;

  try {
    await withErrorHandling(async () => {
      if (tipo === "atividade") {
        const atividadeRemovida = atividades.value[index];
        const response = await atividadeService.excluirAtividade(atividadeRemovida.codigo);
        processarRespostaLocal(response);
      } else if (tipo === "conhecimento" && conhecimentoCodigo !== undefined) {
        const atividade = atividades.value[index];
        const response = await atividadeService.excluirConhecimento(atividade.codigo, conhecimentoCodigo);
        processarRespostaLocal(response);
      }
    });
    mostrarModalConfirmacaoRemocao.value = false;
    dadosRemocao.value = null;
  } catch (e: any) {
    const err = lastError.value?.message || e.message;
    notify(err || TEXTOS.atividades.ERRO_REMOVER, 'danger');
    mostrarModalConfirmacaoRemocao.value = false;
  }
}

async function salvarEdicaoAtividade(codigo: number, descricao: string) {
  if (descricao.trim() && codSubprocesso.value) {
    const atividadeOriginal = atividades.value.find((a) => a.codigo === codigo);
    if (atividadeOriginal) {
      const atividadeAtualizada: Atividade = {
        ...atividadeOriginal,
        descricao: descricao.trim(),
      };
      try {
        await withErrorHandling(async () => {
          const response = await atividadeService.atualizarAtividade(codigo, atividadeAtualizada);
          processarRespostaLocal(response);
        });
      } catch {
        notify(TEXTOS.atividades.ERRO_SALVAR_ATIVIDADE, "danger");
      }
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
    try {
      await withErrorHandling(async () => {
        const response = await atividadeService.criarConhecimento(atividade.codigo, request);
        processarRespostaLocal(response);
      });
    } catch {
      notify(TEXTOS.atividades.ERRO_ADICIONAR_CONHECIMENTO, "danger");
    }
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
    const conhecimentoAtualizado: Conhecimento = {
      codigo: conhecimentoCodigo,
      descricao: descricao.trim(),
    };
    try {
      await withErrorHandling(async () => {
        const response = await atividadeService.atualizarConhecimento(
            atividadeCodigo,
            conhecimentoCodigo,
            conhecimentoAtualizado,
        );
        processarRespostaLocal(response);
      });
    } catch {
      notify(TEXTOS.atividades.ERRO_ATUALIZAR_CONHECIMENTO, "danger");
    }
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
      const data = await subprocessosStore.buscarContextoEdicao(codigoSubprocesso);
      if (data) {
        sincronizarEstadoInicialContexto(data);
        atividades.value = data.atividadesDisponiveis ?? [];
        if (data.unidade) {
          unidade.value = data.unidade as Unidade;
        }
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
  const situacaoEsperada = isRevisao.value
      ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
      : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

  if (subprocesso.value?.situacao !== situacaoEsperada) {
    notify(TEXTOS.comum.ACAO_NAO_PERMITIDA_SITUACAO(formatSituacaoSubprocesso(situacaoEsperada)), 'danger');
    return;
  }

  if (codSubprocesso.value) {
    loadingValidacao.value = true;
    errosValidacao.value = [];
    erroGlobal.value = null;
    try {
      const resultado = await subprocessosStore.validarCadastro(codSubprocesso.value);
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
  if (!codSubprocesso.value) return;

  let sucesso: boolean;
  if (isRevisao.value) {
    sucesso = await subprocessosStore.disponibilizarRevisaoCadastro(codSubprocesso.value);
  } else {
    sucesso = await subprocessosStore.disponibilizarCadastro(codSubprocesso.value);
  }

  mostrarModalConfirmacao.value = false;
  if (sucesso) {
    await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value);
    const msg = isRevisao.value
        ? TEXTOS.sucesso.REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA
        : TEXTOS.sucesso.CADASTRO_ATIVIDADES_DISPONIBILIZADO;
    toastStore.setPending(msg);
    await router.push("/painel");
  }
}

async function abrirModalHistorico() {
  if (codSubprocesso.value) {
    analisesCadastro.value = await listarAnalisesCadastro(codSubprocesso.value);
  }
  mostrarModalHistorico.value = true;
}

function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
  if (codSubprocesso.value) {
    loadingImpacto.value = true;
    mapasStore.buscarImpactoMapa(codSubprocesso.value).finally(() => (loadingImpacto.value = false));
  }
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}

async function handleAdicionarAtividade() {
  const sucesso = await adicionarAtividade();
  await nextTick();
  if (sucesso || erroNovaAtividade.value) atividadeFormRef.value?.inputRef?.$el?.focus();
}

onMounted(async () => {
  const codProcessoRef = Number(props.codProcesso);
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(codProcessoRef, props.sigla);

  if (id) {
    codSubprocesso.value = id;
    const data = await subprocessosStore.buscarContextoEdicao(id);
    if (data) {
      sincronizarEstadoInicialContexto(data);
      if (data.atividadesDisponiveis) {
        atividades.value = data.atividadesDisponiveis;
      }
      if (data.unidade) {
        unidade.value = data.unidade as Unidade;
      }
    }
  } else {
    logger.error("ERRO: Subprocesso não encontrado!");
  }
});

watch(() => atividades.value?.length, (newLen, oldLen) => {
  if (newLen === 0 && oldLen === undefined) {
    nextTick(() => atividadeFormRef.value?.inputRef?.$el?.focus());
  }
}, {immediate: true});
</script>
