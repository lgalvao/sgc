<template>
  <div class="d-flex flex-column gap-3">
    <BCard v-for="item in itens" :key="item.codigoUnidade" class="shadow-sm"
           data-testid="card-relatorio-gaps-diagnostico">
      <BCardTitle class="mb-1 relatorio-diagnostico__titulo">{{ item.siglaUnidade }}</BCardTitle>
      <BCardText class="text-muted mb-3">{{ item.nomeUnidade }}</BCardText>

      <BTable
          :fields="campos"
          :items="item.competencias"
          bordered
          responsive
          small
      >
        <template #cell(mediaGap)="{ item: competencia }">
          {{ formatarMediaGap(competencia.mediaGap) }}
        </template>
      </BTable>
    </BCard>
  </div>
</template>

<script lang="ts" setup>
import {BCard, BCardText, BCardTitle, BTable} from "bootstrap-vue-next";

type CompetenciaGap = {
  competenciaDescricao: string;
  mediaGap: number | null;
  totalAvaliacoesConsideradas: number;
};

type ItemRelatorioGap = {
  codigoUnidade: number;
  siglaUnidade: string;
  nomeUnidade: string;
  competencias: CompetenciaGap[];
};

defineProps<{
  campos: { key: string; label: string }[];
  itens: ItemRelatorioGap[];
}>();

function formatarMediaGap(mediaGap: number | null) {
  return mediaGap == null ? "-" : mediaGap.toFixed(2);
}
</script>

<style scoped>
.relatorio-diagnostico__titulo {
  color: var(--bs-heading-color);
  font-size: 1.05rem;
}
</style>
