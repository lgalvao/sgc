<template>
  <div>
    <!-- Modal de confirmação cadastro -->
    <ModalConfirmacao
        :auto-close="false"
        :cancel-title="TEXTOS.comum.BOTAO_CANCELAR"
        :loading="isLoading"
        :model-value="mostrarConfirmacao"
        :ok-title="TEXTOS.comum.BOTAO_INICIAR"
        :titulo="TEXTOS.processo.cadastro.INICIAR_TITULO"
        test-id-cancelar="btn-iniciar-processo-cancelar"
        test-id-confirmar="btn-iniciar-processo-confirmar"
        variant="success"
        @confirmar="$emit('confirmar-iniciar')"
        @update:model-value="$emit('update:mostrarConfirmacao', $event)"
    >
      <div class="confirmacao-resumo">
        <p><strong>Descrição:</strong> {{ descricao }}</p>
        <p><strong>Tipo:</strong> {{ tipoLabel }}</p>
        <p><strong>Unidades selecionadas:</strong> {{ totalUnidades }}</p>
      </div>
      <hr>
      <p class="mb-0">
        {{ TEXTOS.processo.cadastro.INICIAR_CONFIRMACAO }}
      </p>
    </ModalConfirmacao>

    <!-- Modal de confirmação de remoção -->
    <ModalConfirmacao
        :auto-close="false"
        :loading="isLoading"
        :model-value="mostrarRemocao"
        :ok-title="TEXTOS.processo.cadastro.BOTAO_REMOVER"
        :titulo="TEXTOS.processo.cadastro.REMOVER_TITULO"
        variant="danger"
        @confirmar="$emit('confirmar-remocao')"
        @update:model-value="$emit('update:mostrarRemocao', $event)"
    >
      <p class="mb-0">{{ TEXTOS.processo.cadastro.REMOVER_CONFIRMACAO(descricao) }}</p>
    </ModalConfirmacao>
  </div>
</template>

<script lang="ts" setup>
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

defineProps<{
  mostrarConfirmacao: boolean;
  mostrarRemocao: boolean;
  isLoading: boolean;
  descricao: string;
  tipoLabel: string;
  totalUnidades: number;
}>();

defineEmits<{
  'update:mostrarConfirmacao': [value: boolean];
  'update:mostrarRemocao': [value: boolean];
  'confirmar-iniciar': [];
  'confirmar-remocao': [];
}>();
</script>

<style scoped>
.confirmacao-resumo p {
  margin-bottom: 0.5rem;
}
</style>

