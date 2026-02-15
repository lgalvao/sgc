<template>
  <LayoutPadrao>
    <div v-if="subprocesso">
      <PageHeader
          :title="`Subprocesso - ${subprocesso.unidade.sigla}`"
          :subtitle="subprocesso.processoDescricao"
          :etapa="`Etapa atual: ${subprocesso.situacaoLabel || subprocesso.situacao}`"
          :proxima-acao="proximaAcaoSubprocesso"
      />

      <SubprocessoHeader
          :pode-alterar-data-limite="subprocesso.permissoes.podeAlterarDataLimite"
          :pode-reabrir-cadastro="subprocesso.permissoes.podeReabrirCadastro"
          :pode-reabrir-revisao="subprocesso.permissoes.podeReabrirRevisao"
          :pode-enviar-lembrete="subprocesso.permissoes.podeEnviarLembrete"
          :processo-descricao="subprocesso.processoDescricao || ''"
          :responsavel-email="subprocesso.responsavel?.email || ''"
          :responsavel-nome="subprocesso.responsavel?.nome || ''"
          :responsavel-ramal="subprocesso.responsavel?.ramal || ''"
          :situacao="subprocesso.situacaoLabel"
          :titular-email="subprocesso.titular?.email || ''"
          :titular-nome="subprocesso.titular?.nome || ''"
          :titular-ramal="subprocesso.titular?.ramal || ''"
          :unidade-nome="subprocesso.unidade.nome"
          :unidade-sigla="subprocesso.unidade.sigla"
          @alterar-data-limite="abrirModalAlterarDataLimite"
          @reabrir-cadastro="abrirModalReabrirCadastro"
          @reabrir-revisao="abrirModalReabrirRevisao"
          @enviar-lembrete="confirmarEnviarLembrete"
      />

      <SubprocessoCards
          v-if="codSubprocesso"
          :cod-processo="props.codProcesso"
          :cod-subprocesso="codSubprocesso"
          :mapa="mapa"
          :permissoes="subprocesso.permissoes || { podeEditarMapa: true, podeVisualizarMapa: true, podeVisualizarDiagnostico: true, podeAlterarDataLimite: false, podeDisponibilizarCadastro: false, podeDevolverCadastro: false, podeAceitarCadastro: false, podeVisualizarImpacto: false, podeVerPagina: true, podeRealizarAutoavaliacao: false, podeDisponibilizarMapa: false }"
          :sigla-unidade="props.siglaUnidade"
          :situacao="subprocesso.situacao"
          :tipo-processo="subprocesso.tipoProcesso || TipoProcesso.MAPEAMENTO"
      />

      <TabelaMovimentacoes :movimentacoes="movimentacoes"/>
    </div>
    <div v-else-if="subprocessosStore.lastError" class="py-2">
      <ErrorAlert
          :error="subprocessosStore.lastError"
          @dismiss="subprocessosStore.clearError()"
      />
    </div>
    <div v-else class="text-center py-5">
      <BSpinner label="Carregando informações da unidade..." variant="primary" />
      <p class="mt-2 text-muted">Carregando informações da unidade...</p>
    </div>
  </LayoutPadrao>

  <SubprocessoModal
      :data-limite-atual="dataLimite"
      :etapa-atual="subprocesso?.etapaAtual || null"
      :loading="loading.isLoading('dataLimite')"
      :mostrar-modal="modals.isOpen('alterarDataLimite')"
      @fechar-modal="fecharModalAlterarDataLimite"
      @confirmar-alteracao="confirmarAlteracaoDataLimite"
  />

  <!-- Modal para reabrir cadastro/revisão -->
  <ModalConfirmacao
      v-model="modals.modals.reabrir.value.isOpen"
      :titulo="tipoReabertura === 'cadastro' ? 'Reabrir cadastro' : 'Reabrir Revisão'"
      variant="warning"
      ok-title="Confirmar Reabertura"
      :loading="loading.isLoading('reabertura')"
      :ok-disabled="!justificativaReabertura.trim()"
      :auto-close="false"
      test-id-confirmar="btn-confirmar-reabrir"
      @confirmar="confirmarReabertura"
  >
    <p>Informe a justificativa para reabrir o {{ tipoReabertura === 'cadastro' ? 'cadastro' : 'revisão de cadastro' }}:</p>
    <BFormTextarea
        v-model="justificativaReabertura"
        data-testid="inp-justificativa-reabrir"
        placeholder="Justificativa obrigatória..."
        rows="3"
    />
  </ModalConfirmacao>

  <ModalConfirmacao
      v-model="modalLembreteAberto"
      titulo="Enviar lembrete"
      variant="info"
      ok-title="Confirmar envio"
      :auto-close="false"
      test-id-confirmar="btn-confirmar-enviar-lembrete"
      @confirmar="enviarLembreteConfirmado"
  >
    <p data-testid="txt-modelo-lembrete">
      Este lembrete será enviado para os responsáveis da unidade {{ subprocesso?.unidade?.sigla }} sobre o prazo do
      processo {{ subprocesso?.processoDescricao }}.
    </p>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {BFormTextarea, BSpinner} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import SubprocessoCards from "@/components/processo/SubprocessoCards.vue";
