<template>
  <div class="container mt-4">
    <button class="btn btn-primary mb-3 me-2" @click="irParaAtividadesConhecimentos">Atividades e Conhecimentos</button>
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Detalhes da Unidade</h2>
    <div v-if="unidade">
      <div class="mb-3">
        <strong>Sigla:</strong> {{ unidade.sigla }}<br>
        <strong>Nome:</strong> {{ unidade.nome || unidade.sigla }}<br>
        <strong>Responsável:</strong> {{ atribuicao?.nomeResponsavel || 'Não definido' }}<br>
        <strong>Contato:</strong> {{ atribuicao?.contato || 'Não informado' }}<br>
        <strong>Situação:</strong> <span class="badge" :class="badgeClass(unidade.situacao)">{{ unidade.situacao }}</span><br>
      </div>
      <div class="mb-4">
        <h5>Atribuição Temporária</h5>
        <div v-if="atribuicao">
          <p><strong>Responsável:</strong> {{ atribuicao.nomeResponsavel }}</p>
          <p><strong>Data de início:</strong> {{ atribuicao.dataInicio }}</p>
          <p><strong>Data de término:</strong> {{ atribuicao.dataTermino }}</p>
          <button class="btn btn-warning btn-sm me-2" @click="editarAtribuicao">Editar</button>
          <button class="btn btn-danger btn-sm" @click="removerAtribuicao">Remover</button>
        </div>
        <div v-else>
          <button class="btn btn-primary btn-sm" @click="novaAtribuicao">Nova atribuição temporária</button>
        </div>
      </div>
      <div class="mb-4">
        <h5>Mapa de Competências</h5>
        <div v-if="mapa">
          <div v-if="mapa.status === 'em_andamento'">
            <button class="btn btn-primary btn-sm me-2" @click="editarMapa">Editar mapa</button>
          </div>
          <div v-else-if="mapa.status === 'disponivel_validacao'">
            <button class="btn btn-info btn-sm me-2" @click="visualizarMapa">Visualizar mapa</button>
          </div>
          <div v-else>
            <button class="btn btn-success btn-sm me-2" @click="criarMapa">Criar mapa</button>
          </div>
        </div>
        <div v-else>
          <button class="btn btn-success btn-sm me-2" @click="criarMapa">Criar mapa</button>
        </div>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>
    <!-- Modais ou formulários para editar/criar atribuição/mapa podem ser adicionados aqui -->
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUnidadesStore } from '../stores/unidades'
import { useAtribuicaoTemporariaStore } from '../stores/atribuicaoTemporaria'
import { useMapasStore } from '../stores/mapas'

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const unidadesStore = useUnidadesStore()
const { unidades } = storeToRefs(unidadesStore)
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

function badgeClass(status) {
  if (status === 'Aguardando' || status === 'Em andamento' || status === 'Aguardando validação') return 'bg-warning text-dark'
  if (status === 'Finalizado' || status === 'Validado') return 'bg-success'
  if (status === 'Devolvido') return 'bg-danger'
  return 'bg-secondary'
}

function voltar() {
  router.back()
}
function editarAtribuicao() {
  // abrir modal ou navegação para edição
}
function removerAtribuicao() {
  atribuicaoStore.removerAtribuicao(sigla.value)
}
function novaAtribuicao() {
  // abrir modal ou navegação para criar nova atribuição
}
function criarMapa() {
  // abrir modal ou navegação para criar novo mapa
}
function editarMapa() {
  // abrir modal ou navegação para editar mapa
}
function visualizarMapa() {
  // abrir modal ou navegação para visualizar mapa
}

function irParaAtividadesConhecimentos() {
  // Tenta obter o id do processo da query string ou do localStorage, se não disponível, alerta
  const processoId = route.query.processoId || localStorage.getItem('processoIdAtual')
  if (processoId) {
    router.push(`/processos/${processoId}/unidade/${sigla.value}/atividades`)
  } else {
    alert('ID do processo não encontrado. Acesse a partir do painel do processo.')
  }
}
</script> 