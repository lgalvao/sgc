<template>
  <LayoutPadrao>
    <AppAlert
        v-if="processosStore.lastError"
        :message="processosStore.lastError.message"
        variant="danger"
        @dismissed="processosStore.clearError()"/>

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
          title-test-id="processo-info">

        <template #default>
          <ProcessoInfo
              :show-data-limite="false"
              :situacao="processo.situacao"
              :tipo="processo.tipo"/>
        </template>

        <template #actions>
          <BButton
              v-if="podeAceitarBloco"
              :disabled="!habilitarAceiteBloco"
              data-testid="btn-processo-aceitar-bloco"
              variant="success"
              @click="abrirModalBloco('aceitar')">
            {{ rotuloAcaoAceitarBloco }}
          </BButton>

          <BButton
              v-if="podeHomologarBloco"
              :disabled="!habilitarHomologacaoBloco"
              class="text-white"
              data-testid="btn-processo-homologar-bloco"
              variant="warning"
              @click="abrirModalBloco('homologar')">
            {{ rotuloAcaoHomologarBloco }}
          </BButton>

          <BButton
              v-if="podeDisponibilizarBloco"
              :disabled="!habilitarDisponibilizacaoBloco"
              class="text-white"
              data-testid="btn-processo-disponibilizar-bloco"
              variant="info"
              @click="abrirModalBloco('disponibilizar')">
            {{ rotuloAcaoDisponibilizarBloco }}
          </BButton>
        </template>
      </PageHeader>

      <ProcessoSubprocessosTable
          :participantes-hierarquia="participantesHierarquia"
          @row-click="abrirDetalhesUnidade"/>

      <div>
        <p v-if="isProcessoFinalizado" class="mt-3 text-muted">
          Processo concluído.
        </p>
        <BButton
            v-if="podeFinalizar"
            class="mt-3"
            data-testid="btn-processo-finalizar"
            variant="danger"
            @click="finalizarProcesso"
        >
          Finalizar processo
        </BButton>
      </div>
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
import {BAlert, BButton, BSpinner} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import ModalAcaoBloco from "@/components/processo/ModalAcaoBloco.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import ProcessoInfo from "@/components/processo/ProcessoInfo.vue";
import ProcessoSubprocessosTable from "@/components/processo/ProcessoSubprocessosTable.vue";
import {useProcessosStore} from "@/stores/processos";
import {usePerfilStore} from "@/stores/perfil";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import {usePerfil} from "@/composables/usePerfil";
import {SituacaoProcesso, SituacaoSubprocesso} from "@/types/tipos";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {logger} from "@/utils";

type ContextoBloco = "cadastro" | "validacao" | "misto";

const route = useRoute();
const router = useRouter();
const processosStore = useProcessosStore();
const perfilStore = usePerfilStore();
const {isGlobalGestor, podeHomologarBlocoGlobal, isGlobalAdmin} = usePerfil();
const {notificacao, notify, clear} = useNotification();
const toastStore = useToastStore();
const codProcesso = Number(route.params.codProcesso || route.query.codProcesso);
const modalBlocoRef = ref<any>(null);
const mostrarModalFinalizacao = ref(false);
const acaoBlocoAtual = ref<"aceitar" | "homologar" | "disponibilizar">("aceitar");
const processandoAcaoBloco = ref(false);

const processo = computed(() => processosStore.processoDetalhe);
const participantesHierarquia = computed(() => processo.value?.unidades || []);
const subprocessosElegiveis = computed(() => processosStore.subprocessosElegiveis || []);

const podeAceitarBloco = computed(() => {
  return isGlobalGestor.value && (processo.value?.podeAceitarCadastroBloco ?? false);
});

const podeHomologarBloco = computed(() => {
  return podeHomologarBlocoGlobal.value &&
      ((processo.value?.podeHomologarCadastro ?? false) || (processo.value?.podeHomologarMapa ?? false));
});

const podeDisponibilizarBloco = computed(() => {
  return isGlobalAdmin.value &&
      !isProcessoFinalizado.value &&
      (processo.value?.podeDisponibilizarMapaBloco || false);
});

// Habilitação dos Botões (Português)
const habilitarAceiteBloco = computed(() => {
  return !processandoAcaoBloco.value && unidadesElegiveisPorAcao.value.aceitar.length > 0;
});

const habilitarHomologacaoBloco = computed(() => {
  return !processandoAcaoBloco.value && unidadesElegiveisPorAcao.value.homologar.length > 0;
});

const habilitarDisponibilizacaoBloco = computed(() => {
  return !processandoAcaoBloco.value && unidadesElegiveisPorAcao.value.disponibilizar.length > 0;
});

const podeFinalizar = computed(() => {
  return processo.value?.podeFinalizar || false;
});

