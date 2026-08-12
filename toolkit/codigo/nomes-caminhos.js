import path from "node:path";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";

function obterDiretorioSaidaNomenclatura(diretorioBase) {
    return path.join(resolverCaminhoConfigurado("artefatosQualidade", diretorioBase), "nomenclatura", "mais-recente");
}

function obterCaminhoSimbolos(diretorioBase) {
    return path.join(obterDiretorioSaidaNomenclatura(diretorioBase), "simbolos.json");
}

function obterCaminhoConsistencia(diretorioBase) {
    return path.join(obterDiretorioSaidaNomenclatura(diretorioBase), "consistencia.json");
}

function obterCaminhoIdioma(diretorioBase) {
    return path.join(obterDiretorioSaidaNomenclatura(diretorioBase), "idioma.json");
}

export {
    obterCaminhoConsistencia,
    obterCaminhoIdioma,
    obterCaminhoSimbolos
};
