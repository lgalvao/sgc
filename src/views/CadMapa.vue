<template>
  <div class="container mt-4">
    <div class="fs-5 w-100 mb-3">
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
          @click="finalizarEdicao"
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
          :key="comp.id"
          class="card mb-2 competencia-card"
          data-testid="competencia-item"
        >
          <div class="card-body py-2">
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
                  @click="excluirCompetencia(comp.id)"
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
                <div class="card-body d-flex align-items-center py-1 px-2">
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
                    @click="removerAtividadeAssociada(comp.id, atvId)"
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
                  :key="atividade.id"
                  :class="{ checked: atividadesSelecionadas.includes(atividade.id) }"
                  class="card atividade-card-item"
                  :data-testid="atividadesSelecionadas.includes(atividade.id) ? 'atividade-associada' : 'atividade-nao-associada'"
                  @click="toggleAtividade(atividade.id)"
                >
                  <div class="card-body d-flex align-items-center py-2">
                    <input
                      :id="`atv-${atividade.id}`"
                      v-model="atividadesSelecionadas"
                      :value="atividade.id"
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
              @click="fecharModalDisponibilizar"
            >
              Cancelar
            </button>
            <button
              :disabled="!dataLimiteValidacao"
              class="btn btn-success"
              type="button"
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
import {computed, onMounted, ref, watch} from 'vue'
import {storeToRefs} from 'pinia'
import {useRoute} from 'vue-router'
import {useMapasStore} from '@/stores/mapas'
import {useAtividadesStore} from '@/stores/atividades'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {usePerfilStore} from '@/stores/perfil'
import {useProcessosStore} from '@/stores/processos'
import {useRevisaoStore} from '@/stores/revisao'
import {useUnidadesStore} from '@/stores/unidades'
import {useAlertasStore} from '@/stores/alertas'
import {Atividade, Competencia, Perfil, Subprocesso, Unidade} from '@/types/tipos'
import ImpactoMapaModal from '@/components/ImpactoMapaModal.vue'
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes';

const route = useRoute()
const mapasStore = useMapasStore()
const {mapas} = storeToRefs(mapasStore)
const atividadesStore = useAtividadesStore()
const notificacoesStore = useNotificacoesStore()
const perfilStore = usePerfilStore()
const processosStore = useProcessosStore()
const revisaoStore = useRevisaoStore()
const unidadesStore = useUnidadesStore()
const alertasStore = useAlertasStore()
const {unidades} = storeToRefs(unidadesStore)

const idProcesso = computed(() => Number(route.params.idProcesso))
const siglaUnidade = computed(() => String(route.params.siglaUnidade))

const subprocesso = computed(() => {
  if (!idProcesso.value || !siglaUnidade.value) return null;
  return (processosStore.subprocessos as Subprocesso[]).find(
      (pu) => pu.idProcesso === idProcesso.value && pu.unidade === siglaUnidade.value
  );
});

const podeVerImpacto = computed(() => {
  if (!perfilStore.perfilSelecionado || !subprocesso.value) return false;

  const perfil = perfilStore.perfilSelecionado;
  const situacao = subprocesso.value.situacao;

  const isPermittedProfile = perfil === Perfil.ADMIN;
  const isCorrectSituation = situacao === SITUACOES_SUBPROCESSO.REVISAO_CADASTRO_HOMOLOGADA || situacao === SITUACOES_SUBPROCESSO.MAPA_AJUSTADO;

  return isPermittedProfile && isCorrectSituation;
});

const mostrarModalImpacto = ref(false);

