-- #################################################################
-- SCRIPT DDL ORACLE PARA CRIAÇÃO DAS TABELAS DO MODELO DE DADOS SGC
-- #################################################################

-- 1. Tabela PROCESSO
CREATE TABLE PROCESSO
(
    codigo           NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    data_criacao     TIMESTAMP     NOT NULL,
    data_finalizacao TIMESTAMP     NULL,
    data_limite      DATE          NOT NULL,
    descricao        VARCHAR2(255) NOT NULL,
    situacao         VARCHAR2(20)  NOT NULL,
    tipo             VARCHAR2(20)  NOT NULL,
    CONSTRAINT pk_processo PRIMARY KEY (codigo)
);

COMMENT
    ON COLUMN PROCESSO.codigo IS 'Identificador único do processo.';
COMMENT
    ON COLUMN PROCESSO.data_criacao IS 'Data de criação do processo.';
COMMENT
    ON COLUMN PROCESSO.data_finalizacao IS 'Data de finalização do processo.';
COMMENT
    ON COLUMN PROCESSO.data_limite IS 'Data limite para conclusão.';
COMMENT
    ON COLUMN PROCESSO.descricao IS 'Descrição do processo.';
COMMENT
    ON COLUMN PROCESSO.situacao IS 'Situação atual ("CRIADO", "EM_ANDAMENTO", "FINALIZADO").';
COMMENT
    ON COLUMN PROCESSO.tipo IS 'Tipo do processo ("MAPEAMENTO", "REVISAO", "DIAGNOSTICO").';


-- 2. Tabela UNIDADE_PROCESSO (Tabela de Associação N:M entre PROCESSO e unidades - Snapshot)
CREATE TABLE UNIDADE_PROCESSO
(
    processo_codigo          NUMBER        NOT NULL,
    unidade_codigo           NUMBER        NOT NULL,
    nome                     VARCHAR2(255) NULL,
    sigla                    VARCHAR2(20)  NULL,
    matricula_titular        VARCHAR2(8)   NULL,
    titulo_titular           VARCHAR2(12)  NULL,
    data_inicio_titularidade DATE          NULL,
    tipo                     VARCHAR2(20)  NULL,
    situacao                 VARCHAR2(20)  NULL,
    unidade_superior_codigo  NUMBER        NULL,
    CONSTRAINT pk_unidade_processo PRIMARY KEY (processo_codigo, unidade_codigo),
    CONSTRAINT fk_up_processo FOREIGN KEY (processo_codigo) REFERENCES PROCESSO (codigo)
);

COMMENT
    ON COLUMN UNIDADE_PROCESSO.processo_codigo IS 'Código do processo (ref PROCESSO) (PK, FK).';
COMMENT
    ON COLUMN UNIDADE_PROCESSO.unidade_codigo IS 'Identificador único da unidade (PK).';
COMMENT
    ON COLUMN UNIDADE_PROCESSO.nome IS 'Nome da unidade.';
COMMENT
    ON COLUMN UNIDADE_PROCESSO.sigla IS 'Sigla da unidade.';
COMMENT
    ON COLUMN UNIDADE_PROCESSO.matricula_titular IS 'Matrícula do usuário titular da unidade.';
COMMENT
    ON COLUMN UNIDADE_PROCESSO.titulo_titular IS 'TE do usuário titular da unidade.';
COMMENT
    ON COLUMN UNIDADE_PROCESSO.data_inicio_titularidade IS 'Data de início da titularidade.';
COMMENT
    ON COLUMN UNIDADE_PROCESSO.tipo IS 'Tipo da unidade ("OPERACIONAL", "INTEROPERACIONAL", "INTERMEDIARIA").';
COMMENT
    ON COLUMN UNIDADE_PROCESSO.situacao IS 'Situação da unidade.';
COMMENT
    ON COLUMN UNIDADE_PROCESSO.unidade_superior_codigo IS 'Unidade superior na hierarquia.';


-- 3. Tabela SERVIDOR_PROCESSO
CREATE TABLE SERVIDOR_PROCESSO
(
    codigo          NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    processo_codigo NUMBER        NOT NULL,
    unidade_codigo  NUMBER        NOT NULL,
    usuario_titulo  VARCHAR2(12)  NOT NULL,
    matricula       VARCHAR2(8)   NULL,
    nome            VARCHAR2(255) NOT NULL,
    email           VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_servidor_processo PRIMARY KEY (codigo),
    CONSTRAINT fk_servidor_processo FOREIGN KEY (processo_codigo) REFERENCES PROCESSO (codigo),
    CONSTRAINT uk_servidor_processo UNIQUE (processo_codigo, unidade_codigo, usuario_titulo)
);

COMMENT
    ON COLUMN SERVIDOR_PROCESSO.codigo IS 'Identificador único do snapshot de servidor participante do processo.';
COMMENT
    ON COLUMN SERVIDOR_PROCESSO.processo_codigo IS 'Código do processo ao qual o servidor participante pertence.';
