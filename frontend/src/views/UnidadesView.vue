<template>
  <LayoutPadrao>
    <PageHeader title="Unidades">
      <template #description>
        Clique em unidade para ver detalhes.
      </template>
    </PageHeader>

    <BAlert
        v-if="erroUnidades"
        :model-value="true"
        variant="danger"
        dismissible
        @dismissed="clearError()"
    >
      {{ erroUnidades.message }}
    </BAlert>

    <div v-if="isLoading" class="text-center py-5">
      <BSpinner label="Carregando unidades..." variant="primary"/>
      <p class="mt-2 text-muted">Carregando árvore de unidades...</p>
    </div>

    <div v-else-if="unidades.length > 0">
      <ArvoreUnidades
          v-model="selecaoVazia"
          :modo-selecao="false"
          :unidades="unidades"
      />
    </div>

    <EmptyState
        v-else
        description="Nenhuma unidade retornada. Tente atualizar a listagem."
        icon="bi-diagram-3"
        title="Nenhuma unidade encontrada."
    >
      <BButton
          data-testid="btn-unidades-recarregar"
          size="sm"
          variant="outline-primary"
          @click="carregarUnidades"
      >
        Atualizar unidades
      </BButton>
    </EmptyState>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BAlert, BButton, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ArvoreUnidades from "@/components/unidade/ArvoreUnidades.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {buscarTodasUnidades, mapUnidadesArray} from "@/services/unidadeService";
import type {Unidade} from "@/types/tipos";

const unidades = ref<Unidade[]>([]);
const isLoading = ref(false);
const erro = ref<string | null>(null);

const erroUnidades = computed(() =>
    erro.value ? {message: erro.value} : null
);

function clearError() {
  erro.value = null;
}

const selecaoVazia = ref<number[]>([]);

async function carregarUnidades() {
  isLoading.value = true;
  erro.value = null;
  try {
    const response = await buscarTodasUnidades();
    unidades.value = mapUnidadesArray(response as any);
  } catch (err: any) {
    erro.value = err.message || "Erro ao buscar unidades";
  } finally {
    isLoading.value = false;
  }
}

onMounted(carregarUnidades);

</script>
