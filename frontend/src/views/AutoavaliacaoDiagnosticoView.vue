<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <!-- Cabeçalho com dados da unidade -->
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            <i aria-hidden="true" class="bi bi-clipboard-check text-primary me-2"/>
            {{ TEXTOS.diagnostico.TITULO_AUTOAVALIACAO }}
          </h1>
          <div v-if="contexto" class="text-muted small">
            <strong>{{ contexto.unidadeSigla }}</strong> — {{ contexto.unidadeNome }}
            <BBadge :variant="varianteSituacao" class="ms-2">
              {{ contexto.situacaoDiagnostico }}
            </BBadge>
          </div>
        </div>
        <BButton variant="outline-secondary" size="sm" @click="voltar">
          <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
          {{ TEXTOS.diagnostico.BTN_VOLTAR }}
        </BButton>
      </div>

      <!-- Alerta de erro -->
      <AppAlert
          v-if="erroMensagem"
          :mensagem="erroMensagem"
          variante="danger"
          @dismissed="erroMensagem = ''"
      />

      <!-- Alerta de sucesso -->
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

      <!-- Indicador de situação do servidor -->
      <BAlert
          v-if="ehAutoavaliacaoConcluida && !ehChefe"
          variant="info"
          :model-value="true"
          class="mb-4"
      >
        <i aria-hidden="true" class="bi bi-info-circle me-2"/>
        Sua autoavaliação foi concluída. Aguarde o consenso da chefia.
      </BAlert>

      <BAlert
          v-if="ehConsensoCriado && !ehChefe"
          variant="warning"
          :model-value="true"
          class="mb-4"
      >
        <i aria-hidden="true" class="bi bi-exclamation-triangle me-2"/>
        A chefia registrou um consenso. Consulte e aprove ou discorde.
      </BAlert>

      <BAlert
          v-if="ehConsensoAprovado"
          variant="success"
          :model-value="true"
          class="mb-4"
      >
        <i aria-hidden="true" class="bi bi-check-circle me-2"/>
        Consenso aprovado. Avaliação finalizada.
      </BAlert>

      <!-- Tabela de competências -->
      <BCard class="mb-4">
        <BCardHeader>
          <strong>{{ TEXTOS.diagnostico.TITULO_AUTOAVALIACAO }}</strong>
          <span class="text-muted small ms-2">{{ TEXTOS.diagnostico.ESCALA_HINT }}</span>
        </BCardHeader>
        <BTable
            :fields="colunas"
            :items="competenciasComDescricao"
            responsive
            small
            striped
            hover
        >
          <template #cell(importancia)="{ item }">
            <BFormSelect
                v-if="podeEditar"
                :data-testid="`autoavaliacao-importancia-${item.competenciaCodigo}`"
                :disabled="!podeEditar"
                :model-value="item.importancia"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNota(item.competenciaCodigo, 'importancia', v as number | null)"
            />
            <span v-else>{{ item.importancia ?? TEXTOS.diagnostico.NOTA_NAO_INFORMADA }}</span>
          </template>

          <template #cell(dominio)="{ item }">
            <BFormSelect
                v-if="podeEditar"
                :data-testid="`autoavaliacao-dominio-${item.competenciaCodigo}`"
                :disabled="!podeEditar"
                :model-value="item.dominio"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNota(item.competenciaCodigo, 'dominio', v as number | null)"
            />
            <span v-else>{{ item.dominio ?? TEXTOS.diagnostico.NOTA_NAO_INFORMADA }}</span>
          </template>
        </BTable>
      </BCard>

      <!-- Ações do servidor -->
      <div v-if="!ehChefe && podeEditar" class="d-flex gap-2 mb-4">
        <BButton
            :disabled="concluindo"
            data-testid="btn-concluir-autoavaliacao"
            variant="primary"
            @click="abrirModalConcluir"
        >
          <BSpinner v-if="concluindo" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_CONCLUIR_AUTOAVALIACAO }}
        </BButton>
      </div>

      <!-- Ações do servidor para aprovar consenso -->
      <div v-if="ehConsensoCriado && !ehChefe" class="d-flex gap-2 mb-4">
        <BButton
            :disabled="aprovando"
            data-testid="btn-aprovar-consenso"
            variant="success"
            @click="abrirModalAprovar"
        >
          <BSpinner v-if="aprovando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_APROVAR_CONSENSO }}
        </BButton>
      </div>

      <!-- Acompanhamento da equipe (para chefia) -->
      <BCard v-if="ehChefe" class="mb-4">
        <BCardHeader>
          <strong>Equipe</strong>
          <BBadge v-if="pendentes > 0" variant="warning" class="ms-2">
            {{ pendentes }} pendente(s)
          </BBadge>
        </BCardHeader>
        <BListGroup flush>
          <BListGroupItem
              v-for="membro in itensEquipe"
              :key="membro.servidorTitulo"
              class="d-flex align-items-center justify-content-between"
          >
            <div>
              <strong>{{ membro.servidorNome }}</strong>
              <small class="text-muted ms-2">{{ membro.servidorTitulo }}</small>
            </div>
            <div class="d-flex align-items-center gap-2">
              <BBadge :variant="varianteSituacaoServidor(membro.situacaoServidor)">
                {{ formatarSituacaoServidor(membro.situacaoServidor) }}
              </BBadge>
              <BButton
                  v-if="membro.situacaoServidor === 'AUTOAVALIACAO_CONCLUIDA'"
                  :data-testid="`btn-consenso-${membro.servidorTitulo}`"
                  size="sm"
                  variant="outline-primary"
                  @click="navegarParaConsenso(membro.servidorTitulo)"
              >
                Registrar consenso
              </BButton>
              <BButton
                  v-if="podeImpossibilitar(membro.situacaoServidor)"
                  :data-testid="`btn-impossibilitar-${membro.servidorTitulo}`"
                  size="sm"
                  variant="outline-danger"
                  @click="abrirModalImpossibilitar(membro.servidorTitulo)"
              >
                Impossibilitar
              </BButton>
            </div>
          </BListGroupItem>
        </BListGroup>
      </BCard>
    </template>

    <!-- Modal: Concluir autoavaliação -->
    <ModalConfirmacao
        v-model="modalConcluirAberto"
        :loading="concluindo"
        :mensagem="TEXTOS.diagnostico.MODAL_CONCLUIR_MENSAGEM"
        :titulo="TEXTOS.diagnostico.MODAL_CONCLUIR_TITULO"
        ok-title="Concluir"
        test-id-confirmar="btn-confirmar-concluir"
        @confirmar="confirmarConcluir"
    />

    <!-- Modal: Aprovar consenso -->
    <ModalConfirmacao
        v-model="modalAprovarAberto"
        :loading="aprovando"
        :mensagem="TEXTOS.diagnostico.MODAL_APROVAR_MENSAGEM"
        :titulo="TEXTOS.diagnostico.MODAL_APROVAR_TITULO"
        ok-title="Aprovar"
        ok-variant="success"
        test-id-confirmar="btn-confirmar-aprovar"
        @confirmar="confirmarAprovar"
    />

    <!-- Modal: Impossibilitar avaliação -->
    <BModal
        v-model="modalImpossibilitarAberto"
        :title="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_TITULO"
        centered
        @hide="fecharModalImpossibilitar"
    >
      <p>{{ TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_MENSAGEM }}</p>
      <BFormTextarea
          v-model="justificativaImpossibilidade"
          :placeholder="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_PLACEHOLDER"
          data-testid="textarea-justificativa-impossibilidade"
          rows="3"
      />
      <BFormText v-if="erroJustificativa" class="text-danger">
        {{ erroJustificativa }}
      </BFormText>
      <template #footer>
        <BButton variant="link" class="text-secondary" @click="fecharModalImpossibilitar">Cancelar</BButton>
        <BButton
            :disabled="impossibilitando"
            data-testid="btn-confirmar-impossibilitar"
            variant="danger"
            @click="confirmarImpossibilitar"
        >
          <BSpinner v-if="impossibilitando" aria-hidden="true" class="me-1" small/>
          Impossibilitar
        </BButton>
      </template>
    </BModal>
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
  BFormText,
  BFormTextarea,
  BListGroup,
  BListGroupItem,
  BModal,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {useAutoavaliacaoDiagnostico} from '@/composables/useAutoavaliacaoDiagnostico';
