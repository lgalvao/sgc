import {execa} from "execa";
import {Listr} from "listr2";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {executarDoctor} from "./doctor.js";

async function executarSetup(opcoes = {}) {
    const tarefas = new Listr([
        {
            title: "Validar ambiente do projeto",
            task: async (ctx, task) => {
                const diagnostico = await executarDoctor({silencioso: true});
                ctx.diagnostico = diagnostico;
                task.output = `status ${diagnostico.statusGeral}`;

                if (diagnostico.statusGeral === "falha") {
                    throw new Error("Ambiente incompleto. Corrija as falhas do doctor antes de continuar.");
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
                await execa("npm", ["install", "--prefix", "etc/scripts"], {
                    cwd: resolverNaRaiz(),
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
        {
            title: "Importar certificados Java locais",
            enabled: () => Boolean(opcoes.importarCertificados),
            task: async () => {
                await execa("node", ["etc/scripts/sgc.js", "backend", "java", "instalar-certificados"], {
                    cwd: resolverNaRaiz(),
                    stdio: "inherit",
                    shell: process.platform === "win32"
                });
            }
        }
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
    executarSetup
};
