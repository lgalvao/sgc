<template>
  <div>
    <PageHeader
        v-if="exibirCabecalho"
        :subtitle="unidade?.unidadeSigla"
        title="Detalhes do subprocesso"
    >
      <template #actions>
        <BButton
            v-if="exibirBotaoVoltar"
            size="sm"
            variant="outline-secondary"
            @click="void router.back()"
        >
          <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
          {{ TEXTOS.diagnostico.BTN_VOLTAR }}
        </BButton>
      </template>
    </PageHeader>

    <AppAlertaAcao
        :feedback="feedbackAcao"
        data-testid="alert-subprocesso-diagnostico-feedback"
        @dismissed="limparFeedbackAcao"
    />

    <BCard class="mb-4">
      <EmptyState
          v-if="servidores.length === 0"
          :description="TEXTOS.diagnostico.VAZIO_EQUIPE_TEXTO"
          :title="TEXTOS.diagnostico.VAZIO_EQUIPE_TITULO"
          icon="bi-people"
      />
      <BTable
          v-else
          :fields="colunasServidores"
          :items="servidores"
          data-testid="tbl-servidores-diagnostico"
          hover
          responsive
          small
      >
        <template #cell(situacaoServidor)="{ item }">
          <BBadge :variant="varianteSituacaoServidor(item.situacaoServidor)">
            {{ formatarSituacaoServidor(item.situacaoServidor) }}
          </BBadge>
        </template>
        <template #cell(acoes)="{ item }">
          <BDropdown
              v-if="ehChefe"
              :data-testid="`dropdown-acoes-${item.servidorTitulo}`"
              right
              size="sm"
              :text="TEXTOS.diagnostico.COLUNA_ACOES"
              teleport-to="body"
              toggle-class="text-nowrap"
              variant="outline-secondary"
          >
            <BDropdownItemButton
                :data-testid="`btn-manter-consenso-${item.servidorTitulo}`"
                :disabled="!item.podeManterConsenso"
                role="menuitem"
                @click="navegarParaConsenso(item.servidorTitulo)"
            >
              {{ TEXTOS.diagnostico.BTN_MANTER_CONSENSO }}
            </BDropdownItemButton>
            <BDropdownItemButton
                :data-testid="`btn-impossibilitar-${item.servidorTitulo}`"
                :disabled="!item.podeImpossibilitar"
                role="menuitem"
                @click="abrirModalImpossibilitar(item)"
            >
              {{ TEXTOS.diagnostico.BTN_IMPOSSIBILITAR }}
            </BDropdownItemButton>
            <BDropdownItemButton
                :data-testid="`btn-desfazer-impossibilidade-${item.servidorTitulo}`"
                :disabled="!item.podePermitirAvaliacao"
                role="menuitem"
                @click="abrirModalPermitirAvaliacao(item)"
            >
              {{ TEXTOS.diagnostico.BTN_PERMITIR_AVALIACAO }}
            </BDropdownItemButton>
          </BDropdown>
        </template>
      </BTable>
    </BCard>

    <div class="d-flex gap-2 flex-wrap mb-4">
      <BButton
          v-if="podeConcluir && exibirBotaoConcluirDiagnostico"
           :disabled="concluindo || !habilitarConcluir"
          data-testid="btn-concluir-diagnostico"
          variant="success"
          @click="abrirModalConcluir"
      >
        <BSpinner v-if="concluindo" aria-hidden="true" class="me-1" small/>
        {{ TEXTOS.diagnostico.BTN_CONCLUIR_DIAGNOSTICO }}
      </BButton>
      <BButton
          v-if="podeValidar"
          :disabled="validando"
          data-testid="btn-validar-diagnostico"
          variant="success"
          @click="abrirModalValidar"
      >
        <BSpinner v-if="validando" aria-hidden="true" class="me-1" small/>
        {{ TEXTOS.diagnostico.BTN_VALIDAR }}
      </BButton>
      <BButton
          v-if="podeDevolver"
          :disabled="devolvendo"
          data-testid="btn-devolver-diagnostico"
          variant="warning"
          @click="abrirModalDevolver"
      >
        <BSpinner v-if="devolvendo" aria-hidden="true" class="me-1" small/>
        {{ TEXTOS.diagnostico.BTN_DEVOLVER }}
      </BButton>
      <BButton
          v-if="podeHomologar"
          :disabled="homologando"
          data-testid="btn-homologar-diagnostico"
          variant="primary"
          @click="abrirModalHomologar"
      >
        <BSpinner v-if="homologando" aria-hidden="true" class="me-1" small/>
        {{ TEXTOS.diagnostico.BTN_HOMOLOGAR }}
      </BButton>
    </div>

    <DiagnosticoFluxoModais
        :concluindo="concluindo"
        :devolvendo="devolvendo"
        :erro-concluir="erroConcluirModal"
        :erro-devolver="erroDevolver?.message || null"
        :erro-homologar="erroHomologar?.message || null"
        :erro-impossibilitar="erroImpossibilitar?.message || null"
        :erro-validar="erroValidar?.message || null"
        :feedback-justificativa-devolver="erroJustificativaDevolver"
        :feedback-justificativa-impossibilidade="erroJustificativaImpossibilidade"
        :homologando="homologando"
        :impossibilitando="impossibilitando"
        :justificativa-devolver="justificativaDevolver"
        :justificativa-impossibilidade="justificativaImpossibilidade"
        :modal-concluir-aberto="modalConcluirAberto"
        :modal-devolver-aberto="modalDevolverAberto"
        :modal-homologar-aberto="modalHomologarAberto"
        :modal-impossibilitar-aberto="modalImpossibilitarAberto"
        :modal-permitir-avaliacao-aberto="modalPermitirAvaliacaoAberto"
        :modal-validar-aberto="modalValidarAberto"
        :observacoes-homologar="observacoesHomologar"
        :observacoes-validar="observacoesValidar"
        :permitindo="permitindo"
        :test-id-confirmar-concluir="'btn-confirmar-concluir-diagnostico'"
        :test-id-confirmar-devolver="'btn-confirmar-devolver'"
        :test-id-confirmar-homologar="'btn-confirmar-homologar'"
        :test-id-confirmar-impossibilitar="'btn-confirmar-impossibilitar'"
        :test-id-confirmar-permitir-avaliacao="'btn-confirmar-permitir-avaliacao'"
        :test-id-confirmar-validar="'btn-confirmar-validar'"
        :texto-impossibilitar="servidorSelecionado ? TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_MENSAGEM(servidorSelecionado.servidorNome) : ''"
        :texto-permitir-avaliacao="servidorSelecionado ? TEXTOS.diagnostico.MODAL_PERMITIR_AVALIACAO_MENSAGEM(servidorSelecionado.servidorNome) : ''"
        :validando="validando"
        @confirmar-concluir="confirmarConcluir"
        @confirmar-devolver="confirmarDevolver"
        @confirmar-homologar="confirmarHomologar"
        @confirmar-impossibilitar="confirmarImpossibilitar"
        @confirmar-permitir-avaliacao="confirmarPermitirAvaliacao"
        @confirmar-validar="confirmarValidar"
        @update:justificativa-devolver="justificativaDevolver = $event"
        @update:justificativa-impossibilidade="justificativaImpossibilidade = $event"
        @update:modal-concluir-aberto="modalConcluirAberto = $event"
        @update:modal-devolver-aberto="modalDevolverAberto = $event"
        @update:modal-homologar-aberto="modalHomologarAberto = $event"
        @update:modal-impossibilitar-aberto="modalImpossibilitarAberto = $event"
        @update:modal-permitir-avaliacao-aberto="modalPermitirAvaliacaoAberto = $event"
        @update:modal-validar-aberto="modalValidarAberto = $event"
        @update:observacoes-homologar="observacoesHomologar = $event"
        @update:observacoes-validar="observacoesValidar = $event"
    />
  </div>
