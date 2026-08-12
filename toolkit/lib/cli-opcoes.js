/**
 * Lê uma opção que recebe valor, preservando o valor padrão quando ausente.
 *
 * @param {string[]} argumentos
 * @param {string} nome
 * @param {string} padrao
 * @returns {string}
 */
function lerOpcao(argumentos, nome, padrao) {
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

export {
    lerOpcao
};
