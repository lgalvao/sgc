<template>
  <ModalPadrao
      v-model="modelValueComputed"
      :mostrar-botao-acao="false"
      tamanho="xl"
      texto-cancelar="Fechar"
      titulo="Andamento Geral dos Processos"
  >
    <div class="mb-3">
      <BButton
          data-testid="export-csv-andamento"
          size="sm"
          variant="outline-primary"
          @click="exportar"
      >
        <i aria-hidden="true" class="bi bi-download"/> Exportar CSV
      </BButton>
    </div>
    <div class="table-responsive">
      <table class="table table-striped">
        <thead>
        <tr>
          <th>Descrição</th>
          <th>Tipo</th>
          <th>Situação</th>
          <th>Data Limite</th>
          <th>Unidade</th>
          <th>% Concluído</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="processo in processos"
            :key="processo.codigo"
        >
          <td>{{ processo.descricao }}</td>
          <td>{{ processo.tipo }}</td>
          <td>{{ processo.situacao }}</td>
          <td>{{ formatDateBR(processo.dataLimite) }}</td>
          <td>{{ processo.unidadeNome }}</td>
          <td>{{ calcularPercentualConcluido() }}%</td>
        </tr>
        </tbody>
      </table>
    </div>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {BButton} from "bootstrap-vue-next";
import {computed} from "vue";
import {downloadCSV, gerarCSV} from "@/utils/csv";
import type {ProcessoResumo} from "@/types/tipos";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import {formatDateBR} from "@/utils";

const props = defineProps<{
  modelValue: boolean;
  processos: ProcessoResumo[];
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

const modelValueComputed = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

function calcularPercentualConcluido() {
  // A lógica de percentual concluído precisa ser reavaliada com os novos DTOs.
  // Por enquanto, retornaremos um valor fixo ou uma lógica simplificada.
  return 0;
}

function exportar() {
  const dados = props.processos.map((processo) => ({
    Descricao: processo.descricao,
    Tipo: processo.tipo,
    Situacao: processo.situacao,
    "Data Limite": formatDateBR(processo.dataLimite),
    Unidade: processo.unidadeNome,
    "% Concluido": calcularPercentualConcluido(),
  }));

  const csv = gerarCSV(dados);
  downloadCSV(csv, "andamento-geral.csv");
}

defineExpose({
  modelValueComputed,
  calcularPercentualConcluido,
  exportar
});
</script>
