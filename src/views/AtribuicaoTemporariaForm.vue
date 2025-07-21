<template>
  <div class="container mt-4">
    <h2>Criar Atribuição Temporária</h2>
    <div v-if="unidade">
      <div class="card mb-4">
        <div class="card-body">
          <h5 class="card-title">Unidade: {{ unidade.nome }} ({{ unidade.sigla }})</h5>
          <form @submit.prevent="criarAtribuicao">
            <div class="mb-3">
              <label class="form-label" for="servidor">Servidor</label>
              <select id="servidor" v-model="servidorSelecionado" class="form-select" required>
                <option disabled value="">Selecione um servidor</option>
                <option v-for="servidor in servidoresElegiveis" :key="servidor.id" :value="servidor.id">
                  {{ servidor.nome }}
                </option>
              </select>
              <div v-if="erroServidor" class="text-danger small">{{ erroServidor }}</div>
            </div>

            <div class="mb-3">
              <label class="form-label" for="dataTermino">Data de término</label>
              <input id="dataTermino" v-model="dataTermino" class="form-control" required type="date"/>
            </div>

            <div class="mb-3">
              <label class="form-label" for="justificativa">Justificativa</label>
              <textarea id="justificativa" v-model="justificativa" class="form-control" required></textarea>
            </div>
            <button class="btn btn-primary" type="submit">Criar atribuição</button>
            <button class="btn btn-secondary ms-2" type="button" @click="voltar">Cancelar</button>
          </form>

          <div v-if="sucesso" class="alert alert-success mt-3">Atribuição criada com sucesso!</div>
        </div>
      </div>
    </div>

    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '../stores/unidades'
import {useAtribuicaoTemporariaStore} from '../stores/atribuicaoTemporaria'
import servidoresMock from '../mocks/servidores.json'

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const atribuicaoStore = useAtribuicaoTemporariaStore()

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
const atribuicoes = computed(() =>
    atribuicaoStore.atribuicoes
        ? atribuicaoStore.atribuicoes.filter(a => a.unidadeSigla === sigla.value)
        : []
)

const servidorSelecionado = ref("")
const dataTermino = ref("")
const justificativa = ref("")
const sucesso = ref(false)
const erroServidor = ref("")

const servidores = ref(servidoresMock)

const servidoresDaUnidade = computed(() => {
  // Apenas servidores da unidade selecionada
  return servidores.value.filter(s => s.unidade === sigla.value)
})

const servidoresElegiveis = computed(() => {
  // Não pode já ter atribuição para esta unidade nem ser o titular da unidade
  const titularId = unidade.value?.titular
  return servidoresDaUnidade.value.filter(servidor => {
    const jaTemAtribuicao = atribuicoes.value.some(a => a.servidorId === servidor.id)
    return servidor.id !== titularId && !jaTemAtribuicao
  })
})

function criarAtribuicao() {
  erroServidor.value = ""
  if (!servidorSelecionado.value) {
    erroServidor.value = "Selecione um servidor elegível."
    return
  }
  // Regra: não permitir atribuição duplicada para a mesma unidade
  if (atribuicoes.value.some(a => a.servidorId === servidorSelecionado.value)) {
    erroServidor.value = "Este servidor já possui atribuição temporária nesta unidade."
    return
  }
  atribuicaoStore.criarAtribuicao({
    unidadeSigla: sigla.value,
    servidorId: servidorSelecionado.value,
    dataTermino: dataTermino.value,
    justificativa: justificativa.value
  })
  sucesso.value = true
  setTimeout(() => {
    router.push(`/unidade/${sigla.value}`)
  }, 1200)
}

function voltar() {
  router.push(`/unidade/${sigla.value}`)
}
</script> 