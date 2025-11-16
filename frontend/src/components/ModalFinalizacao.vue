<template>
  <b-modal
    v-model="show"
    title="Finalização de processo"
    size="lg"
    centered
    @hidden="fechar"
  >
    <div class="alert alert-info">
      <i class="bi bi-info-circle" />
      Confirma a finalização do processo <strong>{{ processoDescricao }}</strong>?<br>
      Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades
      participantes do processo.
    </div>

    <template #footer>
      <b-button
        variant="secondary"
        data-testid="btn-cancelar-finalizacao"
        @click="fechar"
      >
        <i class="bi bi-x-circle" /> Cancelar
      </b-button>
      <b-button
        variant="success"
        data-testid="btn-confirmar-finalizacao"
        @click="emit('confirmar')"
      >
        <i class="bi bi-check-circle" />
        Confirmar
      </b-button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

const props = defineProps<{
  mostrar: boolean;
  processoDescricao: string;
}>();

const emit = defineEmits<{
  (e: 'update:mostrar', value: boolean): void
  (e: 'confirmar'): void
}>();

const show = computed({
  get: () => props.mostrar,
  set: (value) => emit('update:mostrar', value)
})

function fechar() {
  emit('update:mostrar', false)
}
</script>
