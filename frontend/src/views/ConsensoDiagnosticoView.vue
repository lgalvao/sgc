<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <!-- Cabeçalho -->
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            {{ TEXTOS.diagnostico.TITULO_CONSENSO }}
          </h1>
          <div class="text-muted small">
            <strong>{{ nomeServidorSubtitulo }}</strong>
          </div>
        </div>
        <BButton size="sm" variant="outline-secondary" @click="void router.back()">
          <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
          {{ TEXTOS.diagnostico.BTN_VOLTAR }}
        </BButton>
      </div>

      <!-- Alertas -->
      <AppAlert
          v-if="erroMensagem"
          :mensagem="erroMensagem"
          variante="danger"
          @dismissed="erroMensagem = ''"
      />

      <!-- Badge de autosave -->
      <div v-if="ehChefe" class="mb-3 text-muted small d-flex align-items-center gap-2">
        <template v-if="salvandoAutomaticamente">
          <BSpinner small variant="secondary"/>
          {{ TEXTOS.diagnostico.LABEL_SALVANDO }}
        </template>
      </div>

      <!-- Aviso de consenso já aprovado -->
      <BAlert
          v-if="ehConsensoAprovado"
          :model-value="true"
          class="mb-4"
          variant="success"
      >
        <i aria-hidden="true" class="bi bi-check-circle me-2"/>
        A avaliação de consenso deste servidor já foi aprovada. Apenas visualização.
      </BAlert>

      <!-- Tabela de competências com consenso (CDU-44) -->
      <BCard class="mb-4">
        <div
            v-if="ehChefe"
            class="table-responsive"
        >
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
              <th class="text-center grupo grupo-servidor divisor-grupo" colspan="2">Servidor</th>
              <th class="text-center grupo grupo-chefia divisor-grupo" colspan="2">Chefia</th>
              <th class="text-center grupo grupo-consenso divisor-grupo" colspan="2">Consenso</th>
            </tr>
            <tr>
              <th class="text-center subcoluna grupo-servidor divisor-grupo">{{ TEXTOS.diagnostico.COLUNA_IMPORTANCIA }}</th>
              <th class="text-center subcoluna grupo-servidor">{{ TEXTOS.diagnostico.COLUNA_DOMINIO }}</th>
              <th class="text-center subcoluna grupo-chefia divisor-grupo">{{ TEXTOS.diagnostico.COLUNA_IMPORTANCIA }}</th>
              <th class="text-center subcoluna grupo-chefia">{{ TEXTOS.diagnostico.COLUNA_DOMINIO }}</th>
              <th class="text-center subcoluna grupo-consenso divisor-grupo">{{ TEXTOS.diagnostico.COLUNA_IMPORTANCIA }}</th>
              <th class="text-center subcoluna grupo-consenso">{{ TEXTOS.diagnostico.COLUNA_DOMINIO }}</th>
            </tr>
            </thead>
            <tbody>
            <tr
                v-for="item in competenciasDetalhadasComDescricao"
                :key="item.competenciaCodigo"
            >
              <td class="celula-competencia">{{ item.descricao }}</td>
              <td class="text-center grupo-servidor divisor-grupo coluna-nota">
                <span class="valor-estatico valor-estatico-bloco">{{ formatarNota(item.autoimportancia) }}</span>
              </td>
              <td class="text-center grupo-servidor coluna-nota">
                <span class="valor-estatico valor-estatico-bloco">{{ formatarNota(item.autodominio) }}</span>
              </td>
              <td class="text-center grupo-chefia divisor-grupo coluna-nota">
                <BFormSelect
                    v-if="!ehConsensoAprovado"
                    :data-testid="`consenso-chefia-importancia-${item.competenciaCodigo}`"
                    :model-value="item.chefiaImportancia"
                    :options="opcoesNota"
                    class="form-select-sm seletor-nota"
                    @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'chefia', campo: 'importancia', valor: normalizarValorNota(v)})"
                />
                <span v-else class="valor-estatico">{{ formatarNota(item.chefiaImportancia) }}</span>
              </td>
              <td class="text-center grupo-chefia coluna-nota">
                <BFormSelect
                    v-if="!ehConsensoAprovado"
                    :data-testid="`consenso-chefia-dominio-${item.competenciaCodigo}`"
                    :model-value="item.chefiaDominio"
                    :options="opcoesNota"
                    class="form-select-sm seletor-nota"
                    @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'chefia', campo: 'dominio', valor: normalizarValorNota(v)})"
                />
                <span v-else class="valor-estatico">{{ formatarNota(item.chefiaDominio) }}</span>
              </td>
              <td class="text-center grupo-consenso divisor-grupo celula-consenso coluna-nota">
                <BFormSelect
                    v-if="!ehConsensoAprovado"
                    :data-testid="`consenso-final-importancia-${item.competenciaCodigo}`"
                    :model-value="item.consensoImportancia"
                    :options="opcoesNota"
                    class="form-select-sm seletor-nota seletor-consenso"
                    @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'consenso', campo: 'importancia', valor: normalizarValorNota(v)})"
                />
                <span v-else class="valor-estatico valor-consenso">{{ formatarNota(item.consensoImportancia) }}</span>
              </td>
              <td class="text-center grupo-consenso celula-consenso coluna-nota">
                <BFormSelect
                    v-if="!ehConsensoAprovado"
                    :data-testid="`consenso-final-dominio-${item.competenciaCodigo}`"
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
        <BTable
            v-else
            :fields="colunasServidor"
            :items="competenciasSimplesComDescricao"
            hover
            responsive
            small
        >
          <template #cell(importancia)="{ item }">
            <span>{{ formatarNota(item.importancia) }}</span>
          </template>
          <template #cell(dominio)="{ item }">
            <span>{{ formatarNota(item.dominio) }}</span>
          </template>
        </BTable>
      </BCard>

      <!-- Ação: Aprovar consenso (servidor logado, CDU-45) -->
      <div v-if="!ehChefe && !ehConsensoAprovado" class="d-flex gap-2 flex-wrap">
        <BButton
            :disabled="aprovando"
            data-testid="btn-aprovar-consenso"
            variant="primary"
            @click="confirmarAprovarConsenso"
        >
          <BSpinner v-if="aprovando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_APROVAR_CONSENSO }}
        </BButton>
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {
  BAlert,
  BButton,
  BCard,
  BFormSelect,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {useConsensoDiagnostico} from '@/composables/useConsensoDiagnostico';
import {TEXTOS} from '@/constants/textos';
import {usePerfilStore} from '@/stores/perfil';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
  servidorTitulo: string;
}>();

