<template>
  <BaseModal
    :mostrar="mostrarModal"
    :titulo="tituloModal"
    :tipo="perfil === 'ADMIN' ? 'success' : 'primary'"
    :icone="perfil === 'ADMIN' ? 'bi bi-check-circle' : 'bi bi-check-circle'"
    @fechar="$emit('fecharModal')"
  >
    <template #conteudo>
      <div data-testid="modal-aceite-body">
        <p v-if="perfil === 'ADMIN'">
          {{ corpoModal }}
        </p>
        <div
          v-else
          class="mb-3"
        >
          <label
            for="observacao-textarea"
            class="form-label"
          >
            Observações <span class="text-muted small">(opcional)</span>
          </label>
          <textarea
            id="observacao-textarea"
            v-model="observacao"
            class="form-control"
            rows="4"
            placeholder="Digite suas observações sobre o mapa..."
            data-testid="observacao-aceite-textarea"
          />
          <div class="form-text">
            As observações serão registradas junto com a validação do mapa.
          </div>
        </div>
      </div>
    </template>

    <template #acoes>
      <button
        type="button"
        class="btn btn-secondary"
        data-testid="modal-aceite-cancelar"
        @click="$emit('fecharModal')"
      >
        <i class="bi bi-x-circle me-1" />
        Cancelar
      </button>
      <button
        type="button"
        class="btn btn-success"
        data-testid="modal-aceite-confirmar"
        @click="$emit('confirmarAceitacao', observacao)"
      >
        <i class="bi bi-check-circle me-1" />
        Aceitar
      </button>
    </template>
  </BaseModal>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import BaseModal from './BaseModal.vue';

interface Props {
  mostrarModal: boolean;
  perfil?: string;
}

const props = defineProps<Props>();

defineEmits<{
  fecharModal: [];
  confirmarAceitacao: [observacao: string];
}>();

const observacao = ref('');

const tituloModal = computed(() => {
  return props.perfil === 'ADMIN' ? 'Homologação' : 'Aceitar Mapa de Competências';
});

const corpoModal = computed(() => {
  return props.perfil === 'ADMIN'
    ? 'Confirma a homologação do mapa de competências?'
    : 'Observações';
});

computed(() => {
  return props.perfil !== 'ADMIN';
});
</script>

<style scoped>
.form-control:focus {
  border-color: var(--bs-success);
  box-shadow: 0 0 0 0.2rem rgba(var(--bs-success-rgb), 0.25);
}

.btn-success {
  background: linear-gradient(135deg, var(--bs-success) 0%, var(--bs-success) 100%);
  border: none;
  transition: transform 0.2s ease;
}

.btn-success:hover {
  transform: translateY(-1px);
  background: linear-gradient(135deg, var(--bs-success) 0%, var(--bs-success) 100%);
}
</style>