<script lang="ts" setup>
import {computed} from "vue";
import {BFormGroup, BFormInvalidFeedback} from "bootstrap-vue-next";
import EditorTextoRico from "@/components/comum/EditorTextoRico.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import AppAlert from "@/components/comum/AppAlert.vue";

interface Props {
    modelValue: boolean;
    loading: boolean;
    titulo?: string;
    okTitle?: string;
    texto?: string;
    observacao: string;
    testIdConfirmar: string;
    inputId: string;
    inputDataTestid: string;
    label: string;
    variant?: "success" | "danger" | "primary";
    erro?: string | null;
    labelObrigatoria?: boolean;
    estadoObservacao?: boolean | null;
    feedbackObservacao?: string | null;
}

const props = withDefaults(defineProps<Props>(), {
    titulo: "",
    okTitle: "",
    texto: "",
    variant: "primary",
    erro: null,
    labelObrigatoria: false,
    estadoObservacao: null,
    feedbackObservacao: null,
});

const emit = defineEmits<{
    (e: "update:modelValue", value: boolean): void;
    (e: "update:observacao", value: string): void;
    (e: "confirmar"): void;
}>();

const model = computed({
    get: () => props.modelValue,
    set: (valor) => emit("update:modelValue", valor),
});

const observacaoModel = computed({
    get: () => props.observacao,
    set: (valor) => emit("update:observacao", valor),
});
</script>

<template>
    <ModalConfirmacao
        v-model="model"
        :auto-close="false"
        :loading="loading"
        :ok-title="okTitle"
        :test-id-confirmar="testIdConfirmar"
        :titulo="titulo"
        :variant="variant"
        @confirmar="$emit('confirmar')"
    >
        <AppAlert v-if="erro" :mensagem="erro" class="mb-3" variante="danger"/>
        <p>{{ texto }}</p>
        <BFormGroup class="mb-3">
            <template v-if="labelObrigatoria" #label>
                {{ label }} <span aria-hidden="true" class="text-danger">*</span>
            </template>
            <template v-else #label>{{ label }}</template>
            <EditorTextoRico
                :id="inputId"
                v-model="observacaoModel"
                :data-testid="inputDataTestid"
                minimo-altura="10rem"
                :rotulo="label"
            />
            <BFormInvalidFeedback v-if="feedbackObservacao" :state="estadoObservacao">
                {{ feedbackObservacao }}
            </BFormInvalidFeedback>
        </BFormGroup>
    </ModalConfirmacao>
</template>
