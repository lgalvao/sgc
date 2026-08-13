// Padrões de exclusão de cobertura do perfil SGC.

const PADROES_EXCLUSAO_COBERTURA_SGC: readonly RegExp[] = [
    /MapperImpl$/,
    /\.Sgc$/,
    /(?:^|\.).*Config(?:\..*)?$/,
    /(?:^|\.).*Configuration(?:\..*)?$/,
    /Properties$/,
    /Dto$/,
    /Request$/,
    /Response$/,
    /(?:^|\.)Erro.+$/,
    /Exception$/,
    /Repo$/,
    /\.model\.(Perfil|Usuario|Unidade.+|Administrador|Vinculacao.+|Atribuicao.+|Parametro|Movimentacao|Analise|Alerta.+|Conhecimento|Mapa|Atividade|Competencia.+|Notificacao|Processo)$/,
    /Builder$/,
    /BuilderImpl$/,
    /(?:^|\.).*Status.+$/,
    /(?:^|\.).*Tipo.+$/,
    /(?:^|\.).*Situacao.+$/
];

export {PADROES_EXCLUSAO_COBERTURA_SGC};
