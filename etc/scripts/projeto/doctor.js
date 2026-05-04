import path from "node:path";
import net from "node:net";
import which from "which";
import fs from "fs-extra";
import {execa} from "execa";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha, formatarStatus, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const CATEGORIAS = {
    AMBIENTE: "Ambiente e Ferramentas",
    CONFIGURACAO: "Configuração do Projeto",
    INFRA: "Infraestrutura e Serviços",
    DEPENDENCIAS: "Dependências"
};

const RECURSOS = [
    {tipo: "comando", nome: "node", obrigatorio: true, categoria: CATEGORIAS.AMBIENTE, versaoMin: "22.0.0"},
    {tipo: "comando", nome: "pnpm", obrigatorio: true, categoria: CATEGORIAS.AMBIENTE},
    {tipo: "comando", nome: "git", obrigatorio: true, categoria: CATEGORIAS.AMBIENTE},
    {tipo: "comando", nome: "java", obrigatorio: true, categoria: CATEGORIAS.AMBIENTE, versaoEsperada: "25"},
    {tipo: "comando", nome: "keytool", obrigatorio: false, categoria: CATEGORIAS.AMBIENTE},

    {tipo: "arquivo", nome: "gradlew", caminho: "gradlew", obrigatorio: true, categoria: CATEGORIAS.CONFIGURACAO},
    {
        tipo: "arquivo",
        nome: "package.json raiz",
        caminho: "package.json",
        obrigatorio: true,
        categoria: CATEGORIAS.CONFIGURACAO
    },
    {
        tipo: "arquivo",
        nome: "frontend/package.json",
        caminho: "frontend/package.json",
        obrigatorio: true,
        categoria: CATEGORIAS.CONFIGURACAO
    },
    {
        tipo: "arquivo",
        nome: "backend/build.gradle.kts",
        caminho: "backend/build.gradle.kts",
        obrigatorio: true,
        categoria: CATEGORIAS.CONFIGURACAO
    },
    {tipo: "arquivo", nome: ".env.e2e", caminho: ".env.e2e", obrigatorio: false, categoria: CATEGORIAS.CONFIGURACAO},

    {tipo: "porta", nome: "Backend (10000)", porta: 10000, obrigatorio: false, categoria: CATEGORIAS.INFRA},
    {tipo: "porta", nome: "Frontend (5173)", porta: 5173, portaPadrao: true, categoria: CATEGORIAS.INFRA},
    {tipo: "porta", nome: "QA Dashboard (4179)", porta: 4179, portaPadrao: true, categoria: CATEGORIAS.INFRA},
    {
        tipo: "conectividade",
        nome: "Internet (google.com)",
        host: "google.com",
        obrigatorio: false,
        categoria: CATEGORIAS.INFRA
    },

    {
        tipo: "diretorio",
        nome: "node_modules raiz",
        caminho: "node_modules",
        obrigatorio: false,
        categoria: CATEGORIAS.DEPENDENCIAS
    },
    {
        tipo: "diretorio",
        nome: "frontend/node_modules",
        caminho: "frontend/node_modules",
        obrigatorio: false,
        categoria: CATEGORIAS.DEPENDENCIAS
    }
];

function determinarStatus(sucesso, obrigatorio) {
    if (sucesso) return "ok";
    return obrigatorio ? "falha" : "alerta";
}

async function verificarPorta(porta) {
    return new Promise((resolve) => {
        const server = net.createServer();
        server.once("error", () => resolve(false));
        server.once("listening", () => {
            server.close();
            resolve(true);
        });
        server.listen(porta, "127.0.0.1");
    });
}

async function verificarConectividade(host) {
    return new Promise((resolve) => {
        const socket = net.createConnection(80, host);
        socket.setTimeout(2000);
        socket.on("connect", () => {
            socket.destroy();
            resolve(true);
        });
        socket.on("error", () => resolve(false));
        socket.on("timeout", () => {
            socket.destroy();
            resolve(false);
        });
    });
}

async function obterVersao(comando) {
    try {
        const {stdout} = await execa(comando, ["--version"]);
        return stdout.trim();
    } catch {
        try {
            const {stdout} = await execa(comando, ["-v"]);
            return stdout.trim();
        } catch {
            return null;
        }
    }
}

