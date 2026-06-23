<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <div :class="{'cursor-salvando': salvandoAutomaticamente}">
      <PageHeader
          :subtitle="subtituloServidor"
          :title="TEXTOS.diagnostico.TITULO_CONSENSO"
      >
        <template #actions>
          <BButton
              v-if="podeConcluirAvaliacao"
              :disabled="concluindoAvaliacao || !habilitarConcluirAvaliacao"
              data-testid="btn-concluir-avaliacao"
              size="sm"
              variant="success"
              @click="confirmarConcluirAvaliacao"
          >
            <BSpinner v-if="concluindoAvaliacao" aria-hidden="true" class="me-1" small/>
            {{ TEXTOS.diagnostico.BTN_CONCLUIR_AVALIACAO }}
          </BButton>
          <BButton
              v-if="podeAprovarConsenso || servidorEhUsuarioLogado"
              :disabled="aprovando || !habilitarAprovarConsenso"
              data-testid="btn-aprovar-consenso"
              size="sm"
              variant="success"
              @click="confirmarAprovarConsenso"
          >
            <BSpinner v-if="aprovando" aria-hidden="true" class="me-1" small/>
            {{ TEXTOS.diagnostico.BTN_APROVAR_CONSENSO }}
          </BButton>
          <BButton size="sm" variant="outline-secondary" @click="void router.back()">
            <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
            {{ TEXTOS.diagnostico.BTN_VOLTAR }}
          </BButton>
        </template>
      </PageHeader>

      <!-- Alertas -->
      <AppAlert
          v-if="erroMensagem"
          :chave="erroMensagemChave"
          :mensagem="erroMensagem"
          variante="danger"
          @dismissed="erroMensagem = ''"
      />

      <!-- Aviso de consenso já aprovado -->
      <BAlert
          v-if="ehConsensoAprovado"
          :model-value="true"
          class="mb-4"
          variant="success"
      >
        <i aria-hidden="true" class="bi bi-check-circle me-2"/>
        A avaliação de consenso já foi aprovada.
      </BAlert>

      <!-- Tabela de competências com consenso (CDU-44) -->
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
              <th class="text-center grupo grupo-servidor divisor-grupo" colspan="3">{{ TEXTOS.diagnostico.COLUNA_IMPORTANCIA }}</th>
              <th class="text-center grupo grupo-chefia divisor-grupo" colspan="3">{{ TEXTOS.diagnostico.COLUNA_DOMINIO }}</th>
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
                v-for="item in competenciasDetalhadasComDescricao"
                :key="item.competenciaCodigo"
            >
              <td class="celula-competencia">{{ item.descricao }}</td>
              <td class="text-center grupo-servidor divisor-grupo coluna-nota">
                <span class="valor-estatico valor-estatico-bloco">{{ formatarNota(item.servidorImportancia) }}</span>
              </td>
              <td class="text-center grupo-servidor coluna-nota">
                <BFormSelect
                    v-if="podeEditar && habilitarConcluirAvaliacao && !ehConsensoAprovado"
                    :data-testid="`consenso-chefia-importancia-${item.competenciaCodigo}`"
                    :model-value="item.chefiaImportancia"
                    :options="opcoesNota"
                    class="form-select-sm seletor-nota"
                    @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'chefia', campo: 'importancia', valor: normalizarValorNota(v)})"
                />
                <span v-else class="valor-estatico">{{ formatarNota(item.chefiaImportancia) }}</span>
              </td>
              <td class="text-center grupo-consenso celula-consenso coluna-nota">
                <BFormSelect
                    v-if="podeEditar && habilitarConcluirAvaliacao && !ehConsensoAprovado"
                    :data-testid="`consenso-final-importancia-${item.competenciaCodigo}`"
                    :disabled="!campoConsensoHabilitado(item, 'importancia')"
                    :model-value="item.consensoImportancia"
                    :options="opcoesNota"
                    class="form-select-sm seletor-nota seletor-consenso"
                    @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'consenso', campo: 'importancia', valor: normalizarValorNota(v)})"
                />
                <span v-else class="valor-estatico valor-consenso">{{ formatarNota(item.consensoImportancia) }}</span>
              </td>
              <td class="text-center grupo-chefia divisor-grupo coluna-nota">
                <span class="valor-estatico valor-estatico-bloco">{{ formatarNota(item.servidorDominio) }}</span>
              </td>
              <td class="text-center grupo-chefia coluna-nota">
                <BFormSelect
                    v-if="podeEditar && habilitarConcluirAvaliacao && !ehConsensoAprovado"
                    :data-testid="`consenso-chefia-dominio-${item.competenciaCodigo}`"
                    :model-value="item.chefiaDominio"
                    :options="opcoesNota"
                    class="form-select-sm seletor-nota"
                    @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'chefia', campo: 'dominio', valor: normalizarValorNota(v)})"
                />
                <span v-else class="valor-estatico">{{ formatarNota(item.chefiaDominio) }}</span>
              </td>
              <td class="text-center grupo-consenso celula-consenso coluna-nota">
                <BFormSelect
                    v-if="podeEditar && habilitarConcluirAvaliacao && !ehConsensoAprovado"
                    :data-testid="`consenso-final-dominio-${item.competenciaCodigo}`"
                    :disabled="!campoConsensoHabilitado(item, 'dominio')"
                    :model-value="item.consensoDominio"
                    :options="opcoesNota"
                    class="form-select-sm seletor-nota seletor-consenso"
                    @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'consenso', campo: 'dominio', valor: normalizarValorNota(v)})"
                />
                <span v-else class="valor-estatico valor-consenso">{{ formatarNota(item.consensoDominio) }}</span>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </BCard>
      </div>

    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {BAlert, BButton, BCard, BFormSelect, BSpinner,} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {useConsensoDiagnostico} from '@/composables/useConsensoDiagnostico';
