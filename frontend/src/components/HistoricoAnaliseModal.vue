<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
      hide-footer
      size="lg"
      title="Histórico de Análises"
      @hide="emit('fechar')"
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
        <table class="table table-striped table-hover">
          <thead>
          <tr>
            <th>Data/Hora</th>
            <th>Unidade</th>
            <th>Resultado</th>
            <th>Observação</th>
          </tr>
          </thead>
          <tbody>
          <tr
              v-for="(analise, index) in analises"
              :key="index"
              :data-testid="`row-historico-${index}`"
          >
            <td :data-testid="`cell-data-${index}`">{{ formatarData(analise.dataHora) }}</td>
            <td :data-testid="`cell-unidade-${index}`">
              {{ (analise as AnaliseValidacao).unidade || (analise as AnaliseCadastro).unidadeSigla }}
            </td>
            <td :data-testid="`cell-resultado-${index}`">{{ analise.acao || analise.resultado }}</td>
            <td :data-testid="`cell-observacao-${index}`">{{ analise.observacoes || '-' }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
    <template #footer>
      <BButton
          data-testid="btn-modal-fechar"
          variant="secondary"
          @click="emit('fechar')"
      >
        Fechar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BModal} from "bootstrap-vue-next";
import {format} from "date-fns";
import {ptBR} from "date-fns/locale";
import {ref, watch} from "vue";
import {useAnalisesStore} from "@/stores/analises";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

const props = defineProps<{
  mostrar: boolean;
  codSubrocesso: number | undefined;
}>();

const emit = defineEmits(["fechar"]);

const analisesStore = useAnalisesStore();
const analises = ref<Analise[]>([]);

watch(
    () => props.mostrar,
    async (newVal) => {
      if (newVal && props.codSubrocesso) {
        await analisesStore.buscarAnalisesCadastro(props.codSubrocesso);
        analises.value = analisesStore.obterAnalisesPorSubprocesso(
            props.codSubrocesso,
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
