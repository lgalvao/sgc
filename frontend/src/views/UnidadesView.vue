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
      <TreeTable
          :columns="colunas"
          :data="unidadesMapeadas"
          @row-click="irParaDetalhes"
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
import {computed, onMounted} from "vue";
import {BButton, BSpinner} from "bootstrap-vue-next";
import {useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import TreeTable from "@/components/comum/TreeTable.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useUnidadesStore} from "@/stores/unidades";
import type {Unidade} from "@/types/tipos";

const router = useRouter();
const unidadesStore = useUnidadesStore();
const unidades = computed(() => unidadesStore.unidades);
const erroUnidades = computed(() =>
    unidadesStore.error ? {message: unidadesStore.error} : null
);

const colunas = [
  {key: "sigla", label: "Sigla", width: "20%"},
  {key: "nome", label: "Nome", width: "50%"},
  {key: "responsavel", label: "Responsável", width: "30%"},
];

const unidadesMapeadas = computed(() => {
  return mapUnidades(unidades.value);
});

function mapUnidades(unidades: Unidade[]): any[] {
  if (!unidades) return [];

  const result: any[] = [];
  for (const u of unidades) {
    if (u.sigla === 'ADMIN') {
      // Se for ADMIN, promovemos os filhos para o nível atual
      if (u.filhas) {
        result.push(...mapUnidades(u.filhas));
      }
    } else {
      result.push({
        codigo: u.codigo,
        sigla: u.sigla,
        nome: u.nome,
        responsavel: u.responsavel?.nome || u.tituloTitular || "-",
        children: u.filhas ? mapUnidades(u.filhas) : [],
        expanded: false
      });
    }
  }
  return result;
}

async function carregarUnidades() {
  await unidadesStore.buscarTodasAsUnidades();
}

function irParaDetalhes(row: any) {
  router.push({
    name: "Unidade",
    params: {codUnidade: row.codigo}
  });
}

onMounted(carregarUnidades);

</script>

<style scoped>
</style>
