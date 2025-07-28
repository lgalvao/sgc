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
        <button class="btn btn-outline-secondary me-2" @click="voltar"> Voltar</button>
        <button class="btn btn-outline-primary" @click="irParaCriarAtribuicao">
          Criar atribuição
        </button>
      </div>
    </div>

    <div v-if="unidade" class="card mb-4">
      <div class="card-body">
        <h2 class="card-title mb-3">{{ unidade.sigla }} - {{ unidade.nome }}</h2>
        <p><strong>Responsável:</strong> {{ unidade?.nomeResponsavel || 'Não definido' }}</p>
        <p><strong>Contato:</strong> {{ unidade?.contato || 'Não informado' }}</p>
        <p><strong>Mapa vigente:</strong> {{ unidade?.contato || 'Não informado' }}</p>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '../stores/unidades'

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)

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

function voltar() {
  router.back()
}

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${sigla.value}/atribuir`})
}

function visualizarMapa() {
  router.push({path: `/unidade/${sigla.value}/mapa/visualizar`})
}

</script>