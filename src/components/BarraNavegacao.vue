<template>
  <div class="d-flex align-items-center gap-3">
    <button
      v-if="shouldShowBackButton"
      class="btn btn-outline-secondary btn-sm"
      type="button"
      @click="goBack"
    >
      <i class="bi bi-arrow-left" /> Voltar
    </button>

    <nav
      v-if="shouldShowBreadcrumbs"
      aria-label="breadcrumb"
      data-testid="breadcrumbs"
    >
      <ol class="breadcrumb mb-0">
        <li
          v-for="(crumb, index) in crumbs"
          :key="index"
          :class="{ active: index === crumbs.length - 1 }"
          aria-current="page"
          class="breadcrumb-item"
          data-testid="breadcrumb-item"
        >
          <router-link
            v-if="index < crumbs.length - 1 && crumb.to"
            :to="crumb.to"
          >
            <i
              v-if="crumb.isHome"
              aria-label="Início"
              class="bi bi-house-door"
              data-testid="breadcrumb-home-icon"
            />
            <span v-else>{{ crumb.label }}</span>
          </router-link>
          <span v-else>
            {{ crumb.label }}
          </span>
        </li>
      </ol>
    </nav>
  </div>
</template>

<script lang="ts" setup>
import {computed} from 'vue';
import {type RouteLocationNamedRaw, useRoute, useRouter} from 'vue-router';
import {usePerfilStore} from "@/stores/perfil";
import {Perfil} from "@/types/tipos";

const route = useRoute();
const router = useRouter();

interface Breadcrumb {
  label: string;
  to?: RouteLocationNamedRaw;
  isHome?: boolean;
}

function goBack() {
  router.back();
}

const shouldShowBackButton = computed(() => route.path !== '/login' && route.path !== '/painel');

const shouldShowBreadcrumbs = computed(() => route.path !== '/login' && route.path !== '/painel');

const crumbs = computed((): Breadcrumb[] => {
  const breadcrumbs: Breadcrumb[] = [{ label: 'Painel', to: { name: 'Painel' }, isHome: true }];
  const params = route.params;
  const perfil = usePerfilStore();
  const perfilUsuario = perfil.perfilSelecionado;

  // Processo crumb
  if (params.idProcesso && (perfilUsuario === Perfil.ADMIN || perfilUsuario === Perfil.GESTOR)) {
    breadcrumbs.push({
      label: 'Processo',
      to: { name: 'Processo', params: { idProcesso: Number(params.idProcesso) } }
    });
  }

  // Unidade crumb (within a process or standalone)
  if (params.siglaUnidade) {
    // If it's a subprocess route, link to Subprocesso
    if (params.idProcesso) {
      breadcrumbs.push({
        label: params.siglaUnidade as string,
        to: { name: 'Subprocesso', params: { idProcesso: Number(params.idProcesso), siglaUnidade: params.siglaUnidade } }
      });
    } else { // Standalone unit route
      breadcrumbs.push({
        label: params.siglaUnidade as string,
        to: { name: 'Unidade', params: { siglaUnidade: params.siglaUnidade } }
      });
    }
  }

  // Determine the label for the *current* page (last crumb)
  let currentPageLabel: string | undefined;
  const currentRouteMetaBreadcrumb = route.meta.breadcrumb;

  if (currentRouteMetaBreadcrumb) {
      if (typeof currentRouteMetaBreadcrumb === 'function') {
          currentPageLabel = currentRouteMetaBreadcrumb(route);
      } else if (typeof currentRouteMetaBreadcrumb === 'string') {
          currentPageLabel = currentRouteMetaBreadcrumb;
      }
  }

  // Add the current page's label as the last crumb, if it's not already covered
  // and it's not the 'Painel' or 'Login' route itself.
  if (currentPageLabel && route.name !== 'Painel' && route.name !== 'Login') {
      const lastCrumbInArray = breadcrumbs[breadcrumbs.length - 1];
      // Avoid adding if it's a duplicate of the previous crumb's label
      // This handles cases like /processo/123 (Processo is last) or /unidade/XYZ (XYZ is last)
      if (lastCrumbInArray.label !== currentPageLabel) {
          breadcrumbs.push({ label: currentPageLabel });
      }
  }

  // Special case for Painel: if only Home and Painel are there, just show Home.
  if (breadcrumbs.length === 2 && breadcrumbs[1].label === 'Painel') {
      return [breadcrumbs[0]];
  }
  
  return breadcrumbs;
});
</script>

<style scoped>
.breadcrumb {
  --bs-breadcrumb-divider: '›';
}
.breadcrumb-item a {
  text-decoration: none;
}
.breadcrumb-item.active {
  font-weight: bold;
}
</style>
