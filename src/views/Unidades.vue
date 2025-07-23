<template>
  <div class="container mt-4">
    <h2>Estrutura Organizacional</h2>
    <div v-if="unidades && unidades.length > 0">
      <TreeTable
        :data="dadosFormatados"
        :columns="colunasTabela"
        title="Unidades"
        @row-click="navigateToUnit"
      />
    </div>
    <div v-else>
      <p>Nenhuma unidade encontrada.</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUnidadesStore } from '../stores/unidades'
import { storeToRefs } from 'pinia'
import TreeTable from '../components/TreeTable.vue'

const router = useRouter()
const unidadesStore = useUnidadesStore()
const { unidades } = storeToRefs(unidadesStore)

const navigateToUnit = (unitId) => {
  router.push(`/unidade/${unitId}`)
}

const colunasTabela = [
  { key: 'nome', label: 'Unidade' },
  { key: 'situacao', label: 'Situação' }
]

// Função para formatar os dados para a árvore
const formatarDadosParaArvore = (dados) => {
  if (!dados) return []
  return dados.map(item => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas) : []
    return {
      id: item.sigla,
      nome: item.sigla + ' - ' + item.nome,
      situacao: item.situacao || 'Não iniciado',
      expanded: true,
      ...(children.length > 0 && { children })
    }
  })
}

const dadosFormatados = computed(() => {
  return formatarDadosParaArvore(unidades.value)
})
</script>
