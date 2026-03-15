<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      :title="isRevisao ? TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TITULO : TEXTOS.atividades.MODAL_DISPONIBILIZAR_TITULO"
      centered
      @hide="emit('fechar')"
  >
    <template #default>
      <p>
        {{
          isRevisao ? TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TEXTO : TEXTOS.atividades.MODAL_DISPONIBILIZAR_TEXTO
        }}
      </p>
    </template>
    <template #footer>
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            class="text-decoration-none text-secondary fw-medium btn-cancelar-link"
            data-testid="btn-disponibilizar-revisao-cancelar"
            variant="link"
            @click="emit('fechar')"
        >
          Cancelar
        </BButton>
        <BButton
            data-testid="btn-confirmar-disponibilizacao"
            variant="success"
            @click="emit('confirmar')"
        >
          Confirmar
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BModal} from "bootstrap-vue-next";
import {TEXTOS} from "@/constants/textos";

defineProps<{
  mostrar: boolean;
  isRevisao: boolean;
}>();

const emit = defineEmits<{
  (e: 'fechar'): void;
  (e: 'confirmar'): void;
}>();
</script>

<style scoped>
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
