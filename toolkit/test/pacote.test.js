import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {execa} from "execa";
import {expect, test} from "vitest";

const DIRETORIO_REPOSITORIO = path.resolve(import.meta.dirname, "..", "..");
const NOME_EXECUTAVEL = process.platform === "win32" ? "sgc.cmd" : "sgc";

test("pacote fonte executa em um projeto consumidor isolado", async () => {
    const diretorioTemporario = await mkdtemp(path.join(os.tmpdir(), "sgc-pacote-"));
    const diretorioPacote = path.join(diretorioTemporario, "pacote");
    const diretorioConsumidor = path.join(diretorioTemporario, "consumidor");
    await fs.ensureDir(diretorioPacote);
    await fs.ensureDir(diretorioConsumidor);

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
    const nomePacote = empacotamento.stdout.trim().split(/\r?\n/).at(-1);
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

    await fs.outputFile(
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

    expect(await fs.pathExists(path.join(diretorioConsumidor, "node_modules", "sgc-scripts", "sgc.ts"))).toBe(true);
    expect(JSON.parse(resultado.stdout)).toMatchObject({
        base: await fs.realpath(diretorioConsumidor),
        pontuacao: {faixa: "bom"}
    });

    const diretorioBinario = path.join(diretorioTemporario, "bin");
    const caminhoSemgrep = path.join(diretorioBinario, "semgrep");
    await fs.ensureDir(diretorioBinario);
    await fs.outputFile(caminhoSemgrep, [
        "#!/bin/sh",
        "case \"$*\" in",
        "  *sgc-scripts/qualidade/politicas/semgrep/sgc-qualidade.yml*) printf '{\"results\":[]}';;",
        "  *) exit 2;;",
        "esac",
        ""
    ].join("\n"));
    await fs.chmod(caminhoSemgrep, 0o755);
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
    expect(JSON.parse(auditoriaSemgrep.stdout)).toMatchObject({
        totalAchados: 0,
        codigoSaida: 0
    });
    expect(await fs.pathExists(path.join(diretorioConsumidor, "toolkit", "qualidade", "artefatos", "semgrep"))).toBe(false);

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
    expect(JSON.parse(gravacaoSemgrep.stdout)).toMatchObject({totalAchados: 0, codigoSaida: 0});
    expect(await fs.pathExists(path.join(diretorioConsumidor, "toolkit", "qualidade", "artefatos", "semgrep", "mais-recente", "resultado.json"))).toBe(true);
}, 60000);
