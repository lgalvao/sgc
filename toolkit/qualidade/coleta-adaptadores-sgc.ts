import path from "node:path";
import process from "node:process";
import {extrairCoberturaJacoco} from "../lib/dominios/cobertura-java.js";
import {extrairCoberturaFrontend} from "../lib/dominios/cobertura-web.js";
import type {
    CatalogoAdaptadores,
    ContextoColeta,
    ExecucaoQualidade,
    OpcoesComando,
    OpcoesPlaywright,
    ResultadoComando,
    ResultadoJUnit
} from "./coleta-execucao.js";
import type {HotspotQualidade} from "./coleta-leitores.js";

interface ResultadoResiduos {
    status?: string;
    resumo?: {
        scoreTotal?: number;
        faixa?: string;
    };
    violacoes?: unknown[];
    avisos?: unknown[];
    hotspots?: HotspotQualidade[];
}

interface ResultadoArquitetura {
    resumo?: {
        scoreTotal?: number;
        faixa?: string;
        metricas?: Record<string, unknown>;
    };
    hotspots?: unknown[];
}

interface ResultadoPlaywright extends Record<string, unknown> {
    stats?: Record<string, unknown> & {
        expected?: number;
    };
}

type PerfilQualidadeSgc = "rapido" | "completo" | "backend" | "frontend";
type NomeAdaptadorSgc =
    | "testesBackendUnitarios"
    | "testesBackendIntegracao"
    | "coberturaBackend"
    | "coberturaFrontend"
    | "lintFrontend"
    | "tiposFrontend"
    | "residuosFrontend"
    | "arquiteturaFrontend"
    | "testesIntegracaoPlaywright"
    | "identificadoresTesteFrontend";

interface DependenciasAdaptadoresSgc {
    criarExecucao: (
        codigo: string,
        nome: string,
        categoria: "teste" | "cobertura" | "qualidade",
        comando: string,
        diretorio: string
    ) => ExecucaoQualidade;
    executarComando: (opcoes: OpcoesComando) => Promise<ResultadoComando>;
    executarComandoSgc: (
        contexto: ContextoColeta,
        argumentos: string[],
        incluirBase?: boolean
    ) => Promise<ResultadoComando>;
    consolidarJUnit: (diretorioRelatorio: string, base: string) => Promise<ResultadoJUnit>;
    registrarResultadoExecucao: (execucao: ExecucaoQualidade, resultado: ResultadoComando) => void;
    parseJsonSeguro: <T>(conteudo: string, fallback: T) => T;
    obterOpcoesPlaywright: (diretorioBase: string) => OpcoesPlaywright;
}

const PERFIS_SGC = {
    rapido: ["testesBackendUnitarios", "coberturaBackend", "coberturaFrontend", "lintFrontend", "tiposFrontend", "residuosFrontend", "arquiteturaFrontend", "identificadoresTesteFrontend"],
    completo: ["testesBackendUnitarios", "testesBackendIntegracao", "coberturaBackend", "coberturaFrontend", "lintFrontend", "tiposFrontend", "residuosFrontend", "arquiteturaFrontend", "testesIntegracaoPlaywright", "identificadoresTesteFrontend"],
    backend: ["testesBackendUnitarios", "testesBackendIntegracao", "coberturaBackend"],
    frontend: ["coberturaFrontend", "lintFrontend", "tiposFrontend", "residuosFrontend", "arquiteturaFrontend", "identificadoresTesteFrontend"]
} as const satisfies Record<PerfilQualidadeSgc, readonly NomeAdaptadorSgc[]>;

