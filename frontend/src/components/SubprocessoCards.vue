<template>
  <div class="row">
    <template v-if="tipoProcesso === TipoProcesso.MAPEAMENTO || tipoProcesso === TipoProcesso.REVISAO">
      <section class="col-md-4 mb-3">
        <div
          v-if="permissoes.podeEditarMapa"
          class="card h-100 card-actionable"
          data-testid="atividades-card"
          @click="navegarPara('SubprocessoCadastro')"
        >
          <div class="card-body">
            <h5 class="card-title">
              Atividades e conhecimentos
            </h5>
            <p class="card-text text-muted">
              Cadastro de atividades e conhecimentos da unidade
            </p>
            <span
              :class="badgeClass(situacao)"
              class="badge"
            >{{ situacaoLabel(situacao) }}</span>
          </div>
        </div>
        <div
          v-else-if="permissoes.podeVisualizarMapa"
          class="card h-100 card-actionable"
          data-testid="atividades-card-vis"
          @click="navegarPara('SubprocessoVisCadastro')"
        >
          <div class="card-body">
            <h5 class="card-title">
              Atividades e conhecimentos
            </h5>
            <p class="card-text text-muted">
              Visualização das atividades e conhecimentos da unidade
            </p>
            <span
              :class="badgeClass(situacao)"
              class="badge"
            >{{ situacaoLabel(situacao) }}</span>
          </div>
        </div>
      </section>

      <section class="col-md-4 mb-3">
        <div
          v-if="permissoes.podeVisualizarMapa"
          :class="{ 'disabled-card': !mapa }"
          class="card h-100 card-actionable"
          data-testid="mapa-card"
          @click="navegarPara('SubprocessoMapa')"
        >
          <div class="card-body">
            <h5 class="card-title">
              Mapa de Competências
            </h5>
            <p class="card-text text-muted">
              Mapa de competências técnicas da unidade
            </p>
            <span
              :class="badgeClass(mapa?.situacao)"
              class="badge"
            >{{ situacaoLabel(mapa?.situacao) }}</span>
          </div>
        </div>
      </section>
    </template>

    <template v-else-if="tipoProcesso === TipoProcesso.DIAGNOSTICO">
      <section class="col-md-4 mb-3">
        <div
          v-if="permissoes.podeVisualizarDiagnostico"
          class="card h-100 card-actionable"
          data-testid="diagnostico-card"
          @click="navegarPara('DiagnosticoEquipe')"
        >
          <div class="card-body">
            <h5 class="card-title">
              Diagnóstico da Equipe
            </h5>
            <p class="card-text text-muted">
              Diagnóstico das competências pelos servidores da unidade
            </p>
            <span
              :class="badgeClass(situacao)"
              class="badge"
            >{{ situacaoLabel(situacao) }}</span>
          </div>
        </div>
      </section>

      <section class="col-md-4 mb-3">
        <div
          class="card h-100 card-actionable"
          data-testid="ocupacoes-card"
          @click="navegarPara('OcupacoesCriticas')"
        >
          <div class="card-body">
            <h5 class="card-title">
              Ocupações Críticas
            </h5>
            <p class="card-text text-muted">
              Identificação das ocupações críticas da unidade
            </p>
            <span
              :class="badgeClass(situacao)"
              class="badge"
            >{{ situacaoLabel(situacao) }}</span>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router';
import { Mapa, MapaCompleto, SubprocessoPermissoes, TipoProcesso } from '@/types/tipos';
import { badgeClass, situacaoLabel } from '@/utils';

defineProps<{
  tipoProcesso: TipoProcesso;
  mapa: Mapa | MapaCompleto | null;
  situacao?: string;
  permissoes: SubprocessoPermissoes;
}>();

const router = useRouter();
const route = router.currentRoute;

const navegarPara = (routeName: string) => {
  if (!route.value.params.codSubprocesso) return;
  router.push({
    name: routeName,
    params: { codSubprocesso: route.value.params.codSubprocesso },
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
