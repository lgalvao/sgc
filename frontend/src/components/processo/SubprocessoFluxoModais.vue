<script lang="ts" setup>
import {BFormInvalidFeedback} from "bootstrap-vue-next";
import EditorTextoRico from "@/components/comum/EditorTextoRico.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import SubprocessoModal from "@/components/processo/SubprocessoModal.vue";
import {TEXTOS} from "@/constants/textos";
import type {Analise} from "@/types/tipos";

defineProps<{
  dataLimiteAtual: Date | null;
  dataFimEtapa1: Date | null;
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
  modalConcluirDiagnosticoAberto?: boolean;
  concluindoDiagnostico?: boolean;
  erroConcluirDiagnostico?: string;
  modalHistoricoDiagnosticoAberto?: boolean;
  carregandoHistoricoDiagnostico?: boolean;
  historicoAnalisesDiagnostico?: Analise[];
}>();

defineEmits<{
  (e: "fechar-modal-data"): void;
  (e: "confirmar-alteracao-data", novaData: string): void;
  (e: "update:mostrarModalReabrir", valor: boolean): void;
  (e: "confirmar-reabertura"): void;
  (e: "update:justificativaReabertura", valor: string): void;
  (e: "update:modalLembreteAberto", valor: boolean): void;
  (e: "confirmar-enviar-lembrete"): void;
  (e: "update:modalConcluirDiagnosticoAberto", valor: boolean): void;
  (e: "confirmar-concluir-diagnostico"): void;
  (e: "update:modalHistoricoDiagnosticoAberto", valor: boolean): void;
}>();
</script>

<template>
  <SubprocessoModal
      :data-limite-atual="dataLimiteAtual"
      :data-fim-etapa-anterior="dataFimEtapa1"
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

  <ModalConfirmacao
      v-if="modalConcluirDiagnosticoAberto !== undefined"
      :model-value="modalConcluirDiagnosticoAberto"
      :loading="concluindoDiagnostico"
      :mensagem="TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_MENSAGEM"
      :titulo="TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_TITULO"
      ok-title="Concluir diagnóstico"
      test-id-confirmar="btn-confirmar-concluir-diagnostico-cabecalho"
      variant="success"
      @confirmar="$emit('confirmar-concluir-diagnostico')"
      @update:model-value="$emit('update:modalConcluirDiagnosticoAberto', $event)"
  >
    <div>
      <p class="mb-0">{{ TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_MENSAGEM }}</p>
      <div v-if="erroConcluirDiagnostico" class="text-danger small mt-3">{{ erroConcluirDiagnostico }}</div>
    </div>
  </ModalConfirmacao>

  <HistoricoAnaliseModal
      v-if="modalHistoricoDiagnosticoAberto !== undefined && historicoAnalisesDiagnostico !== undefined"
      :historico="historicoAnalisesDiagnostico"
      :loading="carregandoHistoricoDiagnostico"
      :mostrar="modalHistoricoDiagnosticoAberto"
      @fechar="$emit('update:modalHistoricoDiagnosticoAberto', false)"
  />
</template>
