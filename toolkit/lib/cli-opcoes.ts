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

export {lerNumero, lerOpcao};
