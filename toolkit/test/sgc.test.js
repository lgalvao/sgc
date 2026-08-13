import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {describe, expect, test} from "vitest";
import {execa, execaNode} from "execa";
import {pathToFileURL} from "node:url";
import {calcularTotais, construirArvore, listarArquivosGit} from "../projeto/arvore-linhas.ts";
import {sincronizarVersao} from "../projeto/versao-sincronizar.ts";
import {carregarConfiguracao, validarConfiguracao, VERSAO_CONFIGURACAO} from "../lib/configuracao.ts";
import {resolverCaminhosOpenapi} from "../integracao/contratos-openapi-caminhos.js";
import {ADAPTADORES, PERFIS, principal as coletarFotografiaQualidade} from "../qualidade/coleta-execucao.js";
import {resolverDiretoriosPadrao} from "../codigo/semgrep-auditar.js";
import {executarAuditoria as executarAuditoriaCheiros} from "../codigo/cheiros-auditar.js";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "toolkit", "sgc.js");
const CAMINHO_TSX = path.join(DIRETORIO_RAIZ, "node_modules", ".bin", process.platform === "win32" ? "tsx.cmd" : "tsx");
const CAMINHO_SGC_COMPILADO = path.join(DIRETORIO_RAIZ, "toolkit", "dist", "sgc.js");
const CAMINHO_TESTES_PRIORIZAR = path.join(DIRETORIO_RAIZ, "toolkit", "backend", "testes-priorizar.js");
const CAMINHOS_COMANDOS_TESTES_BACKEND = [
    "testes-analisar.js",
    "testes-priorizar.js"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "backend", nome));
const FIXTURE_FOTOGRAFIA = path.join(DIRETORIO_RAIZ, "toolkit", "test", "fixtures", "qualidade", "fotografia.json");
const CAMINHO_FRONTEND_COBERTURA_AUDITORIA = path.join(DIRETORIO_RAIZ, "toolkit", "frontend", "cobertura-auditoria.js");
const CAMINHOS_COMANDOS_COBERTURA_BACKEND = [
    "cobertura-ramificacoes.js",
    "cobertura-auditoria.js"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "backend", nome));
const CAMINHOS_COMANDOS_AUDITORIA_BACKEND = [
    "arquitetura-auditar.js",
    "coesao-auditar.js",
    "contratos-auditar.js"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "backend", nome));
const CAMINHO_AUDITORIA_ASSUNTOS = path.join(DIRETORIO_RAIZ, "toolkit", "backend", "notificacoes-assuntos-auditar.js");
const CAMINHO_CORRIGIR_FQN = path.join(DIRETORIO_RAIZ, "toolkit", "backend", "java-corrigir-fqn.js");
const CAMINHO_SEMGREP_AUDITAR = path.join(DIRETORIO_RAIZ, "toolkit", "codigo", "semgrep-auditar.js");
const CAMINHO_CHEIROS_AUDITAR = path.join(DIRETORIO_RAIZ, "toolkit", "codigo", "cheiros-auditar.js");
const CAMINHOS_COMANDOS_CONTRATOS = [
    "contratos-diff.js",
    "contratos-exportar-openapi.js",
    "contratos-fixar-baseline.js"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "integracao", nome));
const CAMINHOS_COMANDOS_PROJETO = [
    "arvore-linhas.ts",
    "diagnostico.js",
    "versao-sincronizar.ts"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "projeto", nome));
const CAMINHOS_COMANDOS_QUALIDADE = [
    "coleta.js",
    "coleta-execucao.js",
    "resumo.js"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "qualidade", nome));
const CAMINHOS_COMANDOS_CONSISTENCIA = [
    "nomes-simbolos-coletar.js",
    "nomes-consistencia-auditar.js",
    "idioma-consistencia-auditar.js"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "codigo", nome));
const CAMINHOS_COMANDOS_ESTRUTURA_FRONTEND = [
    "arquitetura-auditar.js",
    "arquitetura-validar.js",
    "modais-validar.js",
    "residuos-auditar.js",
    "residuos-validar.js",
    "identificadores-teste-listar.js",
    "identificadores-teste-listar-duplicados.js",
    "views-templates-validar.js"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "frontend", nome));
const CAMINHOS_COMANDOS_COBERTURA_FRONTEND = [
    "cobertura-auditoria.js",
    "cobertura-ramificacoes.js",
    "cobertura-ramificacoes-erros.js"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "frontend", nome));
const CAMINHOS_COMANDOS_ACESSIBILIDADE_FRONTEND = [
    "acessibilidade-crawler.js",
    "acessibilidade-processar-resultados.js"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "frontend", nome));
const DIRETORIO_SCRIPTS_BACKEND_LEGADO = path.join(DIRETORIO_RAIZ, "backend", "etc", "scripts");
const DIRETORIO_SCRIPTS_FRONTEND_LEGADO = path.join(DIRETORIO_RAIZ, "frontend", "etc", "scripts");

