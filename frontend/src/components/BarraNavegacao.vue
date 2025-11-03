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
    const breadcrumbs: Breadcrumb[] = [];
    const perfil = usePerfilStore();
    const perfilUsuario = perfil.perfilSelecionado;

    // Add home breadcrumb
    breadcrumbs.push({ label: 'Painel', to: { name: 'Painel' }, isHome: true });

    // Iterate over matched routes to build breadcrumbs
    route.matched.forEach((routeRecord) => {
        const { meta, name } = routeRecord;

        const shouldAddCrumb = () => {
            if (name === 'Processo' && (perfilUsuario === Perfil.CHEFE || perfilUsuario === Perfil.SERVIDOR)) {
                return false;
            }
            return true;
        };

        if (meta.breadcrumb && shouldAddCrumb()) {
            const label = typeof meta.breadcrumb === 'function' ? meta.breadcrumb(route) : (meta.breadcrumb as string);

            if (label) {
                // Add breadcrumb if it doesn't already exist as the last one
                if (breadcrumbs.length === 0 || breadcrumbs[breadcrumbs.length - 1].label !== label) {
                    breadcrumbs.push({
                        label,
                        to: { name: name as string, params: route.params },
                    });
                }
            }
        }
    });

    // Remove link from the last breadcrumb
    if (breadcrumbs.length > 0) {
        breadcrumbs[breadcrumbs.length - 1].to = undefined;
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