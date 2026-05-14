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
import {TEXTOS} from "@/constants/textos";
import {Perfil, type Unidade} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";
import {buscarCodigosUnidadesComMapaVigente, buscarTodasUnidades} from "@/services/unidadeService";

const relatoriosStore = useRelatoriosStore();
const perfilStore = usePerfilStore();
const {notify} = useNotification();

const unidadesDisponiveis = ref<Unidade[]>([]);
const unidadesSelecionadas = ref<number[]>([]);
const carregando = ref(false);
const relatorioMapas = computed(() => relatoriosStore.relatorioMapas);
const temUnidadesSelecionadas = computed(() => unidadesSelecionadas.value.length > 0);
const semMapasDisponiveis = computed(() => !carregando.value && unidadesDisponiveis.value.length === 0);
const mensagemSemMapasDisponiveis = computed(() =>
    perfilStore.perfilSelecionado === Perfil.GESTOR
        ? "Não há mapas vigentes para sua unidade ou unidades subordinadas."
        : "Não há mapas vigentes."
);

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

function buscarSubarvore(unidades: Unidade[], codigoRaiz: number): Unidade[] {
  for (const unidade of unidades) {
    if (unidade.codigo === codigoRaiz) {
      return [unidade];
    }

    const encontrada = buscarSubarvore(unidade.filhas ?? [], codigoRaiz);
    if (encontrada.length > 0) {
      return encontrada;
    }
  }

  return [];
}

function aplicarEscopoPerfil(unidades: Unidade[]): Unidade[] {
  if (perfilStore.perfilSelecionado !== Perfil.GESTOR || !perfilStore.unidadeSelecionada) {
    return unidades;
  }

  return buscarSubarvore(unidades, perfilStore.unidadeSelecionada);
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
    unidadesDisponiveis.value = aplicarEscopoPerfil(
        filtrarArvorePorMapaVigente(unidadesComElegibilidade)
    );
  } catch {
    notify("Erro ao carregar unidades", "danger");
  }
}

async function exportarPdf() {
  if (!temUnidadesSelecionadas.value) {
    return;
  }

  try {
    carregando.value = true;
    await relatoriosStore.exportarMapasPdf(unidadesSelecionadas.value);
  } catch {
    // O erro já é normalizado na store; a view só precisa encerrar o estado de carregamento.
  } finally {
    carregando.value = false;
  }

  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_GERAR, "danger");
  }
}

async function gerarRelatorio() {
  if (!temUnidadesSelecionadas.value) {
    return;
  }

  try {
    carregando.value = true;
    await relatoriosStore.buscarRelatorioMapas(unidadesSelecionadas.value);
  } catch {
    // O erro já é normalizado na store; a view só precisa encerrar o estado de carregamento.
  } finally {
    carregando.value = false;
  }

  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
  }
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
  carregarUnidades();
});
</script>
