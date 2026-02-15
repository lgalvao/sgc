<script lang="ts" setup>
import {BButton, BTable} from "bootstrap-vue-next";
import {computed} from "vue";
import EmptyState from "@/components/EmptyState.vue";
import BadgeSituacao from "@/components/comum/BadgeSituacao.vue";
import type {ProcessoResumo} from "@/types/tipos";

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
      key: "dataFinalizacaoFormatada",
      label: "Finalizado em",
      sortable: true,
    });
  }

  baseFields.push({key: "situacao", label: "Situação", sortable: true});
  return baseFields;
});

function handleSortChange(ctx: any) {
  emit("ordenar", ctx.sortBy);
}

function handleSelecionarProcesso(processo: any) {
  // O BTable do bootstrap-vue-next pode emitir o item diretamente ou um objeto com {item, index, event}
  const item = processo?.item || processo;
  emit("selecionarProcesso", item);
}

function rowClass(item: ProcessoResumo | null, type: string) {
  return item && type === 'row' ? `row-processo-${item.codigo}` : '';
}

function rowAttr(item: ProcessoResumo | null, type: string) {
  if (item && type === 'row') {
    return {
      tabindex: '0',
      style: {cursor: 'pointer'},
      onKeydown: (e: KeyboardEvent) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          handleSelecionarProcesso(item);
        }
      }
    };
  }
  return {};
}

defineExpose({ fields });
</script>

<template>
  <div class="table-responsive">
    <BTable
        aria-label="Lista de processos cadastrados"
        :fields="fields"
        :items="processos"
        :sort-by="[{key: criterioOrdenacao, order: direcaoOrdenacaoAsc ? 'asc' : 'desc'}]"
        :sort-desc="[!direcaoOrdenacaoAsc]"
        :tbody-tr-attr="rowAttr"
        :tbody-tr-class="rowClass"
        data-testid="tbl-processos"
        hover
        responsive
        show-empty
        @row-clicked="handleSelecionarProcesso"
        @sort-changed="handleSortChange">

      <template #empty>
        <EmptyState
            icon="bi-folder2-open"
            title="Nenhum processo encontrado"
            description="Os processos em que sua unidade participa aparecerão aqui."
            data-testid="empty-state-processos"
            class="border-0 bg-transparent mb-0">
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

      <template #cell(situacao)="{ item }">
        <BadgeSituacao :situacao="item.situacao" :texto="item.situacaoLabel" />
      </template>

      <template #cell(tipo)="{ item }">
        {{ item.tipoLabel }}
      </template>
    </BTable>
  </div>
</template>
