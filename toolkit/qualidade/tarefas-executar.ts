import {readFileSync} from "node:fs";
import path from "node:path";
import process from "node:process";
import {execa} from "execa";
import {Listr} from "listr2";
import {carregarConfiguracao, type PerfilQualidadeConfigurado, type TarefaConfigurada} from "../biblioteca/configuracao.js";
import {DIRETORIO_RAIZ, DIRETORIO_TOOLKIT} from "../biblioteca/caminhos.js";
import {imprimirCabecalho} from "../biblioteca/saida.js";

type TarefaQualidade = TarefaConfigurada;

type DefinicaoPerfilQualidade = Omit<PerfilQualidadeConfigurado, "tarefas"> & {tarefas: readonly TarefaQualidade[]};

type CatalogoPerfisQualidade = Record<string, DefinicaoPerfilQualidade>;

interface OpcoesExecucaoTarefas {
    base?: string;
    perfis?: CatalogoPerfisQualidade;
    executarComando?: ExecutarComando;
    verificarInstalacaoToolkit?: VerificarInstalacaoToolkit;
}

interface ResultadoTarefasQualidade {
    diretorioBase: string;
    perfil: string;
    tarefas: string[];
}

type ExecutarComando = (comando: string, argumentos: readonly string[], diretorioBase: string) => Promise<void>;
type VerificarInstalacaoToolkit = () => void | Promise<void>;

function verificarInstalacaoToolkit(): void {
    const pacote = JSON.parse(readFileSync(path.join(DIRETORIO_TOOLKIT, "package.json"), "utf8")) as {
        dependencies?: Record<string, string>;
    };
    const ausentes = Object.keys(pacote.dependencies ?? {}).filter(dependencia => {
        try {
            import.meta.resolve(dependencia);
            return false;
        } catch {
            return true;
        }
    });
    if (ausentes.length > 0) {
        throw new Error(`Instalacao do toolkit invalida; dependencias ausentes: ${ausentes.join(", ")}.`);
    }
}

const PERFIS_QUALIDADE_SGC: CatalogoPerfisQualidade = {
    all: {
        descricao: "Executa a verificacao consolidada do projeto inteiro.",
        tarefas: [
            {
                titulo: "Gradle qualityCheckAll",
                comando: "./gradlew",
                argumentos: ["qualityCheckAll"]
            }
        ]
    },
    servidor: {
        descricao: "Executa as verificacoes de qualidade do servidor.",
        tarefas: [
            {
                titulo: "Gradle verificacao de qualidade do servidor (backendQualityCheck)",
                comando: "./gradlew",
                argumentos: ["backendQualityCheck"]
            }
        ]
    },
    cliente: {
        descricao: "Executa as verificacoes de qualidade do cliente.",
        tarefas: [
            {
                titulo: "Gradle verificacao de qualidade do cliente (frontendQualityCheck)",
                comando: "./gradlew",
                argumentos: ["frontendQualityCheck"]
            }
        ]
    },
    rapido: {
        descricao: "Executa uma verificacao rapida de qualidade.",
        tarefas: [
            {
                titulo: "Gradle qualityCheckFast",
                comando: "./gradlew",
                argumentos: ["qualityCheckFast"]
            }
        ]
    }
};

const executarComandoPadrao: ExecutarComando = async (comando, argumentos, diretorioBase) => {
    const caminhoNode = path.dirname(process.execPath);
    const caminhoPath = [caminhoNode, process.env.PATH].filter(Boolean).join(path.delimiter);
    await execa(comando, argumentos, {
        cwd: diretorioBase,
        env: {...process.env, PATH: caminhoPath},
        stdio: "inherit",
        shell: process.platform === "win32"
    });
};

function resolverPerfilTarefasQualidade(
    perfil: string,
    perfis: CatalogoPerfisQualidade = PERFIS_QUALIDADE_SGC
): DefinicaoPerfilQualidade {
    const definicao = perfis[perfil];
    if (!definicao) {
        throw new Error(`Perfil de qualidade invalido: ${perfil}`);
    }

    return definicao;
}

async function executarTarefasQualidade(
    perfil: string,
    opcoes: OpcoesExecucaoTarefas = {}
): Promise<ResultadoTarefasQualidade> {
    const diretorioBase = path.resolve(opcoes.base ?? DIRETORIO_RAIZ);
    const perfis = opcoes.perfis
        ?? carregarConfiguracao(diretorioBase).execucoes?.qualidade
        ?? PERFIS_QUALIDADE_SGC;
    const definicao = resolverPerfilTarefasQualidade(perfil, perfis);
    const executarComando = opcoes.executarComando ?? executarComandoPadrao;
    const verificarInstalacao = opcoes.verificarInstalacaoToolkit ?? verificarInstalacaoToolkit;

    imprimirCabecalho("Qualidade do projeto", definicao.descricao);
    const tarefas = new Listr(
        definicao.tarefas.map((tarefa) => ({
            title: tarefa.titulo,
            task: async () => {
                await verificarInstalacao();
                await executarComando(tarefa.comando, tarefa.argumentos, diretorioBase);
                await verificarInstalacao();
            }
        })),
        {
            concurrent: false,
            rendererOptions: {
                collapseSubtasks: false
            }
        }
    );

    await tarefas.run();

    return {
        diretorioBase,
        perfil,
        tarefas: definicao.tarefas.map(tarefa => tarefa.titulo)
    };
}

export {
    PERFIS_QUALIDADE_SGC,
    executarTarefasQualidade,
    resolverPerfilTarefasQualidade,
    type CatalogoPerfisQualidade,
    type DefinicaoPerfilQualidade,
    type ExecutarComando,
    type OpcoesExecucaoTarefas,
    type ResultadoTarefasQualidade,
    type TarefaQualidade,
    type VerificarInstalacaoToolkit
};
