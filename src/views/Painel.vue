<template>
  <div class="container mt-4">
    <!-- Tabela de Processos -->
    <div class="mb-5">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="display-6 mb-0" data-testid="titulo-processos">Processos</div>
        <router-link v-if="perfil.perfilSelecionado === 'ADMIN'" :to="{ name: 'CadProcesso' }"
                     class="btn btn-outline-primary" data-testid="btn-criar-processo">
          <i class="bi bi-plus-lg"></i> Criar processo
        </router-link>
      </div>
      <table class="table table-hover" data-testid="tabela-processos">
        <thead>
        <tr>
          <th data-testid="coluna-descricao" style="cursor:pointer" @click="ordenarPor('descricao')">
            Descrição
            <span v-if="criterio === 'descricao'">{{ asc ? '↑' : '↓' }}</span>
          </th>
          <th data-testid="coluna-tipo" style="cursor:pointer" @click="ordenarPor('tipo')">
            Tipo
            <span v-if="criterio === 'tipo'">{{ asc ? '↑' : '↓' }}</span>
          </th>
          <th data-testid="coluna-unidades" style="cursor:pointer" @click="ordenarPor('unidades')">
            Unidades participantes
            <span v-if="criterio === 'unidades'">{{ asc ? '↑' : '↓' }}</span>
          </th>
          <th data-testid="coluna-situacao" style="cursor:pointer" @click="ordenarPor('situacao')">
            Situação
            <span v-if="criterio === 'situacao'">{{ asc ? '↑' : '↓' }}</span>
          </th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="processo in processosOrdenados" :key="processo.id" style="cursor:pointer;"
            @click="abrirDetalhesProcesso(processo)">
          <td>
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
        <div class="mb-0 display-6" data-testid="titulo-alertas">Alertas</div>
      </div>
      <table class="table" data-testid="tabela-alertas">
        <thead>
        <tr>
          <th>Data/Hora</th>
          <th>Descrição</th>
          <th>Processo</th>
          <th>Origem</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(alerta, index) in alertasFormatados" :key="index">
          <td>{{ formatarDataHora(alerta.data) }}</td>
          <td>{{ alerta.descricao }}</td>
          <td>{{ alerta.processo }}</td>
          <td>{{ alerta.unidade }}</td>
        </tr>
        <tr v-if="!alertas || alertas.length === 0">
          <td class="text-center text-muted" colspan="4">Nenhum alerta no momento.</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {storeToRefs} from 'pinia'
import {usePerfilStore} from '@/stores/perfil'
import {useProcessosStore} from '@/stores/processos'
import {useAlertasStore} from '@/stores/alertas'
import {useRouter} from 'vue-router'
import {Alerta, Perfil, Processo} from '@/types/tipos'

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
  const perfilUsuario = perfil.perfilSelecionado;
  if (perfilUsuario === Perfil.ADMIN || perfilUsuario === Perfil.GESTOR) {
    router.push({name: 'Processo', params: {idProcesso: processo.id}})
  } else { // CHEFE ou SERVIDOR
    const siglaUnidade = perfil.unidadeSelecionada;
    if (siglaUnidade) {
      router.push({name: 'Subprocesso', params: {idProcesso: processo.id, siglaUnidade: siglaUnidade}})
    } else {
      console.error('Unidade do usuário não encontrada para o perfil CHEFE/SERVIDOR.');
    }
  }
}

const alertasFormatados = computed(() => {
  return (alertas.value as Alerta[]).map(alerta => {
    const processo = processos.value.find(p => p.id === alerta.idProcesso);

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