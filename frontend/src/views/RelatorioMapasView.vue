<template>
  <LayoutPadrao>
    <PageHeader title="Relatório: Mapas vigentes">
      <template #actions>
        <BButton to="/relatorios" variant="outline-secondary">
          <i class="bi bi-arrow-left me-1"/> Voltar
        </BButton>
      </template>
    </PageHeader>

    <RelatorioMapasFiltros
        :carregando="carregando"
        :tem-unidades-selecionadas="temUnidadesSelecionadas"
        :unidades-disponiveis="unidadesDisponiveis"
        :unidades-selecionadas="unidadesSelecionadas"
        @exportar="exportarPdf"
        @gerar="gerarRelatorio"
        @update:unidades-selecionadas="unidadesSelecionadas = $event"/>

    <div v-if="carregando && relatorioMapas.length === 0" class="text-center py-5">
      <BSpinner variant="primary"/>
    </div>

    <template v-else-if="relatorioMapas.length > 0">
      <div class="d-flex flex-column gap-3">
        <RelatorioMapaVigenteCard
            v-for="mapa in relatorioMapas"
            :key="mapa.codigoUnidade"
            :mapa="mapa"/>
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import RelatorioMapasFiltros from "@/components/relatorios/RelatorioMapasFiltros.vue";
import RelatorioMapaVigenteCard from "@/components/relatorios/RelatorioMapaVigenteCard.vue";
import {useRelatoriosStore} from "@/stores/relatorios";
import {TEXTOS} from "@/constants/textos";
import type {Unidade} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";
import {buscarCodigosUnidadesComMapaVigente, buscarTodasUnidades} from "@/services/unidadeService";

const relatoriosStore = useRelatoriosStore();
const {notify} = useNotification();

const unidadesDisponiveis = ref<Unidade[]>([]);
const unidadesSelecionadas = ref<number[]>([]);
const carregando = ref(false);
const relatorioMapas = computed(() => relatoriosStore.relatorioMapas);
const temUnidadesSelecionadas = computed(() => unidadesSelecionadas.value.length > 0);

function aplicarElegibilidadeMapaVigente(unidades: Unidade[], codigosElegiveis: Set<number>): Unidade[] {
  return unidades.map(unidade => ({
    ...unidade,
    isElegivel: codigosElegiveis.has(unidade.codigo),
    filhas: unidade.filhas ? aplicarElegibilidadeMapaVigente(unidade.filhas, codigosElegiveis) : []
  }));
}

function filtrarArvorePorMapaVigente(unidades: Unidade[]): Unidade[] {
  return unidades
      .map((unidade): Unidade | null => {
        const filhasFiltradas = unidade.filhas ? filtrarArvorePorMapaVigente(unidade.filhas) : [];
        const manterUnidade = unidade.isElegivel === true || filhasFiltradas.length > 0;

        if (!manterUnidade) {
          return null;
        }

        return {
          ...unidade,
          filhas: filhasFiltradas
        };
      })
      .filter((unidade): unidade is Unidade => unidade !== null);
}

async function carregarUnidades() {
  try {
    const [arvore, codigosComMapa] = await Promise.all([
      buscarTodasUnidades(),
      buscarCodigosUnidadesComMapaVigente()
    ]);
    const unidadesComElegibilidade = aplicarElegibilidadeMapaVigente(
        arvore,
        new Set(codigosComMapa)
    );
    unidadesDisponiveis.value = filtrarArvorePorMapaVigente(unidadesComElegibilidade);
  } catch {
    notify("Erro ao carregar unidades", "danger");
  }
}

async function exportarPdf() {
  if (!temUnidadesSelecionadas.value) {
    return;
  }

  carregando.value = true;
  await relatoriosStore.exportarMapasPdf(unidadesSelecionadas.value);
  carregando.value = false;
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_GERAR, "danger");
  }
}

async function gerarRelatorio() {
  if (!temUnidadesSelecionadas.value) {
    return;
  }

  carregando.value = true;
  await relatoriosStore.buscarRelatorioMapas(unidadesSelecionadas.value);
  carregando.value = false;
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
  }
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
  carregarUnidades();
});
</script>
