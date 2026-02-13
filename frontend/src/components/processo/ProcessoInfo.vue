<template>
  <div class="process-info mb-4">
    <div v-if="showTipo && tipoLabel" class="mb-2">
      <strong>Tipo:</strong> {{ tipoLabel }}
    </div>
    <div v-if="showSituacao && situacaoLabel" class="mb-2">
      <strong>Situação:</strong> {{ situacaoLabel }}
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
withDefaults(
  defineProps<{
    tipoLabel?: string;
    situacaoLabel?: string;
    dataLimite?: string;
    numUnidades?: number;
    showTipo?: boolean;
    showSituacao?: boolean;
    showDataLimite?: boolean;
    showUnidades?: boolean;
  }>(),
  {
    tipoLabel: '',
    situacaoLabel: '',
    dataLimite: '',
    numUnidades: 0,
    showTipo: true,
    showSituacao: true,
    showDataLimite: true,
    showUnidades: false
  }
);

function formatarData(data?: string): string {
  if (!data) return '';
  const date = new Date(data);
  return date.toLocaleDateString('pt-BR');
}
defineExpose({ formatarData });
</script>
