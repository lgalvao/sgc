import path from "node:path";
import process from "node:process";
import {extrairCoberturaJacoco} from "../biblioteca/dominios/cobertura-java.js";
import {extrairCoberturaCliente} from "../biblioteca/dominios/cobertura-web.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import type {
    CatalogoAdaptadores,
    ContextoColeta,
    ExecucaoQualidade,
    OpcoesComando,
    OpcoesPlaywright,
    ResultadoComando,
    ResultadoJUnit
} from "./coleta-motor.js";
import type {PontoCriticoQualidade} from "./coleta-leitores.js";
import {resolverExecutavelLocal} from "./coleta-executor.js";

interface ResultadoResiduos {
    status?: string;
    resumo?: {
        pontuacaoTotal?: number;
        classificacao?: string;
    };
    violacoes?: unknown[];
    avisos?: unknown[];
    pontosCriticos?: PontoCriticoQualidade[];
}

interface ResultadoArquitetura {
    resumo?: {
        pontuacaoTotal?: number;
        classificacao?: string;
        metricas?: Record<string, unknown>;
    };
    pontosCriticos?: PontoCriticoQualidade[];
}

interface ResultadoPlaywright extends Record<string, unknown> {
    stats?: Record<string, unknown> & {
        expected?: number;
    };
}

