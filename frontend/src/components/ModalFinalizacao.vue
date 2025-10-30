<template>
  <div>
    <div
      v-if="mostrar"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-check-circle text-success" />
              Finalização de processo
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="emit('fechar')"
            />
          </div>
          <div class="modal-body">
            <div class="alert alert-info">
              <i class="bi bi-info-circle" />
              Confirma a finalização do processo <strong>{{ processoDescricao }}</strong>?<br>
              Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades
              participantes do processo.
            </div>
          </div>
          <div class="modal-footer">
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
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="mostrar"
      class="modal-backdrop fade show"
    />
  </div>
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