</template>

<!-- eslint-disable max-lines -->
<script lang="ts" setup>
import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {normalizarErro} from '@/utils/apiError/normalizer';
import {useToast} from '@/composables/useToast';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {BBadge, BButton, BCard, BDropdown, BDropdownItemButton, BSpinner, BTable,} from 'bootstrap-vue-next';
import DiagnosticoFluxoModais from '@/components/diagnostico/DiagnosticoFluxoModais.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import AppAlertaAcao, {type FeedbackAcao} from '@/components/comum/AppAlertaAcao.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {useDiagnosticoUnidade} from '@/composables/useDiagnosticoUnidade';
import {useFluxoDiagnostico} from '@/composables/useFluxoDiagnostico';
import {TEXTOS} from '@/constants/textos';
import type {ServidorDiagnostico, SituacaoAvaliacaoServidor} from '@/types/diagnostico-competencias';

const props = withDefaults(defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
  exibirCabecalho?: boolean;
  exibirBotaoVoltar?: boolean;
  exibirBotaoConcluirDiagnostico?: boolean;
}>(), {
  exibirCabecalho: true,
  exibirBotaoVoltar: true,
  exibirBotaoConcluirDiagnostico: true,
});

const router = useRouter();
const {registrarPendente} = useToast();
const {
  podeCriarConsenso,
  podeConcluirDiagnostico,
  habilitarConcluirDiagnostico,
  habilitarValidarDiagnostico,
  habilitarDevolverDiagnostico,
  habilitarHomologarDiagnostico,
} = useDiagnosticoPermissoes(props.codSubprocesso);
const {unidade, servidores} = useDiagnosticoUnidade(props.codSubprocesso);
const {
  concluindo,
  validando,
  devolvendo,
  homologando,
  impossibilitando,
  permitindo,
  erroConcluir,
  erroValidacaoConcluir,
  erroValidar,
  erroValidacaoValidar,
  erroDevolver,
  erroValidacaoDevolver,
  erroHomologar,
  erroValidacaoHomologar,
  erroImpossibilitar,
  validarConclusaoDiagnostico,
  validarAcaoValidarDiagnostico,
  validarAcaoDevolverDiagnostico,
  validarAcaoHomologarDiagnostico,
  concluirDiagnostico,
  validarDiagnostico,
  devolverDiagnostico,
  homologarDiagnostico,
  impossibilitarAvaliacao,
  permitirAvaliacao,
} = useFluxoDiagnostico(props.codSubprocesso);

