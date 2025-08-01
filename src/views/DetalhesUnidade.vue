<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <button class="btn btn-outline-secondary me-2" @click="voltar"> Voltar</button>
        <button v-if="perfilStore.perfilSelecionado === 'ADMIN'" class="btn btn-outline-primary" @click="irParaCriarAtribuicao">
          Criar atribuição
        </button>
      </div>
    </div>

    <div v-if="unidade" class="card mb-4">
      <div class="card-body">
        <h2 class="card-title mb-3">{{ unidade.sigla }} - {{ unidade.nome }}</h2>
        <p><strong>Responsável:</strong> {{ responsavel }}</p>
        <p><strong>Contato:</strong> {{ unidade?.contato || 'Não informado' }}</p>
        <p><strong>Mapa vigente:</strong> {{ mapaVigente ? mapaVigente.situacao : 'Não disponível' }}</p>
        <button v-if="mapaVigente" class="btn btn-info btn-sm" @click="visualizarMapa">Visualizar Mapa</button>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <div v-if="unidade && unidade.filhas && unidade.filhas.length > 0" class="mt-5">
      <TreeTable
          :data="dadosFormatadosSubordinadas"
          :columns="colunasTabela"
          title="Unidades Subordinadas"
          :hideHeaders="true"
          @row-click="navigateToSubordinateUnit"
      />
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '../stores/unidades'
import {usePerfilStore} from '../stores/perfil'
import {useServidoresStore} from '../stores/servidores'
import {useMapasStore} from '../stores/mapas'
import TreeTable from '../components/TreeTable.vue'

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const unidadesStore = useUnidadesStore()
const perfilStore = usePerfilStore()
const servidoresStore = useServidoresStore()
const mapasStore = useMapasStore()

const unidade = computed(() => unidadesStore.findUnit(sigla.value))
const responsavel = computed(() => {
  if (unidade.value && unidade.value.titular) {
    return servidoresStore.getServidorById(unidade.value.titular)?.nome || 'Não definido'
  }
  return 'Não definido'
})
const mapaVigente = computed(() => mapasStore.getMapaPorUnidade(sigla.value))

function voltar() {
  router.back()
}

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${sigla.value}/atribuir`})
}

const dadosFormatadosSubordinadas = computed(() => {
  if (!unidade.value || !unidade.value.filhas || unidade.value.filhas.length === 0) return []
  return formatarDadosParaArvore(unidade.value.filhas)
})

const colunasTabela = [
  { key: 'nome', label: 'Unidade' }
]

function formatarDadosParaArvore(dados) {
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

function navigateToSubordinateUnit(item) {
  router.push({ path: `/unidade/${item.id}` })
}

</script>