function criarAdaptadoresSgc(dependencias: DependenciasAdaptadoresSgc): CatalogoAdaptadores {
    const {
        criarExecucao,
        executarComando,
        executarComandoSgc,
        consolidarJUnit,
        registrarResultadoExecucao,
        parseJsonSeguro,
        obterOpcoesPlaywright
    } = dependencias;

    return {
        async testesBackendUnitarios(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("backend-unitario", "Backend unitario", "teste", "./gradlew :backend:unitTest", "backend");
            const saida = await executarComando({
                comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
                args: [":backend:unitTest"],
                cwd: contexto.base
            });
            const relatorio = await consolidarJUnit(path.join(contexto.diretorioBackend, "build", "test-results", "unitTest"), contexto.base);
            execucao.status = saida.codigoSaida === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = relatorio;
            execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
            return execucao;
        },
        async testesBackendIntegracao(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("backend-integracao", "Backend integracao", "teste", "./gradlew :backend:integrationTest", "backend");
            const saida = await executarComando({
                comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
                args: [":backend:integrationTest"],
                cwd: contexto.base
            });
            const relatorio = await consolidarJUnit(path.join(contexto.diretorioBackend, "build", "test-results", "integrationTest"), contexto.base);
            execucao.status = saida.codigoSaida === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = relatorio;
            execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
            return execucao;
        },
        async coberturaBackend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("backend-cobertura", "Backend cobertura", "cobertura", "./gradlew :backend:jacocoTestReport", "backend");
            const saida = await executarComando({
                comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
                args: [":backend:jacocoTestReport"],
                cwd: contexto.base
            });
            const cobertura = await extrairCoberturaJacoco(null, {diretorioBase: contexto.base});
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = cobertura;
            execucao.sumario = `Cobertura: ${cobertura.linhas.percentual}% linhas, ${cobertura.ramificacoes.percentual}% ramificações.`;
            return execucao;
        },
        async coberturaFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("frontend-cobertura", "Frontend cobertura", "cobertura", "npm --prefix frontend run coverage:unit:collect", "frontend");
            const saida = await executarComando({
                comando: "npm",
                args: ["run", "coverage:unit:collect"],
                cwd: contexto.diretorioFrontend
            });
            const cobertura = await extrairCoberturaFrontend(null, {diretorioBase: contexto.base});
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = cobertura;
            execucao.sumario = `Cobertura: ${cobertura.lines.percentual}% linhas.`;
            return execucao;
        },
        async lintFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("frontend-lint", "Frontend lint", "qualidade", "npx eslint .", "frontend");
            const saida = await executarComando({
                comando: "npx",
                args: ["eslint", ".", "--max-warnings", "0"],
                cwd: contexto.diretorioFrontend
            });
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.sumario = saida.codigoSaida === 0 ? "Lint sem problemas." : "Problemas de lint encontrados.";
            return execucao;
        },
        async tiposFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("frontend-typecheck", "Frontend typecheck", "qualidade", "npm --prefix frontend run typecheck", "frontend");
            const saida = await executarComando({
                comando: "npm",
                args: ["run", "typecheck"],
                cwd: contexto.diretorioFrontend
            });
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.sumario = saida.codigoSaida === 0 ? "Typecheck sem erros." : "Erros de tipagem encontrados.";
            return execucao;
        },
        async residuosFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("frontend-residuos", "Residuos do frontend", "qualidade", "npx tsx toolkit/sgc.ts frontend residuos validar --json-resumido --gravar", ".");
            const saida = await executarComandoSgc(contexto, ["frontend", "residuos", "validar", "--json-resumido", "--gravar"]);
            const resultado = parseJsonSeguro<ResultadoResiduos>(saida.saida, {});
            execucao.status = saida.codigoSaida === 0 && resultado.status === "ok" ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = {
                scoreTotal: resultado.resumo?.scoreTotal ?? null,
                faixa: resultado.resumo?.faixa ?? null,
                violacoes: resultado.violacoes ?? [],
                avisos: resultado.avisos ?? [],
                hotspots: resultado.hotspots ?? []
            };
            execucao.sumario = resultado.resumo
                ? `Pontuacao de residuos: ${resultado.resumo.scoreTotal} (${resultado.resumo.faixa}).`
                : "Validacao de residuos executada.";
            return execucao;
        },
        async arquiteturaFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("frontend-arquitetura", "Frontend arquitetura", "qualidade", "npx tsx toolkit/sgc.ts frontend arquitetura auditar --json --gravar", ".");
            const saida = await executarComandoSgc(contexto, ["frontend", "arquitetura", "auditar", "--json", "--gravar"]);
            const resultado = parseJsonSeguro<ResultadoArquitetura>(saida.saida, {});
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = {
                scoreTotal: resultado.resumo?.scoreTotal ?? null,
                faixa: resultado.resumo?.faixa ?? null,
                viewsComVazamentoCache: resultado.resumo?.metricas?.viewsComVazamentoCache ?? null,
                viewsComServiceDireto: resultado.resumo?.metricas?.viewsComServiceDireto ?? null,
                viewsComFanoutAlto: resultado.resumo?.metricas?.viewsComFanoutAlto ?? null,
                acessosDiretosCache: resultado.resumo?.metricas?.acessosDiretosCache ?? null,
                booleanosPosicionais: resultado.resumo?.metricas?.booleanosPosicionais ?? null,
                ocorrenciasForcar: resultado.resumo?.metricas?.ocorrenciasForcar ?? null,
                arquivosComBolsaDependenciasLarga: resultado.resumo?.metricas?.arquivosComBolsaDependenciasLarga ?? null,
                arquivosComSuperficieAmpla: resultado.resumo?.metricas?.arquivosComSuperficieAmpla ?? null,
                arquivosComMisturaCamadas: resultado.resumo?.metricas?.arquivosComMisturaCamadas ?? null,
                hubsCentraisComSinais: resultado.resumo?.metricas?.hubsCentraisComSinais ?? null,
                hotspots: resultado.hotspots ?? [],
            };
            execucao.sumario = resultado.resumo
                ? `Score arquitetural: ${resultado.resumo.scoreTotal} (${resultado.resumo.faixa}).`
                : "Auditoria arquitetural executada.";
            return execucao;
        },
        async identificadoresTesteFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("frontend-identificadores-teste", "Identificadores de teste do frontend", "qualidade", "npx tsx toolkit/sgc.ts frontend identificadores-teste listar-duplicados", ".");
            const saida = await executarComandoSgc(contexto, [
                "frontend",
                "identificadores-teste",
                "listar-duplicados",
                "--base",
                contexto.base,
            ], false);
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.sumario = saida.codigoSaida === 0 ? "Nenhum identificador de teste duplicado." : "Identificadores de teste duplicados encontrados.";
            return execucao;
        },
        async testesIntegracaoPlaywright(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const opcoesPlaywright = obterOpcoesPlaywright(contexto.base);
            const execucao = criarExecucao("e2e-playwright", "E2E Playwright", "teste", opcoesPlaywright.descricao, ".");
            const saida = await executarComando({
                comando: "npx",
                args: opcoesPlaywright.argumentos,
                cwd: contexto.base,
                env: {CI: "1"}
            });
            const resultado = parseJsonSeguro<ResultadoPlaywright>(saida.saida, {});
            const stats = resultado.stats ?? {};
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = stats;
            execucao.sumario = `${stats.expected ?? 0} testes E2E aprovados.`;
            return execucao;
        }
    };
}

export {
    PERFIS_SGC,
    criarAdaptadoresSgc
};
