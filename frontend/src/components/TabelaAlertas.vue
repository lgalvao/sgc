<template>
  <div class="table-responsive">
    <BTable
        :fields="fields"
        :items="alertas"
        :tbody-tr-attr="rowAttr"
        :tbody-tr-class="rowClass"
        data-testid="tbl-alertas"
        hover
        responsive
        show-empty
        :striped="alertas.length > 0"
        @sort-changed="handleSortChange"
    >
      <template #cell(mensagem)="data">
        <span v-if="!data.item.dataHoraLeitura" class="visually-hidden">Não lido: </span>
        {{ data.value }}
      </template>

      <template #empty>
        <EmptyState
            icon="bi-bell-slash"
            title="Tudo limpo!"
            description="Você não tem novos alertas no momento."
            data-testid="empty-state-alertas"
            class="border-0 bg-transparent mb-0"
        />
      </template>
    </BTable>
  </div>
</template>

<script lang="ts" setup>
import {BTable} from "bootstrap-vue-next";
import EmptyState from "@/components/EmptyState.vue";
import type {Alerta} from "@/types/tipos";

defineProps<{
  alertas: Alerta[];
}>();

const emit = defineEmits<{
  (e: "ordenar", criterio: "data" | "processo"): void;
}>();

const fields = [
  {key: "dataHoraFormatada", label: "Data/Hora", sortable: true},
  {key: "mensagem", label: "Descrição"},
  {key: "processo", label: "Processo", sortable: true},
  {key: "origem", label: "Origem"},
];

const rowClass = (item: Alerta) => {
  return !item.dataHoraLeitura ? "fw-bold" : "";
};

const handleSortChange = (ctx: any) => {
  if (ctx.sortBy === "dataHoraFormatada") {
    emit("ordenar", "data");
  } else if (ctx.sortBy === "processo") {
    emit("ordenar", "processo");
  }
};

const rowAttr = (item: Alerta | null, type: string) => {
  if (item && type === 'row') {
    return {'data-testid': `row-alerta-${item.codigo}`};
  }
  return {};
};
</script>
