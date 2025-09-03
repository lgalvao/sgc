<template>
  <div class="container mt-4">
    <SubprocessoHeader
        v-if="unidadeComResponsavelDinamico"
        :processoDescricao="processoAtual?.descricao || ''"
        :unidadeSigla="unidadeComResponsavelDinamico.sigla"
        :unidadeNome="unidadeComResponsavelDinamico.nome"
        :situacao="situacaoUnidadeNoProcesso"
        :titularNome="titularDetalhes?.nome || ''"
        :titularRamal="titularDetalhes?.ramal || ''"
        :titularEmail="titularDetalhes?.email || ''"
        :responsavelNome="responsavelDetalhes?.nome || ''"
        :responsavelRamal="responsavelDetalhes?.ramal || ''"
        :responsavelEmail="responsavelDetalhes?.email || ''"
        :unidadeAtual="SubprocessoDetalhes?.unidadeAtual || ''"
        :perfilUsuario="perfilStore.perfilSelecionado"
        :isSubprocessoEmAndamento="isSubprocessoEmAndamento"
        @alterarDataLimite="abrirModalAlterarDataLimite"
    />
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <SubprocessoCards
        :tipoProcesso="processoAtual?.tipo || TipoProcesso.MAPEAMENTO"
        :mapa="mapa"
        :situacao="situacaoUnidadeNoProcesso"
        @irParaAtividades="irParaAtividadesConhecimentos"
        @navegarParaMapa="navegarParaMapa"
        @irParaDiagnosticoEquipe="irParaDiagnosticoEquipe"
        @irParaOcupacoesCriticas="irParaOcupacoesCriticas"
    />


    <!-- Seção de Movimentações do Processo -->
    <div class="mt-4">
      <h4>Movimentações do Processo</h4>
      <div v-if="movements.length === 0" class="alert alert-info">
        Nenhuma movimentação registrada para este subprocesso.
      </div>
      <table v-else class="table table-striped">
        <thead>
          <tr>
            <th>Data/Hora</th>
            <th>Unidade Origem</th>
            <th>Unidade Destino</th>
            <th>Descrição</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="movement in movements" :key="movement.id">
            <td>{{ formatDateTimeBR(movement.dataHora) }}</td>
            <td>{{ movement.unidadeOrigem }}</td>
            <td>{{ movement.unidadeDestino }}</td>
            <td>{{ movement.descricao }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <SubprocessoModal
      :mostrarModal="mostrarModalAlterarDataLimite"
      :dataLimiteAtual="dataLimiteAtual"
      :etapaAtual="etapaAtual"
      :situacaoEtapaAtual="SubprocessoDetalhes?.situacao || 'Não informado'"
      @fecharModal="fecharModalAlterarDataLimite"
      @confirmarAlteracao="confirmarAlteracaoDataLimite"
  />
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicaoTemporaria'
import {useMapasStore} from '@/stores/mapas'
import {useServidoresStore} from '@/stores/servidores'
import {useProcessosStore} from '@/stores/processos'
import {usePerfilStore} from '@/stores/perfil'
import {
  Mapa,
  Movimentacao,
  Perfil,
  Processo,
  Servidor,
  Subprocesso,
  TipoProcesso,
  TipoResponsabilidade,
  Unidade
} from "@/types/tipos";
import {formatDateTimeBR, parseDate} from '@/utils/dateUtils';
import {SITUACOES_EM_ANDAMENTO} from '@/constants/situacoes';
import {useNotificacoesStore} from '@/stores/notificacoes';
import SubprocessoHeader from '@/components/SubprocessoHeader.vue';
import SubprocessoCards from '@/components/SubprocessoCards.vue';
import SubprocessoModal from '@/components/SubprocessoModal.vue';

const props = defineProps<{ idProcesso: number; siglaUnidade: string }>();

const router = useRouter()
const idProcesso = computed(() => props.idProcesso)
const siglaParam = computed<string | undefined>(() => props.siglaUnidade)
const unidadesStore = useUnidadesStore()
const atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();
const mapaStore = useMapasStore()
const servidoresStore = useServidoresStore()
const processosStore = useProcessosStore()
const perfilStore = usePerfilStore()
const notificacoesStore = useNotificacoesStore()
const {processos} = storeToRefs(processosStore)

// Estados reativos para o modal de alteração de data limite
const mostrarModalAlterarDataLimite = ref(false)

const SubprocessoDetalhes = computed<Subprocesso | undefined>(() => {
  return processosStore.getUnidadesDoProcesso(idProcesso.value).find((pu: Subprocesso) => pu.unidade === siglaParam.value);
});

const processoAtual = computed<Processo | null>(() => {
  if (!SubprocessoDetalhes.value) return null;
  return (processos.value as Processo[]).find(p => p.id === SubprocessoDetalhes.value?.idProcesso) || null;
});

const sigla = computed<string | undefined>(() => {
  return SubprocessoDetalhes.value?.unidade;
});

const unidadeOriginal = computed<Unidade | null>(() => {
  if (!sigla.value) {

    return null;
  }
  const unidadeEncontrada = unidadesStore.pesquisarUnidade(sigla.value);

  if (unidadeEncontrada && unidadeEncontrada.sigla && unidadeEncontrada.nome) {
    return unidadeEncontrada as Unidade;
  }

  return null;
});

const unidadeComResponsavelDinamico = computed<Unidade | null>(() => {
  const unidade = unidadeOriginal.value;
  if (!unidade) return null;

  const atribuicoes = atribuicaoTemporariaStore.getAtribuicoesPorUnidade(unidade.sigla);
  const hoje = new Date();

  // Encontrar a atribuição temporária vigente
  const atribuicaoVigente = atribuicoes.find(atrb => {
    const dataInicio = new Date(atrb.dataInicio);
    const dataTermino = new Date(atrb.dataTermino);
    return hoje >= dataInicio && hoje <= dataTermino;
  });

  if (atribuicaoVigente) {
    // Retorna uma nova unidade com o responsável da atribuição temporária
    return {
      ...unidade,
      responsavel: {
        idServidor: atribuicaoVigente.idServidor,
        tipo: TipoResponsabilidade.ATRIBUICAO, // Usar o enum
        dataInicio: new Date(atribuicaoVigente.dataInicio),
        dataFim: new Date(atribuicaoVigente.dataTermino),
      }
    };
  }

  // Se não houver atribuição temporária vigente, retorna a unidade original
  return unidade;
});

const titularDetalhes = computed<Servidor | null>(() => {
  if (unidadeComResponsavelDinamico.value && unidadeComResponsavelDinamico.value.idServidorTitular) {
    return servidoresStore.getServidorById(unidadeComResponsavelDinamico.value.idServidorTitular) || null;
  }
  return null;
});

const responsavelDetalhes = computed<Servidor | null>(() => {
  if (!unidadeComResponsavelDinamico.value || !unidadeComResponsavelDinamico.value.responsavel || !unidadeComResponsavelDinamico.value.responsavel.idServidor) {
    return null;
  }
  return servidoresStore.getServidorById(unidadeComResponsavelDinamico.value.responsavel.idServidor) || null;
});

const situacaoUnidadeNoProcesso = computed<string>(() => {
  return SubprocessoDetalhes.value?.situacao || 'Não informado';
});

const mapa = computed<Mapa | null>(() => {
  if (!unidadeComResponsavelDinamico.value || !processoAtual.value) return null;
  return mapaStore.getMapaByUnidadeId(unidadeComResponsavelDinamico.value.sigla, processoAtual.value.id) || null;
});

// Computed para verificar se o subprocesso está em andamento
const isSubprocessoEmAndamento = computed(() => {
  if (!SubprocessoDetalhes.value) return false;
  return SITUACOES_EM_ANDAMENTO.includes(SubprocessoDetalhes.value.situacao as any);
});

// Computed para identificar a etapa atual e sua data limite
const etapaAtual = computed(() => {
  if (!SubprocessoDetalhes.value) return null;

  // Se etapa 1 ainda não terminou, é a etapa 1
  if (!SubprocessoDetalhes.value.dataFimEtapa1) {
    return 1;
  }

  // Se etapa 1 terminou mas etapa 2 não começou ou não terminou, é a etapa 2
  if (SubprocessoDetalhes.value.dataLimiteEtapa2 && !SubprocessoDetalhes.value.dataFimEtapa2) {
    return 2;
  }

  // Se ambas as etapas terminaram, não há etapa em andamento
  return null;
});

const dataLimiteAtual = computed(() => {
  if (!SubprocessoDetalhes.value || !etapaAtual.value) return null;

  if (etapaAtual.value === 1) {
    return SubprocessoDetalhes.value.dataLimiteEtapa1;
  } else if (etapaAtual.value === 2) {
    return SubprocessoDetalhes.value.dataLimiteEtapa2;
  }

  return null;
});

// Computed properties movidos para os componentes específicos

const movements = computed<Movimentacao[]>(() => {
  if (!SubprocessoDetalhes.value) return [];
  return processosStore.getMovementsForSubprocesso(SubprocessoDetalhes.value.id);
});

function navegarParaMapa() {
  if (!sigla.value || !processoAtual.value || !mapa.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.id, siglaUnidade: sigla.value};
  router.push({name: 'SubprocessoVisMapa', params});
}

function irParaAtividadesConhecimentos() {
  if (!sigla.value || !processoAtual.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.id, siglaUnidade: sigla.value};

  // Verifica se o perfil é CHEFE e se a unidade do subprocesso é a unidade selecionada do perfil
  if (perfilStore.perfilSelecionado === Perfil.CHEFE && perfilStore.unidadeSelecionada === sigla.value) {
    router.push({name: 'SubprocessoCadastro', params}); // Abre CadAtividades.vue
  } else {
    router.push({name: 'SubprocessoVisCadastro', params}); // Abre VisAtividades.vue
  }
}

function irParaDiagnosticoEquipe() {
  if (!sigla.value || !processoAtual.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.id, siglaUnidade: sigla.value};
  router.push({name: 'DiagnosticoEquipe', params});
}

function irParaOcupacoesCriticas() {
  if (!sigla.value || !processoAtual.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.id, siglaUnidade: sigla.value};
  router.push({name: 'OcupacoesCriticas', params});
}

// Funções para o modal de alteração de data limite
function abrirModalAlterarDataLimite() {
  mostrarModalAlterarDataLimite.value = true;
}

function fecharModalAlterarDataLimite() {
  mostrarModalAlterarDataLimite.value = false;
}

async function confirmarAlteracaoDataLimite(novaData: string) {
  if (!novaData || !SubprocessoDetalhes.value) {
    return;
  }

  try {
    // Chamar a store para atualizar a data limite
    await processosStore.alterarDataLimiteSubprocesso({
      idProcesso: SubprocessoDetalhes.value.idProcesso,
      unidade: SubprocessoDetalhes.value.unidade,
      etapa: etapaAtual.value || 1,
      novaDataLimite: parseDate(novaData) || new Date()
    });

    // Fechar modal
    fecharModalAlterarDataLimite();

    // Mostrar notificação de sucesso
    notificacoesStore.sucesso(
        'Data limite alterada',
        'A data limite foi alterada com sucesso!'
    );

  } catch (error) {
    notificacoesStore.erro(
        'Erro ao alterar data limite',
        'Ocorreu um erro ao alterar a data limite. Tente novamente.'
    );
  }
}
</script>

// Estilos movidos para os componentes específicos