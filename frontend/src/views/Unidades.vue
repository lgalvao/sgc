<template>
  <BContainer class="mt-4">
    <PageHeader title="Unidades do TRE-PE">
      <template #description>
        Clique em unidade para ver detalhes.
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
import {computed, onMounted} from "vue";
import {BAlert, BContainer, BSpinner} from "bootstrap-vue-next";
import PageHeader from "@/components/layout/PageHeader.vue";
import ArvoreUnidades from "@/components/ArvoreUnidades.vue";
import {useUnidadesStore} from "@/stores/unidades";

const unidadesStore = useUnidadesStore();
const unidades = computed(() => unidadesStore.unidades);

onMounted(async () => {
  await unidadesStore.buscarTodasAsUnidades();
});

</script>

<style scoped>
.card {
  border-radius: 0.5rem;
}
</style>
