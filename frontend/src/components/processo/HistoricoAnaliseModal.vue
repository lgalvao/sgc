<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
      hide-footer
      size="lg"
      title="Histórico de Análises"
      @hide="fechar"
  >
    <div data-testid="modal-historico-body">
      <div v-if="loading" class="text-center py-4">
        <output aria-label="Carregando dados" class="spinner-border text-primary">
          <span class="visually-hidden">Carregando...</span>
        </output>
      </div>
      <template v-else>
        <BAlert
            v-if="historico.length === 0"
            :fade="false"
            :model-value="true"
            variant="info"
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
      <BButton
          data-testid="btn-modal-fechar"
          variant="secondary"
          @click="fechar"
      >
        Fechar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BModal, BTable} from "bootstrap-vue-next";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";
import {formatDateTimeBR} from "@/utils/dateUtils";

type Analise = AnaliseCadastro | AnaliseValidacao;

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
/* Estilos específicos para o modal, se necessário */
</style>
