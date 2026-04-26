<template>
  <ModalConfirmacao
      v-model="mostrarComputado"
      :auto-close="false"
      :loading="loading"
      :ok-title="TEXTOS.comum.BOTAO_DISPONIBILIZAR"
      :titulo="isRevisao ? TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TITULO : TEXTOS.atividades.MODAL_DISPONIBILIZAR_TITULO"
      test-codigo-cancelar="btn-disponibilizar-revisao-cancelar"
      test-codigo-confirmar="btn-confirmar-disponibilizacao"
      variant="success"
      @confirmar="confirmar"
  >
    <AppAlert v-if="erro" :mensagem="erro" class="mb-3" variante="danger" />
    <p>
      {{
        isRevisao ? TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TEXTO : TEXTOS.atividades.MODAL_DISPONIBILIZAR_TEXTO
      }}
    </p>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {computed} from "vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  mostrar: boolean;
  isRevisao: boolean;
  loading?: boolean;
  erro?: string | null;
}>();

const emit = defineEmits<{
  (e: 'fechar'): void;
  (e: 'confirmar'): void;
}>();

const mostrarComputado = computed({
  get: () => props.mostrar,
  set: (mostrar: boolean) => {
    if (!mostrar) emit('fechar');
  }
});

function confirmar() {
  emit('confirmar');
}
</script>
