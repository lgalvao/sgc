import {execa} from "execa";
import {Listr} from "listr2";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {imprimirCabecalho} from "../lib/saida.js";

const ESCOPOS_AUDITORIA = [
    {
        titulo: "Auditar dependencias da raiz",
        diretorio: resolverNaRaiz()
    },
    {
        titulo: "Auditar dependencias do frontend",
        diretorio: resolverNaRaiz("frontend")
    },
    {
        titulo: "Auditar dependencias do toolkit",
        diretorio: resolverNaRaiz("etc", "scripts")
    }
];

async function executarAuditoriaDependencias() {
    imprimirCabecalho(
        "Auditoria de dependencias",
        "Executa o knip nos manifestos da raiz, do frontend e do toolkit."
    );

    const tarefas = new Listr(
        ESCOPOS_AUDITORIA.map((escopo) => ({
            title: escopo.titulo,
            task: async () => {
                await execa("npm", ["run", "deps:audit"], {
                    cwd: escopo.diretorio,
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
    executarAuditoriaDependencias
};