function abrirModalImpacto() {
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
const idSubprocesso = computed(() => {
  const Subprocesso = processosStore.subprocessos.find(
      (pu: Subprocesso) => pu.idProcesso === idProcesso.value && pu.unidade === siglaUnidade.value
  );
  return Subprocesso?.id;
});

const atividades = computed<Atividade[]>(() => {
  if (typeof idSubprocesso.value !== 'number') {
    return []
  }
  return atividadesStore.getAtividadesPorSubprocesso(idSubprocesso.value) || []
})
const mapa = computed(() => mapas.value.find(m => m.unidade === siglaUnidade.value && m.idProcesso === idProcesso.value))
const competencias = ref<Competencia[]>([])
watch(mapa, (novoMapa) => {
  if (novoMapa) {
    competencias.value = [...novoMapa.competencias];
  }
}, {immediate: true});
const atividadesSelecionadas = ref<number[]>([])
const novaCompetencia = ref({descricao: ''})

function toggleAtividade(id: number) {
  const index = atividadesSelecionadas.value.indexOf(id);
  if (index > -1) {
    atividadesSelecionadas.value.splice(index, 1);
  } else {
    atividadesSelecionadas.value.push(id);
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


function descricaoAtividade(id: number): string {
  const atv = atividades.value.find(a => a.id === id)
  return atv ? atv.descricao : 'Atividade não encontrada'
}

function getConhecimentosTooltip(atividadeId: number): string {
  const atividade = atividades.value.find(a => a.id === atividadeId)
  if (!atividade || !atividade.conhecimentos.length) {
    return 'Nenhum conhecimento cadastrado'
  }

  const conhecimentosHtml = atividade.conhecimentos
      .map(c => `<div class="mb-1">• ${c.descricao}</div>`)
      .join('')

  return `<div class="text-start"><strong>Conhecimentos:</strong><br>${conhecimentosHtml}</div>`
}

function getAtividadeCompleta(id: number): Atividade | undefined {
  return atividades.value.find(a => a.id === id)
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

function adicionarOuAtualizarCompetencia() {
  if (!novaCompetencia.value.descricao || atividadesSelecionadas.value.length === 0) return;

  if (competenciaSendoEditada.value) {
    const index = competencias.value.findIndex(c => c.id === competenciaSendoEditada.value!.id);
    if (index !== -1) {
      competencias.value[index].descricao = novaCompetencia.value.descricao;
      competencias.value[index].atividadesAssociadas = [...atividadesSelecionadas.value];
    }
  } else {
    competencias.value.push({
      id: Date.now(),
      descricao: novaCompetencia.value.descricao,
      atividadesAssociadas: [...atividadesSelecionadas.value]
    });
  }

  // Persistir as mudanças no mapaStore
  if (mapa.value) {
    mapasStore.editarMapa(mapa.value.id, {competencias: competencias.value});
  } else {
    // Se não houver mapa, cria um novo.
    const novoMapa = {
      id: Date.now(),
      unidade: siglaUnidade.value,
      idProcesso: idProcesso.value,
      competencias: competencias.value,
      situacao: 'em_andamento',
      dataCriacao: new Date(),
      dataDisponibilizacao: null,
      dataFinalizacao: null,
    };
    mapas.value.push(novoMapa);
    mapasStore.adicionarMapa(novoMapa);
  }
  
  // 10. Se a situação do subprocesso ainda for 'Cadastro homologado', alterar para 'Mapa criado'
  if (subprocesso.value && subprocesso.value.situacao === 'Cadastro homologado') {
    const subprocessoIndex = processosStore.subprocessos.findIndex(pu => pu.id === subprocesso.value!.id);
    if (subprocessoIndex !== -1) {
      processosStore.subprocessos[subprocessoIndex].situacao = 'Mapa criado';
    }
  }

  novaCompetencia.value.descricao = '';
  atividadesSelecionadas.value = [];
  competenciaSendoEditada.value = null;
}

function adicionarCompetenciaEFecharModal() {
  adicionarOuAtualizarCompetencia();
  fecharModalCriarNovaCompetencia();
}

function finalizarEdicao() {
  if (mapa.value) {
    mapasStore.editarMapa(mapa.value.id, {competencias: competencias.value})
  } else {
    const novoMapa = {
      id: Date.now(),
      unidade: siglaUnidade.value,
      idProcesso: idProcesso.value,
      competencias: competencias.value,
      situacao: 'em_andamento',
      dataCriacao: new Date(),
      dataDisponibilizacao: null,
      dataFinalizacao: null,
    };
    mapas.value.push(novoMapa);
  }
  mostrarModalDisponibilizar.value = true;
  dataLimiteValidacao.value = '';
  observacoesDisponibilizacao.value = '';
  notificacaoDisponibilizacao.value = '';
}

function excluirCompetencia(id: number) {
  const competencia = competencias.value.find(comp => comp.id === id);
  if (competencia) {
    competenciaParaExcluir.value = competencia;
    mostrarModalExcluirCompetencia.value = true;
  }
}

function confirmarExclusaoCompetencia() {
  if (competenciaParaExcluir.value) {
    competencias.value = competencias.value.filter(comp => comp.id !== competenciaParaExcluir.value!.id);
    if (mapa.value) {
      mapasStore.editarMapa(mapa.value.id, {competencias: competencias.value});
    }
    fecharModalExcluirCompetencia();
  }
}

function fecharModalExcluirCompetencia() {
  mostrarModalExcluirCompetencia.value = false;
  competenciaParaExcluir.value = null;
}

function removerAtividadeAssociada(competenciaId: number, atividadeId: number) {
  const competenciaIndex = competencias.value.findIndex(comp => comp.id === competenciaId);
  if (competenciaIndex !== -1) {
    const competencia = competencias.value[competenciaIndex];
    competencia.atividadesAssociadas = competencia.atividadesAssociadas.filter(id => id !== atividadeId);
    if (mapa.value) {
      mapasStore.editarMapa(mapa.value.id, {competencias: competencias.value});
    }
  }
}

function formatarData(data: string): string {
  if (!data) return ''
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}

function disponibilizarMapa() {
  if (!mapa.value || !unidade.value) {
    notificacaoDisponibilizacao.value = 'Erro: Mapa ou unidade não encontrados.'
    return
  }

  const currentMapa = mapa.value;
  const currentUnidade = unidade.value;

  // Validações conforme plano
  const competenciasSemAtividades = competencias.value.filter(comp => comp.atividadesAssociadas.length === 0);
  if (competenciasSemAtividades.length > 0) {
    notificacaoDisponibilizacao.value = `Erro: As seguintes competências não têm atividades associadas: ${competenciasSemAtividades.map(c => c.descricao).join(', ')}`;
    return;
  }

  const atividadesIds = atividades.value.map(a => a.id);
  const atividadesAssociadas = competencias.value.flatMap(comp => comp.atividadesAssociadas);
  const atividadesNaoAssociadas = atividadesIds.filter(id => !atividadesAssociadas.includes(id));

  if (atividadesNaoAssociadas.length > 0) {
    const descricoesNaoAssociadas = atividadesNaoAssociadas.map(id => descricaoAtividade(id)).join(', ');
    notificacaoDisponibilizacao.value = `Erro: As seguintes atividades não estão associadas a nenhuma competência: ${descricoesNaoAssociadas}`;
    return;
  }

  // Alterar situação do subprocesso para 'Mapa disponibilizado' (CDU-17)
  const subprocesso = processosStore.subprocessos.find(
      pu => pu.idProcesso === idProcesso.value && pu.unidade === siglaUnidade.value
  );
  if (subprocesso) {
    subprocesso.situacao = SITUACOES_SUBPROCESSO.MAPA_DISPONIBILIZADO;
    subprocesso.dataLimiteEtapa2 = new Date(dataLimiteValidacao.value);
    if (observacoesDisponibilizacao.value) {
      subprocesso.observacoes = observacoesDisponibilizacao.value;
    }

    // Registrar movimentação
    processosStore.addMovement({
      idSubprocesso: subprocesso.id,
      unidadeOrigem: 'SEDOC',
      unidadeDestino: siglaUnidade.value,
      descricao: 'Disponibilização do mapa de competências'
    });
  }

  // Atualizar mapa
  mapasStore.editarMapa(currentMapa.id, {
    situacao: 'disponivel_validacao',
    dataDisponibilizacao: new Date(),
  });

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

  // Criar alerta interno (CDU-17 item 18)
  alertasStore.criarAlerta({
    descricao: `Mapa de competências da unidade ${currentUnidade.sigla} disponibilizado para análise`,
    idProcesso: idProcesso.value,
    dataHora: new Date(),
    unidadeOrigem: 'SEDOC',
    unidadeDestino: currentUnidade.sigla
  });

  // Excluir sugestões e histórico de análise (CDU-17 item 19)
  if (subprocesso) {
    subprocesso.sugestoes = '';
    subprocesso.analises = [];
  }

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
  background: #f0f4fa;
  box-shadow: 0 0 0 2px #e3f0ff;
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
  background: #f8fafc;
  border-bottom: 1px solid #e3e8ee;
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

.atividade-associada-card-item {
  border: 1px solid #dee2e6;
  border-radius: 0.375rem;
  background-color: #f8f9fa;
}

.atividade-associada-descricao {
  font-size: 0.85rem;
  color: #495057;
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