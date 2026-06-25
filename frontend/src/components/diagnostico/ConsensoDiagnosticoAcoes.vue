<template>
  <div class="d-flex flex-wrap justify-content-end gap-2">
    <BButton
        v-if="podeConcluirAvaliacao"
        :disabled="concluindoAvaliacao || !habilitarConcluirAvaliacao"
        data-testid="btn-concluir-avaliacao"
        size="sm"
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
        size="sm"
        variant="success"
        @click="$emit('aprovarConsenso')"
    >
      <BSpinner v-if="aprovando" aria-hidden="true" class="me-1" small/>
      {{ TEXTOS.diagnostico.BTN_APROVAR_CONSENSO }}
    </BButton>
    <BButton size="sm" variant="outline-secondary" @click="$emit('voltar')">
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

defineEmits<{
  (e: 'aprovarConsenso'): void;
  (e: 'concluirAvaliacao'): void;
  (e: 'voltar'): void;
}>();
</script>
