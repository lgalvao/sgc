<template>
  <BContainer class="mt-4">
    <PageHeader title="Histórico de processos" subtitle="Lista de processos finalizados." />

    <BCard no-body class="shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th scope="col" style="width: 60%">Processo</th>
              <th scope="col" style="width: 20%">Tipo</th>
              <th scope="col" style="width: 20%">Finalizado em</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="3" class="text-center py-4">
                <BSpinner label="Carregando..." variant="primary" />
              </td>
            </tr>
            <tr v-else-if="processos.length === 0">
              <td colspan="3">
                <EmptyState
                    icon="bi-folder2-open"
                    title="Nenhum processo finalizado"
                    description="Os processos finalizados aparecerão aqui."
                    class="border-0 bg-transparent mb-0"
                />
              </td>
            </tr>
            <tr
              v-for="proc in processos"
              v-else
              :key="proc.codigo"
              class="cursor-pointer"
              tabindex="0"
              @click="verDetalhes(proc.codigo)"
              @keydown.enter.prevent="verDetalhes(proc.codigo)"
              @keydown.space.prevent="verDetalhes(proc.codigo)"
            >
              <td>
                <div class="fw-bold">{{ proc.descricao }}</div>
              </td>
              <td>
                <span :class="['badge', getBadgeClass(proc.tipo)]">
                  {{ proc.tipoLabel }}
                </span>
              </td>
              <td>{{ proc.dataFinalizacaoFormatada }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </BCard>
  </BContainer>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import {useRouter} from 'vue-router';
import {BCard, BContainer, BSpinner} from 'bootstrap-vue-next';
import PageHeader from '@/components/layout/PageHeader.vue';
import EmptyState from '@/components/EmptyState.vue';
import {useProcessosStore} from '@/stores/processos';
import {logger} from '@/utils';

const router = useRouter();
const processosStore = useProcessosStore();
const processos = computed(() => processosStore.processosFinalizados);
const loading = ref(false);

async function carregarHistorico() {
  loading.value = true;
  try {
    await processosStore.buscarProcessosFinalizados();
  } catch (e) {
    logger.error("Erro ao carregar histórico:", e);
  } finally {
    loading.value = false;
  }
}

function verDetalhes(codigo: number) {
  router.push(`/processo/${codigo}`);
}

function getBadgeClass(tipo: string): string {
  const map: Record<string, string> = {
    'MAPEAMENTO': 'bg-primary',
    'REVISAO': 'bg-info text-dark',
    'DIAGNOSTICO': 'bg-warning text-dark'
  };
  return map[tipo] || 'bg-secondary';
}

onMounted(() => {
  carregarHistorico();
});
</script>

<style scoped>
tbody tr:focus-visible {
  outline: 2px solid var(--bs-primary);
  background-color: var(--bs-table-hover-bg);
  z-index: 1; /* Ensure outline sits on top of adjacent borders */
  position: relative;
}
</style>
