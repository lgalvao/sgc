<script lang="ts" setup>
import {BTable} from "bootstrap-vue-next";
import {computed} from "vue";
import type {ProcessoResumo} from "@/types/tipos";
import {formatarSituacaoProcesso, formatarTipoProcesso} from "@/utils/formatters";

const props = defineProps<{
  processos: ProcessoResumo[];
  criterioOrdenacao: keyof ProcessoResumo | "dataFinalizacao";
  direcaoOrdenacaoAsc: boolean;
  showDataFinalizacao?: boolean;
  compacto?: boolean;
}>();

/**
 * TabelaProcessos - Componente de apresentação "leve"
 *
 * IMPORTANTE: Este componente NÃO ordena dados localmente.
 * A ordenação é SERVER-SIDE e funciona da seguinte forma:
 *
 * 1. Usuário clica em coluna ordenável
 * 2. BTable emite evento @sort-changed
 * 3. Componente emite evento 'ordenar' para o pai (view)
 * 4. View chama store com parâmetros de ordenação
 * 5. Store chama backend com query params (ex: ?sort=descricao&order=asc)
 * 6. Backend retorna dados ordenados
 * 7. View atualiza prop 'processos' com novos dados
 *
 * Props 'criterioOrdenacao' e 'direcaoOrdenacaoAsc' são usadas apenas
 * para indicar visualmente qual coluna está ordenada (setas na UI).
 *
 * Em modo 'compacto', a ordenação visual e funcional é desativada.
 */
const emit = defineEmits<{
  (e: "ordenar", campo: keyof ProcessoResumo | "dataFinalizacao"): void;
  (e: "selecionarProcesso", processo: ProcessoResumo): void;
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

const handleSortChange = (ctx: any) => {
  emit("ordenar", ctx.sortBy);
};

const handleSelecionarProcesso = (processo: ProcessoResumo) => {
  emit("selecionarProcesso", processo);
};

const rowClass = (item: ProcessoResumo | null, type: string) => {
  if (item && type === 'row') {
    return `row-processo-${item.codigo}`;
  }
  return '';
};

const rowAttr = (item: ProcessoResumo | null, type: string) => {
  if (item && type === 'row') {
    return {
      tabindex: '0',
      onKeydown: (e: KeyboardEvent) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          handleSelecionarProcesso(item);
        }
      }
    };
  }
  return {};
};
</script>

<template>
  <div>
    <BTable
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
        @sort-changed="handleSortChange"
    >
      <template #empty>
        <div class="text-center text-muted">
          Nenhum processo encontrado.
        </div>
      </template>

      <template #cell(situacao)="data">
        {{ formatarSituacaoProcesso(data.value as string) }}
      </template>

      <template #cell(tipo)="data">
        {{ formatarTipoProcesso(data.value as string) }}
      </template>
    </BTable>
  </div>
</template>
