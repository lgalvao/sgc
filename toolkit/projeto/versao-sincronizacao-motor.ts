import {existsSync, readFileSync, writeFileSync} from "node:fs";
import path from "node:path";

type FormatoAlvoVersao = "propriedadesGradle" | "manifestoNpm";

interface AlvoVersao {
    caminho: string;
    formato: FormatoAlvoVersao;
}

interface OpcoesSincronizacaoVersao {
    novaVersao: string;
    diretorioBase: string;
    alvos: readonly AlvoVersao[];
    gravar?: boolean;
}

interface ResultadoSincronizacao {
    novaVersao: string;
    arquivosAtualizados: string[];
    arquivosPendentes: string[];
    gravado: boolean;
}

function sincronizarPropriedadesGradle(caminho: string, novaVersao: string, gravar: boolean): boolean {
    if (!existsSync(caminho)) {
        return false;
    }

    const conteudo = readFileSync(caminho, "utf-8");
    if (!/^version\s*=.*$/m.test(conteudo)) {
        return false;
    }

    const conteudoAtualizado = conteudo.replace(/^version\s*=.*$/m, `version=${novaVersao}`);
    if (conteudoAtualizado === conteudo) {
        return false;
    }

    if (gravar) {
        writeFileSync(caminho, conteudoAtualizado, "utf-8");
    }
    return true;
}

function sincronizarManifestoNpm(caminho: string, novaVersao: string, gravar: boolean): boolean {
    if (!existsSync(caminho)) {
        return false;
    }

    const pacote = JSON.parse(readFileSync(caminho, "utf-8")) as Record<string, unknown>;
    if (pacote.version === novaVersao) {
        return false;
    }

    if (gravar) {
        pacote.version = novaVersao;
        writeFileSync(caminho, `${JSON.stringify(pacote, null, 2)}\n`, "utf-8");
    }
    return true;
}

function sincronizarVersao({
    novaVersao,
    diretorioBase,
    alvos,
    gravar = false
}: OpcoesSincronizacaoVersao): ResultadoSincronizacao {
    if (!novaVersao) {
        throw new Error("Informe a versão que deve ser sincronizada.");
    }

    const baseResolvida = path.resolve(diretorioBase);
    const arquivosAtualizados: string[] = [];
    const arquivosPendentes: string[] = [];

    for (const alvo of alvos) {
        const caminho = path.isAbsolute(alvo.caminho) ? alvo.caminho : path.resolve(baseResolvida, alvo.caminho);
        const alterado = alvo.formato === "propriedadesGradle"
            ? sincronizarPropriedadesGradle(caminho, novaVersao, gravar)
            : sincronizarManifestoNpm(caminho, novaVersao, gravar);
        if (!alterado) {
            continue;
        }

        const nomeRelativo = path.relative(baseResolvida, caminho).replaceAll(path.sep, "/");
        arquivosPendentes.push(nomeRelativo);
        if (gravar) {
            arquivosAtualizados.push(nomeRelativo);
        }
    }

    return {novaVersao, arquivosAtualizados, arquivosPendentes, gravado: gravar};
}

export {sincronizarVersao};

export type {AlvoVersao, ResultadoSincronizacao};