import {useConsensoDiagnostico} from '@/composables/useConsensoDiagnostico';
import {useEquipeDiagnostico} from '@/composables/useEquipeDiagnostico';
import {usePerfilStore} from '@/stores/perfil';
import {Perfil} from '@/types/tipos';
import {TEXTOS} from '@/constants/textos';
import type {SituacaoAvaliacaoServidor} from '@/types/diagnostico-competencias';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();

const router = useRouter();
const perfilStore = usePerfilStore();

// ── Composables ──────────────────────────────────────────────────────────────
const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);

const {
  competenciasLocais,
  situacaoServidor,
  carregando,
  salvandoAutomaticamente,
  autoguardado,
  concluindo,
  erroConcluir,
  atualizarNota,
  concluirAutoavaliacao,
} = useAutoavaliacaoDiagnostico(props.codSubprocesso);

const {
  aprovando,
  erroAprovar,
  erroImpossibilitar,
  impossibilitando,
  aprovarConsenso,
  impossibilitarAvaliacao,
} = useConsensoDiagnostico(props.codSubprocesso);

const {itens: itensEquipe, pendentes} = useEquipeDiagnostico(props.codSubprocesso);

// ── Perfil ───────────────────────────────────────────────────────────────────
const ehChefe = computed(() =>
  perfilStore.perfilSelecionado === Perfil.CHEFE ||
  perfilStore.perfilSelecionado === Perfil.GESTOR ||
  perfilStore.perfilSelecionado === Perfil.ADMIN,
);

