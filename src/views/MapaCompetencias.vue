<template>
  <div class="container mt-4">
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Edição de Mapa de Competências</h2>
    <div v-if="unidade">
      <div class="mb-3">
        <strong>Unidade:</strong> {{ unidade.sigla }} - {{ unidade.nome || unidade.sigla }}
      </div>
      <div class="mb-4">
        <h5>Atividades</h5>
        <div v-for="atividade in atividades" :key="atividade.id" class="form-check">
          <input class="form-check-input" type="checkbox" :id="'atv-' + atividade.id" v-model="atividadesSelecionadas" :value="atividade.id">
          <label class="form-check-label" :for="'atv-' + atividade.id">
            {{ atividade.descricao }}
          </label>
        </div>
      </div>
      <div class="mb-4">
        <h5>Descrição da Competência</h5>
        <form @submit.prevent="adicionarCompetencia">
          <div class="mb-2">
            <textarea v-model="novaCompetencia.descricao" class="form-control" placeholder="Descreva a competência" rows="3"></textarea>
          </div>
          <button class="btn btn-primary" type="submit" :disabled="atividadesSelecionadas.length === 0 || !novaCompetencia.descricao.trim()">Adicionar competência</button>
        </form>
      </div>
      <div class="mb-4">
        <h5>Competências cadastradas</h5>
        <div v-if="competencias.length === 0" class="text-muted">Nenhuma competência cadastrada ainda.</div>
        <div v-for="comp in competencias" :key="comp.id" class="card mb-2">
          <div class="card-body">
            <strong> {{ comp.descricao }}</strong><br>
            <ul>
              <li v-for="atvId in comp.atividadesAssociadas" :key="atvId">
                {{ descricaoAtividade(atvId) }}
              </li>
            </ul>
          </div>
        </div>
      </div>
      <button class="btn btn-success" @click="finalizarEdicao" :disabled="competencias.length === 0">Gerar Mapa</button>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useMapasStore } from '../stores/mapas'
import { useUnidadesStore } from '../stores/unidades'
import { useAtividadesConhecimentosStore } from '../stores/atividadesConhecimentos'

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.query.sigla || route.params.sigla)
const unidadesStore = useUnidadesStore()
const mapaStore = useMapasStore()
const atividadesStore = useAtividadesConhecimentosStore()
const { unidades } = storeToRefs(unidadesStore)
const { atividadesPorUnidade } = storeToRefs(atividadesStore)

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
const atividades = computed(() => atividadesPorUnidade.value[sigla.value] || [])
const mapa = computed(() => mapaStore.getMapaPorUnidade(sigla.value))
const competencias = ref(mapa.value ? [...mapa.value.competencias] : [])
const atividadesSelecionadas = ref([])
const novaCompetencia = ref({ descricao: '' })

function descricaoAtividade(id) {
  const atv = atividades.value.find(a => a.id === id)
  return atv ? atv.descricao : 'Atividade não encontrada'
}

function adicionarCompetencia() {
  if (!novaCompetencia.value.descricao.trim() || atividadesSelecionadas.value.length === 0) return
  competencias.value.push({
    id: Date.now(),
    descricao: novaCompetencia.value.descricao,
    atividadesAssociadas: [...atividadesSelecionadas.value]
  })
  novaCompetencia.value.descricao = ''
  atividadesSelecionadas.value = []
}

function finalizarEdicao() {
  mapaStore.editarMapa(mapa.value.id, { competencias: competencias.value })
  router.push({ path: '/finalizacao-mapa', query: { sigla: sigla.value } })
}

function voltar() {
  router.back()
}
</script> 