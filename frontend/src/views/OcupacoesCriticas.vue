<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h2 class="mb-0">
          Ocupações Críticas
        </h2>
        <small class="text-muted">{{ siglaUnidade }} - {{ nomeUnidade }}</small>
      </div>
      <div class="d-flex gap-2">
        <button
          class="btn btn-outline-success"
          @click="finalizarIdentificacao"
        >
          <i class="bi bi-check-circle me-2" />Finalizar Identificação
        </button>
      </div>
    </div>

    <div class="alert alert-info">
      <i class="bi bi-info-circle me-2" />
      Identifique as ocupações críticas da unidade baseadas nas competências avaliadas no diagnóstico.
    </div>

    <!-- Lista de ocupações críticas identificadas -->
    <div class="card mb-4">
      <div class="card-header">
        <h5 class="card-title mb-0">
          Ocupações Críticas Identificadas
        </h5>
      </div>
      <div class="card-body">
        <div
          v-if="ocupacoesCriticas.length === 0"
          class="text-muted"
        >
          Nenhuma ocupação crítica identificada ainda.
        </div>
        <div
          v-for="(ocupacao, index) in ocupacoesCriticas"
          :key="index"
          class="border rounded p-3 mb-3"
        >
          <div class="d-flex justify-content-between align-items-start">
            <div class="flex-grow-1">
              <h6 class="mb-2">
                {{ ocupacao.nome }}
              </h6>
              <p class="mb-2 text-muted">
                {{ ocupacao.descricao }}
              </p>
              <div class="mb-2">
                <strong>Competências críticas associadas:</strong>
                <div class="d-flex flex-wrap gap-1 mt-1">
                  <span
                    v-for="comp in ocupacao.competenciasCriticas"
                    :key="comp"
                    class="badge bg-warning text-dark"
                  >{{ comp }}</span>
                </div>
              </div>
              <small class="text-muted">Nível de criticidade: {{ ocupacao.nivelCriticidade }}/5</small>
            </div>
            <button
              class="btn btn-sm btn-outline-danger"
              @click="removerOcupacao(index)"
            >
              <i class="bi bi-trash" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Formulário para adicionar ocupação crítica -->
    <div class="card">
      <div class="card-header">
        <h5 class="card-title mb-0">
          Adicionar Ocupação Crítica
        </h5>
      </div>
      <div class="card-body">
        <form @submit.prevent="adicionarOcupacao">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Nome da Ocupação</label>
              <input
                v-model="novaOcupacao.nome"
                type="text"
                class="form-control"
                required
              >
            </div>
            <div class="col-md-6">
              <label class="form-label">Nível de Criticidade</label>
              <select
                v-model="novaOcupacao.nivelCriticidade"
                class="form-select"
                required
              >
                <option value="1">
                  1 - Baixo
                </option>
                <option value="2">
                  2 - Baixo-Médio
                </option>
                <option value="3">
                  3 - Médio
                </option>
                <option value="4">
                  4 - Alto
                </option>
                <option value="5">
                  5 - Muito Alto
                </option>
              </select>
            </div>
            <div class="col-12">
              <label class="form-label">Descrição</label>
              <textarea
                v-model="novaOcupacao.descricao"
                class="form-control"
                rows="3"
                required
              />
            </div>
            <div class="col-12">
              <label class="form-label">Competências Críticas Associadas</label>
              <div class="border rounded p-3">
                <div
                  v-for="competencia in competencias"
                  :key="competencia.codigo"
                  class="form-check"
                >
                  <input
                    :id="'comp-' + competencia.codigo"
                    v-model="novaOcupacao.competenciasCriticas"
                    :value="competencia.descricao"
                    type="checkbox"
                    class="form-check-input"
                  >
                  <label
                    :for="'comp-' + competencia.codigo"
                    class="form-check-label"
                  >
                    {{ competencia.descricao }}
                  </label>
                </div>
              </div>
            </div>
            <div class="col-12">
              <button
                type="submit"
                class="btn btn-primary"
              >
                <i class="bi bi-plus-circle me-2" />Adicionar Ocupação
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal de confirmação -->
    <div
      v-if="mostrarModalConfirmacao"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Finalizar Identificação
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalConfirmacao"
            />
          </div>
          <div class="modal-body">
            <p>Confirma a finalização da identificação de ocupações críticas? Esta ação não poderá ser desfeita.</p>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalConfirmacao"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-success"
              @click="confirmarFinalizacao"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="mostrarModalConfirmacao"
      class="modal-backdrop fade show"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useMapasStore} from '@/stores/mapas'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {usePerfilStore} from '@/stores/perfil'
