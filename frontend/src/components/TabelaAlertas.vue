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
        @click="emit('selecionar-alerta', alerta)"
      >
        <td>{{ alerta.dataHoraFormatada }}</td>
        <td>{{ alerta.mensagem }}</td>
        <td>{{ alerta.processo }}</td>
        <td>{{ alerta.origem }}</td>
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
import type {Alerta} from '@/types/tipos';

defineProps<{
  alertas: Alerta[]
}>();

const emit = defineEmits<{
  (e: 'ordenar', criterio: 'data' | 'processo'): void
  (e: 'selecionar-alerta', alerta: Alerta): void
}>();
</script>
