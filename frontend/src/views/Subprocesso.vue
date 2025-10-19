<template>
  <div class="container mt-4">
    <SubprocessoHeader
      v-if="unidadeComResponsavelDinamico"
      :processo-descricao="processoAtual?.descricao || ''"
      :unidade-sigla="unidadeComResponsavelDinamico.sigla"
      :unidade-nome="unidadeComResponsavelDinamico.nome"
      :situacao="situacaoUnidadeNoProcesso as string"
      :titular-nome="titularDetalhes?.nome || ''"
      :titular-ramal="titularDetalhes?.ramal || ''"
      :titular-email="titularDetalhes?.email || ''"
      :responsavel-nome="responsavelDetalhes?.nome || ''"
      :responsavel-ramal="responsavelDetalhes?.ramal || ''"
      :responsavel-email="responsavelDetalhes?.email || ''"
      :unidade-atual="unidadeOriginal?.sigla || ''"
      :perfil-usuario="perfilStore.perfilSelecionado"
      :is-subprocesso-em-andamento="isSubprocessoEmAndamento"
      @alterar-data-limite="abrirModalAlterarDataLimite"
    />
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <SubprocessoCards
      :tipo-processo="processoAtual?.tipo || TipoProcesso.MAPEAMENTO"
      :mapa="mapa"
      :situacao="situacaoUnidadeNoProcesso as string"
      @ir-para-atividades="irParaAtividadesConhecimentos"
      @navegar-para-mapa="navegarParaMapa"
      @ir-para-diagnostico-equipe="irParaDiagnosticoEquipe"
      @ir-para-ocupacoes-criticas="irParaOcupacoesCriticas"
    />


    <!-- Seção de Movimentações do Processo -->
    <div class="mt-4">
      <h4>Movimentações do Processo</h4>
      <div
        v-if="movements.length === 0"
        class="alert alert-info"
      >
        Nenhuma movimentação registrada para este subprocesso.
      </div>
      <table
        v-else
        class="table table-striped"
      >
        <thead>
          <tr>
            <th>Data/Hora</th>
            <th>Unidade Origem</th>
            <th>Unidade Destino</th>
            <th>Descrição</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="movement in movements"
            :key="movement.codigo"
          >
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
    :mostrar-modal="mostrarModalAlterarDataLimite"
    :data-limite-atual="dataLimiteAtual || new Date()"
    :etapa-atual="etapaAtual"
    :situacao-etapa-atual="SubprocessoDetalhes?.situacao as any || 'Não informado'"
    @fechar-modal="fecharModalAlterarDataLimite"
    @confirmar-alteracao="confirmarAlteracaoDataLimite"
  />
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicoes'
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
  SituacaoSubprocesso,
  TipoProcesso,
  TipoResponsabilidade,
  Unidade
} from "@/types/tipos";
import {formatDateTimeBR, parseDate} from '@/utils';
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
const { processosPainel } = storeToRefs(processosStore)

// Estados reativos para o modal de alteração de data limite
const mostrarModalAlterarDataLimite = ref(false)

const SubprocessoDetalhes = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.resumoSubprocessos.find(u => u.unidade.sigla === siglaParam.value);
});

const processoAtual = computed(() => processosStore.processoDetalhe);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(idProcesso.value);
});

const sigla = computed<string | undefined>(() => {
  return SubprocessoDetalhes.value?.unidade.sigla;
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
    return { ...unidade, responsavel: atribuicaoVigente.servidor };
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
  if (!unidadeComResponsavelDinamico.value || !unidadeComResponsavelDinamico.value.responsavel) {
    return null;
  }
  return unidadeComResponsavelDinamico.value.responsavel;
});

const situacaoUnidadeNoProcesso = computed(() => {
  return SubprocessoDetalhes.value?.situacao || SituacaoSubprocesso.NAO_INICIADO;
});

