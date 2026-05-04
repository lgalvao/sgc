<script lang="ts" setup>
import {BCard, BCardBody, BFormCheckbox} from "bootstrap-vue-next";
import type {Atividade} from "@/types/tipos";

defineProps<{
  atividade: Atividade;
  selecionadas: number[];
}>();

const model = defineModel<number[]>();
</script>

<template>
  <BCard
      :class="selecionadas.includes(atividade.codigo) ? 'atividade-card-item checked' : 'atividade-card-item'"
      :data-testid="selecionadas.includes(atividade.codigo) ? 'atividade-associada' : 'atividade-nao-associada'"
      no-body
  >
    <BCardBody class="d-flex align-items-center">
      <BFormCheckbox :id="`atv-${atividade.codigo}`" v-model="model" :value="atividade.codigo"
                     data-testid="chk-criar-competencia-atividade">
        {{ atividade.descricao }}
      </BFormCheckbox>
    </BCardBody>
  </BCard>
</template>

<style scoped>
.atividade-card-item {
  cursor: pointer;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  transition: all 0.2s ease-in-out;
  background-color: var(--bs-body-bg);
}

.atividade-card-item:hover {
  border-color: var(--bs-primary);
  box-shadow: 0 0 0 0.25rem var(--bs-primary);
}

.atividade-card-item.checked {
  background-color: var(--bs-primary-bg-subtle);
  border-color: var(--bs-primary);
}
</style>
