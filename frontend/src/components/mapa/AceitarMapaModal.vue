<template>
  <ModalConfirmacao
      v-model="mostrarModalComputado"
      :auto-close="false"
      :loading="loading"
      :ok-title="rotuloConfirmar"
      :titulo="tituloModal"
      ok-variant="success"
      test-id-cancelar="btn-aceite-mapa-cancelar"
      test-id-confirmar="btn-aceite-mapa-confirmar"
      variant="success"
      @confirmar="confirmarAceitacao"
  >
    <div data-testid="body-aceite-mapa">
      <p class="mb-0">{{ corpoModal }}</p>
    </div>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {computed} from "vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

interface Props {
  mostrarModal: boolean;
  perfil?: string;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  perfil: ""
});

const emit = defineEmits<{
  fecharModal: [];
  confirmarAceitacao: [observacao: string];
}>();

const mostrarModalComputado = computed({
  get: () => props.mostrarModal,
  set: (mostrar: boolean) => {
    if (!mostrar) emit("fecharModal");
  }
});

const tituloModal = computed(() => {
  return props.perfil === "ADMIN"
      ? TEXTOS.mapa.MODAL_HOMOLOGAR_TITULO
      : TEXTOS.mapa.MODAL_ACEITE_TITULO;
});

const corpoModal = computed(() => {
  return props.perfil === "ADMIN"
      ? TEXTOS.mapa.MODAL_HOMOLOGAR_TEXTO
      : TEXTOS.mapa.MODAL_ACEITE_TEXTO;
});

const rotuloConfirmar = computed(() => {
  return props.perfil === "ADMIN"
      ? TEXTOS.mapa.LABEL_HOMOLOGAR
      : TEXTOS.mapa.BOTAO_ACEITAR;
});

function confirmarAceitacao() {
  emit("confirmarAceitacao", "");
}
</script>
