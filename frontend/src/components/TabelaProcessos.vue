<template>
  <div>
    <BTable
      :items="processos"
      :fields="fields"
      hover
      responsive
      data-testid="tabela-processos"
      :sort-by="[{key: criterioOrdenacao, order: direcaoOrdenacaoAsc ? 'asc' : 'desc'}]"
      :sort-desc="[!direcaoOrdenacaoAsc]"
      @row-clicked="handleSelecionarProcesso"
      @sort-changed="handleSortChange"
    >
      <template #empty>
        <div class="text-center text-muted">
          Nenhum processo encontrado.
        </div>
      </template>
    </BTable>
  </div>
</template>

<script lang="ts" setup>
import {BTable} from "bootstrap-vue-next";
import {computed} from "vue";
import type {ProcessoResumo} from "@/types/tipos";

const props = defineProps<{
  processos: ProcessoResumo[];
  criterioOrdenacao: keyof ProcessoResumo | "dataFinalizacao";
  direcaoOrdenacaoAsc: boolean;
  showDataFinalizacao?: boolean;
}>();

const emit = defineEmits<{
  (e: "ordenar", campo: keyof ProcessoResumo | "dataFinalizacao"): void;
  (e: "selecionarProcesso", processo: ProcessoResumo): void;
}>();

const fields = computed(() => {
  const baseFields = [
    {key: "descricao", label: "Descrição", sortable: true},
    {key: "tipo", label: "Tipo", sortable: true},
    {key: "unidadesParticipantes", label: "Unidades Participantes", sortable: false},
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
</script>
