<script lang="ts" setup>
import {BBadge, BButton, BTable, type BTableSortBy, type ColorVariant} from "bootstrap-vue-next";
import {computed} from "vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {TEXTOS} from "@/constants/textos";
import {type ProcessoResumo} from "@/types/tipos";
import {formatDate, formatSituacaoProcesso, formatTipoProcesso} from "@/utils/formatters";
import {getProcessoBadgeVariant} from "@/utils/statusHelpers";

type CampoOrdenacaoProcesso = keyof ProcessoResumo | "dataFinalizacao";
type EventoLinhaProcesso = ProcessoResumo | { item: ProcessoResumo };

const props = withDefaults(defineProps<{
  processos: ProcessoResumo[];
  criterioOrdenacao: CampoOrdenacaoProcesso;
  direcaoOrdenacaoAsc: boolean;
  showDataFinalizacao?: boolean;
  showSituacao?: boolean;
  compacto?: boolean;
  mostrarCtaVazio?: boolean;
  textoCtaVazio?: string;
  emptyTitle?: string;
  emptyDescription?: string;
}>(), {
  showSituacao: true,
  textoCtaVazio: "",
});

const emit = defineEmits<{
  (e: "ordenar", campo: CampoOrdenacaoProcesso): void;
  (e: "selecionarProcesso", processo: ProcessoResumo): void;
  (e: "ctaVazio"): void;
}>();

const fields = computed(() => {
  const unidadesLabel = props.compacto ? "Unidades" : "Unidades participantes";

  const baseFields = [
    {key: "descricao", label: "Descrição", sortable: true},
    {key: "tipo", label: "Tipo", sortable: true},
    {key: "unidadesParticipantes", label: unidadesLabel, sortable: false},
  ];

  if (props.showDataFinalizacao) {
    baseFields.push({
      key: "dataFinalizacao",
      label: "Finalizado em",
      sortable: true,
    });
  }

  if (props.showSituacao !== false) {
    baseFields.push({key: "situacao", label: "Situação", sortable: true});
  }
  return baseFields;
});

const internalSortBy = computed(() => [{
  key: props.criterioOrdenacao,
  order: props.direcaoOrdenacaoAsc ? 'asc' : 'desc'
}] satisfies BTableSortBy[]);

function handleSortChange(val: readonly BTableSortBy[] | undefined) {
  const sortBy = Array.isArray(val) ? val[0] : val;
  if (!sortBy) {
    return;
  }

  if (sortBy.key !== props.criterioOrdenacao || (sortBy.order === 'asc') !== props.direcaoOrdenacaoAsc) {
    emit("ordenar", sortBy.key);
  }
}

function handleSelecionarProcesso(processo: EventoLinhaProcesso) {
  const item = "item" in processo ? processo.item : processo;
  emit("selecionarProcesso", item);
}

function rowClass(item: ProcessoResumo | null, type: string) {
  return item && type === 'row' ? `row-processo-${item.codigo}` : '';
}

function rowAttr(item: ProcessoResumo | null, type: string) {
  if (item && type === 'row') {
    return {
      'data-testid': `row-processo-${item.codigo}`,
      tabindex: "0",
      style: {cursor: "pointer"},
      onKeydown: (e: KeyboardEvent) => {
        const key = e.key.toLowerCase();
        if (key === "enter" || key === " " || key === "space") {
          e.preventDefault();
          handleSelecionarProcesso(item);
        }
      }
    };
  }
  return {};
}

defineExpose({fields});
</script>

<template>
  <div v-if="processos.length > 0" class="table-responsive">
    <BTable
        :fields="fields"
        :items="processos"
        :sort-by="internalSortBy"
        :tbody-tr-attrs="rowAttr"
        :tbody-tr-class="rowClass"
        aria-label="Lista de processos cadastrados"
        data-testid="tbl-processos"
        hover
        responsive
        stacked="md"
        @row-clicked="handleSelecionarProcesso"
        @update:sort-by="handleSortChange"
    >
      <template #cell(dataFinalizacao)="{ item }">
        {{ formatDate(item.dataFinalizacao, false) }}
      </template>

      <template #cell(situacao)="{ item }">
        <BBadge :variant="(getProcessoBadgeVariant(item.situacao) as ColorVariant)" data-testid="badge-situacao">
          {{ formatSituacaoProcesso(item.situacao) }}
        </BBadge>
      </template>

      <template #cell(tipo)="{ item }">
        {{ formatTipoProcesso(item.tipo) }}
      </template>
    </BTable>
  </div>
  <EmptyState
      v-else
      :description="emptyDescription || TEXTOS.tabelaProcessos.EMPTY_DESCRIPTION"
      :title="emptyTitle || TEXTOS.tabelaProcessos.EMPTY_TITLE"
      class="mb-0"
      data-testid="empty-state-processos"
      icon="bi-folder2-open"
  >
  </EmptyState>
</template>
