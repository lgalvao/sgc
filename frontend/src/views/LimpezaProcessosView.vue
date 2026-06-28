<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.administracao.LIMPEZA_TITULO">
      <template #alerta>
        <Alerta
            v-if="notificacao"
            :chave="notificacao.chave"
            :dispensavel="notificacao.dispensavel ?? true"
            :mensagem="notificacao.mensagem"
            :variante="notificacao.variante"
            @dismissed="clear()"
        />

        <Alerta
            :dispensavel="true"
            :mensagem="TEXTOS.administracao.LIMPEZA_ALERTA"
            variante="warning"
        />
      </template>
    </PageHeader>

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

    <LimpezaProcessosFluxoModais
        :codigo-confirmacao="codigoConfirmacao ?? null"
        :excluindo="excluindo"
        :mostrar-confirmacao="mostrarConfirmacao"
        @confirmar-exclusao="confirmarExclusao"
        @update:mostrar-confirmacao="mostrarConfirmacao = $event"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BCard, BFormGroup, BFormInput} from 'bootstrap-vue-next';
import Alerta from '@/components/comum/Alerta.vue';
import LimpezaProcessosFluxoModais from '@/components/administracao/LimpezaProcessosFluxoModais.vue';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import {TEXTOS} from '@/constants/textos';
import {useLimpezaProcessosTela} from '@/composables/useLimpezaProcessosTela';

const {
  codigoProcesso,
  codigoConfirmacao,
  excluindo,
  mensagemErroCodigo,
  mostrarConfirmacao,
  notificacao,
  clear,
  abrirConfirmacao,
  confirmarExclusao,
} = useLimpezaProcessosTela();
</script>
