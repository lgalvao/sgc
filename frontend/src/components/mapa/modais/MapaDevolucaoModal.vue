<script lang="ts" setup>
import {computed} from "vue";
import {BFormGroup, BFormInvalidFeedback} from "bootstrap-vue-next";
import EditorTextoRico from "@/components/comum/EditorTextoRico.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
    modelValue: boolean;
    loading: boolean;
    observacao: string;
    erro?: string;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", valor: boolean): void;
    (e: "update:observacao", valor: string): void;
    (e: "confirmar"): void;
}>();

const mostrar = computed({
    get: () => props.modelValue,
    set: (valor: boolean) => emit("update:modelValue", valor),
});

const observacaoModel = computed({
    get: () => props.observacao,
    set: (valor: string) => emit("update:observacao", valor),
});
</script>

<template>
    <ModalConfirmacao
        v-model="mostrar"
        :auto-close="false"
        :loading="loading"
        :ok-title="TEXTOS.mapa.BOTAO_DEVOLVER"
        test-id-cancelar="btn-devolucao-mapa-cancelar"
        test-id-confirmar="btn-devolucao-mapa-confirmar"
        titulo="Devolver mapa"
        variant="danger"
        @confirmar="$emit('confirmar')"
    >
        <p>Confirma a devolução da validação do mapa para ajustes?</p>
        <BFormGroup :state="erro ? false : null" class="mb-3">
            <template #label>Observação: <span aria-hidden="true" class="text-danger">*</span></template>
            <EditorTextoRico
                v-model="observacaoModel"
                data-testid="inp-devolucao-mapa-obs"
                minimo-altura="10rem"
                rotulo="Observação da devolução"
            />
            <BFormInvalidFeedback :state="erro ? false : null">{{ erro }}</BFormInvalidFeedback>
        </BFormGroup>
    </ModalConfirmacao>
</template>
