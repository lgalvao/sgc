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
      <SubprocessoResumoHeader
          :format-data-simples="formatDataSimples"
          :format-situacao-subprocesso="formatSituacaoSubprocesso"
          :format-tipo-responsabilidade="formatTipoResponsabilidade"
          :mostrar-acoes-cabecalho="mostrarAcoesCabecalho"
          :mostrar-alterar-data-limite="mostrarAlterarDataLimite"
          :mostrar-enviar-lembrete="mostrarEnviarLembrete"
          :mostrar-reabrir-cadastro="mostrarReabrirCadastro"
          :mostrar-reabrir-revisao="mostrarReabrirRevisao"
          :pode-alterar-data-limite="podeAlterarDataLimite"
          :pode-enviar-lembrete="podeEnviarLembrete"
          :pode-reabrir-cadastro="podeReabrirCadastro"
          :pode-reabrir-revisao="podeReabrirRevisao"
          :sigla-unidade-fallback="props.siglaUnidade"
          :subprocesso="subprocesso"
          @abrir-alterar-data-limite="abrirModalAlterarDataLimite"
          @abrir-reabrir-cadastro="abrirModalReabrirCadastro"
          @abrir-reabrir-revisao="abrirModalReabrirRevisao"
          @confirmar-enviar-lembrete="confirmarEnviarLembrete"
      />

      <SubprocessoCards
          v-if="codigoSubprocesso"
          :cod-processo="props.codProcesso"
          :cod-subprocesso="codigoSubprocesso"
          :mapa="null"
          :sigla-unidade="props.siglaUnidade"
          :situacao="subprocesso.situacao"
          :subprocesso="subprocesso"
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
            small
            responsive
            show-empty
            stacked="md"
        >
          <template #empty>
            <EmptyState
                class="mb-0"
                data-testid="empty-state-movimentacoes"
                :description="TEXTOS.subprocesso.MOVIMENTACOES_VAZIO_TEXTO"
                icon="bi-arrow-left-right"
                :title="TEXTOS.subprocesso.MOVIMENTACOES_VAZIO_TITULO"
            />
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
    <div v-else-if="subprocessoStore.erroIntegracaoContexto" class="py-2">
      <BAlert
          :model-value="true"
          variant="danger"
          dismissible
          @dismissed="subprocessoStore.limparErroIntegracao()"
      >
        {{ subprocessoStore.erroIntegracaoContexto.message }}
        <div v-if="subprocessoStore.erroIntegracaoContexto.details">
          <small>Detalhes: {{ subprocessoStore.erroIntegracaoContexto.details }}</small>
        </div>
      </BAlert>
    </div>
    <div v-else-if="erroNaoEncontrado" class="text-center py-5">
      <i class="bi bi-exclamation-triangle fs-1 text-warning mb-3 d-block"></i>
      <h3>{{ TEXTOS.subprocesso.NAO_ENCONTRADO_TITULO }}</h3>
      <p class="text-muted">{{ TEXTOS.subprocesso.NAO_ENCONTRADO_DESC }}</p>
      <BButton to="/painel" variant="primary" class="mt-3">Voltar para o Painel</BButton>
    </div>
    <CarregamentoPagina v-else class="py-5" />
  </LayoutPadrao>

  <SubprocessoModal
      :data-limite-atual="dataLimite"
      :etapa-atual="subprocesso?.etapaAtual ?? null"
      :loading="loadingDataLimite"
      :mostrar-modal="mostrarModalAlterarDataLimite"
      :ultima-data-limite-subprocesso="subprocesso?.ultimaDataLimiteSubprocesso ? parseDate(subprocesso.ultimaDataLimiteSubprocesso) : null"
      @fechar-modal="fecharModalAlterarDataLimite"
      @confirmar-alteracao="confirmarAlteracaoDataLimite"
  />

  <!-- Modal para reabrir cadastro/revisão -->
  <ModalConfirmacao
      v-model="mostrarModalReabrir"
      :auto-close="false"
      :loading="loadingReabertura"
      :titulo="tipoReabertura === 'cadastro' ? TEXTOS.subprocesso.REABRIR_CADASTRO_TITULO : TEXTOS.subprocesso.REABRIR_REVISAO_TITULO"
      :ok-title="TEXTOS.comum.BOTAO_REABRIR"
      test-codigo-confirmar="btn-confirmar-reabrir"
      variant="success"
      @confirmar="confirmarReabertura"
  >
    <p>{{ TEXTOS.subprocesso.REABRIR_JUSTIFICATIVA_PREFIXO }} {{
        tipoReabertura === 'cadastro' ? TEXTOS.subprocesso.CADASTRO : TEXTOS.subprocesso.REVISAO_CADASTRO
      }} <span aria-hidden="true" class="text-danger">*</span>:</p>
    <BFormTextarea
        id="justificativaReabertura"
        v-model="justificativaReabertura"
        :state="mensagemErroJustificativa ? false : null"
        data-testid="inp-justificativa-reabrir"
        :placeholder="TEXTOS.subprocesso.REABRIR_JUSTIFICATIVA_PLACEHOLDER"
        rows="3"
    />
    <BFormInvalidFeedback
        :state="mensagemErroJustificativa ? false : null"
        class="d-block"
        data-testid="txt-reabertura-pendencia-justificativa"
    >
      {{ mensagemErroJustificativa }}
    </BFormInvalidFeedback>
  </ModalConfirmacao>

  <ModalConfirmacao
      v-model="modalLembreteAberto"
      :auto-close="false"
      :loading="loadingLembrete"
      :ok-title="TEXTOS.subprocesso.BOTAO_CONFIRMAR_LEMBRETE"
      test-codigo-confirmar="btn-confirmar-enviar-lembrete"
      :titulo="TEXTOS.subprocesso.LEMBRETE_TITULO"
      variant="success"
      @confirmar="enviarLembreteConfirmado"
  >
    <p data-testid="txt-modelo-lembrete">
      {{ TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(subprocesso?.unidade?.sigla ?? '') }}
    </p>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BCard,
  BCardBody,
  BDropdown,
  BDropdownItemButton,
  BFormInvalidFeedback,
  BFormTextarea,
  BTable,
  useToast
} from "bootstrap-vue-next";
import {computed, onActivated, onMounted, ref, type Ref, watch} from "vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import SubprocessoCards from "@/components/processo/SubprocessoCards.vue";
import SubprocessoModal from "@/components/processo/SubprocessoModal.vue";
import SubprocessoResumoHeader from "@/components/processo/SubprocessoResumoHeader.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import {useNotification} from "@/composables/useNotification";
import {useFluxoSubprocesso} from "@/composables/useFluxoSubprocesso";
import {enviarLembrete as enviarLembreteService} from "@/services/processoService";

