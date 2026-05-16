<template>
  <LayoutPadrao>
    <PageHeader title="Unidades sem mapas vigentes">
      <template #actions>
        <BButton to="/relatorios" variant="outline-secondary">
          <i class="bi bi-arrow-left me-1"/> Voltar
        </BButton>
      </template>
    </PageHeader>

    <BCard class="mb-4">
      <div class="d-flex flex-wrap gap-2">
        <BButton
            :disabled="carregando"
            data-testid="btn-visualizar-unidades-sem-mapa"
            variant="success"
            @click="visualizarRelatorio"
        >
          <BSpinner v-if="carregando" class="me-1" small/>
          <i v-else class="bi bi-eye me-1"/>
          Visualizar
        </BButton>
        <BButton
            :disabled="carregando"
            data-testid="btn-pdf-unidades-sem-mapa"
            variant="outline-danger"
            @click="exportarPdf"
        >
          <BSpinner v-if="carregando" class="me-1" small/>
          <i v-else class="bi bi-file-earmark-pdf me-1"/>
          PDF
        </BButton>
      </div>
    </BCard>

    <CarregamentoPagina v-if="carregando && relatorioVisualizado"/>

    <template v-else-if="relatorioVisualizado">
      <EmptyState
          v-if="unidadesSemMapaVigenteArvore.length === 0"
          title="Não há unidades sem mapa vigente."
          icon="bi-building"
      />

      <div v-else class="d-flex flex-column gap-3">
        <BCard
            v-for="card in cardsRelatorio"
            :key="card.chave"
            class="p-3"
        >
          <div class="d-flex align-items-baseline gap-2 mb-3">
            <h3 class="mb-0 text-primary fw-bold">{{ card.titulo }}</h3>
            <span v-if="card.subtitulo && card.titulo !== card.subtitulo" class="text-uppercase small fw-bold text-secondary">{{ card.subtitulo }}</span>
          </div>

          <div v-if="card.unidades.length > 0" class="border rounded p-3">
            <UnidadesSemMapaArvore :unidades="card.unidades"/>
          </div>
        </BCard>
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
import {BButton, BCard, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {buscarCodigosUnidadesSemMapaVigente, buscarTodasUnidades} from "@/services/unidadeService";
import {relatoriosService} from "@/services/relatoriosService";
import {TEXTOS} from "@/constants/textos";
import {useNotification} from "@/composables/useNotification";
import UnidadesSemMapaArvore from "@/components/relatorios/UnidadesSemMapaArvore.vue";
import type {Unidade} from "@/types/tipos";
import {organizarArvoreUnidades, TITULO_GRUPO_ZONAS_ELEITORAIS} from "@/utils/treeUtils";

const {notify} = useNotification();
const carregando = ref(false);
const unidadesSemMapaVigenteArvore = ref<Unidade[]>([]);
const relatorioVisualizado = ref(false);

type CardUnidade = {
  chave: string;
  titulo: string;
  subtitulo?: string;
  unidades: Unidade[];
};

function ordenarComoArvoreUnidades(unidades: Unidade[]): Unidade[] {
  return organizarArvoreUnidades(unidades, "raiz", {
    obterCodigo: (unidade) => unidade.codigo,
    obterRotulo: (unidade) => unidade.nome,
    obterSigla: (unidade) => unidade.sigla,
    obterTipo: (unidade) => unidade.tipo,
    obterFilhos: (unidade) => unidade.filhas,
    clonarComFilhos: (unidade, filhas) => ({
      ...unidade,
      filhas
    }),
    criarGrupoZonas: (_, filhas) => ({
      codigo: -1,
      sigla: TITULO_GRUPO_ZONAS_ELEITORAIS,
      nome: TITULO_GRUPO_ZONAS_ELEITORAIS,
      tipo: "AGRUPADOR_VISUAL",
      filhas
    }),
    criarIdentificadorGrupoFilhos: (unidade) => unidade.codigo
  });
}

function filtrarArvoreSemMapaVigente(unidades: Unidade[], codigosSemMapaVigente: Set<number>): Unidade[] {
  return unidades
      .map((unidade): Unidade | null => {
        const filhasFiltradas = filtrarArvoreSemMapaVigente(unidade.filhas ?? [], codigosSemMapaVigente);
        const unidadeSemMapaVigente = codigosSemMapaVigente.has(unidade.codigo);

        if (!unidadeSemMapaVigente && filhasFiltradas.length === 0) {
          return null;
        }

        return {
          ...unidade,
          filhas: filhasFiltradas
        };
      })
      .filter((unidade): unidade is Unidade => unidade !== null);
}

const cardsRelatorio = computed<CardUnidade[]>(() => {
  const maesOrdenadas = ordenarComoArvoreUnidades(unidadesSemMapaVigenteArvore.value);

  return maesOrdenadas.map(unidadeMae => {
    // Se for um agrupador visual (Zonas), as filhas já estão prontas/ordenadas
    const unidades = unidadeMae.tipo === "AGRUPADOR_VISUAL"
        ? (unidadeMae.filhas ?? [])
        : ordenarComoArvoreUnidades(unidadeMae.filhas ?? []);

    return {
      chave: `card-unidade-${unidadeMae.codigo}`,
      titulo: unidadeMae.sigla,
      subtitulo: unidadeMae.nome,
      unidades
    };
  });
});

async function carregarUnidadesSemMapaVigente() {
  carregando.value = true;
  await Promise.all([
    buscarTodasUnidades(),
    buscarCodigosUnidadesSemMapaVigente()
  ]).then(([arvore, codigosSemMapaVigente]) => {
    const codigosSemMapaVigenteSet = new Set(codigosSemMapaVigente);

    unidadesSemMapaVigenteArvore.value = filtrarArvoreSemMapaVigente(arvore, codigosSemMapaVigenteSet);
  }).catch(() => notify(TEXTOS.relatorios.ERRO_BUSCA, "danger"))
      .finally(() => { carregando.value = false; });
}

async function visualizarRelatorio() {
  relatorioVisualizado.value = true;
  await carregarUnidadesSemMapaVigente();
}

async function exportarPdf() {
  carregando.value = true;
  await relatoriosService.downloadRelatorioUnidadesSemMapasVigentesPdf()
      .catch(() => notify(TEXTOS.relatorios.ERRO_EXPORTAR, "danger"))
      .finally(() => { carregando.value = false; });
}
</script>
