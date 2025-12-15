<template>
      <BModal
        :fade="false"
        :model-value="mostrar"
        :title="processoDescricao ? 'Finalização de processo' : 'Finalização'"
        centered
        hide-footer
        size="lg"
        @hide="emit('fechar')"
      >    <BAlert
        :fade="false"
        :model-value="true"
        variant="info"
    >
      <i class="bi bi-info-circle"/>
      Confirma a finalização do processo <strong>{{ processoDescricao }}</strong>?<br>
      Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades
      participantes do processo.
    </BAlert>
    <template #footer>
      <BButton
          data-testid="btn-finalizar-processo-cancelar"
          variant="secondary"
          @click="emit('fechar')"
      >
        <i class="bi bi-x-circle"/> Cancelar
      </BButton>
      <BButton
          data-testid="btn-finalizar-processo-confirmar"
          variant="success"
          @click="emit('confirmar')"
      >
        <i class="bi bi-check-circle"/>
        Confirmar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BModal} from "bootstrap-vue-next";

defineProps<{
  mostrar: boolean;
  processoDescricao: string;
}>();

const emit = defineEmits<{
  (e: "fechar"): void;
  (e: "confirmar"): void;
}>();
</script>
