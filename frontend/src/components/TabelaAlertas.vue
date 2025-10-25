<template>
  <table
    class="table"
    data-testid="tabela-alertas"
  >
    <thead>
      <tr>
        <th
          style="cursor: pointer;"
          @click="emit('ordenar', 'data')"
        >
          Data/Hora
        </th>
        <th>Descrição</th>
        <th
          style="cursor: pointer;"
          @click="emit('ordenar', 'processo')"
        >
          Processo
        </th>
        <th>Origem</th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="(alerta, index) in alertas"
        :key="index"
        style="cursor: pointer;"
        @click="emit('marcarComoLido', alerta.codigo)"
      >
        <td>{{ alerta.dataFormatada }}</td>
        <td>{{ alerta.descricao }}</td>
        <td>{{ alerta.processoCodigo }}</td>
        <td>{{ alerta.unidadeOrigemCodigo }}</td>
      </tr>
      <tr v-if="!alertas || alertas.length === 0">
        <td
          class="text-center text-muted"
          colspan="4"
        >
          Nenhum alerta no momento.
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script lang="ts" setup>
import type { AlertaFormatado } from '@/types/tipos';

defineProps<{
  alertas: AlertaFormatado[]
}>();

const emit = defineEmits<{
  (e: 'ordenar', criterio: 'data' | 'processo'): void
  (e: 'marcarComoLido', id: number): void
}>();
</script>
