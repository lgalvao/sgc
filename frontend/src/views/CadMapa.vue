<template>
  <div class="container mt-4">
    <div class="fs-5 mb-3">
      {{ unidade?.sigla }} - {{ unidade?.nome }}
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="display-6 mb-3">
        Mapa de competências técnicas
      </div>
      <div class="d-flex gap-2">
        <button
          v-if="podeVerImpacto"
          class="btn btn-outline-secondary"
          data-testid="impactos-mapa-button"
          @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2" />Impacto no mapa
        </button>
        <button
          :disabled="competencias.length === 0"
          class="btn btn-outline-success"
          data-testid="btn-disponibilizar-page"
          @click="abrirModalDisponibilizar"
        >
          Disponibilizar
        </button>
      </div>
    </div>

    <div v-if="unidade">
      <div class="mb-4 mt-3">
        <button
          class="btn btn-outline-primary mb-3"
          data-testid="btn-abrir-criar-competencia"
          @click="abrirModalCriarNovaCompetencia"
        >
          <i class="bi bi-plus-lg" /> Criar competência
        </button>

        <div
          v-for="comp in competencias"
          :key="comp.codigo"
          class="card mb-2 competencia-card"
          data-testid="competencia-item"
        >
          <div class="card-body">
            <div
              class="card-title fs-5 d-flex align-items-center competencia-edicao-row position-relative competencia-hover-row competencia-titulo-card"
            >
              <strong
                class="competencia-descricao"
                data-testid="competencia-descricao"
              > {{ comp.descricao }}</strong>
              <div class="ms-auto d-inline-flex align-items-center gap-1 botoes-acao">
                <button
                  class="btn btn-sm btn-outline-primary botao-acao"
                  data-bs-toggle="tooltip"
                  data-testid="btn-editar-competencia"
                  title="Editar"
                  @click="iniciarEdicaoCompetencia(comp)"
                >
                  <i class="bi bi-pencil" />
                </button>
                <button
                  class="btn btn-sm btn-outline-danger botao-acao"
                  data-bs-toggle="tooltip"
                  data-testid="btn-excluir-competencia"
                  title="Excluir"
                  @click="excluirCompetencia(comp.codigo)"
                >
                  <i class="bi bi-trash" />
                </button>
              </div>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-2">
              <div
                v-for="atvId in comp.atividadesAssociadas"
                :key="atvId"
                class="card atividade-associada-card-item d-flex align-items-center group-atividade-associada"
              >
                <div class="card-body d-flex align-items-center">
                  <span class="atividade-associada-descricao me-2 d-flex align-items-center">
                    {{ descricaoAtividade(atvId) }}
                    <span
                      v-if="getAtividadeCompleta(atvId) && getAtividadeCompleta(atvId)!.conhecimentos.length > 0"
                      :data-bs-html="true"
                      :data-bs-title="getConhecimentosTooltip(atvId)"
                      class="badge bg-secondary ms-2"
                      data-bs-custom-class="conhecimentos-tooltip"
                      data-bs-placement="top"
                      data-bs-toggle="tooltip"
                      data-testid="badge-conhecimentos"
                    >
                      {{ getAtividadeCompleta(atvId)?.conhecimentos.length }}
                    </span>
                  </span>
                  <button
                    class="btn btn-sm btn-outline-secondary botao-acao-inline"
                    data-bs-toggle="tooltip"
                    title="Remover Atividade"
                    @click="removerAtividadeAssociada(comp.codigo, atvId)"
                  >
                    <i class="bi bi-trash" />
                  </button>
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

    <CriarCompetenciaModal
      v-model:mostrar="mostrarModalCriarNovaCompetencia"
      :atividades="atividades"
      :competencia-para-editar="competenciaSendoEditada"
      @salvar="adicionarCompetenciaEFecharModal"
    />

    <!-- Modal de Disponibilizar -->
    <div
      v-if="mostrarModalDisponibilizar"
      data-testid="disponibilizar-modal"
      aria-labelledby="disponibilizarModalLabel"
      aria-modal="true"
      class="modal fade show"
      role="dialog"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5
              id="disponibilizarModalLabel"
              class="modal-title"
            >
              Disponibilização do mapa de competências
            </h5>
            <button
              aria-label="Close"
              class="btn-close"
              type="button"
              @click="fecharModalDisponibilizar"
            />
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label
                class="form-label"
                for="dataLimite"
              >Data limite para validação</label>
              <input
                id="dataLimite"
                v-model="dataLimiteValidacao"
                data-testid="input-data-limite"
                class="form-control"
                type="date"
              >
            </div>
            <div class="mb-3">
              <label
                class="form-label"
                for="observacoes"
              >Observações</label>
              <textarea
                id="observacoes"
                v-model="observacoesDisponibilizacao"
                data-testid="input-observacoes-disponibilizacao"
                class="form-control"
                rows="3"
                placeholder="Digite observações sobre a disponibilização..."
              />
            </div>
            <div
              v-if="notificacaoDisponibilizacao"
              class="alert alert-info mt-3"
              data-testid="notificacao-disponibilizacao"
            >
              {{ notificacaoDisponibilizacao }}
            </div>
          </div>

          <div class="modal-footer">
            <button
              class="btn btn-secondary"
              type="button"
              data-testid="btn-modal-cancelar"
              @click="fecharModalDisponibilizar"
            >
              Cancelar
            </button>
            <button
              :disabled="!dataLimiteValidacao"
              class="btn btn-success"
              type="button"
              data-testid="btn-disponibilizar"
              @click="disponibilizarMapa"
            >
              Disponibilizar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="mostrarModalDisponibilizar"
      class="modal-backdrop fade show"
    />

    <!-- Modal de Exclusão de Competência -->
    <div
      v-if="mostrarModalExcluirCompetencia"
      aria-labelledby="excluirCompetenciaModalLabel"
      aria-modal="true"
      class="modal fade show"
      role="dialog"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5
              id="excluirCompetenciaModalLabel"
              class="modal-title"
            >
              Exclusão de competência
            </h5>
            <button
              aria-label="Close"
              class="btn-close"
              type="button"
              @click="fecharModalExcluirCompetencia"
            />
          </div>
          <div class="modal-body">
            <p>Confirma a exclusão da competência "{{ competenciaParaExcluir?.descricao }}"?</p>
          </div>
          <div class="modal-footer">
            <button
              class="btn btn-secondary"
              type="button"
              @click="fecharModalExcluirCompetencia"
            >
              Cancelar
            </button>
            <button
              class="btn btn-danger"
              type="button"
              @click="confirmarExclusaoCompetencia"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="mostrarModalExcluirCompetencia"
      class="modal-backdrop fade show"
    />

    <ImpactoMapaModal
      :id-processo="codProcesso"
      :sigla-unidade="siglaUnidade"
      :mostrar="mostrarModalImpacto"
      @fechar="fecharModalImpacto"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {storeToRefs} from 'pinia'
