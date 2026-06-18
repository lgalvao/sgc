<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS_RELATORIOS.TITULO"/>

    <BRow>
      <BCol class="mb-4" md="4">
        <BCard
            class="h-100 card-actionable"
            data-testid="card-relatorio-andamento"
            role="button"
            tabindex="0"
            @click="void router.push('/relatorios/andamento')"
            @keydown="tratarKeyDown($event, '/relatorios/andamento')"
        >
          <div class="card-click-area">
            <BCardTitle class="d-flex align-items-start gap-3 mb-3">
              <i aria-hidden="true" class="bi bi-bar-chart-steps text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">{{ TEXTOS_RELATORIOS.ANDAMENTO_PROCESSO }}</span>
            </BCardTitle>
            <BCardText class="text-muted">
              Situação das unidades participantes em um processo ativo
            </BCardText>
          </div>
        </BCard>
      </BCol>

      <BCol class="mb-4" md="4">
        <BCard
            class="h-100 card-actionable"
            data-testid="card-relatorio-mapas"
            role="button"
            tabindex="0"
            @click="void router.push('/relatorios/mapas-vigentes')"
            @keydown="tratarKeyDown($event, '/relatorios/mapas-vigentes')"
        >
          <div class="card-click-area">
            <BCardTitle class="d-flex align-items-start gap-3 mb-3">
              <i aria-hidden="true" class="bi bi-diagram-3 text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">Mapas vigentes</span>
            </BCardTitle>
            <BCardText class="text-muted">
              Detalhes de mapas de competências vigentes por unidade
            </BCardText>
          </div>
        </BCard>
      </BCol>

      <BCol class="mb-4" md="4">
        <BCard
            class="h-100 card-actionable"
            data-testid="card-relatorio-gaps-diagnostico"
            role="button"
            tabindex="0"
            @click="void router.push('/relatorios/diagnostico/gaps')"
            @keydown="tratarKeyDown($event, '/relatorios/diagnostico/gaps')"
        >
          <div class="card-click-area">
            <BCardTitle class="d-flex align-items-start gap-3 mb-3">
              <i aria-hidden="true" class="bi bi-graph-up-arrow text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">{{ TEXTOS_RELATORIOS.GAPS_DIAGNOSTICO }}</span>
            </BCardTitle>
            <BCardText class="text-muted">
              Consolidação dos gaps médios por unidade e competência em processos de diagnóstico
            </BCardText>
          </div>
        </BCard>
      </BCol>

      <BCol class="mb-4" md="4">
        <BCard
            class="h-100 card-actionable"
            data-testid="card-relatorio-situacao-capacitacao-diagnostico"
            role="button"
            tabindex="0"
            @click="void router.push('/relatorios/diagnostico/situacao-capacitacao')"
            @keydown="tratarKeyDown($event, '/relatorios/diagnostico/situacao-capacitacao')"
        >
          <div class="card-click-area">
            <BCardTitle class="d-flex align-items-start gap-3 mb-3">
              <i aria-hidden="true" class="bi bi-journal-check text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">{{ TEXTOS_RELATORIOS.SITUACAO_CAPACITACAO_DIAGNOSTICO }}</span>
            </BCardTitle>
            <BCardText class="text-muted">
              Quantitativos de NA, AC, EC, C e I por unidade e competência
            </BCardText>
          </div>
        </BCard>
      </BCol>

      <BCol v-if="perfilStore.perfilSelecionado === Perfil.ADMIN" class="mb-4" md="4">
        <BCard
            class="h-100 card-actionable"
            data-testid="card-relatorio-unidades-sem-mapas-vigentes"
            role="button"
            tabindex="0"
            @click="void router.push('/relatorios/unidades-sem-mapas-vigentes')"
            @keydown="tratarKeyDown($event, '/relatorios/unidades-sem-mapas-vigentes')"
        >
          <div class="card-click-area">
            <BCardTitle class="d-flex align-items-start gap-3 mb-3">
              <i aria-hidden="true" class="bi bi-journal-x text-primary flex-shrink-0 mt-1"></i>
              <span class="lh-sm">Unidades sem mapas vigentes</span>
            </BCardTitle>
            <BCardText class="text-muted">
              Unidades que ainda não passaram por um processo de mapeamento.
            </BCardText>
          </div>
        </BCard>
      </BCol>
    </BRow>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BCard, BCardText, BCardTitle, BCol, BRow} from "bootstrap-vue-next";
import {useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil} from "@/types/tipos";

const router = useRouter();
const perfilStore = usePerfilStore();

function tratarKeyDown(event: KeyboardEvent, rota: string) {
  const key = event.key ? event.key.toLowerCase() : '';
  if (key === 'enter' || event.keyCode === 13 || key === ' ' || key === 'spacebar' || event.keyCode === 32) {
    event.preventDefault();
    void router.push(rota);
  }
}
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
</style>
