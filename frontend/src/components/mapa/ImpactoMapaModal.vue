<template>
  <BModal
      v-model="mostrarComputado"
      :title="TEXTOS.mapa.impacto.TITULO_MODAL"
      centered
      size="lg"
      @hide="fechar">
    <div v-if="loading" class="text-center p-4">
      <BSpinner label="Carregando..." variant="primary"/>
      <p class="mt-2 text-muted">
        {{ TEXTOS.mapa.impacto.VERIFICANDO }}
      </p>
    </div>

    <div v-else-if="impacto" class="impacto-container" data-testid="modal-impacto-body">
      <BAlert v-if="!impacto.temImpactos" :model-value="true" variant="success">
        <i aria-hidden="true" class="bi bi-check-circle me-2"/>
        {{ TEXTOS.mapa.impacto.SEM_IMPACTOS }}
      </BAlert>

      <div v-else>
        <!-- 7.1 Atividades Inseridas -->
        <section v-if="impacto.atividadesInseridas?.length" class="mb-4">
          <h5 class="text-success border-bottom pb-2 mb-3">
            <i class="bi bi-plus-circle me-2"></i>{{ TEXTOS.mapa.impacto.ATIVIDADES_INSERIDAS }}
          </h5>
          <BListGroup>
            <BListGroupItem v-for="ativ in impacto.atividadesInseridas" :key="ativ.codigo">
              <div class="fw-bold">{{ ativ.descricao }}</div>
              <div v-if="ativ.conhecimentos?.length" class="mt-1 small text-muted">
                <i class="bi bi-dot"></i> {{ ativ.conhecimentos.join(', ') }}
              </div>
            </BListGroupItem>
          </BListGroup>
        </section>

        <!-- 7.2 Competências Impactadas -->
        <section v-if="impacto.competenciasImpactadas?.length" class="mb-4">
          <h5 class="text-warning-emphasis border-bottom pb-2 mb-3">
            <i class="bi bi-exclamation-triangle me-2"></i>{{ TEXTOS.mapa.impacto.COMPETENCIAS_IMPACTADAS }}
          </h5>
          <div v-for="comp in impacto.competenciasImpactadas" :key="comp.codigo" class="mb-3">
            <BCard no-body>
              <template #header>
                <div class="fw-bold">{{ comp.descricao }}</div>
              </template>
              <BCardBody class="p-3">
                <div class="impact-details">
                  <div
                      v-for="(msg, idx) in comp.atividadesAfetadas"
                      :key="idx"
                      :class="{
                           'mb-1': true,
                           'ms-4 fst-italic text-muted': msg.startsWith('  ') || msg.startsWith('Descrição') || msg.startsWith('Conhecimento'),
                           'mt-3': idx > 0 && !msg.startsWith('  ') && !msg.startsWith('Descrição') && !msg.startsWith('Conhecimento')
                         }"
                      class="d-flex align-items-start">

                    <!-- Ícone apenas para a linha principal da atividade -->
                    <template
                        v-if="!msg.startsWith('  ') && !msg.startsWith('Descrição') && !msg.startsWith('Conhecimento')">
                      <i v-if="msg.includes('removida')" class="bi bi-dash-circle text-danger me-2"></i>
                      <i v-else-if="msg.includes('alterada')" class="bi bi-pencil text-primary me-2"></i>
                      <i v-else class="bi bi-dot text-secondary me-1"></i>
                    </template>

                    <span class="small">{{ msg }}</span>
                  </div>
                </div>
              </BCardBody>
            </BCard>
          </div>
        </section>
      </div>
    </div>

    <BAlert v-else-if="mostrar" :model-value="true" variant="danger">
      {{ TEXTOS.mapa.impacto.ERRO_CARREGAR }}
    </BAlert>

    <template #footer>
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            data-testid="btn-fechar-impacto"
            type="button"
            variant="link"
            class="text-decoration-none text-secondary fw-medium btn-fechar-link"
            @click="fechar"
        >
          {{ TEXTOS.comum.BOTAO_FECHAR }}
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCard, BCardBody, BListGroup, BListGroupItem, BModal, BSpinner} from "bootstrap-vue-next";
import {type ImpactoMapa} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {computed} from "vue";

interface Props {
  mostrar: boolean;
  impacto?: ImpactoMapa | null;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  impacto: null,
  loading: false,
});

const emit = defineEmits<(e: "fechar") => void>();

const mostrarComputado = computed({
  get: () => props.mostrar,
  set: (val) => {
    if (!val) emit("fechar");
  }
});

function fechar() {
  emit("fechar");
}
</script>

<style scoped>
.btn-fechar-link {
  padding: 0.375rem 0.75rem;
  transition: all 0.2s;
  border-radius: 0.375rem;
}

.btn-fechar-link:hover {
  color: var(--bs-emphasis-color) !important;
  background-color: var(--bs-secondary-bg);
}

.impacto-container {
  max-height: 60vh;
  overflow-y: auto;
}
</style>