const mapa = computed<Mapa | null>(() => {
  if (!unidadeComResponsavelDinamico.value || !processoAtual.value) return null;
  const mapaEncontrado = mapaStore.getMapaByUnidadeId(unidadeComResponsavelDinamico.value.codigo);
  if (mapaEncontrado) {
    return mapaEncontrado as any;
  }
  return null;
});

// Computed para verificar se o subprocesso está em andamento
const isSubprocessoEmAndamento = computed(() => {
  if (!SubprocessoDetalhes.value) return false;
  const situacao = SubprocessoDetalhes.value.situacao;
  const situacoesFinalizadas = [
    SituacaoSubprocesso.CONCLUIDO,
    SituacaoSubprocesso.ATIVIDADES_HOMOLOGADAS,
    SituacaoSubprocesso.MAPA_HOMOLOGADO,
    SituacaoSubprocesso.NAO_INICIADO
  ] as const;

  return !situacoesFinalizadas.includes(situacao as (typeof situacoesFinalizadas)[number]);
});

// Computed para identificar a etapa atual e sua data limite
const etapaAtual = computed(() => {
  if (!SubprocessoDetalhes.value) return null;

  // Se etapa 1 ainda não terminou, é a etapa 1
  if (!SubprocessoDetalhes.value.dataFimEtapa1) {
    return 1;
  }

  // Se etapa 1 terminou mas etapa 2 não começou ou não terminou, é a etapa 2
  if (SubprocessoDetalhes.value.dataLimiteEtapa2) {
    return 2;
  }

  // Se ambas as etapas terminaram, não há etapa em andamento
  return null;
});

const dataLimiteAtual = computed<Date>(() => {
  if (!SubprocessoDetalhes.value || !etapaAtual.value) return new Date();

  if (etapaAtual.value === 1) {
    return new Date(SubprocessoDetalhes.value.dataLimite);
  } else if (etapaAtual.value === 2) {
    return new Date(SubprocessoDetalhes.value.dataLimiteEtapa2);
  }

  return new Date();
});

// Computed properties movidos para os componentes específicos

const movements = computed<Movimentacao[]>(() => {
  if (!SubprocessoDetalhes.value) return [];
  return processosStore.getMovementsForSubprocesso(SubprocessoDetalhes.value.codigo);
});

function navegarParaMapa() {
  if (!sigla.value || !processoAtual.value || !mapa.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.codigo, siglaUnidade: sigla.value};
  router.push({name: 'SubprocessoVisMapa', params});
}

function irParaAtividadesConhecimentos() {
  if (!sigla.value || !processoAtual.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.codigo, siglaUnidade: sigla.value};

  // Verifica se o perfil é CHEFE e se a unidade do subprocesso é a unidade selecionada do perfil
  if (perfilStore.perfilSelecionado === Perfil.CHEFE && perfilStore.unidadeSelecionada === sigla.value) {
    console.log('Navigating to SubprocessoCadastro with params:', params); // ADD THIS
    router.push({name: 'SubprocessoCadastro', params}); // Abre CadAtividades.vue
  } else {
    console.log('Navigating to SubprocessoVisCadastro with params:', params); // ADD THIS
    router.push({name: 'SubprocessoVisCadastro', params}); // Abre VisAtividades.vue
  }
}

function irParaDiagnosticoEquipe() {
  if (!sigla.value || !processoAtual.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.codigo, siglaUnidade: sigla.value};
  router.push({name: 'DiagnosticoEquipe', params});
}

function irParaOcupacoesCriticas() {
  if (!sigla.value || !processoAtual.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.codigo, siglaUnidade: sigla.value};
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
  if (!novaData || !SubprocessoDetalhes.value || !unidadeOriginal.value) {
    return;
  }

  try {
    // Chamar a store para atualizar a data limite
    await processosStore.alterarDataLimiteSubprocesso({
      idProcesso: processoAtual.value?.codigo || 0,
      unidade: unidadeOriginal.value.sigla,
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

  } catch {
    notificacoesStore.erro(
        'Erro ao alterar data limite',
        'Ocorreu um erro ao alterar a data limite. Tente novamente.'
    );
  }
}
</script>

// Estilos movidos para os componentes específicos