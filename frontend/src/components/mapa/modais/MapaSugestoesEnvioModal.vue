<script lang="ts" setup>
import {computed} from "vue";
import {BFormGroup, BFormInvalidFeedback} from "bootstrap-vue-next";
import EditorTextoRico from "@/components/comum/EditorTextoRico.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
    modelValue: boolean;
    loading: boolean;
    sugestoes: string;
    erro?: string;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", valor: boolean): void;
    (e: "update:sugestoes", valor: string): void;
    (e: "confirmar"): void;
}>();

const mostrar = computed({
    get: () => props.modelValue,
    set: (valor: boolean) => emit("update:modelValue", valor),
});

const sugestoesModel = computed({
    get: () => props.sugestoes,
    set: (valor: string) => emit("update:sugestoes", valor),
});
</script>

<template>
    <ModalConfirmacao
        v-model="mostrar"
        :auto-close="false"
        :loading="loading"
        :ok-title="TEXTOS.comum.BOTAO_APRESENTAR"
        test-id-cancelar="btn-sugestoes-mapa-cancelar"
        test-id-confirmar="btn-sugestoes-mapa-confirmar"
        titulo="Apresentar sugestões"
        variant="success"
        @confirmar="$emit('confirmar')"
    >
        <BFormGroup :state="erro ? false : null" class="mb-3">
            <template #label>
                Sugestões para o mapa de competências:
                <span aria-hidden="true" class="text-danger">*</span>
            </template>
            <EditorTextoRico
                v-model="sugestoesModel"
                aria-required="true"
                data-testid="inp-sugestoes-mapa-texto"
                minimo-altura="10rem"
                rotulo="Sugestões para o mapa de competências"
            />
            <BFormInvalidFeedback :state="erro ? false : null">{{ erro }}</BFormInvalidFeedback>
        </BFormGroup>
    </ModalConfirmacao>
</template>
