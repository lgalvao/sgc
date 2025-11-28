<template>
  <BModal
    :model-value="mostrarModal"
    title="Sistema de Notificações"
    size="xl"
    centered
    hide-footer
    @hide="fecharModal"
  >
    <div class="mb-3">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h6>Notificações do Sistema</h6>
        <BButton
          variant="outline-danger"
          size="sm"
          data-testid="btn-limpar-notificacoes"
          @click="limparTodas"
        >
          <i class="bi bi-trash me-1" />
          Limpar Todas
        </BButton>
      </div>

      <div
        v-if="notificacoes.length === 0"
        class="text-center text-muted py-4"
      >
        <i class="bi bi-bell-slash display-4" />
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
          :data-testid="`notificacao-${notificacao.tipo}`"
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
              <BButton
                variant="outline-secondary"
                size="sm"
                title="Remover notificação"
                @click="removerNotificacao(notificacao.id)"
              >
                <i class="bi bi-x" />
              </BButton>
            </div>
          </div>
          <p class="mb-1">
            {{ notificacao.mensagem }}
          </p>
        </div>
      </div>
    </div>
    <template #footer>
      <BButton
        variant="secondary"
        data-testid="btn-modal-fechar"
        @click="fecharModal"
      >
        Fechar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BModal} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {computed, ref} from "vue";
import {type Notificacao, type TipoNotificacao, useNotificacoesStore,} from "@/stores/notificacoes";
import {formatDateTimeBR} from "@/utils";

interface Props {
  mostrarModal: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  fecharModal: [];
}>();

const notificacoesStore = useNotificacoesStore();
const {notificacoes} = storeToRefs(notificacoesStore);
const {removerNotificacao, limparTodas} = notificacoesStore;

const notificacoesOrdenadas = computed(() => {
  return [...notificacoes.value].sort(
      (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime(),
  );
});

const classeNotificacao = (notificacao: Notificacao): string => {
  switch (notificacao.tipo) {
    case "success":
      return "border-success";
    case "error":
      return "border-danger";
    case "warning":
      return "border-warning";
    case "info":
      return "border-info";
    default:
      return "";
  }
};

const iconeTipo = (tipo: TipoNotificacao): string => {
  switch (tipo) {
    case "success":
      return "bi bi-check-circle-fill text-success";
    case "error":
      return "bi bi-exclamation-triangle-fill text-danger";
    case "warning":
      return "bi bi-exclamation-triangle-fill text-warning";
    case "info":
      return "bi bi-info-circle-fill text-info";
    default:
      return "bi bi-bell-fill";
  }
};

const formatarDataHora = (date: Date): string => {
  return formatDateTimeBR(date);
};

const fecharModal = () => {
  emit("fecharModal");
};
</script>

<style scoped>
.list-group-item {
  border-left-width: 4px;
}
</style>
