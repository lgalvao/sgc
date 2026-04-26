<template>
  <ModalPadrao
      :loading="loading"
      :model-value="mostrarModal"
      data-testid="mdl-alterar-data-limite"
      test-codigo-cancelar="subprocesso-modal__btn-modal-cancelar"
      test-codigo-confirmar="btn-modal-confirmar"
      titulo="Alterar data limite"
      @confirmar="confirmar"
      @fechar="$emit('fecharModal')"
  >
    <BFormGroup
        description="Selecione uma data futura"
        label-for="novaDataLimite"
        :state="mensagemErroDataLimite ? false : null"
        :invalid-feedback="mensagemErroDataLimite"
        class="mb-3"
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
import {computed, ref, watch} from "vue";
import {formatDateBR, formatDateForInput, isDateStrictlyFuture, obterAmanhaFormatado, parseDate,} from "@/utils";
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
const erroLocalDataLimite = ref("");
const {
  validarSubmissao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const mensagemErroDataLimite = computed(() => {
  if (erroLocalDataLimite.value) return erroLocalDataLimite.value;
  return deveExibirErro(!novaDataLimite.value) ? "A data limite é obrigatória." : "";
});

watch(novaDataLimite, (novaData) => {
  erroLocalDataLimite.value = "";
  if (!novaData || novaData.length !== 10) {
    return;
  }
  if (!isDateStrictlyFuture(parseDate(novaData))) {
    erroLocalDataLimite.value = "A data limite para validação deve ser uma data futura.";
    return;
  }
  if (dataLimiteMinimaPorUltimaData.value && novaData < dataLimiteMinimaPorUltimaData.value) {
    erroLocalDataLimite.value = "A data limite deve ser maior ou igual à última data limite do subprocesso.";
  }
});

const dataLimiteMinimaPorUltimaData = computed(() => {
  if (!props.ultimaDataLimiteSubprocesso) return "";
  return formatDateForInput(props.ultimaDataLimiteSubprocesso);
});

const dataLimiteMinima = computed(() => {
  const amanha = obterAmanhaFormatado();
  const ultimaDataLimite = dataLimiteMinimaPorUltimaData.value;
  if (!ultimaDataLimite) return amanha;
  return ultimaDataLimite > amanha ? ultimaDataLimite : amanha;
});

const dataLimiteAtualFormatada = computed(() => {
  return formatDateBR(props.dataLimiteAtual);
});

const isDataValida = computed(() => {
  if (!novaDataLimite.value) return false;
  if (!isDateStrictlyFuture(parseDate(novaDataLimite.value))) return false;
  return !dataLimiteMinimaPorUltimaData.value || novaDataLimite.value >= dataLimiteMinimaPorUltimaData.value;
});

watch(
    () => props.mostrarModal,
    (novoValor: boolean) => {
      if (novoValor && props.dataLimiteAtual) {
        novaDataLimite.value = formatDateForInput(props.dataLimiteAtual);
      } else {
        novaDataLimite.value = "";
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
