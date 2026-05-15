import {coletarCodigosElegiveis} from "@/components/unidade/arvoreSelecaoHelpers";
import type {Unidade} from "@/types/tipos";

export function filtrarSelecionadasPorElegibilidade(
    selecionadas: number[],
    unidadesArvore: Unidade[]
): number[] {
    const codigosElegiveis = coletarCodigosElegiveis(unidadesArvore);
    return selecionadas.filter((codigo) => codigosElegiveis.has(codigo));
}

export function removerUnidadesSemEquipe(unidadesArvore: Unidade[]): Unidade[] {
    return unidadesArvore.flatMap((unidade) => {
        const filhasFiltradas = removerUnidadesSemEquipe(unidade.filhas ?? []);

        if (unidade.tipo === "SEM_EQUIPE") {
            return filhasFiltradas;
        }

        return [{
            ...unidade,
            filhas: filhasFiltradas
        }];
    });
}
