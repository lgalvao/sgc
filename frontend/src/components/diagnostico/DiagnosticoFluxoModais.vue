<script lang="ts" setup>
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import ModalObservacaoAcao from "@/components/comum/ModalObservacaoAcao.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import {TEXTOS} from "@/constants/textos";
import type {Analise} from "@/types/tipos";

defineProps<{
    modalConcluirAberto?: boolean;
    erroConcluir?: string;
    concluindo?: boolean;
    tituloConcluir?: string;
    mensagemConcluir?: string;
    botaoConcluir?: string;
    modalValidarAberto?: boolean;
    observacoesValidar?: string;
    validando?: boolean;
    erroValidar?: string | null;
    modalDevolverAberto?: boolean;
    justificativaDevolver?: string;
    devolvendo?: boolean;
    erroDevolver?: string | null;
    feedbackJustificativaDevolver?: string | null;
    modalHomologarAberto?: boolean;
    observacoesHomologar?: string;
    homologando?: boolean;
    erroHomologar?: string | null;
    modalImpossibilitarAberto?: boolean;
    justificativaImpossibilidade?: string;
    impossibilitando?: boolean;
    erroImpossibilitar?: string | null;
    textoImpossibilitar?: string;
    feedbackJustificativaImpossibilidade?: string | null;
    modalPermitirAvaliacaoAberto?: boolean;
    permitindo?: boolean;
    textoPermitirAvaliacao?: string;
    modalHistoricoAberto?: boolean;
    carregandoHistorico?: boolean;
    historicoAnalises?: Analise[];
    testIdConfirmarConcluir?: string;
    testIdConfirmarValidar?: string;
    testIdConfirmarDevolver?: string;
    testIdConfirmarHomologar?: string;
    testIdConfirmarImpossibilitar?: string;
    testIdConfirmarPermitirAvaliacao?: string;
}>()

defineEmits<{
    (e: "update:modalConcluirAberto", valor: boolean): void;
    (e: "confirmarConcluir"): void;
    (e: "update:modalValidarAberto", valor: boolean): void;
    (e: "update:observacoesValidar", valor: string): void;
    (e: "confirmarValidar"): void;
    (e: "update:modalDevolverAberto", valor: boolean): void;
    (e: "update:justificativaDevolver", valor: string): void;
    (e: "confirmarDevolver"): void;
    (e: "update:modalHomologarAberto", valor: boolean): void;
    (e: "update:observacoesHomologar", valor: string): void;
    (e: "confirmarHomologar"): void;
    (e: "update:modalImpossibilitarAberto", valor: boolean): void;
    (e: "update:justificativaImpossibilidade", valor: string): void;
    (e: "confirmarImpossibilitar"): void;
    (e: "update:modalPermitirAvaliacaoAberto", valor: boolean): void;
    (e: "confirmarPermitirAvaliacao"): void;
    (e: "update:modalHistoricoAberto", valor: boolean): void;
}>();
</script>

