package sgc.comum;

/**
 * Constantes de mensagens de validação e regras de negócio do sistema SGC.
 *
 * Centraliza todos os textos do sistema para facilitar manutenção e consistência.
 * Use {@link String#formatted} para os templates que contêm {@code %s}.
 */
public final class MsgValidacao {

    private MsgValidacao() {}

    // ── Campos genéricos ─────────────────────────────────────────────────────
    public static final String CAMPO_TEXTO_OBRIGATORIO          = "O campo texto é obrigatório";
    public static final String DATA_OBRIGATORIA                 = "A data é obrigatória";

    // ── Chave de parâmetro ───────────────────────────────────────────────────
    public static final String CHAVE_OBRIGATORIA                = "A chave não pode estar vazia";
    public static final String CHAVE_MAX                        = "A chave deve ter no máximo 50 caracteres";
    public static final String VALOR_OBRIGATORIO                = "O valor não pode estar vazio";
    public static final String CODIGO_PARAMETRO_OBRIGATORIO     = "O código do parâmetro é obrigatório";

    // ── Descrição ────────────────────────────────────────────────────────────
    public static final String DESCRICAO_OBRIGATORIA            = "Preencha a descrição";
    public static final String DESCRICAO_MAX                    = "A descrição deve ter no máximo 255 caracteres";
    public static final String DESCRICAO_COMPETENCIA_OBRIGATORIA = "A descrição da competência é obrigatória";
    public static final String DESCRICAO_NAO_PODE_SER_VAZIA     = "Descrição não pode ser vazia";

    // ── Justificativa ────────────────────────────────────────────────────────
    public static final String JUSTIFICATIVA_OBRIGATORIA        = "A justificativa é obrigatória";
    public static final String JUSTIFICATIVA_MAX                = "A justificativa deve ter no máximo 500 caracteres";

    // ── Observações ──────────────────────────────────────────────────────────
    public static final String OBSERVACOES_MAX_500              = "Observações devem ter no máximo 500 caracteres";
    public static final String OBSERVACOES_MAX_1000             = "As observações devem ter no máximo 1000 caracteres";

    // ── Sigla de unidade ─────────────────────────────────────────────────────
    public static final String SIGLA_OBRIGATORIA                = "Sigla é obrigatória";
    public static final String SIGLA_MAX                        = "A sigla deve ter no máximo 20 caracteres";
    public static final String SIGLA_UNIDADE_OBRIGATORIA        = "A sigla da unidade é obrigatória";
    public static final String SIGLA_UNIDADE_MAX                = "Sigla da unidade deve ter no máximo 20 caracteres";

    // ── Processo ─────────────────────────────────────────────────────────────
    public static final String CODIGO_PROCESSO_OBRIGATORIO      = "O código do processo é obrigatório";
    public static final String TIPO_PROCESSO_OBRIGATORIO        = "Tipo do processo é obrigatório";
    public static final String TIPO_PROCESSO_OBRIGATORIO_INICIAR = "O tipo do processo é obrigatório";
    public static final String DATA_LIMITE_OBRIGATORIA          = "Preencha a data limite";
    public static final String DATA_LIMITE_FUTURA               = "A data limite deve ser futura";
    public static final String UNIDADES_PARTICIPANTES_OBRIGATORIO = "Pelo menos uma unidade participante deve ser incluída.";

    // ── Unidade ──────────────────────────────────────────────────────────────
    public static final String CODIGO_UNIDADE_OBRIGATORIO       = "O código da unidade é obrigatório";

    // ── Subprocesso ──────────────────────────────────────────────────────────
    public static final String CODIGO_SUBPROCESSO_ORIGEM_OBRIGATORIO = "O código do subprocesso de origem é obrigatório";
    public static final String DATA_LIMITE_ETAPA1_OBRIGATORIA   = "A data limite da etapa 1 é obrigatória";

    // ── Mapa de competências ─────────────────────────────────────────────────
    public static final String CODIGO_MAPA_OBRIGATORIO          = "Código do mapa é obrigatório";
    public static final String CODIGO_ATIVIDADE_OBRIGATORIO     = "Código da atividade é obrigatório";

    // ── Autenticação e perfil ────────────────────────────────────────────────
    public static final String SENHA_OBRIGATORIA                = "A senha é obrigatória.";
    public static final String SENHA_MAX                        = "A senha deve ter no máximo 64 caracteres.";
    public static final String PERFIL_OBRIGATORIO               = "O perfil é obrigatório.";
    public static final String PERFIL_MAX                       = "O perfil deve ter no máximo 50 caracteres.";

