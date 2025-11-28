<template>
  <BContainer class="mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h2 class="mb-0">
          Ocupações Críticas
        </h2>
        <small class="text-muted">{{ siglaUnidade }} - {{ nomeUnidade }}</small>
      </div>
      <div class="d-flex gap-2">
        <BButton
          variant="outline-success"
          @click="finalizarIdentificacao"
        >
          <i class="bi bi-check-circle me-2" />Finalizar Identificação
        </BButton>
      </div>
    </div>

    <BAlert
      variant="info"
      :model-value="true"
    >
      <i class="bi bi-info-circle me-2" />
      Identifique as ocupações críticas da unidade baseadas nas competências avaliadas no diagnóstico.
    </BAlert>

    <!-- Lista de ocupações críticas identificadas -->
    <BCard class="mb-4">
      <template #header>
        <h5 class="card-title mb-0">
          Ocupações Críticas Identificadas
        </h5>
      </template>
      <div
        v-if="ocupacoesCriticas.length === 0"
        class="text-muted"
      >
        Nenhuma ocupação crítica identificada ainda.
      </div>
      <div
        v-for="(ocupacao, index) in ocupacoesCriticas"
        :key="index"
        class="border rounded p-3 mb-3"
      >
        <div class="d-flex justify-content-between align-items-start">
          <div class="flex-grow-1">
            <h6 class="mb-2">
              {{ ocupacao.nome }}
            </h6>
            <p class="mb-2 text-muted">
              {{ ocupacao.descricao }}
            </p>
            <div class="mb-2">
              <strong>Competências críticas associadas:</strong>
              <div class="d-flex flex-wrap gap-1 mt-1">
                <span
                  v-for="comp in ocupacao.competenciasCriticas"
                  :key="comp"
                  class="badge bg-warning text-dark"
                >{{ comp }}</span>
              </div>
            </div>
            <small class="text-muted">Nível de criticidade: {{ ocupacao.nivelCriticidade }}/5</small>
          </div>
          <BButton
            variant="outline-danger"
            size="sm"
            @click="removerOcupacao(index)"
          >
            <i class="bi bi-trash" />
          </BButton>
        </div>
      </div>
    </BCard>

    <!-- Formulário para adicionar ocupação crítica -->
    <BCard>
      <template #header>
        <h5 class="card-title mb-0">
          Adicionar Ocupação Crítica
        </h5>
      </template>
      <BForm @submit.prevent="adicionarOcupacao">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Nome da Ocupação</label>
            <BFormInput
              v-model="novaOcupacao.nome"
              type="text"
              required
            />
          </div>
          <div class="col-md-6">
            <label class="form-label">Nível de Criticidade</label>
            <BFormSelect
              v-model="novaOcupacao.nivelCriticidade"
              required
              :options="[
                { value: 1, text: '1 - Baixo' },
                { value: 2, text: '2 - Baixo-Médio' },
                { value: 3, text: '3 - Médio' },
                { value: 4, text: '4 - Alto' },
                { value: 5, text: '5 - Muito Alto' },
              ]"
            />
          </div>
          <div class="col-12">
            <label class="form-label">Descrição</label>
            <BFormTextarea
              v-model="novaOcupacao.descricao"
              rows="3"
              required
            />
          </div>
          <div class="col-12">
            <label class="form-label">Competências Críticas Associadas</label>
            <div class="border rounded p-3">
              <div
                v-for="competencia in competencias"
                :key="competencia.codigo"
                class="form-check"
              >
                <BFormCheckbox
                  :id="'comp-' + competencia.codigo"
                  v-model="novaOcupacao.competenciasCriticas"
                  :value="competencia.descricao"
                >
                  {{ competencia.descricao }}
                </BFormCheckbox>
              </div>
            </div>
          </div>
          <div class="col-12">
            <BButton
              type="submit"
              variant="primary"
            >
              <i class="bi bi-plus-circle me-2" />Adicionar Ocupação
            </BButton>
          </div>
        </div>
      </BForm>
    </BCard>

    <!-- Modal de confirmação -->
    <BModal
      v-model="mostrarModalConfirmacao"
      title="Finalizar Identificação"
      centered
      hide-footer
    >
      <p>Confirma a finalização da identificação de ocupações críticas? Esta ação não poderá ser desfeita.</p>
      <template #footer>
        <BButton
          variant="secondary"
          @click="fecharModalConfirmacao"
        >
          Cancelar
        </BButton>
        <BButton
          variant="success"
          @click="confirmarFinalizacao"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>
  </BContainer>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BCard,
  BContainer,
  BForm,
  BFormCheckbox,
  BFormInput,
  BFormSelect,
  BFormTextarea,
  BModal, useToast,
} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {useMapasStore} from "@/stores/mapas";

