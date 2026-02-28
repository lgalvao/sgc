<template>
  <BAlert
      :fade="false"
      :model-value="Boolean(show)"
      :variant="variant"
      class="mb-3"
      dismissible
      @update:model-value="(val) => val === false && $emit('update:show', false)"
  >
    <h4 v-if="title" class="alert-heading">{{ title }}</h4>
    <p v-if="body" class="mb-0">{{ body }}</p>
    <ul v-if="errors && errors.length > 0" class="mt-2 mb-0">
      <li v-for="(error, index) in errors" :key="index">{{ error }}</li>
    </ul>
    <div v-if="stackTrace" class="mt-3">
      <BButton
          class="text-muted p-0 border-0 d-block mb-1 text-decoration-none"
          size="sm"
          variant="link"
          @click="showStack = !showStack"
      >
        <small>{{ showStack ? 'Ocultar detalhes técnicos' : 'Mostrar detalhes técnicos' }}</small>
      </BButton>
      <pre
v-if="showStack" class="bg-dark text-light p-2 rounded small overflow-auto"
           style="max-height: 200px; font-size: 0.75rem;">{{ stackTrace }}</pre>
    </div>
  </BAlert>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import {BAlert, BButton} from "bootstrap-vue-next";

type Variant = 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark';

defineProps<{
  show: boolean;
  variant: Variant;
  title?: string;
  body?: string;
  errors?: string[];
  stackTrace?: string;
}>();

const showStack = ref(false);

defineEmits<{
  'update:show': [value: boolean];
}>();
</script>
