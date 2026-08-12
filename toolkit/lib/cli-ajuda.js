import {escreverLinha} from "./saida.js";

/**
 * @typedef {object} OpcoesAjudaComando
 * @property {string} comandoSgc
 * @property {string} scriptDireto
 * @property {string} [descricao]
 * @property {string|null} [argumentos]
 * @property {string[]} [opcoes]
 * @property {string[]} [exemplos]
 */

/**
 * @param {OpcoesAjudaComando} opcoes
 * @returns {void}
 */
function exibirAjudaComando({
                                comandoSgc,
                                scriptDireto,
                                descricao,
                                argumentos = null,
                                opcoes = [],
                                exemplos = []
                            }) {
    const linhas = [];
    linhas.push(`Uso recomendado: node toolkit/sgc.js ${comandoSgc}${argumentos ? ` ${argumentos}` : ''}`);

    linhas.push(`Execucao direta: node --import=tsx toolkit/${scriptDireto}${argumentos ? ` ${argumentos}` : ''}`);

    if (descricao) {
        linhas.push('');
        linhas.push(descricao);
    }

    if (opcoes.length > 0) {
        linhas.push('');
        linhas.push('Opcoes:');
        opcoes.forEach(opcao => linhas.push(`  ${opcao}`));
    }

    if (exemplos.length > 0) {
        linhas.push('');
        linhas.push('Exemplos:');
        exemplos.forEach(exemplo => linhas.push(`  ${exemplo}`));
    }

    escreverLinha(linhas.join('\n'));
}

export {
    exibirAjudaComando
};
