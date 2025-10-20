<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="display-6">
        Mapa de competências técnicas
      </div>
      <div class="d-flex gap-2">
        <button
          v-if="podeValidar"
          class="btn btn-outline-warning"
          title="Apresentar sugestões"
          data-testid="apresentar-sugestoes-btn"
          @click="abrirModalSugestoes"
        >
          Apresentar sugestões
        </button>
        <button
          v-if="podeValidar"
          class="btn btn-outline-success"
          title="Validar mapa"
          data-testid="validar-btn"
          @click="abrirModalValidar"
        >
          Validar
        </button>

        <!-- Botão Histórico de análise para CHEFE (CDU-19) -->
        <button
          v-if="podeValidar && temHistoricoAnalise"
          class="btn btn-outline-secondary"
          title="Histórico de análise"
          data-testid="historico-analise-btn"
          @click="verHistorico"
        >
          Histórico de análise
        </button>

        <!-- Botões para GESTOR/ADMIN (CDU-20 - Analisar validação) -->
        <button
          v-if="podeAnalisar"
          v-show="podeVerSugestoes"
          class="btn btn-outline-info"
          title="Ver sugestões"
          data-testid="ver-sugestoes-btn"
          @click="verSugestoes"
        >
          Ver sugestões
        </button>
        <button
          v-if="podeAnalisar"
          class="btn btn-outline-secondary"
          title="Histórico de análise"
          data-testid="historico-analise-btn-gestor"
          @click="verHistorico"
        >
          Histórico de análise
        </button>
        <button
          v-if="podeAnalisar"
          class="btn btn-outline-danger"
          title="Devolver para ajustes"
          data-testid="devolver-ajustes-btn"
          @click="abrirModalDevolucao"
        >
          Devolver para ajustes
        </button>
        <button
          v-if="podeAnalisar"
          class="btn btn-outline-success"
          data-testid="btn-registrar-aceite-homologar"
          title="Aceitar"
          @click="abrirModalAceitar"
        >
          {{ perfilSelecionado === 'ADMIN' ? 'Homologar' : 'Registrar aceite' }}
        </button>
      </div>
    </div>

    <div v-if="unidade">
      <div class="mb-5 d-flex align-items-center">
        <div
          class="fs-5"
          data-testid="unidade-info"
        >
          {{ unidade.sigla }} - {{ unidade.nome }}
        </div>
      </div>

      <div class="mb-4 mt-3">
        <div v-if="competencias.length === 0">
          Nenhuma competência cadastrada.
        </div>
        <div
          v-for="comp in competencias"
          :key="comp.codigo"
          class="card mb-3 competencia-card"
          data-testid="competencia-block"
        >
          <div class="card-body py-2">
            <div
              class="card-title fs-5 d-flex align-items-center position-relative competencia-titulo-card"
            >
              <strong
                class="competencia-descricao"
                data-testid="competencia-descricao"
              > {{ comp.descricao }}</strong>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-2 ps-3">
              <div
                v-for="atvId in comp.atividadesAssociadas"
                :key="atvId"
              >
                <div
                  v-if="getAtividadeCompleta(atvId)"
                  class="card atividade-associada-card-item d-flex flex-column group-atividade-associada"
                  data-testid="atividade-item"
                >
                  <div class="card-body d-flex align-items-center py-1 px-2">
                    <span class="atividade-associada-descricao me-2">{{ getAtividadeCompleta(atvId)?.descricao }}</span>
                  </div>
                  <div class="conhecimentos-atividade px-2 pb-2 ps-3">
                    <span
                      v-for="conhecimento in getConhecimentosAtividade(atvId)"
                      :key="conhecimento.id"
                      class="me-3 mb-1"
                      data-testid="conhecimento-item"
                    >
                      {{ conhecimento.descricao }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <AceitarMapaModal
      :mostrar-modal="mostrarModalAceitar"
      :perfil="perfilSelecionado || undefined"
      @fechar-modal="fecharModalAceitar"
      @confirmar-aceitacao="confirmarAceitacao"
    />

    <!-- Modal para apresentar sugestões (CDU-19) -->
    <div
      class="modal fade"
      :class="{ 'show': mostrarModalSugestoes }"
      :style="{ display: mostrarModalSugestoes ? 'block' : 'none' }"
      tabindex="-1"
      data-testid="modal-apresentar-sugestoes"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5
              class="modal-title"
              data-testid="modal-apresentar-sugestoes-title"
            >
              Apresentar Sugestões
            </h5>
            <button
              type="button"
              class="btn-close"
              data-testid="modal-apresentar-sugestoes-close"
              @click="fecharModalSugestoes"
            />
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label
                for="sugestoesTextarea"
                class="form-label"
              >Sugestões para o mapa de competências:</label>
              <textarea
                id="sugestoesTextarea"
                v-model="sugestoes"
                class="form-control"
                rows="5"
                placeholder="Digite suas sugestões para o mapa de competências..."
                data-testid="sugestoes-textarea"
              />
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-testid="modal-apresentar-sugestoes-cancelar"
              @click="fecharModalSugestoes"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-primary"
              data-testid="modal-apresentar-sugestoes-confirmar"
              @click="confirmarSugestoes"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal para ver sugestões (CDU-20) -->
    <div
      class="modal fade"
      :class="{ 'show': mostrarModalVerSugestoes }"
      :style="{ display: mostrarModalVerSugestoes ? 'block' : 'none' }"
      tabindex="-1"
      data-testid="modal-sugestoes"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5
              class="modal-title"
              data-testid="modal-sugestoes-title"
            >
              Sugestões
            </h5>
            <button
              type="button"
              class="btn-close"
              data-testid="modal-sugestoes-close"
              @click="fecharModalVerSugestoes"
            />
          </div>
          <div
            class="modal-body"
            data-testid="modal-sugestoes-body"
          >
            <div class="mb-3">
              <label class="form-label">Sugestões registradas para o mapa de competências:</label>
              <textarea
                v-model="sugestoesVisualizacao"
                class="form-control"
                rows="5"
                readonly
                data-testid="sugestoes-visualizacao-textarea"
              />
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-testid="modal-sugestoes-fechar"
              @click="fecharModalVerSugestoes"
            >
              Fechar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal para validar mapa (CDU-19) -->
    <div
      class="modal fade"
      :class="{ 'show': mostrarModalValidar }"
      :style="{ display: mostrarModalValidar ? 'block' : 'none' }"
      tabindex="-1"
      data-testid="modal-validar"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5
              class="modal-title"
              data-testid="modal-validar-title"
            >
              Validar Mapa de Competências
            </h5>
            <button
              type="button"
              class="btn-close"
              data-testid="modal-validar-close"
              @click="fecharModalValidar"
            />
          </div>
          <div
            class="modal-body"
            data-testid="modal-validar-body"
          >
            <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-testid="modal-validar-cancelar"
              @click="fecharModalValidar"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-success"
              data-testid="modal-validar-confirmar"
              @click="confirmarValidacao"
            >
              Validar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal para devolver para ajustes (CDU-20) -->
    <div
      class="modal fade"
      :class="{ 'show': mostrarModalDevolucao }"
      :style="{ display: mostrarModalDevolucao ? 'block' : 'none' }"
      tabindex="-1"
      data-testid="modal-devolucao"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5
              class="modal-title"
              data-testid="modal-devolucao-title"
            >
              Devolução
            </h5>
            <button
              type="button"
              class="btn-close"
              data-testid="modal-devolucao-close"
              @click="fecharModalDevolucao"
            />
          </div>
          <div
            class="modal-body"
            data-testid="modal-devolucao-body"
          >
            <p>Confirma a devolução da validação do mapa para ajustes?</p>
            <div class="mb-3">
              <label
                for="observacaoDevolucao"
                class="form-label"
              >Observação:</label>
              <textarea
                id="observacaoDevolucao"
                v-model="observacaoDevolucao"
                class="form-control"
                rows="3"
                placeholder="Digite observações sobre a devolução..."
                data-testid="observacao-devolucao-textarea"
              />
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-testid="modal-devolucao-cancelar"
              @click="fecharModalDevolucao"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-danger"
              data-testid="modal-devolucao-confirmar"
              @click="confirmarDevolucao"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal para histórico de análise (CDU-20) -->
    <div
      class="modal fade"
      :class="{ 'show': mostrarModalHistorico }"
      :style="{ display: mostrarModalHistorico ? 'block' : 'none' }"
      tabindex="-1"
      data-testid="modal-historico"
    >
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5
              class="modal-title"
              data-testid="modal-historico-title"
            >
              Histórico de Análise
            </h5>
            <button
              type="button"
              class="btn-close"
              data-testid="modal-historico-close"
              @click="fecharModalHistorico"
            />
          </div>
          <div
            class="modal-body"
            data-testid="modal-historico-body"
          >
            <table
              class="table table-striped"
              data-testid="tabela-historico"
            >
              <thead>
                <tr>
                  <th>Data/Hora</th>
                  <th>Unidade</th>
                  <th>Resultado</th>
                  <th>Observações</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="item in historicoAnalise"
                  :key="item.codigo"
                  data-testid="historico-item"
                >
                  <td>{{ item.data }}</td>
                  <td>{{ item.unidade }}</td>
                  <td>{{ item.resultado }}</td>
                  <td>{{ item.observacoes }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-testid="modal-historico-fechar"
              @click="fecharModalHistorico"
            >
              Fechar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'

import {useMapasStore} from '@/stores/mapas'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtividadesStore} from "@/stores/atividades";
import {useProcessosStore} from "@/stores/processos";
import {useNotificacoesStore} from "@/stores/notificacoes";
import {useAnalisesStore} from "@/stores/analises";
import {usePerfil} from "@/composables/usePerfil";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {Atividade, Competencia, Conhecimento, SituacaoSubprocesso, Unidade} from '@/types/tipos';
import AceitarMapaModal from '@/components/AceitarMapaModal.vue';

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.siglaUnidade as string)
const idProcesso = computed(() => Number(route.params.idProcesso))
const unidadesStore = useUnidadesStore()
const mapaStore = useMapasStore()
const atividadesStore = useAtividadesStore()
const processosStore = useProcessosStore()
const notificacoesStore = useNotificacoesStore()
const analisesStore = useAnalisesStore()
const subprocessosStore = useSubprocessosStore()
const {perfilSelecionado} = usePerfil()

