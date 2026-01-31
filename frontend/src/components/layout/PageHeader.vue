<template>
  <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-2">
    <div>
      <h2 class="mb-0" :data-testid="titleTestId">{{ title }}</h2>
      <p v-if="subtitle || $slots.default" class="text-muted mb-0 mt-1">
        <slot>{{ subtitle }}</slot>
      </p>
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
  titleTestId?: string
  actionsTestId?: string
}

defineProps<Props>()
</script>