import {useAcesso} from "@/composables/useAcesso";
import {type Movimentacao, type ResponsavelDto, type SubprocessoDetalhe, TipoProcesso} from "@/types/tipos";
import {formatDateTimeBR, parseDate} from "@/utils";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {TEXTOS} from "@/constants/textos";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import {useToastStore} from "@/stores/toast";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

const props = defineProps<{ codProcesso: number; siglaUnidade: string; codSubprocesso?: number }>();

function formatDataSimples(dataStr: string | null): string {
  if (!dataStr) return '';
  const data = new Date(dataStr);
  return data.toLocaleDateString('pt-BR');
}

function formatTipoResponsabilidade(resp: ResponsavelDto | null): string {
  if (!resp?.tipo) return '';
  if (resp.tipo === 'Substituição' && resp.dataFim) {
    return `Substituição (até ${formatDataSimples(resp.dataFim)})`;
  } else if (resp.tipo === 'Atribuição temporária' && resp.dataFim) {
    return `Atrib. temporária (até ${formatDataSimples(resp.dataFim)})`;
  }
  return resp.tipo;
}

const subprocessoStore = useSubprocessoStore();
const mapasStore = useMapasStore();
const fluxoSubprocesso = useFluxoSubprocesso();
const {notificacao, notify, clear} = useNotification();
const toastStore = useToastStore();
const toast = useToast();
const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const tipoReabertura = ref<'cadastro' | 'revisao'>('cadastro');
const justificativaReabertura = ref('');
const codigoSubprocesso = ref<number | null>(null);
const erroNaoEncontrado = ref(false);
const modalLembreteAberto = ref(false);
const mostrarModalAlterarDataLimite = ref(false);
const mostrarModalReabrir = ref(false);
const loadingDataLimite = ref(false);
const loadingReabertura = ref(false);
const loadingLembrete = ref(false);
const carregamentoInicialConcluido = ref(false);

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
    () => subprocessoStore.contextoEdicao?.detalhes ?? null,
);

