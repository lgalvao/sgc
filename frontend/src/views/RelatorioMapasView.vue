<template>
  <LayoutPadrao>
    <PageHeader title="Mapas vigentes">
      <template #actions>
        <BButton variant="outline-secondary" to="/relatorios">
          <i class="bi bi-arrow-left me-1"/> Voltar
        </BButton>
      </template>
    </PageHeader>

    <BCard class="mb-4">
      <BRow class="align-items-end">
        <BCol md="5">
          <BFormGroup :label="TEXTOS.relatorios.LABEL_SELECIONE_PROCESSO" label-for="select-processo-mapas">
            <BFormSelect
              id="select-processo-mapas"
              v-model="processoIdSelecionado"
              :options="opcoesProcessos"
              data-testid="select-processo-mapas"
            />
          </BFormGroup>
        </BCol>
        <BCol md="4">
          <BFormGroup :label="TEXTOS.relatorios.LABEL_SELECIONE_UNIDADE" label-for="select-unidade-mapas">
            <BFormSelect
              id="select-unidade-mapas"
              v-model="unidadeIdSelecionada"
              :options="opcoesUnidades"
              data-testid="select-unidade-mapas"
            />
          </BFormGroup>
        </BCol>
        <BCol md="auto">
          <BButton
            :disabled="!processoIdSelecionado || carregando"
            variant="success"
            data-testid="btn-gerar-mapas"
            @click="exportarPdf"
          >
            <BSpinner v-if="carregando" small class="me-1" />
            <i v-else class="bi bi-file-earmark-pdf me-1" />
            {{ TEXTOS.relatorios.BOTAO_GERAR_PDF }}
          </BButton>
        </BCol>
      </BRow>
    </BCard>

    <EmptyState
      v-if="!processoIdSelecionado"
      title="Selecione um processo"
      description="Para gerar o relatório de mapas vigentes, selecione um processo e opcionalmente uma unidade."
      icon="bi-file-earmark-pdf"
      data-testid="empty-state-mapas"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton, BCard, BCol, BFormGroup, BFormSelect, BRow, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useRelatoriosStore} from "@/stores/relatorios";
import {usePerfilStore} from "@/stores/perfil";
import {TEXTOS} from "@/constants/textos";
import * as painelService from "@/services/painelService";
import type {ProcessoResumo} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";

const relatoriosStore = useRelatoriosStore();
const perfilStore = usePerfilStore();
const { notify } = useNotification();

const processoIdSelecionado = ref<number | null>(null);
const unidadeIdSelecionada = ref<number | null>(null);
const processosDisponiveis = ref<ProcessoResumo[]>([]);
const carregando = ref(false);

const opcoesProcessos = computed(() => [
  { value: null, text: TEXTOS.relatorios.SELECIONE },
  ...processosDisponiveis.value.map(p => ({ value: p.codigo, text: p.descricao }))
]);

const opcoesUnidades = computed(() => [
  { value: null, text: TEXTOS.relatorios.TODAS_UNIDADES },
  ...(perfilStore.unidadeSelecionada && perfilStore.unidadeSelecionadaSigla
    ? [{ value: perfilStore.unidadeSelecionada, text: perfilStore.unidadeSelecionadaSigla }]
    : [])
]);

async function carregarProcessos() {
  try {
    const response = await painelService.listarProcessos({ page: 0, size: 100 });
    processosDisponiveis.value = response?.content ?? [];
  } catch {
    notify("Erro ao carregar processos", "danger");
  }
}

async function exportarPdf() {
  if (!processoIdSelecionado.value) return;
  carregando.value = true;
  await relatoriosStore.exportarMapasPdf(processoIdSelecionado.value, unidadeIdSelecionada.value || undefined);
  carregando.value = false;
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_GERAR, "danger");
  }
}

onMounted(() => {
  carregarProcessos();
});
</script>
