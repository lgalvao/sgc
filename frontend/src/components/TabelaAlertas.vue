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
      <template #empty>
        <div class="text-center text-muted py-5" data-testid="empty-state-alertas">
          <i class="bi bi-bell-slash display-4 d-block mb-3" aria-hidden="true"></i>
          <p class="h5">Tudo limpo!</p>
          <p class="small">Você não tem novos alertas no momento.</p>
        </div>
      </template>
    </BTable>
  </div>
</template>

<script lang="ts" setup>
import {BTable} from "bootstrap-vue-next";
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
