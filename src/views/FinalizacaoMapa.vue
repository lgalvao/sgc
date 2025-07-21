<template>
  <div class="container mt-4">
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Finalização de Mapa de Competências</h2>
    <div v-if="unidade && mapa">
      <div class="mb-3">
        <strong>Unidade:</strong> {{ unidade.sigla }} - {{ unidade.nome }}
      </div>
      <div class="mb-4">
        <h5>Competências cadastradas</h5>
        <div v-for="comp in mapa.competencias" :key="comp.id" class="card mb-2">
          <div class="card-body">
            <strong>{{ comp.descricao }}</strong> <br>
            <div v-if="incluirAtividades">
              <ul>
                <li v-for="atvId in comp.atividadesAssociadas" :key="atvId">
                  {{ descricaoAtividade(atvId) }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      <div class="form-check mb-3">
        <input class="form-check-input" type="checkbox" id="incluirAtividades" v-model="incluirAtividades">
        <label class="form-check-label" for="incluirAtividades">
          Incluir descrição das atividades no mapa gerado
        </label>
      </div>
      <div class="mb-3">
        <label for="dataLimite" class="form-label">Data limite para validação</label>
        <input type="date" id="dataLimite" v-model="dataLimite" class="form-control" />
      </div>
      <button class="btn btn-success" @click="disponibilizarMapa" :disabled="!dataLimite">Disponibilizar</button>
      <div v-if="notificacao" class="alert alert-info mt-4">
        {{ notificacao }}
      </div>
    </div>
    <div v-else>
      <p>Unidade ou mapa não encontrado.</p>
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
const { unidades } = storeToRefs(unidadesStore)
const mapaStore = useMapasStore()
const atividadesStore = useAtividadesConhecimentosStore()
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
const mapa = computed(() => mapaStore.getMapaPorUnidade(sigla.value))
const atividades = computed(() => atividadesPorUnidade.value[sigla.value] || [])
const incluirAtividades = ref(true)
const dataLimite = ref('')
const notificacao = ref('')

function descricaoAtividade(id) {
  const atv = atividades.value.find(a => a.id === id)
  return atv ? atv.descricao : 'Atividade não encontrada'
}

function disponibilizarMapa() {
  mapaStore.editarMapa(mapa.value.id, {
    situacao: 'disponivel_validacao',
    dataDisponibilizacao: new Date().toISOString(),
    dataLimite: dataLimite.value
  })
  notificacao.value = `Notificação: O mapa de competências da unidade ${unidade.value.sigla} foi disponibilizado para validação até ${dataLimite.value}. (Simulação)`
}

function voltar() {
  router.back()
}
</script> 