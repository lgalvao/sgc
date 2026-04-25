<template>
  <BCard
      v-for="atividade in atividades"
      :key="atividade.codigo"
      class="mb-3 atividade-card"
      no-body
  >
    <BCardBody class="py-2">
      <BCardTitle class="d-flex align-items-center atividade-titulo-card">
        <strong class="atividade-descricao" data-testid="txt-atividade-descricao">{{ atividade.descricao }}</strong>
      </BCardTitle>
      <div class="mt-3 ms-3">
        <div
            v-for="conhecimento in atividade.conhecimentos"
            :key="conhecimento.codigo"
            class="d-flex align-items-center mb-2"
        >
          <span data-testid="txt-conhecimento-descricao">{{ conhecimento.descricao }}</span>
        </div>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {BCard, BCardBody, BCardTitle} from "bootstrap-vue-next";
import type {Atividade} from "@/types/tipos";

defineProps<{
  atividades: Atividade[];
}>();
</script>

<style scoped>
.atividade-card {
  transition: box-shadow 0.2s;
}

.atividade-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.atividade-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.atividade-titulo-card {
  background: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
}

.atividade-titulo-card .atividade-descricao {
  font-size: 1.1rem;
}
</style>
