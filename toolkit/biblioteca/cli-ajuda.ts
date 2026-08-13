import {escreverLinha} from "./saida.js";

interface OpcoesAjudaComando {
    comandoToolkit: string;
    descricao?: string;
    argumentos?: string | null;
    opcoes?: string[];
    exemplos?: string[];
}

function exibirAjudaComando({
    comandoToolkit,
    descricao,
    argumentos = null,
    opcoes = [],
    exemplos = []
}: OpcoesAjudaComando): void {
    const linhas: string[] = [];
    linhas.push(`Uso recomendado: ferramentas ${comandoToolkit}${argumentos ? ` ${argumentos}` : ''}`);

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
