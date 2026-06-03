<template>
  <LayoutPadrao>
    <PageHeader title="Histórico"/>

    <CarregamentoPagina v-if="loading"/>

    <TabelaProcessos
        v-else
        :compacto="true"
        :criterio-ordenacao="criterio"
        :direcao-ordenacao-asc="asc"
        :empty-description="TEXTOS.historico.EMPTY_DESCRIPTION(diasInativacao)"
        :empty-title="TEXTOS.historico.EMPTY_TITLE"
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
import logger from '@/utils/logger';
import {useRouter} from 'vue-router';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import TabelaProcessos from "@/components/processo/TabelaProcessos.vue";
import {usePerfilStore} from '@/stores/perfil';
import {useConfiguracoes} from '@/composables/useConfiguracoes';
import {useHistoricoQuery} from '@/composables/useHistoricoQuery';
import {TEXTOS} from '@/constants/textos';
import type {ProcessoResumo} from "@/types/tipos";

const router = useRouter();
const perfilStore = usePerfilStore();
const {carregarConfiguracoes, obterDiasInativacaoProcesso} = useConfiguracoes();
const historicoQuery = useHistoricoQuery();

const loading = computed(() => historicoQuery.isPending.value || historicoQuery.isLoading.value);
const podeCarregarConfiguracoes = computed(() => perfilStore.permissoesSessao?.mostrarMenuConfiguracoes === true);

const criterio = ref<keyof ProcessoResumo>("dataFinalizacao");
const asc = ref(false);

const diasInativacao = computed(() => obterDiasInativacaoProcesso());

const processosOrdenados = computed(() => {
  const lista = [...(historicoQuery.data.value ?? [])];
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
    void router.push(path);
  }
}

// Flag para distinguir o primeiro mount de ativações subsequentes (keepAlive).
// onActivated é chamado também no primeiro mount, antes de onMounted — a flag evita
// recarregamento duplo nesse caso.
let montadoUmaVez = false;

async function carregarDadosTela(deveRecarregarHistorico: boolean) {
  const promessas = [];
  if (deveRecarregarHistorico) {
    promessas.push(void historicoQuery.refetch());
  }
  if (podeCarregarConfiguracoes.value) {
    promessas.push(carregarConfiguracoes());
  }

  try {
    await Promise.all(promessas);
  } catch (erro) {
    logger.error('Erro ao carregar dados da tela de histórico', erro);
  }
}

onMounted(async () => {
  await carregarDadosTela(true);
  montadoUmaVez = true;
});

onActivated(async () => {
  if (!montadoUmaVez) return;
  try {
    await historicoQuery.refresh();
  } catch (e) {
    // Erros em recarga de background no histórico são ignorados para manter a UI estável
  }
});
</script>
