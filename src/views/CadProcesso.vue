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
                <input
                    :id="`chk-${unidade.sigla}`"
                    :checked="getEstadoSelecao(unidade) === true"
                    :indeterminate.prop="getEstadoSelecao(unidade) === 'indeterminate'"
                    class="form-check-input"
                    type="checkbox"
                    @change="() => toggleUnidade(unidade)"
                />
                <label :for="`chk-${unidade.sigla}`" class="form-check-label ms-2">
                  <strong>{{ unidade.sigla }}</strong> - {{ unidade.nome }}
                </label>
              </div>
              <div v-if="unidade.filhas && unidade.filhas.length" class="ms-4">
                <template v-for="filha in unidade.filhas" :key="filha.sigla">
                  <div class="form-check">
                    <input
                        :id="`chk-${filha.sigla}`"
                        :checked="getEstadoSelecao(filha) === true"
                        :indeterminate.prop="getEstadoSelecao(filha) === 'indeterminate'"
                        class="form-check-input"
                        type="checkbox"
                        @change="() => toggleUnidade(filha)"
                    />
                    <label :for="'chk-' + filha.sigla" class="form-check-label ms-2">
                      <strong>{{ filha.sigla }}</strong> - {{ filha.nome }}
                    </label>
                  </div>
                  <div v-if="filha.filhas && filha.filhas.length" class="ms-4">
                    <div v-for="neta in filha.filhas" :key="neta.sigla" class="form-check">
                      <input
                          :id="'chk-' + neta.sigla"
                          :checked="isChecked(neta.sigla)"
                          class="form-check-input"
                          type="checkbox"
                          @change="() => toggleUnidade(neta)"
                      />
                      <label :for="'chk-' + neta.sigla" class="form-check-label ms-2">
                        <strong>{{ neta.sigla }}</strong> - {{ neta.nome }}
                      </label>
                    </div>
                  </div>
                </template>
              </div>
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

function getTodasSubunidades(unidade) {
  let subunidades = []
  if (unidade.filhas && unidade.filhas.length) {
    unidade.filhas.forEach(filha => {
      subunidades.push(filha.sigla)
      subunidades = [...subunidades, ...getTodasSubunidades(filha)]
    })
  }
  return subunidades
}

function isFolha(unidade) {
  return !unidade.filhas || unidade.filhas.length === 0
}

function isChecked(sigla) {
  return unidadesSelecionadas.value.includes(sigla)
}

function getEstadoSelecao(unidade) {
  if (isFolha(unidade)) {
    return !!isChecked(unidade.sigla)
  }

  const subunidades = getTodasSubunidades(unidade)
  const selecionadas = subunidades.filter(sigla => isChecked(sigla)).length

  if (selecionadas === 0) return false
  if (selecionadas === subunidades.length) return true
  return 'indeterminate'
}

function toggleUnidade(unidade) {
  const todasSubunidades = [unidade.sigla, ...getTodasSubunidades(unidade)]
  const todasEstaoSelecionadas = todasSubunidades.every(sigla => isChecked(sigla))

  if (todasEstaoSelecionadas) {
    // Desseleciona a unidade e todas as subunidades
    unidadesSelecionadas.value = unidadesSelecionadas.value.filter(
        sigla => !todasSubunidades.includes(sigla)
    )
  } else {
    // Seleciona a unidade e todas as subunidades
    todasSubunidades.forEach(sigla => {
      if (!unidadesSelecionadas.value.includes(sigla)) {
        unidadesSelecionadas.value.push(sigla)
      }
    })
  }
}
</script>

<style scoped>
/* Estilo para o checkbox indeterminado */
input[type="checkbox"]:indeterminate {
  background-color: #0d6efd;
  border-color: #0d6efd;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20'%3e%3cpath fill='none' stroke='%23fff' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M6 10h8'/%3e%3c/svg%3e");
}

/* Melhora o espaçamento e a hierarquia visual */
.form-check {
  margin-bottom: 0.25rem;
  padding-left: 1.5em;
}

/* Melhora a visualização da hierarquia */
.ms-4 {
  border-left: 1px dashed #dee2e6;
  padding-left: 1rem;
  margin-left: 0.5rem;
}

/* Estilo para os labels */
.form-check-label {
  cursor: pointer;
  user-select: none;
  padding: 0.25rem 0;
  display: inline-block;
}

/* Ajuste para os checkboxes */
.form-check-input {
  margin-top: 0.25rem;
}
</style>