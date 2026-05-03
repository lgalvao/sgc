<script setup lang="ts">
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
  modoSomenteLeitura: boolean; atividades: Atividade[]; codigoSubprocesso?: number | null;
  mostrarModalCriarNovaCompetencia: boolean; competenciaSendoEditada: Competencia | null; loadingCompetencia: boolean; fieldErrors: Record<string, string | undefined>;
  mostrarModalDisponibilizar: boolean; loadingDisponibilizacao: boolean; notificacaoDisponibilizacao: string; ultimaDataLimiteSubprocesso?: string | null;
  mostrarModalExcluirCompetencia: boolean; loadingExclusao: boolean; competenciaParaExcluir: Competencia | null;
  carregandoFluxoMapa: boolean; homologacao: boolean; mostrarModalAceitar: boolean; mostrarModalValidar: boolean; mostrarModalDevolucao: boolean;
  mensagemErroDevolucao: string; observacaoDevolucao: string; mostrarModalSugestoes: boolean; loadingSugestoesEnvio: boolean;
  mensagemErroSugestoes: string; sugestoes: string; mostrarModalVerSugestoes: boolean; podeApresentarSugestoes: boolean;
  sugestoesVisualizacao: string; mostrarModalImpacto: boolean; loadingImpacto: boolean; impactos?: ImpactoMapa | null;
  mostrarModalHistorico: boolean; historicoAnalise: Analise[];
}>();

defineEmits<{
  (e: "fechar-criar-competencia"): void; (e: "salvar-competencia", v: { descricao: string; atividadesSelecionadas: number[] }): void;
  (e: "fechar-disponibilizar"): void; (e: "disponibilizar", v: { dataLimite: string; observacoes: string }): void;
  (e: "update:mostrarModalExcluirCompetencia", v: boolean): void; (e: "confirmar-exclusao-competencia"): void;
  (e: "fechar-aceite"): void; (e: "confirmar-aceitacao", v: string): void;
  (e: "update:mostrarModalValidar", v: boolean): void; (e: "confirmar-validacao"): void;
  (e: "update:mostrarModalDevolucao", v: boolean): void; (e: "confirmar-devolucao"): void; (e: "update:observacaoDevolucao", v: string): void;
  (e: "update:mostrarModalSugestoes", v: boolean): void; (e: "confirmar-sugestoes"): void; (e: "update:sugestoes", v: string): void;
  (e: "update:mostrarModalVerSugestoes", v: boolean): void; (e: "fechar-ver-sugestoes"): void; (e: "update:sugestoesVisualizacao", v: string): void;
  (e: "fechar-impacto"): void; (e: "fechar-historico"): void;
}>();
</script>

<template>
  <template v-if="!modoSomenteLeitura">
    <CompetenciaEdicaoModal :mostrar="mostrarModalCriarNovaCompetencia" :atividades="atividades" :loading="loadingCompetencia" :competencia-para-editar="competenciaSendoEditada" :field-errors="fieldErrors" @fechar="$emit('fechar-criar-competencia')" @salvar="$emit('salvar-competencia', $event)" />
    <MapaDisponibilizacaoModal :mostrar="mostrarModalDisponibilizar" :loading="loadingDisponibilizacao" :notificacao="notificacaoDisponibilizacao" :field-errors="fieldErrors" :ultima-data-limite-subprocesso="ultimaDataLimiteSubprocesso ?? undefined" @fechar="$emit('fechar-disponibilizar')" @disponibilizar="$emit('disponibilizar', $event)" />
    <CompetenciaExclusaoModal :model-value="mostrarModalExcluirCompetencia" :loading="loadingExclusao" :descricao="competenciaParaExcluir?.descricao || ''" @update:model-value="$emit('update:mostrarModalExcluirCompetencia', $event)" @confirmar="$emit('confirmar-exclusao-competencia')" />
  </template>
  <MapaAceitacaoModal :mostrar-modal="mostrarModalAceitar" :loading="carregandoFluxoMapa" :homologacao="homologacao" @fechar-modal="$emit('fechar-aceite')" @confirmar-aceitacao="$emit('confirmar-aceitacao', $event)" />
  <MapaValidacaoModal :model-value="mostrarModalValidar" :loading="carregandoFluxoMapa" @update:model-value="$emit('update:mostrarModalValidar', $event)" @confirmar="$emit('confirmar-validacao')" />
  <MapaDevolucaoModal :model-value="mostrarModalDevolucao" :loading="carregandoFluxoMapa" :observacao="observacaoDevolucao" :erro="mensagemErroDevolucao" @update:model-value="$emit('update:mostrarModalDevolucao', $event)" @update:observacao="$emit('update:observacaoDevolucao', $event)" @confirmar="$emit('confirmar-devolucao')" />
  <MapaSugestoesEnvioModal :model-value="mostrarModalSugestoes" :loading="loadingSugestoesEnvio" :sugestoes="sugestoes" :erro="mensagemErroSugestoes" @update:model-value="$emit('update:mostrarModalSugestoes', $event)" @update:sugestoes="$emit('update:sugestoes', $event)" @confirmar="$emit('confirmar-sugestoes')" />
  <MapaSugestoesVisualizacaoModal :model-value="mostrarModalVerSugestoes" :sugestoes="sugestoesVisualizacao" :pode-editar="podeApresentarSugestoes" @update:model-value="$emit('update:mostrarModalVerSugestoes', $event)" @fechar="$emit('fechar-ver-sugestoes')" />
  <ImpactoMapaModal v-if="codigoSubprocesso" :mostrar="mostrarModalImpacto" :loading="loadingImpacto" :impacto="impactos" @fechar="$emit('fechar-impacto')" />
  <HistoricoAnaliseModal :mostrar="mostrarModalHistorico" :historico="historicoAnalise" @fechar="$emit('fechar-historico')" />
</template>
