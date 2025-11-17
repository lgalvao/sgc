<template>
  <b-modal
    :model-value="mostrar"
    title="Finalização de processo"
    header-bg-variant="success"
    header-text-variant="white"
    size="lg"
    centered
    @hidden="emit('fechar')"
  >
    <div class="alert alert-info">
      <i class="bi bi-info-circle" />
      Confirma a finalização do processo <strong>{{ processoDescricao }}</strong>?<br>
      Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades
      participantes do processo.
    </div>
    <template #footer>
      <button
        type="button"
        class="btn btn-secondary"
        data-testid="btn-cancelar-finalizacao"
        @click="emit('fechar')"
      >
        <i class="bi bi-x-circle" /> Cancelar
      </button>
      <button
        type="button"
        class="btn btn-success"
        data-testid="btn-confirmar-finalizacao"
        @click="emit('confirmar')"
      >
        <i class="bi bi-check-circle" />
        Confirmar
      </button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
defineProps<{
  mostrar: boolean;
  processoDescricao: string;
}>();

const emit = defineEmits<{
  (e: 'fechar'): void
  (e: 'confirmar'): void
}>();
</script>
