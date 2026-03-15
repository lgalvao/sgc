<template>
  <LayoutPadrao>
    <AppAlert
        v-if="notificacao"
        :dismissible="notificacao.dismissible ?? true"
        :message="notificacao.message"
        :variant="notificacao.variant"
        @dismissed="clear()"
    />
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
              {{ TEXTOS.subprocesso.BOTAO_ALTERAR_DATA_LIMITE }}
            </BButton>
            <BButton
                v-if="podeReabrirCadastro && !isProcessoFinalizado"
                data-testid="btn-reabrir-cadastro"
                variant="outline-warning"
                @click="abrirModalReabrirCadastro"
            >
              <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
              {{ TEXTOS.subprocesso.BOTAO_REABRIR_CADASTRO }}
            </BButton>
            <BButton
                v-if="podeReabrirRevisao && !isProcessoFinalizado"
                data-testid="btn-reabrir-revisao"
                variant="outline-warning"
                @click="abrirModalReabrirRevisao"
            >
              <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
              {{ TEXTOS.subprocesso.BOTAO_REABRIR_REVISAO }}
            </BButton>
            <BButton
                v-if="podeEnviarLembrete && !isProcessoFinalizado"
                data-testid="btn-enviar-lembrete"
                variant="outline-info"
                @click="confirmarEnviarLembrete"
            >
              <i aria-hidden="true" class="bi bi-bell me-1"/>
              {{ TEXTOS.subprocesso.BOTAO_ENVIAR_LEMBRETE }}
            </BButton>
          </template>
        </PageHeader>

        <BCard class="mb-4" data-testid="header-subprocesso-details" no-body>
          <BCardBody>
            <p data-testid="txt-header-processo">
              <strong>{{ TEXTOS.subprocesso.LABEL_PROCESSO }}:</strong> {{ subprocesso.processoDescricao }}
            </p>
            <p>
              <span class="fw-bold me-1">{{ TEXTOS.subprocesso.LABEL_SITUACAO }}:</span>
              <span data-testid="subprocesso-header__txt-situacao">{{ formatSituacaoSubprocesso(subprocesso.situacao) }}</span>
            </p>
            <p>
              <span class="fw-bold me-1">{{ TEXTOS.subprocesso.LABEL_LOCALIZACAO }}:</span>
              <span data-testid="subprocesso-header__txt-localizacao">{{ subprocesso.localizacaoAtual || subprocesso.unidade.sigla }}</span>
            </p>
            <p v-if="subprocesso.prazoEtapaAtual">
              <span class="fw-bold me-1">{{ TEXTOS.subprocesso.LABEL_PRAZO_ETAPA }}:</span>
              <span>{{ formatDataSimples(subprocesso.prazoEtapaAtual) }}</span>
            </p>
            <p class="mt-2"><strong>{{ TEXTOS.subprocesso.LABEL_TITULAR }}:</strong> {{ subprocesso.titular?.nome || '' }}</p>
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
            <template v-if="subprocesso.responsavel?.usuario?.nome && subprocesso.responsavel.usuario.nome !== subprocesso.titular?.nome">
              <p class="mt-2">
                <strong>{{ TEXTOS.subprocesso.LABEL_RESPONSAVEL }}:</strong> {{ subprocesso.responsavel.usuario.nome || '' }}
                <span v-if="subprocesso.responsavel.tipo" class="ms-1">
                  - {{ formatTipoResponsabilidade(subprocesso.responsavel) }}
                </span>
              </p>
              <p class="ms-3 mb-0">
                <span v-if="subprocesso.responsavel.usuario.ramal" class="me-3">
                  <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
                  <a :href="`tel:${subprocesso.responsavel.usuario.ramal}`">{{ subprocesso.responsavel.usuario.ramal }}</a>
                </span>
                <span v-if="subprocesso.responsavel.usuario.email">
                  <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
                  <a :href="`mailto:${subprocesso.responsavel.usuario.email}`">{{ subprocesso.responsavel.usuario.email }}</a>
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
        <h4>{{ TEXTOS.subprocesso.MOVIMENTACOES_TITULO }}</h4>
        <BTable
            :fields="camposMovimentacoes"
            :items="movimentacoes"
            :tbody-tr-props="rowAttrMovimentacao"
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
              <p class="h5">{{ TEXTOS.subprocesso.MOVIMENTACOES_VAZIO_TITULO }}</p>
              <p class="small">{{ TEXTOS.subprocesso.MOVIMENTACOES_VAZIO_TEXTO }}</p>
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
      <BAlert
          :model-value="true"
          variant="danger"
          dismissible
          @dismissed="subprocessosStore.clearError()"
      >
        {{ subprocessosStore.lastError.message }}
        <div v-if="subprocessosStore.lastError.details">
          <small>Detalhes: {{ subprocessosStore.lastError.details }}</small>
        </div>
      </BAlert>
    </div>
    <div v-else class="text-center py-5">
      <BSpinner :label="TEXTOS.subprocesso.CARREGANDO" variant="primary"/>
      <p class="mt-2 text-muted">{{ TEXTOS.subprocesso.CARREGANDO }}</p>
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
      :titulo="tipoReabertura === 'cadastro' ? TEXTOS.subprocesso.REABRIR_CADASTRO_TITULO : TEXTOS.subprocesso.REABRIR_REVISAO_TITULO"
      :ok-title="TEXTOS.subprocesso.BOTAO_CONFIRMAR_REABERTURA"
      test-id-confirmar="btn-confirmar-reabrir"
      variant="warning"
      @confirmar="confirmarReabertura"
  >
    <p>{{ TEXTOS.subprocesso.REABRIR_JUSTIFICATIVA_PREFIXO }} {{
        tipoReabertura === 'cadastro' ? TEXTOS.subprocesso.CADASTRO : TEXTOS.subprocesso.REVISAO_CADASTRO
      }}:</p>
    <BFormTextarea
        v-model="justificativaReabertura"
        data-testid="inp-justificativa-reabrir"
        :placeholder="TEXTOS.subprocesso.REABRIR_JUSTIFICATIVA_PLACEHOLDER"
        rows="3"
    />
  </ModalConfirmacao>

  <ModalConfirmacao
      v-model="modalLembreteAberto"
      :auto-close="false"
      :ok-title="TEXTOS.subprocesso.BOTAO_CONFIRMAR_LEMRETE"
      test-id-confirmar="btn-confirmar-enviar-lembrete"
      :titulo="TEXTOS.subprocesso.LEMBRETE_TITULO"
      variant="info"
      @confirmar="enviarLembreteConfirmado"
  >
    <p data-testid="txt-modelo-lembrete">
      {{ TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(subprocesso?.unidade?.sigla || '') }}
      {{ TEXTOS.subprocesso.LEMBRETE_MODELO_PROCESSO(subprocesso?.processoDescricao || '') }}
    </p>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCard, BCardBody, BFormTextarea, BSpinner, BTable} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import SubprocessoCards from "@/components/processo/SubprocessoCards.vue";
