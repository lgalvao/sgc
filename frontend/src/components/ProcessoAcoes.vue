<template>
  <div>
    <div
      v-if="mostrarBotoesBloco"
      class="mt-3 d-flex gap-2"
    >
      <button
        v-if="perfil === 'GESTOR'"
        class="btn btn-outline-primary"
        data-testid="btn-aceitar-em-bloco"
        @click="emit('aceitarBloco')"
      >
        <i class="bi bi-check-circle me-1" />
        Aceitar em bloco
      </button>
      <button
        v-if="perfil === 'ADMIN'"
        class="btn btn-outline-success"
        data-testid="btn-abrir-modal-homologar-bloco"
        @click="emit('homologarBloco')"
      >
        <i class="bi bi-check-all me-1" />
        Homologar em bloco
      </button>
    </div>
    <button
      v-if="perfil === 'ADMIN' && situacaoProcesso === 'EM_ANDAMENTO'"
      class="btn btn-danger mt-3"
      data-testid="btn-finalizar-processo"
      @click="emit('finalizar')"
    >
      Finalizar processo
    </button>
  </div>
</template>

<script lang="ts" setup>
import type {Perfil, SituacaoProcesso} from '@/types/tipos';

defineProps<{
  mostrarBotoesBloco: boolean;
  perfil: Perfil | string;
  situacaoProcesso: SituacaoProcesso | string;
}>();

const emit = defineEmits<{
  (e: 'aceitarBloco'): void
  (e: 'homologarBloco'): void
  (e: 'finalizar'): void
}>();
</script>
