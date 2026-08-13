import fs from "node:fs/promises";
import path from "node:path";
import {execa} from "execa";

interface OpcoesDiffContratos {
    base: string;
    anterior: string;
    atual: string;
}

interface ResultadoDiffContratos {
    base: string;
    anterior: string;
    atual: string;
    houveMudancas: boolean;
    modo: "identico" | "diferencaTextual";
    saidaPadrao: string;
    saidaErro: string;
}

function resolverArquivo(base: string, arquivo: string): string {
    return path.isAbsolute(arquivo) ? arquivo : path.resolve(base, arquivo);
}

async function executarDiffContratos({base, anterior, atual}: OpcoesDiffContratos): Promise<ResultadoDiffContratos> {
    const baseResolvida = path.resolve(base);
    const anteriorResolvido = resolverArquivo(baseResolvida, anterior);
    const atualResolvido = resolverArquivo(baseResolvida, atual);
    const [conteudoAnterior, conteudoAtual] = await Promise.all([
        fs.readFile(anteriorResolvido, "utf-8"),
        fs.readFile(atualResolvido, "utf-8")
    ]);

    if (conteudoAnterior === conteudoAtual) {
        return {
            base: baseResolvida,
            anterior: anteriorResolvido,
            atual: atualResolvido,
            houveMudancas: false,
            modo: "identico",
            saidaPadrao: "Nenhuma diferença detectada entre a referência e a fotografia atual.",
            saidaErro: ""
        };
    }

    const resultado = await execa("git", ["diff", "--no-index", "--minimal", "--unified=3", anteriorResolvido, atualResolvido], {
        reject: false,
        cwd: baseResolvida
    });

    const codigoSaida = resultado.exitCode ?? 0;
    if (codigoSaida > 1) {
        throw new Error(resultado.stderr || `git diff terminou com codigo ${codigoSaida}.`);
    }

    return {
        base: baseResolvida,
        anterior: anteriorResolvido,
        atual: atualResolvido,
        houveMudancas: true,
        modo: "diferencaTextual",
        saidaPadrao: resultado.stdout ?? "",
        saidaErro: resultado.stderr ?? ""
    };
}

export {executarDiffContratos};
