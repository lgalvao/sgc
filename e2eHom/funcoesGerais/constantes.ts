/**
 * Constantes e variáveis padrão reutilizáveis para os testes E2E em homologação.
 * Idioma: Português brasileiro.
 */

// Perfis de acesso padrão do sistema SGC
export const PERFIS = {
    ADMINISTRADOR: 'ADMIN',
    GESTOR: 'GESTOR',
    CHEFE: 'CHEFE',
    SERVIDOR: 'SERVIDOR',
} as const;

// Caminhos comuns de navegação (URLs) do SGC
export const ROTAS = {
    LOGIN: '/login',
    PAINEL: '/painel',
    CADASTRO_PROCESSO: '/processo/cadastro',
    NOTIFICACOES_ADMIN: '/administracao/notificacoes',
    ERRO: '/erro',
} as const;

// Mensagens padrão do sistema utilizadas para validações nos testes
export const MENSAGENS = {
    SUCESSO_CADASTRO: 'Cadastro realizado com sucesso',
    SUCESSO_EXCLUSAO: 'Excluído com sucesso',
    CAMPO_OBRIGATORIO: 'Campo obrigatório',
    ERRO_PERMISSAO: 'Acesso negado ou não autorizado',
} as const;

// Dados fictícios padrão para preenchimento de formulários
export const DADOS_TESTE = {
    CPF_PADRAO: '000.000.000-00',
    CNPJ_PADRAO: '00.000.000/0001-00',
    PROCESSO_TITULO: 'Processo de Teste de Homologação',
    PROCESSO_DESCRICAO: 'Descrição do processo gerada de forma automatizada pelo teste E2E.',
} as const;

// Identificadores de elementos HTML (test-ids)
export const IDENTIFICADORES = {
    // Login
    FORM_LOGIN:               'form-login',
    INPUT_USUARIO:            'inp-login-usuario',
    INPUT_SENHA:              'inp-login-senha',
    BTN_ENTRAR:               'btn-login-entrar',
    SELECT_PERFIL:            'sel-login-perfil',

    // Logout
    BTN_LOGOUT:               'btn-logout',

    // Painel
    PAINEL_CARREGANDO:        'painel-carregando',
    BOTAO_CRIAR_PROCESSO:     'btn-painel-criar-processo',

    // Formulário de processo
    INPUT_DESCRICAO:          'inp-processo-descricao',
    SELECT_TIPO:              'sel-processo-tipo',
    INPUT_DATA_LIMITE:        'inp-processo-data-limite',
    BTN_INICIAR_RODAPE:       'btn-processo-iniciar-rodape',
    BTN_CONFIRMAR_INICIO:     'btn-iniciar-processo-confirmar',

    // Árvore de unidades (prefixos — completar com a sigla: `${PREFIXO_EXPAND_UNIDADE}SEDOC`)
    PREFIXO_EXPAND_UNIDADE:   'btn-arvore-expand-',
    PREFIXO_CHK_UNIDADE:      'chk-arvore-unidade-',

    // Modal de unidades interoperacionais
    MODAL_INTEROPERACIONAL:   '#modal-unidades-com-equipe-propria',
    BTN_CONFIRMAR_MODAL:      'btn-acao-bloco-confirmar',

    // Tela de notificações (admin)
    TABELA_NOTIFICACOES:            'tbl-notificacoes',
    PREFIXO_BTN_PREVIEW_NOTIFICACAO:'btn-preview-',
    MODAL_PREVIEW_EMAIL:            'modal-preview-email',
    IFRAME_PREVIEW_EMAIL:           'iframe-preview-email',
    BTN_FECHAR_PREVIEW_EMAIL:       'btn-fechar-preview-email',
} as const;

