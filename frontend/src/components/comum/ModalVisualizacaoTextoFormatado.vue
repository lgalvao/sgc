<script lang="ts" setup>
import {computed} from "vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import ConteudoTextoFormatado from "@/components/comum/ConteudoTextoFormatado.vue";

const props = withDefaults(defineProps<{
  modelValue: boolean;
  titulo: string;
  conteudo: string;
  testIdConteudo?: string;
}>(), {
  testIdConteudo: undefined,
});

const emit = defineEmits<{
  (e: "update:modelValue", valor: boolean): void;
  (e: "fechar"): void;
}>();

const mostrar = computed({
  get: () => props.modelValue,
  set: (valor: boolean) => emit("update:modelValue", valor),
});
</script>

<template>
  <ModalPadrao
      v-model="mostrar"
      :mostrar-botao-acao="false"
      test-id-cancelar="btn-visualizacao-texto-formatado-fechar"
      texto-cancelar="Fechar"
      :titulo="titulo"
      @fechar="$emit('fechar')"
  >
    <ConteudoTextoFormatado
        :conteudo="conteudo"
        :test-id="testIdConteudo"
        class="border rounded p-3 bg-body-tertiary"
    />
  </ModalPadrao>
</template>