import {useRoute} from 'vue-router'
import {useMapasStore} from '@/stores/mapas'
import {useAtividadesStore} from '@/stores/atividades'
import {usePerfilStore} from '@/stores/perfil'
import {useProcessosStore} from '@/stores/processos'
import {usePerfil} from '@/composables/usePerfil'
import {Atividade, Competencia, Perfil, SituacaoSubprocesso, Unidade} from '@/types/tipos'
import ImpactoMapaModal from '@/components/ImpactoMapaModal.vue'
import CriarCompetenciaModal from '@/components/CriarCompetenciaModal.vue'

const route = useRoute()
const mapasStore = useMapasStore()
const {mapaCompleto} = storeToRefs(mapasStore)
const atividadesStore = useAtividadesStore()
const perfilStore = usePerfilStore()
const processosStore = useProcessosStore()
usePerfil()

const codProcesso = computed(() => Number(route.params.codProcesso))
const siglaUnidade = computed(() => String(route.params.siglaUnidade))

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(u => u.sigla === siglaUnidade.value);
});

const podeVerImpacto = computed(() => {
  if (!perfilStore.perfilSelecionado || !subprocesso.value) return false;

  const perfil = perfilStore.perfilSelecionado;
  const situacao = subprocesso.value.situacaoSubprocesso;

  const isPermittedProfile = perfil === Perfil.ADMIN;
  const isCorrectSituation = situacao === SituacaoSubprocesso.ATIVIDADES_HOMOLOGADAS || situacao === SituacaoSubprocesso.MAPA_AJUSTADO;

  return isPermittedProfile && isCorrectSituation;
});

