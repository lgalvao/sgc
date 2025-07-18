<template>
  <div class="container mt-4">
    <h2>Criar Atribuição Temporária</h2>
    <div v-if="unidade">
      <div class="card mb-4">
        <div class="card-body">
          <h5 class="card-title">Unidade: {{ unidade.nome }} ({{ unidade.sigla }})</h5>
          <form @submit.prevent="criarAtribuicao">
            <div class="mb-3">
              <label for="servidor" class="form-label">Servidor</label>
              <select v-model="servidorSelecionado" id="servidor" class="form-select" required>
                <option value="" disabled>Selecione um servidor</option>
                <option v-for="servidor in servidoresElegiveis" :key="servidor.id" :value="servidor.id">
                  {{ servidor.nome }}
                </option>
              </select>
              <div v-if="erroServidor" class="text-danger small">{{ erroServidor }}</div>
            </div>
            <div class="mb-3">
              <label for="dataTermino" class="form-label">Data de término</label>
              <input type="date" v-model="dataTermino" id="dataTermino" class="form-control" required />
            </div>
            <div class="mb-3">
              <label for="justificativa" class="form-label">Justificativa</label>
              <textarea v-model="justificativa" id="justificativa" class="form-control" required></textarea>
            </div>
            <button type="submit" class="btn btn-primary">Criar atribuição</button>
            <button type="button" class="btn btn-secondary ms-2" @click="voltar">Cancelar</button>
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
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUnidadesStore } from '../stores/unidades'
import { useAtribuicaoTemporariaStore } from '../stores/atribuicaoTemporaria'
import servidoresMock from '../mocks/servidores.json'

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const unidadesStore = useUnidadesStore()
const { unidades } = storeToRefs(unidadesStore)
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
  // Não pode já ter atribuição para esta unidade
  // Não pode ser o titular da unidade
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