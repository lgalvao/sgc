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
              v-if="podeAceitarBloco"
              id="btn-aceitar-bloco"
              :disabled="!habilitarAceiteBloco"
              data-testid="btn-processo-aceitar-bloco"
              variant="success"
              @click="abrirModalBloco('aceitar')">
            {{ rotuloAcaoAceitarBloco }}
          </BButton>

          <BButton
              v-if="podeHomologarBloco"
              id="btn-homologar-bloco"
              :disabled="!habilitarHomologacaoBloco"
              data-testid="btn-processo-homologar-bloco"
              variant="success"
              @click="abrirModalBloco('homologar')">
            {{ rotuloAcaoHomologarBloco }}
          </BButton>

          <BButton
              v-if="podeDisponibilizarBloco"
              id="btn-disponibilizar-bloco"
              :disabled="!habilitarDisponibilizacaoBloco"
              data-testid="btn-processo-disponibilizar-bloco"
              variant="success"
              @click="abrirModalBloco('disponibilizar')">
            {{ rotuloAcaoDisponibilizarBloco }}
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
        :mostrar-data-limite="acaoBlocoAtual === 'disponibilizar'"
        :rotulo-botao="rotuloBotaoBloco"
        :texto="textoModalBloco"
        :titulo="tituloModalBloco"
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

      <BAlert
          :fade="false"
          :model-value="true"
          variant="secondary">

        <i aria-hidden="true" class="bi bi-info-circle"/>
        {{ TEXTOS.processo.FINALIZACAO_CONFIRMACAO_PREFIXO }}
        <strong>{{ processo?.descricao || '' }}</strong>?<br>
        {{ TEXTOS.processo.FINALIZACAO_CONFIRMACAO_COMPLEMENTO }}
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
import {usePerfilStore} from "@/stores/perfil";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import type {Processo, SubprocessoElegivel} from "@/types/tipos";
import {SituacaoProcesso, SituacaoSubprocesso} from "@/types/tipos";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {logger} from "@/utils";
import {normalizeError, type NormalizedError} from "@/utils/apiError";
import * as processoService from "@/services/processoService";
import {TEXTOS} from "@/constants/textos";

type ContextoBloco = "cadastro" | "validacao" | "misto";
type AcaoBloco = "aceitar" | "homologar" | "disponibilizar";
type ConfiguracaoTextoAcaoBloco = {
  rotulo: string;
  titulo: string;
  texto: string;
  mensagemSucesso: string;
};

const route = useRoute();
const router = useRouter();
const perfilStore = usePerfilStore();
const {notificacao, notify, clear} = useNotification();
const toastStore = useToastStore();
const codProcesso = Number(route.params.codProcesso || route.query.codProcesso);
const processo = ref<Processo | null>(null);
const subprocessosElegiveis = ref<SubprocessoElegivel[]>([]);
const lastError = ref<NormalizedError | null>(null);
const modalBlocoRef = ref<any>(null);
const mostrarModalFinalizacao = ref(false);
const acaoBlocoAtual = ref<AcaoBloco>("aceitar");
const processandoAcaoBloco = ref(false);

function clearError() {
  lastError.value = null;
}

async function carregarContextoCompleto() {
  clearError();
  processo.value = null;
  subprocessosElegiveis.value = [];

  try {
    const data = await processoService.buscarContextoCompleto(codProcesso);
    if (data) {
      processo.value = data;
      subprocessosElegiveis.value = data.elegiveis ?? [];
    }
    return data;
  } catch (error) {
    lastError.value = normalizeError(error);
    throw error;
  }
}

