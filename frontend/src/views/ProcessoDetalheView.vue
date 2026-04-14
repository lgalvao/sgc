<template>
  <LayoutPadrao>
    <AppAlert
        v-if="lastError"
        :message="lastError.message"
        variant="danger"
        @dismissed="clearError()"/>

    <AppAlert
        v-if="notificacao"
        :dismissible="notificacao.dismissible ?? true"
        :message="notificacao.message"
        :notification="notificacao.notification"
        :stack-trace="notificacao.stackTrace"
        :variant="notificacao.variant"
        @dismissed="clear()"/>

    <div v-if="processo">
      <PageHeader
          :title="processo.descricao"
          title-test-codigo="processo-info">

        <template #default>
          <ProcessoInfo
              :show-data-limite="false"
              :situacao="processo.situacao"
              :tipo="processo.tipo"/>
        </template>

        <template #actions>
          <BButton
              v-if="podeFinalizar"
              data-testid="btn-processo-finalizar"
              variant="danger"
              @click="finalizarProcesso"
          >
            {{ TEXTOS.processo.FINALIZAR }}
          </BButton>

          <BButton
              v-for="acao in acoesBlocoVisiveis"
              :id="obterIdBotaoAcao(acao.codigo)"
              :key="acao.codigo"
              :data-testid="obterTestIdBotaoAcao(acao.codigo)"
              :disabled="!acao.habilitar || processandoAcaoBloco"
              variant="success"
              @click="abrirModalBloco(acao)">
            {{ acao.rotulo }}
          </BButton>
        </template>
      </PageHeader>

      <ProcessoSubprocessosTable
          :participantes-hierarquia="participantesHierarquia"
          @row-click="abrirDetalhesUnidade"/>
    </div>

    <div v-else class="text-center py-5">
      <BSpinner :label="TEXTOS.processo.CARREGANDO_DETALHES" variant="primary"/>
      <p class="mt-2 text-muted">{{ TEXTOS.processo.CARREGANDO_DETALHES }}</p>
    </div>

    <!-- Modal de Ação em Bloco -->
    <ModalAcaoBloco
        :id="'modal-acao-bloco'"
        ref="modalBlocoRef"
        :mostrar-data-limite="acaoBlocoAtual?.requerDataLimite ?? false"
        :rotulo-botao="acaoBlocoAtual?.rotuloBotao ?? ''"
        :texto="acaoBlocoAtual?.texto ?? ''"
        :titulo="acaoBlocoAtual?.titulo ?? ''"
        :unidades="unidadesElegiveis"
        :unidades-pre-selecionadas="idsElegiveis"
        @confirmar="executarAcaoBloco"/>

    <ModalConfirmacao
        v-model="mostrarModalFinalizacao"
        :ok-title="TEXTOS.comum.BOTAO_FINALIZAR"
        test-codigo-cancelar="btn-finalizar-processo-cancelar"
        test-codigo-confirmar="btn-finalizar-processo-confirmar"
        :titulo="TEXTOS.processo.FINALIZACAO_TITULO"
        variant="danger"
        @confirmar="confirmarFinalizacao">
      <p class="mb-2">
        {{ TEXTOS.processo.FINALIZACAO_CONFIRMACAO_PREFIXO }}
        <strong>{{ processo?.descricao || '' }}</strong>?
      </p>
      <p class="mb-0">{{ TEXTOS.processo.FINALIZACAO_CONFIRMACAO_COMPLEMENTO }}</p>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BSpinner} from "bootstrap-vue-next";
