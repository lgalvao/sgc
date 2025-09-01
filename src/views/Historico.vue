<template>
  <div class="container mt-4">
    <h2 class="display-6">Histórico de processos</h2>
    <TabelaProcessos
        :processos="processosFinalizadosOrdenadosComFormatacao"
        :criterioOrdenacao="criterio"
        :direcaoOrdenacaoAsc="asc"
        :showDataFinalizacao="true"
        @ordenar="ordenarPor"
        @selecionarProcesso="abrirProcesso"
    />

    <div v-if="processosFinalizadosOrdenadosComFormatacao.length === 0" class="alert alert-info mt-4">
      Nenhum processo finalizado.
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'

import {useProcessosStore} from '@/stores/processos'
import {usePerfilStore} from '@/stores/perfil'
import {Perfil, Processo, Subprocesso} from '@/types/tipos'
import TabelaProcessos from '@/components/TabelaProcessos.vue';
import {useProcessosFiltrados} from '@/composables/useProcessosFiltrados';

type SortCriteria = 'descricao' | 'tipo' | 'unidades' | 'dataFinalizacao';

const router = useRouter()
const processosStore = useProcessosStore()

const perfil = usePerfilStore()

const criterio = ref<SortCriteria>('descricao')
const asc = ref(true)

const {processosFiltrados} = useProcessosFiltrados(ref(true));

const processosFinalizadosOrdenados = computed(() => {
  return [...processosFiltrados.value].sort((a: Processo, b: Processo) => {
    let valA: string | number | null;
    let valB: string | number | null;

    if (criterio.value === 'unidades') {
      valA = processosStore.getUnidadesDoProcesso(a.id).map((pu: Subprocesso) => pu.unidade).join(', ');
      valB = processosStore.getUnidadesDoProcesso(b.id).map((pu: Subprocesso) => pu.unidade).join(', ');
    } else if (criterio.value === 'dataFinalizacao') {
      const dateA = a.dataFinalizacao ? new Date(a.dataFinalizacao) : null;
      const dateB = b.dataFinalizacao ? new Date(b.dataFinalizacao) : null;
      valA = dateA && !isNaN(dateA.getTime()) ? dateA.getTime() : null;
      valB = dateB && !isNaN(dateB.getTime()) ? dateB.getTime() : null;

      if (valA === null && valB === null) return 0;
      if (valA === null) return asc.value ? -1 : 1;
      if (valB === null) return asc.value ? 1 : -1;
      return (valA - valB) * (asc.value ? 1 : -1);
    } else if (criterio.value === 'descricao') {
      valA = a.descricao;
      valB = b.descricao;
    } else if (criterio.value === 'tipo') {
      valA = a.tipo;
      valB = b.tipo;
    }

    if (valA < valB) return asc.value ? -1 : 1;
    if (valA > valB) return asc.value ? 1 : -1;
    return 0;
  });
});

const processosFinalizadosOrdenadosComFormatacao = computed(() => {
  return processosFinalizadosOrdenados.value.map(p => ({
    ...p,
    unidadesFormatadas: processosStore.getUnidadesDoProcesso(p.id).map(pu => pu.unidade).join(', '),
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
  console.log('Perfil do usuário:', perfilUsuario); // LOG
  console.log('Processo ID:', processo.id); // LOG

  if (perfilUsuario === Perfil.ADMIN || perfilUsuario === Perfil.GESTOR) {
    console.log('Navegando para /processo/' + processo.id); // LOG
    // Usar name e params explicitamente, garantindo que apenas idProcesso seja passado
    router.push({name: 'Processo', params: {idProcesso: processo.id.toString()}}); // <-- Alterado aqui
  } else { // CHEFE ou SERVIDOR
    const siglaUnidade = perfil.unidadeSelecionada;
    console.log('Navegando para /subprocesso/' + processo.id + '/' + siglaUnidade); // LOG
    if (siglaUnidade) {
      router.push({name: 'Subprocesso', params: {idProcesso: processo.id, siglaUnidade: siglaUnidade}})
    } else {
      console.error('Unidade do usuário não encontrada para o perfil CHEFE/SERVIDOR.');
    }
  }
}
</script>