// Estados reativos para o modal
const mostrarModalAceitar = ref(false)
const mostrarModalSugestoes = ref(false)
const mostrarModalVerSugestoes = ref(false)
const mostrarModalValidar = ref(false)
const mostrarModalDevolucao = ref(false)
const mostrarModalHistorico = ref(false)
const sugestoes = ref('')
const sugestoesVisualizacao = ref('')
const observacaoDevolucao = ref('')

const unidade = computed<Unidade | null>(() => {
  function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | null {
    for (const unidade of unidades) {
      if (unidade.sigla === sigla) return unidade
      if (unidade.filhas && unidade.filhas.length) {
        const encontrada = buscarUnidade(unidade.filhas, sigla)
        if (encontrada) return encontrada
      }
    }
    return null
  }

  return buscarUnidade(unidadesStore.unidades as Unidade[], sigla.value)
})

const idSubprocesso = computed(() => subprocesso.value?.codUnidade);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(idProcesso.value);
});

const atividades = computed<Atividade[]>(() => {
  if (typeof idSubprocesso.value !== 'number') {
    return []
  }
  return atividadesStore.getAtividadesPorSubprocesso(idSubprocesso.value) || []
})

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(idProcesso.value);
  // Correção temporária: usando idProcesso como idSubprocesso
  await mapaStore.fetchMapaCompleto(idProcesso.value);
});

