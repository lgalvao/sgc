<template>
  <div class="process-info mb-4">
    <div v-if="showTipo" class="mb-2">
      <strong>Tipo:</strong> {{ tipoFormatado }}
    </div>
    <div v-if="showSituacao" class="mb-2">
      <strong>Situação:</strong> {{ situacaoFormatada }}
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
import { computed } from "vue";
import { formatarTipoProcesso, formatarSituacaoProcesso } from "@/utils/formatters";

const props = withDefaults(
  defineProps<{
    tipo?: string;
    tipoLabel?: string;
    situacao?: string;
    situacaoLabel?: string;
    dataLimite?: string;
    numUnidades?: number;
    showTipo?: boolean;
    showSituacao?: boolean;
    showDataLimite?: boolean;
    showUnidades?: boolean;
  }>(),
  {
    showTipo: true,
    showSituacao: true,
    showDataLimite: true,
    showUnidades: false
  }
);

// Use label from backend if available, otherwise format the enum value
const tipoFormatado = computed(() => props.tipoLabel || (props.tipo ? formatarTipoProcesso(props.tipo) : ''));
const situacaoFormatada = computed(() => props.situacaoLabel || (props.situacao ? formatarSituacaoProcesso(props.situacao) : ''));

function formatarData(data?: string): string {
  if (!data) return '';
  const date = new Date(data);
  return date.toLocaleDateString('pt-BR');
}
</script>