COMMENT
    ON COLUMN SERVIDOR_PROCESSO.unidade_codigo IS 'Código da unidade participante associada ao servidor no snapshot.';
COMMENT
    ON COLUMN SERVIDOR_PROCESSO.usuario_titulo IS 'Título eleitoral do servidor participante no snapshot.';
COMMENT
    ON COLUMN SERVIDOR_PROCESSO.matricula IS 'Matrícula do servidor participante no snapshot.';
COMMENT
    ON COLUMN SERVIDOR_PROCESSO.nome IS 'Nome do servidor participante no snapshot.';
COMMENT
    ON COLUMN SERVIDOR_PROCESSO.email IS 'E-mail do servidor participante no snapshot.';


-- 4. Tabela SUBPROCESSO
CREATE TABLE SUBPROCESSO
(
    codigo             NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    processo_codigo    NUMBER       NOT NULL,
    unidade_codigo     NUMBER       NOT NULL,
    data_limite_etapa1 DATE         NOT NULL,
    data_fim_etapa1    TIMESTAMP    NULL,
    data_limite_etapa2 DATE         NULL,
    data_fim_etapa2    TIMESTAMP    NULL,
    situacao           VARCHAR2(50) NOT NULL,
    CONSTRAINT pk_subprocesso PRIMARY KEY (codigo),
    CONSTRAINT fk_subprocesso_up FOREIGN KEY (processo_codigo, unidade_codigo) REFERENCES UNIDADE_PROCESSO (processo_codigo, unidade_codigo)
);

COMMENT
    ON COLUMN SUBPROCESSO.codigo IS 'Identificador único do subprocesso.';
COMMENT
    ON COLUMN SUBPROCESSO.processo_codigo IS 'Processo ao qual pertence o subprocesso.';
COMMENT
    ON COLUMN SUBPROCESSO.unidade_codigo IS 'Unidade do subprocesso.';
COMMENT
    ON COLUMN SUBPROCESSO.data_limite_etapa1 IS 'Data limite da etapa 1.';
COMMENT
    ON COLUMN SUBPROCESSO.data_fim_etapa1 IS 'Data de fim da etapa 1.';
COMMENT
    ON COLUMN SUBPROCESSO.data_limite_etapa2 IS 'Data limite da etapa 2.';
COMMENT
    ON COLUMN SUBPROCESSO.data_fim_etapa2 IS 'Data de fim da etapa 2.';
COMMENT
    ON COLUMN SUBPROCESSO.situacao IS 'Situação atual do subprocesso.';


-- 5. Tabela MAPA
CREATE TABLE MAPA
(
    codigo                       NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    subprocesso_codigo           NUMBER         NOT NULL,
    data_hora_disponibilizado    TIMESTAMP      NULL,
    observacoes_disponibilizacao VARCHAR2(1000) NULL,
    sugestoes                    VARCHAR2(1000) NULL,
    data_hora_homologado         TIMESTAMP      NULL,
    CONSTRAINT pk_mapa PRIMARY KEY (codigo),
    CONSTRAINT fk_mapa_subprocesso FOREIGN KEY (subprocesso_codigo) REFERENCES SUBPROCESSO (codigo)
);

COMMENT
    ON COLUMN MAPA.codigo IS 'Identificador único do mapa.';
COMMENT
    ON COLUMN MAPA.subprocesso_codigo IS 'Subprocesso ao qual pertence o mapa (ref SUBPROCESSO).';
COMMENT
    ON COLUMN MAPA.data_hora_disponibilizado IS 'Data e hora em que o mapa foi disponibilizado.';
COMMENT
    ON COLUMN MAPA.observacoes_disponibilizacao IS 'Observacoes fornecidas durante a disponibilização do mapa.';
COMMENT
    ON COLUMN MAPA.sugestoes IS 'Sugestões apresentadas durante a validação do mapa.';
COMMENT
    ON COLUMN MAPA.data_hora_homologado IS 'Data e hora em que o mapa foi homologado.';


-- 6. Tabela DIAGNOSTICO
CREATE TABLE DIAGNOSTICO
(
    codigo             NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    subprocesso_codigo NUMBER    NOT NULL,
    data_conclusao     TIMESTAMP NULL,
    CONSTRAINT pk_diagnostico PRIMARY KEY (codigo),
    CONSTRAINT fk_diagnostico_subprocesso FOREIGN KEY (subprocesso_codigo) REFERENCES SUBPROCESSO (codigo),
    CONSTRAINT uk_diagnostico_subprocesso UNIQUE (subprocesso_codigo)
);

COMMENT
    ON COLUMN DIAGNOSTICO.codigo IS 'Identificador único do diagnóstico.';
COMMENT
    ON COLUMN DIAGNOSTICO.subprocesso_codigo IS 'Subprocesso ao qual o diagnóstico pertence.';
COMMENT
    ON COLUMN DIAGNOSTICO.data_conclusao IS 'Data e hora de conclusão do diagnóstico da unidade.';