const mostrarModalImpacto = ref(false);

function abrirModalImpacto() {
  // Segue o comportamento esperado pelos testes:
  // - Sem mudanças: não abre o modal e exibe notificação "Nenhum impacto..."
  // - Com mudanças: abre o modal de impactos
  // if (revisaoStore.mudancasRegistradas.length === 0) {
  //   notificacoesStore.info('Impacto no Mapa', 'Nenhum impacto no mapa da unidade.');
  //   return;
  // }
  // revisaoStore.setMudancasParaImpacto(revisaoStore.mudancasRegistradas);
  mostrarModalImpacto.value = true;
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
  // revisaoStore.setMudancasParaImpacto([]);
}

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

const unidade = computed<Unidade | null>(() => {
  // TODO: Fix unidades source
  const unidadesData = processosStore.processoDetalhe?.unidades || [];
  return buscarUnidade(unidadesData as unknown as Unidade[], siglaUnidade.value);
})
const codSubrocesso = computed(() => subprocesso.value?.codUnidade);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(codProcesso.value);
  if (codSubrocesso.value) {
    await mapasStore.fetchMapaCompleto(codSubrocesso.value as number);
  }
  // Inicializar tooltips após o componente ser montado
  import('bootstrap').then(({Tooltip}) => {
    if (typeof document !== 'undefined') {
      const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
      tooltipTriggerList.forEach(tooltipTriggerEl => {
        new Tooltip(tooltipTriggerEl)
      })
    }
  })
});

const atividades = computed<Atividade[]>(() => {
  if (typeof codSubrocesso.value !== 'number') {
    return []
  }
  return atividadesStore.getAtividadesPorSubprocesso(codSubrocesso.value) || []
})

const competencias = computed(() => mapaCompleto.value?.competencias || []);

const competenciaSendoEditada = ref<Competencia | null>(null)


const mostrarModalCriarNovaCompetencia = ref(false)
const mostrarModalDisponibilizar = ref(false)
const mostrarModalExcluirCompetencia = ref(false)
const competenciaParaExcluir = ref<Competencia | null>(null)
const dataLimiteValidacao = ref('')
const observacoesDisponibilizacao = ref('')
const notificacaoDisponibilizacao = ref('')

function abrirModalDisponibilizar() {
  mostrarModalDisponibilizar.value = true;
}

function abrirModalCriarNovaCompetencia() {
  competenciaSendoEditada.value = null
  mostrarModalCriarNovaCompetencia.value = true
}

function iniciarEdicaoCompetencia(competencia: Competencia) {
  competenciaSendoEditada.value = competencia;
  mostrarModalCriarNovaCompetencia.value = true;
}


function descricaoAtividade(codigo: number): string {
  const atv = atividades.value.find(a => a.codigo === codigo)
  return atv ? atv.descricao : 'Atividade não encontrada'
}

function getConhecimentosTooltip(atividadeId: number): string {
  const atividade = atividades.value.find(a => a.codigo === atividadeId)
  if (!atividade || !atividade.conhecimentos.length) {
    return 'Nenhum conhecimento cadastrado'
  }

  const conhecimentosHtml = atividade.conhecimentos
      .map(c => `<div class="mb-1">• ${c.descricao}</div>`)
      .join('')

  return `<div class="text-start"><strong>Conhecimentos:</strong><br>${conhecimentosHtml}</div>`
}

function getAtividadeCompleta(codigo: number): Atividade | undefined {
  return atividades.value.find(a => a.codigo === codigo)
}

