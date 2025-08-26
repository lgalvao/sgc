<template>
  <div v-if="mostrar" aria-labelledby="criarCompetenciaModalLabel" aria-modal="true"
       class="modal fade show" role="dialog" style="display: block;" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 id="criarCompetenciaModalLabel" class="modal-title">
            {{ competenciaSendoEditada ? 'Edição de competência' : 'Criação de competência' }}
          </h5>
          <button aria-label="Close" class="btn-close" type="button" @click="fechar"></button>
        </div>
        <div class="modal-body">
          <div class="mb-4">
            <h5>Descrição</h5>
            <div class="mb-2">
              <textarea v-model="novaCompetencia.descricao"
                        class="form-control"
                        placeholder="Descreva a competência"
                        rows="3"></textarea>
            </div>
          </div>

          <div class="mb-4">
            <h5>Atividades</h5>
            <div class="d-flex flex-wrap gap-2">
              <div v-for="atividade in atividades" :key="atividade.id"
                   :class="{ checked: atividadesSelecionadas.includes(atividade.id) }"
                   class="card atividade-card-item"
                   @click="toggleAtividade(atividade.id)">
                <div class="card-body d-flex align-items-center py-2">
                  <input :id="`atv-${atividade.id}`" v-model="atividadesSelecionadas"
                         :value="atividade.id"
                         class="form-check-input me-2"
                         hidden
                         type="checkbox">
                  <label class="form-check-label mb-0 d-flex align-items-center">
                    {{ atividade.descricao }}
                    <span v-if="atividade.conhecimentos.length > 0"
                          :data-bs-html="true"
                          :data-bs-title="getConhecimentosModal(atividade)"
                          class="badge bg-secondary ms-2"
                          data-bs-custom-class="conhecimentos-tooltip"
                          data-bs-placement="right"
                          data-bs-toggle="tooltip">
                      {{ atividade.conhecimentos.length }}
                    </span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" type="button" @click="fechar">Cancelar</button>
          <button :disabled="atividadesSelecionadas.length === 0 || !novaCompetencia.descricao"
                  class="btn btn-primary"
                  type="button" @click="salvar">
            <i class="bi bi-save"></i> Salvar
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-if="mostrar" class="modal-backdrop fade show"></div>
</template>

<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'
import { Atividade, Competencia } from '@/types/tipos'

const props = defineProps<{
  mostrar: boolean
  atividades: Atividade[]
  competenciaParaEditar?: Competencia | null
}>()

const emit = defineEmits<{
  fechar: []
  salvar: [competencia: { descricao: string, atividadesSelecionadas: number[] }]
}>()

const novaCompetencia = ref({ descricao: '' })
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
      import('bootstrap').then(({ Tooltip }) => {
        const modalTooltips = document.querySelectorAll('.modal [data-bs-toggle="tooltip"]')
        modalTooltips.forEach(tooltipEl => {
          new Tooltip(tooltipEl)
        })
      })
    }, 100)
  }
})

function toggleAtividade(id: number) {
  const index = atividadesSelecionadas.value.indexOf(id)
  if (index > -1) {
    atividadesSelecionadas.value.splice(index, 1)
  } else {
    atividadesSelecionadas.value.push(id)
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
  border: 1px solid #dee2e6;
  border-radius: 0.375rem;
  transition: all 0.2s ease-in-out;
  background-color: #fff;
}

.atividade-card-item:hover {
  border-color: #007bff;
  box-shadow: 0 0 0 0.25rem rgba(0, 123, 255, 0.25);
}

.atividade-card-item.checked {
  background-color: #e9f5ff;
  border-color: #007bff;
}

.atividade-card-item .form-check-label {
  cursor: pointer;
  padding: 0.25rem 0;
}

.atividade-card-item.checked .form-check-label {
  font-weight: bold;
  color: #007bff;
}

.atividade-card-item .card-body {
  padding: 0.5rem 0.75rem;
}
</style>