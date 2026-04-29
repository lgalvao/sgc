<template>
  <LayoutPadrao>
    <BAlert
        v-if="!carregandoPagina && lastError"
        :model-value="true"
        variant="danger"
        dismissible
        @dismissed="clearError()"
    >
      {{ lastError.message }}
      <div v-if="lastError.details">
        <small>Detalhes: {{ lastError.details }}</small>
      </div>
    </BAlert>

    <CarregamentoPagina v-if="carregandoPagina" :mensagem="TEXTOS.unidade.CARREGANDO" />
    <template v-else>
      <div v-if="unidade">
        <PageHeader :subtitle="unidade.nome" :title="unidade.sigla">
          <template #actions>
            <BButton
                v-if="mapaVigente"
                data-testid="btn-mapa-vigente"
                variant="outline-secondary"
                @click="visualizarMapa"
            >
              <i
                  class="bi bi-file-earmark-spreadsheet me-2"
              />{{ TEXTOS.unidade.BOTAO_MAPA_VIGENTE }}
            </BButton>
            <BButton
                v-if="mostrarCriarAtribuicaoTemporaria"
                data-testid="unidade-view__btn-criar-atribuicao"
                variant="outline-secondary"
                @click="irParaCriarAtribuicao"
            >
              {{ TEXTOS.unidade.BOTAO_CRIAR_ATRIBUICAO }}
            </BButton>
          </template>
        </PageHeader>

        <BCard class="mb-4" no-body>
          <BCardBody>
            <p class="mt-2" data-testid="unidade-titular-info">
              <strong>{{ TEXTOS.unidade.LABEL_TITULAR }}</strong> {{ titularDetalhes ? titularDetalhes.nome : TEXTOS.unidade.NAO_INFORMADO }}
            </p>
            <p v-if="titularDetalhes" class="ms-3 mb-2">
              <span v-if="titularDetalhes.ramal" class="me-3">
                <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
                {{ titularDetalhes.ramal }}
              </span>
              <span v-if="titularDetalhes.email">
                <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
                <a
                    :aria-label="`Enviar e-mail para ${titularDetalhes.email}`"
                    :href="`mailto:${titularDetalhes.email}`"
                >{{ titularDetalhes.email }}</a>
              </span>
            </p>
            <template v-if="responsavelExibivel">
              <p class="mt-2" data-testid="unidade-responsavel-info">
                <strong>{{ TEXTOS.unidade.LABEL_RESPONSAVEL }}</strong> {{ responsavelExibivel.nome }}
              </p>
              <p class="ms-3 mb-0">
                <span v-if="responsavelExibivel.ramal" class="me-3">
                  <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
                  {{ responsavelExibivel.ramal }}
                </span>
                <span v-if="responsavelExibivel.email">
                  <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
                  <a
                      :aria-label="`Enviar e-mail para ${responsavelExibivel.email}`"
                      :href="`mailto:${responsavelExibivel.email}`"
                  >{{ responsavelExibivel.email }}</a>
                </span>
              </p>
            </template>
          </BCardBody>
        </BCard>
      </div>
      <EmptyState
          v-else
          :description="TEXTOS.unidade.EMPTY_DESCRIPTION"
          icon="bi-building"
          :title="TEXTOS.unidade.EMPTY_TITLE"
      />

      <div
          v-if="unidade && unidade.filhas && unidade.filhas.length > 0"
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
import {BAlert, BButton, BCard, BCardBody} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed, onActivated, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import type {MapaVigenteReferencia, Unidade, Usuario} from "@/types/tipos";
import TreeTable, {type TreeItem} from "@/components/comum/TreeTable.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import {
  buscarArvoreUnidade as buscarArvoreUnidadeServico,
  buscarReferenciaMapaVigente as buscarReferenciaMapaVigenteServico
} from "@/services/unidadeService";
import {usePerfil} from "@/composables/usePerfil";
import {useUnidadeAtual} from "@/composables/useUnidadeAtual";
import {buscarUsuarioPorTitulo} from "@/services/usuarioService";
import {logger} from "@/utils";
import {TEXTOS} from "@/constants/textos";
import {Error} from "storybook/internal/components";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const {mostrarCriarAtribuicaoTemporaria} = usePerfil();
const {definirUnidadeAtual} = useUnidadeAtual();

const unidade = ref<Unidade | null>(null);
const titularDetalhes = ref<Usuario | null>(null);
const mapaVigente = ref<MapaVigenteReferencia | null>(null);
const lastError = ref<{message: string; details?: string} | null>(null);
const carregandoPagina = ref(true);
const carregamentoInicialConcluido = ref(false);

function clearError() {
  lastError.value = null;
}

async function carregarDados() {
  carregandoPagina.value = true;
  clearError();
  unidade.value = null;
  titularDetalhes.value = null;
  mapaVigente.value = null;
  try {
    const [unidadeResp, mapaResp] = await Promise.all([
      buscarArvoreUnidadeServico(props.codUnidade),
      buscarReferenciaMapaVigenteServico(props.codUnidade),
    ]);
    unidade.value = unidadeResp as Unidade;
    definirUnidadeAtual(unidade.value);
    mapaVigente.value = mapaResp;

    if (unidade.value?.tituloTitular) {
      titularDetalhes.value = await buscarUsuarioPorTitulo(unidade.value.tituloTitular);
    }
  } catch (error: unknown) {
    const err = error as Error;
    lastError.value = {message: err.message || TEXTOS.unidade.ERRO_CARREGAR};
    logger.error("Erro ao carregar dados da unidade:", error);
  } finally {
    carregandoPagina.value = false;
  }
}

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${props.codUnidade}/atribuicao`});
}

function navegarParaUnidadeSubordinada(row: TreeItem) {
  router.push({path: `/unidade/${row.codigo}`});
}

function visualizarMapa() {
  if (mapaVigente.value) {
    router.push({
      name: "SubprocessoMapa",
      params: {
        codProcesso: mapaVigente.value.codProcesso,
        siglaUnidade: unidade.value?.sigla
      }
    });
  }
}

onMounted(async () => {
  await carregarDados();
  carregamentoInicialConcluido.value = true;
});
onActivated(async () => {
  if (!carregamentoInicialConcluido.value) {
    return;
  }
  await carregarDados();
});
watch(() => props.codUnidade, () => {
  void carregarDados();
});

const colunasTabela = [{key: "nome", label: TEXTOS.unidade.CAMPO_UNIDADE}];

const dadosFormatadosSubordinadas = computed(() => {
  if (!unidade.value || !unidade.value.filhas || unidade.value.filhas.length === 0)
    return [];
  return formatarDadosParaArvore(unidade.value.filhas);
});

const responsavelExibivel = computed(() => {
  const responsavel = unidade.value?.responsavel;
  if (!responsavel || responsavelEhTitular(responsavel, titularDetalhes.value, unidade.value?.tituloTitular)) {
    return null;
  }
  return responsavel;
});

interface UnidadeFormatada {
  codigo: number;
  nome: string;
  expanded: boolean;
  children?: UnidadeFormatada[];
  [key: string]: unknown;
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

function responsavelEhTitular(responsavel: Usuario, titular: Usuario | null, tituloTitular?: string): boolean {
  if (responsavel.tituloEleitoral && tituloTitular && responsavel.tituloEleitoral === tituloTitular) {
    return true;
  }
  if (responsavel.tituloEleitoral && titular?.tituloEleitoral) {
    return responsavel.tituloEleitoral === titular.tituloEleitoral;
  }
  return Boolean(responsavel.nome && titular?.nome && responsavel.nome === titular.nome);
}
</script>
