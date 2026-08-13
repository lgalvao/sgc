import path from "node:path";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";

function obterDiretorioSaidaNomenclatura(diretorioBase: string): string {
    return path.join(resolverCaminhoConfigurado("artefatosQualidade", diretorioBase), "nomenclatura", "mais-recente");
}

function obterCaminhoSimbolos(diretorioBase: string): string {
    return path.join(obterDiretorioSaidaNomenclatura(diretorioBase), "simbolos.json");
}

function obterCaminhoConsistencia(diretorioBase: string): string {
    return path.join(obterDiretorioSaidaNomenclatura(diretorioBase), "consistencia.json");
}

function obterCaminhoIdioma(diretorioBase: string): string {
    return path.join(obterDiretorioSaidaNomenclatura(diretorioBase), "idioma.json");
}

export {
    obterCaminhoConsistencia,
    obterCaminhoIdioma,
    obterCaminhoSimbolos
};