// Assuntos e corpos das notificações (e-mails)
export const NOTIFICACOES = {
    // Passo 1: Início de processo (Direto para a unidade)
    INICIO_PROCESSO_DIRETO: {
        ASSUNTO: /In[ií]cio de processo de mapeamento de competências/i,
        OBTER_CORPO: (processo: string, dataLimite: string) => [
            `Comunicamos o início do processo ${processo} para a sua unidade.`,
            'Já é possível realizar o cadastro de atividades e conhecimentos no Sistema de Gestão de Competências (SGC).',
            `O prazo para conclusão desta etapa do processo é ${dataLimite}.`
        ]
    },

    // Passo 1: Início de processo (Para unidades superiores / subordinadas)
    INICIO_PROCESSO_SUBORDINADO: {
        ASSUNTO: /In[ií]cio de processo de mapeamento de competências em unidades subordinadas/i,
        OBTER_CORPO: (processo: string, unidadeSubordinada: string, dataLimite: string) => [
            `Comunicamos o início do processo ${processo} nas unidades ${unidadeSubordinada}.`,
            'Estas unidades já podem iniciar o cadastro de atividades e conhecimentos. À medida que estes cadastros forem sendo disponibilizados, será possível visualizar e realizar a sua validação.',
            `O prazo para conclusão desta etapa do processo é ${dataLimite}.`,
            'Acompanhe o processo no Sistema de Gestão de Competências: https://sgc.tre-pe.jus.br.'
        ]
    },

    // Passo 2: Disponibilização do cadastro (unidade → chefe imediato)
    DISPONIBILIZACAO_CADASTRO: {
        ASSUNTO: /Cadastro de atividades e conhecimentos disponibilizado/i,
        OBTER_CORPO: (unidadeOrigem: string, unidadeDestino: string) => [
            `Prezado(a) responsável pela ${unidadeDestino},`,
            `A unidade ${unidadeOrigem} disponibilizou o cadastro de atividades e conhecimentos`,
            'A análise desse cadastro já pode ser realizada no Sistema de Gestão de Competências'
        ]
    },

    // Passo 3+: Aceite do cadastro (gestor intermediário ou final)
    ACEITE_CADASTRO: {
        ASSUNTO: /Cadastro de atividades e conhecimentos da .* submetido para análise/i,
        OBTER_CORPO: (unidadeOrigem: string, unidadeDestino: string) => [
            `Prezado(a) responsável pela ${unidadeDestino},`,
            `O cadastro de atividades e conhecimentos da ${unidadeOrigem}`,
            'foi submetido para análise por essa unidade'
        ]
    },

    // Passo 6: Disponibilização direta do mapa (Admin para a unidade)
    MAPA_DISPONIBILIZADO_DIRETO: {
        ASSUNTO: /Mapa de competências disponibilizado$/i,
        OBTER_CORPO: (unidadeDestino: string) => [
            `Prezado(a) responsável pela ${unidadeDestino},`,
            'O mapa de competências de sua unidade foi disponibilizado no contexto do processo'
        ]
    },

    // Passo 6: Disponibilização do mapa para superiores
    MAPA_DISPONIBILIZADO_SUPERIOR: {
        ASSUNTO: /Mapa de competências disponibilizado - .*/i,
        OBTER_CORPO: (unidadeOrigem: string, unidadeDestino: string) => [
            `Prezado(a) responsável pela ${unidadeDestino},`,
            `O mapa de competências da ${unidadeOrigem} foi disponibilizado no contexto do processo`
        ]
    },

    // Passo 7: Validação do mapa pelo Chefe (SEDOC para superior COEDE)
    MAPA_VALIDADO: {
        ASSUNTO: /Validação do mapa de competências da .* submetida para análise/i,
        OBTER_CORPO: (unidadeOrigem: string, unidadeDestino: string) => [
            `Prezado(a) responsável pela ${unidadeDestino},`,
            `A unidade ${unidadeOrigem} validou o mapa de competências elaborado no processo`
        ]
    },

    // Passo 8 e 9: Aceite de validação do mapa (COEDE para SGP, SGP para ADMIN)
    MAPA_ACEITO: {
        ASSUNTO: /Validação do mapa de competências da .* submetida para análise/i,
        OBTER_CORPO: (unidadeOrigem: string, unidadeDestino: string) => [
            `Prezado(a) responsável pela ${unidadeDestino},`,
            `A validação do mapa de competências da ${unidadeOrigem} no processo`,
            'foi submetida para análise por essa unidade'
        ]
    }
} as const;


