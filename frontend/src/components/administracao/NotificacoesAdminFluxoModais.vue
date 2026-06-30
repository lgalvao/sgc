<script lang="ts" setup>
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import {TEXTOS} from "@/constants/textos";
import type {Notificacao} from "@/services/notificacaoService";

defineProps<{
  itemParaDetalhes: Notificacao | null;
  itemParaPreview: Notificacao | null;
  itemSelecionado: Notificacao | null;
  mostrarDetalhes: boolean;
  mostrarModalReenvio: boolean;
  mostrarPreview: boolean;
  reenviando: boolean;
  formatarDataOuHifen: (valor?: string | null) => string;
  formatarTextoOuHifen: (valor?: string | null) => string;
  formatarTipoNotificacao: (tipo?: string) => string;
  montarPreviewHtml: (html?: string) => string;
  obterStatusNotificacao: (situacao?: string | null) => { label: string; variant?: string; prioridade?: number };
}>();

defineEmits<{
  (e: "confirmarReenvio"): void;
  (e: "update:mostrarDetalhes", valor: boolean): void;
  (e: "update:mostrarModalReenvio", valor: boolean): void;
  (e: "update:mostrarPreview", valor: boolean): void;
}>();
</script>

<template>
  <ModalPadrao
      :model-value="mostrarPreview"
      :mostrar-botao-acao="false"
      :test-id-cancelar="'btn-fechar-preview-email'"
      :texto-cancelar="'Fechar'"
      :titulo="itemParaPreview?.assunto ?? 'Preview do e-mail'"
      data-testid="modal-preview-email"
      tamanho="lg"
      @fechar="$emit('update:mostrarPreview', false)"
      @update:model-value="$emit('update:mostrarPreview', $event)"
  >
    <div v-if="itemParaPreview" class="p-3">
      <div class="mb-3 border-bottom pb-2">
        <strong>{{ TEXTOS.administracao.NOTIFICACOES_PREVIEW_DESTINATARIO }}:</strong> {{
          itemParaPreview.destinatario
        }}<br>
        <strong>{{ TEXTOS.administracao.NOTIFICACOES_PREVIEW_CRIACAO }}:</strong>
        {{ formatarDataOuHifen(itemParaPreview.dataHoraCriacao) }}
      </div>
      <iframe
          :srcdoc="montarPreviewHtml(itemParaPreview.corpoHtml)"
          class="email-content-preview"
          data-testid="iframe-preview-email"
          sandbox=""
          title="Preview do e-mail"
      />
    </div>
  </ModalPadrao>

  <ModalPadrao
      :model-value="mostrarDetalhes"
      :mostrar-botao-acao="false"
      data-testid="modal-detalhes-notificacao"
      tamanho="lg"
      texto-cancelar="Fechar"
      titulo="Detalhes da notificação"
      @fechar="$emit('update:mostrarDetalhes', false)"
      @update:model-value="$emit('update:mostrarDetalhes', $event)"
  >
    <div v-if="itemParaDetalhes" class="p-3">
      <dl class="row mb-0 detalhes-notificacao">
        <dt class="col-sm-4">Destinatário</dt>
        <dd class="col-sm-8">{{ itemParaDetalhes.destinatario }}</dd>
        <dt class="col-sm-4">Tipo</dt>
        <dd class="col-sm-8">{{ formatarTipoNotificacao(itemParaDetalhes.tipoNotificacao) }}</dd>
        <dt class="col-sm-4">Situação</dt>
        <dd class="col-sm-8">{{ obterStatusNotificacao(itemParaDetalhes.situacao).label }}</dd>
        <dt class="col-sm-4">Criado em</dt>
        <dd class="col-sm-8">{{ formatarDataOuHifen(itemParaDetalhes.dataHoraCriacao) }}</dd>
        <dt class="col-sm-4">Enviado em</dt>
        <dd class="col-sm-8">{{ formatarDataOuHifen(itemParaDetalhes.dataHoraEnvio) }}</dd>
        <dt class="col-sm-4">Próxima tentativa</dt>
        <dd class="col-sm-8">{{ formatarDataOuHifen(itemParaDetalhes.proximaTentativaEm) }}</dd>
        <dt class="col-sm-4">Falhas anteriores</dt>
        <dd class="col-sm-8">{{ itemParaDetalhes.tentativas }}</dd>
        <dt class="col-sm-4">Último erro</dt>
        <dd class="col-sm-8 text-break">{{ formatarTextoOuHifen(itemParaDetalhes.ultimoErro) }}</dd>
      </dl>
    </div>
  </ModalPadrao>

  <ModalConfirmacao
      :model-value="mostrarModalReenvio"
      :auto-close="false"
      :loading="reenviando"
      :ok-title="TEXTOS.administracao.NOTIFICACOES_REENVIAR"
      :titulo="TEXTOS.administracao.NOTIFICACOES_MODAL_REENVIAR_TITULO"
      test-id-confirmar="btn-notificacoes-reenviar-confirmar"
      variant="danger"
      @confirmar="$emit('confirmarReenvio')"
      @update:model-value="$emit('update:mostrarModalReenvio', $event)"
  >
    <p v-if="itemSelecionado" data-testid="txt-notificacoes-reenviar-confirmacao">
      Confirma o reenvio deste e-mail específico para {{ itemSelecionado.destinatario }}?
    </p>
  </ModalConfirmacao>
</template>
