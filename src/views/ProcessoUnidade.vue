<template>
  <div class="container mt-4">
    <div v-if="unidade" class="card mb-4">
      <div class="card-body">
        <span class="badge text-bg-secondary mb-2" style="border-radius: 0">Processo de unidade</span>

        <h2 class="card-title mb-3">{{ unidade.sigla }} - {{ unidade.nome }}</h2>
        <p><strong>Responsável:</strong> {{ responsavelDetalhes?.nome }}</p>
        <p class="ms-3"><strong>Ramal:</strong> {{ responsavelDetalhes?.ramal }}</p>
        <p class="ms-3"><strong>E-mail:</strong> {{ responsavelDetalhes?.email }}</p>
        <p>
          <span class="fw-bold me-1">Situação:</span>
          <span :class="badgeClass(situacaoUnidadeNoProcesso)" class="badge">{{ situacaoUnidadeNoProcesso }}</span>
        </p>
        <p v-if="processoUnidadeDetalhes">
          <strong>Unidade Atual:</strong> {{ processoUnidadeDetalhes.unidadeAtual || 'Não informado' }}<br>
          <strong>Unidade Anterior:</strong> {{ processoUnidadeDetalhes.unidadeAnterior || 'N/A' }}
        </p>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <div class="row">
      <template v-if="processoAtual?.tipo === ProcessoTipo.MAPEAMENTO || processoAtual?.tipo === ProcessoTipo.REVISAO">
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
          <div class="card h-100" style="cursor: pointer;"
               @click="mapa ? (mapa.situacao === 'em_andamento' ? editarMapa() : visualizarMapa()) : criarMapa()">
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

      <template v-else-if="processoAtual?.tipo === ProcessoTipo.DIAGNOSTICO">
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

<script setup lang="ts">
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicaoTemporaria'
import {useMapasStore} from '@/stores/mapas'
import {useServidoresStore} from '@/stores/servidores'
import {useProcessosStore} from '@/stores/processos'
import {Mapa, Processo, ProcessoTipo, ProcessoUnidade, Servidor, Unidade} from "@/types/tipos";

const route = useRoute()
const router = useRouter()
const idProcessoUnidade = computed(() => Number(route.params.idProcessoUnidade))
const unidadesStore = useUnidadesStore()
useAtribuicaoTemporariaStore();
const mapaStore = useMapasStore()
const servidoresStore = useServidoresStore()
const processosStore = useProcessosStore()
const {processos} = storeToRefs(processosStore)

const processoUnidadeDetalhes = computed<ProcessoUnidade | undefined>(() => {
  return processosStore.getProcessoUnidadeById(idProcessoUnidade.value);
});

const processoAtual = computed<Processo | null>(() => {
  if (!processoUnidadeDetalhes.value) return null;
  return (processos.value as Processo[]).find(p => p.id === processoUnidadeDetalhes.value?.processoId) || null;
});

const sigla = computed<string | undefined>(() => processoUnidadeDetalhes.value?.unidade);

const unidade = computed<Unidade | null>(() => {
  if (!sigla.value) {
    return null;
  }
  const unidadeEncontrada = unidadesStore.pesquisarUnidade(sigla.value);
  if (unidadeEncontrada && unidadeEncontrada.sigla && unidadeEncontrada.nome) {
    return unidadeEncontrada as Unidade;
  }
  return null;
});

const responsavelDetalhes = computed<Servidor | null>(() => {
  if (!unidade.value || !unidade.value.responsavel) {
    return null;
  }
  return servidoresStore.getServidorById(unidade.value.responsavel) || null;
});

const situacaoUnidadeNoProcesso = computed<string>(() => {
  return processoUnidadeDetalhes.value?.situacao || 'Não informado';
});

const mapa = computed<Mapa | null>(() => {
  if (!unidade.value || !processoAtual.value) return null;
  return mapaStore.getMapaByUnidadeId(unidade.value.sigla, processoAtual.value.id) || null;
});

function badgeClass(situacao: string): string {
  if (situacao === 'Aguardando' || situacao === 'Em andamento' || situacao === 'Aguardando validação') return 'bg-warning text-dark'
  if (situacao === 'Finalizado' || situacao === 'Validado') return 'bg-success'
  if (situacao === 'Devolvido') return 'bg-danger'
  return 'bg-secondary'
}

function criarMapa() {
  if (sigla.value && processoAtual.value) {
    router.push({path: `/unidade/${sigla.value}/mapa`, query: {processoId: processoAtual.value.id}})
  }
}

function editarMapa() {
  if (sigla.value && processoAtual.value) {
    router.push({path: `/unidade/${sigla.value}/mapa`, query: {processoId: processoAtual.value.id}})
  }
}

function visualizarMapa() {
  if (sigla.value && processoAtual.value) {
    router.push({path: `/unidade/${sigla.value}/mapa/visualizar`, query: {processoId: processoAtual.value.id}})
  }
}

function irParaAtividadesConhecimentos() {
  if (sigla.value && processoAtual.value) {
    router.push(`/processos/${processoAtual.value.id}/unidade/${sigla.value}/atividades`)
  }
}
</script>