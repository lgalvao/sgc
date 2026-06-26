<template>
  <LayoutPadrao>
    <AppAlert
        v-if="ultimoErro"
        :mensagem="ultimoErro.mensagem"
        variant="danger"
        @dismissed="limparErro()"/>

    <AppAlert
        v-if="notificacao"
        :chave="notificacao.chave"
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

    <ProcessoDetalheFluxoModais
        :descricao-processo="descricaoProcesso"
        :ids-elegiveis="idsElegiveis"
        :loading-finalizacao="loadingFinalizacao"
        :registrar-modal-bloco-ref="registrarModalBlocoRef"
        :mostrar-data-limite="acaoBlocoAtual?.requerDataLimite"
        :mostrar-modal-finalizacao="mostrarModalFinalizacao"
        :processo-acao-rotulo-botao="acaoBlocoAtual?.rotuloBotao"
        :processo-acao-texto="acaoBlocoAtual?.texto"
        :processo-acao-titulo="acaoBlocoAtual?.titulo"
        :unidades-elegiveis="unidadesElegiveis"
        @confirmar-finalizacao="confirmarFinalizacao"
        @executar-acao-bloco="executarAcaoBloco"
        @update:mostrar-modal-finalizacao="mostrarModalFinalizacao = $event"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onActivated, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import ProcessoDetalheFluxoModais from "@/components/processo/ProcessoDetalheFluxoModais.vue";
import ProcessoAcoes from "@/components/processo/ProcessoAcoes.vue";
import ProcessoSubprocessosTable from "@/components/processo/ProcessoSubprocessosTable.vue";
import {useNotification} from "@/composables/useNotification";
import {useProcessoQuery} from "@/composables/useProcessoQuery";
import {useProcessoAcoes} from "@/views/processoDetalheAcoes";
import type {ModalAcaoBlocoRef} from "@/views/processoDetalheTipos";
import {usePerfil} from "@/composables/usePerfil";
import type {Processo} from "@/types/tipos";
import {type ErroNormalizado, normalizarErro} from "@/utils/apiError";
import {TEXTOS} from "@/constants/textos";

type LinhaCliqueSubprocesso = {
  clickable?: boolean;
  sigla?: string;
  codSubprocesso?: number | null;
};

const route = useRoute();
const router = useRouter();
const {notificacao, notify, clear} = useNotification();
const {ehAdmin} = usePerfil();
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
    const resultado = await processoQuery.refetch();
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
const mostrarFinalizarProcesso = computed(() => ehAdmin.value);
const acoesBlocoVisiveis = computed(() => {
  const acoes = processo.value?.acoesBloco;
  if (!acoes) return [];
  const filtradas = acoes.filter(acao => acao.mostrar);
  if (processo.value?.tipo === 'DIAGNOSTICO') {
    return filtradas.filter(acao => acao.codigo === 'aceitar-diagnostico');
  } else {
    return filtradas.filter(acao => acao.codigo !== 'aceitar-diagnostico');
  }
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

function registrarModalBlocoRef(instancia: unknown) {
  modalBlocoRef.value = (instancia as ModalAcaoBlocoRef | null) ?? null;
}

async function abrirDetalhesUnidade(row: LinhaCliqueSubprocesso) {
  if (!row.clickable || !row.sigla) {
    return;
  }

  if (processo.value?.tipo === "DIAGNOSTICO" && typeof row.codSubprocesso === "number") {
    await router.push({
      name: "DiagnosticoUnidade",
      params: {
        codSubprocesso: String(row.codSubprocesso),
        siglaUnidade: row.sigla
      },
    });
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
  try {
    const {data} = await processoQuery.refresh();
    if (data) {
      processo.value = data;
    }
  } catch (error) {
    ultimoErro.value = normalizarErro(error);
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
