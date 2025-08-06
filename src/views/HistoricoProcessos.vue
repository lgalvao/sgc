<template>
  <div class="container mt-4">
    <h2>Histórico de processos</h2>
    <table class="table table-striped mt-4">
      <thead>
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
      <tr v-for="processo in processosFinalizadosOrdenados" :key="processo.id">
        <td>{{ processo.descricao }}</td>
        <td>{{ processo.tipo }}</td>
        <td>{{ processosStore.getUnidadesDoProcesso(processo.id).map(pu => pu.unidadeId).join(', ') }}</td>
        <td>{{ formatarData(processo.dataFinalizacao) }}</td>
      </tr>
      </tbody>
    </table>

    <div v-if="processosFinalizadosOrdenados.length === 0" class="alert alert-info mt-4">
      Nenhum processo finalizado.
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {storeToRefs} from 'pinia'
import {useProcessosStore} from '../stores/processos'

const processosStore = useProcessosStore()
const {processos} = storeToRefs(processosStore)

const criterio = ref('descricao')
const asc = ref(true)

const processosFinalizados = computed(() =>
    processos.value.filter(p => p.situacao === 'Finalizado')
)

const processosFinalizadosOrdenados = computed(() => {
  return [...processosFinalizados.value].sort((a, b) => {
    let valA = a[criterio.value];
    let valB = b[criterio.value];

    if (criterio.value === 'unidades') {
      valA = processosStore.getUnidadesDoProcesso(a.id).map(pu => pu.unidadeId).join(', ');
      valB = processosStore.getUnidadesDoProcesso(b.id).map(pu => pu.unidadeId).join(', ');
    }

    if (valA < valB) return asc.value ? -1 : 1;
    if (valA > valB) return asc.value ? 1 : -1;
    return 0;
  });
});

function formatarData(data) {
  if (!data) return ''
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}

function ordenarPor(campo) {
  if (criterio.value === campo) {
    asc.value = !asc.value
  } else {
    criterio.value = campo
    asc.value = true
  }
}
</script>