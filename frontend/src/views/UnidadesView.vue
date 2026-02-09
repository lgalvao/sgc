<template>
  <BContainer class="mt-4">
    <PageHeader title="Unidades do Tribunal">
      <template #description>
        Explore a hierarquia organizacional completa do Tribunal.
      </template>
    </PageHeader>

    <BAlert
      v-if="unidadesStore.error"
      :model-value="true"
      variant="danger"
      dismissible
      @dismissed="unidadesStore.clearError()"
    >
      {{ unidadesStore.error }}
    </BAlert>

    <div v-if="unidadesStore.isLoading" class="text-center my-5">
      <BSpinner variant="primary" label="Carregando unidades..." />
      <p class="mt-2">Carregando árvore de unidades...</p>
    </div>

    <div v-else-if="unidades.length > 0" class="card shadow-sm">
      <div class="card-body">
        <ArvoreUnidades
          :unidades="unidades"
          :model-value="[]"
          :modo-selecao="false"
          :ocultar-raiz="false"
          @update:model-value="() => {}"
        />
      </div>
    </div>

    <div v-else class="text-center my-5">
      <p>Nenhuma unidade encontrada.</p>
    </div>
  </BContainer>
</template>

<script lang="ts" setup>
import { onMounted, computed } from "vue";
import { BContainer, BAlert, BSpinner } from "bootstrap-vue-next";
import PageHeader from "@/components/layout/PageHeader.vue";
import ArvoreUnidades from "@/components/ArvoreUnidades.vue";
import { useUnidadesStore } from "@/stores/unidades";
import { useRouter } from "vue-router";

const unidadesStore = useUnidadesStore();
const router = useRouter();

const unidades = computed(() => unidadesStore.unidades);

onMounted(async () => {
  await unidadesStore.buscarTodasAsUnidades();
});

// ArvoreUnidades triggers navigation when a node is clicked?
// Let's check UnidadeTreeNode.vue or ArvoreUnidades.vue to see how it handles clicks in non-selection mode.
</script>

<style scoped>
.card {
  border-radius: 0.5rem;
}
</style>
