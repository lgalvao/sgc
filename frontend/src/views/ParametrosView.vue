<template>
  <LayoutPadrao>
    <PageHeader title="Parâmetros" />

    <div v-if="store.loading" class="text-center py-4">
      <BSpinner label="Carregando..." variant="primary" />
    </div>

    <BAlert v-else-if="store.error" :model-value="true" variant="danger">
      {{ store.error }}
    </BAlert>

    <template v-else>
      <AppAlert
          v-if="notificacao"
          :dismissible="notificacao.dismissible ?? true"
          :message="notificacao.message"
          :variant="notificacao.variant"
          @dismissed="clear"
      />

      <BForm @submit.prevent="salvar">
        <BFormGroup
            label-for="diasInativacao"
            class="mb-3"
        >
          <template #label>
            Dias para inativação de processos (DIAS_INATIVACAO_PROCESSO) <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <template #description>
            Dias depois da finalização de um processo para que seja considerado inativo.
          </template>
          <BFormInput
              id="diasInativacao"
              v-model="form.diasInativacao"
              min="1"
              required
              type="number"
          />
        </BFormGroup>

        <BFormGroup
            label-for="diasAlertaNovo"
            class="mb-3"
        >
          <template #label>
            Dias para indicação de alerta como novo (DIAS_ALERTA_NOVO) <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <template #description>
            Dias depois de um alerta ser enviado para que deixe de ser marcado como novo.
          </template>
          <BFormInput
              id="diasAlertaNovo"
              v-model="form.diasAlertaNovo"
              min="1"
              required
              type="number"
          />
        </BFormGroup>

        <div class="d-flex justify-content-end">
          <LoadingButton
              :loading="salvando"
              icon="check-lg"
              text="Salvar configurações"
              type="submit"
              variant="success"
          />
        </div>
      </BForm>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {onMounted, reactive, ref} from 'vue';
import {BAlert, BForm, BFormGroup, BFormInput, BSpinner} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import {type Parametro, useConfiguracoesStore} from '@/stores/configuracoes';
import {useNotification} from '@/composables/useNotification';

const store = useConfiguracoesStore();
const {notify, notificacao, clear} = useNotification();
const salvando = ref(false);

const form = reactive({
  diasInativacao: 30,
  diasAlertaNovo: 3
});

function atualizarFormulario() {
  form.diasInativacao = store.getDiasInativacaoProcesso();
  form.diasAlertaNovo = store.getDiasAlertaNovo();
}

async function carregar() {
  await store.carregarConfiguracoes();
  atualizarFormulario();
}

async function salvar() {
  salvando.value = true;

  const paramsToSave: Parametro[] = [];

  const findCodigo = (chave: string) => store.parametros.find(p => p.chave === chave)?.codigo;

  paramsToSave.push({
    codigo: findCodigo('DIAS_INATIVACAO_PROCESSO'),
    chave: 'DIAS_INATIVACAO_PROCESSO',
    descricao: 'Dias para inativação de processos',
    valor: form.diasInativacao.toString()
  }, {
    codigo: findCodigo('DIAS_ALERTA_NOVO'),
    chave: 'DIAS_ALERTA_NOVO',
    descricao: 'Dias para indicação de alerta como novo',
    valor: form.diasAlertaNovo.toString()
  });

  const sucesso = await store.salvarConfiguracoes(paramsToSave);

  if (sucesso) {
    notify('Configurações salvas.', 'success');
  } else {
    notify('Erro ao salvar configurações.', 'danger');
  }

  salvando.value = false;
}

onMounted(async () => {
  if (store.parametros.length === 0) {
    await carregar();
  } else {
    atualizarFormulario();
  }
});
</script>
