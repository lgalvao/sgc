<template>
  <LayoutPadrao>
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
            :description="TEXTOS.mapa.EMPTY_DESCRIPTION"
            icon="bi-journal-plus"
            :title="TEXTOS.mapa.EMPTY_TITLE"
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
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CompetenciaCard from "@/components/mapa/CompetenciaCard.vue";
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
import type {Atividade, Competencia, SalvarCompetenciaRequest, Unidade} from "@/types/tipos";
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
const {
  mostrarModalImpacto,
  loadingImpacto,
  abrirModalImpacto,
  fecharModalImpacto,
} = useImpactoMapaModal(codSubprocesso, (codigo) => mapasStore.buscarImpactoMapa(codigo));

function sincronizarMapa(mapaAtualizado: unknown) {
  if (mapaAtualizado) {
    mapasStore.mapaCompleto.value = mapaAtualizado as any;
  }
}

async function atualizarContextoEdicao(codigo: number) {
  const data = await subprocessosStore.buscarContextoEdicao(codigo);
  if (!data) {
    return null;
  }

  if (data.atividadesDisponiveis) {
    atividades.value = data.atividadesDisponiveis;
  }

  if (data.unidade) {
    unidade.value = data.unidade as Unidade;
  }

  return data;
}

async function carregarMapaInicial(codigo: number) {
  const data = await atualizarContextoEdicao(codigo);
  if (data?.mapa) {
    sincronizarMapa(data.mapa);
    return;
  }

  await mapasStore.buscarMapaCompleto(codigo);
}

onMounted(async () => {
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(codProcesso.value, siglaUnidade.value);
  if (id) {
    codSubprocesso.value = id;
    await carregarMapaInicial(id);
  }
});

const atividades = ref<Atividade[]>([]);

const competencias = computed(() => mapaCompleto.value?.competencias || []);
const atividadesSemCompetencia = computed(() => {
  if (atividades.value.length === 0) {
    return [];
  }

  const atividadesAssociadas = new Set(
      competencias.value.flatMap((competencia) =>
          (competencia.atividades || []).map((atividade) => atividade.codigo)
      )
  );

  return atividades.value.filter((atividade) => !atividadesAssociadas.has(atividade.codigo));
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

function handleErrors(store: any) {
  setFromNormalizedError(store.lastError);
  if (fieldErrors.value.atividadesAssociadas) fieldErrors.value.atividades = fieldErrors.value.atividadesAssociadas;
  if (fieldErrors.value.atividadesIds) fieldErrors.value.atividades = fieldErrors.value.atividadesIds;
}

function abrirModalDisponibilizar() {
  mostrarModalDisponibilizar.value = true;
  clearErrors();
}

function abrirModalCriarNovaCompetencia(competenciaParaEditar?: Competencia) {
  mostrarModalCriarNovaCompetencia.value = true;
  clearErrors();
  fluxoMapa.clearError();

  if (competenciaParaEditar) {
    competenciaSendoEditada.value = competenciaParaEditar;
  } else {
    competenciaSendoEditada.value = null;
  }
}

function abrirModalCriarLimpo() {
  competenciaSendoEditada.value = null;
  abrirModalCriarNovaCompetencia();
}

function fecharModalCriarNovaCompetencia() {
  mostrarModalCriarNovaCompetencia.value = false;
  clearErrors();
}

function iniciarEdicaoCompetencia(competencia: Competencia) {
  competenciaSendoEditada.value = competencia;
  abrirModalCriarNovaCompetencia(competencia);
}

async function adicionarCompetenciaEFecharModal(dados: { descricao: string; atividadesSelecionadas: number[] }) {
  if (!codSubprocesso.value) return;
  const request: SalvarCompetenciaRequest = {
    descricao: dados.descricao,
    atividadesIds: dados.atividadesSelecionadas,
  };

  try {
    if (competenciaSendoEditada.value) {
      sincronizarMapa(await fluxoMapa.atualizarCompetencia(codSubprocesso.value, competenciaSendoEditada.value.codigo, request));
    } else {
      sincronizarMapa(await fluxoMapa.adicionarCompetencia(codSubprocesso.value, request));
    }

    await atualizarContextoEdicao(codSubprocesso.value);
    fecharModalCriarNovaCompetencia();
  } catch {
    handleErrors(fluxoMapa);
  }
}

function excluirCompetencia(codigo: number) {
  const competencia = competencias.value.find((comp) => comp.codigo === codigo);
  if (competencia) {
    competenciaParaExcluir.value = competencia;
    mostrarModalExcluirCompetencia.value = true;
  }
}

async function confirmarExclusaoCompetencia() {
  if (competenciaParaExcluir.value && codSubprocesso.value) {
    loadingExclusao.value = true;
    try {
      sincronizarMapa(await fluxoMapa.removerCompetencia(
          codSubprocesso.value,
          competenciaParaExcluir.value.codigo,
      ));
      await atualizarContextoEdicao(codSubprocesso.value);
      fecharModalExcluirCompetencia();
    } catch {
      handleErrors(fluxoMapa);
    } finally {
      loadingExclusao.value = false;
    }
  }
}

function fecharModalExcluirCompetencia() {
  mostrarModalExcluirCompetencia.value = false;
  competenciaParaExcluir.value = null;
}

async function removerAtividadeAssociada(competenciaId: number, codAtividade: number) {
  if (!codSubprocesso.value) return;
  const competencia = competencias.value.find(
      (comp) => comp.codigo === competenciaId,
  );
  if (competencia) {
    const atividadesIds = (competencia.atividades || []).map((a) => a.codigo).filter((id) => id !== codAtividade);

    const request: SalvarCompetenciaRequest = {
      descricao: competencia.descricao,
      atividadesIds: atividadesIds,
    };

    sincronizarMapa(await fluxoMapa.atualizarCompetencia(
        codSubprocesso.value,
        competencia.codigo,
        request,
    ));
  }
}

async function disponibilizarMapa(payload: { dataLimite: string; observacoes: string }) {
  if (!codSubprocesso.value) return;
  fluxoMapa.clearError();
  loadingDisponibilizacao.value = true;

  try {
    await fluxoMapa.disponibilizarMapa(codSubprocesso.value as number, payload);
    fecharModalDisponibilizar();
    toastStore.setPending(TEXTOS.sucesso.MAPA_DISPONIBILIZADO);
    await router.push({name: "Painel"});
  } catch {
    handleErrors(fluxoMapa);
  } finally {
    loadingDisponibilizacao.value = false;
  }
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