-- 7. Tabela ADMINISTRADOR
CREATE TABLE ADMINISTRADOR
(
    usuario_titulo VARCHAR2(12) NOT NULL,
    CONSTRAINT pk_administrador PRIMARY KEY (usuario_titulo)
    -- FK implícita para VW_USUARIO.titulo
);

COMMENT
    ON COLUMN ADMINISTRADOR.usuario_titulo IS 'Usuário administrador (PK).';


-- 8. Tabela ALERTA
CREATE TABLE ALERTA
(
    codigo                 NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    processo_codigo        NUMBER        NULL,
    data_hora              TIMESTAMP     NOT NULL,
    unidade_origem_codigo  NUMBER        NOT NULL,
    unidade_destino_codigo NUMBER        NULL,
    usuario_destino_titulo VARCHAR2(12)  NULL, -- legado: associação alerta-usuário agora via ALERTA_USUARIO
    descricao              VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_alerta PRIMARY KEY (codigo),
    CONSTRAINT fk_alerta_processo FOREIGN KEY (processo_codigo) REFERENCES PROCESSO (codigo),
    CONSTRAINT ck_alerta_destino CHECK (unidade_destino_codigo IS NOT NULL OR usuario_destino_titulo IS NOT NULL)
);

COMMENT
    ON COLUMN ALERTA.codigo IS 'Identificador único do alerta.';
COMMENT
    ON COLUMN ALERTA.processo_codigo IS 'Processo associado (ref PROCESSO).';
COMMENT
    ON COLUMN ALERTA.data_hora IS 'Data e hora do alerta.';
COMMENT
    ON COLUMN ALERTA.unidade_origem_codigo IS 'Unidade de origem do alerta.';
COMMENT
    ON COLUMN ALERTA.unidade_destino_codigo IS 'Unidade de destino do alerta.';
COMMENT
    ON COLUMN ALERTA.usuario_destino_titulo IS 'Usuario destino do alerta para alertas pessoais.';
COMMENT
    ON COLUMN ALERTA.descricao IS 'Descrição do alerta.';


-- 9. Tabela ALERTA_USUARIO (Tabela de Associação N:M)
CREATE TABLE ALERTA_USUARIO
(
    alerta_codigo     NUMBER       NOT NULL,
    usuario_titulo    VARCHAR2(12) NOT NULL,
    data_hora_leitura TIMESTAMP    NOT NULL,
    CONSTRAINT pk_alerta_usuario PRIMARY KEY (alerta_codigo, usuario_titulo),
    CONSTRAINT fk_alerta_usuario_alerta FOREIGN KEY (alerta_codigo) REFERENCES ALERTA (codigo)
    -- FK implícita para VW_USUARIO.titulo
);

COMMENT
    ON COLUMN ALERTA_USUARIO.alerta_codigo IS 'Alerta associado (ref ALERTA) (PK, FK).';
COMMENT
    ON COLUMN ALERTA_USUARIO.usuario_titulo IS 'Usuário associado (PK).';
COMMENT
    ON COLUMN ALERTA_USUARIO.data_hora_leitura IS 'Indica a data e hora de leitura do alerta pelo usuário.';


-- 10. Tabela ANALISE
CREATE TABLE ANALISE
(
    codigo             NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    subprocesso_codigo NUMBER        NOT NULL,
    data_hora          TIMESTAMP     NOT NULL,
    tipo               VARCHAR2(25)  NOT NULL,
    acao               VARCHAR2(25)  NOT NULL,
    usuario_titulo     VARCHAR2(12)  NULL,
    unidade_codigo     NUMBER        NULL,
    motivo             VARCHAR2(200) NULL,
    observacoes        VARCHAR2(500) NULL,
    CONSTRAINT pk_analise PRIMARY KEY (codigo),
    CONSTRAINT fk_analise_subprocesso FOREIGN KEY (subprocesso_codigo) REFERENCES SUBPROCESSO (codigo)
    -- FKs implícitas para VW_USUARIO.titulo e VW_UNIDADE.codigo
);

COMMENT
    ON COLUMN ANALISE.codigo IS 'Identificador único da análise.';
COMMENT
    ON COLUMN ANALISE.subprocesso_codigo IS 'Subprocesso analisado (ref SUBPROCESSO).';
COMMENT
    ON COLUMN ANALISE.data_hora IS 'Data e hora da análise.';
COMMENT
    ON COLUMN ANALISE.tipo IS 'Tipo da análise ("CADASTRO", "VALIDACAO").';
COMMENT
    ON COLUMN ANALISE.acao IS 'Ação da análise ("ACEITE", "DEVOLUCAO").';
COMMENT
    ON COLUMN ANALISE.usuario_titulo IS 'Título do usuário que realizou a análise.';
COMMENT
    ON COLUMN ANALISE.unidade_codigo IS 'Código da unidade que realizou a análise.';
COMMENT
    ON COLUMN ANALISE.observacoes IS 'Observações da análise.';


