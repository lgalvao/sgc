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
      :pode-alterar-data-limite="subprocesso.permissoes.podeAlterarDataLimite"
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
    />

    <TabelaMovimentacoes :movimentacoes="movimentacoes" />
  </div>

  <SubprocessoModal
    :mostrar-modal="mostrarModalAlterarDataLimite"
    :data-limite-atual="dataLimite"
    :situacao-etapa-atual="subprocesso?.situacao || 'Não informado'"
    @fechar-modal="fecharModalAlterarDataLimite"
    @confirmar-alteracao="confirmarAlteracaoDataLimite"
  />
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProcessosStore } from '@/stores/processos'
import { useMapasStore } from '@/stores/mapas'
import { Movimentacao, SubprocessoDetalhe, TipoProcesso } from "@/types/tipos";
import { useNotificacoesStore } from '@/stores/notificacoes';
import SubprocessoHeader from '@/components/SubprocessoHeader.vue';
import SubprocessoCards from '@/components/SubprocessoCards.vue';
import SubprocessoModal from '@/components/SubprocessoModal.vue';
import TabelaMovimentacoes from '@/components/TabelaMovimentacoes.vue';

const props = defineProps<{ codProcesso: number; siglaUnidade: string }>();

const route = useRoute()
const router = useRouter()
const processosStore = useProcessosStore()
const notificacoesStore = useNotificacoesStore()
const mapaStore = useMapasStore()

const mostrarModalAlterarDataLimite = ref(false)

const codSubprocesso = computed(() => Number(route.params.codSubprocesso));

const subprocesso = computed<SubprocessoDetalhe | null>(() => processosStore.processoDetalhe);
const processoAtual = computed(() => processosStore.processoDetalhe?.processo);
const mapa = computed(() => mapaStore.mapaCompleto);
const movimentacoes = computed<Movimentacao[]>(() => subprocesso.value?.movimentacoes || []);
const dataLimite = computed(() => subprocesso.value?.prazoEtapaAtual ? new Date(subprocesso.value.prazoEtapaAtual) : new Date());

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(codSubprocesso.value);
  await mapaStore.fetchMapaCompleto(codSubprocesso.value);
});


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
