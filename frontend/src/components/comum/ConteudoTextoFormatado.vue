<script lang="ts" setup>
withDefaults(defineProps<{
  conteudo: string;
  vazio?: string;
  testId?: string;
}>(), {
  vazio: "-",
  testId: undefined,
});
</script>

<template>
  <!-- eslint-disable vue/no-v-html -->
  <div
      v-if="conteudo"
      :data-testid="testId"
      class="conteudo-texto-formatado"
      v-html="conteudo"
  />
  <span v-else :data-testid="testId">{{ vazio }}</span>
</template>

<style scoped>
.conteudo-texto-formatado {
  line-height: 1.55;
  word-break: break-word;
}

.conteudo-texto-formatado :deep(p:last-child),
.conteudo-texto-formatado :deep(ul:last-child),
.conteudo-texto-formatado :deep(ol:last-child) {
  margin-bottom: 0;
}

.conteudo-texto-formatado :deep(ul),
.conteudo-texto-formatado :deep(ol) {
  padding-left: 1.4rem;
}
</style>
