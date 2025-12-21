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
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Carregando...</span>
        </div>
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
            <template #cell(dataHora)="{ item }">
              {{ formatarData((item as Analise).dataHora) }}
            </template>
            <template #cell(unidade)="{ item }">
              {{ (item as AnaliseValidacao).unidade || (item as AnaliseCadastro).unidadeSigla }}
            </template>
            <template #cell(resultado)="{ item }">
              {{ (item as Analise).acao || (item as Analise).resultado }}
            </template>
            <template #cell(observacoes)="{ item }">
              {{ (item as Analise).observacoes || '-' }}
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
import {format} from "date-fns";
import {ptBR} from "date-fns/locale";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

const props = defineProps<{
  mostrar: boolean;
  historico: Analise[];
  loading?: boolean;
}>();

const emit = defineEmits(["fechar"]);

const fields = [
  {key: "dataHora", label: "Data/Hora"},
  {key: "unidade", label: "Unidade"},
  {key: "resultado", label: "Resultado"},
  {key: "observacoes", label: "Observação"},
];

/**
 * Fecha o modal
 */
function fechar() {
  emit("fechar");
}

function formatarData(data: string): string {
  return format(new Date(data), "dd/MM/yyyy HH:mm:ss", {locale: ptBR});
}
</script>

<style scoped>
/* Estilos específicos para o modal, se necessário */
</style>
