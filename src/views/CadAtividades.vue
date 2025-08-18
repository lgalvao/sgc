<template>
  <div class="container mt-4">
    <!-- Modal de Importação de Atividades -->
    <div class="modal fade" id="importarAtividadesModal" tabindex="-1" aria-labelledby="importarAtividadesModalLabel"
         aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="importarAtividadesModalLabel">Importação de atividades</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">

            <!-- Etapa 1: Seleção de Processo -->
            <div class="mb-3">
              <label for="processo-select" class="form-label">Processo</label>
              <select id="processo-select" class="form-select" v-model="processoSelecionadoId">
                <option disabled value="">Selecione</option>
                <option v-for="proc in processosDisponiveis" :key="proc.id" :value="proc.id">
                  {{ proc.descricao }}
                </option>
              </select>
              <div v-if="!processosDisponiveis.length" class="text-center text-muted mt-3">
                Nenhum processo disponível para importação.
              </div>
            </div>

            <!-- Etapa 2: Seleção de Unidade -->
            <div class="mb-3">
              <label for="unidade-select" class="form-label">Unidade</label>
              <select id="unidade-select" class="form-select" v-model="unidadeSelecionadaId"
                      :disabled="!processoSelecionado">
                <option disabled value="">Selecione</option>
                <option v-for="pu in unidadesParticipantes" :key="pu.id" :value="pu.id">
                  {{ pu.unidade }}
                </option>
              </select>
            </div>

            <!-- Etapa 3: Seleção de Atividades -->
            <div v-if="unidadeSelecionada">
              <h6>Atividades para importar</h6>
              <div v-if="atividadesParaImportar.length" class="atividades-container border rounded p-2">
                <div v-for="ativ in atividadesParaImportar" :key="ativ.id" class="form-check">
                  <input class="form-check-input" type="checkbox" :value="ativ" v-model="atividadesSelecionadas"
                         :id="`ativ-check-${ativ.id}`">
                  <label class="form-check-label" :for="`ativ-check-${ativ.id}`">
                    {{ ativ.descricao }}
                  </label>
                </div>
              </div>
              <div v-else class="text-center text-muted mt-3">
                Nenhuma atividade encontrada para esta unidade/processo.
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal" @click="resetModal">Cancelar
            </button>
            <button type="button" class="btn btn-outline-primary" @click="importarAtividades"
                    :disabled="!atividadesSelecionadas.length">Importar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="fs-5 w-100 mb-3">
      {{ siglaUnidade }} - {{ nomeUnidade }}
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h1 class="mb-0 display-6">Atividades e conhecimentos</h1>

      <div class="d-flex gap-2">
        <button v-if="isChefe" class="btn btn-outline-primary" title="Importar"
                data-bs-toggle="modal" data-bs-target="#importarAtividadesModal">
          Importar atividades
        </button>
        <button class="btn btn-outline-success" title="Disponibilizar" data-bs-toggle="tooltip"
                @click="disponibilizarCadastro">
          Disponibilizar
        </button>
      </div>
    </div>

    <!-- Adicionar atividade -->
    <form class="row g-2 align-items-center mb-4" @submit.prevent="adicionarAtividade">
      <div class="col">
        <input v-model="novaAtividade" class="form-control" placeholder="Nova atividade" type="text"
               data-testid="input-nova-atividade"/>
      </div>
      <div class="col-auto">
        <button class="btn btn-outline-primary btn-sm" type="submit" title="Adicionar atividade"
                data-bs-toggle="tooltip"
                data-testid="btn-adicionar-atividade"><i
            class="bi bi-save"></i></button>
      </div>
    </form>

    <!-- Lista de atividades -->
    <div v-for="(atividade, idx) in atividades" :key="atividade.id || idx" class="card mb-3 atividade-card">
      <div class="card-body py-2">
        <div
            class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card">
          <template v-if="editandoAtividade === atividade.id">
            <input v-model="atividadeEditada" class="form-control me-2 atividade-edicao-input"
                   data-testid="input-editar-atividade"/>
            <button class="btn btn-sm btn-outline-success me-1 botao-acao" @click="salvarEdicaoAtividade(atividade.id)"
                    title="Salvar"
                    data-bs-toggle="tooltip" data-testid="btn-salvar-edicao-atividade"><i class="bi bi-save"></i>
            </button>
            <button class="btn btn-sm btn-outline-secondary botao-acao" @click="cancelarEdicaoAtividade()"
                    title="Cancelar"
                    data-bs-toggle="tooltip" data-testid="btn-cancelar-edicao-atividade"><i class="bi bi-x"></i>
            </button>
          </template>

          <template v-else>
            <strong class="atividade-descricao" data-testid="atividade-descricao">{{ atividade.descricao }}</strong>
            <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao-atividade fade-group">
              <button class="btn btn-sm btn-outline-primary botao-acao"
                      @click="iniciarEdicaoAtividade(atividade.id, atividade.descricao)" title="Editar"
                      data-bs-toggle="tooltip" data-testid="btn-editar-atividade"><i class="bi bi-pencil"></i></button>
              <button class="btn btn-sm btn-outline-danger botao-acao" @click="removerAtividade(idx)" title="Remover"
                      data-bs-toggle="tooltip" data-testid="btn-remover-atividade"><i
                  class="bi bi-trash"></i></button>
            </div>
          </template>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div v-for="(conhecimento, cidx) in atividade.conhecimentos" :key="conhecimento.id"
               class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row">
            <template v-if="editandoConhecimento.idxAtividade === idx && editandoConhecimento.idxConhecimento === cidx">
              <input v-model="conhecimentoEditado" class="form-control form-control-sm me-2 conhecimento-edicao-input"
                     style="max-width: 300px;" data-testid="input-editar-conhecimento"/>
              <button class="btn btn-sm btn-outline-success me-1 botao-acao"
                      @click="salvarEdicaoConhecimento(idx, cidx)"
                      title="Salvar"
                      data-bs-toggle="tooltip" data-testid="btn-salvar-edicao-conhecimento"><i class="bi bi-save"></i>
              </button>
              <button class="btn btn-sm btn-outline-secondary botao-acao" @click="cancelarEdicaoConhecimento"
                      title="Cancelar"
                      data-bs-toggle="tooltip" data-testid="btn-cancelar-edicao-conhecimento"><i class="bi bi-x"></i>
              </button>
            </template>
            <template v-else>
              <span data-testid="conhecimento-descricao">{{ conhecimento.descricao }}</span>
              <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao fade-group">
                <button class="btn btn-sm btn-outline-primary botao-acao"
                        @click="iniciarEdicaoConhecimento(idx, cidx, conhecimento.descricao)"
                        title="Editar" data-bs-toggle="tooltip" data-testid="btn-editar-conhecimento"><i
                    class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger botao-acao" @click="removerConhecimento(idx, cidx)"
                        title="Remover"
                        data-bs-toggle="tooltip" data-testid="btn-remover-conhecimento"><i class="bi bi-trash"></i>
                </button>
              </div>
            </template>
          </div>
          <form class="row g-2 align-items-center" @submit.prevent="adicionarConhecimento(idx)">
            <div class="col">
              <input v-model="atividade.novoConhecimento" class="form-control form-control-sm"
                     placeholder="Novo conhecimento"
                     type="text" data-testid="input-novo-conhecimento"/>
            </div>
            <div class="col-auto">
              <button class="btn btn-outline-secondary btn-sm" type="submit" title="Adicionar Conhecimento"
                      data-bs-toggle="tooltip" data-testid="btn-adicionar-conhecimento"><i
                  class="bi bi-save"></i></button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, onUnmounted, ref, watch} from 'vue'
