<template>
  <BContainer class="mt-4">
    <BAlert
        v-if="processosStore.lastError"
        :model-value="true"
        variant="danger"
        dismissible
        @dismissed="processosStore.clearError()"
    >
      {{ processosStore.lastError.message }}
      <div v-if="processosStore.lastError.details">
        <small>Detalhes: {{ processosStore.lastError.details }}</small>
      </div>
    </BAlert>

    <div v-if="processo">
      <div>
        <BBadge
            class="mb-2"
            style="border-radius: 0"
            variant="secondary"
        >
          Detalhes do processo
        </BBadge>
        <h2
            class="display-6"
            data-testid="processo-info"
        >
          {{ processo.descricao }}
        </h2>
        <div class="mb-4 mt-3">
          <strong>Tipo:</strong> {{ formatarTipoProcesso(processo.tipo) }}<br>
          <strong>Situação:</strong> {{ formatarSituacaoProcesso(processo.situacao) }}<br>
        </div>
      </div>

      <TreeTable
          :columns="colunasTabela"
          :data="dadosFormatados"
          title="Unidades participantes"
          @row-click="abrirDetalhesUnidade"
      />

      <!-- Botões de Ação em Bloco -->
      <div v-if="mostrarBotoesBloco" class="mt-3 d-flex gap-2 justify-content-end">
          <button v-if="podeAceitarBloco" class="btn btn-success" @click="abrirModalBloco('aceitar')">
             Aceitar em Bloco
          </button>
          <button v-if="podeHomologarBloco" class="btn btn-warning text-white" @click="abrirModalBloco('homologar')">
             Homologar em Bloco
          </button>
          <button v-if="podeDisponibilizarBloco" class="btn btn-info text-white" @click="abrirModalBloco('disponibilizar')">
             Disponibilizar Mapas em Bloco
          </button>
      </div>

      <ProcessoAcoes
          :mostrar-botoes-bloco="false"
          :perfil="perfilStore.perfilSelecionado"
          :situacao-processo="processo.situacao"
          @finalizar="finalizarProcesso"
      />
    </div>

    <!-- Modal de Ação em Bloco -->
    <ModalAcaoBloco
        :id="'modal-acao-bloco'"
        ref="modalBlocoRef"
        :titulo="tituloModalBloco"
        :texto="textoModalBloco"
        :rotulo-botao="rotuloBotaoBloco"
        :unidades="unidadesElegiveis"
        :unidades-pre-selecionadas="idsElegiveis"
        :mostrar-data-limite="acaoBlocoAtual === 'disponibilizar'"
        @confirmar="executarAcaoBloco"
    />

    <ModalConfirmacao
        v-model="mostrarModalFinalizacao"
        test-id-cancelar="btn-finalizar-processo-cancelar"
        test-id-confirmar="btn-finalizar-processo-confirmar"
        titulo="Finalização de processo"
        variant="success"
        @confirmar="confirmarFinalizacao"
    >
      <BAlert
          :fade="false"
          :model-value="true"
          variant="info"
      >
        <i class="bi bi-info-circle"/>
        Confirma a finalização do processo <strong>{{ processo?.descricao || '' }}</strong>?<br>
        Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades
        participantes do processo.
      </BAlert>
    </ModalConfirmacao>
  </BContainer>
</template>

<script lang="ts" setup>
import {BAlert, BBadge, BContainer} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import ModalAcaoBloco, {type UnidadeSelecao} from "@/components/ModalAcaoBloco.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import ProcessoAcoes from "@/components/ProcessoAcoes.vue";
import TreeTable from "@/components/TreeTableView.vue";

import {usePerfilStore} from "@/stores/perfil";
import {formatarSituacaoProcesso, formatarTipoProcesso} from "@/utils/formatters";
import {useProcessosStore} from "@/stores/processos";
import {useFeedbackStore} from "@/stores/feedback";
import type {Processo, UnidadeParticipante} from "@/types/tipos";
import {SituacaoSubprocesso} from "@/types/tipos";
import * as subprocessoService from "@/services/subprocessoService";

interface TreeTableItem {
  codigo: number | string;
  nome: string;
  situacao: string;
  dataLimite: string;
  unidadeAtual: string;
  expanded: boolean;
  children: TreeTableItem[];
  clickable?: boolean;
  [key: string]: any;
}

const route = useRoute();
const router = useRouter();
const processosStore = useProcessosStore();
const {processoDetalhe} = storeToRefs(processosStore);
const perfilStore = usePerfilStore();
const feedbackStore = useFeedbackStore();

const modalBlocoRef = ref<InstanceType<typeof ModalAcaoBloco> | null>(null);
const mostrarModalFinalizacao = ref(false);
const acaoBlocoAtual = ref<'aceitar' | 'homologar' | 'disponibilizar' | null>(null);

const codProcesso = computed(() => Number(route.params.codProcesso || route.query.codProcesso));

onMounted(async () => {
  if (codProcesso.value) {
    await processosStore.buscarContextoCompleto(codProcesso.value);
  }
});

