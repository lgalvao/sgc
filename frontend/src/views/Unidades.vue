<template>
  <LayoutPadrao>
    <PageHeader title="Unidades do TRE-PE">
      <template #description>
        Clique em unidade para ver detalhes.
      </template>
    </PageHeader>

    <ErrorAlert
        :error="erroUnidades"
        @dismiss="unidadesStore.clearError()"
    />

    <div v-if="unidadesStore.isLoading" class="text-center py-5">
      <BSpinner variant="primary" label="Carregando unidades..." />
      <p class="mt-2 text-muted">Carregando árvore de unidades...</p>
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

    <EmptyState
        v-else
        icon="bi-diagram-3"
        title="Nenhuma unidade encontrada."
        description="Tente atualizar a página ou ajustar os filtros de visualização."
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted} from "vue";
import {BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ArvoreUnidades from "@/components/ArvoreUnidades.vue";
import ErrorAlert from "@/components/common/ErrorAlert.vue";
import EmptyState from "@/components/EmptyState.vue";
import {useUnidadesStore} from "@/stores/unidades";

const unidadesStore = useUnidadesStore();
const unidades = computed(() => unidadesStore.unidades);
const erroUnidades = computed(() =>
    unidadesStore.error ? {message: unidadesStore.error} : null
);

onMounted(async () => {
  await unidadesStore.buscarTodasAsUnidades();
});

</script>

<style scoped>
.card {
  border-radius: 0.5rem;
}
</style>
