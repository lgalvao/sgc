<template>
  <ModalPadrao
      :loading="loading"
      :model-value="mostrar"
      data-testid="mdl-disponibilizar-mapa"
      test-codigo-cancelar="btn-disponibilizar-mapa-cancelar"
      texto-acao="Disponibilizar"
      texto-acao-carregando="Disponibilizando..."
      titulo="Disponibilização do mapa de competências"
      variant-acao="success"
      @confirmar="disponibilizar"
      @fechar="fechar"
  >
    <BAlert v-if="fieldErrors?.generic" :model-value="true" variant="danger" class="mb-3">
      {{ fieldErrors.generic }}
    </BAlert>
    <BFormGroup
        label-for="dataLimite"
        :state="mensagemErroDataLimite ? false : null"
        :invalid-feedback="mensagemErroDataLimite"
        class="mb-3"
    >
      <template #label>
        Data limite para validação <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <InputData
          id="dataLimite"
          v-model="dataLimiteValidacao"
          :state="mensagemErroDataLimite ? false : null"
          data-testid="inp-disponibilizar-mapa-data"
          max="2099-12-31"
          :min="dataMinimaPermitida"
      />
      <BFormInvalidFeedback
          :state="mensagemErroDataLimite ? false : null"
          class="d-block"
          data-testid="txt-disponibilizar-mapa-erro-data"
      >
        {{ mensagemErroDataLimite }}
      </BFormInvalidFeedback>
    </BFormGroup>

    <BFormGroup
        label="Observações"
        label-for="observacoes"
        :state="fieldErrors?.observacoes ? false : null"
        :invalid-feedback="fieldErrors?.observacoes"
        class="mb-3"
    >
        <BFormTextarea
            id="observacoes"
            v-model="observacoesDisponibilizacao"
            :state="fieldErrors?.observacoes ? false : null"
            data-testid="inp-disponibilizar-mapa-obs"
            rows="3"
        />
    </BFormGroup>
    <BAlert
        v-if="notificacao"
        :fade="false"
        :model-value="true"
        class="mt-3"
        data-testid="alert-disponibilizar-mapa"
        variant="secondary"
    >
      {{ notificacao }}
    </BAlert>
    <template #acao>
      <LoadingButton
          :loading="loading"
          data-testid="btn-disponibilizar-mapa-confirmar"
          loading-text="Disponibilizando..."
          text="Disponibilizar"
          variant="success"
          @click="disponibilizar"
      />
    </template>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BFormGroup, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import InputData from "@/components/comum/InputData.vue";
import {computed, ref, watch} from "vue";
import {ehDataEstritamenteFutura, obterAmanhaFormatado} from "@/utils/date";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

const props = defineProps<{
  mostrar: boolean;
  notificacao?: string;
  loading?: boolean;
  ultimaDataLimiteSubprocesso?: string;
  fieldErrors?: {
    dataLimite?: string;
    observacoes?: string;
    generic?: string;
  };
}>();

const emit = defineEmits<{
  fechar: [];
  disponibilizar: [payload: { dataLimite: string; observacoes: string }];
}>();

const dataLimiteValidacao = ref("");
const observacoesDisponibilizacao = ref("");
const erroLocalDataLimite = ref("");

const {
  validarSubmissao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const ultimaDataLimiteFormatada = computed(() => extrairData(props.ultimaDataLimiteSubprocesso));
const mensagemErroDataLimite = computed(() => {
  if (props.fieldErrors?.dataLimite) return props.fieldErrors.dataLimite;
  if (erroLocalDataLimite.value) return erroLocalDataLimite.value;
  return deveExibirErro(!dataLimiteValidacao.value) ? "A data limite é obrigatória." : "";
});
const dataMinimaPermitida = computed(() => {
  const amanha = obterAmanhaFormatado();
  const ultimaDataLimite = ultimaDataLimiteFormatada.value;
  if (!ultimaDataLimite) return amanha;
  return ultimaDataLimite > amanha ? ultimaDataLimite : amanha;
});

watch([dataLimiteValidacao, ultimaDataLimiteFormatada], ([novaData, ultimaDataLimite]) => {
  erroLocalDataLimite.value = "";
  if (!novaData || novaData.length !== 10) return;

  if (!ehDataEstritamenteFutura(novaData)) {
    erroLocalDataLimite.value = "A data limite para validação deve ser uma data futura.";
    return;
  }

  if (ultimaDataLimite && novaData < ultimaDataLimite) {
    erroLocalDataLimite.value = "A data limite deve ser maior ou igual à última data limite do subprocesso.";
  }
});

watch(
    () => props.mostrar,
    (mostrar) => {
      if (mostrar) {
        dataLimiteValidacao.value = "";
        observacoesDisponibilizacao.value = "";
        erroLocalDataLimite.value = "";
      }
    },
);

function fechar() {
  emit("fechar");
}

function disponibilizar() {
  const formularioValido = Boolean(dataLimiteValidacao.value) && !erroLocalDataLimite.value;
  
  if (!validarSubmissao(formularioValido)) {
    void focarPrimeiroErroInvalido();
    return;
  }

  emit("disponibilizar", {
    dataLimite: dataLimiteValidacao.value,
    observacoes: observacoesDisponibilizacao.value
  });
}

function extrairData(data?: string) {
  return data?.split("T")[0] ?? "";
}
</script>
