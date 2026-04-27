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
                    data-testid="cad-mapa__txt-competencia-descricao"
                >{{ competencia.descricao }}</strong>
              </BCardTitle>
            </BCardHeader>
            <BCardBody>
              <div class="d-flex flex-wrap gap-2">
                <BCard
                    v-for="atividade in competencia.atividades"
                    :key="atividade.codigo"
                    class="atividade-associada-card-item d-flex"
                    no-body
                >
                  <BCardBody class="p-2">
                    <span class="atividade-associada-descricao fw-bold text-break d-block mb-1">
                      {{ atividade.descricao }}
                    </span>
                    <div v-if="(atividade.conhecimentos?.length ?? 0) > 0" class="conhecimentos-inline mt-1">
                      <ul class="list-unstyled mb-0 small text-muted border-top pt-1">
                        <li
                            v-for="conhecimento in atividade.conhecimentos"
                            :key="conhecimento.codigo"
                            class="conhecimento-item text-break"
                            data-testid="txt-conhecimento-item"
                        >
                          <i aria-hidden="true" class="bi bi-dot me-1"/>{{ conhecimento.descricao }}
                        </li>
                      </ul>
                    </div>
                  </BCardBody>
                </BCard>
              </div>
            </BCardBody>
          </BCard>
  </template>
</template>

<script lang="ts" setup>
import {BCard, BCardBody, BCardHeader, BCardTitle} from "bootstrap-vue-next";
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
  overflow-wrap: anywhere;
  word-break: break-word;
}

.atividade-associada-card-item {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 0.5rem;
  background-color: transparent;
  min-width: 150px;
  max-width: 300px;
  flex: 1 1 auto;
  transition: all 0.2s ease-in-out;
}

.atividade-associada-card-item:hover {
  border-color: var(--bs-primary-border-subtle);
  background-color: rgba(var(--bs-primary-rgb), 0.02);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.atividade-associada-descricao {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--bs-emphasis-color);
  overflow-wrap: anywhere;
  word-break: break-word;
}

.conhecimento-item {
  line-height: 1.2;
  overflow-wrap: anywhere;
  word-break: break-word;
}
</style>
