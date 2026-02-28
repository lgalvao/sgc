<template>
  <BButton
      :disabled="loading || disabled"
      v-bind="$attrs"
  >
    <BSpinner v-if="loading" class="me-1" small/>
    <i v-else-if="icon" :class="`bi bi-${icon} me-1`" aria-hidden="true"></i>
    <slot>{{ loadingText && loading ? loadingText : text }}</slot>
  </BButton>
</template>

<script lang="ts" setup>
import {BButton, BSpinner} from 'bootstrap-vue-next';

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
  icon: undefined,
  text: undefined,
  loadingText: undefined
})
</script>
