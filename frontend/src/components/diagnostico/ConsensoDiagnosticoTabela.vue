<template>
  <BCard class="mb-4">
    <div class="table-responsive">
      <table class="table table-sm table-hover align-middle mb-0 tabela-consenso">
        <colgroup>
          <col class="coluna-competencia"/>
          <col class="coluna-nota"/>
          <col class="coluna-nota"/>
          <col class="coluna-nota"/>
          <col class="coluna-nota"/>
          <col class="coluna-nota"/>
          <col class="coluna-nota"/>
        </colgroup>
        <thead>
        <tr>
          <th class="coluna-competencia" rowspan="2">{{ TEXTOS.diagnostico.COLUNA_COMPETENCIA }}</th>
          <th class="text-center grupo grupo-servidor divisor-grupo" colspan="3">
            {{ TEXTOS.diagnostico.COLUNA_IMPORTANCIA }}
          </th>
          <th class="text-center grupo grupo-chefia divisor-grupo" colspan="3">{{
              TEXTOS.diagnostico.COLUNA_DOMINIO
            }}
          </th>
        </tr>
        <tr>
          <th class="text-center subcoluna grupo-servidor divisor-grupo">{{ TEXTOS.diagnostico.COLUNA_SERVIDOR }}</th>
          <th class="text-center subcoluna grupo-servidor">{{ TEXTOS.diagnostico.COLUNA_CHEFE }}</th>
          <th class="text-center subcoluna grupo-servidor">{{ TEXTOS.diagnostico.COLUNA_CONSENSO }}</th>
          <th class="text-center subcoluna grupo-chefia divisor-grupo">{{ TEXTOS.diagnostico.COLUNA_SERVIDOR }}</th>
          <th class="text-center subcoluna grupo-chefia">{{ TEXTOS.diagnostico.COLUNA_CHEFE }}</th>
          <th class="text-center subcoluna grupo-chefia">{{ TEXTOS.diagnostico.COLUNA_CONSENSO }}</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="item in competencias"
            :key="item.competenciaCodigo"
        >
          <td class="celula-competencia">{{ item.descricao }}</td>
          <td class="text-center grupo-servidor divisor-grupo coluna-nota">
            <span class="valor-estatico valor-estatico-bloco">{{ formatarNota(item.servidorImportancia) }}</span>
          </td>
          <td class="text-center grupo-servidor coluna-nota">
            <BFormSelect
                v-if="permiteEdicao"
                :data-testid="`consenso-chefia-importancia-${item.competenciaCodigo}`"
                :model-value="item.chefiaImportancia"
                :options="opcoesNota"
                :aria-label="`Importância da chefia para ${item.descricao}`"
                class="form-select-sm seletor-nota"
                @update:model-value="emitirAtualizacao(item.competenciaCodigo, {origem: 'chefia', campo: 'importancia', valor: $event})"
            />
            <span v-else class="valor-estatico">{{ formatarNota(item.chefiaImportancia) }}</span>
          </td>
          <td class="text-center grupo-consenso celula-consenso coluna-nota">
            <BFormSelect
                v-if="permiteEdicao"
                :data-testid="`consenso-final-importancia-${item.competenciaCodigo}`"
                :disabled="!campoConsensoHabilitado(item, 'importancia')"
                :model-value="item.consensoImportancia"
                :options="opcoesNota"
                :aria-label="`Importância do consenso para ${item.descricao}`"
                class="form-select-sm seletor-nota seletor-consenso"
                @update:model-value="emitirAtualizacao(item.competenciaCodigo, {origem: 'consenso', campo: 'importancia', valor: $event})"
            />
            <span v-else class="valor-estatico valor-consenso">{{ formatarNota(item.consensoImportancia) }}</span>
          </td>
          <td class="text-center grupo-chefia divisor-grupo coluna-nota">
            <span class="valor-estatico valor-estatico-bloco">{{ formatarNota(item.servidorDominio) }}</span>
          </td>
          <td class="text-center grupo-chefia coluna-nota">
            <BFormSelect
                v-if="permiteEdicao"
                :data-testid="`consenso-chefia-dominio-${item.competenciaCodigo}`"
                :model-value="item.chefiaDominio"
                :options="opcoesNota"
                :aria-label="`Domínio da chefia para ${item.descricao}`"
                class="form-select-sm seletor-nota"
                @update:model-value="emitirAtualizacao(item.competenciaCodigo, {origem: 'chefia', campo: 'dominio', valor: $event})"
            />
            <span v-else class="valor-estatico">{{ formatarNota(item.chefiaDominio) }}</span>
          </td>
          <td class="text-center grupo-consenso celula-consenso coluna-nota">
            <BFormSelect
                v-if="permiteEdicao"
                :data-testid="`consenso-final-dominio-${item.competenciaCodigo}`"
                :disabled="!campoConsensoHabilitado(item, 'dominio')"
                :model-value="item.consensoDominio"
                :options="opcoesNota"
                :aria-label="`Domínio do consenso para ${item.descricao}`"
                class="form-select-sm seletor-nota seletor-consenso"
                @update:model-value="emitirAtualizacao(item.competenciaCodigo, {origem: 'consenso', campo: 'dominio', valor: $event})"
            />
            <span v-else class="valor-estatico valor-consenso">{{ formatarNota(item.consensoDominio) }}</span>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </BCard>
