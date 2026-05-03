<script setup lang="ts">
import {computed} from "vue";
import {BFormGroup, BFormTextarea} from "bootstrap-vue-next";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";

const props = defineProps<{
  modelValue: boolean;
  sugestoes: string;
  podeEditar?: boolean;
}>();

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
      test-codigo-cancelar="btn-ver-sugestoes-mapa-fechar"
      texto-cancelar="Fechar"
      titulo="Sugestões sobre o mapa"
      @fechar="$emit('fechar')"
  >
    <BFormGroup class="mb-3">
      <template #label>Sugestões registradas para o mapa de competências:</template>
      <div v-if="!podeEditar" class="border rounded p-3 bg-body-tertiary white-space-pre-line" data-testid="txt-ver-sugestoes-mapa-texto">
        {{ sugestoes }}
      </div>
      <BFormTextarea v-else id="sugestoesVisualizacao" :model-value="sugestoes" data-testid="txt-ver-sugestoes-mapa" readonly rows="5" />
    </BFormGroup>
  </ModalPadrao>
</template>
