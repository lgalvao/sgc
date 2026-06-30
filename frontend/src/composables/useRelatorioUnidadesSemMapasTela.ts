import {computed, ref} from "vue";
import {buscarCodigosUnidadesSemMapaVigente, buscarTodasUnidades} from "@/services/unidadeService";
import {relatoriosService} from "@/services/relatoriosService";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {useNotification} from "@/composables/useNotification";
import type {Unidade} from "@/types/tipos";
import {organizarArvoreUnidades, TITULO_GRUPO_ZONAS_ELEITORAIS} from "@/utils/treeUtils";

type CardUnidade = {
    chave: string;
    titulo: string;
    subtitulo?: string;
    unidades: Unidade[];
};

export function useRelatorioUnidadesSemMapasTela() {
    const {notify} = useNotification();
    const carregando = ref(false);
    const unidadesSemMapaVigenteArvore = ref<Unidade[]>([]);
    const relatorioVisualizado = ref(false);

    function ordenarComoArvoreUnidades(unidades: Unidade[]): Unidade[] {
        return organizarArvoreUnidades(unidades, "raiz", {
            obterCodigo: (unidade) => unidade.codigo,
            obterRotulo: (unidade) => unidade.nome,
            obterSigla: (unidade) => unidade.sigla,
            obterTipo: (unidade) => unidade.tipo,
            obterFilhos: (unidade) => unidade.filhas,
            clonarComFilhos: (unidade, filhas) => ({
                ...unidade,
                filhas
            }),
            criarGrupoZonas: (_, filhas) => ({
                codigo: -1,
                sigla: TITULO_GRUPO_ZONAS_ELEITORAIS,
                nome: TITULO_GRUPO_ZONAS_ELEITORAIS,
                tipo: "AGRUPADOR_VISUAL",
                filhas
            }),
            criarIdentificadorGrupoFilhos: (unidade) => unidade.codigo
        });
    }

    function filtrarArvoreSemMapaVigente(unidades: Unidade[], codigosSemMapaVigente: Set<number>): Unidade[] {
        return unidades
            .map((unidade): Unidade | null => {
                const filhasValidas = unidade.filhas ? unidade.filhas : [];
                const filhasFiltradas = filtrarArvoreSemMapaVigente(filhasValidas, codigosSemMapaVigente);
                const unidadeSemMapaVigente = codigosSemMapaVigente.has(unidade.codigo);

                if (!unidadeSemMapaVigente && filhasFiltradas.length === 0) {
                    return null;
                }

                return {
                    ...unidade,
                    filhas: filhasFiltradas
                };
            })
            .filter((unidade): unidade is Unidade => !!unidade);
    }

    function filtrarUnidadesExibidas(arvore: Unidade[]): Unidade[] {
        const unidadesExibidas: Unidade[] = [];

        for (const unidade of arvore) {
            if (unidade.filhas && unidade.filhas.length > 0) {
                unidadesExibidas.push(...unidade.filhas);
            }
        }

        return unidadesExibidas;
    }

    const cardsRelatorio = computed<CardUnidade[]>(() => {
        const maesOrdenadas = ordenarComoArvoreUnidades(unidadesSemMapaVigenteArvore.value);

        return maesOrdenadas.map(unidadeMae => {
            const filhasMae = unidadeMae.filhas ? unidadeMae.filhas : [];
            const unidades = unidadeMae.tipo === "AGRUPADOR_VISUAL"
                ? filhasMae
                : ordenarComoArvoreUnidades(filhasMae);

            return {
                chave: `card-unidade-${unidadeMae.codigo}`,
                titulo: unidadeMae.sigla,
                subtitulo: unidadeMae.nome,
                unidades
            };
        });
    });

    async function carregarUnidadesSemMapaVigente() {
        carregando.value = true;
        await Promise.all([
            buscarTodasUnidades(),
            buscarCodigosUnidadesSemMapaVigente()
        ]).then(([arvore, codigosSemMapaVigente]) => {
            const codigosSemMapaVigenteSet = new Set(codigosSemMapaVigente);
            const unidadesExibidas = filtrarUnidadesExibidas(arvore);

            unidadesSemMapaVigenteArvore.value = filtrarArvoreSemMapaVigente(unidadesExibidas, codigosSemMapaVigenteSet);
        }).catch(() => notify(TEXTOS_RELATORIOS.ERRO_BUSCA, "danger"))
            .finally(() => {
                carregando.value = false;
            });
    }

    async function visualizarRelatorio() {
        relatorioVisualizado.value = true;
        await carregarUnidadesSemMapaVigente();
    }

    async function exportarPdf() {
        carregando.value = true;
        await relatoriosService.downloadRelatorioUnidadesSemMapasVigentesPdf()
            .catch(() => notify(TEXTOS_RELATORIOS.ERRO_EXPORTAR, "danger"))
            .finally(() => {
                carregando.value = false;
            });
    }

    return {
        carregando,
        unidadesSemMapaVigenteArvore,
        relatorioVisualizado,
        cardsRelatorio,
        visualizarRelatorio,
        exportarPdf
    };
}
