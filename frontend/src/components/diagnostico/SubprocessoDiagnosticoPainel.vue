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

    <AppAlert
        v-if="erroMensagem"
        :mensagem="erroMensagem"
        variante="danger"
        @dismissed="erroMensagem = ''"
    />
    <AppAlert
        v-if="alertaSucesso"
        :mensagem="alertaSucesso"
        variante="success"
        @dismissed="alertaSucesso = ''"
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
              toggle-class="text-nowrap"
              variant="outline-secondary"
          >
            <BDropdownItemButton
                :data-testid="`btn-manter-consenso-${item.servidorTitulo}`"
                @click="navegarParaConsenso(item.servidorTitulo)"
            >
              {{ TEXTOS.diagnostico.BTN_MANTER_CONSENSO }}
            </BDropdownItemButton>
            <BDropdownItemButton
                :data-testid="`btn-impossibilitar-${item.servidorTitulo}`"
                :disabled="item.situacaoServidor === 'AVALIACAO_IMPOSSIBILITADA' || item.situacaoServidor === 'CONSENSO_APROVADO'"
                @click="abrirModalImpossibilitar(item)"
            >
              {{ TEXTOS.diagnostico.BTN_IMPOSSIBILITAR }}
            </BDropdownItemButton>
            <BDropdownItemButton
                :data-testid="`btn-desfazer-impossibilidade-${item.servidorTitulo}`"
                :disabled="item.situacaoServidor !== 'AVALIACAO_IMPOSSIBILITADA'"
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
          v-if="podeConcluir"
          :disabled="concluindo"
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

    <BModal
        v-model="modalImpossibilitarAberto"
        :title="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_TITULO"
        centered
    >
      <p v-if="servidorSelecionado" class="mb-3">
        {{ TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_MENSAGEM(servidorSelecionado.servidorNome) }}
      </p>
      <BFormTextarea
          v-model="justificativaImpossibilidade"
          :placeholder="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_PLACEHOLDER"
          data-testid="textarea-justificativa-impossibilidade"
          rows="3"
      />
      <BFormText v-if="erroJustificativaImpossibilidade" class="text-danger">
        {{ erroJustificativaImpossibilidade }}
      </BFormText>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalImpossibilitarAberto = false">Cancelar</BButton>
        <BButton
            :disabled="impossibilitando"
            data-testid="btn-confirmar-impossibilitar"
            variant="danger"
            @click="confirmarImpossibilitar"
        >
          <BSpinner v-if="impossibilitando" aria-hidden="true" class="me-1" small/>
          Indicar impossibilidade
        </BButton>
      </template>
    </BModal>

    <BModal
        v-model="modalPermitirAvaliacaoAberto"
        :title="TEXTOS.diagnostico.MODAL_PERMITIR_AVALIACAO_TITULO"
        centered
    >
      <p v-if="servidorSelecionado" class="mb-0">
        {{ TEXTOS.diagnostico.MODAL_PERMITIR_AVALIACAO_MENSAGEM(servidorSelecionado.servidorNome) }}
      </p>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalPermitirAvaliacaoAberto = false">Cancelar</BButton>
        <BButton
            :disabled="permitindo"
            data-testid="btn-confirmar-permitir-avaliacao"
            variant="success"
            @click="confirmarPermitirAvaliacao"
        >
          <BSpinner v-if="permitindo" aria-hidden="true" class="me-1" small/>
          Confirmar
        </BButton>
      </template>
    </BModal>

    <BModal
        v-model="modalConcluirAberto"
        :title="TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_TITULO"
        centered
    >
      <AppAlert
          v-if="erroConcluirModal"
          :mensagem="erroConcluirModal"
          @dismissed="erroConcluirModal = ''"
      />
      <p class="mb-0">{{ TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_MENSAGEM }}</p>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalConcluirAberto = false">Cancelar</BButton>
        <BButton
            :disabled="concluindo"
            data-testid="btn-confirmar-concluir-diagnostico"
            variant="success"
            @click="confirmarConcluir"
        >
          <BSpinner v-if="concluindo" aria-hidden="true" class="me-1" small/>
          Concluir diagnóstico
        </BButton>
      </template>
    </BModal>

    <BModal
        v-model="modalValidarAberto"
        :title="TEXTOS.diagnostico.MODAL_VALIDAR_TITULO"
        centered
    >
      <BFormTextarea
          v-model="observacoesValidar"
          :placeholder="TEXTOS.diagnostico.LABEL_OBSERVACOES"
          rows="3"
      />
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalValidarAberto = false">Cancelar</BButton>
        <BButton
            :disabled="validando"
            data-testid="btn-confirmar-validar"
            variant="success"
            @click="confirmarValidar"
        >
          <BSpinner v-if="validando" aria-hidden="true" class="me-1" small/>
          Aceitar
        </BButton>
      </template>
    </BModal>

    <BModal
        v-model="modalDevolverAberto"
        :title="TEXTOS.diagnostico.MODAL_DEVOLVER_TITULO"
        centered
    >
      <BFormTextarea
          v-model="justificativaDevolver"
          :placeholder="TEXTOS.diagnostico.MODAL_DEVOLVER_PLACEHOLDER"
          rows="3"
      />
      <BFormText v-if="erroJustificativaDevolver" class="text-danger">
        {{ erroJustificativaDevolver }}
      </BFormText>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalDevolverAberto = false">Cancelar</BButton>
        <BButton
            :disabled="devolvendo"
            data-testid="btn-confirmar-devolver"
            variant="warning"
            @click="confirmarDevolver"
        >
          <BSpinner v-if="devolvendo" aria-hidden="true" class="me-1" small/>
          Devolver
        </BButton>
      </template>
    </BModal>

    <BModal
        v-model="modalHomologarAberto"
        :title="TEXTOS.diagnostico.MODAL_HOMOLOGAR_TITULO"
        centered
    >
      <BFormTextarea
          v-model="observacoesHomologar"
          :placeholder="TEXTOS.diagnostico.LABEL_OBSERVACOES"
          rows="3"
      />
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalHomologarAberto = false">Cancelar</BButton>
        <BButton
            :disabled="homologando"
            data-testid="btn-confirmar-homologar"
            variant="primary"
            @click="confirmarHomologar"
        >
          <BSpinner v-if="homologando" aria-hidden="true" class="me-1" small/>
          Homologar
        </BButton>
      </template>
    </BModal>
  </div>
