<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
      size="lg"
      title="Histórico de análise"
      @hide="fechar"
  >
    <div data-testid="modal-historico-body">
      <div v-if="loading" class="text-center py-4">
        <BSpinner label="Carregando dados" variant="primary" />
      </div>
      <template v-else>
        <BAlert
            v-if="historico.length === 0"
            :fade="false"
            :model-value="true"
            data-testid="alert-historico-vazio"
            variant="secondary"
        >
          Nenhuma análise registrada para este subprocesso.
        </BAlert>
        <div v-else>
          <BTable
              :fields="fields"
              :items="historico"
              hover
              responsive
              striped
          >
            <template #cell(dataHora)="{ item, index }">
              <span :data-testid="`cell-dataHora-${index}`">
                {{ formatDateTimeBR((item as Analise).dataHora) }}
              </span>
            </template>
            <template #cell(unidadeSigla)="{ item, index }">
              <span :data-testid="`cell-unidade-${index}`" :title="(item as Analise).unidadeNome">
                {{ (item as Analise).unidadeSigla }}
              </span>
            </template>
            <template #cell(acao)="{ item, index }">
              <span :data-testid="`cell-resultado-${index}`">
                {{ (item as Analise).acao }}
              </span>
            </template>
            <template #cell(observacoes)="{ item, index }">
              <span :data-testid="`cell-observacao-${index}`">
                {{ (item as Analise).observacoes || '-' }}
              </span>
            </template>
          </BTable>
        </div>
      </template>
    </div>
    <template #footer>
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            class="text-decoration-none text-secondary fw-medium btn-cancelar-link"
            data-testid="btn-modal-fechar"
            variant="link"
            @click="fechar"
        >
          Fechar
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BModal, BTable, BSpinner} from "bootstrap-vue-next";
import type {Analise} from "@/types/tipos";
import {formatDateTimeBR} from "@/utils/dateUtils";


const __ = defineProps<{
  mostrar: boolean;
  historico: Analise[];
  loading?: boolean;
}>();

const emit = defineEmits(["fechar"]);

const fields = [
  {key: "dataHora", label: "Data/Hora"},
  {key: "unidadeSigla", label: "Unidade"},
  {key: "acao", label: "Resultado"},
  {key: "analistaUsuarioTitulo", label: "Analista"},
  {key: "observacoes", label: "Observação"},
];

/**
 * Fecha o modal
 */
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