import {useProcessosStore} from "@/stores/processos";
import {useUnidadesStore} from "@/stores/unidades";
import type {Competencia, MapaCompleto} from "@/types/tipos";

const route = useRoute();
const router = useRouter();
const mapasStore = useMapasStore();
const unidadesStore = useUnidadesStore();
const processosStore = useProcessosStore();
const toast = useToast(); // Instantiate toast


const codProcesso = computed(() => Number(route.params.codProcesso));
const siglaUnidade = computed(() => route.params.siglaUnidade as string);

const unidade = computed(() =>
    unidadesStore.unidade,
);
const nomeUnidade = computed(() => unidade.value?.nome || "");

const processoAtual = computed(() => processosStore.processoDetalhe);

onMounted(async () => {
  await unidadesStore.buscarUnidade(siglaUnidade.value);
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  // Correção temporária: usando codProcesso como codSubrocesso
  await mapasStore.buscarMapaCompleto(codProcesso.value);
});

const mapa = computed<MapaCompleto | null>(() => {
  return mapasStore.mapaCompleto;
});

const competencias = computed<Competencia[]>(() => {
  return mapa.value?.competencias || [];
});

// Estado das ocupações críticas
const ocupacoesCriticas = ref<
    Array<{
      nome: string;
      descricao: string;
      nivelCriticidade: number;
      competenciasCriticas: string[];
    }>
>([]);

const novaOcupacao = ref({
  nome: "",
  descricao: "",
  nivelCriticidade: 3,
  competenciasCriticas: [] as string[],
});

// Modal
const mostrarModalConfirmacao = ref(false);

function adicionarOcupacao() {
  if (!novaOcupacao.value.nome.trim() || !novaOcupacao.value.descricao.trim()) {
    toast.show({
        title: "Dados incompletos",
        body: "Preencha nome e descrição da ocupação.",
        props: { variant: 'danger', value: true },
    });
    return;
  }

  ocupacoesCriticas.value.push({
    nome: novaOcupacao.value.nome,
    descricao: novaOcupacao.value.descricao,
    nivelCriticidade: novaOcupacao.value.nivelCriticidade,
    competenciasCriticas: [...novaOcupacao.value.competenciasCriticas],
  });

  // Limpar formulário
  novaOcupacao.value = {
    nome: "",
    descricao: "",
    nivelCriticidade: 3,
    competenciasCriticas: [],
  };

  toast.show({
      title: "Ocupação adicionada",
      body: "Ocupação crítica adicionada!",
      props: { variant: 'success', value: true },
  });
}

function removerOcupacao(index: number) {
  ocupacoesCriticas.value.splice(index, 1);
  toast.show({
      title: "Ocupação removida",
      body: "Ocupação crítica removida!",
      props: { variant: 'success', value: true },
  });
}

function finalizarIdentificacao() {
  mostrarModalConfirmacao.value = true;
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false;
}

function confirmarFinalizacao() {
  if (!processoAtual.value) return;

  // TODO: Implementar chamada real ao backend para finalizar identificação
  // Registrar movimentação e alertas é responsabilidade do backend

  toast.show({
      title: "Identificação finalizada",
      body: "A identificação de ocupações críticas foi concluída!",
      props: { variant: 'success', value: true },
  });

  fecharModalConfirmacao();
  router.push("/painel");
}
</script>
