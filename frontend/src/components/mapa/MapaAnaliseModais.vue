<script setup lang="ts">
import {computed, defineAsyncComponent, ref} from "vue";
import {BFormGroup, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import AceitarMapaModal from "@/components/mapa/AceitarMapaModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import type {Analise, ImpactoMapa} from "@/types/tipos";

const ImpactoMapaModal = defineAsyncComponent(() => import("@/components/mapa/ImpactoMapaModal.vue"));

interface Props {
  carregandoFluxoMapa: boolean;
  homologacao: boolean;
  mostrarModalAceitar: boolean;
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

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: "fechar-aceite"): void;
  (e: "confirmar-aceitacao", valor: string): void;
  (e: "update:mostrarModalValidar", valor: boolean): void;
  (e: "confirmar-validacao"): void;
  (e: "update:mostrarModalDevolucao", valor: boolean): void;
  (e: "confirmar-devolucao"): void;
  (e: "update:observacaoDevolucao", valor: string): void;
  (e: "fechar-impacto"): void;
  (e: "fechar-historico"): void;
}>();

const modalValidar = computed({
  get: () => props.mostrarModalValidar,
  set: (valor: boolean) => emit("update:mostrarModalValidar", valor),
});

const modalDevolucao = computed({
  get: () => props.mostrarModalDevolucao,
  set: (valor: boolean) => emit("update:mostrarModalDevolucao", valor),
});

const observacaoDevolucaoModel = computed({
  get: () => props.observacaoDevolucao,
  set: (valor: string) => emit("update:observacaoDevolucao", valor),
});

const observacaoDevolucaoRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

function focarDevolucao() {
  observacaoDevolucaoRef.value?.$el?.focus();
}
</script>

<template>
  <AceitarMapaModal
      :loading="carregandoFluxoMapa"
      :homologacao="homologacao"
      :mostrar-modal="mostrarModalAceitar"
      @fechar-modal="$emit('fechar-aceite')"
      @confirmar-aceitacao="$emit('confirmar-aceitacao', $event)"
  />

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
