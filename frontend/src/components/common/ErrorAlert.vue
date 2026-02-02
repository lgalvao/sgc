<template>
  <BAlert
      v-if="error"
      :model-value="true"
      :variant="variant"
      dismissible
      @dismissed="handleDismiss"
  >
    {{ error.message }}
    <div v-if="error.details">
      <small>Detalhes: {{ error.details }}</small>
    </div>
  </BAlert>
</template>

<script lang="ts" setup>
import { BAlert } from "bootstrap-vue-next";

interface ErrorObject {
  message: string;
  details?: string;
}

const props = withDefaults(
  defineProps<{
    error: ErrorObject | null;
    variant?: 'danger' | 'warning' | 'info';
  }>(),
  {
    variant: 'danger'
  }
);

const emit = defineEmits<{
  dismiss: [];
}>();

function handleDismiss() {
  emit('dismiss');
}
</script>
