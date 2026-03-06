<template>
  <LayoutPadrao>
    <PageHeader title="Relatórios" />

    <BCard class="mb-4">
      <BTabs content-class="mt-3" fill>
        <BTab active title="Andamento de processo">
          <BRow class="mb-3">
            <BCol md="6">
              <BFormGroup label="Selecione o Processo" label-for="select-processo-andamento">
                <BFormSelect
                  id="select-processo-andamento"
                  v-model="processoIdSelecionado"
                  :options="opcoesProcessos"
                />
              </BFormGroup>
            </BCol>
            <BCol class="d-flex align-items-end" md="2">
              <BButton
                :disabled="!processoIdSelecionado || gerandoAndamento"
                variant="primary"
                @click="gerarRelatorioAndamento"
              >
                <BSpinner v-if="gerandoAndamento" small />
                {{ gerandoAndamento ? 'Gerando...' : 'Gerar Relatório' }}
              </BButton>
            </BCol>
             <BCol v-if="relatorioAndamento.length > 0" class="d-flex align-items-end" md="2">
               <BButton variant="outline-danger" @click="exportarPdfAndamento">
                 <i class="bi bi-file-earmark-pdf me-2" />
                 Sair em PDF
               </BButton>
             </BCol>
          </BRow>

          <BTable
            v-if="relatorioAndamento.length > 0"
            :items="relatorioAndamento"
            class="mt-4"
            hover
            responsive
            striped
          />
          <EmptyState
            v-else-if="processoIdSelecionado && !gerandoAndamento"
             description="Nenhum dado encontrado ou relatório ainda não gerado."
             icon="bi-table"
             title="Relatório de Andamento"
          />
        </BTab>
        <BTab title="Mapas">
          <BRow class="mb-3">
            <BCol md="4">
              <BFormGroup label="Selecione o Processo" label-for="select-processo-mapas">
                <BFormSelect
                  id="select-processo-mapas"
                  v-model="processoIdSelecionadoMapas"
                  :options="opcoesProcessos"
                />
              </BFormGroup>
            </BCol>
            <BCol md="4">
              <BFormGroup label="Selecione a unidade" label-for="select-unidade-mapas">
                 <!-- Integrate with real units endpoint or store if needed -->
                 <BFormSelect
                   id="select-unidade-mapas"
                   v-model="unidadeIdSelecionadaMapas"
                   :options="opcoesUnidades"
                 />
              </BFormGroup>
            </BCol>
            <BCol class="d-flex align-items-end" md="4">
              <BButton
                :disabled="!processoIdSelecionadoMapas || gerandoMapas"
                 variant="primary"
                 @click="gerarRelatorioMapas"
              >
                 <BSpinner v-if="gerandoMapas" small />
                 <i class="bi bi-file-earmark-pdf me-2" />
                 Gerar PDF
              </BButton>
            </BCol>
          </BRow>
        </BTab>
      </BTabs>
    </BCard>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import { BButton, BCard, BCol, BFormGroup, BFormSelect, BRow, BSpinner, BTable, BTabs, BTab } from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import { useProcessosStore } from "@/stores/processos";
import { useNotification } from "@/composables/useNotification";
import { useRelatorios } from "@/composables/api/useRelatorios";

const processosStore = useProcessosStore();
const { notify } = useNotification();
const { obterRelatorioAndamento, downloadRelatorioAndamentoPdf, downloadRelatorioMapasPdf } = useRelatorios();

// Tabs Config
const processoIdSelecionado = ref<number | null>(null);
const processoIdSelecionadoMapas = ref<number | null>(null);
const unidadeIdSelecionadaMapas = ref<number | null>(null);

// Loading states
const gerandoAndamento = ref(false);
const gerandoMapas = ref(false);

const relatorioAndamento = ref<any[]>([]);

// Options
const opcoesProcessos = computed(() => {
  const processos = processosStore.processosPainel || [];
  return [
    { value: null, text: "Selecione..." },
    ...processos.map(p => ({ value: p.codigo, text: p.descricao }))
  ];
});

// Options for units (mocked for now, will connect to useUnidadesStore if necessary)
const opcoesUnidades = ref<Array<{ value: number | null, text: string }>>([
  { value: null, text: "Todas as unidades (processo inteiro)" }
]);

const gerarRelatorioAndamento = async () => {
  if (!processoIdSelecionado.value) return;
  gerandoAndamento.value = true;
  try {
     relatorioAndamento.value = await obterRelatorioAndamento(processoIdSelecionado.value);
  } catch {
     notify("Erro ao buscar relatório", "danger");
  } finally {
     gerandoAndamento.value = false;
  }
};

const exportarPdfAndamento = async () => {
  if (!processoIdSelecionado.value) return;
  try {
      await downloadRelatorioAndamentoPdf(processoIdSelecionado.value);
  } catch {
      notify("Erro ao exportar PDF", "danger");
  }
};

const gerarRelatorioMapas = async () => {
    if (!processoIdSelecionadoMapas.value) return;
    gerandoMapas.value = true;
    try {
        await downloadRelatorioMapasPdf(
            processoIdSelecionadoMapas.value, 
            unidadeIdSelecionadaMapas.value || undefined
        );
    } catch {
       notify("Erro ao gerar PDF", "danger");
    } finally {
       gerandoMapas.value = false;
    }
}

onMounted(async () => {
   // Basic loading of available processes for the select box
   // The store might need a new method to fetch just the list of active processes globally for reports
   // For now reusing the painel method as fallback
});
</script>
