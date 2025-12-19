<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      :title="isRevisao ? 'Disponibilização da revisão do cadastro' : 'Disponibilização do cadastro'"
      centered
      hide-footer
      @hide="emit('fechar')"
  >
    <template #default>
      <p>
        {{
          isRevisao ? 'Confirma a finalização da revisão e a disponibilização do cadastro?' : 'Confirma a finalização e a disponibilização do cadastro?'
        }} Essa ação bloqueia a edição e habilita a análise do cadastro por unidades superiores.
      </p>
    </template>
    <template #footer>
      <BButton
          variant="secondary"
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
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BModal} from "bootstrap-vue-next";

defineProps<{
  mostrar: boolean;
  isRevisao: boolean;
}>();

const emit = defineEmits<{
  (e: 'fechar'): void;
  (e: 'confirmar'): void;
}>();
</script>
