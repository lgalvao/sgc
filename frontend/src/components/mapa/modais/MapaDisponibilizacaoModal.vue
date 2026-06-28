<template>
  <ModalPadrao
      :loading="loading"
      :model-value="mostrar"
      data-testid="mdl-disponibilizar-mapa"
      test-id-cancelar="btn-disponibilizar-mapa-cancelar"
      texto-acao="Disponibilizar"
      texto-acao-carregando="Disponibilizando..."
      titulo="Disponibilização do mapa de competências"
      variant-acao="success"
      @confirmar="disponibilizar"
      @fechar="fechar"
  >
    <template #alerta>
      <Alerta
          :mensagem="fieldErrors?.generic"
          data-testid="alert-disponibilizar-mapa-erro"
      />
    </template>
    <BFormGroup
        :invalid-feedback="mensagemErroDataLimite"
        :state="mensagemErroDataLimite ? false : null"
        class="mb-3"
        label-for="dataLimite"
    >
      <template #label>
        Data limite para validação <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <InputData
          id="dataLimite"
          v-model="dataLimiteValidacao"
          :min="dataMinimaPermitida"
          :state="mensagemErroDataLimite ? false : null"
          data-testid="inp-disponibilizar-mapa-data"
          max="2099-12-31"
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
        :invalid-feedback="fieldErrors?.observacoes"
        :state="fieldErrors?.observacoes ? false : null"
        class="mb-3"
        label="Observações"
        label-for="observacoes"
    >
      <EditorTextoRico
          id="observacoes"
          v-model="observacoesDisponibilizacao"
          data-testid="inp-disponibilizar-mapa-obs"
          minimo-altura="10rem"
          rotulo="Observações"
      />
    </BFormGroup>
    <div
        v-if="notificacao"
        class="mt-3 p-3 bg-body-tertiary border rounded text-secondary small d-flex align-items-start gap-2"
        data-testid="alert-disponibilizar-mapa"
    >
      <i class="bi bi-info-circle-fill fs-6 mt-0"></i>
      <div>{{ notificacao }}</div>
    </div>
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
import {BFormGroup, BFormInvalidFeedback} from "bootstrap-vue-next";
import Alerta from "@/components/comum/Alerta.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import InputData from "@/components/comum/InputData.vue";
import EditorTextoRico from "@/components/comum/EditorTextoRico.vue";
import {computed, ref, watch} from "vue";
import {ehDataEstritamenteFutura, obterAmanhaFormatado} from "@/utils/date";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

const props = defineProps<{
  mostrar: boolean;
  notificacao?: string;
  loading?: boolean;
  dataFimEtapaAnterior?: string;
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

const dataFimEtapaAnteriorFormatada = computed(() => extrairData(props.dataFimEtapaAnterior));
const mensagemErroDataLimite = computed(() => {
  if (props.fieldErrors?.dataLimite) return props.fieldErrors.dataLimite;
  if (erroLocalDataLimite.value) return erroLocalDataLimite.value;
  return deveExibirErro(!dataLimiteValidacao.value) ? "A data limite é obrigatória." : "";
});
const dataMinimaPermitida = computed(() => {
  const amanha = obterAmanhaFormatado();
  const dataFimEtapaAnterior = dataFimEtapaAnteriorFormatada.value;
  if (!dataFimEtapaAnterior) return amanha;
  const primeiroDiaValido = somarDias(dataFimEtapaAnterior, 1);
  return primeiroDiaValido > amanha ? primeiroDiaValido : amanha;
});

watch([dataLimiteValidacao, dataFimEtapaAnteriorFormatada], ([novaData, dataFimEtapaAnterior]) => {
  erroLocalDataLimite.value = "";
  if (!novaData || novaData.length !== 10) return;

  if (!ehDataEstritamenteFutura(novaData)) {
    erroLocalDataLimite.value = "A data limite para validação deve ser uma data futura.";
    return;
  }

  if (dataFimEtapaAnterior && novaData <= dataFimEtapaAnterior) {
    erroLocalDataLimite.value = "A data limite deve ser maior que a data de fim da etapa anterior.";
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

function somarDias(dataIso: string, dias: number) {
  const data = new Date(`${dataIso}T00:00:00`);
  data.setDate(data.getDate() + dias);
  return data.toISOString().split("T")[0];
}
</script>
