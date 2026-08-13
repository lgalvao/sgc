import {existsSync, readFileSync} from "node:fs";
import path from "node:path";
import {DIRETORIO_RAIZ, DIRETORIO_TOOLKIT} from "./caminhos.js";

type NomeDiretorioConfigurado =
    | "backend"
    | "frontend"
    | "backendCodigo"
    | "backendTestes"
    | "frontendCodigo"
    | "testesIntegracao"
    | "artefatosQualidade"
    | "coberturaBackend"
    | "coberturaFrontend"
    | "regrasSemgrep"
    | "contratosOpenapi"
    | "orcamentoResiduosFrontend"
    | "excecoesResiduosFrontend";

type DiretoriosConfigurados = Partial<Record<NomeDiretorioConfigurado, string>>;

interface TarefaConfigurada {
    titulo: string;
    comando: string;
    argumentos: string[];
}

interface EscopoComandoConfigurado extends TarefaConfigurada {
    segmento: string;
    codigoNaoZeroIndicaAchados?: boolean;
}

interface PerfilQualidadeConfigurado {
    descricao: string;
    tarefas: TarefaConfigurada[];
}

interface EscopoInstalacaoConfigurado {
    titulo: string;
    segmento: string;
}

interface ExecucoesConfiguradas {
    dependencias?: EscopoComandoConfigurado[];
    qualidade?: Record<string, PerfilQualidadeConfigurado>;
    instalacao?: EscopoInstalacaoConfigurado[];
}

interface ConfiguracaoToolkit {
    versao: 1;
    diretorios: DiretoriosConfigurados;
    execucoes?: ExecucoesConfiguradas;
}

interface ConfiguracaoSobreposta {
    versao: 1;
    diretorios?: DiretoriosConfigurados;
    execucoes?: ExecucoesConfiguradas;
}

const NOME_ARQUIVO_CONFIGURACAO = "configuracao-toolkit.json";
const VERSAO_CONFIGURACAO = 1 as const;
const DIRETORIOS_OPCIONAIS = new Set<NomeDiretorioConfigurado>(["orcamentoResiduosFrontend", "excecoesResiduosFrontend"]);
const DIRETORIOS_FORNECIDOS_PELO_TOOLKIT = new Set<NomeDiretorioConfigurado>(["regrasSemgrep"]);

const CONFIGURACAO_PADRAO: ConfiguracaoToolkit = {
    versao: VERSAO_CONFIGURACAO,
    diretorios: {
        backend: "backend",
        frontend: "frontend",
        backendCodigo: "backend/src/main/java/sgc",
        backendTestes: "backend/src/test/java",
        frontendCodigo: "frontend/src",
        testesIntegracao: "e2e",
        artefatosQualidade: "toolkit/qualidade/artefatos",
        coberturaBackend: "backend/build/reports/jacoco/test/jacocoTestReport.xml",
        coberturaFrontend: "frontend/coverage/coverage-final.json",
        regrasSemgrep: "toolkit/qualidade/politicas/semgrep/sgc-qualidade.yml",
        contratosOpenapi: "toolkit/qualidade/artefatos/openapi"
    }
};

function ehObjeto(valor: unknown): valor is Record<string, unknown> {
    return typeof valor === "object" && valor !== null && !Array.isArray(valor);
}

function validarTexto(valor: unknown, caminho: string): string {
    if (typeof valor !== "string" || valor.trim() === "") {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho} deve ser um texto não vazio.`);
    }
    return valor;
}

function validarArgumentos(valor: unknown, caminho: string): string[] {
    if (!Array.isArray(valor) || valor.some(argumento => typeof argumento !== "string")) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho} deve ser uma lista de textos.`);
    }
    return [...valor];
}

function validarTarefa(
    valor: unknown,
    caminho: string,
    permitirSegmento = false,
    chavesAdicionais: readonly string[] = []
): TarefaConfigurada {
    if (!ehObjeto(valor)) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho} deve ser um objeto.`);
    }
    const chavesPermitidas = new Set([
        "titulo",
        "comando",
        "argumentos",
        ...(permitirSegmento ? ["segmento"] : []),
        ...chavesAdicionais
    ]);
    const chavesDesconhecidas = Object.keys(valor).filter(chave => !chavesPermitidas.has(chave));
    if (chavesDesconhecidas.length > 0) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho} possui chave(s) desconhecida(s): ${chavesDesconhecidas.join(", ")}.`);
    }
    return {
        titulo: validarTexto(valor.titulo, `${caminho}.titulo`),
        comando: validarTexto(valor.comando, `${caminho}.comando`),
        argumentos: validarArgumentos(valor.argumentos, `${caminho}.argumentos`),
    };
}

