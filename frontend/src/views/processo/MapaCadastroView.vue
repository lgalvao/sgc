<template>
  <LayoutPadrao>
    <PageHeader title="Mapa de competências técnicas">
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
            variant="outline-secondary"
            icon="arrow-right-circle"
            text="Impacto no mapa"
            @click="abrirModalImpacto"
        />
        <BButton
            v-if="podeDisponibilizarMapa"
            :disabled="competencias.length === 0"
            data-testid="btn-cad-mapa-disponibilizar"
            variant="outline-success"
            @click="abrirModalDisponibilizar"
        >
          Disponibilizar
        </BButton>
      </template>
    </PageHeader>

    <ErrorAlert
        :error="mapasStore.erro ? { message: mapasStore.erro } : null"
        @dismiss="mapasStore.erro = null"
    />

    <CompetenciasListSection
        :unidade="unidade"
        :competencias="competencias"
        :atividades="atividades"
        :pode-editar="podeEditarMapa"
        @criar="abrirModalCriarLimpo"
        @editar="iniciarEdicaoCompetencia"
        @excluir="excluirCompetencia"
        @remover-atividade="removerAtividadeAssociada"
    />

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
        data-testid="mdl-excluir-competencia"
        titulo="Exclusão de competência"
        :mensagem="`Confirma a exclusão da competência '${competenciaParaExcluir?.descricao}'?`"
        variant="danger"
        test-id-confirmar="btn-confirmar-exclusao-competencia"
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
import {BButton} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import CompetenciasListSection from "@/components/mapa/CompetenciasListSection.vue";
import {storeToRefs} from "pinia";
import {computed, defineAsyncComponent, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {usePerfil} from "@/composables/usePerfil";
import {useAcesso} from "@/composables/useAcesso";
import {useFormErrors} from '@/composables/useFormErrors';
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import type {Atividade, Competencia} from "@/types/tipos";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {formatSituacaoSubprocesso} from "@/utils/formatters";

// Lazy loading de componentes pesados ou modais
const ImpactoMapaModal = defineAsyncComponent(() => import("@/components/mapa/ImpactoMapaModal.vue"));
const CriarCompetenciaModal = defineAsyncComponent(() => import("@/components/mapa/CriarCompetenciaModal.vue"));
const ModalMapaDisponibilizar = defineAsyncComponent(() => import("@/components/mapa/ModalMapaDisponibilizar.vue"));

const route = useRoute();
const router = useRouter();
const mapasStore = useMapasStore();
const {mapaCompleto, impactoMapa: impactos} = storeToRefs(mapasStore);
const atividadesStore = useAtividadesStore();
const subprocessosStore = useSubprocessosStore();
const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
const unidadesStore = useUnidadesStore();
usePerfil();

const codProcesso = computed(() => Number(route.params.codProcesso));
const siglaUnidade = computed(() => String(route.params.siglaUnidade));

const {podeVisualizarImpacto, podeEditarMapa, podeDisponibilizarMapa} = useAcesso(subprocesso);
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

const unidade = computed(() => unidadesStore.unidade);
const codSubprocesso = ref<number | null>(null);

onMounted(async () => {
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
      codProcesso.value,
      siglaUnidade.value,
  );

  if (id) {
    codSubprocesso.value = id;
    await subprocessosStore.buscarContextoEdicao(id);
  }
});

const atividades = computed<Atividade[]>(() => {
  if (!codSubprocesso.value) return [];
  return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value) || [];
});

const competencias = computed(() => mapaCompleto.value?.competencias || []);
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
  const competencia: Competencia = {
    codigo: competenciaSendoEditada.value?.codigo ?? undefined,
    descricao: dados.descricao,
    atividadesAssociadas: dados.atividadesSelecionadas} as any;

  try {
    if (competenciaSendoEditada.value) {
      await mapasStore.atualizarCompetencia(codSubprocesso.value as number, competencia);
    } else {
      await mapasStore.adicionarCompetencia(codSubprocesso.value as number, competencia);
    }

    await subprocessosStore.buscarContextoEdicao(codSubprocesso.value as number);

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
  if (competenciaParaExcluir.value) {
    loadingExclusao.value = true;
    try {
      await mapasStore.removerCompetencia(
          codSubprocesso.value as number,
          competenciaParaExcluir.value.codigo,
      );
      await subprocessosStore.buscarContextoEdicao(codSubprocesso.value as number);
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

function removerAtividadeAssociada(competenciaId: number, atividadeId: number) {
  const competencia = competencias.value.find(
      (comp) => comp.codigo === competenciaId,
  );
  if (competencia) {
    const competenciaAtualizada = {
      ...competencia,
      atividadesAssociadas: (competencia.atividadesAssociadas || []).filter(
          (id) => id !== atividadeId,
      )};
    mapasStore.atualizarCompetencia(
        codSubprocesso.value as number,
        competenciaAtualizada,
    );
  }
}

async function disponibilizarMapa(payload: { dataLimite: string; observacoes: string }) {
  if (!codSubprocesso.value) return;
  mapasStore.erro = null;
  loadingDisponibilizacao.value = true;

  try {
    await mapasStore.disponibilizarMapa(codSubprocesso.value, payload);
    fecharModalDisponibilizar();
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
  unidade,
  competencias,
  atividades,
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
