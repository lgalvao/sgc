<template>
  <BContainer class="mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="mb-0">Monitoramento do Diagnóstico</h2>
        <span class="badge bg-secondary">{{ diagnostico?.situacaoLabel }}</span>
      </div>
      <div class="d-flex gap-2">
        <BButton variant="outline-primary" to="/painel">Voltar</BButton>
        <BButton 
          variant="warning" 
          :to="`/diagnostico/${codSubprocesso}/ocupacoes`"
          :disabled="!todosConcluiramAutoavaliacao"
        >
          Ocupações Críticas
        </BButton>
        <BButton 
          variant="success" 
          data-testid="btn-concluir-diagnostico"
          :to="`/diagnostico/${codSubprocesso}/conclusao`"
          :disabled="!diagnostico?.podeSerConcluido"
        >
          Concluir Diagnóstico
        </BButton>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5">
      <BSpinner label="Carregando..." />
    </div>

    <div v-else>
      <div class="row">
        <div class="col-md-4 mb-3">
          <BCard class="h-100 text-center border-primary shadow-sm">
            <h1 class="display-4 fw-bold text-primary">{{ progressoGeral }}%</h1>
            <p class="text-muted mb-0">Progresso Geral</p>
          </BCard>
        </div>
        <div class="col-md-4 mb-3">
            <BCard class="h-100 text-center border-success shadow-sm">
            <h1 class="display-4 fw-bold text-success">{{ totalServidoresConcluidos }}</h1>
            <p class="text-muted mb-0">Servidores Concluídos</p>
            </BCard>
        </div>
        <div class="col-md-4 mb-3">
             <BCard class="h-100 text-center border-warning shadow-sm">
            <h1 class="display-4 fw-bold text-warning">{{ totalServidoresPendentes }}</h1>
            <p class="text-muted mb-0">Servidores Pendentes</p>
             </BCard>
        </div>
      </div>

      <BCard title="Servidores da Unidade" class="shadow-sm">
        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead class="table-light">
              <tr>
                <th>Servidor</th>
                <th>Competências Avaliadas</th>
                <th>Situação</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="servidor in diagnostico?.servidores" :key="servidor.tituloEleitoral">
                <td>
                    <div class="fw-bold">{{ servidor.nome }}</div>
                    <small class="text-muted">{{ servidor.tituloEleitoral }}</small>
                </td>
                <td>
                  <div class="d-flex align-items-center">
                    <div class="progress flex-grow-1 me-2" style="height: 10px;">
                      <div 
                        class="progress-bar" 
                        :class="getProgressBarClass(servidor)"
                        role="progressbar" 
                        :style="`width: ${getProgressoServidor(servidor)}%`"
                      ></div>
                    </div>
                    <span>{{ servidor.competenciasAvaliadas }}/{{ servidor.totalCompetencias }}</span>
                  </div>
                </td>
                <td>
                  <span class="badge" :class="getStatusBadgeClass(servidor.situacao)">
                    {{ servidor.situacaoLabel }}
                  </span>
                </td>
                <td>
                  <!-- Ações futuras: Ver detalhes, Desbloquear, etc -->
                  <BButton size="sm" variant="link" disabled>Detalhes</BButton>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </BCard>
    </div>
  </BContainer>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { BContainer, BButton, BCard, BSpinner } from 'bootstrap-vue-next';
import { useFeedbackStore } from '@/stores/feedback';
import { diagnosticoService, type DiagnosticoDto, type ServidorDiagnosticoDto } from '@/services/diagnosticoService';

const route = useRoute();
const feedbackStore = useFeedbackStore();

const loading = ref(true);
const codSubprocesso = computed(() => Number(route.params.codSubprocesso));
const diagnostico = ref<DiagnosticoDto | null>(null);

const totalServidoresConcluidos = computed(() => 
    diagnostico.value?.servidores.filter(s => 
        s.situacao === 'AUTOAVALIACAO_CONCLUIDA' || 
        s.situacao === 'CONSENSO_APROVADO').length || 0
);

const totalServidoresPendentes = computed(() => 
    (diagnostico.value?.servidores.length || 0) - totalServidoresConcluidos.value
);

const todosConcluiramAutoavaliacao = computed(() => totalServidoresPendentes.value === 0);

const progressoGeral = computed(() => {
    if (!diagnostico.value || diagnostico.value.servidores.length === 0) return 0;
    return Math.round((totalServidoresConcluidos.value / diagnostico.value.servidores.length) * 100);
});

onMounted(async () => {
    try {
        loading.value = true;
        diagnostico.value = await diagnosticoService.buscarDiagnostico(codSubprocesso.value);
    } catch (error) {
        feedbackStore.show('Erro', 'Erro ao carregar monitoramento.', 'danger');
    } finally {
        loading.value = false;
    }
});

function getProgressoServidor(s: ServidorDiagnosticoDto) {
    if (s.totalCompetencias === 0) return 0;
    return Math.round((s.competenciasAvaliadas / s.totalCompetencias) * 100);
}

function getProgressBarClass(s: ServidorDiagnosticoDto) {
    if (s.situacao === 'AUTOAVALIACAO_CONCLUIDA') return 'bg-success';
    if (s.competenciasAvaliadas > 0) return 'bg-primary';
    return 'bg-secondary';
}

function getStatusBadgeClass(situacao: string) {
    switch (situacao) {
        case 'AUTOAVALIACAO_CONCLUIDA': return 'bg-success';
        case 'AUTOAVALIACAO_NAO_REALIZADA': return 'bg-secondary';
        case 'CONSENSO_CRIADO': return 'bg-warning text-dark';
        case 'CONSENSO_APROVADO': return 'bg-success';
        case 'AVALIACAO_IMPOSSIBILITADA': return 'bg-dark';
        default: return 'bg-light text-dark';
    }
}
</script>
