<template>
  <BCard class="relatorio-mapas__card shadow-sm" data-testid="card-mapa-vigente" no-body>
    <BCardBody>
      <BCardTitle class="mb-3 relatorio-mapas__cabecalho">
        <span class="relatorio-mapas__titulo">{{ mapa.siglaUnidade }}</span>
        <span class="relatorio-mapas__subtitulo">{{ mapa.nomeUnidade }}</span>
      </BCardTitle>

      <div class="d-flex flex-column gap-2">
        <section
            v-for="competencia in mapa.competencias"
            :key="competencia.codigo"
            class="relatorio-mapas__competencia"
        >
          <h6 class="mb-2 relatorio-mapas__secao">{{ competencia.descricao }}</h6>

          <div
              v-for="atividade in competencia.atividades"
              :key="atividade.codigo"
              class="relatorio-mapas__atividade"
          >
            <div class="relatorio-mapas__atividade-titulo">{{ atividade.descricao }}</div>
            <ul v-if="atividade.conhecimentos.length > 0" class="relatorio-mapas__conhecimentos">
              <li v-for="conhecimento in atividade.conhecimentos" :key="conhecimento.codigo">
                {{ conhecimento.descricao }}
              </li>
            </ul>
          </div>
        </section>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {BCard, BCardBody, BCardTitle} from "bootstrap-vue-next";

interface Conhecimento {
  codigo: number;
  descricao: string;
}

interface Atividade {
  codigo: number;
  descricao: string;
  conhecimentos: Conhecimento[];
}

interface Competencia {
  codigo: number;
  descricao: string;
  atividades: Atividade[];
}

interface MapaRelatorio {
  codigoUnidade: number;
  siglaUnidade: string;
  nomeUnidade: string;
  competencias: Competencia[];
}

defineProps<{
  mapa: MapaRelatorio;
}>();
</script>

<style scoped>
.relatorio-mapas__card {
  border: 1px solid var(--bs-border-color);
  background: var(--bs-body-bg);
}

.relatorio-mapas__cabecalho {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  padding-bottom: 0.85rem;
  margin-bottom: 0.9rem;
  border-bottom: 1px solid var(--bs-border-color);
}

.relatorio-mapas__titulo {
  color: var(--bs-primary-text-emphasis);
  font-size: 1.45rem;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.relatorio-mapas__subtitulo {
  color: var(--bs-secondary-color);
  font-size: 0.95rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.relatorio-mapas__competencia {
  padding: 0.95rem 1rem;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.65rem;
  background: var(--bs-secondary-bg);
}

.relatorio-mapas__secao {
  color: var(--bs-heading-color);
  font-size: 1.08rem;
  font-weight: 700;
  line-height: 1.35;
}

.relatorio-mapas__atividade + .relatorio-mapas__atividade {
  margin-top: 0.7rem;
}

.relatorio-mapas__atividade-titulo {
  color: var(--bs-body-color);
  font-size: 0.98rem;
  font-weight: 600;
  line-height: 1.4;
}

.relatorio-mapas__conhecimentos {
  margin: 0.35rem 0 0;
  padding-left: 1.2rem;
  color: var(--bs-body-color);
}

.relatorio-mapas__conhecimentos li + li {
  margin-top: 0.18rem;
}

@media (max-width: 768px) {
  .relatorio-mapas__titulo {
    font-size: 1.2rem;
  }

  .relatorio-mapas__competencia {
    padding: 0.8rem 0.85rem;
  }
}
</style>
