<template>
  <BCard class="mb-4">
    <div class="d-flex flex-column gap-3">
      <BFormGroup label-for="arvore-unidades-mapas">
        <template #label>
          Selecione as unidades
        </template>
        <div class="border rounded p-3 container-arvore" data-testid="container-arvore-unidades-mapas">
          <ArvoreUnidades
              id="arvore-unidades-mapas"
              :model-value="unidadesSelecionadas"
              :mostrar-superiores-nao-elegiveis-como-indeterminados="true"
              :unidades="unidadesDisponiveis"
              @update:model-value="$emit('update:unidadesSelecionadas', $event)"
          />
        </div>
      </BFormGroup>

      <div class="d-flex flex-wrap gap-2">
        <BButton
            :disabled="carregando || !temUnidadesSelecionadas"
            data-testid="btn-gerar-html-mapas"
            variant="success"
            @click="$emit('gerar')"
        >
          <BSpinner v-if="carregando" class="me-1" small/>
          <i v-else class="bi bi-eye me-1"/>
          Visualizar
        </BButton>
        <BButton
            :disabled="carregando || !temUnidadesSelecionadas"
            data-testid="btn-gerar-mapas"
            variant="outline-danger"
            @click="$emit('exportar')"
        >
          <BSpinner v-if="carregando" class="me-1" small/>
          <i v-else class="bi bi-file-earmark-pdf me-1"/>
          PDF
        </BButton>
      </div>
    </div>
  </BCard>
</template>

<script lang="ts" setup>
import {BButton, BCard, BFormGroup, BSpinner} from "bootstrap-vue-next";
import ArvoreUnidades from "@/components/unidade/ArvoreUnidades.vue";
import type {Unidade} from "@/types/tipos";

interface Props {
  unidadesDisponiveis: Unidade[];
  unidadesSelecionadas: number[];
  carregando: boolean;
  temUnidadesSelecionadas: boolean;
}

defineProps<Props>();

defineEmits<{
  (e: "update:unidadesSelecionadas", valor: number[]): void;
  (e: "gerar"): void;
  (e: "exportar"): void;
}>();
</script>

<style scoped>
.container-arvore {
  overflow-x: hidden;
  max-height: 22rem;
  overflow-y: auto;
  background: var(--bs-body-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 0.65rem;
}
</style>
