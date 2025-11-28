<template>
  <BContainer class="mt-4">
    <div v-if="processo">
      <ProcessoDetalhes
        :descricao="processo.descricao"
        :tipo="processo.tipo"
        :situacao="processo.situacao"
      />

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
        @aceitar-bloco="abrirModalAcaoBloco('aceitar')"
        @homologar-bloco="abrirModalAcaoBloco('homologar')"
        @finalizar="finalizarProcesso"
      />
    </div>

    <ModalAcaoBloco
      :mostrar="mostrarModalBloco"
      :tipo="tipoAcaoBloco"
      :unidades="unidadesSelecionadasBloco"
      @fechar="fecharModalBloco"
      @confirmar="confirmarAcaoBloco"
    />

    <ModalFinalizacao
      :mostrar="mostrarModalFinalizacao"
      :processo-descricao="processo?.descricao || ''"
      @fechar="fecharModalFinalizacao"
      @confirmar="confirmarFinalizacao"
    />

    <!-- Alerta de sucesso -->
    <BAlert
      v-if="mostrarAlertaSucesso"
      variant="success"
      class="position-fixed"
      style="top: 20px; right: 20px; z-index: 9999;"
      dismissible
      :model-value="true"
      @dismissed="mostrarAlertaSucesso = false"
    >
      <i class="bi bi-check-circle" />
      Cadastros {{ tipoAcaoBloco === 'aceitar' ? 'aceitos' : 'homologados' }} em bloco!
    </BAlert>
  </BContainer>
</template>

<script lang="ts" setup>
import {BAlert, BContainer} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import ModalAcaoBloco, {type UnidadeSelecao,} from "@/components/ModalAcaoBloco.vue";
import ModalFinalizacao from "@/components/ModalFinalizacao.vue";
import ProcessoAcoes from "@/components/ProcessoAcoes.vue";
import ProcessoDetalhes from "@/components/ProcessoDetalhes.vue";
import TreeTable from "@/components/TreeTableView.vue";
import {useNotificacoesStore} from "@/stores/notificacoes";
import {usePerfilStore} from "@/stores/perfil";
import {useProcessosStore} from "@/stores/processos";
import type {Processo, UnidadeParticipante} from "@/types/tipos";

interface TreeTableItem {
  id: number | string;
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
const notificacoesStore = useNotificacoesStore();

const mostrarModalBloco = ref(false);
const tipoAcaoBloco = ref<"aceitar" | "homologar">("aceitar");
const unidadesSelecionadasBloco = ref<UnidadeSelecao[]>([]);
const mostrarAlertaSucesso = ref(false);
const mostrarModalFinalizacao = ref(false);

const codProcesso = computed(() =>
    Number(
        route.params.codProcesso || route.params.id || route.query.codProcesso,
    ),
);

onMounted(async () => {
  if (codProcesso.value) {
    await processosStore.buscarProcessoDetalhe(codProcesso.value);
    await processosStore.buscarSubprocessosElegiveis(codProcesso.value);
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
    id: item.codUnidade,
    nome: `${item.sigla} - ${item.nome}`,
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
        perfilStore.unidadeSelecionada === item.id
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
    notificacoesStore.sucesso(
        "Processo finalizado",
        "O processo foi finalizado. Todos os mapas de competências estão agora vigentes.",
    );
    await router.push("/painel");
  } catch {
    notificacoesStore.erro(
        "Erro ao finalizar processo",
        "Ocorreu um erro durante a finalização. Tente novamente.",
    );
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

function confirmarFinalizacao() {
  fecharModalFinalizacao();
  executarFinalizacao();
}

async function confirmarAcaoBloco(unidades: UnidadeSelecao[]) {
  const unidadesSelecionadas = unidades
      .filter((u) => u.selecionada)
      .map((u) => u.sigla);
  if (unidadesSelecionadas.length === 0) {
    notificacoesStore.erro(
        "Nenhuma unidade selecionada",
        "Selecione ao menos uma unidade para processar.",
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
    mostrarAlertaSucesso.value = true;
    fecharModalBloco();
    setTimeout(() => {
      mostrarAlertaSucesso.value = false;
      router.push("/painel");
    }, 2000);
  } catch {
    notificacoesStore.erro(
        "Erro ao processar em bloco",
        "Ocorreu um erro ao processar os cadastros em bloco.",
    );
  }
}

const mostrarBotoesBloco = computed(
    () => subprocessosElegiveis.value.length > 0,
);
</script>
