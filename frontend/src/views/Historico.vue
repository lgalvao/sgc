<template>
  <div class="container mt-4">
    <h2 class="display-6">
      Histórico de processos
    </h2>
    <TabelaProcessos
      :processos="processosFinalizadosOrdenadosComFormatacao"
      :criterio-ordenacao="criterio"
      :direcao-ordenacao-asc="asc"
      :show-data-finalizacao="true"
      @ordenar="ordenarPor"
      @selecionar-processo="abrirProcesso"
    />

    <div
      v-if="processosFinalizadosOrdenadosComFormatacao.length === 0"
      class="alert alert-info mt-4"
    >
      Nenhum processo finalizado.
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'

import {useProcessosStore} from '@/stores/processos'
import {usePerfilStore} from '@/stores/perfil'
import {Perfil} from '@/types/tipos'
import TabelaProcessos from '@/components/TabelaProcessos.vue';
import {ProcessoResumo} from '../mappers/processos';
import {formatDateTimeBR} from '@/utils';

type SortCriteria = keyof Processo | 'unidades' | 'dataFinalizacao';

const router = useRouter()
const processosStore = useProcessosStore()

const perfil = usePerfilStore()

const criterio = ref<SortCriteria>('descricao')
const asc = ref(true)

const processosFinalizados = ref<Processo[]>([]);

onMounted(async () => {
  // TODO: Implementar a busca de processos finalizados do backend
  // Por enquanto, vamos usar os processos do painel e filtrar
  await processosStore.fetchProcessosPainel(perfil.perfilSelecionado || '', Number(perfil.unidadeSelecionada) || 0, 0, 100); // Buscar um número maior para simular todos
  processosFinalizados.value = processosStore.processosPainel.filter(p => p.situacao === 'FINALIZADO').map(p => ({
    ...p,
    id: p.codigo,
    dataFinalizacao: p.dataFinalizacao ? new Date(p.dataFinalizacao) : null,
    dataLimite: new Date(p.dataLimite),
  }));
});

const processosFinalizadosOrdenados = computed(() => {
  return [...processosFinalizados.value].sort((a: Processo, b: Processo) => {

    if (criterio.value === 'unidades') {
      // TODO: Implementar lógica de ordenação por unidades
      return 0;
    } else if (criterio.value === 'dataFinalizacao') {
      const dateA = a.dataFinalizacao ? new Date(a.dataFinalizacao).getTime() : null;
      const dateB = b.dataFinalizacao ? new Date(b.dataFinalizacao).getTime() : null;

      if (dateA === null && dateB === null) return 0;
      if (dateA === null) return asc.value ? -1 : 1;
      if (dateB === null) return asc.value ? 1 : -1;
      return (dateA - dateB) * (asc.value ? 1 : -1);
    } else if (criterio.value === 'descricao') {
      const valA = String(a.descricao);
      const valB = String(b.descricao);
      if (valA < valB) return asc.value ? -1 : 1;
      if (valA > valB) return asc.value ? 1 : -1;
      return 0;
    } else if (criterio.value === 'tipo') {
      const valA = String(a.tipo);
      const valB = String(b.tipo);
      if (valA < valB) return asc.value ? -1 : 1;
      if (valA > valB) return asc.value ? 1 : -1;
      return 0;
    } else if (criterio.value === 'situacao') {
      const valA = String(a.situacao);
      const valB = String(b.situacao);
      if (valA < valB) return asc.value ? -1 : 1;
      if (valA > valB) return asc.value ? 1 : -1;
      return 0;
    }
    return 0; // Fallback para garantir que todos os caminhos retornem um valor
  });
});

const processosFinalizadosOrdenadosComFormatacao = computed(() => {
  return processosFinalizadosOrdenados.value.map(p => ({
    ...p,
    unidadesFormatadas: 'N/A', // TODO: Obter unidades do processo detalhe
    dataFinalizacaoFormatada: p.dataFinalizacao ? formatDateTimeBR(new Date(p.dataFinalizacao)) : null
  }));
});


function ordenarPor(campo: SortCriteria) {
  if (criterio.value === campo) {
    asc.value = !asc.value
  } else {
    criterio.value = campo
    asc.value = true
  }
}

function abrirProcesso(processo: ProcessoResumo) {
  const perfilUsuario = perfil.perfilSelecionado;
  if (perfilUsuario === Perfil.ADMIN || perfilUsuario === Perfil.GESTOR) {
    router.push({name: 'Processo', params: {idProcesso: processo.codigo.toString()}}); 
  } else { // CHEFE ou SERVIDOR
    const siglaUnidade = perfil.unidadeSelecionada;
    if (siglaUnidade) {
      router.push({name: 'Subprocesso', params: {idProcesso: processo.codigo, siglaUnidade: siglaUnidade}})
    } else {
      console.error('Unidade do usuário não encontrada para o perfil CHEFE/SERVIDOR.');
    }
  }
}