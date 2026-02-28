<template>
  <LayoutPadrao>
    <ErrorAlert
        :error="processosStore.lastError"
        @dismiss="processosStore.clearError()"/>

    <div v-if="processo">
      <PageHeader
          :etapa="`Etapa atual: ${formatSituacaoProcesso(processo.situacao)}`"
          :proxima-acao="proximaAcaoProcesso"
          :title="processo.descricao"
          title-test-id="processo-info"
      >
        <template #default>
          <BBadge class="mb-2" style="border-radius: 0" variant="secondary">
            Detalhes do processo
          </BBadge>

          <ProcessoInfo
              :show-data-limite="false"
              :situacao="processo.situacao"
              :tipo="processo.tipo"/>
        </template>

        <template #actions>
          <BButton v-if="mostrarBotoesBloco && podeAceitarBloco" variant="success" @click="abrirModalBloco('aceitar')">
            Aceitar em bloco
          </BButton>

          <BButton
              v-if="mostrarBotoesBloco && podeHomologarBloco" class="text-white" variant="warning"
              @click="abrirModalBloco('homologar')">
            Homologar em bloco
          </BButton>

          <BButton
              v-if="mostrarBotoesBloco && podeDisponibilizarBloco" class="text-white" variant="info"
              @click="abrirModalBloco('disponibilizar')">
            Disponibilizar mapas em bloco
          </BButton>
        </template>
      </PageHeader>

      <ProcessoSubprocessosTable
          :participantes-hierarquia="participantesHierarquia"
          @row-click="abrirDetalhesUnidade"/>

      <ProcessoAcoes
          :pode-aceitar-bloco="podeAceitarBloco"
          :pode-finalizar="podeFinalizar"
          :pode-homologar-bloco="podeHomologarBloco"
          @finalizar="finalizarProcesso"/>
    </div>

    <div v-else class="text-center py-5">
      <BSpinner label="Carregando detalhes do processo..." variant="primary"/>
      <p class="mt-2 text-muted">Carregando detalhes do processo...</p>
    </div>

    <!-- Modal de Ação em Bloco -->
    <ModalAcaoBloco
        :id="'modal-acao-bloco'"
        ref="modalBlocoRef"
        :mostrar-data-limite="acaoBlocoAtual === 'disponibilizar'"
        :rotulo-botao="rotuloBotaoBloco"
        :texto="textoModalBloco"
        :titulo="tituloModalBloco"
        :unidades="unidadesElegiveis"
        :unidades-pre-selecionadas="idsElegiveis"
        @confirmar="executarAcaoBloco"/>

    <ModalConfirmacao
        v-model="mostrarModalFinalizacao"
        test-id-cancelar="btn-finalizar-processo-cancelar"
        test-id-confirmar="btn-finalizar-processo-confirmar"
        titulo="Finalização de processo"
        variant="success"
        @confirmar="confirmarFinalizacao">

      <BAlert
          :fade="false"
          :model-value="true"
          variant="info">

        <i aria-hidden="true" class="bi bi-info-circle"/>
        Confirma a finalização do processo <strong>{{ processo?.descricao || '' }}</strong>?<br>
        Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades
        participantes do processo.
      </BAlert>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BBadge, BButton, BSpinner} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import ModalAcaoBloco from "@/components/processo/ModalAcaoBloco.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ProcessoAcoes from "@/components/processo/ProcessoAcoes.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import ProcessoInfo from "@/components/processo/ProcessoInfo.vue";
import ProcessoSubprocessosTable from "@/components/processo/ProcessoSubprocessosTable.vue";
import {useProximaAcao} from "@/composables/useProximaAcao";
import {useProcessosStore} from "@/stores/processos";
import {usePerfilStore} from "@/stores/perfil";
import {useFeedbackStore} from "@/stores/feedback";
import {SituacaoProcesso, SituacaoSubprocesso} from "@/types/tipos";
import {formatSituacaoProcesso, formatSituacaoSubprocesso} from "@/utils/formatters";
import {logger} from "@/utils";

function flattenUnidades(unidades: any[]): any[] {
  let result: any[] = [];
  for (const u of unidades) {
    result.push(u);
    if (u.filhos && u.filhos.length > 0) {
      result = result.concat(flattenUnidades(u.filhos));
    }
  }
  return result;
}

const route = useRoute();
const router = useRouter();
const processosStore = useProcessosStore();
const perfilStore = usePerfilStore();
const feedbackStore = useFeedbackStore();
const {obterProximaAcao} = useProximaAcao();

const codProcesso = Number(route.params.codProcesso || route.query.codProcesso);
const modalBlocoRef = ref<any>(null);
const mostrarModalFinalizacao = ref(false);
const acaoBlocoAtual = ref<"aceitar" | "homologar" | "disponibilizar">("aceitar");

const processo = computed(() => processosStore.processoDetalhe);
const participantesHierarquia = computed(() => processo.value?.unidades || []);

const podeAceitarBloco = computed(() => {
  return !isProcessoFinalizado.value && (processo.value?.podeAceitarCadastroBloco || false)
      && unidadesElegiveisPorAcao.value.aceitar.length > 0;
});

const podeHomologarBloco = computed(() => {
  return !isProcessoFinalizado.value && (processo.value?.podeHomologarCadastro || processo.value?.podeHomologarMapa || false)
      && unidadesElegiveisPorAcao.value.homologar.length > 0;
});

const podeDisponibilizarBloco = computed(() => {
  return !isProcessoFinalizado.value && (processo.value?.podeDisponibilizarMapaBloco || false)
      && unidadesElegiveisPorAcao.value.disponibilizar.length > 0;
});

