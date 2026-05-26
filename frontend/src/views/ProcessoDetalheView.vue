<template>
  <LayoutPadrao>
    <AppAlert
        v-if="ultimoErro"
        :mensagem="ultimoErro.mensagem"
        variant="danger"
        @dismissed="limparErro()"/>

    <AppAlert
        v-if="notificacao"
        :dispensavel="notificacao.dispensavel"
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

    <CarregamentoPagina v-else-if="!ultimoErro" :mensagem="TEXTOS.processo.CARREGANDO_DETALHES"/>

    <ModalAcaoBloco
        :id="'modal-acao-bloco'"
        ref="modalBlocoRef"
        :mostrar-data-limite="acaoBlocoAtual?.requerDataLimite"
        :rotulo-botao="acaoBlocoAtual?.rotuloBotao"
        :texto="acaoBlocoAtual?.texto"
        :titulo="acaoBlocoAtual?.titulo"
        :unidades="unidadesElegiveis"
        :unidades-pre-selecionadas="idsElegiveis"
        @confirmar="executarAcaoBloco"/>

    <ModalConfirmacao
        v-model="mostrarModalFinalizacao"
        :loading="loadingFinalizacao"
        :ok-title="TEXTOS.comum.BOTAO_FINALIZAR"
        :titulo="TEXTOS.processo.FINALIZACAO_TITULO"
        test-id-cancelar="btn-finalizar-processo-cancelar"
        test-id-confirmar="btn-finalizar-processo-confirmar"
        variant="danger"
        @confirmar="confirmarFinalizacao">
      <p class="mb-2">
        {{ TEXTOS.processo.FINALIZACAO_CONFIRMACAO_PREFIXO }}
        <strong>{{ descricaoProcesso }}</strong>?
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
import {useProcessoQuery} from "@/composables/useProcessoQuery";
import {useProcessoAcoes} from "@/views/processoDetalheAcoes";
import {usePerfil} from "@/composables/usePerfil";
import type {Processo} from "@/types/tipos";
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
const codProcesso = Number(route.params.codProcesso || route.query.codProcesso);
const processoQuery = useProcessoQuery(codProcesso);
const processo = ref<Processo | null>(null);
const ultimoErro = ref<ErroNormalizado | null>(null);
const carregamentoInicialConcluido = ref(false);

function limparErro() {
  ultimoErro.value = null;
}

function registrarErro(error: unknown) {
  ultimoErro.value = normalizarErro(error);
  return ultimoErro.value?.mensagem || TEXTOS.processo.ERRO_PADRAO;
}

async function carregarContextoCompleto() {
  limparErro();
  const processoAnterior = processo.value;

  try {
    const resultado = carregamentoInicialConcluido.value
        ? await processoQuery.refresh(true)
        : await processoQuery.refetch(true);
    const data = resultado.data;
    if (data) {
      processo.value = data;
    }
    return data;
  } catch (error) {
    // Em recargas em background, mantemos o último processo carregado até que haja sucesso ou usuário decida sair
    processo.value = processoAnterior;
    ultimoErro.value = normalizarErro(error);
    return null;
  }
}

const participantesHierarquia = computed(() => processo.value ? processo.value.unidades : []);
const podeFinalizar = computed(() => !!processo.value?.podeFinalizar);
const mostrarFinalizarProcesso = computed(() => isAdmin.value);
const acoesBlocoVisiveis = computed(() => {
  const acoes = processo.value?.acoesBloco;
  return acoes ? acoes.filter(acao => acao.mostrar) : [];
});
const usarMenuAcoesBloco = computed(() => acoesBlocoVisiveis.value.length > 1);
const acaoBlocoPrincipal = computed(() => acoesBlocoVisiveis.value[0] ?? null);

const descricaoProcesso = computed(() => processo.value ? processo.value.descricao : "");
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
  limparErro,
  notify,
  registrarErro,
});

async function abrirDetalhesUnidade(row: LinhaCliqueSubprocesso) {
  if (!row.clickable || !row.sigla) {
    return;
  }

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
}

onMounted(async () => {
  if (codProcesso) {
    await carregarContextoCompleto();
    carregamentoInicialConcluido.value = true;
  }
});

onActivated(async () => {
  if (!codProcesso || !carregamentoInicialConcluido.value) {
    return;
  }
  await carregarContextoCompleto();
});

defineExpose({
  abrirDetalhesUnidade,
  executarAcaoBloco,
  acaoBlocoAtual,
  unidadesElegiveis,
  acoesBlocoVisiveis,
});
</script>
