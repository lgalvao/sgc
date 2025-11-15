<template>
  <div class="container mt-4">
    <SubprocessoHeader
        v-if="subprocesso"
        :processo-descricao="processoAtual?.descricao || ''"
        :unidade-sigla="subprocesso.unidade.sigla"
        :unidade-nome="subprocesso.unidade.nome"
        :situacao="subprocesso.situacaoLabel"
        :titular-nome="subprocesso.titular?.nome || ''"
        :titular-ramal="subprocesso.titular?.ramal || ''"
        :titular-email="subprocesso.titular?.email || ''"
        :responsavel-nome="subprocesso.responsavel?.nome || ''"
        :responsavel-ramal="subprocesso.responsavel?.ramal || ''"
        :responsavel-email="subprocesso.responsavel?.email || ''"
        :unidade-atual="subprocesso.unidade.sigla"
        :perfil-usuario="perfilStore.perfilSelecionado"
        :is-subprocesso-em-andamento="subprocesso.isEmAndamento"
        @alterar-data-limite="abrirModalAlterarDataLimite"
    />
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <SubprocessoCards
        v-if="subprocesso"
        :tipo-processo="processoAtual?.tipo || TipoProcesso.MAPEAMENTO"
        :mapa="mapa"
        :situacao="subprocesso.situacao"
        :permissoes="subprocesso.permissoes"
        @navegar-para-mapa="navegarParaMapa"
        @ir-para-diagnostico-equipe="irParaDiagnosticoEquipe"
        @ir-para-ocupacoes-criticas="irParaOcupacoesCriticas"
    />

    <TabelaMovimentacoes :movimentacoes="movimentacoes" />
  </div>

  <SubprocessoModal
      :mostrar-modal="mostrarModalAlterarDataLimite"
      :data-limite-atual="dataLimite"
      :etapa-atual="etapaAtual"
      :situacao-etapa-atual="subprocesso?.situacao || 'Não informado'"
      @fechar-modal="fecharModalAlterarDataLimite"
      @confirmar-alteracao="confirmarAlteracaoDataLimite"
  />
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useProcessosStore } from '@/stores/processos'
import { usePerfilStore } from '@/stores/perfil'
import { useMapasStore } from '@/stores/mapas'
import { Movimentacao, SubprocessoDetalhe, TipoProcesso } from "@/types/tipos";
import { useNotificacoesStore } from '@/stores/notificacoes';
import SubprocessoHeader from '@/components/SubprocessoHeader.vue';
import SubprocessoCards from '@/components/SubprocessoCards.vue';
import SubprocessoModal from '@/components/SubprocessoModal.vue';
import TabelaMovimentacoes from '@/components/TabelaMovimentacoes.vue';

const props = defineProps<{ codProcesso: number; siglaUnidade: string }>();

const route = useRoute()
const processosStore = useProcessosStore()
const perfilStore = usePerfilStore()
const notificacoesStore = useNotificacoesStore()
const mapaStore = useMapasStore()

const mostrarModalAlterarDataLimite = ref(false)

const codSubprocesso = computed(() => Number(route.params.codSubprocesso));

const subprocesso = computed<SubprocessoDetalhe | null>(() => processosStore.processoDetalhe);
const processoAtual = computed(() => processosStore.processoDetalhe?.processo);
const mapa = computed(() => mapaStore.mapaCompleto);
const movimentacoes = computed<Movimentacao[]>(() => subprocesso.value?.movimentacoes || []);
const dataLimite = computed(() => subprocesso.value?.prazoEtapaAtual ? new Date(subprocesso.value.prazoEtapaAtual) : new Date());
const etapaAtual = computed(() => subprocesso.value?.etapaAtual);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(codSubprocesso.value);
  await mapaStore.fetchMapaCompleto(codSubprocesso.value);
});

function navegarParaMapa() {
  // A lógica de navegação será simplificada ou movida para o SubprocessoCards
}

function irParaDiagnosticoEquipe() {
  // A lógica de navegação será simplificada ou movida para o SubprocessoCards
}

function irParaOcupacoesCriticas() {
  // A lógica de navegação será simplificada ou movida para o SubprocessoCards
}

function abrirModalAlterarDataLimite() {
  if (subprocesso.value?.permissoes.podeAlterarDataLimite) {
    mostrarModalAlterarDataLimite.value = true;
  } else {
    notificacoesStore.erro('Ação não permitida', 'Você não tem permissão para alterar a data limite.');
  }
}

function fecharModalAlterarDataLimite() {
  mostrarModalAlterarDataLimite.value = false;
}

async function confirmarAlteracaoDataLimite(novaData: string) {
  if (!novaData || !subprocesso.value) {
    return;
  }

  try {
    await processosStore.alterarDataLimiteSubprocesso(subprocesso.value.unidade.codigo, { novaData });
    fecharModalAlterarDataLimite();
    notificacoesStore.sucesso('Data limite alterada', 'A data limite foi alterada com sucesso!');
  } catch (error) {
    notificacoesStore.erro('Erro ao alterar data limite', 'Não foi possível alterar a data limite.');
  }
}
</script>
