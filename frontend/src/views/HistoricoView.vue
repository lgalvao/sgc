<template>
  <LayoutPadrao>
    <PageHeader title="Histórico"/>

    <CarregamentoPagina v-if="loading"/>

    <TabelaProcessos
        v-else
        :compacto="true"
        :criterio-ordenacao="criterio"
        :direcao-ordenacao-asc="asc"
        :processos="processosOrdenados"
        :show-data-finalizacao="true"
        :show-situacao="false"
        @ordenar="ordenarPor"
        @selecionar-processo="verDetalhes"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onActivated, onMounted, ref} from 'vue';
import {useRouter} from 'vue-router';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import TabelaProcessos from "@/components/processo/TabelaProcessos.vue";
import {useHistoricoStore} from '@/stores/historico';
import type {ProcessoResumo} from "@/types/tipos";

const router = useRouter();
const historicoStore = useHistoricoStore();
const loading = computed(() => historicoStore.carregando);

const criterio = ref<keyof ProcessoResumo>("dataFinalizacao");
const asc = ref(false);

const processosOrdenados = computed(() => {
  const lista = [...historicoStore.processos];
  const campo = criterio.value;
  const direcao = asc.value ? 1 : -1;

  return lista.sort((a, b) => {
    const valA = a[campo];
    const valB = b[campo];

    if (valA === undefined || valA === null) return 1;
    if (valB === undefined || valB === null) return -1;

    if (valA < valB) return -1 * direcao;
    if (valA > valB) return 1 * direcao;
    return 0;
  });
});

function ordenarPor(campo: keyof ProcessoResumo) {
  if (criterio.value === campo) {
    asc.value = !asc.value;
  } else {
    criterio.value = campo;
    asc.value = true;
  }
}

function verDetalhes(proc: ProcessoResumo | undefined) {
  if (proc) {
    const path = proc.linkDestino || `/processo/${proc.codigo}`;
    router.push(path);
  }
}

// Flag para distinguir o primeiro mount de ativações subsequentes (keepAlive).
// onActivated é chamado também no primeiro mount, antes de onMounted — a flag evita
// recarregamento duplo nesse caso.
let montadoUmaVez = false;

onMounted(() => {
  montadoUmaVez = true;
  void historicoStore.garantirDados();
});

onActivated(() => {
  if (!montadoUmaVez) return;
  if (historicoStore.dadosValidos()) return;
  void historicoStore.garantirDados();
});
</script>
