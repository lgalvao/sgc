-- #################################################################
-- SCRIPT DDL ORACLE PARA CRIAÇÃO DAS TABELAS DO MODELO DE DADOS SGC
-- #################################################################

-- Drop das tabelas para garantir um ambiente limpo (Opcional, mas recomendado para testes)
/*
DROP TABLE UNIDADE_MAPA CASCADE CONSTRAINTS;
DROP TABLE PARAMETRO CASCADE CONSTRAINTS;
DROP TABLE NOTIFICACAO CASCADE CONSTRAINTS;
DROP TABLE MOVIMENTACAO CASCADE CONSTRAINTS;
DROP TABLE CONHECIMENTO CASCADE CONSTRAINTS;
DROP TABLE COMPETENCIA_ATIVIDADE CASCADE CONSTRAINTS;
DROP TABLE COMPETENCIA CASCADE CONSTRAINTS;
DROP TABLE ATIVIDADE CASCADE CONSTRAINTS;
DROP TABLE ATRIBUICAO_TEMPORARIA CASCADE CONSTRAINTS;
DROP TABLE ANALISE CASCADE CONSTRAINTS;
DROP TABLE ALERTA_USUARIO CASCADE CONSTRAINTS;
DROP TABLE ALERTA CASCADE CONSTRAINTS;
DROP TABLE ADMINISTRADOR CASCADE CONSTRAINTS;
DROP TABLE MAPA CASCADE CONSTRAINTS;
DROP TABLE SUBPROCESSO CASCADE CONSTRAINTS;
DROP TABLE UNIDADE_PROCESSO CASCADE CONSTRAINTS;
DROP TABLE PROCESSO CASCADE CONSTRAINTS;
*/

