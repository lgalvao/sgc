<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <!-- Cabeçalho -->
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            <i aria-hidden="true" class="bi bi-people text-primary me-2"/>
            {{ TEXTOS.diagnostico.TITULO_OCUPACOES_CRITICAS }}
          </h1>
          <div v-if="unidade" class="text-muted small">
            <strong>{{ unidade.unidadeSigla }}</strong> — {{ unidade.unidadeNome }}
            <BBadge :variant="varianteSituacao" class="ms-2">{{ unidade.situacaoSubprocesso }}</BBadge>
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

      <!-- Badge de autosave -->
      <div class="mb-3 text-muted small d-flex align-items-center gap-2">
        <template v-if="salvandoAutomaticamente">
          <BSpinner small variant="secondary"/>
          {{ TEXTOS.diagnostico.LABEL_SALVANDO }}
        </template>
        <template v-else-if="autoguardado">
          <i aria-hidden="true" class="bi bi-check-circle text-success"/>
          {{ TEXTOS.diagnostico.LABEL_AUTOGUARDADO }}
        </template>
      </div>

      <!-- Aviso de pendências -->
      <BAlert
          v-if="pendentes > 0"
          variant="warning"
          :model-value="true"
          class="mb-3"
      >
        <i aria-hidden="true" class="bi bi-exclamation-triangle me-2"/>
        Existem <strong>{{ pendentes }}</strong> ocupações críticas sem situação definida.
      </BAlert>

      <!-- Tabela de ocupações críticas -->
      <BCard class="mb-4">
        <BCardHeader>
          <strong>{{ TEXTOS.diagnostico.TITULO_OCUPACOES_CRITICAS }}</strong>
        </BCardHeader>

        <EmptyState
            v-if="ocupacoesLocais.length === 0"
            :description="TEXTOS.diagnostico.VAZIO_OCUPACOES_TEXTO"
            :title="TEXTOS.diagnostico.VAZIO_OCUPACOES_TITULO"
            icon="bi-people"
        />

        <BTable
            v-else
            :fields="colunas"
            :items="ocupacoesComDescricao"
            hover
            responsive
            small
            striped
        >
          <template #cell(situacaoCapacitacao)="{ item }">
            <BFormSelect
                :data-testid="`ocupacao-${item.servidorTitulo}-${item.competenciaCodigo}`"
                :model-value="item.situacaoCapacitacao"
                :options="opcoesCapacitacao"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarCapacitacao(item.servidorTitulo, item.competenciaCodigo, v as SituacaoCapacitacao)"
            />
          </template>
        </BTable>
      </BCard>

      <!-- Ações de fluxo -->
      <div class="d-flex gap-2 flex-wrap">
        <BButton
            :disabled="concluindo || pendentes > 0"
            data-testid="btn-concluir-diagnostico"
            variant="primary"
            @click="abrirModalConcluir"
        >
          <BSpinner v-if="concluindo" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_CONCLUIR_DIAGNOSTICO }}
        </BButton>
      </div>
    </template>

    <!-- Modal: Concluir diagnóstico -->
    <ModalConfirmacao
        v-model="modalConcluirAberto"
        :loading="concluindo"
        :mensagem="TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_MENSAGEM"
        :titulo="TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_TITULO"
        ok-title="Concluir"
        test-id-confirmar="btn-confirmar-concluir-diag"
        @confirmar="confirmarConcluir"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {
  BAlert,
  BBadge,
  BButton,
  BCard,
  BCardHeader,
  BFormSelect,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {useOcupacoesCriticasDiagnostico} from '@/composables/useOcupacoesCriticasDiagnostico';
import {useFluxoDiagnostico} from '@/composables/useFluxoDiagnostico';
import {TEXTOS} from '@/constants/textos';
import type {SituacaoCapacitacao} from '@/types/diagnostico-competencias';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();

const router = useRouter();
const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);

const {
  ocupacoesLocais,
  unidade,
  carregando,
  salvandoAutomaticamente,
  autoguardado,
  pendentes,
  atualizarCapacitacao,
} = useOcupacoesCriticasDiagnostico(props.codSubprocesso);

const {concluindo, erroConcluir, concluirDiagnostico} = useFluxoDiagnostico(props.codSubprocesso);

// ── Alertas ──────────────────────────────────────────────────────────────────
const erroMensagem = ref('');
const alertaSucesso = ref('');

// ── Modal ────────────────────────────────────────────────────────────────────
const modalConcluirAberto = ref(false);

function abrirModalConcluir() {
  modalConcluirAberto.value = true;
}

async function confirmarConcluir() {
  try {
    await concluirDiagnostico();
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_CONCLUIDO;
    void router.back();
  } catch {
    erroMensagem.value = erroConcluir.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

// ── Formatação ────────────────────────────────────────────────────────────────
const varianteSituacao = computed(() => {
  switch (unidade.value?.situacaoSubprocesso) {
    case 'DIAGNOSTICO_CONCLUIDO':
      return 'success';
    case 'DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO':
      return 'warning';
    default:
      return 'secondary';
  }
});

// Adiciona descrição das competências e nomes dos servidores
const ocupacoesComDescricao = computed(() => {
  const mapaCompetencia = Object.fromEntries(
    (contexto.value?.competencias ?? []).map((c) => [c.competenciaCodigo, c.descricao]),
  );
  return ocupacoesLocais.value.map((o) => ({
    ...o,
    nomeCompetencia: mapaCompetencia[o.competenciaCodigo] ?? `Competência ${o.competenciaCodigo}`,
  }));
});

// ── Opções de capacitação ─────────────────────────────────────────────────────
const opcoesCapacitacao = [
  {value: null, text: '—'},
  {value: 'NA', text: TEXTOS.diagnostico.CAPACITACAO_NA},
  {value: 'AC', text: TEXTOS.diagnostico.CAPACITACAO_AC},
  {value: 'EC', text: TEXTOS.diagnostico.CAPACITACAO_EC},
  {value: 'C', text: TEXTOS.diagnostico.CAPACITACAO_C},
  {value: 'I', text: TEXTOS.diagnostico.CAPACITACAO_I},
];

const colunas = [
  {key: 'servidorTitulo', label: TEXTOS.diagnostico.COLUNA_SERVIDOR},
  {key: 'nomeCompetencia', label: TEXTOS.diagnostico.COLUNA_COMPETENCIA},
  {key: 'situacaoCapacitacao', label: TEXTOS.diagnostico.COLUNA_CAPACITACAO},
];
</script>
