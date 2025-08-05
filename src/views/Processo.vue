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

import TreeTable from '../components/TreeTable.vue'

const route = useRoute()
const router = useRouter()
const processosStore = useProcessosStore()
const {processos} = storeToRefs(processosStore)
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const perfilStore = usePerfilStore()


const processoId = computed(() => Number(route.params.id))
const processo = computed(() => processos.value.find(p => p.id === processoId.value))
const unidadesParticipantes = computed(() => {
  if (!processo.value) return []
  return processosStore.getUnidadesDoProcesso(processo.value.id).map(pu => pu.unidadeId)
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
  {key: 'nome', label: 'Unidade', width: '40%'},
  {key: 'situacao', label: 'Situação', width: '15%'},
  {key: 'dataLimite', label: 'Data limite', width: '15%'},
  {key: 'unidadeAtual', label: 'Unidade Atual', width: '15%'},
  {key: 'unidadeAnterior', label: 'Unidade Anterior', width: '15%'}
]

const dadosFormatados = computed(() => {
  return formatarDadosParaArvore(participantesHierarquia.value, processoId.value)
})

function formatarDadosParaArvore(dados, processoId) {
  if (!dados) return []
  return dados.map(item => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas, processoId) : []
    const processoUnidade = processosStore.getUnidadesDoProcesso(processoId).find(pu => pu.unidadeId === item.sigla);

    return {
      id: item.sigla,
      nome: item.sigla + ' - ' + item.nome,
      situacao: processoUnidade ? processoUnidade.situacao : 'Não iniciado', // Usando a situação do ProcessoUnidade
      dataLimite: processoUnidade ? processoUnidade.dataLimite : 'Não informado',
      unidadeAtual: processoUnidade ? processoUnidade.unidadeAtual : 'Não informado',
      unidadeAnterior: processoUnidade ? processoUnidade.unidadeAnterior || 'N/A' : 'Não informado',
      expanded: true,
      children: children,
      // Garante que a estrutura de filhos seja consistente
      ...(children.length > 0 && {children})
    }
  })
}

function abrirDetalhesUnidade(item) {
  const processoUnidade = processosStore.getUnidadesDoProcesso(processoId.value).find(pu => pu.unidadeId === item.id);
  if (processoUnidade) {
    // Se a unidade clicada é uma ProcessoUnidade direta, navega para ProcessoUnidade.vue
    router.push({path: `/processo-unidade/${processoUnidade.id}`, query: {processoId: processoId.value}})
  } else if (item.children && item.children.length > 0) {
    // Se a unidade clicada não é uma ProcessoUnidade direta, mas tem filhos, navega para ProcessosSubordinadas.vue
    router.push({path: `/processos/${processoId.value}/unidades/${item.id}`})
  } else {
    // Caso contrário, não faz nada (unidade folha não participante)
    console.log(`Unidade ${item.id} não é participante e não possui subordinadas participantes.`);
  }
}

function finalizarProcesso() {
  if (confirm('Tem certeza que deseja finalizar este processo?')) {
    processosStore.finalizarProcesso(processoId.value);
    router.push('/painel');
  }
}

</script>