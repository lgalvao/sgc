<template>
  <EmptyState
      v-if="!mapa || mapa.competencias.length === 0"
      :title="TEXTOS.mapaVisualizacao.EMPTY_TITLE"
      :description="TEXTOS.mapaVisualizacao.EMPTY_DESCRIPTION"
      icon="bi-journal-x"
  />
  <template v-else>
    <BCard
        v-for="competencia in mapa.competencias"
        :key="competencia.codigo"
        class="mb-2 competencia-card"
        data-testid="cad-mapa__card-competencia"
        no-body
    >
      <BCardHeader>
        <BCardTitle class="fs-5 mb-0">
          <strong
              class="competencia-descricao"
              data-testid="vis-mapa__txt-competencia-descricao"
          >{{ competencia.descricao }}</strong>
        </BCardTitle>
      </BCardHeader>
      <BCardBody>
        <div class="d-flex flex-wrap gap-2">
          <BCard
              v-for="atividade in competencia.atividades"
              :key="atividade.codigo"
              class="atividade-associada-card-item"
              no-body
          >
            <BCardBody>
              <span class="atividade-associada-descricao">{{ atividade.descricao }}</span>
              <BBadge
                  v-if="(atividade.conhecimentos?.length ?? 0) > 0"
                  variant="secondary"
                  class="ms-2"
              >
                {{ atividade.conhecimentos.length }}
              </BBadge>
              <ul v-if="(atividade.conhecimentos?.length ?? 0) > 0" class="mb-0 mt-2 ps-3">
                <li
                    v-for="conhecimento in atividade.conhecimentos"
                    :key="conhecimento.codigo"
                    data-testid="txt-conhecimento-item"
                >
                  {{ conhecimento.descricao }}
                </li>
              </ul>
            </BCardBody>
          </BCard>
        </div>
      </BCardBody>
    </BCard>
  </template>
</template>

<script lang="ts" setup>
import {BBadge, BCard, BCardBody, BCardHeader, BCardTitle} from "bootstrap-vue-next";
import EmptyState from "@/components/comum/EmptyState.vue";
import {TEXTOS} from "@/constants/textos";
import type {MapaVisualizacao} from "@/types/tipos";

defineProps<{
  mapa: MapaVisualizacao | null;
}>();
</script>

<style scoped>
.competencia-card {
  transition: box-shadow 0.2s;
}

.competencia-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.competencia-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.atividade-associada-card-item {
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  background-color: var(--bs-secondary-bg);
}

.atividade-associada-descricao {
  font-size: 0.85rem;
  color: var(--bs-body-color);
}
</style>
