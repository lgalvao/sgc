// Test constants for CDU tests - repeated strings and selectors

// Common selectors
export const SELECTORS = {
    // Page elements
    TITULO_PROCESSOS: 'titulo-processos',
    TITULO_ALERTAS: 'titulo-alertas',
    TABELA_PROCESSOS: 'tabela-processos',
    TABELA_ALERTAS: 'tabela-alertas',
    COLUNA_DESCRICAO: 'coluna-descricao',
    COLUNA_TIPO: 'coluna-tipo',
    COLUNA_UNIDADES: 'coluna-unidades',
    COLUNA_SITUACAO: 'coluna-situacao',
    BTN_CRIAR_PROCESSO: 'btn-criar-processo',
    SUBPROCESSO_HEADER: 'subprocesso-header',
    PROCESSO_INFO: 'processo-info',
    UNIDADE_CARD: 'unidade-card',
    TREE_TABLE_ROW: 'tree-table-row',
    MAPA_CARD: 'mapa-card',
    COMPETENCIA_BLOCK: 'competencia-block',
    ATIVIDADE_ITEM: 'atividade-item',
    CONHECIMENTO_ITEM: 'conhecimento-item',
    EDITAR_COMPETENCIA: 'editar-competencia',
    EXCLUIR_COMPETENCIA: 'excluir-competencia',
    BADGE_CONHECIMENTOS: 'badge-conhecimentos',
    ATIVIDADE_NAO_ASSOCIADA: 'atividade-nao-associada',
    UNIDADE_INFO: 'unidade-info',
} as const;

