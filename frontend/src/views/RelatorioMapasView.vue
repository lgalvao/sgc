<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando && relatorioMapas.length === 0"/>

    <template v-else>
      <PageHeader title="Mapas vigentes">
        <template #actions>
          <BButton to="/relatorios" variant="outline-secondary">
            <i class="bi bi-arrow-left me-1"/> Voltar
          </BButton>
        </template>
      </PageHeader>

      <EmptyState
          v-if="semMapasDisponiveis"
          :title="mensagemSemMapasDisponiveis"
          icon="bi-file-earmark-spreadsheet"
      />

      <RelatorioMapasFiltros
          v-else-if="unidadesDisponiveis.length > 0"
          :carregando="carregando"
          :tem-unidades-selecionadas="temUnidadesSelecionadas"
          :unidades-disponiveis="unidadesDisponiveis"
          :unidades-selecionadas="unidadesSelecionadas"
          @exportar="exportarPdf"
          @gerar="gerarRelatorio"
          @update:unidades-selecionadas="unidadesSelecionadas = $event"/>

      <div v-if="relatorioMapas.length > 0">
        <div class="d-flex flex-column gap-3">
          <RelatorioMapaVigenteCard
              v-for="mapa in relatorioMapas"
              :key="mapa.codigoUnidade"
              :mapa="mapa"/>
        </div>
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import RelatorioMapasFiltros from "@/components/relatorios/RelatorioMapasFiltros.vue";
import RelatorioMapaVigenteCard from "@/components/relatorios/RelatorioMapaVigenteCard.vue";
import {useRelatoriosStore} from "@/stores/relatorios";
import {usePerfilStore} from "@/stores/perfil";
import {useRelatorioUnidadesComMapaQuery} from "@/composables/useRelatorioMapasQuery";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {Perfil} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";
import {useAsyncAction} from "@/composables/useAsyncAction";

const relatoriosStore = useRelatoriosStore();
const perfilStore = usePerfilStore();
const {notify} = useNotification();
const acaoRelatorio = useAsyncAction();

const unidadesSelecionadas = ref<number[]>([]);

const unidadesQuery = useRelatorioUnidadesComMapaQuery();
const unidadesDisponiveis = computed(() => unidadesQuery.data.value ?? []);

const carregando = computed(() => unidadesQuery.isLoading.value || unidadesQuery.isPending.value || acaoRelatorio.carregando.value);
const relatorioMapas = computed(() => relatoriosStore.relatorioMapas);
const temUnidadesSelecionadas = computed(() => unidadesSelecionadas.value.length > 0);
const semMapasDisponiveis = computed(() => !carregando.value && unidadesDisponiveis.value.length === 0);

const mensagemSemMapasDisponiveis = computed(() =>
    perfilStore.perfilSelecionado === Perfil.GESTOR
        ? "Não há mapas vigentes para sua unidade ou unidades subordinadas."
        : "Não há mapas vigentes."
);

async function exportarPdf() {
  if (!temUnidadesSelecionadas.value) return;
  await acaoRelatorio.executar(
      () => relatoriosStore.exportarMapasPdf(unidadesSelecionadas.value),
      TEXTOS_RELATORIOS.ERRO_GERAR,
      {
        relancarErro: false,
        aoOcorrerErro: (erro) => notify(erro.mensagem || TEXTOS_RELATORIOS.ERRO_GERAR, "danger"),
      },
  );
}

async function gerarRelatorio() {
  if (!temUnidadesSelecionadas.value) return;
  await acaoRelatorio.executar(
      () => relatoriosStore.buscarRelatorioMapas(unidadesSelecionadas.value),
      TEXTOS_RELATORIOS.ERRO_BUSCA,
      {
        relancarErro: false,
        aoOcorrerErro: (erro) => notify(erro.mensagem || TEXTOS_RELATORIOS.ERRO_BUSCA, "danger"),
      },
  );
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
});
</script>
