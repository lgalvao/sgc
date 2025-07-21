<template>
  <div class="container mt-4">
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Atividades e Conhecimentos</h2>
    <div class="mb-3"><strong>Unidade:</strong> {{ siglaUnidade }}</div>

    <!-- Adicionar nova atividade -->
    <form class="row g-2 align-items-center mb-4" @submit.prevent="adicionarAtividade">
      <div class="col">
        <input v-model="novaAtividade" class="form-control" placeholder="Descrição da nova atividade" type="text"/>
      </div>
      <div class="col-auto">
        <button class="btn btn-primary" type="submit">Adicionar Atividade</button>
      </div>
    </form>

    <!-- Lista de atividades -->
    <div v-for="(atividade, idx) in atividades" :key="atividade.id" class="card mb-3">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-center">
          <strong>{{ atividade.descricao }}</strong>
          <button class="btn btn-sm btn-outline-danger" @click="removerAtividade(idx)">Remover</button>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div v-for="(conhecimento, cidx) in atividade.conhecimentos" :key="conhecimento.id"
               class="d-flex align-items-center mb-2">
            <span>{{ conhecimento.descricao }}</span>
            <button class="btn btn-sm btn-link text-danger ms-2" @click="removerConhecimento(idx, cidx)">Remover
            </button>
          </div>
          <form class="row g-2 align-items-center" @submit.prevent="adicionarConhecimento(idx)">
            <div class="col">
              <input v-model="atividade.novoConhecimento" class="form-control form-control-sm" placeholder="Novo conhecimento"
                     type="text"/>
            </div>
            <div class="col-auto">
              <button class="btn btn-sm btn-outline-primary" type="submit">Adicionar Conhecimento</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useAtividadesConhecimentosStore} from '../stores/atividadesConhecimentos'
import {useUnidadesStore} from '../stores/unidades'

const route = useRoute()
const unidadeId = computed(() => route.params.unidadeId)
const store = useAtividadesConhecimentosStore()
const {atividadesPorUnidade} = storeToRefs(store)
const unidadesStore = useUnidadesStore()

function buscarSigla(unidades, sigla) {
  for (const unidade of unidades) {
    if (unidade.sigla === sigla) return unidade.sigla
    if (unidade.filhas && unidade.filhas.length) {
      const encontrada = buscarSigla(unidade.filhas, sigla)
      if (encontrada) return encontrada
    }
  }
  return sigla // fallback
}

const siglaUnidade = computed(() => buscarSigla(unidadesStore.unidades, unidadeId.value))

let idAtividade = 1
let idConhecimento = 1
const novaAtividade = ref('')

const atividades = computed({
  get: () => atividadesPorUnidade.value[unidadeId.value] || [],
  set: (val) => store.setAtividades(unidadeId.value, val)
})

function adicionarAtividade() {
  if (novaAtividade.value.trim()) {
    store.adicionarAtividade(unidadeId.value, {
      id: idAtividade++,
      descricao: novaAtividade.value,
      conhecimentos: [],
      novoConhecimento: ''
    })
    novaAtividade.value = ''
  }
}

function removerAtividade(idx) {
  store.removerAtividade(unidadeId.value, idx)
}

function adicionarConhecimento(idx) {
  const atividade = atividades.value[idx]
  if (atividade.novoConhecimento && atividade.novoConhecimento.trim()) {
    store.adicionarConhecimento(unidadeId.value, idx, {
      id: idConhecimento++,
      descricao: atividade.novoConhecimento
    })
    atividade.novoConhecimento = ''
  }
}

function removerConhecimento(idx, cidx) {
  store.removerConhecimento(unidadeId.value, idx, cidx)
}

const router = useRouter()

function voltar() {
  router.back()
}

onMounted(() => {
  // Se a unidade for SESEL e ainda não estiver carregada, inicializa do mock (já feito no store)
  if (unidadeId.value === 'SESEL' && !atividadesPorUnidade.value.SESEL) {
    store.setAtividades('SESEL', store.atividadesPorUnidade.SESEL)
  }
})
</script> 