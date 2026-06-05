<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            <i aria-hidden="true" class="bi bi-clipboard-check text-primary me-2"/>
            {{ TEXTOS.diagnostico.TITULO_AUTOAVALIACAO }}
          </h1>
          <div v-if="contexto" class="text-muted small">
            <strong>{{ contexto.unidadeSigla }}</strong> - {{ contexto.unidadeNome }}
            <BBadge :variant="varianteSituacao" class="ms-2">
              {{ contexto.situacaoDiagnostico }}
            </BBadge>
          </div>
        </div>
        <BButton size="sm" variant="outline-secondary" @click="voltar">
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

      <BAlert
          v-if="ehAutoavaliacaoConcluida && !ehChefe"
          :model-value="true"
          class="mb-4"
          variant="info"
      >
        <i aria-hidden="true" class="bi bi-info-circle me-2"/>
        Sua autoavaliação foi concluída. Aguarde a avaliação de consenso da chefia.
      </BAlert>

      <BAlert
          v-if="ehConsensoCriado && !ehChefe"
          :model-value="true"
          class="mb-4"
          variant="warning"
      >
        <i aria-hidden="true" class="bi bi-exclamation-triangle me-2"/>
        A chefia registrou a avaliação de consenso. Revise e aprove para finalizar.
      </BAlert>

      <BAlert
          v-if="ehConsensoAprovado"
          :model-value="true"
          class="mb-4"
          variant="success"
      >
        <i aria-hidden="true" class="bi bi-check-circle me-2"/>
        Avaliação de consenso aprovada. Fluxo finalizado.
      </BAlert>

      <BCard class="mb-4">
        <BCardHeader>
          <strong>{{ TEXTOS.diagnostico.TITULO_AUTOAVALIACAO }}</strong>
          <span class="text-muted small ms-2">{{ TEXTOS.diagnostico.ESCALA_HINT }}</span>
        </BCardHeader>
        <BTable
            :fields="colunas"
            :items="competenciasComDescricao"
            hover
            responsive
            small
            striped
        >
          <template #cell(descricao)="{ item }">
            <div class="d-flex flex-column gap-2">
              <span>{{ item.descricao }}</span>
              <div v-if="item.atividades.length > 0" class="small">
                <BButton
                    :data-testid="`toggle-atividades-${item.competenciaCodigo}`"
                    size="sm"
                    variant="link"
                    class="p-0 text-decoration-none"
                    @click="alternarDetalhesCompetencia(item.competenciaCodigo)"
                >
                  {{ detalhesCompetenciaAbertos[item.competenciaCodigo] ? 'Ocultar' : 'Atividade e conhecimentos' }}
                </BButton>
                <div v-if="detalhesCompetenciaAbertos[item.competenciaCodigo]" class="mt-2">
                  <ul class="mb-0 ps-3">
                    <li v-for="atividade in item.atividades" :key="atividade.codigo" class="mb-1">
                      <strong>{{ atividade.descricao }}</strong>
                      <div class="text-muted">
                        {{ formatarConhecimentos(atividade.conhecimentos) }}
                      </div>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </template>
          <template #cell(importancia)="{ item }">
            <BFormSelect
                v-if="podeEditar"
                :data-testid="`autoavaliacao-importancia-${item.competenciaCodigo}`"
                :disabled="!podeEditar"
                :model-value="item.importancia"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNota(item.competenciaCodigo, 'importancia', normalizarValorNota(v))"
            />
            <span v-else>{{ formatarNota(item.importancia) }}</span>
          </template>

          <template #cell(dominio)="{ item }">
            <BFormSelect
                v-if="podeEditar"
                :data-testid="`autoavaliacao-dominio-${item.competenciaCodigo}`"
                :disabled="!podeEditar"
                :model-value="item.dominio"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNota(item.competenciaCodigo, 'dominio', normalizarValorNota(v))"
            />
            <span v-else>{{ formatarNota(item.dominio) }}</span>
          </template>
        </BTable>
      </BCard>

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

      <BCard v-if="ehChefe" class="mb-4">
        <BCardHeader>
          <strong>Equipe</strong>
          <BBadge v-if="pendentes > 0" class="ms-2" variant="warning">
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
                  @click="abrirModalImpossibilitar(membro)"
              >
                {{ TEXTOS.diagnostico.BTN_IMPOSSIBILITAR }}
              </BButton>
            </div>
          </BListGroupItem>
        </BListGroup>
      </BCard>
    </template>

    <ModalConfirmacao
        v-model="modalConcluirAberto"
        :loading="concluindo"
        :mensagem="TEXTOS.diagnostico.MODAL_CONCLUIR_MENSAGEM"
        :titulo="TEXTOS.diagnostico.MODAL_CONCLUIR_TITULO"
        ok-title="Concluir"
        test-id-confirmar="btn-confirmar-concluir"
        @confirmar="confirmarConcluir"
    />

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

    <BModal
        v-model="modalImpossibilitarAberto"
        :title="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_TITULO"
        centered
        @hide="fecharModalImpossibilitar"
    >
      <p v-if="servidorParaImpossibilitar" class="mb-3">
        {{ TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_MENSAGEM(servidorParaImpossibilitar.servidorNome) }}
      </p>
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
        <BButton class="text-secondary" variant="link" @click="fecharModalImpossibilitar">Cancelar</BButton>
        <BButton
            :disabled="impossibilitando"
            data-testid="btn-confirmar-impossibilitar"
            variant="danger"
            @click="confirmarImpossibilitar"
        >
          <BSpinner v-if="impossibilitando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_IMPOSSIBILITAR }}
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
import {useCacheDiagnostico} from '@/composables/useDiagnosticoCache';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {useAutoavaliacaoDiagnostico} from '@/composables/useAutoavaliacaoDiagnostico';
import {useConsensoDiagnostico} from '@/composables/useConsensoDiagnostico';
import {useEquipeDiagnostico} from '@/composables/useEquipeDiagnostico';
import {impossibilitarAvaliacao} from '@/services/diagnosticoService';
import {TEXTOS} from '@/constants/textos';
import type {Atividade, Conhecimento} from '@/types/mapa-modelos';
import type {ItemEquipeDiagnostico, SituacaoAvaliacaoServidor} from '@/types/diagnostico-competencias';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();

