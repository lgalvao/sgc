<template>
  <div class="process-info mb-4">
    <div v-if="showTipo && tipo" class="mb-2">
      <strong>Tipo:</strong> {{ formatTipoProcesso(tipo) }}
    </div>
    <div v-if="showSituacao && situacao" class="mb-2">
      <strong>Situação:</strong> {{ formatSituacaoProcesso(situacao) }}
    </div>
    <div v-if="showDataLimite && dataLimite" class="mb-2">
      <strong>Data Limite:</strong> {{ formatDate(dataLimite, false) }}
    </div>
    <div v-if="showUnidades && numUnidades !== undefined" class="mb-2">
      <strong>Unidades participantes:</strong> {{ numUnidades }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import {formatDate, formatSituacaoProcesso, formatTipoProcesso} from '@/utils/formatters';

withDefaults(
    defineProps<{
      tipo?: string;
      situacao?: string;
      dataLimite?: string;
      numUnidades?: number;
      showTipo?: boolean;
      showSituacao?: boolean;
      showDataLimite?: boolean;
      showUnidades?: boolean;
    }>(),
    {
      tipo: '',
      situacao: '',
      dataLimite: '',
      numUnidades: 0,
      showTipo: true,
      showSituacao: true,
      showDataLimite: true,
      showUnidades: false
    }
);
</script>
