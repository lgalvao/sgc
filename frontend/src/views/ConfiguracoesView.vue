<template>
  <BContainer class="mt-4">
    <h2>Configurações do Sistema</h2>
    <BForm @submit.prevent="salvarConfiguracoes">
      <div class="mb-3">
        <label
          class="form-label"
          for="diasInativacaoProcesso"
        >Dias para inativação de processos:</label>
        <BFormInput
          id="diasInativacaoProcesso"
          v-model.number="configuracoesStore.diasInativacaoProcesso"
          min="1"
          required
          type="number"
        />
        <div class="form-text">
          Dias depois da finalização de um processo para que seja considerado inativo.
        </div>
      </div>

      <div class="mb-3">
        <label
          class="form-label"
          for="diasAlertaNovo"
        >Dias para indicação de alerta como novo:</label>
        <BFormInput
          id="diasAlertaNovo"
          v-model.number="configuracoesStore.diasAlertaNovo"
          min="1"
          required
          type="number"
        />
        <div class="form-text">
          Dias depois de um alerta ser enviado para uma unidade, para que deixe de ser marcado como novo.
        </div>
      </div>

      <BButton
        variant="primary"
        type="submit"
      >
        Salvar
      </BButton>
    </BForm>
    <BAlert
      v-if="mensagemSucesso"
      variant="success"
      class="mt-3"
      :model-value="true"
    >
      {{ mensagemSucesso }}
    </BAlert>
  </BContainer>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BContainer, BForm, BFormInput,} from "bootstrap-vue-next";
import {onMounted, ref} from "vue";
import {useConfiguracoesStore} from "@/stores/configuracoes";

const configuracoesStore = useConfiguracoesStore();
const mensagemSucesso = ref("");

onMounted(() => {
  configuracoesStore.carregarConfiguracoes();
});

const salvarConfiguracoes = () => {
  if (configuracoesStore.salvarConfiguracoes()) {
    mensagemSucesso.value = "Configurações salvas!";
    setTimeout(() => {
      mensagemSucesso.value = "";
    }, 3000);
  }
};
</script>