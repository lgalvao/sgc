<script setup lang="ts">
import {computed, defineAsyncComponent, ref} from "vue";
import {BFormGroup, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import AceitarMapaModal from "@/components/mapa/AceitarMapaModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import type {Analise, Atividade, Competencia, ImpactoMapa} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

const ImpactoMapaModal = defineAsyncComponent(() => import("@/components/mapa/ImpactoMapaModal.vue"));
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
  impactos: ImpactoMapa;
  loadingImpacto: boolean;
  mostrarModalImpacto: boolean;
  historicoAnalise: Analise[];
  mostrarModalHistorico: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: "fechar-criar-competencia"): void;
  (e: "salvar-competencia", valor: Competencia): void;
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

const modalExcluirCompetencia = computed({
  get: () => props.mostrarModalExcluirCompetencia,
  set: (valor: boolean) => emit("update:mostrarModalExcluirCompetencia", valor),
});

const modalVerSugestoes = computed({
  get: () => props.mostrarModalVerSugestoes,
  set: (valor: boolean) => emit("update:mostrarModalVerSugestoes", valor),
});

const modalSugestoes = computed({
  get: () => props.mostrarModalSugestoes,
  set: (valor: boolean) => emit("update:mostrarModalSugestoes", valor),
});

const modalValidar = computed({
  get: () => props.mostrarModalValidar,
  set: (valor: boolean) => emit("update:mostrarModalValidar", valor),
});

const modalDevolucao = computed({
  get: () => props.mostrarModalDevolucao,
  set: (valor: boolean) => emit("update:mostrarModalDevolucao", valor),
});

const sugestoesVisualizacaoModel = computed({
  get: () => props.sugestoesVisualizacao,
  set: (valor: string) => emit("update:sugestoesVisualizacao", valor),
});

const sugestoesModel = computed({
  get: () => props.sugestoes,
  set: (valor: string) => emit("update:sugestoes", valor),
});

const observacaoDevolucaoModel = computed({
  get: () => props.observacaoDevolucao,
  set: (valor: string) => emit("update:observacaoDevolucao", valor),
});

const sugestoesTextareaRef = ref<InstanceType<typeof BFormTextarea> | null>(null);
const observacaoDevolucaoRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

function focarSugestoes() {
  sugestoesTextareaRef.value?.$el?.focus();
}

function focarDevolucao() {
  observacaoDevolucaoRef.value?.$el?.focus();
}
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
        :ultima-data-limite-subprocesso="ultimaDataLimiteSubprocesso"
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

  <AceitarMapaModal
      :loading="carregandoFluxoMapa"
      :homologacao="homologacao"
      :mostrar-modal="mostrarModalAceitar"
      @fechar-modal="$emit('fechar-aceite')"
      @confirmar-aceitacao="$emit('confirmar-aceitacao', $event)"
  />

  <ModalPadrao
      v-model="modalVerSugestoes"
      :mostrar-botao-acao="false"
      test-codigo-cancelar="btn-ver-sugestoes-mapa-fechar"
      texto-cancelar="Fechar"
      titulo="Sugestões sobre o mapa"
      @fechar="$emit('fechar-ver-sugestoes')"
  >
    <BFormGroup class="mb-3">
      <template #label>
        Sugestões registradas para o mapa de competências:
      </template>

      <div
          v-if="!isChefe"
          class="border rounded p-3 bg-body-tertiary white-space-pre-line"
          data-testid="txt-ver-sugestoes-mapa-texto"
      >
        {{ sugestoesVisualizacao }}
      </div>

      <BFormTextarea
          v-else
          id="sugestoesVisualizacao"
          v-model="sugestoesVisualizacaoModel"
          data-testid="txt-ver-sugestoes-mapa"
          readonly
          rows="5"
      />
    </BFormGroup>
  </ModalPadrao>

  <ModalConfirmacao
      v-model="modalSugestoes"
      :auto-close="false"
      :loading="loadingSugestoesEnvio"
      :ok-title="TEXTOS.comum.BOTAO_APRESENTAR"
      test-codigo-cancelar="btn-sugestoes-mapa-cancelar"
      test-codigo-confirmar="btn-sugestoes-mapa-confirmar"
      titulo="Apresentar sugestões"
      variant="success"
      @confirmar="$emit('confirmar-sugestoes')"
      @shown="focarSugestoes"
  >
    <BFormGroup
        label-for="sugestoesTextarea"
        :state="mensagemErroSugestoes ? false : null"
        class="mb-3"
    >
      <template #label>
        Sugestões para o mapa de competências: <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <BFormTextarea
          id="sugestoesTextarea"
          ref="sugestoesTextareaRef"
          v-model="sugestoesModel"
          aria-required="true"
          :state="mensagemErroSugestoes ? false : null"
          data-testid="inp-sugestoes-mapa-texto"
          rows="5"
      />
      <BFormInvalidFeedback :state="mensagemErroSugestoes ? false : null">
        {{ mensagemErroSugestoes }}
      </BFormInvalidFeedback>
    </BFormGroup>
  </ModalConfirmacao>

  <ModalConfirmacao
      v-model="modalValidar"
      :loading="carregandoFluxoMapa"
      :ok-title="TEXTOS.comum.BOTAO_VALIDAR"
      test-codigo-cancelar="btn-validar-mapa-cancelar"
      test-codigo-confirmar="btn-validar-mapa-confirmar"
      titulo="Validação de mapa"
      variant="success"
      @confirmar="$emit('confirmar-validacao')"
  >
    <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
  </ModalConfirmacao>

  <ModalConfirmacao
      v-model="modalDevolucao"
      :auto-close="false"
      :loading="carregandoFluxoMapa"
      :ok-title="TEXTOS.mapa.BOTAO_DEVOLVER"
      test-codigo-cancelar="btn-devolucao-mapa-cancelar"
      test-codigo-confirmar="btn-devolucao-mapa-confirmar"
      titulo="Devolver mapa"
      variant="danger"
      @confirmar="$emit('confirmar-devolucao')"
      @shown="focarDevolucao"
  >
    <p>Confirma a devolução da validação do mapa para ajustes?</p>
    <BFormGroup
        label-for="observacaoDevolucao"
        :state="mensagemErroDevolucao ? false : null"
        class="mb-3"
    >
      <template #label>
        Observação: <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <BFormTextarea
          id="observacaoDevolucao"
          ref="observacaoDevolucaoRef"
          v-model="observacaoDevolucaoModel"
          :state="mensagemErroDevolucao ? false : null"
          data-testid="inp-devolucao-mapa-obs"
          placeholder="Digite observações sobre a devolução..."
          rows="3"
      />
      <BFormInvalidFeedback :state="mensagemErroDevolucao ? false : null">
        {{ mensagemErroDevolucao }}
      </BFormInvalidFeedback>
    </BFormGroup>
  </ModalConfirmacao>

  <ImpactoMapaModal
      v-if="codigoSubprocesso"
      :impacto="impactos"
      :loading="loadingImpacto"
      :mostrar="mostrarModalImpacto"
      @fechar="$emit('fechar-impacto')"
  />

  <HistoricoAnaliseModal
      :historico="historicoAnalise"
      :mostrar="mostrarModalHistorico"
      @fechar="$emit('fechar-historico')"
  />
</template>
