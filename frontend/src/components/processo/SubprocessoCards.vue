<template>
  <BRow>
    <template v-if="tipoProcesso === TipoProcessoEnum.MAPEAMENTO || tipoProcesso === TipoProcessoEnum.REVISAO">
      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            v-if="podeEditarCadastroFinal"
            class="h-100 card-actionable"
            data-testid="card-subprocesso-atividades"
            role="button"
            tabindex="0"
            @click="navegarPara('SubprocessoCadastro')"
            @keydown="handleKeyDown($event, 'SubprocessoCadastro')"
        >
          <div class="card-click-area">
            <BCardTitle>
              {{ TEXTOS.subprocesso.cards.ATUALIZACAO_CADASTRO_TITULO }}
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.ATUALIZACAO_CADASTRO_TEXTO }}
            </BCardText>
          </div>
        </BCard>
        <BCard
            v-else
            class="h-100 card-actionable"
            data-testid="card-subprocesso-atividades-vis"
            role="button"
            tabindex="0"
            @click="navegarPara('SubprocessoVisCadastro')"
            @keydown="handleKeyDown($event, 'SubprocessoVisCadastro')"
        >
          <div class="card-click-area">
            <BCardTitle>
              {{ TEXTOS.subprocesso.cards.ATUALIZACAO_CADASTRO_TITULO }}
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.VISUALIZACAO_CADASTRO_TEXTO }}
            </BCardText>
          </div>
        </BCard>
      </BCol>

      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            v-if="podeEditarMapaFinal"
            class="h-100 card-actionable"
            data-testid="card-subprocesso-mapa-edicao"
            role="button"
            tabindex="0"
            @click="navegarPara('SubprocessoMapa')"
            @keydown="handleKeyDown($event, 'SubprocessoMapa')"
        >
          <div class="card-click-area">
            <BCardTitle>
              {{ TEXTOS.subprocesso.cards.MAPA_TITULO }}
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.MAPA_TEXTO }}
            </BCardText>
          </div>
        </BCard>
        <BCard
            v-else
            :class="['h-100', mapaHabilitado ? 'card-actionable' : 'card-disabled']"
            :data-testid="mapaHabilitado ? 'card-subprocesso-mapa-visualizacao' : 'card-subprocesso-mapa-desabilitado'"
            :role="mapaHabilitado ? 'button' : undefined"
            :tabindex="mapaHabilitado ? 0 : undefined"
            @click="mapaHabilitado && navegarPara('SubprocessoVisMapa')"
            @keydown="mapaHabilitado && handleKeyDown($event, 'SubprocessoVisMapa')"
        >
          <div class="card-click-area">
            <BCardTitle :class="mapaHabilitado ? undefined : 'text-muted'">
              {{ TEXTOS.subprocesso.cards.MAPA_TITULO }}
            </BCardTitle>
            <BCardText class="text-muted">
              {{ mapaHabilitado ? TEXTOS.subprocesso.cards.MAPA_VISUALIZACAO_TEXTO : TEXTOS.subprocesso.cards.MAPA_TEXTO }}
            </BCardText>
          </div>
        </BCard>
      </BCol>
    </template>

    <template v-else-if="tipoProcesso === TipoProcessoEnum.DIAGNOSTICO">
      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            class="h-100 card-actionable"
            data-testid="card-subprocesso-diagnostico"
            role="button"
            tabindex="0"
            @click="navegarParaDiag('AutoavaliacaoDiagnostico')"
            @keydown="handleKeyDown($event, 'AutoavaliacaoDiagnostico', true)"
        >
          <div class="card-click-area">
            <BCardTitle>
              {{ TEXTOS.subprocesso.cards.AUTOAVALIACAO_TITULO }}
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.AUTOAVALIACAO_TEXTO }}
            </BCardText>
          </div>
        </BCard>
      </BCol>

      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            class="h-100 card-actionable"
            data-testid="card-subprocesso-ocupacoes"
            role="button"
            tabindex="0"
            @click="navegarParaDiag('OcupacoesCriticasDiagnostico')"
            @keydown="handleKeyDown($event, 'OcupacoesCriticasDiagnostico', true)"
        >
          <div class="card-click-area">
            <BCardTitle>
              {{ TEXTOS.subprocesso.cards.OCUPACOES_TITULO }}
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.OCUPACOES_TEXTO }}
            </BCardText>
          </div>
        </BCard>
      </BCol>
      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            class="h-100 card-actionable"
            data-testid="card-subprocesso-monitoramento"
            role="button"
            tabindex="0"
            @click="navegarParaDiag('MonitoramentoDiagnostico')"
            @keydown="handleKeyDown($event, 'MonitoramentoDiagnostico', true)"
        >
          <div class="card-click-area">
            <BCardTitle>
              {{ TEXTOS.subprocesso.cards.MONITORAMENTO_TITULO }}
            </BCardTitle>
            <BCardText class="text-muted">
              {{ TEXTOS.subprocesso.cards.MONITORAMENTO_TEXTO }}
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
import {useAcesso} from "@/composables/useAcesso";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useProcessosStore} from "@/stores/processos";
import {SituacaoProcesso, TipoProcesso, type Mapa, type MapaCompleto} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

const TipoProcessoEnum = TipoProcesso;

const props = defineProps<{
  tipoProcesso: TipoProcesso;
  mapa: Mapa | MapaCompleto | null;
  situacao?: string;
  codSubprocesso: number;
  codProcesso: number;
  siglaUnidade: string;
}>();

const router = useRouter();
const subprocessosStore = useSubprocessosStore();
const processosStore = useProcessosStore();

const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);

const isProcessoFinalizado = computed(() => processosStore.processoDetalhe?.situacao === SituacaoProcesso.FINALIZADO);

const {podeEditarCadastro, podeEditarMapa, habilitarAcessoMapa} = useAcesso(subprocesso);

const podeEditarCadastroFinal = computed(() => podeEditarCadastro.value && !isProcessoFinalizado.value);
const podeEditarMapaFinal = computed(() => podeEditarMapa.value && !isProcessoFinalizado.value);
const mapaHabilitado = computed(() => habilitarAcessoMapa.value);

function navegarPara(routeName: string) {
  router.push({
    name: routeName,
    params: {
      codProcesso: props.codProcesso,
      siglaUnidade: props.siglaUnidade
    }
  });
}

function navegarParaDiag(routeName: string) {
  router.push({
    name: routeName,
    params: {
      codSubprocesso: props.codSubprocesso,
      siglaUnidade: props.siglaUnidade
    }
  });
}

function handleKeyDown(event: KeyboardEvent, routeName: string, diag = false) {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    if (diag) {
      navegarParaDiag(routeName);
    } else {
      navegarPara(routeName);
    }
  }
}

defineExpose({
  TipoProcessoEnum,
  navegarPara,
  navegarParaDiag,
  handleKeyDown
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
