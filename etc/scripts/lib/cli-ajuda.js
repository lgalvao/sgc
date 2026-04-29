function exibirAjudaComando({
    comandoSgc,
    scriptDireto,
    descricao,
    argumentos = null,
    opcoes = [],
    exemplos = []
}) {
    const linhas = [];
    linhas.push(`Uso recomendado: node etc/scripts/sgc.js ${comandoSgc}${argumentos ? ` ${argumentos}` : ''}`);

    linhas.push(`Execucao direta: node etc/scripts/${scriptDireto}${argumentos ? ` ${argumentos}` : ''}`);

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

export {
    exibirAjudaComando
};
