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
        <div v-if="erroDiagnosticoOrganizacional" class="mt-1">{{ erroDiagnosticoOrganizacional }}</div>
        <div v-else-if="unidadesSemResponsavel.length > 0" class="mt-1">
          {{ prefixoMensagemUnidadesSemResponsavel }}
          <template v-for="(unidade, indice) in unidadesSemResponsavel" :key="unidade.sigla">
            <span v-if="indice > 0">{{ separadorListaUnidades(indice, unidadesSemResponsavel.length) }}</span>
            <RouterLink
                v-if="unidade.codigo !== null"
                :to="`/unidade/${unidade.codigo}`"
                :data-testid="`link-unidade-sem-responsavel-${indice}`"
            >
              <strong>{{ unidade.sigla }}</strong>
            </RouterLink>
            <strong v-else>{{ unidade.sigla }}</strong>
          </template>
          {{ sufixoMensagemUnidadesSemResponsavel }} A responsabilidade
          deve ser definida externamente, no SGRH, ou por atribuição temporária no próprio sistema.
        </div>
        <div v-else class="mt-1">{{ resumoDiagnostico }}</div>
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
import {RouterLink, useRouter} from "vue-router";
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
const resumoDiagnostico = computed(() =>
    erroDiagnosticoOrganizacional.value
    ?? diagnosticoOrganizacional.value?.resumo
    ?? ""
);
const unidadesSemResponsavel = computed(() => {
  const grupo = diagnosticoOrganizacional.value?.grupos.find((item) => item.tipo === "Unidade sem responsável");
  if (!grupo || grupo.ocorrencias.length === 0) {
    return [];
  }

  return grupo.ocorrencias
      .map(extrairSiglaUnidade)
      .filter((sigla): sigla is string => Boolean(sigla))
      .map((sigla) => ({
        sigla,
        codigo: buscarCodigoUnidadePorSigla(unidades.value, sigla),
      }));
});
const prefixoMensagemUnidadesSemResponsavel = computed(() =>
    unidadesSemResponsavel.value.length > 1 ? "As unidades " : "A unidade "
);
const sufixoMensagemUnidadesSemResponsavel = computed(() =>
    unidadesSemResponsavel.value.length > 1
        ? " estão atualmente sem responsável. Enquanto isso, não poderão participar de processos."
        : " está atualmente sem responsável. Enquanto isso, não poderá participar de processos."
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

function extrairSiglaUnidade(ocorrencia: string): string | null {
  if (!ocorrencia) {
    return null;
  }

  const correspondencia = ocorrencia.match(/^sigla=([^,]+?)(?:,\s|$)/);
  return correspondencia?.[1]?.trim() || null;
}

function separadorListaUnidades(indice: number, total: number): string {
  if (indice === total - 1) {
    return total === 2 ? " e " : ", e ";
  }

  return ", ";
}

function buscarCodigoUnidadePorSigla(unidadesOrigem: Unidade[], sigla: string): number | null {
  for (const unidade of unidadesOrigem) {
    if (unidade.sigla === sigla) {
      return unidade.codigo;
    }

    const codigoFilha = buscarCodigoUnidadePorSigla(unidade.filhas ?? [], sigla);
    if (codigoFilha !== null) {
      return codigoFilha;
    }
  }

  return null;
}

async function carregarUnidades() {
  await executarSilencioso(async () => {
    unidades.value = await buscarTodasUnidades();
  }, TEXTOS.comum.ERRO_OPERACAO);
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
  void carregarUnidades();
  carregamentoInicialConcluido.value = true;
});

onActivated(() => {
  if (!carregamentoInicialConcluido.value) {
    return;
  }

  if (!dadosLocaisValidos()) {
    void carregarUnidades();
  }
});

</script>
