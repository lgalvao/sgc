<template>
  <BContainer class="mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h2 class="mb-0">
          Diagnóstico da Equipe
        </h2>
        <small class="text-muted">{{ siglaUnidade }} - {{ nomeUnidade }}</small>
      </div>
      <div class="d-flex gap-2">
        <BButton
          variant="outline-success"
          @click="finalizarDiagnostico"
        >
          <i class="bi bi-check-circle me-2" />Finalizar Diagnóstico
        </BButton>
      </div>
    </div>

    <BAlert
      variant="info"
      :model-value="true"
    >
      <i class="bi bi-info-circle me-2" />
      Nesta etapa, os servidores da unidade devem avaliar a importância e o domínio das competências da unidade.
    </BAlert>

    <!-- Lista de competências para avaliação -->
    <div
      v-if="competencias.length > 0"
      class="row"
    >
      <div
        v-for="competencia in competencias"
        :key="competencia.codigo"
        class="col-md-6 mb-4"
      >
        <BCard class="h-100">
          <template #header>
            <h5 class="card-title mb-0">
              {{ competencia.descricao }}
            </h5>
          </template>
          <div class="mb-3">
            <label class="form-label fw-bold">Importância da competência:</label>
            <BFormSelect
              v-model="avaliacoes[competencia.codigo].importancia"
              :options="[
                { value: 1, text: '1 - Muito baixa' },
                { value: 2, text: '2 - Baixa' },
                { value: 3, text: '3 - Média' },
                { value: 4, text: '4 - Alta' },
                { value: 5, text: '5 - Muito alta' },
              ]"
            />
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold">Domínio da competência pela equipe:</label>
            <BFormSelect
              v-model="avaliacoes[competencia.codigo].dominio"
              :options="[
                { value: 1, text: '1 - Muito baixo' },
                { value: 2, text: '2 - Baixo' },
                { value: 3, text: '3 - Médio' },
                { value: 4, text: '4 - Alto' },
                { value: 5, text: '5 - Muito alto' },
              ]"
            />
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold">Observações:</label>
            <BFormTextarea
              v-model="avaliacoes[competencia.codigo].observacoes"
              rows="2"
              placeholder="Comentários sobre esta competência..."
            />
          </div>
        </BCard>
      </div>
    </div>

    <BAlert
      v-else
      variant="warning"
      :model-value="true"
    >
      <i class="bi bi-exclamation-triangle me-2" />
      Nenhum mapa de competências disponível para diagnóstico.
    </BAlert>

    <!-- Modal de confirmação -->
    <BModal
      v-model="mostrarModalConfirmacao"
      title="Finalizar Diagnóstico"
      centered
      hide-footer
    >
      <p>Confirma a finalização do diagnóstico da equipe? Esta ação não poderá ser desfeita.</p>
      <BAlert
        v-if="avaliacoesPendentes.length > 0"
        variant="warning"
        :model-value="true"
      >
        <strong>Atenção:</strong> As seguintes competências ainda não foram avaliadas:
        <ul class="mb-0 mt-2">
          <li
            v-for="comp in avaliacoesPendentes"
            :key="comp.codigo"
          >
            {{ comp.descricao }}
          </li>
        </ul>
      </BAlert>
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
import {BAlert, BButton, BCard, BContainer, BFormSelect, BFormTextarea, BModal, useToast,} from "bootstrap-vue-next";
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
const toast = useToast(); // Instantiate useToast


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
  competencias.value.forEach((comp) => {
    if (!avaliacoes.value[comp.codigo]) {
      avaliacoes.value[comp.codigo] = {
        importancia: 3,
        dominio: 3,
        observacoes: "",
      };
    }
  });
});

const mapa = computed<MapaCompleto | null>(() => {
  return mapasStore.mapaCompleto;
});

const competencias = computed<Competencia[]>(() => {
  return mapa.value?.competencias || [];
});

// Estado das avaliações
const avaliacoes = ref<
    Record<
        string,
        {
          importancia: number;
          dominio: number;
          observacoes: string;
        }
    >
>({});

// Modal
const mostrarModalConfirmacao = ref(false);

// Inicializar avaliações
onMounted(() => {
  competencias.value.forEach((comp) => {
    if (!avaliacoes.value[comp.codigo]) {
      avaliacoes.value[comp.codigo] = {
        importancia: 3,
        dominio: 3,
        observacoes: "",
      };
    }
  });
});

const avaliacoesPendentes = computed(() => {
  return competencias.value.filter((comp) => {
    const aval = avaliacoes.value[comp.codigo];
    return !aval || aval.importancia === 0 || aval.dominio === 0;
  });
});

function finalizarDiagnostico() {
  mostrarModalConfirmacao.value = true;
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false;
}

function confirmarFinalizacao() {
  if (!processoAtual.value) return;

  // TODO: Implementar chamada real ao backend para finalizar diagnóstico
  // Registrar movimentação e alertas é responsabilidade do backend

  toast.create({
      title: "Diagnóstico finalizado",
      body: "O diagnóstico da equipe foi concluído!",
      props: { variant: 'success', value: true },
  });

  fecharModalConfirmacao();
  router.push("/painel");
}
</script>

<style scoped>
.card {
  transition: box-shadow 0.2s;
}

.card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
