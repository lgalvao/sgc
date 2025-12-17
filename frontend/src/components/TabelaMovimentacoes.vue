<template>
  <div class="mt-4">
    <h4>Movimentações</h4>
    <div
        v-if="movimentacoes.length === 0"
        class="alert alert-info"
    >
      Nenhuma movimentação registrada.
    </div>

    <BTable
        v-else
        :fields="fields"
        :items="movimentacoes"
        :tbody-tr-attr="rowAttr"
        data-testid="tbl-movimentacoes"
        primary-key="codigo"
        striped
    >
      <template #cell(dataHora)="data">
        {{ formatDateTimeBR(data.item.dataHora) }}
      </template>
      <template #cell(unidadeOrigem)="data">
        {{ data.item.unidadeOrigem.sigla }}
      </template>
      <template #cell(unidadeDestino)="data">
        {{ data.item.unidadeDestino.sigla }}
      </template>
    </BTable>
  </div>
</template>

<script lang="ts" setup>
import {BTable} from "bootstrap-vue-next";
import {ref} from "vue";
import type {Movimentacao} from "@/types/tipos";
import {formatDateTimeBR} from "@/utils";

defineProps<{
  movimentacoes: Movimentacao[];
}>();

const fields = ref([
  {key: "dataHora", label: "Data/Hora"},
  {key: "unidadeOrigem", label: "Unidade Origem"},
  {key: "unidadeDestino", label: "Unidade Destino"},
  {key: "descricao", label: "Descrição"}
]);

const rowAttr = (item: Movimentacao | null, type: string) => {
  return item && type === 'row'
      ? {'data-testid': `row-movimentacao-${item.codigo}`}
      : {}
};
</script>