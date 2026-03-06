<template>
  <BAlert
      v-if="message || notification"
      :dismissible="dismissible"
      :fade="false"
      :model-value="true"
      :variant="variant"
      class="mb-3"
      data-testid="app-alert"
      @dismissed="emit('dismissed')"
  >
    <!-- Modo simples -->
    <template v-if="message">
      <p class="mb-0">{{ message }}</p>
    </template>

    <!-- Modo estruturado -->
    <template v-else-if="notification">
      <p class="mb-0">{{ notification.summary }}</p>
      <div v-if="notification.details && notification.details.length > 0" class="mt-2">
        <BButton
            class="text-muted p-0 border-0 d-block mb-1 text-decoration-none"
            size="sm"
            variant="link"
            @click="mostrarDetalhes = !mostrarDetalhes"
        >
          <small>{{ mostrarDetalhes ? 'Ocultar detalhes' : 'Mostrar detalhes' }}</small>
        </BButton>
        <ul v-if="mostrarDetalhes" class="mt-1 mb-0">
          <li v-for="(detalhe, index) in notification.details" :key="index">{{ detalhe }}</li>
        </ul>
      </div>
    </template>

    <!-- Stack trace (apenas em modo desenvolvimento) -->
    <div v-if="stackTrace && isDev" class="mt-3">
      <BButton
          class="text-muted p-0 border-0 d-block mb-1 text-decoration-none"
          size="sm"
          variant="link"
          @click="mostrarStackTrace = !mostrarStackTrace"
      >
        <small>{{ mostrarStackTrace ? 'Ocultar detalhes técnicos' : 'Mostrar detalhes técnicos' }}</small>
      </BButton>
      <pre
          v-if="mostrarStackTrace"
          class="bg-dark text-light p-2 rounded small overflow-auto"
          style="max-height: 200px; font-size: 0.75rem;"
      >{{ stackTrace }}</pre>
    </div>
  </BAlert>
</template>

<script lang="ts" setup>
import {ref} from 'vue';
import {BAlert, BButton} from 'bootstrap-vue-next';

interface NotificacaoEstruturada {
  summary: string;
  details: string[];
}

withDefaults(defineProps<{
  message?: string;
  notification?: NotificacaoEstruturada;
  variant?: 'danger' | 'warning' | 'success' | 'info';
  dismissible?: boolean;
  stackTrace?: string;
}>(), {
  message: undefined,
  notification: undefined,
  variant: 'danger',
  dismissible: true,
  stackTrace: undefined,
});

const emit = defineEmits<{
  dismissed: [];
}>();

const mostrarDetalhes = ref(false);
const mostrarStackTrace = ref(false);
const isDev = import.meta.env.DEV;
</script>
