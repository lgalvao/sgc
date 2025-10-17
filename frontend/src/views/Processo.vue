<template>
  <div class="container mt-4">
    <div v-if="processo">
      <span
        class="badge bg-secondary mb-2"
        style="border-radius: 0"
      >Detalhes do processo</span>
      <h2
        class="display-6"
        data-testid="processo-info"
      >
        {{ processo.descricao }}
      </h2>
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
      <div
        v-if="mostrarBotoesBloco"
        class="mt-3 d-flex gap-2"
      >
        <button
          v-if="perfilStore.perfilSelecionado === 'GESTOR'"
          class="btn btn-outline-primary"
          data-testid="btn-aceitar-em-bloco"
          @click="abrirModalAceitarBloco"
        >
          <i class="bi bi-check-circle me-1" />
          Aceitar em bloco
        </button>
        <button
          v-if="perfilStore.perfilSelecionado === 'ADMIN'"
          class="btn btn-outline-success"
          data-testid="btn-abrir-modal-homologar-bloco"
          @click="abrirModalHomologarBloco"
        >
          <i class="bi bi-check-all me-1" />
          Homologar em bloco
        </button>
      </div>
    </div>
    <button
      v-if="perfilStore.perfilSelecionado === 'ADMIN' && processo?.situacao === SituacaoProcesso.EM_ANDAMENTO"
      class="btn btn-danger mt-3"
      data-testid="btn-finalizar-processo"
      @click="finalizarProcesso"
    >
      Finalizar processo
    </button>

    <!-- Modal de confirmação -->
    <div
      v-if="mostrarModalBloco"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i :class="tipoAcaoBloco === 'aceitar' ? 'bi bi-check-circle text-primary' : 'bi bi-check-all text-success'" />
              {{ tipoAcaoBloco === 'aceitar' ? 'Aceitar cadastros em bloco' : 'Homologar cadastros em bloco' }}
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalBloco"
            />
          </div>
          <div class="modal-body">
            <div class="alert alert-info">
              <i class="bi bi-info-circle" />
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
                  <tr
                    v-for="unidade in unidadesSelecionadasBloco"
                    :key="unidade.sigla"
                  >
                    <td>
                      <input
                        :id="'chk-' + unidade.sigla"
                        v-model="unidade.selecionada"
                        type="checkbox"
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
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalBloco"
            >
              <i class="bi bi-x-circle" /> Cancelar
            </button>
            <button
              type="button"
              class="btn"
              :class="tipoAcaoBloco === 'aceitar' ? 'btn-primary' : 'btn-success'"
              @click="confirmarAcaoBloco"
            >
              <i :class="tipoAcaoBloco === 'aceitar' ? 'bi bi-check-circle' : 'bi bi-check-all'" />
              {{ tipoAcaoBloco === 'aceitar' ? 'Aceitar' : 'Homologar' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="mostrarModalBloco"
      class="modal-backdrop fade show"
    />

    <!-- Modal de finalização do processo CDU-21 -->
    <div
      v-if="mostrarModalFinalizacao"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-check-circle text-success" />
              Finalização de processo
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalFinalizacao"
            />
          </div>
          <div class="modal-body">
            <div class="alert alert-info">
              <i class="bi bi-info-circle" />
              Confirma a finalização do processo <strong>{{ processo?.descricao }}</strong>?<br>
              Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades
              participantes do processo.
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-testid="btn-cancelar-finalizacao"
              @click="fecharModalFinalizacao"
            >
              <i class="bi bi-x-circle" /> Cancelar
            </button>
            <button
              type="button"
              class="btn btn-success"
              data-testid="btn-confirmar-finalizacao"
              @click="confirmarFinalizacao"
            >
              <i class="bi bi-check-circle" />
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="mostrarModalFinalizacao"
      class="modal-backdrop fade show"
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
import {computed, ref, watch, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useProcessosStore} from '@/stores/processos'
import {useUnidadesStore} from '@/stores/unidades'
import {usePerfilStore} from '@/stores/perfil'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {useAlertasStore} from '@/stores/alertas'
import {useMapasStore} from '@/stores/mapas'
import {EMAIL_TEMPLATES} from '@/constants'

import TreeTable from '@/components/TreeTable.vue'
import { ProcessoDetalhe, UnidadeParticipante, SituacaoSubprocesso, ProcessoResumo } from '../mappers/processos'
import { Unidade } from '../mappers/sgrh'
import {ensureValidDate} from '@/utils'
import * as processoService from '@/services/processoService';

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
const unidadesStore = useUnidadesStore()
const perfilStore = usePerfilStore()
const notificacoesStore = useNotificacoesStore()
const alertasStore = useAlertasStore()
const mapasStore = useMapasStore()

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

// Dados reativos para modal de finalização CDU-21
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

// Computed para identificar subprocessos elegíveis
const subprocessosElegiveis = computed(() => {
  if (!idProcesso.value || !processo.value) return []

  // TODO: Implementar lógica de filtro com base em UnidadeParticipante
  return [];
})

function filtrarHierarquiaPorParticipantes(unidades: UnidadeParticipante[], participantes: UnidadeParticipante[]): UnidadeParticipante[] {
  return unidades
      .map((unidade): UnidadeParticipante | null => {
        let filhasFiltradas: UnidadeParticipante[] = []
        if (unidade.filhos && unidade.filhos.length) {
          filhasFiltradas = filtrarHierarquiaPorParticipantes(unidade.filhos, participantes)
        }
        const isParticipante = participantes.some(p => p.codUnidade === unidade.codUnidade)
        if (isParticipante || filhasFiltradas.length > 0) {
          return {
            ...unidade,
            filhos: filhasFiltradas
          }
        }
        return null
      })
      .filter((u): u is UnidadeParticipante => u !== null)
}

const participantesHierarquia = computed<UnidadeParticipante[]>(() => {
  // TODO: Ajustar para usar a hierarquia de unidades do processoDetalhe
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
      router.push({name: 'Subprocesso', params: {idProcesso: idProcesso.value, siglaUnidade: item.sigla}})
    } else if (perfilUsuario === 'CHEFE' || perfilUsuario === 'SERVIDOR') {
      if (perfilStore.unidadeSelecionada === item.sigla) {
        router.push({name: 'Subprocesso', params: {idProcesso: idProcesso.value, siglaUnidade: item.sigla}})
      }
    }
  }
}

async function finalizarProcesso() {
  if (!processo.value) return;

  const processoAtual = processo.value;

  // Verificar se todos os subprocessos de unidades operacionais/interoperacionais estão em 'MAPA_HOMOLOGADO'
  const unidadesOperacionais = processoAtual.unidades.filter(up => {
    // TODO: Obter o tipo da unidade (OPERACIONAL/INTEROPERACIONAL) a partir do codUnidade
    // Por enquanto, vamos considerar todas as unidades como operacionais/interoperacionais para a validação
    return true; 
  });

  const todosHomologados = unidadesOperacionais.every(up => up.situacaoSubprocesso === 'MAPA_HOMOLOGADO');

  if (!todosHomologados) {
    notificacoesStore.erro(
        'Não é possível encerrar o processo',
        'Não é possível encerrar o processo enquanto houver unidades com mapa de competência ainda não homologado.'
    );
    return;
  }

  // Mostrar modal de confirmação
  abrirModalFinalizacao();
}

async function executarFinalizacao() {
  if (!processo.value) return;

  const processoAtual = processo.value;

  try {
    await processosStore.finalizarProcesso(processoAtual.codigo);

    // TODO: Definir mapas vigentes para as unidades participantes (se necessário, o backend deve cuidar disso)
    // TODO: Enviar notificações por e-mail (o backend deve cuidar disso)
    // TODO: Criar alertas para todas as unidades participantes (o backend deve cuidar disso)

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

// Funções auxiliares para agrupar unidades por superior hierárquica (não mais necessárias aqui, backend deve cuidar)
// function agruparUnidadesPorSuperior(subprocessos: Subprocesso[]): Map<string, string[]> {
//   return new Map();
// }

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
    // TODO: Ajustar para a nova estrutura de UnidadeParticipante
    return {
      sigla: '',
      nome: '',
      situacao: '',
      selecionada: true // Por padrão, todas selecionadas
    }
  })
}

function fecharModalBloco() {
  mostrarModalBloco.value = false
}

// Funções para modal de finalização CDU-21
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

    // TODO: Chamar o serviço de backend para processar ações em bloco
    // await processosStore.processarCadastroBloco({
    //   idProcesso: idProcesso.value,
    //   unidades: unidadesSelecionadas,
    //   tipoAcao: tipoAcaoBloco.value,
    //   unidadeUsuario: perfilStore.unidadeSelecionada || ''
    // })

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
}, {immediate: true});