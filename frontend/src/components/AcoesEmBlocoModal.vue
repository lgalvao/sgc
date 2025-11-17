<template>
  <b-modal
    :model-value="mostrar"
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
              <b-form-checkbox
                :id="'chk-' + unidade.sigla"
                v-model="unidade.selecionada"
                :data-testid="'chk-unidade-' + unidade.sigla"
              />
            </td>
            <td><strong>{{ unidade.sigla }}</strong></td>
            <td>{{ unidade.nome }}</td>
            <td>{{ unidade.situacao }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <template #footer>
      <button
        type="button"
        class="btn btn-secondary"
        data-testid="btn-modal-cancelar"
        @click="fechar"
      >
        <i class="bi bi-x-circle" /> Cancelar
      </button>
      <button
        type="button"
        class="btn"
        :class="tipoAcao === 'aceitar' ? 'btn-primary' : 'btn-success'"
        data-testid="btn-confirmar-acao-bloco"
        @click="confirmar"
      >
        <i :class="tipoAcao === 'aceitar' ? 'bi bi-check-circle' : 'bi bi-check-all'" />
        {{ tipoAcao === 'aceitar' ? 'Aceitar' : 'Homologar' }}
      </button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import {ref, watch} from 'vue'

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
  fechar: []
  confirmar: [unidadesSelecionadas: string[]]
}>()

const unidades = ref<UnidadeBloco[]>([])

watch(() => props.mostrar, (mostrar) => {
  if (mostrar) {
    unidades.value = props.unidadesDisponiveis.map(u => ({
      ...u,
      selecionada: true
    }))
  }
})

function fechar() {
  emit('fechar')
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