    // ── Título eleitoral ─────────────────────────────────────────────────────
    public static final String TITULO_ELEITORAL_OBRIGATORIO     = "O título eleitoral é obrigatório.";
    public static final String TITULO_ELEITORAL_MAX             = "O título eleitoral deve ter no máximo 12 caracteres.";
    public static final String TITULO_ELEITORAL_APENAS_NUMEROS  = "O título eleitoral deve conter apenas números.";

    // ── Disponibilização de mapa ─────────────────────────────────────────────
    public static final String DATA_LIMITE_VALIDACAO_OBRIGATORIA = "A data limite para validação é obrigatória.";
    public static final String DATA_LIMITE_VALIDACAO_FUTURA     = "A data limite para validação deve ser uma data futura.";

    // ── Ação em bloco ────────────────────────────────────────────────────────
    public static final String ACAO_OBRIGATORIA                 = "A ação é obrigatória";
    public static final String ACAO_DEVE_SER_INFORMADA          = "A ação deve ser informada";
    public static final String PELO_MENOS_UM_SUBPROCESSO        = "Pelo menos um subprocesso deve ser selecionado";
    public static final String PELO_MENOS_UMA_UNIDADE           = "Pelo menos uma unidade deve ser selecionada";
    public static final String MOTIVO_MAX                       = "Motivo deve ter no máximo 200 caracteres";

    // ── Competências e atividades ────────────────────────────────────────────
    public static final String COMPETENCIA_DEVE_TER_ATIVIDADE   = "A competência deve ter pelo menos uma atividade associada";
    public static final String LISTA_COMPETENCIAS_NAO_PODE_SER_VAZIA = "A lista de competências não pode ser vazia";

    // ── Regras de negócio — Processo ─────────────────────────────────────────
    public static final String PROCESSO_SO_EDITAVEL_EM_CRIADO   = "Apenas processos na situação 'CRIADO' podem ser editados.";
    public static final String PROCESSO_SO_REMOVIVEL_EM_CRIADO  = "Apenas processos na situação 'CRIADO' podem ser removidos.";
    public static final String PROCESSO_SO_INICIAVEL_EM_CRIADO  = "Apenas processos na situação 'CRIADO' podem ser iniciados.";
    public static final String LISTA_UNIDADES_OBRIGATORIA_REVISAO = "A lista de unidades é obrigatória para iniciar o processo de revisão.";
    public static final String SEM_UNIDADES_PARTICIPANTES       = "Não há unidades participantes definidas.";
    public static final String SELECIONE_AO_MENOS_UMA_UNIDADE   = "Selecione ao menos uma unidade.";
    public static final String UNIDADE_NAO_PARTICIPA            = "Unidade não participa deste processo.";
    public static final String SITUACAO_INVALIDA                = "Situação inválida.";
    public static final String SUBPROCESSOS_NAO_HOMOLOGADOS     = "Subprocessos não homologados.";
    public static final String PROCESSO_DEVE_ESTAR_FINALIZADO   = "Processo deve estar finalizado.";
    public static final String DATA_FIM_DEVE_SER_POSTERIOR      = "A data de término deve ser posterior à data de início.";
    public static final String UNIDADES_SEM_MAPA                = "Unidades sem mapa.";
    public static final String UNIDADES_EM_PROCESSO_ATIVO       = "Unidades já em processo ativo.";

    // ── Regras de negócio — Mapa ─────────────────────────────────────────────
    public static final String SUBPROCESSO_SEM_MAPA             = "Subprocesso não possui mapa associado.";
    public static final String MAPA_SEM_ATIVIDADES              = "O mapa de competências deve ter ao menos uma atividade cadastrada.";
    public static final String ATIVIDADES_SEM_CONHECIMENTOS     = "Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.";
    public static final String COMPETENCIAS_SEM_ATIVIDADE       = "Existem competências que não foram associadas a nenhuma atividade.";
    public static final String ATIVIDADES_SEM_COMPETENCIA       = "Existem atividades que não foram associadas a nenhuma competência.";
    public static final String TODAS_COMPETENCIAS_DEVEM_TER_ATIVIDADE = "Todas as competências devem estar associadas a pelo menos uma atividade.";
    public static final String ATIVIDADES_SEM_CONHECIMENTO_ASSOCIADO = "Existem atividades sem conhecimentos associados.";
    public static final String DESCRICAO_ATIVIDADE_DUPLICADA    = "Já existe uma atividade com esta descrição neste mapa.";
    public static final String DESCRICAO_CONHECIMENTO_DUPLICADA = "Já existe um conhecimento com esta descrição nesta atividade.";

