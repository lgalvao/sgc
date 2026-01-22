<template>
  <BButton
    v-bind="$attrs"
    :disabled="loading || disabled"
  >
    <BSpinner v-if="loading" small class="me-1" />
    <i v-else-if="icon" :class="`bi bi-${icon} me-1`" aria-hidden="true"></i>
    <slot>{{ loadingText && loading ? loadingText : text }}</slot>
  </BButton>
</template>

<script setup lang="ts">
import { BButton, BSpinner } from 'bootstrap-vue-next';

interface Props {
  loading?: boolean
  disabled?: boolean
  icon?: string
  text?: string
  loadingText?: string
}

withDefaults(defineProps<Props>(), {
  loading: false,
  disabled: false,
})
</script>
