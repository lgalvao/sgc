<template>
  <div class="container mt-4">
    <div v-if="processo">
      <span class="badge text-bg-secondary mb-2" style="border-radius: 0">Processo</span>
      <h1 class="display-6 mb-3">Detalhes do processo</h1>
      <h2 class="display-6" data-testid="processo-info">{{ processo.descricao }}</h2>
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
    <button v-if="perfilStore.perfilSelecionado === 'ADMIN' && processo?.situacao === 'Em andamento'" class="btn btn-danger mt-3" @click="finalizarProcesso">
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

    <!-- Modal de finalização do processo CDU-21 -->
    <div v-if="mostrarModalFinalizacao" class="modal fade show" style="display: block;" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-check-circle text-success"></i>
              Finalização de processo
            </h5>
            <button type="button" class="btn-close" @click="fecharModalFinalizacao"></button>
          </div>
          <div class="modal-body">
            <div class="alert alert-info">
              <i class="bi bi-info-circle"></i>
              Confirma a finalização do processo <strong>{{ processo?.descricao }}</strong>?<br>
              Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades participantes do processo.
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="fecharModalFinalizacao" data-testid="btn-cancelar-finalizacao">
              <i class="bi bi-x-circle"></i> Cancelar
            </button>
            <button
                type="button"
                class="btn btn-success"
                @click="confirmarFinalizacao"
                data-testid="btn-confirmar-finalizacao"
            >
              <i class="bi bi-check-circle"></i>
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="mostrarModalFinalizacao" class="modal-backdrop fade show"></div>

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
import {useNotificacoesStore} from '@/stores/notificacoes'
import {useAlertasStore} from '@/stores/alertas'
import {useMapasStore} from '@/stores/mapas'
import {EMAIL_TEMPLATES} from '@/constants'

import TreeTable from '@/components/TreeTable.vue'
import {Processo, Subprocesso, Unidade} from '@/types/tipos'

interface TreeTableItem {
  id: number | string;
  nome: string;
  situacao: string;
  dataLimite: string;
  unidadeAtual: string;
  expanded: boolean;
  children: TreeTableItem[];
  clickable?: boolean;
  [key: string]: any; // Para compatibilidade com TreeItem
}

const route = useRoute()
const router = useRouter()
const processosStore = useProcessosStore()
const {processos} = storeToRefs(processosStore)
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
  if (item) {
    const Subprocesso = processosStore.getUnidadesDoProcesso(idProcesso.value).find((pu: Subprocesso) => pu.unidade === item.id);
    if (Subprocesso && Subprocesso.unidade) {
      // É uma unidade participante direta: abre a visão padrão da unidade no processo
      router.push({name: 'Subprocesso', params: {idProcesso: idProcesso.value, siglaUnidade: Subprocesso.unidade}})
    } else if (Array.isArray(item.children) && item.children.length > 0) {
    } else {
    }
  }
}

async function finalizarProcesso() {
  if (!processo.value) return;

  const processoAtual = processo.value;

  // Verificar se todos os subprocessos de unidades operacionais/interoperacionais estão em 'Mapa homologado'
  const subprocessos = processosStore.getUnidadesDoProcesso(processoAtual.id);
  const subprocessosOperacionais = subprocessos.filter(pu => {
    const unidade = unidadesStore.pesquisarUnidade(pu.unidade);
    return unidade && (unidade.tipo === 'OPERACIONAL' || unidade.tipo === 'INTEROPERACIONAL');
  });

  const todosHomologados = subprocessosOperacionais.every(pu => pu.situacao === 'Mapa homologado');

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

  // Obter subprocessos novamente (já verificados anteriormente)
  const subprocessos = processosStore.getUnidadesDoProcesso(processoAtual.id);
  const subprocessosOperacionais = subprocessos.filter(pu => {
    const unidade = unidadesStore.pesquisarUnidade(pu.unidade);
    return unidade && (unidade.tipo === 'OPERACIONAL' || unidade.tipo === 'INTEROPERACIONAL');
  });

  try {
    // Alterar situação do processo para 'Finalizado'
    processosStore.finalizarProcesso(processo.value.id);

    // Definir mapas vigentes para as unidades participantes
    subprocessosOperacionais.forEach(pu => {
      mapasStore.definirMapaComoVigente(pu.unidade, processoAtual.id);
    });

    // Enviar notificações por e-mail conforme especificações
    const unidadesAgrupadas = agruparUnidadesPorSuperior(subprocessosOperacionais);

    for (const [unidadeSuperior, unidadesSubordinadas] of unidadesAgrupadas) {
      if (unidadeSuperior === 'SEDOC') {
        // Notificar unidades operacionais/interoperacionais diretamente
        unidadesSubordinadas.forEach(siglaUnidade => {
          notificacoesStore.email(
            EMAIL_TEMPLATES.FINALIZACAO_PROCESSO_OPERACIONAL(processoAtual.descricao, siglaUnidade),
            `Responsável pela ${siglaUnidade}`,
            EMAIL_TEMPLATES.CORPO_EMAIL_OPERACIONAL(processoAtual.descricao, siglaUnidade)
          );
        });
      } else {
        // Notificar unidades superiores com lista de subordinadas
        notificacoesStore.email(
          EMAIL_TEMPLATES.FINALIZACAO_PROCESSO_INTERMEDIARIA(processoAtual.descricao, unidadeSuperior),
          `Responsável pela ${unidadeSuperior}`,
          EMAIL_TEMPLATES.CORPO_EMAIL_INTERMEDIARIA(processoAtual.descricao, unidadeSuperior, unidadesSubordinadas)
        );
      }
    }

    // Criar alertas para todas as unidades participantes
    subprocessos.forEach(pu => {
      alertasStore.criarAlerta({
        idProcesso: processoAtual.id,
        unidadeOrigem: 'SEDOC',
        unidadeDestino: pu.unidade,
        descricao: `Processo ${processoAtual.descricao} finalizado - mapa de competências vigente`,
        dataHora: new Date()
      });
    });

    notificacoesStore.sucesso(
      'Processo finalizado',
      'O processo foi finalizado com sucesso. Todos os mapas de competências estão agora vigentes.'
    );

    router.push('/painel');

  } catch (error) {
    console.error('Erro ao finalizar processo:', error);
    notificacoesStore.erro(
      'Erro ao finalizar processo',
      'Ocorreu um erro durante a finalização. Tente novamente.'
    );
  }
}

// Função auxiliar para agrupar unidades por superior hierárquica
function agruparUnidadesPorSuperior(subprocessos: Subprocesso[]): Map<string, string[]> {
  const agrupamento = new Map<string, string[]>();

  subprocessos.forEach(pu => {
    const unidade = unidadesStore.pesquisarUnidade(pu.unidade);
    if (unidade) {
      // Para unidades operacionais, a superior é a raiz (SEDOC)
      // Para interoperacionais, pode ter subordinadas, então também notificamos diretamente
      const superior = unidade.tipo === 'OPERACIONAL' ? 'SEDOC' : pu.unidade;

      if (!agrupamento.has(superior)) {
        agrupamento.set(superior, []);
      }
      agrupamento.get(superior)!.push(pu.unidade);
    }
  });

  return agrupamento;
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