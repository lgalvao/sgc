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
    <BFormGroup class="mb-3" :label="TEXTOS.comum.OBSERVACAO" label-for="observacaoDisponibilizacao">
      <BFormTextarea
          id="observacaoDisponibilizacao"
          v-model="observacao"
          data-testid="inp-disponibilizar-cadastro-obs"
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
  (e: 'confirmar', observacao: string): void;
}>();

const observacao = ref("");

const mostrarComputado = computed({
  get: () => props.mostrar,
  set: (mostrar: boolean) => {
    if (!mostrar) emit('fechar');
  }
});

watch(
    () => props.mostrar,
    (mostrar) => {
      if (mostrar) {
        observacao.value = "";
      }
    }
);

function confirmar() {
  emit('confirmar', observacao.value);
}
</script>
