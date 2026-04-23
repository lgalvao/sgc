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
    <p>
      {{
        isRevisao ? TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TEXTO : TEXTOS.atividades.MODAL_DISPONIBILIZAR_TEXTO
      }}
    </p>
    <BFormGroup
        class="mt-3"
        label="Observações"
        label-for="observacoesDisponibilizacao"
    >
      <BFormTextarea
          id="observacoesDisponibilizacao"
          v-model="observacoes"
          data-testid="inp-disponibilizacao-observacoes"
          rows="3"
      />
    </BFormGroup>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from "vue";
import {BFormGroup, BFormTextarea} from "bootstrap-vue-next";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  mostrar: boolean;
  isRevisao: boolean;
  loading?: boolean;
}>();

const emit = defineEmits<{
  (e: 'fechar'): void;
  (e: 'confirmar', observacoes: string): void;
}>();
const observacoes = ref("");

const mostrarComputado = computed({
  get: () => props.mostrar,
  set: (mostrar: boolean) => {
    if (!mostrar) emit('fechar');
  }
});

watch(() => props.mostrar, (mostrar) => {
  if (mostrar) {
    observacoes.value = "";
  }
});

function confirmar() {
  emit('confirmar', observacoes.value);
}
</script>