</template>

<script lang="ts" setup>
import {computed} from 'vue';
import {BCard, BFormSelect} from 'bootstrap-vue-next';
import {TEXTOS} from '@/constants/textos';
import type {ConsensoCompetenciaDetalhada} from '@/types/diagnostico-competencias';

type CompetenciaDetalhadaComDescricao = ConsensoCompetenciaDetalhada & {
  descricao: string;
};

type AtualizacaoNota = {
  origem: 'chefia' | 'consenso';
  campo: 'importancia' | 'dominio';
  valor: unknown;
};

const props = defineProps<{
  competencias: CompetenciaDetalhadaComDescricao[];
  ehConsensoAprovado: boolean;
  habilitarConcluirAvaliacao: boolean;
  podeEditar: boolean;
}>();

const emit = defineEmits<{
  (e: 'atualizarNota', competenciaCodigo: number, atualizacao: {
    origem: 'chefia' | 'consenso';
    campo: 'importancia' | 'dominio';
    valor: number | null
  }): void;
}>();

const permiteEdicao = computed(() =>
    props.podeEditar && props.habilitarConcluirAvaliacao && !props.ehConsensoAprovado,
);

const opcoesNota = [
  {value: null, text: '-'},
  {value: 0, text: 'NA'},
  {value: 1, text: '1'},
  {value: 2, text: '2'},
  {value: 3, text: '3'},
  {value: 4, text: '4'},
  {value: 5, text: '5'},
  {value: 6, text: '6'},
];

function formatarNota(valor: number | null): string {
  if (valor === null) return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
  if (valor === 0) return TEXTOS.diagnostico.NOTA_NA;
  return String(valor);
}

function normalizarValorNota(valor: unknown): number | null {
  if (valor === null || valor === undefined || valor === '') return null;
  if (typeof valor === 'number') return Number.isNaN(valor) ? null : valor;
  const numero = Number(valor);
  return Number.isNaN(numero) ? null : numero;
}

function campoConsensoHabilitado(
    item: ConsensoCompetenciaDetalhada,
    campo: 'importancia' | 'dominio',
): boolean {
  if (campo === 'importancia') {
    return item.servidorImportancia !== null && item.chefiaImportancia !== null;
  }
  return item.servidorDominio !== null && item.chefiaDominio !== null;
}

function emitirAtualizacao(competenciaCodigo: number, atualizacao: AtualizacaoNota) {
  emit('atualizarNota', competenciaCodigo, {
    ...atualizacao,
    valor: normalizarValorNota(atualizacao.valor),
  });
}
</script>

<style scoped>
.tabela-consenso {
  width: 100%;
  min-width: 60rem;
}

.tabela-consenso col.coluna-competencia {
  width: 32%;
}

.tabela-consenso th,
.tabela-consenso td {
  vertical-align: middle;
  padding: 0.6rem 0.5rem;
}

.coluna-nota {
  width: 5.66rem;
  min-width: 5.66rem;
}

.celula-competencia {
  white-space: normal;
}

.grupo {
  font-size: 0.84rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.divisor-grupo {
  border-left: 2px solid var(--bs-border-color, #dee2e6);
}

.seletor-nota {
  display: block;
  width: 100%;
  min-width: 0;
}

.seletor-consenso:disabled {
  cursor: not-allowed;
}

.valor-estatico {
  font-weight: 600;
}

.valor-estatico-bloco {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: calc(1.5em + 0.5rem + 2px);
  font-weight: 600;
}

.valor-consenso {
  color: var(--bs-success-text-emphasis, #146c43);
}

.grupo-consenso,
.celula-consenso {
  font-weight: 600;
}
</style>
