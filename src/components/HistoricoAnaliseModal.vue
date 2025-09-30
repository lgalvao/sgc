<template>
  <div
    v-if="mostrar"
    class="modal fade show"
    style="display: block;"
    tabindex="-1"
  >
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            Histórico de Análises
          </h5>
          <button
            type="button"
            class="btn-close"
            @click="emit('fechar')"
          />
        </div>
        <div class="modal-body" data-testid="modal-historico-body">
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
                  <td>{{ analise.unidade }}</td>
                  <td>{{ analise.resultado }}</td>
                  <td>{{ analise.observacao || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer">
          <button
            type="button"
            class="btn btn-secondary"
            data-testid="btn-modal-fechar"
            @click="emit('fechar')"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  </div>
  <div
    v-if="mostrar"
    class="modal-backdrop fade show"
  />
</template>

<script lang="ts" setup>
import {ref, watch} from 'vue';

import {useAnalisesStore} from '@/stores/analises';
import {AnaliseValidacao} from '@/types/tipos';
import {format} from 'date-fns';
import {ptBR} from 'date-fns/locale';

const props = defineProps<{
  mostrar: boolean;
  idSubprocesso: number | undefined;
}>();

const emit = defineEmits(['fechar']);


const analisesStore = useAnalisesStore();
const analises = ref<AnaliseValidacao[]>([]);

watch(() => props.mostrar, (newVal) => {
  if (newVal && props.idSubprocesso) {
    // TODO: Implementar método na store para buscar análises
    analises.value = analisesStore.getAnalisesPorSubprocesso(props.idSubprocesso);
  }
}, { immediate: true });

function formatarData(data: Date): string {
  return format(new Date(data), 'dd/MM/yyyy HH:mm:ss', { locale: ptBR });
}
</script>

<style scoped>
/* Estilos específicos para o modal, se necessário */
</style>