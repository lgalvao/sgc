<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="isLoading"/>

    <template v-else>
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
      <ProcessoDiagnosticoAlert
          :carregando="carregandoDiagnosticoOrganizacional"
          :exibir="exibirAlertaDiagnostico"
          :grupos="gruposDiagnostico"
          :resumo="resumoDiagnostico"
          :unidades-sem-responsavel="unidadesSemResponsavel"
          @dismiss="dispensarAlertaDiagnostico"
      />

      <BAlert
          v-if="erroUnidades"
          :model-value="true"
          dismissible
          variant="danger"
          @dismissed="limparErro()"
      >
        {{ erroUnidades.message }}
      </BAlert>

      <div v-if="dadosArvore.length > 0">
        <ArvoreToolbar
            v-model:termo-busca="termoBusca"
            :exibir-acoes-expansao="false"
            :modo-selecao="false"
        />

        <TreeTable
            ref="treeTableRef"
            :columns="colunas"
            :data="dadosArvore"
            :hide-controls="true"
            :hide-headers="true"
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
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from "vue";
import {BAlert, BButton} from "bootstrap-vue-next";
import {useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import TreeTable from "@/components/comum/TreeTable.vue";
import ProcessoDiagnosticoAlert from "@/components/processo/ProcessoDiagnosticoAlert.vue";
import ArvoreToolbar from "@/components/unidade/ArvoreToolbar.vue";
import type {Unidade} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {useDiagnosticoOrganizacionalAlert} from "@/composables/useDiagnosticoOrganizacionalAlert";
import {usePerfil} from "@/composables/usePerfil";
import {useUnidadesQuery} from "@/composables/useUnidadesQuery";
import {normalizarErro} from "@/utils/apiError";

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

const treeTableRef = ref<TreeTableRef | null>(null);
const unidadesQuery = useUnidadesQuery();
const {mostrarDiagnosticoOrganizacional} = usePerfil();
const erroDispensado = ref(false);
const unidades = computed(() => unidadesQuery.data.value ?? []);
const isLoading = computed(() => unidadesQuery.isPending.value || unidadesQuery.isLoading.value);
const {
  carregandoDiagnosticoOrganizacional,
  gruposDiagnostico,
  resumoDiagnostico,
  unidadesSemResponsavel,
  exibirAlertaDiagnostico,
  dispensarAlertaDiagnostico,
} = useDiagnosticoOrganizacionalAlert(unidades, mostrarDiagnosticoOrganizacional);
const router = useRouter();
const termoBusca = ref("");

const erroUnidades = computed(() => {
  if (erroDispensado.value || !unidadesQuery.error.value) {
    return null;
  }
  return {message: normalizarErro(unidadesQuery.error.value).mensagem};
});
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

const unidadesFiltradas = computed(() => filtrarUnidadesPorSigla(unidadesExibidas.value, termoBusca.value));
const dadosArvore = computed(() => mapearUnidadesParaLinhas(unidadesFiltradas.value));

function limparErro() {
  erroDispensado.value = true;
}

function expandirTodasLinhas() {
  treeTableRef.value?.expandAll();
}

function recolherTodasLinhas() {
  treeTableRef.value?.collapseAll();
}

function filtrarUnidadesPorSigla(unidadesOrigem: Unidade[], termo: string, forceInclude = false): Unidade[] {
  const termoNormalizado = termo.trim().toLowerCase();
  if (!termoNormalizado && !forceInclude) {
    return unidadesOrigem;
  }

  const resultado: Unidade[] = [];

  for (const unidade of unidadesOrigem) {
    const corresponde = forceInclude || unidade.sigla.toLowerCase().includes(termoNormalizado);
    const filhasFiltradas = filtrarUnidadesPorSigla(unidade.filhas ?? [], termo, corresponde);

    if (corresponde || filhasFiltradas.length > 0) {
      resultado.push({
        ...unidade,
        filhas: filhasFiltradas
      });
    }
  }

  return resultado;
}

async function carregarUnidades() {
  erroDispensado.value = false;
  try {
    await unidadesQuery.refetch();
  } catch {
    // O estado de erro já fica registrado na query.
  }
}

function mapearUnidadeParaLinha(unidade: Unidade): LinhaUnidadeArvore {
  return {
    codigo: unidade.codigo,
    sigla: unidade.sigla,
    unidade: `${unidade.sigla} - ${unidade.nome}`,
    tipo: unidade.tipo,
    children: mapearUnidadesParaLinhas(unidade.filhas ?? []),
    expanded: false,
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

watch(() => unidadesQuery.error.value, (novoErro) => {
  if (novoErro) {
    erroDispensado.value = false;
  }
});

</script>