const processo = computed<Processo | undefined>(() => processoDetalhe.value || undefined);
const participantesHierarquia = computed<UnidadeParticipante[]>(() => processo.value?.unidades || []);

const colunasTabela = [
  {key: "nome", label: "Unidade", width: "40%"},
  {key: "situacao", label: "Situação", width: "20%"},
  {key: "dataLimite", label: "Data limite", width: "20%"},
  {key: "unidadeAtual", label: "Unidade Atual", width: "20%"},
];

const dadosFormatados = computed<TreeTableItem[]>(() => formatarDadosParaArvore(participantesHierarquia.value));

function formatarData(data: string | null): string {
  if (!data) return "";
  const date = new Date(data);
  const dia = String(date.getDate()).padStart(2, "0");
  const mes = String(date.getMonth() + 1).padStart(2, "0");
  const ano = date.getFullYear();
  return `${dia}/${mes}/${ano}`;
}

function formatarDadosParaArvore(dados: UnidadeParticipante[]): TreeTableItem[] {
  if (!dados) return [];
  return dados.map((item) => ({
    codigo: item.codUnidade,
    nome: item.sigla,
    situacao: item.situacaoSubprocesso || "Não iniciado",
    dataLimite: formatarData(item.dataLimite || null),
    unidadeAtual: item.sigla,
    clickable: true,
    expanded: true,
    children: item.filhos ? formatarDadosParaArvore(item.filhos) : [],
  }));
}

function abrirDetalhesUnidade(item: any) {
  if (item && item.clickable) {
    // Usar isAdmin/isGestor que verificam a lista de perfis, não perfilSelecionado
    // pois perfilSelecionado pode ser null mesmo quando o usuário tem o perfil
    if (perfilStore.isAdmin || perfilStore.isGestor) {
      router.push({
        name: "Subprocesso",
        params: {
          codProcesso: codProcesso.value.toString(),
          siglaUnidade: String(item.unidadeAtual),
        },
      });
    } else {
      // Para CHEFE/SERVIDOR, só navega para sua própria unidade
      const perfilUsuario = perfilStore.perfilSelecionado;
      if (
          (perfilUsuario === "CHEFE" || perfilUsuario === "SERVIDOR") &&
          perfilStore.unidadeSelecionada === item.codigo
      ) {
        router.push({
          name: "Subprocesso",
          params: {
            codProcesso: String(codProcesso.value),
            siglaUnidade: String(item.unidadeAtual),
          },
        });
      }
    }
  }
}

async function finalizarProcesso() {
  if (processo.value) mostrarModalFinalizacao.value = true;
}

async function confirmarFinalizacao() {
  if (!processo.value) return;
  mostrarModalFinalizacao.value = false;
  try {
    await processosStore.finalizarProcesso(processo.value.codigo);
    feedbackStore.show(
        "Processo finalizado",
        "O processo foi finalizado com sucesso.",
        "success"
    );
    await router.push("/painel");
  } catch (error: any) {
    feedbackStore.show("Erro ao finalizar", error.message || "Erro desconhecido", "danger");
  }
}

// --- Lógica de Ações em Bloco ---

function toSituacao(s: string): SituacaoSubprocesso | string {
    return s as SituacaoSubprocesso;
}

const unidadesElegiveis = computed<UnidadeSelecao[]>(() => {
    const flatten = (nodes: UnidadeParticipante[]): UnidadeParticipante[] => {
        let res: UnidadeParticipante[] = [];
        for (const node of nodes) {
            res.push(node);
            if (node.filhos) res = res.concat(flatten(node.filhos));
        }
        return res;
    };

    const all = flatten(participantesHierarquia.value);

    if (acaoBlocoAtual.value === 'aceitar') {
        return all.filter(u =>
            toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
            toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
            toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO // Para CDU-25
        ).map(u => ({ codigo: u.codUnidade, sigla: u.sigla, nome: u.nome, situacao: u.situacaoSubprocesso, selecionada: true }));
    } else if (acaoBlocoAtual.value === 'homologar') {
         return all.filter(u =>
            toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
            toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO // Para CDU-26
         ).map(u => ({ codigo: u.codUnidade, sigla: u.sigla, nome: u.nome, situacao: u.situacaoSubprocesso, selecionada: true }));
    } else if (acaoBlocoAtual.value === 'disponibilizar') {
        return all.filter(u =>
            toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO ||
            toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO
        ).map(u => ({ codigo: u.codUnidade, sigla: u.sigla, nome: u.nome, situacao: u.situacaoSubprocesso, selecionada: true }));
    }

    return [];
});

const idsElegiveis = computed(() => unidadesElegiveis.value.map(u => u.codigo));

