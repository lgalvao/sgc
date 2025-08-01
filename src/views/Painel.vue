<template>
  <div class="container mt-4">
    <!-- Tabela de Processos -->
    <div class="mb-5">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="display-5 mb-0">Processos</div>
        <router-link v-if="perfil.perfilSelecionado === 'ADMIN'" class="btn btn-outline-primary" to="/processos/novo">
          <i class="bi bi-plus-lg"></i> Criar processo
        </router-link>
      </div>
      <table class="table table-hover">
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
            <th style="cursor:pointer" @click="ordenarPor('situacao')">
              Situação
              <span v-if="criterio === 'situacao'">{{ asc ? '↑' : '↓' }}</span>
            </th>
            <!-- Coluna Unidade Atual a ser adicionada na Fase 2 -->
          </tr>
        </thead>
        <tbody>
          <tr v-for="processo in processosOrdenados" :key="processo.id">
            <td style="cursor:pointer; color: var(--bs-link-color);" @click="abrirDetalhesProcesso(processo)">
              {{ processo.descricao }}
            </td>
            <td>{{ processo.tipo }}</td>
            <td>{{ processo.unidades }}</td>
            <td>{{ consolidarSituacaoProcesso(processo) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div>
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="mb-0 display-5">Alertas</div>
      </div>
      <table class="table table-hover">
        <thead>
          <tr>
            <!-- Colunas a serem implementadas na Fase 3 -->
            <th>Data/Hora</th>
            <th>Processo</th>
            <th>Unidade</th>
            <th>Descrição</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(alerta, index) in alertas" :key="index">
            <td>{{ alerta.data }}</td>
            <td>{{ alerta.processo }}</td>
            <td>{{ alerta.unidade }}</td>
            <td>{{ alerta.descricao }}</td>
          </tr>
          <tr v-if="!alertas || alertas.length === 0">
            <td colspan="4" class="text-center text-muted">Nenhum alerta no momento.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { usePerfilStore } from '../stores/perfil'
import { useProcessosStore } from '../stores/processos'
import { useUnidadesStore } from '../stores/unidades'
import { useAlertasStore } from '../stores/alertas'
import { useRouter } from 'vue-router'

const perfil = usePerfilStore()
const processosStore = useProcessosStore()
const { processos } = storeToRefs(processosStore)
const unidadesStore = useUnidadesStore()
const { unidades } = storeToRefs(unidadesStore)
const alertasStore = useAlertasStore()
const { alertas } = storeToRefs(alertasStore)
const router = useRouter()

// Lógica de Ordenação da Tabela de Processos
const criterio = ref('descricao')
const asc = ref(true)

// A lógica de filtragem e exibição será implementada na Fase 2
const processosFiltrados = computed(() => {
  // Por enquanto, retorna todos os processos
  return processos.value
})

const processosOrdenados = computed(() => {
  return [...processosFiltrados.value].sort((a, b) => {
    let valA = a[criterio.value]
    let valB = b[criterio.value]

    // Tratamento especial para a situação consolidada, que não é um campo direto
    if (criterio.value === 'situacao') {
      valA = consolidarSituacaoProcesso(a)
      valB = consolidarSituacaoProcesso(b)
    }

    if (valA < valB) return asc.value ? -1 : 1
    if (valA > valB) return asc.value ? 1 : -1
    return 0
  })
})

function ordenarPor(campo) {
  if (criterio.value === campo) {
    asc.value = !asc.value
  } else {
    criterio.value = campo
    asc.value = true
  }
}

// Navegação será ajustada na Fase 2
function abrirDetalhesProcesso(processo) {
  // Lógica de navegação condicional (CDU-002) a ser implementada
  router.push(`/processos/${processo.id}/unidades`)
}

// Funções de apoio (serão revisadas/utilizadas nas próximas fases)
function getSituacaoUnidade(sigla, unidades) {
  for (const unidade of unidades) {
    if (unidade.sigla === sigla) return unidade.situacao
    if (unidade.filhas && unidade.filhas.length) {
      const achou = getSituacaoUnidade(sigla, unidade.filhas)
      if (achou) return achou
    }
  }
  return null
}

function consolidarSituacaoProcesso(processo) {
  const participantes = processo.unidades.split(',').map(u => u.trim())
  const situacoes = participantes.map(sigla => getSituacaoUnidade(sigla, unidades.value) || 'Não iniciado')

  if (situacoes.every(s => s === 'Não iniciado')) return 'Criado' // Ajustado conforme CDU
  if (situacoes.every(s => s === 'Finalizado')) return 'Finalizado'
  if (situacoes.some(s => s === 'Em andamento')) return 'Em andamento'
  return 'Em andamento' // Default para outros casos intermediários
}
</script>