const isProcessoFinalizado = computed(() => {
  return processo.value?.situacao === SituacaoProcesso.FINALIZADO;
});

function isSituacaoAceiteCadastro(situacao: SituacaoSubprocesso) {
  return situacao === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
      situacao === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
}

function isSituacaoAceiteValidacao(situacao: SituacaoSubprocesso) {
  return situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
      situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES ||
      situacao === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO ||
      situacao === SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES;
}

function isSituacaoHomologacaoCadastro(situacao: SituacaoSubprocesso) {
  return situacao === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
      situacao === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
}

function isSituacaoHomologacaoValidacao(situacao: SituacaoSubprocesso) {
  return situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
      situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES ||
      situacao === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO ||
      situacao === SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES;
}

function isSituacaoDisponibilizacaoMapa(situacao: SituacaoSubprocesso) {
  return situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO ||
      situacao === SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO;
}

function obterSituacao(item: { situacaoSubprocesso?: SituacaoSubprocesso, situacao?: SituacaoSubprocesso }) {
  return item.situacaoSubprocesso ?? item.situacao;
}

function obterContextoBloco(unidades: any[]): ContextoBloco {
  const temCadastro = unidades.some(u => {
    const situacao = obterSituacao(u);
    return situacao ? isSituacaoAceiteCadastro(situacao) || isSituacaoHomologacaoCadastro(situacao) : false;
  });
  const temValidacao = unidades.some(u => {
    const situacao = obterSituacao(u);
    return situacao ? isSituacaoAceiteValidacao(situacao) || isSituacaoHomologacaoValidacao(situacao) : false;
  });

  if (temCadastro && !temValidacao) {
    return "cadastro";
  }
  if (temValidacao && !temCadastro) {
    return "validacao";
  }
  return "misto";
}

function obterUnidadesContextoAcao(acao: "aceitar" | "homologar" | "disponibilizar", ids?: number[]) {
  const unidadesAcao = unidadesElegiveisPorAcao.value[acao] ?? [];
  if (!ids || ids.length === 0) {
    return unidadesAcao;
  }
  return unidadesAcao.filter(unidade => ids.includes(unidade.unidadeCodigo));
}

function obterContextoAtualAcao(acao: "aceitar" | "homologar" | "disponibilizar", ids?: number[]): ContextoBloco {
  return obterContextoBloco(obterUnidadesContextoAcao(acao, ids));
}

function obterMensagemSucesso(
    acao: "aceitar" | "homologar" | "disponibilizar",
    contexto: ContextoBloco
) {
  switch (acao) {
    case "aceitar":
      switch (contexto) {
        case "cadastro":
          return "Cadastros aceitos em bloco";
        case "validacao":
          return "Mapas aceitos em bloco";
        default:
          return "Aceites registrados em bloco";
      }
    case "homologar":
      switch (contexto) {
        case "cadastro":
          return "Cadastros homologados em bloco";
        case "validacao":
          return "Mapas de competências homologados em bloco";
        default:
          return "Homologações registradas em bloco";
      }
    case "disponibilizar":
      return "Mapas de competências disponibilizados em bloco";
    default:
      return "Ação em bloco realizada com sucesso";
  }
}

const unidadesElegiveisPorAcao = computed(() => {
  const unidades = subprocessosElegiveis.value;

  return {
    aceitar: unidades.filter(u => isSituacaoAceiteCadastro(u.situacao) || isSituacaoAceiteValidacao(u.situacao)),
    homologar: unidades.filter(u => isSituacaoHomologacaoCadastro(u.situacao) || isSituacaoHomologacaoValidacao(u.situacao)),
    disponibilizar: unidades.filter(u => isSituacaoDisponibilizacaoMapa(u.situacao))
  };
});

const unidadesElegiveis = computed(() => {
  const elegiveis = unidadesElegiveisPorAcao.value[acaoBlocoAtual.value];
  if (!elegiveis) return [];
  return elegiveis.map(u => ({
    codigo: u.unidadeCodigo,
    sigla: u.unidadeSigla,
    nome: u.unidadeNome,
    situacao: formatSituacaoSubprocesso(u.situacao)
  }));
});

const idsElegiveis = computed(() => unidadesElegiveis.value.map(u => u.codigo));

const contextoAceiteBloco = computed<ContextoBloco>(() => obterContextoBloco(unidadesElegiveisPorAcao.value.aceitar));
const contextoHomologacaoBloco = computed<ContextoBloco>(() => obterContextoBloco(unidadesElegiveisPorAcao.value.homologar));

const rotuloAcaoAceitarBloco = computed(() => {
  switch (contextoAceiteBloco.value) {
    case "cadastro":
      return "Aceitar cadastro em bloco";
    case "validacao":
      return "Aceitar mapas em bloco";
    default:
      return "Registrar aceite em bloco";
  }
});

