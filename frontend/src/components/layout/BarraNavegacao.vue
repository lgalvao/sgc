<!--
BarraNavegacao - Breadcrumbs contextuais e navegação hierárquica
Responsável por: mostrar caminho atual na hierarquia, botão voltar
-->
<template>
  <div class="barra-navegacao d-flex align-items-center gap-2">
    <BButton
        v-if="shouldShowBackButton"
        v-b-tooltip.hover="'Voltar'"
        aria-label="Voltar"
        class="btn-voltar"
        data-testid="btn-nav-voltar"
        size="lg"
        variant="outline-secondary"
        @click="goBack"
    >
      <i aria-hidden="true" class="bi bi-arrow-left-circle"/>
    </BButton>

    <BBreadcrumb
        v-if="shouldShowBreadcrumbs"
        class="py-0 mb-0 breadcrumb-compacto"
        data-testid="nav-breadcrumbs"
    >
      <BBreadcrumbItem
          v-for="(crumb, index) in crumbs"
          :key="index"
          :active="index === crumbs.length - 1"
          :to="crumb.to"
      >
        <i
            v-if="crumb.isHome"
            aria-hidden="true"
            class="bi bi-house-door"
            data-testid="btn-nav-home"
        />
        <span v-if="crumb.isHome" class="visually-hidden">Início</span>
        <span v-else>{{ crumb.label }}</span>
      </BBreadcrumbItem>
    </BBreadcrumb>
  </div>
</template>

<script lang="ts" setup>
import {BBreadcrumb, BBreadcrumbItem, BButton, vBTooltip} from "bootstrap-vue-next";
import {computed} from "vue";
import {useRoute, useRouter} from "vue-router";
import {useBreadcrumbs} from "@/composables/useBreadcrumbs";

const route = useRoute();
const router = useRouter();

const {breadcrumbs: crumbs} = useBreadcrumbs(route);

function goBack() {
  router.back();
}

const shouldShowBackButton = computed(
    () => route.path !== "/login" && route.path !== "/painel",
);

const shouldShowBreadcrumbs = computed(
    () => route.path !== "/login" && route.path !== "/painel",
);
</script>

<style scoped>
.barra-navegacao {
  font-size: 0.85rem;
  line-height: 1;
  min-height: 1.75rem;
}

.btn-voltar {
  padding: 0.25rem 0.55rem;
  font-size: 0.75rem;
  border-color: var(--bs-border-color);
  color: var(--bs-secondary-color);
  min-width: 2.25rem;
  height: 1.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-voltar:hover {
  background-color: var(--bs-secondary-bg);
  border-color: var(--bs-secondary-color);
  color: var(--bs-secondary-color);
}

.breadcrumb-compacto {
  --bs-breadcrumb-divider: '›';
  padding: 0;
  background: transparent;
  display: flex;
  align-items: center;
  min-height: 1.75rem;
  margin: 0;
}

:deep(.breadcrumb) {
  margin-bottom: 0 !important;
  align-items: center;
}

:deep(.breadcrumb-item) {
  display: inline-flex;
  align-items: center;
}

:deep(.breadcrumb-item > a),
:deep(.breadcrumb-item.active) {
  display: inline-flex;
  align-items: center;
}

:deep(.breadcrumb-item > a) {
  color: var(--bs-secondary-color);
  text-decoration: none;
  transition: color 0.15s ease;
}

:deep(.breadcrumb-item > a:hover),
:deep(.breadcrumb-item > a:focus) {
  color: var(--bs-emphasis-color);
  text-decoration: none;
}

:deep(.breadcrumb-item.active) {
  color: var(--bs-emphasis-color);
}

:deep(.bi-house-door) {
  color: var(--bs-secondary-color);
  font-size: 0.9rem;
}
</style>