const mostrarBotoesBloco = computed(() => {
  return podeAceitarBloco.value || podeHomologarBloco.value || podeDisponibilizarBloco.value;
});

const podeFinalizar = computed(() => {
  return processo.value?.podeFinalizar || false;
});

const isProcessoFinalizado = computed(() => {
  return processo.value?.situacao === SituacaoProcesso.FINALIZADO;
});

const unidadesElegiveisPorAcao = computed(() => {
  const unidades = flattenUnidades(participantesHierarquia.value);
  return {
    aceitar: unidades.filter(u =>
        u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
        u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
        u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
        u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO
    ),
    homologar: unidades.filter(u =>
        u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
        u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
        u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
        u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO
    ),
    disponibilizar: unidades.filter(u =>
        u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO ||
        u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO ||
        u.situacaoSubprocesso === SituacaoSubprocesso.NAO_INICIADO
    )
  };
});

const unidadesElegiveis = computed(() => {
  const elegiveis = unidadesElegiveisPorAcao.value[acaoBlocoAtual.value];
  if (!elegiveis) return [];
  return elegiveis.map(u => ({
    codigo: u.codUnidade,
    sigla: u.sigla,
    nome: u.nome,
    situacao: formatSituacaoSubprocesso(u.situacaoSubprocesso)
  }));
});

const idsElegiveis = computed(() => unidadesElegiveis.value.map(u => u.codigo));

const tituloModalBloco = computed(() => {
  switch (acaoBlocoAtual.value) {
    case "aceitar":
      return "Aceitar em Bloco";
    case "homologar":
      return "Homologar em Bloco";
    case "disponibilizar":
      return "Disponibilizar Mapas em Bloco";
    default:
      return "";
  }
});

const textoModalBloco = computed(() => {
  switch (acaoBlocoAtual.value) {
    case "aceitar":
      return "Selecione as unidades para aceitar o cadastro/mapa em bloco:";
    case "homologar":
      return "Selecione as unidades para homologar o cadastro/mapa em bloco:";
    case "disponibilizar":
      return "Selecione as unidades para disponibilizar os mapas em bloco:";
    default:
      return "";
  }
});

const rotuloBotaoBloco = computed(() => {
  switch (acaoBlocoAtual.value) {
    case "aceitar":
      return "Aceitar Selecionados";
    case "homologar":
      return "Homologar Selecionados";
    case "disponibilizar":
      return "Disponibilizar Selecionados";
    default:
      return "";
  }
});

const mensagemSucessoAcaoBloco = computed(() => {
  switch (acaoBlocoAtual.value) {
    case "aceitar":
      return "Cadastros aceitos em bloco";
    case "homologar":
      return "Cadastros homologados em bloco";
    case "disponibilizar":
      return "Mapas de competências disponibilizados em bloco";
    default:
      return "Ação em bloco realizada com sucesso";
  }
});

const proximaAcaoProcesso = computed(() => obterProximaAcao({
  perfil: perfilStore.perfilSelecionado,
  situacao: processo.value?.situacao,
  podeFinalizar: podeFinalizar.value,
  isProcessoFinalizado: isProcessoFinalizado.value,
}));

async function abrirDetalhesUnidade(row: any) {
  if (!row.clickable) {
    return;
  }

  try {
    await router.push({
      name: "Subprocesso",
      params: {
        codProcesso: codProcesso.toString(),
        siglaUnidade: row.sigla
      }
    });
  } catch (error) {
    logger.error(`Erro ao navegar para detalhes da unidade ${row.sigla}:`, error);
  }
}

function finalizarProcesso() {
  mostrarModalFinalizacao.value = true;
}

async function confirmarFinalizacao() {
  try {
    await processosStore.finalizarProcesso(codProcesso);
    feedbackStore.show("Sucesso", "Processo finalizado com sucesso", "success");
    await router.push("/painel");
  } catch (error: any) {
    const mensagem = processosStore.lastError?.message || error.message || "Ocorreu um erro";
    feedbackStore.show("Erro ao finalizar", mensagem, "danger");
  }
}

function abrirModalBloco(acao: "aceitar" | "homologar" | "disponibilizar") {
  acaoBlocoAtual.value = acao;
  modalBlocoRef.value?.abrir();
}

async function executarAcaoBloco(dados: { ids: number[], dataLimite?: string }) {
  try {
    modalBlocoRef.value?.setProcessando(true);
    await processosStore.executarAcaoBloco(acaoBlocoAtual.value, dados.ids, dados.dataLimite);

    feedbackStore.show("Sucesso", mensagemSucessoAcaoBloco.value, "success");
    modalBlocoRef.value?.fechar();
    if (acaoBlocoAtual.value === "aceitar" || acaoBlocoAtual.value === "disponibilizar") {
      await router.push("/painel");
      return;
    }
    await processosStore.buscarContextoCompleto(codProcesso);
  } catch (error: any) {
    modalBlocoRef.value?.setErro(error.message || "Erro ao executar ação em bloco");
    modalBlocoRef.value?.setProcessando(false);
  }
}

onMounted(async () => {
  if (codProcesso) {
    await processosStore.buscarContextoCompleto(codProcesso);
  }
});

defineExpose({
  abrirDetalhesUnidade,
  executarAcaoBloco,
  acaoBlocoAtual,
  unidadesElegiveis,
  perfilStore,
  tituloModalBloco,
  textoModalBloco,
  rotuloBotaoBloco,
  mensagemSucessoAcaoBloco,
});
</script>
