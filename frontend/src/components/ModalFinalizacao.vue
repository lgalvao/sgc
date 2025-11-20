<template>
  <BModal
    :model-value="mostrar"
    title="Finalização de processo"
    header-bg-variant="success"
    header-text-variant="white"
    size="lg"
    centered
    hide-footer
    @hide="emit('fechar')"
  >
    <BAlert
      variant="info"
      :model-value="true"
    >
      <i class="bi bi-info-circle" />
      Confirma a finalização do processo <strong>{{ processoDescricao }}</strong>?<br>
      Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades
      participantes do processo.
    </BAlert>
    <template #footer>
      <BButton
        variant="secondary"
        data-testid="btn-cancelar-finalizacao"
        @click="emit('fechar')"
      >
        <i class="bi bi-x-circle" /> Cancelar
      </BButton>
      <BButton
        variant="success"
        data-testid="btn-confirmar-finalizacao"
        @click="emit('confirmar')"
      >
        <i class="bi bi-check-circle" />
        Confirmar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BModal, BButton, BAlert} from 'bootstrap-vue-next';

defineProps<{
  mostrar: boolean;
  processoDescricao: string;
}>();

const emit = defineEmits<{
  (e: 'fechar'): void
  (e: 'confirmar'): void
}>();
</script>
