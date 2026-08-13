import type {PoliticaClassificacaoTestes} from "./biblioteca/testes-analisar-regras.js";

const POLITICA_CLASSIFICACAO_TESTES_SGC: PoliticaClassificacaoTestes = {
    anotacoesContrato: [
        "NotNull",
        "NotBlank",
        "NotEmpty",
        "Size",
        "Pattern",
        "Email",
        "Future",
        "Past",
        "Positive",
        "Negative",
        "SanitizarHtml",
        "JsonProperty",
        "JsonView",
        "JsonFormat",
        "JsonIgnoreProperties"
    ],
    nomesModelosEstruturais: ["Perfil"],
    prefixosModelosEstruturais: ["Tipo", "Situacao"],
    sufixosModelosEstruturais: ["Views", "Id"],
    nomesOutrosEstruturais: ["Sgc", "Mensagens"],
    prefixosOutrosEstruturais: ["Config", "Erro"],
    sufixosOutrosEstruturais: ["Properties", "SecurityConfig"],
    caminhosOutrosEstruturais: ["/config/", "/erros/"]
};

export {POLITICA_CLASSIFICACAO_TESTES_SGC};
