<template>
  <div>
    <div
        v-if="exibirCabecalho"
        class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2"
    >
      <div>
        <h1 class="h4 mb-1">
          {{ TEXTOS.diagnostico.TITULO_MONITORAMENTO }}
        </h1>
        <div v-if="unidade" class="text-muted small">
          <strong>{{ unidade.unidadeSigla }}</strong>
        </div>
      </div>
      <BButton
          v-if="exibirBotaoVoltar"
          size="sm"
          variant="outline-secondary"
          @click="void router.back()"
      >
        <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
        {{ TEXTOS.diagnostico.BTN_VOLTAR }}
      </BButton>
    </div>

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
                :data-testid="`btn-manter-capacitacao-${item.servidorTitulo}`"
                @click="navegarParaCapacitacao(item.servidorTitulo)"
            >
              Manter situação de capacitação
            </BDropdownItemButton>
            <BDropdownItemButton
                :data-testid="`btn-impossibilitar-${item.servidorTitulo}`"
                :disabled="item.situacaoServidor === 'AVALIACAO_IMPOSSIBILITADA'"
                @click="abrirModalImpossibilitar(item)"
            >
              {{ TEXTOS.diagnostico.BTN_IMPOSSIBILITAR }}
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
        v-model="modalConcluirAberto"
        :title="TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_TITULO"
        centered
    >
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
          Concluir
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
          Validar
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

<script lang="ts" setup>
import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {useCacheDiagnostico} from '@/composables/useDiagnosticoCache';
import {impossibilitarAvaliacao} from '@/services/diagnosticoService';
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
import AppAlert from '@/components/comum/AppAlert.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {useMonitoramentoDiagnostico} from '@/composables/useMonitoramentoDiagnostico';
import {useFluxoDiagnostico} from '@/composables/useFluxoDiagnostico';
import {normalizarErro} from '@/utils/apiError/normalizer';
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
const cacheDiagnostico = useCacheDiagnostico();
const {
  podeCriarConsenso,
  habilitarConcluirDiagnostico,
  habilitarValidarDiagnostico,
  habilitarDevolverDiagnostico,
  habilitarHomologarDiagnostico,
} = useDiagnosticoPermissoes(props.codSubprocesso);
const {unidade, servidores} = useMonitoramentoDiagnostico(props.codSubprocesso);
const {
  concluindo,
  validando,
  devolvendo,
  homologando,
  erroConcluir,
  erroValidar,
  erroDevolver,
  erroHomologar,
  concluirDiagnostico,
  validarDiagnostico,
  devolverDiagnostico,
  homologarDiagnostico,
} = useFluxoDiagnostico(props.codSubprocesso);

const erroMensagem = ref('');
const alertaSucesso = ref('');
const modalConcluirAberto = ref(false);
const modalValidarAberto = ref(false);
const modalDevolverAberto = ref(false);
const modalHomologarAberto = ref(false);
const modalImpossibilitarAberto = ref(false);
const observacoesValidar = ref('');
const justificativaDevolver = ref('');
const erroJustificativaDevolver = ref('');
const observacoesHomologar = ref('');
const servidorSelecionado = ref<ServidorDiagnostico | null>(null);
const justificativaImpossibilidade = ref('');
const erroJustificativaImpossibilidade = ref('');
const impossibilitando = ref(false);

const ehChefe = computed(() => podeCriarConsenso.value);
const podeConcluir = computed(() => habilitarConcluirDiagnostico.value);
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

function navegarParaCapacitacao(servidorTitulo: string) {
  void router.push({
    name: 'OcupacoesCriticasDiagnostico',
    params: {
      codSubprocesso: props.codSubprocesso,
      siglaUnidade: props.siglaUnidade,
    },
    query: {
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

async function confirmarImpossibilitar() {
  if (!justificativaImpossibilidade.value.trim()) {
    erroJustificativaImpossibilidade.value = TEXTOS.diagnostico.ERRO_JUSTIFICATIVA_OBRIGATORIA;
    return;
  }
  if (!servidorSelecionado.value) return;
  try {
    impossibilitando.value = true;
    await impossibilitarAvaliacao(
      props.codSubprocesso,
      servidorSelecionado.value.servidorTitulo,
      {justificativa: justificativaImpossibilidade.value},
    );
    modalImpossibilitarAberto.value = false;
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_IMPOSSIBILITADO;
    cacheDiagnostico.invalidarUnidade(props.codSubprocesso);
  } catch {
    erroMensagem.value = TEXTOS.diagnostico.ERRO_SALVAR;
  } finally {
    impossibilitando.value = false;
  }
}

function abrirModalValidar() {
  observacoesValidar.value = '';
  modalValidarAberto.value = true;
}

function abrirModalConcluir() {
  modalConcluirAberto.value = true;
}

function abrirModalDevolver() {
  justificativaDevolver.value = '';
  erroJustificativaDevolver.value = '';
  modalDevolverAberto.value = true;
}

function abrirModalHomologar() {
  observacoesHomologar.value = '';
  modalHomologarAberto.value = true;
}

async function confirmarConcluir() {
  try {
    await concluirDiagnostico();
    modalConcluirAberto.value = false;
    await router.push({name: 'Painel'});
  } catch (erro) {
    erroMensagem.value = normalizarErro(erro).mensagem
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
