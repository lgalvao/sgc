<template>
  <LayoutPadrao>
    <ErrorAlert
        :error="unidadesStore.lastError"
        @dismiss="unidadesStore.clearError()"
    />

    <div v-if="unidadeComResponsavelDinamico">
      <PageHeader :title="`${unidadeComResponsavelDinamico.sigla} - ${unidadeComResponsavelDinamico.nome}`">
        <template #actions>
          <BButton
              v-if="mapaVigente"
              data-testid="btn-mapa-vigente"
              variant="outline-success"
              @click="visualizarMapa"
          >
            <i
                class="bi bi-file-earmark-spreadsheet me-2"
            />Mapa vigente
          </BButton>
          <BButton
              v-if="perfilStore.perfilSelecionado === 'ADMIN'"
              class="ms-2"
              data-testid="unidade-view__btn-criar-atribuicao"
              variant="outline-primary"
              @click="irParaCriarAtribuicao"
          >
            Criar atribuição
          </BButton>
        </template>
      </PageHeader>

      <UnidadeInfoCard
          :titular-detalhes="titularDetalhes"
          :unidade="unidadeComResponsavelDinamico"
      />
    </div>
    <EmptyState
        v-else
        description="Não foi possível localizar os dados da unidade solicitada."
        icon="bi-building"
        title="Unidade não encontrada"
    />

    <div
        v-if="unidadeComResponsavelDinamico && unidadeComResponsavelDinamico.filhas && unidadeComResponsavelDinamico.filhas.length > 0"
        class="mt-5"
    >
      <TreeTable
          :columns="colunasTabela"
          :data="dadosFormatadosSubordinadas"
          :hide-headers="true"
          title="Unidades Subordinadas"
          @row-click="navegarParaUnidadeSubordinada"
      />
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import type {Unidade, Usuario} from "@/types/tipos";
import TreeTable from "@/components/comum/TreeTable.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import UnidadeInfoCard from "@/components/unidade/UnidadeInfoCard.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useUnidadesStore} from "@/stores/unidades";
import {useAtribuicaoTemporariaStore} from "@/stores/atribuicoes";
import {usePerfilStore} from "@/stores/perfil";
import {useMapasStore} from "@/stores/mapas";
import {buscarUsuarioPorTitulo} from "@/services/usuarioService";
import {logger} from "@/utils";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const unidadesStore = useUnidadesStore();
const atribuicaoStore = useAtribuicaoTemporariaStore();
const perfilStore = usePerfilStore();
const mapasStore = useMapasStore();

const titularDetalhes = ref<Usuario | null>(null);

const unidade = computed(() => unidadesStore.unidade);
const mapaVigente = computed(() => mapasStore.mapaCompleto);

const unidadeComResponsavelDinamico = computed(() => {
  if (!unidade.value) return null;

  const atribuicoes = atribuicaoStore.obterAtribuicoesPorUnidade(unidade.value.sigla);
  const agora = new Date();
  const atribuicaoAtiva = atribuicoes.find(a => {
    const inicio = new Date(a.dataInicio);
    let fim = null;
    if (a.dataTermino) {
      fim = new Date(a.dataTermino);
    } else if (a.dataFim) {
      fim = new Date(a.dataFim);
    }
    return inicio <= agora && (!fim || fim >= agora);
  });

  return {
    ...unidade.value,
    responsavel: atribuicaoAtiva ? atribuicaoAtiva.usuario : (unidade.value.responsavel || null)
  };
});

async function carregarDados() {
  try {
    await Promise.all([
      unidadesStore.buscarArvoreUnidade(props.codUnidade),
      atribuicaoStore.buscarAtribuicoes()
    ]);

    if (unidade.value?.tituloTitular) {
      titularDetalhes.value = await buscarUsuarioPorTitulo(unidade.value.tituloTitular);
    }
  } catch (error) {
    logger.error("Erro ao buscar titular:", error);
  }
}

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${props.codUnidade}/atribuicao`});
}

function navegarParaUnidadeSubordinada(row: any) {
  router.push({path: `/unidade/${row.codigo}`});
}

function visualizarMapa() {
  if (mapaVigente.value) {
    router.push({
      name: "SubprocessoVisMapa",
      params: {
        codProcesso: mapaVigente.value.subprocessoCodigo,
        siglaUnidade: unidade.value?.sigla
      }
    });
  }
}

onMounted(carregarDados);
watch(() => props.codUnidade, carregarDados);

const colunasTabela = [{key: "nome", label: "Unidade"}];

const dadosFormatadosSubordinadas = computed(() => {
  if (
      !unidadeComResponsavelDinamico.value ||
      !unidadeComResponsavelDinamico.value.filhas ||
      unidadeComResponsavelDinamico.value.filhas.length === 0
  )
    return [];
  return formatarDadosParaArvore(unidadeComResponsavelDinamico.value.filhas);
});

interface UnidadeFormatada {
  codigo: number;
  nome: string;
  expanded: boolean;
  children?: UnidadeFormatada[];
}

function formatarDadosParaArvore(dados: Unidade[]): UnidadeFormatada[] {
  if (!dados) return [];

  return dados.map((item) => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas) : [];
    return {
      codigo: item.codigo,
      nome: item.sigla + " - " + item.nome,
      expanded: true,
      ...(children.length > 0 && {children})
    };
  });
}
</script>
