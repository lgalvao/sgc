<template>
  <span :class="classesBadge" data-testid="badge-situacao">
    {{ texto }}
  </span>
</template>

<script lang="ts" setup>
import {computed} from "vue";
import {SituacaoProcesso} from "@/types/tipos";

const props = defineProps<{
  situacao: SituacaoProcesso | string;
  texto?: string;
}>();

const classesBadge = computed(() => {
  const base = "badge";
  if (props.situacao === SituacaoProcesso.FINALIZADO) return `${base} bg-success`;
  if (props.situacao === SituacaoProcesso.EM_ANDAMENTO) return `${base} bg-primary`;
  if (props.situacao === SituacaoProcesso.CRIADO) return `${base} bg-secondary`;
  return `${base} bg-dark`;
});

const texto = computed(() => props.texto || props.situacao);
</script>
