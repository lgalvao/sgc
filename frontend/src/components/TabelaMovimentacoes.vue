<template>
  <div class="mt-4">
    <h4>Movimentações do Processo</h4>
    <div
      v-if="movimentacoes.length === 0"
      class="alert alert-info"
    >
      Nenhuma movimentação registrada para este subprocesso.
    </div>
    <table
      v-else
      class="table table-striped"
    >
      <thead>
        <tr>
          <th>Data/Hora</th>
          <th>Unidade Origem</th>
          <th>Unidade Destino</th>
          <th>Descrição</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="movimentacao in movimentacoes"
          :key="movimentacao.codigo"
        >
          <td>{{ formatDateTimeBR(movimentacao.dataHora) }}</td>
          <td>{{ movimentacao.unidadeOrigem }}</td>
          <td>{{ movimentacao.unidadeDestino }}</td>
          <td>{{ movimentacao.descricao }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import type { Movimentacao } from '@/types/tipos';
import { formatDateTimeBR } from '@/utils';

defineProps<{
  movimentacoes: Movimentacao[]
}>();
</script>
