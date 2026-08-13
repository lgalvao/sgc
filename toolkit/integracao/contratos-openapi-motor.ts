import fs from "node:fs/promises";
import path from "node:path";

interface OpcoesExportarOpenapi {
    base: string;
    url: string;
    saida: string;
}

interface DocumentoOpenapi {
    [chave: string]: unknown;
}

interface ResultadoExportacaoOpenapi {
    base: string;
    url: string;
    saida: string;
    titulo: string | null;
    versao: string | null;
    quantidadeRotas: number;
}

function ehObjeto(valor: unknown): valor is DocumentoOpenapi {
    return typeof valor === "object" && valor !== null && !Array.isArray(valor);
}

function obterTexto(valor: unknown): string | null {
    return typeof valor === "string" ? valor : null;
}

async function exportarOpenapi({base, url, saida}: OpcoesExportarOpenapi): Promise<ResultadoExportacaoOpenapi> {
    const baseResolvida = path.resolve(base);
    const saidaResolvida = path.isAbsolute(saida) ? saida : path.resolve(baseResolvida, saida);
    const resposta = await fetch(url, {
        headers: {
            Accept: "application/json"
        }
    });

    if (!resposta.ok) {
        throw new Error(`Falha ao buscar OpenAPI em ${url}: HTTP ${resposta.status}`);
    }

    const json = await resposta.json() as unknown;
    if (!ehObjeto(json)) {
        throw new Error("O endpoint OpenAPI retornou um JSON que nao representa um documento.");
    }

    const info = ehObjeto(json.info) ? json.info : {};
    const rotas = ehObjeto(json.paths) ? json.paths : {};
    await fs.mkdir(path.dirname(saidaResolvida), {recursive: true});
    await fs.writeFile(saidaResolvida, `${JSON.stringify(json, null, 2)}\n`, "utf-8");

    return {
        base: baseResolvida,
        url,
        saida: saidaResolvida,
        titulo: obterTexto(info.title),
        versao: obterTexto(info.version),
        quantidadeRotas: Object.keys(rotas).length
    };
}

export {exportarOpenapi};
