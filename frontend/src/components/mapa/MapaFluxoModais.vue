<script setup lang="ts">
import type {Analise, Atividade, Competencia, ImpactoMapa} from "@/types/tipos";
import MapaEdicaoModais from "@/components/mapa/MapaEdicaoModais.vue";
import MapaAnaliseModais from "@/components/mapa/MapaAnaliseModais.vue";
import MapaSugestoesModais from "@/components/mapa/MapaSugestoesModais.vue";

interface Props {
  modoSomenteLeitura: boolean;
  atividades: Atividade[];
  competenciaSendoEditada: Competencia | null;
  fieldErrors: Record<string, string | undefined>;
  loadingCompetencia: boolean;
  mostrarModalCriarNovaCompetencia: boolean;
  loadingDisponibilizacao: boolean;
  mostrarModalDisponibilizar: boolean;
  notificacaoDisponibilizacao: string;
  ultimaDataLimiteSubprocesso?: string | null;
  mostrarModalExcluirCompetencia: boolean;
  loadingExclusao: boolean;
  competenciaParaExcluir: Competencia | null;
  carregandoFluxoMapa: boolean;
  homologacao: boolean;
  mostrarModalAceitar: boolean;
  mostrarModalVerSugestoes: boolean;
  isChefe: boolean;
  sugestoesVisualizacao: string;
  mostrarModalSugestoes: boolean;
  loadingSugestoesEnvio: boolean;
  mensagemErroSugestoes: string;
  sugestoes: string;
  mostrarModalValidar: boolean;
  mostrarModalDevolucao: boolean;
  mensagemErroDevolucao: string;
  observacaoDevolucao: string;
  codigoSubprocesso?: number | null;
  impactos?: ImpactoMapa | null;
  loadingImpacto: boolean;
  mostrarModalImpacto: boolean;
  historicoAnalise: Analise[];
  mostrarModalHistorico: boolean;
}

defineProps<Props>();

defineEmits<{
  (e: "fechar-criar-competencia"): void;
  (e: "salvar-competencia", valor: { descricao: string; atividadesSelecionadas: number[] }): void;
  (e: "fechar-disponibilizar"): void;
  (e: "disponibilizar", valor: { dataLimite: string; observacoes: string }): void;
  (e: "update:mostrarModalExcluirCompetencia", valor: boolean): void;
  (e: "confirmar-exclusao-competencia"): void;
  (e: "fechar-aceite"): void;
  (e: "confirmar-aceitacao", valor: string): void;
  (e: "update:mostrarModalVerSugestoes", valor: boolean): void;
  (e: "fechar-ver-sugestoes"): void;
  (e: "update:sugestoesVisualizacao", valor: string): void;
  (e: "update:mostrarModalSugestoes", valor: boolean): void;
  (e: "confirmar-sugestoes"): void;
  (e: "update:sugestoes", valor: string): void;
  (e: "update:mostrarModalValidar", valor: boolean): void;
  (e: "confirmar-validacao"): void;
  (e: "update:mostrarModalDevolucao", valor: boolean): void;
  (e: "confirmar-devolucao"): void;
  (e: "update:observacaoDevolucao", valor: string): void;
  (e: "fechar-impacto"): void;
  (e: "fechar-historico"): void;
}>();
</script>

<template>
  <MapaEdicaoModais
      :atividades="atividades"
      :competencia-para-excluir="competenciaParaExcluir"
      :competencia-sendo-editada="competenciaSendoEditada"
      :field-errors="fieldErrors"
      :loading-competencia="loadingCompetencia"
      :loading-disponibilizacao="loadingDisponibilizacao"
      :loading-exclusao="loadingExclusao"
      :modo-somente-leitura="modoSomenteLeitura"
      :mostrar-modal-criar-nova-competencia="mostrarModalCriarNovaCompetencia"
      :mostrar-modal-disponibilizar="mostrarModalDisponibilizar"
      :mostrar-modal-excluir-competencia="mostrarModalExcluirCompetencia"
      :notificacao-disponibilizacao="notificacaoDisponibilizacao"
      :ultima-data-limite-subprocesso="ultimaDataLimiteSubprocesso"
      @confirmar-exclusao-competencia="$emit('confirmar-exclusao-competencia')"
      @disponibilizar="$emit('disponibilizar', $event)"
      @fechar-criar-competencia="$emit('fechar-criar-competencia')"
      @fechar-disponibilizar="$emit('fechar-disponibilizar')"
      @salvar-competencia="$emit('salvar-competencia', $event)"
      @update:mostrarModalExcluirCompetencia="$emit('update:mostrarModalExcluirCompetencia', $event)"
  />

  <MapaAnaliseModais
      :carregando-fluxo-mapa="carregandoFluxoMapa"
      :codigo-subprocesso="codigoSubprocesso"
      :historico-analise="historicoAnalise"
      :homologacao="homologacao"
      :impactos="impactos"
      :loading-impacto="loadingImpacto"
      :mensagem-erro-devolucao="mensagemErroDevolucao"
      :mostrar-modal-aceitar="mostrarModalAceitar"
      :mostrar-modal-devolucao="mostrarModalDevolucao"
      :mostrar-modal-historico="mostrarModalHistorico"
      :mostrar-modal-impacto="mostrarModalImpacto"
      :mostrar-modal-validar="mostrarModalValidar"
      :observacao-devolucao="observacaoDevolucao"
      @confirmar-aceitacao="$emit('confirmar-aceitacao', $event)"
      @confirmar-devolucao="$emit('confirmar-devolucao')"
      @confirmar-validacao="$emit('confirmar-validacao')"
      @fechar-aceite="$emit('fechar-aceite')"
      @fechar-historico="$emit('fechar-historico')"
      @fechar-impacto="$emit('fechar-impacto')"
      @update:mostrarModalDevolucao="$emit('update:mostrarModalDevolucao', $event)"
      @update:mostrarModalValidar="$emit('update:mostrarModalValidar', $event)"
      @update:observacaoDevolucao="$emit('update:observacaoDevolucao', $event)"
  />

  <MapaSugestoesModais
      :is-chefe="isChefe"
      :loading-sugestoes-envio="loadingSugestoesEnvio"
      :mensagem-erro-sugestoes="mensagemErroSugestoes"
      :mostrar-modal-sugestoes="mostrarModalSugestoes"
      :mostrar-modal-ver-sugestoes="mostrarModalVerSugestoes"
      :sugestoes="sugestoes"
      :sugestoes-visualizacao="sugestoesVisualizacao"
      @confirmar-sugestoes="$emit('confirmar-sugestoes')"
      @fechar-ver-sugestoes="$emit('fechar-ver-sugestoes')"
      @update:mostrarModalSugestoes="$emit('update:mostrarModalSugestoes', $event)"
      @update:mostrarModalVerSugestoes="$emit('update:mostrarModalVerSugestoes', $event)"
      @update:sugestoes="$emit('update:sugestoes', $event)"
      @update:sugestoesVisualizacao="$emit('update:sugestoesVisualizacao', $event)"
  />
</template>