-- 11. Tabela ATRIBUICAO_TEMPORARIA
CREATE TABLE ATRIBUICAO_TEMPORARIA
(
    codigo            NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    unidade_codigo    NUMBER        NOT NULL,
    usuario_matricula VARCHAR2(8)   NOT NULL,
    usuario_titulo    VARCHAR2(12)  NOT NULL,
    data_inicio       DATE          NOT NULL,
    data_termino      DATE          NOT NULL,
    justificativa     VARCHAR2(500) NULL,
    CONSTRAINT pk_atrib_temp PRIMARY KEY (codigo)
    -- FKs implícitas para VW_UNIDADE.codigo e VW_USUARIO.titulo
);

COMMENT
    ON COLUMN ATRIBUICAO_TEMPORARIA.codigo IS 'Identificador único da atribuição.';
COMMENT
    ON COLUMN ATRIBUICAO_TEMPORARIA.unidade_codigo IS 'Unidade da atribuição.';
COMMENT
    ON COLUMN ATRIBUICAO_TEMPORARIA.usuario_matricula IS 'Matrícula do usuário atribuído.';
COMMENT
    ON COLUMN ATRIBUICAO_TEMPORARIA.usuario_titulo IS 'TE do usuário atribuído.';
COMMENT
    ON COLUMN ATRIBUICAO_TEMPORARIA.data_inicio IS 'Data de início da atribuição.';
COMMENT
    ON COLUMN ATRIBUICAO_TEMPORARIA.data_termino IS 'Data de término da atribuição.';
COMMENT
    ON COLUMN ATRIBUICAO_TEMPORARIA.justificativa IS 'Justificativa da atribuição.';


-- 12. Tabela ATIVIDADE
CREATE TABLE ATIVIDADE
(
    codigo      NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    mapa_codigo NUMBER        NOT NULL,
    descricao   VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_atividade PRIMARY KEY (codigo),
    CONSTRAINT fk_atividade_mapa FOREIGN KEY (mapa_codigo) REFERENCES MAPA (codigo)
);

COMMENT
    ON COLUMN ATIVIDADE.codigo IS 'Identificador único da atividade.';
COMMENT
    ON COLUMN ATIVIDADE.mapa_codigo IS 'Mapa ao qual pertence a atividade (ref MAPA).';
COMMENT
    ON COLUMN ATIVIDADE.descricao IS 'Descrição da atividade.';


-- 13. Tabela COMPETENCIA
CREATE TABLE COMPETENCIA
(
    codigo      NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    mapa_codigo NUMBER        NOT NULL,
    descricao   VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_competencia PRIMARY KEY (codigo),
    CONSTRAINT fk_competencia_mapa FOREIGN KEY (mapa_codigo) REFERENCES MAPA (codigo)
);

COMMENT
    ON COLUMN COMPETENCIA.codigo IS 'Identificador único da competência.';
COMMENT
    ON COLUMN COMPETENCIA.mapa_codigo IS 'Mapa ao qual pertence a competência (ref MAPA).';
COMMENT
    ON COLUMN COMPETENCIA.descricao IS 'Descrição da competência.';


-- 14. Tabela COMPETENCIA_ATIVIDADE (Tabela de Associação N:M)
CREATE TABLE COMPETENCIA_ATIVIDADE
(
    atividade_codigo   NUMBER NOT NULL,
    competencia_codigo NUMBER NOT NULL,
    CONSTRAINT pk_comp_ativ PRIMARY KEY (atividade_codigo, competencia_codigo),
    CONSTRAINT fk_comp_ativ_ativ FOREIGN KEY (atividade_codigo) REFERENCES ATIVIDADE (codigo),
    CONSTRAINT fk_comp_ativ_comp FOREIGN KEY (competencia_codigo) REFERENCES COMPETENCIA (codigo)
);

COMMENT
    ON COLUMN COMPETENCIA_ATIVIDADE.atividade_codigo IS 'Referência a ATIVIDADE (PK, FK).';
COMMENT
    ON COLUMN COMPETENCIA_ATIVIDADE.competencia_codigo IS 'Referência a COMPETENCIA (PK, FK).';


