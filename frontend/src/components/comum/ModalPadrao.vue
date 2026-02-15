<template>
  <BModal
      v-model="modelValueComputed"
      :centered="centralizado"
      :fade="fade"
      :size="tamanho"
      :title="titulo"
      @hide="fechar"
      @shown="emit('shown')"
  >
    <slot />
    <template #footer>
      <div class="d-flex justify-content-between w-100">
        <BButton
            :data-testid="testIdCancelar || 'btn-modal-padrao-cancelar'"
            :disabled="loading"
            variant="secondary"
            @click="fechar"
        >
          {{ textoCancelar }}
        </BButton>
        <slot name="acao">
          <BButton
              v-if="mostrarBotaoAcao"
              :data-testid="testIdConfirmar || 'btn-modal-padrao-confirmar'"
              :disabled="loading || acaoDesabilitada"
              :variant="variantAcao"
              @click="emit('confirmar')"
          >
            <output
                v-if="loading"
                aria-hidden="true"
                class="spinner-border spinner-border-sm me-1"
            />
            {{ loading ? textoAcaoCarregando : textoAcao }}
          </BButton>
        </slot>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BModal} from "bootstrap-vue-next";
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
  variantAcao?: "primary" | "secondary" | "success" | "danger";
  loading?: boolean;
  acaoDesabilitada?: boolean;
  mostrarBotaoAcao?: boolean;
  testIdConfirmar?: string;
  testIdCancelar?: string;
}>(), {
  tamanho: undefined,
  fade: false,
  centralizado: true,
  textoCancelar: "Cancelar",
  textoAcao: "Confirmar",
  textoAcaoCarregando: "Processando...",
  variantAcao: "primary",
  loading: false,
  acaoDesabilitada: false,
  mostrarBotaoAcao: true,
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
