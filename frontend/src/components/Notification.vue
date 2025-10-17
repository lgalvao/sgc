<template>
  <div v-if="notification" :class="['notification', notification.tipo]" role="alert">
    <span class="notification-title">{{ notification.titulo }}</span>
    <span class="notification-message">{{ notification.mensagem }}</span>
    <button @click="dismiss" class="btn-close" aria-label="Close"></button>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useNotificacoesStore } from '@/stores/notificacoes';

const notificacoesStore = useNotificacoesStore();
const notification = computed(() => notificacoesStore.notificacoes[0]);

const dismiss = () => {
  if (notification.value) {
    notificacoesStore.removerNotificacao(notification.value.id);
  }
};
</script>

<style scoped>
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 1rem 1.5rem;
  border-radius: 0.25rem;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 300px;
  z-index: 1050;
}
.notification.success { background-color: var(--bs-success); }
.notification.error { background-color: var(--bs-danger); }
.notification.warning { background-color: var(--bs-warning); }
.notification.info { background-color: var(--bs-info); }

.notification-title {
  font-weight: bold;
  margin-right: 1rem;
}

.btn-close {
  filter: invert(1) grayscale(100%) brightness(200%);
}
</style>