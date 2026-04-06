<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.administracao.LIMPEZA_TITULO" />

    <AppAlert
        v-if="notificacao"
        :dismissible="notificacao.dismissible ?? true"
        :message="notificacao.message"
        :variant="notificacao.variant"
        @dismissed="clear()"
    />

    <BAlert :model-value="true" variant="warning">
      {{ TEXTOS.administracao.LIMPEZA_ALERTA }}
    </BAlert>

    <BCard class="mt-3">
      <p class="mb-3">
        {{ TEXTOS.administracao.LIMPEZA_DESCRICAO }}
      </p>

      <BFormGroup label-for="codigoProcesso">
        <template #label>
          {{ TEXTOS.administracao.LIMPEZA_LABEL_CODIGO }}
        </template>
        <BFormInput
            id="codigoProcesso"
            v-model="codigoProcesso"
            data-testid="input-codigo-processo"
            min="1"
            :placeholder="TEXTOS.administracao.LIMPEZA_PLACEHOLDER_CODIGO"
            type="number"
            @keydown.enter.prevent="abrirConfirmacao"
        />
      </BFormGroup>

      <div class="d-flex justify-content-end mt-3">
        <LoadingButton
            data-testid="btn-excluir-processo-completo"
            :loading="excluindo"
            icon="trash"
            :text="TEXTOS.administracao.LIMPEZA_BOTAO_ABRIR"
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
import {normalizeError} from '@/utils/apiError';
import {excluirProcessoCompleto} from '@/services/processoService';

const {notificacao, notify, clear} = useNotification();

const codigoProcesso = ref('');
const excluindo = ref(false);
const mostrarConfirmacao = ref(false);

const codigoConfirmacao = computed(() => {
  const codigo = Number(codigoProcesso.value);
  return Number.isInteger(codigo) && codigo > 0 ? codigo : null;
});

function abrirConfirmacao() {
  if (codigoConfirmacao.value === null) {
    notify(TEXTOS.administracao.LIMPEZA_ERRO_CODIGO, 'warning');
    return;
  }

  mostrarConfirmacao.value = true;
}

async function confirmarExclusao() {
  if (codigoConfirmacao.value === null) {
    notify(TEXTOS.administracao.LIMPEZA_ERRO_CODIGO, 'warning');
    return;
  }

  excluindo.value = true;
  try {
    await excluirProcessoCompleto(codigoConfirmacao.value);
    mostrarConfirmacao.value = false;
    codigoProcesso.value = '';
    notify(TEXTOS.administracao.LIMPEZA_SUCESSO, 'success');
  } catch (error) {
    notify(normalizeError(error).message, 'danger');
  } finally {
    excluindo.value = false;
  }
}
</script>
