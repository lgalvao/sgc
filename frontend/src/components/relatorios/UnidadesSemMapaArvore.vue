<template>
  <ul class="arvore-unidades-sem-mapa list-unstyled mb-0">
    <li v-for="unidade in unidades" :key="unidade.codigo">
      <div class="d-flex align-items-baseline gap-1 py-1">
        <span class="fw-semibold text-primary text-nowrap">{{ unidade.sigla }}</span>
        <span v-if="unidade.sigla && unidade.nome" class="text-muted">-</span>
        <span class="text-body">{{ unidade.nome }}</span>
      </div>
      <UnidadesSemMapaArvore
          v-if="unidade.filhas && unidade.filhas.length > 0"
          :unidades="unidade.filhas"
      />
    </li>
  </ul>
</template>

<script lang="ts" setup>
import type {Unidade} from "@/types/tipos";

defineProps<{
  unidades: Unidade[];
}>();
</script>

<style scoped>
.arvore-unidades-sem-mapa {
  padding-left: 1.25rem;
  margin-left: 0.25rem;
}
</style>
