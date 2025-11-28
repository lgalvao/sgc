<template>
  <BModal
    :model-value="mostrar"
    title="Histórico de Análises"
    size="lg"
    centered
    hide-footer
    @hide="emit('fechar')"
  >
    <div data-testid="modal-historico-body">
      <BAlert
        v-if="analises.length === 0"
        variant="info"
        :model-value="true"
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
            >
              <td>{{ formatarData(analise.dataHora) }}</td>
              <td>{{ (analise as AnaliseValidacao).unidade || (analise as AnaliseCadastro).unidadeSigla }}</td>
              <td>{{ analise.resultado }}</td>
              <td>{{ analise.observacoes || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <template #footer>
      <BButton
        variant="secondary"
        data-testid="btn-modal-fechar"
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
    (newVal) => {
    if (newVal && props.codSubrocesso) {
      analises.value = analisesStore.obterAnalisesPorSubprocesso(
          props.codSubrocesso,
      );
    }
  },
  { immediate: true },
);

function formatarData(data: string): string {
  return format(new Date(data), "dd/MM/yyyy HH:mm:ss", {locale: ptBR});
}
</script>

<style scoped>
/* Estilos específicos para o modal, se necessário */
</style>
