<template>
  <div class="row">
    <template v-if="tipoProcesso === TipoProcesso.MAPEAMENTO || tipoProcesso === TipoProcesso.REVISAO">
      <section class="col-md-4 mb-3">
        <div class="card h-100 card-actionable" @click="$emit('irParaAtividades')">
          <div class="card-body">
            <h5 class="card-title">Atividades e conhecimentos</h5>
            <p class="card-text text-muted">Cadastro de atividades e conhecimentos da unidade</p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge bg-secondary">{{ LABELS_SITUACAO[SITUACOES_MAPA.DISPONIVEL_VALIDACAO] }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="col-md-4 mb-3">
        <div :class="{ 'disabled-card': !mapa }" class="card h-100 card-actionable" @click="$emit('navegarParaMapa')">
          <div class="card-body">
            <h5 class="card-title">Mapa de Competências</h5>
            <p class="card-text text-muted">Mapa de competências técnicas da unidade</p>
            <div v-if="mapa">
              <div v-if="mapa.situacao === SITUACOES_MAPA.EM_ANDAMENTO">
                <span class="badge bg-warning text-dark">{{ LABELS_SITUACAO[SITUACOES_MAPA.EM_ANDAMENTO] }}</span>
              </div>
              <div v-else-if="mapa.situacao === SITUACOES_MAPA.DISPONIVEL_VALIDACAO">
                <span class="badge bg-success">{{ LABELS_SITUACAO[SITUACOES_MAPA.DISPONIVEL_VALIDACAO] }}</span>
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
        <div class="card h-100 card-actionable">
          <div class="card-body">
            <h5 class="card-title">Diagnóstico da Equipe</h5>
            <p class="card-text text-muted">Diagnóstico das competências pelos servidores da unidade</p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge bg-secondary">{{ LABELS_SITUACAO.NAO_DISPONIBILIZADO }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="col-md-4 mb-3">
        <div class="card h-100 card-actionable">
          <div class="card-body">
            <h5 class="card-title">Ocupações Críticas</h5>
            <p class="card-text text-muted">Identificação das ocupações críticas da unidade</p>
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
import { TipoProcesso, Mapa } from '@/types/tipos';
import { SITUACOES_MAPA, LABELS_SITUACAO } from '@/constants/situacoes';

interface Props {
  tipoProcesso: TipoProcesso;
  mapa: Mapa | null;
}

defineProps<Props>();

defineEmits<{
  irParaAtividades: [];
  navegarParaMapa: [];
}>();
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
  pointer-events: none;
  opacity: 0.6;
}
</style>