<template>
  <div class="mt-4">
    <h4>Movimentações do Processo</h4>
    <div
      v-if="movimentacoes.length === 0"
      class="alert alert-info"
    >
      Nenhuma movimentação registrada para este subprocesso.
    </div>
    <BTable
      v-else
      striped
      :items="movimentacoes"
      :fields="fields"
      primary-key="codigo"
    >
      <template #cell(dataHora)="data">
        {{ formatDateTimeBR(data.item.dataHora) }}
      </template>
    </BTable>
  </div>
</template>

<script lang="ts" setup>
import type {Movimentacao} from '@/types/tipos';
import {formatDateTimeBR} from '@/utils';
import {ref} from 'vue';
import {BTable} from 'bootstrap-vue-next';

defineProps<{
  movimentacoes: Movimentacao[]
}>();

const fields = ref([
  { key: 'dataHora', label: 'Data/Hora' },
  { key: 'unidadeOrigem', label: 'Unidade Origem' },
  { key: 'unidadeDestino', label: 'Unidade Destino' },
  { key: 'descricao', label: 'Descrição' }
]);
</script>
