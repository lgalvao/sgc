<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <button class="btn btn-outline-secondary me-2" @click="voltar"> Voltar</button>
        <button v-if="perfilStore.perfilSelecionado === 'ADMIN'" class="btn btn-outline-primary"
                @click="irParaCriarAtribuicao">
          Criar atribuição
        </button>
      </div>
    </div>

    <div v-if="unidade" class="card mb-4">
      <div class="card-body">
        <h2 class="card-title mb-3">{{ unidade.sigla }} - {{ unidade.nome }}</h2>
        <p><strong>Responsável:</strong> {{ responsavel }}</p>
        <p><strong>Contato:</strong> {{ responsavelEmail }}</p>
        <p><strong>Mapa vigente:</strong> {{ mapaVigente ? mapaVigente.situacao : 'Não disponível' }}</p>
        <button v-if="mapaVigente" class="btn btn-info btn-sm" @click="visualizarMapa">Visualizar Mapa</button>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <div v-if="unidade && unidade.filhas && unidade.filhas.length > 0" class="mt-5">
      <TreeTable
          :columns="colunasTabela"
          :data="dadosFormatadosSubordinadas"
          :hideHeaders="true"
          title="Unidades Subordinadas"
          @row-click="navegarParaUnidadeSubordinada"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useUnidadesStore} from '@/stores/unidades.js'
import {usePerfilStore} from '@/stores/perfil.js'
import {useServidoresStore} from '@/stores/servidores.js'
import {useMapasStore} from '@/stores/mapas.js'
import TreeTable from '../components/TreeTable.vue'
import {Mapa, Unidade} from '@/types/tipos';

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla as string)
const unidadesStore = useUnidadesStore()
const perfilStore = usePerfilStore()
const servidoresStore = useServidoresStore()
const mapasStore = useMapasStore()

const unidade = computed<Unidade | null>(() => unidadesStore.pesquisarUnidade(sigla.value) || null)
const responsavel = computed<string>(() => {
  if (unidade.value && unidade.value.titular) {
    return servidoresStore.getServidorById(unidade.value.titular)?.nome || 'Não definido'
  }
  return 'Não definido'
})

const responsavelEmail = computed<string>(() => {
  if (unidade.value && unidade.value.titular) {
    return servidoresStore.getServidorById(unidade.value.titular)?.email || 'Não informado'
  }
  return 'Não informado'
})

const mapaVigente = computed<Mapa | undefined>(() => mapasStore.getMapaVigentePorUnidade(sigla.value))

function voltar() {
  router.back()
}

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${sigla.value}/atribuicao`})
}

const dadosFormatadosSubordinadas = computed(() => {
  if (!unidade.value || !unidade.value.filhas || unidade.value.filhas.length === 0) return []
  return formatarDadosParaArvore(unidade.value.filhas)
})

const colunasTabela = [
  {key: 'nome', label: 'Unidade'}
]

interface FormattedUnidade {
  id: string;
  nome: string;
  expanded: boolean;
  children?: FormattedUnidade[];
}

function formatarDadosParaArvore(dados: Unidade[]): FormattedUnidade[] {
  if (!dados) return []
  return dados.map(item => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas) : []
    return {
      id: item.sigla,
      nome: item.sigla + ' - ' + item.nome,
      expanded: true,
      ...(children.length > 0 && {children})
    }
  })
}

function navegarParaUnidadeSubordinada(item: { id: unknown }) {
  if (item && typeof item.id === 'string') {
    router.push({path: `/unidade/${item.id}`});
  }
}

function visualizarMapa() {
  router.push({path: `/visualizacao-mapa`, query: {sigla: sigla.value}})
}

</script>