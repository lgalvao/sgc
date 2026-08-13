import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {execa} from "execa";
import {executarSgc, escreverArquivo, escreverJson, lerArquivo, lerJson, existe, criarDiretorio} from "./apoio.js";
import {calcularTotais, construirArvore, ehArquivoTeste, listarArquivosGit, lerOpcoes} from "../projeto/arvore-linhas.js";
import {sincronizarVersao} from "../projeto/versao-sincronizar.js";
import {executarVerificacaoAmbiente, obterRecursosAmbientePadrao} from "../projeto/ambiente-verificar.js";
import {limparArtefatos, obterPadroesArtefatos} from "../projeto/artefatos-limpar.js";
import {executarTarefasQualidade} from "../qualidade/tarefas-executar.js";
import {executarAuditoriaDependencias} from "../projeto/dependencias-auditar.js";
import {VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";

type ChamadaComando = {
    comando: string;
    argumentos: readonly string[];
    base?: string;
    diretorio?: string;
};

interface VerificacaoAmbienteJson {
    nome: string;
    status?: string;
    detalhe?: string;
}

describe("Comandos de projeto do toolkit", () => {
    test("exibe ajuda do comando de sincronizacao de versao do projeto", async () => {
        const resultado = await executarSgc(["projeto", "versao-sincronizar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Atualiza gradle.properties e o package.json do frontend configurado");
    });

    test("simula e aplica sincronizacao de versao em um diretorio informado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-versao-sincronizar-"));
        await escreverArquivo(path.join(diretorioBase, "gradle.properties"), "version=1.0.0\nother=value\n");
        await escreverJson(path.join(diretorioBase, "frontend", "package.json"), {name: "exemplo", version: "1.0.0"});

        const simulacao = sincronizarVersao("2.3.4", diretorioBase);

        expect(simulacao.gravado).toBe(false);
        expect(simulacao.arquivosAtualizados).toEqual([]);
        expect(simulacao.arquivosPendentes).toEqual(["gradle.properties", "frontend/package.json"]);
        expect(await lerArquivo(path.join(diretorioBase, "gradle.properties"), "utf8")).toContain("version=1.0.0");
        expect((await lerJson<{version: string}>(path.join(diretorioBase, "frontend", "package.json"))).version).toBe("1.0.0");

        const resultado = sincronizarVersao("2.3.4", diretorioBase, true);

        expect(resultado.gravado).toBe(true);
        expect(resultado.arquivosAtualizados).toEqual(["gradle.properties", "frontend/package.json"]);
        expect(await lerArquivo(path.join(diretorioBase, "gradle.properties"), "utf8")).toContain("version=2.3.4");
        expect((await lerJson<{version: string}>(path.join(diretorioBase, "frontend", "package.json"))).version).toBe("2.3.4");
    });

    test("aplica sincronizacao de versao pela CLI somente com gravar", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-versao-sincronizar-cli-"));
        await escreverArquivo(path.join(diretorioBase, "gradle.properties"), "version=1.0.0\n");
        await escreverJson(path.join(diretorioBase, "frontend", "package.json"), {name: "exemplo", version: "1.0.0"});

        const simulacao = await executarSgc([
            "projeto",
            "versao-sincronizar",
            "2.3.4",
            "--base",
            diretorioBase
        ]);

        expect(simulacao.exitCode).toBe(0);
        expect(simulacao.stdout).toContain("[simulação]");
        expect(await lerArquivo(path.join(diretorioBase, "gradle.properties"), "utf8")).toContain("version=1.0.0");

        const aplicacao = await executarSgc([
            "projeto",
            "versao-sincronizar",
            "2.3.4",
            "--base",
            diretorioBase,
            "--gravar"
        ]);

        expect(aplicacao.exitCode).toBe(0);
        expect(aplicacao.stdout).toContain("[v]");
        expect(await lerArquivo(path.join(diretorioBase, "gradle.properties"), "utf8")).toContain("version=2.3.4");
    });

    test("sincroniza versao no frontend configurado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-versao-frontend-configurado-"));
        await escreverJson(path.join(diretorioBase, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {frontend: "cliente"}
        });
        await escreverArquivo(path.join(diretorioBase, "gradle.properties"), "version=1.0.0\n");
        await escreverJson(path.join(diretorioBase, "cliente", "package.json"), {name: "cliente", version: "1.0.0"});

        const resultado = sincronizarVersao("2.4.0", diretorioBase, true);

        expect(resultado.arquivosAtualizados).toEqual(["gradle.properties", "cliente/package.json"]);
        expect((await lerJson<{version: string}>(path.join(diretorioBase, "cliente", "package.json"))).version).toBe("2.4.0");
    });

    test("calcula arvore de linhas usando o diretorio base informado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-arvore-linhas-"));
        await escreverArquivo(path.join(diretorioBase, "src", "Principal.java"), "linha 1\nlinha 2\n");
        await escreverArquivo(path.join(diretorioBase, "README.md"), "linha 1\n");

        const arvore = construirArvore(["src/Principal.java", "README.md"], diretorioBase);
        calcularTotais(arvore);

        expect(arvore.linhas).toBe(5);
        expect(arvore.filhos.src.linhas).toBe(3);
        expect(arvore.filhos.src.filhos["Principal.java"].linhas).toBe(3);
    });

    test("lista arquivos Git a partir do diretorio base informado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-arvore-git-"));
        await execa("git", ["init", "--quiet"], {cwd: diretorioBase});
        await escreverArquivo(path.join(diretorioBase, "arquivo.txt"), "conteúdo\n");
        await execa("git", ["add", "arquivo.txt"], {cwd: diretorioBase});

        expect(listarArquivosGit(diretorioBase)).toEqual(["arquivo.txt"]);
    });

    test("padroniza opcoes da arvore de linhas em portugues e reconhece testes Java externos", () => {
        expect(lerOpcoes([
            "--base",
            "/tmp/projeto",
            "--profundidade",
            "2",
            "--minimo-linhas",
            "10",
            "--excluir-testes"
        ])).toMatchObject({
            diretorioBase: "/tmp/projeto",
            profundidadeMaxima: 2,
            minimoLinhas: 10,
            excluirTestes: true
        });
        expect(ehArquivoTeste("aplicacao/src/test/java/ExemploTest.java")).toBe(true);
        expect(ehArquivoTeste("aplicacao/src/main/java/Exemplo.java")).toBe(false);
    });

    test("executa a verificacao do ambiente em JSON", async () => {
        const resultado = await executarSgc(["projeto", "ambiente", "verificar", "--json"]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(["ok", "alerta"]).toContain(json.statusGeral);
        expect(Array.isArray(json.verificacoes)).toBe(true);
        expect(json.verificacoes.some((item: Pick<VerificacaoAmbienteJson, "nome">) => item.nome === "node")).toBe(true);
    });

    test("verificacao do ambiente aceita recursos registrados por projeto externo", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-ambiente-configurado-"));
        await escreverArquivo(path.join(diretorioBase, "package.json"), "{}\n");

        const resultado = await executarVerificacaoAmbiente({
            base: diretorioBase,
            silencioso: true,
            recursos: [{
                tipo: "arquivo",
                nome: "manifesto",
                caminho: "package.json",
                obrigatorio: true,
                categoria: "Configuração do Projeto"
            }],
            comandosRegistrados: []
        });

        expect(resultado.statusGeral).toBe("ok");
        expect(resultado.verificacoes).toHaveLength(1);
        expect(resultado.verificacoes[0]).toMatchObject({nome: "manifesto", status: "ok"});
    });

    test("verificacao do ambiente deriva recursos estruturais dos diretorios configurados", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-ambiente-diretorios-"));
        await escreverJson(path.join(diretorioBase, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backend: "servidor",
                frontend: "cliente",
                testesIntegracao: "testes-e2e"
            }
        });

        const caminhos = obterRecursosAmbientePadrao(diretorioBase).flatMap(recurso => "caminho" in recurso ? [recurso.caminho] : []);
        expect(caminhos).toContain("servidor/build.gradle.kts");
        expect(caminhos).toContain("cliente/package.json");
        expect(caminhos).toContain("testes-e2e/package.json");
        expect(caminhos).not.toContain("backend/build.gradle.kts");
        expect(caminhos).not.toContain("frontend/package.json");
        expect(caminhos).not.toContain("toolkit/package.json");

        const verificacaoAmbiente = await executarVerificacaoAmbiente({
            base: diretorioBase,
            silencioso: true,
            comandosRegistrados: []
        });
        expect(verificacaoAmbiente.verificacoes.some(verificacao => verificacao.nome === "toolkit/package.json")).toBe(false);

        await escreverArquivo(path.join(diretorioBase, "toolkit", "sgc.ts"), "");
        const caminhosSgc = obterRecursosAmbientePadrao(diretorioBase).flatMap(recurso => "caminho" in recurso ? [recurso.caminho] : []);
        expect(caminhosSgc).toContain("toolkit/package.json");
    });

    test("audita cobertura JaCoCo a partir de arquivo e base externos", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-backend-"));
        const caminhoXml = path.join(diretorioBase, "jacoco.xml");
        await escreverArquivo(caminhoXml, [
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
        expect(json.classes[0].ramificacoesPerdidas).toBe(1);
        expect(json.classes[0].ramificacoesPerdidasLista).toEqual(["10(1/2)"]);
    });

    test("simula e executa limpeza em diretório temporário", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-scripts-"));
        await criarDiretorio(path.join(diretorioBase, "backend", "build"));
        await criarDiretorio(path.join(diretorioBase, "toolkit", "qualidade", "artefatos", "mais-recente"));
        await escreverArquivo(path.join(diretorioBase, "backend-cobertura-auditoria.md"), "# teste");
        await escreverArquivo(path.join(diretorioBase, "toolkit", "qualidade", "artefatos", "mais-recente", "resumo.md"), "ok");

        const previa = await executarSgc(["projeto", "artefatos", "limpar", "--json", "--base", diretorioBase]);
        expect(previa.exitCode).toBe(0);
        const jsonPrevia = JSON.parse(previa.stdout);
        expect(jsonPrevia.modo).toBe("simular");
        expect(jsonPrevia.itens).toContain("backend/build");
        expect(await existe(path.join(diretorioBase, "backend", "build"))).toBe(true);

        const execucao = await executarSgc(["projeto", "artefatos", "limpar", "--json", "--confirmar", "--base", diretorioBase]);
        expect(execucao.exitCode).toBe(0);
        const jsonExecucao = JSON.parse(execucao.stdout);
        expect(jsonExecucao.modo).toBe("executar");
        expect(await existe(path.join(diretorioBase, "backend", "build"))).toBe(false);
        expect(await existe(path.join(diretorioBase, "backend-cobertura-auditoria.md"))).toBe(false);
    });

    test("aceita política de padrões de limpeza de projeto externo", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-limpeza-politica-"));
        await escreverArquivo(path.join(diretorioBase, "artefatos-projeto", "saida.txt"), "temporario");
        await escreverArquivo(path.join(diretorioBase, "nao-listado.txt"), "preservar");

        const resultado = await limparArtefatos({
            base: diretorioBase,
            padroes: ["artefatos-projeto"]
        });

        expect(resultado).toMatchObject({
            diretorioBase,
            modo: "simular",
            total: 1,
            itens: ["artefatos-projeto"]
        });
        expect(await existe(path.join(diretorioBase, "artefatos-projeto"))).toBe(true);
        expect(await existe(path.join(diretorioBase, "nao-listado.txt"))).toBe(true);
    });

    test("deriva padrões de limpeza dos diretórios configurados e remove nomes legados", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-limpeza-configurada-"));
        await escreverJson(path.join(diretorioBase, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backend: "servidor",
                frontend: "cliente",
                artefatosQualidade: "artefatos"
            }
        });

        const padroes = obterPadroesArtefatos(diretorioBase);
        expect(padroes).toContain("servidor/build");
        expect(padroes).toContain("cliente/coverage");
        expect(padroes).toContain("artefatos");
        expect(padroes).not.toContain("backend/build");
        expect(padroes).not.toContain("frontend/coverage");
        expect(padroes.some(padrao => padrao.includes("latest"))).toBe(false);
        expect(padroes).not.toContain("complexity-ranking.md");
    });

    test("executa catálogo de qualidade externo na base indicada", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-base-"));
        const chamadas: ChamadaComando[] = [];
        const resultado = await executarTarefasQualidade("verificacao", {
            base: diretorioBase,
            perfis: {
                verificacao: {
                    descricao: "Verificacao do projeto externo.",
                    tarefas: [{
                        titulo: "Comando externo",
                        comando: "ferramenta",
                        argumentos: ["verificar", "--json"]
                    }]
                }
            },
            executarComando: async (comando, argumentos, base) => {
                chamadas.push({comando, argumentos, base});
            }
        });

        expect(resultado).toEqual({
            diretorioBase,
            perfil: "verificacao",
            tarefas: ["Comando externo"]
        });
        expect(chamadas).toEqual([{
            comando: "ferramenta",
            argumentos: ["verificar", "--json"],
            base: diretorioBase
        }]);
    });

    test("audita dependencias em escopos e comandos externos", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-dependencias-base-"));
        const chamadas: ChamadaComando[] = [];
        const resultado = await executarAuditoriaDependencias({
            base: diretorioBase,
            escopos: [
                {
                    titulo: "Auditar cliente",
                    segmento: "cliente",
                    comando: "ferramenta",
                    argumentos: ["dependencias"]
                },
                {
                    titulo: "Auditar servidor",
                    segmento: "servidor",
                    comando: "ferramenta",
                    argumentos: ["dependencias", "--json"]
                }
            ],
            executarComando: async (comando, argumentos, diretorio) => {
                chamadas.push({comando, argumentos, diretorio});
                return {exitCode: 0};
            }
        });

        expect(resultado).toEqual({
            diretorioBase,
            statusGeral: "ok",
            resultados: [
                {escopo: "Auditar cliente", codigoSaida: 0, status: "ok"},
                {escopo: "Auditar servidor", codigoSaida: 0, status: "ok"}
            ]
        });
        expect(chamadas).toEqual([
            {
                comando: "ferramenta",
                argumentos: ["dependencias"],
                diretorio: path.join(diretorioBase, "cliente")
            },
            {
                comando: "ferramenta",
                argumentos: ["dependencias", "--json"],
                diretorio: path.join(diretorioBase, "servidor")
            }
        ]);
    });

    test("distingue achados esperados de falhas de execução", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-dependencias-status-"));
        const resultado = await executarAuditoriaDependencias({
            base: diretorioBase,
            escopos: [
                {
                    titulo: "Dependencias desatualizadas",
                    segmento: "",
                    comando: "npm",
                    argumentos: ["outdated"],
                    codigoNaoZeroIndicaAchados: true
                },
                {
                    titulo: "Ferramenta indisponivel",
                    segmento: "",
                    comando: "ferramenta",
                    argumentos: []
                }
            ],
            executarComando: async (comando) => comando === "npm" ? {exitCode: 1} : {exitCode: undefined}
        });

        expect(resultado.statusGeral).toBe("falha");
        expect(resultado.resultados).toEqual([
            {escopo: "Dependencias desatualizadas", codigoSaida: 1, status: "achados"},
            {escopo: "Ferramenta indisponivel", codigoSaida: 1, status: "falha"}
        ]);
    });
}, 30000);
