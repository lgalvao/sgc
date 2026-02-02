<template>
  <BContainer class="mt-4">
    <PageHeader title="Histórico de Processos" subtitle="Lista de processos finalizados." />

    <BCard no-body class="shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th scope="col" style="width: 40%">Processo</th>
              <th scope="col" style="width: 20%">Tipo</th>
              <th scope="col" style="width: 20%">Finalizado em</th>
              <th scope="col" style="width: 20%">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="4" class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">Carregando...</span>
                </div>
              </td>
            </tr>
            <tr v-else-if="processos.length === 0">
              <td colspan="4">
                <EmptyState
                    icon="bi-folder2-open"
                    title="Nenhum processo finalizado encontrado"
                    description="Os processos finalizados aparecerão aqui."
                    class="border-0 bg-transparent mb-0"
                />
              </td>
            </tr>
            <tr v-for="proc in processos" v-else :key="proc.codigo">
              <td>
                <div class="fw-bold">{{ proc.descricao }}</div>
                <!-- <small class="text-muted">{{ formatarParticipantes(proc) }}</small> -->
              </td>
              <td>
                <span :class="['badge', getBadgeClass(proc.tipo)]">
                  {{ proc.tipoLabel }}
                </span>
              </td>
              <td>{{ proc.dataFinalizacaoFormatada || formatDateBR(proc.dataFinalizacao) }}</td>
              <td>
                <BButton
                  size="sm"
                  variant="outline-primary"
                  @click="verDetalhes(proc.codigo)"
                >
                  <i aria-hidden="true" class="bi bi-eye"></i> Detalhes
                </BButton>
              </td>
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
import {BButton, BCard, BContainer} from 'bootstrap-vue-next';
import PageHeader from '@/components/layout/PageHeader.vue';
import EmptyState from '@/components/EmptyState.vue';
import {useProcessosStore} from '@/stores/processos';
import {logger} from '@/utils';
import {formatDateBR} from '@/utils/dateUtils';

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
  router.push(`/processos/${codigo}`);
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