// ── Estado das situações ─────────────────────────────────────────────────────
const ehAutoavaliacaoConcluida = computed(() => situacaoServidor.value === 'AUTOAVALIACAO_CONCLUIDA');
const ehConsensoCriado = computed(() => situacaoServidor.value === 'CONSENSO_CRIADO');
const ehConsensoAprovado = computed(() => situacaoServidor.value === 'CONSENSO_APROVADO');
const podeEditar = computed(
  () =>
    situacaoServidor.value === 'AUTOAVALIACAO_NAO_REALIZADA' ||
    situacaoServidor.value === 'AUTOAVALIACAO_CONCLUIDA',
);

// ── Alertas ──────────────────────────────────────────────────────────────────
const erroMensagem = ref('');
const alertaSucesso = ref('');

// ── Modais ───────────────────────────────────────────────────────────────────
const modalConcluirAberto = ref(false);
const modalAprovarAberto = ref(false);
const modalImpossibilitarAberto = ref(false);
const servidorParaImpossibilitar = ref('');
const justificativaImpossibilidade = ref('');
const erroJustificativa = ref('');

function abrirModalConcluir() {
  modalConcluirAberto.value = true;
}

function abrirModalAprovar() {
  modalAprovarAberto.value = true;
}

function abrirModalImpossibilitar(titulo: string) {
  servidorParaImpossibilitar.value = titulo;
  justificativaImpossibilidade.value = '';
  erroJustificativa.value = '';
  modalImpossibilitarAberto.value = true;
}

function fecharModalImpossibilitar() {
  modalImpossibilitarAberto.value = false;
}

