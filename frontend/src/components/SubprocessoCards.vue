<template>
  <div class="row">
    <template v-if="tipoProcesso === TipoProcesso.MAPEAMENTO || tipoProcesso === TipoProcesso.REVISAO">
      <section class="col-md-4 mb-3">
        <div
          class="card h-100 card-actionable"
          data-testid="atividades-card"
          @click="$emit('irParaAtividades')"
        >
          <div class="card-body">
            <h5 class="card-title">
              Atividades e conhecimentos
            </h5>
            <p class="card-text text-muted">
              Cadastro de atividades e conhecimentos da unidade
            </p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge bg-secondary">{{ LABELS_SITUACAO[SITUACOES_MAPA.DISPONIVEL_VALIDACAO] }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="col-md-4 mb-3">
        <div
          :class="{ 'disabled-card': !mapa }"
          class="card h-100 card-actionable"
          data-testid="mapa-card"
          @click="handleMapaClick"
        >
          <div class="card-body">
            <h5 class="card-title">
              Mapa de Competências
            </h5>
            <p class="card-text text-muted">
              Mapa de competências técnicas da unidade
            </p>
            <div v-if="mapa">
              <div v-if="situacao === 'Mapa em andamento'">
                <span class="badge bg-warning text-dark">Em andamento</span>
              </div>
              <div v-else-if="situacao === 'Mapa disponibilizado'">
                <span class="badge bg-success">Disponibilizado</span>
              </div>
              <div v-else-if="situacao">
                <span class="badge bg-secondary">{{ situacao }}</span>
              </div>
              <div v-else>
                <span class="badge bg-secondary">Disponibilizado</span>
              </div>
            </div>
            <div v-else>
              <span class="badge bg-secondary">{{ LABELS_SITUACAO.NAO_DISPONIBILIZADO }}</span>
            </div>
          </div>
        </div>
      </section>
    </template>

    <template v-else-if="tipoProcesso === TipoProcesso.DIAGNOSTICO">
      <section class="col-md-4 mb-3">
        <div
          class="card h-100 card-actionable"
          @click="$emit('irParaDiagnosticoEquipe')"
        >
          <div class="card-body">
            <h5 class="card-title">
              Diagnóstico da Equipe
            </h5>
            <p class="card-text text-muted">
              Diagnóstico das competências pelos servidores da unidade
            </p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge bg-secondary">{{ LABELS_SITUACAO.NAO_DISPONIBILIZADO }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="col-md-4 mb-3">
        <div
          class="card h-100 card-actionable"
          @click="$emit('irParaOcupacoesCriticas')"
        >
          <div class="card-body">
            <h5 class="card-title">
              Ocupações Críticas
            </h5>
            <p class="card-text text-muted">
              Identificação das ocupações críticas da unidade
            </p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge bg-secondary">{{ LABELS_SITUACAO.NAO_DISPONIBILIZADO }}</span>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script lang="ts" setup>
import {Mapa, TipoProcesso} from '@/types/tipos';
import {LABELS_SITUACAO, SITUACOES_MAPA} from '@/constants/situacoes';

defineProps<{
  tipoProcesso: TipoProcesso;
  mapa: Mapa | null;
  situacao?: string;
}>();

const emit = defineEmits<{
  irParaAtividades: [];
  navegarParaMapa: [];
  irParaDiagnosticoEquipe: [];
  irParaOcupacoesCriticas: [];
}>();


const handleMapaClick = () => {
  emit('navegarParaMapa');
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
  /* Allow clicks for testing purposes */
  /* pointer-events: none; */
  opacity: 0.6;
}
</style>