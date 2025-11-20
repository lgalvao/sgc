<template>
  <BCard
    class="mb-4"
    data-testid="subprocesso-header"
    no-body
  >
    <BCardBody>
      <p
        class="text-muted small mb-1"
        data-testid="processo-info"
      >
        Processo: {{ processoDescricao }}
      </p>
      <h2
        class="display-6 mb-3"
        data-testid="unidade-info"
      >
        {{ unidadeSigla }} - {{ unidadeNome }}
      </h2>
      <p>
        <span class="fw-bold me-1">Situação:</span>
        <span
          :class="badgeClass(situacao)"
          class="badge"
          data-testid="situacao-badge"
        >{{ situacao }}</span>
      </p>

      <p><strong>Titular:</strong> {{ titularNome }}</p>
      <p class="ms-3">
        <i class="bi bi-telephone-fill me-2" />{{ titularRamal }}
        <i class="bi bi-envelope-fill ms-3 me-2" />{{ titularEmail }}
      </p>

      <template v-if="responsavelNome">
        <p><strong>Responsável:</strong> {{ responsavelNome }}</p>
        <p class="ms-3">
          <i class="bi bi-telephone-fill me-2" />{{ responsavelRamal }}
          <i class="bi bi-envelope-fill ms-3 me-2" />{{ responsavelEmail }}
        </p>
      </template>

      <p v-if="unidadeAtual">
        <strong>Unidade atual:</strong> {{ unidadeAtual }}
      </p>

      <!-- Botão para alterar data limite -->
      <div
        v-if="podeAlterarDataLimite"
        class="mt-3"
      >
        <BButton
          variant="outline-primary"
          @click="handleAlterarDataLimite"
        >
          <i class="bi bi-calendar me-1" />
          Alterar data limite
        </BButton>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import { badgeClass } from '@/utils';
import { BButton, BCard, BCardBody } from 'bootstrap-vue-next';

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
  podeAlterarDataLimite: boolean;
}

defineProps<Props>();

const emit = defineEmits({
  alterarDataLimite: null,
});

const handleAlterarDataLimite = () => {
  emit('alterarDataLimite');
};
</script>