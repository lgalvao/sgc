<template>
  <LayoutPadrao>
    <div v-if="subprocesso">
      <div data-testid="header-subprocesso">
        <PageHeader
            :subtitle="subprocesso.unidade.nome"
            :title="subprocesso.unidade.sigla"
            title-test-id="subprocesso-header__txt-header-unidade"
        >
          <template #actions>
            <BButton
                v-if="podeAlterarDataLimite && !isProcessoFinalizado"
                data-testid="btn-alterar-data-limite"
                variant="outline-primary"
                @click="abrirModalAlterarDataLimite"
            >
              <i aria-hidden="true" class="bi bi-calendar me-1"/>
              Alterar data limite
            </BButton>
            <BButton
                v-if="podeReabrirCadastro && !isProcessoFinalizado"
                data-testid="btn-reabrir-cadastro"
                variant="outline-warning"
                @click="abrirModalReabrirCadastro"
            >
              <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
              Reabrir cadastro
            </BButton>
            <BButton
                v-if="podeReabrirRevisao && !isProcessoFinalizado"
                data-testid="btn-reabrir-revisao"
                variant="outline-warning"
                @click="abrirModalReabrirRevisao"
            >
              <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
              Reabrir Revisão
            </BButton>
            <BButton
                v-if="podeEnviarLembrete && !isProcessoFinalizado"
                data-testid="btn-enviar-lembrete"
                variant="outline-info"
                @click="confirmarEnviarLembrete"
            >
              <i aria-hidden="true" class="bi bi-bell me-1"/>
              Enviar lembrete
            </BButton>
          </template>
        </PageHeader>

        <BCard class="mb-4" data-testid="header-subprocesso-details" no-body>
          <BCardBody>
            <p data-testid="txt-header-processo">
              <strong>Processo:</strong> {{ subprocesso.processoDescricao }}
            </p>
            <p>
              <span class="fw-bold me-1">Situação:</span>
              <span data-testid="subprocesso-header__txt-situacao">{{ formatSituacaoSubprocesso(subprocesso.situacao) }}</span>
            </p>
            <p><strong>Titular:</strong> {{ subprocesso.titular?.nome || '' }}</p>
            <p class="ms-3 mb-2">
              <span v-if="subprocesso.titular?.ramal" class="me-3">
                <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
                <a :href="`tel:${subprocesso.titular.ramal}`">{{ subprocesso.titular.ramal }}</a>
              </span>
              <span v-if="subprocesso.titular?.email">
                <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
                <a :href="`mailto:${subprocesso.titular.email}`">{{ subprocesso.titular.email }}</a>
              </span>
            </p>
            <template v-if="subprocesso.responsavel?.nome && subprocesso.responsavel?.nome !== subprocesso.titular?.nome">
              <p class="mt-2"><strong>Responsável:</strong> {{ subprocesso.responsavel?.nome || '' }}</p>
              <p class="ms-3 mb-0">
                <span v-if="subprocesso.responsavel?.ramal" class="me-3">
                  <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
                  <a :href="`tel:${subprocesso.responsavel.ramal}`">{{ subprocesso.responsavel.ramal }}</a>
                </span>
                <span v-if="subprocesso.responsavel?.email">
                  <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
                  <a :href="`mailto:${subprocesso.responsavel.email}`">{{ subprocesso.responsavel.email }}</a>
                </span>
              </p>
            </template>
          </BCardBody>
        </BCard>
      </div>

      <SubprocessoCards
          v-if="codSubprocesso"
          :cod-processo="props.codProcesso"
          :cod-subprocesso="codSubprocesso"
          :mapa="mapa"
          :sigla-unidade="props.siglaUnidade"
          :situacao="subprocesso.situacao"
          :tipo-processo="subprocesso.tipoProcesso || TipoProcesso.MAPEAMENTO"
      />

      <div class="mt-4">
        <h4>Movimentações</h4>
        <BTable
            :fields="camposMovimentacoes"
            :items="movimentacoes"
            :tbody-tr-attr="rowAttrMovimentacao"
            data-testid="tbl-movimentacoes"
            primary-key="codigo"
            responsive
            show-empty
            stacked="md"
            striped
        >
          <template #empty>
            <div class="text-center text-muted py-5" data-testid="empty-state-movimentacoes">
              <i aria-hidden="true" class="bi bi-arrow-left-right display-4 d-block mb-3"></i>
              <p class="h5">Nenhuma movimentação</p>
              <p class="small">O histórico de movimentações deste processo aparecerá aqui.</p>
            </div>
          </template>
          <template #cell(dataHora)="data">
            {{ formatDateTimeBR(data.item.dataHora) }}
          </template>
          <template #cell(unidadeOrigem)="data">
            {{ data.item.unidadeOrigemSigla || '-' }}
          </template>
          <template #cell(unidadeDestino)="data">
            {{ data.item.unidadeDestinoSigla || '-' }}
          </template>
        </BTable>
      </div>
    </div>
    <div v-else-if="subprocessosStore.lastError" class="py-2">
      <ErrorAlert
          :error="subprocessosStore.lastError"
          @dismiss="subprocessosStore.clearError()"
      />
    </div>
    <div v-else class="text-center py-5">
      <BSpinner label="Carregando informações da unidade..." variant="primary"/>
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
      :auto-close="false"
      :loading="loading.isLoading('reabertura')"
      :ok-disabled="!justificativaReabertura.trim()"
      :titulo="tipoReabertura === 'cadastro' ? 'Reabrir cadastro' : 'Reabrir Revisão'"
      ok-title="Confirmar Reabertura"
      test-id-confirmar="btn-confirmar-reabrir"
      variant="warning"
      @confirmar="confirmarReabertura"
  >
    <p>Informe a justificativa para reabrir o {{
        tipoReabertura === 'cadastro' ? 'cadastro' : 'revisão de cadastro'
      }}:</p>
    <BFormTextarea
        v-model="justificativaReabertura"
        data-testid="inp-justificativa-reabrir"
        placeholder="Justificativa obrigatória..."
        rows="3"
    />
  </ModalConfirmacao>

  <ModalConfirmacao
      v-model="modalLembreteAberto"
      :auto-close="false"
      ok-title="Confirmar envio"
      test-id-confirmar="btn-confirmar-enviar-lembrete"
      titulo="Enviar lembrete"
      variant="info"
      @confirmar="enviarLembreteConfirmado"
  >
    <p data-testid="txt-modelo-lembrete">
      Este lembrete será enviado para os responsáveis da unidade {{ subprocesso?.unidade?.sigla }} sobre o prazo do
      processo {{ subprocesso?.processoDescricao }}.
    </p>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BFormTextarea, BSpinner, BTable} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import SubprocessoCards from "@/components/processo/SubprocessoCards.vue";
