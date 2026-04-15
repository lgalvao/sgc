<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.relatorios.ANDAMENTO_PROCESSO">
      <template #actions>
        <BButton variant="outline-secondary" to="/relatorios">
          <i class="bi bi-arrow-left me-1"/> Voltar
        </BButton>
      </template>
    </PageHeader>

    <BCard class="mb-4">
      <BRow class="align-items-end">
        <BCol md="6">
          <BFormGroup :label="TEXTOS.relatorios.LABEL_SELECIONE_PROCESSO" label-for="select-processo">
            <BFormSelect
              id="select-processo"
              v-model="processoIdSelecionado"
              :options="opcoesProcessos"
              data-testid="select-processo-andamento"
            />
          </BFormGroup>
        </BCol>
        <BCol md="auto">
          <BButton
            :disabled="!processoIdSelecionado || carregando"
            variant="primary"
            @click="gerarRelatorio"
            data-testid="btn-gerar-andamento"
          >
            <BSpinner v-if="carregando" small class="me-1" />
            <i v-else class="bi bi-search me-1" />
            {{ TEXTOS.relatorios.BOTAO_GERAR }}
          </BButton>
        </BCol>
        <BCol v-if="relatorioAndamento.length > 0" md="auto">
          <BButton variant="outline-success" @click="exportarPdf" data-testid="btn-exportar-andamento">
            <i class="bi bi-file-earmark-pdf me-1" />
            {{ TEXTOS.relatorios.BOTAO_PDF }}
          </BButton>
        </BCol>
      </BRow>
    </BCard>

    <div v-if="carregando && relatorioAndamento.length === 0" class="text-center py-5">
      <BSpinner variant="primary" />
    </div>

    <template v-else>
      <div v-if="relatorioAndamento.length > 0" class="table-responsive">
        <BTable
          :fields="campos"
          :items="relatorioAndamento"
          hover
          striped
          responsive
          data-testid="tbl-relatorio-andamento"
        />
      </div>
      <EmptyState
        v-else-if="processoIdSelecionado && !carregando"
        :title="TEXTOS.relatorios.EMPTY_TITLE"
        :description="TEXTOS.relatorios.EMPTY_DESCRIPTION"
        icon="bi-table"
        data-testid="empty-state-andamento"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton, BCard, BCol, BFormGroup, BFormSelect, BRow, BSpinner, BTable} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useRelatoriosStore} from "@/stores/relatorios";
import {TEXTOS} from "@/constants/textos";
import * as painelService from "@/services/painelService";
import type {ProcessoResumo} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";

const relatoriosStore = useRelatoriosStore();
const { notify } = useNotification();

const processoIdSelecionado = ref<number | null>(null);
const processosDisponiveis = ref<ProcessoResumo[]>([]);
const carregando = ref(false);

const relatorioAndamento = computed(() => relatoriosStore.relatorioAndamento);

const campos = [
  { key: 'siglaUnidade', label: 'Unidade', sortable: true },
  { key: 'situacaoAtual', label: 'Situação', sortable: true },
  { key: 'responsavel', label: 'Responsável', sortable: true },
];

const opcoesProcessos = computed(() => [
  { value: null, text: TEXTOS.relatorios.SELECIONE },
  ...processosDisponiveis.value.map(p => ({ value: p.codigo, text: p.descricao }))
]);

async function carregarProcessos() {
  try {
    const response = await painelService.listarProcessos({ page: 0, size: 100 });
    processosDisponiveis.value = response?.content ?? [];
  } catch {
    notify("Erro ao carregar processos", "danger");
  }
}

async function gerarRelatorio() {
  if (!processoIdSelecionado.value) return;
  carregando.value = true;
  await relatoriosStore.buscarRelatorioAndamento(processoIdSelecionado.value);
  carregando.value = false;
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
  }
}

async function exportarPdf() {
  if (!processoIdSelecionado.value) return;
  await relatoriosStore.exportarAndamentoPdf(processoIdSelecionado.value);
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_EXPORTAR, "danger");
  }
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
  carregarProcessos();
});
</script>
