<template>
  <BRow>
    <template v-if="tipoProcesso === TipoProcessoEnum.MAPEAMENTO || tipoProcesso === TipoProcessoEnum.REVISAO">
      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            v-if="permissoes.podeEditarMapa"
            class="h-100 card-actionable"
            data-testid="card-subprocesso-atividades"
            role="button"
            tabindex="0"
            @click="navegarPara('SubprocessoCadastro')"
            @keydown.enter.prevent="navegarPara('SubprocessoCadastro')"
            @keydown.space.prevent="navegarPara('SubprocessoCadastro')"
        >
          <BCardTitle>
            Atividades e conhecimentos
          </BCardTitle>
          <BCardText class="text-muted">
            Cadastro de atividades e conhecimentos da unidade
          </BCardText>
          <span
              :class="badgeClass(situacao)"
              class="badge"
          >{{ situacaoLabel(situacao) }}</span>
        </BCard>
        <BCard
            v-else-if="permissoes.podeVisualizarMapa"
            class="h-100 card-actionable"
            data-testid="card-subprocesso-atividades-vis"
            role="button"
            tabindex="0"
            @click="navegarPara('SubprocessoVisCadastro')"
            @keydown.enter.prevent="navegarPara('SubprocessoVisCadastro')"
            @keydown.space.prevent="navegarPara('SubprocessoVisCadastro')"
        >
          <BCardTitle>
            Atividades e conhecimentos
          </BCardTitle>
          <BCardText class="text-muted">
            Visualização das atividades e conhecimentos da unidade
          </BCardText>
          <span
              :class="badgeClass(situacao)"
              class="badge"
          >{{ situacaoLabel(situacao) }}</span>
        </BCard>
      </BCol>

      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            v-if="permissoes.podeEditarMapa"
            :aria-disabled="!mapa"
            :class="{ 'disabled-card': !mapa }"
            class="h-100 card-actionable"
            data-testid="card-subprocesso-mapa"
            role="button"
            :tabindex="!mapa ? -1 : 0"
            @click="!mapa ? null : navegarPara('SubprocessoMapa')"
            @keydown.enter.prevent="!mapa ? null : navegarPara('SubprocessoMapa')"
            @keydown.space.prevent="!mapa ? null : navegarPara('SubprocessoMapa')"
        >
          <BCardTitle>
            Mapa de Competências
          </BCardTitle>
          <BCardText class="text-muted">
            Mapa de competências técnicas da unidade
          </BCardText>
          <span
              :class="badgeClass(situacao)"
              class="badge"
          >{{ situacaoLabel(situacao) }}</span>
        </BCard>
        <BCard
            v-else-if="permissoes.podeVisualizarMapa"
            :aria-disabled="!mapa"
            :class="{ 'disabled-card': !mapa }"
            class="h-100 card-actionable"
            data-testid="card-subprocesso-mapa"
            role="button"
            :tabindex="!mapa ? -1 : 0"
            @click="!mapa ? null : navegarPara('SubprocessoVisMapa')"
            @keydown.enter.prevent="!mapa ? null : navegarPara('SubprocessoVisMapa')"
            @keydown.space.prevent="!mapa ? null : navegarPara('SubprocessoVisMapa')"
        >
          <BCardTitle>
            Mapa de Competências
          </BCardTitle>
          <BCardText class="text-muted">
            Visualização do mapa de competências técnicas
          </BCardText>
          <span
              :class="badgeClass(situacao)"
              class="badge"
          >{{ situacaoLabel(situacao) }}</span>
        </BCard>
      </BCol>
    </template>

    <template v-else-if="tipoProcesso === TipoProcessoEnum.DIAGNOSTICO">
      <BCol
          class="mb-3"
          md="4"
      >
        <BCard
            v-if="permissoes.podeVisualizarDiagnostico"
            class="h-100 card-actionable"
            data-testid="card-subprocesso-diagnostico"
            role="button"
            tabindex="0"
            @click="navegarParaDiag('AutoavaliacaoDiagnostico')"
            @keydown.enter.prevent="navegarParaDiag('AutoavaliacaoDiagnostico')"
            @keydown.space.prevent="navegarParaDiag('AutoavaliacaoDiagnostico')"
        >
          <BCardTitle>
            Autoavaliação
          </BCardTitle>
          <BCardText class="text-muted">
            Realize sua autoavaliação de competências
          </BCardText>
          <span
              :class="badgeClass(situacao)"
              class="badge"
          >{{ situacaoLabel(situacao) }}</span>
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
            @keydown.enter.prevent="navegarParaDiag('OcupacoesCriticasDiagnostico')"
            @keydown.space.prevent="navegarParaDiag('OcupacoesCriticasDiagnostico')"
        >
          <BCardTitle>
            Ocupações Críticas
          </BCardTitle>
          <BCardText class="text-muted">
            Identificação das ocupações críticas da unidade
          </BCardText>
          <span
              :class="badgeClass(situacao)"
              class="badge"
          >{{ situacaoLabel(situacao) }}</span>
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
            @keydown.enter.prevent="navegarParaDiag('MonitoramentoDiagnostico')"
            @keydown.space.prevent="navegarParaDiag('MonitoramentoDiagnostico')"
        >
          <BCardTitle>
            Monitoramento
          </BCardTitle>
          <BCardText class="text-muted">
            Acompanhamento e conclusão do diagnóstico da unidade
          </BCardText>
          <span
              :class="badgeClass(situacao)"
              class="badge"
          >{{ situacaoLabel(situacao) }}</span>
        </BCard>
      </BCol>
    </template>
  </BRow>
</template>

<script lang="ts" setup>
import {BCard, BCardText, BCardTitle, BCol, BRow} from "bootstrap-vue-next";
import {useRouter} from "vue-router";
import {type Mapa, type MapaCompleto, SubprocessoPermissoes, TipoProcesso,} from "@/types/tipos";
import {badgeClass, situacaoLabel} from "@/utils";

const TipoProcessoEnum = TipoProcesso;

const props = defineProps<{
  tipoProcesso: TipoProcesso;
  mapa: Mapa | MapaCompleto | null;
  situacao?: string;
  permissoes: SubprocessoPermissoes;
  codSubprocesso?: number | null;
  codProcesso?: number;
  siglaUnidade?: string;
}>();

const router = useRouter();
const route = router.currentRoute;

const navegarPara = (routeName: string) => {
  const codigoProcesso = props.codProcesso || Number(route.value.params.codProcesso);
  const sigla = props.siglaUnidade || String(route.value.params.siglaUnidade);

  if (!codigoProcesso || !sigla) return;

  router.push({
    name: routeName,
    params: {
      codProcesso: codigoProcesso,
      siglaUnidade: sigla
    },
  });
};

const navegarParaDiag = (routeName: string) => {
  if (!props.codSubprocesso) return;
  const sigla = props.siglaUnidade || String(route.value.params.siglaUnidade);

  router.push({
    name: routeName,
    params: {
      codSubprocesso: props.codSubprocesso,
      siglaUnidade: sigla
    }
  });
};
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
</style>
