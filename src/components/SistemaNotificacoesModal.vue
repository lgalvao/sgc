<template>
  <div
      v-if="mostrarModal"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
  >
    <div class="modal-dialog modal-xl">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-bell me-2"/>
            Sistema de Notificações
          </h5>
          <button
              type="button"
              class="btn-close"
              @click="fecharModal"
          />
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h6>Notificações do Sistema</h6>
              <button
                  class="btn btn-sm btn-outline-danger"
                  @click="limparTodas"
              >
                <i class="bi bi-trash me-1"/>
                Limpar Todas
              </button>
            </div>

            <div
                v-if="notificacoes.length === 0"
                class="text-center text-muted py-4"
            >
              <i class="bi bi-bell-slash display-4"/>
              <p class="mt-2">
                Nenhuma notificação no momento.
              </p>
            </div>

            <div
                v-else
                class="list-group"
            >
              <div
                  v-for="notificacao in notificacoesOrdenadas"
                  :key="notificacao.id"
                  :class="['list-group-item list-group-item-action', classeNotificacao(notificacao)]"
              >
                <div class="d-flex w-100 justify-content-between">
                  <div class="d-flex align-items-center">
                    <i
                        :class="iconeTipo(notificacao.tipo)"
                        class="me-2"
                    />
                    <h6 class="mb-1">
                      {{ notificacao.titulo }}
                    </h6>
                  </div>
                  <div class="d-flex align-items-center">
                    <small class="text-muted me-2">{{ formatarDataHora(notificacao.timestamp) }}</small>
                    <button
                        class="btn btn-sm btn-outline-secondary"
                        title="Remover notificação"
                        @click="removerNotificacao(notificacao.id)"
                    >
                      <i class="bi bi-x"/>
                    </button>
                  </div>
                </div>
                <p class="mb-1">
                  {{ notificacao.mensagem }}
                </p>
                <div
                    v-if="notificacao.tipo === 'email' && notificacao.emailContent"
                    class="mt-2"
                >
                  <button
                      class="btn btn-sm btn-outline-primary"
                      @click="mostrarEmail(notificacao)"
                  >
                    <i class="bi bi-envelope me-1"/>
                    Ver e-mail completo
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModal"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  </div>

  <div
      v-if="mostrarModal"
      class="modal-backdrop fade show"
  />

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
            <i class="bi bi-envelope me-2"/>
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
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {storeToRefs} from 'pinia'
import {type EmailContent, type Notificacao, type TipoNotificacao, useNotificacoesStore} from '@/stores/notificacoes'
import {formatDateTimeBR} from '@/utils/dateUtils'

interface Props {
  mostrarModal: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  fecharModal: []
}>()

const notificacoesStore = useNotificacoesStore()
const {notificacoes} = storeToRefs(notificacoesStore)
const {removerNotificacao, limparTodas} = notificacoesStore

// Estado para modal de email
const emailModalVisivel = ref(false)
const emailAtual = ref<EmailContent | null>(null)

const notificacoesOrdenadas = computed(() => {
  return [...notificacoes.value].sort((a, b) =>
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
  )
})

const classeNotificacao = (notificacao: Notificacao): string => {
  switch (notificacao.tipo) {
    case 'success':
      return 'border-success'
    case 'error':
      return 'border-danger'
    case 'warning':
      return 'border-warning'
    case 'info':
      return 'border-info'
    case 'email':
      return 'border-primary'
    default:
      return ''
  }
}

const iconeTipo = (tipo: TipoNotificacao): string => {
  switch (tipo) {
    case 'success':
      return 'bi bi-check-circle-fill text-success'
    case 'error':
      return 'bi bi-exclamation-triangle-fill text-danger'
    case 'warning':
      return 'bi bi-exclamation-triangle-fill text-warning'
    case 'info':
      return 'bi bi-info-circle-fill text-info'
    case 'email':
      return 'bi bi-envelope-fill text-primary'
    default:
      return 'bi bi-bell-fill'
  }
}

const formatarDataHora = (date: Date): string => {
  return formatDateTimeBR(date)
}

const mostrarEmail = (notificacao: Notificacao) => {
  if (notificacao.emailContent) {
    emailAtual.value = notificacao.emailContent
    emailModalVisivel.value = true
  }
}

const fecharEmailModal = () => {
  emailModalVisivel.value = false
  emailAtual.value = null
}

const fecharModal = () => {
  emit('fecharModal')
}
</script>

<style scoped>
.list-group-item {
  border-left-width: 4px;
}
</style>