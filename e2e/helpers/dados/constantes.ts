export const SELETORES = {
    BTN_LOGIN: '[data-testid="btn-login"]',
    TABELA_PROCESSOS: '[data-testid="tabela-processos"]',
    TABELA_ALERTAS: '[data-testid="tabela-alertas"]',
    TITULO_PROCESSOS: '[data-testid="titulo-processos"]',
    TITULO_ALERTAS: '[data-testid="titulo-alertas"]',

    BTN_CRIAR_PROCESSO: '[data-testid="btn-criar-processo"]',
    BTN_INICIAR_PROCESSO: '[data-testid="btn-iniciar-processo"]',
    BTN_FINALIZAR_PROCESSO: '[data-testid="btn-finalizar-processo"]',
    INFO_UNIDADE: '[data-testid="info-unidade"]',
    BTN_MODAL_CONFIRMAR: '[data-testid="btn-modal-confirmar"]',
    BTN_MODAL_CANCELAR: '[data-testid="btn-modal-cancelar"]',
    BTN_MODAL_FECHAR: '[data-testid="btn-modal-fechar"]',
    INPUT_DATA_LIMITE: '[data-testid="input-data-limite"]',
    INPUT_OBSERVACOES: '[data-testid="input-observacoes"]',
    DISPONIBILIZAR: '[data-testid="disponibilizar"]',
    BTN_DISPONIBILIZAR_PAGE: '[data-testid="btn-disponibilizar-page"]',
    EDITAR_COMPETENCIA: '[data-testid="editar-competencia"]',
    EXCLUIR_COMPETENCIA: '[data-testid="excluir-competencia"]',
    BTN_EXCLUIR: '[data-testid="btn-excluir"]',
    MODAL_CRIAR_COMPETENCIA: '[data-testid="criar-competencia-modal"]',
    MODAL_DISPONIBILIZAR: '[data-testid="disponibilizar-modal"]',
    MODAL_APRESENTAR_SUGESTOES: '[data-testid="apresentar-sugestoes-modal"]',
    MODAL_VALIDAR: '[data-testid="validar-modal"]',
    NOTIFICACAO_SUCESSO: '[data-testid="notificacao-success"]',
    NOTIFICACAO_ERRO: '[data-testid="notificacao-error"]',
    NOTIFICACAO_EMAIL: '[data-testid="notificacao-email"]',
    CAMPO_DESCRICAO: '[data-testid="input-descricao"]',
    CAMPO_TIPO: '[data-testid="select-tipo"]',
    CAMPO_DATA_LIMITE: '[data-testid="input-dataLimite"]',
    CHECKBOX_STIC: '[data-testid="chk-STIC"]',
    BTN_HISTORICO_ANALISE: '[data-testid="btn-historico-analise"]',
    PROCESSO_INFO: '[data-testid="processo-info"]',
    BTN_IMPACTOS_MAPA: '[data-testid="impactos-mapa-button"]',
    BTN_VALIDAR: '[data-testid="validar-btn"]',
    SUBPROCESSO_HEADER: '[data-testid="subprocesso-header"]',
    BTN_REGISTRAR_ACEITE: '[data-testid="registrar-aceite-btn"]',
    MODAL_VISIVEL: '[data-testid*="-modal"].show',

    CHECKBOX: 'input[type="checkbox"]',
    CHECKBOX_MARCADO: 'input[type="checkbox"]:checked',
    LINHA_TABELA_ARVORE: '.p-treetable-tbody > .p-treetable-row',
    BTN_APRESENTAR_SUGESTOES: '[data-testid="apresentar-sugestoes-btn"]',
    TITULO_MODAL_INICIAR_PROCESSO: '.modal-title',
    LINHA_TABELA: 'table tbody tr',

    // Atividades
    INPUT_NOVA_ATIVIDADE: '[data-testid="input-nova-atividade"]',
    BTN_ADICIONAR_ATIVIDADE: '[data-testid="btn-adicionar-atividade"]',
    ITEM_ATIVIDADE: '[data-testid="item-atividade"]',
    CARD_ATIVIDADE: '[data-testid="card-atividade"]',
    BTN_EDITAR_ATIVIDADE: '[data-testid="btn-editar-atividade"]',
    BTN_REMOVER_ATIVIDADE: '[data-testid="btn-remover-atividade"]',
    INPUT_EDITAR_ATIVIDADE: '[data-testid="input-editar-atividade"]',
    BTN_SALVAR_EDICAO_ATIVIDADE: '[data-testid="btn-salvar-edicao-atividade"]',
    BTN_CANCELAR_EDICAO_ATIVIDADE: '[data-testid="btn-cancelar-edicao-atividade"]',

    // Conhecimentos
    INPUT_NOVO_CONHECIMENTO: '[data-testid="input-novo-conhecimento"]',
    BTN_ADICIONAR_CONHECIMENTO: '[data-testid="btn-adicionar-conhecimento"]',
    ITEM_CONHECIMENTO: '[data-testid="item-conhecimento"]',
    GRUPO_CONHECIMENTO: '[data-testid="grupo-conhecimento"]',
    BTN_EDITAR_CONHECIMENTO: '[data-testid="btn-editar-conhecimento"]',
    BTN_REMOVER_CONHECIMENTO: '[data-testid="btn-remover-conhecimento"]',
};

