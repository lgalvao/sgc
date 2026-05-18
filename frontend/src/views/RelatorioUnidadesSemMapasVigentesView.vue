<template>
  <LayoutPadrao>
    <PageHeader title="Unidades sem mapas vigentes">
      <template #actions>
        <BButton to="/relatorios" variant="outline-secondary">
          <i class="bi bi-arrow-left me-1"/> Voltar
        </BButton>
      </template>
    </PageHeader>

    <BCard class="relatorio-sem-mapa__filtros-card mb-4">
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
            class="relatorio-sem-mapa__card p-3"
        >
          <div class="relatorio-sem-mapa__cabecalho d-flex align-items-baseline gap-2 mb-3">
            <h3 class="relatorio-sem-mapa__titulo mb-0">{{ card.titulo }}</h3>
            <span
                v-if="card.subtitulo && card.titulo !== card.subtitulo"
                class="relatorio-sem-mapa__subtitulo"
            >
              {{ card.subtitulo }}
            </span>
          </div>

          <div v-if="card.unidades.length > 0" class="relatorio-sem-mapa__conteudo border rounded p-3">
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

function filtrarUnidadesExibidas(arvore: Unidade[]): Unidade[] {
  const unidadesExibidas: Unidade[] = [];

  for (const unidade of arvore) {
    if (unidade.filhas && unidade.filhas.length > 0) {
      unidadesExibidas.push(...unidade.filhas);
    }
  }

  return unidadesExibidas;
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
    const unidadesExibidas = filtrarUnidadesExibidas(arvore);

    unidadesSemMapaVigenteArvore.value = filtrarArvoreSemMapaVigente(unidadesExibidas, codigosSemMapaVigenteSet);
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

<style scoped>
.relatorio-sem-mapa__filtros-card,
.relatorio-sem-mapa__card {
  border: 1px solid var(--bs-border-color);
  background: var(--bs-body-bg);
  border-radius: 0.75rem;
  box-shadow: var(--bs-box-shadow-sm);
}

.relatorio-sem-mapa__cabecalho {
  padding-bottom: 0.85rem;
  border-bottom: 1px solid var(--bs-border-color);
}

.relatorio-sem-mapa__titulo {
  color: var(--bs-primary-text-emphasis);
  font-size: 1.45rem;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.relatorio-sem-mapa__subtitulo {
  color: var(--bs-secondary-color);
  font-size: 0.95rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.relatorio-sem-mapa__conteudo {
  background-color: transparent;
  border-color: var(--bs-border-color) !important;
  border-radius: 0.6rem !important;
}

@media (max-width: 768px) {
  .relatorio-sem-mapa__titulo {
    font-size: 1.2rem;
  }
}
</style>