const configuracoesAcaoBloco: Record<AcaoBloco, Record<ContextoBloco, ConfiguracaoTextoAcaoBloco>> = {
  aceitar: {
    cadastro: {
      rotulo: TEXTOS.acaoBloco.aceitar.ROTULO_CADASTRO,
      titulo: TEXTOS.acaoBloco.aceitar.TITULO_CADASTRO,
      texto: TEXTOS.acaoBloco.aceitar.TEXTO_CADASTRO,
      mensagemSucesso: TEXTOS.sucesso.CADASTROS_ACEITOS_EM_BLOCO,
    },
    validacao: {
      rotulo: TEXTOS.acaoBloco.aceitar.ROTULO_VALIDACAO,
      titulo: TEXTOS.acaoBloco.aceitar.TITULO_VALIDACAO,
      texto: TEXTOS.acaoBloco.aceitar.TEXTO_VALIDACAO,
      mensagemSucesso: TEXTOS.sucesso.MAPAS_ACEITOS_EM_BLOCO,
    },
    misto: {
      rotulo: TEXTOS.acaoBloco.aceitar.ROTULO_MISTO,
      titulo: TEXTOS.acaoBloco.aceitar.TITULO_MISTO,
      texto: TEXTOS.acaoBloco.aceitar.TEXTO_MISTO,
      mensagemSucesso: TEXTOS.sucesso.ACEITES_REGISTRADOS_EM_BLOCO,
    },
  },
  homologar: {
    cadastro: {
      rotulo: TEXTOS.acaoBloco.homologar.ROTULO_CADASTRO,
      titulo: TEXTOS.acaoBloco.homologar.TITULO_CADASTRO,
      texto: TEXTOS.acaoBloco.homologar.TEXTO_CADASTRO,
      mensagemSucesso: TEXTOS.sucesso.CADASTROS_HOMOLOGADOS_EM_BLOCO,
    },
    validacao: {
      rotulo: TEXTOS.acaoBloco.homologar.ROTULO_VALIDACAO,
      titulo: TEXTOS.acaoBloco.homologar.TITULO_VALIDACAO,
      texto: TEXTOS.acaoBloco.homologar.TEXTO_VALIDACAO,
      mensagemSucesso: TEXTOS.sucesso.MAPAS_HOMOLOGADOS_EM_BLOCO,
    },
    misto: {
      rotulo: TEXTOS.acaoBloco.homologar.ROTULO_MISTO,
      titulo: TEXTOS.acaoBloco.homologar.TITULO_MISTO,
      texto: TEXTOS.acaoBloco.homologar.TEXTO_MISTO,
      mensagemSucesso: TEXTOS.sucesso.HOMOLOGACOES_REGISTRADAS_EM_BLOCO,
    },
  },
  disponibilizar: {
    cadastro: {
      rotulo: TEXTOS.acaoBloco.disponibilizar.ROTULO,
      titulo: TEXTOS.acaoBloco.disponibilizar.TITULO,
      texto: TEXTOS.acaoBloco.disponibilizar.TEXTO,
      mensagemSucesso: TEXTOS.sucesso.MAPAS_DISPONIBILIZADOS_EM_BLOCO,
    },
    validacao: {
      rotulo: TEXTOS.acaoBloco.disponibilizar.ROTULO,
      titulo: TEXTOS.acaoBloco.disponibilizar.TITULO,
      texto: TEXTOS.acaoBloco.disponibilizar.TEXTO,
      mensagemSucesso: TEXTOS.sucesso.MAPAS_DISPONIBILIZADOS_EM_BLOCO,
    },
    misto: {
      rotulo: TEXTOS.acaoBloco.disponibilizar.ROTULO,
      titulo: TEXTOS.acaoBloco.disponibilizar.TITULO,
      texto: TEXTOS.acaoBloco.disponibilizar.TEXTO,
      mensagemSucesso: TEXTOS.sucesso.MAPAS_DISPONIBILIZADOS_EM_BLOCO,
    },
  },
};

const participantesHierarquia = computed(() => processo.value?.unidades || []);

const podeAceitarBloco = computed(() => {
  return processo.value?.podeAceitarCadastroBloco ?? false;
});

const podeHomologarBloco = computed(() => {
  return (processo.value?.podeHomologarCadastro ?? false) || (processo.value?.podeHomologarMapa ?? false);
});

const podeDisponibilizarBloco = computed(() => {
  return !isProcessoFinalizado.value && (processo.value?.podeDisponibilizarMapaBloco || false);
});

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

function isSituacaoCadastroPronto(situacao: SituacaoSubprocesso) {
  return situacao === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
      situacao === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
}

function isSituacaoMapaValidadoOuAjustado(situacao: SituacaoSubprocesso) {
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
    return situacao ? isSituacaoCadastroPronto(situacao) : false;
  });
  const temValidacao = unidades.some(u => {
    const situacao = obterSituacao(u);
    return situacao ? isSituacaoMapaValidadoOuAjustado(situacao) : false;
  });

  if (temCadastro && !temValidacao) {
    return "cadastro";
  }
  if (temValidacao && !temCadastro) {
    return "validacao";
  }
  return "misto";
}

function obterUnidadesContextoAcao(acao: AcaoBloco, ids?: number[]) {
  const unidadesAcao = unidadesElegiveisPorAcao.value[acao] ?? [];
  if (!ids || ids.length === 0) {
    return unidadesAcao;
  }
  return unidadesAcao.filter(unidade => ids.includes(unidade.unidadeCodigo));
}

function obterContextoAtualAcao(acao: AcaoBloco, ids?: number[]): ContextoBloco {
  return obterContextoBloco(obterUnidadesContextoAcao(acao, ids));
}

function obterConfiguracaoAcaoBloco(acao: AcaoBloco, contexto: ContextoBloco): ConfiguracaoTextoAcaoBloco {
  return configuracoesAcaoBloco[acao][contexto];
}

const unidadesElegiveisPorAcao = computed(() => {
  const unidades = subprocessosElegiveis.value;
  const unidadeAtiva = perfilStore.unidadeSelecionada;

  return {
    aceitar: unidades.filter(u => (isSituacaoCadastroPronto(u.situacao) || isSituacaoMapaValidadoOuAjustado(u.situacao)) && u.localizacaoCodigo === unidadeAtiva),
    homologar: unidades.filter(u => (isSituacaoCadastroPronto(u.situacao) || isSituacaoMapaValidadoOuAjustado(u.situacao)) && u.localizacaoCodigo === unidadeAtiva),
    disponibilizar: unidades.filter(u => isSituacaoDisponibilizacaoMapa(u.situacao) && u.localizacaoCodigo === unidadeAtiva)
  };
});