    // ── Regras de negócio — Usuário ──────────────────────────────────────────
    public static final String USUARIO_JA_ADMINISTRADOR         = "Usuário já é um administrador do sistema";
    public static final String NAO_REMOVER_UNICO_ADMINISTRADOR  = "Não é permitido remover o único administrador do sistema";
    public static final String NAO_REMOVER_A_SI_MESMO           = "Não é permitido remover a si mesmo como administrador";

    // ── Regras de negócio — Importação ──────────────────────────────────────
    public static final String IMPORTACAO_ATIVIDADES_DUPLICADAS = "Uma ou mais atividades selecionadas já existentes no cadastro não puderam ser importadas.";
    public static final String IMPORTACAO_SO_PROCESSOS_FINALIZADOS = "A importação de atividades só permite subprocessos de processos finalizados.";

    // ── Templates (usar com .formatted()) ───────────────────────────────────
    public static final String TRANSICAO_INVALIDA               = "Transição de situação inválida: %s -> %s";
    public static final String SITUACAO_NAO_PERMITE             = "Situação do subprocesso não permite esta operação. Situação atual: %s. Situações permitidas: %s";
    public static final String SITUACAO_ATUAL                   = "Situação do subprocesso não permite esta operação. Situação atual: %s";
    public static final String AJUSTES_ESTADOS_ESPECIFICOS      = "Ajustes no mapa só podem ser feitos em estados específicos. Situação atual: %s";
    public static final String SITUACAO_IMPEDE_IMPORTACAO       = "Situação do subprocesso não permite importação. Situação atual: %s";
    public static final String SITUACAO_IMPEDE_IMPACTO          = "Situação do subprocesso (%s) não permite verificação de impactos para o perfil %s.";
    public static final String ATIVIDADES_PENDENTES_PREFIXO     = "Todas as atividades devem estar associadas a pelo menos uma competência.%nAtividades pendentes: %s";
    public static final String ATIVIDADES_DEVEM_TER_COMPETENCIA = "Todas as atividades devem estar associadas a pelo menos uma competência.";
    public static final String UNIDADES_INTERMEDIARIA_INVALIDAS = "Unidades INTERMEDIARIA inválidas: %s";
    public static final String UNIDADES_SEM_MAPA_VIGENTE        = "Unidades sem mapa vigente: %s";
    public static final String UNIDADES_SEM_SUBPROCESSOS        = "Algumas unidades selecionadas não possuem subprocessos vinculados neste processo: %s";

    // ── Permissões e Segurança ──────────────────────────────────────────────
    public static final String SEM_PERMISSAO_IMPORTAR           = "Usuário não tem permissão para importar atividades.";
    public static final String SEM_PERMISSAO_CONSULTAR_ORIGEM   = "Usuário não tem permissão para consultar o subprocesso de origem.";
    public static final String SEM_PERMISSAO_ACESSO_PERFIL      = "Usuário não tem permissão para acessar com perfil e unidade informados.";
    public static final String CREDENCIAIS_INVALIDAS            = "Credenciais inválidas";
    public static final String MUITAS_TENTATIVAS_LOGIN          = "Muitas tentativas de login no sistema. Tente novamente mais tarde.";
    public static final String MUITAS_TENTATIVAS_DORMINDO       = "Muitas tentativas de login. Tente novamente em alguns minutos.";
    public static final String AUTENTICACAO_EXTERNA_FALHA       = "Falha na autenticação externa.";
    public static final String AUTENTICACAO_ERRO_INESPERADO     = "Ocorreu um erro inesperado durante a autenticação.";
    public static final String SES_EXPIRADA                     = "Sessão expirada ou inválida. Faça login novamente.";
    public static final String SES_INVALIDA_USUARIO             = "Sessão inválida para o usuário informado.";
    public static final String SEM_PERMISSAO_DISPONIBILIZAR     = "Sem permissão para disponibilizar mapas.";
    public static final String SEM_PERMISSAO_EDITAR_ATIVIDADES  = "Usuário não tem permissão para editar atividades neste subprocesso.";
    public static final String SEM_PERMISSAO_VERIFICAR_IMPACTOS = "Usuário não tem permissão para verificar impactos.";
}
