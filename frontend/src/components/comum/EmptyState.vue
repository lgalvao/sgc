<template>
  <section
      aria-label="Estado vazio"
      class="text-center py-5 rounded-4 mb-3 empty-state shadow-sm border-0"
      data-testid="empty-state"
  >
    <div class="empty-state-icon-wrapper mb-3">
        <i
            v-if="icon"
            :class="`bi ${icon} d-block`"
            aria-hidden="true"
        />
    </div>
    <h2
        v-if="title"
        class="h4 fw-bold mb-2 empty-state-title"
        data-testid="empty-state-title"
    >
      {{ title }}
    </h2>
    <p
        v-if="description"
        class="mb-4 empty-state-description mx-auto"
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
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  padding-left: 1.5rem;
  padding-right: 1.5rem;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.empty-state:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.05) !important;
}

.empty-state-icon-wrapper {
  font-size: 3.5rem;
  color: #64748b;
  opacity: 0.6;
}

.empty-state-title {
  color: #1e293b;
}

.empty-state-description {
  color: #64748b;
  font-size: 1rem;
  line-height: 1.6;
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

