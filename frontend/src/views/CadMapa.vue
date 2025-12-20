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
      <div class="mb-4 mt-3">
        <BButton
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
        :mostrar="mostrarModalDisponibilizar"
        :notificacao="notificacaoDisponibilizacao"
        @disponibilizar="disponibilizarMapa"
        @fechar="fecharModalDisponibilizar"
    />

    <!-- Modal de Exclusão de Competência (Simples demais para componente separado por enquanto, ou usar ModalAcaoBloco?) -->
    <BModal
        v-model="mostrarModalExcluirCompetencia"
        :fade="false"
        cancel-title="Cancelar"
        centered
        data-testid="mdl-excluir-competencia"
        ok-title="Confirmar"
        ok-variant="danger"
        title="Exclusão de competência"
        @hidden="fecharModalExcluirCompetencia"
        @ok="confirmarExclusaoCompetencia"
    >
      <p>Confirma a exclusão da competência "{{ competenciaParaExcluir?.descricao }}"?</p>
    </BModal>

    <ImpactoMapaModal
        v-if="codSubprocesso"
        :cod-subprocesso="codSubprocesso"
        :mostrar="mostrarModalImpacto"
        @fechar="fecharModalImpacto"
    />
  </BContainer>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BContainer, BModal,} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import CompetenciaCard from "@/components/CompetenciaCard.vue";
import CriarCompetenciaModal from "@/components/CriarCompetenciaModal.vue";
import DisponibilizarMapaModal from "@/components/DisponibilizarMapaModal.vue";
import {usePerfil} from "@/composables/usePerfil";
import {situacaoLabel} from "@/utils";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import type {Atividade, Competencia} from "@/types/tipos";
import {useFormErrors} from "@/composables/useFormErrors";
import {normalizeError} from "@/utils/apiError";

const route = useRoute();
const router = useRouter();
const mapasStore = useMapasStore();
const {mapaCompleto} = storeToRefs(mapasStore);
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

const mostrarModalImpacto = ref(false);

function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}

const unidade = computed(() => unidadesStore.unidade);
const codSubprocesso = ref<number | null>(null);

onMounted(async () => {
  try {
    const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
        codProcesso.value,
        siglaUnidade.value,
    );

    if (id) {
      codSubprocesso.value = id;
      const contexto = await subprocessosStore.buscarContextoEdicao(id);

      unidadesStore.unidade = contexto.unidade;
      subprocessosStore.subprocessoDetalhe = contexto.subprocesso;
      mapasStore.mapaCompleto = contexto.mapa;
      atividadesStore.atividadesPorSubprocesso.set(id, contexto.atividadesDisponiveis);
    }
  } catch (e) {
    console.error("Erro ao carregar contexto de edição", e);
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

const { errors: fieldErrors, setFromNormalizedError, clearErrors } = useFormErrors(['descricao', 'atividades', 'dataLimite', 'observacoes', 'generic']);

function handleApiErrors(error: any, defaultMsg: string) {
  const normalized = normalizeError(error);
  setFromNormalizedError(normalized);

  // Custom handling for aliases and generic errors
  if (normalized.subErrors) {
    const genericErrors: string[] = [];
    normalized.subErrors.forEach(e => {
      if (e.field === 'atividadesAssociadas') fieldErrors.value.atividades = e.message || 'Inválido';
      else if (!['descricao', 'atividades', 'dataLimite', 'observacoes'].includes(e.field || '')) {
        genericErrors.push(e.message || 'Inválido');
      }
    });
    if (genericErrors.length > 0) {
      fieldErrors.value.generic = genericErrors.join('; ');
    }
  }

  // If no specific field errors, use the main message
  const hasFieldErrors = fieldErrors.value.descricao || fieldErrors.value.atividades || fieldErrors.value.dataLimite || fieldErrors.value.observacoes || fieldErrors.value.generic;

  if (!hasFieldErrors) {
    fieldErrors.value.generic = normalized.message || defaultMsg;
  }
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

    await Promise.all([
      mapasStore.buscarMapaCompleto(codSubprocesso.value as number),
      subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value as number),
    ]);

    fecharModalCriarNovaCompetencia();
  } catch (error) {
    handleApiErrors(error, "Erro ao salvar competência");
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
      await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value as number);
      fecharModalExcluirCompetencia();
    } catch (error) {
      handleApiErrors(error, "Erro ao remover competência");
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

  try {
    await mapasStore.disponibilizarMapa(codSubprocesso.value, payload);
    fecharModalDisponibilizar();
    await router.push({name: "Painel"});
  } catch (error) {
    handleApiErrors(error, "Erro ao disponibilizar mapa");
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