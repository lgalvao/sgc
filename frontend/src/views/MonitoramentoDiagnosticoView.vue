<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <!-- Cabeçalho -->
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            <i aria-hidden="true" class="bi bi-activity text-primary me-2"/>
            {{ TEXTOS.diagnostico.TITULO_MONITORAMENTO }}
          </h1>
          <div v-if="unidade" class="text-muted small">
            <strong>{{ unidade.unidadeSigla }}</strong> - {{ unidade.unidadeNome }}
            <BBadge :variant="varianteSituacao" class="ms-2">{{ situacao }}</BBadge>
          </div>
        </div>
        <BButton size="sm" variant="outline-secondary" @click="void router.back()">
          <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
          {{ TEXTOS.diagnostico.BTN_VOLTAR }}
        </BButton>
      </div>

      <!-- Alertas -->
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

      <!-- Cards de resumo -->
      <BRow class="mb-4 g-3">
        <BCol md="4">
          <BCard class="text-center h-100">
            <div class="display-6 fw-bold text-primary">{{ totalServidores }}</div>
            <div class="text-muted small mt-1">Servidores</div>
          </BCard>
        </BCol>
        <BCol md="4">
          <BCard class="text-center h-100">
            <div class="display-6 fw-bold" :class="totalPendentes > 0 ? 'text-warning' : 'text-success'">
              {{ totalPendentes }}
            </div>
            <div class="text-muted small mt-1">Pendentes</div>
          </BCard>
        </BCol>
        <BCol md="4">
          <BCard class="text-center h-100">
            <div class="display-6 fw-bold text-info">{{ ocupacoesCriticas.length }}</div>
            <div class="text-muted small mt-1">Situação de Capacitação</div>
          </BCard>
        </BCol>
      </BRow>

      <!-- Tabela de servidores e situações -->
      <BCard class="mb-4">
        <BCardHeader>
          <strong>Equipe e Situações</strong>
        </BCardHeader>
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
            striped
        >
          <template #cell(situacaoServidor)="{ item }">
            <BBadge :variant="varianteSituacaoServidor(item.situacaoServidor)">
              {{ formatarSituacaoServidor(item.situacaoServidor) }}
            </BBadge>
          </template>
          <template #cell(acoes)="{ item }">
            <div class="d-flex gap-1 flex-nowrap">
              <!-- Manter avaliação de consenso — sempre habilitado (CDU-43) -->
              <BButton
                  v-if="ehChefe"
                  :data-testid="`btn-manter-consenso-${item.servidorTitulo}`"
                  size="sm"
                  variant="outline-primary"
                  @click="navegarParaConsenso(item.servidorTitulo)"
              >
                <i aria-hidden="true" class="bi bi-pencil-square me-1"/>
                {{ TEXTOS.diagnostico.BTN_MANTER_CONSENSO }}
              </BButton>
              <!-- Indicar impossibilidade — desabilitado se já impossibilitado (CDU-46) -->
              <BButton
                  v-if="ehChefe"
                  :data-testid="`btn-impossibilitar-${item.servidorTitulo}`"
                  :disabled="item.situacaoServidor === 'AVALIACAO_IMPOSSIBILITADA'"
                  size="sm"
                  variant="outline-danger"
                  @click="abrirModalImpossibilitar(item)"
              >
                <i aria-hidden="true" class="bi bi-x-circle me-1"/>
                {{ TEXTOS.diagnostico.BTN_IMPOSSIBILITAR }}
              </BButton>
            </div>
          </template>
        </BTable>
      </BCard>

      <!-- Movimentações -->
      <BCard v-if="movimentacoes.length > 0" class="mb-4">
        <BCardHeader>
          <strong>Movimentações</strong>
        </BCardHeader>
        <BListGroup flush>
          <BListGroupItem
              v-for="(mov, indice) in movimentacoes"
              :key="`${mov.dataHora}-${mov.descricao}-${indice}`"
              class="py-2"
          >
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <div class="fw-semibold small">{{ mov.descricao }}</div>
                <div class="text-muted" style="font-size: 0.78rem">
                  {{ mov.unidadeOrigem }} → {{ mov.unidadeDestino }}
                </div>
              </div>
              <span class="text-muted" style="font-size: 0.78rem; white-space: nowrap">{{ mov.dataHora }}</span>
            </div>
          </BListGroupItem>
        </BListGroup>
      </BCard>

      <!-- Ações de fluxo do gestor/admin -->
      <div class="d-flex gap-2 flex-wrap mb-4">
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
    </template>

    <!-- Modal: Indicar impossibilidade (CDU-46) -->
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

    <!-- Modal: Validar -->
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

    <!-- Modal: Devolver -->
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

    <!-- Modal: Homologar -->
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
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {CHAVE_DIAGNOSTICO} from '@/composables/useDiagnosticoContexto';
import {useQueryCache} from '@pinia/colada';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {impossibilitarAvaliacao} from '@/services/diagnosticoService';
import {
  BBadge,
  BButton,
  BCard,
  BCardHeader,
  BCol,
  BFormText,
  BFormTextarea,
  BListGroup,
  BListGroupItem,
  BModal,
  BRow,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {useMonitoramentoDiagnostico} from '@/composables/useMonitoramentoDiagnostico';
import {useFluxoDiagnostico} from '@/composables/useFluxoDiagnostico';
import {TEXTOS} from '@/constants/textos';
import type {ServidorDiagnostico, SituacaoAvaliacaoServidor} from '@/types/diagnostico-competencias';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();

const router = useRouter();
const cache = useQueryCache();
const {
  podeCriarConsenso,
  habilitarValidarDiagnostico,
  habilitarDevolverDiagnostico,
  habilitarHomologarDiagnostico,
} = useDiagnosticoPermissoes(props.codSubprocesso);

const {
  unidade,
  servidores,
  ocupacoesCriticas,
  movimentacoes,
  carregando,
  situacao,
  totalPendentes,
} = useMonitoramentoDiagnostico(props.codSubprocesso);

const {
  validando,
  devolvendo,
  homologando,
  erroValidar,
  erroDevolver,
  erroHomologar,
  validarDiagnostico,
  devolverDiagnostico,
  homologarDiagnostico,
} = useFluxoDiagnostico(props.codSubprocesso);

// « Estado local »
const erroMensagem = ref('');
const alertaSucesso = ref('');
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

// « Perfil e permissões »
const ehChefe = computed(() => podeCriarConsenso.value);
const podeValidar = computed(() => habilitarValidarDiagnostico.value);
const podeDevolver = computed(() => habilitarDevolverDiagnostico.value);
const podeHomologar = computed(() => habilitarHomologarDiagnostico.value);

const totalServidores = computed(() => servidores.value.length);

// « Navegação »
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

// « Modal impossibilidade »
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
    void cache.invalidateQueries({key: [CHAVE_DIAGNOSTICO, 'unidade', props.codSubprocesso]});
  } catch {
    erroMensagem.value = TEXTOS.diagnostico.ERRO_SALVAR;
  } finally {
    impossibilitando.value = false;
  }
}

// « Modais de fluxo »
function abrirModalValidar() {
  observacoesValidar.value = '';
  modalValidarAberto.value = true;
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

// « Formatação »
const varianteSituacao = computed(() => {
  switch (situacao.value) {
    case 'CONCLUIDO':
      return 'success';
    case 'VALIDADO':
      return 'info';
    case 'HOMOLOGADO':
      return 'primary';
    case 'EM_ANDAMENTO':
      return 'warning';
    default:
      return 'info';
  }
});

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
    AUTOAVALIACAO_NAO_REALIZADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
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
