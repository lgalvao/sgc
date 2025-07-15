<template>
  <div class="container mt-4">
    <h2>Painel</h2>
    <p v-if="perfil.value === 'SEDOC'">Visão geral dos processos, unidades e pendências do sistema.</p>
    <p v-else-if="perfil.value === 'GESTOR'">Acompanhe os processos e pendências das suas unidades subordinadas.</p>
    <p v-else-if="perfil.value === 'CHEFE'">Acompanhe o status do cadastro da sua unidade e acesse rapidamente suas principais funções.</p>

    <!-- Tabela de processos para GESTOR e SEDOC -->
    <div v-if="perfil.value === 'GESTOR' || perfil.value === 'SEDOC'" class="mb-4">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="mb-0">Lista de Processos</h4>
        <router-link v-if="perfil.value === 'SEDOC'" to="/processos/novo" class="btn btn-success">Novo processo</router-link>
      </div>
      <table class="table table-striped">
        <thead>
          <tr>
            <th>#</th>
            <th>Descrição</th>
            <th>Tipo</th>
            <th>Unidades participantes</th>
            <th>Data limite</th>
            <th>Situação</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(processo, idx) in processos" :key="processo.id" @click="abrirUnidades(processo.id)" style="cursor:pointer">
            <td>{{ processo.id }}</td>
            <td>{{ processo.descricao }}</td>
            <td>{{ processo.tipo }}</td>
            <td>{{ processo.unidades }}</td>
            <td>{{ processo.dataLimite }}</td>
            <td>{{ consolidarSituacaoProcesso(processo) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="row mt-4">
      <div v-for="(cartao, idx) in painelConfig.cartoes" :key="idx" class="col-md-3 mb-3">
        <div class="card h-100">
          <div class="card-body d-flex flex-column justify-content-between">
            <h5 class="card-title">{{ cartao.titulo }}</h5>
            <!-- Cartões de processos com campos -->
            <div v-if="cartao.campos">
              <p class="card-text" v-for="campo in cartao.campos" :key="campo.chaveDado">
                {{ campo.rotulo }}:
                <span class="badge bg-secondary">{{ dadosPainel[campo.chaveDado].value }}</span>
              </p>
              <router-link v-if="cartao.link" :to="cartao.link.to" class="btn btn-outline-primary btn-sm">{{ cartao.link.rotulo }}</router-link>
            </div>
            <!-- Cartões de listas -->
            <ul v-else-if="cartao.chaveLista" class="list-group list-group-flush">
              <li v-for="item in dadosPainel[cartao.chaveLista].value" :key="item.unidade || item.texto || item.nome" class="list-group-item d-flex justify-content-between align-items-center">
                <template v-if="cartao.tipo === 'pendencias'">
                  {{ item.unidade }} <span class="badge" :class="badgeClass(item.status)">{{ item.status }}</span>
                </template>
                <template v-else-if="cartao.tipo === 'notificacoes'">
                  <span class="badge" :class="badgeClass(item.tipo)">{{ item.tipo === 'info' ? 'Novo' : '!' }}</span> {{ item.texto }}
                </template>
                <template v-else-if="cartao.tipo === 'processosSubordinadas'">
                  {{ item.nome }} <span class="badge" :class="badgeClass(item.status)">{{ item.status }}</span>
                </template>
              </li>
            </ul>
            <!-- Cartão de descrição -->
            <p v-else-if="cartao.chaveDescricao">{{ dadosPainel[cartao.chaveDescricao].value }}</p>
            <!-- Cartão de ações rápidas -->
            <div v-else-if="cartao.tipo === 'acoes'">
              <router-link v-for="acao in cartao.acoes" :key="acao.to" :to="acao.to" class="btn btn-sm mb-2 btn-outline-primary">{{ acao.rotulo }}</router-link>
            </div>
            <!-- Cartão de status simples -->
            <p v-else-if="cartao.tipo === 'statusCadastro'">
              Situação: <span class="badge bg-warning text-dark">{{ dadosPainel.situacaoCadastro.value }}</span>
              <br>
              <router-link v-if="cartao.link" :to="cartao.link.to" class="btn btn-primary btn-sm mt-2">{{ cartao.link.rotulo }}</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!painelConfig.cartoes">
      <p>Nenhum painel para o perfil: [{{ perfil.value }}]</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { usePerfil } from '../composables/usePerfil'
import { storeToRefs } from 'pinia'
import { usePainelStore } from '../stores/painel'
import { useProcessosStore } from '../stores/processos'
import { useUnidadesStore } from '../stores/unidades'
import { useRouter } from 'vue-router'
import painelSEDOC from '../mocks/painel/painel_SEDOC.json'
import painelGESTOR from '../mocks/painel/painel_GESTOR.json'
import painelCHEFE from '../mocks/painel/painel_CHEFE.json'

const perfil = usePerfil()
const perfisPainel = {
  SEDOC: painelSEDOC,
  GESTOR: painelGESTOR,
  CHEFE: painelCHEFE
}
const painelConfig = computed(() => perfisPainel[perfil.value] || painelSEDOC)

const painelStore = usePainelStore()
const dadosPainel = storeToRefs(painelStore)

const processosStore = useProcessosStore()
const { processos } = storeToRefs(processosStore)
const unidadesStore = useUnidadesStore()
const { unidades } = storeToRefs(unidadesStore)
const router = useRouter()

function abrirUnidades(id) {
  router.push(`/processos/${id}/unidades`)
}

function badgeClass(status) {
  if (status === 'Aguardando' || status === 'Em andamento' || status === 'Aguardando validação') return 'bg-warning text-dark'
  if (status === 'Finalizado' || status === 'Validado') return 'bg-success'
  if (status === 'Devolvido') return 'bg-danger'
  if (status === 'info' || status === 'Novo') return 'bg-info text-dark'
  if (status === 'danger' || status === '!') return 'bg-danger'
  return 'bg-secondary'
}

function getSituacaoUnidade(sigla, unidades) {
  for (const unidade of unidades) {
    if (unidade.sigla === sigla) return unidade.situacao
    if (unidade.filhas && unidade.filhas.length) {
      const found = getSituacaoUnidade(sigla, unidade.filhas)
      if (found) return found
    }
  }
  return null
}

function consolidarSituacaoProcesso(processo) {
  const participantes = processo.unidades.split(',').map(u => u.trim())
  const situacoes = participantes.map(sigla => getSituacaoUnidade(sigla, unidades.value) || 'Não iniciado')
  if (situacoes.every(s => s === 'Finalizado')) return 'Finalizado'
  if (situacoes.some(s => s === 'Em andamento')) return 'Em andamento'
  return 'Não iniciado'
}
</script> 