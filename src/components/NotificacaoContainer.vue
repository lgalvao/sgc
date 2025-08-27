<template>
  <div class="notification-container">
    <TransitionGroup name="notification" tag="div" class="notifications">
      <div
          v-for="notificacao in notificacoes"
          :key="notificacao.id"
          :class="['notification', `notification-${notificacao.tipo}`]"
      >
        <div class="notification-content">
          <div class="notification-header">
            <i :class="iconeTipo(notificacao.tipo)" class="me-2"></i>
            <strong>{{ notificacao.titulo }}</strong>
            <button
                type="button"
                class="btn-close btn-close-white ms-auto"
                @click="removerNotificacao(notificacao.id)"
            ></button>
          </div>
          <div class="notification-body">
            {{ notificacao.mensagem }}
          </div>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script lang="ts" setup>
import {storeToRefs} from 'pinia';
import {type TipoNotificacao, useNotificacoesStore} from '@/stores/notificacoes';

const notificacoesStore = useNotificacoesStore();
const {notificacoes} = storeToRefs(notificacoesStore);

const {removerNotificacao} = notificacoesStore;

const iconeTipo = (tipo: TipoNotificacao): string => {
  switch (tipo) {
    case 'success':
      return 'bi bi-check-circle-fill text-success';
    case 'error':
      return 'bi bi-exclamation-triangle-fill text-danger';
    case 'warning':
      return 'bi bi-exclamation-triangle-fill text-warning';
    case 'info':
      return 'bi bi-info-circle-fill text-info';
    default:
      return 'bi bi-bell-fill';
  }
};
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  max-width: 400px;
  width: 100%;
}

.notifications {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notification {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-left: 4px solid;
  overflow: hidden;
  animation: slideIn 0.3s ease-out;
}

.notification-success {
  border-left-color: #198754;
}

.notification-error {
  border-left-color: #dc3545;
}

.notification-warning {
  border-left-color: #ffc107;
}

.notification-info {
  border-left-color: #0dcaf0;
}

.notification-content {
  padding: 15px;
}

.notification-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.notification-header strong {
  font-size: 14px;
}

.notification-body {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

/* Animações */
.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.notification-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.notification-move {
  transition: transform 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Responsividade */
@media (max-width: 480px) {
  .notification-container {
    left: 10px;
    right: 10px;
    max-width: none;
  }
}
</style>