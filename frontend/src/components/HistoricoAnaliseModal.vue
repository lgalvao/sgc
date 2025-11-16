<template>
  <b-modal
    v-model="show"
    title="Histórico de Análises"
    size="lg"
    centered
    @hidden="fechar"
  >
    <div
      class="modal-body"
      data-testid="modal-historico-body"
    >
      <div
        v-if="analises.length === 0"
        class="alert alert-info"
      >
        Nenhuma análise registrada para este subprocesso.
      </div>
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
      <b-button
        variant="secondary"
        data-testid="btn-modal-fechar"
        @click="fechar"
      >
        Fechar
      </b-button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import {ref, watch, computed} from 'vue';
import {useAnalisesStore} from '@/stores/analises';
import type {AnaliseCadastro, AnaliseValidacao} from '@/types/tipos';
import {format} from 'date-fns';
import {ptBR} from 'date-fns/locale';

type Analise = AnaliseCadastro | AnaliseValidacao;

const props = defineProps<{
  mostrar: boolean;
  codSubrocesso: number | undefined;
}>();

const emit = defineEmits(['update:mostrar']);

const analisesStore = useAnalisesStore();
const analises = ref<Analise[]>([]);

const show = computed({
  get: () => props.mostrar,
  set: (value) => emit('update:mostrar', value)
})

watch(
  () => props.mostrar,
  newVal => {
    if (newVal && props.codSubrocesso) {
      analises.value = analisesStore.getAnalisesPorSubprocesso(props.codSubrocesso);
    }
  },
  { immediate: true },
);

function fechar() {
  emit('update:mostrar', false);
}

function formatarData(data: string): string {
  return format(new Date(data), 'dd/MM/yyyy HH:mm:ss', { locale: ptBR });
}
</script>

<style scoped>
/* Estilos específicos para o modal, se necessário */
</style>