<template>
  <div class="container-fluid mt-4">
    <div class="card">
      <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Configurações do Sistema</h5>
        <button class="btn btn-light btn-sm" @click="recarregar">
          <i class="bi bi-arrow-clockwise"></i> Recarregar
        </button>
      </div>
      <div class="card-body">
        <div v-if="store.loading" class="text-center py-4">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Carregando...</span>
          </div>
        </div>

        <div v-else-if="store.error" class="alert alert-danger">
          {{ store.error }}
        </div>

        <form v-else @submit.prevent="salvar">
          <div class="mb-3">
            <label for="diasInativacao" class="form-label">
              Dias para inativação de processos (DIAS_INATIVACAO_PROCESSO)
            </label>
            <input
              type="number"
              class="form-control"
              id="diasInativacao"
              v-model="form.diasInativacao"
              min="1"
              required
            />
            <div class="form-text">
              Dias depois da finalização de um processo para que seja considerado inativo.
            </div>
          </div>

          <div class="mb-3">
            <label for="diasAlertaNovo" class="form-label">
              Dias para indicação de alerta como novo (DIAS_ALERTA_NOVO)
            </label>
            <input
              type="number"
              class="form-control"
              id="diasAlertaNovo"
              v-model="form.diasAlertaNovo"
              min="1"
              required
            />
            <div class="form-text">
              Dias depois de um alerta ser enviado para que deixe de ser marcado como novo.
            </div>
          </div>

          <div class="d-flex justify-content-end">
            <button type="submit" class="btn btn-success" :disabled="salvando">
              <span v-if="salvando" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              Salvar Configurações
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { useConfiguracoesStore, type Parametro } from '@/stores/configuracoes';
import { useNotificacoesStore } from '@/stores/feedback';

const store = useConfiguracoesStore();
const notificacoes = useNotificacoesStore();
const salvando = ref(false);

const form = reactive({
  diasInativacao: 30,
  diasAlertaNovo: 3
});

function atualizarFormulario() {
  form.diasInativacao = store.getDiasInativacaoProcesso();
  form.diasAlertaNovo = store.getDiasAlertaNovo();
}

async function recarregar() {
  await store.carregarConfiguracoes();
  atualizarFormulario();
}

async function salvar() {
  salvando.value = true;

  const paramsToSave: Parametro[] = [];

  const findId = (chave: string) => store.parametros.find(p => p.chave === chave)?.id;

  paramsToSave.push({
    id: findId('DIAS_INATIVACAO_PROCESSO'),
    chave: 'DIAS_INATIVACAO_PROCESSO',
    descricao: 'Dias para inativação de processos',
    valor: form.diasInativacao.toString()
  });

  paramsToSave.push({
    id: findId('DIAS_ALERTA_NOVO'),
    chave: 'DIAS_ALERTA_NOVO',
    descricao: 'Dias para indicação de alerta como novo',
    valor: form.diasAlertaNovo.toString()
  });

  const sucesso = await store.salvarConfiguracoes(paramsToSave);

  if (sucesso) {
    notificacoes.show('Sucesso', 'Configurações salvas com sucesso!', 'success');
  } else {
    notificacoes.show('Erro', 'Erro ao salvar configurações.', 'danger');
  }

  salvando.value = false;
}

onMounted(async () => {
  if (store.parametros.length === 0) {
    await store.carregarConfiguracoes();
  }
  atualizarFormulario();
});
</script>
