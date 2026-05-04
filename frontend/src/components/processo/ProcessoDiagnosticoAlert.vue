<template>
  <div v-if="exibir" class="mb-3 pt-2">
    <BAlert
        :model-value="true"
        data-testid="alert-diagnostico-organizacional"
        dismissible
        variant="warning"
        @dismissed="$emit('dismiss')"
    >
      <div class="d-flex align-items-start gap-2">
        <i class="bi bi-exclamation-triangle-fill fs-5 mt-1"></i>
        <div>
          <strong>Há unidades sem responsável atual.</strong>
          <div class="mt-1">{{ resumo }}</div>
          <ul class="mb-0 mt-2 ps-3 small">
            <li v-for="grupo in grupos" :key="grupo.tipo">
              {{ grupo.tipo }}: {{ grupo.quantidadeOcorrencias }} ocorrência(s)
            </li>
          </ul>
        </div>
      </div>
    </BAlert>
  </div>
</template>

<script lang="ts" setup>
import {BAlert} from "bootstrap-vue-next";

interface GrupoDiagnostico {
  tipo: string;
  quantidadeOcorrencias: number;
}

defineProps<{
  exibir: boolean;
  resumo: string;
  grupos: GrupoDiagnostico[];
}>();

defineEmits<{
  dismiss: [];
}>();
</script>