async function executarSgc(args, opcoes = {}) {
    return execa(CAMINHO_TSX, [CAMINHO_SGC, ...args], {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
}

async function executarScriptFrontendCobertura(args, opcoes = {}) {
    return execa(CAMINHO_TSX, [CAMINHO_FRONTEND_COBERTURA_AUDITORIA, ...args], {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
}

async function executarScriptTestesPriorizar(args, opcoes = {}) {
    return execa(CAMINHO_TSX, [CAMINHO_TESTES_PRIORIZAR, ...args], {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
}

describe("CLI raiz do toolkit", () => {
    test("pode ser importada sem executar a CLI", async () => {
        const caminhoSgc = pathToFileURL(CAMINHO_SGC).href;
        const resultado = await execa(process.execPath, [
            "--import=tsx",
                "--input-type=module",
            "-e",
            `process.argv.push("--help"); await import(${JSON.stringify(caminhoSgc)}); process.stdout.write("importacao-ok\\n");`
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("CLI compilada despacha scripts compilados", async () => {
        const compilacao = await execa("npm", ["run", "build"], {
            cwd: path.join(DIRETORIO_RAIZ, "toolkit"),
            reject: false
        });
        expect(compilacao.exitCode).toBe(0);

        const resultado = await execaNode(CAMINHO_SGC_COMPILADO, [
            "frontend",
            "residuos",
            "auditar",
            "--help"
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Audita residuos estruturais do frontend.");
    });

    test("pode importar comandos de contratos sem executar integrações", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_CONTRATOS.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("resolve artefatos OpenAPI a partir da base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-openapi-base-"));
        await fs.outputJSON(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                contratosOpenapi: "artefatos/contratos"
            }
        });

        const caminhos = resolverCaminhosOpenapi(base);
        const documentoAtual = {
            openapi: "3.1.0",
            info: {title: "Projeto externo", version: "1.0.0"},
            paths: {"/exemplo": {get: {responses: {200: {description: "OK"}}}}}
        };
        const url = `data:application/json,${encodeURIComponent(JSON.stringify(documentoAtual))}`;
        const exportacao = await executarSgc([
            "integracao",
            "contratos",
            "exportar-openapi",
            "--json",
            "--base",
            base,
            "--url",
            url
        ]);

        expect(exportacao.exitCode).toBe(0);
        expect(JSON.parse(exportacao.stdout)).toMatchObject({
            base,
            saida: caminhos.caminhoAtual,
            paths: 1
        });
        expect(await fs.pathExists(caminhos.caminhoAtual)).toBe(true);

        await fs.outputJSON(caminhos.caminhoReferencia, {
            openapi: "3.1.0",
            info: {title: "Projeto externo", version: "0.9.0"},
            paths: {}
        });

        const diferenca = await executarSgc([
            "integracao",
            "contratos",
            "diff",
            "--json",
            "--base",
            base
        ]);

        expect(diferenca.exitCode).toBe(0);
        expect(JSON.parse(diferenca.stdout)).toMatchObject({
            base,
            anterior: caminhos.caminhoReferencia,
            atual: caminhos.caminhoAtual,
            houveMudancas: true
        });
        expect(await fs.pathExists(caminhos.caminhoRelatorio)).toBe(true);

        const fixacao = await executarSgc([
            "integracao",
            "contratos",
            "fixar-baseline",
            "--json",
            "--base",
            base
        ]);

        expect(fixacao.exitCode).toBe(0);
        expect(JSON.parse(fixacao.stdout)).toMatchObject({
            base,
            origem: caminhos.caminhoAtual,
            destino: caminhos.caminhoReferencia
        });
        expect(await fs.readFile(caminhos.caminhoReferencia, "utf8")).toBe(await fs.readFile(caminhos.caminhoAtual, "utf8"));
    });

    test("pode importar comandos de cobertura backend sem ler JaCoCo", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_COBERTURA_BACKEND.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar auditores estruturais do backend sem analisar o projeto", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_AUDITORIA_BACKEND.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar o corretor de FQN sem alterar arquivos", async () => {
        const urlModulo = pathToFileURL(CAMINHO_CORRIGIR_FQN).href;
        const resultado = await execa(process.execPath, [
            "--import=tsx",
                "--input-type=module",
            "-e",
            `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("pode importar comandos de analise de testes sem ler relatorios", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_TESTES_BACKEND.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar o auditor Semgrep sem executar a ferramenta externa", async () => {
        const urlModulo = pathToFileURL(CAMINHO_SEMGREP_AUDITAR).href;
        const resultado = await execa(process.execPath, [
            "--import=tsx",
                "--input-type=module",
            "-e",
            `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("pode importar o auditor de cheiros sem ler o projeto", async () => {
        const urlModulo = pathToFileURL(CAMINHO_CHEIROS_AUDITAR).href;
        const resultado = await execa(process.execPath, [
            "--import=tsx",
                "--input-type=module",
            "-e",
            `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("corrige FQNs em uma raiz de backend externa no modo simulacao", async () => {
        const diretorioBackend = await mkdtemp(path.join(os.tmpdir(), "sgc-corrigir-fqn-"));
        const caminhoJava = path.join(diretorioBackend, "src", "main", "java", "exemplo", "Exemplo.java");
        const conteudoOriginal = [
            "package exemplo;",
            "",
            "public class Exemplo {",
            "    com.externo.Alvo alvo;",
            "}"
        ].join("\n");
        await fs.outputFile(caminhoJava, conteudoOriginal);

        const resultado = await executarSgc([
            "backend",
            "java",
            "corrigir-fqn",
            "--base",
            diretorioBackend,
            "--dry-run"
        ]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("[simulacao] Atualizado");
        expect(await fs.readFile(caminhoJava, "utf8")).toBe(conteudoOriginal);
    });

    test("corrige FQNs no modo de escrita sem duplicar linhas e permanece idempotente", async () => {
        const diretorioBackend = await mkdtemp(path.join(os.tmpdir(), "sgc-corrigir-fqn-escrita-"));
        const caminhoJava = path.join(diretorioBackend, "src", "main", "java", "exemplo", "Exemplo.java");
        const conteudoOriginal = [
            "package exemplo;",
            "",
            "public class Exemplo {",
            "    com.externo.Alvo alvo;",
            "}"
        ].join("\n");
        const conteudoEsperado = [
            "package exemplo;",
            "import com.externo.Alvo;",
            "",
            "public class Exemplo {",
            "    Alvo alvo;",
            "}"
        ].join("\n");
        await fs.outputFile(caminhoJava, conteudoOriginal);

        const primeiraExecucao = await executarSgc([
            "backend",
            "java",
            "corrigir-fqn",
            "--base",
            diretorioBackend
        ]);

        expect(primeiraExecucao.exitCode).toBe(0);
        expect(primeiraExecucao.stdout).toContain("Atualizado");
        expect(await fs.readFile(caminhoJava, "utf8")).toBe(conteudoEsperado);

        const segundaExecucao = await executarSgc([
            "backend",
            "java",
            "corrigir-fqn",
            "--base",
            diretorioBackend
        ]);

        expect(segundaExecucao.exitCode).toBe(0);
        expect(segundaExecucao.stdout).toContain("Total de arquivos atualizados: 0");
        expect(await fs.readFile(caminhoJava, "utf8")).toBe(conteudoEsperado);
    });

    test("pode importar auditoria de assuntos sem ler o backend", async () => {
        const urlModulo = pathToFileURL(CAMINHO_AUDITORIA_ASSUNTOS).href;
        const resultado = await execa(process.execPath, [
            "--import=tsx",
                "--input-type=module",
            "-e",
            `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("pode importar comandos de projeto sem executar efeitos", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_PROJETO.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar comandos de qualidade sem executar coleta ou resumo", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_QUALIDADE.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar auditores de consistencia sem gerar artefatos", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_CONSISTENCIA.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar auditores estruturais do frontend sem auditar o projeto", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_ESTRUTURA_FRONTEND.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar comandos de cobertura frontend sem ler o relatorio V8", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_COBERTURA_FRONTEND.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar comandos de acessibilidade sem executar o crawler ou ler resultados", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_ACESSIBILIDADE_FRONTEND.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("processa resultados de acessibilidade em uma base externa", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-acessibilidade-processar-"));
        const entrada = path.join(diretorioBase, "resultados.json");
        const saida = path.join(diretorioBase, "relatorios", "acessibilidade.md");

        await fs.outputJSON(entrada, [{
            name: "Pagina inicial",
            route: "/",
            violations: [{
                impact: "moderate",
                id: "botao-com-nome",
                help: "Botoes devem ter nome acessivel",
                helpUrl: "https://dequeuniversity.com/rules/axe/",
                description: "Verifique o nome acessivel do botao.",
                nodes: [{target: ["button"]}]
            }]
        }]);

        const resultado = await executarSgc([
            "frontend",
            "acessibilidade",
            "processar",
            "--base",
            diretorioBase,
            "--entrada",
            entrada,
            "--saida",
            saida
        ]);

        expect(resultado.exitCode).toBe(0);
        expect(await fs.pathExists(saida)).toBe(true);
        expect(await fs.readFile(saida, "utf8")).toContain("botao-com-nome");
    });

    test("analisa cobertura frontend a partir de base e relatorio V8 externos", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-frontend-"));
        const caminhoArquivo = path.join(diretorioBase, "frontend", "src", "exemplo.ts");
        const caminhoRelatorio = path.join(diretorioBase, "coverage", "coverage-final.json");

        await fs.outputFile(caminhoArquivo, "export function exemplo() { return true; }\n");
        await fs.outputJSON(caminhoRelatorio, {
            [caminhoArquivo]: {
                s: {"1": 0, "2": 1},
                f: {"1": 0},
                b: {"1": [0, 1]},
                statementMap: {"1": {}, "2": {}}
            }
        });

        const resultado = await executarSgc([
            "frontend",
            "cobertura",
            "ramificacoes",
            "--json",
            "--base",
            diretorioBase,
            "--arquivo",
            caminhoRelatorio
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.totais.total).toBe(2);
        expect(conteudo.arquivos[0]).toMatchObject({
            arquivo: "frontend/src/exemplo.ts",
            branchesPerdidos: 1,
        });
    });

    test("resolve artefatos de nomenclatura relativos a uma base externa", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-nomenclatura-base-"));
        await fs.outputFile(
            path.join(diretorioBase, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "codigo",
            "nomes",
            "coletar-simbolos",
            "--json",
            "--base",
            diretorioBase
        ]);

        expect(resultado.exitCode).toBe(0);
        const inventario = JSON.parse(resultado.stdout);
        expect(inventario.base).toBe(diretorioBase);
        expect(inventario.totais.arquivos).toBe(1);
        expect(await fs.pathExists(path.join(
            diretorioBase,
            "toolkit",
            "qualidade",
            "artefatos",
            "nomenclatura",
            "mais-recente",
            "simbolos.json"
        ))).toBe(true);
    });

    test("respeita sem-gravar ao gerar inventario auxiliar da auditoria de nomenclatura", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-nomenclatura-sem-gravar-"));
        await fs.outputFile(
            path.join(diretorioBase, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "codigo",
            "nomes",
            "auditar-consistencia",
            "--json",
            "--sem-gravar",
            "--base",
            diretorioBase
        ]);

        expect(resultado.exitCode).toBe(0);
        expect(JSON.parse(resultado.stdout).base).toBe(diretorioBase);
        expect(await fs.pathExists(path.join(diretorioBase, "toolkit"))).toBe(false);
    });

    test("exibe a ajuda principal", async () => {
        const resultado = await executarSgc(["--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Toolkit do SGC");
        expect(resultado.stdout).toContain("projeto diagnostico");
    });

    test("despacha ajuda de um comando de auditoria do backend", async () => {
        const resultado = await executarSgc(["backend", "cobertura", "auditoria", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Auditoria unificada de cobertura e risco (Backend).");
    });

    test("despacha ajuda da auditoria de assuntos de notificacao do backend", async () => {
        const resultado = await executarSgc(["backend", "notificacoes", "auditar-assuntos", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Audita literais de assunto de notificacao fora de AssuntosNotificacao.");
    });

    test("audita assuntos literais fora de AssuntosNotificacao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-assuntos-auditar-"));
        const dir = path.join(base, "backend", "src", "main", "java", "sgc");

        await fs.outputFile(
            path.join(dir, "alerta", "AssuntosNotificacao.java"),
            [
                "package sgc.alerta;",
                "public final class AssuntosNotificacao {",
                "  public static String ok() {",
                "    return \"SGC: Assunto centralizado\";",
                "  }",
                "}"
            ].join("\n")
        );

        await fs.outputFile(
            path.join(dir, "diagnostico", "ServicoInvalido.java"),
            [
                "package sgc.diagnostico;",
                "class ServicoInvalido {",
                "  void enviar() {",
                "    String assunto = \"SGC: Assunto espalhado\";",
                "  }",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "backend",
            "notificacoes",
            "auditar-assuntos",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const corpo = JSON.parse(resultado.stdout);
        expect(corpo.resumo.arquivosComViolacao).toBe(1);
        expect(corpo.relatorio[0].arquivo).toBe("backend/src/main/java/sgc/diagnostico/ServicoInvalido.java");
        expect(corpo.relatorio[0].achados.some(item => item.regra === "literal_sgc")).toBe(true);
    });

    test("audita cheiros de codigo em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cheiros-"));
        const frontendDir = path.join(base, "frontend", "src");
        const backendDir = path.join(base, "backend", "src", "main", "java", "sgc", "exemplo", "dto");

        await fs.outputFile(
            path.join(frontendDir, "Exemplo.ts"),
            [
                "export function exemplo(valor: any) {",
                "  if (valor === null) return valor || [];",
                "  return valor as any;",
                "}"
            ].join("\n")
        );

        await fs.outputFile(
            path.join(backendDir, "ExemploDto.java"),
            [
                "package sgc.exemplo.dto;",
                "import org.jspecify.annotations.Nullable;",
                "public record ExemploDto(@Nullable String nome) {",
                "  public boolean vazio(String valor) {",
                "    return valor == null;",
                "  }",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "codigo",
            "cheiros",
            "auditar",
            "--json",
            "--sem-gravar",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.contagens.backend_nullable_dto).toBe(1);
        expect(conteudo.contagens.backend_null_checks).toBe(1);
        expect(conteudo.contagens.frontend_any_producao).toBe(2);
        expect(conteudo.contagens.frontend_null_checks).toBe(1);
        expect(conteudo.contagens.frontend_fallback_or).toBe(1);
    });

    test("auditores backend usam caminhos de codigo definidos pela configuracao do projeto", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-configuracao-codigo-backend-"));
        const codigoBackend = path.join(base, "servidor", "java");
        await fs.outputJSON(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                artefatosQualidade: "artefatos"
            }
        });
        await fs.outputFile(
            path.join(codigoBackend, "exemplo", "ExemploService.java"),
            [
                "package exemplo;",
                "public class ExemploService {",
                "    public void buscar() {}",
                "    public void criar() {}",
                "    public void iniciar() {}",
                "    public void notificar() {}",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "backend",
            "coesao",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.totalAnalisados).toBe(1);
        expect(conteudo.todos[0].caminhoRelativo).toBe("servidor/java/exemplo/ExemploService.java");
        expect(await fs.pathExists(path.join(base, "artefatos", "backend", "latest", "coesao-auditoria.json"))).toBe(true);
    });

    test("carrega configuracao versionada e preserva defaults ausentes", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-configuracao-valida-"));
        await fs.outputJSON(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java"
            }
        });

        const configuracao = carregarConfiguracao(base);

        expect(configuracao.versao).toBe(VERSAO_CONFIGURACAO);
        expect(configuracao.diretorios.backendCodigo).toBe("servidor/java");
        expect(configuracao.diretorios.frontend).toBe("frontend");
    });

    test("rejeita configuracao com versao, chave ou caminho invalido", () => {
        expect(() => validarConfiguracao({diretorios: {}})).toThrow("deve informar a versão");
        expect(() => validarConfiguracao({versao: 2})).toThrow("versão 2");
        expect(() => validarConfiguracao({versao: VERSAO_CONFIGURACAO, diretorios: {backendCodigoo: "servidor/java"}})).toThrow("backendCodigoo");
        expect(() => validarConfiguracao({versao: VERSAO_CONFIGURACAO, diretorios: {backendCodigo: 42}})).toThrow("backendCodigo");
        expect(() => validarConfiguracao({versao: VERSAO_CONFIGURACAO, diretorios: {backendCodigo: "   "}})).toThrow("não vazio");
    });

    test("grava fotografia de qualidade na base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-base-"));
        const adaptadores = [...PERFIS.backend];
        const originais = new Map(adaptadores.map((nome) => [nome, ADAPTADORES[nome]]));

        try {
            for (const nome of adaptadores) {
                ADAPTADORES[nome] = async () => ({
                    codigo: nome,
                    status: "sucesso",
                    metricas: {},
                });
            }

            const fotografia = await coletarFotografiaQualidade([
                "--perfil",
                "backend",
                "--base",
                base,
            ]);

            const caminhoFotografia = path.join(
                base,
                "toolkit",
                "qualidade",
                "artefatos",
                "mais-recente",
                "fotografia.json"
            );
            expect(fotografia.resumo.statusGeral).toBe("verde");
            expect(fotografia.verificacoes).toHaveLength(adaptadores.length);
            expect(await fs.pathExists(caminhoFotografia)).toBe(true);
        } finally {
            for (const [nome, adaptador] of originais) {
                ADAPTADORES[nome] = adaptador;
            }
        }
    });

    test("resolve alvos padrao do Semgrep pela configuracao da base", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-semgrep-base-"));
        await fs.outputJSON(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                frontendCodigo: "aplicacao/src"
            }
        });

        expect(resolverDiretoriosPadrao(base)).toEqual([
            "servidor/java",
            "aplicacao/src"
        ]);
    });

    test("aplica filtros de cheiros aos diretorios de codigo configurados", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cheiros-base-"));
        await fs.outputJSON(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                frontendCodigo: "aplicacao/src"
            }
        });
        await fs.outputFile(
            path.join(base, "servidor", "java", "ExemploResponse.java"),
            "class ExemploResponse { @Nullable String nome; }\n"
        );
        await fs.outputFile(
            path.join(base, "aplicacao", "src", "Exemplo.ts"),
            "export function exemplo(valor: any) { return valor || []; }\n"
        );

        const resultado = await executarAuditoriaCheiros({base, semGravar: true});

        expect(resultado.snapshot.contagens.backend_nullable_dto).toBe(1);
        expect(resultado.snapshot.contagens.frontend_any_producao).toBe(1);
        expect(resultado.snapshot.contagens.frontend_fallback_or).toBe(1);
    });

    test("audita residuos do frontend em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-auditar-"));
        const frontendDir = path.join(base, "frontend", "src");
        const orcamento = path.join(base, "orcamento.json");

        await fs.outputJson(orcamento, {
            versaoSchema: "1.0.0",
            camadas: {
                service: {meta: 4, limite: 8},
                component: {meta: 6, limite: 10},
                outro: {meta: 6, limite: 10}
            },
            metricas: {
                maximosProducao: {
                    anyExplicito: 1,
                    checksNull: 1,
                    fallbacksDefensivos: 1,
                    catchBlocks: 1,
                    castsDuplos: 0,
                    storageDireto: 1,
                    exportacoesSuspeitas: 1,
                    arquivosAcimaMetaPorCamada: {
                        service: 1,
                        component: 1,
                        outro: 0
                    }
                }
            }
        });

        await fs.outputFile(
            path.join(frontendDir, "services", "exemploService.ts"),
            [
                "export function exemploService(valor: any) {",
                "  if (valor === null) {",
                "    return valor || [];",
                "  }",
                "  return valor;",
                "}",
            ].join("\n")
        );
        await fs.outputFile(
            path.join(frontendDir, "components", "ExemploCard.vue"),
            [
                "<script setup lang=\"ts\">",
                "const salvar = () => localStorage.setItem('chave', 'valor');",
                "</script>",
                "<template><button @click=\"salvar\">Salvar</button></template>"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend",
            "residuos",
            "auditar",
            "--json",
            "--sem-gravar",
            "--base",
            base,
            "--orcamento",
            orcamento
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.contagens.producao.anyExplicito).toBe(1);
        expect(conteudo.contagens.producao.checksNull).toBe(1);
        expect(conteudo.contagens.producao.fallbacksDefensivos).toBe(1);
        expect(conteudo.contagens.producao.storageDireto).toBe(1);
        expect(conteudo.contagens.producao.exportacoesSuspeitas).toBe(1);
        expect(conteudo.contagens.producao.arquivosAcimaMeta.service).toBe(1);
    });

    test("calcula a saida padrao de residuos a partir da base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-saida-base-"));
        await fs.outputFile(
            path.join(base, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "frontend",
            "residuos",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const fotografia = JSON.parse(resultado.stdout);
        expect(fotografia.base).toBe(base);
        expect(await fs.pathExists(path.join(
            base,
            "toolkit",
            "qualidade",
            "artefatos",
            "frontend-residuos",
            "mais-recente",
            "fotografia.json"
        ))).toBe(true);
    });

    test("audita vazamentos arquiteturais do frontend em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-auditar-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(path.join(frontendDir, "stores", "unidade.ts"), "export const useUnidadeStore = () => ({ invalidar: () => undefined, obterUnidade: () => undefined, recarregarUnidade: () => undefined, dadosEdicaoValidos: () => true, sincronizarUnidade: () => undefined, marcarUnidadeParaAtualizacao: () => undefined, limparContextoAtual: () => undefined, resetar: () => undefined, contextoAtual: null, erroAtual: null, carregando: false });");
        await fs.outputFile(path.join(frontendDir, "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await fs.outputFile(path.join(frontendDir, "composables", "useUnidadeTela.ts"), "export function useUnidadeTela() { return { carregar: () => undefined }; }");
        await fs.outputFile(path.join(frontendDir, "router", "unidade.routes.ts"), "export const rotasUnidade = [];");

        await fs.outputFile(
            path.join(frontendDir, "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {buscarUnidade} from '@/services/unidadeService';",
                "import {useUnidadeTela} from '@/composables/useUnidadeTela';",
                "import {rotasUnidade} from '@/router/unidade.routes';",
                "const unidadeStore = useUnidadeStore();",
                "const unidadeTela = useUnidadeTela();",
                "function carregarDados(forcar = false) {",
                "  unidadeTela.carregar();",
                "  unidadeStore.obterUnidade(1, true);",
                "  unidadeStore.recarregarUnidade(1);",
                "  unidadeStore.invalidar();",
                "  return buscarUnidade();",
                "}",
                "const emCache = unidadeStore.cacheUnidades.get(1);",
                "const stale = false;",
                "console.log(rotasUnidade);",
                "</script>"
            ].join("\n")
        );
        await fs.outputFile(
            path.join(frontendDir, "composables", "useCadastroUnidade.ts"),
            [
                "interface DependenciasCadastroUnidade {",
                "  alpha: string;",
                "  beta: string;",
                "  gamma: string;",
                "  delta: string;",
                "  epsilon: string;",
                "  zeta: string;",
                "}",
                "export function useCadastroUnidade() {",
                "  return {",
                "    a: 1,",
                "    b: 2,",
                "    c: 3,",
                "    d: 4,",
                "    e: 5,",
                "    f: 6,",
                "    g: 7,",
                "    h: 8,",
                "    i: 9,",
                "  };",
                "}",
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend",
            "arquitetura",
            "auditar",
            "--json",
            "--sem-gravar",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.metricas.viewsComVazamentoCache).toBe(1);
        expect(conteudo.resumo.metricas.acessosDiretosCache).toBe(1);
        expect(conteudo.resumo.metricas.booleanosPosicionais).toBe(1);
        expect(conteudo.resumo.metricas.ocorrenciasForcar).toBeGreaterThanOrEqual(1);
        expect(conteudo.resumo.metricas.viewsComServiceDireto).toBe(1);
        expect(conteudo.resumo.metricas.viewsComServerStateCaseiro).toBe(1);
        expect(conteudo.resumo.metricas.viewsComFanoutAlto).toBe(1);
        expect(conteudo.resumo.metricas.arquivosComBolsaDependenciasLarga).toBe(1);
        expect(conteudo.resumo.metricas.arquivosComSuperficieAmpla).toBe(1);
        expect(conteudo.resumo.metricas.arquivosComMisturaCamadas).toBe(1);
        expect(conteudo.resumo.metricas.arquivosComServerStateCaseiro).toBe(1);
        expect(conteudo.hotspots[0].arquivo).toBe("frontend/src/views/UnidadeView.vue");
        expect(conteudo.hotspots[0].sinaisAtivos).toContain("serverStateCaseiro");
        expect(conteudo.hotspots.some((hotspot) => hotspot.hubCentral && hotspot.sinaisAtivos.includes("superficieAmpla"))).toBe(false);
    });

    test("calcula a saida padrao de arquitetura a partir da base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-saida-base-"));
        await fs.outputFile(
            path.join(base, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "frontend",
            "arquitetura",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const fotografia = JSON.parse(resultado.stdout);
        expect(fotografia.base).toBe(base);
        expect(await fs.pathExists(path.join(
            base,
            "toolkit",
            "qualidade",
            "artefatos",
            "frontend-arquitetura",
            "mais-recente",
            "fotografia.json"
        ))).toBe(true);
    });

    test("tipos internos de store nao disparam bolsaDependenciasLarga", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-contexto-store-"));
        await fs.outputFile(
            path.join(base, "frontend", "src", "stores", "subprocesso", "tipos.ts"),
            `export type ConfiguracaoContexto<T> = {
                tipoCodigo: string;
                tipoProcessoUnidade: string;
                contextoRef: unknown;
                contextoInvalidoRef: unknown;
                codigosPorProcessoUnidade: unknown;
                buscarPorCodigo: () => Promise<unknown>;
                buscarPorProcessoEUnidade: () => Promise<unknown>;
                registrar: () => void;
                mensagemCodigo: () => string;
                mensagemProcessoUnidade: () => string;
            };`
        );
        const resultado = await executarSgc(["frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.metricas.arquivosComBolsaDependenciasLarga).toBe(0);
    });

    test("hub central nao dispara superficieAmpla", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-hub-central-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(
            path.join(frontendDir, "stores", "perfil.ts"),
            [
                "import {defineStore} from 'pinia';",
                "export const usePerfilStore = defineStore('perfil', () => {",
                "  return {",
                "    a: 1, b: 2, c: 3, d: 4, e: 5, f: 6, g: 7, h: 8, i: 9, j: 10, k: 11, l: 12,",
                "  };",
                "});",
                "export const perfilA = 1;",
                "export const perfilB = 2;",
                "export const perfilC = 3;",
                "export const perfilD = 4;",
                "export const perfilE = 5;",
                "export const perfilF = 6;",
                "export const perfilG = 7;",
                "export const perfilH = 8;",
                "export const perfilI = 9;",
                "export const perfilJ = 10;",
                "export const perfilK = 11;",
                "export const perfilL = 12;",
            ].join("\n")
        );

        const resultado = await executarSgc(["frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((item) => item.arquivo === "frontend/src/stores/perfil.ts");
        expect(hotspot).toBeUndefined();
    });

    test("gate arquitetural falha quando view importa service diretamente", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-gate-falha-"));
        const frontendDir = path.join(base, "frontend");

        await fs.outputJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await fs.outputJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await fs.outputFile(path.join(frontendDir, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await fs.outputFile(
            path.join(frontendDir, "src", "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {buscarUnidade} from '../services/unidadeService';",
                "void buscarUnidade();",
                "</script>",
            ].join("\n")
        );
        await fs.copy(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).not.toBe(0);
        expect(resultado.stdout).toContain("view-sem-service-direto");
    });

    test("gate arquitetural passa quando view usa composable", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-gate-ok-"));
        const frontendDir = path.join(base, "frontend");

        await fs.outputJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await fs.outputJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await fs.outputFile(path.join(frontendDir, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await fs.outputFile(
            path.join(frontendDir, "src", "composables", "useUnidadeTela.ts"),
            [
                "import {buscarUnidade} from '../services/unidadeService';",
                "export function useUnidadeTela() {",
                "  return { carregar: () => buscarUnidade() };",
                "}",
            ].join("\n")
        );
        await fs.outputFile(
            path.join(frontendDir, "src", "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {useUnidadeTela} from '../composables/useUnidadeTela';",
                "const tela = useUnidadeTela();",
                "void tela.carregar();",
                "</script>",
            ].join("\n")
        );
        await fs.copy(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhuma violacao arquitetural encontrada");
    });

    test("gate arquitetural falha quando frontend calcula habilitacao de acao por perfil ou situacao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-acoes-backend-falha-"));
        const frontendDir = path.join(base, "frontend");

        await fs.outputJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await fs.outputJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await fs.outputFile(
            path.join(frontendDir, "src", "views", "ConsensoView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {computed} from 'vue';",
                "import {Perfil} from '@/types/perfil';",
                "const perfilStore = { perfilSelecionado: Perfil.SERVIDOR };",
                "const consenso = { situacao: 'CONSENSO_CRIADO' };",
                "const habilitarAprovarConsenso = computed(() => perfilStore.perfilSelecionado === Perfil.SERVIDOR && consenso.situacao === 'CONSENSO_CRIADO');",
                "</script>",
            ].join("\n")
        );
        await fs.copy(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).not.toBe(0);
        expect(resultado.stdout).toContain("frontend-sem-regra-local-acoes");
        expect(resultado.stdout).toContain("habilitarAprovarConsenso");
    });

    test("gate arquitetural permite flag de acao vinda diretamente do backend", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-acoes-backend-ok-"));
        const frontendDir = path.join(base, "frontend");

        await fs.outputJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await fs.outputJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await fs.outputFile(
            path.join(frontendDir, "src", "views", "ConsensoView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {computed} from 'vue';",
                "const query = { data: { value: { habilitarAprovarConsenso: true } } };",
                "const habilitarAprovarConsenso = computed(() => query.data.value.habilitarAprovarConsenso ?? false);",
                "</script>",
            ].join("\n")
        );
        await fs.copy(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhum calculo local novo de habilitacao/exibicao de acoes encontrado");
    });

    test("composable fachada de store não é penalizado por chamadasStore >= 8", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-facade-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(
            path.join(frontendDir, "stores", "perfil.ts"),
            "import {defineStore} from 'pinia'; export const usePerfilStore = defineStore('perfil', () => ({ a: 1, b: 2, c: 3, d: 4, e: 5, f: 6, g: 7, h: 8, i: 9, j: 10, k: 11, l: 12 }));"
        );

        // Composable que só delega para uma única store (fachada) — acessa a store 12 vezes
        await fs.outputFile(
            path.join(frontendDir, "composables", "usePerfil.ts"),
            [
                "import {computed} from 'vue';",
                "import {usePerfilStore} from '@/stores/perfil';",
                "export function usePerfil() {",
                "  const store = usePerfilStore();",
                "  return {",
                "    a: computed(() => store.a),",
                "    b: computed(() => store.b),",
                "    c: computed(() => store.c),",
                "    d: computed(() => store.d),",
                "    e: computed(() => store.e),",
                "    f: computed(() => store.f),",
                "    g: computed(() => store.g),",
                "    h: computed(() => store.h),",
                "    i: computed(() => store.i),",
                "    j: computed(() => store.j),",
                "    k: computed(() => store.k),",
                "    l: computed(() => store.l),",
                "  };",
                "}",
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((h) => h.arquivo.endsWith("usePerfil.ts"));
        // Fachada de store: acessar a store muitas vezes é esperado — sem penalidade
        expect(hotspot).toBeUndefined();
    });

    test("módulo em stores/ sem defineStore não é penalizado como store Pinia", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-nao-store-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(
            path.join(frontendDir, "services", "autenticacaoService.ts"),
            "export async function login() { return null; } export async function logout() { return null; } export async function renovar() { return null; }"
        );

        // Módulo de funções puras em stores/ que NÃO usa defineStore (orquestração de autenticação)
        await fs.outputFile(
            path.join(frontendDir, "stores", "autenticacao.ts"),
            [
                "import * as autenticacaoService from '@/services/autenticacaoService';",
                "export async function entrar() { return autenticacaoService.login(); }",
                "export async function sair() { return autenticacaoService.logout(); }",
                "export async function renovarSessao() { return autenticacaoService.renovar(); }",
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((h) => h.arquivo.endsWith("autenticacao.ts"));
        // Orquestração sem defineStore: chamar serviços é esperado — sem score nem sinal serviceDireto
        expect(hotspot).toBeUndefined();
    });

    test("composable que chama serviço diretamente não recebe sinal serviceDireto", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-composable-servico-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(
            path.join(frontendDir, "services", "itemService.ts"),
            "export async function buscarItens() { return []; }"
        );

        // Composable com superfície exportada ampla E chamada de serviço direta
        // → deve aparecer em hotspots pelo superficieAmpla, mas NÃO pelo serviceDireto
        await fs.outputFile(
            path.join(frontendDir, "composables", "useItens.ts"),
            [
                "import * as itemService from '@/services/itemService';",
                "export function useItens() {",
                "  return {",
                "    a: 1, b: 2, c: 3, d: 4, e: 5, f: 6, g: 7, h: 8, i: 9,",
                "    carregar: () => itemService.buscarItens(),",
                "  };",
                "}",
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((h) => h.arquivo.endsWith("useItens.ts"));
        // Composable aparece por superficieAmpla, mas chamar serviços não é sinalizado
        expect(hotspot).toBeDefined();
        expect(hotspot.sinaisAtivos).toContain("superficieAmpla");
        expect(hotspot.sinaisAtivos).not.toContain("serviceDireto");
    });

    test("valida residuos do frontend com excecao de tamanho", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-validar-"));
        const frontendDir = path.join(base, "frontend", "src");
        const orcamento = path.join(base, "orcamento.json");
        const excecoes = path.join(base, "excecoes.json");

        await fs.outputJson(orcamento, {
            versaoSchema: "1.0.0",
            camadas: {
                service: {meta: 3, limite: 6},
                outro: {meta: 6, limite: 10}
            },
            metricas: {
                maximosProducao: {
                    anyExplicito: 0,
                    checksNull: 0,
                    fallbacksDefensivos: 0,
                    catchBlocks: 0,
                    castsDuplos: 0,
                    storageDireto: 0,
                    exportacoesSuspeitas: 2,
                    arquivosAcimaMetaPorCamada: {
                        service: 1,
                        outro: 0
                    }
                }
            }
        });
        await fs.outputJson(excecoes, {
            versaoSchema: "1.0.0",
            excecoes: [
                {
                    arquivo: "frontend/src/services/exemploService.ts",
                    camada: "service",
                    maxLinhas: 6,
                    responsavel: "teste",
                    justificativa: "Congelamento de baseline",
                    criterioRemocao: "Reduzir o arquivo."
                }
            ]
        });
        await fs.outputFile(
            path.join(frontendDir, "services", "exemploService.ts"),
            [
                "export function exemploService() {",
                "  return 1;",
                "}",
                "export function outro() {",
                "  return 2;",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend",
            "residuos",
            "validar",
            "--json",
            "--sem-gravar",
            "--base",
            base,
            "--orcamento",
            orcamento,
            "--excecoes",
            excecoes
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.status).toBe("ok");
        expect(conteudo.violacoes).toEqual([]);
    });

    test("analisa testes do backend com resumo no console e sidecar JSON", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analisar-"));
        const markdown = path.join(diretorioSaida, "relatorio.md");
        const json = path.join(diretorioSaida, "relatorio.json");

        const resultado = await executarSgc(["backend", "testes", "analisar", "--output", markdown, "--output-json", json]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Resumo:");
        expect(resultado.stdout).toContain("Repositories:");
        expect(resultado.stdout).toContain("Cobertura indireta:");
        expect(resultado.stdout).toContain("DTOs:");
        expect(await fs.pathExists(markdown)).toBe(true);
        expect(await fs.pathExists(json)).toBe(true);

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.total_classes).toBeGreaterThan(0);
        expect(typeof conteudoJson.estatisticas.classes_com_cobertura_indireta).toBe("number");
        expect(typeof conteudoJson.estatisticas.classes_sem_evidencia_no_escopo).toBe("number");
        expect(typeof conteudoJson.estatisticas.classes_fora_escopo_jacoco).toBe("number");
        expect(typeof conteudoJson.estatisticas.classes_ruido_ignorado).toBe("number");
        expect(conteudoJson.categorias.Repositories.tested.length).toBeGreaterThanOrEqual(1);
    }, 60000);

    test("ignora DTOs estruturais e contratuais do backlog real", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-dto-"));
        const backendDir = path.join(base, "backend-fake");
        const dtoDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo", "dto");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await fs.outputFile(
            path.join(dtoDir, "DtoEstrutural.java"),
            "package sgc.exemplo.dto; public record DtoEstrutural(Long codigo, String nome) {}"
        );
        await fs.outputFile(
            path.join(dtoDir, "RequestContratual.java"),
            "package sgc.exemplo.dto; import jakarta.validation.constraints.NotBlank; public record RequestContratual(@NotBlank String nome) {}"
        );
        await fs.outputFile(
            path.join(dtoDir, "DtoComportamental.java"),
            "package sgc.exemplo.dto; public class DtoComportamental { public static DtoComportamental of(String valor) { return new DtoComportamental(); } }"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--dir",
            backendDir,
            "--output",
            markdown,
            "--output-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("DTOs: 0/1 testados no backlog real (2 ignorados)");

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.dtos_comportamentais).toBe(1);
        expect(conteudoJson.estatisticas.dtos_estruturais).toBe(2);
        expect(conteudoJson.estatisticas.dtos_estruturais_contratuais).toBe(1);
        expect(conteudoJson.estatisticas.classes_ruido_ignorado).toBe(2);

        const dtoUntested = conteudoJson.categorias.DTOs.untested;
        expect(dtoUntested.find((item) => item.classe === "DtoEstrutural").dto_ruido_ignorado).toBe(true);
        expect(dtoUntested.find((item) => item.classe === "RequestContratual").perfil_dto).toBe("estrutural_contrato");
        expect(dtoUntested.find((item) => item.classe === "DtoComportamental").dto_ruido_ignorado).toBe(false);
    });

    test("ignora models estruturais e contratuais do backlog real", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-model-"));
        const backendDir = path.join(base, "backend-fake");
        const modelDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo", "model");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await fs.outputFile(
            path.join(modelDir, "SituacaoExemplo.java"),
            "package sgc.exemplo.model; public enum SituacaoExemplo { ATIVO }"
        );
        await fs.outputFile(
            path.join(modelDir, "AnotacaoExemplo.java"),
            "package sgc.exemplo.model; import java.lang.annotation.*; public @interface AnotacaoExemplo { String value() default \"\"; }"
        );
        await fs.outputFile(
            path.join(modelDir, "ProcessoExemplo.java"),
            "package sgc.exemplo.model; import java.util.*; public class ProcessoExemplo { public void sincronizar(Set<Long> codigos) { if (codigos.isEmpty()) return; codigos.stream().filter(Objects::nonNull).toList(); } }"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--dir",
            backendDir,
            "--output",
            markdown,
            "--output-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Models: 0/1 testados no backlog real (2 ignorados)");

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.models_comportamentais).toBe(1);
        expect(conteudoJson.estatisticas.models_estruturais).toBe(2);
        expect(conteudoJson.estatisticas.models_estruturais_contratuais).toBe(1);

        const modelUntested = conteudoJson.categorias.Models.untested;
        expect(modelUntested.find((item) => item.classe === "SituacaoExemplo").model_ruido_ignorado).toBe(true);
        expect(modelUntested.find((item) => item.classe === "AnotacaoExemplo").perfil_model).toBe("estrutural_contrato");
        expect(modelUntested.find((item) => item.classe === "ProcessoExemplo").model_ruido_ignorado).toBe(false);
    });

    test("ignora others estruturais e contratuais do backlog real e reclassifica commands como DTOs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-others-"));
        const backendDir = path.join(base, "backend-fake");
        const otherDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo");
        const dtoDir = path.join(otherDir, "dto");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await fs.outputFile(
            path.join(otherDir, "Mensagens.java"),
            "package sgc.exemplo; public final class Mensagens { private Mensagens() {} public static final String OI = \"oi\"; }"
        );
        await fs.outputFile(
            path.join(otherDir, "AnotacaoSegura.java"),
            "package sgc.exemplo; public @interface AnotacaoSegura {}"
        );
        await fs.outputFile(
            path.join(otherDir, "LimitadorExemplo.java"),
            "package sgc.exemplo; import java.util.*; public class LimitadorExemplo { public void verificar(String valor) { if (valor.isBlank()) return; List.of(valor).stream().toList(); } }"
        );
        await fs.outputFile(
            path.join(dtoDir, "WorkflowCommand.java"),
            "package sgc.exemplo.dto; public record WorkflowCommand(String nome) {}"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--dir",
            backendDir,
            "--output",
            markdown,
            "--output-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Others: 0/1 testados no backlog real (2 ignorados)");
        expect(resultado.stdout).toContain("DTOs: 0/0 testados no backlog real (1 ignorados)");

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.others_comportamentais).toBe(1);
        expect(conteudoJson.estatisticas.others_estruturais).toBe(2);
        expect(conteudoJson.estatisticas.others_estruturais_contratuais).toBe(1);

        const otherUntested = conteudoJson.categorias.Others.untested;
        expect(otherUntested.find((item) => item.classe === "Mensagens").other_ruido_ignorado).toBe(true);
        expect(otherUntested.find((item) => item.classe === "AnotacaoSegura").perfil_other).toBe("estrutural_contrato");
        expect(otherUntested.find((item) => item.classe === "LimitadorExemplo").other_ruido_ignorado).toBe(false);

        const dtoUntested = conteudoJson.categorias.DTOs.untested;
        expect(dtoUntested.find((item) => item.classe === "WorkflowCommand").dto_ruido_ignorado).toBe(true);
    });

    test("classifica separadamente teste dedicado, cobertura indireta, sem evidencia e fora do escopo", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-jacoco-"));
        const backendDir = path.join(base, "backend-fake");
        const srcDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo");
        const testDir = path.join(backendDir, "src", "test", "java", "sgc", "exemplo");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");
        const jacoco = path.join(base, "jacoco.xml");

        await fs.outputFile(path.join(srcDir, "ClasseDireta.java"), "package sgc.exemplo; public class ClasseDireta {}");
        await fs.outputFile(
            path.join(srcDir, "ClasseIndireta.java"),
            "package sgc.exemplo; public class ClasseIndireta { public String calcular(boolean ativo) { return ativo ? \"ok\" : \"pendente\"; } }"
        );
        await fs.outputFile(
            path.join(srcDir, "ClasseSemEvidencia.java"),
            "package sgc.exemplo; public class ClasseSemEvidencia { public boolean validar(String valor) { return valor != null && !valor.isBlank(); } }"
        );
        await fs.outputFile(
            path.join(srcDir, "ClasseForaEscopo.java"),
            "package sgc.exemplo; public class ClasseForaEscopo { public int contarPositivos(java.util.List<Integer> valores) { return (int) valores.stream().filter(valor -> valor > 0).count(); } }"
        );
        await fs.outputFile(path.join(testDir, "ClasseDiretaTest.java"), "package sgc.exemplo; class ClasseDiretaTest {}");
        // language=XML
        await fs.outputFile(jacoco, `
<report name="fake">
  <package name="sgc/exemplo">
    <sourcefile name="ClasseDireta.java">
      <line nr="1" mi="0" ci="1" mb="0" cb="0"/>
      <counter type="LINE" missed="0" covered="1"/>
    </sourcefile>
    <sourcefile name="ClasseIndireta.java">
      <line nr="1" mi="0" ci="1" mb="0" cb="0"/>
      <counter type="LINE" missed="0" covered="1"/>
    </sourcefile>
    <sourcefile name="ClasseSemEvidencia.java">
      <line nr="1" mi="1" ci="0" mb="0" cb="0"/>
      <counter type="LINE" missed="1" covered="0"/>
    </sourcefile>
  </package>
</report>`.trim());

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--dir",
            backendDir,
            "--output",
            markdown,
            "--output-json",
            json,
            "--jacoco-xml",
            jacoco
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Cobertura indireta: 1");
        expect(resultado.stdout).toContain("Sem evidencia no escopo: 1");
        expect(resultado.stdout).toContain("Fora do escopo do JaCoCo: 1");

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.classes_com_teste_dedicado).toBe(1);
        expect(conteudoJson.estatisticas.classes_com_cobertura_indireta).toBe(1);
        expect(conteudoJson.estatisticas.classes_sem_evidencia_no_escopo).toBe(1);
        expect(conteudoJson.estatisticas.classes_fora_escopo_jacoco).toBe(1);

        const others = conteudoJson.categorias.Others;
        expect(others.tested).toHaveLength(1);
        expect(others.untested).toHaveLength(3);
        expect(others.untested.find((item) => item.classe === "ClasseIndireta").coberta_somente_indiretamente).toBe(true);
        expect(others.untested.find((item) => item.classe === "ClasseSemEvidencia").evidencia_qualidade).toBe("sem_evidencia_no_escopo");
        expect(others.untested.find((item) => item.classe === "ClasseForaEscopo").fora_escopo_jacoco).toBe(true);
    });

    test("prioriza testes usando sidecar JSON automaticamente quando disponivel", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-"));
        const markdown = path.join(diretorioSaida, "unit-test-report.md");
        const json = path.join(diretorioSaida, "unit-test-report.json");
        const saida = path.join(diretorioSaida, "prioritized-tests.md");

        await fs.writeFile(markdown, "# Relatorio simplificado\n");
        await fs.writeJson(json, {
            categorias: {
                Services: {
                    untested: [
                        {caminho_relativo: "sgc/mapa/service/MapaCriticoService.java"}
                    ]
                },
                Repositories: {
                    untested: [
                        {caminho_relativo: "sgc/mapa/model/CompetenciaRepo.java"}
                    ]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--output", saida], {cwd: diretorioSaida});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Entrada utilizada: unit-test-report.json");
        expect(resultado.stdout).toContain("Encontrados 1 P1, 0 P2, 1 P3");

        const conteudo = await fs.readFile(saida, "utf-8");
        expect(conteudo).toContain("sgc/mapa/service/MapaCriticoService.java");
        expect(conteudo).toContain("sgc/mapa/model/CompetenciaRepo.java");
    });

    test("prioriza apenas backlog acionavel do JSON e preserva evidencia", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-real-"));
        const json = path.join(diretorioSaida, "unit-test-report.json");
        const saida = path.join(diretorioSaida, "prioritized-tests.md");

        await fs.writeJson(json, {
            categorias: {
                Services: {
                    untested: [
                        {
                            caminho_relativo: "sgc/mapa/service/MapaCriticoService.java",
                            evidencia_qualidade: "sem_evidencia_no_escopo"
                        }
                    ]
                },
                DTOs: {
                    untested: [
                        {
                            caminho_relativo: "sgc/mapa/dto/MapaRuidoCommand.java",
                            evidencia_qualidade: "ruido_dto_estrutural",
                            dto_ruido_ignorado: true
                        }
                    ]
                },
                Others: {
                    untested: [
                        {caminho_relativo: "sgc/comum/Mensagens.java", evidencia_qualidade: "fora_escopo_jacoco"},
                        {
                            caminho_relativo: "sgc/seguranca/AcaoPermissao.java",
                            evidencia_qualidade: "cobertura_indireta"
                        }
                    ]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--input", json, "--output", saida]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Encontrados 1 P1, 0 P2, 1 P3");

        const conteudo = await fs.readFile(saida, "utf-8");
        expect(conteudo).toContain("sgc/mapa/service/MapaCriticoService.java");
        expect(conteudo).toContain("sem evidência");
        expect(conteudo).toContain("sgc/seguranca/AcaoPermissao.java");
        expect(conteudo).toContain("cobertura indireta");
        expect(conteudo).not.toContain("Mensagens.java");
        expect(conteudo).not.toContain("MapaRuidoCommand.java");
    });

    test("resume uma fotografia de qualidade a partir de fixture", async () => {
        const resultado = await executarSgc(["qualidade", "resumo", "--json", "--arquivo", FIXTURE_FOTOGRAFIA]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(json.resumo.statusGeral).toBe("verde");
        expect(json.hotspots).toHaveLength(2);
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

    test("despacha ajuda da validacao de modais do frontend", async () => {
        const resultado = await executarSgc(["frontend", "modais", "validar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("ModalPadrao");
        expect(resultado.stdout).toContain("componente-base");
    });

    test("valida previsibilidade estrutural das views em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-views-templates-"));
        const viewsDir = path.join(base, "frontend", "src", "views");

        await fs.outputFile(
            path.join(viewsDir, "PainelView.vue"),
            [
                "<template>",
                "  <LayoutPadrao>",
                "    <PageHeader title=\"Painel\" />",
                "  </LayoutPadrao>",
                "</template>"
            ].join("\n")
        );

        await fs.outputFile(
            path.join(viewsDir, "LegacyView.vue"),
            [
                "<template>",
                "  <div>",
                "    <BModal />",
                "  </div>",
                "</template>"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend",
            "views",
            "templates-validar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.totalViews).toBe(2);
        expect(conteudo.violacoes.some((violacao) => violacao.regra === "view-com-bmodal-cru")).toBe(true);
        expect(conteudo.violacoes.some((violacao) => violacao.regra === "view-sem-layout-padrao")).toBe(true);
        expect(conteudo.violacoes.some((violacao) => violacao.regra === "view-sem-cabecalho-padrao")).toBe(true);
    });

    test("valida padronizacao de modais em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-modais-validar-"));
        await fs.outputFile(
            path.join(base, "frontend", "src", "components", "comum", "ModalPadrao.vue"),
            "<template><BModal title=\"Base\" /></template>"
        );
        await fs.outputFile(
            path.join(base, "frontend", "src", "components", "mapa", "ImpactoMapaModal.vue"),
            "<template><BModal title=\"Impacto\" /></template>"
        );

        const resultado = await executarSgc([
            "frontend",
            "modais",
            "validar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.violacoes).toHaveLength(1);
        expect(conteudo.violacoes[0].arquivo).toBe("frontend/src/components/mapa/ImpactoMapaModal.vue");
        expect(conteudo.violacoes[0].regra).toBe("componente-com-bmodal-cru");
    });

    test("exibe ajuda padronizada no script frontend cobertura auditoria", async () => {
        const resultado = await executarScriptFrontendCobertura(["--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Auditoria unificada de cobertura e risco (Frontend).");
    });

    test("exibe ajuda do comando de sincronizacao de versao do projeto", async () => {
        const resultado = await executarSgc(["projeto", "versao-sincronizar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Sincroniza a versao entre gradle.properties e frontend/package.json.");
    });

    test("sincroniza a versao em um diretorio informado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-versao-sincronizar-"));
        await fs.outputFile(path.join(diretorioBase, "gradle.properties"), "version=1.0.0\nother=value\n");
        await fs.outputJSON(path.join(diretorioBase, "frontend", "package.json"), {name: "exemplo", version: "1.0.0"});

        const resultado = sincronizarVersao("2.3.4", diretorioBase);

        expect(resultado.arquivosAtualizados).toEqual(["gradle.properties", "frontend/package.json"]);
        expect(await fs.readFile(path.join(diretorioBase, "gradle.properties"), "utf8")).toContain("version=2.3.4");
        expect((await fs.readJSON(path.join(diretorioBase, "frontend", "package.json"))).version).toBe("2.3.4");
    });

    test("calcula arvore de linhas usando o diretorio base informado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-arvore-linhas-"));
        await fs.outputFile(path.join(diretorioBase, "src", "Principal.java"), "linha 1\nlinha 2\n");
        await fs.outputFile(path.join(diretorioBase, "README.md"), "linha 1\n");

        const arvore = construirArvore(["src/Principal.java", "README.md"], diretorioBase);
        calcularTotais(arvore);

        expect(arvore.linhas).toBe(5);
        expect(arvore.filhos.src.linhas).toBe(3);
        expect(arvore.filhos.src.filhos["Principal.java"].linhas).toBe(3);
    });

    test("lista arquivos Git a partir do diretorio base informado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-arvore-git-"));
        await execa("git", ["init", "--quiet"], {cwd: diretorioBase});
        await fs.outputFile(path.join(diretorioBase, "arquivo.txt"), "conteúdo\n");
        await execa("git", ["add", "arquivo.txt"], {cwd: diretorioBase});

        expect(listarArquivosGit(diretorioBase)).toEqual(["arquivo.txt"]);
    });

    test("executa o diagnostico em JSON", async () => {
        const resultado = await executarSgc(["projeto", "diagnostico", "--json"]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(["ok", "alerta"]).toContain(json.statusGeral);
        expect(Array.isArray(json.verificacoes)).toBe(true);
        expect(json.verificacoes.some((item) => item.nome === "node")).toBe(true);
    });

    test("audita cobertura JaCoCo a partir de arquivo e base externos", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-backend-"));
        const caminhoXml = path.join(diretorioBase, "jacoco.xml");
        await fs.outputFile(caminhoXml, [
            "<report name=\"exemplo\">",
            "  <counter type=\"INSTRUCTION\" missed=\"1\" covered=\"2\"/>",
            "  <counter type=\"BRANCH\" missed=\"1\" covered=\"1\"/>",
            "  <counter type=\"LINE\" missed=\"1\" covered=\"1\"/>",
            "  <counter type=\"METHOD\" missed=\"0\" covered=\"1\"/>",
            "  <counter type=\"COMPLEXITY\" missed=\"0\" covered=\"1\"/>",
            "  <package name=\"com.exemplo\">",
            "    <sourcefile name=\"Servico.java\">",
            "      <line nr=\"10\" ci=\"1\" mb=\"1\" cb=\"1\"/>",
            "      <line nr=\"11\" ci=\"0\" mb=\"0\" cb=\"0\"/>",
            "      <counter type=\"COMPLEXITY\" missed=\"0\" covered=\"1\"/>",
            "    </sourcefile>",
            "  </package>",
            "</report>"
        ].join("\n"));

        const resultado = await executarSgc([
            "backend",
            "cobertura",
            "ramificacoes",
            "--json",
            "--base",
            diretorioBase,
            "--arquivo",
            caminhoXml
        ]);

        expect(resultado.exitCode).toBe(0);
        const json = JSON.parse(resultado.stdout);
        expect(json.totais.percentual).toBe(50);
        expect(json.classes[0].nome).toBe("com.exemplo.Servico");
        expect(json.classes[0].branchesPerdidos).toBe(1);
        expect(json.classes[0].branchesPerdidosLista).toEqual(["10(1/2)"]);
    });

    test("simula e executa limpeza em diretório temporário", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-scripts-"));
        await fs.ensureDir(path.join(diretorioBase, "backend", "build"));
        await fs.ensureDir(path.join(diretorioBase, "toolkit", "qualidade", "artefatos", "mais-recente"));
        await fs.outputFile(path.join(diretorioBase, "backend-coverage-auditoria.md"), "# teste");
        await fs.outputFile(path.join(diretorioBase, "toolkit", "qualidade", "artefatos", "mais-recente", "resumo.md"), "ok");

        const previa = await executarSgc(["projeto", "limpar", "--json", "--base", diretorioBase]);
        expect(previa.exitCode).toBe(0);
        const jsonPrevia = JSON.parse(previa.stdout);
        expect(jsonPrevia.modo).toBe("simular");
        expect(jsonPrevia.itens).toContain("backend/build");
        expect(await fs.pathExists(path.join(diretorioBase, "backend", "build"))).toBe(true);

        const execucao = await executarSgc(["projeto", "limpar", "--json", "--confirmar", "--base", diretorioBase]);
        expect(execucao.exitCode).toBe(0);
        const jsonExecucao = JSON.parse(execucao.stdout);
        expect(jsonExecucao.modo).toBe("executar");
        expect(await fs.pathExists(path.join(diretorioBase, "backend", "build"))).toBe(false);
        expect(await fs.pathExists(path.join(diretorioBase, "backend-coverage-auditoria.md"))).toBe(false);
    });

    test("nao possui diretorios legados de scripts em backend/frontend", async () => {
        expect(await fs.pathExists(DIRETORIO_SCRIPTS_BACKEND_LEGADO)).toBe(false);
        expect(await fs.pathExists(DIRETORIO_SCRIPTS_FRONTEND_LEGADO)).toBe(false);
    });

    test("projeto diagnostico identifica corretamente a ausencia de arquivos essenciais e falha com codigo 1", async () => {
        const diretorioVazio = await mkdtemp(path.join(os.tmpdir(), "sgc-diagnostico-vazio-"));

        // Executamos o diagnostico passando o diretorio temporario vazio como base
        const resultado = await executarSgc(["projeto", "diagnostico", "--json", "--base", diretorioVazio]);

        // Como arquivos essenciais como gradlew e package.json estão ausentes, deve retornar código de erro 1
        expect(resultado.exitCode).toBe(1);

        const dados = JSON.parse(resultado.stdout);
        expect(dados.statusGeral).toBe("falha");
        expect(dados.totais.falha).toBeGreaterThan(0);

        // Verifica se um dos arquivos obrigatórios ausentes foi reportado como falha
        const falhaGradlew = dados.verificacoes.find((v) => v.nome === "gradlew");
        expect(falhaGradlew).toBeDefined();
        expect(falhaGradlew.status).toBe("falha");
        expect(falhaGradlew.detalhe).toContain("gradlew ausente");
    });

    test("lista com sucesso identificadores de teste do frontend em recorte controlado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-listar-"));

        // Criar arquivos .vue de teste com identificadores
        await fs.outputFile(
            path.join(diretorioBase, "ComponenteA.vue"),
            "<template><button data-test-codigo=\"btn-salvar\">Salvar</button></template>"
        );
        await fs.outputFile(
            path.join(diretorioBase, "ComponenteB.vue"),
            "<template><input data-testid=\"input-nome\" /></template>"
        );

        const resultado = await executarSgc(["frontend", "identificadores-teste", "listar", "--base", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("ComponenteA.vue");
        expect(resultado.stdout).toContain("btn-salvar");
        expect(resultado.stdout).toContain("ComponenteB.vue");
        expect(resultado.stdout).toContain("input-nome");
    });

    test("detecta corretamente identificadores de teste duplicados e falha com codigo 1", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-duplicados-"));

        // Criar dois arquivos com o mesmo test-id
        await fs.outputFile(
            path.join(diretorioBase, "ComponenteX.vue"),
            "<template><button data-testid=\"btn-acao\">Ação X</button></template>"
        );
        await fs.outputFile(
            path.join(diretorioBase, "ComponenteY.vue"),
            "<template><div data-testid=\"btn-acao\">Ação Y</div></template>"
        );

        const resultado = await executarSgc(["frontend", "identificadores-teste", "listar-duplicados", "--base", diretorioBase]);

        // O script deve falhar com exitCode 1 quando encontra duplicados
        expect(resultado.exitCode).toBe(1);
        expect(resultado.stdout).toContain("Identificadores de teste duplicados encontrados");
        expect(resultado.stdout).toContain("btn-acao");
        expect(resultado.stdout).toContain("ComponenteX.vue");
        expect(resultado.stdout).toContain("ComponenteY.vue");
    });

    test("passa com sucesso se nao houver identificadores de teste duplicados", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-unicos-"));

        await fs.outputFile(
            path.join(diretorioBase, "ComponenteUnico.vue"),
            "<template><button data-testid=\"btn-unico\">Ação Única</button></template>"
        );

        const resultado = await executarSgc(["frontend", "identificadores-teste", "listar-duplicados", "--base", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhum identificador de teste duplicado encontrado.");
    });
}, 30000);
