<template>
  <b-modal
    :model-value="mostrar"
    title="Importação de atividades"
    size="lg"
    centered
    @hidden="fechar"
  >
    <div
      v-if="erroImportacao"
      class="alert alert-danger alert-dismissible"
      role="alert"
    >
      <div>{{ erroImportacao }}</div>
      <button
        type="button"
        class="btn-close"
        aria-label="Close"
        @click="limparErroImportacao"
      />
    </div>
    <fieldset :disabled="importando">
      <div class="mb-3">
        <label
          class="form-label"
          for="processo-select"
        >Processo</label>
        <b-form-select
          id="processo-select"
          v-model="processoSelecionadoId"
          data-testid="select-processo"
          :options="processosDisponiveis"
          value-field="codigo"
          text-field="descricao"
        >
          <template #first>
            <b-form-select-option
              disabled
              value=""
            >
              Selecione
            </b-form-select-option>
          </template>
        </b-form-select>
        <div
          v-if="!processosDisponiveis.length"
          class="text-center text-muted mt-3"
        >
          Nenhum processo disponível para importação.
        </div>
      </div>

      <div class="mb-3">
        <label
          class="form-label"
          for="unidade-select"
        >Unidade</label>
        <b-form-select
          id="unidade-select"
          v-model="unidadeSelecionadaId"
          :disabled="!processoSelecionado"
          data-testid="select-unidade"
          :options="unidadesParticipantes"
          value-field="codUnidade"
          text-field="sigla"
        >
          <template #first>
            <b-form-select-option
              disabled
              value=""
            >
              Selecione
            </b-form-select-option>
          </template>
        </b-form-select>
      </div>

      <div v-if="unidadeSelecionada">
        <h6>Atividades para importar</h6>
        <div
          v-if="atividadesParaImportar.length"
          class="atividades-container border rounded p-2"
        >
          <div
            v-for="ativ in atividadesParaImportar"
            :key="ativ.codigo"
            class="form-check"
          >
            <b-form-checkbox
              :id="`ativ-check-${ativ.codigo}`"
              v-model="atividadesSelecionadas"
              :value="ativ"
              :data-testid="`checkbox-atividade-${ativ.codigo}`"
            >
              {{ ativ.descricao }}
            </b-form-checkbox>
          </div>
        </div>
        <div
          v-else
          class="text-center text-muted mt-3"
        >
          Nenhuma atividade encontrada para esta unidade/processo.
        </div>
      </div>
    </fieldset>
    <template #footer>
      <button
        class="btn btn-outline-secondary"
        type="button"
        data-testid="btn-modal-cancelar"
        @click="fechar"
      >
        Cancelar
      </button>
      <button
        :disabled="!atividadesSelecionadas.length || importando"
        class="btn btn-outline-primary"
        type="button"
        data-testid="btn-importar"
        @click="importar"
      >
        <span
          v-if="importando"
          class="spinner-border spinner-border-sm"
          role="status"
          aria-hidden="true"
        />
        {{ importando ? 'Importando...' : 'Importar' }}
      </button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useProcessosStore} from '@/stores/processos'
import {useAtividadesStore} from '@/stores/atividades'
import {type Atividade, type ProcessoResumo, TipoProcesso, type UnidadeParticipante} from '@/types/tipos'
import {useApi} from '@/composables/useApi';

const props = defineProps<{
  mostrar: boolean,
  codSubrocessoDestino: number | undefined
}>()

const emit = defineEmits<{
  fechar: []
  importar: []
}>()

const processosStore = useProcessosStore()
const atividadesStore = useAtividadesStore()

const {
  execute: executarImportacao,
  error: erroImportacao,
  isLoading: importando,
  clearError: limparErroImportacao
} = useApi(atividadesStore.importarAtividades);

const processoSelecionado = ref<ProcessoResumo | null>(null)
const processoSelecionadoId = ref<number | null>(null)
const unidadesParticipantes = ref<UnidadeParticipante[]>([])
const unidadeSelecionada = ref<UnidadeParticipante | null>(null)
const unidadeSelecionadaId = ref<number | null>(null)
const atividadesParaImportar = ref<Atividade[]>([])
const atividadesSelecionadas = ref<Atividade[]>([])

const processosDisponiveis = computed<ProcessoResumo[]>(() => {
  return processosStore.processosPainel.filter(p =>
      (p.tipo === TipoProcesso.MAPEAMENTO || p.tipo === TipoProcesso.REVISAO) && p.situacao === 'FINALIZADO'
  )
})

onMounted(() => {
  processosStore.fetchProcessosPainel('ADMIN', 0, 0, 1000); // Usar um perfil e unidade genéricos para obter todos os processos
});


watch(() => props.mostrar, (mostrar) => {
  if (mostrar) {
    resetModal()
  }
})

watch(processoSelecionadoId, async (newId) => {
  if (newId) {
    const processo = processosDisponiveis.value.find(p => p.codigo === Number(newId))
    if (processo) {
      await selecionarProcesso(processo)
    }
  } else {
    await selecionarProcesso(null)
  }
})

watch(unidadeSelecionadaId, (newId) => {
  if (newId) {
    const unidade = unidadesParticipantes.value.find(u => u.codUnidade === Number(newId))
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

async function selecionarProcesso(processo: ProcessoResumo | null) {
  processoSelecionado.value = processo;
  if (processo) {
    await processosStore.fetchProcessoDetalhe(processo.codigo);
    unidadesParticipantes.value = processosStore.processoDetalhe?.unidades || [];
  } else {
    unidadesParticipantes.value = [];
  }
  unidadeSelecionada.value = null
  unidadeSelecionadaId.value = null
}

async function selecionarUnidade(unidadePu: UnidadeParticipante | null) {
  unidadeSelecionada.value = unidadePu
  if (unidadePu) {
    await atividadesStore.fetchAtividadesParaSubprocesso(unidadePu.codUnidade);
    const atividadesDaOutraUnidade = atividadesStore.getAtividadesPorSubprocesso(unidadePu.codUnidade);
    atividadesParaImportar.value = atividadesDaOutraUnidade ? [...atividadesDaOutraUnidade] : [];
  } else {
    atividadesParaImportar.value = [];
  }
}

function fechar() {
  emit('fechar')
}

async function importar() {
  limparErroImportacao();
  if (!props.codSubrocessoDestino || !unidadeSelecionada.value) {
    return;
  }
  if (atividadesSelecionadas.value.length === 0) {
    erroImportacao.value = 'Selecione ao menos uma atividade para importar.';
    return;
  }

  const idsAtividades = atividadesSelecionadas.value.map(a => a.codigo);

  try {
    await executarImportacao(props.codSubrocessoDestino, unidadeSelecionada.value.codSubprocesso, idsAtividades);
    emit('importar');
    fechar();
  } catch {
    // O erro já está sendo tratado pelo useApi, não é necessário fazer nada aqui
    // a não ser que queira algum comportamento adicional no erro.
  }
}
</script>

<style scoped>
.atividades-container {
  max-height: 250px;
  overflow-y: auto;
}
</style>
