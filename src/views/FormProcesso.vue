<template>
  <div class="container mt-4">
    <h2>Detalhes de processo</h2>
    <div v-if="feedback" class="alert alert-info mt-3">{{ feedback }}</div>
    <form class="mt-4 col-md-6 col-sm-8 col-12 p-0">
      <div class="mb-3">
        <label class="form-label" for="descricao">Descrição</label>
        <input id="descricao" v-model="descricao" class="form-control" placeholder="Descreva o processo" type="text"/>
      </div>

      <div class="mb-3">
        <label class="form-label" for="tipo">Tipo</label>
        <select id="tipo" v-model="tipo" class="form-select">
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
              <div :style="{ marginLeft: '0px' }" class="form-check">
                <input :id="'chk-' + unidade.sigla" :checked="isChecked(unidade.sigla)" :value="unidade.sigla" class="form-check-input"
                       type="checkbox" @change="() => toggleUnidade(unidade.sigla)"/>
                <label :for="'chk-' + unidade.sigla" class="form-check-label ms-2"><strong>{{ unidade.sigla }}</strong></label>
              </div>

              <template v-if="unidade.filhas && unidade.filhas.length">
                <div v-for="filha in unidade.filhas" :key="filha.sigla" :style="{ marginLeft: '20px' }"
                     class="form-check">
                  <input :id="'chk-' + filha.sigla" :checked="isChecked(filha.sigla)" :value="filha.sigla" class="form-check-input"
                         type="checkbox" @change="() => toggleUnidade(filha.sigla)"/>
                  <label :for="'chk-' + filha.sigla" class="form-check-label ms-2"><strong>{{
                      filha.sigla
                    }}</strong></label>
                  <template v-if="filha.filhas && filha.filhas.length">
                    <div v-for="neta in filha.filhas" :key="neta.sigla" :style="{ marginLeft: '40px' }"
                         class="form-check">
                      <input :id="'chk-' + neta.sigla" :checked="isChecked(neta.sigla)" :value="neta.sigla" class="form-check-input"
                             type="checkbox" @change="() => toggleUnidade(neta.sigla)"/>
                      <label :for="'chk-' + neta.sigla" class="form-check-label ms-2"><strong>{{ neta.sigla }}</strong></label>
                    </div>
                  </template>
                </div>
              </template>
            </template>
          </div>
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label" for="dataLimite">Data limite</label>
        <input id="dataLimite" v-model="dataLimite" class="form-control" type="date"/>
      </div>
      <button class="btn btn-primary" type="button" @click="salvarProcesso">Salvar</button>
      <button class="btn btn-success ms-2" type="button" @click="iniciarProcesso">Iniciar processo</button>
      <router-link class="btn btn-secondary ms-2" to="/painel">Cancelar</router-link>
    </form>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {useProcessosStore} from '../stores/processos'
import {useUnidadesStore} from '../stores/unidades'

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
    unidades: unidadesSelecionadas.value.join(', '),
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