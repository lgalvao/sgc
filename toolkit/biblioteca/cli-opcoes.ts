interface EsquemaArgumentos {
    opcoesComValor: readonly string[];
    opcoesBooleanas: readonly string[];
    minimoPosicionais: number;
    maximoPosicionais: number;
}

function ehValorNumericoNegativo(argumento: string): boolean {
    return /^-\d+(?:\.\d+)?$/.test(argumento);
}

function ehOpcao(argumento: string): boolean {
    return argumento.startsWith("-") && !ehValorNumericoNegativo(argumento);
}

function nomeOpcao(argumento: string): string {
    const indiceIgualdade = argumento.indexOf("=");
    return indiceIgualdade >= 0 ? argumento.slice(0, indiceIgualdade) : argumento;
}

/**
 * Valida e normaliza os argumentos de um comando encaminhado pela CLI.
 *
 * A forma `--opcao=valor` é convertida para `--opcao valor` para que scripts
 * antigos e novos recebam exatamente o mesmo formato interno.
 */
function validarArgumentos(argumentos: readonly string[], esquema: EsquemaArgumentos): string[] {
    const opcoesComValor = new Set(esquema.opcoesComValor);
    const opcoesBooleanas = new Set(["--help", "-h", ...esquema.opcoesBooleanas]);
    const normalizados: string[] = [];
    let posicionais = 0;

    for (let indice = 0; indice < argumentos.length; indice += 1) {
        const argumento = argumentos[indice];
        if (!ehOpcao(argumento)) {
            normalizados.push(argumento);
            posicionais += 1;
            continue;
        }

        const nome = nomeOpcao(argumento);
        const valorAtribuido = argumento.includes("=")
            ? argumento.slice(argumento.indexOf("=") + 1)
            : undefined;

        if (opcoesBooleanas.has(nome)) {
            if (valorAtribuido !== undefined) {
                throw new Error(`A opção ${nome} não recebe valor.`);
            }
            normalizados.push(argumento);
            continue;
        }

        if (!opcoesComValor.has(nome)) {
            throw new Error(`Opção desconhecida: ${argumento}.`);
        }

        if (valorAtribuido !== undefined) {
            if (!valorAtribuido) {
                throw new Error(`Informe um valor para ${nome}.`);
            }
            normalizados.push(nome, valorAtribuido);
            continue;
        }

        const valor = argumentos[indice + 1];
        if (!valor || ehOpcao(valor)) {
            throw new Error(`Informe um valor para ${nome}.`);
        }
        normalizados.push(nome, valor);
        indice += 1;
    }

    const exibirAjuda = normalizados.includes("--help") || normalizados.includes("-h");
    const quantidadeInvalida = posicionais > esquema.maximoPosicionais
        || (!exibirAjuda && posicionais < esquema.minimoPosicionais);
    if (quantidadeInvalida) {
        const limite = esquema.minimoPosicionais === esquema.maximoPosicionais
            ? String(esquema.maximoPosicionais)
            : `${esquema.minimoPosicionais} a ${esquema.maximoPosicionais}`;
        throw new Error(`Quantidade de argumentos posicionais inválida: esperado ${limite}, recebido ${posicionais}.`);
    }

    return normalizados;
}

/**
 * Lê uma opção que recebe valor, preservando o valor padrão quando ausente.
 */
function lerOpcao(argumentos: string[], nome: string, padrao: string | undefined): string | undefined {
    const indice = argumentos.indexOf(nome);
    if (indice >= 0) {
        const valor = argumentos[indice + 1];
        if (!valor || valor.startsWith("--")) {
            throw new Error(`Informe um valor para ${nome}.`);
        }
        return valor;
    }

    const prefixoAtribuicao = `${nome}=`;
    const argumentoAtribuido = argumentos.find(argumento => argumento.startsWith(prefixoAtribuicao));
    if (argumentoAtribuido) {
        const valor = argumentoAtribuido.slice(prefixoAtribuicao.length);
        if (!valor) {
            throw new Error(`Informe um valor para ${nome}.`);
        }
        return valor;
    }

    return padrao;
}

interface RegrasNumero {
    inteiro?: boolean;
    minimo?: number;
    maximo?: number;
}

/**
 * Lê e valida uma opção numérica da CLI.
 *
 * A conversão é estrita: valores parciais como `10abc` e números fora dos
 * limites declarados falham imediatamente, evitando resultados silenciosos
 * com `NaN` ou intervalos inválidos.
 */
function lerNumero(
    argumentos: string[],
    nome: string,
    padrao: number | undefined,
    regras: RegrasNumero = {}
): number | undefined {
    const valor = lerOpcao(argumentos, nome, padrao === undefined ? undefined : String(padrao));
    if (valor === undefined) {
        return undefined;
    }

    const numero = Number(valor);
    const inteiro = regras.inteiro ?? true;
    if (!Number.isFinite(numero) || (inteiro && !Number.isInteger(numero))) {
        throw new Error(`A opção ${nome} deve receber ${inteiro ? "um número inteiro" : "um número"} válido.`);
    }
    if (regras.minimo !== undefined && numero < regras.minimo) {
        throw new Error(`A opção ${nome} deve ser maior ou igual a ${regras.minimo}.`);
    }
    if (regras.maximo !== undefined && numero > regras.maximo) {
        throw new Error(`A opção ${nome} deve ser menor ou igual a ${regras.maximo}.`);
    }

    return numero;
}

export {lerNumero, lerOpcao, validarArgumentos};

export type {EsquemaArgumentos};
