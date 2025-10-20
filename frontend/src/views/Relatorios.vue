<template>
  <div class="container mt-4">
    <h2 class="display-6 mb-4">
      Relatórios
    </h2>

    <!-- Filtros -->
    <div class="row mb-4">
      <div class="col-md-4">
        <label
            for="filtroTipo"
            class="form-label"
        >Tipo de Processo</label>
        <select
            id="filtroTipo"
            v-model="filtroTipo"
            class="form-select"
        >
          <option value="">
            Todos
          </option>
          <option value="Mapeamento">
            Mapeamento
          </option>
          <option value="Revisão">
            Revisão
          </option>
          <option value="Diagnóstico">
            Diagnóstico
          </option>
        </select>
      </div>
      <div class="col-md-4">
        <label
            for="filtroDataInicio"
            class="form-label"
        >Data Início</label>
        <input
            id="filtroDataInicio"
            v-model="filtroDataInicio"
            type="date"
            class="form-control"
        >
      </div>
      <div class="col-md-4">
        <label
            for="filtroDataFim"
            class="form-label"
        >Data Fim</label>
        <input
            id="filtroDataFim"
            v-model="filtroDataFim"
            type="date"
            class="form-control"
        >
      </div>
    </div>

    <div class="row">
      <div class="col-md-4 mb-4">
        <div
            class="card h-100"
            style="cursor: pointer;"
            @click="abrirModalMapasVigentes"
        >
          <div class="card-body">
            <h5 class="card-title">
              Mapas Vigentes
            </h5>
            <p class="card-text">
              Visualize os mapas de competências atualmente vigentes em todas as unidades.
            </p>
            <small class="text-muted">{{ mapasVigentes.length }} mapas encontrados</small>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-4">
        <div
            class="card h-100"
            style="cursor: pointer;"
            @click="abrirModalDiagnosticosGaps"
        >
          <div class="card-body">
            <h5 class="card-title">
              Diagnósticos de Gaps
            </h5>
            <p class="card-text">
              Analise os gaps de competências identificados nos processos de diagnóstico.
            </p>
            <small class="text-muted">{{ diagnosticosGaps.length }} diagnósticos encontrados</small>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-4">
        <div
            class="card h-100"
            style="cursor: pointer;"
            @click="abrirModalAndamentoGeral"
        >
          <div class="card-body">
            <h5 class="card-title">
              Andamento Geral
            </h5>
            <p class="card-text">
              Acompanhe o andamento de todos os processos de mapeamento e revisão.
            </p>
            <small class="text-muted">{{ processosFiltrados.length }} processos encontrados</small>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Mapas Vigentes -->
    <div
        id="modalMapasVigentes"
        class="modal fade"
        :class="{ show: mostrarModalMapasVigentes }"
        :style="{ display: mostrarModalMapasVigentes ? 'block' : 'none' }"
        tabindex="-1"
    >
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Mapas Vigentes
            </h5>
            <button
                type="button"
                class="btn-close"
                @click="mostrarModalMapasVigentes = false"
            />
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <button
                  class="btn btn-outline-primary btn-sm"
                  data-testid="export-csv-mapas"
                  @click="exportarMapasVigentes"
              >
                <i class="bi bi-download"/> Exportar CSV
              </button>
            </div>
            <div class="table-responsive">
              <table class="table table-striped">
                <thead>
                <tr>
                  <th>Unidade</th>
                  <th>Competências</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="mapa in mapasVigentes"
                    :key="mapa.id"
                >
                  <td>{{ mapa.unidade }}</td>
                  <td>{{ mapa.competencias?.length || 0 }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Diagnósticos de Gaps -->
    <div
        id="modalDiagnosticosGaps"
        class="modal fade"
        :class="{ show: mostrarModalDiagnosticosGaps }"
        :style="{ display: mostrarModalDiagnosticosGaps ? 'block' : 'none' }"
        tabindex="-1"
    >
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Diagnósticos de Gaps
            </h5>
            <button
                type="button"
                class="btn-close"
                @click="mostrarModalDiagnosticosGaps = false"
            />
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <button
                  class="btn btn-outline-primary btn-sm"
                  data-testid="export-csv-diagnosticos"
                  @click="exportarDiagnosticosGaps"
              >
                <i class="bi bi-download"/> Exportar CSV
              </button>
            </div>
            <div class="table-responsive">
              <table class="table table-striped">
                <thead>
                <tr>
                  <th>Processo</th>
                  <th>Unidade</th>
                  <th>Gaps Identificados</th>
                  <th>Importância Média</th>
                  <th>Dominio Médio</th>
                  <th>Competências Críticas</th>
                  <th>Status</th>
                  <th>Data Diagnóstico</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="diagnostico in diagnosticosGapsFiltrados"
                    :key="diagnostico.id"
                >
                  <td>{{ diagnostico.processo }}</td>
                  <td>{{ diagnostico.unidade }}</td>
                  <td>{{ diagnostico.gaps }}</td>
                  <td>{{ diagnostico.importanciaMedia }}/5</td>
                  <td>{{ diagnostico.dominioMedio }}/5</td>
                  <td>
                    <small class="text-muted">
                      {{ diagnostico.competenciasCriticas.join(', ') }}
                    </small>
                  </td>
                  <td>
                      <span :class="getClasseStatus(diagnostico.status)">
                        {{ diagnostico.status }}
                      </span>
                  </td>
                  <td>{{ formatarData(diagnostico.data) }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Andamento Geral -->
    <div
        id="modalAndamentoGeral"
        class="modal fade"
        :class="{ show: mostrarModalAndamentoGeral }"
        :style="{ display: mostrarModalAndamentoGeral ? 'block' : 'none' }"
        tabindex="-1"
    >
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Andamento Geral dos Processos
            </h5>
            <button
                type="button"
                class="btn-close"
                @click="mostrarModalAndamentoGeral = false"
            />
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <button
                  class="btn btn-outline-primary btn-sm"
                  data-testid="export-csv-andamento"
                  @click="exportarAndamentoGeral"
              >
                <i class="bi bi-download"/> Exportar CSV
              </button>
            </div>
            <div class="table-responsive">
              <table class="table table-striped">
                <thead>
                <tr>
                  <th>Descrição</th>
                  <th>Tipo</th>
                  <th>Situação</th>
                  <th>Data Limite</th>
                  <th>Unidade</th>
                  <th>% Concluído</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="processo in processosFiltrados"
                    :key="processo.codigo"
                >
                  <td>{{ processo.descricao }}</td>
                  <td>{{ processo.tipo }}</td>
                  <td>{{ processo.situacao }}</td>
                  <td>{{ formatarData(new Date(processo.dataLimite)) }}</td>
                  <td>{{ processo.unidadeNome }}</td>
                  <td>{{ calcularPercentualConcluido }}%</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import {useProcessosStore} from '@/stores/processos'
import {useMapasStore} from '@/stores/mapas'
import {TipoProcesso} from '@/types/tipos';
import {formatDateBR} from '@/utils'

type CSVData = Record<string, string | number | undefined>;

const processosStore = useProcessosStore()
const mapasStore = useMapasStore()

const filtroTipo = ref('')
const filtroDataInicio = ref('')
const filtroDataFim = ref('')

const mostrarModalMapasVigentes = ref(false)
const mostrarModalDiagnosticosGaps = ref(false)
const mostrarModalAndamentoGeral = ref(false)

// Dados computados
const processosFiltrados = computed(() => {
  let processos = processosStore.processosPainel

  if (filtroTipo.value) {
    processos = processos.filter(p => p.tipo === filtroTipo.value)
  }

  if (filtroDataInicio.value) {
    const dataInicio = new Date(filtroDataInicio.value)
    processos = processos.filter(p => new Date(p.dataCriacao) >= dataInicio)
  }

  if (filtroDataFim.value) {
    const dataFim = new Date(filtroDataFim.value)
    processos = processos.filter(p => new Date(p.dataCriacao) <= dataFim)
  }

  return processos
})

const mapasVigentes = computed(() => {
  // Filtrar mapas vigentes (aqueles com processos finalizados)
  if (mapasStore.mapaCompleto && mapasStore.mapaCompleto.competencias && mapasStore.mapaCompleto.competencias.length > 0) {
    return [{
      ...(mapasStore.mapaCompleto as any),
      unidade: (mapasStore.mapaCompleto as any).unidade.sigla,
      id: mapasStore.mapaCompleto.codigo
    }]
  }
  return []
})

const diagnosticosGaps = computed(() => {
  // Simulação de dados de diagnóstico mais completos
  return [
    {
      id: 1,
      processo: 'Diagnóstico Anual 2024',
      unidade: 'SESEL',
      gaps: 3,
      importanciaMedia: 4.2,
      dominioMedio: 2.8,
      competenciasCriticas: ['Gestão de Processos', 'Análise de Dados', 'Comunicação'],
      data: new Date('2024-08-15'),
      status: 'Em análise'
    },
    {
      id: 2,
      processo: 'Diagnóstico Anual 2024',
      unidade: 'COJUR',
      gaps: 5,
      importanciaMedia: 3.9,
      dominioMedio: 3.1,
      competenciasCriticas: ['Gestão Jurídica', 'Análise de Riscos', 'Ética Profissional', 'Gestão de Equipes'],
      data: new Date('2024-08-20'),
      status: 'Finalizado'
    },
    {
      id: 3,
      processo: 'Diagnóstico Semestral 2024',
      unidade: 'COSIS',
      gaps: 2,
      importanciaMedia: 4.5,
      dominioMedio: 3.2,
      competenciasCriticas: ['Segurança da Informação', 'Gestão de Projetos'],
      data: new Date('2024-07-10'),
      status: 'Em análise'
    },
    {
      id: 4,
      processo: 'Diagnóstico Anual 2024',
      unidade: 'STIC',
      gaps: 4,
      importanciaMedia: 4.1,
      dominioMedio: 2.9,
      competenciasCriticas: ['Infraestrutura de TI', 'Suporte Técnico', 'Análise de Sistemas'],
      data: new Date('2024-09-01'),
      status: 'Em análise'
    }
  ]
})

const diagnosticosGapsFiltrados = computed(() => {
  let diagnosticos = diagnosticosGaps.value

  if (filtroTipo.value && filtroTipo.value !== TipoProcesso.DIAGNOSTICO) {
    // Se filtro não for diagnóstico, mostrar apenas diagnósticos relacionados ao tipo
    return []
  }

  if (filtroDataInicio.value) {
    const dataInicio = new Date(filtroDataInicio.value)
    diagnosticos = diagnosticos.filter(d => d.data >= dataInicio)
  }

  if (filtroDataFim.value) {
    const dataFim = new Date(filtroDataFim.value)
    diagnosticos = diagnosticos.filter(d => d.data <= dataFim)
  }

  return diagnosticos
})

const getClasseStatus = (status: string) => {
  switch (status) {
    case 'Finalizado':
      return 'badge bg-success'
    case 'Em análise':
      return 'badge bg-warning text-dark'
    case 'Pendente':
      return 'badge bg-danger'
    default:
      return 'badge bg-secondary'
  }
}

const formatarData = (data: Date) => {
  return formatDateBR(data)
}

const calcularPercentualConcluido = () => {
  // A lógica de percentual concluído precisa ser reavaliada com os novos DTOs.
  // Por enquanto, retornaremos um valor fixo ou uma lógica simplificada.
  return 0;
}

const abrirModalMapasVigentes = () => {
  mostrarModalMapasVigentes.value = true;
}
const abrirModalDiagnosticosGaps = () => {
  mostrarModalDiagnosticosGaps.value = true;
}
const abrirModalAndamentoGeral = () => {
  mostrarModalAndamentoGeral.value = true;
}


const exportarMapasVigentes = () => {
  const dados = mapasVigentes.value.map(mapa => ({
    Unidade: mapa.unidade,
    Competencias: mapa.competencias?.length || 0
  }))

  const csv = gerarCSV(dados)
  downloadCSV(csv, 'mapas-vigentes.csv')
}

const exportarDiagnosticosGaps = () => {
  const dados = diagnosticosGapsFiltrados.value.map(diag => ({
    Processo: diag.processo,
    Unidade: diag.unidade,
    'Gaps Identificados': diag.gaps,
    'Importancia Media': diag.importanciaMedia,
    'Dominio Medio': diag.dominioMedio,
    'Competencias Criticas': diag.competenciasCriticas.join('; '),
    Status: diag.status,
    'Data Diagnostico': formatarData(diag.data)
  }))

  const csv = gerarCSV(dados)
  downloadCSV(csv, 'diagnosticos-gaps.csv')
}

const exportarAndamentoGeral = () => {
  const dados = processosFiltrados.value.map(processo => ({
    Descricao: processo.descricao,
    Tipo: processo.tipo,
    Situacao: processo.situacao,
    'Data Limite': formatarData(new Date(processo.dataLimite)),
    'Unidade': processo.unidadeNome,
    '% Concluido': calcularPercentualConcluido()
  }))

  const csv = gerarCSV(dados)
  downloadCSV(csv, 'andamento-geral.csv')
}

const gerarCSV = (dados: CSVData[]) => {
  if (dados.length === 0) return ''

  const headers = Object.keys(dados[0])
  const linhas = dados.map(item =>
      headers.map(header => `"${item[header]}"`).join(',')
  )

  return [headers.join(','), ...linhas].join('\n')
}

const downloadCSV = (csv: string, nomeArquivo: string) => {
  const blob = new Blob([csv], {type: 'text/csv;charset=utf-8;'})
  const link = document.createElement('a')

  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', nomeArquivo)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}
</script>

<style scoped>
.card:hover {
  background-color: var(--bs-light);
  border-color: var(--bs-primary);
}
</style>
