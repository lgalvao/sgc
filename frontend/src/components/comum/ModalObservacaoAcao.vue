<script lang="ts" setup>
import {computed} from "vue";
import {BFormGroup, BFormText, BFormTextarea} from "bootstrap-vue-next";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import AppAlert from "@/components/comum/AppAlert.vue";

const props = withDefaults(defineProps<{
    modelValue: boolean;
    titulo: string;
    texto?: string;
    observacao: string;
    label: string;
    placeholder?: string;
    loading?: boolean;
    erro?: string | null;
    feedbackObservacao?: string | null;
    obrigatoria?: boolean;
    variantAcao?: "primary" | "secondary" | "success" | "danger";
    textoAcao?: string;
    textoAcaoCarregando?: string;
    testIdConfirmar?: string;
    testIdCancelar?: string;
    dataTestid?: string;
    inputDataTestid?: string;
    inputId?: string;
    linhas?: number;
}>(), {
    texto: "",
    loading: false,
    erro: null,
    feedbackObservacao: null,
    obrigatoria: false,
    variantAcao: "success",
    textoAcao: "Confirmar",
    textoAcaoCarregando: "Processando...",
    testIdConfirmar: "",
    testIdCancelar: "",
    dataTestid: "",
    inputDataTestid: "",
    inputId: "",
    placeholder: "",
    linhas: 3,
});

const emit = defineEmits<{
    (e: "update:modelValue", valor: boolean): void;
    (e: "update:observacao", valor: string): void;
    (e: "confirmar"): void;
    (e: "fechar"): void;
}>();

const model = computed({
    get: () => props.modelValue,
    set: (valor: boolean) => emit("update:modelValue", valor),
});

const observacaoModel = computed({
    get: () => props.observacao,
    set: (valor: string) => emit("update:observacao", valor),
});
</script>

<template>
    <ModalPadrao
        v-model="model"
        :loading="loading"
        :data-testid="dataTestid || undefined"
        :test-id-cancelar="testIdCancelar"
        :test-id-confirmar="testIdConfirmar"
        :texto-acao="textoAcao"
        :texto-acao-carregando="textoAcaoCarregando"
        :titulo="titulo"
        :variant-acao="variantAcao"
        @confirmar="$emit('confirmar')"
        @fechar="$emit('fechar')"
    >
        <AppAlert v-if="erro" :mensagem="erro" class="mb-3" variante="danger"/>
        <p v-if="texto" class="mb-3">{{ texto }}</p>
        <BFormGroup class="mb-0">
            <template v-if="obrigatoria" #label>
                {{ label }} <span aria-hidden="true" class="text-danger">*</span>
            </template>
            <template v-else #label>{{ label }}</template>
            <BFormTextarea
                :id="inputId || undefined"
                v-model="observacaoModel"
                :data-testid="inputDataTestid || undefined"
                :placeholder="placeholder"
                :rows="linhas"
            />
            <BFormText v-if="feedbackObservacao" class="text-danger">
                {{ feedbackObservacao }}
            </BFormText>
        </BFormGroup>
    </ModalPadrao>
</template>
