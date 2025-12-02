<template>
  <BModal
    :fade="false"
    :model-value="mostrarModal"
    :title="tituloModal"
    :header-bg-variant="perfil === 'ADMIN' ? 'success' : 'primary'"
    header-text-variant="white"
    centered
    hide-footer
    @hide="$emit('fecharModal')"
  >
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
        <BFormTextarea
          id="observacao-textarea"
          v-model="observacao"
          rows="4"
          placeholder="Digite suas observações sobre o mapa..."
          data-testid="observacao-aceite-textarea"
        />
        <div class="form-text">
          As observações serão registradas junto com a validação do mapa.
        </div>
      </div>
    </div>

    <template #footer>
      <BButton
        variant="secondary"
        data-testid="modal-aceite-cancelar"
        @click="$emit('fecharModal')"
      >
        <i class="bi bi-x-circle me-1" />
        Cancelar
      </BButton>
      <BButton
        variant="success"
        data-testid="modal-aceite-confirmar"
        @click="$emit('confirmarAceitacao', observacao)"
      >
        <i class="bi bi-check-circle me-1" />
        Aceitar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BFormTextarea, BModal} from "bootstrap-vue-next";
import {computed, ref} from "vue";

interface Props {
  mostrarModal: boolean;
  perfil?: string;
}

const props = defineProps<Props>();

defineEmits<{
  fecharModal: [];
  confirmarAceitacao: [observacao: string];
}>();

const observacao = ref("");

const tituloModal = computed(() => {
  return props.perfil === "ADMIN"
      ? "Homologação"
      : "Aceitar Mapa de Competências";
});

const corpoModal = computed(() => {
  return props.perfil === "ADMIN"
      ? "Confirma a homologação do mapa de competências?"
      : "Observações";
});

computed(() => {
  return props.perfil !== "ADMIN";
});
</script>

<style scoped>
.form-control:focus {
  border-color: var(--bs-success);
  box-shadow: 0 0 0 0.2rem rgba(var(--bs-success-rgb), 0.25);
}

/* BButton handles variants, but custom gradients might still be desired or need removal for Bootstrap standard look */
</style>
