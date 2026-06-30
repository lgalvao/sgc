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
      <p>{{ corpoModal }}</p>

      <BFormGroup :label="TEXTOS.comum.OBSERVACAO" class="mb-0">
        <EditorTextoRico
            v-model="observacao"
            data-testid="inp-aceite-mapa-observacao"
            minimo-altura="10rem"
            rotulo="Observação"
        />
      </BFormGroup>
    </div>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {BFormGroup} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import EditorTextoRico from "@/components/comum/EditorTextoRico.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

interface Props {
  mostrarModal: boolean;
  homologacao?: boolean;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  homologacao: false,
});

const emit = defineEmits<{
  fecharModal: [];
  confirmarAceitacao: [observacao: string];
}>();

const observacao = ref("");

const mostrarModalComputado = computed({
  get: () => props.mostrarModal,
  set: (mostrar: boolean) => {
    if (!mostrar) emit("fecharModal");
  },
});

const tituloModal = computed(() => {
  return props.homologacao
      ? TEXTOS.mapa.MODAL_HOMOLOGAR_TITULO
      : TEXTOS.mapa.MODAL_ACEITE_TITULO;
});

const corpoModal = computed(() => {
  return props.homologacao
      ? TEXTOS.mapa.MODAL_HOMOLOGAR_TEXTO
      : TEXTOS.mapa.MODAL_ACEITE_TEXTO;
});

const rotuloConfirmar = computed(() => {
  return props.homologacao
      ? TEXTOS.mapa.LABEL_HOMOLOGAR
      : TEXTOS.mapa.LABEL_REGISTRAR_ACEITE;
});

watch(() => props.mostrarModal, (mostrar) => {
  if (mostrar) {
    observacao.value = "";
  }
});

function confirmarAceitacao() {
  emit("confirmarAceitacao", observacao.value);
}
</script>
