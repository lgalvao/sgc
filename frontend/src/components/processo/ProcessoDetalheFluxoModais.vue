<script lang="ts" setup>
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import ModalAcaoBloco from "@/components/processo/ModalAcaoBloco.vue";
import {TEXTOS} from "@/constants/textos";
import {TipoProcesso} from "@/types/comum";
import type {UnidadeSelecao} from "@/types/subprocesso-modelos";
import type {DadosAcaoBloco} from "@/views/processoDetalheTipos";

defineProps<{
  descricaoProcesso: string;
  idsElegiveis: number[];
  loadingFinalizacao: boolean;
  registrarModalBlocoRef: (instancia: unknown) => void;
  mostrarDataLimite?: boolean;
  mostrarModalFinalizacao: boolean;
  tipoProcesso: TipoProcesso;
  processoAcaoRotuloBotao?: string;
  processoAcaoTexto?: string;
  processoAcaoTitulo?: string;
  unidadesElegiveis: UnidadeSelecao[];
}>();

defineEmits<{
  (e: "confirmarFinalizacao"): void;
  (e: "executarAcaoBloco", dados: DadosAcaoBloco): void;
  (e: "update:mostrarModalFinalizacao", valor: boolean): void;
}>();
</script>

<template>
  <ModalAcaoBloco
      :id="'modal-acao-bloco'"
      :ref="registrarModalBlocoRef"
      :mostrar-data-limite="mostrarDataLimite"
      :rotulo-botao="processoAcaoRotuloBotao"
      :texto="processoAcaoTexto"
      :titulo="processoAcaoTitulo"
      :unidades="unidadesElegiveis"
      :unidades-pre-selecionadas="idsElegiveis"
      @confirmar="$emit('executarAcaoBloco', $event)"
  />

  <ModalConfirmacao
      :model-value="mostrarModalFinalizacao"
      :loading="loadingFinalizacao"
      :ok-title="TEXTOS.comum.BOTAO_FINALIZAR"
      :titulo="TEXTOS.processo.FINALIZACAO_TITULO"
      test-id-cancelar="btn-finalizar-processo-cancelar"
      test-id-confirmar="btn-finalizar-processo-confirmar"
      variant="danger"
      @confirmar="$emit('confirmarFinalizacao')"
      @update:model-value="$emit('update:mostrarModalFinalizacao', $event)"
  >
    <p class="mb-2">
      {{ TEXTOS.processo.FINALIZACAO_CONFIRMACAO_PREFIXO }}
      <strong>{{ descricaoProcesso }}</strong>?
    </p>
    <p class="mb-0">{{ TEXTOS.processo.FINALIZACAO_CONFIRMACAO_COMPLEMENTO(tipoProcesso) }}</p>
  </ModalConfirmacao>
</template>
