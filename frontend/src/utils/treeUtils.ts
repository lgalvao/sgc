/**
 * Utilitários para manipulação de estruturas de árvore hierárquicas.
 */

/**
 * Achata uma estrutura de árvore hierárquica em uma lista plana.
 *
 * @example
 * // Com subordinadas (unidades)
 * const arvore = [
 *   { codigo: 1, subordinadas: [{ codigo: 2 }] },
 *   { codigo: 3 }
 * ];
 * const plano = flattenTree(arvore, 'subordinadas');
 * // Resultado: [{ codigo: 1, ... }, { codigo: 2 }, { codigo: 3 }]
 *
 * @example
 * // Com filhos (processos)
 * const arvore = [
 *   { codigo: 1, filhos: [{ codigo: 2 }] },
 *   { codigo: 3 }
 * ];
 * const plano = flattenTree(arvore, 'filhos');
 * // Resultado: [{ codigo: 1, ... }, { codigo: 2 }, { codigo: 3 }]
 */
export function flattenTree<T extends Record<string, unknown>>(
    items: T[],
    childrenKey: string = 'subordinadas'
): T[] {
    const result: T[] = [];

    for (const item of items) {
        result.push(item);

        if (item[childrenKey] && Array.isArray(item[childrenKey])) {
            result.push(...flattenTree(item[childrenKey], childrenKey));
        }
    }

    return result;
}

type IdentificadorGrupo = number | string;

interface ConfiguracaoOrganizacaoArvore<T extends object> {
    obterCodigo: (item: T) => IdentificadorGrupo;
    obterRotulo: (item: T) => string | undefined;
    obterSigla: (item: T) => string | undefined;
    obterTipo: (item: T) => string | undefined;
    obterFilhos: (item: T) => T[] | undefined;
    clonarComFilhos: (item: T, filhos: T[]) => T;
    criarGrupoZonas: (identificadorGrupo: IdentificadorGrupo, filhos: T[]) => T;
    criarIdentificadorGrupoFilhos: (item: T) => IdentificadorGrupo;
}

export const TITULO_GRUPO_ZONAS_ELEITORAIS = "ZONAS ELEITORAIS";
const TIPO_ZONA_ELEITORAL = "ZONA ELEITORAL";
const TERMO_SECRETARIA = "SECRETARIA";

function ehTextoZonaEleitoral(valor: unknown): boolean {
    return typeof valor === "string"
        && valor.trim().toUpperCase().includes(TIPO_ZONA_ELEITORAL);
}

function ehSiglaZonaEleitoral(valor: unknown): boolean {
    return typeof valor === "string"
        && /Z\.?\s*E\.?/i.test(valor.trim());
}

function ehTextoSecretaria(valor: unknown): boolean {
    return typeof valor === "string"
        && valor.trim().toUpperCase().includes(TERMO_SECRETARIA);
}

function compararTextoPtBr(a: string, b: string): number {
    return a.localeCompare(b, "pt-BR", {sensitivity: "base"});
}

function obterTextoOrdenacao<T extends object>(item: T, config: ConfiguracaoOrganizacaoArvore<T>): string {
    return config.obterSigla(item) ?? config.obterRotulo(item) ?? "";
}

export function ehZonaEleitoralPorMetadados(item: {
    tipo?: string;
    sigla?: string;
    nome?: string;
}): boolean {
    return ehTextoZonaEleitoral(item.tipo)
        || ehSiglaZonaEleitoral(item.sigla)
        || ehTextoZonaEleitoral(item.nome);
}

export function ehSecretariaPorMetadados(item: {
    nome?: string;
}): boolean {
    return ehTextoSecretaria(item.nome);
}

export function organizarArvoreUnidades<T extends object>(
    items: T[],
    identificadorGrupo: IdentificadorGrupo,
    config: ConfiguracaoOrganizacaoArvore<T>
): T[] {
    const secretarias: T[] = [];
    const zonasEleitorais: T[] = [];
    const demais: T[] = [];

    for (const item of items) {
        const filhos = organizarArvoreUnidades(
            config.obterFilhos(item) ?? [],
            config.criarIdentificadorGrupoFilhos(item),
            config
        );

        const itemNormalizado = config.clonarComFilhos(item, filhos);
        const nome = config.obterRotulo(itemNormalizado) ?? "";

        if (ehZonaEleitoralPorMetadados({
            tipo: config.obterTipo(itemNormalizado),
            sigla: config.obterSigla(itemNormalizado),
            nome
        })) {
            zonasEleitorais.push(itemNormalizado);
            continue;
        }

        if (ehSecretariaPorMetadados({nome})) {
            secretarias.push(itemNormalizado);
            continue;
        }

        demais.push(itemNormalizado);
    }

    const ordenarAlfabeticamente = (grupo: T[]) => grupo.sort((a, b) => compararTextoPtBr(
        obterTextoOrdenacao(a, config),
        obterTextoOrdenacao(b, config)
    ));

    ordenarAlfabeticamente(secretarias);
    ordenarAlfabeticamente(zonasEleitorais);
    ordenarAlfabeticamente(demais);

    const resultado = [...secretarias];

    if (zonasEleitorais.length > 0) {
        resultado.push(config.criarGrupoZonas(identificadorGrupo, zonasEleitorais));
    }

    resultado.push(...demais);
    return resultado;
}