import {Modal} from 'bootstrap'
import {usePerfil} from '@/composables/usePerfil'
import {useAtividadesStore} from '@/stores/atividades'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {Atividade, Processo, ProcessoTipo, ProcessoUnidade, Unidade} from '@/types/tipos'

interface AtividadeComEdicao extends Atividade {
  novoConhecimento?: string;
}

const props = defineProps<{
  idProcesso: number | string,
  sigla: string
}>()

const unidadeId = computed(() => props.sigla)
const idProcesso = computed(() => Number(props.idProcesso))

const atividadesStore = useAtividadesStore()
const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()

const unidade = computed(() => {
  function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | undefined {
    for (const u of unidades) {
      if (u.sigla === sigla) return u
      if (u.filhas && u.filhas.length) {
        const encontrada = buscarUnidade(u.filhas, sigla)
        if (encontrada) return encontrada
      }
    }
  }

  return buscarUnidade(unidadesStore.unidades as Unidade[], unidadeId.value)
})

const siglaUnidade = computed(() => unidade.value?.sigla || unidadeId.value)

const nomeUnidade = computed(() => (unidade.value?.nome ? `${unidade.value.nome}` : ''))

const novaAtividade = ref('')

const subidProcesso = computed(() => {
  const processoUnidade = (processosStore.processosUnidade as ProcessoUnidade[]).find(
      pu => pu.idProcesso === idProcesso.value && pu.unidade === unidadeId.value
  );
  return processoUnidade?.id;
});

