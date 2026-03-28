function exibirAjudaComando({
    comandoSgc,
    scriptDireto,
    descricao,
    argumentos = null,
    opcoes = [],
    exemplos = []
}) {
    const linhas = [];
    linhas.push(`Uso recomendado: node backend/etc/scripts/sgc.cjs ${comandoSgc}`);

    if (argumentos) {
        linhas[0] += ` ${argumentos}`;
    }

    linhas.push(`Execucao direta: node backend/etc/scripts/${scriptDireto}${argumentos ? ` ${argumentos}` : ''}`);

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

    console.log(linhas.join('\n'));
}

module.exports = {
    exibirAjudaComando
};