function validarEscoposComando(valor: unknown, caminho: string): EscopoComandoConfigurado[] {
    if (!Array.isArray(valor)) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho} deve ser uma lista.`);
    }
    return valor.map((item, indice) => {
        const tarefa = validarTarefa(item, `${caminho}[${indice}]`, true, ["codigoNaoZeroIndicaAchados"]);
        if (!ehObjeto(item)) {
            throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho}[${indice}] deve ser um objeto.`);
        }
        const codigoNaoZeroIndicaAchados = item.codigoNaoZeroIndicaAchados;
        if (codigoNaoZeroIndicaAchados !== undefined && typeof codigoNaoZeroIndicaAchados !== "boolean") {
            throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho}[${indice}].codigoNaoZeroIndicaAchados deve ser booleano.`);
        }
        return {
            ...tarefa,
            segmento: validarTexto(item.segmento, `${caminho}[${indice}].segmento`),
            ...(codigoNaoZeroIndicaAchados === undefined ? {} : {codigoNaoZeroIndicaAchados})
        };
    });
}

function validarEscoposInstalacao(valor: unknown, caminho: string): EscopoInstalacaoConfigurado[] {
    if (!Array.isArray(valor)) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho} deve ser uma lista.`);
    }
    return valor.map((item, indice) => {
        if (!ehObjeto(item)) {
            throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho}[${indice}] deve ser um objeto.`);
        }
        const chavesPermitidas = new Set(["titulo", "segmento"]);
        const chavesDesconhecidas = Object.keys(item).filter(chave => !chavesPermitidas.has(chave));
        if (chavesDesconhecidas.length > 0) {
            throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.${caminho}[${indice}] possui chave(s) desconhecida(s): ${chavesDesconhecidas.join(", ")}.`);
        }
        return {
            titulo: validarTexto(item.titulo, `${caminho}[${indice}].titulo`),
            segmento: validarTexto(item.segmento, `${caminho}[${indice}].segmento`),
        };
    });
}

