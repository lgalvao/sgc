import {computed} from 'vue';
import {type RouteLocationNamedRaw, type RouteLocationNormalizedLoaded} from 'vue-router';
import {usePerfilStore} from '@/stores/perfil';
import {Perfil} from '@/types/tipos';
import {useUnidadeAtual} from '@/composables/useUnidadeAtual';

export interface Breadcrumb {
    label: string;
    to?: RouteLocationNamedRaw;
    isHome?: boolean;
}

interface MetaBreadcrumb {
    breadcrumb?: string | ((route: RouteLocationNormalizedLoaded) => string);
    title?: string;
}

const TITULOS_PAGINA_SUBPROCESSO: Record<string, string> = {
    SubprocessoMapa: "Mapa de competências",
    SubprocessoCadastro: "Atividades e conhecimentos",
};

const TITULOS_PAGINA_UNIDADE: Record<string, string> = {
    Mapa: "Mapa de competências",
    AtribuicaoTemporariaForm: "Atribuição temporária",
};

export function useBreadcrumbs(route: RouteLocationNormalizedLoaded) {
    const perfil = usePerfilStore();
    const {unidadeAtual} = useUnidadeAtual();

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
                    to: isProcessoRoute ? undefined : {name: "Processo", params: {codProcesso}},
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
                to: routeName === "Subprocesso" ? undefined : {
                    name: "Subprocesso",
                    params: {codProcesso, siglaUnidade}
                },
            });

            const pageTitle = TITULOS_PAGINA_SUBPROCESSO[routeName];
            if (pageTitle) {
                crumbs.push({label: pageTitle});
            }
        }
        return crumbs;
    };

    const getUnidadeBreadcrumbs = (
        codUnidade: string,
        isUnidadeRoute: boolean,
        routeName: string,
    ): Breadcrumb[] => {
        const crumbs: Breadcrumb[] = [];
        if (codUnidade && isUnidadeRoute) {
            crumbs.push({
                label: unidadeAtual.value?.sigla ?? `Unidade ${codUnidade}`,
                to: routeName === "Unidade" ? undefined : {name: "Unidade", params: {codUnidade}},
            });

            const unidadePageTitle = TITULOS_PAGINA_UNIDADE[routeName];
            if (unidadePageTitle) {
                crumbs.push({label: unidadePageTitle});
            }
        }
        return crumbs;
    };

    const addFallbackBreadcrumbs = (breadcrumbs: Breadcrumb[]) => {
        route.matched.forEach((routeRecord) => {
            const {meta, name} = routeRecord;
            const metaBreadcrumb = meta as MetaBreadcrumb;
            const label = metaBreadcrumb.breadcrumb
                ? typeof metaBreadcrumb.breadcrumb === "function"
                    ? metaBreadcrumb.breadcrumb(route)
                    : metaBreadcrumb.breadcrumb
                : metaBreadcrumb.title;

            if (label && (breadcrumbs.length === 0 || breadcrumbs.at(-1)?.label !== label)) {
                breadcrumbs.push({
                    label,
                    to: {name: name as string, params: route.params},
                });
            }
        });

        if (breadcrumbs.length > 0) {
            const last = breadcrumbs.at(-1);
            if (last) last.to = undefined;
        }
    };

    const crumbs = computed((): Breadcrumb[] => {
        const routeName = route.name as string;
        const breadcrumbs: Breadcrumb[] = [{
            label: "Painel",
            to: routeName === "Painel" ? undefined : {name: "Painel"},
            isHome: true
        }];
        const perfilUsuario = perfil.perfilSelecionado;
        const codProcesso = route.params.codProcesso as string;
        const siglaUnidade = route.params.siglaUnidade as string;
        const codUnidade = route.params.codUnidade as string;

        const isProcessoRoute = routeName === "Processo";
        const isSubprocessoRoute = ["Subprocesso", "SubprocessoMapa", "SubprocessoCadastro"].includes(routeName);
        const isUnidadeRoute = ["Unidade", "Mapa", "AtribuicaoTemporariaForm"].includes(routeName);

        breadcrumbs.push(
            ...getProcessoBreadcrumbs(codProcesso, isProcessoRoute, isSubprocessoRoute, perfilUsuario),
            ...getSubprocessoBreadcrumbs(codProcesso, siglaUnidade, isSubprocessoRoute, routeName),
            ...getUnidadeBreadcrumbs(codUnidade, isUnidadeRoute, routeName)
        );

        if (!isProcessoRoute && !isSubprocessoRoute && !isUnidadeRoute) {
            addFallbackBreadcrumbs(breadcrumbs);
        }

        return breadcrumbs;
    });

    return {breadcrumbs: crumbs};
}
