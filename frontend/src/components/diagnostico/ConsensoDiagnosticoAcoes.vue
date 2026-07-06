<template>
  <div class="d-flex flex-wrap justify-content-end gap-2">
    <BButton
        v-if="podeConcluirAvaliacao"
        :disabled="concluindoAvaliacao || !habilitarConcluirAvaliacao"
        data-testid="btn-concluir-avaliacao"
        variant="success"
        @click="$emit('concluirAvaliacao')"
    >
      <BSpinner v-if="concluindoAvaliacao" aria-hidden="true" class="me-1" small/>
      {{ TEXTOS.diagnostico.BTN_CONCLUIR_AVALIACAO }}
    </BButton>
    <BButton
        v-if="podeAprovarConsenso || servidorEhUsuarioLogado"
        :disabled="aprovando || !habilitarAprovarConsenso"
        data-testid="btn-aprovar-consenso"
        variant="success"
        @click="$emit('aprovarConsenso')"
    >
      <BSpinner v-if="aprovando" aria-hidden="true" class="me-1" small/>
      {{ TEXTOS.diagnostico.BTN_APROVAR_CONSENSO }}
    </BButton>
    <BButton
        class="btn-voltar-diagnostico"
        variant="light"
        :style="estiloBotaoVoltar"
        @click="$emit('voltar')"
    >
      <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
      {{ TEXTOS.diagnostico.BTN_VOLTAR }}
    </BButton>
  </div>
</template>

<script lang="ts" setup>
import {BButton, BSpinner} from 'bootstrap-vue-next';
import {TEXTOS} from '@/constants/textos';

defineProps<{
  aprovando: boolean;
  concluindoAvaliacao: boolean;
  habilitarAprovarConsenso: boolean;
  habilitarConcluirAvaliacao: boolean;
  podeAprovarConsenso: boolean;
  podeConcluirAvaliacao: boolean;
  servidorEhUsuarioLogado: boolean;
}>();

const estiloBotaoVoltar = {
  '--bs-btn-color': '#f8fafc',
  '--bs-btn-border-color': '#f8fafc',
  color: '#f8fafc',
  borderColor: '#f8fafc'
};

defineEmits<{
  (e: 'aprovarConsenso'): void;
  (e: 'concluirAvaliacao'): void;
  (e: 'voltar'): void;
}>();
</script>

<style scoped>
:global([data-bs-theme="dark"] .btn-voltar-diagnostico .bi) {
  color: inherit !important;
}
</style>
