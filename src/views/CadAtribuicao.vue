<template>
  <div class="container mt-4">
    <h2>Criar atribuição temporária</h2>
    <div class="card mb-4 mt-4">
      <div class="card-body">
        <h5 class="card-title mb-3">{{ unidade?.sigla }} - {{ unidade?.nome }}</h5>
        <form @submit.prevent="criarAtribuicao">
          <div class="mb-3">
            <label class="form-label" for="servidor">Servidor</label>
            <select id="servidor" v-model="servidorSelecionado" class="form-select" data-testid="select-servidor"
                    required>
              <option :value="null" disabled>Selecione um servidor</option>
              <option v-for="servidor in servidoresElegiveis" :key="servidor.id" :value="servidor.id">
                {{ servidor.nome }}
              </option>
            </select>
            <div v-if="erroServidor" class="text-danger small">{{ erroServidor }}</div>
          </div>

          <div class="mb-3">
            <label class="form-label" for="dataTermino">Data de término</label>
            <input id="dataTermino" v-model="dataTermino" class="form-control" data-testid="input-data-termino" required
                   type="date"/>
          </div>

          <div class="mb-3">
            <label class="form-label" for="justificativa">Justificativa</label>
            <textarea id="justificativa" v-model="justificativa" class="form-control" data-testid="textarea-justificativa"
                      required></textarea>
          </div>
          <button class="btn btn-primary" data-testid="btn-criar-atribuicao" type="submit">Criar</button>
          <button class="btn btn-secondary ms-2" data-testid="btn-cancelar-atribuicao" type="button"
                  @click="router.push(`/unidade/${sigla}`)">Cancelar
          </button>
        </form>

        <div v-if="sucesso" class="alert alert-success mt-3">Atribuição criada com sucesso!</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicaoTemporaria'
import servidoresMock from '@/mocks/servidores.json'
import {AtribuicaoTemporaria, Servidor, Unidade} from '@/types/tipos'

const props = defineProps<{ sigla: string }>()

const router = useRouter()
const sigla = computed(() => props.sigla)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const atribuicaoStore = useAtribuicaoTemporariaStore()

function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | null {
  for (const unidade of unidades) {
    if (unidade.sigla === sigla) return unidade
    if (unidade.filhas && unidade.filhas.length) {
      const encontrada = buscarUnidade(unidade.filhas, sigla)
      if (encontrada) return encontrada
    }
  }
  return null
}

const unidade = computed<Unidade | null>(() => buscarUnidade(unidades.value as Unidade[], sigla.value))
const atribuicoes = computed<AtribuicaoTemporaria[]>(() =>
    atribuicaoStore.atribuicoes
        ? atribuicaoStore.atribuicoes.filter((a: AtribuicaoTemporaria) => a.unidade === sigla.value)
        : []
)

const servidorSelecionado = ref<number | null>(null)
const dataTermino = ref("")
const justificativa = ref("")
const sucesso = ref(false)
const erroServidor = ref("")

const servidores = ref<Servidor[]>(servidoresMock as Servidor[])

const servidoresDaUnidade = computed<Servidor[]>(() => {
  return servidores.value.filter(s => s.unidade === sigla.value)
})

const servidoresElegiveis = computed<Servidor[]>(() => {
  const titularId = unidade.value?.titular
  return servidoresDaUnidade.value.filter(servidor => {
    const jaTemAtribuicao = atribuicoes.value.some(a => a.idServidor === servidor.id)
    return servidor.id !== titularId && !jaTemAtribuicao
  })
})

function criarAtribuicao() {
  erroServidor.value = ""
  if (!servidorSelecionado.value) {
    erroServidor.value = "Selecione um servidor elegível."
    return
  }

  if (atribuicoes.value.some(a => a.idServidor === servidorSelecionado.value)) {
    erroServidor.value = "Este servidor já possui atribuição temporária nesta unidade."
    return
  }
  atribuicaoStore.criarAtribuicao({
    unidade: sigla.value,
    servidorId: servidorSelecionado.value,
    dataInicio: new Date(),
    dataTermino: new Date(dataTermino.value),
    justificativa: justificativa.value
  })
  sucesso.value = true
  setTimeout(() => {
    router.push(`/unidade/${sigla.value}`)
  }, 1200)
}
</script>