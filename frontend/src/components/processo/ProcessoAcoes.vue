<script lang="ts" setup>
import {BButton, BDropdown, BDropdownItemButton} from "bootstrap-vue-next";
import PageHeader from "@/components/layout/PageHeader.vue";
import ProcessoInfo from "@/components/processo/ProcessoInfo.vue";
import {TEXTOS} from "@/constants/textos";
import type {AcaoBlocoProcesso, Processo} from "@/types/tipos";
import {obterIdBotaoAcaoProcesso, obterTestIdBotaoAcaoProcesso} from "@/components/processo/processoAcoes";

interface Props {
  processo: Processo;
  mostrarFinalizarProcesso: boolean;
  podeFinalizar: boolean;
  usarMenuAcoesBloco: boolean;
  acoesBlocoVisiveis: AcaoBlocoProcesso[];
  acaoBlocoPrincipal: AcaoBlocoProcesso | null;
  processandoAcaoBloco: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  (e: "finalizar"): void;
  (e: "abrir-acao-bloco", acao: AcaoBlocoProcesso): void;
}>();
</script>

<template>
  <PageHeader :title="processo.descricao" title-test-id="processo-info">
    <template #default>
      <ProcessoInfo
          :show-data-limite="false"
          :situacao="processo.situacao"
          :tipo="processo.tipo"/>
    </template>

    <template #actions>
      <BButton
          v-if="mostrarFinalizarProcesso"
          :disabled="!podeFinalizar"
          data-testid="btn-processo-finalizar"
          variant="danger"
          @click="emit('finalizar')"
      >
        {{ TEXTOS.processo.FINALIZAR }}
      </BButton>

      <BDropdown
          v-if="usarMenuAcoesBloco"
          :text="TEXTOS.processo.ACOES_EM_BLOCO"
          data-testid="btn-processo-acoes-bloco"
          toggle-class="text-nowrap"
          variant="secondary">
        <BDropdownItemButton
            v-for="acao in acoesBlocoVisiveis"
            :id="obterIdBotaoAcaoProcesso(acao.codigo)"
            :key="acao.codigo"
            :data-testid="obterTestIdBotaoAcaoProcesso(acao.codigo)"
            :disabled="!acao.habilitar || processandoAcaoBloco"
            @click="emit('abrir-acao-bloco', acao)">
          {{ acao.rotulo }}
        </BDropdownItemButton>
      </BDropdown>

      <BButton
          v-else-if="acaoBlocoPrincipal"
          :id="obterIdBotaoAcaoProcesso(acaoBlocoPrincipal.codigo)"
          :data-testid="obterTestIdBotaoAcaoProcesso(acaoBlocoPrincipal.codigo)"
          :disabled="!acaoBlocoPrincipal.habilitar || processandoAcaoBloco"
          variant="success"
          @click="emit('abrir-acao-bloco', acaoBlocoPrincipal)">
        {{ acaoBlocoPrincipal.rotulo }}
      </BButton>
    </template>
  </PageHeader>
</template>
