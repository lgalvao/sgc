<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2>Processos</h2>
      <router-link to="/processos/novo" class="btn btn-success">Novo processo</router-link>
    </div>
    <table class="table table-striped mt-4">
      <thead>
        <tr>
          <th>#</th>
          <th>Descrição</th>
          <th>Tipo</th>
          <th>Unidades participantes</th>
          <th>Data limite</th>
          <th>Situação</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(processo, idx) in processos" :key="processo.id" @click="abrirUnidades(processo.id)" style="cursor:pointer">
          <td>{{ processo.id }}</td>
          <td>{{ processo.descricao }}</td>
          <td>{{ processo.tipo }}</td>
          <td>{{ processo.unidades }}</td>
          <td>{{ processo.dataLimite }}</td>
          <td>{{ processo.situacao }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
// Lista fictícia de processos importada de JSON
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import processosJson from '../mocks/processos.json'
const router = useRouter()

const processos = ref(processosJson)

function abrirUnidades(id) {
  router.push(`/processos/${id}/unidades`)
}
</script> 