import {TEXTOS} from '@/constants/textos';
import {usePerfilStore} from '@/stores/perfil';
import {useToastStore} from '@/stores/toast';
import type {ConsensoCompetenciaDetalhada} from '@/types/diagnostico-competencias';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
  servidorTitulo: string;
  servidorNome?: string;
}>();

const router = useRouter();
const perfilStore = usePerfilStore();
const servidorEhUsuarioLogado = computed(() =>
  String(props.servidorTitulo) === String(perfilStore.usuarioCodigo ?? ''),
);

const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);
const {
  query,
  competenciasLocais,
  podeEditar,
  podeConcluirAvaliacao,
  habilitarConcluirAvaliacao,
  podeAprovarConsenso,
  habilitarAprovarConsenso,
  ehConsensoAprovado,
  carregando,
  salvandoAutomaticamente,
  aprovando,
  erroConcluir,
  erroAprovar,
  atualizarNotaDetalhada,
  salvarConsensoAgora,
  concluirAvaliacao,
  aprovarConsenso,
} = useConsensoDiagnostico(props.codSubprocesso, props.servidorTitulo);
const nomeServidorQuery = computed(() => query.data.value?.servidorNome ?? null);
const nomeServidorSubtitulo = computed(() =>
  servidorEhUsuarioLogado.value
    ? (perfilStore.usuarioNome ?? nomeServidorQuery.value ?? props.servidorNome ?? props.servidorTitulo)
    : (nomeServidorQuery.value ?? props.servidorNome ?? props.servidorTitulo),
);
const subtituloServidor = computed(() => `${nomeServidorSubtitulo.value} - ${props.servidorTitulo}`);

// « Alertas »
const erroMensagem = ref('');
const erroMensagemChave = ref(0);
const concluindoAvaliacao = ref(false);

function exibirErro(mensagem: string) {
  erroMensagemChave.value += 1;
  erroMensagem.value = mensagem;
}

async function confirmarAprovarConsenso() {
  try {
    await aprovarConsenso();
    const toastStore = useToastStore();
    toastStore.setPending(TEXTOS.diagnostico.SUCESSO_CONSENSO_APROVADO);
    if (contexto.value?.processoCodigo) {
      await router.push({
        name: 'Subprocesso',
        params: {
          codProcesso: String(contexto.value.processoCodigo),
          siglaUnidade: props.siglaUnidade,
        },
        query: {
          codSubprocesso: String(props.codSubprocesso),
        },
      });
      return;
    }
    void router.back();
  } catch {
    exibirErro(erroAprovar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

function consensoCompleto(item: ConsensoCompetenciaDetalhada): boolean {
  return item.chefiaImportancia !== null
    && item.chefiaDominio !== null
    && item.consensoImportancia !== null
    && item.consensoDominio !== null;
}

async function confirmarConcluirAvaliacao() {
  if (competenciasLocais.value.some((item) => !consensoCompleto(item))) {
    exibirErro(TEXTOS.diagnostico.ERRO_PREENCHIMENTO_CONSENSO_INCOMPLETO);
    return;
  }

  try {
    await salvarConsensoAgora();
    concluindoAvaliacao.value = true;
    await concluirAvaliacao();
    const toastStore = useToastStore();
    toastStore.setPending(TEXTOS.diagnostico.SUCESSO_CONSENSO_CRIADO);
    if (contexto.value?.processoCodigo) {
      await router.push({
        name: 'Subprocesso',
        params: {
          codProcesso: String(contexto.value.processoCodigo),
          siglaUnidade: props.siglaUnidade,
        },
        query: {
          codSubprocesso: String(props.codSubprocesso),
        },
      });
      return;
    }
    void router.back();
  } catch {
    exibirErro(erroConcluir.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR);
  } finally {
    concluindoAvaliacao.value = false;
  }
}


function formatarNota(valor: number | null): string {
  if (valor === null) return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
  if (valor === 0) return TEXTOS.diagnostico.NOTA_NA;
  return String(valor);
}

const competenciasDetalhadasComDescricao = computed(() => {
  return competenciasLocais.value.map((c) => ({
    ...c,
    descricao: c.competenciaDescricao,
  }));
});

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

.cursor-salvando,
.cursor-salvando * {
  cursor: wait !important;
}
</style>
