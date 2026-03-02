<template>
  <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-2">
    <div>
      <h2 :data-testid="titleTestId" class="mb-0">{{ title }}</h2>
      <p v-if="subtitle || $slots.default" class="text-muted mb-0 mt-1">
        <slot>{{ subtitle }}</slot>
      </p>
    </div>
    <div v-if="$slots.actions" :data-testid="actionsTestId" class="d-flex gap-2 align-self-start align-self-md-center">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script lang="ts" setup>
interface Props {
  title: string
  subtitle?: string
  titleTestId?: string
  actionsTestId?: string
}

defineProps<Props>()
</script>
