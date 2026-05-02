<script setup lang="ts">
import {computed, defineAsyncComponent} from "vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import type {Atividade, Competencia} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

const CriarCompetenciaModal = defineAsyncComponent(() => import("@/components/mapa/CriarCompetenciaModal.vue"));
const DisponibilizarMapaModal = defineAsyncComponent(() => import("@/components/mapa/DisponibilizarMapaModal.vue"));

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
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: "fechar-criar-competencia"): void;
  (e: "salvar-competencia", valor: { descricao: string; atividadesSelecionadas: number[] }): void;
  (e: "fechar-disponibilizar"): void;
  (e: "disponibilizar", valor: { dataLimite: string; observacoes: string }): void;
  (e: "update:mostrarModalExcluirCompetencia", valor: boolean): void;
  (e: "confirmar-exclusao-competencia"): void;
}>();

const modalExcluirCompetencia = computed({
  get: () => props.mostrarModalExcluirCompetencia,
  set: (valor: boolean) => emit("update:mostrarModalExcluirCompetencia", valor),
});
</script>

<template>
  <template v-if="!modoSomenteLeitura">
    <CriarCompetenciaModal
        :atividades="atividades"
        :competencia-para-editar="competenciaSendoEditada"
        :field-errors="fieldErrors"
        :loading="loadingCompetencia"
        :mostrar="mostrarModalCriarNovaCompetencia"
        @fechar="$emit('fechar-criar-competencia')"
        @salvar="$emit('salvar-competencia', $event)"
    />

    <DisponibilizarMapaModal
        :field-errors="fieldErrors"
        :loading="loadingDisponibilizacao"
        :mostrar="mostrarModalDisponibilizar"
        :notificacao="notificacaoDisponibilizacao"
        :ultima-data-limite-subprocesso="ultimaDataLimiteSubprocesso ?? undefined"
        @disponibilizar="$emit('disponibilizar', $event)"
        @fechar="$emit('fechar-disponibilizar')"
    />

    <ModalConfirmacao
        v-model="modalExcluirCompetencia"
        :loading="loadingExclusao"
        :mensagem="TEXTOS.mapa.EXCLUSAO_CONFIRMACAO(competenciaParaExcluir?.descricao || '')"
        data-testid="mdl-excluir-competencia"
        test-codigo-confirmar="btn-confirmar-exclusao-competencia"
        :titulo="TEXTOS.mapa.EXCLUSAO_TITULO"
        variant="danger"
        @confirmar="$emit('confirmar-exclusao-competencia')"
    />
  </template>
</template>