const mapa = computed(() => {
  if (unidade.value?.codigo) {
    return mapaStore.mapaCompleto;
  }
  return null;
})
const competencias = computed<Competencia[]>(() => mapa.value ? mapa.value.competencias : [])

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(u => u.sigla === sigla.value);
})

// Computed para determinar quais botões mostrar baseado no perfil e situação
const podeValidar = computed(() => {
  return perfilSelecionado.value === 'CHEFE' &&
      subprocesso.value?.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CONCLUIDO
})

const podeAnalisar = computed(() => {
  return (perfilSelecionado.value === 'GESTOR' || perfilSelecionado.value === 'ADMIN') &&
      (subprocesso.value?.situacaoSubprocesso === SituacaoSubprocesso.MAPA_VALIDADO || subprocesso.value?.situacaoSubprocesso === SituacaoSubprocesso.AGUARDANDO_AJUSTES_MAPA)
})

const podeVerSugestoes = computed(() => {
  return subprocesso.value?.situacaoSubprocesso === SituacaoSubprocesso.AGUARDANDO_AJUSTES_MAPA
})

const temHistoricoAnalise = computed(() => {
  return historicoAnalise.value.length > 0
})

const historicoAnalise = computed(() => {
  if (!idSubprocesso.value) return []

  return analisesStore.getAnalisesPorSubprocesso(idSubprocesso.value).map(analise => ({
    codigo: analise.codigo,
    data: new Date(analise.dataHora).toLocaleString('pt-BR'),
    unidade: (analise as any).unidadeSigla || (analise as any).unidade,
    resultado: analise.resultado,
    observacoes: analise.observacoes || ''
  }))
})

function getAtividadeCompleta(id: number): Atividade | undefined {
  return atividades.value.find(a => a.codigo === id);
}

