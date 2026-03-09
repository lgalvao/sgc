<script lang="ts" setup>
import {BBadge, BButton, BTable} from "bootstrap-vue-next";
import {computed} from "vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {type ProcessoResumo, SituacaoProcesso} from "@/types/tipos";
import {formatDate, formatSituacaoProcesso, formatTipoProcesso} from "@/utils/formatters";

function getBadgeVariant(situacao: SituacaoProcesso | string) {
  if (situacao === SituacaoProcesso.FINALIZADO) return "success";
  if (situacao === SituacaoProcesso.EM_ANDAMENTO) return "primary";
  if (situacao === SituacaoProcesso.CRIADO) return "secondary";
  return "dark";
}

const props = defineProps<{
  processos: ProcessoResumo[];
  criterioOrdenacao: keyof ProcessoResumo | "dataFinalizacao";
  direcaoOrdenacaoAsc: boolean;
  showDataFinalizacao?: boolean;
  compacto?: boolean;
  mostrarCtaVazio?: boolean;
  textoCtaVazio?: string;
}>();

const emit = defineEmits<{
  (e: "ordenar", campo: keyof ProcessoResumo | "dataFinalizacao"): void;
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

  baseFields.push({key: "situacao", label: "Situação", sortable: true});
  return baseFields;
});

const internalSortBy = computed(() => [{
  key: props.criterioOrdenacao,
  order: (props.direcaoOrdenacaoAsc ? 'asc' : 'desc') as any
}]);

function handleSortChange(val: any) {
  const sortBy = Array.isArray(val) ? val[0] : val;
  if (sortBy?.key) {
    // Só emite se for realmente diferente do estado atual vindo das props
    if (sortBy.key !== props.criterioOrdenacao || (sortBy.order === 'asc') !== props.direcaoOrdenacaoAsc) {
      emit("ordenar", sortBy.key);
    }
  }
}

function handleSelecionarProcesso(processo: any) {
  const item = processo?.item || processo;
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
  <div class="table-responsive">
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
        show-empty
        stacked="md"
        @row-clicked="handleSelecionarProcesso"
        @update:sort-by="handleSortChange"
    >

      <template #empty>
        <EmptyState
            class="border-0 bg-transparent mb-0"
            data-testid="empty-state-processos"
            description="Os processos em que sua unidade participa aparecerão aqui."
            icon="bi-folder2-open"
            title="Nenhum processo encontrado">
          <BButton
              v-if="mostrarCtaVazio"
              data-testid="btn-empty-state-criar-processo"
              size="sm"
              variant="outline-primary"
              @click="emit('ctaVazio')"
          >
            {{ textoCtaVazio || 'Criar processo' }}
          </BButton>
        </EmptyState>
      </template>

      <template #cell(dataFinalizacao)="{ item }">
        {{ formatDate(item.dataFinalizacao) }}
      </template>

      <template #cell(situacao)="{ item }">
        <BBadge :variant="getBadgeVariant(item.situacao)" data-testid="badge-situacao">
          {{ formatSituacaoProcesso(item.situacao) }}
        </BBadge>
      </template>

      <template #cell(tipo)="{ item }">
        {{ formatTipoProcesso(item.tipo) }}
      </template>
    </BTable>
  </div>
</template>
