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
import { useSubprocessosStore } from '@/stores/subprocessos'
import {usePerfilStore} from '@/stores/perfil'
import {Perfil, Processo, Subprocesso} from '@/types/tipos'
import TabelaProcessos from '@/components/TabelaProcessos.vue';
import {useProcessosFiltrados} from '@/composables/useProcessosFiltrados';

type SortCriteria = keyof Processo | 'unidades' | 'dataFinalizacao';

const router = useRouter()
const processosStore = useProcessosStore()
const subprocessosStore = useSubprocessosStore()
const perfil = usePerfilStore()

const criterio = ref<SortCriteria>('descricao')
const asc = ref(true)

const {processosFiltrados} = useProcessosFiltrados(ref(true));

onMounted(async () => {
  // Garante que os subprocessos de todos os processos visíveis sejam carregados
  for (const processo of processosFiltrados.value) {
    await processosStore.carregarDetalhesProcesso(processo.id);
  }
});

const processosFinalizadosOrdenados = computed(() => {
  return [...processosFiltrados.value].sort((a: Processo, b: Processo) => {

    if (criterio.value === 'unidades') {
      const valA = subprocessosStore.getUnidadesDoProcesso(a.id).map((pu: Subprocesso) => pu.unidade).join(', ');
      const valB = subprocessosStore.getUnidadesDoProcesso(b.id).map((pu: Subprocesso) => pu.unidade).join(', ');
      if (valA < valB) return asc.value ? -1 : 1;
      if (valA > valB) return asc.value ? 1 : -1;
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
    unidadesFormatadas: subprocessosStore.getUnidadesDoProcesso(p.id).map(pu => pu.unidade).join(', '),
    dataFinalizacaoFormatada: p.dataFinalizacao ? new Date(p.dataFinalizacao).toLocaleDateString('pt-BR') : null
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

function abrirProcesso(processo: Processo) {
  const perfilUsuario = perfil.perfilSelecionado;
  if (perfilUsuario === Perfil.ADMIN || perfilUsuario === Perfil.GESTOR) {
    router.push({name: 'Processo', params: {idProcesso: processo.id.toString()}}); // <-- Alterado aqui
  } else { // CHEFE ou SERVIDOR
    const siglaUnidade = perfil.unidadeSelecionada;
    if (siglaUnidade) {
      router.push({name: 'Subprocesso', params: {idProcesso: processo.id, siglaUnidade: siglaUnidade}})
    } else {
      console.error('Unidade do usuário não encontrada para o perfil CHEFE/SERVIDOR.');
    }
  }
}
</script>