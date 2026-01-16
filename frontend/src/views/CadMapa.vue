<template>
  <BContainer class="mt-4">
    <div class="fs-5 mb-3">
      {{ unidade?.sigla }} - {{ unidade?.nome }}
      <span class="ms-3" data-testid="txt-badge-situacao">{{
          subprocessosStore.subprocessoDetalhe?.situacaoLabel || situacaoLabel(subprocessosStore.subprocessoDetalhe?.situacao)
        }}</span>
    </div>

    <BAlert
        v-if="mapasStore.lastError"
        :model-value="true"
        variant="danger"
        dismissible
        @dismissed="mapasStore.clearError()"
    >
      {{ mapasStore.lastError.message }}
      <div v-if="mapasStore.lastError.details">
        <small>Detalhes: {{ mapasStore.lastError.details }}</small>
      </div>
    </BAlert>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="display-6 mb-3">
        Mapa de competências técnicas
      </div>
      <div class="d-flex gap-2">
        <BButton
            v-if="podeVerImpacto"
            data-testid="cad-mapa__btn-impactos-mapa"
            variant="outline-secondary"
            @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2"/>Impacto no mapa
        </BButton>
        <BButton
            v-if="podeDisponibilizarMapa"
            :disabled="competencias.length === 0"
            data-testid="btn-cad-mapa-disponibilizar"
            variant="outline-success"
            @click="abrirModalDisponibilizar"
        >
          Disponibilizar
        </BButton>
      </div>
    </div>

    <div v-if="unidade">
      <div v-if="competencias.length === 0" class="mb-4 mt-3">
        <EmptyState
            icon="bi-journal-plus"
            title="Vamos começar?"
            description="Nenhuma competência cadastrada para esta unidade."
        >
          <BButton
              v-if="podeEditarMapa"
              data-testid="btn-abrir-criar-competencia-empty"
              variant="primary"
              @click="abrirModalCriarLimpo"
          >
            <i class="bi bi-plus-lg me-2"/> Criar primeira competência
          </BButton>
        </EmptyState>
      </div>

      <div v-else class="mb-4 mt-3">
        <BButton
            v-if="podeEditarMapa"
            class="mb-3"
            data-testid="btn-abrir-criar-competencia"
            variant="outline-primary"
            @click="abrirModalCriarLimpo"
        >
          <i class="bi bi-plus-lg"/> Criar competência
        </BButton>

        <CompetenciaCard
            v-for="comp in competencias"
            :key="comp.codigo"
            :atividades="atividades"
            :competencia="comp"
            :pode-editar="podeEditarMapa"
            @editar="iniciarEdicaoCompetencia"
            @excluir="excluirCompetencia"
            @remover-atividade="removerAtividadeAssociada"
        />
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
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
        @disponibilizar="disponibilizarMapa"
        @fechar="fecharModalDisponibilizar"
    />

    <ModalConfirmacao
        v-model="mostrarModalExcluirCompetencia"
        data-testid="mdl-excluir-competencia"
        titulo="Exclusão de competência"
        :mensagem="`Confirma a exclusão da competência '${competenciaParaExcluir?.descricao}'?`"
        variant="danger"
        test-id-confirmar="btn-confirmar-exclusao-competencia"
        @confirmar="confirmarExclusaoCompetencia"
    />

    <ImpactoMapaModal
        v-if="codSubprocesso"
        :impacto="impactoMapa"
        :loading="loadingImpacto"
        :mostrar="mostrarModalImpacto"
        @fechar="fecharModalImpacto"
    />
  </BContainer>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BContainer,} from "bootstrap-vue-next";
import EmptyState from "@/components/EmptyState.vue";
import {storeToRefs} from "pinia";
import {computed, defineAsyncComponent, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {usePerfil} from "@/composables/usePerfil";
import {useFormErrors} from '@/composables/useFormErrors';
import {situacaoLabel} from "@/utils";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import type {Atividade, Competencia} from "@/types/tipos";
import CompetenciaCard from "@/components/CompetenciaCard.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";

// Lazy loading de componentes pesados ou modais
const ImpactoMapaModal = defineAsyncComponent(() => import("@/components/ImpactoMapaModal.vue"));
const CriarCompetenciaModal = defineAsyncComponent(() => import("@/components/CriarCompetenciaModal.vue"));
const DisponibilizarMapaModal = defineAsyncComponent(() => import("@/components/DisponibilizarMapaModal.vue"));

const route = useRoute();
const router = useRouter();
const mapasStore = useMapasStore();
const {mapaCompleto, impactoMapa} = storeToRefs(mapasStore);
const atividadesStore = useAtividadesStore();
const subprocessosStore = useSubprocessosStore();
const unidadesStore = useUnidadesStore();
usePerfil();

const codProcesso = computed(() => Number(route.params.codProcesso));
const siglaUnidade = computed(() => String(route.params.siglaUnidade));

const podeVerImpacto = computed(() => {
  return (
      subprocessosStore.subprocessoDetalhe?.permissoes?.podeVisualizarImpacto ||
      false
  );
});

const podeEditarMapa = computed(() => {
  return (
      subprocessosStore.subprocessoDetalhe?.permissoes?.podeEditarMapa ||
      false
  );
});

const podeDisponibilizarMapa = computed(() => {
  return (
      subprocessosStore.subprocessoDetalhe?.permissoes?.podeDisponibilizarMapa ||
      false
  );
});

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
  if (typeof codSubprocesso.value !== "number") {
    return [];
  }
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
  mapasStore.clearError();

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
    atividadesAssociadas: dados.atividadesSelecionadas,
  } as any;

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
    try {
      await mapasStore.removerCompetencia(
          codSubprocesso.value as number,
          competenciaParaExcluir.value.codigo,
      );
      await subprocessosStore.buscarContextoEdicao(codSubprocesso.value as number);
      fecharModalExcluirCompetencia();
    } catch {
      handleErrors(mapasStore);
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
      atividadesAssociadas: competencia.atividadesAssociadas.filter(
          (id) => id !== atividadeId,
      ),
    };
    mapasStore.atualizarCompetencia(
        codSubprocesso.value as number,
        competenciaAtualizada,
    );
  }
}

async function disponibilizarMapa(payload: { dataLimite: string; observacoes: string }) {
  if (!codSubprocesso.value) return;
  mapasStore.clearError();
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
</script>

<style scoped>
/* Estilos restantes que não foram para componentes */
</style>
