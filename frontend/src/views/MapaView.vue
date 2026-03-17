<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.mapa.TITULO_TECNICO">
      <template #default>
        <div class="fs-5">
          {{ unidade?.sigla }} - {{ unidade?.nome }}
          <span class="ms-3" data-testid="txt-badge-situacao">{{
              formatSituacaoSubprocesso(subprocessosStore.subprocessoDetalhe?.situacao)
            }}</span>
        </div>
      </template>
      <template #actions>
        <LoadingButton
            v-if="podeVerImpacto"
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
        v-if="mapasStore.erro"
        :model-value="true"
        variant="danger"
        dismissible
        @dismissed="mapasStore.erro = null"
    >
      {{ mapasStore.erro }}
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

    <ModalMapaDisponibilizar
        :field-errors="fieldErrors"
        :loading="loadingDisponibilizacao"
        :mostrar="mostrarModalDisponibilizar"
        :notificacao="notificacaoDisponibilizacao"
        @disponibilizar="disponibilizarMapa"
        @fechar="fecharModalDisponibilizar"
    />

    <ModalConfirmacao
        v-model="mostrarModalExcluirCompetencia"
        :loading="loadingExclusao"
        :mensagem="TEXTOS.mapa.EXCLUSAO_CONFIRMACAO(competenciaParaExcluir?.descricao || '')"
        data-testid="mdl-excluir-competencia"
        test-id-confirmar="btn-confirmar-exclusao-competencia"
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
import {storeToRefs} from "pinia";
import {computed, defineAsyncComponent, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {usePerfil} from "@/composables/usePerfil";
import {useAcesso} from "@/composables/useAcesso";
import {useFormErrors} from '@/composables/useFormErrors';
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useToastStore} from "@/stores/toast";
import type {Atividade, Competencia, SalvarCompetenciaRequest, Unidade} from "@/types/tipos";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {TEXTOS} from "@/constants/textos";

// Lazy loading de componentes pesados ou modais
const ImpactoMapaModal = defineAsyncComponent(() => import("@/components/mapa/ImpactoMapaModal.vue"));
const CriarCompetenciaModal = defineAsyncComponent(() => import("@/components/mapa/CriarCompetenciaModal.vue"));
const ModalMapaDisponibilizar = defineAsyncComponent(() => import("@/components/mapa/ModalMapaDisponibilizar.vue"));

const route = useRoute();
const router = useRouter();
const mapasStore = useMapasStore();
const {mapaCompleto, impactoMapa: impactos} = storeToRefs(mapasStore);
const subprocessosStore = useSubprocessosStore();
const toastStore = useToastStore();
const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
usePerfil();

const codProcesso = computed(() => Number(route.params.codProcesso));
const siglaUnidade = computed(() => String(route.params.siglaUnidade));

const {podeVisualizarImpacto, podeEditarMapa, podeDisponibilizarMapa, habilitarEditarMapa, habilitarDisponibilizarMapa} = useAcesso(subprocesso);
const podeVerImpacto = computed(() => podeVisualizarImpacto.value);

const mostrarModalImpacto = ref(false);
const loadingImpacto = ref(false);

function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
  if (codSubprocesso.value) {
    loadingImpacto.value = true;
    mapasStore.buscarImpactoMapa(codSubprocesso.value)
        .finally(() => loadingImpacto.value = false);
  }
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}

const unidade = ref<Unidade | null>(null);
const codSubprocesso = ref<number | null>(null);

onMounted(async () => {
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
      codProcesso.value,
      siglaUnidade.value,
  );

  if (id) {
    codSubprocesso.value = id;
    const data = await subprocessosStore.buscarContextoEdicao(id);
    if (data) {
      if (data.atividadesDisponiveis) {
        atividades.value = data.atividadesDisponiveis;
      }
      if (data.unidade) {
        unidade.value = data.unidade as Unidade;
      }
    }
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
const podeConfirmarDisponibilizacao = computed(() => {
  return competencias.value.length > 0 && atividadesSemCompetencia.value.length === 0;
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
  mapasStore.erro = null;

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
      await mapasStore.atualizarCompetencia(codSubprocesso.value, competenciaSendoEditada.value.codigo, request);
    } else {
      await mapasStore.adicionarCompetencia(codSubprocesso.value, request);
    }

    const data = await subprocessosStore.buscarContextoEdicao(codSubprocesso.value);
    if (data && data.atividadesDisponiveis) {
      atividades.value = data.atividadesDisponiveis;
    }

    fecharModalCriarNovaCompetencia();
  } catch {
    handleErrors(mapasStore);
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
      await mapasStore.removerCompetencia(
          codSubprocesso.value,
          competenciaParaExcluir.value.codigo,
      );
      const data = await subprocessosStore.buscarContextoEdicao(codSubprocesso.value);
      if (data && data.atividadesDisponiveis) {
        atividades.value = data.atividadesDisponiveis;
      }
      fecharModalExcluirCompetencia();
    } catch {
      handleErrors(mapasStore);
    } finally {
      loadingExclusao.value = false;
    }
  }
}

function fecharModalExcluirCompetencia() {
  mostrarModalExcluirCompetencia.value = false;
  competenciaParaExcluir.value = null;
}

function removerAtividadeAssociada(competenciaId: number, codAtividade: number) {
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

    mapasStore.atualizarCompetencia(
        codSubprocesso.value,
        competencia.codigo,
        request,
    );
  }
}

async function disponibilizarMapa(payload: { dataLimite: string; observacoes: string }) {
  if (!codSubprocesso.value) return;
  mapasStore.erro = null;
  loadingDisponibilizacao.value = true;

  try {
    await mapasStore.disponibilizarMapa(codSubprocesso.value as number, payload);
    fecharModalDisponibilizar();
    toastStore.setPending(TEXTOS.sucesso.MAPA_DISPONIBILIZADO);
    await router.push({name: "Painel"});
  } catch {
    handleErrors(mapasStore);
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
  ModalMapaDisponibilizar,
  podeVerImpacto,
  podeEditarMapa,
  podeDisponibilizarMapa,
  podeConfirmarDisponibilizacao,
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
