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
      <BFormGroup
          label="Data limite"
          label-for="dataLimiteBloco"
          label-class="required"
          :state="erroLocalDataLimite ? false : null"
          :invalid-feedback="erroLocalDataLimite"
      >
        <InputData
            id="dataLimiteBloco"
            v-model="dataLimite"
            data-testid="inp-data-limite-bloco"
            max="2099-12-31"
            :min="dataMinimaPermitida"
            :state="erroLocalDataLimite ? false : null"
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
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            :disabled="processando"
            class="text-decoration-none text-secondary fw-medium btn-cancelar-link"
            variant="link"
            @click="fechar"
        >
          Cancelar
        </BButton>
        <BButton
            :disabled="processando || selecionadosLocal.length === 0 || (mostrarDataLimite && (!!erroLocalDataLimite || !dataLimite))"
            variant="success"
            @click="confirmar"
        >
          <BSpinner v-if="processando" aria-hidden="true" class="me-2" small />
          {{ rotuloBotao }}
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {BAlert, BButton, BFormCheckbox, BFormGroup, BModal, BSpinner, BTable} from 'bootstrap-vue-next';
import InputData from '@/components/comum/InputData.vue';
import {isDateStrictlyFuture, obterAmanhaFormatado} from "@/utils/dateUtils";

export interface UnidadeSelecao {
  codigo: number;
  sigla: string;
  nome: string;
  situacao: string;
  ultimaDataLimite?: string;
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
const erroLocalDataLimite = ref('');

const ultimaDataLimiteSelecionada = computed(() => {
  return props.unidades
      .filter(unidade => selecionadosLocal.value.includes(unidade.codigo))
      .map(unidade => extrairData(unidade.ultimaDataLimite))
      .filter(Boolean)
      .reduce<string>((maior, atual) => !maior || atual > maior ? atual : maior, '');
});

const dataMinimaPermitida = computed(() => {
  const amanha = obterAmanhaFormatado();
  const ultimaDataLimite = ultimaDataLimiteSelecionada.value;
  if (!ultimaDataLimite) return amanha;
  return ultimaDataLimite > amanha ? ultimaDataLimite : amanha;
});

watch([dataLimite, ultimaDataLimiteSelecionada], ([novaData, ultimaDataLimite]) => {
  erroLocalDataLimite.value = "";
  if (novaData?.length !== 10 || !props.mostrarDataLimite) {
    return;
  }
  if (!isDateStrictlyFuture(novaData)) {
    erroLocalDataLimite.value = "A data limite para validação deve ser uma data futura.";
    return;
  }
  if (ultimaDataLimite && novaData < ultimaDataLimite) {
    erroLocalDataLimite.value = "A data limite deve ser maior ou igual à última data limite do subprocesso.";
  }
});

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
  if (props.mostrarDataLimite) {
    if (!dataLimite.value) {
      erro.value = "A data limite é obrigatória.";
      return;
    }
    if (erroLocalDataLimite.value) {
      return;
    }
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
  erroLocalDataLimite.value = '';
}

function setProcessando(val: boolean) {
  processando.value = val;
}

function setErro(msg: string | null) {
  erro.value = msg;
}

function extrairData(data?: string) {
  return data?.split("T")[0] || "";
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

.btn-cancelar-link {
  padding: 0.375rem 0.75rem;
  transition: all 0.2s;
  border-radius: 0.375rem;
}

.btn-cancelar-link:hover {
  color: var(--bs-emphasis-color) !important;
  background-color: var(--bs-secondary-bg);
}
</style>
