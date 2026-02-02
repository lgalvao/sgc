<template>
  <div class="process-info mb-4">
    <div v-if="showType" class="mb-2">
      <strong>Tipo:</strong> {{ formatarTipoProcesso(tipo) }}
    </div>
    <div v-if="showSituacao" class="mb-2">
      <strong>Situação:</strong> {{ formatarSituacaoProcesso(situacao) }}
    </div>
    <div v-if="showDataLimite && dataLimite" class="mb-2">
      <strong>Data Limite:</strong> {{ formatarData(dataLimite) }}
    </div>
    <div v-if="showUnidades && numUnidades !== undefined" class="mb-2">
      <strong>Unidades participantes:</strong> {{ numUnidades }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import { formatarTipoProcesso, formatarSituacaoProcesso } from "@/utils/formatters";

withDefaults(
  defineProps<{
    tipo?: string;
    situacao?: string;
    dataLimite?: string;
    numUnidades?: number;
    showType?: boolean;
    showSituacao?: boolean;
    showDataLimite?: boolean;
    showUnidades?: boolean;
  }>(),
  {
    showType: true,
    showSituacao: true,
    showDataLimite: true,
    showUnidades: false
  }
);

function formatarData(data: string): string {
  if (!data) return '';
  const date = new Date(data);
  return date.toLocaleDateString('pt-BR');
}
</script>
