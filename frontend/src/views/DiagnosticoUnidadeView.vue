<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <!-- Cabeçalho -->
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            <i aria-hidden="true" class="bi bi-building text-primary me-2"/>
            {{ TEXTOS.diagnostico.TITULO_UNIDADE }}
          </h1>
          <div v-if="unidade" class="text-muted small">
            <strong>{{ unidade.sigla }}</strong> — {{ unidade.nome }}
            <BBadge :variant="varianteSituacao" class="ms-2">{{ unidade.situacao }}</BBadge>
          </div>
        </div>
        <BButton size="sm" variant="outline-secondary" @click="router.back()">
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

      <!-- Cards de métricas -->
      <BRow class="mb-4 g-3">
        <BCol md="4">
          <BCard class="text-center h-100">
            <div class="display-6 fw-bold text-primary">{{ servidores.length }}</div>
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
            <div class="text-muted small mt-1">Ocupações Críticas</div>
          </BCard>
        </BCol>
      </BRow>

      <!-- Tabela de servidores com detalhamento do consenso -->
      <BCard class="mb-4">
        <BCardHeader class="d-flex justify-content-between align-items-center">
          <strong>Servidores e Consenso</strong>
          <BBadge v-if="totalPendentes > 0" variant="warning">{{ totalPendentes }} pendente(s)</BBadge>
        </BCardHeader>

        <EmptyState
            v-if="servidores.length === 0"
            :description="TEXTOS.diagnostico.VAZIO_EQUIPE_TEXTO"
            :title="TEXTOS.diagnostico.VAZIO_EQUIPE_TITULO"
            icon="bi-people"
        />

        <template v-else>
          <BAccordion v-for="servidor in servidores" :key="servidor.titulo" class="mb-1">
            <BAccordionItem>
              <template #title>
                <div class="d-flex align-items-center gap-2 w-100 justify-content-between pe-3">
                  <span>
                    <strong>{{ servidor.nome }}</strong>
                    <small class="text-muted ms-2">{{ servidor.titulo }}</small>
                  </span>
                  <BBadge :variant="varianteSituacaoServidor(servidor.situacaoServidor)">
                    {{ formatarSituacaoServidor(servidor.situacaoServidor) }}
                  </BBadge>
                </div>
              </template>
              <BTable
                  :fields="colunasCompetencias"
                  :items="servidor.competencias"
                  bordered
                  responsive
                  small
              >
                <template #cell(importancia)="{ item }">
                  {{ item.importancia ?? TEXTOS.diagnostico.NOTA_NAO_INFORMADA }}
                </template>
                <template #cell(dominio)="{ item }">
                  {{ item.dominio ?? TEXTOS.diagnostico.NOTA_NAO_INFORMADA }}
                </template>
                <template #cell(gap)="{ item }">
                  <span :class="calcularGap(item) > 0 ? 'text-danger fw-bold' : 'text-success'">
                    {{ calcularGap(item) > 0 ? `+${calcularGap(item)}` : calcularGap(item) }}
                  </span>
                </template>
              </BTable>
            </BAccordionItem>
          </BAccordion>
        </template>
      </BCard>

      <!-- Ocupações críticas -->
      <BCard v-if="ocupacoesCriticas.length > 0" class="mb-4">
        <BCardHeader><strong>Ocupações Críticas</strong></BCardHeader>
        <BTable
            :fields="colunasOcupacoes"
            :items="ocupacoesComDescricao"
            bordered
            responsive
            small
        >
          <template #cell(situacaoCapacitacao)="{ item }">
            <BBadge :variant="varianteCapacitacao(item.situacaoCapacitacao)">
              {{ formatarCapacitacao(item.situacaoCapacitacao) }}
            </BBadge>
          </template>
        </BTable>
      </BCard>

      <!-- Ações do gestor/admin -->
      <div class="d-flex gap-2 flex-wrap mb-4">
        <BButton
            v-if="podeValidar"
            :disabled="validando"
            data-testid="btn-validar-diagnostico-unidade"
            variant="success"
            @click="abrirModalValidar"
        >
          <BSpinner v-if="validando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_VALIDAR }}
        </BButton>

        <BButton
            v-if="podeDevolver"
            :disabled="devolvendo"
            data-testid="btn-devolver-diagnostico-unidade"
            variant="warning"
            @click="abrirModalDevolver"
        >
          <BSpinner v-if="devolvendo" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_DEVOLVER }}
        </BButton>

        <BButton
            v-if="podeHomologar"
            :disabled="homologando"
            data-testid="btn-homologar-diagnostico-unidade"
            variant="primary"
            @click="abrirModalHomologar"
        >
          <BSpinner v-if="homologando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_HOMOLOGAR }}
        </BButton>
      </div>

      <!-- Histórico de movimentações -->
      <BCard v-if="movimentacoes.length > 0" class="mb-4">
        <BCardHeader><strong>Histórico de Movimentações</strong></BCardHeader>
        <BListGroup flush>
          <BListGroupItem
              v-for="(mov, idx) in movimentacoes"
              :key="idx"
              class="small"
          >
            <div class="d-flex justify-content-between">
              <span><strong>{{ mov.descricao }}</strong></span>
              <span class="text-muted">{{ mov.dataHora }}</span>
            </div>
            <div class="text-muted">{{ mov.unidadeOrigem }} → {{ mov.unidadeDestino }}</div>
          </BListGroupItem>
        </BListGroup>
      </BCard>
    </template>

    <!-- Modal: Validar -->
    <BModal v-model="modalValidarAberto" :title="TEXTOS.diagnostico.MODAL_VALIDAR_TITULO" centered>
      <BFormTextarea v-model="observacoesValidar" :placeholder="TEXTOS.diagnostico.LABEL_OBSERVACOES" rows="3"/>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalValidarAberto = false">Cancelar</BButton>
        <BButton :disabled="validando" data-testid="btn-confirmar-validar-unidade" variant="success" @click="confirmarValidar">
          <BSpinner v-if="validando" aria-hidden="true" class="me-1" small/>
          Validar
        </BButton>
      </template>
    </BModal>

    <!-- Modal: Devolver -->
    <BModal v-model="modalDevolverAberto" :title="TEXTOS.diagnostico.MODAL_DEVOLVER_TITULO" centered>
      <BFormTextarea v-model="justificativaDevolver" :placeholder="TEXTOS.diagnostico.MODAL_DEVOLVER_PLACEHOLDER" rows="3"/>
      <BFormText v-if="erroJustificativaDevolver" class="text-danger">{{ erroJustificativaDevolver }}</BFormText>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalDevolverAberto = false">Cancelar</BButton>
        <BButton :disabled="devolvendo" data-testid="btn-confirmar-devolver-unidade" variant="warning" @click="confirmarDevolver">
          <BSpinner v-if="devolvendo" aria-hidden="true" class="me-1" small/>
          Devolver
        </BButton>
      </template>
    </BModal>

    <!-- Modal: Homologar -->
    <BModal v-model="modalHomologarAberto" :title="TEXTOS.diagnostico.MODAL_HOMOLOGAR_TITULO" centered>
      <BFormTextarea v-model="observacoesHomologar" :placeholder="TEXTOS.diagnostico.LABEL_OBSERVACOES" rows="3"/>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalHomologarAberto = false">Cancelar</BButton>
        <BButton :disabled="homologando" data-testid="btn-confirmar-homologar-unidade" variant="primary" @click="confirmarHomologar">
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
import {
  BAccordion,
  BAccordionItem,
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
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {usePerfilStore} from '@/stores/perfil';
import {Perfil} from '@/types/tipos';
import {TEXTOS} from '@/constants/textos';
import type {AvaliacaoCompetencia, SituacaoAvaliacaoServidor, SituacaoCapacitacao} from '@/types/diagnostico-competencias';
import type {ColorVariant} from 'bootstrap-vue-next';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();

const router = useRouter();
const perfilStore = usePerfilStore();
const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);

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

