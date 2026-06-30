<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <div :class="{'cursor-salvando': salvandoAutomaticamente}">
      <PageHeader
          :subtitle="unidade?.unidadeSigla"
          :title="TEXTOS.diagnostico.TITULO_SITUACAO_CAPACITACAO"
      >
        <template #actions>
          <BButton variant="outline-secondary" @click="void router.back()">
            <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
            {{ TEXTOS.diagnostico.BTN_VOLTAR }}
          </BButton>
        </template>
      </PageHeader>

      <BCard class="mb-4">
        <EmptyState
            v-if="servidores.length === 0"
            :description="TEXTOS.diagnostico.VAZIO_CAPACITACAO_TEXTO"
            :title="TEXTOS.diagnostico.VAZIO_CAPACITACAO_TITULO"
            icon="bi-award"
        />

        <div v-else class="p-3">
          <div class="mb-3">
            <div class="scroll-container-servidores">
              <BListGroup data-testid="lista-servidores-situacao-capacitacao">
                <BListGroupItem
                    v-for="item in servidoresOrdenados"
                    :key="item.servidorTitulo"
                    :active="item.servidorTitulo === servidorSelecionadoTitulo"
                    button
                    class="d-flex align-items-center justify-content-between gap-3"
                    :data-testid="`btn-selecionar-servidor-situacao-capacitacao-${item.servidorTitulo}`"
                    @click="servidorSelecionadoTitulo = item.servidorTitulo"
                >
                  <div class="fw-semibold">{{ item.servidorNome }}</div>
                  <BBadge :variant="varianteSituacaoServidor(item.situacaoServidor)">
                    {{ formatarSituacaoServidor(item.situacaoServidor) }}
                  </BBadge>
                </BListGroupItem>
              </BListGroup>
            </div>
          </div>

          <template v-if="servidorSelecionado">
            <BCard
                class="mb-3 border-0 bg-body-tertiary"
                data-testid="detalhes-servidor-situacao-capacitacao"
            >
              <div class="fw-semibold text-primary-emphasis">{{ servidorSelecionado.servidorNome }}</div>
              <small class="text-muted">Título {{ servidorSelecionado.servidorTitulo }}</small>
            </BCard>

            <div v-if="possuiDadosCompetenciasServidorSelecionado">
              <div class="table-responsive scroll-container-competencias">
                <table class="table table-sm table-hover align-middle mb-0">
                  <thead class="sticky-top bg-white shadow-sm">
                  <tr>
                    <th>{{ TEXTOS.diagnostico.COLUNA_COMPETENCIA }}</th>
                    <th class="text-center">{{ TEXTOS.diagnostico.COLUNA_IMPORTANCIA }}</th>
                    <th class="text-center">{{ TEXTOS.diagnostico.COLUNA_DOMINIO }}</th>
                    <th class="coluna-capacitacao">{{ TEXTOS.diagnostico.COLUNA_CAPACITACAO }}</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr
                      v-for="linha in competenciasServidorSelecionado"
                      :key="linha.competenciaCodigo"
                  >
                    <td class="celula-competencia">
                      {{ linha.competenciaDescricao }}
                    </td>
                    <td class="text-center">
                      <BBadge pill variant="light">{{ formatarNota(linha.importancia) }}</BBadge>
                    </td>
                    <td class="text-center">
                      <BBadge pill variant="light">{{ formatarNota(linha.dominio) }}</BBadge>
                    </td>
                    <td class="coluna-capacitacao">
                      <BFormSelect
                          v-if="habilitarCriarConsenso"
                          :data-testid="`situacao-${servidorSelecionadoTitulo}-${linha.competenciaCodigo}`"
                          :model-value="linha.situacaoCapacitacao"
                          :options="opcoesCapacitacao"
                          :aria-label="`Situação de capacitação para ${linha.competenciaDescricao}`"
                          class="form-select-sm seletor-capacitacao"
                          @update:model-value="(v: unknown) => atualizarCapacitacao(servidorSelecionadoTitulo, linha.competenciaCodigo, v as ValorSituacaoCapacitacao)"
                      />
                      <span
                          v-else
                          :data-testid="`situacao-${servidorSelecionadoTitulo}-${linha.competenciaCodigo}`"
                          class="texto-situacao-capacitacao"
                      >
                        {{ formatarSituacaoCapacitacao(linha.situacaoCapacitacao) }}
                      </span>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <EmptyState
                v-else
                :description="TEXTOS.diagnostico.VAZIO_CAPACITACAO_SERVIDOR_TEXTO"
                :title="TEXTOS.diagnostico.VAZIO_CAPACITACAO_SERVIDOR_TITULO"
                class="my-4"
                icon="bi-hourglass-split"
            />
          </template>

          <EmptyState
              v-else
              :description="TEXTOS.diagnostico.VAZIO_CAPACITACAO_SELECAO_TEXTO"
              :title="TEXTOS.diagnostico.VAZIO_CAPACITACAO_SELECAO_TITULO"
              class="my-4"
              icon="bi-person-check"
          />
        </div>
      </BCard>
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {useRouter} from 'vue-router';
import {BBadge, BButton, BCard, BFormSelect, BListGroup, BListGroupItem,} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {useSituacaoCapacitacaoDiagnostico} from '@/composables/useSituacaoCapacitacaoDiagnostico';
import {TEXTOS} from '@/constants/textos';
import type {SituacaoAvaliacaoServidor, ValorSituacaoCapacitacao} from '@/types/diagnostico-competencias';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();