-- 15. Tabela AVALIACAO_SERVIDOR
CREATE TABLE AVALIACAO_SERVIDOR
(
    codigo                     NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    diagnostico_codigo         NUMBER        NOT NULL,
    servidor_titulo            VARCHAR2(12)  NOT NULL,
    servidor_nome_snapshot     VARCHAR2(255) NULL,
    competencia_codigo         NUMBER        NOT NULL,
    autoimportancia            NUMBER(1)     NULL,
    autodominio                NUMBER(1)     NULL,
    chefia_importancia         NUMBER(1)     NULL,
    chefia_dominio             NUMBER(1)     NULL,
    consenso_importancia       NUMBER(1)     NULL,
    consenso_dominio           NUMBER(1)     NULL,
    importancia                NUMBER(1)     NULL,
    dominio                    NUMBER(1)     NULL,
    gap                        NUMBER        NULL,
    observacoes                CLOB          NULL,
    situacao_servidor          VARCHAR2(50)  NOT NULL,
    situacao_servidor_anterior VARCHAR2(50)  NULL,
    CONSTRAINT pk_avaliacao_servidor PRIMARY KEY (codigo),
    CONSTRAINT fk_avaliacao_diagnostico FOREIGN KEY (diagnostico_codigo) REFERENCES DIAGNOSTICO (codigo),
    CONSTRAINT fk_avaliacao_competencia FOREIGN KEY (competencia_codigo) REFERENCES COMPETENCIA (codigo),
    CONSTRAINT uk_avaliacao_servidor UNIQUE (diagnostico_codigo, servidor_titulo, competencia_codigo),
    CONSTRAINT ck_avaliacao_autoimportancia CHECK (autoimportancia BETWEEN 0 AND 6),
    CONSTRAINT ck_avaliacao_autodominio CHECK (autodominio BETWEEN 0 AND 6),
    CONSTRAINT ck_avaliacao_chefia_importancia CHECK (chefia_importancia BETWEEN 0 AND 6),
    CONSTRAINT ck_avaliacao_chefia_dominio CHECK (chefia_dominio BETWEEN 0 AND 6),
    CONSTRAINT ck_avaliacao_consenso_importancia CHECK (consenso_importancia BETWEEN 0 AND 6),
    CONSTRAINT ck_avaliacao_consenso_dominio CHECK (consenso_dominio BETWEEN 0 AND 6),
    CONSTRAINT ck_avaliacao_importancia CHECK (importancia BETWEEN 0 AND 6),
    CONSTRAINT ck_avaliacao_dominio CHECK (dominio BETWEEN 0 AND 6),
    CONSTRAINT ck_avaliacao_situacao CHECK (situacao_servidor IN (
                                                                  'AUTOAVALIACAO_NAO_INICIADA',
                                                                  'AUTOAVALIACAO_CONCLUIDA',
                                                                  'CONSENSO_CRIADO',
                                                                  'CONSENSO_APROVADO',
                                                                  'AVALIACAO_IMPOSSIBILITADA'
        )),
    CONSTRAINT ck_avaliacao_situacao_anterior CHECK (situacao_servidor_anterior IN (
                                                                                    'AUTOAVALIACAO_NAO_INICIADA',
                                                                                    'AUTOAVALIACAO_CONCLUIDA',
                                                                                    'CONSENSO_CRIADO',
                                                                                    'CONSENSO_APROVADO',
                                                                                    'AVALIACAO_IMPOSSIBILITADA'
        ))
);

COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.codigo IS 'Identificador único da avaliação de competência por servidor.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.diagnostico_codigo IS 'Diagnóstico ao qual a avaliação pertence.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.servidor_titulo IS 'Título eleitoral do servidor avaliado.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.servidor_nome_snapshot IS 'Nome do servidor no momento da criação do diagnóstico.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.competencia_codigo IS 'Competência avaliada.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.autoimportancia IS 'Autoavaliação de importância na escala 0..6.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.autodominio IS 'Autoavaliação de domínio na escala 0..6.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.chefia_importancia IS 'Avaliação da chefia para importância na escala 0..6.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.chefia_dominio IS 'Avaliação da chefia para domínio na escala 0..6.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.consenso_importancia IS 'Valor de consenso para importância na escala 0..6.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.consenso_dominio IS 'Valor de consenso para domínio na escala 0..6.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.importancia IS 'Valor final de importância na escala 0..6.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.dominio IS 'Valor final de domínio na escala 0..6.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.gap IS 'Diferença calculada entre importância e domínio quando aplicável.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.observacoes IS 'Observações registradas durante a avaliação.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.situacao_servidor IS 'Situação atual da avaliação do servidor no diagnóstico.';
COMMENT
    ON COLUMN AVALIACAO_SERVIDOR.situacao_servidor_anterior IS 'Situação anterior preservada para reversões de impossibilidade.';


-- 16. Tabela SITUACAO_CAPACITACAO
CREATE TABLE SITUACAO_CAPACITACAO
(
    codigo                  NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    diagnostico_codigo      NUMBER        NOT NULL,
    servidor_titulo         VARCHAR2(12)  NOT NULL,
    competencia_codigo      NUMBER        NOT NULL,
    situacao_capacitacao    VARCHAR2(2)   NULL,
    servidor_nome_snapshot  VARCHAR2(255) NULL,
    unidade_codigo_snapshot NUMBER        NULL,
    unidade_sigla_snapshot  VARCHAR2(20)  NULL,
    unidade_nome_snapshot   VARCHAR2(255) NULL,
    CONSTRAINT pk_situacao_capacitacao PRIMARY KEY (codigo),
    CONSTRAINT fk_sit_cap_diagnostico FOREIGN KEY (diagnostico_codigo) REFERENCES DIAGNOSTICO (codigo),
    CONSTRAINT fk_sit_cap_competencia FOREIGN KEY (competencia_codigo) REFERENCES COMPETENCIA (codigo),
    CONSTRAINT uk_situacao_capacitacao UNIQUE (diagnostico_codigo, servidor_titulo, competencia_codigo),
    CONSTRAINT ck_situacao_capacitacao CHECK (situacao_capacitacao IN ('NA', 'AC', 'EC', 'C', 'I'))
);