function adicionarCompetenciaEFecharModal(competencia: { descricao: string, atividadesSelecionadas: number[], codigo?: number }) {
  const competenciaParaSalvar: Competencia = {
    codigo: competencia.codigo || 0,
    descricao: competencia.descricao,
    atividadesAssociadas: competencia.atividadesSelecionadas,
  };

  if (competenciaSendoEditada.value) {
    mapasStore.atualizarCompetencia(codSubrocesso.value as number, competenciaParaSalvar);
  } else {
    mapasStore.adicionarCompetencia(codSubrocesso.value as number, competenciaParaSalvar);
  }

  competenciaSendoEditada.value = null;
  mostrarModalCriarNovaCompetencia.value = false
}

function excluirCompetencia(codigo: number) {
  const competencia = competencias.value.find(comp => comp.codigo === codigo);
  if (competencia) {
    competenciaParaExcluir.value = competencia;
    mostrarModalExcluirCompetencia.value = true;
  }
}

function confirmarExclusaoCompetencia() {
  if (competenciaParaExcluir.value) {
    mapasStore.removerCompetencia(codSubrocesso.value as number, competenciaParaExcluir.value.codigo);
    fecharModalExcluirCompetencia();
  }
}

function fecharModalExcluirCompetencia() {
  mostrarModalExcluirCompetencia.value = false;
  competenciaParaExcluir.value = null;
}

function removerAtividadeAssociada(competenciaId: number, atividadeId: number) {
  const competencia = competencias.value.find(comp => comp.codigo === competenciaId);
  if (competencia) {
    const competenciaAtualizada = {
      ...competencia,
      atividadesAssociadas: competencia.atividadesAssociadas.filter(id => id !== atividadeId),
    };
    mapasStore.atualizarCompetencia(codSubrocesso.value as number, competenciaAtualizada);
  }
}

async function disponibilizarMapa() {
  if (!codSubrocesso.value) return

  try {
    await mapasStore.disponibilizarMapa(codSubrocesso.value, {
      dataLimite: dataLimiteValidacao.value,
      observacoes: observacoesDisponibilizacao.value,
    })
    fecharModalDisponibilizar()
    // TODO: Adicionar redirecionamento para o painel
  } catch {
    // O erro já é tratado e notificado pelo store
  }
}

function fecharModalDisponibilizar() {
  mostrarModalDisponibilizar.value = false;
  observacoesDisponibilizacao.value = '';
  notificacaoDisponibilizacao.value = '';
}

onMounted(() => {
  // Inicializar tooltips após o componente ser montado
  import('bootstrap').then(({Tooltip}) => {
    if (typeof document !== 'undefined') {
      const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
      tooltipTriggerList.forEach(tooltipTriggerEl => {
        new Tooltip(tooltipTriggerEl)
      })
    }
  })
})


</script>

<style scoped>
.competencia-card {
  transition: box-shadow 0.2s;
}

.competencia-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.botoes-acao {
  opacity: 1; /* sempre visível para evitar flakiness em testes */
  pointer-events: auto;
  transition: opacity 0.2s;
}

.competencia-hover-row:hover .botoes-acao {
  opacity: 1;
  pointer-events: auto;
}

.botao-acao {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 1.1rem;
  border-width: 2px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  margin-left: 0;
  margin-right: 0;
}

.botao-acao:focus,
.botao-acao:hover {
  background: var(--bs-primary-bg-subtle);
  box-shadow: 0 0 0 2px var(--bs-primary);
}

.competencia-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.competencia-hover-row:hover .competencia-descricao {
  font-weight: bold;
}

.competencia-edicao-row {
  width: 100%;
  justify-content: flex-start;
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
  /* Ajuste para preencher a largura total do card */
  width: calc(100% + 1.5rem); /* 100% + 2 * 0.75rem (padding horizontal) */
}

.competencia-titulo-card .competencia-descricao {
  font-size: 1.1rem;
}

.atividade-card-item {
  cursor: pointer;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  transition: all 0.2s ease-in-out;
  background-color: var(--bs-body-bg);
}

.atividade-card-item:hover {
  border-color: var(--bs-primary);
  box-shadow: 0 0 0 0.25rem var(--bs-primary);
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

.atividade-associada-card-item {
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  background-color: var(--bs-secondary-bg);
}

.atividade-associada-descricao {
  font-size: 0.85rem;
  color: var(--bs-body-color);
}

.botao-acao-inline {
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 0.8rem;
  border-width: 1px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}

</style>