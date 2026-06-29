<template>
  <BModal
      v-model="modelValueComputed"
      :body-class="bodyClass"
      :centered="centralizado"
      :fade="fade"
      :size="tamanho"
      :title="testIdTitulo ? undefined : titulo"
      modal-class="modal-responsivo"
      @hide="fechar"
      @shown="emit('shown')"
  >
    <template v-if="testIdTitulo" #title>
      <span :data-testid="testIdTitulo">{{ titulo }}</span>
    </template>
    <div v-if="$slots.alerta" class="modal-padrao__alerta mb-3">
      <slot name="alerta"></slot>
    </div>
    <slot/>
    <template #footer>
      <div class="d-flex justify-content-end w-100 footer-modal-padrao gap-3 align-items-center">
        <BButton
            :data-testid="testIdCancelar || 'btn-modal-padrao-cancelar'"
            :disabled="loading"
            :class="variantCancelar === 'link' ? 'text-decoration-none text-secondary fw-medium btn-cancelar-link' : ''"
            :variant="variantCancelar"
            @click="fechar"
        >
          {{ textoCancelar }}
        </BButton>
        <slot name="acao">
          <BButton
              v-if="mostrarBotaoAcao"
              :aria-busy="loading"
              :data-testid="testIdConfirmar || 'btn-modal-padrao-confirmar'"
              :disabled="loading || acaoDesabilitada"
              :variant="variantAcao"
              @click="emit('confirmar')"
          >
            <BSpinner
                v-if="loading"
                aria-hidden="true"
                class="me-1"
                small
            />
            {{ loading ? textoAcaoCarregando : textoAcao }}
          </BButton>
        </slot>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BModal, BSpinner} from "bootstrap-vue-next";
import {computed} from "vue";

const props = withDefaults(defineProps<{
  modelValue: boolean;
  titulo: string;
  tamanho?: "sm" | "lg" | "xl";
  fade?: boolean;
  centralizado?: boolean;
  textoCancelar?: string;
  textoAcao?: string;
  textoAcaoCarregando?: string;
  testIdTitulo?: string;
  variantAcao?: "primary" | "secondary" | "success" | "danger";
  variantCancelar?: "link" | "secondary" | "outline-secondary";
  loading?: boolean;
  acaoDesabilitada?: boolean;
  mostrarBotaoAcao?: boolean;
  bodyClass?: string;
  testIdConfirmar?: string;
  testIdCancelar?: string;
}>(), {
  tamanho: undefined,
  fade: false,
  centralizado: true,
  textoCancelar: "Cancelar",
  textoAcao: "Confirmar",
  textoAcaoCarregando: "Processando...",
  testIdTitulo: "",
  variantAcao: "success",
  variantCancelar: "link",
  loading: false,
  acaoDesabilitada: false,
  mostrarBotaoAcao: true,
  bodyClass: undefined,
  testIdConfirmar: "",
  testIdCancelar: "",
});

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean): void;
  (e: "fechar"): void;
  (e: "confirmar"): void;
  (e: "shown"): void;
}>();

const modelValueComputed = computed({
  get: () => props.modelValue,
  set: (valor: boolean) => emit("update:modelValue", valor),
});

function fechar() {
  emit("fechar");
  modelValueComputed.value = false;
}
</script>

<style scoped>
@media (max-width: 576px) {
  .footer-modal-padrao {
    flex-direction: column;
    gap: 0.5rem;
  }

  .footer-modal-padrao > button {
    width: 100%;
  }

  :deep(.modal-responsivo .modal-dialog) {
    margin: 0.5rem;
  }
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

:global([data-bs-theme="dark"] .btn-cancelar-link) {
  color: #cbd5e1 !important;
}
</style>
