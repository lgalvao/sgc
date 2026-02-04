<template>
  <section
      class="text-center py-4 border rounded bg-light mb-3 text-muted empty-state"
      data-testid="empty-state"
      aria-label="Estado vazio"
  >
    <i
        v-if="icon"
        :class="`bi ${icon} fs-3 d-block mb-2 text-secondary`"
        aria-hidden="true"
    />
    <p
        v-if="title"
        class="h5 fw-normal mb-1"
        data-testid="empty-state-title"
    >
      {{ title }}
    </p>
    <p
        v-if="description"
        class="small mb-2"
        data-testid="empty-state-description"
    >
      {{ description }}
    </p>

    <!-- Slot para botões de ação -->
    <slot />
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
  background-color: var(--bs-light);
  border: 1px dashed var(--bs-gray-300) !important;
}
</style>
