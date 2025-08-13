<template>
  <div class="modal fade" id="importarAtividadesModal" tabindex="-1" aria-labelledby="importarAtividadesModalLabel"
       aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="importarAtividadesModalLabel">Importar Atividades</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="importarAtividades">
            <div class="mb-3">
              <label for="processoSelect" class="form-label">Processo:</label>
              <select class="form-select" id="processoSelect" v-model="processoSelecionadoId"
                      data-testid="select-processo-importar">
                <option :value="null" disabled>Selecione um processo</option>
                <option v-for="processo in processosDisponiveis" :key="processo.id" :value="processo.id">
                  {{ processo.descricao }}
                </option>
              </select>
            </div>

            <div class="mb-3" v-if="processoSelecionadoId">
              <label for="unidadeSelect" class="form-label">Unidade:</label>
              <select class="form-select" id="unidadeSelect" v-model="unidadeSelecionadaSigla"
                      data-testid="select-unidade-importar">
                <option :value="null" disabled>Selecione uma unidade</option>
                <option v-for="unidade in unidadesDisponiveisParaProcesso" :key="unidade.sigla" :value="unidade.sigla">
                  {{ unidade.sigla }} - {{ unidade.nome }}
                </option>
              </select>
            </div>

            <div v-if="atividadesDisponiveis.length > 0">
              <h6 class="mt-4">Atividades disponíveis para importação:</h6>
              <div class="form-check" v-for="atividade in atividadesDisponiveis" :key="atividade.id">
                <input class="form-check-input" type="checkbox" :value="atividade.id" v-model="atividadesSelecionadas"
                       :id="'atividade-' + atividade.id" data-testid="checkbox-atividade-importar">
                <label class="form-check-label" :for="'atividade-' + atividade.id">
                  {{ atividade.descricao }}
                  <span class="list-unstyled ms-3" v-if="atividade.conhecimentos && atividade.conhecimentos.length > 0">
                    <span v-for="conhecimento in atividade.conhecimentos" :key="conhecimento.id">
                      - {{ conhecimento.descricao }}
                    </span>
                  </span>
                </label>
              </div>
            </div>
            <div v-else-if="processoSelecionadoId && unidadeSelecionadaSigla">
              <p class="text-muted mt-4">Nenhuma atividade encontrada para o processo e unidade selecionados.</p>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click="resetModal">Cancelar</button>
          <button type="button" class="btn btn-primary" @click="importarAtividades"
                  :disabled="atividadesSelecionadas.length === 0">Importar Selecionadas
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {useAtividadesStore} from '@/stores/atividades'
import {useMapasStore} from '@/stores/mapas'
import {Atividade, ProcessoTipo, ProcessoUnidade} from '@/types/tipos'

const emit = defineEmits(['import-atividades'])

const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()
const atividadesStore = useAtividadesStore()
const mapasStore = useMapasStore()

const processoSelecionadoId = ref<number | null>(null)
const unidadeSelecionadaSigla = ref<string | null>(null)
const atividadesDisponiveis = ref<Atividade[]>([])
const atividadesSelecionadas = ref<number[]>([])

const processosDisponiveis = computed(() => {
  return processosStore.processos.filter(p => {
    const mapa = mapasStore.mapas.find(m => m.processoId === p.id)
    return (p.tipo === ProcessoTipo.REVISAO || p.tipo === ProcessoTipo.MAPEAMENTO) && mapa
  })
})

const unidadesDisponiveisParaProcesso = computed(() => {
  if (!processoSelecionadoId.value) return []

  const unidadesSiglas = processosStore.processosUnidade
      .filter(pu => pu.processoId === processoSelecionadoId.value)
      .map(pu => pu.unidade)

  return unidadesStore.unidades.filter(u => unidadesSiglas.includes(u.sigla))
})

const processoUnidadeIdSelecionado = computed(() => {
  if (!processoSelecionadoId.value || !unidadeSelecionadaSigla.value) return null

  const processoUnidade = processosStore.processosUnidade.find(
      (pu: ProcessoUnidade) =>
          pu.processoId === processoSelecionadoId.value && pu.unidade === unidadeSelecionadaSigla.value
  )
  return processoUnidade ? processoUnidade.id : null
})

watch([processoSelecionadoId, unidadeSelecionadaSigla], () => {
  atividadesDisponiveis.value = []
  atividadesSelecionadas.value = []
  if (processoUnidadeIdSelecionado.value) {
    buscarAtividadesParaImportacao()
  }
})

function buscarAtividadesParaImportacao() {
  if (!processoUnidadeIdSelecionado.value) return

  const atividadesDaUnidadeAno = atividadesStore.getAtividadesPorProcessoUnidade(
      processoUnidadeIdSelecionado.value
  )
  atividadesDisponiveis.value = atividadesDaUnidadeAno || []
}

function importarAtividades() {
  const atividadesParaImportar: Atividade[] = []
  atividadesSelecionadas.value.forEach(id => {
    const atividade = atividadesDisponiveis.value.find(a => a.id === id)
    if (atividade) {
      atividadesParaImportar.push(atividade)
    }
  })
  emit('import-atividades', atividadesParaImportar)
  resetModal()
}

function resetModal() {
  processoSelecionadoId.value = null
  unidadeSelecionadaSigla.value = null
  atividadesDisponiveis.value = []
  atividadesSelecionadas.value = []
}

onMounted(() => {
  const modalElement = document.getElementById('importarAtividadesModal');
  if (modalElement) {
    modalElement.addEventListener('hidden.bs.modal', resetModal);
  }
});
</script>

<style scoped>
/* Estilos do modal aqui */
</style>