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
import {usePerfil} from '@/composables/usePerfil'
import {Perfil, type ProcessoResumo} from '@/types/tipos'
import TabelaProcessos from '@/components/TabelaProcessos.vue';
import {formatDateTimeBR} from '@/utils';

type SortCriteria = keyof ProcessoResumo | 'dataFinalizacao';

const router = useRouter()
const processosStore = useProcessosStore()
const perfilStore = usePerfilStore()
const {unidadeSelecionada} = usePerfil()

const criterio = ref<SortCriteria>('descricao')
const asc = ref(true)

onMounted(async () => {
  await processosStore.fetchProcessosFinalizados();
});

const processosFinalizadosOrdenados = computed(() => {
  return [...processosStore.processosFinalizados].sort((a, b) => {
    if (criterio.value === 'dataFinalizacao') {
      const dateA = a.dataFinalizacao ? new Date(a.dataFinalizacao).getTime() : 0;
      const dateB = b.dataFinalizacao ? new Date(b.dataFinalizacao).getTime() : 0;
      return (dateA - dateB) * (asc.value ? 1 : -1);
    } else {
      const valA = String(a[criterio.value as keyof ProcessoResumo]);
      const valB = String(b[criterio.value as keyof ProcessoResumo]);
      if (valA < valB) return asc.value ? -1 : 1;
      if (valA > valB) return asc.value ? 1 : -1;
    }
    return 0;
  });
});

const processosFinalizadosOrdenadosComFormatacao = computed(() => {
  return processosFinalizadosOrdenados.value.map(p => ({
    ...p,
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
  const perfilUsuario = perfilStore.perfilSelecionado;
  if (perfilUsuario === Perfil.ADMIN || perfilUsuario === Perfil.GESTOR) {
    router.push({name: 'Processo', params: {codProcesso: processo.codigo.toString()}});
  } else { // CHEFE ou SERVIDOR
    const sigla = unidadeSelecionada.value;
    if (sigla) {
      router.push({name: 'Subprocesso', params: {codProcesso: processo.codigo, siglaUnidade: sigla}})
    } else {
      console.error('Unidade do usuário não encontrada para o perfil CHEFE/SERVIDOR.');
    }
  }
}
</script>