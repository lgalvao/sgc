<template>
  <div>
    <BBadge
      variant="secondary"
      class="mb-2"
      style="border-radius: 0"
    >Detalhes do processo</BBadge>
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
import {BBadge} from 'bootstrap-vue-next';

defineProps<{
  descricao: string;
  tipo: TipoProcesso | string;
  situacao: SituacaoProcesso | string;
}>();
</script>
