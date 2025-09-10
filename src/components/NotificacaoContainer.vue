<template>
  <div class="notification-container">
    <TransitionGroup
      name="notification"
      tag="div"
      class="notifications"
    >
      <div
        v-for="notificacao in notificacoes"
        :key="notificacao.id"
        :class="['notification', `notification-${notificacao.tipo}`]"
        :data-testid="`notificacao-${notificacao.id}`"
      >
        <div class="notification-content">
          <div class="notification-header">
            <i
              :class="iconeTipo(notificacao.tipo)"
              class="me-2"
            />
            <strong>{{ notificacao.titulo }}</strong>
            <button
              type="button"
              class="btn-close btn-close-white ms-auto"
              @click="removerNotificacao(notificacao.id)"
            />
          </div>
          <div class="notification-body">
            {{ notificacao.mensagem }}
            <div
              v-if="notificacao.tipo === 'email' && notificacao.emailContent"
              class="mt-2"
            >
              <button
                class="btn btn-sm btn-outline-primary"
                @click="mostrarEmail(notificacao)"
              >
                Ver e-mail completo
              </button>
            </div>
          </div>
        </div>
      </div>
    </TransitionGroup>

    <!-- Modal para visualizar e-mail completo -->
    <div
      v-if="emailModalVisivel"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-envelope me-2" />
              E-mail Simulado
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharEmailModal"
            />
          </div>
          <div class="modal-body">
            <div v-if="emailAtual">
              <div class="mb-3">
                <strong>Assunto:</strong> {{ emailAtual.assunto }}
              </div>
              <div class="mb-3">
                <strong>Destinatário:</strong> {{ emailAtual.destinatario }}
              </div>
              <div class="mb-3">
                <strong>Corpo:</strong>
                <div class="mt-2 p-3 bg-light rounded">
                  <pre style="white-space: pre-wrap; font-family: inherit;">{{ emailAtual.corpo }}</pre>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharEmailModal"
            >
              Fechar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="emailModalVisivel"
      class="modal-backdrop fade show"
    />
  </div>
</template>

<script lang="ts" setup>
import {type EmailContent, type Notificacao, useNotificacoesStore} from '@/stores/notificacoes';
import {iconeTipo} from '@/utils/notificationIcons'; // Importar a função iconeTipo
import {storeToRefs} from 'pinia';
import {onMounted, onUnmounted, ref, watch} from 'vue';

const notificacoesStore = useNotificacoesStore();
const {notificacoes} = storeToRefs(notificacoesStore);

const {removerNotificacao} = notificacoesStore;

// Estado para modal de email
const emailModalVisivel = ref(false);
const emailAtual = ref<EmailContent | null>(null);

// Auto-hide para notificações de sucesso após 3 segundos
let autoHideTimeouts: Map<string, number> = new Map();

const scheduleAutoHide = (notificacao: Notificacao) => {
  // Só auto-hide para notificações de sucesso
  if (notificacao.tipo === 'success') {
    const timeoutId = window.setTimeout(() => {
      removerNotificacao(notificacao.id);
      autoHideTimeouts.delete(notificacao.id);
    }, 3000); // 3 segundos

    autoHideTimeouts.set(notificacao.id, timeoutId);
  }
};

const cancelAutoHide = (notificacaoId: string) => {
  const timeoutId = autoHideTimeouts.get(notificacaoId);
  if (timeoutId) {
    clearTimeout(timeoutId);
    autoHideTimeouts.delete(notificacaoId);
  }
};

// Configurar auto-hide para notificações existentes
onMounted(() => {
  notificacoes.value.forEach(scheduleAutoHide);
});

// Limpar timeouts quando o componente for desmontado
onUnmounted(() => {
  autoHideTimeouts.forEach((timeoutId) => {
    clearTimeout(timeoutId);
  });
  autoHideTimeouts.clear();
});

watch(notificacoes, (novasNotificacoes, notificacoesAntigas) => {
  // Verificar notificações adicionadas
  novasNotificacoes.forEach(notificacao => {
    const existsInOld = notificacoesAntigas.some(old => old.id === notificacao.id);
    if (!existsInOld) {
      scheduleAutoHide(notificacao);
    }
  });

  // Verificar notificações removidas
  notificacoesAntigas.forEach(notificacao => {
    const existsInNew = novasNotificacoes.some(newNot => newNot.id === notificacao.id);
    if (!existsInNew) {
      cancelAutoHide(notificacao.id);
    }
  });
});

const mostrarEmail = (notificacao: Notificacao) => {
  if (notificacao.emailContent) {
    emailAtual.value = notificacao.emailContent;
    emailModalVisivel.value = true;
  }
};

const fecharEmailModal = () => {
  emailModalVisivel.value = false;
  emailAtual.value = null;
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