<template>
  <div class="container mt-4">
    <div v-if="processo">
      <span class="badge text-bg-secondary mb-2" style="border-radius: 0">Processo</span>
      <h2 class="display-6">{{ processo.descricao }}</h2>
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
    <button v-if="perfilStore.perfilSelecionado === 'ADMIN'" class="btn btn-danger mt-3" @click="finalizarProcesso">
      Finalizar processo
    </button>
  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useProcessosStore} from '@/stores/processos'
import {useUnidadesStore} from '@/stores/unidades'
import {usePerfilStore} from '@/stores/perfil'

import TreeTable from '@/components/TreeTable.vue'
import {Processo, ProcessoUnidade, Unidade} from '@/types/tipos'

interface TreeTableItem {
  id: string;
  nome: string;
  situacao: string;
  dataLimite: string;
  unidadeAtual: string;
  expanded: boolean;
  children: TreeTableItem[];
}

const route = useRoute()
const router = useRouter()
const processosStore = useProcessosStore()
const {processos} = storeToRefs(processosStore)
const unidadesStore = useUnidadesStore()
const perfilStore = usePerfilStore()


const idProcesso = computed(() => Number((route.params as any).idProcesso || (route.params as any).id || route.query.idProcesso))
const processo = computed<Processo | undefined>(() => (processos.value as Processo[]).find(p => p.id === idProcesso.value))
const unidadesParticipantes = computed<string[]>(() => {
  if (!processo.value) return []
  return processosStore.getUnidadesDoProcesso(processo.value.id).map((pu: ProcessoUnidade) => pu.unidade)
})

function filtrarHierarquiaPorParticipantes(unidades: Unidade[], participantes: string[]): Unidade[] {
  return unidades
      .map((unidade): Unidade | null => {
        let filhasFiltradas: Unidade[] = []
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
      .filter((u): u is Unidade => u !== null)
}

const participantesHierarquia = computed<Unidade[]>(() => {
  const sedoc = unidadesStore.pesquisarUnidade('SEDOC');
  const unidadesRaiz = sedoc && sedoc.filhas ? sedoc.filhas : [];
  return filtrarHierarquiaPorParticipantes(unidadesRaiz as Unidade[], unidadesParticipantes.value)
})

const colunasTabela = [
  {key: 'nome', label: 'Unidade', width: '40%'},
  {key: 'situacao', label: 'Situação', width: '20%'},
  {key: 'dataLimite', label: 'Data limite', width: '20%'},
  {key: 'unidadeAtual', label: 'Unidade Atual', width: '20%'}
]

const dadosFormatados = computed<TreeTableItem[]>(() => {
  return formatarDadosParaArvore(participantesHierarquia.value, idProcesso.value)
})

function formatarData(data: Date | null): string {
  if (!data) return ''
  const dia = String(data.getDate()).padStart(2, '0');
  const mes = String(data.getMonth() + 1).padStart(2, '0'); // Mês é 0-indexed
  const ano = data.getFullYear();
  return `${dia}/${mes}/${ano}`
}

function formatarDadosParaArvore(dados: Unidade[], idProcesso: number): TreeTableItem[] {
  if (!dados) return []
  return dados.map(item => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas, idProcesso) : []
    const unidadeOriginal = unidadesStore.pesquisarUnidade(item.sigla);
    const isIntermediaria = unidadeOriginal && unidadeOriginal.tipo === 'INTERMEDIARIA';

    let situacao = 'Não iniciado';
    let dataLimite = 'Não informado';
    let unidadeAtual = 'Não informado';

    if (!isIntermediaria) {
      const processoUnidade = processosStore.getUnidadesDoProcesso(idProcesso).find((pu: ProcessoUnidade) => pu.unidade === item.sigla);
      if (processoUnidade) {
        situacao = processoUnidade.situacao;
        dataLimite = formatarData(processoUnidade.dataLimiteEtapa1);
        unidadeAtual = processoUnidade.unidadeAtual;
      }
    }

    return {
      id: item.sigla,
      nome: item.sigla + ' - ' + item.nome,
      situacao: isIntermediaria ? '' : situacao,
      dataLimite: isIntermediaria ? '' : dataLimite,
      unidadeAtual: isIntermediaria ? '' : unidadeAtual,
      clickable: !isIntermediaria,
      expanded: true,
      children: children,
    }
  })
}

function abrirDetalhesUnidade(item: any) {
  // The @row-click event from TreeTable emits a generic object.
  // We use `any` and perform runtime checks to safely handle the event payload.
  if (item && typeof item.id === 'string') {
    const processoUnidade = processosStore.getUnidadesDoProcesso(idProcesso.value).find((pu: ProcessoUnidade) => pu.unidade === item.id);
    if (processoUnidade && processoUnidade.unidade) {
      // É uma unidade participante direta: abre a visão padrão da unidade no processo
      router.push({name: 'ProcessoUnidade', params: {idProcesso: idProcesso.value, siglaUnidade: processoUnidade.unidade}})
    } else if (Array.isArray(item.children) && item.children.length > 0) {
      // Unidade INTERMEDIARIA: não navegar mais (link desabilitado)
      console.log(`Unidade intermediária ${item.id}: navegação desabilitada.`)
    } else {
      // Otherwise, do nothing (non-participating leaf unit)
      console.log(`Unidade ${item.id} não é participante e não possui subordinadas participantes.`);
    }
  }
}

function finalizarProcesso() {
  if (confirm('Tem certeza que deseja finalizar este processo?')) {
    if (processo.value) {
      processosStore.finalizarProcesso(processo.value.id);
      router.push('/painel');
    }
  }
}

</script>