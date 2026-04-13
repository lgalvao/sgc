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

    <!-- Slot para botões de ação -->
    <div class="empty-state-actions d-flex justify-content-center gap-2">
      <slot/>
    </div>
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
  background: #f8f9fa;
  padding-left: 1.5rem;
  padding-right: 1.5rem;
  border: 1px solid var(--bs-border-color-translucent);
}

.empty-state-icon-wrapper {
  font-size: 2.1rem;
  color: #64748b;
  opacity: 0.75;
}

.empty-state-title {
  color: #1e293b;
}

.empty-state-description {
  color: #64748b;
  font-size: 0.95rem;
  line-height: 1.45;
}

:deep(.btn) {
  padding: 0.5rem 1.5rem;
  font-weight: 500;
  border-radius: 0.5rem;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.btn-outline-primary) {
  color: #0a58ca !important; /* Mais escuro para acessibilidade (AA) */
  border-color: #0a58ca !important;
}

:deep(.btn-outline-primary:hover) {
  background-color: #0a58ca !important;
  color: white !important;
  transform: scale(1.02);
}
</style>

