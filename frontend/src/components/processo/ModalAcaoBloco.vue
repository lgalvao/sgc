<template>
  <BModal
      :id="id"
      v-model="mostrar"
      :title="titulo"
      size="lg"
      @hide="fechar"
  >
    <p class="mb-3">{{ texto }}</p>

    <BAlert v-if="erro" :model-value="true" variant="danger" class="mb-3">
      {{ erro }}
    </BAlert>

    <div v-if="mostrarDataLimite" class="mb-3">
      <BFormGroup label="Data Limite" label-for="dataLimiteBloco" label-class="required">
        <InputData
            id="dataLimiteBloco"
            v-model="dataLimite"
            data-testid="inp-data-limite-bloco"
            max="2099-12-31"
            min="2000-01-01"
            required
        />
      </BFormGroup>
    </div>

    <BTable
        :items="unidades"
        :fields="campos"
        small
        hover
        responsive
        sticky-header="300px"
    >
      <template #head(selecao)>
        <BFormCheckbox
            v-model="todosSelecionados"
            :disabled="processando"
        />
      </template>
      <template #cell(selecao)="{ item }">
        <BFormCheckbox
            v-model="selecionadosLocal"
            :disabled="processando"
            :value="item.codigo"
        />
      </template>
    </BTable>

    <template #footer>
      <BButton :disabled="processando" variant="secondary" @click="fechar">Cancelar</BButton>
      <BButton
          :disabled="processando || selecionadosLocal.length === 0"
          variant="primary"
          @click="confirmar"
      >
        <BSpinner v-if="processando" aria-hidden="true" class="me-2" small />
        {{ rotuloBotao }}
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { BModal, BTable, BFormCheckbox, BAlert, BButton, BSpinner, BFormGroup } from 'bootstrap-vue-next';
import InputData from '@/components/comum/InputData.vue';

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

const mostrar = ref(false);
const selecionadosLocal = ref<number[]>([]);
const processando = ref(false);
const erro = ref<string | null>(null);
const dataLimite = ref<string>('');

const campos = [
  { key: 'selecao', label: '', thStyle: { width: '40px' } },
  { key: 'sigla', label: 'Sigla' },
  { key: 'nome', label: 'Nome' },
  { key: 'situacao', label: 'Situação' }
];

const todosSelecionados = computed({
  get() {
    return props.unidades.length > 0 && selecionadosLocal.value.length === props.unidades.length;
  },
  set(val: boolean) {
    if (val) {
      selecionadosLocal.value = props.unidades.map(u => u.codigo);
    } else {
      selecionadosLocal.value = [];
    }
  }
});

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
  mostrar.value = true;
}

function fechar() {
  mostrar.value = false;
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
}, { immediate: true });
</script>

<style scoped>
:deep(.required:after) {
  content: " *";
  color: red;
}
</style>