const rotuloAcaoHomologarBloco = computed(() => {
  switch (contextoHomologacaoBloco.value) {
    case "cadastro":
      return "Homologar em bloco";
    case "validacao":
      return "Homologar mapa de competências em bloco";
    default:
      return "Homologar em bloco";
  }
});

const rotuloAcaoDisponibilizarBloco = computed(() => {
  return "Disponibilizar mapas em bloco";
});

const tituloModalBloco = computed(() => {
  switch (acaoBlocoAtual.value) {
    case "aceitar":
      switch (contextoAceiteBloco.value) {
        case "cadastro":
          return "Aceite de cadastro em bloco";
        case "validacao":
          return "Aceite de mapas em bloco";
        default:
          return "Aceite em bloco";
      }
    case "homologar":
      switch (contextoHomologacaoBloco.value) {
        case "cadastro":
          return "Homologação de cadastro em bloco";
        case "validacao":
          return "Homologação de mapa em bloco";
        default:
          return "Homologação em bloco";
      }
    case "disponibilizar":
      return "Disponibilização de mapa em bloco";
    default:
      return "";
  }
});

const textoModalBloco = computed(() => {
  switch (acaoBlocoAtual.value) {
    case "aceitar":
      switch (contextoAceiteBloco.value) {
        case "cadastro":
          return "Selecione as unidades cujos cadastros deverão ser aceitos:";
        case "validacao":
          return "Selecione as unidades para aceite dos mapas correspondentes";
        default:
          return "Selecione as unidades para registrar o aceite correspondente.";
      }
    case "homologar":
      switch (contextoHomologacaoBloco.value) {
        case "cadastro":
          return "Selecione abaixo as unidades cujos cadastros deverão ser homologados:";
        case "validacao":
          return "Selecione abaixo as unidades cujos mapas deverão ser homologados:";
        default:
          return "Selecione as unidades para homologação em bloco.";
      }
    case "disponibilizar":
      return "Selecione abaixo as unidades cujos mapas deverão ser disponibilizados:";
    default:
      return "";
  }
});

const rotuloBotaoBloco = computed(() => {
  switch (acaoBlocoAtual.value) {
    case "aceitar":
      return "Registrar aceite";
    case "homologar":
      return "Homologar";
    case "disponibilizar":
      return "Disponibilizar";
    default:
      return "";
  }
});

const mensagemSucessoAcaoBloco = computed(() => {
  const contexto = acaoBlocoAtual.value === "homologar"
      ? contextoHomologacaoBloco.value
      : contextoAceiteBloco.value;
  return obterMensagemSucesso(acaoBlocoAtual.value, contexto);
});

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
    toastStore.setPending("Processo finalizado com sucesso.");
    await router.push("/painel");
  } catch (error: any) {
    const mensagem = processosStore.lastError?.message || error.message || "Ocorreu um erro";
    notify(mensagem, 'danger');
  }
}

function abrirModalBloco(acao: "aceitar" | "homologar" | "disponibilizar") {
  acaoBlocoAtual.value = acao;
  modalBlocoRef.value?.abrir();
}

async function executarAcaoBloco(dados: { ids: number[], dataLimite?: string }) {
  try {
    processandoAcaoBloco.value = true;
    modalBlocoRef.value?.setProcessando(true);
    const contextoExecucao = obterContextoAtualAcao(acaoBlocoAtual.value, dados.ids);
    const mensagemSucesso = obterMensagemSucesso(acaoBlocoAtual.value, contextoExecucao);
    await processosStore.executarAcaoBloco(acaoBlocoAtual.value, dados.ids, dados.dataLimite);

    modalBlocoRef.value?.fechar();
    const deveRedirecionarPainel = acaoBlocoAtual.value === "aceitar" ||
        acaoBlocoAtual.value === "disponibilizar" ||
        (acaoBlocoAtual.value === "homologar" && contextoExecucao === "validacao");

    if (deveRedirecionarPainel) {
      toastStore.setPending(mensagemSucesso);
      await router.push("/painel");
      return;
    }
    notify(mensagemSucesso, 'success');
    await processosStore.buscarContextoCompleto(codProcesso);
  } catch (error: any) {
    modalBlocoRef.value?.setErro(error.message || "Erro ao executar ação em bloco");
    modalBlocoRef.value?.setProcessando(false);
  } finally {
    processandoAcaoBloco.value = false;
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
  rotuloAcaoAceitarBloco,
  rotuloAcaoHomologarBloco,
  rotuloAcaoDisponibilizarBloco,
  tituloModalBloco,
  textoModalBloco,
  rotuloBotaoBloco,
  mensagemSucessoAcaoBloco,
});
</script>
