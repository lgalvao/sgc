<template>
  <div class="container mt-4">
    <!-- Tabela de Processos -->
    <div class="mb-5">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="display-5 mb-0">Processos</div>
        <router-link v-if="perfil.perfilSelecionado === 'ADMIN'" class="btn btn-outline-primary" to="/processos/novo">
          <i class="bi bi-plus-lg"></i> Criar processo
        </router-link>
      </div>
      <table class="table table-hover">
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
          <th style="cursor:pointer" @click="ordenarPor('situacao')">
            Situação
            <span v-if="criterio === 'situacao'">{{ asc ? '↑' : '↓' }}</span>
          </th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="processo in processosOrdenados" :key="processo.id">
          <td style="cursor:pointer; color: var(--bs-link-color);" @click="abrirDetalhesProcesso(processo)">
            {{ processo.descricao }}
          </td>
          <td>{{ processo.tipo }}</td>
          <td>{{ processosStore.getUnidadesDoProcesso(processo.id).map(pu => pu.unidade).join(', ') }}</td>
          <td>{{ processo.situacao }}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div>
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="mb-0 display-5">Alertas</div>
      </div>
      <table class="table table-hover">
        <thead>
        <tr>
          <th>Data/Hora</th>
          <th>Processo</th>
          <th>Unidade Origem</th>
          <th>Descrição</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(alerta, index) in alertasFormatados" :key="index">
          <td>{{ formatarDataHora(alerta.data) }}</td>
          <td>{{ alerta.processo }}</td>
          <td>{{ alerta.unidade }}</td>
          <td>{{ alerta.descricao }}</td>
        </tr>
        <tr v-if="!alertas || alertas.length === 0">
          <td colspan="4" class="text-center text-muted">Nenhum alerta no momento.</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import {storeToRefs} from 'pinia'
import {usePerfilStore} from '@/stores/perfil'
import {useProcessosStore} from '@/stores/processos'
import {useAlertasStore} from '@/stores/alertas'
import {useRouter} from 'vue-router'
import {Alerta, Processo} from '@/types/tipos'

const perfil = usePerfilStore()
const processosStore = useProcessosStore()
const {processos} = storeToRefs(processosStore)
const alertasStore = useAlertasStore()
const {alertas} = storeToRefs(alertasStore)

const router = useRouter()

const criterio = ref<keyof Processo | 'unidades'>('descricao')
const asc = ref(true)

const processosFiltrados = computed<Processo[]>(() => {
  return processos.value as Processo[]
})

const processosOrdenados = computed<Processo[]>(() => {
  return [...processosFiltrados.value].sort((a, b) => {
    let valA: any = a[criterio.value as keyof Processo]
    let valB: any = b[criterio.value as keyof Processo]

    if (criterio.value === 'unidades') {
      valA = processosStore.getUnidadesDoProcesso(a.id).map(pu => pu.unidade).join(', ')
      valB = processosStore.getUnidadesDoProcesso(b.id).map(pu => pu.unidade).join(', ')
    }

    if (valA < valB) return asc.value ? -1 : 1
    if (valA > valB) return asc.value ? 1 : -1
    return 0
  })
})

function ordenarPor(campo: keyof Processo | 'unidades') {
  if (criterio.value === campo) {
    asc.value = !asc.value
  } else {
    criterio.value = campo
    asc.value = true
  }
}

function abrirDetalhesProcesso(processo: Processo) {
  router.push(`/processos/${processo.id}/unidades`)
}

const alertasFormatados = computed(() => {
  return (alertas.value as Alerta[]).map(alerta => {
    const processo = processosStore.processos.find(p => p.id === alerta.processoId);

    return {
      data: alerta.dataHora,
      processo: processo ? processo.descricao : 'Processo não encontrado',
      unidade: alerta.unidadeOrigem,
      descricao: alerta.descricao
    };
  });
});

function formatarDataHora(data: Date) {
  const options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  };
  return data.toLocaleString('pt-BR', options);
}
</script>