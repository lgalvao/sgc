<script lang="ts" setup>
import {computed} from 'vue';
import CadastroObservacaoModal from "@/components/cadastro/CadastroObservacaoModal.vue";
import {useCadastroObservacaoModalModel} from "@/components/cadastro/cadastroObservacaoModalModel";
import {TEXTOS} from "@/constants/textos";

interface Props {
  modelValue: boolean;
  loading: boolean;
  isRevisao: boolean;
  observacao: string;
  erro?: string | null;
  erroObservacao?: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'update:observacao', value: string): void;
  (e: 'confirmar'): void;
}>();
const {model, observacaoModel} = useCadastroObservacaoModalModel(props, emit);

const estadoObservacao = computed(() => {
  if (props.erroObservacao) return false;
  if (observacaoModel.value.trim().length > 0) return true;
  return null;
});
</script>

<template>
  <CadastroObservacaoModal
      v-model="model"
      v-model:observacao="observacaoModel"
      :erro="erro"
      :estado-observacao="estadoObservacao"
      :feedback-observacao="erroObservacao || null"
      :input-data-testid="'inp-devolucao-cadastro-obs'"
      :input-id="'observacaoDevolucao'"
      :label="TEXTOS.comum.OBSERVACAO"
      :label-obrigatoria="true"
      :loading="loading"
      :ok-title="TEXTOS.comum.BOTAO_DEVOLVER"
      :test-id-confirmar="'btn-devolucao-cadastro-confirmar'"
      :texto="isRevisao ? TEXTOS.atividades.MODAL_DEVOLVER_REVISAO_TEXTO : TEXTOS.atividades.MODAL_DEVOLVER_TEXTO"
      :titulo="isRevisao ? TEXTOS.atividades.MODAL_DEVOLVER_REVISAO_TITULO : TEXTOS.atividades.MODAL_DEVOLVER_TITULO"
      variant="danger"
      @confirmar="$emit('confirmar')"
  />
</template>

