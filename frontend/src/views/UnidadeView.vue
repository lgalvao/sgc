<template>
  <LayoutPadrao>
    <BAlert
        v-if="unidadesStore.lastError"
        :model-value="true"
        variant="danger"
        dismissible
        @dismissed="unidadesStore.clearError()"
    >
      {{ unidadesStore.lastError.message }}
      <div v-if="unidadesStore.lastError.details">
        <small>Detalhes: {{ unidadesStore.lastError.details }}</small>
      </div>
    </BAlert>

    <div v-if="unidade">
      <PageHeader :title="`${unidade.sigla} - ${unidade.nome}`">
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
              v-if="isAdmin"
              class="ms-2"
              data-testid="unidade-view__btn-criar-atribuicao"
              variant="outline-primary"
              @click="irParaCriarAtribuicao"
          >
            Criar atribuição
          </BButton>
        </template>
      </PageHeader>

      <BCard class="mb-4 shadow-sm" no-body>
        <BCardBody>
          <BRow>
            <BCol md="6">
              <div data-testid="unidade-titular-info">
                <h5 class="mb-1">
                  Titular: {{ titularDetalhes ? titularDetalhes.nome : 'Não informado' }}
                </h5>
                <div v-if="titularDetalhes" class="d-flex flex-column">
                  <span v-if="titularDetalhes.ramal">
                    Ramal: <a
                      :aria-label="`Ligar para ${titularDetalhes.ramal}`"
                      :href="`tel:${titularDetalhes.ramal}`"
                    >{{ titularDetalhes.ramal }}</a>
                  </span>
                  <span v-if="titularDetalhes.email">
                    E-mail: <a
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
                  Responsável: {{ unidade.responsavel ? unidade.responsavel.nome : 'Não informado' }}
                </h5>
                <div v-if="unidade.responsavel" class="d-flex flex-column">
                  <span v-if="unidade.responsavel.ramal">
                    Ramal: <a
                      :aria-label="`Ligar para ${unidade.responsavel.ramal}`"
                      :href="`tel:${unidade.responsavel.ramal}`"
                    >{{ unidade.responsavel.ramal }}</a>
                  </span>
                  <span v-if="unidade.responsavel.email">
                    E-mail: <a
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
        description="Não foi possível localizar os dados da unidade solicitada."
        icon="bi-building"
        title="Unidade não encontrada"
    />

    <div
        v-if="unidade && unidade.filhas && unidade.filhas.length > 0"
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
import {BAlert, BButton, BCard, BCardBody, BCol, BRow} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import type {Unidade, Usuario} from "@/types/tipos";
import TreeTable from "@/components/comum/TreeTable.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useUnidadesStore} from "@/stores/unidades";
import {usePerfil} from "@/composables/usePerfil";
import {useMapasStore} from "@/stores/mapas";
import {buscarUsuarioPorTitulo} from "@/services/usuarioService";
import {logger} from "@/utils";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const unidadesStore = useUnidadesStore();
const {isAdmin} = usePerfil();
const mapasStore = useMapasStore();

const titularDetalhes = ref<Usuario | null>(null);

const unidade = computed(() => unidadesStore.unidade);
const mapaVigente = computed(() => mapasStore.mapaCompleto);

async function carregarDados() {
  try {
    await unidadesStore.buscarArvoreUnidade(props.codUnidade);

    if (unidade.value?.tituloTitular) {
      titularDetalhes.value = await buscarUsuarioPorTitulo(unidade.value.tituloTitular);
    }
  } catch (error) {
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

const colunasTabela = [{key: "nome", label: "Unidade"}];

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
