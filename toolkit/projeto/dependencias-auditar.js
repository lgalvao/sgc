import {execa} from "execa";
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
        diretorio: resolverNaRaiz("toolkit")
    }
];

async function executarAuditoriaDependencias() {
    imprimirCabecalho(
        "Auditoria de dependencias",
        "Executa o knip nos manifestos da raiz, do frontend e do toolkit."
    );

    const resultados = [];
    for (const escopo of ESCOPOS_AUDITORIA) {
        process.stdout.write(`\n${escopo.titulo}\n`);
        const resultado = await execa("npm", ["run", "deps:audit"], {
            cwd: escopo.diretorio,
            stdio: "inherit",
            shell: process.platform === "win32",
            reject: false
        });
        resultados.push({escopo: escopo.titulo, codigoSaida: resultado.exitCode});
    }

    const falhas = resultados.filter((resultado) => resultado.codigoSaida !== 0);
    if (falhas.length > 0) {
        throw new Error(`${falhas.length} auditoria(s) de dependencias falharam.`);
    }

    return resultados;
}

export {
    executarAuditoriaDependencias
};
