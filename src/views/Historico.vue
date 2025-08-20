<template>
  <div class="container mt-4">
    <h2 class="display-6">Histórico de processos</h2>
    <table class="table table-striped table-hover mt-4">
      <thead role="rowgroup">
      <tr>
        <th style="cursor:pointer" @click="ordenarPor('descricao')">
          Descrição
          <span v-if="criterio === 'descricao'">{{ asc ? '↑' : '↓' }}</span>
        </th>
        <th style="cursor:pointer" @click="ordenarPor('tipo')">
          Tipo
          <span v-if="criterio === 'tipo'">{{ asc ? '↑' : '↓' }}</span>
        </th>
        <th style="cursor:pointer" @click="ordenarPor('unidades')">
          Unidades participantes
          <span v-if="criterio === 'unidades'">{{ asc ? '↑' : '↓' }}</span>
        </th>
        <th style="cursor:pointer" @click="ordenarPor('dataFinalizacao')">
          Finalizado em
          <span v-if="criterio === 'dataFinalizacao'">{{ asc ? '↑' : '↓' }}</span>
        </th>
      </tr>
      </thead>

      <tbody>
      <tr v-for="processo in processosFinalizadosOrdenados" :key="processo.id" class="clickable-row"
          @click="abrirProcesso(processo.id)">
        <td>{{ processo.descricao }}</td>
        <td>{{ processo.tipo }}</td>
        <td>{{ processosStore.getUnidadesDoProcesso(processo.id).map(pu => pu.unidade).join(', ') }}</td>
        <td>{{ processo.dataFinalizacao ? new Date(processo.dataFinalizacao).toLocaleDateString('pt-BR') : '' }}</td>
      </tr>
      </tbody>
    </table>

    <div v-if="processosFinalizadosOrdenados.length === 0" class="alert alert-info mt-4">
      Nenhum processo finalizado.
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useProcessosStore} from '@/stores/processos'
import {Processo, Subprocesso} from '@/types/tipos' // Import Processo and Subprocesso types

type SortCriteria = 'descricao' | 'tipo' | 'unidades' | 'dataFinalizacao';

const router = useRouter()
const processosStore = useProcessosStore()
const {processos} = storeToRefs(processosStore)

const criterio = ref<SortCriteria>('descricao')
const asc = ref(true)

const processosFinalizados = computed(() =>
    processos.value.filter((p: Processo) => p.situacao === 'Finalizado')
)

const processosFinalizadosOrdenados = computed(() => {
  return [...processosFinalizados.value].sort((a: Processo, b: Processo) => {
    let valA: any;
    let valB: any;

    if (criterio.value === 'unidades') {
      valA = processosStore.getUnidadesDoProcesso(a.id).map((pu: Subprocesso) => pu.unidade).join(', ');
      valB = processosStore.getUnidadesDoProcesso(b.id).map((pu: Subprocesso) => pu.unidade).join(', ');
    } else if (criterio.value === 'dataFinalizacao') {
      const dateA = a.dataFinalizacao ? new Date(a.dataFinalizacao) : null;
      const dateB = b.dataFinalizacao ? new Date(b.dataFinalizacao) : null;
      valA = dateA && !isNaN(dateA.getTime()) ? dateA.getTime() : null;
      valB = dateB && !isNaN(dateB.getTime()) ? dateB.getTime() : null;

      // Handle nulls for date comparison
      if (valA === null && valB === null) return 0;
      if (valA === null) return asc.value ? -1 : 1; // nulls come first if asc, last if desc
      if (valB === null) return asc.value ? 1 : -1;
      return (valA - valB) * (asc.value ? 1 : -1); // Compare timestamps
    } else if (criterio.value === 'descricao') {
      valA = a.descricao;
      valB = b.descricao;
    } else if (criterio.value === 'tipo') {
      valA = a.tipo;
      valB = b.tipo;
    }

    // General comparison for strings and numbers (after date specific handling)
    if (valA < valB) return asc.value ? -1 : 1;
    if (valA > valB) return asc.value ? 1 : -1;
    return 0;
  });
});

function ordenarPor(campo: SortCriteria) {
  if (criterio.value === campo) {
    asc.value = !asc.value
  } else {
    criterio.value = campo
    asc.value = true
  }
}

function abrirProcesso(processoId: number) {
  router.push({name: 'Processo', params: {idProcesso: processoId}})
}

</script>

<style scoped>
.clickable-row {
  cursor: pointer;
}
</style>
