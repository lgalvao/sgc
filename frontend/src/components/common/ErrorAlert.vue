<template>
  <BAlert
      v-if="error"
      :model-value="true"
      :variant="variant"
      dismissible
      @dismissed="$emit('dismiss')"
  >
    {{ error.message }}
    <div v-if="error.details">
      <small>Detalhes: {{ error.details }}</small>
    </div>
  </BAlert>
</template>

<script lang="ts" setup>
import {BAlert} from "bootstrap-vue-next";

interface ErrorObject {
  message: string;
  details?: string | Record<string, any>;
}

withDefaults(
  defineProps<{
    error: ErrorObject | null;
    variant?: 'danger' | 'warning' | 'info';
  }>(),
  {
    variant: 'danger'
  }
);

defineEmits<{
  dismiss: [];
}>();
</script>
