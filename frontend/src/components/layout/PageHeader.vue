<template>
  <div class="page-header mb-3">
    <div class="page-header__principal d-flex flex-column flex-md-row justify-content-between align-items-md-start gap-3">
      <div class="page-header__heading">
        <h2 :data-testid="titleTestId" class="page-header__title mb-0">{{ title }}</h2>
        <p v-if="subtitle || $slots.default" class="page-header__subtitle text-muted mb-0 mt-1">
          <slot>{{ subtitle }}</slot>
        </p>
      </div>
      <div
          v-if="$slots.actions"
          :data-testid="actionsTestId"
          class="page-header__actions d-flex flex-wrap gap-2 align-self-start align-self-md-start justify-content-md-end"
      >
        <slot name="actions"></slot>
      </div>
    </div>

    <div v-if="$slots.alerta" class="page-header__alerta mt-3">
      <slot name="alerta"></slot>
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

<style scoped>
.page-header__principal {
  min-width: 0;
}

.page-header__heading {
  flex: 1 1 auto;
  min-width: 0;
}

.page-header__title,
.page-header__subtitle {
  overflow-wrap: anywhere;
}

.page-header__actions {
  flex: 0 1 auto;
}

.page-header__actions :deep(.btn) {
  white-space: nowrap;
}
</style>
