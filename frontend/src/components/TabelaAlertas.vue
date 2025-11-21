<template>
  <BTable
    :items="alertas"
    :fields="fields"
    hover
    striped
    responsive
    data-testid="tabela-alertas"
    @row-clicked="emit('selecionar-alerta', $event)"
    @sort-changed="handleSortChange"
  >
    <template #empty>
      <div class="text-center text-muted">
        Nenhum alerta no momento.
      </div>
    </template>
  </BTable>
</template>

<script lang="ts" setup>
import {BTable} from "bootstrap-vue-next";
import type {Alerta} from "@/types/tipos";

defineProps<{
  alertas: Alerta[];
}>();

const emit = defineEmits<{
  (e: "ordenar", criterio: "data" | "processo"): void;
  (e: "selecionar-alerta", alerta: Alerta): void;
}>();

const fields = [
  {key: "dataHoraFormatada", label: "Data/Hora", sortable: true},
  {key: "mensagem", label: "Descrição"},
  {key: "processo", label: "Processo", sortable: true},
  {key: "origem", label: "Origem"},
];

const handleSortChange = (ctx: any) => {
  if (ctx.sortBy === "dataHoraFormatada") {
    emit("ordenar", "data");
  } else if (ctx.sortBy === "processo") {
    emit("ordenar", "processo");
  }
};
</script>
