<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
      size="lg"
      :title="TEXTOS.mapa.impacto.TITULO_MODAL"
      @hide="fechar">
    <div v-if="loading" class="text-center p-4">
      <BSpinner label="Carregando..." variant="primary" />
      <p class="mt-2">
        {{ TEXTOS.mapa.impacto.VERIFICANDO }}
      </p>
    </div>

    <div v-else-if="impacto" data-testid="modal-impacto-body">
      <BAlert v-if="!impacto.temImpactos" :fade="false" :model-value="true" variant="success">
        <i aria-hidden="true" class="bi bi-check-circle me-2"/> {{ TEXTOS.mapa.impacto.SEM_IMPACTOS }}
      </BAlert>

      <div v-else class="mt-3">
        <!-- Inseridas -->
        <div v-if="impacto.atividadesInseridas && impacto.atividadesInseridas.length > 0" class="mb-4">
          <h5 class="text-success mb-3">
            <i aria-hidden="true" class="bi bi-plus-circle me-2"/>{{ TEXTOS.mapa.impacto.ATIVIDADES_INSERIDAS }}
          </h5>
          <BListGroup data-testid="lista-atividades-inseridas">
            <BListGroupItem
                v-for="ativ in impacto.atividadesInseridas"
                :key="ativ.codigo"
            >
              <strong>{{ ativ.descricao }}</strong>
              <div
                  v-if="ativ.conhecimentos && ativ.conhecimentos.length > 0"
                  class="mt-1"
              >
                <ul class="list-unstyled ms-3 small text-muted">
                  <li v-for="(conhecimento, idx) in ativ.conhecimentos" :key="idx">
                    <i aria-hidden="true" class="bi bi-dot"/> {{ conhecimento }}
                  </li>
                </ul>
              </div>
              <div
                  v-if="ativ.competenciasVinculadas && ativ.competenciasVinculadas.length > 0"
                  class="mt-1 border-top pt-1"
              >
                <small class="text-muted">Vinculada a: {{ ativ.competenciasVinculadas.join(', ') }}</small>
              </div>
            </BListGroupItem>
          </BListGroup>
        </div>

        <!-- Removidas -->
        <div v-if="impacto.atividadesRemovidas && impacto.atividadesRemovidas.length > 0" class="mb-4">
          <h5 class="text-danger mb-3">
            <i aria-hidden="true" class="bi bi-dash-circle me-2"/>{{ TEXTOS.mapa.impacto.ATIVIDADES_REMOVIDAS }}
          </h5>
          <BListGroup data-testid="lista-atividades-removidas">
            <BListGroupItem v-for="ativ in impacto.atividadesRemovidas" :key="ativ.codigo">
              <strong class="text-decoration-line-through text-muted">{{ ativ.descricao }}</strong>
            </BListGroupItem>
          </BListGroup>
        </div>

        <!-- Alteradas -->
        <div
            v-if="impacto.atividadesAlteradas && impacto.atividadesAlteradas.length > 0"
            class="mb-4"
        >
          <h5 class="text-primary mb-3">
            <i aria-hidden="true" class="bi bi-pencil me-2"/>{{ TEXTOS.mapa.impacto.ATIVIDADES_ALTERADAS }}
          </h5>
          <BListGroup data-testid="lista-atividades-alteradas">
            <BListGroupItem
                v-for="ativ in impacto.atividadesAlteradas"
                :key="ativ.codigo">
              <div class="d-flex flex-column">
                <strong>{{ ativ.descricao }}</strong>
                <small
                    v-if="ativ.descricaoAnterior"
                    class="text-muted"
                >Anterior: {{ ativ.descricaoAnterior }}</small>
              </div>
            </BListGroupItem>
          </BListGroup>
        </div>

        <!-- Competencias -->
        <div
            v-if="impacto.competenciasImpactadas && impacto.competenciasImpactadas.length > 0"
            class="mb-4"
            data-testid="lista-competencias-impactadas"
        >
          <h5 class="text-warning mb-3">
            <i aria-hidden="true" class="bi bi-exclamation-triangle me-2"/>{{ TEXTOS.mapa.impacto.COMPETENCIAS_IMPACTADAS }}
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
            <BListGroup flush>
              <BListGroupItem
                  v-for="(ativ, idx) in comp.atividadesAfetadas"
                  :key="idx"
                  class="text-muted small"
              >
                <i aria-hidden="true" class="bi bi-dot me-1"/> {{ TEXTOS.mapa.impacto.IMPACTADA_POR }} {{ ativ }}
              </BListGroupItem>
              <BListGroupItem
                  v-if="comp.tiposImpacto && comp.tiposImpacto.length > 0"
                  class="text-muted small fst-italic"
              >
                {{ TEXTOS.mapa.impacto.TIPOS_IMPACTO }} {{ comp.tiposImpacto.map(formatTipoImpacto).join(', ') }}
              </BListGroupItem>
            </BListGroup>
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
      {{ TEXTOS.mapa.impacto.ERRO_CARREGAR }}
    </BAlert>

    <template #footer>
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            class="text-decoration-none text-secondary fw-medium btn-cancelar-link"
            data-testid="btn-fechar-impacto"
            type="button"
            variant="link"
            @click="fechar"
        >
          {{ TEXTOS.comum.BOTAO_FECHAR }}
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCard, BListGroup, BListGroupItem, BModal, BSpinner} from "bootstrap-vue-next";
import {type ImpactoMapa, TipoImpactoCompetencia} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

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
      return TEXTOS.mapa.impacto.TIPO_REMOVIDA;
    case TipoImpactoCompetencia.ATIVIDADE_ALTERADA:
      return TEXTOS.mapa.impacto.TIPO_ALTERADA;
    default:
      return tipo;
  }
}

function fechar() {
  emit("fechar");
}
</script>

<style scoped>
.btn-cancelar-link {
  padding: 0.375rem 0.75rem;
  transition: all 0.2s;
  border-radius: 0.375rem;
}

.btn-cancelar-link:hover {
  color: var(--bs-emphasis-color) !important;
  background-color: var(--bs-secondary-bg);
}
</style>
