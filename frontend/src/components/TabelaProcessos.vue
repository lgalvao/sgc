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
            @click="emit('ordenar', 'descricao')"
          >
            Descrição
            <span v-if="criterioOrdenacao === 'descricao'">{{ direcaoOrdenacaoAsc ? '↑' : '↓' }}</span>
          </th>
          <th
            data-testid="coluna-tipo"
            style="cursor:pointer"
            @click="emit('ordenar', 'tipo')"
          >
            Tipo
            <span v-if="criterioOrdenacao === 'tipo'">{{ direcaoOrdenacaoAsc ? '↑' : '↓' }}</span>
          </th>
          <th
            data-testid="coluna-unidades"
            style="cursor:pointer"
            @click="emit('ordenar', 'unidades')"
          >
            Unidades participantes
            <span v-if="criterioOrdenacao === 'unidades'">{{ direcaoOrdenacaoAsc ? '↑' : '↓' }}</span>
          </th>
          <th
            v-if="showDataFinalizacao"
            data-testid="coluna-data-finalizacao"
            style="cursor:pointer"
            @click="emit('ordenar', 'dataFinalizacao')"
          >
            Finalizado em
            <span v-if="criterioOrdenacao === 'dataFinalizacao'">{{ direcaoOrdenacaoAsc ? '↑' : '↓' }}</span>
          </th>
          <th
            data-testid="coluna-situacao"
            style="cursor:pointer"
            @click="emit('ordenar', 'situacao')"
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
          class="clickable-row"
          style="cursor:pointer;"
          @click="emit('selecionarProcesso', processo)"
        >
          <td>
            {{ processo.descricao }}
          </td>
          <td>{{ processo.tipo }}</td>
          <td>{{ processo.unidadesFormatadas }}</td>
          <td v-if="showDataFinalizacao">
            {{ processo.dataFinalizacaoFormatada }}
          </td>
          <td>{{ processo.situacao }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import {Processo} from '@/types/tipos';

defineProps<{
  processos: (Processo & { unidadesFormatadas: string, dataFinalizacaoFormatada?: string | null })[];
  criterioOrdenacao: keyof Processo | 'unidades' | 'dataFinalizacao';
  direcaoOrdenacaoAsc: boolean;
  showDataFinalizacao?: boolean;
}>();

const emit = defineEmits<{
  (e: 'ordenar', campo: keyof Processo | 'unidades' | 'dataFinalizacao'): void;
  (e: 'selecionarProcesso', processo: Processo): void;
}>();
</script>
