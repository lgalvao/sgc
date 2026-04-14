<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial" />
    <template v-else>
      <PageHeader :title="TEXTOS.mapa.TITULO_TECNICO">
        <template #default>
          <div class="fs-5" data-testid="subprocesso-header__txt-header-unidade">
            {{ unidade?.sigla }}
          </div>
        </template>

        <template #actions>
          <LoadingButton
              v-if="podeVisualizarImpacto"
              :loading="loadingImpacto"
              data-testid="cad-mapa__btn-impactos-mapa"
              icon="arrow-right-circle"
              :text="TEXTOS.mapa.BOTAO_IMPACTO"
              variant="outline-secondary"
              @click="abrirModalImpacto"
          />
          <BButton
              v-if="podeEditarMapa"
              :disabled="!habilitarEditarMapa"
              data-testid="btn-abrir-criar-competencia"
              variant="outline-primary"
              @click="abrirModalCriarLimpo"
          >
            <i aria-hidden="true" class="bi bi-plus-lg me-1"/> {{ TEXTOS.mapa.BOTAO_CRIAR }}
          </BButton>
          <BButton
              v-if="podeDisponibilizarMapa"
              :disabled="!habilitarDisponibilizarMapa || !podeConfirmarDisponibilizacao"
              data-testid="btn-cad-mapa-disponibilizar"
              variant="success"
              @click="abrirModalDisponibilizar"
          >
            {{ TEXTOS.mapa.BOTAO_DISPONIBILIZAR }}
          </BButton>
        </template>
      </PageHeader>

      <BAlert
          v-if="erroMapa"
          :model-value="true"
          variant="danger"
          dismissible
          @dismissed="erroMapa = null"
      >
        {{ erroMapa }}
      </BAlert>

      <div v-if="unidade">
        <div v-if="competencias.length === 0" class="mb-4 mt-3">
          <EmptyState
              title="Lista de competências"
              :description="TEXTOS.mapa.EMPTY_DESCRIPTION"
              class="border-0 bg-transparent mb-0 px-0"
              icon="bi-journal-plus"
          />
        </div>

        <div v-else class="mb-4 mt-3">
          <CompetenciaCard
              v-for="comp in competencias"
              :key="comp.codigo"
              :atividades="atividades"
              :competencia="comp"
              :pode-editar="podeEditarMapa"
              @editar="iniciarEdicaoCompetencia"
              @excluir="excluirCompetencia"
              @remover-atividade="(competenciaId, codAtividade) => removerAtividadeAssociada(competenciaId, codAtividade)"
          />
        </div>
      </div>

      <div v-else>
        <p>{{ TEXTOS.mapa.UNIDADE_NAO_ENCONTRADA }}</p>
      </div>

      <CriarCompetenciaModal
          :atividades="atividades"
          :competencia-para-editar="competenciaSendoEditada"
          :field-errors="fieldErrors"
          :mostrar="mostrarModalCriarNovaCompetencia"
          @fechar="fecharModalCriarNovaCompetencia"
          @salvar="adicionarCompetenciaEFecharModal"
      />

      <DisponibilizarMapaModal
          :field-errors="fieldErrors"
          :loading="loadingDisponibilizacao"
          :mostrar="mostrarModalDisponibilizar"
          :notificacao="notificacaoDisponibilizacao"
          :ultima-data-limite-subprocesso="subprocesso?.ultimaDataLimiteSubprocesso"
          @disponibilizar="disponibilizarMapa"
          @fechar="fecharModalDisponibilizar"
      />

      <ModalConfirmacao
          v-model="mostrarModalExcluirCompetencia"
          :loading="loadingExclusao"
          :mensagem="TEXTOS.mapa.EXCLUSAO_CONFIRMACAO(competenciaParaExcluir?.descricao || '')"
          data-testid="mdl-excluir-competencia"
          test-codigo-confirmar="btn-confirmar-exclusao-competencia"
          :titulo="TEXTOS.mapa.EXCLUSAO_TITULO"
          variant="danger"
          @confirmar="confirmarExclusaoCompetencia"
      />

      <ImpactoMapaModal
          v-if="codSubprocesso"
          :impacto="impactos"
          :loading="loadingImpacto"
          :mostrar="mostrarModalImpacto"
          @fechar="fecharModalImpacto"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CompetenciaCard from "@/components/mapa/CompetenciaCard.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import {computed, defineAsyncComponent, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {usePerfil} from "@/composables/usePerfil";