<template>
    <ModalConfirmacao
        v-if="modalConcluirAberto !== undefined"
        :auto-close="false"
        data-testid="modal-concluir"
        :model-value="modalConcluirAberto"
        :loading="concluindo"
        :mensagem="mensagemConcluir || TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_MENSAGEM"
        :ok-title="botaoConcluir || TEXTOS.diagnostico.BTN_CONCLUIR_DIAGNOSTICO"
        :titulo="tituloConcluir || TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_TITULO"
        :test-id-confirmar="testIdConfirmarConcluir || 'btn-confirmar-concluir-diagnostico'"
        variant="success"
        @confirmar="$emit('confirmarConcluir')"
        @update:model-value="$emit('update:modalConcluirAberto', $event)"
    >
        <div>
            <p class="mb-0">{{ mensagemConcluir || TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_MENSAGEM }}</p>
            <div v-if="erroConcluir" class="text-danger small mt-3">{{ erroConcluir }}</div>
        </div>
    </ModalConfirmacao>

    <ModalObservacaoAcao
        v-if="modalValidarAberto !== undefined && observacoesValidar !== undefined"
        :data-testid="'modal-validar'"
        :erro="erroValidar"
        :loading="validando"
        :model-value="modalValidarAberto"
        :observacao="observacoesValidar"
        :placeholder="TEXTOS.diagnostico.LABEL_OBSERVACOES"
        :texto-acao="'Aceitar'"
        :titulo="TEXTOS.diagnostico.MODAL_VALIDAR_TITULO"
        input-data-testid="textarea-validar-diagnostico"
        label="Observações"
        :test-id-confirmar="testIdConfirmarValidar || 'btn-confirmar-validar-diagnostico'"
        variant-acao="success"
        @confirmar="$emit('confirmarValidar')"
        @update:model-value="$emit('update:modalValidarAberto', $event)"
        @update:observacao="$emit('update:observacoesValidar', $event)"
    />

    <ModalObservacaoAcao
        v-if="modalDevolverAberto !== undefined && justificativaDevolver !== undefined"
        :data-testid="'modal-devolver'"
        :erro="erroDevolver"
        :feedback-observacao="feedbackJustificativaDevolver"
        :loading="devolvendo"
        :model-value="modalDevolverAberto"
        :observacao="justificativaDevolver"
        :obrigatoria="true"
        :placeholder="TEXTOS.diagnostico.MODAL_DEVOLVER_PLACEHOLDER"
        :texto-acao="'Devolver'"
        :titulo="TEXTOS.diagnostico.MODAL_DEVOLVER_TITULO"
        input-data-testid="textarea-devolver-diagnostico"
        label="Justificativa"
        :test-id-confirmar="testIdConfirmarDevolver || 'btn-confirmar-devolver-diagnostico'"
        variant-acao="danger"
        @confirmar="$emit('confirmarDevolver')"
        @update:model-value="$emit('update:modalDevolverAberto', $event)"
        @update:observacao="$emit('update:justificativaDevolver', $event)"
    />

    <ModalObservacaoAcao
        v-if="modalHomologarAberto !== undefined && observacoesHomologar !== undefined"
        :data-testid="'modal-homologar'"
        :erro="erroHomologar"
        :loading="homologando"
        :model-value="modalHomologarAberto"
        :observacao="observacoesHomologar"
        :placeholder="TEXTOS.diagnostico.LABEL_OBSERVACOES"
        :texto-acao="'Homologar'"
        :titulo="TEXTOS.diagnostico.MODAL_HOMOLOGAR_TITULO"
        input-data-testid="textarea-homologar-diagnostico"
        label="Observações"
        :test-id-confirmar="testIdConfirmarHomologar || 'btn-confirmar-homologar-diagnostico'"
        variant-acao="primary"
        @confirmar="$emit('confirmarHomologar')"
        @update:model-value="$emit('update:modalHomologarAberto', $event)"
        @update:observacao="$emit('update:observacoesHomologar', $event)"
    />

    <ModalObservacaoAcao
        v-if="modalImpossibilitarAberto !== undefined && justificativaImpossibilidade !== undefined"
        :data-testid="'modal-impossibilitar'"
        :erro="erroImpossibilitar"
        :feedback-observacao="feedbackJustificativaImpossibilidade"
        :loading="impossibilitando"
        :model-value="modalImpossibilitarAberto"
        :observacao="justificativaImpossibilidade"
        :obrigatoria="true"
        :placeholder="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_PLACEHOLDER"
        :texto="textoImpossibilitar"
        :texto-acao="TEXTOS.diagnostico.BTN_IMPOSSIBILITAR"
        :titulo="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_TITULO"
        input-data-testid="textarea-justificativa-impossibilidade"
        label="Justificativa"
        :test-id-confirmar="testIdConfirmarImpossibilitar || 'btn-confirmar-impossibilitar-diagnostico'"
        variant-acao="danger"
        @confirmar="$emit('confirmarImpossibilitar')"
        @update:model-value="$emit('update:modalImpossibilitarAberto', $event)"
        @update:observacao="$emit('update:justificativaImpossibilidade', $event)"
    />

    <ModalConfirmacao
        v-if="modalPermitirAvaliacaoAberto !== undefined"
        :auto-close="false"
        data-testid="modal-permitir"
        :model-value="modalPermitirAvaliacaoAberto"
        :loading="permitindo"
        :mensagem="textoPermitirAvaliacao"
        :titulo="TEXTOS.diagnostico.MODAL_PERMITIR_AVALIACAO_TITULO"
        :test-id-confirmar="testIdConfirmarPermitirAvaliacao || 'btn-confirmar-permitir-avaliacao-diagnostico'"
        variant="success"
        @confirmar="$emit('confirmarPermitirAvaliacao')"
        @update:model-value="$emit('update:modalPermitirAvaliacaoAberto', $event)"
    />

    <HistoricoAnaliseModal
        v-if="modalHistoricoAberto !== undefined && historicoAnalises !== undefined"
        :historico="historicoAnalises"
        :loading="carregandoHistorico"
        :mostrar="modalHistoricoAberto"
        @fechar="$emit('update:modalHistoricoAberto', false)"
    />
</template>
