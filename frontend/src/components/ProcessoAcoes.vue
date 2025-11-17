<template>
  <div>
    <div
      v-if="mostrarBotoesBloco"
      class="mt-3 d-flex gap-2"
    >
      <b-button
        v-if="perfil === 'GESTOR'"
        variant="outline-primary"
        data-testid="btn-aceitar-em-bloco"
        @click="emit('aceitarBloco')"
      >
        <i class="bi bi-check-circle me-1" />
        Aceitar em bloco
      </b-button>
      <b-button
        v-if="perfil === 'ADMIN'"
        variant="outline-success"
        data-testid="btn-abrir-modal-homologar-bloco"
        @click="emit('homologarBloco')"
      >
        <i class="bi bi-check-all me-1" />
        Homologar em bloco
      </b-button>
    </div>
    <b-button
      v-if="perfil === 'ADMIN' && situacaoProcesso === 'EM_ANDAMENTO'"
      variant="danger"
      class="mt-3"
      data-testid="btn-finalizar-processo"
      @click="emit('finalizar')"
    >
      Finalizar processo
    </b-button>
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
