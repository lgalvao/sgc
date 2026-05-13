<template>
  <ModalPadrao
      :loading="loading"
      :model-value="mostrarModal"
      data-testid="mdl-alterar-data-limite"
      test-id-cancelar="subprocesso-modal__btn-modal-cancelar"
      test-id-confirmar="btn-modal-confirmar"
      texto-acao="Alterar"
      titulo="Alterar data limite"
      variant-acao="success"
      @confirmar="confirmar"
      @fechar="$emit('fecharModal')"
  >
    <BFormGroup
        :invalid-feedback="mensagemErroDataLimite"
        :state="mensagemErroDataLimite ? false : null"
        class="mb-3"
        description="Selecione uma data futura"
        label-for="novaDataLimite"
    >
      <template #label>
        Nova data limite <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <InputData
          id="novaDataLimite"
          v-model="novaDataLimite"
          :min="dataLimiteMinima"
          :state="mensagemErroDataLimite ? false : null"
          data-testid="input-nova-data-limite"
          max="2099-12-31"
      />
      <template #description>
        <div class="mt-1">
          Data limite atual: {{ dataLimiteAtualFormatada }}
        </div>
      </template>
    </BFormGroup>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {BFormGroup} from "bootstrap-vue-next";
import InputData from "@/components/comum/InputData.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import {computed, nextTick, ref, watch} from "vue";
import {
  analisarData,
  ehDataEstritamenteFutura,
  formatarDataBR,
  formatarDataParaInput,
  obterAmanhaFormatado,
} from "@/utils";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";


interface Props {
  mostrarModal: boolean;
  dataLimiteAtual: Date | null;
  ultimaDataLimiteSubprocesso: Date | null;
  etapaAtual: number | null;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
});

const emit = defineEmits<{
  fecharModal: [];
  confirmarAlteracao: [novaData: string];
}>();

const novaDataLimite = ref("");
const campoDataInteragido = ref(false);
const preenchendoValorInicial = ref(false);
const {
  validacaoSubmetida,
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const mensagemErroDataLimite = computed(() => {
  const deveExibirMensagem = campoDataInteragido.value || validacaoSubmetida.value;
  if (!deveExibirMensagem) return "";
  if (deveExibirErro(!novaDataLimite.value)) return "A data limite é obrigatória.";
  if (!ehDataEstritamenteFutura(analisarData(novaDataLimite.value))) {
    return "A data limite para validação deve ser uma data futura.";
  }
  if (dataLimiteMinimaPorUltimaData.value && novaDataLimite.value < dataLimiteMinimaPorUltimaData.value) {
    return "A data limite deve ser maior ou igual à última data limite do subprocesso.";
  }
  return "";
});

watch(novaDataLimite, (novaData) => {
  if (preenchendoValorInicial.value) {
    return;
  }
  campoDataInteragido.value = true;
  if (!novaData || novaData.length !== 10) {
    return;
  }
});

const dataLimiteMinimaPorUltimaData = computed(() => {
  if (!props.ultimaDataLimiteSubprocesso) return "";
  return formatarDataParaInput(props.ultimaDataLimiteSubprocesso);
});

const dataLimiteMinima = computed(() => {
  const amanha = obterAmanhaFormatado();
  const ultimaDataLimite = dataLimiteMinimaPorUltimaData.value;
  if (!ultimaDataLimite) return amanha;
  return ultimaDataLimite > amanha ? ultimaDataLimite : amanha;
});

const dataLimiteAtualFormatada = computed(() => {
  return formatarDataBR(props.dataLimiteAtual);
});

const isDataValida = computed(() => {
  if (!novaDataLimite.value) return false;
  if (!ehDataEstritamenteFutura(analisarData(novaDataLimite.value))) return false;
  return !dataLimiteMinimaPorUltimaData.value || novaDataLimite.value >= dataLimiteMinimaPorUltimaData.value;
});

watch(
    () => props.mostrarModal,
    async (novoValor: boolean) => {
      resetarValidacao();
      campoDataInteragido.value = false;
      if (novoValor && props.dataLimiteAtual) {
        preenchendoValorInicial.value = true;
        novaDataLimite.value = formatarDataParaInput(props.dataLimiteAtual);
        await nextTick();
        preenchendoValorInicial.value = false;
      } else {
        novaDataLimite.value = "";
        preenchendoValorInicial.value = false;
      }
    },
    {immediate: true},
);

function confirmar() {
  if (!validarSubmissao(isDataValida.value)) {
    void focarPrimeiroErroInvalido();
    return;
  }
  emit('confirmarAlteracao', novaDataLimite.value);
}
</script>
