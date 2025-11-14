<template>
  <div class="container mt-4">
    <h2>Configurações do Sistema</h2>
    <form @submit.prevent="salvarConfiguracoes">
      <div class="mb-3">
        <label
          class="form-label"
          for="diasInativacaoProcesso"
        >Dias para inativação de processos:</label>
        <input
          id="diasInativacaoProcesso"
          v-model.number="configuracoesStore.diasInativacaoProcesso"
          class="form-control"
          min="1"
          required
          type="number"
        >
        <div class="form-text">
          Dias depois da finalização de um processo para que seja considerado inativo.
        </div>
      </div>

      <div class="mb-3">
        <label
          class="form-label"
          for="diasAlertaNovo"
        >Dias para indicação de alerta como novo:</label>
        <input
          id="diasAlertaNovo"
          v-model.number="configuracoesStore.diasAlertaNovo"
          class="form-control"
          min="1"
          required
          type="number"
        >
        <div class="form-text">
          Dias depois de um alerta ser enviado para uma unidade, para que deixe de ser marcado como novo.
        </div>
      </div>

      <button
        class="btn btn-primary"
        type="submit"
      >
        Salvar
      </button>
    </form>
    <div
      v-if="mensagemSucesso"
      class="alert alert-success mt-3"
      role="alert"
    >
      {{ mensagemSucesso }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {useConfiguracoesStore} from '@/stores/configuracoes';

const configuracoesStore = useConfiguracoesStore();
const mensagemSucesso = ref('');

onMounted(() => {
  configuracoesStore.loadConfiguracoes();
});

const salvarConfiguracoes = () => {
  if (configuracoesStore.saveConfiguracoes()) {
    mensagemSucesso.value = 'Configurações salvas!';
    setTimeout(() => {
      mensagemSucesso.value = '';
    }, 3000);
  }
};
</script>