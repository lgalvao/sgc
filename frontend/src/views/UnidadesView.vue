<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.unidades.TITULO">
      <template #description>
        {{ TEXTOS.unidades.SUBTITULO }}
      </template>
    </PageHeader>

    <div v-if="exibirAlertaDiagnostico" class="sticky-top mb-3">
      <BAlert
          :model-value="true"
          variant="warning"
      >
        <strong>Pendências organizacionais identificadas.</strong>
        <div class="mt-1">{{ resumoDiagnostico }}</div>
        <ul v-if="gruposDiagnostico.length > 0" class="mb-0 mt-2 ps-3">
          <li v-for="grupo in gruposDiagnostico" :key="grupo.tipo">
            {{ grupo.tipo }}: {{ grupo.quantidadeOcorrencias }} ocorrência(s)
          </li>
        </ul>
      </BAlert>
    </div>

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
import {buscarDiagnosticoOrganizacional, buscarTodasUnidades, mapUnidadesArray} from "@/services/unidadeService";
import type {DiagnosticoOrganizacional, Unidade} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {usePerfil} from "@/composables/usePerfil";
import {logger} from "@/utils";

const unidades = ref<Unidade[]>([]);
const {carregando: isLoading, erro, executarSilencioso} = useAsyncAction();
const {isAdmin} = usePerfil();
const diagnosticoOrganizacional = ref<DiagnosticoOrganizacional | null>(null);
const erroDiagnosticoOrganizacional = ref<string | null>(null);

const erroUnidades = computed(() =>
    erro.value ? {message: erro.value} : null
);
const gruposDiagnostico = computed(() => diagnosticoOrganizacional.value?.grupos ?? []);
const resumoDiagnostico = computed(() =>
    erroDiagnosticoOrganizacional.value
        ?? diagnosticoOrganizacional.value?.resumo
        ?? ""
);
const exibirAlertaDiagnostico = computed(() =>
    isAdmin.value && (!!erroDiagnosticoOrganizacional.value || diagnosticoOrganizacional.value?.possuiViolacoes === true)
);

function clearError() {
  erro.value = null;
}

const selecaoVazia = ref<number[]>([]);

async function carregarUnidades() {
  await executarSilencioso(async () => {
    const response = await buscarTodasUnidades();
    unidades.value = mapUnidadesArray(response as any);
  }, TEXTOS.comum.ERRO_OPERACAO);
}

async function carregarDiagnostico() {
  if (!isAdmin.value) {
    return;
  }

  try {
    diagnosticoOrganizacional.value = await buscarDiagnosticoOrganizacional();
    erroDiagnosticoOrganizacional.value = null;
  } catch (error) {
    diagnosticoOrganizacional.value = null;
    erroDiagnosticoOrganizacional.value = "Não foi possível verificar as pendências organizacionais desta tela.";
    logger.error("Erro ao carregar diagnostico organizacional das unidades:", error);
  }
}

onMounted(() => {
  void carregarDiagnostico();
  void carregarUnidades();
});

</script>