async function confirmarConcluir() {
  try {
    await concluirAutoavaliacao();
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_AUTOAVALIACAO_CONCLUIDA;
  } catch {
    erroMensagem.value = erroConcluir.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

async function confirmarAprovar() {
  try {
    await aprovarConsenso();
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_CONSENSO_APROVADO;
  } catch {
    erroMensagem.value = erroAprovar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

async function confirmarImpossibilitar() {
  if (!justificativaImpossibilidade.value.trim()) {
    erroJustificativa.value = TEXTOS.diagnostico.ERRO_JUSTIFICATIVA_OBRIGATORIA;
    return;
  }
  try {
    await impossibilitarAvaliacao(servidorParaImpossibilitar.value, justificativaImpossibilidade.value);
    fecharModalImpossibilitar();
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_IMPOSSIBILITADO;
  } catch {
    erroMensagem.value = erroImpossibilitar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

// ── Navegação ────────────────────────────────────────────────────────────────
function navegarParaConsenso(servidorTitulo: string) {
  router.push({
    name: 'ConsensoDiagnostico',
    params: {
      codSubprocesso: props.codSubprocesso,
      siglaUnidade: props.siglaUnidade,
      servidorTitulo,
    },
  });
}

function voltar() {
  router.back();
}

function podeImpossibilitar(situacao: SituacaoAvaliacaoServidor) {
  return (
    situacao === 'AUTOAVALIACAO_NAO_REALIZADA' ||
    situacao === 'AUTOAVALIACAO_CONCLUIDA' ||
    situacao === 'CONSENSO_CRIADO'
  );
}

// ── Formatação ───────────────────────────────────────────────────────────────
const varianteSituacao = computed(() => {
  switch (contexto.value?.situacaoSubprocesso) {
    case 'DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO':
      return 'warning';
    case 'DIAGNOSTICO_CONCLUIDO':
      return 'success';
    default:
      return 'secondary';
  }
});

function varianteSituacaoServidor(situacao: SituacaoAvaliacaoServidor) {
  switch (situacao) {
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

function formatarSituacaoServidor(situacao: SituacaoAvaliacaoServidor): string {
  const mapa: Record<SituacaoAvaliacaoServidor, string> = {
    AUTOAVALIACAO_NAO_REALIZADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
    AUTOAVALIACAO_CONCLUIDA: TEXTOS.diagnostico.SITUACAO_AUTOAVALIACAO_CONCLUIDA,
    CONSENSO_CRIADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_CRIADO,
    CONSENSO_APROVADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_APROVADO,
    AVALIACAO_IMPOSSIBILITADA: TEXTOS.diagnostico.SITUACAO_IMPOSSIBILITADA,
  };
  return mapa[situacao] ?? situacao;
}

// ── Colunas da tabela ────────────────────────────────────────────────────────
const colunas = [
  {key: 'competenciaCodigo', label: 'Código'},
  {key: 'descricao', label: TEXTOS.diagnostico.COLUNA_COMPETENCIA},
  {key: 'importancia', label: TEXTOS.diagnostico.COLUNA_IMPORTANCIA},
  {key: 'dominio', label: TEXTOS.diagnostico.COLUNA_DOMINIO},
];

// Adiciona descricao a partir do contexto
const competenciasComDescricao = computed(() => {
  const mapa = Object.fromEntries(
    (contexto.value?.competencias ?? []).map((c) => [c.competenciaCodigo, c.descricao]),
  );
  return competenciasLocais.value.map((c) => ({
    ...c,
    descricao: mapa[c.competenciaCodigo] ?? `Competência ${c.competenciaCodigo}`,
  }));
});

// Opções de nota para os selects (1-5 + opção vazia)
const opcoesNota = [
  {value: null, text: '—'},
  {value: 1, text: '1'},
  {value: 2, text: '2'},
  {value: 3, text: '3'},
  {value: 4, text: '4'},
  {value: 5, text: '5'},
];
</script>