// ── Estado local ──────────────────────────────────────────────────────────────
const erroMensagem = ref('');
const alertaSucesso = ref('');
const modalValidarAberto = ref(false);
const modalDevolverAberto = ref(false);
const modalHomologarAberto = ref(false);
const observacoesValidar = ref('');
const justificativaDevolver = ref('');
const erroJustificativaDevolver = ref('');
const observacoesHomologar = ref('');

// ── Permissões ────────────────────────────────────────────────────────────────
const ehGestor = computed(
  () =>
    perfilStore.perfilSelecionado === Perfil.GESTOR ||
    perfilStore.perfilSelecionado === Perfil.ADMIN,
);
const podeValidar = computed(() => ehGestor.value && situacao.value === 'DIAGNOSTICO_CONCLUIDO');
const podeDevolver = computed(() => ehGestor.value && situacao.value === 'DIAGNOSTICO_CONCLUIDO');
const podeHomologar = computed(
  () => perfilStore.perfilSelecionado === Perfil.ADMIN && situacao.value === 'DIAGNOSTICO_VALIDADO',
);

// ── Modais ────────────────────────────────────────────────────────────────────
function abrirModalValidar() { observacoesValidar.value = ''; modalValidarAberto.value = true; }
function abrirModalDevolver() {
  justificativaDevolver.value = '';
  erroJustificativaDevolver.value = '';
  modalDevolverAberto.value = true;
}
function abrirModalHomologar() { observacoesHomologar.value = ''; modalHomologarAberto.value = true; }

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

