import {resolverNaRaiz} from "../lib/caminhos.js";

const CAMINHO_OPENAPI_LATEST = resolverNaRaiz("etc/qualidade/openapi/latest/openapi.json");
const CAMINHO_OPENAPI_BASELINE = resolverNaRaiz("etc/qualidade/openapi/baseline/openapi.json");
const CAMINHO_TIPOS_FRONTEND = resolverNaRaiz("frontend/src/generated/sgc-openapi.d.ts");
const URL_OPENAPI_PADRAO = "http://127.0.0.1:10000/api-docs";

export {
    CAMINHO_OPENAPI_BASELINE,
    CAMINHO_OPENAPI_LATEST,
    CAMINHO_TIPOS_FRONTEND,
    URL_OPENAPI_PADRAO
};
