<script setup lang="ts">
import {BTable} from "bootstrap-vue-next";
import EmptyState from "@/components/comum/EmptyState.vue";
import type {Movimentacao} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {formatarDataHoraBR} from "@/utils";

defineProps<{
  movimentacoes: Movimentacao[];
}>();

const camposMovimentacoes = [
  {key: "dataHora", label: TEXTOS.subprocesso.MOVIMENTACOES_CAMPO_DATA},
  {key: "unidadeOrigem", label: TEXTOS.subprocesso.MOVIMENTACOES_CAMPO_ORIGEM},
  {key: "unidadeDestino", label: TEXTOS.subprocesso.MOVIMENTACOES_CAMPO_DESTINO},
  {key: "descricao", label: TEXTOS.subprocesso.MOVIMENTACOES_CAMPO_DESCRICAO}
];

function rowAttrMovimentacao(item: Movimentacao | null) {
  return item ? {'data-testid': `row-movimentacao-${item.codigo}`} : {};
}
</script>

<template>
  <div class="mt-4">
    <h4>{{ TEXTOS.subprocesso.MOVIMENTACOES_TITULO }}</h4>
    <BTable
        :fields="camposMovimentacoes"
        :items="movimentacoes"
        :tbody-tr-props="rowAttrMovimentacao"
        data-testid="tbl-movimentacoes"
        primary-key="codigo"
        small
        responsive
        show-empty
        stacked="md"
    >
      <template #empty>
        <EmptyState
            class="mb-0"
            data-testid="empty-state-movimentacoes"
            :description="TEXTOS.subprocesso.MOVIMENTACOES_VAZIO_TEXTO"
            icon="bi-arrow-left-right"
            :title="TEXTOS.subprocesso.MOVIMENTACOES_VAZIO_TITULO"
        />
      </template>
      <template #cell(dataHora)="data">
        {{ formatarDataHoraBR(data.item.dataHora) }}
      </template>
      <template #cell(unidadeOrigem)="data">
        {{ data.item.unidadeOrigemSigla || '-' }}
      </template>
      <template #cell(unidadeDestino)="data">
        {{ data.item.unidadeDestinoSigla || '-' }}
      </template>
    </BTable>
  </div>
</template>
