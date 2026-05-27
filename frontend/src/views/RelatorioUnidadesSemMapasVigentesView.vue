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
import {BButton, BCard, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import UnidadesSemMapaArvore from "@/components/relatorios/UnidadesSemMapaArvore.vue";
import {useRelatorioUnidadesSemMapasTela} from "@/composables/useRelatorioUnidadesSemMapasTela";

const tela = useRelatorioUnidadesSemMapasTela();

const {
  carregando,
  unidadesSemMapaVigenteArvore,
  relatorioVisualizado,
  cardsRelatorio,
  visualizarRelatorio,
  exportarPdf,
} = tela;

defineExpose({
  carregando: tela.carregando,
  unidadesSemMapaVigenteArvore: tela.unidadesSemMapaVigenteArvore,
  relatorioVisualizado: tela.relatorioVisualizado,
  cardsRelatorio: tela.cardsRelatorio,
  visualizarRelatorio: tela.visualizarRelatorio,
  exportarPdf: tela.exportarPdf,
});
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