import {Competencia, MapaCompleto} from '@/types/tipos'

const route = useRoute()
const router = useRouter()
const mapasStore = useMapasStore()
const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()
const notificacoesStore = useNotificacoesStore()
const perfilStore = usePerfilStore()

const idProcesso = computed(() => Number(route.params.idProcesso))
const siglaUnidade = computed(() => route.params.siglaUnidade as string)

const unidade = computed(() => unidadesStore.pesquisarUnidade(siglaUnidade.value))
const nomeUnidade = computed(() => unidade.value?.nome || '')

const processoAtual = computed(() => processosStore.processoDetalhe);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(idProcesso.value);
  // Correção temporária: usando idProcesso como idSubprocesso
  await mapasStore.fetchMapaCompleto(idProcesso.value);
});

const mapa = computed<MapaCompleto | null>(() => {
  return mapasStore.mapaCompleto;
})

const competencias = computed<Competencia[]>(() => {
  return mapa.value?.competencias || []
})

// Estado das ocupações críticas
const ocupacoesCriticas = ref<Array<{
  nome: string
  descricao: string
  nivelCriticidade: number
  competenciasCriticas: string[]
}>>([])

const novaOcupacao = ref({
  nome: '',
  descricao: '',
  nivelCriticidade: 3,
  competenciasCriticas: [] as string[]
})

// Modal
const mostrarModalConfirmacao = ref(false)

function adicionarOcupacao() {
  if (!novaOcupacao.value.nome.trim() || !novaOcupacao.value.descricao.trim()) {
    notificacoesStore.erro('Dados incompletos', 'Preencha nome e descrição da ocupação.')
    return
  }

  ocupacoesCriticas.value.push({
    nome: novaOcupacao.value.nome,
    descricao: novaOcupacao.value.descricao,
    nivelCriticidade: novaOcupacao.value.nivelCriticidade,
    competenciasCriticas: [...novaOcupacao.value.competenciasCriticas]
  })

  // Limpar formulário
  novaOcupacao.value = {
    nome: '',
    descricao: '',
    nivelCriticidade: 3,
    competenciasCriticas: []
  }

  notificacoesStore.sucesso('Ocupação adicionada', 'Ocupação crítica adicionada com sucesso!')
}

function removerOcupacao(index: number) {
  ocupacoesCriticas.value.splice(index, 1)
  notificacoesStore.sucesso('Ocupação removida', 'Ocupação crítica removida com sucesso!')
}

function finalizarIdentificacao() {
  mostrarModalConfirmacao.value = true
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false
}

function confirmarFinalizacao() {
  if (!processoAtual.value) return

  // Registrar movimentação
  const subprocesso = processoAtual.value?.unidades.find(u => u.sigla === siglaUnidade.value);
  if (subprocesso && unidade.value) {
    const usuario = `${perfilStore.perfilSelecionado} - ${perfilStore.unidadeSelecionada}`;
    processosStore.addMovement({
      usuario: usuario,
      unidadeOrigem: unidade.value,
      unidadeDestino: {codigo: 0, nome: 'SEDOC', sigla: 'SEDOC'},
      descricao: 'Identificação de ocupações críticas finalizada'
    });
  }

  // A criação de alertas agora é responsabilidade do backend

  notificacoesStore.sucesso(
      'Identificação finalizada',
      'A identificação de ocupações críticas foi concluída com sucesso!'
  )

  fecharModalConfirmacao()
  router.push('/painel')
}
</script>

<style scoped>
.card {
  transition: box-shadow 0.2s;
}

.card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.border {
  border-color: var(--bs-border-color) !important;
}
</style>