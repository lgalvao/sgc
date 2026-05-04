<template>
  <div>
    <p :data-testid="dataTestid" class="mt-2">
      <strong>{{ label }}</strong> {{ nomeExibido }}
    </p>
    <p v-if="contato" :class="detalhesClass">
      <span v-if="contato.ramal" class="me-3">
        <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
        {{ contato.ramal }}
      </span>
      <span v-if="contato.email">
        <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
        <a
            :aria-label="`Enviar e-mail para ${contato.email}`"
            :href="`mailto:${contato.email}`"
        >{{ contato.email }}</a>
      </span>
    </p>
  </div>
</template>

<script lang="ts" setup>
import {computed} from "vue";
import type {Responsavel, Usuario} from "@/types/tipos";

const props = withDefaults(defineProps<{
  label: string;
  contato?: Usuario | Responsavel | null;
  dataTestid?: string;
  nomeFallback?: string;
  detalhesClass?: string;
}>(), {
  contato: null,
  dataTestid: undefined,
  nomeFallback: "",
  detalhesClass: "ms-3 mb-0",
});

const nomeExibido = computed(() => props.contato?.nome ?? props.nomeFallback);
</script>
