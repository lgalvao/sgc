<template>
  <BCard class="mb-3 shadow-sm border-0 border-start border-4 border-primary">
    <div class="d-flex justify-content-between align-items-start mb-2">
      <h5 class="mb-0" data-testid="vis-mapa__txt-competencia-descricao">
        {{ competencia.descricao }}
      </h5>
    </div>

    <div v-if="competencia.atividades && competencia.atividades.length > 0" class="mt-3">
      <div
        v-for="atividade in competencia.atividades"
        :key="atividade.codigo"
        class="mb-3 p-2 bg-light rounded"
      >
        <div class="d-flex align-items-baseline">
          <i class="bi bi-gear-fill me-2 text-secondary small" aria-hidden="true"/>
          <p class="atividade-associada-descricao mb-1 fw-medium">{{ atividade.descricao }}</p>
        </div>

        <div v-if="atividade.conhecimentos && atividade.conhecimentos.length > 0" class="ms-4 mt-2">
          <div class="d-flex flex-wrap gap-2">
            <span
              v-for="c in atividade.conhecimentos"
              :key="c.codigo"
              class="badge bg-white text-dark border fw-normal py-1 px-2"
              data-testid="txt-conhecimento-item"
            >
              <i class="bi bi-book me-1 text-info" aria-hidden="true"/>
              {{ c.descricao }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </BCard>
</template>

<script lang="ts" setup>
import {BCard} from "bootstrap-vue-next";
import type {Competencia} from "@/types/tipos";

defineProps<{
  competencia: Competencia;
}>();
</script>
