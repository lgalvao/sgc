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
        stacked="md"
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
            title="Nenhum alerta"
            description="Não há alertas no momento. Atualize para verificar novas notificações."
            data-testid="empty-state-alertas"
            class="border-0 bg-transparent mb-0"
        >
          <BButton
              size="sm"
              variant="outline-primary"
              data-testid="btn-empty-state-alertas-atualizar"
              @click="$emit('recarregar')"
          >
            Atualizar alertas
          </BButton>
        </EmptyState>
      </template>
    </BTable>
  </div>
</template>

<script lang="ts" setup>
import {BButton, BTable} from "bootstrap-vue-next";
import EmptyState from "@/components/comum/EmptyState.vue";
import type {Alerta} from "@/types/tipos";
import {formatDateBR} from "@/utils";

defineProps<{
  alertas: Alerta[];
}>();

const emit = defineEmits<{
  'ordenar': [criterio: "data" | "processo"];
  'recarregar': [];
}>();

const fields = [
  {key: "dataHora", label: "Data/Hora", sortable: true, formatter: (v: any) => formatDateBR(v)},
  {key: "mensagem", label: "Descrição"},
  {key: "processo", label: "Processo", sortable: true},
  {key: "origem", label: "Origem"},
];

const rowClass = (item: Alerta | null) => {
  if (!item) return "";
  return item.dataHoraLeitura ? "" : "fw-bold";
};

const handleSortChange = (ctx: any) => {
  if (ctx.sortBy === "dataHora") {
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
