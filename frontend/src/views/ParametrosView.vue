<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.configuracoes.TITULO" />

    <div v-if="store.loading" class="text-center py-4">
      <BSpinner :label="TEXTOS.comum.CARREGANDO" variant="primary" />
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
            {{ TEXTOS.configuracoes.LABEL_DIAS_INATIVACAO }} <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <template #description>
            {{ TEXTOS.configuracoes.DESC_DIAS_INATIVACAO }}
          </template>
          <BFormInput
              id="diasInativacao"
              v-model="form.diasInativacao"
              aria-required="true"
              min="1"
              :state="mensagemErroDiasInativacao ? false : null"
              type="number"
          />
          <BFormInvalidFeedback :state="mensagemErroDiasInativacao ? false : null">
            {{ mensagemErroDiasInativacao }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <BFormGroup
            label-for="diasAlertaNovo"
            class="mb-3"
        >
          <template #label>
            {{ TEXTOS.configuracoes.LABEL_DIAS_ALERTA_NOVO }} <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <template #description>
            {{ TEXTOS.configuracoes.DESC_DIAS_ALERTA_NOVO }}
          </template>
          <BFormInput
              id="diasAlertaNovo"
              v-model="form.diasAlertaNovo"
              aria-required="true"
              min="1"
              :state="mensagemErroDiasAlertaNovo ? false : null"
              type="number"
          />
          <BFormInvalidFeedback :state="mensagemErroDiasAlertaNovo ? false : null">
            {{ mensagemErroDiasAlertaNovo }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <div class="d-flex justify-content-end">
          <LoadingButton
              :loading="salvando"
              icon="check-lg"
              :text="TEXTOS.configuracoes.BOTAO_SALVAR"
              type="submit"
              variant="success"
          />
        </div>
      </BForm>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {BAlert, BForm, BFormGroup, BFormInput, BFormInvalidFeedback, BSpinner} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import {type Parametro, useConfiguracoesStore} from '@/stores/configuracoes';
import {useNotification} from '@/composables/useNotification';
import {TEXTOS} from '@/constants/textos';

const store = useConfiguracoesStore();
const {notify, notificacao, clear} = useNotification();
const salvando = ref(false);
const validacaoSubmetida = ref(false);

const form = reactive({
  diasInativacao: 30,
  diasAlertaNovo: 3
});

const mensagemErroDiasInativacao = computed(() => {
  if (!validacaoSubmetida.value) return "";
  return Number(form.diasInativacao) >= 1 ? "" : "Informe um valor maior ou igual a 1.";
});

const mensagemErroDiasAlertaNovo = computed(() => {
  if (!validacaoSubmetida.value) return "";
  return Number(form.diasAlertaNovo) >= 1 ? "" : "Informe um valor maior ou igual a 1.";
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
  validacaoSubmetida.value = true;
  if (mensagemErroDiasInativacao.value || mensagemErroDiasAlertaNovo.value) {
    return;
  }

  salvando.value = true;

  const paramsToSave: Parametro[] = [];

  const findCodigo = (chave: string) => store.configuracoes.find(p => p.chave === chave)?.codigo;

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
    validacaoSubmetida.value = false;
    notify(TEXTOS.configuracoes.SUCESSO_SALVAR, 'success');
  } else {
    notify(TEXTOS.configuracoes.ERRO_SALVAR, 'danger');
  }

  salvando.value = false;
}

onMounted(async () => {
  if (store.configuracoes.length === 0) {
    await carregar();
  } else {
    atualizarFormulario();
  }
});
</script>