import SubprocessoModal from "@/components/processo/SubprocessoModal.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {useMapasStore} from "@/stores/mapas";
import {useNotification} from "@/composables/useNotification";
import {useModalManager} from "@/composables/useModalManager";
import {useLoadingManager} from "@/composables/useLoadingManager";

import {useAcesso} from "@/composables/useAcesso";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useProcessosStore} from "@/stores/processos";
import {type Movimentacao, SituacaoProcesso, type SubprocessoDetalhe, TipoProcesso} from "@/types/tipos";
import {formatDateTimeBR, logger} from "@/utils";
import {normalizeError} from "@/utils/apiError";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{ codProcesso: number; siglaUnidade: string }>();

function formatDataSimples(dataStr: string | null): string {
  if (!dataStr) return '';
  const data = new Date(dataStr);
  return data.toLocaleDateString('pt-BR');
}

function formatTipoResponsabilidade(resp: any): string {
  if (!resp || !resp.tipo) return '';
  if (resp.tipo === 'Substituição' && resp.dataFim) {
    return `Substituição (até ${formatDataSimples(resp.dataFim)})`;
  } else if (resp.tipo === 'Atribuição temporária' && resp.dataFim) {
    return `Atrib. temporária (até ${formatDataSimples(resp.dataFim)})`;
  }
  return resp.tipo;
}

const subprocessosStore = useSubprocessosStore();
const processosStore = useProcessosStore();

const mapaStore = useMapasStore();
const {notificacao, notify, clear} = useNotification();

// Gerenciamento simplificado de modals e loading com composables
const modals = useModalManager(['alterarDataLimite', 'reabrir']);
const loading = useLoadingManager(['dataLimite', 'reabertura']);

const tipoReabertura = ref<'cadastro' | 'revisao'>('cadastro');
const justificativaReabertura = ref('');
const codSubprocesso = ref<number | null>(null);
const modalLembreteAberto = ref(false);

const camposMovimentacoes = [
  {key: "dataHora", label: TEXTOS.subprocesso.MOVIMENTACOES_CAMPO_DATA},
  {key: "unidadeOrigem", label: TEXTOS.subprocesso.MOVIMENTACOES_CAMPO_ORIGEM},
  {key: "unidadeDestino", label: TEXTOS.subprocesso.MOVIMENTACOES_CAMPO_DESTINO},
  {key: "descricao", label: TEXTOS.subprocesso.MOVIMENTACOES_CAMPO_DESCRICAO}
];

const rowAttrMovimentacao = (item: Movimentacao | null) => {
  return item
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
  podeEnviarLembrete
} = useAcesso(subprocesso);

const isProcessoFinalizado = computed(() => {
  return processosStore.processoDetalhe?.situacao === SituacaoProcesso.FINALIZADO;
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
    if (normalizeError(error).kind !== 'unauthorized') {
      logger.error(`Erro ao carregar detalhes do subprocesso:`, error, errorBody);
    }
  }
});

function abrirModalAlterarDataLimite() {
  if (podeAlterarDataLimite.value) {
    modals.open('alterarDataLimite');
  } else {
    notify("Você não tem permissão para alterar a data limite.", 'danger');
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
      notify("Data limite alterada", 'success');
    } catch {
      notify("Não foi possível alterar a data limite", 'danger');
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
    notify("Justificativa é obrigatória", 'danger');
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
      notify(
          tipoReabertura.value === 'cadastro'
              ? 'Cadastro reaberto'
              : 'Revisão reaberta',
          'success',
      );
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
  try {
    await processosStore.enviarLembrete(props.codProcesso, subprocesso.value.unidade.codigo);
    await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value);
    modalLembreteAberto.value = false;
    notify('Lembrete enviado', 'success');
  } catch {
    notify('Não foi possível enviar o lembrete', 'danger');
  }
}
</script>
