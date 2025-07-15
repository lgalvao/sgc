<template>
  <div class="container mt-4">
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Detalhes do Processo</h2>
    <div v-if="processo">
      <div class="mb-3">
        <strong>Descrição:</strong> {{ processo.descricao }}<br>
        <strong>Tipo:</strong> {{ processo.tipo }}<br>
        <strong>Data limite:</strong> {{ processo.dataLimite }}<br>
        <strong>Situação:</strong> {{ processo.situacao }}<br>
      </div>
      <h4 class="mt-4">Unidades participantes</h4>
      <div class="list-group mt-2">
        <TreeNode
          v-for="unidade in participantesHierarquia"
          :key="unidade.sigla"
          :unidade="unidade"
          :abertas="abertas"
          @abrir="abrirAtividadesConhecimentos"
        />
      </div>
    </div>
    <div v-else>
      <p>Processo não encontrado.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useProcessosStore } from '../stores/processos'
import { useUnidadesStore } from '../stores/unidades'
import TreeNode from '../components/TreeNode.vue'

const route = useRoute()
const router = useRouter()
const processosStore = useProcessosStore()
const { processos } = storeToRefs(processosStore)
const unidadesStore = useUnidadesStore()
const { unidades } = storeToRefs(unidadesStore)
const abertas = ref({})

const processoId = computed(() => Number(route.params.id))
const processo = computed(() => processos.value.find(p => p.id === processoId.value))
const unidadesParticipantes = computed(() => {
  if (!processo.value) return []
  return processo.value.unidades.split(',').map(u => u.trim())
})

function consolidarSituacaoUnidade(unidade) {
  if (!unidade.filhas || unidade.filhas.length === 0) return unidade.situacao || 'Não iniciado'
  const situacoes = unidade.filhas.map(consolidarSituacaoUnidade)
  if (situacoes.every(s => s === 'Finalizado')) return 'Finalizado'
  if (situacoes.some(s => s === 'Em andamento')) return 'Em andamento'
  return 'Não iniciado'
}

function filtrarHierarquiaPorParticipantes(unidades, participantes) {
  // Retorna apenas os nós da hierarquia que são participantes ou têm filhos participantes
  return unidades
    .map(unidade => {
      let filhasFiltradas = []
      if (unidade.filhas && unidade.filhas.length) {
        filhasFiltradas = filtrarHierarquiaPorParticipantes(unidade.filhas, participantes)
      }
      const isParticipante = participantes.includes(unidade.sigla)
      if (isParticipante || filhasFiltradas.length > 0) {
        return {
          ...unidade,
          situacao: consolidarSituacaoUnidade({ ...unidade, filhas: filhasFiltradas }),
          filhas: filhasFiltradas
        }
      }
      return null
    })
    .filter(Boolean)
}

const participantesHierarquia = computed(() => filtrarHierarquiaPorParticipantes(unidades.value, unidadesParticipantes.value))

function abrirAtividadesConhecimentos(sigla) {
  // router.push(`/processos/${processoId.value}/unidade/${sigla}/atividades`)
  router.push({ path: `/unidade/${sigla}`, query: { processoId: processoId.value } })
}
function expandirTodos(unidadesArr) {
  for (const unidade of unidadesArr) {
    if (unidade.filhas && unidade.filhas.length) {
      abertas.value[unidade.sigla] = true
      expandirTodos(unidade.filhas)
    }
  }
}
onMounted(() => {
  expandirTodos(participantesHierarquia.value)
})

function voltar() {
  router.back()
}
</script>

<style scoped>
.unidade-folha {
  cursor: pointer;
  transition: background 0.2s;
}
.unidade-folha:hover {
  background: #e9ecef;
}
</style> 