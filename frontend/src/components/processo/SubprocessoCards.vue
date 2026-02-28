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
              Atividades e conhecimentos
            </BCardTitle>
            <BCardText class="text-muted">
              Cadastro de atividades e conhecimentos da unidade
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
              Atividades e conhecimentos
            </BCardTitle>
            <BCardText class="text-muted">
              Visualização das atividades e conhecimentos da unidade
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
            :aria-disabled="!mapa"
            :class="{ 'disabled-card': !mapa }"
            :tabindex="!mapa ? -1 : 0"
            class="h-100 card-actionable"
            data-testid="card-subprocesso-mapa-edicao"
            role="button"
            @click="!mapa ? null : navegarPara('SubprocessoMapa')"
            @keydown="handleKeyDown($event, 'SubprocessoMapa')"
        >
          <div class="card-click-area">
            <BCardTitle>
              Mapa de Competências
            </BCardTitle>
            <BCardText class="text-muted">
              Mapa de competências técnicas da unidade
            </BCardText>
          </div>
        </BCard>
        <BCard
            v-else
            :aria-disabled="!mapa"
            :class="{ 'disabled-card': !mapa }"
            :tabindex="!mapa ? -1 : 0"
            class="h-100 card-actionable"
            data-testid="card-subprocesso-mapa-visualizacao"
            role="button"
            @click="!mapa ? null : navegarPara('SubprocessoVisMapa')"
            @keydown="handleKeyDown($event, 'SubprocessoVisMapa')"
        >
          <div class="card-click-area">
            <BCardTitle>
              Mapa de Competências
            </BCardTitle>
            <BCardText class="text-muted">
              Visualização do mapa de competências técnicas
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
              Autoavaliação
            </BCardTitle>
            <BCardText class="text-muted">
              Realize sua autoavaliação de competências
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
              Ocupações Críticas
            </BCardTitle>
            <BCardText class="text-muted">
              Identificação das ocupações críticas da unidade
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
              Monitoramento
            </BCardTitle>
            <BCardText class="text-muted">
              Acompanhamento e conclusão do diagnóstico da unidade
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
import {type Mapa, type MapaCompleto, SituacaoSubprocesso, TipoProcesso,} from "@/types/tipos";

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
const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);

const {podeEditarCadastro, podeEditarMapa} = useAcesso(subprocesso);

const isProcessoFinalizado = computed(() => {
  return props.situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO ||
      props.situacao === SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO;
});

const podeEditarCadastroFinal = computed(() => podeEditarCadastro.value && !isProcessoFinalizado.value);
const podeEditarMapaFinal = computed(() => podeEditarMapa.value && !isProcessoFinalizado.value);

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
    } else if (routeName === 'SubprocessoMapa' || routeName === 'SubprocessoVisMapa') {
      if (props.mapa) navegarPara(routeName);
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

.card-actionable.disabled-card {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Ensure hover effect doesn't happen on disabled cards */
.card-actionable.disabled-card:hover {
  transform: none;
  box-shadow: none;
}

.card-actionable:focus-visible {
  outline: 2px solid var(--bs-primary);
  outline-offset: 2px;
}

.card-click-area {
  height: 100%;
}
</style>
