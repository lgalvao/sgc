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
        :processos="processosOrdenados"
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
      <TabelaAlertas
        :alertas="alertasOrdenados"
        @ordenar="ordenarAlertasPor"
        @marcar-como-lido="marcarComoLido"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {storeToRefs} from 'pinia'
import {usePerfilStore} from '@/stores/perfil'
import {useProcessosStore} from '@/stores/processos'
import {useAlertasStore} from '@/stores/alertas'
import {useRouter} from 'vue-router'
import {Perfil, type ProcessoResumo, type AlertaFormatado} from '@/types/tipos'
import TabelaProcessos from '@/components/TabelaProcessos.vue';
import TabelaAlertas from '@/components/TabelaAlertas.vue';
import {formatDateTimeBR} from '@/utils';

const perfil = usePerfilStore()
const processosStore = useProcessosStore()
const alertasStore = useAlertasStore()

const { processosPainel } = storeToRefs(processosStore)
const { alertas } = storeToRefs(alertasStore)

const router = useRouter()

const criterio = ref<keyof ProcessoResumo>('descricao')
const asc = ref(true)

onMounted(() => {
  if (perfil.perfilSelecionado && perfil.unidadeSelecionada) {
    processosStore.fetchProcessosPainel(perfil.perfilSelecionado, Number(perfil.unidadeSelecionada), 0, 10); // Paginação inicial
    alertasStore.fetchAlertas(perfil.servidorId?.toString() || '', Number(perfil.unidadeSelecionada), 0, 10); // Paginação inicial
  }
});

const processosOrdenados = computed(() => {
  return [...processosPainel.value].sort((a, b) => {
    let valA: unknown = a[criterio.value]
    let valB: unknown = b[criterio.value]

    const valAString = String(valA);
    const valBString = String(valB);

    if (valAString < valBString) return asc.value ? -1 : 1;
    if (valAString > valBString) return asc.value ? 1 : -1;
    return 0
  });
})

function ordenarPor(campo: keyof ProcessoResumo) {
  if (criterio.value === campo) {
    asc.value = !asc.value
  } else {
    criterio.value = campo
    asc.value = true
  }
}

function abrirDetalhesProcesso(processo: ProcessoResumo) {
  const perfilUsuario = perfil.perfilSelecionado;
  
  // CDU-05: Para ADMIN, processos "Criado" vão para tela de cadastro
  if (perfilUsuario === Perfil.ADMIN && processo.situacao === 'CRIADO') { 
    router.push({name: 'CadProcesso', query: {idProcesso: processo.codigo}})
    return;
  }
  
  if (perfilUsuario === Perfil.ADMIN || perfilUsuario === Perfil.GESTOR) {
    router.push({name: 'Processo', params: {idProcesso: processo.codigo}})
  } else { // CHEFE ou SERVIDOR
    const siglaUnidade = perfil.unidadeSelecionada;
    if (siglaUnidade) {
      router.push({name: 'Subprocesso', params: {idProcesso: processo.codigo, siglaUnidade: siglaUnidade}})
    } else {
      console.error('Unidade do usuário não encontrada para o perfil CHEFE/SERVIDOR.');
    }
  }
}

const alertasFormatados = computed((): AlertaFormatado[] => {
  return alertas.value.map(alerta => {
    const partes = alerta.mensagem.split(' ');
    return {
      ...alerta,
      dataHoraFormatada: formatDateTimeBR(new Date(alerta.data)),
      origem: partes[0],
      processo: partes[2],
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
      const da = new Date(a.data).getTime();
      const db = new Date(b.data).getTime();
      return alertaAsc.value ? da - db : db - da;
    } else {
      const pa = (a.processo || '').toString().toLowerCase();
      const pb = (b.processo || '').toString().toLowerCase();
      if (pa < pb) return alertaAsc.value ? -1 : 1;
      if (pa > pb) return alertaAsc.value ? 1 : -1;
      return 0;
    }
  });
});

function ordenarAlertasPor(campo: 'data' | 'processo') {
    if (campo === 'data') {
        if (alertaCriterio.value === 'data') {
            alertaAsc.value = !alertaAsc.value;
        } else {
            alertaCriterio.value = 'data';
            alertaAsc.value = false;
        }
    } else {
        if (alertaCriterio.value === 'processo') {
            alertaAsc.value = !alertaAsc.value;
        } else {
            alertaCriterio.value = 'processo';
            alertaAsc.value = true;
        }
    }
}

function marcarComoLido(idAlerta: number) {
  alertasStore.marcarAlertaComoLido(idAlerta);
}
</script>
