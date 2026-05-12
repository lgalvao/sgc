<script lang="ts" setup>
import {BFormInvalidFeedback} from "bootstrap-vue-next";
import EditorTextoRico from "@/components/comum/EditorTextoRico.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import SubprocessoModal from "@/components/processo/SubprocessoModal.vue";
import {TEXTOS} from "@/constants/textos";

defineProps<{
    dataLimiteAtual: Date | null;
    etapaAtual: number | null;
    loadingDataLimite: boolean;
    mostrarModalAlterarDataLimite: boolean;
    ultimaDataLimiteSubprocesso: Date | null;
    mostrarModalReabrir: boolean;
    loadingReabertura: boolean;
    tipoReabertura: "cadastro" | "revisao";
    justificativaReabertura: string;
    mensagemErroJustificativa: string;
    modalLembreteAberto: boolean;
    loadingLembrete: boolean;
    siglaUnidade: string;
}>();

const emit = defineEmits<{
    (e: "fechar-modal-data"): void;
    (e: "confirmar-alteracao-data", novaData: string): void;
    (e: "update:mostrarModalReabrir", valor: boolean): void;
    (e: "confirmar-reabertura"): void;
    (e: "update:justificativaReabertura", valor: string): void;
    (e: "update:modalLembreteAberto", valor: boolean): void;
    (e: "confirmar-enviar-lembrete"): void;
}>();
</script>

<template>
    <SubprocessoModal
        :data-limite-atual="dataLimiteAtual"
        :etapa-atual="etapaAtual"
        :loading="loadingDataLimite"
        :mostrar-modal="mostrarModalAlterarDataLimite"
        :ultima-data-limite-subprocesso="ultimaDataLimiteSubprocesso"
        @fechar-modal="$emit('fechar-modal-data')"
        @confirmar-alteracao="$emit('confirmar-alteracao-data', $event)"
    />

    <ModalConfirmacao
        :auto-close="false"
        :loading="loadingReabertura"
        :model-value="mostrarModalReabrir"
        :ok-title="TEXTOS.comum.BOTAO_REABRIR"
        :titulo="tipoReabertura === 'cadastro' ? TEXTOS.subprocesso.REABRIR_CADASTRO_TITULO : TEXTOS.subprocesso.REABRIR_REVISAO_TITULO"
        test-id-confirmar="btn-confirmar-reabrir"
        variant="success"
        @confirmar="$emit('confirmar-reabertura')"
        @update:model-value="$emit('update:mostrarModalReabrir', $event)"
    >
        <p>
            {{ TEXTOS.subprocesso.REABRIR_JUSTIFICATIVA_PREFIXO }}
            {{ tipoReabertura === "cadastro" ? TEXTOS.subprocesso.CADASTRO : TEXTOS.subprocesso.REVISAO_CADASTRO }}
            <span aria-hidden="true" class="text-danger">*</span>:
        </p>
        <EditorTextoRico
            :model-value="justificativaReabertura"
            :rotulo="TEXTOS.subprocesso.REABRIR_JUSTIFICATIVA_PLACEHOLDER"
            data-testid="inp-justificativa-reabrir"
            minimo-altura="10rem"
            @update:model-value="$emit('update:justificativaReabertura', $event)"
        />
        <BFormInvalidFeedback
            :state="mensagemErroJustificativa ? false : null"
            class="d-block"
            data-testid="txt-reabertura-pendencia-justificativa"
        >
            {{ mensagemErroJustificativa }}
        </BFormInvalidFeedback>
    </ModalConfirmacao>

    <ModalConfirmacao
        :auto-close="false"
        :loading="loadingLembrete"
        :model-value="modalLembreteAberto"
        :ok-title="TEXTOS.subprocesso.BOTAO_CONFIRMAR_LEMBRETE"
        :titulo="TEXTOS.subprocesso.LEMBRETE_TITULO"
        test-id-confirmar="btn-confirmar-enviar-lembrete"
        variant="success"
        @confirmar="$emit('confirmar-enviar-lembrete')"
        @update:model-value="$emit('update:modalLembreteAberto', $event)"
    >
        <p data-testid="txt-modelo-lembrete">
            {{ TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(siglaUnidade) }}
        </p>
    </ModalConfirmacao>
</template>
