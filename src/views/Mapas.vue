<template>
  <div class="container mt-4">
    <h2>Mapas de Competências</h2>
    <div class="row mb-3">
      <div class="col-md-4">
        <label for="filtroUnidade" class="form-label">Filtrar por unidade</label>
        <select v-model="filtroUnidade" id="filtroUnidade" class="form-select">
          <option value="">Todas</option>
          <option v-for="u in unidadesFolha" :key="u.sigla" :value="u.sigla">{{ u.sigla }} - {{ u.nome }}</option>
        </select>
      </div>
      <div class="col-md-4">
        <label for="filtroStatus" class="form-label">Filtrar por status</label>
        <select v-model="filtroStatus" id="filtroStatus" class="form-select">
          <option value="">Todos</option>
          <option value="em_andamento">Em andamento</option>
          <option value="disponivel_validacao">Disponível para validação</option>
          <option value="finalizado">Finalizado</option>
        </select>
      </div>
    </div>
    <div v-if="mapasFiltrados.length === 0" class="alert alert-warning">Nenhum mapa encontrado para o filtro selecionado.</div>
    <div class="row mt-4">
      <div v-for="mapa in mapasFiltrados" :key="mapa.id" class="col-md-6 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Unidade: {{ mapa.unidade }}</h5>
            <p class="card-text mb-1"><strong>Status:</strong> <span :class="badgeClass(mapa.status)">{{ statusLabel(mapa.status) }}</span></p>
            <p class="card-text mb-1"><strong>Competências:</strong> {{ mapa.competencias.length }}</p>
            <p class="card-text mb-1"><strong>Data de criação:</strong> {{ mapa.dataCriacao ? formatarData(mapa.dataCriacao) : '-' }}</p>
            <router-link :to="`/unidade/${mapa.unidade}/mapa/visualizar`" class="btn btn-outline-primary btn-sm mt-2">Visualizar</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useMapasStore } from '../stores/mapas'
import { useUnidadesStore } from '../stores/unidades'

const mapasStore = useMapasStore()
const { mapas } = storeToRefs(mapasStore)
const unidadesStore = useUnidadesStore()
const { unidades } = storeToRefs(unidadesStore)

const filtroUnidade = ref('')
const filtroStatus = ref('')

function coletarFolhas(unidades) {
  let folhas = []
  for (const unidade of unidades) {
    if (!unidade.filhas || unidade.filhas.length === 0) {
      folhas.push({ sigla: unidade.sigla, nome: unidade.nome || unidade.sigla })
    } else {
      folhas = folhas.concat(coletarFolhas(unidade.filhas))
    }
  }
  return folhas
}
const unidadesFolha = computed(() => coletarFolhas(unidades.value))

const mapasFiltrados = computed(() => {
  return mapas.value.filter(m => {
    const unidadeOk = !filtroUnidade.value || m.unidade === filtroUnidade.value
    const statusOk = !filtroStatus.value || m.status === filtroStatus.value
    return unidadeOk && statusOk
  })
})

function badgeClass(status) {
  if (status === 'em_andamento') return 'badge bg-warning text-dark'
  if (status === 'disponivel_validacao') return 'badge bg-info text-dark'
  if (status === 'finalizado') return 'badge bg-success'
  return 'badge bg-secondary'
}
function statusLabel(status) {
  if (status === 'em_andamento') return 'Em andamento'
  if (status === 'disponivel_validacao') return 'Disponível para validação'
  if (status === 'finalizado') return 'Finalizado'
  return status
}
function formatarData(dataISO) {
  if (!dataISO) return '-'
  const d = new Date(dataISO)
  return d.toLocaleDateString('pt-BR')
}
</script> 