<script lang="ts" setup>
import {defineAsyncComponent} from "vue";
import type {Analise, Atividade, Competencia, ImpactoMapa} from "@/types/tipos";
import CompetenciaEdicaoModal from "./CompetenciaEdicaoModal.vue";
import MapaDisponibilizacaoModal from "./MapaDisponibilizacaoModal.vue";
import CompetenciaExclusaoModal from "./CompetenciaExclusaoModal.vue";
import MapaAceitacaoModal from "./MapaAceitacaoModal.vue";
import MapaValidacaoModal from "./MapaValidacaoModal.vue";
import MapaDevolucaoModal from "./MapaDevolucaoModal.vue";
import MapaSugestoesEnvioModal from "./MapaSugestoesEnvioModal.vue";
import MapaSugestoesVisualizacaoModal from "./MapaSugestoesVisualizacaoModal.vue";

const ImpactoMapaModal = defineAsyncComponent(() => import("@/components/mapa/ImpactoMapaModal.vue"));
const HistoricoAnaliseModal = defineAsyncComponent(() => import("@/components/processo/HistoricoAnaliseModal.vue"));

defineProps<{
  modoSomenteLeitura: boolean;
  atividades: Atividade[];
  codigoSubprocesso?: number | null;
  mostrarModalCriarNovaCompetencia: boolean;
  competenciaSendoEditada: Competencia | null;
  loadingCompetencia: boolean;
  fieldErrors: Record<string, string | undefined>;
  mostrarModalDisponibilizar: boolean;
  loadingDisponibilizacao: boolean;
  notificacaoDisponibilizacao: string;
  dataFimEtapa1?: string | null;
  ultimaDataLimiteSubprocesso?: string | null;
  mostrarModalExcluirCompetencia: boolean;
  loadingExclusao: boolean;
  competenciaParaExcluir: Competencia | null;
  carregandoFluxoMapa: boolean;
  homologacao: boolean;
  mostrarModalAceitar: boolean;
  mostrarModalValidar: boolean;
  mostrarModalDevolucao: boolean;
  mensagemErroDevolucao: string;
  observacaoDevolucao: string;
  mostrarModalSugestoes: boolean;
  loadingSugestoesEnvio: boolean;
  mensagemErroSugestoes: string;
  sugestoes: string;
  mostrarModalVerSugestoes: boolean;
  podeApresentarSugestoes: boolean;
  sugestoesVisualizacao: string;
  mostrarModalImpacto: boolean;
  loadingImpacto: boolean;
  impactos?: ImpactoMapa | null;
  mostrarModalHistorico: boolean;
  historicoAnalise: Analise[];
}>();

defineEmits<{
  (e: "fechar-criar-competencia"): void;
  (e: "salvar-competencia", v: { descricao: string; atividadesSelecionadas: number[] }): void;
  (e: "fechar-disponibilizar"): void;
  (e: "disponibilizar", v: { dataLimite: string; observacoes: string }): void;
  (e: "update:mostrarModalExcluirCompetencia", v: boolean): void;
  (e: "confirmar-exclusao-competencia"): void;
  (e: "fechar-aceite"): void;
  (e: "confirmar-aceitacao", v: string): void;
  (e: "update:mostrarModalValidar", v: boolean): void;
  (e: "confirmar-validacao"): void;
  (e: "update:mostrarModalDevolucao", v: boolean): void;
  (e: "confirmar-devolucao"): void;
  (e: "update:observacaoDevolucao", v: string): void;
  (e: "update:mostrarModalSugestoes", v: boolean): void;
  (e: "confirmar-sugestoes"): void;
  (e: "update:sugestoes", v: string): void;
  (e: "update:mostrarModalVerSugestoes", v: boolean): void;
  (e: "fechar-ver-sugestoes"): void;
  (e: "update:sugestoesVisualizacao", v: string): void;
  (e: "fechar-impacto"): void;
  (e: "fechar-historico"): void;
}>();
</script>

<template>
  <template v-if="!modoSomenteLeitura">
    <CompetenciaEdicaoModal
        :atividades="atividades" :competencia-para-editar="competenciaSendoEditada"
        :field-errors="fieldErrors" :loading="loadingCompetencia"
        :mostrar="mostrarModalCriarNovaCompetencia" @fechar="$emit('fechar-criar-competencia')"
        @salvar="$emit('salvar-competencia', $event)"/>
    <MapaDisponibilizacaoModal
        :field-errors="fieldErrors" :loading="loadingDisponibilizacao"
        :mostrar="mostrarModalDisponibilizar" :notificacao="notificacaoDisponibilizacao"
        :data-fim-etapa-anterior="dataFimEtapa1 ?? undefined"
        :ultima-data-limite-subprocesso="ultimaDataLimiteSubprocesso ?? undefined"
        @disponibilizar="$emit('disponibilizar', $event)"
        @fechar="$emit('fechar-disponibilizar')"/>
    <CompetenciaExclusaoModal
        :descricao="competenciaParaExcluir?.descricao || ''" :loading="loadingExclusao"
        :model-value="mostrarModalExcluirCompetencia"
        @confirmar="$emit('confirmar-exclusao-competencia')"
        @update:model-value="$emit('update:mostrarModalExcluirCompetencia', $event)"/>
  </template>
  <MapaAceitacaoModal
      :homologacao="homologacao" :loading="carregandoFluxoMapa" :mostrar-modal="mostrarModalAceitar"
      @fechar-modal="$emit('fechar-aceite')"
      @confirmar-aceitacao="$emit('confirmar-aceitacao', $event)"/>
  <MapaValidacaoModal
      :loading="carregandoFluxoMapa" :model-value="mostrarModalValidar"
      @confirmar="$emit('confirmar-validacao')"
      @update:model-value="$emit('update:mostrarModalValidar', $event)"/>
  <MapaDevolucaoModal
      :erro="mensagemErroDevolucao" :loading="carregandoFluxoMapa"
      :model-value="mostrarModalDevolucao" :observacao="observacaoDevolucao"
      @confirmar="$emit('confirmar-devolucao')"
      @update:model-value="$emit('update:mostrarModalDevolucao', $event)"
      @update:observacao="$emit('update:observacaoDevolucao', $event)"/>
  <MapaSugestoesEnvioModal
      :erro="mensagemErroSugestoes" :loading="loadingSugestoesEnvio" :model-value="mostrarModalSugestoes"
      :sugestoes="sugestoes"
      @confirmar="$emit('confirmar-sugestoes')"
      @update:model-value="$emit('update:mostrarModalSugestoes', $event)"
      @update:sugestoes="$emit('update:sugestoes', $event)"/>
  <MapaSugestoesVisualizacaoModal
      :model-value="mostrarModalVerSugestoes" :pode-editar="podeApresentarSugestoes"
      :sugestoes="sugestoesVisualizacao"
      @fechar="$emit('fechar-ver-sugestoes')"
      @update:model-value="$emit('update:mostrarModalVerSugestoes', $event)"/>
  <ImpactoMapaModal
      v-if="codigoSubprocesso" :impacto="impactos" :loading="loadingImpacto"
      :mostrar="mostrarModalImpacto" @fechar="$emit('fechar-impacto')"/>
  <HistoricoAnaliseModal
      :historico="historicoAnalise" :mostrar="mostrarModalHistorico"
      @fechar="$emit('fechar-historico')"/>
</template>
