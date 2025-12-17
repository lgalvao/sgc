<template>
  <div>
    <BBadge
        class="mb-2"
        style="border-radius: 0"
        variant="secondary"
    >
      Detalhes do processo
    </BBadge>
    <h2
        class="display-6"
        data-testid="processo-info"
    >
      {{ descricao }}
    </h2>
    <div class="mb-4 mt-3">
      <strong>Tipo:</strong> {{ formatarTipoProcesso(tipo) }}<br>
      <strong>Situação:</strong> {{ formatarSituacaoProcesso(situacao) }}<br>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {BBadge} from "bootstrap-vue-next";
import type {SituacaoProcesso, TipoProcesso} from "@/types/tipos";
import { formatarSituacaoProcesso, formatarTipoProcesso } from "@/utils/formatters";

defineProps<{
  descricao: string;
  tipo: TipoProcesso | string;
  situacao: SituacaoProcesso | string;
}>();
</script>