const {
  podeAlterarDataLimite,
  podeReabrirCadastro,
  podeReabrirRevisao,
  podeEnviarLembrete,
  mostrarAlterarDataLimite,
  mostrarReabrirCadastro,
  mostrarReabrirRevisao,
  mostrarEnviarLembrete
} = useAcesso(subprocesso);

const mostrarAcoesCabecalho = computed(() =>
    mostrarAlterarDataLimite.value
    || mostrarReabrirCadastro.value
    || mostrarReabrirRevisao.value
    || mostrarEnviarLembrete.value
);

const movimentacoes = computed<Movimentacao[]>(
    () => subprocesso.value?.movimentacoes ?? [],
);
const dataLimite = computed(() => {
  if (subprocesso.value?.prazoEtapaAtual) {
    return parseDate(subprocesso.value.prazoEtapaAtual);
  }
  const ultimaDataLimite = subprocesso.value?.ultimaDataLimiteSubprocesso;
  return ultimaDataLimite ? parseDate(ultimaDataLimite) : null;
});

const mensagemErroJustificativa = computed(() => {
  return deveExibirErro(!justificativaReabertura.value.trim()) ? "Informe a justificativa para reabrir." : "";
});

async function executarComCarregamento(loading: Ref<boolean>, acao: () => Promise<void>) {
  loading.value = true;
  try {
    await acao();
  } finally {
    loading.value = false;
  }
}

function exibirToastPendente() {
  const pendente = toastStore.consumePending();
  if (pendente) {
    toast.create({
      props: {
        body: pendente.body,
        variant: 'success',
        modelValue: 4000,
        pos: 'bottom-end',
        noProgress: true,
      }
    });
  }
}

async function carregarSubprocesso(limpar = false) {
  const resultadoDireto = typeof props.codSubprocesso === "number"
      ? await subprocessoStore.garantirContextoEdicao(props.codSubprocesso, limpar)
      : null;

  if (resultadoDireto) {
    codigoSubprocesso.value = resultadoDireto.detalhes.codigo;
    erroNaoEncontrado.value = false;
    return;
  }

  const resultado = await subprocessoStore.garantirContextoEdicaoPorProcessoEUnidade(
      props.codProcesso,
      props.siglaUnidade,
      limpar,
  );

  if (!resultado) {
    codigoSubprocesso.value = null;
    erroNaoEncontrado.value = !subprocessoStore.erroIntegracaoContexto;
    return;
  }

  codigoSubprocesso.value = resultado.codigo;
  erroNaoEncontrado.value = false;
}

onMounted(async () => {
  exibirToastPendente();
  await carregarSubprocesso(true);
  carregamentoInicialConcluido.value = true;
});

watch(
    () => [props.codProcesso, props.siglaUnidade, props.codSubprocesso],
    async () => {
      await carregarSubprocesso(true);
    }
);

onActivated(async () => {
  exibirToastPendente();
  if (!carregamentoInicialConcluido.value) {
    return;
  }
  if (codigoSubprocesso.value && subprocessoStore.dadosValidosEdicao(codigoSubprocesso.value)) {
    return;
  }
  await carregarSubprocesso();
});

async function atualizarSubprocessoAtual() {
  if (!codigoSubprocesso.value) {
    return;
  }

  mapasStore.invalidar();
  await subprocessoStore.garantirContextoEdicao(codigoSubprocesso.value, true);
}

