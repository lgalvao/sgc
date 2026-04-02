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
import {computed, onMounted, ref, type Ref} from "vue";
import {BButton, BCard, BCol, BFormGroup, BFormSelect, BRow, BSpinner, BTab, BTable, BTabs} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {usePerfil} from "@/composables/usePerfil";
import {usePerfilStore} from "@/stores/perfil";
import {useNotification} from "@/composables/useNotification";
import {useRelatorios} from "@/composables/api/useRelatorios";
import {TEXTOS} from "@/constants/textos";
import * as painelService from "@/services/painelService";
import type {ProcessoResumo} from "@/types/tipos";

const perfil = usePerfil();
const perfilStore = usePerfilStore();
const { notify } = useNotification();
const { obterRelatorioAndamento, downloadRelatorioAndamentoPdf, downloadRelatorioMapasPdf } = useRelatorios();
const processosDisponiveis = ref<ProcessoResumo[]>([]);

const processoIdSelecionado = ref<number | null>(null);
const processoIdSelecionadoMapas = ref<number | null>(null);
const unidadeIdSelecionadaMapas = ref<number | null>(null);

const gerandoAndamento = ref(false);
const gerandoMapas = ref(false);

const relatorioAndamento = ref<Array<Record<string, unknown>>>([]);

const opcoesProcessos = computed(() => {
  const processos = processosDisponiveis.value;
  return [
    { value: null, text: TEXTOS.relatorios.SELECIONE },
    ...processos.map(p => ({ value: p.codigo, text: p.descricao }))
  ];
});

const opcoesUnidades = computed<Array<{ value: number | null, text: string }>>(() => {
  const codigoUnidade = obterCodigoUnidadeSelecionada();
  const opcoesBase = [{ value: null, text: TEXTOS.relatorios.TODAS_UNIDADES }];

  if (!codigoUnidade) {
    return opcoesBase;
  }

  const sigla = perfilStore.unidadeSelecionadaSigla ?? `Unidade ${codigoUnidade}`;
  return [...opcoesBase, { value: codigoUnidade, text: sigla }];
});

function obterCodigoUnidadeSelecionada() {
  if (!perfil.perfilSelecionado.value || !perfilStore.unidadeSelecionada) {
    return null;
  }

  const unidadeSelecionada = perfilStore.unidadeSelecionada as unknown;
  if (typeof unidadeSelecionada === "number") {
    return unidadeSelecionada;
  }

  if (typeof unidadeSelecionada === "object" && unidadeSelecionada !== null && "codigo" in unidadeSelecionada) {
    return Number(unidadeSelecionada.codigo);
  }

  return null;
}

async function executarComCarregamento(
  acao: () => Promise<void>,
  mensagemErro: string,
  carregando?: Ref<boolean>
) {
  if (carregando) {
    carregando.value = true;
  }

  try {
    await acao();
  } catch {
    notify(mensagemErro, "danger");
  } finally {
    if (carregando) {
      carregando.value = false;
    }
  }
}

async function carregarProcessosDisponiveis() {
  const unidadeCodigo = obterCodigoUnidadeSelecionada();
  if (!unidadeCodigo) {
    processosDisponiveis.value = [];
    return;
  }

  const response = await painelService.listarProcessos(unidadeCodigo, 0, 100);
  processosDisponiveis.value = response?.content ?? [];
}

const gerarRelatorioAndamento = async () => {
  if (!processoIdSelecionado.value) return;
  await executarComCarregamento(async () => {
    relatorioAndamento.value = await obterRelatorioAndamento(processoIdSelecionado.value);
  }, TEXTOS.relatorios.ERRO_BUSCA, gerandoAndamento);
};

const exportarPdfAndamento = async () => {
  if (!processoIdSelecionado.value) return;
  await executarComCarregamento(async () => {
    await downloadRelatorioAndamentoPdf(processoIdSelecionado.value!);
  }, TEXTOS.relatorios.ERRO_EXPORTAR);
};

const gerarRelatorioMapas = async () => {
    if (!processoIdSelecionadoMapas.value) return;
    await executarComCarregamento(async () => {
      await downloadRelatorioMapasPdf(
        processoIdSelecionadoMapas.value!,
        unidadeIdSelecionadaMapas.value || undefined
      );
    }, TEXTOS.relatorios.ERRO_GERAR, gerandoMapas);
}

onMounted(async () => {
   await carregarProcessosDisponiveis();
});
</script>
