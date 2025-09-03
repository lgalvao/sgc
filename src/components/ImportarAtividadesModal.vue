<template>
  <div v-if="mostrar" class="modal fade show" style="display: block;" tabindex="-1">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Importação de atividades</h5>
          <button type="button" class="btn-close" @click="fechar"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label class="form-label" for="processo-select">Processo</label>
            <select id="processo-select" v-model="processoSelecionadoId" class="form-select">
              <option disabled value="">Selecione</option>
              <option v-for="proc in processosDisponiveis" :key="proc.id" :value="proc.id">
                {{ proc.descricao }}
              </option>
            </select>
            <div v-if="!processosDisponiveis.length" class="text-center text-muted mt-3">
              Nenhum processo disponível para importação.
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label" for="unidade-select">Unidade</label>
            <select id="unidade-select" v-model="unidadeSelecionadaId" :disabled="!processoSelecionado" class="form-select">
              <option disabled value="">Selecione</option>
              <option v-for="pu in unidadesParticipantes" :key="pu.id" :value="pu.id">
                {{ pu.unidade }}
              </option>
            </select>
          </div>

          <div v-if="unidadeSelecionada">
            <h6>Atividades para importar</h6>
            <div v-if="atividadesParaImportar.length" class="atividades-container border rounded p-2">
              <div v-for="ativ in atividadesParaImportar" :key="ativ.id" class="form-check">
                <input :id="`ativ-check-${ativ.id}`" v-model="atividadesSelecionadas" :value="ativ" class="form-check-input" type="checkbox">
                <label :for="`ativ-check-${ativ.id}`" class="form-check-label">
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
          <button class="btn btn-outline-secondary" type="button" @click="fechar">Cancelar</button>
          <button :disabled="!atividadesSelecionadas.length" class="btn btn-outline-primary" type="button" @click="importar">Importar</button>
        </div>
      </div>
    </div>
  </div>
  <div v-if="mostrar" class="modal-backdrop fade show"></div>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {useProcessosStore} from '@/stores/processos'
import {useAtividadesStore} from '@/stores/atividades'
import {Atividade, Processo, SituacaoProcesso, Subprocesso, TipoProcesso} from '@/types/tipos'

const props = defineProps<{
  mostrar: boolean
}>()

const emit = defineEmits<{
  fechar: []
  importar: [atividades: Atividade[]]
}>()

const processosStore = useProcessosStore()
const atividadesStore = useAtividadesStore()

const processoSelecionado = ref<Processo | null>(null)
const processoSelecionadoId = ref<number | null>(null)
const unidadesParticipantes = ref<Subprocesso[]>([])
const unidadeSelecionada = ref<Subprocesso | null>(null)
const unidadeSelecionadaId = ref<number | null>(null)
const atividadesParaImportar = ref<Atividade[]>([])
const atividadesSelecionadas = ref<Atividade[]>([])

const processosDisponiveis = computed<Processo[]>(() => {
  return processosStore.processos.filter(p =>
    (p.tipo === TipoProcesso.MAPEAMENTO || p.tipo === TipoProcesso.REVISAO) && p.situacao === SituacaoProcesso.FINALIZADO
  )
})

watch(() => props.mostrar, (mostrar) => {
  if (mostrar) {
    resetModal()
  }
})

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
  unidadeSelecionada.value = null
  unidadeSelecionadaId.value = null
}

async function selecionarUnidade(unidadePu: Subprocesso | null) {
  unidadeSelecionada.value = unidadePu
  if (unidadePu) {
    await atividadesStore.fetchAtividadesPorSubprocesso(unidadePu.id)
    const atividadesDaOutraUnidade = atividadesStore.getAtividadesPorSubprocesso(unidadePu.id)
    atividadesParaImportar.value = atividadesDaOutraUnidade ? [...atividadesDaOutraUnidade] : []
  } else {
    atividadesParaImportar.value = []
  }
}

function fechar() {
  emit('fechar')
}

function importar() {
  if (atividadesSelecionadas.value.length === 0) {
    alert('Selecione ao menos uma atividade para importar.')
    return
  }
  emit('importar', atividadesSelecionadas.value)
}
</script>

<style scoped>
.atividades-container {
  max-height: 250px;
  overflow-y: auto;
}
</style>