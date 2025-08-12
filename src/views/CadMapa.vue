<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="display-6">Mapa de competências técnicas</div>
    </div>

    <div v-if="unidade">
      <div class="mb-5 d-flex align-items-center">
        <div class="fs-5">{{ unidade.sigla }} - {{ unidade.nome }}</div>
      </div>

      <div class="mb-4 mt-3">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <div class="fs-4 mb-0">Competências cadastradas</div>
          <button class="btn btn-outline-primary" @click="abrirModalCriarNovaCompetencia" data-testid="btn-abrir-criar-competencia">
            <i class="bi bi-plus-lg"></i> Criar competência
          </button>
        </div>

        <div v-if="competencias.length === 0" class="text-muted">Nenhuma competência cadastrada ainda.</div>
        <div v-for="comp in competencias" :key="comp.id" class="card mb-2 competencia-card"
             data-testid="competencia-item">
          <div class="card-body py-2">
            <div
                class="card-title fs-5 d-flex align-items-center competencia-edicao-row position-relative competencia-hover-row competencia-titulo-card">
              

              <strong data-testid="competencia-descricao" class="competencia-descricao"> {{ comp.descricao }}</strong>
              <div class="ms-auto d-inline-flex align-items-center gap-1 botoes-acao">
                <button class="btn btn-sm btn-outline-primary botao-acao"
                        @click="iniciarEdicaoCompetencia(comp)" title="Editar"
                        data-bs-toggle="tooltip" data-testid="btn-editar-competencia"><i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger botao-acao" @click="excluirCompetencia(comp.id)"
                        title="Excluir"
                        data-bs-toggle="tooltip" data-testid="btn-excluir-competencia"><i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-2">
              <div v-for="atvId in comp.atividadesAssociadas" :key="atvId"
                   class="card atividade-associada-card-item d-flex align-items-center group-atividade-associada">
                <div class="card-body d-flex align-items-center py-1 px-2">
                  <span class="atividade-associada-descricao me-2">{{ descricaoAtividade(atvId) }}</span>
                  <button class="btn btn-sm btn-outline-secondary botao-acao-inline fade-group"
                          @click="removerAtividadeAssociada(comp.id, atvId)" title="Remover Atividade"
                          data-bs-toggle="tooltip"><i class="bi bi-trash"></i></button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <button :disabled="competencias.length === 0" class="btn btn-lg btn-success" @click="finalizarEdicao">
        Disponibilizar
      </button>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <!-- Modal de Criar Nova Competência -->
    <div v-if="mostrarModalCriarNovaCompetencia" class="modal fade show" style="display: block;" tabindex="-1"
         aria-labelledby="criarCompetenciaModalLabel" aria-modal="true" role="dialog">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="criarCompetenciaModalLabel">{{ competenciaSendoEditada ? 'Edição de competência' : 'Criação de competência' }}</h5>
            <button type="button" class="btn-close" @click="fecharModalCriarNovaCompetencia"
                    aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <!-- Conteúdo do card movido para cá -->
            <div class="mb-4">
              <h5>Descrição</h5>
              <div class="mb-2">
                <textarea v-model="novaCompetencia.descricao"
                          class="form-control"
                          placeholder="Descreva a competência"
                          rows="3"
                          data-testid="input-nova-competencia"></textarea>
              </div>
            </div>

            <div class="mb-4">
              <h5>Atividades</h5>
              <div class="d-flex flex-wrap gap-2">
                <div v-for="atividade in atividades" :key="atividade.id"
                     class="card atividade-card-item"
                     :class="{ checked: atividadesSelecionadas.includes(atividade.id) }"
                     @click="toggleAtividade(atividade.id)">
                  <div class="card-body d-flex align-items-center py-2">
                    <input :id="`atv-${atividade.id}`" v-model="atividadesSelecionadas"
                           :value="atividade.id"
                           class="form-check-input me-2"
                           type="checkbox"
                           data-testid="atividade-checkbox"
                           hidden>
                    <label class="form-check-label mb-0">
                      {{ atividade.descricao }}
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="fecharModalCriarNovaCompetencia">Cancelar</button>
            <button :disabled="atividadesSelecionadas.length === 0 || !novaCompetencia.descricao"
                    class="btn btn-primary"
                    type="button" @click="adicionarCompetenciaEFecharModal" title="Criar Competência"
                    data-bs-toggle="tooltip" data-testid="btn-criar-competencia"><i
                class="bi bi-save"></i> Salvar
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="mostrarModalCriarNovaCompetencia" class="modal-backdrop fade show"></div>

    
  <!-- Modal de Disponibilizar -->
    <div v-if="mostrarModalDisponibilizar" class="modal fade show" style="display: block;" tabindex="-1" aria-labelledby="disponibilizarModalLabel" aria-modal="true" role="dialog">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="disponibilizarModalLabel">Disponibilizar Mapa</h5>
            <button type="button" class="btn-close" @click="fecharModalDisponibilizar" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label" for="dataLimite">Data limite para validação</label>
              <input id="dataLimite" v-model="dataLimiteValidacao" class="form-control" type="date"/>
            </div>
            <div v-if="notificacaoDisponibilizacao" class="alert alert-info mt-3">
              {{ notificacaoDisponibilizacao }}
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="fecharModalDisponibilizar">Cancelar</button>
            <button :disabled="!dataLimiteValidacao" type="button" class="btn btn-success" @click="disponibilizarMapa">Disponibilizar</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="mostrarModalDisponibilizar" class="modal-backdrop fade show"></div>

  </div>