const erroConcluirModal = ref('');
const feedbackAcao = ref<FeedbackAcao | null>(null);
const modalConcluirAberto = ref(false);
const modalValidarAberto = ref(false);
const modalDevolverAberto = ref(false);
const modalHomologarAberto = ref(false);
const modalImpossibilitarAberto = ref(false);
const modalPermitirAvaliacaoAberto = ref(false);
const observacoesValidar = ref('');
const justificativaDevolver = ref('');
const erroJustificativaDevolver = ref('');
const observacoesHomologar = ref('');
const servidorSelecionado = ref<ServidorDiagnostico | null>(null);
const justificativaImpossibilidade = ref('');
const erroJustificativaImpossibilidade = ref('');

function limparFeedbackAcao() {
  feedbackAcao.value = null;
}

function registrarFeedbackAcao(variante: FeedbackAcao["variante"], mensagem?: string | null) {
  feedbackAcao.value = {
    variante,
    mensagem: mensagem?.trim() || TEXTOS.diagnostico.ERRO_SALVAR,
  };
}

function registrarErro(mensagem?: string | null) {
  registrarFeedbackAcao('danger', mensagem);
}

const ehChefe = computed(() => podeCriarConsenso.value);
const podeConcluir = computed(() => podeConcluirDiagnostico.value);
const habilitarConcluir = computed(() => habilitarConcluirDiagnostico.value);
const podeValidar = computed(() => habilitarValidarDiagnostico.value);
const podeDevolver = computed(() => habilitarDevolverDiagnostico.value);
const podeHomologar = computed(() => habilitarHomologarDiagnostico.value);

function navegarParaConsenso(servidorTitulo: string) {
  void router.push({
    name: 'ConsensoDiagnostico',
    params: {
      codSubprocesso: props.codSubprocesso,
      siglaUnidade: props.siglaUnidade,
      servidorTitulo,
    },
  });
}

function abrirModalImpossibilitar(servidor: ServidorDiagnostico) {
  servidorSelecionado.value = servidor;
  justificativaImpossibilidade.value = '';
  erroJustificativaImpossibilidade.value = '';
  modalImpossibilitarAberto.value = true;
}

function abrirModalPermitirAvaliacao(servidor: ServidorDiagnostico) {
  servidorSelecionado.value = servidor;
  modalPermitirAvaliacaoAberto.value = true;
}

