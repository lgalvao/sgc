<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
      data-testid="mdl-disponibilizar-mapa"
      title="Disponibilização do mapa de competências"
      @hide="fechar"
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
    <template #footer>
      <BButton
          data-testid="btn-disponibilizar-mapa-cancelar"
          variant="secondary"
          @click="fechar"
      >
        Cancelar
      </BButton>
      <BButton
          :disabled="!dataLimiteValidacao"
          data-testid="btn-disponibilizar-mapa-confirmar"
          variant="success"
          @click="disponibilizar"
      >
        Disponibilizar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormInput, BFormInvalidFeedback, BFormTextarea, BModal} from "bootstrap-vue-next";
import {ref, watch} from "vue";

const props = defineProps<{
  mostrar: boolean;
  notificacao?: string;
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