<template>
  <div :id="id" ref="modalElement" aria-hidden="true" class="modal fade" tabindex="-1">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ titulo }}</h5>
          <button aria-label="Fechar" class="btn-close" data-bs-dismiss="modal" type="button"></button>
        </div>
        <div class="modal-body">
          <p class="mb-3">{{ texto }}</p>

          <div v-if="erro" class="alert alert-danger mb-3">
            {{ erro }}
          </div>

          <div v-if="mostrarDataLimite" class="mb-3">
            <label class="form-label required" for="dataLimiteBloco">Data Limite</label>
            <input id="dataLimiteBloco" v-model="dataLimite" class="form-control" required type="date">
          </div>

          <div class="table-responsive" style="max-height: 300px; overflow-y: auto;">
            <table class="table table-sm table-hover">
              <thead>
              <tr>
                <th style="width: 40px">
                  <input
                      :checked="todosSelecionados"
                      :disabled="processando"
                      class="form-check-input"
                      type="checkbox"
                      @change="toggleTodos"
                  >
                </th>
                <th>Sigla</th>
                <th>Nome</th>
                <th>Situação</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="unidade in unidades" :key="unidade.codigo">
                <td>
                  <input
                      v-model="selecionadosLocal"
                      :disabled="processando"
                      :value="unidade.codigo"
                      class="form-check-input"
                      type="checkbox"
                  >
                </td>
                <td>{{ unidade.sigla }}</td>
                <td>{{ unidade.nome }}</td>
                <td>{{ unidade.situacao }}</td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer">
          <button :disabled="processando" class="btn btn-secondary" data-bs-dismiss="modal" type="button">Cancelar
          </button>
          <button
              :disabled="processando || selecionadosLocal.length === 0"
              class="btn btn-primary"
              type="button"
              @click="confirmar"
          >
            <output v-if="processando" aria-hidden="true" class="spinner-border spinner-border-sm me-2"></output>
            {{ rotuloBotao }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue';
import {Modal} from 'bootstrap';

export interface UnidadeSelecao {
  codigo: number;
  sigla: string;
  nome: string;
  situacao: string;
  selecionada?: boolean; // For compatibility if passed from parent with this prop
}

const props = defineProps<{
  id: string;
  titulo: string;
  texto: string;
  rotuloBotao: string;
  unidades: UnidadeSelecao[];
  unidadesPreSelecionadas: number[];
  mostrarDataLimite?: boolean;
}>();


const emit = defineEmits<{
  'confirmar': [dados: { ids: number[], dataLimite?: string }];
}>();

const modalElement = ref<HTMLElement | null>(null);
const modalInstance = ref<Modal | null>(null);
const selecionadosLocal = ref<number[]>([]);
const processando = ref(false);
const erro = ref<string | null>(null);
const dataLimite = ref<string>('');

const todosSelecionados = computed(() => {
  return props.unidades.length > 0 && selecionadosLocal.value.length === props.unidades.length;
});

function toggleTodos(e: Event) {
  const checked = (e.target as HTMLInputElement).checked;
  if (checked) {
    selecionadosLocal.value = props.unidades.map(u => u.codigo);
  } else {
    selecionadosLocal.value = [];
  }
}

function confirmar() {
  if (props.mostrarDataLimite && !dataLimite.value) {
    erro.value = "A data limite é obrigatória.";
    return;
  }

  processando.value = true;
  erro.value = null;

  emit('confirmar', {
    ids: selecionadosLocal.value,
    dataLimite: props.mostrarDataLimite ? dataLimite.value : undefined
  });
}

function abrir() {
  modalInstance.value?.show();
}

function fechar() {
  modalInstance.value?.hide();
  processando.value = false;
  erro.value = null;
  dataLimite.value = '';
}

function setProcessando(val: boolean) {
  processando.value = val;
}

function setErro(msg: string | null) {
  erro.value = msg;
}

// Expor métodos para controle do pai e satisfazer o linter (uso no template)
defineExpose({
  abrir,
  fechar,
  setProcessando,
  setErro,
  todosSelecionados,
  confirmar
});

watch(() => props.unidadesPreSelecionadas, (newVal) => {
  selecionadosLocal.value = [...newVal];
}, {immediate: true});

onMounted(() => {
  if (modalElement.value) {
    modalInstance.value = new Modal(modalElement.value);
  }
});
</script>

<style scoped>
.required:after {
  content: " *";
  color: red;
}
</style>
