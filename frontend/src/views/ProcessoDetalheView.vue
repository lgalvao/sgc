<template>
  <LayoutPadrao>
    <AppAlert
        v-if="lastError"
        :mensagem="lastError.mensagem"
        variant="danger"
        @dismissed="clearError()"/>

    <AppAlert
        v-if="notificacao"
        :dispensavel="notificacao.dispensavel ?? true"
        :mensagem="notificacao.mensagem"
        :notification="notificacao.notificacao"
        :stack-trace="notificacao.stackTrace"
        :variante="notificacao.variante"
        @dismissed="clear()"/>

    <div v-if="processo">
      <ProcessoAcoes
          :acao-bloco-principal="acaoBlocoPrincipal"
          :acoes-bloco-visiveis="acoesBlocoVisiveis"
          :mostrar-finalizar-processo="mostrarFinalizarProcesso"
          :pode-finalizar="podeFinalizar"
          :processando-acao-bloco="processandoAcaoBloco"
          :processo="processo"
          :usar-menu-acoes-bloco="usarMenuAcoesBloco"
          @finalizar="finalizarProcesso"
          @abrir-acao-bloco="abrirModalBloco"/>

      <ProcessoSubprocessosTable
          :participantes-hierarquia="participantesHierarquia"
          @row-click="abrirDetalhesUnidade"/>
    </div>

    <CarregamentoPagina v-else-if="!lastError" :mensagem="TEXTOS.processo.CARREGANDO_DETALHES"/>

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
        :loading="loadingFinalizacao"
        :ok-title="TEXTOS.comum.BOTAO_FINALIZAR"
        :titulo="TEXTOS.processo.FINALIZACAO_TITULO"
        test-codigo-cancelar="btn-finalizar-processo-cancelar"
        test-codigo-confirmar="btn-finalizar-processo-confirmar"
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
import {computed, onActivated, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import ModalAcaoBloco from "@/components/processo/ModalAcaoBloco.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import ProcessoAcoes from "@/components/processo/ProcessoAcoes.vue";
import ProcessoSubprocessosTable from "@/components/processo/ProcessoSubprocessosTable.vue";
import {useNotification} from "@/composables/useNotification";
import {useProcessoAcoes} from "@/views/processoDetalheAcoes";
import {usePerfil} from "@/composables/usePerfil";
import {useProcessoStore} from "@/stores/processo";
import type {Processo} from "@/types/tipos";
import {logger} from "@/utils";
import {type ErroNormalizado, normalizarErro} from "@/utils/apiError";
import {TEXTOS} from "@/constants/textos";

type LinhaCliqueSubprocesso = {
  clickable?: boolean;
  sigla?: string;
  codSubprocesso?: number;
};

const route = useRoute();
const router = useRouter();
const {notificacao, notify, clear} = useNotification();
const {isAdmin} = usePerfil();
const processoStore = useProcessoStore();
const codProcesso = Number(route.params.codProcesso || route.query.codProcesso);
const processo = ref<Processo | null>(null);
const lastError = ref<ErroNormalizado | null>(null);
const carregamentoInicialConcluido = ref(false);

function clearError() {
  lastError.value = null;
}

function registrarErro(error: unknown) {
  lastError.value = normalizarErro(error);
  return lastError.value?.mensagem || TEXTOS.processo.ERRO_PADRAO;
}

async function carregarContextoCompleto() {
  clearError();

  try {
    const data = await processoStore.garantirContextoCompleto(codProcesso);
    if (data) {
      processo.value = data;
    }
    return data;
  } catch (error) {
    // Limpa os dados apenas em caso de erro para evitar exibir informações desatualizadas como válidas.
    // Durante o recarregamento em background, os dados antigos permanecem visíveis (sem flash de spinner).
    processo.value = null;
    lastError.value = normalizarErro(error);
    throw error;
  }
}

const participantesHierarquia = computed(() => processo.value?.unidades || []);
const podeFinalizar = computed(() => processo.value?.podeFinalizar || false);
const mostrarFinalizarProcesso = computed(() => isAdmin.value);
const acoesBlocoVisiveis = computed(() => (processo.value?.acoesBloco ?? []).filter(acao => acao.mostrar));
const usarMenuAcoesBloco = computed(() => acoesBlocoVisiveis.value.length > 1);
const acaoBlocoPrincipal = computed(() => acoesBlocoVisiveis.value[0] ?? null);
const {
  acaoBlocoAtual,
  abrirModalBloco,
  confirmarFinalizacao,
  executarAcaoBloco,
  finalizarProcesso,
  idsElegiveis,
  loadingFinalizacao,
  modalBlocoRef,
  mostrarModalFinalizacao,
  processandoAcaoBloco,
  unidadesElegiveis,
} = useProcessoAcoes({
  codProcesso,
  processo,
  carregarContextoCompleto,
  clearError,
  notify,
  registrarErro,
});

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
      query: typeof row.codSubprocesso === "number"
          ? {codSubprocesso: String(row.codSubprocesso)}
          : undefined,
    });
  } catch (error) {
    logger.error(`Erro ao navegar para detalhes da unidade ${row.sigla}:`, error);
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
  if (processoStore.dadosValidos(codProcesso)) {
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
