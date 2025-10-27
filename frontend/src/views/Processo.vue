<template>
  <div class="container mt-4">
    <div v-if="processo">
      <ProcessoDetalhes
        :descricao="processo.descricao"
        :tipo="processo.tipo"
        :situacao="processo.situacao"
      />

      <TreeTable
        :columns="colunasTabela"
        :data="dadosFormatados"
        title="Unidades participantes"
        @row-click="abrirDetalhesUnidade"
      />

      <ProcessoAcoes
        :mostrar-botoes-bloco="mostrarBotoesBloco"
        :perfil="perfilStore.perfilSelecionado"
        :situacao-processo="processo.situacao"
        @aceitar-bloco="abrirModalAceitarBloco"
        @homologar-bloco="abrirModalHomologarBloco"
        @finalizar="finalizarProcesso"
      />
    </div>

    <ModalAcaoBloco
      :mostrar="mostrarModalBloco"
      :tipo="tipoAcaoBloco"
      :unidades="unidadesSelecionadasBloco"
      @fechar="fecharModalBloco"
      @confirmar="confirmarAcaoBloco"
    />

    <ModalFinalizacao
      :mostrar="mostrarModalFinalizacao"
      :processo-descricao="processo?.descricao || ''"
      @fechar="fecharModalFinalizacao"
      @confirmar="confirmarFinalizacao"
    />

    <!-- Alerta de sucesso -->
    <div
      v-if="mostrarAlertaSucesso"
      class="alert alert-success alert-dismissible fade show position-fixed"
      style="top: 20px; right: 20px; z-index: 9999;"
    >
      <i class="bi bi-check-circle" />
      Cadastros {{ tipoAcaoBloco === 'aceitar' ? 'aceitos' : 'homologados' }} em bloco com sucesso!
      <button
        type="button"
        class="btn-close"
        @click="mostrarAlertaSucesso = false"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useProcessosStore} from '@/stores/processos'
import {usePerfilStore} from '@/stores/perfil'
import {useNotificacoesStore} from '@/stores/notificacoes'

import TreeTable from '@/components/TreeTable.vue'
import ProcessoDetalhes from '@/components/ProcessoDetalhes.vue'
import ProcessoAcoes from '@/components/ProcessoAcoes.vue'
import ModalAcaoBloco, { type UnidadeSelecao } from '@/components/ModalAcaoBloco.vue'
import ModalFinalizacao from '@/components/ModalFinalizacao.vue'

import {ProcessoDetalhe, UnidadeParticipante} from '@/types/tipos'

interface TreeTableItem {
  id: number | string;
  nome: string;
  situacao: string;
  dataLimite: string;
  unidadeAtual: string;
  expanded: boolean;
  children: TreeTableItem[];
  clickable?: boolean;

  [key: string]: any;
}

const route = useRoute()
const router = useRouter()
const processosStore = useProcessosStore()
const {processoDetalhe} = storeToRefs(processosStore)
const perfilStore = usePerfilStore()
const notificacoesStore = useNotificacoesStore()

const mostrarBotoesBloco = ref(false)
const mostrarModalBloco = ref(false)
const tipoAcaoBloco = ref<'aceitar' | 'homologar'>('aceitar')
const unidadesSelecionadasBloco = ref<UnidadeSelecao[]>([])

const mostrarAlertaSucesso = ref(false)
const mostrarModalFinalizacao = ref(false)

const idProcesso = computed(() =>
    Number(route.params.idProcesso || route.params.id || route.query.idProcesso))

// Carregar detalhes do processo ao montar o componente
onMounted(async () => {
  if (idProcesso.value) {
    await processosStore.fetchProcessoDetalhe(idProcesso.value);
  }
});

const processo = computed<ProcessoDetalhe | undefined>(() => processoDetalhe.value || undefined)

const unidadesParticipantes = computed<UnidadeParticipante[]>(() => {
  return processo.value?.unidades || [];
})

const subprocessosElegiveis = computed(() => {
  if (!idProcesso.value || !processo.value) return []

  if (perfilStore.perfilSelecionado === 'GESTOR') {
    return processosStore.getSubprocessosElegiveisAceiteBloco(idProcesso.value, String(perfilStore.unidadeSelecionada) || '');
  } else if (perfilStore.perfilSelecionado === 'ADMIN') {
    return processosStore.getSubprocessosElegiveisHomologacaoBloco(idProcesso.value);
  }
  return [];
})


