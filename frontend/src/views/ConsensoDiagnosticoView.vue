<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <!-- Cabeçalho -->
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            <i aria-hidden="true" class="bi bi-person-lines-fill text-primary me-2"/>
            {{ TEXTOS.diagnostico.TITULO_CONSENSO }}
          </h1>
          <div class="text-muted small">
            Servidor: <strong>{{ servidorTitulo }}</strong>
            <BBadge :variant="varianteSituacao" class="ms-2">
              {{ formatarSituacaoServidor(situacaoServidor) }}
            </BBadge>
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
      <AppAlert
          v-if="alertaSucesso"
          :mensagem="alertaSucesso"
          variante="success"
          @dismissed="alertaSucesso = ''"
      />

      <!-- Aviso de consenso já aprovado -->
      <BAlert
          v-if="ehConsensoAprovado"
          :model-value="true"
          class="mb-4"
          variant="success"
      >
        <i aria-hidden="true" class="bi bi-check-circle me-2"/>
        O consenso deste servidor já foi aprovado. Apenas visualização.
      </BAlert>

      <!-- Tabela de competências com consenso -->
      <BCard class="mb-4">
        <BCardHeader>
          <strong>{{ TEXTOS.diagnostico.TITULO_CONSENSO }}</strong>
          <span class="text-muted small ms-2">{{ TEXTOS.diagnostico.ESCALA_HINT }}</span>
        </BCardHeader>
        <BTable
            :fields="colunas"
            :items="competenciasDetalhadasComDescricao"
            hover
            responsive
            small
            striped
        >
          <template #cell(autoimportancia)="{ item }">
            <span>{{ formatarNota(item.autoimportancia) }}</span>
          </template>
          <template #cell(autodominio)="{ item }">
            <span>{{ formatarNota(item.autodominio) }}</span>
          </template>
          <template #cell(chefiaImportancia)="{ item }">
            <BFormSelect
                v-if="ehChefe && !ehConsensoAprovado"
                :data-testid="`consenso-chefia-importancia-${item.competenciaCodigo}`"
                :model-value="item.chefiaImportancia"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'chefia', campo: 'importancia', valor: v as number | null})"
            />
            <span v-else>{{ formatarNota(item.chefiaImportancia) }}</span>
          </template>
          <template #cell(chefiaDominio)="{ item }">
            <BFormSelect
                v-if="ehChefe && !ehConsensoAprovado"
                :data-testid="`consenso-chefia-dominio-${item.competenciaCodigo}`"
                :model-value="item.chefiaDominio"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'chefia', campo: 'dominio', valor: v as number | null})"
            />
            <span v-else>{{ formatarNota(item.chefiaDominio) }}</span>
          </template>
          <template #cell(consensoImportancia)="{ item }">
            <BFormSelect
                v-if="ehChefe && !ehConsensoAprovado"
                :data-testid="`consenso-final-importancia-${item.competenciaCodigo}`"
                :model-value="item.consensoImportancia"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'consenso', campo: 'importancia', valor: v as number | null})"
            />
            <span v-else>{{ formatarNota(item.consensoImportancia) }}</span>
          </template>
          <template #cell(consensoDominio)="{ item }">
            <BFormSelect
                v-if="ehChefe && !ehConsensoAprovado"
                :data-testid="`consenso-final-dominio-${item.competenciaCodigo}`"
                :model-value="item.consensoDominio"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNotaDetalhada(item.competenciaCodigo, {origem: 'consenso', campo: 'dominio', valor: v as number | null})"
            />
            <span v-else>{{ formatarNota(item.consensoDominio) }}</span>
          </template>
        </BTable>
      </BCard>

      <!-- Campo de motivo de reabertura (quando chefia reabre consenso já aprovado) -->
      <BCard v-if="ehChefe && precisaMotivo" class="mb-4">
        <BCardHeader>
          <strong>{{ TEXTOS.diagnostico.MODAL_MOTIVO_REABERTURA_TITULO }}</strong>
        </BCardHeader>
        <BCardBody>
          <BFormTextarea
              v-model="motivoReabertura"
              :placeholder="TEXTOS.diagnostico.MODAL_MOTIVO_REABERTURA_PLACEHOLDER"
              data-testid="textarea-motivo-reabertura"
              rows="3"
          />
        </BCardBody>
      </BCard>

      <!-- Ações -->
      <div class="d-flex gap-2 flex-wrap">
        <!-- Chefia: salvar consenso -->
        <BButton
            v-if="ehChefe && !ehConsensoAprovado"
            :disabled="salvando"
            data-testid="btn-salvar-consenso"
            variant="primary"
            @click="confirmarSalvarConsenso"
        >
          <BSpinner v-if="salvando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_SALVAR_CONSENSO }}
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
  BBadge,
  BButton,
  BCard,
  BCardBody,
  BCardHeader,
  BFormSelect,
  BFormTextarea,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {useConsensoDiagnostico} from '@/composables/useConsensoDiagnostico';
