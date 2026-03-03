<template>
  <LayoutPadrao>
    <PageHeader title="Histórico"/>

    <div v-if="loading" class="text-center py-5">
      <BSpinner label="Carregando..." variant="primary"/>
    </div>

    <TabelaProcessos
        v-else
        :compacto="true"
        :criterio-ordenacao="criterio"
        :direcao-ordenacao-asc="asc"
        :processos="processosOrdenados"
        :show-data-finalizacao="true"
        @ordenar="ordenarPor"
        @selecionar-processo="verDetalhes"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue';
import {useRouter} from 'vue-router';
import {BSpinner} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import TabelaProcessos from "@/components/processo/TabelaProcessos.vue";
import {useProcessosStore} from '@/stores/processos';
import type {ProcessoResumo} from "@/types/tipos";
import {logger} from '@/utils';

const router = useRouter();
const processosStore = useProcessosStore();
const processos = computed(() => processosStore.processosFinalizados);
const loading = ref(false);

const criterio = ref<keyof ProcessoResumo>("dataFinalizacao");
const asc = ref(false);

const processosOrdenados = computed(() => {
  const lista = [...processos.value];
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
    await processosStore.buscarProcessosFinalizados();
  } catch (e) {
    logger.error("Erro ao carregar histórico:", e);
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

onMounted(() => {
  carregarHistorico();
});
</script>

<style scoped>
</style>
