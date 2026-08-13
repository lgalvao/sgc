import os from "node:os";
import path from "node:path";
import {access, chmod, mkdir, mkdtemp, readFile, realpath, writeFile} from "node:fs/promises";
import {execa} from "execa";
import {expect, test} from "vitest";

const DIRETORIO_REPOSITORIO = path.resolve(import.meta.dirname, "..", "..");
const NOME_EXECUTAVEL = process.platform === "win32" ? "sgc.cmd" : "sgc";

async function escreverArquivo(caminho: string, conteudo: string): Promise<void> {
    await mkdir(path.dirname(caminho), {recursive: true});
    await writeFile(caminho, conteudo, "utf8");
}

async function existe(caminho: string): Promise<boolean> {
    try {
        await access(caminho);
        return true;
    } catch {
        return false;
    }
}

test("pacote fonte executa em um projeto consumidor isolado", async () => {
    const diretorioTemporario = await mkdtemp(path.join(os.tmpdir(), "sgc-pacote-"));
    const diretorioPacote = path.join(diretorioTemporario, "pacote");
    const diretorioConsumidor = path.join(diretorioTemporario, "consumidor");
    await mkdir(diretorioPacote, {recursive: true});
    await mkdir(diretorioConsumidor, {recursive: true});

    const empacotamento = await execa("npm", [
        "pack",
        "--workspace",
        "toolkit",
        "--pack-destination",
        diretorioPacote,
        "--silent"
    ], {
        cwd: DIRETORIO_REPOSITORIO
    });
    const nomePacote = empacotamento.stdout.trim().split(/\r?\n/).at(-1) ?? "";
    expect(nomePacote).toMatch(/^sgc-scripts-\d+\.\d+\.\d+\.tgz$/);
    const caminhoPacote = path.join(diretorioPacote, nomePacote);

    await execa("npm", ["init", "-y"], {
        cwd: diretorioConsumidor
    });
    await execa("npm", [
        "install",
        caminhoPacote,
        "--ignore-scripts",
        "--no-audit",
        "--no-fund"
    ], {
        cwd: diretorioConsumidor,
        timeout: 60000
    });

    await escreverArquivo(
        path.join(diretorioConsumidor, "frontend", "src", "Exemplo.ts"),
        "export function exemplo(valor: unknown) { return valor; }\n"
    );
    const executavel = path.join(diretorioConsumidor, "node_modules", ".bin", NOME_EXECUTAVEL);
    const resultado = await execa(executavel, [
        "codigo",
        "cheiros",
        "auditar",
        "--json"
    ], {
        cwd: diretorioConsumidor
    });

    expect(await existe(path.join(diretorioConsumidor, "node_modules", "sgc-scripts", "sgc.ts"))).toBe(true);
    expect(JSON.parse(String(resultado.stdout))).toMatchObject({
        base: await realpath(diretorioConsumidor),
        pontuacao: {faixa: "bom"}
    });

    const diretorioBinario = path.join(diretorioTemporario, "bin");
    const caminhoSemgrep = path.join(diretorioBinario, "semgrep");
    await mkdir(diretorioBinario, {recursive: true});
    await escreverArquivo(caminhoSemgrep, [
        "#!/bin/sh",
        "case \"$*\" in",
        "  *sgc-scripts/qualidade/politicas/semgrep/sgc-qualidade.yml*) printf '{\"results\":[]}';;",
        "  *) exit 2;;",
        "esac",
        ""
    ].join("\n"));
    await chmod(caminhoSemgrep, 0o755);
    const auditoriaSemgrep = await execa(executavel, [
        "codigo",
        "semgrep",
        "auditar",
        "--json"
    ], {
        cwd: diretorioConsumidor,
        env: {
            PATH: `${diretorioBinario}${path.delimiter}${process.env.PATH ?? ""}`
        }
    });
    expect(JSON.parse(String(auditoriaSemgrep.stdout))).toMatchObject({
        totalAchados: 0,
        codigoSaida: 0
    });
    expect(await existe(path.join(diretorioConsumidor, "toolkit", "qualidade", "artefatos", "semgrep"))).toBe(false);

    const gravacaoSemgrep = await execa(executavel, [
        "codigo",
        "semgrep",
        "auditar",
        "--json",
        "--gravar"
    ], {
        cwd: diretorioConsumidor,
        env: {
            PATH: `${diretorioBinario}${path.delimiter}${process.env.PATH ?? ""}`
        }
    });
    expect(JSON.parse(String(gravacaoSemgrep.stdout))).toMatchObject({totalAchados: 0, codigoSaida: 0});
    const caminhoResumoSemgrep = path.join(diretorioConsumidor, "toolkit", "qualidade", "artefatos", "semgrep", "mais-recente", "resumo.md");
    expect(await existe(path.join(diretorioConsumidor, "toolkit", "qualidade", "artefatos", "semgrep", "mais-recente", "resultado.json"))).toBe(true);
    expect(await readFile(caminhoResumoSemgrep, "utf8")).toContain("# Auditoria Semgrep\n");
}, 60000);
