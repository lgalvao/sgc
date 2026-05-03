<template>
  <BCard no-body class="relatorio-andamento__card shadow-sm" data-testid="card-resultado-andamento">
    <BCardBody>
      <BCardTitle class="mb-3 relatorio-andamento__cabecalho">
        <span class="relatorio-andamento__titulo">{{ item.siglaUnidade }}</span>
        <span class="relatorio-andamento__subtitulo">{{ item.nomeUnidade }}</span>
      </BCardTitle>

      <div class="relatorio-andamento__secao-geral mb-4">
        <div class="relatorio-andamento__grid-geral">
          <div class="relatorio-andamento__info-item">
            <span class="relatorio-andamento__info-label">Situação</span>
            <span class="relatorio-andamento__info-valor fw-bold">{{ item.situacaoAtual }}</span>
          </div>
          <div class="relatorio-andamento__info-item">
            <span class="relatorio-andamento__info-label">Localização</span>
            <span class="relatorio-andamento__info-valor fw-bold">{{ item.localizacao }}</span>
          </div>
          <div class="relatorio-andamento__info-item">
            <span class="relatorio-andamento__info-label">Última movimentação</span>
            <span class="relatorio-andamento__info-valor fw-bold">{{ item.dataUltimaMovimentacao }}</span>
          </div>
        </div>
      </div>

      <div class="relatorio-andamento__etapas mb-4">
        <div class="row">
          <div class="col-md-6 mb-3 mb-md-0">
            <div class="relatorio-andamento__etapa-card h-100">
              <h6 class="relatorio-andamento__etapa-titulo">ETAPA 1: CADASTRO</h6>
              <div class="d-flex gap-4">
                <div class="relatorio-andamento__info-item">
                  <span class="relatorio-andamento__info-label">Data limite</span>
                  <span class="relatorio-andamento__info-valor">{{ item.dataLimiteEtapa1 }}</span>
                </div>
                <div class="relatorio-andamento__info-item">
                  <span class="relatorio-andamento__info-label">Conclusão</span>
                  <span class="relatorio-andamento__info-valor">{{ item.dataFimEtapa1 }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="relatorio-andamento__etapa-card h-100">
              <h6 class="relatorio-andamento__etapa-titulo">ETAPA 2: MAPA</h6>
              <div class="d-flex gap-4">
                <div class="relatorio-andamento__info-item">
                  <span class="relatorio-andamento__info-label">Data limite</span>
                  <span class="relatorio-andamento__info-valor">
                    {{ item.dataLimiteEtapa2 }}
                    <small v-if="item.mostraPrazoAjustado" class="text-muted fst-italic fw-normal">(Prazo ajustado)</small>
                  </span>
                </div>
                <div class="relatorio-andamento__info-item">
                  <span class="relatorio-andamento__info-label">Conclusão</span>
                  <span class="relatorio-andamento__info-valor">{{ item.dataFimEtapa2 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="relatorio-andamento__responsaveis pt-3 border-top">
        <div class="row">
          <div class="col-md-6 mb-2 mb-md-0">
            <span class="relatorio-andamento__info-label">Titular</span>
            <span class="relatorio-andamento__info-valor fw-bold">{{ item.titular }}</span>
          </div>
          <div v-if="item.titular !== item.responsavel" class="col-md-6">
            <span class="relatorio-andamento__info-label">Responsável atual</span>
            <span class="relatorio-andamento__info-valor fw-bold">{{ item.responsavel }}</span>
          </div>
        </div>
      </div>
    </BCardBody>
  </BCard>
</template>

<script setup lang="ts">
import {BCard, BCardBody, BCardTitle} from "bootstrap-vue-next";

interface LinhaRelatorioAndamento {
  siglaUnidade: string;
  nomeUnidade: string;
  situacaoAtual: string;
  localizacao: string;
  dataLimiteEtapa1: string;
  dataLimiteEtapa2: string;
  dataFimEtapa1: string;
  dataFimEtapa2: string;
  dataUltimaMovimentacao: string;
  titular: string;
  responsavel: string;
  mostraPrazoAjustado: boolean;
}
defineProps<{ item: LinhaRelatorioAndamento }>();
</script>

<style scoped>
.relatorio-andamento__card {
  border: 1px solid var(--bs-border-color);
  background: var(--bs-body-bg);
  border-radius: 0.75rem;
  overflow: hidden;
}

.relatorio-andamento__cabecalho {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  padding-bottom: 0.85rem;
  margin-bottom: 1.2rem;
  border-bottom: 1px solid var(--bs-border-color);
}

.relatorio-andamento__titulo {
  color: var(--bs-primary-text-emphasis);
  font-size: 1.45rem;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.relatorio-andamento__subtitulo {
  color: var(--bs-secondary-color);
  font-size: 0.95rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.relatorio-andamento__grid-geral {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.relatorio-andamento__info-item {
  display: flex;
  flex-direction: column;
}

.relatorio-andamento__info-label {
  color: var(--bs-secondary-color);
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.025em;
  margin-bottom: 0.15rem;
}

.relatorio-andamento__info-valor {
  color: var(--bs-body-color);
  font-size: 0.95rem;
}

.relatorio-andamento__etapa-card {
  padding: 1rem;
  background-color: var(--bs-tertiary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 0.6rem;
}

.relatorio-andamento__etapa-titulo {
  color: var(--bs-heading-color);
  font-size: 0.9rem;
  font-weight: 700;
  margin-bottom: 0.75rem;
  letter-spacing: 0.02em;
}

@media (max-width: 768px) {
  .relatorio-andamento__titulo {
    font-size: 1.2rem;
  }
}
</style>
