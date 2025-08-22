<template>
  <div class="container mt-4">
    <h2>Configurações do Sistema</h2>
    <form @submit.prevent="salvarConfiguracoes">
      <div class="mb-3">
        <label for="diasInativacaoProcesso" class="form-label">Dias para inativação de processos:</label>
        <input
          type="number"
          class="form-control"
          id="diasInativacaoProcesso"
          v-model.number="configuracoesStore.diasInativacaoProcesso"
          min="1"
          required
        />
        <div class="form-text">Dias depois da finalização de um processo para que seja considerado inativo.</div>
      </div>

      <div class="mb-3">
        <label for="diasAlertaNovo" class="form-label">Dias para indicação de alerta como novo:</label>
        <input
          type="number"
          class="form-control"
          id="diasAlertaNovo"
          v-model.number="configuracoesStore.diasAlertaNovo"
          min="1"
          required
        />
        <div class="form-text">Dias depois de um alerta ser enviado para uma unidade, para que deixe de ser marcado como novo.</div>
      </div>

      <button type="submit" class="btn btn-primary">Salvar</button>
    </form>
    <div v-if="mensagemSucesso" class="alert alert-success mt-3" role="alert">
      {{ mensagemSucesso }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {useConfiguracoesStore} from '@/stores/configuracoes';

const configuracoesStore = useConfiguracoesStore();
const mensagemSucesso = ref('');

const salvarConfiguracoes = () => {
  if (configuracoesStore.saveConfiguracoes()) {
    mensagemSucesso.value = 'Configurações salvas com sucesso!';
    setTimeout(() => {
      mensagemSucesso.value = '';
    }, 3000);
  }
};

onMounted(() => {
  configuracoesStore.loadConfiguracoes();
});
</script>

<style scoped>
/* Estilos para o componente de configurações */
</style>