import {computed, onActivated, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import ModalAcaoBloco from "@/components/processo/ModalAcaoBloco.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import ProcessoInfo from "@/components/processo/ProcessoInfo.vue";
import ProcessoSubprocessosTable from "@/components/processo/ProcessoSubprocessosTable.vue";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import {useProcessoStore} from "@/stores/processo";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import type {AcaoBlocoProcesso, Processo, SubprocessoElegivel} from "@/types/tipos";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {logger} from "@/utils";
import {type NormalizedError, normalizeError} from "@/utils/apiError";
import * as processoService from "@/services/processoService";
import {TEXTOS} from "@/constants/textos";

type ModalAcaoBlocoRef = {
  abrir: () => void;
  fechar: () => void;
  setProcessando: (valor: boolean) => void;
  setErro: (mensagem: string) => void;
};
type LinhaCliqueSubprocesso = {
  clickable?: boolean;
  sigla?: string;
  codSubprocesso?: number;
};

const route = useRoute();
const router = useRouter();
const {notificacao, notify, clear} = useNotification();
const toastStore = useToastStore();
const processoStore = useProcessoStore();
const {invalidarCachesProcesso, invalidarCachesSubprocesso} = useInvalidacaoNavegacao();
const codProcesso = Number(route.params.codProcesso || route.query.codProcesso);
const processo = ref<Processo | null>(null);
const lastError = ref<NormalizedError | null>(null);
const modalBlocoRef = ref<ModalAcaoBlocoRef | null>(null);
const mostrarModalFinalizacao = ref(false);
const acaoBlocoAtual = ref<AcaoBlocoProcesso | null>(null);
const processandoAcaoBloco = ref(false);
const carregamentoInicialConcluido = ref(false);

function clearError() {
  lastError.value = null;
}

async function carregarContextoCompleto() {
  clearError();
  processo.value = null;

  try {
    const data = await processoStore.garantirContextoCompleto(codProcesso);
    if (data) {
      processo.value = data;
    }
    return data;
  } catch (error) {
    lastError.value = normalizeError(error);
    throw error;
  }
}

const participantesHierarquia = computed(() => processo.value?.unidades || []);

const podeFinalizar = computed(() => {
  return processo.value?.podeFinalizar || false;
});

const acoesBlocoVisiveis = computed(() => (processo.value?.acoesBloco ?? []).filter(acao => acao.mostrar));

const unidadesElegiveis = computed(() => {
  const elegiveis = acaoBlocoAtual.value?.unidades ?? [];
  return elegiveis.map((u: SubprocessoElegivel) => ({
    codigo: u.unidadeCodigo,
    sigla: u.unidadeSigla,
    nome: u.unidadeNome,
    situacao: formatSituacaoSubprocesso(u.situacao),
    ultimaDataLimite: u.ultimaDataLimite
  }));
});

const idsElegiveis = computed(() => unidadesElegiveis.value.map(u => u.codigo));

async function abrirDetalhesUnidade(row: LinhaCliqueSubprocesso) {
  if (!row.clickable || !row.sigla) {
    return;
  }

  try {
    await router.push({
      name: "Subprocesso",
      params: {
        codProcesso: codProcesso.toString(),
        siglaUnidade: row.sigla
      },
      query: row.codSubprocesso
          ? {codSubprocesso: String(row.codSubprocesso)}
          : undefined,
    });
  } catch (error) {
    logger.error(`Erro ao navegar para detalhes da unidade ${row.sigla}:`, error);
  }
}

function finalizarProcesso() {
  mostrarModalFinalizacao.value = true;
}

function obterIdBotaoAcao(codigoAcao: string) {
  switch (codigoAcao) {
    case "aceitar-cadastro":
      return "btn-aceitar-bloco";
    case "aceitar-mapa":
      return "btn-aceitar-mapas-bloco";
    case "homologar-cadastro":
      return "btn-homologar-bloco";
    case "homologar-mapa":
      return "btn-homologar-mapas-bloco";
    case "disponibilizar-mapa":
      return "btn-disponibilizar-bloco";
    default:
      return `btn-${codigoAcao}`;
  }
}

function obterTestIdBotaoAcao(codigoAcao: string) {
  switch (codigoAcao) {
    case "aceitar-cadastro":
      return "btn-processo-aceitar-bloco";
    case "aceitar-mapa":
      return "btn-processo-aceitar-mapas-bloco";
    case "homologar-cadastro":
      return "btn-processo-homologar-bloco";
    case "homologar-mapa":
      return "btn-processo-homologar-mapas-bloco";
    case "disponibilizar-mapa":
      return "btn-processo-disponibilizar-bloco";
    default:
      return `btn-processo-${codigoAcao}`;
  }
}

async function confirmarFinalizacao() {
  try {
    clearError();
    await processoService.finalizarProcesso(codProcesso);
    toastStore.setPending(TEXTOS.sucesso.PROCESSO_FINALIZADO);
    invalidarCachesProcesso();
    await router.push("/painel");
  } catch (error) {
    lastError.value = normalizeError(error);
    const mensagem = lastError.value?.message || TEXTOS.processo.ERRO_PADRAO;
    notify(mensagem, 'danger');
  }
}

function abrirModalBloco(acao: AcaoBlocoProcesso) {
  acaoBlocoAtual.value = acao;
  modalBlocoRef.value?.abrir();
}

async function executarAcaoBloco(dados: { ids: number[], dataLimite?: string }) {
  try {
    clearError();
    processandoAcaoBloco.value = true;
    modalBlocoRef.value?.setProcessando(true);
    if (!processo.value) {
      modalBlocoRef.value?.setErro("Detalhes do processo não carregados.");
      processandoAcaoBloco.value = false;
      return;
    }
    if (!acaoBlocoAtual.value) {
      modalBlocoRef.value?.setErro(TEXTOS.processo.ERRO_ACAO_BLOCO);
      processandoAcaoBloco.value = false;
      return;
    }
    await processoService.executarAcaoEmBloco(processo.value.codigo, {
      unidadeCodigos: dados.ids,
      acao: acaoBlocoAtual.value.acao,
      dataLimite: dados.dataLimite,
    });

    modalBlocoRef.value?.fechar();
    const {mensagemSucesso, redirecionarPainel} = acaoBlocoAtual.value;

    if (redirecionarPainel) {
      toastStore.setPending(mensagemSucesso);
      invalidarCachesProcesso();
      await router.push("/painel");
      return;
    }
    notify(mensagemSucesso, 'success');
    invalidarCachesSubprocesso({incluirPainel: false});
    await carregarContextoCompleto();
  } catch (error) {
    lastError.value = normalizeError(error);
    modalBlocoRef.value?.setErro(lastError.value?.message || TEXTOS.processo.ERRO_ACAO_BLOCO);
    modalBlocoRef.value?.setProcessando(false);
  } finally {
    processandoAcaoBloco.value = false;
  }
}

onMounted(async () => {
  if (codProcesso) {
    try {
      await carregarContextoCompleto();
      carregamentoInicialConcluido.value = true;
    } catch {
      // O erro já foi convertido em estado local para exibição inline.
    }
  }
});

onActivated(async () => {
  if (!codProcesso || !carregamentoInicialConcluido.value) {
    return;
  }
  // Só recarrega se o cache tiver sido invalidado por ação de workflow
  if (processoStore.dadosValidos(codProcesso)) {
    processo.value = processoStore.contextoCompleto;
    return;
  }
  try {
    await carregarContextoCompleto();
  } catch {
    // O erro já foi convertido em estado local para exibição inline.
  }
});

defineExpose({
  abrirDetalhesUnidade,
  executarAcaoBloco,
  acaoBlocoAtual,
  unidadesElegiveis,
  acoesBlocoVisiveis,
});
</script>
