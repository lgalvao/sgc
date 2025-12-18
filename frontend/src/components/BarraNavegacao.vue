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
import {type RouteLocationNamedRaw, useRoute, useRouter} from "vue-router";
import {usePerfilStore} from "@/stores/perfil";
import {useUnidadesStore} from "@/stores/unidades";
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

const shouldShowBackButton = computed(
    () => route.path !== "/login" && route.path !== "/painel",
);

const shouldShowBreadcrumbs = computed(
    () => route.path !== "/login" && route.path !== "/painel",
);

const crumbs = computed((): Breadcrumb[] => {
  const breadcrumbs: Breadcrumb[] = [];
  const perfil = usePerfilStore();
  const unidadesStore = useUnidadesStore();
  const perfilUsuario = perfil.perfilSelecionado;

  // Add home breadcrumb
  breadcrumbs.push({label: "Painel", to: {name: "Painel"}, isHome: true});

  const routeName = route.name as string;
  const codProcesso = route.params.codProcesso as string;
  const siglaUnidade = route.params.siglaUnidade as string;
  const codUnidade = route.params.codUnidade as string;

  // Verifica se é uma rota de processo ou subprocesso
  const isProcessoRoute = routeName === "Processo";
  const isSubprocessoRoute = [
    "Subprocesso",
    "SubprocessoMapa",
    "SubprocessoVisMapa",
    "SubprocessoCadastro",
    "SubprocessoVisCadastro",
  ].includes(routeName);
  
  // Verifica se é uma rota de unidade
  const isUnidadeRoute = [
    "Unidade",
    "Mapa",
    "AtribuicaoTemporariaForm",
  ].includes(routeName);

  // Para CHEFE e SERVIDOR, não mostra "Detalhes do processo"
  const shouldShowProcessoCrumb =
      perfilUsuario !== Perfil.CHEFE && perfilUsuario !== Perfil.SERVIDOR;

  // Adiciona breadcrumb de "Detalhes do processo" se aplicável
  if (codProcesso && (isProcessoRoute || isSubprocessoRoute)) {
    if (shouldShowProcessoCrumb) {
      breadcrumbs.push({
        label: "Detalhes do processo",
        to: isProcessoRoute ? undefined : {name: "Processo", params: {codProcesso}},
      });
    }
  }

  // Adiciona breadcrumb do subprocesso (sigla da unidade)
  if (siglaUnidade && isSubprocessoRoute) {
    breadcrumbs.push({
      label: siglaUnidade,
      to: routeName === "Subprocesso" ? undefined : {name: "Subprocesso", params: {codProcesso, siglaUnidade}},
    });

    // Adiciona breadcrumb final para páginas específicas do subprocesso
    const pageTitles: Record<string, string> = {
      SubprocessoMapa: "Mapa de competências",
      SubprocessoVisMapa: "Visualizar mapa",
      SubprocessoCadastro: "Atividades e conhecimentos",
      SubprocessoVisCadastro: "Atividades e conhecimentos",
    };

    const pageTitle = pageTitles[routeName];
    if (pageTitle) {
      breadcrumbs.push({
        label: pageTitle,
      });
    }
  }

  // Adiciona breadcrumbs para rotas de unidade
  if (codUnidade && isUnidadeRoute) {
    // Obtém a sigla da unidade do store (se carregada)
    const siglaUnidadeStore = unidadesStore.unidade?.sigla;
    
    breadcrumbs.push({
      label: siglaUnidadeStore || `Unidade ${codUnidade}`,
      to: routeName === "Unidade" ? undefined : {name: "Unidade", params: {codUnidade}},
    });

    // Adiciona breadcrumb final para páginas específicas de unidade
    const unidadePageTitles: Record<string, string> = {
      Unidade: "Minha unidade",
      Mapa: "Mapa de competências",
      AtribuicaoTemporariaForm: "Atribuição temporária",
    };

    const unidadePageTitle = unidadePageTitles[routeName];
    if (unidadePageTitle) {
      breadcrumbs.push({
        label: unidadePageTitle,
      });
    }
  }

  // Para outras rotas, usa a lógica padrão baseada em meta.breadcrumb
  if (!isProcessoRoute && !isSubprocessoRoute && !isUnidadeRoute) {
    route.matched.forEach((routeRecord) => {
      const {meta, name} = routeRecord;

      if (meta.breadcrumb) {
        const label =
            typeof meta.breadcrumb === "function"
                ? meta.breadcrumb(route)
                : (meta.breadcrumb as string);

        if (label) {
          if (
              breadcrumbs.length === 0 ||
              breadcrumbs[breadcrumbs.length - 1].label !== label
          ) {
            breadcrumbs.push({
              label,
              to: {name: name as string, params: route.params},
            });
          }
        }
      }
    });

    // Remove link from the last breadcrumb
    if (breadcrumbs.length > 0) {
      breadcrumbs[breadcrumbs.length - 1].to = undefined;
    }
  }

  return breadcrumbs;
});
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