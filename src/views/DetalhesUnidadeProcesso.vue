<template>
  <div class="container mt-4">
    <div v-if="unidade" class="card mb-4">
      <div class="card-body">
        <span class="badge text-bg-secondary mb-2" style="border-radius: 0" >Processo de unidade</span>

        <h2 class="card-title mb-3">{{ unidade.sigla }} - {{ unidade.nome}}</h2>
        <p><strong>Responsável:</strong> {{ atribuicao?.nomeResponsavel || 'Não definido' }}</p>
        <p><strong>Contato:</strong> {{ atribuicao?.contato || 'Não informado' }}</p>
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
      <section class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Atividades e conhecimentos</h5>
            <div>
              <button class="btn btn-primary btn-sm" @click="irParaAtividadesConhecimentos">Criar</button>
            </div>
          </div>
        </div>
      </section>

      <section class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Mapa de Competências</h5>
            <div v-if="mapa">
              <div v-if="mapa.situacao === 'em_andamento'">
                <button class="btn btn-primary btn-sm me-2" @click="editarMapa">Editar</button>
              </div>

              <div v-else-if="mapa.situacao === 'disponivel_validacao'">
                <button class="btn btn-info btn-sm me-2" @click="visualizarMapa">Visualizar</button>
              </div>
            </div>
            <div v-else>
              <button class="btn btn-primary btn-sm me-2" @click="criarMapa">Criar</button>
            </div>
          </div>
        </div>
      </section>

      <section class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Atribuição temporária</h5>
            <div v-if="atribuicao">
              <p><strong>Responsável:</strong> {{ atribuicao.nomeResponsavel }}</p>
              <p><strong>Data de início:</strong> {{ atribuicao.dataInicio }}</p>
              <p><strong>Data de término:</strong> {{ atribuicao.dataTermino }}</p>
              <button class="btn btn-warning btn-sm me-2" @click="editarAtribuicao">Editar</button>
              <button class="btn btn-danger btn-sm" @click="removerAtribuicao">Remover</button>
            </div>
            <div v-else>
              <button class="btn btn-primary btn-sm" @click="irParaCriarAtribuicao">Criar</button>
            </div>
          </div>
        </div>
      </section>

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

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const atribuicaoStore = useAtribuicaoTemporariaStore()
const mapaStore = useMapasStore()

const unidade = computed(() => {
  console.log("Sigla recebida na rota:", sigla.value);
  return unidadesStore.findUnit(sigla.value);
});
const atribuicao = computed(() => atribuicaoStore.getAtribuicaoPorUnidade(sigla.value))
const mapa = computed(() => mapaStore.getMapaPorUnidade(sigla.value))

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