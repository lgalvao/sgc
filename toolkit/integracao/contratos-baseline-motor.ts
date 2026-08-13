import fs from "node:fs/promises";
import path from "node:path";

interface OpcoesFixarBaselineContrato {
    base: string;
    origem: string;
    destino: string;
}

interface ResultadoFixacaoBaselineContrato {
    base: string;
    origem: string;
    destino: string;
}

function resolverArquivo(base: string, arquivo: string): string {
    return path.isAbsolute(arquivo) ? arquivo : path.resolve(base, arquivo);
}

async function fixarBaselineContrato({base, origem, destino}: OpcoesFixarBaselineContrato): Promise<ResultadoFixacaoBaselineContrato> {
    const baseResolvida = path.resolve(base);
    const origemResolvida = resolverArquivo(baseResolvida, origem);
    const destinoResolvido = resolverArquivo(baseResolvida, destino);
    await fs.mkdir(path.dirname(destinoResolvido), {recursive: true});
    await fs.copyFile(origemResolvida, destinoResolvido);
    return {base: baseResolvida, origem: origemResolvida, destino: destinoResolvido};
}

export {fixarBaselineContrato};
