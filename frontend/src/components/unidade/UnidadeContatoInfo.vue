<template>
  <div class="contato-unidade">
    <p
        :data-testid="dataTestid"
        class="contato-unidade__titulo mt-2 mb-1"
    >
      <strong>{{ label }}</strong>
      <span>{{ nomeExibido }}</span>
    </p>
    <p
        v-if="descricao"
        class="contato-unidade__descricao ms-3 mb-1 text-muted"
        data-testid="unidade-contato-descricao"
    >
      {{ descricao }}
    </p>
    <p
        v-if="contato"
        :class="detalhesClass"
        class="contato-unidade__detalhes"
    >
      <span v-if="contato.ramal" class="me-3">
        <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
        {{ contato.ramal }}
      </span>
      <span v-if="contato.email">
        <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
        <a
            :aria-label="`Enviar e-mail para ${contato.email}`"
            class="link-discreto"
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
  descricao?: string;
  detalhesClass?: string;
}>(), {
  contato: null,
  dataTestid: undefined,
  nomeFallback: "",
  descricao: "",
  detalhesClass: "ms-3 mb-0",
});

const nomeExibido = computed(() => props.contato?.nome ?? props.nomeFallback);
</script>

<style scoped>
.contato-unidade__titulo {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  align-items: baseline;
}

.contato-unidade__detalhes {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}
</style>