function getConhecimentosAtividade(id: number): Conhecimento[] {
  const atividade = getAtividadeCompleta(id)
  return atividade ? atividade.conhecimentos : []
}

function abrirModalAceitar() {
  mostrarModalAceitar.value = true
}

function fecharModalAceitar() {
  mostrarModalAceitar.value = false
}

function abrirModalSugestoes() {
  // Pré-preencher com sugestões existentes se houver
  if ((mapa.value as any)?.sugestoes) {
    sugestoes.value = (mapa.value as any).sugestoes
  }
  mostrarModalSugestoes.value = true
}

function fecharModalSugestoes() {
  mostrarModalSugestoes.value = false
  sugestoes.value = ''
}

function fecharModalVerSugestoes() {
  mostrarModalVerSugestoes.value = false
  sugestoesVisualizacao.value = ''
}

function abrirModalValidar() {
  mostrarModalValidar.value = true
}

function fecharModalValidar() {
  mostrarModalValidar.value = false
}

function abrirModalDevolucao() {
  mostrarModalDevolucao.value = true
}

function fecharModalDevolucao() {
  mostrarModalDevolucao.value = false
  observacaoDevolucao.value = ''
}

function abrirModalHistorico() {
  mostrarModalHistorico.value = true
}

function fecharModalHistorico() {
  mostrarModalHistorico.value = false
}

function verSugestoes() {
  // Buscar sugestões do subprocesso
  const sugestoes = (mapa.value as any)?.sugestoes || "Nenhuma sugestão registrada.";

  // Mostrar modal de visualização com sugestões
  mostrarModalVerSugestoes.value = true;
  sugestoesVisualizacao.value = sugestoes;
}

function verHistorico() {
  // Abrir modal de histórico
  abrirModalHistorico();
}

async function confirmarSugestoes() {
  try {
    await processosStore.apresentarSugestoes()

    fecharModalSugestoes()

    notificacoesStore.sucesso(
        'Sugestões apresentadas',
        'Sugestões submetidas para análise da unidade superior'
    )

    await router.push({
      name: 'Subprocesso',
      params: {idProcesso: idProcesso.value, siglaUnidade: sigla.value}
    })

  } catch {
    notificacoesStore.erro(
        'Erro ao apresentar sugestões',
        'Ocorreu um erro. Tente novamente.'
    )
  }
}

async function confirmarValidacao() {
  try {
    await processosStore.validarMapa()

    fecharModalValidar()

    notificacoesStore.sucesso(
        'Mapa validado',
        'Mapa validado e submetido para análise da unidade superior'
    )

    await router.push({
      name: 'Subprocesso',
      params: {idProcesso: idProcesso.value, siglaUnidade: sigla.value}
    })

  } catch {
    notificacoesStore.erro(
        'Erro ao validar mapa',
        'Ocorreu um erro. Tente novamente.'
    )
  }
}

async function confirmarAceitacao(observacoes?: string) {
  if (!idSubprocesso.value) return;

  const perfil = perfilSelecionado.value;
  const isHomologacao = perfil === 'ADMIN';

  if (isHomologacao) {
    await subprocessosStore.homologarRevisaoCadastro(idSubprocesso.value, {observacoes: observacoes || ''}); // Adicionar observacoes
  } else {
    await subprocessosStore.aceitarRevisaoCadastro(idSubprocesso.value, {observacoes: observacoes || ''}); // Adicionar observacoes
  }

  fecharModalAceitar();
  await router.push({name: 'Painel'});
}


async function confirmarDevolucao() {
  if (!idSubprocesso.value) return;

  await subprocessosStore.devolverRevisaoCadastro(idSubprocesso.value, {
    motivo: '', // Adicionar motivo
    observacoes: observacaoDevolucao.value,
  });

  fecharModalDevolucao();
  await router.push({name: 'Painel'});
}
</script>

<style scoped>
.competencia-card {
  transition: box-shadow 0.2s;
}

.competencia-titulo-card {
  background: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
  width: calc(100% + 1.5rem);
}

.competencia-titulo-card .competencia-descricao {
  font-size: 1.1rem;
}

.atividade-associada-card-item {
  background-color: transparent;
}

.atividade-associada-descricao {
  color: var(--bs-body-color);
  font-weight: bold;
}

.conhecimentos-atividade {
  margin-top: 0.25rem;
  font-size: 0.9rem;
}

.conhecimentos-atividade {
  border-radius: 0.25rem;
  font-weight: normal;
}
</style>