import {useAcesso} from "@/composables/useAcesso";
import {useFluxoMapa} from "@/composables/useFluxoMapa";
import {useFormErrors} from '@/composables/useFormErrors';
import {useImpactoMapaModal} from "@/composables/useImpactoMapaModal";
import {useMapas} from "@/composables/useMapas";
import {useSubprocessos} from "@/composables/useSubprocessos";
import {useToastStore} from "@/stores/toast";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import type {Atividade, Competencia, MapaCompleto, SalvarCompetenciaRequest, Unidade} from "@/types/tipos";
import type {NormalizedError} from "@/utils/apiError";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

// Lazy loading de componentes pesados ou modais
const ImpactoMapaModal = defineAsyncComponent(() => import("@/components/mapa/ImpactoMapaModal.vue"));
const CriarCompetenciaModal = defineAsyncComponent(() => import("@/components/mapa/CriarCompetenciaModal.vue"));
const DisponibilizarMapaModal = defineAsyncComponent(() => import("@/components/mapa/DisponibilizarMapaModal.vue"));

const route = useRoute();
const router = useRouter();
const mapasStore = useMapas();
const fluxoMapa = useFluxoMapa();
const {mapaCompleto, impactoMapa: impactos, erro: erroMapa} = mapasStore;
const subprocessosStore = useSubprocessos();
const toastStore = useToastStore();
const subprocessoStoreCache = useSubprocessoStore();
const {invalidarCachesSubprocesso, limparEstadoSubprocessoAtual} = useInvalidacaoNavegacao();
const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
usePerfil();

const codProcesso = computed(() => Number(route.params.codProcesso));
const siglaUnidade = computed(() => String(route.params.siglaUnidade));

const {
  podeVisualizarImpacto,
  podeEditarMapa,
  podeDisponibilizarMapa,
  habilitarEditarMapa,
  habilitarDisponibilizarMapa
} = useAcesso(subprocesso);


const unidade = ref<Unidade | null>(null);
const codSubprocesso = ref<number | null>(null);
const carregandoInicial = ref(true);
const {
  mostrarModalImpacto,
  loadingImpacto,
  abrirModalImpacto,
  fecharModalImpacto,
} = useImpactoMapaModal(codSubprocesso, (codigo) => mapasStore.buscarImpactoMapa(codigo));

async function carregarContextoEdicao(codigo: number) {
  const data = await subprocessoStoreCache.garantirContextoEdicao(codigo);
  if (!data) {
    return null;
  }

  subprocessosStore.subprocessoDetalhe = data.detalhes;
  atividades.value = data.atividadesDisponiveis;
  unidade.value = data.unidade;

  return data;
}

async function carregarContextoInicial() {
  const codigoQuery = Number(route.query.codSubprocesso);
  const resultado = Number.isFinite(codigoQuery) && codigoQuery > 0
      ? await subprocessoStoreCache.garantirContextoEdicao(codigoQuery)
          .then((contexto) => contexto ? {codigo: codigoQuery, contexto} : null)
      : await subprocessoStoreCache.garantirContextoEdicaoPorProcessoEUnidade(
          codProcesso.value,
          siglaUnidade.value,
      );

  if (!resultado) {
    return null;
  }

  codSubprocesso.value = resultado.codigo;
  subprocessosStore.subprocessoDetalhe = resultado.contexto.detalhes;
  atividades.value = resultado.contexto.atividadesDisponiveis;
  unidade.value = resultado.contexto.unidade;

  return resultado.contexto;
}

async function executarComSubprocesso(
    callback: (codigoSubprocesso: number) => Promise<void>
) {
  const codigoSubprocesso = codSubprocesso.value;
  if (!codigoSubprocesso) return;
  await callback(codigoSubprocesso);
}

