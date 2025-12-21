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
      <BAlert
          v-if="analises.length === 0"
          :fade="false"
          :model-value="true"
          variant="info"
      >
        Nenhuma análise registrada para este subprocesso.
      </BAlert>
      <div v-else>
        <BTable
            :fields="fields"
            :items="analises"
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
import {ref, watch} from "vue";
import {useAnalisesStore} from "@/stores/analises";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

const props = defineProps<{
  mostrar: boolean;
  codSubprocesso: number | undefined;
}>();

const emit = defineEmits(["fechar"]);

const analisesStore = useAnalisesStore();
const analises = ref<Analise[]>([]);

const fields = [
  {key: "dataHora", label: "Data/Hora"},
  {key: "unidade", label: "Unidade"},
  {key: "resultado", label: "Resultado"},
  {key: "observacoes", label: "Observação"},
];

/**
 * Fecha o modal e limpa os dados para evitar flicker ao reabrir
 */
function fechar() {
  analises.value = [];
  analisesStore.clearError();
  emit("fechar");
}

/**
 * Watch que busca análises quando o modal é aberto
 * Previne race conditions verificando se já está carregando
 */
watch(
    () => props.mostrar,
    async (newVal) => {
      if (newVal && props.codSubprocesso && !analisesStore.isLoading) {
        await analisesStore.buscarAnalisesCadastro(props.codSubprocesso);
        analises.value = analisesStore.obterAnalisesPorSubprocesso(
            props.codSubprocesso,
        );
      }
    },
    {immediate: true},
);

function formatarData(data: string): string {
  return format(new Date(data), "dd/MM/yyyy HH:mm:ss", {locale: ptBR});
}
</script>

<style scoped>
/* Estilos específicos para o modal, se necessário */
</style>