// ── Formatação ────────────────────────────────────────────────────────────────
const varianteSituacao = computed(() => {
  switch (situacao.value) {
    case 'DIAGNOSTICO_CONCLUIDO': return 'success';
    case 'DIAGNOSTICO_VALIDADO': return 'info';
    case 'DIAGNOSTICO_HOMOLOGADO': return 'primary';
    default: return 'warning';
  }
});

function varianteSituacaoServidor(s: SituacaoAvaliacaoServidor): ColorVariant {
  const mapa: Record<SituacaoAvaliacaoServidor, ColorVariant> = {
    CONSENSO_APROVADO: 'success',
    AVALIACAO_IMPOSSIBILITADA: 'secondary',
    CONSENSO_CRIADO: 'warning',
    AUTOAVALIACAO_CONCLUIDA: 'info',
    AUTOAVALIACAO_NAO_REALIZADA: 'light',
  };
  return mapa[s] ?? 'light';
}

function formatarSituacaoServidor(s: SituacaoAvaliacaoServidor): string {
  return {
    AUTOAVALIACAO_NAO_REALIZADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
    AUTOAVALIACAO_CONCLUIDA: TEXTOS.diagnostico.SITUACAO_AUTOAVALIACAO_CONCLUIDA,
    CONSENSO_CRIADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_CRIADO,
    CONSENSO_APROVADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_APROVADO,
    AVALIACAO_IMPOSSIBILITADA: TEXTOS.diagnostico.SITUACAO_IMPOSSIBILITADA,
  }[s] ?? s;
}

function varianteCapacitacao(s: SituacaoCapacitacao): ColorVariant {
  const mapa: Record<SituacaoCapacitacao, ColorVariant> = {NA: 'secondary', AC: 'danger', EC: 'warning', C: 'success', I: 'primary'};
  return mapa[s] ?? 'light';
}

function formatarCapacitacao(s: SituacaoCapacitacao): string {
  return {
    NA: TEXTOS.diagnostico.CAPACITACAO_NA,
    AC: TEXTOS.diagnostico.CAPACITACAO_AC,
    EC: TEXTOS.diagnostico.CAPACITACAO_EC,
    C: TEXTOS.diagnostico.CAPACITACAO_C,
    I: TEXTOS.diagnostico.CAPACITACAO_I,
  }[s] ?? s;
}

function calcularGap(item: AvaliacaoCompetencia): number {
  if (item.importancia === null || item.dominio === null) return 0;
  return item.importancia - item.dominio;
}

const mapaDescricaoCompetencia = computed(() =>
  Object.fromEntries((contexto.value?.competencias ?? []).map((c) => [c.codigo, c.descricao])),
);

const ocupacoesComDescricao = computed(() =>
  ocupacoesCriticas.value.map((o) => ({
    ...o,
    nomeCompetencia: mapaDescricaoCompetencia.value[o.competenciaCodigo] ?? `Competência ${o.competenciaCodigo}`,
  })),
);

const colunasCompetencias = [
  {key: 'competenciaCodigo', label: 'Código'},
  {key: 'importancia', label: TEXTOS.diagnostico.COLUNA_IMPORTANCIA},
  {key: 'dominio', label: TEXTOS.diagnostico.COLUNA_DOMINIO},
  {key: 'gap', label: TEXTOS.diagnostico.COLUNA_GAP},
];

const colunasOcupacoes = [
  {key: 'servidorTitulo', label: TEXTOS.diagnostico.COLUNA_SERVIDOR},
  {key: 'nomeCompetencia', label: TEXTOS.diagnostico.COLUNA_COMPETENCIA},
  {key: 'situacaoCapacitacao', label: TEXTOS.diagnostico.COLUNA_CAPACITACAO},
];
</script>