function sincronizarMapa(mapaAtualizado: MapaCompleto | null | undefined) {
  if (mapaAtualizado) {
    mapasStore.mapaCompleto.value = mapaAtualizado;
    subprocessoStoreCache.invalidar();
  }
}

async function carregarMapaInicial(codigo: number, contextoInicial?: Awaited<ReturnType<typeof carregarContextoEdicao>> | null) {
  const data = contextoInicial ?? await carregarContextoEdicao(codigo);
  if (data?.mapa) {
    sincronizarMapa(data.mapa);
    return;
  }

  await mapasStore.buscarMapaCompleto(codigo);
}

onMounted(async () => {
  try {
    limparEstadoSubprocessoAtual();
    const contextoInicial = await carregarContextoInicial();
    if (!codSubprocesso.value) {
      return;
    }
    await carregarMapaInicial(codSubprocesso.value, contextoInicial);
  } finally {
    carregandoInicial.value = false;
  }
});

const atividades = ref<Atividade[]>([]);

const competencias = computed(() => mapaCompleto.value?.competencias || []);
const codigosAtividadesAssociadas = computed(() => {
  return new Set(
      competencias.value.flatMap((competencia) =>
          (competencia.atividades || []).map((atividade) => atividade.codigo)
      )
  );
});
const atividadesSemCompetencia = computed(() => {
  if (atividades.value.length === 0) {
    return [];
  }

  return atividades.value.filter((atividade) => !codigosAtividadesAssociadas.value.has(atividade.codigo));
});

const existeCompetenciaSemAtividade = computed(() => {
  return competencias.value.some((competencia) => (competencia.atividades?.length ?? 0) === 0);
});

const associacoesMapaValidas = computed(() => {
  return !existeCompetenciaSemAtividade.value && atividadesSemCompetencia.value.length === 0;
});

const podeConfirmarDisponibilizacao = computed(() => {
  return (
      competencias.value.length > 0
      && associacoesMapaValidas.value
  );
});
const competenciaSendoEditada = ref<Competencia | null>(null);

const mostrarModalCriarNovaCompetencia = ref(false);
const mostrarModalDisponibilizar = ref(false);
const mostrarModalExcluirCompetencia = ref(false);
const competenciaParaExcluir = ref<Competencia | null>(null);
const notificacaoDisponibilizacao = ref("");
const loadingDisponibilizacao = ref(false);
const loadingExclusao = ref(false);

const {errors: fieldErrors, setFromNormalizedError, clearErrors} = useFormErrors([
  'descricao',
  'atividades',
  'atividadesIds',
  'atividadesAssociadas',
  'dataLimite',
  'observacoes',
  'generic'
]);

function handleErrors(store: { lastError: unknown }) {
  setFromNormalizedError(store.lastError as NormalizedError | null);
  if (fieldErrors.value.atividadesAssociadas) fieldErrors.value.atividades = fieldErrors.value.atividadesAssociadas;
  if (fieldErrors.value.atividadesIds) fieldErrors.value.atividades = fieldErrors.value.atividadesIds;
}

function abrirModalCriarNovaCompetencia(competenciaParaEditar: Competencia | null = null) {
  mostrarModalCriarNovaCompetencia.value = true;
  clearErrors();
  fluxoMapa.clearError();
  competenciaSendoEditada.value = competenciaParaEditar;
}

function abrirModalCriarLimpo() {
  abrirModalCriarNovaCompetencia();
}

function fecharModalCriarNovaCompetencia() {
  mostrarModalCriarNovaCompetencia.value = false;
  clearErrors();
}

function iniciarEdicaoCompetencia(competencia: Competencia) {
  abrirModalCriarNovaCompetencia(competencia);
}

function abrirModalDisponibilizar() {
  mostrarModalDisponibilizar.value = true;
  clearErrors();
}

