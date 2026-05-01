<template>
  <LayoutPadrao>
    <PageHeader title="Histórico"/>

    <CarregamentoPagina v-if="loading" />

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
import * as processoService from '@/services/processoService';
import {useHistoricoStore} from '@/stores/historico';
import type {ProcessoResumo} from "@/types/tipos";
import {logger} from '@/utils';

const router = useRouter();
const historicoStore = useHistoricoStore();
const loading = ref(false);

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

async function carregarHistorico() {
  loading.value = true;
  try {
    const processos = await processoService.buscarProcessosFinalizados() ?? [];
    historicoStore.definirDados(processos);
  } catch (e) {
    logger.error("Erro ao carregar histórico:", e);
    historicoStore.definirDados([]);
  } finally {
    loading.value = false;
  }
}

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

let montadoUmaVez = false;

onMounted(() => {
  montadoUmaVez = true;
  carregarHistorico();
});

onActivated(() => {
  if (!montadoUmaVez) return;
  if (historicoStore.dadosValidos()) return;
  carregarHistorico();
});
</script>