const router = useRouter();
const perfilStore = usePerfilStore();
const servidorEhUsuarioLogado = computed(() =>
  String(props.servidorTitulo) === String(perfilStore.usuarioCodigo ?? ''),
);

const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);
const {podeCriarConsenso} = useDiagnosticoPermissoes(props.codSubprocesso);
const {
  competenciasLocais,
  competenciasDetalhadasLocais,
  ehConsensoAprovado,
  carregando,
  salvandoAutomaticamente,
  aprovando,
  erroAprovar,
  atualizarNotaDetalhada,
  aprovarConsenso,
} = useConsensoDiagnostico(props.codSubprocesso, props.servidorTitulo);

// « Perfil »
const ehChefe = computed(() => podeCriarConsenso.value);
const nomeServidorSubtitulo = computed(() =>
  servidorEhUsuarioLogado.value
    ? (perfilStore.usuarioNome ?? props.servidorTitulo)
    : props.servidorTitulo,
);

// « Alertas »
const erroMensagem = ref('');

async function confirmarAprovarConsenso() {
  try {
    await aprovarConsenso();
    void router.back();
  } catch {
    erroMensagem.value = erroAprovar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}


function formatarNota(valor: number | null): string {
  if (valor === null) return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
  if (valor === 0) return TEXTOS.diagnostico.NOTA_NA;
  return String(valor);
}

const competenciasDetalhadasComDescricao = computed(() => {
  const mapaDesc = Object.fromEntries(
    (contexto.value?.competencias ?? []).map((c) => [c.competenciaCodigo, c.descricao]),
  );
  const origem = competenciasDetalhadasLocais.value.length > 0
    ? competenciasDetalhadasLocais.value
    : competenciasLocais.value.map((c) => ({
      competenciaCodigo: c.competenciaCodigo,
      autoimportancia: c.importancia,
      autodominio: c.dominio,
      chefiaImportancia: c.importancia,
      chefiaDominio: c.dominio,
      consensoImportancia: c.importancia,
      consensoDominio: c.dominio,
    }));
  return origem.map((c) => ({
    ...c,
    descricao: mapaDesc[c.competenciaCodigo] ?? `Competência ${c.competenciaCodigo}`,
  }));
});

const competenciasSimplesComDescricao = computed(() => {
  const mapaDesc = Object.fromEntries(
    (contexto.value?.competencias ?? []).map((c) => [c.competenciaCodigo, c.descricao]),
  );
  return competenciasLocais.value.map((c) => ({
    ...c,
    descricao: mapaDesc[c.competenciaCodigo] ?? `Competência ${c.competenciaCodigo}`,
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

const colunasServidor = [
  {key: 'descricao', label: TEXTOS.diagnostico.COLUNA_COMPETENCIA},
  {key: 'importancia', label: TEXTOS.diagnostico.COLUNA_IMPORTANCIA},
  {key: 'dominio', label: TEXTOS.diagnostico.COLUNA_DOMINIO},
];

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
