<template>
  <!-- Modal para aceitar mapa com observações -->
  <div
      v-if="mostrarModal"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5
              class="modal-title"
              data-testid="modal-aceite-title"
          >
            <i class="bi bi-check-circle text-success me-2"/>
            {{ tituloModal }}
          </h5>
          <button
              type="button"
              class="btn-close"
              data-testid="modal-aceite-close"
              @click="$emit('fecharModal')"
          />
        </div>
        <div
            class="modal-body"
            data-testid="modal-aceite-body"
        >
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
            >{{ corpoModal }}</label>
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
        <div class="modal-footer">
          <button
              type="button"
              class="btn btn-secondary"
              data-testid="modal-aceite-cancelar"
              @click="$emit('fecharModal')"
          >
            <i class="bi bi-x-circle me-1"/>
            Cancelar
          </button>
          <button
            type="button"
            class="btn btn-success"
            data-testid="modal-aceite-confirmar"
            @click="$emit('confirmarAceitacao', observacao)"
          >
            <i class="bi bi-check-circle me-1"/>
            Aceitar
          </button>
        </div>
      </div>
    </div>
  </div>
  <div
      v-if="mostrarModal"
      class="modal-backdrop fade show"
  />
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';

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
    : 'Observações <small class="text-muted">(opcional)</small>';
});

computed(() => {
  return props.perfil !== 'ADMIN';
});
</script>

<style scoped>
.modal-content {
  border: none;
  border-radius: 0.5rem;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.modal-header {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: none;
  border-radius: 0.5rem 0.5rem 0 0;
}

.modal-title {
  font-weight: 600;
}

.form-control:focus {
  border-color: #28a745;
  box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25);
}

.btn-success {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  border: none;
  transition: transform 0.2s ease;
}

.btn-success:hover {
  transform: translateY(-1px);
  background: linear-gradient(135deg, #218838 0%, #1aa085 100%);
}
</style>