import {spawn} from "node:child_process";
import path from "node:path";
import {fileURLToPath, pathToFileURL} from "node:url";

type ExecutarComando = (
    comando: string,
    argumentos: readonly string[],
    diretorio: string
) => Promise<{codigoSaida: number}>;

function lerOpcao(argumentos: readonly string[], nome: string, padrao: string | undefined): string | undefined {
    const indice = argumentos.indexOf(nome);
    if (indice >= 0) {
        const valor = argumentos[indice + 1];
        if (!valor || valor.startsWith("--")) {
            throw new Error(`Informe um valor para ${nome}.`);
        }
        return valor;
    }

    const prefixo = `${nome}=`;
    const argumentoAtribuido = argumentos.find(argumento => argumento.startsWith(prefixo));
    if (argumentoAtribuido) {
        const valor = argumentoAtribuido.slice(prefixo.length);
        if (!valor) {
            throw new Error(`Informe um valor para ${nome}.`);
        }
        return valor;
    }

    return padrao;
}

function removerOpcoesLocais(argumentos: readonly string[], nomes: ReadonlySet<string>): string[] {
    const resultado: string[] = [];
    for (let indice = 0; indice < argumentos.length; indice += 1) {
        const argumento = argumentos[indice];
        const nome = argumento.split("=", 1)[0];
        if (!nomes.has(nome)) {
            resultado.push(argumento);
            continue;
        }
        if (!argumento.includes("=")) {
            indice += 1;
        }
    }
    return resultado;
}

function caminhoRaizProjeto(): string {
    return path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
}

function ehEntradaPrincipal(urlModulo: string): boolean {
    return Boolean(process.argv[1] && urlModulo === pathToFileURL(process.argv[1]).href);
}

const executarComandoPadrao: ExecutarComando = async (comando, argumentos, diretorio) => new Promise((resolve, reject) => {
    const processo = spawn(comando, argumentos, {
        cwd: diretorio,
        stdio: "inherit",
        shell: process.platform === "win32"
    });
    processo.once("error", reject);
    processo.once("close", codigo => resolve({codigoSaida: codigo ?? 1}));
});

interface OpcoesAjuda {
    uso: string;
    descricao: string;
    opcoes?: readonly string[];
    exemplos?: readonly string[];
}

function exibirAjuda({uso, descricao, opcoes = [], exemplos = []}: OpcoesAjuda): void {
    const linhas = [`Uso: ${uso}`, "", descricao];
    if (opcoes.length > 0) {
        linhas.push("", "Opcoes:", ...opcoes.map(opcao => `  ${opcao}`));
    }
    if (exemplos.length > 0) {
        linhas.push("", "Exemplos:", ...exemplos.map(exemplo => `  ${exemplo}`));
    }
    console.log(linhas.join("\n"));
}

export {
    caminhoRaizProjeto,
    ehEntradaPrincipal,
    executarComandoPadrao,
    exibirAjuda,
    lerOpcao,
    removerOpcoesLocais
};

export type {ExecutarComando, OpcoesAjuda};
