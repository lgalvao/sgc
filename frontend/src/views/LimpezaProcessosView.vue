<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.administracao.LIMPEZA_TITULO"/>

    <AppAlert
        v-if="notificacao"
        :dispensavel="notificacao.dispensavel ?? true"
        :mensagem="notificacao.mensagem"
        :variante="notificacao.variante"
        @dismissed="clear()"
    />

    <BAlert :model-value="true" dismissible variant="warning">
      {{ TEXTOS.administracao.LIMPEZA_ALERTA }}
    </BAlert>

    <BCard class="mt-3">
      <BFormGroup
          :state="mensagemErroCodigo ? false : null"
          label-for="codigoProcesso"
      >
        <template #label>
          {{ TEXTOS.administracao.LIMPEZA_LABEL_CODIGO }} <span aria-hidden="true" class="text-danger">*</span>
        </template>
        <BFormInput
            id="codigoProcesso"
            v-model="codigoProcesso"
            :state="mensagemErroCodigo ? false : null"
            data-testid="input-codigo-processo"
            min="1"
            type="number"
            @keydown.enter.prevent="abrirConfirmacao"
        />
        <BFormInvalidFeedback :state="mensagemErroCodigo ? false : null">
          {{ mensagemErroCodigo }}
        </BFormInvalidFeedback>
      </BFormGroup>

      <div class="d-flex justify-content-end mt-3">
        <LoadingButton
            :loading="excluindo"
            :text="TEXTOS.administracao.LIMPEZA_BOTAO_ABRIR"
            data-testid="btn-excluir-processo-completo"
            icon="trash"
            variant="danger"
            @click="abrirConfirmacao"
        />
      </div>
    </BCard>

    <ModalConfirmacao
        v-model="mostrarConfirmacao"
        :auto-close="false"
        :loading="excluindo"
        :ok-title="TEXTOS.comum.BOTAO_REMOVER"
        :titulo="TEXTOS.administracao.LIMPEZA_MODAL_TITULO"
        variant="danger"
        @confirmar="confirmarExclusao"
    >
      <p v-if="codigoConfirmacao !== null">
        {{ TEXTOS.administracao.LIMPEZA_MODAL_TEXTO(codigoConfirmacao) }}
      </p>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import {BAlert, BCard, BFormGroup, BFormInput} from 'bootstrap-vue-next';
import AppAlert from '@/components/comum/AppAlert.vue';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import {TEXTOS} from '@/constants/textos';
import {useNotification} from '@/composables/useNotification';
import {useValidacaoFormulario} from '@/composables/useValidacaoFormulario';
import {normalizarErro} from '@/utils/apiError';
import {excluirProcessoCompleto} from '@/services/processo';

const {notificacao, notify, clear} = useNotification();
const {
  validarSubmissao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const codigoProcesso = ref('');
const excluindo = ref(false);
const mostrarConfirmacao = ref(false);

const codigoConfirmacao = computed(() => {
  const codigo = Number(codigoProcesso.value);
  return Number.isInteger(codigo) && codigo > 0 ? codigo : null;
});

const mensagemErroCodigo = computed(() => {
  return deveExibirErro(codigoConfirmacao.value === null) ? TEXTOS.administracao.LIMPEZA_ERRO_CODIGO : '';
});

async function abrirConfirmacao() {
  if (!validarSubmissao(codigoConfirmacao.value !== null)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  mostrarConfirmacao.value = true;
}

async function confirmarExclusao() {
  if (codigoConfirmacao.value === null) {
    return;
  }

  excluindo.value = true;
  try {
    await excluirProcessoCompleto(codigoConfirmacao.value);
    mostrarConfirmacao.value = false;
    codigoProcesso.value = '';
    notify(TEXTOS.administracao.LIMPEZA_SUCESSO, 'success');
  } catch (error) {
    notify(normalizarErro(error).mensagem, 'danger');
  } finally {
    excluindo.value = false;
  }
}
</script>
