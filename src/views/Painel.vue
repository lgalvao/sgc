<template>
  <div class="container mt-4">
    <h2>Painel</h2>
    <p v-if="perfil.value === 'SEDOC'">Visão geral dos processos e alertas.</p>
    <p v-else-if="perfil.value === 'GESTOR'">Processos de suas unidades subordinadas.</p>
    <p v-else-if="perfil.value === 'CHEFE'">Situação do cadastro de atividades unidade.</p>

    <!-- Tabela de processos para GESTOR e SEDOC -->
    <div v-if="perfil === 'GESTOR' || perfil.value === 'SEDOC'" class="mb-4">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="mb-0">Processos</h4>
        <router-link v-if="perfil.value === 'SEDOC'" class="btn btn-outline-primary" to="/processos/novo">Novo
          processo
        </router-link>
      </div>

      <table class="table table-striped">
        <thead>
        <tr>
          <th style="cursor:pointer" @click="ordenarPor('descricao')">
            Descrição
            <span v-if="criterio === 'descricao'">{{ asc ? '↑' : '↓' }}</span>
          </th>
          <th style="cursor:pointer" @click="ordenarPor('tipo')">
            Tipo
            <span v-if="criterio === 'tipo'">{{ asc ? '↑' : '↓' }}</span>
          </th>
          <th style="cursor:pointer" @click="ordenarPor('unidades')">
            Unidades participantes
            <span v-if="criterio === 'unidades'">{{ asc ? '↑' : '↓' }}</span>
          </th>
          <th style="cursor:pointer" @click="ordenarPor('dataLimite')">
            Data limite
            <span v-if="criterio === 'dataLimite'">{{ asc ? '↑' : '↓' }}</span>
          </th>
          <th style="cursor:pointer" @click="ordenarPor('situacao')">
            Situação
            <span v-if="criterio === 'situacao'">{{ asc ? '↑' : '↓' }}</span>
          </th>
        </tr>
        </thead>

        <tbody>
        <tr v-for="processo in processosOrdenados" :key="processo.id" style="cursor:pointer"
            @click="abrirUnidades(processo.id)">
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
      <div v-for="(cartao, idx) in painelConfig.cartoes" :key="idx"
           :class="cartao.tipo === 'alertas' ? 'col-12 mb-3' : 'col-md-3 mb-3'">
        <div class="card h-100">
          <div class="card-body d-flex flex-column justify-content-between">
            <h5 class="card-title">{{ cartao.titulo }}</h5>

            <!-- Cartões de processos com campos -->
            <div v-if="cartao.campos">
              <p v-for="campo in cartao.campos" :key="campo.chaveDado" class="card-text">
                {{ campo.rotulo }}:
                <span class="badge bg-secondary">{{ dadosPainel[campo.chaveDado].value }}</span>
              </p>
              <router-link v-if="cartao.link" :to="cartao.link.to" class="btn btn-outline-primary btn-sm">{{
                  cartao.link.rotulo
                }}
              </router-link>
            </div>

            <!-- Cartões de listas -->
            <ul v-else-if="cartao.chaveLista && cartao.tipo !== 'alertas'" class="list-group list-group-flush">
              <li v-for="item in dadosPainel[cartao.chaveLista].value" :key="item.unidade || item.texto || item.nome"
                  class="list-group-item d-flex align-items-center gap-2 py-2 px-3 notification-item">
                <template v-if="cartao.tipo === 'pendencias'">
                  {{ item.unidade }} <span :class="badgeClass(item.situacao)" class="badge">{{ item.situacao }}</span>
                </template>

                <template v-else-if="cartao.tipo === 'notificacoes'">
                  <span :class="badgeClass(item.tipo)" class="badge">{{ item.tipo === 'info' ? 'Novo' : '!' }}</span>
                  <span class="notification-text ms-2">{{ item.texto }}</span>
                </template>

                <template v-else-if="cartao.tipo === 'processosSubordinadas'">
                  {{ item.nome }} <span :class="badgeClass(item.situacao)" class="badge">{{ item.situacao }}</span>
                </template>
              </li>
            </ul>

            <!-- Tabela de alertas -->
            <table v-else-if="cartao.tipo === 'alertas'" class="table table-bordered table-sm mb-0">
              <thead>
              <tr>
                <th style="width: 100px;">Data</th>
                <th style="width: 120px;">Unidade</th>
                <th>Descrição</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(alerta, idx) in dadosPainel[cartao.chaveLista].value" :key="idx">
                <td>{{ formatarData(alerta.data) }}</td>
                <td>{{ alerta.unidade }}</td>
                <td>{{ alerta.descricao }}</td>
              </tr>
              </tbody>
            </table>

            <!-- Cartão de descrição -->
            <p v-else-if="cartao.chaveDescricao">{{ dadosPainel[cartao.chaveDescricao].value }}</p>
            <!-- Cartão de ações rápidas -->
            <div v-else-if="cartao.tipo === 'acoes'">
              <router-link v-for="acao in cartao.acoes" :key="acao.to" :to="acao.to"
                           class="btn btn-sm mb-2 btn-outline-primary">{{
                  acao.rotulo
                }}
              </router-link>
            </div>

            <!-- Cartão de situacao simples -->
            <p v-else-if="cartao.tipo === 'situacaoCadastro'">
              Situação: <span class="badge bg-warning text-dark">{{ dadosPainel.situacaoCadastro.value }}</span>
              <br>
              <router-link v-if="cartao.link" :to="cartao.link.to" class="btn btn-primary btn-sm mt-2">{{
                  cartao.link.rotulo
                }}
              </router-link>
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
import {computed, ref} from 'vue'
import {usePerfil} from '../composables/usePerfil'
import {storeToRefs} from 'pinia'
import {usePainelStore} from '../stores/painel'
import {useProcessosStore} from '../stores/processos'
import {useUnidadesStore} from '../stores/unidades'
import {useRouter} from 'vue-router'
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
const {processos} = storeToRefs(processosStore)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const router = useRouter()

// Ordenação simples
const criterio = ref('descricao')
const asc = ref(true)
const processosOrdenados = computed(() => {
  return [...processos.value].sort((a, b) => {
    if (a[criterio.value] < b[criterio.value]) return asc.value ? -1 : 1
    if (a[criterio.value] > b[criterio.value]) return asc.value ? 1 : -1
    return 0
  })
})

function ordenarPor(campo) {
  if (criterio.value === campo) {
    asc.value = !asc.value
  } else {
    criterio.value = campo
    asc.value = true
  }
}

function abrirUnidades(id) {
  router.push(`/processos/${id}/unidades`)
}

function badgeClass(situacao) {
  if (situacao === 'Aguardando' || situacao === 'Em andamento' || situacao === 'Aguardando validação') return 'bg-warning text-dark'
  if (situacao === 'Finalizado' || situacao === 'Validado') return 'bg-success'
  if (situacao === 'Devolvido') return 'bg-danger'
  if (situacao === 'info' || situacao === 'Novo') return 'bg-info text-dark'
  if (situacao === 'danger' || situacao === '!') return 'bg-danger'
  return 'bg-secondary'
}

function getSituacaoUnidade(sigla, unidades) {
  for (const unidade of unidades) {
    if (unidade.sigla === sigla) return unidade.situacao
    if (unidade.filhas && unidade.filhas.length) {
      const achou = getSituacaoUnidade(sigla, unidade.filhas)
      if (achou) return achou
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

function formatarData(dataISO) {
  if (!dataISO) return '';
  const [ano, mes, dia] = dataISO.split('-');
  return `${dia}/${mes}/${ano}`;
}
</script>