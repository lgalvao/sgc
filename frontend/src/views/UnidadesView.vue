<template>
  <LayoutPadrao>
    <PageHeader title="Unidades">
      <template #description>
        Clique em unidade para ver detalhes.
      </template>
    </PageHeader>

    <ErrorAlert
        :error="erroUnidades"
        @dismiss="unidadesStore.clearError()"
    />

    <div v-if="unidadesStore.isLoading" class="text-center py-5">
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
import {BButton, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ArvoreUnidades from "@/components/unidade/ArvoreUnidades.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useUnidadesStore} from "@/stores/unidades";

const unidadesStore = useUnidadesStore();
const unidades = computed(() => unidadesStore.unidades);
const erroUnidades = computed(() =>
    unidadesStore.error ? {message: unidadesStore.error} : null
);
const selecaoVazia = ref<number[]>([]);

async function carregarUnidades() {
  await unidadesStore.buscarTodasAsUnidades();
}

onMounted(carregarUnidades);

</script>
