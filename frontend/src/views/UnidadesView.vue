<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.unidades.TITULO">
      <template #description>
        {{ TEXTOS.unidades.SUBTITULO }}
      </template>
      <template #actions>
        <BButton
            aria-label="Expandir todas as linhas"
            class="me-2"
            data-testid="btn-unidades-expandir-todas"
            size="sm"
            variant="outline-primary"
            @click="expandirTodasLinhas"
        >
          <i aria-hidden="true" class="bi bi-arrows-expand"/>
        </BButton>
        <BButton
            aria-label="Recolher todas as linhas"
            data-testid="btn-unidades-recolher-todas"
            size="sm"
            variant="outline-secondary"
            @click="recolherTodasLinhas"
        >
          <i aria-hidden="true" class="bi bi-arrows-collapse"/>
        </BButton>
      </template>
    </PageHeader>

    <div v-if="exibirAlertaDiagnostico" class="mb-3 pt-2">
      <BAlert
          :model-value="true"
          dismissible
          variant="warning"
          @dismissed="dispensarAlertaDiagnostico"
      >
        <strong>Há unidades sem responsável atual.</strong>
        <div class="mt-1">{{ resumoDiagnostico }}</div>
        <ul v-if="gruposDiagnostico.length > 0" class="mb-0 mt-2 ps-3">
          <li v-for="grupo in gruposDiagnostico" :key="grupo.tipo">
            {{ grupo.tipo }}: {{ grupo.quantidadeOcorrencias }} ocorrência(s)
          </li>
        </ul>
      </BAlert>
    </div>

    <BAlert
        v-if="erroUnidades"
        :model-value="true"
        dismissible
        variant="danger"
        @dismissed="clearError()"
    >
      {{ erroUnidades.message }}
    </BAlert>

    <div v-if="isLoading" class="text-center py-5">
      <BSpinner :label="TEXTOS.unidades.CARREGANDO" variant="primary"/>
      <p class="mt-2 text-muted">{{ TEXTOS.unidades.CARREGANDO_ARVORE }}</p>
    </div>

    <div v-else-if="dadosArvore.length > 0">
      <TreeTable
          ref="treeTableRef"
          :columns="colunas"
          :data="dadosArvore"
          :hide-controls="true"
          :hide-headers="true"
          :striped="false"
          @row-click="abrirDetalheUnidade"
      />
    </div>

    <EmptyState
        v-else
        :description="TEXTOS.unidades.EMPTY_DESCRIPTION"
        :title="TEXTOS.unidades.EMPTY_TITLE"
        icon="bi-diagram-3"
    >
      <BButton
          data-testid="btn-unidades-recarregar"
          size="sm"
          variant="outline-primary"
          @click="carregarUnidades"
      >
        {{ TEXTOS.unidades.BOTAO_ATUALIZAR }}
      </BButton>
    </EmptyState>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onActivated, onMounted, ref} from "vue";
import {BAlert, BButton, BSpinner} from "bootstrap-vue-next";
import {useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import TreeTable from "@/components/comum/TreeTable.vue";
import {buscarTodasUnidades} from "@/services/unidadeService";
import type {Unidade} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {usePerfil} from "@/composables/usePerfil";
import {useOrganizacaoStore} from "@/stores/organizacao";

type LinhaUnidadeArvore = {
  codigo: number;
  sigla: string;
  unidade: string;
  children: LinhaUnidadeArvore[];
  tipo?: string;
  expanded: boolean;
  clickable: boolean;
};

type TreeTableRef = {
  expandAll: () => void;
  collapseAll: () => void;
};

const unidades = ref<Unidade[]>([]);
const treeTableRef = ref<TreeTableRef | null>(null);
const {carregando: isLoading, erro, executarSilencioso} = useAsyncAction();
const {mostrarDiagnosticoOrganizacional} = usePerfil();
const organizacaoStore = useOrganizacaoStore();
const erroDiagnosticoOrganizacional = computed(() => organizacaoStore.erroDiagnostico);
const diagnosticoOrganizacional = computed(() => organizacaoStore.diagnostico);
const router = useRouter();
const carregamentoInicialConcluido = ref(false);

const erroUnidades = computed(() =>
    erro.value ? {message: erro.value} : null
);
const gruposDiagnostico = computed(() => diagnosticoOrganizacional.value?.grupos ?? []);
const resumoDiagnostico = computed(() =>
    erroDiagnosticoOrganizacional.value
    ?? diagnosticoOrganizacional.value?.resumo
    ?? ""
);
const alertaDiagnosticoDispensado = ref(false);
const exibirAlertaDiagnostico = computed(() =>
    mostrarDiagnosticoOrganizacional.value
    && (!!erroDiagnosticoOrganizacional.value || diagnosticoOrganizacional.value?.possuiViolacoes === true)
    && !alertaDiagnosticoDispensado.value
);

const colunas = [
  {key: "unidade", label: TEXTOS.subprocesso.COLUNA_UNIDADE, width: "100%"},
];

const unidadesExibidas = computed(() => {
  const lista: Unidade[] = [];

  for (const unidade of unidades.value) {
    if (unidade.filhas && unidade.filhas.length > 0) {
      lista.push(...unidade.filhas);
    }
  }

  return lista;
});

const dadosArvore = computed(() => mapearUnidadesParaLinhas(unidadesExibidas.value));

function dadosLocaisValidos(): boolean {
  return unidades.value.length > 0 && !erro.value;
}

function clearError() {
  erro.value = null;
}

function dispensarAlertaDiagnostico() {
  alertaDiagnosticoDispensado.value = true;
}

function expandirTodasLinhas() {
  treeTableRef.value?.expandAll();
}

function recolherTodasLinhas() {
  treeTableRef.value?.collapseAll();
}

async function carregarUnidades() {
  await executarSilencioso(async () => {
    unidades.value = await buscarTodasUnidades();
  }, TEXTOS.comum.ERRO_OPERACAO);
}

async function carregarDiagnostico() {
  await organizacaoStore.garantirDiagnostico(mostrarDiagnosticoOrganizacional.value);
}

function mapearUnidadeParaLinha(unidade: Unidade): LinhaUnidadeArvore {
  return {
    codigo: unidade.codigo,
    sigla: unidade.sigla,
    unidade: `${unidade.sigla} - ${unidade.nome}`,
    tipo: unidade.tipo,
    children: mapearUnidadesParaLinhas(unidade.filhas ?? []),
    expanded: true,
    clickable: true,
  };
}

function mapearUnidadesParaLinhas(unidadesOrigem: Unidade[]): LinhaUnidadeArvore[] {
  return unidadesOrigem.map(mapearUnidadeParaLinha);
}

function abrirDetalheUnidade(item: unknown) {
  const unidade = item as LinhaUnidadeArvore;
  void router.push({path: `/unidade/${unidade.codigo}`});
}

onMounted(() => {
  void carregarDiagnostico();
  void carregarUnidades();
  carregamentoInicialConcluido.value = true;
});

onActivated(() => {
  if (!carregamentoInicialConcluido.value) {
    return;
  }

  void carregarDiagnostico();
  if (!dadosLocaisValidos()) {
    void carregarUnidades();
  }
});

</script>
