import path from "node:path";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {obterDiretorioArtefatos} from "../biblioteca/qualidade.js";

interface ContextoColeta {
    base: string;
    diretorioArtefatos: string;
    diretorioExecucoes: string;
    diretorioMaisRecente: string;
    diretorioServidor: string;
    diretorioCliente: string;
    diretorioCodigoCliente: string;
}

function criarContextoColeta(base: string = DIRETORIO_RAIZ): ContextoColeta {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const diretorioArtefatos = obterDiretorioArtefatos(baseResolvida);

    return {
        base: baseResolvida,
        diretorioArtefatos,
        diretorioExecucoes: path.join(diretorioArtefatos, "execucoes"),
        diretorioMaisRecente: path.join(diretorioArtefatos, "mais-recente"),
        diretorioServidor: resolverCaminhoConfigurado("servidor", baseResolvida),
        diretorioCliente: resolverCaminhoConfigurado("cliente", baseResolvida),
        diretorioCodigoCliente: resolverCaminhoConfigurado("codigoCliente", baseResolvida),
    };
}

export {criarContextoColeta, type ContextoColeta};
