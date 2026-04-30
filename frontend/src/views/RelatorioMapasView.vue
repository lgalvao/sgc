<template>
  <LayoutPadrao>
    <PageHeader title="Relatório: Mapas vigentes">
      <template #actions>
        <BButton variant="outline-secondary" to="/relatorios">
          <i class="bi bi-arrow-left me-1"/> Voltar
        </BButton>
      </template>
    </PageHeader>

    <BCard class="mb-4">
      <BRow class="align-items-end">
        <BCol md="5">
          <BFormGroup
              label-for="select-processo-mapas"
              :state="mensagemErroProcesso ? false : null"
          >
            <template #label>
              {{ TEXTOS.relatorios.LABEL_SELECIONE_PROCESSO }} <span aria-hidden="true" class="text-danger">*</span>
            </template>
            <BFormSelect
              id="select-processo-mapas"
              v-model="processoIdSelecionado"
              :options="opcoesProcessos"
              :state="mensagemErroProcesso ? false : null"
              data-testid="select-processo-mapas"
            />
            <BFormInvalidFeedback :state="mensagemErroProcesso ? false : null">
              {{ mensagemErroProcesso }}
            </BFormInvalidFeedback>
          </BFormGroup>
        </BCol>
        <BCol md="4">
          <BFormGroup :label="TEXTOS.relatorios.LABEL_SELECIONE_UNIDADE" label-for="select-unidade-mapas">
            <BFormSelect
              id="select-unidade-mapas"
              v-model="unidadeIdSelecionada"
              :disabled="!processoIdSelecionado"
              :options="opcoesUnidades"
              data-testid="select-unidade-mapas"
            />
          </BFormGroup>
        </BCol>
        <BCol md="auto">
          <BButton
            :disabled="carregando"
            variant="primary"
            data-testid="btn-gerar-html-mapas"
            @click="gerarRelatorio"
          >
            <BSpinner v-if="carregando" small class="me-1" />
            <i v-else class="bi bi-search me-1" />
            {{ TEXTOS.relatorios.BOTAO_GERAR }}
          </BButton>
        </BCol>
        <BCol md="auto">
          <BButton
            :disabled="carregando"
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

    <div v-if="carregando && relatorioMapas.length === 0" class="text-center py-5">
      <BSpinner variant="primary"/>
    </div>

    <template v-else-if="relatorioMapas.length > 0">
      <div class="d-flex flex-column gap-3">
        <BCard
            v-for="mapa in relatorioMapas"
            :key="mapa.codigoUnidade"
            class="border-start border-4 border-secondary shadow-sm"
            no-body
            data-testid="card-relatorio-mapas"
        >
          <BCardBody>
            <BCardTitle class="mb-3">
              <span class="fw-bold text-primary">{{ mapa.siglaUnidade }} - {{ mapa.nomeUnidade }}</span>
            </BCardTitle>

            <div class="mb-3 pb-3 border-bottom">
              <span class="text-muted d-block small">Competências vigentes</span>
              <span class="fw-bold text-dark">{{ mapa.totalCompetencias }}</span>
            </div>

            <div class="d-flex flex-column gap-3">
              <section
                  v-for="competencia in mapa.competencias"
                  :key="competencia.codigo"
                  class="relatorio-mapas__competencia"
              >
                <h6 class="fw-bold text-primary mb-2">{{ competencia.descricao }}</h6>

                <div
                    v-for="atividade in competencia.atividades"
                    :key="atividade.codigo"
                    class="mb-2"
                >
                  <div class="fw-semibold">{{ atividade.descricao }}</div>
                  <ul v-if="atividade.conhecimentos.length > 0" class="mb-0 mt-1 ps-3 text-muted">
                    <li v-for="conhecimento in atividade.conhecimentos" :key="conhecimento.codigo">
                      {{ conhecimento.descricao }}
                    </li>
                  </ul>
                </div>
              </section>
            </div>
          </BCardBody>
        </BCard>
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, watch} from "vue";
import {BButton, BCard, BCardBody, BCardTitle, BCol, BFormGroup, BFormInvalidFeedback, BFormSelect, BRow, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {useRelatoriosStore} from "@/stores/relatorios";
import {TEXTOS} from "@/constants/textos";
import * as painelService from "@/services/painelService";
import * as processoService from "@/services/processoService";
import type {ProcessoResumo, UnidadeParticipante} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

const relatoriosStore = useRelatoriosStore();
const { notify } = useNotification();
const {
  validarSubmissao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const processoIdSelecionado = ref<number | null>(null);
const unidadeIdSelecionada = ref<number | null>(null);
const processosDisponiveis = ref<ProcessoResumo[]>([]);
const unidadesParticipantes = ref<Array<{ value: number; text: string }>>([]);
const carregando = ref(false);
const relatorioMapas = computed(() => relatoriosStore.relatorioMapas);

const mensagemErroProcesso = computed(() => {
  return deveExibirErro(!processoIdSelecionado.value) ? "Selecione um processo." : "";
});

const opcoesProcessos = computed(() => [
  { value: null, text: TEXTOS.relatorios.SELECIONE },
  ...processosDisponiveis.value.map(p => ({ value: p.codigo, text: p.descricao }))
]);

const opcoesUnidades = computed(() => [
  {
    value: null,
    text: processoIdSelecionado.value ? TEXTOS.relatorios.TODAS_UNIDADES : "(Selecione um processo)"
  },
  ...unidadesParticipantes.value
]);

async function carregarProcessos() {
  try {
    const response = await painelService.listarProcessos({ page: 0, size: 100 });
    processosDisponiveis.value = response?.content ?? [];
  } catch {
    notify("Erro ao carregar processos", "danger");
  }
}

function coletarUnidadesParticipantes(unidades: UnidadeParticipante[]): Array<{ value: number; text: string }> {
  const opcoes = new Map<number, string>();

  const visitar = (itens: UnidadeParticipante[]) => {
    itens.forEach((unidade) => {
      const sigla = unidade.sigla?.trim() ?? "";
      const nome = unidade.nome?.trim() ?? "";
      const ehVirtual = sigla.toUpperCase() === "ADMIN";

      if (!ehVirtual && unidade.codUnidade) {
        opcoes.set(unidade.codUnidade, sigla);
      }

      if (unidade.filhos?.length) {
        visitar(unidade.filhos);
      }
    });
  };

  visitar(unidades);

  return Array.from(opcoes.entries())
      .map(([value, text]) => ({value, text}))
      .sort((a, b) => a.text.localeCompare(b.text, "pt-BR"));
}

async function carregarUnidadesDoProcesso(codProcesso: number | null) {
  if (!codProcesso) {
    unidadesParticipantes.value = [];
    unidadeIdSelecionada.value = null;
    return;
  }

  try {
    const processo = await processoService.obterDetalhesProcesso(codProcesso);
    unidadesParticipantes.value = coletarUnidadesParticipantes(processo.unidades ?? []);

    if (!unidadesParticipantes.value.some((unidade) => unidade.value === unidadeIdSelecionada.value)) {
      unidadeIdSelecionada.value = null;
    }
  } catch {
    unidadesParticipantes.value = [];
    unidadeIdSelecionada.value = null;
    notify("Erro ao carregar unidades do processo", "danger");
  }
}

async function exportarPdf() {
  if (!validarSubmissao(!!processoIdSelecionado.value)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  carregando.value = true;
  await relatoriosStore.exportarMapasPdf(processoIdSelecionado.value!, unidadeIdSelecionada.value || undefined);
  carregando.value = false;
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_GERAR, "danger");
  }
}

async function gerarRelatorio() {
  if (!validarSubmissao(!!processoIdSelecionado.value)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  carregando.value = true;
  await relatoriosStore.buscarRelatorioMapas(processoIdSelecionado.value!, unidadeIdSelecionada.value || undefined);
  carregando.value = false;
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
  }
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
  carregarProcessos();
});

watch(processoIdSelecionado, async (novoProcessoId) => {
  await carregarUnidadesDoProcesso(novoProcessoId);
});
</script>
