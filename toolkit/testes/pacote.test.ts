import os from "node:os";
import path from "node:path";
import {access, chmod, mkdir, mkdtemp, readFile, realpath, writeFile} from "node:fs/promises";
import {execa} from "execa";
import {expect, test} from "vitest";

const DIRETORIO_REPOSITORIO = path.resolve(import.meta.dirname, "..", "..");
const NOME_EXECUTAVEL = process.platform === "win32" ? "ferramentas.cmd" : "ferramentas";

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
    expect(nomePacote).toMatch(/^ferramentas-projeto-\d+\.\d+\.\d+\.tgz$/);
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

    expect(await existe(path.join(diretorioConsumidor, "node_modules", "ferramentas-projeto", "ferramentas.ts"))).toBe(true);
    expect(JSON.parse(String(resultado.stdout))).toMatchObject({
        base: await realpath(diretorioConsumidor),
        pontuacao: {classificacao: "tendencia"}
    });

    const diretorioBinario = path.join(diretorioTemporario, "bin");
    const caminhoSemgrep = path.join(diretorioBinario, "semgrep");
    const caminhoRegrasSemgrep = path.join(diretorioConsumidor, "regras.yml");
    await mkdir(diretorioBinario, {recursive: true});
    await escreverArquivo(caminhoRegrasSemgrep, "rules: []\n");
    await escreverArquivo(caminhoSemgrep, [
        "#!/bin/sh",
        "case \"$*\" in",
        "  *regras.yml*) printf '{\"results\":[]}';;",
        "  *) exit 2;;",
        "esac",
        ""
    ].join("\n"));
    await chmod(caminhoSemgrep, 0o755);
    const auditoriaSemgrep = await execa(executavel, [
        "codigo",
        "semgrep",
        "auditar",
        "--json",
        "--regra",
        caminhoRegrasSemgrep
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
        "--regra",
        caminhoRegrasSemgrep,
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

test("pacote fonte expõe cobertura parametrizável para consumidor TypeScript", async () => {
    const diretorioTemporario = await mkdtemp(path.join(os.tmpdir(), "sgc-pacote-api-"));
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
    expect(nomePacote).toMatch(/^ferramentas-projeto-\d+\.\d+\.\d+\.tgz$/);
    const caminhoPacote = path.join(diretorioPacote, nomePacote);

    await execa("npm", [
        "init",
        "-y"
    ], {
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

    const caminhoRelatorioJacoco = path.join(diretorioConsumidor, "relatorios", "jacoco.xml");
    await escreverArquivo(
        caminhoRelatorioJacoco,
        [
            "<report>",
            "  <counter type=\"LINE\" missed=\"1\" covered=\"2\"/>",
            "  <counter type=\"BRANCH\" missed=\"0\" covered=\"1\"/>",
            "  <package name=\"exemplo/controle\">",
            "    <sourcefile name=\"Exemplo.java\">",
            "      <line nr=\"10\" ci=\"1\" mb=\"0\" cb=\"0\"/>",
            "      <line nr=\"11\" ci=\"0\" mb=\"0\" cb=\"0\"/>",
            "    </sourcefile>",
            "  </package>",
            "</report>"
        ].join("\n")
    );
    await escreverArquivo(
        path.join(diretorioConsumidor, "cliente", "coverage", "coverage-final.json"),
        JSON.stringify({
            [path.join(diretorioConsumidor, "cliente", "src", "Exemplo.ts")]: {
                s: {"1": 1, "2": 0},
                f: {"1": 1},
                b: {"1": [1, 0]},
                statementMap: {"1": {}, "2": {}}
            }
        })
    );
    await escreverArquivo(
        path.join(diretorioConsumidor, "specs", "cdu", "cdu-01.md"),
        [
            "# CDU-01 - Exemplo",
            "",
            "## Atores",
            "",
            "- OPERADOR",
            "",
            "## Pré-condições",
            "",
            "- Sistema disponível.",
            "",
            "## Fluxo principal",
            "",
            "1. O operador acessa o `Painel`."
        ].join("\n")
    );
    const caminhoConsumidor = path.join(diretorioConsumidor, "consumidor.mts");
    await escreverArquivo(
        caminhoConsumidor,
        [
            'import {extrairCoberturaJacoco} from "ferramentas-projeto/cobertura-java";',
            'import {extrairCoberturaCliente} from "ferramentas-projeto/cobertura-web";',
            'import {auditarCasosDeUso, inventariarCasosDeUso} from "ferramentas-projeto/casos-de-uso";',
            "",
            "const diretorioBase = process.argv[2];",
            "const jacoco = await extrairCoberturaJacoco(\"relatorios/jacoco.xml\", {diretorioBase});",
            "const coberturaCliente = await extrairCoberturaCliente(\"cliente/coverage/coverage-final.json\", {diretorioBase});",
            "const inventario = await inventariarCasosDeUso({base: diretorioBase, secoes: [\"formatos\"]});",
            "const auditoria = await auditarCasosDeUso({base: diretorioBase, secoes: [\"estrutura\"]});",
            "console.log(JSON.stringify({",
            "    jacoco: {linhas: jacoco.linhas.percentual, arquivos: jacoco.totais.totalArquivos},",
            "    cliente: {linhas: coberturaCliente.linhas.percentual, arquivos: coberturaCliente.arquivos.length},",
            "    cdu: {arquivos: inventario.totalArquivos, erros: auditoria.resumo.erros}",
            "}));",
            ""
        ].join("\n")
    );

    const caminhoTsx = path.join(
        diretorioConsumidor,
        "node_modules",
        ".bin",
        process.platform === "win32" ? "tsx.cmd" : "tsx"
    );
    const resultado = await execa(caminhoTsx, [caminhoConsumidor, diretorioConsumidor], {
        cwd: diretorioConsumidor
    });

    expect(JSON.parse(String(resultado.stdout))).toEqual({
        jacoco: {linhas: 66.67, arquivos: 1},
        cliente: {linhas: 50, arquivos: 1},
        cdu: {arquivos: 1, erros: 0}
    });
}, 60000);
