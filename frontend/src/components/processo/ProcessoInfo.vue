<template>
  <div class="process-info mb-4">
    <div v-if="showTipo && tipo" class="mb-2">
      <strong>{{ TEXTOS.processo.INFO_TIPO }}:</strong> {{ formatTipoProcesso(tipo) }}
    </div>
    <div v-if="showSituacao && situacao" class="mb-2">
      <strong>{{ TEXTOS.processo.INFO_SITUACAO }}:</strong> {{ formatSituacaoProcesso(situacao) }}
    </div>
    <div v-if="showDataLimite && dataLimite" class="mb-2">
      <strong>{{ TEXTOS.processo.INFO_DATA_LIMITE }}:</strong> {{ formatDate(dataLimite, false) }}
    </div>
    <div v-if="showUnidades && numUnidades !== undefined" class="mb-2">
      <strong>{{ TEXTOS.processo.INFO_UNIDADES }}:</strong> {{ numUnidades }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import {formatDate, formatSituacaoProcesso, formatTipoProcesso} from '@/utils/formatters';
import {TEXTOS} from '@/constants/textos';

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
