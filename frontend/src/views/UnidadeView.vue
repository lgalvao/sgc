<template>
  <LayoutPadrao>
    <BAlert
        v-if="!carregandoPagina && ultimoErro"
        :model-value="true"
        dismissible
        variant="danger"
        @dismissed="limparErro()"
    >
      {{ ultimoErro }}
    </BAlert>

    <CarregamentoPagina v-if="carregandoPagina" :mensagem="TEXTOS.unidade.CARREGANDO"/>
    <template v-else>
      <div v-if="unidade">
        <PageHeader
            :subtitle="unidade.nome"
            :title="unidade.sigla"
            actions-test-id="unidade-view__acoes"
            title-test-id="unidade-view__titulo"
        >
          <template #actions>
            <BDropdown
                v-if="podeExportarMapaVigente"
                text="Mapa vigente"
                data-testid="btn-exportar-mapa-vigente"
                toggle-class="text-nowrap"
                variant="outline-secondary"
            >
              <BDropdownItemButton
                  :disabled="loadingExportacaoPdf"
                  data-testid="btn-exportar-mapa-vigente-pdf"
                  @click="exportarMapaVigentePdf"
              >
                PDF
              </BDropdownItemButton>
              <BDropdownItemButton
                  :disabled="loadingExportacaoCsv"
                  data-testid="btn-exportar-mapa-vigente-csv"
                  @click="exportarMapaVigenteCsv"
              >
                {{ TEXTOS_RELATORIOS.BOTAO_CSV }}
              </BDropdownItemButton>
            </BDropdown>
            <BButton
                v-if="mostrarCriarAtribuicaoTemporaria"
                data-testid="unidade-view__btn-criar-atribuicao"
                variant="outline-secondary"
                @click="irParaCriarAtribuicao"
            >
              <span data-testid="unidade-view__btn-atribuicao-texto">{{ textoBotaoAtribuicao }}</span>
            </BButton>
          </template>
        </PageHeader>

        <BCard class="mb-4" no-body>
          <BCardBody>
            <UnidadeContatoInfo
                v-if="titularExibivel"
                :contato="unidade.titular"
                :label="TEXTOS.unidade.LABEL_TITULAR"
                :nome-fallback="TEXTOS.unidade.NAO_INFORMADO"
                data-testid="unidade-titular-info"
                detalhes-class="ms-3 mb-2"
            />
            <UnidadeContatoInfo
                v-if="responsavelExibivel"
                :contato="responsavelExibivel"
                :descricao="descricaoContatoPrincipal"
                :label="labelContatoPrincipal"
                data-testid="unidade-responsavel-info"
            />
          </BCardBody>
        </BCard>
      </div>
      <EmptyState
          v-else
          :description="TEXTOS.unidade.EMPTY_DESCRIPTION"
          :title="TEXTOS.unidade.EMPTY_TITLE"
          icon="bi-building"
      />

      <div
          v-if="temSubordinadas"
          class="mt-5"
      >
        <TreeTable
            :columns="colunasTabela"
            :data="dadosFormatadosSubordinadas"
            :hide-headers="true"
            :title="TEXTOS.unidade.SUBORDINADAS_TITULO"
            @row-click="navegarParaUnidadeSubordinada"
        />
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCard, BCardBody, BDropdown, BDropdownItemButton} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed, onActivated, ref, watch} from "vue";
import {useRouter} from "vue-router";
import type {MapaVigenteReferencia, Responsavel, Unidade, Usuario} from "@/types/tipos";
import TreeTable, {type TreeItem} from "@/components/comum/TreeTable.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import UnidadeContatoInfo from "@/components/unidade/UnidadeContatoInfo.vue";
import {useUnidadeStore} from "@/stores/unidade";
import {usePerfil} from "@/composables/usePerfil";
import {useUnidadeAtual} from "@/composables/useUnidadeAtual";
import {formatarDataBR, logger} from "@/utils";
import {normalizarErro} from "@/utils/apiError";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {useNotification} from "@/composables/useNotification";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil} from "@/types/comum";
import {relatoriosService} from "@/services/relatoriosService";


