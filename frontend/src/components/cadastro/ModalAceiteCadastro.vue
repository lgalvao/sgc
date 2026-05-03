<script setup lang="ts">
import CadastroObservacaoModal from "@/components/cadastro/CadastroObservacaoModal.vue";
import {useCadastroObservacaoModalModel} from "@/components/cadastro/cadastroObservacaoModalModel";
import {TEXTOS} from "@/constants/textos";

interface Props {
  modelValue: boolean;
  loading: boolean;
  acao?: {
    tituloModal: string;
    rotuloConfirmacao: string;
    textoModal: string;
  } | null;
  observacao: string;
  erro?: string | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'update:observacao', value: string): void;
  (e: 'confirmar'): void;
}>();
const {model, observacaoModel} = useCadastroObservacaoModalModel(props, emit);
</script>

<template>
  <CadastroObservacaoModal
      v-model="model"
      v-model:observacao="observacaoModel"
      :erro="erro"
      :input-data-testid="'inp-aceite-cadastro-obs'"
      :input-id="'observacaoValidacao'"
      :label="TEXTOS.comum.OBSERVACAO"
      :loading="loading"
      :ok-title="acao?.rotuloConfirmacao"
      :test-codigo-confirmar="'btn-aceite-cadastro-confirmar'"
      :texto="acao?.textoModal"
      :titulo="acao?.tituloModal"
      variant="success"
      @confirmar="$emit('confirmar')"
  />
</template>
