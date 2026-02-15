<template>
  <ModalPadrao
      :model-value="mostrar"
      :loading="loading"
      :acao-desabilitada="!dataLimiteValidacao"
      test-id-cancelar="btn-disponibilizar-mapa-cancelar"
      titulo="Disponibilização do mapa de competências"
      data-testid="mdl-disponibilizar-mapa"
      variant-acao="success"
      texto-acao="Disponibilizar"
      texto-acao-carregando="Disponibilizando..."
      @fechar="fechar"
      @confirmar="disponibilizar"
  >
    <div v-if="fieldErrors?.generic" class="alert alert-danger mb-3">
      {{ fieldErrors.generic }}
    </div>
    <div class="mb-3">
      <label
          class="form-label"
          for="dataLimite"
      >Data limite para validação</label>
      <BFormInput
          id="dataLimite"
          v-model="dataLimiteValidacao"
          :state="fieldErrors?.dataLimite ? false : null"
          data-testid="inp-disponibilizar-mapa-data"
          type="date"
      />
      <BFormInvalidFeedback :state="fieldErrors?.dataLimite ? false : null">
        {{ fieldErrors?.dataLimite }}
      </BFormInvalidFeedback>
    </div>
    <div class="mb-3">
      <label
          class="form-label"
          for="observacoes"
      >Observações</label>
      <BFormTextarea
          id="observacoes"
          v-model="observacoesDisponibilizacao"
          :state="fieldErrors?.observacoes ? false : null"
          data-testid="inp-disponibilizar-mapa-obs"
          placeholder="Digite observações sobre a disponibilização..."
          rows="3"
      />
      <BFormInvalidFeedback :state="fieldErrors?.observacoes ? false : null">
        {{ fieldErrors?.observacoes }}
      </BFormInvalidFeedback>
    </div>
    <BAlert
        v-if="notificacao"
        :fade="false"
        :model-value="true"
        class="mt-3"
        data-testid="alert-disponibilizar-mapa"
        variant="info"
    >
      {{ notificacao }}
    </BAlert>
    <template #acao>
      <LoadingButton
          :loading="loading"
          :disabled="!dataLimiteValidacao"
          data-testid="btn-disponibilizar-mapa-confirmar"
          variant="success"
          text="Disponibilizar"
          loading-text="Disponibilizando..."
          @click="disponibilizar"
      />
    </template>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BFormInput, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import {ref, watch} from "vue";

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

watch(
    () => props.mostrar,
    (mostrar) => {
      if (mostrar) {
        dataLimiteValidacao.value = "";
        observacoesDisponibilizacao.value = "";
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
