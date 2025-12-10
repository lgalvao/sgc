<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
      header-bg-variant="primary"
      header-text-variant="white"
      hide-footer
      title="Disponibilizar Mapa"
      @hide="fechar"
  >
    <div class="mb-3">
      <label
          class="form-label"
          for="dataLimite"
      >Data limite para validação</label>
      <BFormInput
          id="dataLimite"
          v-model="dataLimiteValidacao"
          data-testid="input-data-limite"
          type="date"
      />
    </div>
    <div
        v-if="notificacao"
        class="alert alert-info mt-3"
    >
      {{ notificacao }}
    </div>

    <template #footer>
      <button
          class="btn btn-secondary"
          data-testid="disponibilizar-mapa-modal__btn-modal-cancelar"
          type="button"
          @click="fechar"
      >
        Cancelar
      </button>
      <button
          :disabled="!dataLimiteValidacao"
          class="btn btn-success"
          data-testid="btn-disponibilizar"
          type="button"
          @click="disponibilizar"
      >
        Disponibilizar
      </button>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BFormInput, BModal} from "bootstrap-vue-next";
import {ref, watch} from "vue";

const props = defineProps<{
  mostrar: boolean;
}>();

const emit = defineEmits<{
  fechar: [];
  disponibilizar: [dataLimite: string];
}>();

const dataLimiteValidacao = ref("");
const notificacao = ref("");

watch(
    () => props.mostrar,
    (mostrar) => {
      if (mostrar) {
        dataLimiteValidacao.value = "";
        notificacao.value = "";
      }
    },
);

function fechar() {
  emit("fechar");
}

function disponibilizar() {
  emit("disponibilizar", dataLimiteValidacao.value);
}
</script>