const router = useRouter();
const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);

const {
  situacoesLocais,
  unidade,
  servidores,
  carregando,
  salvandoAutomaticamente,
  habilitarCriarConsenso,
  atualizarCapacitacao,
} = useSituacaoCapacitacaoDiagnostico(props.codSubprocesso);

const servidorSelecionadoTitulo = ref('');

const servidoresOrdenados = computed(() => {
  return [...servidores.value].sort((a, b) => {
    if (a.situacaoServidor === 'CONSENSO_APROVADO' && b.situacaoServidor !== 'CONSENSO_APROVADO') {
      return -1;
    }
    if (a.situacaoServidor !== 'CONSENSO_APROVADO' && b.situacaoServidor === 'CONSENSO_APROVADO') {
      return 1;
    }
    return 0;
  });
});

watch(servidoresOrdenados, (novosServidores) => {
  if (!servidorSelecionadoTitulo.value) {
    return;
  }
  const existeAtual = novosServidores.some((item) => item.servidorTitulo === servidorSelecionadoTitulo.value);
  if (!existeAtual) {
    servidorSelecionadoTitulo.value = '';
  }
}, {immediate: true});

const servidorSelecionado = computed(() =>
  servidores.value.find((item) => item.servidorTitulo === servidorSelecionadoTitulo.value) ?? null,
);

const situacoesLocaisPorChave = computed(() =>
  new Map(
    situacoesLocais.value.map((item) => [`${item.competenciaCodigo}-${item.servidorTitulo}`, item.situacaoCapacitacao]),
  ),
);

const consensoServidorSelecionadoPorCompetencia = computed(() =>
  new Map(
    (servidorSelecionado.value?.consenso ?? []).map((item) => [item.competenciaCodigo, item]),
  ),
);

const competenciasServidorSelecionado = computed(() => {
  return (contexto.value?.competencias ?? []).map((competencia) => {
    const consenso = consensoServidorSelecionadoPorCompetencia.value.get(competencia.competenciaCodigo);
    return {
      competenciaCodigo: competencia.competenciaCodigo,
      competenciaDescricao: competencia.descricao,
      importancia: consenso?.importancia ?? null,
      dominio: consenso?.dominio ?? null,
      situacaoCapacitacao: situacoesLocaisPorChave.value.get(`${competencia.competenciaCodigo}-${servidorSelecionadoTitulo.value}`) ?? null,
    };
  });
});

const possuiDadosCompetenciasServidorSelecionado = computed(() =>
  competenciasServidorSelecionado.value.some((item) => item.importancia !== null || item.dominio !== null),
);

// « Opções de capacitação »
const opcoesCapacitacao = [
  {value: null, text: '-'},
  {value: 'NA', text: `NA - ${TEXTOS.diagnostico.CAPACITACAO_NA}`},
  {value: 'AC', text: `AC - ${TEXTOS.diagnostico.CAPACITACAO_AC}`},
  {value: 'EC', text: `EC - ${TEXTOS.diagnostico.CAPACITACAO_EC}`},
  {value: 'C', text: `C - ${TEXTOS.diagnostico.CAPACITACAO_C}`},
  {value: 'I', text: `I - ${TEXTOS.diagnostico.CAPACITACAO_I}`},
];

function formatarSituacaoCapacitacao(valor: ValorSituacaoCapacitacao | null): string {
  const opcao = opcoesCapacitacao.find((o) => o.value === valor);
  return opcao ? opcao.text : '-';
}

function varianteSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor) {
  switch (situacaoServidor) {
    case 'CONSENSO_APROVADO':
      return 'success';
    case 'AVALIACAO_IMPOSSIBILITADA':
      return 'secondary';
    case 'CONSENSO_CRIADO':
      return 'warning';
    case 'AUTOAVALIACAO_CONCLUIDA':
      return 'info';
    default:
      return 'light';
  }
}

function formatarSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor): string {
  const mapa: Record<SituacaoAvaliacaoServidor, string> = {
    AUTOAVALIACAO_NAO_INICIADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
    AUTOAVALIACAO_CONCLUIDA: TEXTOS.diagnostico.SITUACAO_AUTOAVALIACAO_CONCLUIDA,
    CONSENSO_CRIADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_CRIADO,
    CONSENSO_APROVADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_APROVADO,
    AVALIACAO_IMPOSSIBILITADA: TEXTOS.diagnostico.SITUACAO_IMPOSSIBILITADA,
  };
  return mapa[situacaoServidor] ?? situacaoServidor;
}

function formatarNota(valor: number | null): string {
  if (valor === null) {
    return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
  }
  if (valor === 0) {
    return TEXTOS.diagnostico.NOTA_NA;
  }
  return String(valor);
}
</script>

<style scoped>
.celula-competencia {
  min-width: 16rem;
  white-space: normal;
}

.coluna-capacitacao {
  min-width: 14rem;
  width: 14rem;
}

.seletor-capacitacao {
  width: 100%;
}

.scroll-container-servidores {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--bs-border-color);
  border-radius: var(--bs-border-radius);
}

.scroll-container-competencias {
  max-height: 400px;
  overflow-y: auto;
}

.cursor-salvando,
.cursor-salvando * {
  cursor: wait !important;
}
</style>
