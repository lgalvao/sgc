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

      <ProcessoAcoes
          :mostrar-botoes-bloco="mostrarBotoesBloco"
          :perfil="perfilStore.perfilSelecionado"
          :situacao-processo="processo.situacao"
          @finalizar="finalizarProcesso"
          @aceitar-bloco="abrirModalAcaoBloco('aceitar')"
          @homologar-bloco="abrirModalAcaoBloco('homologar')"
      />
    </div>

    <ModalAcaoBloco
        :mostrar="mostrarModalBloco"
        :tipo="tipoAcaoBloco"
        :unidades="unidadesSelecionadasBloco"
        @confirmar="confirmarAcaoBloco"
        @fechar="fecharModalBloco"
    />

    <ModalConfirmacao
        v-model="mostrarModalFinalizacao"
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
import ModalAcaoBloco, {type UnidadeSelecao,} from "@/components/ModalAcaoBloco.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import ProcessoAcoes from "@/components/ProcessoAcoes.vue";
import TreeTable from "@/components/TreeTableView.vue";

import {usePerfilStore} from "@/stores/perfil";
import {formatarSituacaoProcesso, formatarTipoProcesso} from "@/utils/formatters";
import {useProcessosStore} from "@/stores/processos";
import {useFeedbackStore} from "@/stores/feedback";
import type {Processo, UnidadeParticipante} from "@/types/tipos";

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
const {processoDetalhe, subprocessosElegiveis} = storeToRefs(processosStore);
const perfilStore = usePerfilStore();
const feedbackStore = useFeedbackStore();


const mostrarModalBloco = ref(false);
const tipoAcaoBloco = ref<"aceitar" | "homologar">("aceitar");
const unidadesSelecionadasBloco = ref<UnidadeSelecao[]>([]);
const mostrarModalFinalizacao = ref(false);

const codProcesso = computed(() =>
    Number(
        route.params.codProcesso || route.query.codProcesso,
    ),
);

onMounted(async () => {
  if (codProcesso.value) {
    await processosStore.buscarContextoCompleto(codProcesso.value);
  }
});

const processo = computed<Processo | undefined>(
    () => processoDetalhe.value || undefined,
);
const participantesHierarquia = computed<UnidadeParticipante[]>(
    () => processo.value?.unidades || [],
);

const colunasTabela = [
  {key: "nome", label: "Unidade", width: "40%"},
  {key: "situacao", label: "Situação", width: "20%"},
  {key: "dataLimite", label: "Data limite", width: "20%"},
  {key: "unidadeAtual", label: "Unidade Atual", width: "20%"},
];

const dadosFormatados = computed<TreeTableItem[]>(() =>
    formatarDadosParaArvore(participantesHierarquia.value),
);

function formatarData(data: string | null): string {
  if (!data) return "";
  const date = new Date(data);
  const dia = String(date.getDate()).padStart(2, "0");
  const mes = String(date.getMonth() + 1).padStart(2, "0");
  const ano = date.getFullYear();
  return `${dia}/${mes}/${ano}`;
}

function formatarDadosParaArvore(
    dados: UnidadeParticipante[],
): TreeTableItem[] {
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
    const perfilUsuario = perfilStore.perfilSelecionado;
    if (perfilUsuario === "ADMIN" || perfilUsuario === "GESTOR") {
      router.push({
        name: "Subprocesso",
        params: {
          codProcesso: codProcesso.value.toString(),
          siglaUnidade: String(item.unidadeAtual),
        },
      });
    } else if (
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

async function finalizarProcesso() {
  if (processo.value) {
    mostrarModalFinalizacao.value = true;
  }
}

async function executarFinalizacao() {
  if (!processo.value) return;
  try {
    await processosStore.finalizarProcesso(processo.value.codigo);
    feedbackStore.show(
        "Processo finalizado",
        "O processo foi finalizado. Todos os mapas de competências estão agora vigentes.",
        "success"
    );
    await router.push("/painel");
  } catch {
    // Erro já está em processosStore.lastError e será exibido pelo BAlert
  }
}

function abrirModalAcaoBloco(tipo: "aceitar" | "homologar") {
  tipoAcaoBloco.value = tipo;
  unidadesSelecionadasBloco.value = subprocessosElegiveis.value.map((pu) => ({
    sigla: pu.unidadeSigla,
    nome: pu.unidadeNome,
    situacao: pu.situacao || "Não iniciado",
    selecionada: true,
  }));
  mostrarModalBloco.value = true;
}

function fecharModalBloco() {
  mostrarModalBloco.value = false;
}

function fecharModalFinalizacao() {
  mostrarModalFinalizacao.value = false;
}

async function confirmarFinalizacao() {
  fecharModalFinalizacao();
  await executarFinalizacao();
}

async function confirmarAcaoBloco(unidades: UnidadeSelecao[]) {
  const unidadesSelecionadas = unidades
      .filter((u) => u.selecionada)
      .map((u) => u.sigla);
  if (unidadesSelecionadas.length === 0) {
    feedbackStore.show(
        "Nenhuma unidade selecionada",
        "Selecione ao menos uma unidade para processar.",
        "danger"
    );
    return;
  }
  try {
    await processosStore.processarCadastroBloco({
      codProcesso: codProcesso.value,
      unidades: unidadesSelecionadas,
      tipoAcao: tipoAcaoBloco.value,
      unidadeUsuario: String(perfilStore.unidadeSelecionada) || "",
    });
    feedbackStore.show(
        `Cadastros ${tipoAcaoBloco.value === 'aceitar' ? 'aceitos' : 'homologados'} em bloco!`,
        `Operação de ${tipoAcaoBloco.value} em bloco concluída com sucesso!`,
        "success"
    );
    fecharModalBloco();
    await router.push("/painel");
  } catch {
    // Erro já está em processosStore.lastError e será exibido pelo BAlert
    fecharModalBloco(); // Fecha o modal para mostrar o erro na tela (ou poderíamos mostrar no modal se passássemos o erro para ele)
  }
}

const mostrarBotoesBloco = computed(
    () => subprocessosElegiveis.value.length > 0,
);
</script>
