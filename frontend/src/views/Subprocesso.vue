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

    <TabelaMovimentacoes :movimentacoes="movements" />
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
import {useUnidadesStore} from '@/stores/unidades'
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicoes'
import {useMapasStore} from '@/stores/mapas'
import {useServidoresStore} from '@/stores/servidores'
import {useProcessosStore} from '@/stores/processos'
import {usePerfilStore} from '@/stores/perfil'
import {
  MapaCompleto,
  type Movimentacao,
  Perfil,
  Servidor,
  SituacaoSubprocesso,
  TipoProcesso,
  Unidade
} from "@/types/tipos";
import {useNotificacoesStore} from '@/stores/notificacoes';
import SubprocessoHeader from '@/components/SubprocessoHeader.vue';
import SubprocessoCards from '@/components/SubprocessoCards.vue';
import SubprocessoModal from '@/components/SubprocessoModal.vue';
import TabelaMovimentacoes from '@/components/TabelaMovimentacoes.vue';

const props = defineProps<{ codProcesso: number; siglaUnidade: string }>();

const router = useRouter()
const codProcesso = computed(() => props.codProcesso)
const siglaParam = computed<string | undefined>(() => props.siglaUnidade)
const unidadesStore = useUnidadesStore()
const atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();
const mapaStore = useMapasStore()
const servidoresStore = useServidoresStore()
const processosStore = useProcessosStore()
const perfilStore = usePerfilStore()
const notificacoesStore = useNotificacoesStore()

const mostrarModalAlterarDataLimite = ref(false)

const SubprocessoDetalhes = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.resumoSubprocessos.find(u => u.unidadeNome === siglaParam.value);
});

const processoAtual = computed(() => processosStore.processoDetalhe);

const sigla = computed<string | undefined>(() => {
  return SubprocessoDetalhes.value?.unidadeNome;
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
    return {...unidade, responsavel: atribuicaoVigente.servidor};
  }

  // Se não houver atribuição temporária vigente, retorna a unidade original
  return unidade;
});

const titularDetalhes = computed<Servidor | null>(() => {
  if (unidadeComResponsavelDinamico.value && unidadeComResponsavelDinamico.value.idServidorTitular) {
    return servidoresStore.getServidorById(Number(unidadeComResponsavelDinamico.value.idServidorTitular)) || null;
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

const codSubrocesso = computed(() => SubprocessoDetalhes.value?.unidadeCodigo);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(codProcesso.value);
  if (codSubrocesso.value) {
    await mapaStore.fetchMapaCompleto(codSubrocesso.value);
  }
});

const mapa = computed<MapaCompleto | null>(() => {
  if (!unidadeComResponsavelDinamico.value || !processoAtual.value) return null;
  return mapaStore.mapaCompleto;
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
  ];

  return !situacoesFinalizadas.includes(situacao as any);
});

// Computed para identificar a etapa atual e sua data limite
const etapaAtual = computed(() => {
  if (!SubprocessoDetalhes.value) return null;

  // Lógica simplificada: se a situação não for finalizada, consideramos etapa 1.
  const situacao = SubprocessoDetalhes.value.situacao;
  const situacoesFinalizadas = [
    SituacaoSubprocesso.CONCLUIDO,
    SituacaoSubprocesso.ATIVIDADES_HOMOLOGADAS,
    SituacaoSubprocesso.MAPA_HOMOLOGADO,
  ];

  if (!situacoesFinalizadas.includes(situacao as any)) {
    return 1;
  }

  return null;
});

const dataLimiteAtual = computed<Date>(() => {
  if (!SubprocessoDetalhes.value || !etapaAtual.value) return new Date();
  return new Date(SubprocessoDetalhes.value.dataLimite);
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

  const params = {codProcesso: processoAtual.value.codigo, siglaUnidade: sigla.value};
  router.push({name: 'SubprocessoVisMapa', params});
}

function irParaAtividadesConhecimentos() {
  if (!sigla.value || !processoAtual.value) {
    return;
  }

  const params = {codProcesso: processoAtual.value.codigo, siglaUnidade: sigla.value};

  // Verifica se o perfil é CHEFE e se a unidade do subprocesso é a unidade selecionada do perfil
  if (perfilStore.perfilSelecionado === Perfil.CHEFE && perfilStore.unidadeSelecionada === unidadeOriginal.value?.codigo) {
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

  const params = {codProcesso: processoAtual.value.codigo, siglaUnidade: sigla.value};
  router.push({name: 'DiagnosticoEquipe', params});
}

function irParaOcupacoesCriticas() {
  if (!sigla.value || !processoAtual.value) {
    return;
  }

  const params = {codProcesso: processoAtual.value.codigo, siglaUnidade: sigla.value};
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
    await processosStore.alterarDataLimiteSubprocesso(SubprocessoDetalhes.value.codigo, { novaData: novaData });

    // Fechar modal
    fecharModalAlterarDataLimite();

    // Mostrar notificação de sucesso
    notificacoesStore.sucesso(
        'Data limite alterada',
        'A data limite foi alterada!'
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