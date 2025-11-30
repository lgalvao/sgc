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
      :cod-subprocesso="codSubprocesso"
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
import {BContainer, useToast} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import SubprocessoCards from "@/components/SubprocessoCards.vue";
import SubprocessoHeader from "@/components/SubprocessoHeader.vue";
import SubprocessoModal from "@/components/SubprocessoModal.vue";
import TabelaMovimentacoes from "@/components/TabelaMovimentacoes.vue";
import {useMapasStore} from "@/stores/mapas";

import {useSubprocessosStore} from "@/stores/subprocessos";
import {type Movimentacao, type SubprocessoDetalhe, TipoProcesso,} from "@/types/tipos";

const props = defineProps<{ codProcesso: number; siglaUnidade: string }>();

const subprocessosStore = useSubprocessosStore();

const mapaStore = useMapasStore();
const toast = useToast(); // Instantiate toast

const mostrarModalAlterarDataLimite = ref(false);
const codSubprocesso = ref<number | null>(null);

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
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
      props.codProcesso,
      props.siglaUnidade,
  );

  if (id) {
    codSubprocesso.value = id;
    await subprocessosStore.buscarSubprocessoDetalhe(id);
    await mapaStore.buscarMapaCompleto(id);
  }
});

function abrirModalAlterarDataLimite() {
  if (subprocesso.value?.permissoes.podeAlterarDataLimite) {
    mostrarModalAlterarDataLimite.value = true;
  } else {
    toast.create({
        title: "Ação não permitida",
        body: "Você não tem permissão para alterar a data limite.",
        props: { variant: 'danger', value: true },
    });
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
    toast.create({
        title: "Data limite alterada",
        body: "A data limite foi alterada com sucesso!",
        props: { variant: 'success', value: true },
    });
  } catch {
    toast.create({
        title: "Erro ao alterar data limite",
        body: "Não foi possível alterar a data limite.",
        props: { variant: 'danger', value: true },
    });
  }
}
</script>
