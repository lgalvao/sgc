import {computed} from 'vue';
import {type RouteLocationNamedRaw, type RouteLocationNormalizedLoaded} from 'vue-router';
import {usePerfilStore} from '@/stores/perfil';
import {useUnidadesStore} from '@/stores/unidades';
import {Perfil} from '@/types/tipos';

export interface Breadcrumb {
  label: string;
  to?: RouteLocationNamedRaw;
  isHome?: boolean;
}

export function useBreadcrumbs(route: RouteLocationNormalizedLoaded) {
  const perfil = usePerfilStore();
  const unidadesStore = useUnidadesStore();

  const getProcessoBreadcrumbs = (
    codProcesso: string,
    isProcessoRoute: boolean,
    isSubprocessoRoute: boolean,
    perfilUsuario: Perfil | null
  ): Breadcrumb[] => {
    const crumbs: Breadcrumb[] = [];
    if (codProcesso && (isProcessoRoute || isSubprocessoRoute)) {
      const shouldShowProcessoCrumb = perfilUsuario !== Perfil.CHEFE && perfilUsuario !== Perfil.SERVIDOR;
      if (shouldShowProcessoCrumb) {
        crumbs.push({
          label: "Detalhes do processo",
          to: isProcessoRoute ? undefined : { name: "Processo", params: { codProcesso } },
        });
      }
    }
    return crumbs;
  };

  const getSubprocessoBreadcrumbs = (
    codProcesso: string,
    siglaUnidade: string,
    isSubprocessoRoute: boolean,
    routeName: string
  ): Breadcrumb[] => {
    const crumbs: Breadcrumb[] = [];
    if (siglaUnidade && isSubprocessoRoute) {
      crumbs.push({
        label: siglaUnidade,
        to: routeName === "Subprocesso" ? undefined : { name: "Subprocesso", params: { codProcesso, siglaUnidade } },
      });

      const pageTitles: Record<string, string> = {
        SubprocessoMapa: "Mapa de competências",
        SubprocessoVisMapa: "Visualizar mapa",
        SubprocessoCadastro: "Atividades e conhecimentos",
        SubprocessoVisCadastro: "Atividades e conhecimentos",
      };

      const pageTitle = pageTitles[routeName];
      if (pageTitle) {
        crumbs.push({ label: pageTitle });
      }
    }
    return crumbs;
  };

  const getUnidadeBreadcrumbs = (
    codUnidade: string,
    isUnidadeRoute: boolean,
    routeName: string,
    perfilUsuario: Perfil | null
  ): Breadcrumb[] => {
    const crumbs: Breadcrumb[] = [];
    if (codUnidade && isUnidadeRoute) {
      const siglaUnidadeStore = unidadesStore.unidade?.sigla;
      crumbs.push({
        label: siglaUnidadeStore || `Unidade ${codUnidade}`,
        to: routeName === "Unidade" ? undefined : { name: "Unidade", params: { codUnidade } },
      });

      const unidadeLabel = perfilUsuario === Perfil.ADMIN ? "Unidades" : "Minha unidade";
      const unidadePageTitles: Record<string, string> = {
        Unidade: unidadeLabel,
        Mapa: "Mapa de competências",
        AtribuicaoTemporariaForm: "Atribuição temporária",
      };

      const unidadePageTitle = unidadePageTitles[routeName];
      if (unidadePageTitle) {
        crumbs.push({ label: unidadePageTitle });
      }
    }
    return crumbs;
  };

  const addFallbackBreadcrumbs = (breadcrumbs: Breadcrumb[]) => {
    route.matched.forEach((routeRecord) => {
      const { meta, name } = routeRecord;
      if (meta.breadcrumb) {
        const label = typeof meta.breadcrumb === "function" ? meta.breadcrumb(route) : (meta.breadcrumb as string);
        if (label && (breadcrumbs.length === 0 || breadcrumbs.at(-1)?.label !== label)) {
          breadcrumbs.push({
            label,
            to: { name: name as string, params: route.params },
          });
        }
      }
    });

    if (breadcrumbs.length > 0) {
      const last = breadcrumbs.at(-1);
      if (last) last.to = undefined;
    }
  };

  const crumbs = computed((): Breadcrumb[] => {
    const breadcrumbs: Breadcrumb[] = [{ label: "Painel", to: { name: "Painel" }, isHome: true }];
    const perfilUsuario = perfil.perfilSelecionado;
    const routeName = route.name as string;
    const codProcesso = route.params.codProcesso as string;
    const siglaUnidade = route.params.siglaUnidade as string;
    const codUnidade = route.params.codUnidade as string;

    const isProcessoRoute = routeName === "Processo";
    const isSubprocessoRoute = ["Subprocesso", "SubprocessoMapa", "SubprocessoVisMapa", "SubprocessoCadastro", "SubprocessoVisCadastro"].includes(routeName);
    const isUnidadeRoute = ["Unidade", "Mapa", "AtribuicaoTemporariaForm"].includes(routeName);

    breadcrumbs.push(
      ...getProcessoBreadcrumbs(codProcesso, isProcessoRoute, isSubprocessoRoute, perfilUsuario),
      ...getSubprocessoBreadcrumbs(codProcesso, siglaUnidade, isSubprocessoRoute, routeName),
      ...getUnidadeBreadcrumbs(codUnidade, isUnidadeRoute, routeName, perfilUsuario)
    );

    if (!isProcessoRoute && !isSubprocessoRoute && !isUnidadeRoute) {
      addFallbackBreadcrumbs(breadcrumbs);
    }

    return breadcrumbs;
  });

  return { breadcrumbs: crumbs };
}
