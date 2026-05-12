<script lang="ts" setup>
import {computed} from "vue";
import ModalVisualizacaoTextoFormatado from "@/components/comum/ModalVisualizacaoTextoFormatado.vue";

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
    <ModalVisualizacaoTextoFormatado
        v-model="mostrar"
        :conteudo="sugestoes"
        test-id-conteudo="txt-ver-sugestoes-mapa-html"
        titulo="Sugestões sobre o mapa"
        @fechar="$emit('fechar')"
    />
</template>
