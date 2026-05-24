import type {Unidade} from "@/types/tipos";

type UnidadeVisual = Unidade & { agrupadorVisual?: boolean };

export function mapearHierarquia(unidades: Unidade[]) {
    const pMap = new Map<number, Unidade>();
    const uMap = new Map<number, Unidade>();

    const traverse = (node: Unidade, parent?: Unidade) => {
        const nodeVisual = node as UnidadeVisual;
        if (nodeVisual.agrupadorVisual) {
            if (node.filhas) {
                node.filhas.forEach(child => traverse(child, parent));
            }
            return;
        }

        uMap.set(node.codigo, node);
        if (parent) {
            pMap.set(node.codigo, parent);
        }
        if (node.filhas) {
            node.filhas.forEach(child => traverse(child, node));
        }
    };

    unidades.forEach(u => traverse(u));
    return {parentMap: pMap, unitMap: uMap};
}

export function coletarCodigosElegiveis(unidades: Unidade[]): Set<number> {
    const codigosElegiveis = new Set<number>();
    const visitar = (unidade: Unidade) => {
        if (unidade.isElegivel === true) {
            codigosElegiveis.add(unidade.codigo);
        }
        (unidade.filhas ?? []).forEach(visitar);
    };
    unidades.forEach(visitar);
    return codigosElegiveis;
}

export function getTodasSubunidades(unidade: Unidade): Unidade[] {
    const result: Unidade[] = [];
    if (unidade.filhas) {
        for (const filha of unidade.filhas) {
            result.push(filha);
            if (filha.filhas) {
                result.push(...getTodasSubunidades(filha));
            }
        }
    }
    return result;
}

export function ehAgrupadorVisual(unidade: Unidade): boolean {
    return Boolean((unidade as UnidadeVisual).agrupadorVisual);
}

export function ordenarCodigos(codigos: number[]): number[] {
    return [...codigos].sort((a, b) => a - b);
}
