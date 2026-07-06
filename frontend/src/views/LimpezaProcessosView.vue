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
          :state="mensagemErroProcesso ? false : null"
          label-for="processoSelecionado"
      >
        <template #label>
          {{ TEXTOS.administracao.LIMPEZA_LABEL_PROCESSO }} <span aria-hidden="true" class="text-danger">*</span>
        </template>
        <BFormSelect
            id="processoSelecionado"
            v-model="codigoProcessoSelecionado"
            :disabled="carregandoProcessos || excluindo"
            :options="processos"
            :state="mensagemErroProcesso ? false : null"
            data-testid="select-processo"
            text-field="descricao"
            value-field="codigo"
            @keydown.enter.prevent="abrirConfirmacao"
        >
          <template #first>
            <BFormSelectOption :value="null" disabled>
              {{ TEXTOS.administracao.LIMPEZA_PLACEHOLDER_PROCESSO }}
            </BFormSelectOption>
          </template>
        </BFormSelect>
        <BFormInvalidFeedback :state="mensagemErroProcesso ? false : null">
          {{ mensagemErroProcesso }}
        </BFormInvalidFeedback>
        <div v-if="!carregandoProcessos && !processos.length" class="mt-2 text-muted">
          {{ TEXTOS.administracao.LIMPEZA_NENHUM_PROCESSO }}
        </div>
      </BFormGroup>

      <div class="d-flex justify-content-end mt-3">
        <LoadingButton
            :disabled="carregandoProcessos || !processoSelecionado"
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
        :descricao-confirmacao="descricaoConfirmacao"
        :excluindo="excluindo"
        :mostrar-confirmacao="mostrarConfirmacao"
        @confirmar-exclusao="confirmarExclusao"
        @update:mostrar-confirmacao="mostrarConfirmacao = $event"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BCard, BFormGroup, BFormInvalidFeedback, BFormSelect, BFormSelectOption} from 'bootstrap-vue-next';
import Alerta from '@/components/comum/Alerta.vue';
import LimpezaProcessosFluxoModais from '@/components/administracao/LimpezaProcessosFluxoModais.vue';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import {TEXTOS} from '@/constants/textos';
import {useLimpezaProcessosTela} from '@/composables/useLimpezaProcessosTela';

const {
  processos,
  carregandoProcessos,
  codigoProcessoSelecionado,
  processoSelecionado,
  codigoConfirmacao,
  descricaoConfirmacao,
  excluindo,
  mensagemErroProcesso,
  mostrarConfirmacao,
  notificacao,
  clear,
  abrirConfirmacao,
  confirmarExclusao,
} = useLimpezaProcessosTela();
</script>