async function verificarComando(recurso) {
    const encontrado = await which(recurso.nome, {nothrow: true});
    if (!encontrado) {
        return {
            ...recurso,
            status: determinarStatus(false, recurso.obrigatorio),
            detalhe: "nao encontrado"
        };
    }

    let detalhe = encontrado;
    let status = "ok";

    if (recurso.versaoEsperada || recurso.versaoMin) {
        const versao = await obterVersao(recurso.nome);
        if (versao) {
            detalhe += ` (versao ${versao})`;
            if (recurso.versaoEsperada && !versao.includes(recurso.versaoEsperada)) {
                status = "alerta";
                detalhe += ` - esperado ${recurso.versaoEsperada}`;
            }
        }
    }

    return {...recurso, status, detalhe};
}

async function verificarRecurso(recurso, diretorioBase) {
    if (recurso.tipo === "comando") {
        return verificarComando(recurso);
    }

    if (recurso.tipo === "porta") {
        const livre = await verificarPorta(recurso.porta);
        return {
            ...recurso,
            status: determinarStatus(livre, recurso.obrigatorio),
            detalhe: livre ? "livre" : "ocupada"
        };
    }

    if (recurso.tipo === "conectividade") {
        const ok = await verificarConectividade(recurso.host);
        return {
            ...recurso,
            status: determinarStatus(ok, recurso.obrigatorio),
            detalhe: ok ? "conectado" : "indisponivel"
        };
    }

    const caminhoAbsoluto = path.resolve(diretorioBase, recurso.caminho);
    const existe = await fs.pathExists(caminhoAbsoluto);
    return {
        ...recurso,
        status: determinarStatus(existe, recurso.obrigatorio),
        detalhe: existe ? caminhoAbsoluto : `${recurso.caminho} ausente`
    };
}

function calcularStatusGeral(totais) {
    if (totais.falha > 0) return "falha";
    if (totais.alerta > 0) return "alerta";
    return "ok";
}

function consolidar(resultado) {
    const totais = {
        ok: resultado.filter((item) => item.status === "ok").length,
        alerta: resultado.filter((item) => item.status === "alerta").length,
        falha: resultado.filter((item) => item.status === "falha").length
    };

    return {
        statusGeral: calcularStatusGeral(totais),
        totais
    };
}

function imprimirHumano(resultado, consolidado) {
    imprimirCabecalho("Diagnostico do SGC", "Valida comandos, arquivos e infraestrutura do sistema.");
    escreverLinha("");

    const porCategoria = {};
    for (const item of resultado) {
        if (!porCategoria[item.categoria]) porCategoria[item.categoria] = [];
        porCategoria[item.categoria].push(item);
    }

    for (const [categoria, itens] of Object.entries(porCategoria)) {
        escreverLinha(pc.bold(categoria));
        for (const item of itens) {
            escreverLinha(`  - ${item.nome}: ${formatarStatus(item.status)} (${item.detalhe})`);
        }
        escreverLinha("");
    }

    escreverLinha(`Status geral: ${formatarStatus(consolidado.statusGeral)}`);
    escreverLinha(`Totais: ${consolidado.totais.ok} ok, ${consolidado.totais.alerta} alertas, ${consolidado.totais.falha} falhas`);
}

async function executarDoctor(opcoes = {}) {
    const diretorioBase = opcoes.base ? path.resolve(opcoes.base) : resolverNaRaiz();
    const verificacoes = await Promise.all(RECURSOS.map((recurso) => verificarRecurso(recurso, diretorioBase)));
    const consolidado = consolidar(verificacoes);
    const saida = {
        diretorioBase,
        ...consolidado,
        verificacoes
    };

    if (!opcoes.silencioso) {
        if (opcoes.json) {
            imprimirJson(saida);
        } else {
            imprimirHumano(verificacoes, consolidado);
        }
    }

    if (consolidado.statusGeral === "falha") {
        process.exitCode = 1;
    }

    return saida;
}

export {
    executarDoctor
};