const atividades = computed<AtividadeComEdicao[]>({
  get: () => {
    if (subidProcesso.value === undefined) return []
    const storeAtividades = atividadesStore.getAtividadesPorProcessoUnidade(subidProcesso.value) || []
    return storeAtividades.map((a: Atividade) => ({...a, novoConhecimento: ''}))
  },
  set: (val: AtividadeComEdicao[]) => {
    if (subidProcesso.value === undefined) return
    const storeVal = val.map(a => {
      const {novoConhecimento, ...rest} = a
      return rest
    })
    atividadesStore.setAtividades(subidProcesso.value, storeVal)
  }
})

function adicionarAtividade() {
  if (novaAtividade.value && subidProcesso.value !== undefined) {
    atividadesStore.adicionarAtividade({
      id: Date.now(),
      descricao: novaAtividade.value,
      subidProcesso: subidProcesso.value,
      conhecimentos: [],
    })
    novaAtividade.value = ''
  }
}

function removerAtividade(idx: number) {
  atividadesStore.removerAtividade(atividades.value[idx].id)
}

function adicionarConhecimento(idx: number) {
  const atividade = atividades.value[idx]
  if (atividade.novoConhecimento?.trim()) {
    atividadesStore.adicionarConhecimento(atividade.id, {
      id: Date.now(),
      descricao: atividade.novoConhecimento
    })
  }
}

function removerConhecimento(idx: number, cidx: number) {
  atividadesStore.removerConhecimento(atividades.value[idx].id, atividades.value[idx].conhecimentos[cidx].id)
}

const editandoConhecimento = ref<{ idxAtividade: number | null, idxConhecimento: number | null }>({
  idxAtividade: null,
  idxConhecimento: null
})
const conhecimentoEditado = ref('')

function iniciarEdicaoConhecimento(idxAtividade: number, idxConhecimento: number, valorAtual: string) {
  editandoConhecimento.value = {idxAtividade, idxConhecimento}
  conhecimentoEditado.value = valorAtual
}

function salvarEdicaoConhecimento(idxAtividade: number, idxConhecimento: number) {
  if (conhecimentoEditado.value) {
    const newAtividades = [...atividades.value];
    const newConhecimentos = [...newAtividades[idxAtividade].conhecimentos];
    newConhecimentos[idxConhecimento] = {...newConhecimentos[idxConhecimento], descricao: conhecimentoEditado.value};
    newAtividades[idxAtividade] = {...newAtividades[idxAtividade], conhecimentos: newConhecimentos};
    atividades.value = newAtividades;
  }
  cancelarEdicaoConhecimento()
}

function cancelarEdicaoConhecimento() {
  editandoConhecimento.value = {idxAtividade: null, idxConhecimento: null}
  conhecimentoEditado.value = ''
}

const editandoAtividade = ref<number | null>(null)
const atividadeEditada = ref('')

function iniciarEdicaoAtividade(id: number, valorAtual: string) {
  editandoAtividade.value = id
  atividadeEditada.value = valorAtual
}

function salvarEdicaoAtividade(id: number) {
  if (String(atividadeEditada.value).trim()) {
    const newAtividades = [...atividades.value];
    const atividadeIndex = newAtividades.findIndex(a => a.id === id);
    if (atividadeIndex !== -1) {
      newAtividades[atividadeIndex].descricao = String(atividadeEditada.value);
      atividades.value = newAtividades;
    }
  }
  cancelarEdicaoAtividade()
}

