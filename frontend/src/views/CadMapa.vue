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
          @click="() => abrirModalCriarNovaCompetencia()"
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

    <!-- Modal de Criar Nova Competência -->
    <div
      v-if="mostrarModalCriarNovaCompetencia"
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
              @click="fecharModalCriarNovaCompetencia"
            />
          </div>
          <div class="modal-body">
            <!-- Conteúdo do card movido para cá -->
            <div class="mb-4">
              <h5>Descrição</h5>
              <div class="mb-2">
                <textarea
                  v-model="novaCompetencia.descricao"
                  class="form-control"
                  data-testid="input-nova-competencia"
                  placeholder="Descreva a competência"
                  rows="3"
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
                  :data-testid="atividadesSelecionadas.includes(atividade.codigo) ? 'atividade-associada' : 'atividade-nao-associada'"
                  @click="toggleAtividade(atividade.codigo)"
                >
                  <div class="card-body d-flex align-items-center">
                    <input
                      :id="`atv-${atividade.codigo}`"
                      v-model="atividadesSelecionadas"
                      :value="atividade.codigo"
                      class="form-check-input me-2"
                      data-testid="atividade-checkbox"
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
                        data-testid="badge-conhecimentos"
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
              @click="fecharModalCriarNovaCompetencia"
            >
              Cancelar
            </button>
            <button
              :disabled="atividadesSelecionadas.length === 0 || !novaCompetencia.descricao"
              class="btn btn-primary"
              data-bs-toggle="tooltip"
              data-testid="btn-criar-competencia"
              title="Criar Competência"
              type="button"
              @click="adicionarCompetenciaEFecharModal"
            >
              <i
                class="bi bi-save"
              /> Salvar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="mostrarModalCriarNovaCompetencia"
      class="modal-backdrop fade show"
    />

    <!-- Modal de Disponibilizar -->
    <div
      v-if="mostrarModalDisponibilizar"
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
                data-testid="input-observacoes"
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
      :id-processo="idProcesso"
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
import {useNotificacoesStore} from '@/stores/notificacoes'
import {usePerfilStore} from '@/stores/perfil'
import {useProcessosStore} from '@/stores/processos'
import {useRevisaoStore} from '@/stores/revisao'
import {useUnidadesStore} from '@/stores/unidades'
import {Atividade, Competencia, Perfil, SituacaoSubprocesso, Unidade} from '@/types/tipos'
import ImpactoMapaModal from '@/components/ImpactoMapaModal.vue'

const route = useRoute()
const mapasStore = useMapasStore()
const {mapaCompleto} = storeToRefs(mapasStore)
const atividadesStore = useAtividadesStore()
const notificacoesStore = useNotificacoesStore()
const perfilStore = usePerfilStore()
const processosStore = useProcessosStore()
const revisaoStore = useRevisaoStore()
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)

const idProcesso = computed(() => Number(route.params.idProcesso))
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
  if (revisaoStore.mudancasRegistradas.length === 0) {
    notificacoesStore.info('Impacto no Mapa', 'Nenhum impacto no mapa da unidade.');
    return;
  }
  revisaoStore.setMudancasParaImpacto(revisaoStore.mudancasRegistradas);
  mostrarModalImpacto.value = true;
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
  revisaoStore.setMudancasParaImpacto([]);
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

const unidade = computed<Unidade | null>(() => buscarUnidade(unidades.value as Unidade[], siglaUnidade.value))
const idSubprocesso = computed(() => subprocesso.value?.codUnidade);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(idProcesso.value);
  if (idSubprocesso.value) {
    await mapasStore.fetchMapaCompleto(idSubprocesso.value as number);
  }
  // Inicializar tooltips após o componente ser montado
  import('bootstrap').then(({Tooltip}) => {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    tooltipTriggerList.forEach(tooltipTriggerEl => {
      new Tooltip(tooltipTriggerEl)
    })
  })
});

const atividades = computed<Atividade[]>(() => {
  if (typeof idSubprocesso.value !== 'number') {
    return []
  }
  return atividadesStore.getAtividadesPorSubprocesso(idSubprocesso.value) || []
})

const competencias = computed(() => mapaCompleto.value?.competencias || []);
const atividadesSelecionadas = ref<number[]>([])
const novaCompetencia = ref({descricao: ''})

function toggleAtividade(codigo: number) {
  const index = atividadesSelecionadas.value.indexOf(codigo);
  if (index > -1) {
    atividadesSelecionadas.value.splice(index, 1);
  } else {
    atividadesSelecionadas.value.push(codigo);
  }
}


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