COMMENT
    ON COLUMN SITUACAO_CAPACITACAO.codigo IS 'Identificador único da situação de capacitação.';
COMMENT
    ON COLUMN SITUACAO_CAPACITACAO.diagnostico_codigo IS 'Diagnóstico ao qual a situação pertence.';
COMMENT
    ON COLUMN SITUACAO_CAPACITACAO.servidor_titulo IS 'Título eleitoral do servidor associado à situação de capacitação.';
COMMENT
    ON COLUMN SITUACAO_CAPACITACAO.servidor_nome_snapshot IS 'Nome do servidor no momento da criação do diagnóstico.';
COMMENT
    ON COLUMN SITUACAO_CAPACITACAO.unidade_codigo_snapshot IS 'Código da unidade do servidor no momento do diagnóstico.';
COMMENT
    ON COLUMN SITUACAO_CAPACITACAO.unidade_sigla_snapshot IS 'Sigla da unidade do servidor no momento do diagnóstico.';
COMMENT
    ON COLUMN SITUACAO_CAPACITACAO.unidade_nome_snapshot IS 'Nome da unidade do servidor no momento do diagnóstico.';
COMMENT
    ON COLUMN SITUACAO_CAPACITACAO.competencia_codigo IS 'Competência associada à situação de capacitação.';
COMMENT
    ON COLUMN SITUACAO_CAPACITACAO.situacao_capacitacao IS 'Situação de capacitação: NA, AC, EC, C ou I.';


-- 17. Tabela CONHECIMENTO
CREATE TABLE CONHECIMENTO
(
    codigo           NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    atividade_codigo NUMBER        NOT NULL,
    descricao        VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_conhecimento PRIMARY KEY (codigo),
    CONSTRAINT fk_conhecimento_atividade FOREIGN KEY (atividade_codigo) REFERENCES ATIVIDADE (codigo)
);

COMMENT
    ON COLUMN CONHECIMENTO.codigo IS 'Identificador único do conhecimento.';
COMMENT
    ON COLUMN CONHECIMENTO.atividade_codigo IS 'Atividade à qual pertence o conhecimento (ref ATIVIDADE).';
COMMENT
    ON COLUMN CONHECIMENTO.descricao IS 'Descrição do conhecimento.';


-- 18. Tabela MOVIMENTACAO
CREATE TABLE MOVIMENTACAO
(
    codigo                 NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    subprocesso_codigo     NUMBER        NOT NULL,
    data_hora              TIMESTAMP     NOT NULL,
    unidade_origem_codigo  NUMBER        NOT NULL,
    unidade_destino_codigo NUMBER        NOT NULL,
    usuario_titulo         VARCHAR2(12)  NOT NULL,
    descricao              VARCHAR2(255) NULL,
    CONSTRAINT pk_movimentacao PRIMARY KEY (codigo),
    CONSTRAINT fk_mov_subprocesso FOREIGN KEY (subprocesso_codigo) REFERENCES SUBPROCESSO (codigo)
    -- FKs implícitas para VW_UNIDADE.codigo e VW_USUARIO.titulo
);

COMMENT
    ON COLUMN MOVIMENTACAO.codigo IS 'Identificador único da movimentação.';
COMMENT
    ON COLUMN MOVIMENTACAO.subprocesso_codigo IS 'Subprocesso ao qual pertence a movimentação (ref SUBPROCESSO).';
COMMENT
    ON COLUMN MOVIMENTACAO.data_hora IS 'Data e hora da movimentação.';
COMMENT
    ON COLUMN MOVIMENTACAO.unidade_origem_codigo IS 'Unidade de origem.';
COMMENT
    ON COLUMN MOVIMENTACAO.unidade_destino_codigo IS 'Unidade de destino.';
COMMENT
    ON COLUMN MOVIMENTACAO.usuario_titulo IS 'TE do usuário que originou a movimentação.';
COMMENT
    ON COLUMN MOVIMENTACAO.descricao IS 'Descrição da movimentação.';

-- 19. Tabela CONFIGURACAO
CREATE TABLE CONFIGURACAO
(
    codigo    NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    chave     VARCHAR2(50)  NOT NULL,
    descricao VARCHAR2(255) NULL,
    valor     VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_configuracao PRIMARY KEY (codigo)
);

COMMENT
    ON COLUMN CONFIGURACAO.codigo IS 'Identificador único da configuração.';
COMMENT
    ON COLUMN CONFIGURACAO.chave IS 'Chave da configuração.';
COMMENT
    ON COLUMN CONFIGURACAO.descricao IS 'Descrição da configuração.';
COMMENT
    ON COLUMN CONFIGURACAO.valor IS 'Valor da configuração.';