const router = useRouter();
const cacheDiagnostico = useCacheDiagnostico();

const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);
const {queryContextoEdicao, podeCriarConsenso} = useDiagnosticoPermissoes(props.codSubprocesso);

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
  aprovarConsenso,
} = useConsensoDiagnostico(props.codSubprocesso);

const {itens: itensEquipe, pendentes} = useEquipeDiagnostico(props.codSubprocesso);

const ehChefe = computed(() => podeCriarConsenso.value);

const ehAutoavaliacaoConcluida = computed(() => situacaoServidor.value === 'AUTOAVALIACAO_CONCLUIDA');
const ehConsensoCriado = computed(() => situacaoServidor.value === 'CONSENSO_CRIADO');
const ehConsensoAprovado = computed(() => situacaoServidor.value === 'CONSENSO_APROVADO');
const podeEditar = computed(
  () =>
    situacaoServidor.value === 'AUTOAVALIACAO_NAO_REALIZADA' ||
    situacaoServidor.value === 'AUTOAVALIACAO_CONCLUIDA',
);

const erroMensagem = ref('');
const alertaSucesso = ref('');

const modalConcluirAberto = ref(false);
const modalAprovarAberto = ref(false);
const modalImpossibilitarAberto = ref(false);
const detalhesCompetenciaAbertos = ref<Record<number, boolean>>({});
const servidorParaImpossibilitar = ref<ItemEquipeDiagnostico | null>(null);
const justificativaImpossibilidade = ref('');
const erroJustificativa = ref('');
const impossibilitando = ref(false);

function abrirModalConcluir() {
  modalConcluirAberto.value = true;
}

function abrirModalAprovar() {
  modalAprovarAberto.value = true;
}

function abrirModalImpossibilitar(servidor: ItemEquipeDiagnostico) {
  servidorParaImpossibilitar.value = servidor;
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
  if (!servidorParaImpossibilitar.value) return;

  try {
    impossibilitando.value = true;
    await impossibilitarAvaliacao(
      props.codSubprocesso,
      servidorParaImpossibilitar.value.servidorTitulo,
      {justificativa: justificativaImpossibilidade.value},
    );
    fecharModalImpossibilitar();
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_IMPOSSIBILITADO;
    cacheDiagnostico.invalidarEquipe(props.codSubprocesso);
    cacheDiagnostico.invalidarUnidade(props.codSubprocesso);
  } catch {
    erroMensagem.value = TEXTOS.diagnostico.ERRO_SALVAR;
  } finally {
    impossibilitando.value = false;
  }
}

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

function voltar() {
  void router.back();
}

function alternarDetalhesCompetencia(competenciaCodigo: number) {
  detalhesCompetenciaAbertos.value[competenciaCodigo] = !detalhesCompetenciaAbertos.value[competenciaCodigo];
}

function podeImpossibilitar(situacao: SituacaoAvaliacaoServidor) {
  return (
    situacao === 'AUTOAVALIACAO_NAO_REALIZADA' ||
    situacao === 'AUTOAVALIACAO_CONCLUIDA' ||
    situacao === 'CONSENSO_CRIADO'
  );
}

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

function formatarNota(valor: number | null): string {
  if (valor === null) return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
  if (valor === 0) return TEXTOS.diagnostico.NOTA_NA;
  return String(valor);
}

function formatarConhecimentos(conhecimentos: Conhecimento[]): string {
  if (conhecimentos.length === 0) {
    return '-';
  }
  return conhecimentos.map((conhecimento) => conhecimento.descricao).join(', ');
}

const colunas = [
  {key: 'competenciaCodigo', label: 'Codigo'},
  {key: 'descricao', label: TEXTOS.diagnostico.COLUNA_COMPETENCIA},
  {key: 'importancia', label: TEXTOS.diagnostico.COLUNA_IMPORTANCIA},
  {key: 'dominio', label: TEXTOS.diagnostico.COLUNA_DOMINIO},
];

const competenciasComDescricao = computed(() => {
  const mapaAtividades = new Map<number, Atividade[]>(
    (queryContextoEdicao.data.value?.mapa.competencias ?? []).map((competencia) => [competencia.codigo, competencia.atividades ?? []]),
  );
  const mapa = Object.fromEntries(
    (contexto.value?.competencias ?? []).map((c) => [c.competenciaCodigo, c.descricao]),
  );
  return competenciasLocais.value.map((c) => ({
    ...c,
    descricao: mapa[c.competenciaCodigo] ?? `Competencia ${c.competenciaCodigo}`,
    atividades: mapaAtividades.get(c.competenciaCodigo) ?? [],
  }));
});

const opcoesNota = [
  {value: 0, text: 'NA'},
  {value: 1, text: '1'},
  {value: 2, text: '2'},
  {value: 3, text: '3'},
  {value: 4, text: '4'},
  {value: 5, text: '5'},
  {value: 6, text: '6'},
];

function normalizarValorNota(valor: unknown): number | null {
  if (valor === null || valor === undefined || valor === '') return null;
  if (typeof valor === 'number') return Number.isNaN(valor) ? null : valor;
  const numero = Number(valor);
  return Number.isNaN(numero) ? null : numero;
}
</script>
