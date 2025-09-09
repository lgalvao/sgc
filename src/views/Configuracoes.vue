<template>
  <div class="container mt-4">
    <h2>Configurações do Sistema</h2>
    <form @submit.prevent="salvarConfiguracoes">
      <div class="mb-3">
        <label
            class="form-label"
            for="diasInativacaoProcesso"
        >Dias para inativação de processos:</label>
        <input
            id="diasInativacaoProcesso"
            v-model.number="configuracoesStore.diasInativacaoProcesso"
            class="form-control"
            min="1"
            required
            type="number"
        >
        <div class="form-text">
          Dias depois da finalização de um processo para que seja considerado inativo.
        </div>
      </div>

      <div class="mb-3">
        <label
            class="form-label"
            for="diasAlertaNovo"
        >Dias para indicação de alerta como novo:</label>
        <input
            id="diasAlertaNovo"
            v-model.number="configuracoesStore.diasAlertaNovo"
            class="form-control"
            min="1"
            required
            type="number"
        >
        <div class="form-text">
          Dias depois de um alerta ser enviado para uma unidade, para que deixe de ser marcado como
          novo.
        </div>
      </div>

      <button
          class="btn btn-primary"
          type="submit"
      >
        Salvar
      </button>
    </form>
    <div
        v-if="mensagemSucesso"
        class="alert alert-success mt-3"
        role="alert"
    >
      {{ mensagemSucesso }}
    </div>

    <!-- Cenários de Demonstração -->
    <div class="mt-5">
      <h3>Cenários de Demonstração</h3>
      <p class="text-muted">
        Carregue cenários pré-configurados para demonstração das funcionalidades do sistema.
      </p>

      <div class="row">
        <div class="col-md-6 mb-3">
          <div class="card">
            <div class="card-body">
              <h5 class="card-title">
                Cenário: Processo em Andamento
              </h5>
              <p class="card-text">
                Processo de mapeamento ativo com unidades em diferentes etapas do fluxo.
              </p>
              <button
                  class="btn btn-outline-primary"
                  @click="carregarCenarioAndamento"
              >
                Carregar Cenário
              </button>
            </div>
          </div>
        </div>

        <div class="col-md-6 mb-3">
          <div class="card">
            <div class="card-body">
              <h5 class="card-title">
                Cenário: Processo Finalizado
              </h5>
              <p class="card-text">
                Processo concluído com mapas vigentes para demonstração de relatórios.
              </p>
              <button
                  class="btn btn-outline-success"
                  @click="carregarCenarioFinalizado"
              >
                Carregar Cenário
              </button>
            </div>
          </div>
        </div>

        <div class="col-md-6 mb-3">
          <div class="card">
            <div class="card-body">
              <h5 class="card-title">
                Cenário: Revisão Completa
              </h5>
              <p class="card-text">
                Processo de revisão com alterações e validações em andamento.
              </p>
              <button
                  class="btn btn-outline-warning"
                  @click="carregarCenarioRevisao"
              >
                Carregar Cenário
              </button>
            </div>
          </div>
        </div>

        <div class="col-md-6 mb-3">
          <div class="card">
            <div class="card-body">
              <h5 class="card-title">
                Cenário: Diagnóstico
              </h5>
              <p class="card-text">
                Processo de diagnóstico com gaps identificados e relatórios completos.
              </p>
              <button
                  class="btn btn-outline-info"
                  @click="carregarCenarioDiagnostico"
              >
                Carregar Cenário
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-3">
        <button
            class="btn btn-outline-secondary"
            @click="resetarDados"
        >
          <i class="bi bi-arrow-counterclockwise"/> Resetar Todos os Dados
        </button>
      </div>
    </div>

    <div
        v-if="mensagemCenario"
        class="alert alert-info mt-3"
        role="alert"
    >
      {{ mensagemCenario }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {useConfiguracoesStore} from '@/stores/configuracoes';
import {useProcessosStore} from '@/stores/processos';
import {useMapasStore} from '@/stores/mapas';
import {useAtividadesStore} from '@/stores/atividades';
import {useAlertasStore} from '@/stores/alertas';

import {SituacaoProcesso, TipoProcesso} from '@/types/tipos';

const configuracoesStore = useConfiguracoesStore();
const processosStore = useProcessosStore();
const mapasStore = useMapasStore();
const atividadesStore = useAtividadesStore();
const alertasStore = useAlertasStore();


const mensagemSucesso = ref('');
const mensagemCenario = ref('');

const salvarConfiguracoes = () => {
  if (configuracoesStore.saveConfiguracoes()) {
    mensagemSucesso.value = 'Configurações salvas com sucesso!';
    setTimeout(() => {
      mensagemSucesso.value = '';
    }, 3000);
  }
};

const carregarCenarioAndamento = () => {
  resetarDados();

  // Cenário: Processo de mapeamento em andamento
  processosStore.processos = [
    {
      id: 1,
      descricao: 'Mapeamento de competências - Demonstração',
      tipo: TipoProcesso.MAPEAMENTO,
      dataLimite: new Date('2025-12-31'),
      situacao: SituacaoProcesso.EM_ANDAMENTO,
      dataFinalizacao: null
    }
  ];

  processosStore.subprocessos = [
    {
      id: 1,
      idProcesso: 1,
      unidade: 'SESEL',
      situacao: 'Cadastro em andamento',
      unidadeAtual: 'SESEL',
      unidadeAnterior: null,
      dataLimiteEtapa1: new Date('2025-12-31'),
      dataFimEtapa1: null,
      dataLimiteEtapa2: null,
      dataFimEtapa2: null
    },
    {
      id: 2,
      idProcesso: 1,
      unidade: 'COSIS',
      situacao: 'Mapa criado',
      unidadeAtual: 'SEDOC',
      unidadeAnterior: null,
      dataLimiteEtapa1: new Date('2025-12-31'),
      dataFimEtapa1: null,
      dataLimiteEtapa2: null,
      dataFimEtapa2: null
    }
  ];

  mensagemCenario.value = 'Cenário "Processo em Andamento" carregado com sucesso!';
  setTimeout(() => mensagemCenario.value = '', 3000);
};

const carregarCenarioFinalizado = () => {
  resetarDados();

  // Cenário: Processo finalizado com mapas vigentes
  processosStore.processos = [
    {
      id: 1,
      descricao: 'Mapeamento concluído - Demonstração',
      tipo: TipoProcesso.MAPEAMENTO,
      dataLimite: new Date('2025-06-30'),
      dataFinalizacao: new Date('2025-07-15'),
      situacao: SituacaoProcesso.FINALIZADO
    }
  ];

  processosStore.subprocessos = [
    {
      id: 1,
      idProcesso: 1,
      unidade: 'SESEL',
      situacao: 'Mapa homologado',
      unidadeAtual: 'SEDOC',
      unidadeAnterior: null,
      dataLimiteEtapa1: new Date('2025-06-30'),
      dataFimEtapa1: new Date('2025-07-15'),
      dataLimiteEtapa2: new Date('2025-07-15'),
      dataFimEtapa2: new Date('2025-07-15')
    },
    {
      id: 2,
      idProcesso: 1,
      unidade: 'COSIS',
      situacao: 'Mapa homologado',
      unidadeAtual: 'SEDOC',
      unidadeAnterior: null,
      dataLimiteEtapa1: new Date('2025-06-30'),
      dataFimEtapa1: new Date('2025-07-15'),
      dataLimiteEtapa2: new Date('2025-07-15'),
      dataFimEtapa2: new Date('2025-07-15')
    }
  ];

  // Adicionar mapas vigentes
  mapasStore.mapas = [
    {
      id: 1,
      unidade: 'SESEL',
      idProcesso: 1,
      competencias: [
        { id: 1, descricao: 'Competência Técnica', atividadesAssociadas: [1] }
      ],
      situacao: 'vigente',
      dataCriacao: new Date('2025-06-15'),
      dataDisponibilizacao: new Date('2025-07-10'),
      dataFinalizacao: new Date('2025-07-15')
    }
  ];

  mensagemCenario.value = 'Cenário "Processo Finalizado" carregado com sucesso!';
  setTimeout(() => mensagemCenario.value = '', 3000);
};

const carregarCenarioRevisao = () => {
  resetarDados();

  // Cenário: Processo de revisão
  processosStore.processos = [
    {
      id: 1,
      descricao: 'Revisão de mapas - Demonstração',
      tipo: TipoProcesso.REVISAO,
      dataLimite: new Date('2025-11-30'),
      situacao: SituacaoProcesso.EM_ANDAMENTO,
      dataFinalizacao: null
    }
  ];

  processosStore.subprocessos = [
    {
      id: 1,
      idProcesso: 1,
      unidade: 'SESEL',
      situacao: 'Revisão do cadastro em andamento',
      unidadeAtual: 'SESEL',
      unidadeAnterior: null,
      dataLimiteEtapa1: new Date('2025-11-30'),
      dataFimEtapa1: null,
      dataLimiteEtapa2: null,
      dataFimEtapa2: null
    }
  ];

  mensagemCenario.value = 'Cenário "Revisão Completa" carregado com sucesso!';
  setTimeout(() => mensagemCenario.value = '', 3000);
};

const carregarCenarioDiagnostico = () => {
  resetarDados();

  // Cenário: Processo de diagnóstico
  processosStore.processos = [
    {
      id: 1,
      descricao: 'Diagnóstico de competências - Demonstração',
      tipo: TipoProcesso.DIAGNOSTICO,
      dataLimite: new Date('2025-10-15'),
      situacao: SituacaoProcesso.EM_ANDAMENTO,
      dataFinalizacao: null
    }
  ];

  mensagemCenario.value = 'Cenário "Diagnóstico" carregado com sucesso!';
  setTimeout(() => mensagemCenario.value = '', 3000);
};

const resetarDados = () => {
  // Resetar todas as stores para estado inicial
  processosStore.processos = [];
  processosStore.subprocessos = [];
  mapasStore.mapas = [];
  atividadesStore.atividades = [];
  alertasStore.alertas = [];

  mensagemCenario.value = 'Todos os dados foram resetados!';
  setTimeout(() => mensagemCenario.value = '', 3000);
};

onMounted(() => {
  configuracoesStore.loadConfiguracoes();
});
</script>

<style scoped>
/* Estilos para o componente de configurações */
</style>