function abrirModalCriarNovaCompetencia(competenciaParaEditar?: Competencia) {
  mostrarModalCriarNovaCompetencia.value = true;
  if (competenciaParaEditar) {
    novaCompetencia.value.descricao = competenciaParaEditar.descricao;
    atividadesSelecionadas.value = [...competenciaParaEditar.atividadesAssociadas];
    competenciaSendoEditada.value = competenciaParaEditar;
  } else {
    novaCompetencia.value.descricao = '';
    atividadesSelecionadas.value = [];
    competenciaSendoEditada.value = null;
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

function fecharModalCriarNovaCompetencia() {
  mostrarModalCriarNovaCompetencia.value = false;
}


function iniciarEdicaoCompetencia(competencia: Competencia) {
  competenciaSendoEditada.value = competencia;
  abrirModalCriarNovaCompetencia(competencia);
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

function getConhecimentosModal(atividade: Atividade): string {
  if (!atividade.conhecimentos.length) {
    return 'Nenhum conhecimento'
  }

  const conhecimentosHtml = atividade.conhecimentos
      .map(c => `<div class="mb-1">• ${c.descricao}</div>`)
      .join('')

  return `<div class="text-start"><strong>Conhecimentos:</strong><br>${conhecimentosHtml}</div>`
}

function adicionarCompetenciaEFecharModal() {
  if (!novaCompetencia.value.descricao || atividadesSelecionadas.value.length === 0) return;

  const competencia: Competencia = {
    codigo: competenciaSendoEditada.value?.codigo || 0,
    descricao: novaCompetencia.value.descricao,
    atividadesAssociadas: atividadesSelecionadas.value,
  };

  if (competenciaSendoEditada.value) {
    mapasStore.atualizarCompetencia(idSubprocesso.value as number, competencia);
  } else {
    mapasStore.adicionarCompetencia(idSubprocesso.value as number, competencia);
  }

  // Limpar formulário
  novaCompetencia.value.descricao = '';
  atividadesSelecionadas.value = [];
  competenciaSendoEditada.value = null;

  fecharModalCriarNovaCompetencia();
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
    mapasStore.removerCompetencia(idSubprocesso.value as number, competenciaParaExcluir.value.codigo);
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
    mapasStore.atualizarCompetencia(idSubprocesso.value as number, competenciaAtualizada);
  }
}

function formatarData(data: string): string {
  if (!data) return ''
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}

function disponibilizarMapa() {
  if (!mapaCompleto.value || !unidade.value) {
    notificacaoDisponibilizacao.value = 'Erro: Mapa ou unidade não encontrados.'
    return
  }

  const currentUnidade = unidade.value;

  // Validações conforme plano
  const competenciasSemAtividades = mapaCompleto.value.competencias.filter(comp => comp.atividadesAssociadas.length === 0);
  if (competenciasSemAtividades.length > 0) {
    notificacaoDisponibilizacao.value = `Erro: As seguintes competências não têm atividades associadas: ${competenciasSemAtividades.map(c => c.descricao).join(', ')}`;
    return;
  }

  const atividadesIds = atividades.value.map(a => a.codigo);
  const atividadesAssociadas = mapaCompleto.value.competencias.flatMap(comp => comp.atividadesAssociadas);
  const atividadesNaoAssociadas = atividadesIds.filter(id => !atividadesAssociadas.includes(id));

  if (atividadesNaoAssociadas.length > 0) {
    const descricoesNaoAssociadas = atividadesNaoAssociadas.map(id => descricaoAtividade(id)).join(', ');
    notificacaoDisponibilizacao.value = `Erro: As seguintes atividades não estão associadas a nenhuma competência: ${descricoesNaoAssociadas}`;
    return;
  }

  // Alterar situação do subprocesso para 'Mapa disponibilizado' (CDU-17)
  const sub = subprocesso.value;
  if (sub) {
    sub.situacaoSubprocesso = SituacaoSubprocesso.MAPEAMENTO_CONCLUIDO;
    sub.dataLimite = dataLimiteValidacao.value;
    if (observacoesDisponibilizacao.value) {
      // O DTO não tem mais um campo de observações direto, isso precisaria ser tratado
      // em uma movimentação ou em um campo específico se existir no backend.
    }

    // Registrar movimentação
    processosStore.addMovement({
      unidadeOrigem: {codigo: 0, nome: 'SEDOC', sigla: 'SEDOC'},
      unidadeDestino: currentUnidade,
      descricao: 'Disponibilização do mapa de competências'
    });
  }


  // Simular notificações por e-mail
  const notificacoesStore = useNotificacoesStore();
  notificacoesStore.email(
      'SGC: Mapa de competências disponibilizado',
      `Responsável pela ${currentUnidade.sigla}`,
      `Prezado(a) responsável pela ${currentUnidade.sigla},\n\nO mapa de competências foi disponibilizado para validação até ${formatarData(dataLimiteValidacao.value)}.\n\nAcompanhe o processo no sistema SGC.`
  );

  // Simular notificações para superiores
  notificacoesStore.email(
      `SGC: Mapa de competências disponibilizado - ${currentUnidade.sigla}`,
      'Unidades superiores',
      `Prezado(a) responsável,\n\nO mapa de competências da ${currentUnidade.sigla} foi disponibilizado para validação.\n\nAcompanhe o processo no sistema SGC.`
  );

  // A criação de alertas agora é responsabilidade do backend

  // Excluir sugestões e histórico de análise (CDU-17 item 19)
  // A ser implementado no backend

  notificacaoDisponibilizacao.value = `Mapa de competências da unidade ${currentUnidade.sigla} foi disponibilizado para validação até ${formatarData(dataLimiteValidacao.value)}.`;

  // Fechar modal e redirecionar para Painel
  setTimeout(() => {
    fecharModalDisponibilizar();
    // Aqui seria o redirecionamento para Painel, mas como é simulação, apenas fechamos o modal
  }, 2000);
}

function fecharModalDisponibilizar() {
  mostrarModalDisponibilizar.value = false;
  observacoesDisponibilizacao.value = '';
  notificacaoDisponibilizacao.value = '';
}

onMounted(() => {
  // Inicializar tooltips após o componente ser montado
  import('bootstrap').then(({Tooltip}) => {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    tooltipTriggerList.forEach(tooltipTriggerEl => {
      new Tooltip(tooltipTriggerEl)
    })
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