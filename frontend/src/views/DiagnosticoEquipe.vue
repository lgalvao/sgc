<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h2 class="mb-0">
          Diagnóstico da Equipe
        </h2>
        <small class="text-muted">{{ siglaUnidade }} - {{ nomeUnidade }}</small>
      </div>
      <div class="d-flex gap-2">
        <button
          class="btn btn-outline-success"
          @click="finalizarDiagnostico"
        >
          <i class="bi bi-check-circle me-2" />Finalizar Diagnóstico
        </button>
      </div>
    </div>

    <div class="alert alert-info">
      <i class="bi bi-info-circle me-2" />
      Nesta etapa, os servidores da unidade devem avaliar a importância e o domínio das competências da unidade.
    </div>

    <!-- Lista de competências para avaliação -->
    <div
      v-if="competencias.length > 0"
      class="row"
    >
      <div
        v-for="competencia in competencias"
        :key="competencia.codigo"
        class="col-md-6 mb-4"
      >
        <div class="card h-100">
          <div class="card-header">
            <h5 class="card-title mb-0">
              {{ competencia.descricao }}
            </h5>
          </div>
          <div class="card-body">
            <div class="mb-3">
              <label class="form-label fw-bold">Importância da competência:</label>
              <select
                v-model="avaliacoes[competencia.codigo].importancia"
                class="form-select"
              >
                <option value="1">
                  1 - Muito baixa
                </option>
                <option value="2">
                  2 - Baixa
                </option>
                <option value="3">
                  3 - Média
                </option>
                <option value="4">
                  4 - Alta
                </option>
                <option value="5">
                  5 - Muito alta
                </option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label fw-bold">Domínio da competência pela equipe:</label>
              <select
                v-model="avaliacoes[competencia.codigo].dominio"
                class="form-select"
              >
                <option value="1">
                  1 - Muito baixo
                </option>
                <option value="2">
                  2 - Baixo
                </option>
                <option value="3">
                  3 - Médio
                </option>
                <option value="4">
                  4 - Alto
                </option>
                <option value="5">
                  5 - Muito alto
                </option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label fw-bold">Observações:</label>
              <textarea
                v-model="avaliacoes[competencia.codigo].observacoes"
                class="form-control"
                rows="2"
                placeholder="Comentários sobre esta competência..."
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-else
      class="alert alert-warning"
    >
      <i class="bi bi-exclamation-triangle me-2" />
      Nenhum mapa de competências disponível para diagnóstico.
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
              Finalizar Diagnóstico
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalConfirmacao"
            />
          </div>
          <div class="modal-body">
            <p>Confirma a finalização do diagnóstico da equipe? Esta ação não poderá ser desfeita.</p>
            <div
              v-if="avaliacoesPendentes.length > 0"
              class="alert alert-warning"
            >
              <strong>Atenção:</strong> As seguintes competências ainda não foram avaliadas:
              <ul class="mb-0 mt-2">
                <li
                  v-for="comp in avaliacoesPendentes"
                  :key="comp.codigo"
                >
                  {{ comp.descricao }}
                </li>
              </ul>
            </div>
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
import {Competencia, MapaCompleto, Servidor, Subprocesso} from '@/types/tipos'
import { usePerfil } from '@/composables/usePerfil'

const route = useRoute()
const router = useRouter()
const mapasStore = useMapasStore()
const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()
const notificacoesStore = useNotificacoesStore()
const perfilStore = usePerfilStore()
const { servidorLogado } = usePerfil()

const idProcesso = computed(() => Number(route.params.idProcesso))
const siglaUnidade = computed(() => route.params.siglaUnidade as string)

const unidade = computed(() => unidadesStore.pesquisarUnidade(siglaUnidade.value))
const nomeUnidade = computed(() => unidade.value?.nome || '')

const processoAtual = computed(() => processosStore.processoDetalhe);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(idProcesso.value);
  // Correção temporária: usando idProcesso como codSubrocesso
  await mapasStore.fetchMapaCompleto(idProcesso.value);
  competencias.value.forEach(comp => {
    if (!avaliacoes.value[comp.codigo]) {
      avaliacoes.value[comp.codigo] = {
        importancia: 3,
        dominio: 3,
        observacoes: ''
      }
    }
  })
});

const mapa = computed<MapaCompleto | null>(() => {
  return mapasStore.mapaCompleto;
})

const competencias = computed<Competencia[]>(() => {
  return mapa.value?.competencias || []
})

// Estado das avaliações
const avaliacoes = ref<Record<string, {
  importancia: number
  dominio: number
  observacoes: string
}>>({})

// Modal
const mostrarModalConfirmacao = ref(false)

// Inicializar avaliações
onMounted(() => {
  competencias.value.forEach(comp => {
    if (!avaliacoes.value[comp.codigo]) {
      avaliacoes.value[comp.codigo] = {
        importancia: 3,
        dominio: 3,
        observacoes: ''
      }
    }
  })
})

const avaliacoesPendentes = computed(() => {
  return competencias.value.filter(comp => {
    const aval = avaliacoes.value[comp.codigo]
    return !aval || aval.importancia === 0 || aval.dominio === 0
  })
})

function finalizarDiagnostico() {
  mostrarModalConfirmacao.value = true
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false
}

function confirmarFinalizacao() {
  if (!processoAtual.value) return

  // Registrar movimentação
  const subprocesso = processoAtual.value?.unidades.find(u => u.sigla === siglaUnidade.value);
  if (subprocesso && unidade.value && servidorLogado.value) {
    const MOCK_SERVER: Servidor = {
      ...servidorLogado.value,
      unidade: unidade.value
    }
    const MOCK_SUBPROCESSO: Subprocesso = {
      ...subprocesso,
      codigo: subprocesso.codSubprocesso,
      unidade: unidade.value,
      situacao: subprocesso.situacaoSubprocesso,
      dataFimEtapa1: '',
      dataLimiteEtapa2: '',
      atividades: []
    }
    processosStore.addMovement({
      subprocesso: MOCK_SUBPROCESSO,
      usuario: MOCK_SERVER,
      unidadeOrigem: unidade.value,
      unidadeDestino: { codigo: 0, nome: 'SEDOC', sigla: 'SEDOC' },
      descricao: 'Diagnóstico da equipe finalizado',
    });
  }

  // A criação de alertas agora é responsabilidade do backend

  notificacoesStore.sucesso(
      'Diagnóstico finalizado',
      'O diagnóstico da equipe foi concluído com sucesso!'
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
</style>