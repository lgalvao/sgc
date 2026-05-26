/**
 * Utilitários para manipulação de estruturas de árvore hierárquicas.
 */

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
    return a.localeCompare(b, "pt-BR", {sensitivity: "base", numeric: true});
}

function obterTextoOrdenacao<T extends object>(item: T, config: ConfiguracaoOrganizacaoArvore<T>): string {
    const sigla = config.obterSigla(item);
    if (sigla !== undefined && sigla !== null) return sigla;
    const rotulo = config.obterRotulo(item);
    return rotulo !== undefined && rotulo !== null ? rotulo : "";
}

function ehZonaEleitoralPorMetadados(item: {
    tipo?: string;
    sigla?: string;
    nome?: string;
}): boolean {
    return ehTextoZonaEleitoral(item.tipo)
        || ehSiglaZonaEleitoral(item.sigla)
        || ehTextoZonaEleitoral(item.nome);
}

function ehSecretariaPorMetadados(item: {
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
        const filhosBrutos = config.obterFilhos(item);
        const filhos = organizarArvoreUnidades(
            filhosBrutos ? filhosBrutos : [],
            config.criarIdentificadorGrupoFilhos(item),
            config
        );

        const itemNormalizado = config.clonarComFilhos(item, filhos);
        const rotuloObtido = config.obterRotulo(itemNormalizado);
        const nome = rotuloObtido ? rotuloObtido : "";

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
