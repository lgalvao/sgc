export function obterIdBotaoAcaoProcesso(codigoAcao: string) {
    switch (codigoAcao) {
        case "aceitar-cadastro":
            return "btn-aceitar-bloco";
        case "aceitar-mapa":
            return "btn-aceitar-mapas-bloco";
        case "homologar-cadastro":
            return "btn-homologar-bloco";
        case "homologar-mapa":
            return "btn-homologar-mapas-bloco";
        case "disponibilizar-mapa":
            return "btn-disponibilizar-bloco";
        default:
            return `btn-${codigoAcao}`;
    }
}

export function obterTestIdBotaoAcaoProcesso(codigoAcao: string) {
    switch (codigoAcao) {
        case "aceitar-cadastro":
            return "btn-processo-aceitar-bloco";
        case "aceitar-mapa":
            return "btn-processo-aceitar-mapas-bloco";
        case "homologar-cadastro":
            return "btn-processo-homologar-bloco";
        case "homologar-mapa":
            return "btn-processo-homologar-mapas-bloco";
        case "disponibilizar-mapa":
            return "btn-processo-disponibilizar-bloco";
        default:
            return `btn-processo-${codigoAcao}`;
    }
}
