<template>
  <ModalPadrao
      :id="id"
      v-model="mostrar"
      :loading="processando"
      :texto-acao="rotuloBotao"
      :titulo="titulo"
      data-testid="mdl-acao-bloco"
      tamanho="lg"
      test-id-cancelar="btn-acao-bloco-cancelar"
      test-id-confirmar="btn-acao-bloco-confirmar"
      variant-acao="success"
      @confirmar="confirmar"
      @fechar="fechar"
  >
    <p class="mb-3">{{ texto }}</p>

    <BAlert v-if="erro" :model-value="true" class="mb-3" dismissible variant="danger">
      {{ erro }}
    </BAlert>

    <div v-if="mostrarDataLimite" class="mb-3">
      <BFormGroup
          :invalid-feedback="mensagemErroDataLimite"
          :state="mensagemErroDataLimite ? false : null"
          label-for="dataLimiteBloco"
      >
        <template #label>
          Data limite <span aria-hidden="true" class="text-danger">*</span>
        </template>
        <InputData
            id="dataLimiteBloco"
            v-model="dataLimite"
            :min="dataMinimaPermitida"
            :state="mensagemErroDataLimite ? false : null"
            data-testid="inp-data-limite-bloco"
            max="2099-12-31"
        />
        <template #description>
          <div class="mt-1">
            Data limite mínima baseada nas unidades selecionadas: {{ dataMinimaPermitidaFormatada }}
          </div>
        </template>
      </BFormGroup>
    </div>

    <BTable
        :class="{ 'border border-danger rounded': mensagemErroSelecao }"
        :fields="campos"
        :items="unidades"
        hover
        responsive
        small
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
    <div v-if="mensagemErroSelecao" class="text-danger small mt-1">
      {{ mensagemErroSelecao }}
    </div>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {BAlert, BFormCheckbox, BFormGroup, BTable} from 'bootstrap-vue-next';
import InputData from '@/components/comum/InputData.vue';
import ModalPadrao from '@/components/comum/ModalPadrao.vue';
import {ehDataEstritamenteFutura, formatarDataBR, obterAmanhaFormatado} from "@/utils/date";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import type {UnidadeSelecao} from "@/types/tipos";

const props = defineProps<{
  id: string;
  titulo: string;
  texto: string;
  rotuloBotao: string;
  unidades: UnidadeSelecao[];
  unidadesPreSelecionadas: number[];
  mostrarDataLimite?: boolean;
  mostrarSituacao?: boolean;
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

const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const mensagemErroDataLimite = computed(() => {
  if (erroLocalDataLimite.value) return erroLocalDataLimite.value;
  return deveExibirErro(props.mostrarDataLimite && !dataLimite.value) ? "A data limite é obrigatória." : "";
});

const mensagemErroSelecao = computed(() => {
  return deveExibirErro(selecionadosLocal.value.length === 0) ? "Selecione ao menos uma unidade." : "";
});

const ultimaDataLimiteSelecionada = computed(() => {
  const datasSelecionadas = props.unidades
      .filter(unidade => selecionadosLocal.value.includes(unidade.codigo))
      .map(unidade => extrairData(unidade.ultimaDataLimite))
      .filter(data => data.length > 0);
  if (datasSelecionadas.length === 0) {
    return "";
  }
  return datasSelecionadas.reduce((maior, atual) => atual > maior ? atual : maior);
});

const dataMinimaPermitida = computed(() => {
  const amanha = obterAmanhaFormatado();
  const ultimaDataLimite = ultimaDataLimiteSelecionada.value;
  if (ultimaDataLimite.length === 0) return amanha;
  return ultimaDataLimite > amanha ? ultimaDataLimite : amanha;
});

const dataMinimaPermitidaFormatada = computed(() => {
  return formatarDataBR(dataMinimaPermitida.value);
});

watch([dataLimite, ultimaDataLimiteSelecionada], ([novaData, ultimaDataLimite]) => {
  erroLocalDataLimite.value = "";
  if (novaData.length !== 10 || !props.mostrarDataLimite) {
    return;
  }
  if (!ehDataEstritamenteFutura(novaData)) {
    erroLocalDataLimite.value = "A data limite para validação deve ser uma data futura.";
    return;
  }
  if (ultimaDataLimite.length > 0 && novaData < ultimaDataLimite) {
    erroLocalDataLimite.value = "A data limite deve ser maior ou igual à última data limite das unidades selecionadas.";
  }
});

const campos = computed(() => {
  const camposBase = [
    {key: 'selecao', label: '', thStyle: {width: '40px'}},
    {key: 'sigla', label: 'Unidade'}
  ];

  if (props.mostrarSituacao === false) {
    return camposBase;
  }

  return [
    ...camposBase,
    {key: 'situacao', label: 'Situação'}
  ];
});

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

const isFormularioValido = computed(() => {
  const selecaoValida = selecionadosLocal.value.length > 0;
  if (!props.mostrarDataLimite) return selecaoValida;
  return selecaoValida && !!dataLimite.value && !erroLocalDataLimite.value;
});

async function confirmar() {
  if (!validarSubmissao(isFormularioValido.value)) {
    await focarPrimeiroErroInvalido();
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
  resetarValidacao();
  mostrar.value = true;
}

function fechar() {
  mostrar.value = false;
  processando.value = false;
  erro.value = null;
  dataLimite.value = '';
  erroLocalDataLimite.value = '';
  resetarValidacao();
}

function setProcessando(val: boolean) {
  processando.value = val;
}

function setErro(msg: string | null) {
  erro.value = msg;
}

function extrairData(data?: string) {
  if (typeof data !== "string" || data.length === 0) {
    return "";
  }
  return data.split("T")[0];
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
}, {immediate: true});
</script>
