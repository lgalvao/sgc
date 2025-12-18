<template>
  <BCard
      class="mb-4"
      data-testid="header-subprocesso"
      no-body
  >
    <BCardBody>
      <p
          class="text-muted small mb-1"
          data-testid="txt-header-processo"
      >
        Processo: {{ processoDescricao }}
      </p>
      <div class="mb-3">
        <h2
            class="display-6 mb-0"
            data-testid="subprocesso-header__txt-header-unidade"
        >
          {{ unidadeSigla }}
        </h2>
        <div class="text-muted">
          {{ unidadeNome }}
        </div>
      </div>
      <p>
        <span class="fw-bold me-1">Situação:</span>
        <span
            data-testid="subprocesso-header__txt-situacao"
        >{{ situacao }}</span>
      </p>

      <p><strong>Titular:</strong> {{ titularNome }}</p>
      <p class="ms-3">
        <i class="bi bi-telephone-fill me-2"/>{{ titularRamal }}
        <i class="bi bi-envelope-fill ms-3 me-2"/>{{ titularEmail }}
      </p>

      <template v-if="responsavelNome && responsavelNome !== titularNome">
        <p><strong>Responsável:</strong> {{ responsavelNome }}</p>
        <p class="ms-3">
          <i class="bi bi-telephone-fill me-2"/>{{ responsavelRamal }}
          <i class="bi bi-envelope-fill ms-3 me-2"/>{{ responsavelEmail }}
        </p>
      </template>

      <!-- Botão para alterar data limite -->
      <div
          v-if="podeAlterarDataLimite"
          class="mt-3"
      >
        <BButton
            data-testid="btn-alterar-data-limite"
            variant="outline-primary"
            @click="handleAlterarDataLimite"
        >
          <i class="bi bi-calendar me-1"/>
          Alterar data limite
        </BButton>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody} from "bootstrap-vue-next";

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
  podeAlterarDataLimite: boolean;
}

defineProps<Props>();

const emit = defineEmits({
  alterarDataLimite: null,
});

const handleAlterarDataLimite = () => {
  emit("alterarDataLimite");
};
</script>