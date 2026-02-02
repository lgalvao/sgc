<template>
  <BAlert
      :model-value="Boolean(show)"
      :variant="variant"
      :fade="false"
      class="mb-3"
      dismissible
      @update:model-value="(val: boolean) => $emit('update:show', val)"
  >
    <h4 v-if="title" class="alert-heading">{{ title }}</h4>
    <p class="mb-0">{{ body }}</p>
    <ul v-if="errors && errors.length > 0" class="mb-0 mt-2">
      <li v-for="(error, index) in errors" :key="index">{{ error }}</li>
    </ul>
  </BAlert>
</template>

<script lang="ts" setup>
import { BAlert } from "bootstrap-vue-next";

type Variant = 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark';

defineProps<{
  show: boolean;
  variant: Variant;
  title?: string;
  body: string;
  errors?: string[];
}>();

defineEmits<{
  'update:show': [value: boolean];
}>();
</script>