function abrirModalAlterarDataLimite() {
  if (podeAlterarDataLimite.value) {
    mostrarModalAlterarDataLimite.value = true;
  } else {
    notify(TEXTOS.subprocesso.ERRO_SEM_PERMISSAO_DATA, 'danger');
  }
}

function fecharModalAlterarDataLimite() {
  mostrarModalAlterarDataLimite.value = false;
}

async function confirmarAlteracaoDataLimite(novaData: string) {
  if (!novaData || !subprocesso.value) {
    return;
  }

  await executarComCarregamento(loadingDataLimite, async () => {
    try {
      await fluxoSubprocesso.alterarDataLimiteSubprocesso(
          subprocesso.value!.codigo,
          {novaData},
      );
      fecharModalAlterarDataLimite();
      notify(TEXTOS.subprocesso.SUCESSO_DATA_ALTERADA, 'success');
      await atualizarSubprocessoAtual();
    } catch {
      notify(TEXTOS.subprocesso.ERRO_DATA_ALTERADA, 'danger');
    }
  });
}

// CDU-32/33: Reabrir cadastro/revisão
function abrirModalReabrirCadastro() {
  resetarValidacao();
  tipoReabertura.value = 'cadastro';
  justificativaReabertura.value = '';
  mostrarModalReabrir.value = true;
}

function abrirModalReabrirRevisao() {
  resetarValidacao();
  tipoReabertura.value = 'revisao';
  justificativaReabertura.value = '';
  mostrarModalReabrir.value = true;
}

function fecharModalReabrir() {
  mostrarModalReabrir.value = false;
  justificativaReabertura.value = '';
}

async function confirmarReabertura() {
  if (!codigoSubprocesso.value) return;

  if (!validarSubmissao(Boolean(justificativaReabertura.value.trim()))) {
    void focarPrimeiroErroInvalido();
    return;
  }

  await executarComCarregamento(loadingReabertura, async () => {
    const isRevisao = tipoReabertura.value === 'revisao';
    const sucesso = await fluxoSubprocesso.reabrirCadastro(codigoSubprocesso.value!, justificativaReabertura.value, isRevisao);

    if (sucesso) {
      fecharModalReabrir();
      exibirToastPendente();
      await atualizarSubprocessoAtual();
    }
  });
}

async function confirmarEnviarLembrete() {
  if (!subprocesso.value) {
    return;
  }
  modalLembreteAberto.value = true;
  return true;
}

async function enviarLembreteConfirmado() {
  if (!subprocesso.value || !codigoSubprocesso.value || loadingLembrete.value) {
    return;
  }
  loadingLembrete.value = true;
  try {
    await enviarLembreteService(props.codProcesso, subprocesso.value.unidade.codigo);
    await subprocessoStore.garantirContextoEdicao(codigoSubprocesso.value, true);
    modalLembreteAberto.value = false;
    notify(TEXTOS.subprocesso.SUCESSO_LEMBRETE_ENVIADO, 'success');
  } catch {
    notify(TEXTOS.subprocesso.ERRO_LEMBRETE_ENVIADO, 'danger');
  } finally {
    loadingLembrete.value = false;
  }
}

defineExpose({
  codigoSubprocesso,
  formatDataSimples,
  formatTipoResponsabilidade,
  rowAttrMovimentacao,
  carregarSubprocesso,
  atualizarSubprocessoAtual,
  confirmarEnviarLembrete,
  enviarLembreteConfirmado,
  confirmarReabertura,
  confirmarAlteracaoDataLimite,
  abrirModalAlterarDataLimite,
  fecharModalAlterarDataLimite,
  abrirModalReabrirCadastro,
  abrirModalReabrirRevisao,
  fecharModalReabrir,
  exibirToastPendente,
  mostrarModalAlterarDataLimite,
  mostrarModalReabrir,
  modalLembreteAberto,
  loadingLembrete,
  justificativaReabertura,
  onActivated
});
</script>

<style scoped>
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  animation: fadeIn 0.5s ease-in-out;
}

.loading-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
