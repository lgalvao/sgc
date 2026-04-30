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
              :options="opcoesUnidades"
              data-testid="select-unidade-mapas"
            />
          </BFormGroup>
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
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton, BCard, BCol, BFormGroup, BFormInvalidFeedback, BFormSelect, BRow, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {useRelatoriosStore} from "@/stores/relatorios";
import {usePerfilStore} from "@/stores/perfil";
import {TEXTOS} from "@/constants/textos";
import * as painelService from "@/services/painelService";
import type {ProcessoResumo} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

const relatoriosStore = useRelatoriosStore();
const perfilStore = usePerfilStore();
const { notify } = useNotification();
const {
  validarSubmissao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const processoIdSelecionado = ref<number | null>(null);
const unidadeIdSelecionada = ref<number | null>(null);
const processosDisponiveis = ref<ProcessoResumo[]>([]);
const carregando = ref(false);

const mensagemErroProcesso = computed(() => {
  return deveExibirErro(!processoIdSelecionado.value) ? "Selecione um processo." : "";
});

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

onMounted(() => {
  carregarProcessos();
});
</script>
