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
import {usePerfilStore} from '../stores/perfil'

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const perfilStore = usePerfilStore()

const unidade = computed(() => unidadesStore.findUnit(sigla.value))

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