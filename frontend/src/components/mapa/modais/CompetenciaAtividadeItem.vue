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
    <BCardBody class="atividade-card-item__body">
      <BFormCheckbox
          :id="`atv-${atividade.codigo}`"
          v-model="model"
          :value="atividade.codigo"
          class="atividade-card-item__checkbox"
          data-testid="chk-criar-competencia-atividade"
      >
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
  width: 100%;
}

.atividade-card-item:hover {
  border-color: var(--bs-primary);
  box-shadow: 0 0 0 0.125rem var(--bs-primary-bg-subtle);
}

.atividade-card-item.checked {
  background-color: var(--bs-primary-bg-subtle);
  border-color: var(--bs-primary);
}

.atividade-card-item__body {
  padding: 0.45rem 0.65rem;
}

.atividade-card-item__checkbox {
  margin-bottom: 0;
  line-height: 1.25;
}
</style>
