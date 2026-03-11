<template>
  <div :id="id" ref="modalElement" aria-hidden="true" class="modal fade" tabindex="-1">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ titulo }}</h5>
          <button aria-label="Close" class="btn-close" data-bs-dismiss="modal" type="button"></button>
        </div>
        <div class="modal-body">
          <p v-if="texto">{{ texto }}</p>

          <div v-if="mostrarDataLimite" class="mb-3">
            <label class="form-label required" for="dataLimiteBloco">Data Limite</label>
            <InputData
                id="dataLimiteBloco"
                v-model="dataLimite"
                data-testid="inp-data-limite-bloco"
                max="2099-12-31"
                min="2000-01-01"
                required
            />
          </div>

          <div v-if="erroLocal" class="alert alert-danger mb-3">
            {{ erroLocal }}
          </div>

          <div class="table-responsive">
            <table class="table table-sm table-hover border">
              <thead class="table-light">
              <tr>
                <th style="width: 40px">
                  <input
                      :checked="todasSelecionadas"
                      class="form-check-input"
                      type="checkbox"
                      @change="alternarTodas"
                  >
                </th>
                <th>Unidade</th>
                <th>Situação</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="u in unidades" :key="u.codigo">
                <td>
                  <input
                      v-model="selecionadas"
                      :value="u.codigo"
                      class="form-check-input"
                      type="checkbox"
                  >
                </td>
                <td>{{ u.sigla }} - {{ u.nome }}</td>
                <td>{{ u.situacao }}</td>
              </tr>
              <tr v-if="unidades.length === 0">
                <td class="text-center py-3 text-muted" colspan="3">
                  Nenhuma unidade elegível encontrada.
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
          <div class="modal-footer">
          <button :disabled="processando" class="btn btn-secondary" data-bs-dismiss="modal" type="button">
            Cancelar
          </button>
          <button
              :disabled="processando || selecionadas.length === 0"
              class="btn btn-primary"
              type="button"
              @click="confirmar"
          >
            <span v-if="processando" class="spinner-border spinner-border-sm me-1" role="status"></span>
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
import InputData from '@/components/comum/InputData.vue';

interface UnidadeBloco {
  codigo: number;
  sigla: string;
  nome: string;
  situacao: string;
}

const props = defineProps<{
  id: string;
  titulo: string;
  texto?: string;
  rotuloBotao: string;
  unidades: UnidadeBloco[];
  unidadesPreSelecionadas?: number[];
  mostrarDataLimite?: boolean;
}>();

const emit = defineEmits<{
  (e: 'confirmar', data: { ids: number[]; dataLimite?: string }): void;
}>();

const modalElement = ref<HTMLElement | null>(null);
const modalInstance = ref<Modal | null>(null);
const selecionadas = ref<number[]>([]);
const dataLimite = ref('');
const processando = ref(false);
const erroLocal = ref('');

const todasSelecionadas = computed(() => {
  return props.unidades.length > 0 && selecionadas.value.length === props.unidades.length;
});

function alternarTodas() {
  if (todasSelecionadas.value) {
    selecionadas.value = [];
  } else {
    selecionadas.value = props.unidades.map(u => u.codigo);
  }
}

function abrir() {
  erroLocal.value = '';
  processando.value = false;
  modalInstance.value?.show();
}

function fechar() {
  modalInstance.value?.hide();
}

function setProcessando(valor: boolean) {
  processando.value = valor;
}

function setErro(msg: string) {
  erroLocal.value = msg;
}

function confirmar() {
  if (props.mostrarDataLimite && !dataLimite.value) {
    erroLocal.value = "A data limite é obrigatória";
    return;
  }
  emit('confirmar', {
    ids: selecionadas.value,
    dataLimite: props.mostrarDataLimite ? dataLimite.value : undefined
  });
}

watch(() => props.unidadesPreSelecionadas, (newVal) => {
  selecionadas.value = [...(newVal || [])];
}, {immediate: true});

onMounted(() => {
  if (modalElement.value) {
    modalInstance.value = new Modal(modalElement.value);
  }
});

defineExpose({
  abrir,
  fechar,
  setProcessando,
  setErro
});
</script>

<style scoped>
.required:after {
  content: " *";
  color: red;
}
</style>
