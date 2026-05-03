<script setup lang="ts">
import {BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
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

defineEmits<{
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
      :model-value="mostrarModalReabrir"
      :auto-close="false"
      :loading="loadingReabertura"
      :titulo="tipoReabertura === 'cadastro' ? TEXTOS.subprocesso.REABRIR_CADASTRO_TITULO : TEXTOS.subprocesso.REABRIR_REVISAO_TITULO"
      :ok-title="TEXTOS.comum.BOTAO_REABRIR"
      test-codigo-confirmar="btn-confirmar-reabrir"
      variant="success"
      @update:modelValue="$emit('update:mostrarModalReabrir', $event)"
      @confirmar="$emit('confirmar-reabertura')"
  >
    <p>{{ TEXTOS.subprocesso.REABRIR_JUSTIFICATIVA_PREFIXO }} {{
        tipoReabertura === 'cadastro' ? TEXTOS.subprocesso.CADASTRO : TEXTOS.subprocesso.REVISAO_CADASTRO
      }} <span aria-hidden="true" class="text-danger">*</span>:</p>
    <BFormTextarea
        id="justificativaReabertura"
        :model-value="justificativaReabertura"
        :state="mensagemErroJustificativa ? false : null"
        data-testid="inp-justificativa-reabrir"
        :placeholder="TEXTOS.subprocesso.REABRIR_JUSTIFICATIVA_PLACEHOLDER"
        rows="3"
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
      :model-value="modalLembreteAberto"
      :auto-close="false"
      :loading="loadingLembrete"
      :ok-title="TEXTOS.subprocesso.BOTAO_CONFIRMAR_LEMBRETE"
      test-codigo-confirmar="btn-confirmar-enviar-lembrete"
      :titulo="TEXTOS.subprocesso.LEMBRETE_TITULO"
      variant="success"
      @update:modelValue="$emit('update:modalLembreteAberto', $event)"
      @confirmar="$emit('confirmar-enviar-lembrete')"
  >
    <p data-testid="txt-modelo-lembrete">
      {{ TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(siglaUnidade) }}
    </p>
  </ModalConfirmacao>
</template>
