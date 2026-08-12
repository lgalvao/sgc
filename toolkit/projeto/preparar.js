import {execa} from "execa";
import {Listr} from "listr2";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {executarDiagnostico} from "./diagnostico.js";

async function executarPreparacao(opcoes = {}) {
    const tarefas = new Listr([
        {
            title: "Validar ambiente do projeto",
            task: async (ctx, task) => {
                const diagnostico = await executarDiagnostico({silencioso: true});
                ctx.diagnostico = diagnostico;
                task.output = `status ${diagnostico.statusGeral}`;

                if (diagnostico.statusGeral === "falha") {
                    throw new Error("Ambiente incompleto. Corrija as falhas do diagnostico antes de continuar.");
                }
            }
        },
        {
            title: "Instalar dependencias da raiz",
            enabled: () => Boolean(opcoes.instalarDependencias),
            task: async () => {
                await execa("npm", ["install"], {
                    cwd: resolverNaRaiz(),
                    stdio: "inherit",
                    shell: process.platform === "win32"
                });
            }
        },
        {
            title: "Instalar dependencias do frontend",
            enabled: () => Boolean(opcoes.instalarDependencias),
            task: async () => {
                await execa("npm", ["install"], {
                    cwd: resolverNaRaiz("frontend"),
                    stdio: "inherit",
                    shell: process.platform === "win32"
                });
            }
        },
        {
            title: "Instalar dependencias do toolkit",
            enabled: () => Boolean(opcoes.instalarDependencias),
            task: async () => {
                await execa("npm", ["install"], {
                    cwd: resolverNaRaiz("toolkit"),
                    stdio: "inherit",
                    shell: process.platform === "win32"
                });
            }
        },
        {
            title: "Instalar Playwright Chromium",
            enabled: () => Boolean(opcoes.instalarPlaywright),
            task: async () => {
                await execa("npx", ["playwright", "install", "chromium"], {
                    cwd: resolverNaRaiz(),
                    stdio: "inherit",
                    shell: process.platform === "win32"
                });
            }
        },
    ], {
        concurrent: false,
        rendererOptions: {
            collapseSubtasks: false,
            showTimer: true
        }
    });

    await tarefas.run();
}

export {
    executarPreparacao
};
