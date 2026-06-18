<template>
  <BRow>
    <template v-if="tipoProcesso === TipoProcessoEnum.MAPEAMENTO || tipoProcesso === TipoProcessoEnum.REVISAO">
      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            :class="['h-100', habilitarAcessoCadastro ? 'card-actionable' : 'card-disabled']"
            :role="habilitarAcessoCadastro ? 'button' : undefined"
            :tabindex="habilitarAcessoCadastro ? 0 : undefined"
            data-testid="card-subprocesso-atividades"
            @click="habilitarAcessoCadastro && navegarPara('SubprocessoCadastro')"
            @keydown="habilitarAcessoCadastro && aoPressionarTecla($event, 'SubprocessoCadastro')"
        >
          <div class="card-click-area">
            <BCardTitle :class="['d-flex align-items-start gap-3 mb-3', habilitarAcessoCadastro ? undefined : 'text-muted']">
              <i aria-hidden="true" class="bi bi-card-checklist text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">{{ TEXTOS.subprocesso.cards.ATUALIZACAO_CADASTRO_TITULO }}</span>
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.ATUALIZACAO_CADASTRO_TEXTO }}
            </BCardText>
          </div>
        </BCard>
      </BCol>

      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            :class="['h-100', mapaHabilitado ? 'card-actionable' : 'card-disabled']"
            :data-testid="mapaHabilitado ? 'card-subprocesso-mapa' : 'card-subprocesso-mapa-desabilitado'"
            :role="mapaHabilitado ? 'button' : undefined"
            :tabindex="mapaHabilitado ? 0 : undefined"
            @click="mapaHabilitado && navegarPara('SubprocessoMapa')"
            @keydown="mapaHabilitado && aoPressionarTecla($event, 'SubprocessoMapa')"
        >
          <div class="card-click-area">
            <BCardTitle :class="['d-flex align-items-start gap-3 mb-3', mapaHabilitado ? undefined : 'text-muted']">
              <i aria-hidden="true" class="bi bi-diagram-3 text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">{{ TEXTOS.subprocesso.cards.MAPA_TITULO }}</span>
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.MAPA_TEXTO }}
            </BCardText>
          </div>
        </BCard>
      </BCol>
    </template>

    <template v-else-if="tipoProcesso === TipoProcessoEnum.DIAGNOSTICO">
      <BCol
          v-if="permissoesDiagnostico?.podePreencherAutoavaliacao"
          class="mb-3"
          md="4"
      >
        <BCard
            class="h-100 card-actionable"
            data-testid="card-subprocesso-diagnostico"
            role="button"
            tabindex="0"
            @click="navegarParaDiag('AutoavaliacaoDiagnostico')"
            @keydown="aoPressionarTeclaDiagnostico($event, 'AutoavaliacaoDiagnostico')"
        >
          <div class="card-click-area">
            <BCardTitle class="d-flex align-items-start gap-3 mb-3">
              <i aria-hidden="true" class="bi bi-clipboard-check text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">{{ TEXTOS.subprocesso.cards.AUTOAVALIACAO_TITULO }}</span>
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.AUTOAVALIACAO_TEXTO }}
            </BCardText>
          </div>
        </BCard>
      </BCol>

      <BCol
          v-if="permissoesDiagnostico?.podePreencherAutoavaliacao && !permissoesDiagnostico?.podeCriarConsenso"
          class="mb-3"
          md="4"
      >
        <BCard
            :class="['h-100', habilitarCardConsenso ? 'card-actionable' : 'card-disabled']"
            data-testid="card-subprocesso-consenso"
            :role="habilitarCardConsenso ? 'button' : undefined"
            :tabindex="habilitarCardConsenso ? 0 : undefined"
            @click="habilitarCardConsenso && navegarParaCardConsenso()"
            @keydown="habilitarCardConsenso && aoPressionarTeclaConsenso($event)"
        >
          <div class="card-click-area">
            <BCardTitle class="d-flex align-items-start gap-3 mb-3">
              <i aria-hidden="true" class="bi bi-person-lines-fill text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">{{ TEXTOS.subprocesso.cards.CONSENSO_TITULO }}</span>
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.CONSENSO_TEXTO }}
            </BCardText>
          </div>
        </BCard>
      </BCol>
      <BCol
          v-if="permissoesDiagnostico?.podeCriarConsenso"
          class="mb-3"
          md="4"
      >
        <BCard
            :class="['h-100', habilitarCardSituacaoCapacitacao ? 'card-actionable' : 'card-disabled']"
            :data-testid="habilitarCardSituacaoCapacitacao ? 'card-subprocesso-situacoes-capacitacao' : 'card-subprocesso-situacoes-capacitacao-desabilitado'"
            :role="habilitarCardSituacaoCapacitacao ? 'button' : undefined"
            :tabindex="habilitarCardSituacaoCapacitacao ? 0 : undefined"
            @click="habilitarCardSituacaoCapacitacao && navegarParaDiag('SituacaoCapacitacaoDiagnostico')"
            @keydown="habilitarCardSituacaoCapacitacao && aoPressionarTeclaDiagnostico($event, 'SituacaoCapacitacaoDiagnostico')"
        >
          <div class="card-click-area">
            <BCardTitle :class="['d-flex align-items-start gap-3 mb-3', habilitarCardSituacaoCapacitacao ? undefined : 'text-muted']">
              <i aria-hidden="true" class="bi bi-people text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">{{ TEXTOS.subprocesso.cards.SITUACAO_CAPACITACAO_TITULO }}</span>
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.SITUACAO_CAPACITACAO_TEXTO }}
            </BCardText>
          </div>
        </BCard>
      </BCol>
    </template>
  </BRow>
