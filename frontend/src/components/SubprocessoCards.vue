<template>
  <BRow>
    <template v-if="tipoProcesso === TipoProcessoEnum.MAPEAMENTO || tipoProcesso === TipoProcessoEnum.REVISAO">
      <BCol
        md="4"
        class="mb-3"
      >
        <BCard
          v-if="permissoes.podeEditarMapa"
          class="h-100 card-actionable"
          data-testid="card-subprocesso-atividades"
          @click="navegarPara('SubprocessoCadastro')"
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
          @click="navegarPara('SubprocessoVisCadastro')"
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
        md="4"
        class="mb-3"
      >
        <BCard
          v-if="permissoes.podeEditarMapa"
          :class="{ 'disabled-card': !mapa }"
          class="h-100 card-actionable"
          data-testid="card-subprocesso-mapa"
          @click="navegarPara('SubprocessoMapa')"
        >
          <BCardTitle>
            Mapa de Competências
          </BCardTitle>
          <BCardText class="text-muted">
            Mapa de competências técnicas da unidade
          </BCardText>
          <span
            :class="badgeClass(mapa?.situacao)"
            class="badge"
          >{{ situacaoLabel(mapa?.situacao) }}</span>
        </BCard>
        <BCard
          v-else-if="permissoes.podeVisualizarMapa"
          :class="{ 'disabled-card': !mapa }"
          class="h-100 card-actionable"
          data-testid="card-subprocesso-mapa-vis"
          @click="navegarPara('SubprocessoVisMapa')"
        >
          <BCardTitle>
            Mapa de Competências
          </BCardTitle>
          <BCardText class="text-muted">
            Visualização do mapa de competências técnicas
          </BCardText>
          <span
            :class="badgeClass(mapa?.situacao)"
            class="badge"
          >{{ situacaoLabel(mapa?.situacao) }}</span>
        </BCard>
      </BCol>
    </template>

    <template v-else-if="tipoProcesso === TipoProcessoEnum.DIAGNOSTICO">
      <BCol
        md="4"
        class="mb-3"
      >
        <BCard
          v-if="permissoes.podeVisualizarDiagnostico"
          class="h-100 card-actionable"
          data-testid="card-subprocesso-diagnostico"
          @click="navegarPara('DiagnosticoEquipe')"
        >
          <BCardTitle>
            Diagnóstico da Equipe
          </BCardTitle>
          <BCardText class="text-muted">
            Diagnóstico das competências pelos servidores da unidade
          </BCardText>
          <span
            :class="badgeClass(situacao)"
            class="badge"
          >{{ situacaoLabel(situacao) }}</span>
        </BCard>
      </BCol>

      <BCol
        md="4"
        class="mb-3"
      >
        <BCard
          class="h-100 card-actionable"
          data-testid="card-subprocesso-ocupacoes"
          @click="navegarPara('OcupacoesCriticas')"
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
}
</style>