import SubprocessoModal from "@/components/processo/SubprocessoModal.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {useMapasStore} from "@/stores/mapas";
import {useFeedbackStore} from "@/stores/feedback";
import {usePerfilStore} from "@/stores/perfil";
import {useModalManager} from "@/composables/useModalManager";
import {useLoadingManager} from "@/composables/useLoadingManager";

import {useAcesso} from "@/composables/useAcesso";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useProcessosStore} from "@/stores/processos";
import {
  type Movimentacao,
  SituacaoProcesso,
  SituacaoSubprocesso,
  type SubprocessoDetalhe,
  TipoProcesso
} from "@/types/tipos";
import {formatDateTimeBR, logger} from "@/utils";
import {formatSituacaoSubprocesso} from "@/utils/formatters";

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

const camposMovimentacoes = [
  {key: "dataHora", label: "Data/hora"},
  {key: "unidadeOrigem", label: "Origem"},
  {key: "unidadeDestino", label: "Destino"},
  {key: "descricao", label: "Descrição"}
];

const rowAttrMovimentacao = (item: Movimentacao | null, type: string) => {
  return item && type === 'row'
      ? {'data-testid': `row-movimentacao-${item.codigo}`}
      : {};
};

const subprocesso = computed<SubprocessoDetalhe | null>(
    () => subprocessosStore.subprocessoDetalhe,
);

const {
  podeAlterarDataLimite,
  podeReabrirCadastro,
  podeReabrirRevisao,
  podeEnviarLembrete,
  podeDisponibilizarCadastro,
  podeEditarCadastro
} = useAcesso(subprocesso);

const isProcessoFinalizado = computed(() => {
  return subprocesso.value?.situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO ||
      subprocesso.value?.situacao === SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO ||
      processosStore.processoDetalhe?.situacao === SituacaoProcesso.FINALIZADO;
});

const mapa = computed(() => mapaStore.mapaCompleto);
const movimentacoes = computed<Movimentacao[]>(
    () => subprocesso.value?.movimentacoes || [],
);
const dataLimite = computed(() =>
    subprocesso.value?.prazoEtapaAtual
        ? new Date(subprocesso.value.prazoEtapaAtual)
        : new Date(),
);

onMounted(async () => {
  try {
    const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
        props.codProcesso,
        props.siglaUnidade,
    );

    if (id) {
      codSubprocesso.value = id;
      await subprocessosStore.buscarSubprocessoDetalhe(id);
      await mapaStore.buscarMapaCompleto(id);
    } else {
      logger.warn(`Subprocesso não encontrado para processo ${props.codProcesso} e unidade ${props.siglaUnidade}`);
    }
  } catch (error: any) {
    const errorBody = error.response?.data || error.message;
    logger.error(`Erro ao carregar detalhes do subprocesso:`, error, errorBody);
  }
});

function abrirModalAlterarDataLimite() {
  if (podeAlterarDataLimite.value) {
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
    let sucesso: boolean;
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
