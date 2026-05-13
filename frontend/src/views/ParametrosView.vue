<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.configuracoes.TITULO"/>

    <CarregamentoPagina v-if="loading"/>

    <BAlert v-else-if="error" :model-value="true" dismissible variant="danger">
      {{ error }}
    </BAlert>

    <template v-else>
      <AppAlert
          v-if="notificacao"
          :dispensavel="notificacao.dispensavel ?? true"
          :mensagem="notificacao.mensagem"
          :variante="notificacao.variante"
          @dismissed="clear"
      />

      <BForm @submit.prevent="salvar">
        <BFormGroup
            class="mb-3"
            label-for="diasInativacao"
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
              :state="mensagemErroDiasInativacao ? false : null"
              aria-required="true"
              min="1"
              type="number"
          />
          <BFormInvalidFeedback :state="mensagemErroDiasInativacao ? false : null">
            {{ mensagemErroDiasInativacao }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <BFormGroup
            class="mb-3"
            label-for="diasAlertaNovo"
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
              :state="mensagemErroDiasAlertaNovo ? false : null"
              aria-required="true"
              min="1"
              type="number"
          />
          <BFormInvalidFeedback :state="mensagemErroDiasAlertaNovo ? false : null">
            {{ mensagemErroDiasAlertaNovo }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <hr class="my-4">

        <div class="d-flex justify-content-end">
          <LoadingButton
              :loading="salvando"
              :text="TEXTOS.configuracoes.BOTAO_SALVAR"
              icon="check-lg"
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
import {BAlert, BForm, BFormGroup, BFormInput, BFormInvalidFeedback} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import {type Parametro, useConfiguracoes} from '@/composables/useConfiguracoes';
import {useNotification} from '@/composables/useNotification';
import {useValidacaoFormulario} from '@/composables/useValidacaoFormulario';
import {TEXTOS} from '@/constants/textos';

const {
  configuracoes,
  loading,
  error,
  carregarConfiguracoes,
  salvarConfiguracoes,
  getDiasInativacaoProcesso,
  getDiasAlertaNovo
} = useConfiguracoes();
const {notify, notificacao, clear} = useNotification();
const salvando = ref(false);
const {
  validacaoSubmetida,
  validarSubmissao,
  resetarValidacao,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const form = reactive({
  diasInativacao: 30,
  diasAlertaNovo: 3
});

const diasInativacaoInvalido = computed(() => Number(form.diasInativacao) < 1);
const diasAlertaNovoInvalido = computed(() => Number(form.diasAlertaNovo) < 1);
const formularioValido = computed(() => !diasInativacaoInvalido.value && !diasAlertaNovoInvalido.value);

const mensagemErroDiasInativacao = computed(() =>
    validacaoSubmetida.value && diasInativacaoInvalido.value ? "Informe um valor maior ou igual a 1." : ""
);

const mensagemErroDiasAlertaNovo = computed(() =>
    validacaoSubmetida.value && diasAlertaNovoInvalido.value ? "Informe um valor maior ou igual a 1." : ""
);

function atualizarFormulario() {
  form.diasInativacao = getDiasInativacaoProcesso();
  form.diasAlertaNovo = getDiasAlertaNovo();
}

async function carregar() {
  await carregarConfiguracoes();
  atualizarFormulario();
}

async function salvar() {
  if (!validarSubmissao(formularioValido.value)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  salvando.value = true;

  const findParametro = (chave: string) => configuracoes.value.find(p => p.chave === chave);

  const pInativacao = findParametro('DIAS_INATIVACAO_PROCESSO');
  const pAlertaNovo = findParametro('DIAS_ALERTA_NOVO');

  const ausentes = [];
  if (!pInativacao) ausentes.push('DIAS_INATIVACAO_PROCESSO');
  if (!pAlertaNovo) ausentes.push('DIAS_ALERTA_NOVO');

  if (ausentes.length > 0) {
    notify(`Os seguintes parâmetros não foram encontrados no banco de dados: ${ausentes.join(', ')}. Certifique-se de executar o script de migração SQL.`, 'danger');
    salvando.value = false;
    return;
  }

  const paramsToSave: Parametro[] = [
    {...pInativacao!, valor: form.diasInativacao.toString()},
    {...pAlertaNovo!, valor: form.diasAlertaNovo.toString()}
  ];

  const houveMudanca = paramsToSave.some(parametro => {
    const atual = findParametro(parametro.chave);
    return atual?.valor !== parametro.valor;
  });

  if (!houveMudanca) {
    resetarValidacao();
    notify(TEXTOS.configuracoes.SUCESSO_SALVAR, 'success');
    salvando.value = false;
    return;
  }

  const sucesso = await salvarConfiguracoes(paramsToSave);

  if (sucesso) {
    resetarValidacao();
    notify(TEXTOS.configuracoes.SUCESSO_SALVAR, 'success');
  } else {
    notify(TEXTOS.configuracoes.ERRO_SALVAR, 'danger');
  }

  salvando.value = false;
}

onMounted(async () => {
  if (configuracoes.value.length === 0) {
    await carregar();
  } else {
    atualizarFormulario();
  }
});

</script>
