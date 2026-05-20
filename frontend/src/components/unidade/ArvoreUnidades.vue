<template>
  <div class="arvore-unidades">
    <ArvoreToolbar
        v-if="unidadesExibidasOriginais.length > 0"
        v-model:termo-busca="termoBusca"
        :modo-selecao="modoSelecao"
        @selecionar-todos="selecionarTodas(unidadesExibidas)"
        @limpar-selecao="deselecionarTodas"
        @expandir-todos="expandirRecursivo(unidadesExibidas)"
        @recolher-todos="limparExpansao"
    />

    <UnidadeTreeNode
        v-for="unidade in unidadesExibidas"
        :key="unidade.sigla"
        :get-estado-selecao="getEstadoSelecao"
        :is-checked="isChecked"
        :is-expanded="isExpanded"
        :is-habilitado="isHabilitado"
        :modo-selecao="modoSelecao"
        :on-toggle="toggle"
        :on-toggle-expand="toggleExpand"
        :unidade="unidade"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from "vue";
import type {Unidade} from "@/types/tipos";
import {organizarArvoreUnidades, TITULO_GRUPO_ZONAS_ELEITORAIS} from "@/utils/treeUtils";
import UnidadeTreeNode from "./UnidadeTreeNode.vue";
import ArvoreToolbar from "./ArvoreToolbar.vue";
import {useArvoreSelecao} from "./useArvoreSelecao";
import {useArvoreExpansao} from "./useArvoreExpansao";

type UnidadeExibida = Unidade & {
  agrupadorVisual?: boolean;
};

interface Props {
  unidades: Unidade[];
  modelValue: number[];
  filtrarPor?: (unidade: Unidade) => boolean;
  ocultarRaiz?: boolean;
  modoSelecao?: boolean;
  mostrarSuperioresNaoElegiveisComoIndeterminados?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  filtrarPor: () => true,
  ocultarRaiz: true,
  modoSelecao: true,
  mostrarSuperioresNaoElegiveisComoIndeterminados: false
});

const emit = defineEmits<(e: "update:modelValue", value: number[]) => void>();

// Composables
const {
  unidadesSelecionadasLocal,
  isChecked,
  isHabilitado,
  getEstadoSelecao,
  toggle,
  selecionarTodas,
  deselecionarTodas,
  filtrarSelecaoPorElegibilidade
} = useArvoreSelecao(props, emit);

const {
  expandedUnits,
  isExpanded,
  toggleExpand,
  expandirRecursivo,
  limparExpansao
} = useArvoreExpansao();

const termoBusca = ref("");

// Lógica de Agrupamento Visual (Zonas Eleitorais)
function criarCodigoGrupoZonasEleitorais(codigoPai: number): number {
  return -((Math.abs(codigoPai) * 1000) + 999);
}

function criarGrupoZonasEleitorais(codigoPai: number, filhas: UnidadeExibida[]): UnidadeExibida {
  return {
    codigo: criarCodigoGrupoZonasEleitorais(codigoPai),
    sigla: "",
    nome: TITULO_GRUPO_ZONAS_ELEITORAIS,
    tipo: "AGRUPADOR_VISUAL",
    isElegivel: false,
    filhas,
    agrupadorVisual: true,
  };
}

function organizarUnidadesParaExibicao(unidades: Unidade[], codigoPai: number): UnidadeExibida[] {
  return organizarArvoreUnidades(unidades as UnidadeExibida[], codigoPai, {
    obterCodigo: (unidade) => unidade.codigo,
    obterRotulo: (unidade) => unidade.nome,
    obterSigla: (unidade) => unidade.sigla,
    obterTipo: (unidade) => unidade.tipo,
    obterFilhos: (unidade) => unidade.filhas as UnidadeExibida[] | undefined,
    clonarComFilhos: (unidade, filhas) => ({
      ...unidade,
      filhas,
    }),
    criarGrupoZonas: (identificadorGrupo, filhas) =>
        criarGrupoZonasEleitorais(Number(identificadorGrupo), filhas),
    criarIdentificadorGrupoFilhos: (unidade) => unidade.codigo,
  });
}

// Lógica de Busca e Filtragem
function filtrarUnidadesRecursivo(unidades: Unidade[], termo: string, forceInclude = false): Unidade[] {
  const termoNormalizado = termo.trim().toLowerCase();
  if (!termoNormalizado && !forceInclude) return unidades;

  const resultado: Unidade[] = [];

  for (const unidade of unidades) {
    const matches = forceInclude ||
        unidade.sigla.toLowerCase().includes(termoNormalizado) ||
        unidade.nome.toLowerCase().includes(termoNormalizado);

    const filhasFiltradas = unidade.filhas
        ? filtrarUnidadesRecursivo(unidade.filhas, termo, matches)
        : [];

    if (matches || filhasFiltradas.length > 0) {
      resultado.push({
        ...unidade,
        filhas: filhasFiltradas
      });
    }
  }

  return resultado;
}

const unidadesExibidasOriginais = computed((): UnidadeExibida[] => {
  const filtradas = props.unidades.filter(props.filtrarPor);
  const lista: UnidadeExibida[] = [];

  for (const u of filtradas) {
    if (props.ocultarRaiz) {
      if (u.filhas) {
        lista.push(...organizarUnidadesParaExibicao(u.filhas, u.codigo));
      }
    } else {
      lista.push(...organizarUnidadesParaExibicao([u], u.codigo));
    }
  }
  return lista;
});

const unidadesExibidas = computed((): UnidadeExibida[] => {
  return filtrarUnidadesRecursivo(unidadesExibidasOriginais.value, termoBusca.value) as UnidadeExibida[];
});

// Watchers
watch(termoBusca, (novoTermo) => {
  if (novoTermo) {
    expandirRecursivo(unidadesExibidas.value);
  }
});

watch(() => props.unidades, (newUnidades) => {
  const selecaoFiltrada = filtrarSelecaoPorElegibilidade(unidadesSelecionadasLocal.value);
  if (selecaoFiltrada.length !== unidadesSelecionadasLocal.value.length) {
    unidadesSelecionadasLocal.value = selecaoFiltrada;
  }

  if (newUnidades && newUnidades.length > 0) {
    limparExpansao();
  }
}, {immediate: true});

defineExpose({
  getEstadoSelecao,
  isChecked,
  isExpanded,
  isHabilitado,
  toggle,
  toggleExpand,
  selecionarTodas,
  deselecionarTodas,
  expandedUnits,
  unidadesSelecionadasLocal
});
</script>

<style scoped>
.arvore-unidades .form-check-input:indeterminate {
  background-color: #6c757d;
  border-color: #6c757d;
}
</style>
