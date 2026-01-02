<template>
  <BContainer class="mt-4">
    <BRow class="mb-4">
      <BCol>
        <h2 class="display-6">Histórico de Processos</h2>
        <p class="text-muted">
          Lista de processos finalizados.
        </p>
      </BCol>
    </BRow>

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
              <td colspan="4" class="text-center py-4 text-muted">
                Nenhum processo finalizado encontrado.
              </td>
            </tr>
            <tr v-for="proc in processos" v-else :key="proc.codigo">
              <td>
                <div class="fw-bold">{{ proc.descricao }}</div>
                <!-- <small class="text-muted">{{ formatarParticipantes(proc) }}</small> -->
              </td>
              <td>
                <span :class="['badge', getBadgeClass(proc.tipo)]">
                  {{ formatarTipo(proc.tipo) }}
                </span>
              </td>
              <td>{{ formatarData(proc.dataFinalizacao) }}</td>
              <td>
                <BButton
                  size="sm"
                  variant="outline-primary"
                  @click="verDetalhes(proc.codigo)"
                >
                  <i class="bi bi-eye"></i> Detalhes
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { BContainer, BRow, BCol, BCard, BButton } from 'bootstrap-vue-next';
import { apiClient } from '@/axios-setup';

interface ProcessoResumo {
  codigo: number;
  descricao: string;
  tipo: string;
  dataFinalizacao: string;
  // participantes: ... (se necessário agregar, mas o backend pode não mandar simples)
}

const router = useRouter();
const processos = ref<ProcessoResumo[]>([]);
const loading = ref(false);

async function carregarHistorico() {
  loading.value = true;
  try {
    const response = await apiClient.get<ProcessoResumo[]>('/processos/finalizados');
    processos.value = response.data;
  } catch (e) {
    console.error("Erro ao carregar histórico:", e);
  } finally {
    loading.value = false;
  }
}

function verDetalhes(codigo: number) {
  router.push(`/processos/${codigo}`);
}

function formatarTipo(tipo: string): string {
  const map: Record<string, string> = {
    'MAPEAMENTO': 'Mapeamento',
    'REVISAO': 'Revisão',
    'DIAGNOSTICO': 'Diagnóstico'
  };
  return map[tipo] || tipo;
}

function getBadgeClass(tipo: string): string {
  const map: Record<string, string> = {
    'MAPEAMENTO': 'bg-primary',
    'REVISAO': 'bg-info text-dark',
    'DIAGNOSTICO': 'bg-warning text-dark'
  };
  return map[tipo] || 'bg-secondary';
}

function formatarData(data: string): string {
  if (!data) return '-';
  return new Date(data).toLocaleDateString('pt-BR');
}

onMounted(() => {
  carregarHistorico();
});
</script>