const participantesHierarquia = computed<UnidadeParticipante[]>(() => {
  // A propriedade 'unidades' de processoDetalhe já vem hierarquizada, então unidadesParticipantes.value já contém a hierarquia.
  return unidadesParticipantes.value;
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

function formatarData(data: string | null): string {
  if (!data) return '';
  const date = new Date(data);
  const dia = String(date.getDate()).padStart(2, '0');
  const mes = String(date.getMonth() + 1).padStart(2, '0'); // Mês é 0-indexed
  const ano = date.getFullYear();
  return `${dia}/${mes}/${ano}`
}

function formatarDadosParaArvore(dados: UnidadeParticipante[], idProcesso: number): TreeTableItem[] {
  if (!dados) return []
  return dados.map(item => {
    const children = item.filhos ? formatarDadosParaArvore(item.filhos, idProcesso) : []

    let situacao = item.situacaoSubprocesso || 'Não iniciado';
    let dataLimite = formatarData(item.dataLimite || null);
    let unidadeAtual = item.sigla; // A unidade atual é a própria sigla da unidade participante

    return {
      id: item.codUnidade,
      nome: item.sigla + ' - ' + item.nome,
      situacao: situacao,
      dataLimite: dataLimite,
      unidadeAtual: unidadeAtual,
      clickable: true,
      expanded: true,
      children: children,
    }
  })
}

function abrirDetalhesUnidade(item: any) {
  if (item && item.clickable) {
    const perfilUsuario = perfilStore.perfilSelecionado;
    if (perfilUsuario === 'ADMIN' || perfilUsuario === 'GESTOR') {
      router.push({name: 'Subprocesso', params: {idProcesso: idProcesso.value.toString(), siglaUnidade: String(item.sigla)}})
    } else if (perfilUsuario === 'CHEFE' || perfilUsuario === 'SERVIDOR') {
      if (perfilStore.unidadeSelecionada === item.sigla) {
        router.push({name: 'Subprocesso', params: {idProcesso: String(idProcesso.value), siglaUnidade: String(item.sigla)}})
      }
    }
  }
}

async function finalizarProcesso() {
  if (!processo.value) return;
  abrirModalFinalizacao();
}

async function executarFinalizacao() {
  if (!processo.value) return;

  const processoAtual = processo.value;

  try {
    await processosStore.finalizarProcesso(processoAtual.codigo);

    notificacoesStore.sucesso(
        'Processo finalizado',
        'O processo foi finalizado com sucesso. Todos os mapas de competências estão agora vigentes.'
    );

    await router.push('/painel');

  } catch (error) {
    console.error('Erro ao finalizar processo:', error);
    notificacoesStore.erro(
        'Erro ao finalizar processo',
        'Ocorreu um erro durante a finalização. Tente novamente.'
    );
  }
}

function abrirModalAceitarBloco() {
  tipoAcaoBloco.value = 'aceitar'
  prepararUnidadesParaBloco()
  mostrarModalBloco.value = true
}

function abrirModalHomologarBloco() {
  tipoAcaoBloco.value = 'homologar'
  prepararUnidadesParaBloco()
  mostrarModalBloco.value = true
}

function prepararUnidadesParaBloco() {
  unidadesSelecionadasBloco.value = subprocessosElegiveis.value.map(pu => {
    return {
      sigla: pu.unidadeNome,
      nome: pu.unidadeNome,
      situacao: pu.situacao || 'Não iniciado',
      selecionada: true // Por padrão, todas selecionadas
    }
  })
}

function fecharModalBloco() {
  mostrarModalBloco.value = false
}

function abrirModalFinalizacao() {
  mostrarModalFinalizacao.value = true
}

function fecharModalFinalizacao() {
  mostrarModalFinalizacao.value = false
}

function confirmarFinalizacao() {
  fecharModalFinalizacao()
  executarFinalizacao()
}

async function confirmarAcaoBloco(unidades: UnidadeSelecao[]) {
  try {
    // Filtrar apenas unidades selecionadas
    const unidadesSelecionadas = unidades
        .filter(u => u.selecionada)
        .map(u => u.sigla);

    if (unidadesSelecionadas.length === 0) {
      alert('Selecione ao menos uma unidade para processar.');
      return;
    }

    await processosStore.processarCadastroBloco({
      idProcesso: idProcesso.value,
      unidades: unidadesSelecionadas,
      tipoAcao: tipoAcaoBloco.value,
      unidadeUsuario: String(perfilStore.unidadeSelecionada) || ''
    })

    mostrarAlertaSucesso.value = true
    fecharModalBloco()

    // Forçar atualização dos dados
    setTimeout(() => {
      mostrarAlertaSucesso.value = false
      // Redirecionar para o painel
      router.push('/painel')
    }, 2000)

  } catch (error) {
    console.error('Erro ao processar cadastro em bloco:', error)
    alert('Ocorreu um erro ao processar os cadastros em bloco.')
  }
}

// Watch para mostrar/esconder botões
watch(subprocessosElegiveis, (novosSubprocessos) => {
  mostrarBotoesBloco.value = novosSubprocessos.length > 0
}, {immediate: true});
</script>
