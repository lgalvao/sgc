import {coletarCodigosElegiveis} from "@/components/unidade/arvoreSelecaoHelpers";
import type {Unidade, UnidadeSelecao} from "@/types/tipos";

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

export function listarUnidadesComEquipePropriaSelecionadas(
    unidadesArvore: Unidade[],
    codigosSelecionados: number[]
): UnidadeSelecao[] {
    const codigosSelecionadosSet = new Set(codigosSelecionados);
    const selecionadas: UnidadeSelecao[] = [];

    const visitar = (unidade: Unidade) => {
        if (unidade.tipo === "INTEROPERACIONAL" && codigosSelecionadosSet.has(unidade.codigo)) {
            selecionadas.push({
                codigo: unidade.codigo,
                sigla: unidade.sigla,
                nome: unidade.nome,
                situacao: "",
            });
        }

        (unidade.filhas ?? []).forEach(visitar);
    };

    unidadesArvore.forEach(visitar);
    return selecionadas;
}

export function aplicarSelecaoDiretaUnidadesComEquipePropria(
    codigosSelecionados: number[],
    codigosComEquipePropria: number[],
    codigosConfirmados: number[]
): number[] {
    const codigosComEquipePropriaSet = new Set(codigosComEquipePropria);
    const codigosConfirmadosSet = new Set(codigosConfirmados);

    return codigosSelecionados.filter((codigo) =>
        !codigosComEquipePropriaSet.has(codigo) || codigosConfirmadosSet.has(codigo)
    );
}
