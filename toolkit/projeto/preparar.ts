import path from "node:path";
import {execa} from "execa";
import {Listr, type ListrTask} from "listr2";
import {carregarConfiguracao, type EscopoInstalacaoConfigurado} from "../lib/configuracao.js";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {executarVerificacaoAmbiente, type Recurso, type RecursoArquivo, type ResultadoVerificacaoAmbiente} from "./ambiente-verificar.js";

type DefinicaoEscopoInstalacao = EscopoInstalacaoConfigurado;

interface EscopoInstalacao extends DefinicaoEscopoInstalacao {
    caminho: string;
}

interface OpcoesPreparacao {
    base?: string;
    instalarDependencias?: boolean;
    instalarPlaywright?: boolean;
    escoposInstalacao?: readonly DefinicaoEscopoInstalacao[];
    recursosVerificacaoAmbiente?: Recurso[];
    comandosRegistradosVerificacaoAmbiente?: RecursoArquivo[];
}

interface ContextoPreparacao {
    verificacaoAmbiente?: ResultadoVerificacaoAmbiente;
}

interface ResultadoPreparacao {
    diretorioBase: string;
    verificacaoAmbiente: ResultadoVerificacaoAmbiente;
}

const ESCOPOS_INSTALACAO_SGC: readonly DefinicaoEscopoInstalacao[] = [
    {titulo: "Instalar dependencias da raiz", segmento: ""},
    {titulo: "Instalar dependencias do frontend", segmento: "frontend"},
    {titulo: "Instalar dependencias do toolkit", segmento: "toolkit"}
];

function resolverEscoposInstalacao(
    diretorioBase: string,
    definicoes?: readonly DefinicaoEscopoInstalacao[]
): EscopoInstalacao[] {
    const definicoesResolvidas = definicoes
        ?? carregarConfiguracao(diretorioBase).execucoes?.instalacao
        ?? ESCOPOS_INSTALACAO_SGC;
    return definicoesResolvidas.map(definicao => ({
        ...definicao,
        caminho: path.resolve(diretorioBase, definicao.segmento)
    }));
}

async function executarPreparacao(opcoes: OpcoesPreparacao = {}): Promise<ResultadoPreparacao> {
    const diretorioBase = path.resolve(opcoes.base ?? resolverNaRaiz());
    const escoposInstalacao = resolverEscoposInstalacao(diretorioBase, opcoes.escoposInstalacao);
    const tarefas = new Listr<ContextoPreparacao>([
        {
            title: "Validar ambiente do projeto",
            task: async (ctx, task) => {
                const verificacaoAmbiente = await executarVerificacaoAmbiente({
                    base: diretorioBase,
                    silencioso: true,
                    recursos: opcoes.recursosVerificacaoAmbiente,
                    comandosRegistrados: opcoes.comandosRegistradosVerificacaoAmbiente
                });
                ctx.verificacaoAmbiente = verificacaoAmbiente;
                task.output = `status ${verificacaoAmbiente.statusGeral}`;

                if (verificacaoAmbiente.statusGeral === "falha") {
                    throw new Error("Ambiente incompleto. Corrija as falhas da verificacao do ambiente antes de continuar.");
                }
            }
        },
        ...escoposInstalacao.map((escopo): ListrTask<ContextoPreparacao> => ({
            title: escopo.titulo,
            enabled: Boolean(opcoes.instalarDependencias),
            task: async () => {
                await execa("npm", ["install"], {
                    cwd: escopo.caminho,
                    stdio: "inherit",
                    shell: process.platform === "win32"
                });
            }
        })),
        {
            title: "Instalar Playwright Chromium",
            enabled: Boolean(opcoes.instalarPlaywright),
            task: async () => {
                await execa("npx", ["playwright", "install", "chromium"], {
                    cwd: diretorioBase,
                    stdio: "inherit",
                    shell: process.platform === "win32"
                });
            }
        },
    ], {
        concurrent: false,
        rendererOptions: {
            collapseSubtasks: false
        }
    });

    const contexto = await tarefas.run();
    if (!contexto.verificacaoAmbiente) {
        throw new Error("Preparacao terminou sem executar a verificacao do ambiente.");
    }

    return {
        diretorioBase,
        verificacaoAmbiente: contexto.verificacaoAmbiente
    };
}

export {
    executarPreparacao,
    ESCOPOS_INSTALACAO_SGC,
    resolverEscoposInstalacao,
    type DefinicaoEscopoInstalacao,
    type EscopoInstalacao,
    type OpcoesPreparacao,
    type ResultadoPreparacao
};