-- 16. Tabela NOTIFICACAO_EMAIL
CREATE TABLE NOTIFICACAO_EMAIL
(
    codigo                 NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    destinatario           VARCHAR2(255)                     NOT NULL,
    assunto                VARCHAR2(500)                     NOT NULL,
    corpo_html             CLOB                              NOT NULL,
    subprocesso_codigo     NUMBER                            NULL,
    tipo_notificacao       VARCHAR2(80)                      NULL,
    usuario_destino_titulo VARCHAR2(12)                      NULL,
    unidade_destino_sigla  VARCHAR2(20)                      NULL,
    situacao               VARCHAR2(30) DEFAULT 'PENDENTE'   NOT NULL,
    tentativas             NUMBER(5)    DEFAULT 0            NOT NULL,
    proxima_tentativa_em   TIMESTAMP                         NULL,
    data_hora_criacao      TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
    data_hora_envio        TIMESTAMP                         NULL,
    ultimo_erro            VARCHAR2(2000)                    NULL,
    chave_idempotencia     VARCHAR2(255)                     NOT NULL,
    CONSTRAINT pk_notif_email PRIMARY KEY (codigo),
    CONSTRAINT uk_notif_email_chave UNIQUE (chave_idempotencia),
    CONSTRAINT ck_notif_email_situacao CHECK (
        situacao IN (
                     'PENDENTE',
                     'ENVIANDO',
                     'ENVIADO',
                     'FALHA_TEMPORARIA',
                     'FALHA_DEFINITIVA'
            )
        ),
    CONSTRAINT ck_notif_email_tentativas CHECK (tentativas >= 0),
    CONSTRAINT fk_notif_email_subproc FOREIGN KEY (subprocesso_codigo) REFERENCES SUBPROCESSO (codigo)
);

COMMENT
    ON TABLE NOTIFICACAO_EMAIL IS 'Caixa de saida de e-mails, para envio assincrono com retry e auditoria.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.codigo IS 'Identificador unico do e-mail.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.subprocesso_codigo IS 'Subprocesso associado ao evento que gerou o e-mail.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.tipo_notificacao IS 'Tipo de notificacao que originou o e-mail.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.usuario_destino_titulo IS 'Usuario destinatario quando a notificacao for pessoal.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.unidade_destino_sigla IS 'Sigla da unidade destinataria quando a notificacao for destinada a uma unidade.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.destinatario IS 'Endereco de e-mail de destino.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.assunto IS 'Assunto do e-mail.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.corpo_html IS 'Corpo do e-mail (html).';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.situacao IS 'Situacao do envio: PENDENTE, ENVIANDO, ENVIADO, FALHA_TEMPORARIA ou FALHA_DEFINITIVA.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.tentativas IS 'Quantidade de tentativas de envio realizadas.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.proxima_tentativa_em IS 'Data/hora a partir da qual o worker pode tentar reenviar.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.data_hora_criacao IS 'Data/hora de criacao do registro de outbox.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.data_hora_envio IS 'Data/hora do envio bem-sucedido.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.ultimo_erro IS 'Ultimo erro registrado durante tentativa de envio.';
COMMENT
    ON COLUMN NOTIFICACAO_EMAIL.chave_idempotencia IS 'Chave unica para evitar duplicidade de e-mail em reprocessamentos.';

CREATE INDEX ix_notif_email_fila ON NOTIFICACAO_EMAIL (situacao, proxima_tentativa_em, data_hora_criacao);
CREATE INDEX ix_notif_email_subproc_sit ON NOTIFICACAO_EMAIL (subprocesso_codigo, situacao);
CREATE INDEX ix_notif_email_usuario ON NOTIFICACAO_EMAIL (usuario_destino_titulo);
CREATE INDEX idx_diagnostico_subprocesso ON DIAGNOSTICO (subprocesso_codigo);
CREATE INDEX idx_avaliacao_diagnostico ON AVALIACAO_SERVIDOR (diagnostico_codigo);
CREATE INDEX idx_avaliacao_servidor ON AVALIACAO_SERVIDOR (servidor_titulo);
CREATE INDEX idx_avaliacao_competencia ON AVALIACAO_SERVIDOR (competencia_codigo);
CREATE INDEX idx_situacao_capacitacao_diagnostico ON SITUACAO_CAPACITACAO (diagnostico_codigo);
CREATE INDEX idx_situacao_capacitacao_servidor ON SITUACAO_CAPACITACAO (servidor_titulo);
CREATE INDEX idx_situacao_capacitacao_competencia ON SITUACAO_CAPACITACAO (competencia_codigo);
CREATE INDEX idx_atividade_mapa ON ATIVIDADE (mapa_codigo);
CREATE INDEX idx_competencia_mapa ON COMPETENCIA (mapa_codigo);
CREATE INDEX idx_conhecimento_atividade ON CONHECIMENTO (atividade_codigo);
CREATE INDEX idx_mapa_subprocesso ON MAPA (subprocesso_codigo);
CREATE INDEX idx_analise_subprocesso ON ANALISE (subprocesso_codigo);
CREATE INDEX idx_movimentacao_subprocesso ON MOVIMENTACAO (subprocesso_codigo);
CREATE INDEX idx_competencia_atividade_inv ON COMPETENCIA_ATIVIDADE (competencia_codigo);
CREATE INDEX idx_subprocesso_processo ON SUBPROCESSO (processo_codigo);
CREATE INDEX idx_subprocesso_unidade ON SUBPROCESSO (unidade_codigo);
CREATE INDEX idx_alerta_unidade_destino ON ALERTA (unidade_destino_codigo);
CREATE INDEX idx_alerta_processo ON ALERTA (processo_codigo);
CREATE INDEX idx_alerta_usuario_usuario ON ALERTA_USUARIO (usuario_titulo);
CREATE INDEX idx_unidade_processo_unidade ON UNIDADE_PROCESSO (unidade_codigo);


