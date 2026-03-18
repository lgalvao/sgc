<template>
  <section
      aria-label="Estado vazio"
      class="text-center py-4 border rounded bg-body-tertiary mb-3 empty-state"
      data-testid="empty-state"
  >
    <i
        v-if="icon"
        :class="`bi ${icon} fs-4 d-block mb-2 empty-state-icon`"
        aria-hidden="true"
    />
    <p
        v-if="title"
        class="h5 fw-normal mb-1 empty-state-title"
        data-testid="empty-state-title"
    >
      {{ title }}
    </p>
    <p
        v-if="description"
        class="small mb-2 empty-state-description"
        data-testid="empty-state-description"
    >
      {{ description }}
    </p>

    <!-- Slot para botões de ação -->
    <slot/>
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
  background-color: var(--bs-tertiary-bg);
  border: 1px dashed var(--bs-gray-400) !important;
}

.empty-state-title {
  color: var(--bs-secondary-color);
}

.empty-state-description,
.empty-state-icon {
  color: var(--bs-secondary-color);
}
</style>
