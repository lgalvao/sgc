import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {describe, expect, test} from "vitest";
import {execa, type Options} from "execa";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "toolkit", "sgc.ts");
const CAMINHO_TSX = path.join(DIRETORIO_RAIZ, "node_modules", ".bin", process.platform === "win32" ? "tsx.cmd" : "tsx");
const FIXTURE_FOTOGRAFIA = path.join(DIRETORIO_RAIZ, "toolkit", "test", "fixtures", "qualidade", "fotografia.json");

interface ResultadoExecucao {
    exitCode?: number;
    stdout: string;
    stderr: string;
}

async function executarSgc(args: string[], opcoes: Options = {}): Promise<ResultadoExecucao> {
    const resultado = await execa(CAMINHO_TSX, [CAMINHO_SGC, ...args], {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
    return {
        exitCode: resultado.exitCode,
        stdout: String(resultado.stdout),
        stderr: String(resultado.stderr)
    };
}

describe("Qualidade do toolkit", () => {
    test("resume uma fotografia de qualidade a partir de fixture", async () => {
        const resultado = await executarSgc(["qualidade", "resumo", "--json", "--arquivo", FIXTURE_FOTOGRAFIA]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(json.resumo.statusGeral).toBe("verde");
        expect(json.hotspots).toHaveLength(2);
    });

    test("resume a fotografia mais recente a partir da base externa", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-resumo-base-"));
        const caminhoFotografia = path.join(diretorioBase, "toolkit", "qualidade", "artefatos", "mais-recente", "fotografia.json");
        await fs.ensureDir(path.dirname(caminhoFotografia));
        await fs.writeJson(caminhoFotografia, {
            resumo: {
                statusGeral: "verde",
                indiceSaude: 98,
                totais: {verificacoes: 1}
            },
            verificacoes: [],
            hotspots: []
        });

        const resultado = await executarSgc(["qualidade", "resumo", "--json", "--base", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        const json = JSON.parse(resultado.stdout);
        expect(json.resumo.statusGeral).toBe("verde");
        expect(json.resumo.indiceSaude).toBe(98);
    });

    test("exibe ajuda de coleta de fotografia com opcao de perfil", async () => {
        const resultado = await executarSgc(["qualidade", "coletar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Perfil de execucao");
        expect(resultado.stdout).toContain("rapido");
    });

    test("falha rapido para perfil invalido de fotografia", async () => {
        const resultado = await executarSgc(["qualidade", "coletar", "--perfil", "inexistente"]);
        expect(resultado.exitCode).toBe(1);
        expect(resultado.stderr).toContain("Perfil invalido");
    });
}, 30000);
