<template>
  <div>
    <span
      class="badge bg-secondary mb-2"
      style="border-radius: 0"
    >Detalhes do processo</span>
    <h2
      class="display-6"
      data-testid="processo-info"
    >
      {{ descricao }}
    </h2>
    <div class="mb-4 mt-3">
      <strong>Tipo:</strong> {{ tipo }}<br>
      <strong>Situação:</strong> {{ situacao }}<br>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type {SituacaoProcesso, TipoProcesso} from '@/types/tipos';

defineProps<{
  descricao: string;
  tipo: TipoProcesso | string;
  situacao: SituacaoProcesso | string;
}>();
</script>
