import {escreverLinha} from "./saida.js";

interface OpcoesAjudaComando {
    comandoSgc: string;
    scriptDireto: string;
    descricao?: string;
    argumentos?: string | null;
    opcoes?: string[];
    exemplos?: string[];
}

function exibirAjudaComando({
    comandoSgc,
    scriptDireto,
    descricao,
    argumentos = null,
    opcoes = [],
    exemplos = []
}: OpcoesAjudaComando): void {
    const linhas: string[] = [];
    linhas.push(`Uso recomendado: npx tsx toolkit/sgc.js ${comandoSgc}${argumentos ? ` ${argumentos}` : ''}`);

    linhas.push(`Execucao direta: npx tsx toolkit/${scriptDireto}${argumentos ? ` ${argumentos}` : ''}`);

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
