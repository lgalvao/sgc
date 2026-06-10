export const TEXTOS_DIAGNOSTICO = {
    // « Títulos de página »
    TITULO_AUTOAVALIACAO: 'Autoavaliação de Competências',
    TITULO_CONSENSO: 'Avaliação de Consenso',
    TITULO_SITUACAO_CAPACITACAO: 'Situação de Capacitação',
    TITULO_MONITORAMENTO: 'Monitoramento do Diagnóstico',
    TITULO_UNIDADE: 'Análise do Diagnóstico da Unidade',

    // « Cabeçalhos de tabela »
    COLUNA_COMPETENCIA: 'Competência',
    COLUNA_IMPORTANCIA: 'Importância',
    COLUNA_DOMINIO: 'Domínio',
    COLUNA_CHEFE: 'Chefe',
    COLUNA_CONSENSO: 'Consenso',
    COLUNA_GAP: 'Gap',
    COLUNA_SERVIDOR: 'Servidor',
    COLUNA_SITUACAO: 'Situação',
    COLUNA_CAPACITACAO: 'Situação de Capacitação',
    COLUNA_UNIDADE: 'Unidade',
    COLUNA_LOCALIZACAO: 'Localização atual',
    COLUNA_DATA_LIMITE: 'Data limite',
    COLUNA_ACOES: 'Ações',

    // « Situações de avaliação do servidor »
    SITUACAO_NAO_REALIZADA: 'Autoavaliação não iniciada',
    SITUACAO_AUTOAVALIACAO_CONCLUIDA: 'Autoavaliação concluída',
    SITUACAO_CONSENSO_CRIADO: 'Avaliação de consenso criada',
    SITUACAO_CONSENSO_APROVADO: 'Avaliação de consenso aprovada',
    SITUACAO_IMPOSSIBILITADA: 'Avaliação impossibilitada',

    // « Situações de capacitação »
    CAPACITACAO_NA: 'Não se aplica',
    CAPACITACAO_AC: 'A capacitar',
    CAPACITACAO_EC: 'Em capacitação',
    CAPACITACAO_C: 'Capacitado',
    CAPACITACAO_I: 'Instrutor',

    // « Botões de ação »
    BTN_SALVAR: 'Salvar',
    BTN_CONCLUIR_AUTOAVALIACAO: 'Concluir autoavaliação',
    BTN_APROVAR_CONSENSO: 'Aprovar consenso',
    BTN_MANTER_CONSENSO: 'Manter avaliação de consenso',
    BTN_CONCLUIR_DIAGNOSTICO: 'Concluir diagnóstico',
    BTN_VALIDAR: 'Validar',
    BTN_DEVOLVER: 'Devolver',
    BTN_HOMOLOGAR: 'Homologar',
    BTN_IMPOSSIBILITAR: 'Indicar impossibilidade',
    BTN_VER_DETALHES: 'Ver detalhes',
    BTN_VOLTAR: 'Voltar',

    // « Mensagens de sucesso »
    SUCESSO_AUTOAVALIACAO_SALVA: 'Autoavaliação salva',
    SUCESSO_AUTOAVALIACAO_CONCLUIDA: 'Autoavaliação concluída',
    SUCESSO_CONSENSO_SALVO: 'Avaliação de consenso salva automaticamente',
    SUCESSO_CONSENSO_APROVADO: 'Avaliação de consenso aprovada',
    SUCESSO_CAPACITACAO_SALVA: 'Situação de capacitação salva',
    SUCESSO_DIAGNOSTICO_CONCLUIDO: 'Diagnóstico da unidade concluído',
    SUCESSO_DIAGNOSTICO_VALIDADO: 'Diagnóstico validado',
    SUCESSO_DIAGNOSTICO_DEVOLVIDO: 'Diagnóstico devolvido para ajustes',
    SUCESSO_DIAGNOSTICO_HOMOLOGADO: 'Diagnóstico homologado',
    SUCESSO_IMPOSSIBILITADO: 'Impossibilidade registrada',

    // « Modais de confirmação »
    MODAL_CONCLUIR_TITULO: 'Concluir autoavaliação',
    MODAL_CONCLUIR_MENSAGEM: 'Confirma a conclusão da autoavaliação?',
    MODAL_CONCLUIR_DIAG_TITULO: 'Concluir diagnóstico da unidade',
    MODAL_CONCLUIR_DIAG_MENSAGEM:
        'Confirma a conclusão do diagnóstico desta unidade? Todos os servidores devem ter consenso aprovado ou avaliação impossibilitada.',
    MODAL_APROVAR_TITULO: 'Aprovar avaliação de consenso',
    MODAL_APROVAR_MENSAGEM: 'Confirma a aprovação da avaliação de consenso definida pelo responsavel da unidade?',
    MODAL_VALIDAR_TITULO: 'Validar diagnóstico',
    MODAL_DEVOLVER_TITULO: 'Devolver diagnóstico',
    MODAL_DEVOLVER_PLACEHOLDER: 'Informe a justificativa para devolução...',
    MODAL_HOMOLOGAR_TITULO: 'Homologar diagnóstico',
    MODAL_IMPOSSIBILITAR_TITULO: 'Indicar impossibilidade de avaliação',
    MODAL_IMPOSSIBILITAR_MENSAGEM: (nomeServidor: string) =>
        `Confirma a impossibilidade de avaliação para ${nomeServidor}?`,
    MODAL_IMPOSSIBILITAR_PLACEHOLDER: 'Justificativa obrigatória...',

    // « Labels de campos »
    LABEL_UNIDADE: 'Unidade',
    LABEL_SITUACAO_SUBPROCESSO: 'Situação',
    LABEL_OBSERVACOES: 'Observações (opcional)',
    LABEL_JUSTIFICATIVA: 'Justificativa',
    LABEL_SALVANDO: 'Salvando...',

    // « Estados vazios »
    VAZIO_EQUIPE_TITULO: 'Sem servidores',
    VAZIO_EQUIPE_TEXTO: 'Nenhum servidor encontrado para este diagnóstico.',
    VAZIO_CAPACITACAO_TITULO: 'Sem situações de capacitação',
    VAZIO_CAPACITACAO_TEXTO: 'Nenhuma situação de capacitação registrada para esta unidade.',
    VAZIO_UNIDADES_TITULO: 'Sem unidades',
    VAZIO_UNIDADES_TEXTO: 'Nenhuma unidade participante encontrada.',

    // « Escala de notas »
    ESCALA_HINT: 'Escala: NA, 1, 2, 3, 4, 5 e 6',
    NOTA_NA: 'NA',
    NOTA_NAO_INFORMADA: '-',

    // « Erros »
    ERRO_CARREGAR_CONTEXTO: 'Não foi possível carregar as informações do diagnóstico.',
    ERRO_SALVAR: 'Não foi possível salvar. Tente novamente.',
    ERRO_JUSTIFICATIVA_OBRIGATORIA: 'A justificativa é obrigatória.',
} as const;
