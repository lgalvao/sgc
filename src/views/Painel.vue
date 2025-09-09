<template>
  <div class="container mt-4">
    <!-- Tabela de Processos -->
    <div class="mb-5">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div
            class="display-6 mb-0"
            data-testid="titulo-processos"
        >
          Processos
        </div>
        <router-link
            v-if="perfil.perfilSelecionado === 'ADMIN'"
            :to="{ name: 'CadProcesso' }"
            class="btn btn-outline-primary"
            data-testid="btn-criar-processo"
        >
          <i class="bi bi-plus-lg"/> Criar processo
        </router-link>
      </div>
      <TabelaProcessos
          :processos="processosOrdenadosComUnidades"
          :criterio-ordenacao="criterio"
          :direcao-ordenacao-asc="asc"
          @ordenar="ordenarPor"
          @selecionar-processo="abrirDetalhesProcesso"
      />
    </div>

    <div>
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div
            class="mb-0 display-6"
            data-testid="titulo-alertas"
        >
          Alertas
        </div>
      </div>
      <table
          class="table"
          data-testid="tabela-alertas"
      >
        <thead>
        <tr>
          <th>Data/Hora</th>
          <th>Descrição</th>
          <th>Processo</th>
          <th>Origem</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="(alerta, index) in alertasFormatados"
            :key="index"
            :class="{ 'fw-bold': !alerta.lido }"
            style="cursor: pointer;"
            @click="marcarComoLido(alerta.id)"
        >
          <td>{{ alerta.dataFormatada }}</td>
          <td>{{ alerta.descricao }}</td>
          <td>{{ alerta.processo }}</td>
          <td>{{ alerta.unidade }}</td>
        </tr>
        <tr v-if="!alertasFormatados || alertasFormatados.length === 0">
          <td
              class="text-center text-muted"
              colspan="4"
          >
            Nenhum alerta no momento.
          </td>
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
import {Perfil, Processo} from '@/types/tipos'
import TabelaProcessos from '@/components/TabelaProcessos.vue';
import {useProcessosFiltrados} from '@/composables/useProcessosFiltrados'; // Importar o novo composable
import {formatDateTimeBR} from '@/utils/dateUtils';

const perfil = usePerfilStore()
const processosStore = useProcessosStore()
const alertasStore = useAlertasStore()

const {processos} = storeToRefs(processosStore) // Manter para alertas, mas não para a tabela de processos


const router = useRouter()

const criterio = ref<keyof Processo | 'unidades'>('descricao')
const asc = ref(true)

// Usar o novo composable para obter os processos filtrados
const { processosFiltrados } = useProcessosFiltrados();

const processosOrdenados = computed<Processo[]>(() => {
  return [...processosFiltrados.value].sort((a, b) => {
    let valA: string | number | Date | null = a[criterio.value as keyof Processo]
    let valB: string | number | Date | null = b[criterio.value as keyof Processo]

    if (criterio.value === 'unidades') {
      valA = processosStore.getUnidadesDoProcesso(a.id).map(pu => pu.unidade).join(', ')
      valB = processosStore.getUnidadesDoProcesso(b.id).map(pu => pu.unidade).join(', ')
    }

    if (valA < valB) return asc.value ? -1 : 1
    if (valA > valB) return asc.value ? 1 : -1
    return 0
  })
})

// Novo computed para formatar as unidades antes de passar para o componente filho
const processosOrdenadosComUnidades = computed(() => {
  return processosOrdenados.value.map(p => ({
    ...p,
    unidadesFormatadas: processosStore.getUnidadesDoProcesso(p.id).map(pu => pu.unidade).join(', ')
  }));
});

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
  const alertasDoServidor = alertasStore.getAlertasDoServidor();

  return alertasDoServidor.map(alerta => {
    const processo = processos.value.find(p => p.id === alerta.idProcesso);

    return {
      id: alerta.id,
      data: alerta.dataHora,
      processo: processo ? processo.descricao : 'Processo não encontrado',
      unidade: alerta.unidadeOrigem,
      descricao: alerta.descricao,
      dataFormatada: formatDateTimeBR(alerta.dataHora),
      lido: alerta.lido
    };
  });
});

// Removida função formatarDataHora - usando utilitário centralizado

function marcarComoLido(idAlerta: number) {
  alertasStore.marcarAlertaComoLido(idAlerta);
}
</script>
