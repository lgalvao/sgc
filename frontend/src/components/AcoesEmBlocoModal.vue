<template>
  <b-modal
    v-model="show"
    :title="tipoAcao === 'aceitar' ? 'Aceitar cadastros em bloco' : 'Homologar cadastros em bloco'"
    size="lg"
    centered
    @hidden="fechar"
  >
    <div class="alert alert-info">
      <i class="bi bi-info-circle" />
      Selecione as unidades que terão seus cadastros {{ tipoAcao === 'aceitar' ? 'aceitos' : 'homologados' }}:
    </div>

    <div class="table-responsive">
      <table class="table table-bordered">
        <thead class="table-light">
          <tr>
            <th>Selecionar</th>
            <th>Sigla</th>
            <th>Nome</th>
            <th>Situação Atual</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="unidade in unidades"
            :key="unidade.sigla"
          >
            <td>
              <input
                :id="'chk-' + unidade.sigla"
                v-model="unidade.selecionada"
                :data-testid="'chk-unidade-' + unidade.sigla"
                type="checkbox"
                class="form-check-input"
              >
            </td>
            <td><strong>{{ unidade.sigla }}</strong></td>
            <td>{{ unidade.nome }}</td>
            <td>{{ unidade.situacao }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <template #footer>
      <b-button
        variant="secondary"
        data-testid="btn-modal-cancelar"
        @click="fechar"
      >
        <i class="bi bi-x-circle" /> Cancelar
      </b-button>
      <b-button
        :variant="tipoAcao === 'aceitar' ? 'primary' : 'success'"
        data-testid="btn-confirmar-acao-bloco"
        @click="confirmar"
      >
        <i :class="tipoAcao === 'aceitar' ? 'bi bi-check-circle' : 'bi bi-check-all'" />
        {{ tipoAcao === 'aceitar' ? 'Aceitar' : 'Homologar' }}
      </b-button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import {ref, watch, computed} from 'vue'

interface UnidadeBloco {
  sigla: string
  nome: string
  situacao: string
  selecionada: boolean
}

const props = defineProps<{
  mostrar: boolean
  tipoAcao: 'aceitar' | 'homologar'
  unidadesDisponiveis: Array<{ sigla: string, nome: string, situacao: string }>
}>()

const emit = defineEmits<{
  (e: 'update:mostrar', value: boolean): void
  (e: 'confirmar', unidadesSelecionadas: string[]): void
}>()

const unidades = ref<UnidadeBloco[]>([])

const show = computed({
  get: () => props.mostrar,
  set: (value) => emit('update:mostrar', value)
})

watch(() => props.mostrar, (mostrar) => {
  if (mostrar) {
    unidades.value = props.unidadesDisponiveis.map(u => ({
      ...u,
      selecionada: true
    }))
  }
})

function fechar() {
  emit('update:mostrar', false)
}

function confirmar() {
  const unidadesSelecionadas = unidades.value
    .filter(u => u.selecionada)
    .map(u => u.sigla)
  
  if (unidadesSelecionadas.length === 0) {
    alert('Selecione ao menos uma unidade para processar.')
    return
  }
  
  emit('confirmar', unidadesSelecionadas)
}
</script>
