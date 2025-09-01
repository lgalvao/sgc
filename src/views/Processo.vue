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
          :columns="colunasTabela"
          :data="dadosFormatados"
          title="Unidades participantes"
          @row-click="abrirDetalhesUnidade"
      />

      <!-- Botões de ação em bloco -->
      <div v-if="mostrarBotoesBloco" class="mt-3 d-flex gap-2">
        <button
            v-if="perfilStore.perfilSelecionado === 'GESTOR'"
            class="btn btn-outline-primary"
            @click="abrirModalAceitarBloco"
        >
          <i class="bi bi-check-circle me-1"></i>
          Aceitar em bloco
        </button>
        <button
            v-if="perfilStore.perfilSelecionado === 'ADMIN'"
            class="btn btn-outline-success"
            @click="abrirModalHomologarBloco"
        >
          <i class="bi bi-check-all me-1"></i>
          Homologar em bloco
        </button>
      </div>
    </div>
    <button v-if="perfilStore.perfilSelecionado === 'ADMIN'" class="btn btn-danger mt-3" @click="finalizarProcesso">
      Finalizar processo
    </button>

    <!-- Modal de confirmação -->
    <div v-if="mostrarModalBloco" class="modal fade show" style="display: block;" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i :class="tipoAcaoBloco === 'aceitar' ? 'bi bi-check-circle text-primary' : 'bi bi-check-all text-success'"></i>
              {{ tipoAcaoBloco === 'aceitar' ? 'Aceitar cadastros em bloco' : 'Homologar cadastros em bloco' }}
            </h5>
            <button type="button" class="btn-close" @click="fecharModalBloco"></button>
          </div>
          <div class="modal-body">
            <div class="alert alert-info">
              <i class="bi bi-info-circle"></i>
              Selecione as unidades que terão seus cadastros {{
                tipoAcaoBloco === 'aceitar' ? 'aceitos' : 'homologados'
              }}:
            </div>

            <div class="table-responsive">
              <table class="table table-bordered">
                <thead class="table-light">
                <tr>
                  <th>Selecionar</th>
                  <th>Sigla</th>
                  <th>Nome</th>
                  <th>Situação Atual</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="unidade in unidadesSelecionadasBloco" :key="unidade.sigla">
                  <td>
                    <input
                        type="checkbox"
                        :id="'chk-' + unidade.sigla"
                        v-model="unidade.selecionada"
                        class="form-check-input"
                    >
                  </td>
                  <td><strong>{{ unidade.sigla }}</strong></td>
                  <td>{{ unidade.nome }}</td>
                  <td>{{ unidade.situacao }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="fecharModalBloco">
              <i class="bi bi-x-circle"></i> Cancelar
            </button>
            <button
                type="button"
                class="btn"
                :class="tipoAcaoBloco === 'aceitar' ? 'btn-primary' : 'btn-success'"
                @click="confirmarAcaoBloco"
            >
              <i :class="tipoAcaoBloco === 'aceitar' ? 'bi bi-check-circle' : 'bi bi-check-all'"></i>
              {{ tipoAcaoBloco === 'aceitar' ? 'Aceitar' : 'Homologar' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="mostrarModalBloco" class="modal-backdrop fade show"></div>

    <!-- Alerta de sucesso -->
    <div v-if="mostrarAlertaSucesso" class="alert alert-success alert-dismissible fade show position-fixed"
         style="top: 20px; right: 20px; z-index: 9999;">
      <i class="bi bi-check-circle"></i>
      Cadastros {{ tipoAcaoBloco === 'aceitar' ? 'aceitos' : 'homologados' }} em bloco com sucesso!
      <button type="button" class="btn-close" @click="mostrarAlertaSucesso = false"></button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useProcessosStore} from '@/stores/processos'
import {useUnidadesStore} from '@/stores/unidades'
import {usePerfilStore} from '@/stores/perfil'

import TreeTable from '@/components/TreeTable.vue'
import {Processo, Subprocesso, Unidade} from '@/types/tipos'

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

// Dados reativos para o CDU-14
const mostrarBotoesBloco = ref(false)
const mostrarModalBloco = ref(false)
const tipoAcaoBloco = ref<'aceitar' | 'homologar'>('aceitar')
const unidadesSelecionadasBloco = ref<Array<{
  sigla: string,
  nome: string,
  situacao: string,
  selecionada: boolean
}>>([])
const mostrarAlertaSucesso = ref(false)

const idProcesso = computed(() =>
    Number(route.params.idProcesso || route.params.id || route.query.idProcesso))

const processo = computed<Processo | undefined>(() => (processos.value as Processo[]).find(p => p.id === idProcesso.value))

const unidadesParticipantes = computed<string[]>(() => {
  if (!processo.value) return []
  return processosStore.getUnidadesDoProcesso(processo.value.id).map((pu: Subprocesso) => pu.unidade)
})

// Computed para identificar subprocessos elegíveis
const subprocessosElegiveis = computed(() => {
  if (!idProcesso.value) return []

  if (perfilStore.perfilSelecionado === 'GESTOR' && perfilStore.unidadeSelecionada) {
    return processosStore.getSubprocessosElegiveisAceiteBloco(
        idProcesso.value,
        perfilStore.unidadeSelecionada
    )
  } else if (perfilStore.perfilSelecionado === 'ADMIN') {
    return processosStore.getSubprocessosElegiveisHomologacaoBloco(idProcesso.value)
  }
  return []
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
      const Subprocesso = processosStore.getUnidadesDoProcesso(idProcesso).find((pu: Subprocesso) => pu.unidade === item.sigla);
      if (Subprocesso) {
        situacao = Subprocesso.situacao;
        dataLimite = formatarData(Subprocesso.dataLimiteEtapa1);
        unidadeAtual = Subprocesso.unidadeAtual;
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

function abrirDetalhesUnidade(item: TreeTableItem) {
  if (item && true) {
    const Subprocesso = processosStore.getUnidadesDoProcesso(idProcesso.value).find((pu: Subprocesso) => pu.unidade === item.id);
    if (Subprocesso && Subprocesso.unidade) {
      // É uma unidade participante direta: abre a visão padrão da unidade no processo
      router.push({name: 'Subprocesso', params: {idProcesso: idProcesso.value, siglaUnidade: Subprocesso.unidade}})
    } else if (Array.isArray(item.children) && item.children.length > 0) {
    } else {
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

// Funções para controle do modal (CDU-14)
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
    // Obter nome da unidade
    const unidade = unidadesStore.pesquisarUnidade(pu.unidade)
    return {
      sigla: pu.unidade,
      nome: unidade ? unidade.nome : pu.unidade,
      situacao: pu.situacao,
      selecionada: true // Por padrão, todas selecionadas
    }
  })
}

function fecharModalBloco() {
  mostrarModalBloco.value = false
}

async function confirmarAcaoBloco() {
  try {
    // Filtrar apenas unidades selecionadas
    const unidadesSelecionadas = unidadesSelecionadasBloco.value
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
      unidadeUsuario: perfilStore.unidadeSelecionada || ''
    })

    // Mostrar mensagem de sucesso
    mostrarAlertaSucesso.value = true

    // Fechar modal
    fecharModalBloco()

    // Recarregar dados
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
}, {immediate: true})

</script>