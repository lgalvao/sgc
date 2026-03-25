<template>
  <ModalPadrao
      :acao-desabilitada="!dataLimiteValidacao || !!erroLocalDataLimite"
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
    <BAlert v-if="fieldErrors?.generic" :model-value="true" variant="danger" class="mb-3">
      {{ fieldErrors.generic }}
    </BAlert>
    <BFormGroup
        label="Data limite para validação"
        label-for="dataLimite"
        :state="(fieldErrors?.dataLimite || erroLocalDataLimite) ? false : null"
        :invalid-feedback="fieldErrors?.dataLimite || erroLocalDataLimite"
        class="mb-3"
    >
      <InputData
          id="dataLimite"
          v-model="dataLimiteValidacao"
          :state="(fieldErrors?.dataLimite || erroLocalDataLimite) ? false : null"
          data-testid="inp-disponibilizar-mapa-data"
          max="2099-12-31"
          :min="obterAmanhaFormatado()"
      />
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
          placeholder="Digite observações sobre a disponibilização..."
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
          :disabled="!dataLimiteValidacao || !!erroLocalDataLimite"
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
import {BAlert, BFormGroup, BFormTextarea} from "bootstrap-vue-next";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import InputData from "@/components/comum/InputData.vue";
import {ref, watch} from "vue";
import {isDateStrictlyFuture, obterAmanhaFormatado} from "@/utils/dateUtils";

const props = defineProps<{
  mostrar: boolean;
  notificacao?: string;
  loading?: boolean;
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

watch(dataLimiteValidacao, (novaData) => {
  erroLocalDataLimite.value = "";
  if (novaData && novaData.length === 10 && !isDateStrictlyFuture(novaData)) {
    erroLocalDataLimite.value = "A data limite para validação deve ser uma data futura.";
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
  emit("disponibilizar", {
    dataLimite: dataLimiteValidacao.value,
    observacoes: observacoesDisponibilizacao.value
  });
}
</script>