-- 1. Tabela PROCESSO
CREATE TABLE PROCESSO
(
    codigo           NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    data_criacao     TIMESTAMP NOT NULL,
    data_finalizacao TIMESTAMP NULL,
    data_limite      DATE NOT NULL,
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
    processo_codigo          NUMBER NOT NULL,
    unidade_codigo           NUMBER NOT NULL,
    nome                     VARCHAR2(255) NULL,
    sigla                    VARCHAR2(20)  NULL,
    matricula_titular        VARCHAR2(8)   NULL,
    titulo_titular           VARCHAR2(12)  NULL,
    data_inicio_titularidade DATE NULL,
    tipo                     VARCHAR2(20)  NULL,
    situacao                 VARCHAR2(20)  NULL,
    unidade_superior_codigo  NUMBER NULL,
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


-- 3. Tabela SUBPROCESSO
CREATE TABLE SUBPROCESSO
(
    codigo             NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    processo_codigo    NUMBER NOT NULL,
    unidade_codigo     NUMBER NOT NULL,
    data_limite_etapa1 DATE NOT NULL,
    data_fim_etapa1    TIMESTAMP NULL,
    data_limite_etapa2 DATE NULL,
    data_fim_etapa2    TIMESTAMP NULL,
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


-- 4. Tabela MAPA
CREATE TABLE MAPA
(
    codigo                       NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    subprocesso_codigo           NUMBER NOT NULL,
    data_hora_disponibilizado    TIMESTAMP NULL,
    observacoes_disponibilizacao VARCHAR2(1000) NULL,
    sugestoes                    VARCHAR2(1000) NULL,
    data_hora_homologado         TIMESTAMP NULL,
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


-- 5. Tabela ADMINISTRADOR
CREATE TABLE ADMINISTRADOR
(
    usuario_titulo VARCHAR2(12) NOT NULL,
    CONSTRAINT pk_administrador PRIMARY KEY (usuario_titulo)
    -- FK implícita para VW_USUARIO.titulo
);

COMMENT
ON COLUMN ADMINISTRADOR.usuario_titulo IS 'Usuário administrador (PK).';


-- 6. Tabela ALERTA
CREATE TABLE ALERTA
(
    codigo                 NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    processo_codigo        NUMBER NOT NULL,
    data_hora              TIMESTAMP NOT NULL,
    unidade_origem_codigo  NUMBER NOT NULL,
    unidade_destino_codigo NUMBER NOT NULL,
    usuario_destino_titulo VARCHAR2(12)  NULL,  -- legado: associação alerta-usuário agora via ALERTA_USUARIO
    descricao              VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_alerta PRIMARY KEY (codigo),
    CONSTRAINT fk_alerta_processo FOREIGN KEY (processo_codigo) REFERENCES PROCESSO (codigo)
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
ON COLUMN ALERTA.usuario_destino_titulo IS 'Usuário destino do alerta.';
COMMENT
ON COLUMN ALERTA.descricao IS 'Descrição do alerta.';


-- 7. Tabela ALERTA_USUARIO (Tabela de Associação N:M)
CREATE TABLE ALERTA_USUARIO
(
    alerta_codigo     NUMBER NOT NULL,
    usuario_titulo    VARCHAR2(12) NOT NULL,
    data_hora_leitura TIMESTAMP NOT NULL,
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


-- 8. Tabela ANALISE
CREATE TABLE ANALISE
(
    codigo             NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    subprocesso_codigo NUMBER NOT NULL,
    data_hora          TIMESTAMP NOT NULL,
    tipo               VARCHAR2(20)  NOT NULL,
    acao               VARCHAR2(20) NOT NULL,
    usuario_titulo     VARCHAR2(12)  NULL,
    unidade_codigo     NUMBER NULL,
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


-- 9. Tabela ATRIBUICAO_TEMPORARIA
CREATE TABLE ATRIBUICAO_TEMPORARIA
(
    codigo            NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    unidade_codigo    NUMBER NOT NULL,
    usuario_matricula VARCHAR2(8)   NOT NULL,
    usuario_titulo    VARCHAR2(12)  NOT NULL,
    data_inicio       DATE NOT NULL,
    data_termino      DATE NOT NULL,
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


-- 10. Tabela ATIVIDADE
CREATE TABLE ATIVIDADE
(
    codigo      NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    mapa_codigo NUMBER NOT NULL,
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


-- 11. Tabela COMPETENCIA
CREATE TABLE COMPETENCIA
(
    codigo      NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    mapa_codigo NUMBER NOT NULL,
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


-- 12. Tabela COMPETENCIA_ATIVIDADE (Tabela de Associação N:M)
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


-- 13. Tabela CONHECIMENTO
CREATE TABLE CONHECIMENTO
(
    codigo           NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    atividade_codigo NUMBER NOT NULL,
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


-- 14. Tabela MOVIMENTACAO
CREATE TABLE MOVIMENTACAO
(
    codigo                 NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    subprocesso_codigo     NUMBER NOT NULL,
    data_hora              TIMESTAMP NOT NULL,
    unidade_origem_codigo  NUMBER NOT NULL,
    unidade_destino_codigo NUMBER NOT NULL,
    usuario_titulo         VARCHAR2(12) NOT NULL,
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


-- 15. Tabela NOTIFICACAO
CREATE TABLE NOTIFICACAO
(
    codigo                 NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    subprocesso_codigo     NUMBER NOT NULL,
    data_hora              TIMESTAMP NOT NULL,
    unidade_origem_codigo  NUMBER NOT NULL,
    unidade_destino_codigo NUMBER NOT NULL,
    conteudo               VARCHAR2(500) NOT NULL,
    CONSTRAINT pk_notificacao PRIMARY KEY (codigo),
    CONSTRAINT fk_notif_subprocesso FOREIGN KEY (subprocesso_codigo) REFERENCES SUBPROCESSO (codigo)
    -- FKs implícitas para VW_UNIDADE.codigo
);

COMMENT
ON COLUMN NOTIFICACAO.codigo IS 'Identificador único da notificação.';
COMMENT
ON COLUMN NOTIFICACAO.subprocesso_codigo IS 'Subprocesso ao qual pertence a notificação (ref SUBPROCESSO).';
COMMENT
ON COLUMN NOTIFICACAO.data_hora IS 'Data e hora da notificação.';
COMMENT
ON COLUMN NOTIFICACAO.unidade_origem_codigo IS 'Unidade de origem.';
COMMENT
ON COLUMN NOTIFICACAO.unidade_destino_codigo IS 'Unidade de destino.';
COMMENT
ON COLUMN NOTIFICACAO.conteudo IS 'Conteúdo da notificação.';


-- 16. Tabela PARAMETRO
CREATE TABLE PARAMETRO
(
    codigo    NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    chave     VARCHAR2(50)  NOT NULL,
    descricao VARCHAR2(255) NULL,
    valor     VARCHAR2(255) NOT NULL,
    CONSTRAINT pk_parametro PRIMARY KEY (codigo)
);

COMMENT
ON COLUMN PARAMETRO.codigo IS 'Identificador único do parâmetro.';
COMMENT
ON COLUMN PARAMETRO.chave IS 'Chave do parâmetro.';
COMMENT
ON COLUMN PARAMETRO.descricao IS 'Descrição do parâmetro.';
COMMENT
ON COLUMN PARAMETRO.valor IS 'Valor do parâmetro.';


-- 17. Tabela UNIDADE_MAPA (Relacionamento 1:1 onde a PK de uma é FK da outra)
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


COMMIT;
