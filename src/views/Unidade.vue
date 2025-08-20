<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <button v-if="perfilStore.perfilSelecionado === 'ADMIN'" class="btn btn-outline-primary"
                data-testid="btn-criar-atribuicao" @click="irParaCriarAtribuicao">
          Criar atribuição
        </button>
      </div>
    </div>

    <div v-if="unidade" class="card mb-4">
      <div class="card-body">
        <h2 class="display-6 mb-3">{{ unidade.sigla }} - {{ unidade.nome }}</h2>
        <p><strong>Responsável:</strong> {{ responsavel }}</p>
        <p><strong>Contato:</strong> {{ responsavelEmail }}</p>
        <button v-if="mapaVigente" class="btn btn-info btn-sm" @click="visualizarMapa">Visualizar Mapa</button>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <div class="row mt-4">
      <div class="col-md-6 mb-4">
        <div class="card h-100 cursor-pointer" data-testid="card-atividades-conhecimentos"
             @click="navegarParaAtividades">
          <div class="card-body d-flex flex-column justify-content-between">
            <h5 class="card-title">Atividades e conhecimentos</h5>
            <p class="card-text text-muted">Gerencie as atividades e conhecimentos da unidade.</p>
          </div>
        </div>
      </div>
      <div class="col-md-6 mb-4">
        <div class="card h-100 cursor-pointer" @click="visualizarMapa">
          <div class="card-body d-flex flex-column justify-content-between">
            <h5 class="card-title">Mapa de Competências</h5>
            <p class="card-text text-muted">Visualize o mapa de competências da unidade.</p>
          </div>
        </div>
      </div>
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

<script lang="ts" setup>
import {computed} from 'vue'
import {useRouter} from 'vue-router'
import {useUnidadesStore} from '@/stores/unidades.js'
import {usePerfilStore} from '@/stores/perfil.js'
import {useServidoresStore} from '@/stores/servidores.js'
import {useMapasStore} from '@/stores/mapas.js'
import TreeTable from '../components/TreeTable.vue'
import {Mapa, Unidade} from '@/types/tipos';

const props = defineProps<{ siglaUnidade: string }>();

const router = useRouter()
const sigla = computed(() => props.siglaUnidade)
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

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${sigla.value}/atribuicao`})
}

const dadosFormatadosSubordinadas = computed(() => {
  if (!unidade.value || !unidade.value.filhas || unidade.value.filhas.length === 0) return []
  return formatarDadosParaArvore(unidade.value.filhas)
})

const colunasTabela = [{key: 'nome', label: 'Unidade'}]

interface UnidadeFormatada {
  id: string;
  nome: string;
  expanded: boolean;
  children?: UnidadeFormatada[];
}

function formatarDadosParaArvore(dados: Unidade[]): UnidadeFormatada[] {
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
  if (item && typeof item.id === 'string') router.push({path: `/unidade/${item.id}`});
}

function navegarParaAtividades() {
  if (mapaVigente.value) {
    router.push({
      name: 'SubprocessoCadastro',
      params: {
        idProcesso: mapaVigente.value.idProcesso,
        siglaUnidade: sigla.value
      }
    });
  } else {
    // Tratar o caso onde não há mapa vigente, talvez com um alerta ou desabilitando o card
    console.warn('Não há mapa vigente para navegar para atividades.');
  }
}

function visualizarMapa() {
  if (mapaVigente.value) {
    router.push({
      name: 'SubprocessoVisMapa',
      params: {
        idProcesso: mapaVigente.value.idProcesso,
        siglaUnidade: sigla.value
      }
    });
  }
}

</script>