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
  dataFimEtapaAnterior: Date | null;
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

const deveValidarFimEtapaAnterior = computed(() => props.etapaAtual === 2);
const dataFimEtapaAnteriorFormatada = computed(() => {
  if (!props.dataFimEtapaAnterior) return "";
  return formatarDataParaInput(props.dataFimEtapaAnterior);
});

const mensagemErroDataLimite = computed(() => {
  const deveExibirMensagem = campoDataInteragido.value || validacaoSubmetida.value;
  if (!deveExibirMensagem) return "";
  if (deveExibirErro(!novaDataLimite.value)) return "A data limite é obrigatória.";
  if (!ehDataEstritamenteFutura(analisarData(novaDataLimite.value))) {
    return "A data limite para validação deve ser uma data futura.";
  }
  if (deveValidarFimEtapaAnterior.value && dataFimEtapaAnteriorFormatada.value && novaDataLimite.value <= dataFimEtapaAnteriorFormatada.value) {
    return "A data limite deve ser maior que a data de fim da etapa anterior.";
  }
  if (props.ultimaDataLimiteSubprocesso && novaDataLimite.value < formatarDataParaInput(props.ultimaDataLimiteSubprocesso)) {
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

const dataLimiteMinima = computed(() => {
  const amanha = obterAmanhaFormatado();
  if (!deveValidarFimEtapaAnterior.value || !dataFimEtapaAnteriorFormatada.value) {
    return amanha;
  }
  const primeiroDiaValido = somarDias(dataFimEtapaAnteriorFormatada.value, 1);
  return primeiroDiaValido > amanha ? primeiroDiaValido : amanha;
});

const dataLimiteAtualFormatada = computed(() => {
  return formatarDataBR(props.dataLimiteAtual);
});

const isDataValida = computed(() => {
  if (!novaDataLimite.value) return false;
  if (!ehDataEstritamenteFutura(analisarData(novaDataLimite.value))) return false;
  if (deveValidarFimEtapaAnterior.value && dataFimEtapaAnteriorFormatada.value && novaDataLimite.value <= dataFimEtapaAnteriorFormatada.value) {
    return false;
  }
  return true;
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

function somarDias(dataIso: string, dias: number) {
  const data = new Date(`${dataIso}T00:00:00`);
  data.setDate(data.getDate() + dias);
  return data.toISOString().split("T")[0];
}
</script>
