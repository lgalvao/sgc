<template>
  <div>
    <div
        v-if="mostrarBotoesBloco"
        class="mt-3 d-flex gap-2"
    >
      <BButton
          v-if="perfil === 'GESTOR'"
          data-testid="btn-acao-bloco-aceitar"
          variant="outline-primary"
          @click="emit('aceitarBloco')"
      >
        <i aria-hidden="true" class="bi bi-check-circle me-1"/>
        Aceitar em bloco
      </BButton>
      <BButton
          v-if="perfil === 'ADMIN'"
          data-testid="btn-acao-bloco-homologar"
          variant="outline-success"
          @click="emit('homologarBloco')"
      >
        <i aria-hidden="true" class="bi bi-check-all me-1"/>
        Homologar em bloco
      </BButton>
    </div>
    <BButton
        v-if="perfil === 'ADMIN' && situacaoProcesso === 'EM_ANDAMENTO'"
        class="mt-3"
        data-testid="btn-processo-finalizar"
        variant="danger"
        @click="emit('finalizar')"
    >
      Finalizar processo
    </BButton>
  </div>
</template>

<script lang="ts" setup>
import {BButton} from "bootstrap-vue-next";
import type {Perfil, SituacaoProcesso} from "@/types/tipos";

defineProps<{
  mostrarBotoesBloco: boolean;
  perfil: Perfil | string | null;
  situacaoProcesso: SituacaoProcesso | string;
}>();

const emit = defineEmits<{
  (e: "aceitarBloco"): void;
  (e: "homologarBloco"): void;
  (e: "finalizar"): void;
}>();
</script>
