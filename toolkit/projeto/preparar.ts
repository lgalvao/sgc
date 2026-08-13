import path from "node:path";
import {execa} from "execa";
import {Listr, type ListrTask} from "listr2";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {executarDiagnostico, type Recurso, type RecursoArquivo, type ResultadoDiagnostico} from "./diagnostico.js";

interface DefinicaoEscopoInstalacao {
    titulo: string;
    segmento: string;
}

interface EscopoInstalacao extends DefinicaoEscopoInstalacao {
    caminho: string;
}

interface OpcoesPreparacao {
    base?: string;
    instalarDependencias?: boolean;
    instalarPlaywright?: boolean;
    escoposInstalacao?: readonly DefinicaoEscopoInstalacao[];
    recursosDiagnostico?: Recurso[];
    comandosRegistradosDiagnostico?: RecursoArquivo[];
}

interface ContextoPreparacao {
    diagnostico?: ResultadoDiagnostico;
}

interface ResultadoPreparacao {
    diretorioBase: string;
    diagnostico: ResultadoDiagnostico;
}

const ESCOPOS_INSTALACAO_SGC: readonly DefinicaoEscopoInstalacao[] = [
    {titulo: "Instalar dependencias da raiz", segmento: ""},
    {titulo: "Instalar dependencias do frontend", segmento: "frontend"},
    {titulo: "Instalar dependencias do toolkit", segmento: "toolkit"}
];

function resolverEscoposInstalacao(
    diretorioBase: string,
    definicoes: readonly DefinicaoEscopoInstalacao[] = ESCOPOS_INSTALACAO_SGC
): EscopoInstalacao[] {
    return definicoes.map(definicao => ({
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
                const diagnostico = await executarDiagnostico({
                    base: diretorioBase,
                    silencioso: true,
                    recursos: opcoes.recursosDiagnostico,
                    comandosRegistrados: opcoes.comandosRegistradosDiagnostico
                });
                ctx.diagnostico = diagnostico;
                task.output = `status ${diagnostico.statusGeral}`;

                if (diagnostico.statusGeral === "falha") {
                    throw new Error("Ambiente incompleto. Corrija as falhas do diagnostico antes de continuar.");
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
    if (!contexto.diagnostico) {
        throw new Error("Preparacao terminou sem executar o diagnostico do ambiente.");
    }

    return {
        diretorioBase,
        diagnostico: contexto.diagnostico
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
