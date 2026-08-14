import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    DIRETORIO_RAIZ,
    executarSgc,
    criarDiretorio,
    escreverArquivo,
    escreverJson,
    existe
} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";
import {
    obterOpcoesPlaywright,
    principal as coletarFotografiaQualidade,
    type AdaptadorQualidade,
    type ContextoColeta,
    type ExecucaoQualidade,
    type ResultadoComando
} from "../qualidade/coleta-execucao.js";
import {criarAdaptadoresSgc, PERFIS_SGC} from "../qualidade/coleta-adaptadores-sgc.js";
import {executarComando, resolverExecutavelLocal} from "../qualidade/coleta-executor.js";
import {consolidarJUnit, extrairPontosCriticosQualidade, parseJsonSeguro} from "../qualidade/coleta-leitores.js";

const FIXTURE_FOTOGRAFIA = path.join(DIRETORIO_RAIZ, "toolkit", "testes", "fixtures", "qualidade", "fotografia.json");

describe("Qualidade do toolkit", () => {
    test("resume uma fotografia de qualidade a partir de fixture", async () => {
        const resultado = await executarSgc(["qualidade", "resumo", "--json", "--arquivo", FIXTURE_FOTOGRAFIA]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(json.versaoSchema).toBe("3.0.0");
        expect(json.resumo.statusGeral).toBe("verde");
        expect(json.pontosCriticos).toHaveLength(2);
    });

    test("resume uma fotografia sem despejar metricas detalhadas", async () => {
        const resultado = await executarSgc(["qualidade", "resumo", "--json-resumido", "--arquivo", FIXTURE_FOTOGRAFIA]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(json).toMatchObject({
            versaoResumo: 1,
            versaoSchema: "3.0.0",
            truncado: true,
            limiteItens: 20,
            resumo: {statusGeral: "verde"}
        });
        expect(json.verificacoes).toEqual([
            {codigo: "servidor-cobertura", status: "sucesso", sumario: "Cobertura do servidor consistente."},
            {codigo: "cliente-cobertura", status: "sucesso", sumario: "Cobertura do cliente acima do limiar."},
            {codigo: "cliente-lint", status: "sucesso", sumario: "Lint sem erros."}
        ]);
        expect(json.pontosCriticos).toHaveLength(2);
        expect(json.metricas).toBeUndefined();
    });

    test("resume a fotografia mais recente a partir da base externa", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-resumo-base-"));
        const caminhoFotografia = path.join(diretorioBase, "toolkit", "qualidade", "artefatos", "mais-recente", "fotografia.json");
        await criarDiretorio(path.dirname(caminhoFotografia));
        await escreverJson(caminhoFotografia, {
            versaoSchema: "3.0.0",
            resumo: {
                statusGeral: "verde",
                totais: {verificacoes: 1}
            },
            verificacoes: [],
            pontosCriticos: []
        });

        const resultado = await executarSgc(["qualidade", "resumo", "--json", "--base", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        const json = JSON.parse(resultado.stdout);
        expect(json.versaoSchema).toBe("3.0.0");
        expect(json.resumo.statusGeral).toBe("verde");
        expect(json.caminho).toBe("toolkit/qualidade/artefatos/mais-recente/fotografia.json");
    });

    test("rejeita fotografia de qualidade com versao incompativel", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-versao-invalida-"));
        const caminhoFotografia = path.join(diretorioBase, "fotografia.json");
        await escreverJson(caminhoFotografia, {versaoSchema: "99.0.0", resumo: {}, verificacoes: [], pontosCriticos: []});

        const resultado = await executarSgc([
            "qualidade",
            "resumo",
            "--json",
            "--base",
            diretorioBase,
            "--arquivo",
            caminhoFotografia
        ]);

        expect(resultado.exitCode).toBe(1);
        expect(resultado.stdout).toBe("");
        expect(resultado.stderr).toContain("versao ausente ou incompativel");
    });

    test("rejeita fotografia de qualidade com estrutura invalida", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-estrutura-invalida-"));
        const caminhoFotografia = path.join(diretorioBase, "fotografia.json");
        await escreverJson(caminhoFotografia, {
            versaoSchema: "3.0.0",
            resumo: {},
            verificacoes: "invalido",
            pontosCriticos: []
        });

        const resultado = await executarSgc([
            "qualidade",
            "resumo",
            "--json",
            "--base",
            diretorioBase,
            "--arquivo",
            caminhoFotografia
        ]);

        expect(resultado.exitCode).toBe(1);
        expect(resultado.stdout).toBe("");
        expect(resultado.stderr).toContain("estrutura invalida");
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

    test("resolve a configuracao Playwright a partir dos testes de integracao da base", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-playwright-configurado-"));
        await escreverJson(path.join(diretorioBase, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                testesIntegracao: "testes-e2e"
            }
        });

        expect(obterOpcoesPlaywright(diretorioBase)).toEqual({
            comando: "playwright",
            descricao: "playwright test --config=testes-e2e/playwright.config.ts",
            argumentos: [
                "test",
                "--config=testes-e2e/playwright.config.ts",
                "--reporter=json"
            ]
        });
    });

    test("resolve binarios locais sem recorrer a download implicito", () => {
        const caminhoTsx = resolverExecutavelLocal("tsx", DIRETORIO_RAIZ);
        expect(caminhoTsx).toContain(`${path.sep}node_modules${path.sep}.bin${path.sep}`);
        expect(resolverExecutavelLocal("comando-inexistente", DIRETORIO_RAIZ)).toBe("comando-inexistente");
    });

    test("rejeita adaptador ausente antes de criar artefatos da coleta", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-adaptador-ausente-"));

        await expect(coletarFotografiaQualidade(["--perfil", "externo", "--base", diretorioBase], {
            perfis: {externo: ["adaptadorAusente"]},
            adaptadores: {}
        })).rejects.toThrow("adaptadorAusente");

        expect(await existe(path.join(diretorioBase, "toolkit"))).toBe(false);
    });

    test("aceita uma fabrica de contexto de artefatos para projeto externo", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-contexto-externo-"));
        const diretorioArtefatos = path.join(diretorioBase, ".qualidade");
        const caminhoFotografiaPersonalizado = path.join(diretorioBase, "relatorios", "qualidade.json");
        let baseRecebida = "";
        const adaptador: AdaptadorQualidade = async contexto => ({
            codigo: "checagemExterna",
            nome: "Checagem externa",
            categoria: "qualidade",
            status: "sucesso",
            duracaoMs: 0,
            comando: "checagem",
            diretorio: contexto.base,
            sumario: "",
            metricas: {},
            erros: [],
            artefatos: []
        });

        const fotografia = await coletarFotografiaQualidade(["--perfil", "externo", "--base", diretorioBase], {
            perfis: {externo: ["checagemExterna"]},
            adaptadores: {checagemExterna: adaptador},
            coletarMetadados: async () => ({origem: "externa"}),
            prepararDiretoriosFotografia: async () => {},
            persistirFotografia: async fotografiaGerada => {
                await escreverJson(caminhoFotografiaPersonalizado, fotografiaGerada);
                return caminhoFotografiaPersonalizado;
            },
            criarContexto: base => {
                baseRecebida = base;
                return {
                    base,
                    diretorioArtefatos,
                    diretorioExecucoes: path.join(diretorioArtefatos, "execucoes"),
                    diretorioMaisRecente: path.join(diretorioArtefatos, "mais-recente"),
                    diretorioServidor: path.join(base, "servidor"),
                    diretorioCliente: path.join(base, "cliente"),
                    diretorioCodigoCliente: path.join(base, "cliente", "src")
                };
            }
        });

        expect(baseRecebida).toBe(diretorioBase);
        expect(fotografia.versaoSchema).toBe("3.0.0");
        expect(fotografia.metadados.controleVersao).toEqual({origem: "externa"});
        expect(fotografia.verificacoes).toHaveLength(1);
        expect(await existe(caminhoFotografiaPersonalizado)).toBe(true);
        expect(await existe(diretorioArtefatos)).toBe(false);
        expect(await existe(path.join(diretorioBase, "toolkit"))).toBe(false);
    });

    test("caracteriza leitura segura de JSON, pontos criticos e relatorios JUnit", async () => {
        expect(parseJsonSeguro<{status: string}>("{\"status\":\"ok\"}", {status: "falha"})).toEqual({status: "ok"});
        expect(parseJsonSeguro("invalido", {status: "falha"})).toEqual({status: "falha"});
        expect(extrairPontosCriticosQualidade({
            pontosCriticos: [
                {arquivo: "src/valido.ts", pontuacao: 12},
                {arquivo: "src/sem-pontuacao.ts"},
                "invalido"
            ]
        })).toEqual([{arquivo: "src/valido.ts", pontuacao: 12}]);
        expect(extrairPontosCriticosQualidade({pontosCriticos: "invalido"})).toEqual([]);

        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-leitores-"));
        const diretorioRelatorio = path.join(base, "backend", "build", "test-results");
        await escreverArquivo(
            path.join(diretorioRelatorio, "TEST-primeiro.xml"),
            "<testsuite tests=\"3\" failures=\"1\" errors=\"0\" skipped=\"1\" time=\"1.25\" />"
        );
        await escreverArquivo(
            path.join(diretorioRelatorio, "TEST-segundo.xml"),
            "<testsuite tests=\"2\" failures=\"0\" errors=\"1\" skipped=\"0\" time=\"0.75\" />"
        );

        const relatorio = await consolidarJUnit(diretorioRelatorio, base);
        expect(relatorio).toMatchObject({
            testes: 5,
            falhas: 2,
            ignorados: 1,
            tempoSegundos: 2,
            sucessos: 2
        });
        expect(relatorio.arquivosXml).toEqual([
            "backend/build/test-results/TEST-primeiro.xml",
            "backend/build/test-results/TEST-segundo.xml"
        ]);
        expect(await consolidarJUnit(path.join(base, "ausente"), base)).toMatchObject({
            testes: 0,
            falhas: 0,
            ignorados: 0,
            sucessos: 0,
            arquivosXml: []
        });
    });

    test("caracteriza executor de subprocessos em sucesso e falha", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-executor-"));
        const sucesso = await executarComando({
            comando: process.execPath,
            args: ["-e", "process.stdout.write('ok')"],
            cwd: base
        });
        expect(sucesso).toMatchObject({codigoSaida: 0, saida: "ok", erro: ""});
        expect(sucesso.duracaoMs).toBeGreaterThanOrEqual(0);

        const falha = await executarComando({
            comando: process.execPath,
            args: ["-e", "process.stderr.write('falhou'); process.exitCode = 3"],
            cwd: base
        });
        expect(falha).toMatchObject({codigoSaida: 3, saida: "", erro: "falhou"});

        const comandoAusente = await executarComando({
            comando: path.join(base, "comando-ausente"),
            args: [],
            cwd: base
        });
        expect(comandoAusente.codigoSaida).toBe(-1);
        expect(comandoAusente.erro).not.toBe("");
    });

    test("caracteriza perfis e adaptadores SGC por composição", async () => {
        expect(PERFIS_SGC.servidor).toEqual([
            "testesServidorUnitarios",
            "testesServidorIntegracao",
            "coberturaServidor"
        ]);
        expect(PERFIS_SGC.cliente).toContain("identificadoresTesteCliente");

        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-adaptadores-"));
        const contexto: ContextoColeta = {
            base: diretorioBase,
            diretorioArtefatos: path.join(diretorioBase, "artefatos"),
            diretorioExecucoes: path.join(diretorioBase, "artefatos", "execucoes"),
            diretorioMaisRecente: path.join(diretorioBase, "artefatos", "mais-recente"),
            diretorioServidor: path.join(diretorioBase, "backend"),
            diretorioCliente: path.join(diretorioBase, "frontend"),
            diretorioCodigoCliente: path.join(diretorioBase, "frontend", "src")
        };
        await escreverArquivo(
            path.join(diretorioBase, "backend", "build", "reports", "jacoco", "test", "jacocoTestReport.xml"),
            "<report><counter type=\"LINE\" missed=\"1\" covered=\"2\"/><counter type=\"BRANCH\" missed=\"0\" covered=\"1\"/></report>"
        );
        await escreverJson(path.join(diretorioBase, "frontend", "coverage", "coverage-final.json"), {
            [path.join(diretorioBase, "frontend", "src", "Exemplo.ts")]: {
                s: {"1": 1},
                f: {"1": 1},
                b: {"1": [1]},
                statementMap: {"1": {}}
            }
        });

        const criarExecucao = (codigo: string, nome: string, categoria: "teste" | "cobertura" | "qualidade", comando: string, diretorio: string): ExecucaoQualidade => ({
            codigo,
            nome,
            categoria,
            status: "nao_executado",
            duracaoMs: 0,
            comando,
            diretorio,
            sumario: "",
            metricas: {},
            erros: [],
            artefatos: []
        });
        const executarComandoFake = async (): Promise<ResultadoComando> => ({codigoSaida: 0, saida: "", erro: "", duracaoMs: 4});
        const executarComandoSgcFake = async (_contexto: ContextoColeta, argumentos: string[]): Promise<ResultadoComando> => ({
            codigoSaida: 0,
            saida: JSON.stringify(argumentos.includes("residuos")
                ? {status: "ok", resumo: {pontuacaoTotal: 90, classificacao: "inventario"}, violacoes: [], avisos: [], pontosCriticos: []}
                : {resumo: {pontuacaoTotal: 95, classificacao: "politica-sgc", metricas: {}}, pontosCriticos: []}),
            erro: "",
            duracaoMs: 3
        });
        const adaptadores = criarAdaptadoresSgc({
            criarExecucao,
            executarComando: executarComandoFake,
            executarComandoSgc: executarComandoSgcFake,
            consolidarJUnit: async () => ({testes: 2, falhas: 0, ignorados: 0, tempoSegundos: 0.2, sucessos: 2, arquivosXml: []}),
            registrarResultadoExecucao: (execucao, resultado) => {
                execucao.duracaoMs = resultado.duracaoMs;
                if (resultado.codigoSaida !== 0) execucao.erros = [resultado.erro];
            },
            parseJsonSeguro: <T>(conteudo: string, fallback: T): T => {
                try {
                    return JSON.parse(conteudo) as T;
                } catch {
                    return fallback;
                }
            },
            obterOpcoesPlaywright: () => ({
                comando: "playwright",
                descricao: "playwright test --reporter=json",
                argumentos: ["test", "--reporter=json"]
            })
        });

        const resultados = await Promise.all([
            adaptadores.testesServidorUnitarios(contexto),
            adaptadores.testesServidorIntegracao(contexto),
            adaptadores.coberturaServidor(contexto),
            adaptadores.coberturaCliente(contexto),
            adaptadores.lintCliente(contexto),
            adaptadores.tiposCliente(contexto),
            adaptadores.residuosCliente(contexto),
            adaptadores.arquiteturaCliente(contexto),
            adaptadores.identificadoresTesteCliente(contexto),
            adaptadores.testesIntegracaoPlaywright(contexto)
        ]);

        expect(resultados.every(resultado => resultado.status === "sucesso")).toBe(true);
        expect(resultados.find(resultado => resultado.codigo === "servidor-cobertura")?.sumario).toContain("ramificações");
        expect(resultados.find(resultado => resultado.codigo === "cliente-cobertura")?.sumario).toContain("linhas");
        expect(resultados.find(resultado => resultado.codigo === "e2e-playwright")?.sumario).toContain("0 testes E2E");
    });
}, 30000);
