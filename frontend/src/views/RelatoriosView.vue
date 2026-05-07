<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.relatorios.TITULO"/>

    <BRow>
      <BCol class="mb-4" md="4">
        <BCard
            class="h-100 card-actionable"
            data-testid="card-relatorio-andamento"
            role="button"
            tabindex="0"
            @click="router.push('/relatorios/andamento')"
            @keydown.enter="router.push('/relatorios/andamento')"
            @keydown.space.prevent="router.push('/relatorios/andamento')"
        >
          <div class="card-click-area">
            <BCardTitle>{{ TEXTOS.relatorios.ANDAMENTO_PROCESSO }}</BCardTitle>
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
            @click="router.push('/relatorios/mapas-vigentes')"
            @keydown.enter="router.push('/relatorios/mapas-vigentes')"
            @keydown.space.prevent="router.push('/relatorios/mapas-vigentes')"
        >
          <div class="card-click-area">
            <BCardTitle>Mapas vigentes</BCardTitle>
            <BCardText class="text-muted">
              Detalhes de mapas de competências vigentes por unidade
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
import {TEXTOS} from "@/constants/textos";

const router = useRouter();
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
