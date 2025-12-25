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
  const crumbs = computed((): Breadcrumb[] => {
    const breadcrumbs: Breadcrumb[] = [];
    const perfil = usePerfilStore();
    const unidadesStore = useUnidadesStore();
    const perfilUsuario = perfil.perfilSelecionado;

    // Add home breadcrumb
    breadcrumbs.push({ label: "Painel", to: { name: "Painel" }, isHome: true });

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
          to: isProcessoRoute ? undefined : { name: "Processo", params: { codProcesso } },
        });
      }
    }

    // Adiciona breadcrumb do subprocesso (sigla da unidade)
    if (siglaUnidade && isSubprocessoRoute) {
      breadcrumbs.push({
        label: siglaUnidade,
        to: routeName === "Subprocesso" ? undefined : { name: "Subprocesso", params: { codProcesso, siglaUnidade } },
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
        to: routeName === "Unidade" ? undefined : { name: "Unidade", params: { codUnidade } },
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
        const { meta, name } = routeRecord;

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
    }

    return breadcrumbs;
  });

  return { breadcrumbs: crumbs };
}
