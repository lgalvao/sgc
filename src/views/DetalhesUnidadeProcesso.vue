<template>
  <div class="container mt-4">
    <!-- Breadcrumb e navegação -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-0">
          <li class="breadcrumb-item"><a style="cursor:pointer" @click="router.push('/')">Início</a></li>
          <li class="breadcrumb-item"><a style="cursor:pointer" @click="router.push('/unidades')">Unidades</a></li>
          <li aria-current="page" class="breadcrumb-item active">{{ unidade?.sigla }}</li>
        </ol>
      </nav>
      <div>
        <button class="btn btn-secondary me-2" @click="voltar"> Voltar</button>
        <button class="btn btn-primary" @click="irParaAtividadesConhecimentos">
          Atividades e conhecimentos
        </button>
      </div>
    </div>

    <!-- Card de informações da unidade -->
    <div v-if="unidade" class="card mb-4">
      <div class="card-body">
        <h2 class="card-title mb-3">Detalhes da Unidade</h2>
        <p><strong>Sigla:</strong> {{ unidade.sigla }}</p>
        <p><strong>Nome:</strong> {{ unidade.nome || unidade.sigla }}</p>
        <p><strong>Responsável:</strong> {{ atribuicao?.nomeResponsavel || 'Não definido' }}</p>
        <p><strong>Contato:</strong> {{ atribuicao?.contato || 'Não informado' }}</p>
        <p>
          <strong>Situação:</strong>
          <span :class="badgeClass(unidade.situacao)" class="badge">{{ unidade.situacao }}</span>
        </p>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <!-- Cards de ações -->
    <div class="row">
      <div class="col-md-6 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Atribuição temporária</h5>
            <div v-if="atribuicao">
              <p><strong>Responsável:</strong> {{ atribuicao.nomeResponsavel }}</p>
              <p><strong>Data de início:</strong> {{ atribuicao.dataInicio }}</p>
              <p><strong>Data de término:</strong> {{ atribuicao.dataTermino }}</p>
              <button class="btn btn-warning btn-sm me-2" @click="editarAtribuicao">Editar</button>
              <button class="btn btn-danger btn-sm" @click="removerAtribuicao">Remover</button>
            </div>
            <div v-else>
              <button class="btn btn-primary btn-sm" @click="irParaCriarAtribuicao">Criar</button>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-6 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Mapa de Competências</h5>
            <div v-if="mapa">
              <div v-if="mapa.situacao === 'em_andamento'">
                <button class="btn btn-primary btn-sm me-2" @click="editarMapa">Editar</button>
              </div>
              <div v-else-if="mapa.situacao === 'disponivel_validacao'">
                <button class="btn btn-info btn-sm me-2" @click="visualizarMapa">Visualizar</button>
              </div>
              <div v-else>
                <button class="btn btn-success btn-sm me-2" @click="criarMapa">Criar</button>
              </div>
            </div>
            <div v-else>
              <button class="btn btn-success btn-sm me-2" @click="criarMapa">Criar</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '../stores/unidades'
import {useAtribuicaoTemporariaStore} from '../stores/atribuicaoTemporaria'
import {useMapasStore} from '../stores/mapas'

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const atribuicaoStore = useAtribuicaoTemporariaStore()
const mapaStore = useMapasStore()

function buscarUnidade(unidades, sigla) {
  for (const unidade of unidades) {
    if (unidade.sigla === sigla) return unidade
    if (unidade.filhas && unidade.filhas.length) {
      const encontrada = buscarUnidade(unidade.filhas, sigla)
      if (encontrada) return encontrada
    }
  }
  return null
}

const unidade = computed(() => buscarUnidade(unidades.value, sigla.value))
const atribuicao = computed(() => atribuicaoStore.getAtribuicaoPorUnidade(sigla.value))
const mapa = computed(() => mapaStore.getMapaPorUnidade(sigla.value))

function badgeClass(situacao) {
  if (situacao === 'Aguardando' || situacao === 'Em andamento' || situacao === 'Aguardando validação') return 'bg-warning text-dark'
  if (situacao === 'Finalizado' || situacao === 'Validado') return 'bg-success'
  if (situacao === 'Devolvido') return 'bg-danger'
  return 'bg-secondary'
}

function voltar() {
  router.back()
}

function removerAtribuicao() {
  atribuicaoStore.removerAtribuicao(sigla.value)
}

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${sigla.value}/atribuir`})
}

function criarMapa() {
  router.push({path: `/unidade/${sigla.value}/mapa`})
}

function editarMapa() {
  router.push({path: `/unidade/${sigla.value}/mapa`})
}

function visualizarMapa() {
  router.push({path: `/unidade/${sigla.value}/mapa/visualizar`})
}

function irParaAtividadesConhecimentos() {
  // Tenta obter o id do processo da query string ou do localStorage, se não disponível, alerta
  const processoId = route.query.processoId || localStorage.getItem('processoIdAtual')
  if (processoId) {
    router.push(`/processos/${processoId}/unidade/${sigla.value}/atividades`)
  } else {
    alert('ID do processo não encontrado. Acesse a partir do processo no painel.')
  }
}
</script>