import SubprocessoHeader from "@/components/processo/SubprocessoHeader.vue";
import SubprocessoModal from "@/components/processo/SubprocessoModal.vue";
import TabelaMovimentacoes from "@/components/processo/TabelaMovimentacoes.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {useProximaAcao} from "@/composables/useProximaAcao";
import {useMapasStore} from "@/stores/mapas";
import {useFeedbackStore} from "@/stores/feedback";
import {usePerfilStore} from "@/stores/perfil";
import {useModalManager} from "@/composables/useModalManager";
import {useLoadingManager} from "@/composables/useLoadingManager";

import {useSubprocessosStore} from "@/stores/subprocessos";
import {useProcessosStore} from "@/stores/processos";
import {type Movimentacao, type SubprocessoDetalhe, TipoProcesso,} from "@/types/tipos";

const props = defineProps<{ codProcesso: number; siglaUnidade: string }>();

const subprocessosStore = useSubprocessosStore();
const processosStore = useProcessosStore();

const mapaStore = useMapasStore();
const feedbackStore = useFeedbackStore();
const perfilStore = usePerfilStore();

// Gerenciamento simplificado de modals e loading com composables
const modals = useModalManager(['alterarDataLimite', 'reabrir']);
const loading = useLoadingManager(['dataLimite', 'reabertura']);

const tipoReabertura = ref<'cadastro' | 'revisao'>('cadastro');
const justificativaReabertura = ref('');
const codSubprocesso = ref<number | null>(null);
const modalLembreteAberto = ref(false);

const subprocesso = computed<SubprocessoDetalhe | null>(
    () => subprocessosStore.subprocessoDetalhe,
);
const mapa = computed(() => mapaStore.mapaCompleto);
const movimentacoes = computed<Movimentacao[]>(
    () => subprocesso.value?.movimentacoes || [],
);
const dataLimite = computed(() =>
    subprocesso.value?.prazoEtapaAtual
        ? new Date(subprocesso.value.prazoEtapaAtual)
        : new Date(),
);
const {obterProximaAcao} = useProximaAcao();
const proximaAcaoSubprocesso = computed(() => obterProximaAcao({
  perfil: perfilStore.perfilSelecionado,
  situacao: subprocesso.value?.situacaoLabel || subprocesso.value?.situacao,
  podeDisponibilizarCadastro: subprocesso.value?.permissoes?.podeDisponibilizarCadastro,
  podeEditarCadastro: subprocesso.value?.permissoes?.podeEditarCadastro,
}));

onMounted(async () => {
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
      props.codProcesso,
      props.siglaUnidade,
  );

  if (id) {
    codSubprocesso.value = id;
    await subprocessosStore.buscarSubprocessoDetalhe(id);
    await mapaStore.buscarMapaCompleto(id);
  }
});

function abrirModalAlterarDataLimite() {
  if (subprocesso.value?.permissoes.podeAlterarDataLimite) {
    modals.open('alterarDataLimite');
  } else {
    feedbackStore.show("Ação não permitida", "Você não tem permissão para alterar a data limite.", "danger");
  }
}

function fecharModalAlterarDataLimite() {
  modals.close('alterarDataLimite');
}

async function confirmarAlteracaoDataLimite(novaData: string) {
  if (!novaData || !subprocesso.value) {
    return;
  }

  await loading.withLoading('dataLimite', async () => {
    try {
      await subprocessosStore.alterarDataLimiteSubprocesso(
          subprocesso.value!.codigo,
          {novaData},
      );
      fecharModalAlterarDataLimite();
      feedbackStore.show("Data limite alterada com sucesso", "A data limite foi alterada com sucesso!", "success");
    } catch {
      feedbackStore.show("Erro ao alterar data limite", "Não foi possível alterar a data limite.", "danger");
    }
  });
}

// CDU-32/33: Reabrir cadastro/revisão
function abrirModalReabrirCadastro() {
  tipoReabertura.value = 'cadastro';
  justificativaReabertura.value = '';
  modals.open('reabrir');
}

function abrirModalReabrirRevisao() {
  tipoReabertura.value = 'revisao';
  justificativaReabertura.value = '';
  modals.open('reabrir');
}

function fecharModalReabrir() {
  modals.close('reabrir');
  justificativaReabertura.value = '';
}

async function confirmarReabertura() {
  if (!codSubprocesso.value || !justificativaReabertura.value.trim()) {
    feedbackStore.show("Erro", "Justificativa é obrigatória.", "danger");
    return;
  }

  await loading.withLoading('reabertura', async () => {
    let sucesso = false;
    if (tipoReabertura.value === 'cadastro') {
      sucesso = await subprocessosStore.reabrirCadastro(codSubprocesso.value!, justificativaReabertura.value);
    } else {
      sucesso = await subprocessosStore.reabrirRevisaoCadastro(codSubprocesso.value!, justificativaReabertura.value);
    }

    if (sucesso) {
      fecharModalReabrir();
      await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value!);
    }
  });
}

async function confirmarEnviarLembrete() {
  if (!subprocesso.value) {
    return;
  }
  modalLembreteAberto.value = true;
}

async function enviarLembreteConfirmado() {
  if (!subprocesso.value || !codSubprocesso.value) {
    return;
  }
  await processosStore.enviarLembrete(props.codProcesso, subprocesso.value.unidade.codigo);
  await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value);
  modalLembreteAberto.value = false;
}
</script>