const unidadesElegiveis = computed(() => {
  const elegiveis = unidadesElegiveisPorAcao.value[acaoBlocoAtual.value];
  if (!elegiveis) return [];
  return elegiveis.map(u => ({
    codigo: u.unidadeCodigo,
    sigla: u.unidadeSigla,
    nome: u.unidadeNome,
    situacao: formatSituacaoSubprocesso(u.situacao),
    ultimaDataLimite: u.ultimaDataLimite
  }));
});

const idsElegiveis = computed(() => unidadesElegiveis.value.map(u => u.codigo));

const contextoAceiteBloco = computed<ContextoBloco>(() => obterContextoBloco(unidadesElegiveisPorAcao.value.aceitar));
const contextoHomologacaoBloco = computed<ContextoBloco>(() => obterContextoBloco(unidadesElegiveisPorAcao.value.homologar));
const contextoDisponibilizacaoBloco = computed<ContextoBloco>(() => "misto");

const rotuloAcaoAceitarBloco = computed(() => obterConfiguracaoAcaoBloco("aceitar", contextoAceiteBloco.value).rotulo);

const rotuloAcaoHomologarBloco = computed(() => obterConfiguracaoAcaoBloco("homologar", contextoHomologacaoBloco.value).rotulo);

const rotuloAcaoDisponibilizarBloco = computed(() => obterConfiguracaoAcaoBloco("disponibilizar", contextoDisponibilizacaoBloco.value).rotulo);

const contextoAtualAcaoBloco = computed<ContextoBloco>(() => {
  if (acaoBlocoAtual.value === "aceitar") {
    return contextoAceiteBloco.value;
  }
  if (acaoBlocoAtual.value === "homologar") {
    return contextoHomologacaoBloco.value;
  }
  return contextoDisponibilizacaoBloco.value;
});

const tituloModalBloco = computed(() => obterConfiguracaoAcaoBloco(acaoBlocoAtual.value, contextoAtualAcaoBloco.value).titulo);

const textoModalBloco = computed(() => obterConfiguracaoAcaoBloco(acaoBlocoAtual.value, contextoAtualAcaoBloco.value).texto);

const rotuloBotaoBloco = computed(() => {
  switch (acaoBlocoAtual.value) {
    case "aceitar":
      return TEXTOS.acaoBloco.aceitar.BOTAO;
    case "homologar":
      return TEXTOS.acaoBloco.homologar.BOTAO;
    case "disponibilizar":
      return TEXTOS.acaoBloco.disponibilizar.BOTAO;
    default:
      return "";
  }
});

const mensagemSucessoAcaoBloco = computed(() => {
  return obterConfiguracaoAcaoBloco(acaoBlocoAtual.value, contextoAtualAcaoBloco.value).mensagemSucesso;
});

async function abrirDetalhesUnidade(row: any) {
  if (!row.clickable || !row.sigla) {
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
    clearError();
    await processoService.finalizarProcesso(codProcesso);
    toastStore.setPending(TEXTOS.sucesso.PROCESSO_FINALIZADO);
    await router.push("/painel");
  } catch (error: any) {
    lastError.value = normalizeError(error);
    const mensagem = lastError.value?.message || error.message || TEXTOS.processo.ERRO_PADRAO;
    notify(mensagem, 'danger');
  }
}

function abrirModalBloco(acao: AcaoBloco) {
  acaoBlocoAtual.value = acao;
  modalBlocoRef.value?.abrir();
}

async function executarAcaoBloco(dados: { ids: number[], dataLimite?: string }) {
  try {
    clearError();
    processandoAcaoBloco.value = true;
    modalBlocoRef.value?.setProcessando(true);
    const contextoExecucao = obterContextoAtualAcao(acaoBlocoAtual.value, dados.ids);
    const mensagemSucesso = obterConfiguracaoAcaoBloco(acaoBlocoAtual.value, contextoExecucao).mensagemSucesso;
    if (!processo.value) {
      throw new Error("Detalhes do processo não carregados.");
    }
    await processoService.executarAcaoEmBloco(processo.value.codigo, {
      unidadeCodigos: dados.ids,
      acao: acaoBlocoAtual.value,
      dataLimite: dados.dataLimite,
    });

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
    await carregarContextoCompleto();
  } catch (error: any) {
    lastError.value = normalizeError(error);
    modalBlocoRef.value?.setErro(error.message || TEXTOS.processo.ERRO_ACAO_BLOCO);
    modalBlocoRef.value?.setProcessando(false);
  } finally {
    processandoAcaoBloco.value = false;
  }
}

onMounted(async () => {
  if (codProcesso) {
    try {
      await carregarContextoCompleto();
    } catch {
      // O erro já foi convertido em estado local para exibição inline.
    }
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
