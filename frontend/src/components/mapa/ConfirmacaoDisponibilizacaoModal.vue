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
      @shown="onShown"
  >
    <p>
      {{
        isRevisao ? TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TEXTO : TEXTOS.atividades.MODAL_DISPONIBILIZAR_TEXTO
      }}
    </p>

    <div class="mt-3">
      <label
          class="form-label fw-medium"
          for="disponibilizar-obs"
      >
        {{ TEXTOS.comum.OBSERVACAO }}
      </label>
      <textarea
          id="disponibilizar-obs"
          v-model="observacoes"
          class="form-control"
          data-testid="inp-disponibilizar-cadastro-obs"
          placeholder="Opcional..."
          rows="3"
      ></textarea>
    </div>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
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

function onShown() {
  observacoes.value = "";
}

function confirmar() {
  emit('confirmar', observacoes.value);
}
</script>