</template>

<!-- eslint-disable max-lines -->
<script lang="ts" setup>
import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {normalizarErro} from '@/utils/apiError/normalizer';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {
  BBadge,
  BButton,
  BCard,
  BDropdown,
  BDropdownItemButton,
  BFormText,
  BFormTextarea,
  BModal,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import PageHeader from '@/components/layout/PageHeader.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {useDiagnosticoUnidade} from '@/composables/useDiagnosticoUnidade';
import {useFluxoDiagnostico} from '@/composables/useFluxoDiagnostico';
import {useToastStore} from '@/stores/toast';
import {TEXTOS} from '@/constants/textos';
import type {ServidorDiagnostico, SituacaoAvaliacaoServidor} from '@/types/diagnostico-competencias';

const props = withDefaults(defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
  exibirCabecalho?: boolean;
  exibirBotaoVoltar?: boolean;
}>(), {
  exibirCabecalho: true,
  exibirBotaoVoltar: true,
});

const router = useRouter();
const toastStore = useToastStore();
const {
  podeCriarConsenso,
  podeConcluirDiagnostico,
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

const erroMensagem = ref('');
const erroConcluirModal = ref('');
const alertaSucesso = ref('');
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

const ehChefe = computed(() => podeCriarConsenso.value);
const podeConcluir = computed(() => podeConcluirDiagnostico.value);
const podeValidar = computed(() => habilitarValidarDiagnostico.value);
const podeDevolver = computed(() => habilitarDevolverDiagnostico.value);
const podeHomologar = computed(() => habilitarHomologarDiagnostico.value);

function navegarParaConsenso(servidorTitulo: string) {
  const servidor = servidores.value.find((item) => item.servidorTitulo === servidorTitulo);
  void router.push({
    name: 'ConsensoDiagnostico',
    params: {
      codSubprocesso: props.codSubprocesso,
      siglaUnidade: props.siglaUnidade,
      servidorTitulo,
    },
    query: servidor?.servidorNome ? {servidorNome: servidor.servidorNome} : undefined,
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
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_IMPOSSIBILIDADE_REVERTIDA;
  } catch {
    erroMensagem.value = TEXTOS.diagnostico.ERRO_SALVAR;
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
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_IMPOSSIBILITADO;
  } catch {
    erroMensagem.value = TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

async function abrirModalValidar() {
  observacoesValidar.value = '';
  try {
    await validarAcaoValidarDiagnostico();
    modalValidarAberto.value = true;
  } catch (erro) {
    erroMensagem.value = normalizarErro(erro).mensagem
      ?? erroValidacaoValidar.value?.message
      ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

async function abrirModalConcluir() {
  erroConcluirModal.value = '';
  try {
    await validarConclusaoDiagnostico();
    modalConcluirAberto.value = true;
  } catch (erro) {
    erroMensagem.value = normalizarErro(erro).mensagem
      ?? erroValidacaoConcluir.value?.message
      ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

async function abrirModalDevolver() {
  justificativaDevolver.value = '';
  erroJustificativaDevolver.value = '';
  try {
    await validarAcaoDevolverDiagnostico();
    modalDevolverAberto.value = true;
  } catch (erro) {
    erroMensagem.value = normalizarErro(erro).mensagem
      ?? erroValidacaoDevolver.value?.message
      ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

async function abrirModalHomologar() {
  observacoesHomologar.value = '';
  try {
    await validarAcaoHomologarDiagnostico();
    modalHomologarAberto.value = true;
  } catch (erro) {
    erroMensagem.value = normalizarErro(erro).mensagem
      ?? erroValidacaoHomologar.value?.message
      ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

async function confirmarConcluir() {
  try {
    await concluirDiagnostico();
    modalConcluirAberto.value = false;
    toastStore.setPending(TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_CONCLUIDO);
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
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_VALIDADO;
  } catch {
    erroMensagem.value = erroValidar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR;
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
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_DEVOLVIDO;
  } catch {
    erroMensagem.value = erroDevolver.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

async function confirmarHomologar() {
  try {
    await homologarDiagnostico(observacoesHomologar.value || undefined);
    modalHomologarAberto.value = false;
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_HOMOLOGADO;
  } catch {
    erroMensagem.value = erroHomologar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR;
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
