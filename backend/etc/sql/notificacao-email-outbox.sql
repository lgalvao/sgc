-- #################################################################
-- SCRIPT DDL ORACLE PARA OUTBOX DE E-MAILS DO SGC
-- #################################################################
--
-- Objetivo:
--   Persistir a intencao de envio de e-mail na mesma transacao do
--   workflow, permitindo envio posterior com retry e auditoria.
--
-- Situacoes previstas:
--   PENDENTE
--   ENVIANDO
--   ENVIADO
--   FALHA_TEMPORARIA
--   FALHA_DEFINITIVA

CREATE TABLE NOTIFICACAO_EMAIL
(
    codigo                NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
    subprocesso_codigo    NUMBER NULL,
    tipo_transicao        VARCHAR2(80) NULL,
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
    CONSTRAINT fk_notif_email_subproc FOREIGN KEY (subprocesso_codigo) REFERENCES SUBPROCESSO (codigo)
);

COMMENT ON TABLE NOTIFICACAO_EMAIL IS 'Outbox de e-mails gerados pelo SGC para envio assíncrono com retry e auditoria.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.codigo IS 'Identificador unico do e-mail pendente.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.subprocesso_codigo IS 'Subprocesso associado ao evento que gerou o e-mail, quando aplicavel.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.tipo_transicao IS 'Tipo de transicao de workflow que originou o e-mail, quando aplicavel.';
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
