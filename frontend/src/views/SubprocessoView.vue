<template>
  <BContainer class="mt-4">
    <SubprocessoHeader
      v-if="subprocesso"
      :processo-descricao="subprocesso.processoDescricao || ''"
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
      :tipo-processo="subprocesso.tipoProcesso || TipoProcesso.MAPEAMENTO"
      :mapa="mapa"
      :situacao="subprocesso.situacao"
      :permissoes="subprocesso.permissoes"
    />

    <TabelaMovimentacoes :movimentacoes="movimentacoes" />
  </BContainer>

  <SubprocessoModal
    :mostrar-modal="mostrarModalAlterarDataLimite"
    :data-limite-atual="dataLimite"
    :etapa-atual="subprocesso?.etapaAtual || null"
    @fechar-modal="fecharModalAlterarDataLimite"
    @confirmar-alteracao="confirmarAlteracaoDataLimite"
  />
</template>

<script lang="ts" setup>
import {BContainer} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRoute} from "vue-router";
import SubprocessoCards from "@/components/SubprocessoCards.vue";
import SubprocessoHeader from "@/components/SubprocessoHeader.vue";
import SubprocessoModal from "@/components/SubprocessoModal.vue";
import TabelaMovimentacoes from "@/components/TabelaMovimentacoes.vue";
import {useMapasStore} from "@/stores/mapas";
import {useNotificacoesStore} from "@/stores/notificacoes";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {type Movimentacao, type SubprocessoDetalhe, TipoProcesso,} from "@/types/tipos";

defineProps<{ codProcesso: number; siglaUnidade: string }>();

const route = useRoute();
const subprocessosStore = useSubprocessosStore();
const notificacoesStore = useNotificacoesStore();
const mapaStore = useMapasStore();

const mostrarModalAlterarDataLimite = ref(false);

const codSubprocesso = computed(() => Number(route.params.codSubprocesso));

const subprocesso = computed<SubprocessoDetalhe | null>(
    () => subprocessosStore.subprocessoDetalhe,
);
const mapa = computed(() => mapaStore.mapaCompleto);
const movimentacoes = computed<Movimentacao[]>(
    () => subprocesso.value?.movimentacoes || [],
);
const dataLimite = computed(() =>
    subprocesso.value?.prazoEtapaAtual
        ? new Date(subprocesso.value.prazoEtapaAtual)
        : new Date(),
);

onMounted(async () => {
  await subprocessosStore.fetchSubprocessoDetalhe(codSubprocesso.value);
  await mapaStore.fetchMapaCompleto(codSubprocesso.value);
});

function abrirModalAlterarDataLimite() {
  if (subprocesso.value?.permissoes.podeAlterarDataLimite) {
    mostrarModalAlterarDataLimite.value = true;
  } else {
    notificacoesStore.erro(
        "Ação não permitida",
        "Você não tem permissão para alterar a data limite.",
    );
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
    await subprocessosStore.alterarDataLimiteSubprocesso(
        subprocesso.value.unidade.codigo,
        {novaData},
    );
    fecharModalAlterarDataLimite();
    notificacoesStore.sucesso(
        "Data limite alterada",
        "A data limite foi alterada com sucesso!",
    );
  } catch {
    notificacoesStore.erro(
        "Erro ao alterar data limite",
        "Não foi possível alterar a data limite.",
    );
  }
}
</script>