const mostrarBotoesBloco = computed(() => {
    const flatten = (nodes: UnidadeParticipante[]): UnidadeParticipante[] => {
        let res: UnidadeParticipante[] = [];
        for (const node of nodes) {
            res.push(node);
            if (node.filhos) res = res.concat(flatten(node.filhos));
        }
        return res;
    };
    const all = flatten(participantesHierarquia.value);

    const temDisponivelAceite = all.some(u =>
        toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
        toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
        toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

    const temDisponivelHomolog = all.some(u =>
        toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
        toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

    const temDisponivelMapa = all.some(u =>
        toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO ||
        toSituacao(u.situacaoSubprocesso) === SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

    return (temDisponivelAceite || temDisponivelHomolog || temDisponivelMapa);
});

const podeAceitarBloco = computed(() => perfilStore.isGestor || perfilStore.isAdmin);
const podeHomologarBloco = computed(() => perfilStore.isAdmin);
const podeDisponibilizarBloco = computed(() => perfilStore.isAdmin);

const tituloModalBloco = computed(() => {
    switch(acaoBlocoAtual.value) {
        case 'aceitar': return 'Aceitar em Bloco';
        case 'homologar': return 'Homologar em Bloco';
        case 'disponibilizar': return 'Disponibilizar Mapas em Bloco';
        default: return '';
    }
});

const textoModalBloco = computed(() => {
     switch(acaoBlocoAtual.value) {
        case 'aceitar': return 'Selecione as unidades para aceitar:';
        case 'homologar': return 'Selecione as unidades para homologar:';
        case 'disponibilizar': return 'Selecione as unidades cujos mapas serão disponibilizados:';
        default: return '';
    }
});

const rotuloBotaoBloco = computed(() => {
     switch(acaoBlocoAtual.value) {
        case 'aceitar': return 'Aceitar';
        case 'homologar': return 'Homologar';
        case 'disponibilizar': return 'Disponibilizar';
        default: return 'Confirmar';
    }
});

function abrirModalBloco(acao: 'aceitar' | 'homologar' | 'disponibilizar') {
    acaoBlocoAtual.value = acao;
    modalBlocoRef.value?.abrir();
}

async function executarAcaoBloco(dados: { ids: number[], dataLimite?: string }) {
    if (!acaoBlocoAtual.value || !processo.value) return;

    // Assumir o contexto baseado na situação das unidades selecionadas
    // Simplificação: Pegamos a primeira unidade selecionada para decidir qual endpoint chamar
    const unidadeExemplo = participantesHierarquia.value.find(u => u.codUnidade === dados.ids[0])
        || unidadesElegiveis.value.find(u => u.codigo === dados.ids[0]);

    if (!unidadeExemplo) {
        feedbackStore.show('Erro', 'Não foi possível identificar o contexto do subprocesso.', 'danger');
        return;
    }

    // Precisamos de um ID de subprocesso válido para usar como base na API
    // Se unidadeExemplo veio de participantesHierarquia, ela tem codSubprocesso.
    // Se veio de unidadesElegiveis (UnidadeSelecao), não tem codSubprocesso explícito no tipo atual,
    // mas a computação de unidadesElegiveis mapeia codUnidade.
    // Vamos tentar encontrar o subprocesso correspondente nos participantes para garantir o ID.

    // Fallback: search in full tree if unidadeExemplo doesn't have codSubprocesso (e.g. if it came from a simplified object)
    let codSubprocessoBase = (unidadeExemplo as any).codSubprocesso;

    if (!codSubprocessoBase) {
        const fullNode = participantesHierarquia.value.find(u => u.codUnidade === dados.ids[0]);
        if (fullNode) codSubprocessoBase = fullNode.codSubprocesso;
    }

    // Se ainda não achou (ex: unidade não carregada na árvore inicial?), abortar ou usar heurística
    if (!codSubprocessoBase) {
        feedbackStore.show('Erro', 'Não foi possível identificar o contexto do subprocesso.', 'danger');
        return;
    }

    // Type assertion is safe here as we check properties or fallbacks
    const situacao = toSituacao((unidadeExemplo as any).situacaoSubprocesso || (unidadeExemplo as any).situacao);

    const payload = {
        unidadeCodigos: dados.ids,
        dataLimite: dados.dataLimite
    };

    try {
        if (acaoBlocoAtual.value === 'aceitar') {
            if (situacao === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                situacao === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
                await subprocessoService.aceitarCadastroEmBloco(codSubprocessoBase, payload);
            } else if (situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
                       situacao === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO) {
                await subprocessoService.aceitarValidacaoEmBloco(codSubprocessoBase, payload);
            }
        } else if (acaoBlocoAtual.value === 'homologar') {
            if (situacao === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                situacao === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
                await subprocessoService.homologarCadastroEmBloco(codSubprocessoBase, payload);
            } else if (situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
                       situacao === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO) {
                await subprocessoService.homologarValidacaoEmBloco(codSubprocessoBase, payload);
            }
        } else if (acaoBlocoAtual.value === 'disponibilizar') {
            await subprocessoService.disponibilizarMapaEmBloco(codSubprocessoBase, payload);
        }

        feedbackStore.show('Sucesso', `Ação ${acaoBlocoAtual.value} realizada em bloco.`, 'success');
        modalBlocoRef.value?.fechar();

        // Recarregar
        await processosStore.buscarContextoCompleto(codProcesso.value);

    } catch (e: any) {
        modalBlocoRef.value?.setErro(e.message || "Erro ao processar em bloco.");
        modalBlocoRef.value?.setProcessando(false);
    }
}
</script>