async function confirmarPermitirAvaliacao() {
  if (!servidorSelecionado.value) return;
  try {
    await permitirAvaliacao(servidorSelecionado.value.servidorTitulo);
    modalPermitirAvaliacaoAberto.value = false;
  } catch {
    registrarErro(TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

async function confirmarImpossibilitar() {
  if (!justificativaImpossibilidade.value.trim()) {
    erroJustificativaImpossibilidade.value = TEXTOS.diagnostico.ERRO_JUSTIFICATIVA_OBRIGATORIA;
    return;
  }
  if (!servidorSelecionado.value) return;
  try {
    await impossibilitarAvaliacao(
      servidorSelecionado.value.servidorTitulo,
      justificativaImpossibilidade.value,
    );
    modalImpossibilitarAberto.value = false;
  } catch {
    registrarErro(TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

async function abrirModalValidar() {
  observacoesValidar.value = '';
  try {
    await validarAcaoValidarDiagnostico();
    modalValidarAberto.value = true;
  } catch (erro) {
    registrarErro(normalizarErro(erro).mensagem
      ?? erroValidacaoValidar.value?.message
      ?? TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

async function abrirModalConcluir() {
  erroConcluirModal.value = '';
  try {
    await validarConclusaoDiagnostico();
    modalConcluirAberto.value = true;
  } catch (erro) {
    registrarErro(normalizarErro(erro).mensagem
      ?? erroValidacaoConcluir.value?.message
      ?? TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

async function abrirModalDevolver() {
  justificativaDevolver.value = '';
  erroJustificativaDevolver.value = '';
  try {
    await validarAcaoDevolverDiagnostico();
    modalDevolverAberto.value = true;
  } catch (erro) {
    registrarErro(normalizarErro(erro).mensagem
      ?? erroValidacaoDevolver.value?.message
      ?? TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

async function abrirModalHomologar() {
  observacoesHomologar.value = '';
  try {
    await validarAcaoHomologarDiagnostico();
    modalHomologarAberto.value = true;
  } catch (erro) {
    registrarErro(normalizarErro(erro).mensagem
      ?? erroValidacaoHomologar.value?.message
      ?? TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

async function confirmarConcluir() {
  try {
    await concluirDiagnostico();
    modalConcluirAberto.value = false;
    registrarPendente(TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_CONCLUIDO);
    await router.push({name: 'Painel'});
  } catch (erro) {
    erroConcluirModal.value = normalizarErro(erro).mensagem
      ?? erroConcluir.value?.message
      ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

async function confirmarValidar() {
  try {
    await validarDiagnostico(observacoesValidar.value || undefined);
    modalValidarAberto.value = false;
    limparFeedbackAcao();
  } catch {
    registrarErro(erroValidar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

async function confirmarDevolver() {
  if (!justificativaDevolver.value.trim()) {
    erroJustificativaDevolver.value = TEXTOS.diagnostico.ERRO_JUSTIFICATIVA_OBRIGATORIA;
    return;
  }
  try {
    await devolverDiagnostico(justificativaDevolver.value);
    modalDevolverAberto.value = false;
    limparFeedbackAcao();
  } catch {
    registrarErro(erroDevolver.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

async function confirmarHomologar() {
  try {
    await homologarDiagnostico(observacoesHomologar.value || undefined);
    modalHomologarAberto.value = false;
    limparFeedbackAcao();
  } catch {
    registrarErro(erroHomologar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

function varianteSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor) {
  switch (situacaoServidor) {
    case 'CONSENSO_APROVADO':
      return 'success';
    case 'AVALIACAO_IMPOSSIBILITADA':
      return 'secondary';
    case 'CONSENSO_CRIADO':
      return 'warning';
    case 'AUTOAVALIACAO_CONCLUIDA':
      return 'info';
    default:
      return 'light';
  }
}

function formatarSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor): string {
  const mapa: Record<SituacaoAvaliacaoServidor, string> = {
    AUTOAVALIACAO_NAO_INICIADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
    AUTOAVALIACAO_CONCLUIDA: TEXTOS.diagnostico.SITUACAO_AUTOAVALIACAO_CONCLUIDA,
    CONSENSO_CRIADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_CRIADO,
    CONSENSO_APROVADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_APROVADO,
    AVALIACAO_IMPOSSIBILITADA: TEXTOS.diagnostico.SITUACAO_IMPOSSIBILITADA,
  };
  return mapa[situacaoServidor] ?? situacaoServidor;
}

const colunasServidores = computed(() => [
  {key: 'servidorNome', label: TEXTOS.diagnostico.COLUNA_SERVIDOR},
  {key: 'situacaoServidor', label: TEXTOS.diagnostico.COLUNA_SITUACAO},
  ...(ehChefe.value ? [{key: 'acoes', label: TEXTOS.diagnostico.COLUNA_ACOES}] : []),
]);
</script>