async function adicionarCompetenciaEFecharModal(dados: { descricao: string; atividadesSelecionadas: number[] }) {
  await executarComSubprocesso(async (codigoSubprocesso) => {
    const request: SalvarCompetenciaRequest = {
      descricao: dados.descricao,
      atividadesIds: dados.atividadesSelecionadas,
    };

    try {
      if (competenciaSendoEditada.value) {
        sincronizarMapa(await fluxoMapa.atualizarCompetencia(codigoSubprocesso, competenciaSendoEditada.value.codigo, request));
      } else {
        sincronizarMapa(await fluxoMapa.adicionarCompetencia(codigoSubprocesso, request));
      }

      await carregarContextoEdicao(codigoSubprocesso);
      fecharModalCriarNovaCompetencia();
    } catch {
      handleErrors(fluxoMapa);
    }
  });
}

function excluirCompetencia(codigo: number) {
  const competencia = competencias.value.find((comp) => comp.codigo === codigo);
  if (competencia) {
    competenciaParaExcluir.value = competencia;
    mostrarModalExcluirCompetencia.value = true;
  }
}

async function confirmarExclusaoCompetencia() {
  const competencia = competenciaParaExcluir.value;
  if (!competencia) return;

  await executarComSubprocesso(async (codigoSubprocesso) => {
    loadingExclusao.value = true;
    try {
      sincronizarMapa(await fluxoMapa.removerCompetencia(codigoSubprocesso, competencia.codigo));
      await carregarContextoEdicao(codigoSubprocesso);
      fecharModalExcluirCompetencia();
    } catch {
      handleErrors(fluxoMapa);
    } finally {
      loadingExclusao.value = false;
    }
  });
}

function fecharModalExcluirCompetencia() {
  mostrarModalExcluirCompetencia.value = false;
  competenciaParaExcluir.value = null;
}

async function removerAtividadeAssociada(competenciaId: number, codAtividade: number) {
  const competencia = competencias.value.find((comp) => comp.codigo === competenciaId);
  if (!competencia) return;

  await executarComSubprocesso(async (codigoSubprocesso) => {
    const atividadesIds = (competencia.atividades || []).map((atividade) => atividade.codigo)
        .filter((codigoAtividade) => codigoAtividade !== codAtividade);
    const request: SalvarCompetenciaRequest = {
      descricao: competencia.descricao,
      atividadesIds: atividadesIds,
    };

    sincronizarMapa(await fluxoMapa.atualizarCompetencia(
        codigoSubprocesso,
        competencia.codigo,
        request,
    ));
  });
}

async function disponibilizarMapa(payload: { dataLimite: string; observacoes: string }) {
  await executarComSubprocesso(async (codigoSubprocesso) => {
    fluxoMapa.clearError();
    loadingDisponibilizacao.value = true;

    try {
      await fluxoMapa.disponibilizarMapa(codigoSubprocesso, payload);
      fecharModalDisponibilizar();
      toastStore.setPending(TEXTOS.sucesso.MAPA_DISPONIBILIZADO);
      invalidarCachesSubprocesso();
      await router.push({name: "Painel"});
    } catch {
      handleErrors(fluxoMapa);
    } finally {
      loadingDisponibilizacao.value = false;
    }
  });
}

function fecharModalDisponibilizar() {
  mostrarModalDisponibilizar.value = false;
  notificacaoDisponibilizacao.value = "";
  clearErrors();
}

defineExpose({
  ImpactoMapaModal,
  CriarCompetenciaModal,
  DisponibilizarMapaModal,
  podeVisualizarImpacto,
  podeEditarMapa,
  podeDisponibilizarMapa,
  podeConfirmarDisponibilizacao,
  existeCompetenciaSemAtividade,
  associacoesMapaValidas,
  unidade,
  competencias,
  atividades,
  atividadesSemCompetencia,
  impactoMapa: impactos,
  mostrarModalImpacto,
  loadingImpacto,
  abrirModalImpacto,
  abrirModalCriarLimpo,
  iniciarEdicaoCompetencia,
  excluirAtividade: confirmarExclusaoCompetencia,
  removerAtividadeAssociada,
  abrirModalDisponibilizar,
  disponibilizarMapa,
  fecharModalDisponibilizar,
  fecharModalImpacto,
  fecharModalCriarNovaCompetencia,
  adicionarCompetenciaEFecharModal,
  confirmarExclusaoCompetencia,
  fecharModalExcluirCompetencia,
  excluirCompetencia
});
</script>
