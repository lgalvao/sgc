<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
      hide-footer
      size="lg"
      title="Impacto no Mapa de Competências"
      @hide="fechar">
    <div v-if="loading" class="text-center p-4">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
      <p class="mt-2">
        Verificando impactos...
      </p>
    </div>

    <div v-else-if="impacto" data-testid="modal-impacto-body">
      <BAlert v-if="!impacto.temImpactos" :fade="false" :model-value="true" variant="success">
        <i class="bi bi-check-circle me-2"/> Nenhum impacto detectado no mapa.
      </BAlert>

      <div v-else class="mt-3">
        <!-- Inseridas -->
        <div v-if="impacto.atividadesInseridas.length > 0" class="mb-4">
          <h5 class="text-success mb-3">
            <i class="bi bi-plus-circle me-2"/>Atividades Inseridas
          </h5>
          <ul class="list-group" data-testid="lista-atividades-inseridas">
            <li
                v-for="ativ in impacto.atividadesInseridas"
                :key="ativ.codigo"
                class="list-group-item"
            >
              <strong>{{ ativ.descricao }}</strong>
              <div
                  v-if="ativ.competenciasVinculadas && ativ.competenciasVinculadas.length > 0"
                  class="mt-1"
              >
                <small class="text-muted">Vinculada a: {{ ativ.competenciasVinculadas.join(', ') }}</small>
              </div>
            </li>
          </ul>
        </div>

        <!-- Removidas -->
        <div v-if="impacto.atividadesRemovidas.length > 0" class="mb-4">
          <h5 class="text-danger mb-3">
            <i class="bi bi-dash-circle me-2"/>Atividades Removidas
          </h5>
          <ul class="list-group" data-testid="lista-atividades-removidas">
            <li v-for="ativ in impacto.atividadesRemovidas" :key="ativ.codigo" class="list-group-item">
              <strong class="text-decoration-line-through text-muted">{{ ativ.descricao }}</strong>
            </li>
          </ul>
        </div>

        <!-- Alteradas -->
        <div
            v-if="impacto.atividadesAlteradas.length > 0"
            class="mb-4"
        >
          <h5 class="text-primary mb-3">
            <i class="bi bi-pencil me-2"/>Atividades Alteradas
          </h5>
          <ul class="list-group" data-testid="lista-atividades-alteradas">
            <li
                v-for="ativ in impacto.atividadesAlteradas"
                :key="ativ.codigo"
                class="list-group-item">
              <div class="d-flex flex-column">
                <strong>{{ ativ.descricao }}</strong>
                <small
                    v-if="ativ.descricaoAnterior"
                    class="text-muted"
                >Anterior: {{ ativ.descricaoAnterior }}</small>
              </div>
            </li>
          </ul>
        </div>

        <!-- Competencias -->
        <div
            v-if="impacto.competenciasImpactadas.length > 0"
            class="mb-4"
            data-testid="lista-competencias-impactadas"
        >
          <h5 class="text-warning mb-3">
            <i class="bi bi-exclamation-triangle me-2"/>Competências Impactadas
          </h5>
          <BCard
              v-for="comp in impacto.competenciasImpactadas"
              :key="comp.codigo"
              class="mb-3"
              no-body
          >
            <template #header>
              <strong>{{ comp.descricao }}</strong>
            </template>
            <ul class="list-group list-group-flush">
              <li
                  v-for="(ativ, idx) in comp.atividadesAfetadas"
                  :key="idx"
                  class="list-group-item text-muted small"
              >
                <i class="bi bi-dot me-1"/> Impactada por: {{ ativ }}
              </li>
              <li
                  v-if="comp.tiposImpacto && comp.tiposImpacto.length > 0"
                  class="list-group-item text-muted small fst-italic"
              >
                Tipos de Impacto: {{ comp.tiposImpacto.map(formatTipoImpacto).join(', ') }}
              </li>
            </ul>
          </BCard>
        </div>
      </div>
    </div>

    <BAlert
        v-else
        :fade="false"
        :model-value="true"
        variant="danger"
    >
      Não foi possível carregar os dados de impacto.
    </BAlert>

    <template #footer>
      <BButton
          data-testid="btn-fechar-impacto"
          type="button"
          variant="secondary"
          @click="fechar"
      >
        Fechar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCard, BModal} from "bootstrap-vue-next";
import {type ImpactoMapa, TipoImpactoCompetencia} from "@/types/tipos";

interface Props {
  mostrar: boolean;
  impacto?: ImpactoMapa | null;
  loading?: boolean;
}

withDefaults(defineProps<Props>(), {
  impacto: null,
  loading: false,
});

const emit = defineEmits<(e: "fechar") => void>();

function formatTipoImpacto(tipo: TipoImpactoCompetencia): string {
  switch (tipo) {
    case TipoImpactoCompetencia.ATIVIDADE_REMOVIDA:
      return "Atividade Removida";
    case TipoImpactoCompetencia.ATIVIDADE_ALTERADA:
      return "Atividade Alterada";
    default:
      return tipo;
  }
}

function fechar() {
  emit("fechar");
}
</script>
