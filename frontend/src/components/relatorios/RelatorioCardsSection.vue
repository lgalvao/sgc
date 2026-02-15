<template>
  <BRow>
    <BCol md="4">
      <BCard
          class="mb-3 text-center h-100 cursor-pointer shadow-sm hover-shadow"
          data-testid="card-relatorio-mapas"
          @click="$emit('abrir-mapas-vigentes')"
      >
        <div class="display-4 mb-2 text-primary">
          <i class="bi bi-map"/>
        </div>
        <h3>Mapas Vigentes</h3>
        <div class="h2 mb-0">{{ mapasVigentesCount }}</div>
        <div class="text-muted">Unidades com mapas</div>
      </BCard>
    </BCol>

    <BCol md="4">
      <BCard
          class="mb-3 text-center h-100 cursor-pointer shadow-sm hover-shadow"
          data-testid="card-relatorio-gaps"
          @click="$emit('abrir-diagnosticos-gaps')"
      >
        <div class="display-4 mb-2 text-danger">
          <i class="bi bi-exclamation-triangle"/>
        </div>
        <h3>Diagnósticos de Gaps</h3>
        <div class="h2 mb-0">{{ diagnosticosGapsCount }}</div>
        <div class="text-muted">Gaps identificados</div>
      </BCard>
    </BCol>

    <BCol md="4">
      <BCard
          class="mb-3 text-center h-100 cursor-pointer shadow-sm hover-shadow"
          data-testid="card-relatorio-andamento"
          @click="$emit('abrir-andamento-geral')"
      >
        <div class="display-4 mb-2 text-success">
          <i class="bi bi-graph-up"/>
        </div>
        <h3>Andamento Geral</h3>
        <div class="h2 mb-0">{{ processosFiltradosCount }}</div>
        <div class="text-muted">Processos em andamento</div>
      </BCard>
    </BCol>
  </BRow>
</template>

<script lang="ts" setup>
import {BCard, BCol, BRow} from "bootstrap-vue-next";

defineProps<{
  mapasVigentesCount: number;
  diagnosticosGapsCount: number;
  processosFiltradosCount: number;
}>();

defineEmits<{
  (e: 'abrir-mapas-vigentes'): void;
  (e: 'abrir-diagnosticos-gaps'): void;
  (e: 'abrir-andamento-geral'): void;
}>();
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
.hover-shadow:hover {
  box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;
}
</style>
