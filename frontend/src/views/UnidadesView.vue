<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.unidades.TITULO">
      <template #description>
        {{ TEXTOS.unidades.SUBTITULO }}
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
      <BSpinner :label="TEXTOS.unidades.CARREGANDO" variant="primary"/>
      <p class="mt-2 text-muted">{{ TEXTOS.unidades.CARREGANDO_ARVORE }}</p>
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
        :description="TEXTOS.unidades.EMPTY_DESCRIPTION"
        icon="bi-diagram-3"
        :title="TEXTOS.unidades.EMPTY_TITLE"
    >
      <BButton
          data-testid="btn-unidades-recarregar"
          size="sm"
          variant="outline-primary"
          @click="carregarUnidades"
      >
        {{ TEXTOS.unidades.BOTAO_ATUALIZAR }}
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
import {TEXTOS} from "@/constants/textos";

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
    erro.value = err.message || TEXTOS.comum.ERRO_OPERACAO;
  } finally {
    isLoading.value = false;
  }
}

onMounted(carregarUnidades);

</script>