import {usePerfilStore} from '@/stores/perfil';
import {Perfil} from '@/types/tipos';
import {TEXTOS} from '@/constants/textos';
import type {SituacaoAvaliacaoServidor} from '@/types/diagnostico-competencias';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
  servidorTitulo: string;
}>();

const router = useRouter();
const perfilStore = usePerfilStore();

const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);
const {
  competenciasLocais,
  competenciasDetalhadasLocais,
  motivoReabertura,
  situacaoServidor,
  carregando,
  salvando,
  erroSalvar,
  atualizarNotaDetalhada,
  salvarConsenso,
} = useConsensoDiagnostico(props.codSubprocesso, props.servidorTitulo);

// ── Perfil ───────────────────────────────────────────────────────────────────
const ehChefe = computed(
  () =>
    perfilStore.perfilSelecionado === Perfil.CHEFE ||
    perfilStore.perfilSelecionado === Perfil.GESTOR ||
    perfilStore.perfilSelecionado === Perfil.ADMIN,
);

const ehConsensoAprovado = computed(() => situacaoServidor.value === 'CONSENSO_APROVADO');
const precisaMotivo = computed(() => ehConsensoAprovado.value && ehChefe.value);

// ── Alertas ───────────────────────────────────────────────────────────────────
const erroMensagem = ref('');
const alertaSucesso = ref('');

async function confirmarSalvarConsenso() {
  try {
    await salvarConsenso(props.servidorTitulo, motivoReabertura.value || undefined);
    alertaSucesso.value = TEXTOS.diagnostico.SUCESSO_CONSENSO_SALVO;
    void router.back();
  } catch {
    erroMensagem.value = erroSalvar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR;
  }
}

// ── Formatação ────────────────────────────────────────────────────────────────
const varianteSituacao = computed(() => {
  switch (situacaoServidor.value) {
    case 'CONSENSO_APROVADO':
      return 'success';
    case 'CONSENSO_CRIADO':
      return 'warning';
    default:
      return 'secondary';
  }
});

function formatarSituacaoServidor(situacao: SituacaoAvaliacaoServidor): string {
  const mapa: Record<SituacaoAvaliacaoServidor, string> = {
    AUTOAVALIACAO_NAO_REALIZADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
    AUTOAVALIACAO_CONCLUIDA: TEXTOS.diagnostico.SITUACAO_AUTOAVALIACAO_CONCLUIDA,
    CONSENSO_CRIADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_CRIADO,
    CONSENSO_APROVADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_APROVADO,
    AVALIACAO_IMPOSSIBILITADA: TEXTOS.diagnostico.SITUACAO_IMPOSSIBILITADA,
  };
  return mapa[situacao] ?? situacao;
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

const opcoesNota = [
  {value: 0, text: 'NA'},
  {value: 1, text: '1'},
  {value: 2, text: '2'},
  {value: 3, text: '3'},
  {value: 4, text: '4'},
  {value: 5, text: '5'},
  {value: 6, text: '6'},
];

const colunas = [
  {key: 'descricao', label: TEXTOS.diagnostico.COLUNA_COMPETENCIA},
  {key: 'autoimportancia', label: 'Servidor: Importância'},
  {key: 'autodominio', label: 'Servidor: Domínio'},
  {key: 'chefiaImportancia', label: 'Chefia: Importância'},
  {key: 'chefiaDominio', label: 'Chefia: Domínio'},
  {key: 'consensoImportancia', label: 'Consenso: Importância'},
  {key: 'consensoDominio', label: 'Consenso: Domínio'},
];
</script>
