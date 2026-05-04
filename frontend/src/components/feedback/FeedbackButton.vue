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
      return 'bi-hourglass-split'
    case 'sucesso':
      return 'bi-check-lg'
    case 'erro':
      return 'bi-exclamation-triangle'
    default:
      return 'bi-chat-square-text'
  }
})

const classeVariante = computed(() => {
  switch (props.estado) {
    case 'sucesso':
      return 'btn-success'
    case 'erro':
      return 'btn-danger'
    default:
      return 'btn-primary'
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
}
</style>