</template>

<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'

import {useMapasStore} from '@/stores/mapas'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtividadesStore} from "@/stores/atividades";
import {useProcessosStore} from "@/stores/processos";
import {Atividade, Competencia, Unidade} from '@/types/tipos';

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla as string)
// processoId agora vem do parâmetro de rota (novo padrão /processo/:processoId/:sigla/...)
const processoId = computed(() => Number(route.params.processoId))
const unidadesStore = useUnidadesStore()
const mapaStore = useMapasStore()
const {mapas} = storeToRefs(mapaStore)
const atividadesStore = useAtividadesStore()
const processosStore = useProcessosStore()

const {unidades} = storeToRefs(unidadesStore)

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

const unidade = computed<Unidade | null>(() => buscarUnidade(unidades.value as Unidade[], sigla.value))
const processoUnidadeId = computed(() => {
  const processoUnidade = processosStore.processosUnidade.find(
      (pu: any) => pu.processoId === processoId.value && pu.unidade === sigla.value
  );
  return processoUnidade?.id;
});

const atividades = computed<Atividade[]>(() => {
  if (typeof processoUnidadeId.value !== 'number') {
    return []
  }
  return atividadesStore.getAtividadesPorProcessoUnidade(processoUnidadeId.value) || []
})
const mapa = computed(() => mapas.value.find(m => m.unidade === sigla.value && m.processoId === processoId.value))
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
const dataLimiteValidacao = ref('')
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
    mapaStore.editarMapa(mapa.value.id, { competencias: competencias.value });
  } else {
    // Se não houver mapa, cria um novo.
    const novoMapa = {
      id: Date.now(),
      unidade: sigla.value,
      processoId: processoId.value,
      competencias: competencias.value,
      situacao: 'em_andamento',
      dataCriacao: new Date(),
      dataDisponibilizacao: null,
      dataFinalizacao: null,
    };
    mapas.value.push(novoMapa);
    mapaStore.adicionarMapa(novoMapa);
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
    mapaStore.editarMapa(mapa.value.id, {competencias: competencias.value})
  } else {
    const novoMapa = {
      id: Date.now(),
      unidade: sigla.value,
      processoId: processoId.value,
      competencias: competencias.value,
      situacao: 'em_andamento',
      dataCriacao: new Date(),
      dataDisponibilizacao: null,
      dataFinalizacao: null,
    };
    mapas.value.push(novoMapa);
  }
  mostrarModalDisponibilizar.value = true;
  dataLimiteValidacao.value = ''; // Limpa a data ao abrir o modal
  notificacaoDisponibilizacao.value = ''; // Limpa a notificação
}

function excluirCompetencia(id: number) {
  competencias.value = competencias.value.filter(comp => comp.id !== id);
  if (mapa.value) {
    mapaStore.editarMapa(mapa.value.id, { competencias: competencias.value });
  }
}

function removerAtividadeAssociada(competenciaId: number, atividadeId: number) {
  const competenciaIndex = competencias.value.findIndex(comp => comp.id === competenciaId);
  if (competenciaIndex !== -1) {
    const competencia = competencias.value[competenciaIndex];
    competencia.atividadesAssociadas = competencia.atividadesAssociadas.filter(id => id !== atividadeId);
    if (mapa.value) {
      mapaStore.editarMapa(mapa.value.id, { competencias: competencias.value });
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

  mapaStore.editarMapa(currentMapa.id, {
    situacao: 'disponivel_validacao',
    dataDisponibilizacao: new Date(),
  })

  notificacaoDisponibilizacao.value = `Notificação: O mapa de competências da unidade ${currentUnidade.sigla}
                       foi disponibilizado para validação até ${formatarData(dataLimiteValidacao.value)}. (Simulação)`
}

function fecharModalDisponibilizar() {
  mostrarModalDisponibilizar.value = false;
  notificacaoDisponibilizacao.value = ''; // Limpa a notificação ao fechar
}

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

.fade-group {
  transition: opacity 0.2s;
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
}

.atividade-card-item.checked .form-check-label {
  font-weight: bold;
  color: #007bff;
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
  opacity: 0;
  pointer-events: none;
}

.group-atividade-associada:hover .botao-acao-inline {
  opacity: 1;
  pointer-events: auto;
}
</style>