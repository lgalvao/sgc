import path from "node:path";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";

const DIRETORIO_OPENAPI = resolverCaminhoConfigurado("contratosOpenapi");
const CAMINHO_OPENAPI_LATEST = path.join(DIRETORIO_OPENAPI, "mais-recente", "openapi.json");
const CAMINHO_OPENAPI_BASELINE = path.join(DIRETORIO_OPENAPI, "referencia", "openapi.json");
const CAMINHO_RELATORIO_OPENAPI = path.join(DIRETORIO_OPENAPI, "mais-recente", "diferencas.md");
const CAMINHO_TIPOS_FRONTEND = resolverCaminhoConfigurado("tiposOpenapiFrontend");
const URL_OPENAPI_PADRAO = "http://127.0.0.1:10000/api-docs";

export {
    CAMINHO_OPENAPI_BASELINE,
    CAMINHO_OPENAPI_LATEST,
    CAMINHO_RELATORIO_OPENAPI,
    CAMINHO_TIPOS_FRONTEND,
    URL_OPENAPI_PADRAO
};
