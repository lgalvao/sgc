<template>
  <div class="container mt-4">
    <h2>Atividades e Conhecimentos</h2>
    <!-- Adicionar nova atividade -->
    <form class="row g-2 align-items-center mb-4" @submit.prevent="adicionarAtividade">
      <div class="col">
        <input v-model="novaAtividade" type="text" class="form-control" placeholder="Descrição da nova atividade" />
      </div>
      <div class="col-auto">
        <button type="submit" class="btn btn-primary">Adicionar Atividade</button>
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
          <div v-for="(conhecimento, cidx) in atividade.conhecimentos" :key="conhecimento.id" class="d-flex align-items-center mb-2">
            <span>{{ conhecimento.descricao }}</span>
            <button class="btn btn-sm btn-link text-danger ms-2" @click="removerConhecimento(idx, cidx)">Remover</button>
          </div>
          <form class="row g-2 align-items-center" @submit.prevent="adicionarConhecimento(idx)">
            <div class="col">
              <input v-model="atividade.novoConhecimento" type="text" class="form-control form-control-sm" placeholder="Novo conhecimento" />
            </div>
            <div class="col-auto">
              <button type="submit" class="btn btn-sm btn-outline-primary">Adicionar Conhecimento</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import atividadesConhecimentosSESEL from '../mocks/atividades_conhecimentos_SESEL.json'

const route = useRoute()
let idAtividade = 1
let idConhecimento = 1
const atividades = ref([])
const novaAtividade = ref('')

function adicionarAtividade() {
  if (novaAtividade.value.trim()) {
    atividades.value.push({
      id: idAtividade++,
      descricao: novaAtividade.value,
      conhecimentos: [],
      novoConhecimento: ''
    })
    novaAtividade.value = ''
  }
}

function removerAtividade(idx) {
  atividades.value.splice(idx, 1)
}

function adicionarConhecimento(idx) {
  const atividade = atividades.value[idx]
  if (atividade.novoConhecimento && atividade.novoConhecimento.trim()) {
    atividade.conhecimentos.push({
      id: idConhecimento++,
      descricao: atividade.novoConhecimento
    })
    atividade.novoConhecimento = ''
  }
}

function removerConhecimento(idx, cidx) {
  atividades.value[idx].conhecimentos.splice(cidx, 1)
}

onMounted(() => {
  // Se a unidade for SESEL, carregar dados do JSON
  if (route.params.unidadeId === 'SESEL') {
    atividades.value = atividadesConhecimentosSESEL.map(a => ({
      id: idAtividade++,
      descricao: a.descricao,
      conhecimentos: a.conhecimentos.map(desc => ({ id: idConhecimento++, descricao: desc })),
      novoConhecimento: ''
    }))
  }
})
</script> 