-- 17. Tabela FEEDBACK
CREATE TABLE FEEDBACK
(
    id                 RAW(16)                     NOT NULL,
    tipo               VARCHAR2(20)                NOT NULL,
    nota               VARCHAR2(2000)              NOT NULL,
    metadata_json      CLOB                        NULL,
    caminho_screenshot VARCHAR2(500)               NULL,
    usuario_id         VARCHAR2(100)               NOT NULL,
    usuario_nome       VARCHAR2(200)               NOT NULL,
    enviado_em         TIMESTAMP WITH TIME ZONE    NOT NULL,
    rota               VARCHAR2(500)               NOT NULL,
    status             VARCHAR2(20) DEFAULT 'NOVO' NOT NULL,
    CONSTRAINT pk_feedback PRIMARY KEY (id),
    CONSTRAINT ck_feedback_tipo CHECK (tipo IN ('BUG', 'SUGESTAO', 'QUESTAO', 'ELOGIO')),
    CONSTRAINT ck_feedback_status CHECK (status IN ('NOVO', 'REVISADO', 'RESOLVIDO', 'DESCARTADO'))
);

COMMENT
    ON TABLE FEEDBACK IS 'Registros de feedback coletados via widget.';
COMMENT
    ON COLUMN FEEDBACK.id IS 'Identificador unico (UUID).';
COMMENT
    ON COLUMN FEEDBACK.tipo IS 'Tipo de feedback: BUG, SUGESTAO, QUESTAO, ELOGIO.';
COMMENT
    ON COLUMN FEEDBACK.nota IS 'Descricao textual do feedback.';
COMMENT
    ON COLUMN FEEDBACK.metadata_json IS 'Metadados contextuais do sistema e navegador em formato JSON.';
COMMENT
    ON COLUMN FEEDBACK.caminho_screenshot IS 'Caminho no sistema de arquivos para a captura de tela.';
COMMENT
    ON COLUMN FEEDBACK.usuario_id IS 'Identificador do usuario que enviou o feedback.';
COMMENT
    ON COLUMN FEEDBACK.usuario_nome IS 'Nome do usuario que enviou o feedback.';
COMMENT
    ON COLUMN FEEDBACK.enviado_em IS 'Data e hora do envio do feedback.';
COMMENT
    ON COLUMN FEEDBACK.rota IS 'Caminho da rota/pagina onde o feedback foi gerado.';
COMMENT
    ON COLUMN FEEDBACK.status IS 'Situacao do feedback: NOVO, REVISADO, RESOLVIDO, DESCARTADO.';

CREATE INDEX idx_feedback_status ON FEEDBACK (status);
CREATE INDEX idx_feedback_usuario ON FEEDBACK (usuario_id);
CREATE INDEX idx_feedback_data ON FEEDBACK (enviado_em);


-- 18. Tabela UNIDADE_MAPA (Relacionamento 1:1 onde a PK de uma é FK da outra)
CREATE TABLE UNIDADE_MAPA
(
    unidade_codigo      NUMBER NOT NULL,
    mapa_vigente_codigo NUMBER NOT NULL,
    CONSTRAINT pk_unidade_mapa PRIMARY KEY (unidade_codigo),
    CONSTRAINT fk_unidade_mapa_mapa FOREIGN KEY (mapa_vigente_codigo) REFERENCES MAPA (codigo)
    -- FK implícita para VW_UNIDADE.codigo
);

COMMENT
    ON COLUMN UNIDADE_MAPA.unidade_codigo IS 'Unidade organizacional (PK).';
COMMENT
    ON COLUMN UNIDADE_MAPA.mapa_vigente_codigo IS 'Mapa vigente para a unidade (PK, FK).';

INSERT INTO CONFIGURACAO (chave, descricao, valor)
VALUES ('DIAS_INATIVACAO_PROCESSO', 'Dias para inativacao de processos', '30');

INSERT INTO CONFIGURACAO (chave, descricao, valor)
VALUES ('DIAS_ALERTA_NOVO', 'Dias para indicacao de alerta como novo', '3');

INSERT INTO CONFIGURACAO (chave, descricao, valor)
VALUES ('TEMA_ESCURO', 'Habilitar tema escuro global', 'false');

COMMIT;
