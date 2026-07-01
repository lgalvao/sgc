<template>
  <section
      aria-label="Estado vazio"
      class="text-center py-4 rounded-3 mb-3 empty-state"
      data-testid="empty-state"
  >
    <div class="empty-state-icon-wrapper mb-2">
      <i
          v-if="icon"
          :class="`bi ${icon} d-block`"
          aria-hidden="true"
      />
    </div>
    <h2
        v-if="title"
        class="h5 fw-semibold mb-2 empty-state-title"
        data-testid="empty-state-title"
    >
      {{ title }}
    </h2>
    <p
        v-if="description"
        class="mb-3 empty-state-description mx-auto"
        data-testid="empty-state-description"
        style="max-width: 400px;"
    >
      {{ description }}
    </p>

  </section>
</template>

<script lang="ts" setup>
defineProps<{
  title?: string;
  description?: string;
  icon?: string;
}>();
</script>

<style scoped>
.empty-state {
  background: var(--bs-tertiary-bg);
  padding-left: 1.5rem;
  padding-right: 1.5rem;
  border: 1px solid var(--bs-border-color);
}

.empty-state-icon-wrapper {
  font-size: 1.5rem;
  color: var(--bs-secondary-color);
  opacity: 0.7;
}

.empty-state-title {
  color: var(--bs-heading-color);
}

.empty-state-description {
  color: var(--bs-secondary-color);
  font-size: 0.95rem;
  line-height: 1.45;
}

</style>