export const SELETORES_CSS = {};

export const TEXTOS = {
    TITULO_PROCESSOS_LABEL: 'Processos',
    ENTRAR: 'Entrar',
    SALVAR: 'Salvar',
    REMOVER: 'Remover',
    CONFIRMAR: 'Confirmar',
    CANCELAR: 'Cancelar',
    INICIAR_PROCESSO: 'Iniciar processo',
    HISTORICO_ANALISE: 'Histórico de Análise',
    IMPACTO_NO_MAPA: 'Impacto no Mapa',
    IMPORTAR_ATIVIDADES: 'Importar Atividades',
    DISPONIBILIZAR: 'Disponibilizar',
    CADASTRO_ATIVIDADES_CONHECIMENTOS: 'Cadastro de Atividades e Conhecimentos',
    MAPA_COMPETENCIAS_TECNICAS: 'Mapa de Competências Técnicas',
    ERRO_LOGIN_INVALIDO: 'Usuário ou senha inválidos',
    DISPONIBILACAO_TITULO: 'Disponibilização de Cadastro',
    UNIDADES_PARTICIPANTES: 'Unidades Participantes',
    PROCESSO_REMOVIDO_INICIO: 'Processo "',
    PROCESSO_REMOVIDO_FIM: '" removido.',
    FINALIZAR_PROCESSO: 'Finalizar Processo',
    FINALIZACAO_PROCESSO: 'Finalização de Processo',
    CONFIRMA_FINALIZACAO: 'Deseja realmente finalizar este processo?',
    CONFIRMACAO_VIGENCIA_MAPAS: 'Todos os mapas homologados entrarão em vigência e os mapas anteriores serão arquivados.',
    FINALIZACAO_BLOQUEADA: 'A finalização do processo foi bloqueada.',
    FINALIZADO: 'Finalizado',
    PROCESSO_FINALIZADO: 'Processo finalizado!',
    MAPAS_VIGENTES: 'Todos os mapas de competências homologados neste processo entraram em vigência.',
    EMAIL_ENVIADO: 'E-mail enviado para os gestores das unidades com mapas em vigência.',
    CADASTRO_DEVOLVIDO_AJUSTES: 'Cadastro devolvido para ajustes.',
    ACEITE_REGISTRADO: 'Aceite registrado.',
    PROCESSO_INICIADO: 'Processo iniciado!',
    EM_ANDAMENTO: 'Em Andamento',
    CONFIRMACAO_INICIAR_PROCESSO: 'Confirmar Início do Processo',
    SITUACAO_LABEL: 'Situação',
    DEVOLVER_PARA_AJUSTES: 'Devolver para Ajustes',
    REGISTRAR_ACEITE: 'Registrar Aceite',
    HOMOLOGAR: 'Homologar',
    COLUNA_DATA_HORA: 'Data/Hora',
    COLUNA_PROCESSO: 'Processo',
    COLUNA_ORIGEM: 'Origem',
    ANALISE_REGISTRADA_SUCESSO: 'Análise registrada.',
    CADASTRO_HOMOLOGADO_SUCESSO: 'Cadastro homologado.',
    VALIDAR: 'Validar',
    DEVOLVER: 'Devolver',
    ACEITE_REVISAO_TITULO: 'Aceite da Revisão',
};

export const URLS = {
    LOGIN: '/login',
    PAINEL: '/painel',
    PROCESSO_CADASTRO: '/processo/cadastro'
};

export const ROTULOS = {
    TITULO_ELEITORAL: 'Título Eleitoral',
    SENHA: 'Senha'
};

export const DADOS_TESTE = {
    PROCESSOS: {
        MAPEAMENTO_STIC: {
            id: 1
        },
        REVISAO_STIC: {
            id: 2
        }
    },
    UNIDADES: {
        STIC: 'STIC',
        SESEL: 'SESEL'
    }
};

export const USUARIOS = {
    ADMIN: {
        titulo: '6',
        nome: 'Ricardo Alves',
        senha: '123',
        unidade: 'STIC',
    },
    GESTOR: {
        titulo: '8',
        nome: 'Paulo Horta',
        senha: '123',
        unidade: 'SEDESENV',
    },
    CHEFE_SGP: {
        titulo: '2',
        nome: 'Carlos Henrique Lima',
        senha: '123',
        unidade: 'SGP',
    },
    CHEFE_STIC: {
        titulo: '777',
        nome: 'Chefe STIC Teste',
        senha: '123',
        unidade: 'STIC',
    },
    CHEFE_SEDESENV: {
        titulo: '3',
        nome: 'Fernanda Oliveira',
        senha: '123',
        unidade: 'SEDESENV',
    },
    SERVIDOR: {
        titulo: '1',
        nome: 'Ana Paula Souza',
        senha: '123',
        unidade: 'SESEL',
    },
    MULTI_PERFIL: {
        titulo: '999999999999',
        nome: 'Usuario Multi Perfil',
        senha: '123',
        perfis: ['ADMIN - STIC', 'GESTOR - STIC'],
    },
    CHEFE_SEDIA: {
        titulo: '10',
        nome: 'Paula Gonçalves',
        senha: '123',
        unidade: 'SEDIA',
    },
} as const;