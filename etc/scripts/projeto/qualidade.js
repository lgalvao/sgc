import {execa} from "execa";
import {Listr} from "listr2";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {imprimirCabecalho} from "../lib/saida.js";

const PERFIS = {
    all: {
        descricao: "Executa a verificacao consolidada do projeto inteiro.",
        tarefas: [
            {
                titulo: "Gradle qualityCheckAll",
                comando: "./gradlew",
                args: ["qualityCheckAll"]
            }
        ]
    },
    backend: {
        descricao: "Executa as verificacoes de qualidade do backend.",
        tarefas: [
            {
                titulo: "Gradle backendQualityCheck",
                comando: "./gradlew",
                args: ["backendQualityCheck"]
            }
        ]
    },
    frontend: {
        descricao: "Executa as verificacoes de qualidade do frontend.",
        tarefas: [
            {
                titulo: "Gradle frontendQualityCheck",
                comando: "./gradlew",
                args: ["frontendQualityCheck"]
            }
        ]
    },
    rapido: {
        descricao: "Executa uma verificacao rapida de qualidade.",
        tarefas: [
            {
                titulo: "Gradle qualityCheckFast",
                comando: "./gradlew",
                args: ["qualityCheckFast"]
            }
        ]
    }
};

async function executarPerfilQualidade(perfil) {
    const definicao = PERFIS[perfil];
    if (!definicao) {
        throw new Error(`Perfil de qualidade invalido: ${perfil}`);
    }

    imprimirCabecalho("Qualidade do projeto", definicao.descricao);
    const tarefas = new Listr(
        definicao.tarefas.map((tarefa) => ({
            title: tarefa.titulo,
            task: async () => {
                await execa(tarefa.comando, tarefa.args, {
                    cwd: resolverNaRaiz(),
                    stdio: "inherit",
                    shell: process.platform === "win32"
                });
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
}

export {
    PERFIS,
    executarPerfilQualidade
};
