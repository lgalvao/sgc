import path from "node:path";
import which from "which";
import fs from "fs-extra";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha, formatarStatus, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const RECURSOS = [
    {tipo: "comando", nome: "node", obrigatorio: true},
    {tipo: "comando", nome: "npm", obrigatorio: true},
    {tipo: "comando", nome: "git", obrigatorio: true},
    {tipo: "comando", nome: "java", obrigatorio: false},
    {tipo: "comando", nome: "keytool", obrigatorio: false},
    {tipo: "arquivo", nome: "gradlew", caminho: "gradlew", obrigatorio: true},
    {tipo: "arquivo", nome: "gradlew.bat", caminho: "gradlew.bat", obrigatorio: false},
    {tipo: "arquivo", nome: "package.json raiz", caminho: "package.json", obrigatorio: true},
    {tipo: "arquivo", nome: "frontend/package.json", caminho: "frontend/package.json", obrigatorio: true},
    {tipo: "arquivo", nome: "backend/build.gradle.kts", caminho: "backend/build.gradle.kts", obrigatorio: true},
    {tipo: "arquivo", nome: "etc/scripts/package.json", caminho: "etc/scripts/package.json", obrigatorio: true},
    {tipo: "diretorio", nome: "node_modules raiz", caminho: "node_modules", obrigatorio: false},
    {tipo: "diretorio", nome: "frontend/node_modules", caminho: "frontend/node_modules", obrigatorio: false}
];

async function verificarRecurso(recurso, diretorioBase) {
    if (recurso.tipo === "comando") {
        const encontrado = await which(recurso.nome, {nothrow: true});
        return {
            ...recurso,
            status: encontrado ? "ok" : (recurso.obrigatorio ? "falha" : "alerta"),
            detalhe: encontrado ?? "nao encontrado"
        };
    }

    const caminhoAbsoluto = path.resolve(diretorioBase, recurso.caminho);
    const existe = await fs.pathExists(caminhoAbsoluto);
    return {
        ...recurso,
        status: existe ? "ok" : (recurso.obrigatorio ? "falha" : "alerta"),
        detalhe: existe ? caminhoAbsoluto : `${recurso.caminho} ausente`
    };
}

function consolidar(resultado) {
    const totais = {
        ok: resultado.filter((item) => item.status === "ok").length,
        alerta: resultado.filter((item) => item.status === "alerta").length,
        falha: resultado.filter((item) => item.status === "falha").length
    };

    return {
        statusGeral: totais.falha > 0 ? "falha" : (totais.alerta > 0 ? "alerta" : "ok"),
        totais
    };
}

function imprimirHumano(resultado, consolidado) {
    imprimirCabecalho("Diagnostico do toolkit", "Valida comandos e arquivos essenciais do repositório.");
    escreverLinha("");
    for (const item of resultado) {
        escreverLinha(`- ${item.nome}: ${formatarStatus(item.status)} (${item.detalhe})`);
    }
    escreverLinha("");
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
