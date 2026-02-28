<template>
  <div class="mt-4">
    <h4>Movimentações</h4>
    <BTable
        :fields="fields"
        :items="movimentacoes"
        :tbody-tr-attr="rowAttr"
        data-testid="tbl-movimentacoes"
        primary-key="codigo"
        responsive
        show-empty
        stacked="md"
        striped
    >
      <template #empty>
        <div class="text-center text-muted py-5" data-testid="empty-state-movimentacoes">
          <i aria-hidden="true" class="bi bi-arrow-left-right display-4 d-block mb-3"></i>
          <p class="h5">Nenhuma movimentação</p>
          <p class="small">O histórico de movimentações deste processo aparecerá aqui.</p>
        </div>
      </template>
      <template #cell(dataHora)="data">
        {{ formatDateTimeBR(data.item.dataHora) }}
      </template>
      <template #cell(unidadeOrigem)="data">
        {{ data.item.unidadeOrigem?.sigla || '-' }}
      </template>
      <template #cell(unidadeDestino)="data">
        {{ data.item.unidadeDestino?.sigla || '-' }}
      </template>
      <template #cell(situacao)="data">
        <BadgeSituacao
            :situacao="data.item.subprocesso?.situacao || 'DESCONHECIDO'"
            :texto="formatarSituacao(data.item.subprocesso?.situacao)"
        />
      </template>
    </BTable>
  </div>
</template>

<script lang="ts" setup>
import {BTable} from "bootstrap-vue-next";
import {ref} from "vue";
import type {Movimentacao} from "@/types/tipos";
import {formatDateTimeBR} from "@/utils";
import BadgeSituacao from "@/components/comum/BadgeSituacao.vue";

defineProps<{
  movimentacoes: Movimentacao[];
}>();

const fields = ref([
  {key: "dataHora", label: "Data/Hora"},
  {key: "unidadeOrigem", label: "Unidade Origem"},
  {key: "unidadeDestino", label: "Unidade Destino"},
  {key: "situacao", label: "Situação"},
  {key: "descricao", label: "Descrição"}
]);

const rowAttr = (item: Movimentacao | null, type: string) => {
  return item && type === 'row'
      ? {'data-testid': `row-movimentacao-${item.codigo}`}
      : {}
};

function formatarSituacao(situacao?: string) {
  if (!situacao) return "Desconhecido";
  return situacao.replaceAll("_", " ");
}
</script>
