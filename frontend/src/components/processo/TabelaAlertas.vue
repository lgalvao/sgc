<template>
  <div class="table-responsive">
    <BTable
        :fields="fields"
        :items="alertas"
        :striped="alertas.length > 0"
        :tbody-tr-attr="rowAttr"
        :tbody-tr-class="rowClass"
        data-testid="tbl-alertas"
        hover
        responsive
        show-empty
        stacked="md"
        @sort-changed="handleSortChange"
    >
      <template #cell(mensagem)="data">
        <span v-if="!data.item.dataHoraLeitura" class="visually-hidden">Não lido: </span>
        {{ data.value }}
      </template>

      <template #empty>
        <EmptyState
            class="border-0 bg-transparent mb-0"
            data-testid="empty-state-alertas"
            description="Não há alertas no momento. Atualize para verificar novas notificações."
            icon="bi-bell-slash"
            title="Nenhum alerta"
        >
          <BButton
              data-testid="btn-empty-state-alertas-atualizar"
              size="sm"
              variant="outline-primary"
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
  const sortBy = Array.isArray(ctx.sortBy) ? ctx.sortBy[0] : ctx.sortBy;
  const key = sortBy?.key || (typeof sortBy === 'string' ? sortBy : null);

  if (key === "dataHora") {
    emit("ordenar", "data");
  } else if (key === "processo") {
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
