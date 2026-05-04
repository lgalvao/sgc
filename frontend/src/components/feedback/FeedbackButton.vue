<script lang="ts" setup>
import {computed} from 'vue'

type EstadoBotao = 'normal' | 'carregando' | 'sucesso' | 'erro'

const props = withDefaults(defineProps<{
  estado?: EstadoBotao
}>(), {
  estado: 'normal',
})

const emit = defineEmits<{
  click: []
}>()

const desabilitado = computed(() => props.estado === 'carregando')

const icone = computed(() => {
  switch (props.estado) {
    case 'carregando':
      return 'bi-arrow-repeat'
    case 'sucesso':
      return 'bi-check2'
    case 'erro':
      return 'bi-exclamation-circle'
    default:
      return 'bi-chat-left-text'
  }
})

const classeVariante = computed(() => {
  switch (props.estado) {
    case 'sucesso':
      return 'btn-outline-secondary'
    case 'erro':
      return 'btn-outline-secondary'
    default:
      return 'btn-outline-secondary'
  }
})
</script>

<template>
  <button
      v-b-tooltip.hover.left="{ title: 'Enviar feedback' }"
      :class="['btn', classeVariante, 'feedback-btn rounded-circle shadow']"
      :disabled="desabilitado"
      aria-label="Enviar feedback"
      data-testid="feedback-btn"
      type="button"
      @click="emit('click')"
  >
    <i :class="['bi', icone]" aria-hidden="true"/>
  </button>
</template>

<style scoped>
.feedback-btn {
  position: fixed;
  bottom: 1.5rem;
  right: 1.5rem;
  z-index: 9999;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  background: rgba(var(--bs-body-bg-rgb), 0.92);
  border-width: 1px;
  backdrop-filter: blur(6px);
}

.feedback-btn :deep(.bi) {
  line-height: 1;
}

.feedback-btn:disabled {
  opacity: 0.8;
}

.bi-arrow-repeat {
  animation: feedback-spin 0.9s linear infinite;
}

@keyframes feedback-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
