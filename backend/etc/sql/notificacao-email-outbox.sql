-- #################################################################
-- SCRIPT DDL ORACLE PARA ALERTAS PESSOAIS E OUTBOX DE E-MAILS
-- #################################################################
--
-- Objetivo:
--   1. Corrigir ALERTA para permitir alertas destinados a usuarios.
--   2. Persistir a intencao de envio de e-mail na mesma transacao do
--      workflow, permitindo envio posterior com retry e auditoria.
--
-- Escopo:
--   Script unico para aplicar sobre o baseline oficial do SGC.
--   Os DDLs baseline em ddl_tabelas.sql e ddl_views.sql permanecem
--   inalterados.
--
-- Situacoes previstas:
--   PENDENTE
--   ENVIANDO
--   ENVIADO
--   FALHA_TEMPORARIA
--   FALHA_DEFINITIVA

-- #################################################################
-- AJUSTE DO MODELO DE ALERTAS
-- #################################################################
--
-- ALERTA pode ser destinado a uma unidade ou a um usuario especifico.
-- Por isso, processo_codigo e unidade_destino_codigo precisam ser opcionais.
-- A constraint abaixo garante que todo alerta tenha ao menos um destino.

ALTER TABLE ALERTA MODIFY (processo_codigo NULL);
ALTER TABLE ALERTA MODIFY (unidade_destino_codigo NULL);

COMMENT ON COLUMN ALERTA.usuario_destino_titulo IS 'Usuario destino do alerta quando o alerta for pessoal.';

ALTER TABLE ALERTA ADD CONSTRAINT ck_alerta_destino CHECK (
    unidade_destino_codigo IS NOT NULL
    OR usuario_destino_titulo IS NOT NULL
);

CREATE TABLE NOTIFICACAO_EMAIL
(
    codigo                NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    alerta_codigo         NUMBER NULL,
    subprocesso_codigo    NUMBER NULL,
    tipo_notificacao      VARCHAR2(80) NULL,
    usuario_destino_titulo VARCHAR2(12) NULL,
    destinatario          VARCHAR2(255) NOT NULL,
    assunto               VARCHAR2(500) NOT NULL,
    corpo_html            CLOB NOT NULL,
    situacao              VARCHAR2(30) DEFAULT 'PENDENTE' NOT NULL,
    tentativas            NUMBER(5) DEFAULT 0 NOT NULL,
    proxima_tentativa_em  TIMESTAMP NULL,
    data_hora_criacao     TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    data_hora_envio       TIMESTAMP NULL,
    ultimo_erro           VARCHAR2(2000) NULL,
    chave_idempotencia    VARCHAR2(255) NOT NULL,
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
    CONSTRAINT fk_notif_email_alerta FOREIGN KEY (alerta_codigo) REFERENCES ALERTA (codigo),
    CONSTRAINT fk_notif_email_subproc FOREIGN KEY (subprocesso_codigo) REFERENCES SUBPROCESSO (codigo)
);

COMMENT ON TABLE NOTIFICACAO_EMAIL IS 'Outbox de e-mails gerados pelo SGC para envio assíncrono com retry e auditoria.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.codigo IS 'Identificador unico do e-mail pendente.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.alerta_codigo IS 'Alerta interno associado ao e-mail, quando houver.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.subprocesso_codigo IS 'Subprocesso associado ao evento que gerou o e-mail, quando aplicavel.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.tipo_notificacao IS 'Tipo de notificacao que originou o e-mail.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.usuario_destino_titulo IS 'Usuario destinatario quando a notificacao for pessoal.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.destinatario IS 'Endereco de e-mail de destino.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.assunto IS 'Assunto final do e-mail.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.corpo_html IS 'Corpo HTML final do e-mail.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.situacao IS 'Situacao do envio: PENDENTE, ENVIANDO, ENVIADO, FALHA_TEMPORARIA ou FALHA_DEFINITIVA.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.tentativas IS 'Quantidade de tentativas de envio ja realizadas.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.proxima_tentativa_em IS 'Data e hora a partir da qual o worker pode tentar reenviar.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.data_hora_criacao IS 'Data e hora de criacao do registro de outbox.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.data_hora_envio IS 'Data e hora do envio bem-sucedido.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.ultimo_erro IS 'Ultimo erro tecnico registrado durante tentativa de envio.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.chave_idempotencia IS 'Chave unica para evitar duplicidade de e-mail em reprocessamentos.';

CREATE INDEX ix_notif_email_fila
    ON NOTIFICACAO_EMAIL (situacao, proxima_tentativa_em, data_hora_criacao);

CREATE INDEX ix_notif_email_subproc
    ON NOTIFICACAO_EMAIL (subprocesso_codigo);

CREATE INDEX ix_notif_email_alerta
    ON NOTIFICACAO_EMAIL (alerta_codigo);

CREATE INDEX ix_notif_email_usuario
    ON NOTIFICACAO_EMAIL (usuario_destino_titulo);
