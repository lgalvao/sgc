<template>
  <LayoutPadrao>
    <BAlert
        v-if="lastError"
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

    <div v-if="unidade">
      <PageHeader :title="`${unidade.sigla} - ${unidade.nome}`">
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
              v-if="isAdmin"
              data-testid="unidade-view__btn-criar-atribuicao"
              variant="outline-secondary"
              @click="irParaCriarAtribuicao"
          >
            {{ TEXTOS.unidade.BOTAO_CRIAR_ATRIBUICAO }}
          </BButton>
        </template>
      </PageHeader>

      <BCard class="mb-4 shadow-sm" no-body>
        <BCardBody>
          <BRow>
            <BCol md="6">
              <div data-testid="unidade-titular-info">
                <h5 class="mb-1">
                  {{ TEXTOS.unidade.LABEL_TITULAR }} {{ titularDetalhes ? titularDetalhes.nome : TEXTOS.unidade.NAO_INFORMADO }}
                </h5>
                <div v-if="titularDetalhes" class="d-flex flex-column">
                  <span v-if="titularDetalhes.ramal">
                    {{ TEXTOS.unidade.LABEL_RAMAL }} <a
                      :aria-label="`Ligar para ${titularDetalhes.ramal}`"
                      :href="`tel:${titularDetalhes.ramal}`"
                    >{{ titularDetalhes.ramal }}</a>
                  </span>
                  <span v-if="titularDetalhes.email">
                    {{ TEXTOS.unidade.LABEL_EMAIL }} <a
                      :aria-label="`Enviar e-mail para ${titularDetalhes.email}`"
                      :href="`mailto:${titularDetalhes.email}`"
                    >{{ titularDetalhes.email }}</a>
                  </span>
                </div>
              </div>
            </BCol>
            <BCol class="border-start" md="6">
              <div data-testid="unidade-responsavel-info">
                <h5 class="mb-1">
                  {{ TEXTOS.unidade.LABEL_RESPONSAVEL }} {{ unidade.responsavel ? unidade.responsavel.nome : TEXTOS.unidade.NAO_INFORMADO }}
                </h5>
                <div v-if="unidade.responsavel" class="d-flex flex-column">
                  <span v-if="unidade.responsavel.ramal">
                    {{ TEXTOS.unidade.LABEL_RAMAL }} <a
                      :aria-label="`Ligar para ${unidade.responsavel.ramal}`"
                      :href="`tel:${unidade.responsavel.ramal}`"
                    >{{ unidade.responsavel.ramal }}</a>
                  </span>
                  <span v-if="unidade.responsavel.email">
                    {{ TEXTOS.unidade.LABEL_EMAIL }} <a
                      :aria-label="`Enviar e-mail para ${unidade.responsavel.email}`"
                      :href="`mailto:${unidade.responsavel.email}`"
                    >{{ unidade.responsavel.email }}</a>
                  </span>
                </div>
              </div>
            </BCol>
          </BRow>
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
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCard, BCardBody, BCol, BRow} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import type {Unidade, Usuario} from "@/types/tipos";
import TreeTable from "@/components/comum/TreeTable.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {buscarArvoreUnidade as buscarArvoreUnidadeServico} from "@/services/unidadeService";
import {usePerfil} from "@/composables/usePerfil";
import {useUnidadeAtual} from "@/composables/useUnidadeAtual";
import {useMapasStore} from "@/stores/mapas";
import {buscarUsuarioPorTitulo} from "@/services/usuarioService";
import {logger} from "@/utils";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const {isAdmin} = usePerfil();
const mapasStore = useMapasStore();
const {definirUnidadeAtual} = useUnidadeAtual();

const unidade = ref<Unidade | null>(null);
const titularDetalhes = ref<Usuario | null>(null);
const lastError = ref<{message: string; details?: string} | null>(null);

function clearError() {
  lastError.value = null;
}

const mapaVigente = computed(() => mapasStore.mapaCompleto);

async function carregarDados() {
  try {
    const response = await buscarArvoreUnidadeServico(props.codUnidade);
    unidade.value = response as Unidade;
    definirUnidadeAtual(unidade.value);

    if (unidade.value?.tituloTitular) {
      titularDetalhes.value = await buscarUsuarioPorTitulo(unidade.value.tituloTitular);
    }
  } catch (error: any) {
    lastError.value = {message: error.message || TEXTOS.unidade.ERRO_CARREGAR};
    logger.error("Erro ao carregar dados da unidade:", error);
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

const colunasTabela = [{key: "nome", label: TEXTOS.unidade.CAMPO_UNIDADE}];

const dadosFormatadosSubordinadas = computed(() => {
  if (!unidade.value || !unidade.value.filhas || unidade.value.filhas.length === 0)
    return [];
  return formatarDadosParaArvore(unidade.value.filhas);
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
