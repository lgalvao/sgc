<template>
  <BAlert
      v-if="mensagem || notificacao"
      :key="chave"
      :dismissible="dispensavel"
      :fade="false"
      :model-value="true"
      :variant="variante"
      class="mb-3"
      :data-testid="dataTestid"
      @dismissed="emit('dismissed')"
  >
    <!-- Modo simples -->
    <template v-if="mensagem">
      <p class="mb-0">{{ mensagem }}</p>
    </template>

    <!-- Modo estruturado -->
    <template v-else-if="notificacao">
      <p class="mb-0">{{ notificacao.resumo }}</p>
      <div v-if="notificacao.detalhes && notificacao.detalhes.length > 0" class="mt-2">
        <BButton
            class="text-muted p-0 border-0 d-block mb-1 text-decoration-none"
            size="sm"
            variant="link"
            @click="mostrarDetalhes = !mostrarDetalhes"
        >
          <small>{{ mostrarDetalhes ? 'Ocultar detalhes' : 'Mostrar detalhes' }}</small>
        </BButton>
        <ul v-if="mostrarDetalhes" class="mt-1 mb-0">
          <li v-for="(detalhe, index) in notificacao.detalhes" :key="index">{{ detalhe }}</li>
        </ul>
      </div>
    </template>

    <!-- Stack trace (apenas em modo desenvolvimento) -->
    <div v-if="stackTrace && ehDev" class="mt-3">
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
  resumo: string;
  detalhes: string[];
}

withDefaults(defineProps<{
  mensagem?: string | null;
  notificacao?: NotificacaoEstruturada;
  chave?: number;
  variante?: 'danger' | 'warning' | 'success' | 'info';
  dispensavel?: boolean;
  stackTrace?: string;
  dataTestid?: string;
}>(), {
  chave: undefined,
  mensagem: undefined,
  notificacao: undefined,
  variante: 'danger',
  dispensavel: true,
  stackTrace: undefined,
  dataTestid: 'app-alert',
});

const emit = defineEmits<{
  dismissed: [];
}>();

const mostrarDetalhes = ref(false);
const mostrarStackTrace = ref(false);
const ehDev = import.meta.env.DEV;
</script>
