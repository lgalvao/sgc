<template>
  <div class="container mt-4">
    <div v-if="processo">
      <span class="badge text-bg-secondary mb-2" style="border-radius: 0">Processo</span>
      <h2>{{ processo.descricao }}</h2>
      <div class="mb-4 mt-3">
        <strong>Tipo:</strong> {{ processo.tipo }}<br>
        <strong>Situação:</strong> {{ processo.situacao }}<br>
      </div>
      <TreeTable
          :data="dadosFormatados"
          :columns="colunasTabela"
          title="Unidades participantes"
          @row-click="abrirDetalhesUnidade"
      />
    </div>
    <button v-if="perfilStore.perfilSelecionado === 'ADMIN'" class="btn btn-danger mt-3" @click="finalizarProcesso">Finalizar processo</button>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useProcessosStore} from '../stores/processos'
import {useUnidadesStore} from '../stores/unidades'
import {usePerfilStore} from '../stores/perfil'
import {useProcessoUnidadesStore} from '../stores/processoUnidades'
import TreeTable from '../components/TreeTable.vue'

const route = useRoute()
const router = useRouter()
const processosStore = useProcessosStore()
const {processos} = storeToRefs(processosStore)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const perfilStore = usePerfilStore()
const processoUnidadesStore = useProcessoUnidadesStore()

const processoId = computed(() => Number(route.params.id))
const processo = computed(() => processos.value.find(p => p.id === processoId.value))
const unidadesParticipantes = computed(() => {
  if (!processo.value || !processo.value.unidades) return []
  return processo.value.unidades.split(',').map(u => u.trim())
})

function filtrarHierarquiaPorParticipantes(unidades, participantes) {
  return unidades
      .map(unidade => {
        let filhasFiltradas = []
        if (unidade.filhas && unidade.filhas.length) {
          filhasFiltradas = filtrarHierarquiaPorParticipantes(unidade.filhas, participantes)
        }
        const isParticipante = participantes.includes(unidade.sigla)
        if (isParticipante || filhasFiltradas.length > 0) {
          console.log("Unidade filtrada:", unidade.sigla);
          return {
            ...unidade,
            filhas: filhasFiltradas
          }
        }
        return null
      })
      .filter(Boolean)
}

const participantesHierarquia = computed(() => filtrarHierarquiaPorParticipantes(unidades.value, unidadesParticipantes.value))

const colunasTabela = [
  {key: 'nome', label: 'Unidade', width: '60%'},
  {key: 'situacao', label: 'Situação', width: '20%'},
  {key: 'dataLimite', label: 'Data limite', width: '20%'}
]

const dadosFormatados = computed(() => {
  return formatarDadosParaArvore(participantesHierarquia.value, processoId.value)
})

function formatarDadosParaArvore(dados, processoId) {
  if (!dados) return []
  return dados.map(item => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas, processoId) : []
    const situacaoUnidade = processoUnidadesStore.getSituacaoUnidadeNoProcesso(processoId, item.sigla) || 'Não iniciado';
    const dataLimiteUnidade = processoUnidadesStore.getDataLimiteUnidadeNoProcesso(processoId, item.sigla) || 'Não informado';

    return {
      id: item.sigla,
      nome: item.sigla + ' - ' + item.nome,
      situacao: situacaoUnidade,
      dataLimite: dataLimiteUnidade,
      expanded: true,
      children: children,
      // Garante que a estrutura de filhos seja consistente
      ...(children.length > 0 && {children})
    }
  })
}

function abrirDetalhesUnidade(item) {
  router.push({path: `/unidade-processo/${item.id}`, query: {processoId: processoId.value}})
}

function finalizarProcesso() {
  if (confirm('Tem certeza que deseja finalizar este processo?')) {
    processosStore.finalizarProcesso(processoId.value);
    router.push('/painel');
  }
}

</script>