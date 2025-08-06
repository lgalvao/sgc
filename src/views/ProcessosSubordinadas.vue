<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <button class="btn btn-outline-secondary me-2" @click="voltar"> Voltar</button>
      </div>
    </div>

    <div v-if="unidade" class="card mb-4">
      <div class="card-body">
        <span class="badge text-bg-secondary mb-2" style="border-radius: 0">Unidade no Processo</span>
        <h2 class="card-title mb-3">{{ unidade.sigla }} - {{ unidade.nome }}</h2>
        <p><strong>Processo:</strong> {{ processoAtual?.descricao }}</p>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <div v-if="unidade && unidade.filhas && unidade.filhas.length > 0" class="mt-5">
      <TreeTable
          :data="dadosFormatadosSubordinadas"
          :columns="colunasTabela"
          title="Unidades Subordinadas Participantes"
          :hideHeaders="true"
          @row-click="navigateToSubordinateUnit"
      />
    </div>
    <div v-else>
      <p>Não há unidades subordinadas participantes neste processo.</p>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '../stores/unidades'
import {useProcessosStore} from '../stores/processos'
import TreeTable from '../components/TreeTable.vue'

const route = useRoute()
const router = useRouter()

const siglaUnidadeAtual = computed(() => route.params.sigla)
const processoId = computed(() => Number(route.params.processoId))

const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()
const {processos} = storeToRefs(processosStore)

const unidade = computed(() => unidadesStore.pesquisarUnidade(siglaUnidadeAtual.value))
const processoAtual = computed(() => processos.value.find(p => p.id === processoId.value))

const colunasTabela = [
  {key: 'nome', label: 'Unidade'},
  {key: 'situacao', label: 'Situação'},
  {key: 'dataLimite', label: 'Data Limite'}
]

const dadosFormatadosSubordinadas = computed(() => {
  if (!unidade.value || !unidade.value.filhas || unidade.value.filhas.length === 0) return []
  return formatarDadosParaArvore(unidade.value.filhas, processoId.value)
})

function formatarData(data) {
  if (!data) return ''
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}

function formatarDadosParaArvore(dados, processoId) {
  if (!dados) return []
  return dados.map(item => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas, processoId) : []
    const processoUnidade = processosStore.getUnidadesDoProcesso(processoId).find(pu => pu.unidadeId === item.sigla);

    return {
      id: item.sigla,
      nome: item.sigla + ' - ' + item.nome,
      situacao: processoUnidade ? processoUnidade.situacao : 'Não participante',
      dataLimite: processoUnidade ? formatarData(processoUnidade.dataLimite) : 'N/A',
      expanded: true,
      ...(children.length > 0 && {children})
    }
  }).filter(item => item.situacao !== 'Não participante' || (item.children && item.children.length > 0)); // Filtra unidades não participantes sem filhos
}

function navigateToSubordinateUnit(item) {
  const processoUnidade = processosStore.getUnidadesDoProcesso(processoId.value).find(pu => pu.unidadeId === item.id);
  if (processoUnidade) {
    // Se a unidade subordinada tem um ProcessoUnidade associado, navega para ProcessoUnidade.vue
    router.push({path: `/processo-unidade/${processoUnidade.id}`, query: {processoId: processoId.value}})
  } else if (item.children && item.children.length > 0) {
    // Se é uma unidade intermediária com filhos, navega para ProcessosSubordinadas.vue para essa unidade
    router.push({path: `/processos/${processoId.value}/unidades/${item.id}`})
  } else {
    // Caso contrário, não faz nada (unidade folha não participante)
    console.log(`Unidade ${item.id} não é participante e não possui subordinadas participantes.`);
  }
}

function voltar() {
  router.back()
}
</script>