function validarExecucoes(valor: unknown): ExecucoesConfiguradas {
    if (!ehObjeto(valor)) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.execucoes deve ser um objeto JSON.`);
    }
    const chavesPermitidas = new Set(["dependencias", "qualidade", "instalacao"]);
    const chavesDesconhecidas = Object.keys(valor).filter(chave => !chavesPermitidas.has(chave));
    if (chavesDesconhecidas.length > 0) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.execucoes possui chave(s) desconhecida(s): ${chavesDesconhecidas.join(", ")}.`);
    }

    const resultado: ExecucoesConfiguradas = {};
    if (valor.dependencias !== undefined) {
        resultado.dependencias = validarEscoposComando(valor.dependencias, "execucoes.dependencias");
    }
    if (valor.instalacao !== undefined) {
        resultado.instalacao = validarEscoposInstalacao(valor.instalacao, "execucoes.instalacao");
    }
    if (valor.qualidade !== undefined) {
        if (!ehObjeto(valor.qualidade)) {
            throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.execucoes.qualidade deve ser um objeto.`);
        }
        const qualidade: Record<string, PerfilQualidadeConfigurado> = Object.create(null) as Record<string, PerfilQualidadeConfigurado>;
        for (const [nome, definicao] of Object.entries(valor.qualidade)) {
            if (nome.trim() === "") {
                throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.execucoes.qualidade deve usar nomes de perfil não vazios.`);
            }
            if (!ehObjeto(definicao)) {
                throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.execucoes.qualidade.${nome} deve ser um objeto.`);
            }
            const chavesPermitidasPerfil = new Set(["descricao", "tarefas"]);
            const chavesDesconhecidasPerfil = Object.keys(definicao).filter(chave => !chavesPermitidasPerfil.has(chave));
            if (chavesDesconhecidasPerfil.length > 0) {
                throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.execucoes.qualidade.${nome} possui chave(s) desconhecida(s): ${chavesDesconhecidasPerfil.join(", ")}.`);
            }
            if (!Array.isArray(definicao.tarefas)) {
                throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.execucoes.qualidade.${nome}.tarefas deve ser uma lista.`);
            }
            qualidade[nome] = {
                descricao: validarTexto(definicao.descricao, `execucoes.qualidade.${nome}.descricao`),
                tarefas: definicao.tarefas.map((tarefa, indice) => validarTarefa(tarefa, `execucoes.qualidade.${nome}.tarefas[${indice}]`)),
            };
        }
        resultado.qualidade = qualidade;
    }

    return resultado;
}

function validarConfiguracao(valor: unknown): ConfiguracaoSobreposta {
    if (!ehObjeto(valor)) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO} deve conter um objeto JSON.`);
    }

    const chavesPermitidas = new Set(["versao", "diretorios", "execucoes"]);
    const chavesDesconhecidas = Object.keys(valor).filter(chave => !chavesPermitidas.has(chave));
    if (chavesDesconhecidas.length > 0) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO} possui chave(s) desconhecida(s): ${chavesDesconhecidas.join(", ")}.`);
    }

    const versao = valor.versao;
    if (versao !== VERSAO_CONFIGURACAO) {
        if (versao === undefined) {
            throw new Error(`${NOME_ARQUIVO_CONFIGURACAO} deve informar a versão ${VERSAO_CONFIGURACAO}.`);
        }
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO} possui versão ${String(versao)}; a versão suportada é ${VERSAO_CONFIGURACAO}.`);
    }

    const execucoes = valor.execucoes === undefined ? undefined : validarExecucoes(valor.execucoes);
    const diretorios = valor.diretorios;
    if (diretorios === undefined) {
        return {
            versao,
            ...(execucoes === undefined ? {} : {execucoes})
        };
    }
    if (!ehObjeto(diretorios)) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.diretorios deve ser um objeto JSON.`);
    }

    const nomesPermitidos = new Set<NomeDiretorioConfigurado>([
        ...(Object.keys(CONFIGURACAO_PADRAO.diretorios) as NomeDiretorioConfigurado[]),
        ...DIRETORIOS_OPCIONAIS,
    ]);
    const nomesDesconhecidos = Object.keys(diretorios).filter(nome => !nomesPermitidos.has(nome as NomeDiretorioConfigurado));
    if (nomesDesconhecidos.length > 0) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.diretorios possui nome(s) desconhecido(s): ${nomesDesconhecidos.join(", ")}.`);
    }

    const diretoriosValidados: DiretoriosConfigurados = {};
    for (const [nome, caminho] of Object.entries(diretorios)) {
        if (typeof caminho !== "string" || caminho.trim() === "") {
            throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.diretorios.${nome} deve ser um caminho textual não vazio.`);
        }
        diretoriosValidados[nome as NomeDiretorioConfigurado] = caminho;
    }

    return {
        versao: VERSAO_CONFIGURACAO,
        diretorios: diretoriosValidados,
        ...(execucoes === undefined ? {} : {execucoes})
    };
}

function combinarConfiguracoes(
    configuracaoBase: ConfiguracaoToolkit,
    configuracaoSobreposta: ConfiguracaoSobreposta
): ConfiguracaoToolkit {
    const execucoesBase = configuracaoBase.execucoes ?? {};
    const execucoesSobrepostas = configuracaoSobreposta.execucoes ?? {};
    const execucoes = {
        ...execucoesBase,
        ...execucoesSobrepostas,
        qualidade: {
            ...execucoesBase.qualidade,
            ...execucoesSobrepostas.qualidade,
        },
    };
    return {
        ...configuracaoBase,
        ...configuracaoSobreposta,
        diretorios: {
            ...configuracaoBase.diretorios,
            ...configuracaoSobreposta.diretorios ?? {}
        },
        ...(Object.keys(execucoes).some(chave => chave !== "qualidade") || Object.keys(execucoes.qualidade ?? {}).length > 0
            ? {execucoes}
            : {})
    };
}

function carregarConfiguracao(diretorioBase = DIRETORIO_RAIZ): ConfiguracaoToolkit {
    const caminho = path.join(diretorioBase, NOME_ARQUIVO_CONFIGURACAO);
    if (!existsSync(caminho)) {
        return CONFIGURACAO_PADRAO;
    }

    let configuracaoSobreposta: ConfiguracaoSobreposta;
    try {
        configuracaoSobreposta = validarConfiguracao(JSON.parse(readFileSync(caminho, "utf8")));
    } catch (erro: unknown) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        throw new Error(`Nao foi possivel validar ${NOME_ARQUIVO_CONFIGURACAO}: ${mensagem}`, {cause: erro});
    }

    return combinarConfiguracoes(CONFIGURACAO_PADRAO, configuracaoSobreposta);
}

function resolverCaminhoConfigurado(nomeDiretorio: NomeDiretorioConfigurado, diretorioBase = DIRETORIO_RAIZ): string {
    const caminho = tentarResolverCaminhoConfigurado(nomeDiretorio, diretorioBase);
    if (!caminho) {
        throw new Error(`Diretorio configurado desconhecido ou ausente: ${nomeDiretorio}`);
    }
    return caminho;
}

function tentarResolverCaminhoConfigurado(nomeDiretorio: NomeDiretorioConfigurado, diretorioBase = DIRETORIO_RAIZ): string | undefined {
    const configuracao = carregarConfiguracao(diretorioBase);
    const caminhoRelativo = configuracao.diretorios[nomeDiretorio];
    if (!caminhoRelativo) {
        return undefined;
    }

    const caminhoPadrao = CONFIGURACAO_PADRAO.diretorios[nomeDiretorio];
    if (DIRETORIOS_FORNECIDOS_PELO_TOOLKIT.has(nomeDiretorio) && caminhoRelativo === caminhoPadrao) {
        const caminhoRelativoNoToolkit = caminhoRelativo.startsWith("toolkit/")
            ? caminhoRelativo.slice("toolkit/".length)
            : caminhoRelativo;
        const caminhoDoToolkit = path.resolve(DIRETORIO_TOOLKIT, caminhoRelativoNoToolkit);
        if (existsSync(caminhoDoToolkit)) {
            return caminhoDoToolkit;
        }
    }

    return path.resolve(diretorioBase, caminhoRelativo);
}

export {
    NOME_ARQUIVO_CONFIGURACAO,
    VERSAO_CONFIGURACAO,
    carregarConfiguracao,
    resolverCaminhoConfigurado,
    tentarResolverCaminhoConfigurado,
    validarConfiguracao,
    type NomeDiretorioConfigurado,
    type EscopoComandoConfigurado,
    type EscopoInstalacaoConfigurado,
    type PerfilQualidadeConfigurado,
    type TarefaConfigurada
};
