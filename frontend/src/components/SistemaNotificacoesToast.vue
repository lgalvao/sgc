<template>
  <div class="position-fixed top-0 end-0 p-3" style="z-index: 1050">
    <BToast
      v-for="notificacao in storeNotificacoes"
      :key="notificacao.id"
      v-model="notificacao.mostrar"
      :variant="getVariant(notificacao.tipo)"
      :title="notificacao.titulo"
      :data-testid="notificacao.testId"
      solid
      auto-hide-delay="5000"
      @hide="onToastHide(notificacao.id)"
    >
      {{ notificacao.mensagem }}
    </BToast>
  </div>
</template>

<script lang="ts" setup>
import {type BaseColorVariant, BToast} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {TipoNotificacao, useNotificacoesStore,} from "@/stores/notificacoes";

const notificacoesStore = useNotificacoesStore();
const { notificacoes: storeNotificacoes } = storeToRefs(notificacoesStore);
const { removerNotificacao } = notificacoesStore;

const getVariant = (tipo: TipoNotificacao): keyof BaseColorVariant => {
  switch (tipo) {
    case "success":
      return "success";
    case "error":
      return "danger";
    case "warning":
      return "warning";
    case "info":
      return "info";
    default:
      return "info";
  }
};

const onToastHide = (id: string) => {
  const notificacao = storeNotificacoes.value.find(n => n.id === id);
  if (notificacao) {
    notificacao.mostrar = false;
  }
  removerNotificacao(id);
};
</script>

<style scoped>
/* Estilos adicionais, se necessário */
</style>
