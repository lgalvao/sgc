<template>
  <div class="container mt-4">
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Visualização de Mapa de Competências</h2>
    <div v-if="unidade && mapa">
      <div class="mb-3">
        <strong>Unidade:</strong> {{ unidade.sigla }} - {{ unidade.nome || unidade.sigla }}
      </div>
      <div class="mb-4">
        <h5>Competências cadastradas</h5>
        <div v-if="mapa.competencias.length === 0" class="text-muted">Nenhuma competência cadastrada ainda.</div>
        <div v-for="comp in mapa.competencias" :key="comp.id" class="card mb-2">
          <div class="card-body">
            <strong>Descrição:</strong> {{ comp.descricao }}<br>
            <strong>Atividades associadas:</strong>
            <ul>
              <li v-for="atvId in comp.atividadesAssociadas" :key="atvId">
                {{ descricaoAtividade(atvId) }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <div v-else>
      <p>Unidade ou mapa não encontrado.</p>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useMapasStore} from '../stores/mapas'
import {useUnidadesStore} from '../stores/unidades'


const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const processoId = computed(() => Number(route.query.processoId))
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const mapaStore = useMapasStore()
const atividadesStore = useAtividadesStore()

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
const processoUnidadeId = computed(() => {
  const processo = processosStore.processos.find(p => p.id === processoId.value);
  const processoUnidade = processo?.processosUnidade.find(pu => pu.unidadeId === sigla.value);
  return processoUnidade?.id;
});

const atividades = computed(() => atividadesStore.getAtividadesPorProcessoUnidade(processoUnidadeId.value) || [])

function descricaoAtividade(id) {
  const atv = atividades.value.find(a => a.id === id)
  return atv ? atv.descricao : 'Atividade não encontrada'
}

function voltar() {
  router.back()
}
</script> 