<template>
  <div
    v-if="mostrar"
    aria-labelledby="criarCompetenciaModalLabel"
    aria-modal="true"
    class="modal fade show"
    role="dialog"
    style="display: block;"
    tabindex="-1"
  >
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5
            id="criarCompetenciaModalLabel"
            class="modal-title"
          >
            {{ competenciaSendoEditada ? 'Edição de competência' : 'Criação de competência' }}
          </h5>
          <button
            aria-label="Close"
            class="btn-close"
            type="button"
            @click="fechar"
          />
        </div>
        <div class="modal-body">
          <div class="mb-4">
            <h5>Descrição</h5>
            <div class="mb-2">
              <textarea
                v-model="novaCompetencia.descricao"
                class="form-control"
                placeholder="Descreva a competência"
                rows="3"
                data-testid="input-descricao-competencia"
              />
            </div>
          </div>

          <div class="mb-4">
            <h5>Atividades</h5>
            <div class="d-flex flex-wrap gap-2">
              <div
                v-for="atividade in atividades"
                :key="atividade.codigo"
                :class="{ checked: atividadesSelecionadas.includes(atividade.codigo) }"
                class="card atividade-card-item"
                @click="toggleAtividade(atividade.codigo)"
              >
                <div class="card-body d-flex align-items-center py-2">
                  <input
                    :id="`atv-${atividade.codigo}`"
                    v-model="atividadesSelecionadas"
                    :value="atividade.codigo"
                    class="form-check-input me-2"
                    hidden
                    type="checkbox"
                  >
                  <label class="form-check-label mb-0 d-flex align-items-center">
                    {{ atividade.descricao }}
                    <span
                      v-if="atividade.conhecimentos.length > 0"
                      :data-bs-html="true"
                      :data-bs-title="getConhecimentosModal(atividade)"
                      class="badge bg-secondary ms-2"
                      data-bs-custom-class="conhecimentos-tooltip"
                      data-bs-placement="right"
                      data-bs-toggle="tooltip"
                    >
                      {{ atividade.conhecimentos.length }}
                    </span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button
            class="btn btn-secondary"
            type="button"
            data-testid="btn-modal-cancelar"
            @click="fechar"
          >
            Cancelar
          </button>
          <button
            :disabled="atividadesSelecionadas.length === 0 || !novaCompetencia.descricao"
            class="btn btn-primary"
            type="button"
            data-testid="btn-modal-confirmar"
            @click="salvar"
          >
            <i class="bi bi-save" /> Salvar
          </button>
        </div>
      </div>
    </div>
  </div>
  <div
    v-if="mostrar"
    class="modal-backdrop fade show"
  />
</template>

<script lang="ts" setup>
import {ref, watch} from 'vue'
import {Atividade, Competencia} from '@/types/tipos'

const props = defineProps<{
  mostrar: boolean
  atividades: Atividade[]
  competenciaParaEditar?: Competencia | null
}>()

const emit = defineEmits<{
  fechar: []
  salvar: [competencia: { descricao: string, atividadesSelecionadas: number[] }]
}>()

const novaCompetencia = ref({descricao: ''})
const atividadesSelecionadas = ref<number[]>([])
const competenciaSendoEditada = ref<Competencia | null>(null)

watch(() => props.mostrar, (mostrar) => {
  if (mostrar) {
    if (props.competenciaParaEditar) {
      novaCompetencia.value.descricao = props.competenciaParaEditar.descricao
      atividadesSelecionadas.value = [...props.competenciaParaEditar.atividadesAssociadas]
      competenciaSendoEditada.value = props.competenciaParaEditar
    } else {
      novaCompetencia.value.descricao = ''
      atividadesSelecionadas.value = []
      competenciaSendoEditada.value = null
    }

    // Inicializar tooltips do modal
    setTimeout(() => {
      import('bootstrap').then(({Tooltip}) => {
        const modalTooltips = document.querySelectorAll('.modal [data-bs-toggle="tooltip"]')
        modalTooltips.forEach(tooltipEl => {
          new Tooltip(tooltipEl)
        })
      })
    }, 100)
  }
}, { immediate: true })

function toggleAtividade(codigo: number) {
  const index = atividadesSelecionadas.value.indexOf(codigo)
  if (index > -1) {
    atividadesSelecionadas.value.splice(index, 1)
  } else {
    atividadesSelecionadas.value.push(codigo)
  }
}

function getConhecimentosModal(atividade: Atividade): string {
  if (!atividade.conhecimentos.length) {
    return 'Nenhum conhecimento'
  }

  const conhecimentosHtml = atividade.conhecimentos
      .map(c => `<div class="mb-1">• ${c.descricao}</div>`)
      .join('')

  return `<div class="text-start"><strong>Conhecimentos:</strong><br>${conhecimentosHtml}</div>`
}

function fechar() {
  emit('fechar')
}

function salvar() {
  emit('salvar', {
    descricao: novaCompetencia.value.descricao,
    atividadesSelecionadas: atividadesSelecionadas.value
  })
}
</script>

<style scoped>
.atividade-card-item {
  cursor: pointer;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  transition: all 0.2s ease-in-out;
  background-color: var(--bs-body-bg);
}

.atividade-card-item:hover {
  border-color: var(--bs-primary);
  box-shadow: 0 0 0 0.25rem rgba(var(--bs-primary-rgb), 0.25);
}

.atividade-card-item.checked {
  background-color: var(--bs-primary-bg-subtle);
  border-color: var(--bs-primary);
}

.atividade-card-item .form-check-label {
  cursor: pointer;
  padding: 0.25rem 0;
}

.atividade-card-item.checked .form-check-label {
  font-weight: bold;
  color: var(--bs-primary);
}

.atividade-card-item .card-body {
  padding: 0.5rem 0.75rem;
}
</style>