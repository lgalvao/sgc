<template>
  <div>
    <table
      class="table table-hover"
      data-testid="tabela-processos"
    >
      <thead>
        <tr>
          <th
            data-testid="coluna-descricao"
            style="cursor:pointer"
            @click="handleOrdenar('descricao')"
          >
            Descrição
            <span v-if="criterioOrdenacao === 'descricao'">{{ direcaoOrdenacaoAsc ? '↑' : '↓' }}</span>
          </th>
          <th
            data-testid="coluna-tipo"
            style="cursor:pointer"
            @click="handleOrdenar('tipo')"
          >
            Tipo
            <span v-if="criterioOrdenacao === 'tipo'">{{ direcaoOrdenacaoAsc ? '↑' : '↓' }}</span>
          </th>
          <th
            v-if="showDataFinalizacao"
            data-testid="coluna-data-finalizacao"
            style="cursor:pointer"
            @click="handleOrdenar('dataFinalizacao')"
          >
            Finalizado em
            <span v-if="criterioOrdenacao === 'dataFinalizacao'">{{ direcaoOrdenacaoAsc ? '↑' : '↓' }}</span>
          </th>
          <th
            data-testid="coluna-situacao"
            style="cursor:pointer"
            @click="handleOrdenar('situacao')"
          >
            Situação
            <span v-if="criterioOrdenacao === 'situacao'">{{ direcaoOrdenacaoAsc ? '↑' : '↓' }}</span>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="processo in processos"
          :key="processo.codigo"
          :data-testid="`processo-row-${processo.codigo}`"
          class="clickable-row"
          style="cursor:pointer;"
          @click="handleSelecionarProcesso(processo)"
        >
          <td>
            {{ processo.descricao }}
          </td>
          <td>{{ processo.tipo }}</td>
          <td v-if="showDataFinalizacao">
            {{ (processo as any).dataFinalizacaoFormatada }}
          </td>
          <td>{{ processo.situacao }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import {ProcessoResumo} from '@/types/tipos';

defineProps<{
  processos: ProcessoResumo[];
  criterioOrdenacao: keyof ProcessoResumo | 'dataFinalizacao';
  direcaoOrdenacaoAsc: boolean;
  showDataFinalizacao?: boolean;
}>();

const emit = defineEmits<{
  (e: 'ordenar', campo: keyof ProcessoResumo | 'dataFinalizacao'): void
  (e: 'selecionarProcesso', processo: ProcessoResumo): void
}>()


const handleOrdenar = (campo: keyof ProcessoResumo | 'dataFinalizacao') => {
  emit('ordenar', campo);
};

const handleSelecionarProcesso = (processo: ProcessoResumo) => {
  emit('selecionarProcesso', processo);
};
</script>