</template>

<script lang="ts" setup>
import {BCard, BCardText, BCardTitle, BCol, BRow} from "bootstrap-vue-next";
import {useRouter} from "vue-router";
import {computed} from "vue";
import {useAcesso} from "@/composables/acesso";
import {usePerfilStore} from "@/stores/perfil";
import {useAutoavaliacaoDiagnostico} from "@/composables/useAutoavaliacaoDiagnostico";
import {useDiagnosticoUnidade} from "@/composables/useDiagnosticoUnidade";
import {type Mapa, type MapaCompleto, type SubprocessoDetalhe, TipoProcesso} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

const TipoProcessoEnum = TipoProcesso;

const props = defineProps<{
  tipoProcesso: TipoProcesso;
  mapa: Mapa | MapaCompleto | null;
  situacao?: string;
  codSubprocesso: number;
  codProcesso: number;
  siglaUnidade: string;
  subprocesso?: SubprocessoDetalhe | null;
}>();

const router = useRouter();
const perfilStore = usePerfilStore();
const subprocesso = computed(() => props.subprocesso ?? null);
const permissoesDiagnostico = computed(() => subprocesso.value?.permissoes ?? null);

const {habilitarAcessoCadastro, habilitarAcessoMapa} = useAcesso(subprocesso);
const mapaHabilitado = computed(() => habilitarAcessoMapa.value);

const { situacaoServidor } = props.tipoProcesso === TipoProcessoEnum.DIAGNOSTICO
  && props.subprocesso?.permissoes?.podePreencherAutoavaliacao
  ? useAutoavaliacaoDiagnostico(props.codSubprocesso)
  : { situacaoServidor: computed(() => 'AUTOAVALIACAO_NAO_INICIADA') };
const habilitarCardConsenso = computed(() => {
  return situacaoServidor.value === 'CONSENSO_CRIADO' || situacaoServidor.value === 'CONSENSO_APROVADO';
});

const {servidores} = props.tipoProcesso === TipoProcessoEnum.DIAGNOSTICO
  && !!props.subprocesso?.permissoes?.podeCriarConsenso
  ? useDiagnosticoUnidade(props.codSubprocesso)
  : {servidores: computed(() => [])};

const habilitarCardSituacaoCapacitacao = computed(() => {
  return servidores.value.some((servidor) => servidor.situacaoServidor === 'CONSENSO_APROVADO');
});

function navegarPara(routeName: string) {
  void router.push({
    name: routeName,
    params: {
      codProcesso: props.codProcesso,
      siglaUnidade: props.siglaUnidade,
    },
    query: {
      codSubprocesso: String(props.codSubprocesso),
    },
  });
}

function navegarParaDiag(routeName: string) {
  void router.push({
    name: routeName,
    params: {
      codSubprocesso: props.codSubprocesso,
      siglaUnidade: props.siglaUnidade
    }
  });
}

function navegarParaConsenso() {
  const servidorTitulo = perfilStore.usuarioCodigo ?? subprocesso.value?.titular?.tituloEleitoral;
  const servidorNome = perfilStore.usuarioNome ?? subprocesso.value?.titular?.nome;
  if (!servidorTitulo) {
    return;
  }
  void router.push({
    name: 'ConsensoDiagnostico',
    params: {
      codSubprocesso: props.codSubprocesso,
      siglaUnidade: props.siglaUnidade,
      servidorTitulo,
    },
    query: servidorNome ? {servidorNome} : undefined,
  });
}

function navegarParaCardConsenso() {
  navegarParaConsenso();
}

function aoPressionarTecla(event: KeyboardEvent, routeName: string) {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    navegarPara(routeName);
  }
}

function aoPressionarTeclaDiagnostico(event: KeyboardEvent, routeName: string) {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    navegarParaDiag(routeName);
  }
}

function aoPressionarTeclaConsenso(event: KeyboardEvent) {
  if (event.key !== 'Enter' && event.key !== ' ') {
    return;
  }
  event.preventDefault();
  navegarParaCardConsenso();
}

defineExpose({
  TipoProcessoEnum,
  navegarPara,
  navegarParaDiag,
  navegarParaConsenso,
  navegarParaCardConsenso,
  aoPressionarTecla,
  aoPressionarTeclaDiagnostico,
  aoPressionarTeclaConsenso,
});
</script>

<style scoped>
.card-actionable {
  cursor: pointer;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.card-actionable:hover {
  transform: translateY(-5px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.card-actionable:focus-visible {
  outline: 2px solid var(--bs-primary);
  outline-offset: 2px;
}

.card-click-area {
  height: 100%;
}

.card-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