// Common text strings
export const TEXTS = {
    // Login page
    SISTEMA_GESTAO_COMPETENCIAS: 'Sistema de Gestão de Competências',
    TITULO_ELEITORAL: 'Título eleitoral',
    SENHA: 'Senha',
    ENTRAR: 'Entrar',

    // Process management
    CRIAR_PROCESSO: 'Criar processo',
    SALVAR: 'Salvar',
    INICIAR_PROCESSO: 'Iniciar processo',
    PROCESSO_INICIADO: 'Processo iniciado',
    PREENCHA_CAMPOS_UNIDADES: 'Preencha todos os campos e selecione ao menos uma unidade.',
    FINALIZAR_PROCESSO: 'Finalizar processo',
    PROCESSO_FINALIZADO: 'Processo finalizado',
    DETALHES_PROCESSO: 'Detalhes do processo',
    DETALHES_SUBPROCESSO: 'Detalhes do subprocesso',

    // Activities and knowledge
    CADASTRO_ATIVIDADES_CONHECIMENTOS: 'Atividades e conhecimentos',
    ADICIONAR_ATIVIDADE: 'Adicionar atividade',
    ADICIONAR_CONHECIMENTO: 'Adicionar conhecimento',
    EDITAR: 'Editar',
    REMOVER: 'Remover',
    CONFIRMAR: 'Confirmar',
    CANCELAR: 'Cancelar',
    DISPONIBILIZAR: 'Disponibilizar',

    // Map management
    MAPA_COMPETENCIAS: 'Mapa de competências',
    CRIAR_COMPETENCIA: 'Criar competência',
    EDICAO_COMPETENCIA: 'Edição de competência',
    DESCRICAO_COMPETENCIA: 'Descrição da competência',
    EDICAO_MAPA: 'Edição de mapa',
    IMPACTOS_MAPA: 'Impactos no mapa',
    HISTORICO_ANALISE: 'Histórico de análise',
    DEVOLVER_AJUSTES: 'Devolver para ajustes',
    REGISTRAR_ACEITE: 'Registrar aceite',
    HOMOLOGAR: 'Homologar',
    ACEITE_REGISTRADO: 'Aceite registrado',
    HOMOLOGACAO_EFETIVADA: 'Homologação efetivada',

    // Validation
    APRESENTAR_SUGESTOES: 'Apresentar sugestões',
    VALIDAR: 'Validar',
    VER_SUGESTOES: 'Ver sugestões',
    MAPA_VALIDADO: 'Mapa validado e submetido para análise à unidade superior',
    MAPA_SUGESTOES: 'Mapa submetido com sugestões para análise da unidade superior',

    // Modal texts
    DISPONIBILIZACAO_CADASTRO: 'Disponibilização do cadastro',
    CONFIRMA_DISPONIBILIZACAO: 'Confirma a finalização e a disponibilização do cadastro?',
    CADASTRO_DISPONIBILIZADO: 'Cadastro de atividades disponibilizado',
    DISPONIBILIZACAO_REVISAO: 'Disponibilização da revisão do cadastro',
    CONFIRMA_DISPONIBILIZACAO_REVISAO: 'Confirma a finalização da revisão e a disponibilização do cadastro?',
    REVISAO_DISPONIBILIZADA: 'Revisão do cadastro de atividades disponibilizada',

    DEVOLUCAO: 'Devolução',
    CONFIRMA_DEVOLUCAO: 'Confirma a devolução do cadastro para ajustes?',
    CONFIRMA_DEVOLUCAO_VALIDACAO: 'Confirma a devolução da validação do mapa para ajustes?',

    ACEITE: 'Aceite',
    CONFIRMA_ACEITE: 'Confirma o aceite do cadastro de atividades?',
    CONFIRMA_ACEITE_REVISAO: 'Confirma o aceite da revisão do cadastro de atividades?',
    CONFIRMA_ACEITE_VALIDACAO: 'Confirma o aceite da validação do mapa de competências?',

    HOMOLOGACAO_CADASTRO: 'Homologação do cadastro de atividades e conhecimentos',
    CONFIRMA_HOMOLOGACAO_CADASTRO: 'Confirma a homologação do cadastro de atividades e conhecimentos?',
    HOMOLOGACAO_MAPA: 'Homologação do mapa de competências',
    CONFIRMA_HOMOLOGACAO_MAPA: 'Confirma a homologação do mapa de competências?',
    REVISAO_SEM_IMPACTOS: 'A revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade',

    IMPACTO_MAPA: 'Impacto no Mapa de Competências',
    NENHUM_IMPACTO: 'Nenhum impacto no mapa da unidade.',
    ATIVIDADES_INSERIDAS: 'Atividades inseridas',
    COMPETENCIAS_IMPACTADAS: 'Competências impactadas',

    VALIDACAO_MAPA: 'Validação do mapa de competências',
    CONFIRMA_VALIDACAO: 'Confirma a validação do mapa de competências?',

    FINALIZACAO_PROCESSO: 'Finalização de processo',
    CONFIRMA_FINALIZACAO: 'Confirma a finalização do processo',
    ACAO_TORNARA_VIGENTES: 'Essa ação tornará vigentes os mapas de competências homologados',
    ERRO_UNIDADES_NAO_HOMOLOGADAS: 'Não é possível encerrar o processo enquanto houver unidades com mapa de competência ainda não homologado',

    // Map visualization
    MAPA_COMPETENCIAS_TECNICAS: 'Mapa de competências técnicas',
} as const;

// Common URLs
export const URLS = {
    PAINEL: '/painel',
    LOGIN: '/login',
    PROCESSO_CADASTRO: '/processo/cadastro',
    PROCESSO_DETAIL: '/processo/',
    SUBPROCESSO: '/processo/',
    VISUALIZACAO_MAPA: '/vis-mapa',
} as const;

// Common form labels
export const LABELS = {
    DESCRICAO: 'Descrição',
    TIPO: 'Tipo',
    DATA_LIMITE: 'Data limite',
    NOVA_ATIVIDADE: 'Nova atividade',
    NOVO_CONHECIMENTO: 'Novo conhecimento',
    EDITAR_ATIVIDADE: 'Editar atividade',
    SUGESTOES: 'sugestões',
    OBSERVACOES: 'observações',
    OBSERVACAO: 'observação',
    TITULO_ELEITORAL: 'Título eleitoral',
    SENHA: 'Senha',
} as const;