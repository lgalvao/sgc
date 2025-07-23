<template>
  <div class="container mt-4">
    <div class="unidade-cabecalho w-100">
      <span class="unidade-sigla">{{ siglaUnidade }}</span>
      <span class="unidade-nome">{{ nomeUnidade }}</span>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="mb-0">Atividades e Conhecimentos</h2>
      <button class="btn btn-outline-primary" title="Importar" data-bs-toggle="tooltip">
        Importar
      </button>
    </div>

    <!-- Adicionar atividade -->
    <form class="row g-2 align-items-center mb-4" @submit.prevent="adicionarAtividade">
      <div class="col">
        <input v-model="novaAtividade" class="form-control" placeholder="Nova atividade" type="text"/>
      </div>
      <div class="col-auto">
        <button class="btn btn-primary btn-sm" type="submit" title="Adicionar Atividade" data-bs-toggle="tooltip"><i
            class="bi bi-plus"></i></button>
      </div>
    </form>

    <!-- Lista de atividades -->
    <div v-for="(atividade, idx) in atividades" :key="atividade.id" class="card mb-3 atividade-card">
      <div class="card-body py-2">
        <div
            class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card">
          <template v-if="editandoAtividade === idx">
            <input v-model="atividadeEditada" class="form-control me-2 atividade-edicao-input"/>
            <button class="btn btn-sm btn-success me-1 botao-acao" @click="salvarEdicaoAtividade(idx)" title="Salvar"
                    data-bs-toggle="tooltip"><i class="bi bi-save"></i></button>
            <button class="btn btn-sm btn-secondary botao-acao" @click="cancelarEdicaoAtividade" title="Cancelar"
                    data-bs-toggle="tooltip"><i class="bi bi-x"></i></button>
          </template>

          <template v-else>
            <strong class="atividade-descricao">{{ atividade.descricao }}</strong>
            <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao-atividade fade-group">
              <button class="btn btn-sm btn-primary botao-acao"
                      @click="iniciarEdicaoAtividade(idx, atividade.descricao)" title="Editar"
                      data-bs-toggle="tooltip"><i class="bi bi-pencil"></i></button>
              <button class="btn btn-sm btn-danger botao-acao" @click="removerAtividade(idx)" title="Remover"
                      data-bs-toggle="tooltip"><i
                  class="bi bi-trash"></i></button>
            </div>
          </template>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div v-for="(conhecimento, cidx) in atividade.conhecimentos" :key="conhecimento.id"
               class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row">
            <template v-if="editandoConhecimento.idxAtividade === idx && editandoConhecimento.idxConhecimento === cidx">
              <input v-model="conhecimentoEditado" class="form-control form-control-sm me-2" style="max-width: 300px;"/>
              <button class="btn btn-sm btn-success me-1 botao-acao" @click="salvarEdicaoConhecimento(idx, cidx)"
                      title="Salvar"
                      data-bs-toggle="tooltip"><i class="bi bi-save"></i></button>
              <button class="btn btn-sm btn-secondary botao-acao" @click="cancelarEdicaoConhecimento" title="Cancelar"
                      data-bs-toggle="tooltip"><i class="bi bi-x"></i></button>
            </template>
            <template v-else>
              <span>{{ conhecimento.descricao }}</span>
              <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao fade-group">
                <button class="btn btn-sm btn-primary botao-acao"
                        @click="iniciarEdicaoConhecimento(idx, cidx, conhecimento.descricao)"
                        title="Editar" data-bs-toggle="tooltip"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-danger botao-acao" @click="removerConhecimento(idx, cidx)" title="Remover"
                        data-bs-toggle="tooltip"><i class="bi bi-trash"></i></button>
              </div>
            </template>
          </div>
          <form class="row g-2 align-items-center" @submit.prevent="adicionarConhecimento(idx)">
            <div class="col">
              <input v-model="atividade.novoConhecimento" class="form-control form-control-sm"
                     placeholder="Novo conhecimento"
                     type="text"/>
            </div>
            <div class="col-auto">
              <button class="btn btn-secondary btn-sm" type="submit" title="Adicionar Conhecimento"
                      data-bs-toggle="tooltip"><i
                  class="bi bi-plus"></i></button>
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

