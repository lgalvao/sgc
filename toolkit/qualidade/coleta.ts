import {executarTsx} from "../biblioteca/execucao.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import logger from "../biblioteca/logger.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";

const PERFIS_VALIDOS = new Set<string>(["rapido", "completo", "servidor", "cliente"]);

function normalizarArgumentosColeta(argumentos: string[] = []): string[] {
    const resultado = [];

    for (let indice = 0; indice < argumentos.length; indice += 1) {
        const atual = argumentos[indice];

        if (atual === "--perfil") {
            const perfil = argumentos[indice + 1];
            if (!perfil) {
                throw new Error("Informe um valor para --perfil (rapido, completo, servidor ou cliente).");
            }

            if (!PERFIS_VALIDOS.has(perfil)) {
                throw new Error(`Perfil invalido: ${perfil}. Use rapido, completo, servidor ou cliente.`);
            }

            resultado.push("--perfil", perfil);
            indice += 1;
            continue;
        }

        if (atual.startsWith("--perfil=")) {
            const perfil = atual.split("=", 2)[1];
            if (!PERFIS_VALIDOS.has(perfil)) {
                throw new Error(`Perfil invalido: ${perfil}. Use rapido, completo, servidor ou cliente.`);
            }
        }

        if (atual === "--base") {
            const base = argumentos[indice + 1];
            if (!base) {
                throw new Error("Informe um valor para --base.");
            }

            resultado.push("--base", base);
            indice += 1;
            continue;
        }

        if (atual.startsWith("--base=") && atual.slice("--base=".length).length === 0) {
            throw new Error("Informe um valor para --base.");
        }

        resultado.push(atual);
    }

    return resultado;
}

async function executarColetaQualidade(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoToolkit: "qualidade coletar",
            descricao: "Coleta uma fotografia de qualidade usando os adaptadores e perfis do SGC.",
            opcoes: [
                "--perfil <perfil>   Perfil de execucao: rapido, completo, servidor ou cliente.",
                "--base <diretorio>  Sobrescreve o diretorio base do projeto auditado."
            ],
            exemplos: [
                "ferramentas qualidade coletar --perfil rapido",
                "ferramentas qualidade coletar --perfil cliente"
            ]
        });
        return;
    }

    const argumentosNormalizados = normalizarArgumentosColeta(argumentos);
    await executarTsx("toolkit/qualidade/coleta-execucao.ts", argumentosNormalizados);
}

if (ehEntradaPrincipal(import.meta.url)) {
    executarColetaQualidade().catch((erro: unknown) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        logger.error(`Erro ao coletar fotografia de qualidade: ${mensagem}`);
        process.exitCode = 1;
    });
}

export {
    executarColetaQualidade
};