const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const {mostrarCriarAtribuicaoTemporaria} = usePerfil();
const {notify} = useNotification();
const {definirUnidadeAtual} = useUnidadeAtual();
const unidadeStore = useUnidadeStore();
const perfilStore = usePerfilStore();

const unidade = ref<Unidade | null>(null);
const mapaVigente = ref<MapaVigenteReferencia | null>(null);
const ultimoErro = ref<string | null>(null);
const carregandoPagina = ref(true);
const carregamentoInicialConcluido = ref(false);
const loadingExportacaoPdf = ref(false);
const loadingExportacaoCsv = ref(false);
let carregamentoEmAndamento: Promise<void> | null = null;

function limparErro() {
  ultimoErro.value = null;
}

function possuiDadosLocaisValidos(): boolean {
  const unidadeEmCache = unidadeStore.cacheUnidades.get(props.codUnidade);
  const mapaVigenteEmCache = unidadeStore.cacheMapasVigentes.get(props.codUnidade);

  return unidade.value?.codigo === props.codUnidade
      && unidadeStore.cacheUnidades.has(props.codUnidade)
      && unidadeStore.cacheMapasVigentes.has(props.codUnidade)
      && unidade.value === unidadeEmCache
      && mapaVigente.value === mapaVigenteEmCache
      && !ultimoErro.value;
}

function reaplicarDadosDoCache(): boolean {
  if (!unidadeStore.cacheUnidades.has(props.codUnidade) || !unidadeStore.cacheMapasVigentes.has(props.codUnidade)) {
    return false;
  }

  unidade.value = unidadeStore.cacheUnidades.get(props.codUnidade) ?? null;
  definirUnidadeAtual(unidade.value);
  mapaVigente.value = unidadeStore.cacheMapasVigentes.get(props.codUnidade) ?? null;
  carregandoPagina.value = false;
  return true;
}

function deveExibirCarregamento(forcar: boolean): boolean {
  return forcar || !unidadeStore.cacheUnidades.has(props.codUnidade);
}

