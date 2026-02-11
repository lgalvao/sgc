<template>
  <div>
    <div
        v-if="podeAceitarBloco || podeHomologarBloco"
        class="mt-3 d-flex gap-2"
    >
      <BButton
          v-if="podeAceitarBloco"
          data-testid="btn-acao-bloco-aceitar"
          variant="outline-primary"
          @click="$emit('aceitarBloco')"
      >
        <i aria-hidden="true" class="bi bi-check-circle me-1"/>
        Aceitar em bloco
      </BButton>
      <BButton
          v-if="podeHomologarBloco"
          data-testid="btn-acao-bloco-homologar"
          variant="outline-success"
          @click="$emit('homologarBloco')"
      >
        <i aria-hidden="true" class="bi bi-check-all me-1"/>
        Homologar em bloco
      </BButton>
    </div>
    <BButton
        v-if="podeFinalizar"
        class="mt-3"
        data-testid="btn-processo-finalizar"
        variant="danger"
        @click="$emit('finalizar')"
    >
      Finalizar processo
    </BButton>
  </div>
</template>

<script lang="ts" setup>
import {BButton} from "bootstrap-vue-next";

defineProps<{
  podeAceitarBloco: boolean;
  podeHomologarBloco: boolean;
  podeFinalizar: boolean;
}>();

defineEmits<{
  (e: "aceitarBloco"): void;
  (e: "homologarBloco"): void;
  (e: "finalizar"): void;
}>();
</script>
