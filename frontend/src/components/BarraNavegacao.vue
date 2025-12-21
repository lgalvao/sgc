<!--
BarraNavegacao - Breadcrumbs contextuais e navegação hierárquica
Responsável por: mostrar caminho atual na hierarquia, botão voltar
-->
<template>
  <div class="barra-navegacao d-flex align-items-center gap-2">
    <BButton
        v-if="shouldShowBackButton"
        v-b-tooltip.hover="'Voltar'"
        variant="outline-secondary"
        class="btn-voltar"
        size="lg"
        data-testid="btn-nav-voltar"
        @click="goBack"
    >
      <i class="bi bi-arrow-left-circle"/>
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
            aria-label="Início"
            class="bi bi-house-door"
            data-testid="btn-nav-home"
        />
        <span v-else>{{ crumb.label }}</span>
      </BBreadcrumbItem>
    </BBreadcrumb>
  </div>
</template>

<script lang="ts" setup>
import {BBreadcrumb, BBreadcrumbItem, BButton, vBTooltip} from "bootstrap-vue-next";
import {computed} from "vue";
import {useRoute, useRouter} from "vue-router";
import { useBreadcrumbs } from "@/composables/useBreadcrumbs";

const route = useRoute();
const router = useRouter();

const { breadcrumbs: crumbs } = useBreadcrumbs(route);

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
}

.btn-voltar {
  padding: 0.4rem;
  font-size: 0.75rem;
  border-color: #dee2e6;
  color: #6c757d;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4px;
}

.btn-voltar:hover {
  background-color: #6c757d;
  border-color: #6c757d;
  color: #fff;
}

.breadcrumb-compacto {
  --bs-breadcrumb-divider: '›';
  padding: 0;
  background: transparent;
  display: flex;
  align-items: center;
  margin: 3px 0 0;
}

:deep(.breadcrumb) {
  margin-bottom: 0 !important;
}

:deep(.breadcrumb-item) {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
}

:deep(.breadcrumb-item a) {
  text-decoration: none !important;
  color: #6c757d !important;
  transition: color 0.2s;
}

:deep(.breadcrumb-item a:hover) {
  color: #212529 !important;
}

:deep(.breadcrumb-item.active) {
  color: #212529 !important;
  font-weight: 400;
}

:deep(.breadcrumb-item + .breadcrumb-item::before) {
  content: var(--bs-breadcrumb-divider, '›') !important;
  color: #adb5bd;
  padding: 0 0.5rem;
  line-height: normal;
}

:deep(.bi-house-door) {
  color: #6c757d;
  font-size: 0.9rem;
  vertical-align: middle;
  margin-top: -2px;
}
</style>