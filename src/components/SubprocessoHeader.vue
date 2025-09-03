<template>
  <div class="card mb-4" data-testid="subprocesso-header">
    <div class="card-body">
      <p class="text-muted small mb-1" data-testid="processo-info">Processo: {{ processoDescricao }}</p>
      <h2 class="display-6 mb-3" data-testid="unidade-info">{{ unidadeSigla }} - {{ unidadeNome }}</h2>
      <p>
        <span class="fw-bold me-1">Situação:</span>
        <span :class="badgeClass(situacao)" class="badge" data-testid="situacao-badge">{{ situacao }}</span>
      </p>

      <p><strong>Titular:</strong> {{ titularNome }}</p>
      <p class="ms-3">
        <i class="bi bi-telephone-fill me-2"></i>{{ titularRamal }}
        <i class="bi bi-envelope-fill ms-3 me-2"></i>{{ titularEmail }}
      </p>

      <template v-if="responsavelNome">
        <p><strong>Responsável:</strong> {{ responsavelNome }}</p>
        <p class="ms-3">
          <i class="bi bi-telephone-fill me-2"></i>{{ responsavelRamal }}
          <i class="bi bi-envelope-fill ms-3 me-2"></i>{{ responsavelEmail }}
        </p>
      </template>

      <p v-if="unidadeAtual">
        <strong>Unidade atual:</strong> {{ unidadeAtual }}
      </p>

      <!-- Botão para alterar data limite (apenas para ADMIN) -->
      <div v-if="mostrarBotaoAlterarData && isSubprocessoEmAndamento" class="mt-3">
        <button class="btn btn-outline-primary" @click="$emit('alterarDataLimite')">
          <i class="bi bi-calendar me-1"></i>
          Alterar data limite
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed} from 'vue';
import {Perfil} from '@/types/tipos';
import {CLASSES_BADGE_SITUACAO} from '@/constants/situacoes';

interface Props {
  processoDescricao: string;
  unidadeSigla: string;
  unidadeNome: string;
  situacao: string;
  titularNome: string;
  titularRamal: string;
  titularEmail: string;
  responsavelNome?: string;
  responsavelRamal?: string;
  responsavelEmail?: string;
  unidadeAtual?: string;
  perfilUsuario: Perfil | null;
  isSubprocessoEmAndamento: boolean;
}

const props = defineProps<Props>();

defineEmits<{
  alterarDataLimite: [];
}>();

const mostrarBotaoAlterarData = computed(() => props.perfilUsuario === Perfil.ADMIN);

function badgeClass(situacao: string): string {
  return CLASSES_BADGE_SITUACAO[situacao as keyof typeof CLASSES_BADGE_SITUACAO] || 'bg-secondary';
}
</script>