type PerfilQualidadeSgc = "rapido" | "completo" | "servidor" | "cliente";
type NomeAdaptadorSgc =
    | "testesServidorUnitarios"
    | "testesServidorIntegracao"
    | "coberturaServidor"
    | "coberturaCliente"
    | "lintCliente"
    | "tiposCliente"
    | "residuosCliente"
    | "arquiteturaCliente"
    | "testesIntegracaoPlaywright"
    | "identificadoresTesteCliente";

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
    rapido: ["testesServidorUnitarios", "coberturaServidor", "coberturaCliente", "lintCliente", "tiposCliente", "residuosCliente", "arquiteturaCliente", "identificadoresTesteCliente"],
    completo: ["testesServidorUnitarios", "testesServidorIntegracao", "coberturaServidor", "coberturaCliente", "lintCliente", "tiposCliente", "residuosCliente", "arquiteturaCliente", "testesIntegracaoPlaywright", "identificadoresTesteCliente"],
    servidor: ["testesServidorUnitarios", "testesServidorIntegracao", "coberturaServidor"],
    cliente: ["coberturaCliente", "lintCliente", "tiposCliente", "residuosCliente", "arquiteturaCliente", "identificadoresTesteCliente"]
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
        async testesServidorUnitarios(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("servidor-unitario", "Teste unitario do servidor", "teste", "./gradlew :backend:unitTest", "backend");
            const saida = await executarComando({
                comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
                args: [":backend:unitTest"],
                cwd: contexto.base
            });
            const relatorio = await consolidarJUnit(path.join(contexto.diretorioServidor, "build", "test-results", "unitTest"), contexto.base);
            execucao.status = saida.codigoSaida === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = relatorio;
            execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
            return execucao;
        },
        async testesServidorIntegracao(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("servidor-integracao", "Teste de integracao do servidor", "teste", "./gradlew :backend:integrationTest", "backend");
            const saida = await executarComando({
                comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
                args: [":backend:integrationTest"],
                cwd: contexto.base
            });
            const relatorio = await consolidarJUnit(path.join(contexto.diretorioServidor, "build", "test-results", "integrationTest"), contexto.base);
            execucao.status = saida.codigoSaida === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = relatorio;
            execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
            return execucao;
        },
        async coberturaServidor(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("servidor-cobertura", "Cobertura do servidor", "cobertura", "./gradlew :backend:jacocoTestReport", "backend");
            const saida = await executarComando({
                comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
                args: [":backend:jacocoTestReport"],
                cwd: contexto.base
            });
            const cobertura = await extrairCoberturaJacoco(
                resolverCaminhoConfigurado("coberturaServidor", contexto.base),
                {diretorioBase: contexto.base}
            );
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = cobertura;
            execucao.sumario = `Cobertura: ${cobertura.linhas.percentual}% linhas, ${cobertura.ramificacoes.percentual}% ramificações.`;
            return execucao;
        },
        async coberturaCliente(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("cliente-cobertura", "Cobertura do cliente", "cobertura", "npm --prefix frontend run coverage:unit:collect", "frontend");
            const saida = await executarComando({
                comando: "npm",
                args: ["run", "coverage:unit:collect"],
                cwd: contexto.diretorioCliente
            });
            const cobertura = await extrairCoberturaCliente(
                resolverCaminhoConfigurado("coberturaCliente", contexto.base),
                {diretorioBase: contexto.base}
            );
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = cobertura;
            execucao.sumario = `Cobertura: ${cobertura.linhas.percentual}% linhas.`;
            return execucao;
        },
        async lintCliente(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("cliente-lint", "Lint do cliente", "qualidade", "eslint . --max-warnings 0", "frontend");
            const saida = await executarComando({
                comando: resolverExecutavelLocal("eslint", contexto.diretorioCliente),
                args: [".", "--max-warnings", "0"],
                cwd: contexto.diretorioCliente
            });
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.sumario = saida.codigoSaida === 0 ? "Lint sem problemas." : "Problemas de lint encontrados.";
            return execucao;
        },
        async tiposCliente(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("cliente-typecheck", "Verificacao de tipos do cliente", "qualidade", "npm --prefix frontend run typecheck", "frontend");
            const saida = await executarComando({
                comando: "npm",
                args: ["run", "typecheck"],
                cwd: contexto.diretorioCliente
            });
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.sumario = saida.codigoSaida === 0 ? "Typecheck sem erros." : "Erros de tipagem encontrados.";
            return execucao;
        },
        async residuosCliente(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("cliente-residuos", "Inventario de residuos do cliente", "qualidade", "ferramentas cliente residuos auditar --json-resumido --gravar", ".");
            const saida = await executarComandoSgc(contexto, ["cliente", "residuos", "auditar", "--json-resumido", "--gravar"]);
            const resultado = parseJsonSeguro<ResultadoResiduos>(saida.saida, {});
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = {
                pontuacaoTotal: resultado.resumo?.pontuacaoTotal ?? null,
                classificacao: resultado.resumo?.classificacao ?? null,
                violacoes: resultado.violacoes ?? [],
                avisos: resultado.avisos ?? [],
                pontosCriticos: resultado.pontosCriticos ?? []
            };
            execucao.sumario = resultado.resumo
                ? `Pontuacao de ordenacao de residuos: ${resultado.resumo.pontuacaoTotal}.`
                : "Inventario de residuos executado.";
            return execucao;
        },
        async arquiteturaCliente(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("cliente-arquitetura", "Arquitetura do cliente", "qualidade", "ferramentas cliente arquitetura auditar --json --gravar", ".");
            const saida = await executarComandoSgc(contexto, ["cliente", "arquitetura", "auditar", "--json", "--gravar"]);
            const resultado = parseJsonSeguro<ResultadoArquitetura>(saida.saida, {});
            execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
            registrarResultadoExecucao(execucao, saida);
            execucao.metricas = {
                pontuacaoTotal: resultado.resumo?.pontuacaoTotal ?? null,
                classificacao: resultado.resumo?.classificacao ?? null,
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
                pontosCriticos: resultado.pontosCriticos ?? [],
            };
            execucao.sumario = resultado.resumo
                ? `Pontuacao de ordenacao arquitetural: ${resultado.resumo.pontuacaoTotal}.`
                : "Auditoria arquitetural executada.";
            return execucao;
        },
        async identificadoresTesteCliente(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
            const execucao = criarExecucao("cliente-identificadores-teste", "Identificadores de teste do cliente", "qualidade", "ferramentas cliente identificadores-teste listar-duplicados", ".");
            const saida = await executarComandoSgc(contexto, [
                "cliente",
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
                comando: resolverExecutavelLocal(opcoesPlaywright.comando, contexto.base),
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
