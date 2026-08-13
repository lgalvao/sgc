import path from "node:path";
import {execa} from "execa";
import {Listr} from "listr2";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {imprimirCabecalho} from "../lib/saida.js";

interface TarefaQualidade {
    titulo: string;
    comando: string;
    argumentos: string[];
}

interface DefinicaoPerfilQualidade {
    descricao: string;
    tarefas: readonly TarefaQualidade[];
}

type CatalogoPerfisQualidade = Record<string, DefinicaoPerfilQualidade>;

interface OpcoesExecucaoPerfil {
    base?: string;
    perfis?: CatalogoPerfisQualidade;
    executarComando?: ExecutarComando;
}

interface ResultadoPerfilQualidade {
    diretorioBase: string;
    perfil: string;
    tarefas: string[];
}

type ExecutarComando = (comando: string, argumentos: readonly string[], diretorioBase: string) => Promise<void>;

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
    backend: {
        descricao: "Executa as verificacoes de qualidade do backend.",
        tarefas: [
            {
                titulo: "Gradle backendQualityCheck",
                comando: "./gradlew",
                argumentos: ["backendQualityCheck"]
            }
        ]
    },
    frontend: {
        descricao: "Executa as verificacoes de qualidade do frontend.",
        tarefas: [
            {
                titulo: "Gradle frontendQualityCheck",
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
    await execa(comando, argumentos, {
        cwd: diretorioBase,
        stdio: "inherit",
        shell: process.platform === "win32"
    });
};

function resolverPerfilQualidade(
    perfil: string,
    perfis: CatalogoPerfisQualidade = PERFIS_QUALIDADE_SGC
): DefinicaoPerfilQualidade {
    const definicao = perfis[perfil];
    if (!definicao) {
        throw new Error(`Perfil de qualidade invalido: ${perfil}`);
    }

    return definicao;
}

async function executarPerfilQualidade(
    perfil: string,
    opcoes: OpcoesExecucaoPerfil = {}
): Promise<ResultadoPerfilQualidade> {
    const diretorioBase = path.resolve(opcoes.base ?? resolverNaRaiz());
    const definicao = resolverPerfilQualidade(perfil, opcoes.perfis);
    const executarComando = opcoes.executarComando ?? executarComandoPadrao;

    imprimirCabecalho("Qualidade do projeto", definicao.descricao);
    const tarefas = new Listr(
        definicao.tarefas.map((tarefa) => ({
            title: tarefa.titulo,
            task: async () => {
                await executarComando(tarefa.comando, tarefa.argumentos, diretorioBase);
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
    executarPerfilQualidade,
    resolverPerfilQualidade,
    type CatalogoPerfisQualidade,
    type DefinicaoPerfilQualidade,
    type ExecutarComando,
    type OpcoesExecucaoPerfil,
    type ResultadoPerfilQualidade,
    type TarefaQualidade
};
