<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.relatorios.TITULO" />

    <BCard class="mb-4">
      <BTabs content-class="mt-3" fill>
        <BTab active :title="TEXTOS.relatorios.ANDAMENTO_PROCESSO">
          <BRow class="mb-3">
            <BCol md="6">
              <BFormGroup :label="TEXTOS.relatorios.LABEL_SELECIONE_PROCESSO" label-for="select-processo-andamento">
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
                variant="success"
                @click="gerarRelatorioAndamento"
              >
                <BSpinner v-if="gerandoAndamento" small />
                {{ gerandoAndamento ? TEXTOS.relatorios.BOTAO_GERANDO : TEXTOS.relatorios.BOTAO_GERAR }}
              </BButton>
            </BCol>
             <BCol v-if="relatorioAndamento.length > 0" class="d-flex align-items-end" md="2">
               <BButton variant="outline-secondary" @click="exportarPdfAndamento">
                 <i class="bi bi-file-earmark-pdf me-2" />
                 {{ TEXTOS.relatorios.BOTAO_PDF }}
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
             :description="TEXTOS.relatorios.EMPTY_DESCRIPTION"
             icon="bi-table"
             :title="TEXTOS.relatorios.EMPTY_TITLE"
          />
        </BTab>
        <BTab :title="TEXTOS.relatorios.MAPAS">
          <BRow class="mb-3">
            <BCol md="4">
              <BFormGroup :label="TEXTOS.relatorios.LABEL_SELECIONE_PROCESSO" label-for="select-processo-mapas">
                <BFormSelect
                  id="select-processo-mapas"
                  v-model="processoIdSelecionadoMapas"
                  :options="opcoesProcessos"
                />
              </BFormGroup>
            </BCol>
            <BCol md="4">
              <BFormGroup :label="TEXTOS.relatorios.LABEL_SELECIONE_UNIDADE" label-for="select-unidade-mapas">
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
                 variant="success"
                 @click="gerarRelatorioMapas"
              >
                 <BSpinner v-if="gerandoMapas" small />
                 <i class="bi bi-file-earmark-pdf me-2" />
                 {{ TEXTOS.relatorios.BOTAO_GERAR_PDF }}
              </BButton>
            </BCol>
          </BRow>
        </BTab>
      </BTabs>
    </BCard>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton, BCard, BCol, BFormGroup, BFormSelect, BRow, BSpinner, BTab, BTable, BTabs} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useProcessos} from "@/composables/useProcessos";
import {usePerfil} from "@/composables/usePerfil";
import {usePerfilStore} from "@/stores/perfil";
import {useNotification} from "@/composables/useNotification";
import {useRelatorios} from "@/composables/api/useRelatorios";
import {TEXTOS} from "@/constants/textos";

const {
  processosPainel,
  buscarProcessosPainel
} = useProcessos();
const perfil = usePerfil();
const perfilStore = usePerfilStore();
const { notify } = useNotification();
const { obterRelatorioAndamento, downloadRelatorioAndamentoPdf, downloadRelatorioMapasPdf } = useRelatorios();

const processoIdSelecionado = ref<number | null>(null);
const processoIdSelecionadoMapas = ref<number | null>(null);
const unidadeIdSelecionadaMapas = ref<number | null>(null);

const gerandoAndamento = ref(false);
const gerandoMapas = ref(false);

const relatorioAndamento = ref<any[]>([]);

const opcoesProcessos = computed(() => {
  const processos = processosPainel.value || [];
  return [
    { value: null, text: TEXTOS.relatorios.SELECIONE },
    ...processos.map(p => ({ value: p.codigo, text: p.descricao }))
  ];
});

// Options for units (mocked for now, will connect to useUnidadesStore if necessary)
const opcoesUnidades = ref<Array<{ value: number | null, text: string }>>([
  { value: null, text: TEXTOS.relatorios.TODAS_UNIDADES }
]);

const gerarRelatorioAndamento = async () => {
  if (!processoIdSelecionado.value) return;
  gerandoAndamento.value = true;
  try {
     relatorioAndamento.value = await obterRelatorioAndamento(processoIdSelecionado.value);
  } catch {
     notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
  } finally {
     gerandoAndamento.value = false;
  }
};

const exportarPdfAndamento = async () => {
  if (!processoIdSelecionado.value) return;
  try {
      await downloadRelatorioAndamentoPdf(processoIdSelecionado.value);
  } catch {
      notify(TEXTOS.relatorios.ERRO_EXPORTAR, "danger");
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
       notify(TEXTOS.relatorios.ERRO_GERAR, "danger");
    } finally {
       gerandoMapas.value = false;
    }
}

onMounted(async () => {
   if (perfil.perfilSelecionado.value && perfilStore.unidadeSelecionada) {
      await buscarProcessosPainel(
          perfil.perfilSelecionado.value,
          Number(perfilStore.unidadeSelecionada),
          0,
          100
      );
   }
});
</script>
