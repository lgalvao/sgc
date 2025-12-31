<template>
  <div class="modal fade" :id="modalId" tabindex="-1" aria-hidden="true" ref="modalElement">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ titulo }}</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
        </div>
        <div class="modal-body">
          <p class="mb-3">{{ texto }}</p>

          <div v-if="erro" class="alert alert-danger mb-3">
            {{ erro }}
          </div>

          <div class="mb-3" v-if="mostrarDataLimite">
            <label for="dataLimiteBloco" class="form-label required">Data Limite</label>
            <input type="date" class="form-control" id="dataLimiteBloco" v-model="dataLimite" required>
          </div>

          <div class="table-responsive" style="max-height: 300px; overflow-y: auto;">
            <table class="table table-sm table-hover">
              <thead>
                <tr>
                  <th style="width: 40px">
                    <input type="checkbox" class="form-check-input" :checked="todosSelecionados" @change="toggleTodos" :disabled="processando">
                  </th>
                  <th>Sigla</th>
                  <th>Nome</th>
                  <th>Situação</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="unidade in unidades" :key="unidade.codigo">
                  <td>
                    <input type="checkbox" class="form-check-input" :value="unidade.codigo" v-model="selecionadosLocal" :disabled="processando">
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
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" :disabled="processando">Cancelar</button>
          <button type="button" class="btn btn-primary" @click="confirmar" :disabled="processando || selecionadosLocal.length === 0">
            <span v-if="processando" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            {{ rotuloBotao }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { Modal } from 'bootstrap';

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
  (e: 'confirmar', dados: { ids: number[], dataLimite?: string }): void;
}>();

const modalElement = ref<HTMLElement | null>(null);
const modalInstance = ref<Modal | null>(null);
const selecionadosLocal = ref<number[]>([]);
const processando = ref(false);
const erro = ref<string | null>(null);
const dataLimite = ref<string>('');

const modalId = computed(() => props.id);

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

// Expor métodos para controle do pai
defineExpose({
  abrir,
  fechar,
  setProcessando,
  setErro
});

watch(() => props.unidadesPreSelecionadas, (newVal) => {
  selecionadosLocal.value = [...newVal];
}, { immediate: true });

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
