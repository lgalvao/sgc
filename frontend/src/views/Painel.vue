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
          <i class="bi bi-plus-lg" /> Criar processo
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
            <th
              style="cursor: pointer;"
              @click="ordenarAlertasPor('data')"
            >
              Data/Hora
            </th>
            <th>Descrição</th>
            <th
              style="cursor: pointer;"
              @click="ordenarAlertasPor('processo')"
            >
              Processo
            </th>
            <th>Origem</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(alerta, index) in alertasOrdenados"
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
          <tr v-if="!alertasOrdenados || alertasOrdenados.length === 0">
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
import {computed, onMounted, ref} from 'vue'
import {storeToRefs} from 'pinia'
import {usePerfilStore} from '@/stores/perfil'
import {useProcessosStore} from '@/stores/processos'
import { useSubprocessosStore } from '@/stores/subprocessos'
import {useAlertasStore} from '@/stores/alertas'
import {useRouter} from 'vue-router'
import {Perfil, Processo} from '@/types/tipos'
import TabelaProcessos from '@/components/TabelaProcessos.vue';
import {useProcessosFiltrados} from '@/composables/useProcessosFiltrados'; // Importar o novo composable
import {formatDateTimeBR} from '@/utils';

const perfil = usePerfilStore()
const processosStore = useProcessosStore()
const subprocessosStore = useSubprocessosStore()
const alertasStore = useAlertasStore()

const {processos} = storeToRefs(processosStore) // Manter para alertas, mas não para a tabela de processos


const router = useRouter()

const criterio = ref<keyof Processo | 'unidades'>('descricao')
const asc = ref(true)

// Usar o novo composable para obter os processos filtrados
const { processosFiltrados } = useProcessosFiltrados();

onMounted(async () => {
  // Garante que os subprocessos de todos os processos visíveis sejam carregados
  for (const processo of processosFiltrados.value) {
    await processosStore.carregarDetalhesProcesso(processo.id);
  }
});

const processosOrdenados = computed<Processo[]>(() => {
  return [...processosFiltrados.value].sort((a, b) => {
    let valA: unknown = a[criterio.value as keyof Processo]
    let valB: unknown = b[criterio.value as keyof Processo]

    if (criterio.value === 'unidades') {
      valA = subprocessosStore.getUnidadesDoProcesso(a.id).map(pu => pu.unidade).join(', ')
      valB = subprocessosStore.getUnidadesDoProcesso(b.id).map(pu => pu.unidade).join(', ')
    }

    const valAString = String(valA);
    const valBString = String(valB);

    if (valAString < valBString) return asc.value ? -1 : 1;
    if (valAString > valBString) return asc.value ? 1 : -1;
    return 0
  })
})

// Novo computed para formatar as unidades antes de passar para o componente filho
const processosOrdenadosComUnidades = computed(() => {
  return processosOrdenados.value.map(p => ({
    ...p,
    unidadesFormatadas: subprocessosStore.getUnidadesDoProcesso(p.id).map(pu => pu.unidade).join(', ')
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
  
  // CDU-05: Para ADMIN, processos "Criado" vão para tela de cadastro
  if (perfilUsuario === Perfil.ADMIN && processo.situacao === 'Criado') {
    router.push({name: 'CadProcesso', query: {idProcesso: processo.id}})
    return;
  }
  
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

// Ordenação de alertas por coluna (CDU-02 - cabeçalho "Processo" e padrão por data desc)
const alertaCriterio = ref<'data' | 'processo'>('data');
const alertaAsc = ref(false); // false = desc (padrão por data/hora)

const alertasOrdenados = computed(() => {
  const lista = [...alertasFormatados.value];
  return lista.sort((a, b) => {
    if (alertaCriterio.value === 'data') {
      const da = a.data.getTime();
      const db = b.data.getTime();
      return alertaAsc.value ? da - db : db - da;
    } else {
      const pa = (a.processo || '').toLowerCase();
      const pb = (b.processo || '').toLowerCase();
      if (pa < pb) return alertaAsc.value ? -1 : 1;
      if (pa > pb) return alertaAsc.value ? 1 : -1;
      return 0;
    }
  });
});

function ordenarAlertasPor(campo: 'data' | 'processo') {
  if (alertaCriterio.value === campo) {
    alertaAsc.value = !alertaAsc.value;
  } else {
    alertaCriterio.value = campo;
    alertaAsc.value = true; // primeira ordenação do campo em asc
  }
}

// Removida função formatarDataHora - usando utilitário centralizado

function marcarComoLido(idAlerta: number) {
  alertasStore.marcarAlertaComoLido(idAlerta);
}
</script>
