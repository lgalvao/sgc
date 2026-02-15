<template>
  <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-2">
    <div>
      <h2 class="mb-0" :data-testid="titleTestId">{{ title }}</h2>
      <p v-if="subtitle || $slots.default" class="text-muted mb-0 mt-1">
        <slot>{{ subtitle }}</slot>
      </p>
      <div v-if="etapa || proximaAcao" class="d-flex flex-wrap align-items-center gap-2 mt-2" data-testid="contexto-page-header">
        <span v-if="etapa" class="badge bg-light text-dark border">{{ etapa }}</span>
        <small v-if="proximaAcao" class="text-muted">{{ proximaAcao }}</small>
      </div>
    </div>
    <div v-if="$slots.actions" class="d-flex gap-2 align-self-start align-self-md-center" :data-testid="actionsTestId">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string
  subtitle?: string
  etapa?: string
  proximaAcao?: string
  titleTestId?: string
  actionsTestId?: string
}

defineProps<Props>()
</script>
