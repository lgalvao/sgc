<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.configuracoes.TITULO"/>

    <CarregamentoPagina v-if="carregandoConfiguracoes"/>

    <AppAlertaTela
        v-else-if="erroTela"
        data-testid="alert-configuracao-erro"
        :mensagem="erroTela"
        @dismissed="erroDispensado = true"
    />

    <template v-else>
      <AppAlert
          v-if="notificacao"
          :chave="notificacao.chave"
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
            {{ TEXTOS.configuracoes.LABEL_DIAS_INATIVACAO }}
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
            {{ TEXTOS.configuracoes.LABEL_DIAS_ALERTA_NOVO }}
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
import {computed, onMounted, reactive, ref, watch} from 'vue';
import {BForm, BFormGroup, BFormInput, BFormInvalidFeedback} from 'bootstrap-vue-next';
import AppAlertaTela from '@/components/comum/AppAlertaTela.vue';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import {useToast} from '@/composables/useToast';
import {type Parametro, useConfiguracoes} from '@/composables/useConfiguracoes';
import {useNotification} from '@/composables/useNotification';
import {useValidacaoFormulario} from '@/composables/useValidacaoFormulario';
import {TEXTOS} from '@/constants/textos';
import {logger} from '@/utils';

const {
  configuracoes,
  carregandoConfiguracoes,
  erro,
  carregarConfiguracoes,
  salvarConfiguracoes,
  obterDiasInativacaoProcesso,
  obterDiasAlertaNovo
} = useConfiguracoes();
const {notify, notificacao, clear} = useNotification();
const {exibirSucesso} = useToast();
const salvando = ref(false);
const erroDispensado = ref(false);
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
const erroTela = computed(() => erroDispensado.value ? null : erro.value);

watch(erro, (novoErro) => {
  if (novoErro) {
    erroDispensado.value = false;
  }
});

function atualizarFormulario() {
  form.diasInativacao = obterDiasInativacaoProcesso();
  form.diasAlertaNovo = obterDiasAlertaNovo();
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

  if (!pInativacao || !pAlertaNovo) {
    logger.error('Parâmetros de configuração ausentes', {
      inativacao: !!pInativacao,
      alertaNovo: !!pAlertaNovo
    });
    return;
  }

  const paramsToSave: Parametro[] = [
    {...pInativacao, valor: form.diasInativacao.toString()},
    {...pAlertaNovo, valor: form.diasAlertaNovo.toString()}
  ];

  const houveMudanca = paramsToSave.some(parametro => {
    const atual = findParametro(parametro.chave);
    return atual?.valor !== parametro.valor;
  });

  if (!houveMudanca) {
    resetarValidacao();
    clear();
    exibirSucesso(TEXTOS.configuracoes.SUCESSO_SALVAR);
    salvando.value = false;
    return;
  }

  const sucesso = await salvarConfiguracoes(paramsToSave);

  if (sucesso) {
    resetarValidacao();
    clear();
    exibirSucesso(TEXTOS.configuracoes.SUCESSO_SALVAR);
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