// Buscar nome da unidade pela sigla
const nomeUnidade = computed(() => {
  function buscarNome(unidades, sigla) {
    for (const unidade of unidades) {
      if (unidade.sigla === sigla) return unidade.nome || unidade.sigla
      if (unidade.filhas && unidade.filhas.length) {
        const encontrada = buscarNome(unidade.filhas, sigla)
        if (encontrada) return encontrada
      }
    }
    return sigla
  }

  return buscarNome(unidadesStore.unidades, siglaUnidade.value)
})

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

// --- Edição de conhecimento ---
const editandoConhecimento = ref({idxAtividade: null, idxConhecimento: null})
const conhecimentoEditado = ref('')

function iniciarEdicaoConhecimento(idxAtividade, idxConhecimento, valorAtual) {
  editandoConhecimento.value = {idxAtividade, idxConhecimento}
  conhecimentoEditado.value = valorAtual
}

function salvarEdicaoConhecimento(idxAtividade, idxConhecimento) {
  const atividade = atividades.value[idxAtividade]
  if (atividade && conhecimentoEditado.value.trim()) {
    atividade.conhecimentos[idxConhecimento].descricao = conhecimentoEditado.value.trim()
  }
  cancelarEdicaoConhecimento()
}

function cancelarEdicaoConhecimento() {
  editandoConhecimento.value = {idxAtividade: null, idxConhecimento: null}
  conhecimentoEditado.value = ''
}

// ---

// --- Edição de atividade ---
const editandoAtividade = ref(null)
const atividadeEditada = ref('')

function iniciarEdicaoAtividade(idx, valorAtual) {
  editandoAtividade.value = idx
  atividadeEditada.value = valorAtual
}

function salvarEdicaoAtividade(idx) {
  if (atividadeEditada.value.trim()) {
    atividades.value[idx].descricao = atividadeEditada.value.trim()
  }
  cancelarEdicaoAtividade()
}

function cancelarEdicaoAtividade() {
  editandoAtividade.value = null
  atividadeEditada.value = ''
}

const router = useRouter()

onMounted(() => {
  // Se a unidade for SESEL e ainda não estiver carregada, inicializa do mock (já feito no store)
  if (unidadeId.value === 'SESEL' && !atividadesPorUnidade.value.SESEL) {
    store.setAtividades('SESEL', store.atividadesPorUnidade.SESEL)
  }
})
</script>

<style scoped>
.atividade-edicao-row {
  width: 100%;
  justify-content: flex-start;
}

.atividade-edicao-input {
  flex-grow: 1;
  min-width: 0;
}

.atividade-card {
  transition: box-shadow 0.2s;
}

.atividade-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.botoes-acao-atividade,
.botoes-acao {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.group-atividade:hover .botoes-acao-atividade,
.group-conhecimento:hover .botoes-acao {
  opacity: 1;
  pointer-events: auto;
}

.botao-acao {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 1.1rem;
  border-width: 2px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  margin-left: 0;
  margin-right: 0;
}

.botao-acao:focus,
.botao-acao:hover {
  background: #f0f4fa;
  box-shadow: 0 0 0 2px #e3f0ff;
}

.fade-group {
  transition: opacity 0.2s;
}

.atividade-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.conhecimento-hover-row:hover span {
  font-weight: bold;
}

.atividade-hover-row:hover .atividade-descricao {
  font-weight: bold;
}

.atividade-titulo-card {
  background: #f8fafc;
  border-bottom: 1px solid #e3e8ee;
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
}

.atividade-titulo-card .atividade-descricao {
  font-size: 1.1rem;
}

.unidade-cabecalho {
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 1.2rem;
  display: flex;
  gap: 0.5rem;
}

.unidade-sigla {
  background: #f8fafc;
  color: #333;
  font-weight: bold;
  border-radius: 0.5rem;
  letter-spacing: 1px;
}

.unidade-nome {
  color: #222;
  opacity: 0.85;
  padding-right: 1rem;
}

</style>