function cancelarEdicaoAtividade() {
  editandoAtividade.value = null
  atividadeEditada.value = ''
}

function handleImportAtividades(atividadesImportadas: Atividade[]) {
  if (subidProcesso.value === undefined) return;

  const novasAtividades = atividadesImportadas.map(atividade => ({
    ...atividade,
    subidProcesso: subidProcesso.value as number,
  }));

  atividadesStore.adicionarMultiplasAtividades(novasAtividades);
}

const {perfilSelecionado} = usePerfil()

const isChefe = computed(() => perfilSelecionado.value === 'CHEFE')

// Variáveis reativas para o modal de importação
const processoSelecionado = ref<Processo | null>(null)
const processoSelecionadoId = ref<number | null>(null)
const unidadesParticipantes = ref<ProcessoUnidade[]>([])
const unidadeSelecionada = ref<ProcessoUnidade | null>(null)
const unidadeSelecionadaId = ref<number | null>(null)
const atividadesParaImportar = ref<Atividade[]>([])
const atividadesSelecionadas = ref<Atividade[]>([])

const modalElement = ref<HTMLElement | null>(null)
const cleanupBackdrop = () => {
  const backdrop = document.querySelector('.modal-backdrop');
  if (backdrop) {
    backdrop.remove();
  }
  document.body.classList.remove('modal-open');
  document.body.style.overflow = '';
  document.body.style.paddingRight = '';
};

onMounted(() => {
  modalElement.value = document.getElementById('importarAtividadesModal');
  modalElement.value?.addEventListener('hidden.bs.modal', cleanupBackdrop);
});

onUnmounted(() => {
  modalElement.value?.removeEventListener('hidden.bs.modal', cleanupBackdrop);
});

watch(processoSelecionadoId, (newId) => {
  if (newId) {
    const processo = processosDisponiveis.value.find(p => p.id === newId)
    if (processo) {
      selecionarProcesso(processo)
    }
  } else {
    selecionarProcesso(null)
  }
})

watch(unidadeSelecionadaId, (newId) => {
  if (newId) {
    const unidade = unidadesParticipantes.value.find(u => u.id === newId)
    if (unidade) {
      selecionarUnidade(unidade)
    }
  } else {
    selecionarUnidade(null)
  }
})

// Computed property para processos disponíveis para importação
const processosDisponiveis = computed<Processo[]>(() => {
  return processosStore.processos.filter(p =>
      (p.tipo === ProcessoTipo.MAPEAMENTO || p.tipo === ProcessoTipo.REVISAO) && p.situacao === 'Finalizado'
  )
})

// Funções do modal
function resetModal() {
  processoSelecionado.value = null
  processoSelecionadoId.value = null
  unidadesParticipantes.value = []
  unidadeSelecionada.value = null
  unidadeSelecionadaId.value = null
  atividadesParaImportar.value = []
  atividadesSelecionadas.value = []
}

function selecionarProcesso(processo: Processo | null) {
  processoSelecionado.value = processo
  unidadesParticipantes.value = processo ? processosStore.getUnidadesDoProcesso(processo.id) : []
  unidadeSelecionada.value = null // Reseta a unidade ao trocar de processo
  unidadeSelecionadaId.value = null
}

async function selecionarUnidade(unidadePu: ProcessoUnidade | null) {
  unidadeSelecionada.value = unidadePu
  if (unidadePu) {
    await atividadesStore.fetchAtividadesPorProcessoUnidade(unidadePu.id)
    const atividadesDaOutraUnidade = atividadesStore.getAtividadesPorProcessoUnidade(unidadePu.id)
    atividadesParaImportar.value = atividadesDaOutraUnidade ? [...atividadesDaOutraUnidade] : []
  } else {
    atividadesParaImportar.value = []
  }
}

function importarAtividades() {
  handleImportAtividades(atividadesSelecionadas.value)
  // Fechar o modal
  const modalElement = document.getElementById('importarAtividadesModal')
  if (modalElement) {
    const modal = Modal.getInstance(modalElement) || new Modal(modalElement)
    modal.hide()
  }
  resetModal()
}

function disponibilizarCadastro() {
}
</script>

<style>
.atividades-container {
  max-height: 250px;
  overflow-y: auto;
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

</style>