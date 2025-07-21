<template>
  <div class="container mt-4">
    <h2>Detalhes de processo</h2>
    <div v-if="feedback" class="alert alert-info mt-3">{{ feedback }}</div>
    <form class="mt-4 col-md-6 col-sm-8 col-12 p-0">
      <div class="mb-3">
        <label for="descricao" class="form-label">Descrição</label>
        <input type="text" class="form-control" id="descricao" placeholder="Descreva o processo" v-model="descricao" />
      </div>

      <div class="mb-3">
        <label for="tipo" class="form-label">Tipo</label>
        <select class="form-select" id="tipo" v-model="tipo">
          <option>Mapeamento</option>
          <option>Revisão</option>
          <option>Diagnóstico</option>
        </select>
      </div>

      <div class="mb-3">
        <label class="form-label">Unidades participantes</label>
        <div class="border rounded p-3">
          <div>
            <template v-for="unidade in unidadesStore.unidades" :key="unidade.sigla">
              <div class="form-check" :style="{ marginLeft: '0px' }">
                <input type="checkbox" class="form-check-input" :id="'chk-' + unidade.sigla" :value="unidade.sigla"
                  :checked="isChecked(unidade.sigla)" @change="() => toggleUnidade(unidade.sigla)" />
                <label class="form-check-label ms-2" :for="'chk-' + unidade.sigla"><strong>{{ unidade.sigla }}</strong></label>
              </div>
              <template v-if="unidade.filhas && unidade.filhas.length">
                <div v-for="filha in unidade.filhas" :key="filha.sigla" class="form-check" :style="{ marginLeft: '20px' }">
                  <input type="checkbox" class="form-check-input" :id="'chk-' + filha.sigla" :value="filha.sigla"
                    :checked="isChecked(filha.sigla)" @change="() => toggleUnidade(filha.sigla)" />
                  <label class="form-check-label ms-2" :for="'chk-' + filha.sigla"><strong>{{ filha.sigla }}</strong></label>
                  <template v-if="filha.filhas && filha.filhas.length">
                    <div v-for="neta in filha.filhas" :key="neta.sigla" class="form-check" :style="{ marginLeft: '40px' }">
                      <input type="checkbox" class="form-check-input" :id="'chk-' + neta.sigla" :value="neta.sigla"
                        :checked="isChecked(neta.sigla)" @change="() => toggleUnidade(neta.sigla)" />
                      <label class="form-check-label ms-2" :for="'chk-' + neta.sigla"><strong>{{ neta.sigla }}</strong></label>
                    </div>
                  </template>
                </div>
              </template>
            </template>
          </div>
        </div>
      </div>

      <div class="mb-3">
        <label for="dataLimite" class="form-label">Data limite</label>
        <input type="date" class="form-control" id="dataLimite" v-model="dataLimite" />
      </div>
      <button type="button" class="btn btn-primary" @click="salvarProcesso">Salvar</button>
      <button type="button" class="btn btn-success ms-2" @click="iniciarProcesso">Iniciar processo</button>
      <router-link to="/painel" class="btn btn-secondary ms-2">Cancelar</router-link>
    </form>
  </div>
</template>

<script setup>
// Formulário fictício para criação de processo
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useProcessosStore } from '../stores/processos'
import { useUnidadesStore } from '../stores/unidades'

const unidadesSelecionadas = ref([])
const descricao = ref('')
const tipo = ref('Mapeamento')
const dataLimite = ref('')
const router = useRouter()
const processosStore = useProcessosStore()
const feedback = ref('')
const unidadesStore = useUnidadesStore()

function limparCampos() {
  descricao.value = ''
  tipo.value = 'Mapeamento'
  dataLimite.value = ''
  unidadesSelecionadas.value = []
}

function formatarDataBR(dataISO) {
  if (!dataISO) return ''
  const [ano, mes, dia] = dataISO.split('-')
  return `${dia}/${mes}/${ano}`
}

function salvarProcesso() {
  if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
    feedback.value = 'Preencha todos os campos e selecione ao menos uma unidade.'
    return
  }
  const novo = {
    id: processosStore.processos.length + 1,
    descricao: descricao.value,
    tipo: tipo.value,
    unidades: unidadesSelecionadas.value.join(', '), // agora só siglas
    dataLimite: formatarDataBR(dataLimite.value),
    situacao: 'Não iniciado'
  }
  processosStore.adicionarProcesso(novo)
  feedback.value = 'Processo salvo com sucesso!'
  setTimeout(() => {
    router.push('/painel')
  }, 1000)
  limparCampos()
}

function iniciarProcesso() {
  if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
    feedback.value = 'Preencha todos os campos e selecione ao menos uma unidade.'
    return
  }
  const novo = {
    id: processosStore.processos.length + 1,
    descricao: descricao.value,
    tipo: tipo.value,
    unidades: unidadesSelecionadas.value.join(', '), // agora só siglas
    dataLimite: formatarDataBR(dataLimite.value),
    situacao: 'Iniciado'
  }
  processosStore.adicionarProcesso(novo)
  feedback.value = 'Processo iniciado! Notificações enviadas às unidades.'
  setTimeout(() => {
    router.push('/painel')
  }, 1200)
  limparCampos()
}

function isChecked(sigla) {
  return unidadesSelecionadas.value.includes(sigla)
}

function toggleUnidade(sigla) {
  const idx = unidadesSelecionadas.value.indexOf(sigla)
  if (idx === -1) {
    unidadesSelecionadas.value.push(sigla)
  } else {
    unidadesSelecionadas.value.splice(idx, 1)
  }
}
</script>