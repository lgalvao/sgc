<script lang="ts" setup>
import {computed} from "vue";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/atividades/ImportarAtividadesModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/mapa/ConfirmacaoDisponibilizacaoModal.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import ModalAceiteCadastro from "@/components/cadastro/ModalAceiteCadastro.vue";
import ModalDevolucaoCadastro from "@/components/cadastro/ModalDevolucaoCadastro.vue";
import type {Analise, AtividadeOperacaoResponse, ImpactoMapa,} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

interface DadosRemocao {
  tipo: "atividade" | "conhecimento";
}

interface Props {
  codigoSubprocesso?: number | null;
  mostrarModalImportar: boolean;
  impactos?: ImpactoMapa | null;
  loadingImpacto: boolean;
  mostrarModalImpacto: boolean;
  isRevisao: boolean;
  loadingDisponibilizacao: boolean;
  mostrarModalConfirmacao: boolean;
  erroFluxo?: string;
  historicoAnalises: Analise[];
  mostrarModalHistorico: boolean;
  mostrarModalConfirmacaoRemocao: boolean;
  loadingRemocao: boolean;
  dadosRemocao: DadosRemocao | null;
  mostrarModalValidarAnalise: boolean;
  observacaoValidacao: string;
  loadingAnaliseCadastro: boolean;
  acaoPrincipalCadastro: {
    tituloModal: string;
    rotuloConfirmacao: string;
    textoModal: string;
  } | null;
  mostrarModalDevolverAnalise: boolean;
  observacaoDevolucao: string;
  loadingDevolucaoAnalise: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: "update:mostrarModalImportar", valor: boolean): void;
  (e: "importar", valor: AtividadeOperacaoResponse): void;
  (e: "fechar-impacto"): void;
  (e: "confirmar-disponibilizacao"): void;
  (e: "update:mostrarModalConfirmacao", valor: boolean): void;
  (e: "update:mostrarModalHistorico", valor: boolean): void;
  (e: "update:mostrarModalConfirmacaoRemocao", valor: boolean): void;
  (e: "confirmar-remocao"): void;
  (e: "update:mostrarModalValidarAnalise", valor: boolean): void;
  (e: "update:observacaoValidacao", valor: string): void;
  (e: "confirmar-validacao-analise"): void;
  (e: "update:mostrarModalDevolverAnalise", valor: boolean): void;
  (e: "update:observacaoDevolucao", valor: string): void;
  (e: "confirmar-devolucao-analise"): void;
}>();

const mostrarModalImportarModel = computed({
  get: () => props.mostrarModalImportar,
  set: (valor: boolean) => emit("update:mostrarModalImportar", valor),
});

const mostrarModalConfirmacaoModel = computed({
  get: () => props.mostrarModalConfirmacao,
  set: (valor: boolean) => emit("update:mostrarModalConfirmacao", valor),
});

const mostrarModalHistoricoModel = computed({
  get: () => props.mostrarModalHistorico,
  set: (valor: boolean) => emit("update:mostrarModalHistorico", valor),
});

const mostrarModalConfirmacaoRemocaoModel = computed({
  get: () => props.mostrarModalConfirmacaoRemocao,
  set: (valor: boolean) => emit("update:mostrarModalConfirmacaoRemocao", valor),
});

const mostrarModalValidarAnaliseModel = computed({
  get: () => props.mostrarModalValidarAnalise,
  set: (valor: boolean) => emit("update:mostrarModalValidarAnalise", valor),
});

const mostrarModalDevolverAnaliseModel = computed({
  get: () => props.mostrarModalDevolverAnalise,
  set: (valor: boolean) => emit("update:mostrarModalDevolverAnalise", valor),
});

const observacaoValidacaoModel = computed({
  get: () => props.observacaoValidacao,
  set: (valor: string) => emit("update:observacaoValidacao", valor),
});

const observacaoDevolucaoModel = computed({
  get: () => props.observacaoDevolucao,
  set: (valor: string) => emit("update:observacaoDevolucao", valor),
});
</script>

<template>
  <ImportarAtividadesModal
      :cod-subprocesso-destino="codigoSubprocesso"
      :mostrar="mostrarModalImportarModel"
      @fechar="mostrarModalImportarModel = false"
      @importar="$emit('importar', $event)"
  />

  <ImpactoMapaModal
      v-if="codigoSubprocesso"
      :impacto="impactos"
      :loading="loadingImpacto"
      :mostrar="mostrarModalImpacto"
      @fechar="$emit('fechar-impacto')"
  />

  <ConfirmacaoDisponibilizacaoModal
      :erro="erroFluxo"
      :is-revisao="isRevisao"
      :loading="loadingDisponibilizacao"
      :mostrar="mostrarModalConfirmacaoModel"
      @confirmar="$emit('confirmar-disponibilizacao')"
      @fechar="mostrarModalConfirmacaoModel = false"
  />

  <HistoricoAnaliseModal
      :historico="historicoAnalises"
      :mostrar="mostrarModalHistoricoModel"
      @fechar="mostrarModalHistoricoModel = false"
  />

  <ModalConfirmacao
      v-model="mostrarModalConfirmacaoRemocaoModel"
      :loading="loadingRemocao"
      :mensagem="dadosRemocao?.tipo === 'atividade' ? TEXTOS.atividades.MODAL_REMOVER_ATIVIDADE_TEXTO : TEXTOS.atividades.MODAL_REMOVER_CONHECIMENTO_TEXTO"
      :ok-title="TEXTOS.comum.BOTAO_REMOVER"
      :titulo="dadosRemocao?.tipo === 'atividade' ? TEXTOS.atividades.MODAL_REMOVER_ATIVIDADE_TITULO : TEXTOS.atividades.MODAL_REMOVER_CONHECIMENTO_TITULO"
      variant="danger"
      @confirmar="$emit('confirmar-remocao')"
  />

  <ModalAceiteCadastro
      v-model="mostrarModalValidarAnaliseModel"
      v-model:observacao="observacaoValidacaoModel"
      :acao="acaoPrincipalCadastro"
      :erro="erroFluxo"
      :loading="loadingAnaliseCadastro"
      @confirmar="$emit('confirmar-validacao-analise')"
  />

  <ModalDevolucaoCadastro
      v-model="mostrarModalDevolverAnaliseModel"
      v-model:observacao="observacaoDevolucaoModel"
      :erro="erroFluxo"
      :is-revisao="isRevisao"
      :loading="loadingDevolucaoAnalise"
      @confirmar="$emit('confirmar-devolucao-analise')"
  />
</template>
