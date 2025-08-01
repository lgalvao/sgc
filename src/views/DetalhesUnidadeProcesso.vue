<template>
  <div class="container mt-4">
    <div v-if="unidade" class="card mb-4">
      <div class="card-body">
        <span class="badge text-bg-secondary mb-2" style="border-radius: 0" >Processo de unidade</span>

        <h2 class="card-title mb-3">{{ unidade.sigla }} - {{ unidade.nome}}</h2>
        <p><strong>Responsável:</strong> {{ responsavelDetalhes.nome }}</p>
        <p class="ms-3">{{ responsavelDetalhes.tipo }}</p>
        <p class="ms-3"><strong>Ramal:</strong> {{ responsavelDetalhes.ramal }}</p>
        <p class="ms-3"><strong>E-mail:</strong> {{ responsavelDetalhes.email }}</p>
        <p>
          <span class="fw-bold me-1">Situação:</span>
          <span :class="badgeClass(unidade.situacao)" class="badge">{{ unidade.situacao }}</span>
        </p>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <div class="row">
      <template v-if="processoAtual?.tipo === 'Mapeamento' || processoAtual?.tipo === 'Revisão'">
        <section class="col-md-4 mb-3">
          <div class="card h-100" @click="irParaAtividadesConhecimentos" style="cursor: pointer;">
            <div class="card-body">
              <h5 class="card-title">Atividades e conhecimentos</h5>
              <p class="card-text text-muted">Cadastro de atividades e conhecimentos da unidade</p>
              <div class="d-flex justify-content-between align-items-center">
                <span class="badge bg-secondary">Não disponibilizado</span>
              </div>
            </div>
          </div>
        </section>

        <section class="col-md-4 mb-3">
          <div class="card h-100" style="cursor: pointer;" @click="mapa ? (mapa.situacao === 'em_andamento' ? editarMapa() : visualizarMapa()) : criarMapa()">
            <div class="card-body">
              <h5 class="card-title">Mapa de Competências</h5>
              <p class="card-text text-muted">Mapa de competências da unidade</p>
              <div v-if="mapa">
                <div v-if="mapa.situacao === 'em_andamento'">
                  <span class="badge bg-warning text-dark">Em andamento</span>
                </div>

                <div v-else-if="mapa.situacao === 'disponivel_validacao'">
                  <span class="badge bg-success">Disponibilizado</span>
                </div>
              </div>
              <div v-else>
                <span class="badge bg-secondary">Não disponibilizado</span>
              </div>
            </div>
          </div>
        </section>
      </template>

      <template v-else-if="processoAtual?.tipo === 'Diagnóstico'">
        <section class="col-md-4 mb-3">
          <div class="card h-100" style="cursor: pointer;">
            <div class="card-body">
              <h5 class="card-title">Diagnóstico da Equipe</h5>
              <p class="card-text text-muted">Diagnóstico das competências pelos servidores da unidade</p>
              <div class="d-flex justify-content-between align-items-center">
                <span class="badge bg-secondary">Não disponibilizado</span>
              </div>
            </div>
          </div>
        </section>

        <section class="col-md-4 mb-3">
          <div class="card h-100" style="cursor: pointer;">
            <div class="card-body">
              <h5 class="card-title">Ocupações Críticas</h5>
              <p class="card-text text-muted">Identificação das ocupações críticas da unidade</p>
              <div class="d-flex justify-content-between align-items-center">
                <span class="badge bg-secondary">Não disponibilizado</span>
              </div>
            </div>
          </div>
        </section>
      </template>

      </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '../stores/unidades'
import {useAtribuicaoTemporariaStore} from '../stores/atribuicaoTemporaria'
import {useMapasStore} from '../stores/mapas'
import {useServidoresStore} from '../stores/servidores'
import {useProcessosStore} from '../stores/processos'

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const atribuicaoStore = useAtribuicaoTemporariaStore()
const mapaStore = useMapasStore()
const servidoresStore = useServidoresStore()
const processosStore = useProcessosStore()

const unidade = computed(() => {
  return unidadesStore.findUnit(sigla.value);
});
const atribuicao = computed(() => atribuicaoStore.getAtribuicaoPorUnidade(sigla.value))
const mapa = computed(() => mapaStore.getMapaPorUnidade(sigla.value))

const processoId = computed(() => Number(route.query.processoId))
const processoAtual = computed(() => processosStore.processos.find(p => p.id === processoId.value))

const responsavelDetalhes = computed(() => {
  if (!unidade.value) return null;

  const titular = servidoresStore.getServidorById(unidade.value.titular);
  let tipoResponsabilidade = 'Titular';
  let dataTermino = '';

  if (atribuicao.value) {
    tipoResponsabilidade = 'Atrib. temporária';
    dataTermino = ` (até ${atribuicao.value.dataTermino})`; // Assumindo que dataTermino existe
  }

  return {
    nome: titular?.nome || 'Não definido',
    tipo: `${tipoResponsabilidade}${dataTermino}`,
    ramal: titular?.ramal || 'Não informado',
    email: titular?.email || 'Não informado'
  };
});

function badgeClass(situacao) {
  if (situacao === 'Aguardando' || situacao === 'Em andamento' || situacao === 'Aguardando validação') return 'bg-warning text-dark'
  if (situacao === 'Finalizado' || situacao === 'Validado') return 'bg-success'
  if (situacao === 'Devolvido') return 'bg-danger'
  return 'bg-secondary'
}

function voltar() {
  router.back()
}

function removerAtribuicao() {
  atribuicaoStore.removerAtribuicao(sigla.value)
}

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${sigla.value}/atribuir`})
}

function criarMapa() {
  router.push({path: `/unidade/${sigla.value}/mapa`})
}

function editarMapa() {
  router.push({path: `/unidade/${sigla.value}/mapa`})
}

function visualizarMapa() {
  router.push({path: `/unidade/${sigla.value}/mapa/visualizar`})
}

function irParaAtividadesConhecimentos() {
  // Tenta obter o id do processo da query string ou do localStorage, se não disponível, alerta
  const processoId = route.query.processoId || localStorage.getItem('processoIdAtual')
  if (processoId) {
    router.push(`/processos/${processoId}/unidade/${sigla.value}/atividades`)
  } else {
    alert('ID do processo não encontrado. Acesse a partir do processo no painel.')
  }
}
</script>