async function carregarDados(forcar = false) {
  if (carregamentoEmAndamento) {
    await carregamentoEmAndamento;
    return;
  }

  limparErro();

  const tarefaCarregamento = (async () => {
    if (deveExibirCarregamento(forcar)) {
      carregandoPagina.value = true;
      unidade.value = null;
      mapaVigente.value = null;
    }

    try {
      const [unidadeResp, mapaResp] = await Promise.all([
        unidadeStore.obterUnidade(props.codUnidade, forcar),
        unidadeStore.obterReferenciaMapaVigente(props.codUnidade, forcar),
      ]);

      unidade.value = unidadeResp;
      definirUnidadeAtual(unidade.value);
      mapaVigente.value = mapaResp;
    } catch (error: unknown) {
      ultimoErro.value = normalizarErro(error).mensagem || TEXTOS.unidade.ERRO_CARREGAR;
      logger.error("Erro ao carregar dados da unidade:", error);
    } finally {
      carregandoPagina.value = false;
    }
  })();

  carregamentoEmAndamento = tarefaCarregamento;
  try {
    await tarefaCarregamento;
  } finally {
    if (carregamentoEmAndamento === tarefaCarregamento) {
      carregamentoEmAndamento = null;
    }
  }
}

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${props.codUnidade}/atribuicao`});
}

function navegarParaUnidadeSubordinada(row: TreeItem) {
  router.push({path: `/unidade/${row.codigo}`});
}

async function exportarMapaVigentePdf() {
  if (!mapaVigente.value) {
    return;
  }

  loadingExportacaoPdf.value = true;
  try {
    await relatoriosService.downloadRelatorioMapaVigenteUnidadePdf(props.codUnidade);
  } catch {
    notify(TEXTOS_RELATORIOS.ERRO_EXPORTAR, "danger");
  } finally {
    loadingExportacaoPdf.value = false;
  }
}

async function exportarMapaVigenteCsv() {
  if (!mapaVigente.value) {
    return;
  }

  loadingExportacaoCsv.value = true;
  try {
    await relatoriosService.downloadRelatorioMapaVigenteUnidadeCsv(props.codUnidade);
  } catch {
    notify(TEXTOS_RELATORIOS.ERRO_EXPORTAR_CSV, "danger");
  } finally {
    loadingExportacaoCsv.value = false;
  }
}

onActivated(async () => {
  if (!carregamentoInicialConcluido.value) {
    return;
  }
  if (possuiDadosLocaisValidos()) {
    return;
  }
  if (reaplicarDadosDoCache()) {
    return;
  }
  await carregarDados();
});
watch(
    () => props.codUnidade,
    async () => {
      await carregarDados();
      carregamentoInicialConcluido.value = true;
    },
    {immediate: true}
);

const colunasTabela = [{key: "nome", label: TEXTOS.unidade.CAMPO_UNIDADE}];
const subordinadas = computed(() => unidade.value?.filhas ?? []);
const temSubordinadas = computed(() => subordinadas.value.length > 0);
const podeExportarMapaVigente = computed(() => perfilStore.perfilSelecionado === Perfil.CHEFE && Boolean(mapaVigente.value));

const dadosFormatadosSubordinadas = computed(() => formatarDadosParaArvore(subordinadas.value));

const responsavelExibivel = computed<Usuario | Responsavel | null>(() => {
  return unidade.value?.responsavel ?? unidade.value?.titular ?? null;
});

const responsavelEhTitular = computed(() => {
  const titular = unidade.value?.titular;
  const responsavel = responsavelExibivel.value;
  if (!titular || !responsavel) {
    return false;
  }
  return titular.tituloEleitoral === responsavel.tituloEleitoral;
});

const titularExibivel = computed(() => {
  const titular = unidade.value?.titular;
  if (!titular || !responsavelExibivel.value) {
    return Boolean(titular);
  }
  return !responsavelEhTitular.value;
});

const textoBotaoAtribuicao = computed(() =>
    unidade.value?.tipoResponsabilidade === "ATRIBUICAO_TEMPORARIA"
        ? TEXTOS.unidade.BOTAO_EDITAR_ATRIBUICAO
        : TEXTOS.unidade.BOTAO_CRIAR_ATRIBUICAO
);

const descricaoResponsabilidade = computed(() => {
  const tipoResponsabilidade = unidade.value?.tipoResponsabilidade ?? "TITULAR";
  const dataFim = unidade.value?.dataFimResponsabilidade;

  if (tipoResponsabilidade === "SUBSTITUTO") {
    return dataFim
        ? `Substituição (até ${formatarDataBR(dataFim)})`
        : "Substituição";
  }

  if (tipoResponsabilidade === "ATRIBUICAO_TEMPORARIA") {
    return dataFim
        ? `Atrib. temporária (até ${formatarDataBR(dataFim)})`
        : "Atrib. temporária";
  }

  return "Titular";
});

const labelContatoPrincipal = computed(() =>
    responsavelEhTitular.value
        ? TEXTOS.unidade.LABEL_TITULAR
        : TEXTOS.unidade.LABEL_RESPONSAVEL
);

const descricaoContatoPrincipal = computed(() =>
    responsavelEhTitular.value
        ? ""
        : descricaoResponsabilidade.value
);

interface UnidadeFormatada {
  codigo: number;
  nome: string;
  expanded: boolean;
  children?: UnidadeFormatada[];

  [key: string]: unknown;
}

function formatarDadosParaArvore(dados: Unidade[]): UnidadeFormatada[] {
  return dados.map((item) => {
    const children = formatarDadosParaArvore(item.filhas ?? []);
    return {
      codigo: item.codigo,
      nome: item.nome,
      sigla: item.sigla,
      expanded: false,
      ...(children.length > 0 && {children})
    };
  });
}
</script>
