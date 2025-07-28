<template>
  <div class="container mt-4">
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Detalhes do processo</h2>
    <div v-if="processo">
      <div class="mb-3">
        <strong>Descrição:</strong> {{ processo.descricao }}<br>
        <strong>Tipo:</strong> {{ processo.tipo }}<br>
        <strong>Data limite:</strong> {{ processo.dataLimite }}<br>
        <strong>Situação:</strong> {{ processo.situacao }}<br>
      </div>
      <TreeTable
        :data="dadosFormatados"
        :columns="colunasTabela"
        title="Unidades participantes"
        @row-click="abrirDetalhesUnidade"
      />
    </div>
    <div v-else>
      <p>Processo não encontrado.</p>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useProcessosStore } from '../stores/processos'
import { useUnidadesStore } from '../stores/unidades'
import { useAtividadesConhecimentosStore } from '../stores/atividadesConhecimentos'
import TreeTable from '../components/TreeTable.vue'

const route = useRoute()
const router = useRouter()
const processosStore = useProcessosStore()
const { processos } = storeToRefs(processosStore)
const unidadesStore = useUnidadesStore()
const { unidades } = storeToRefs(unidadesStore)
const atividadesConhecimentosStore = useAtividadesConhecimentosStore()
const { atividadesPorUnidade } = storeToRefs(atividadesConhecimentosStore)

const abertas = ref({})

const processoId = computed(() => Number(route.params.id))
const processo = computed(() => processos.value.find(p => p.id === processoId.value))
const unidadesParticipantes = computed(() => {
  if (!processo.value || !processo.value.unidades) return []
  return processo.value.unidades.split(',').map(u => u.trim())
})

function consolidarSituacaoUnidade(unidade) {
  if (!unidade.filhas || unidade.filhas.length === 0) return unidade.situacao || 'Não iniciado'
  const situacoes = unidade.filhas.map(consolidarSituacaoUnidade)
  if (situacoes.every(s => s === 'Finalizado')) return 'Finalizado'
  if (situacoes.some(s => s === 'Em andamento')) return 'Em andamento'
  return 'Não iniciado'
}

// Retorna apenas os nós da hierarquia que são participantes ou têm filhos participantes
function filtrarHierarquiaPorParticipantes(unidades, participantes) {
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
            situacao: consolidarSituacaoUnidade({...unidade, filhas: filhasFiltradas}),
            filhas: filhasFiltradas
          }
        }
        return null
      })
      .filter(Boolean)
}

const participantesHierarquia = computed(() => filtrarHierarquiaPorParticipantes(unidades.value, unidadesParticipantes.value))

const colunasTabela = [
  { key: 'nome', label: 'Unidade' },
  { key: 'situacao', label: 'Situação' }
]

const dadosFormatados = computed(() => {
  return formatarDadosParaArvore(participantesHierarquia.value)
})

function formatarDadosParaArvore(dados) {
  if (!dados) return []
  return dados.map(item => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas) : []
    return {
      id: item.sigla,
      nome: item.sigla + ' - ' + item.nome,
      situacao: item.situacao,
      expanded: true,
      children: children,
      // Garante que a estrutura de filhos seja consistente
      ...(children.length > 0 && { children })
    }
  })
}

function abrirDetalhesUnidade(item) {
  router.push({ path: `/unidade-processo/${item.id}`, query: { processoId: processoId.value } })
}

function gerenciarAtividades() {
  const unidadeSigla = unidadesParticipantes.value;
  if (unidadeSigla) {
    router.push({ path: `/processos/${processoId.value}/unidade/${unidadeSigla}/atividades` });
  } else {
    alert('Não foi possível determinar a unidade para gerenciar atividades.');
  }
}

function voltar() {
  router.back()
}
</script>