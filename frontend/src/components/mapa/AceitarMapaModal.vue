<template>
  <BModal
      :fade="false"
      :header-bg-variant="perfil === 'ADMIN' ? 'success' : 'primary'"
      :model-value="mostrarModal"
      :title="tituloModal"
      centered
      header-text-variant="white"
      @hide="$emit('fecharModal')"
  >
    <div data-testid="body-aceite-mapa">
      <p v-if="perfil === 'ADMIN'">
        {{ corpoModal }}
      </p>
      <BFormGroup
          v-else
          label-for="observacao-textarea"
          class="mb-3"
      >
        <template #label>
          Observações <span class="text-muted small">(opcional)</span>
        </template>
        <BFormTextarea
            id="observacao-textarea"
            v-model="observacao"
            data-testid="inp-aceite-mapa-obs"
            placeholder="Digite suas observações sobre o mapa..."
            rows="4"
        />
        <BFormText>
          As observações serão registradas junto com a validação do mapa.
        </BFormText>
      </BFormGroup>
    </div>

    <template #footer>
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            :disabled="loading"
            class="text-decoration-none text-secondary fw-medium btn-cancelar-link"
            data-testid="btn-aceite-mapa-cancelar"
            variant="link"
            @click="$emit('fecharModal')"
        >
          <i aria-hidden="true" class="bi bi-x-circle me-1"/>
          Cancelar
        </BButton>
        <BButton
            :disabled="loading"
            data-testid="btn-aceite-mapa-confirmar"
            variant="success"
            @click="$emit('confirmarAceitacao', observacao)"
        >
          <BSpinner v-if="loading" aria-hidden="true" class="me-1" small/>
          <i v-else aria-hidden="true" class="bi bi-check-circle me-1"/>
          {{ loading ? 'Processando...' : 'Aceitar' }}
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BFormGroup, BFormText, BFormTextarea, BModal, BSpinner} from "bootstrap-vue-next";
import {computed, ref} from "vue";

interface Props {
  mostrarModal: boolean;
  perfil?: string;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  perfil: ""
});

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
</script>

<style scoped>
.form-control:focus {
  border-color: var(--bs-success);
  box-shadow: 0 0 0 0.2rem rgba(var(--bs-success-rgb), 0.25);
}

.btn-cancelar-link {
  padding: 0.375rem 0.75rem;
  transition: all 0.2s;
  border-radius: 0.375rem;
}

.btn-cancelar-link:hover {
  color: var(--bs-emphasis-color) !important;
  background-color: var(--bs-secondary-bg);
}
</style>
