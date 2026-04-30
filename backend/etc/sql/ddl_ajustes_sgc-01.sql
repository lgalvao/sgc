-- #################################################################
-- CRIA TABELA DE OUTBOX DE E-MAILS, FLEXIBILIZA ALERTAS E REMOVE NOTIFICACAO LEGADA
-- #################################################################
-- Objetivo:
--   1. Corrigir ALERTA para permitir alertas destinados a usuarios.
--   2. Persistir a intencao de envio de e-mail na mesma transacao, permitindo envio posterior com retry e auditoria.
--   3. Remover a tabela NOTIFICACAO, substituida por ALERTA e NOTIFICACAO_EMAIL.
--
-- Observacao:
--   Este script usa blocos condicionais para tolerar bancos parcialmente ajustados.

DECLARE
    v_nullable USER_TAB_COLUMNS.NULLABLE%TYPE;
BEGIN
    SELECT nullable
      INTO v_nullable
      FROM user_tab_columns
     WHERE table_name = 'ALERTA'
       AND column_name = 'PROCESSO_CODIGO';

    IF v_nullable = 'N' THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ALERTA MODIFY (processo_codigo NULL)';
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_tables
     WHERE table_name = 'NOTIFICACAO';

    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'DROP TABLE NOTIFICACAO CASCADE CONSTRAINTS';
    END IF;
END;
/

DECLARE
    v_nullable USER_TAB_COLUMNS.NULLABLE%TYPE;
BEGIN
    SELECT nullable
      INTO v_nullable
      FROM user_tab_columns
     WHERE table_name = 'ALERTA'
       AND column_name = 'UNIDADE_DESTINO_CODIGO';

    IF v_nullable = 'N' THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ALERTA MODIFY (unidade_destino_codigo NULL)';
    END IF;
END;
/

COMMENT ON COLUMN ALERTA.usuario_destino_titulo IS 'Usuario destino do alerta para alertas pessoais.';

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_constraints
     WHERE constraint_name = 'CK_ALERTA_DESTINO';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ALERTA ADD CONSTRAINT ck_alerta_destino CHECK (unidade_destino_codigo IS NOT NULL OR usuario_destino_titulo IS NOT NULL)';
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_tables
     WHERE table_name = 'NOTIFICACAO_EMAIL';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE q'[
            CREATE TABLE NOTIFICACAO_EMAIL
            (
                codigo                 NUMBER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) NOT NULL,
                subprocesso_codigo     NUMBER                            NULL,
                tipo_notificacao       VARCHAR2(80)                      NULL,
                usuario_destino_titulo VARCHAR2(12)                      NULL,
                unidade_destino_sigla  VARCHAR2(20)                      NULL,
                destinatario           VARCHAR2(255)                     NOT NULL,
                assunto                VARCHAR2(500)                     NOT NULL,
                corpo_html             CLOB                              NOT NULL,
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
            )
        ]';
    ELSE
        -- Se a tabela ja existe, garante as novas colunas e remove alerta_codigo (legado)
        DECLARE
            v_col_count NUMBER;
        BEGIN
            -- Coluna legada
            SELECT COUNT(*) INTO v_col_count FROM user_tab_columns WHERE table_name = 'NOTIFICACAO_EMAIL' AND column_name = 'ALERTA_CODIGO';
            IF v_col_count > 0 THEN
                FOR r IN (SELECT constraint_name FROM user_constraints WHERE table_name = 'NOTIFICACAO_EMAIL' AND constraint_name = 'FK_NOTIF_EMAIL_ALERTA') LOOP
                    EXECUTE IMMEDIATE 'ALTER TABLE NOTIFICACAO_EMAIL DROP CONSTRAINT ' || r.constraint_name;
                END LOOP;
                EXECUTE IMMEDIATE 'ALTER TABLE NOTIFICACAO_EMAIL DROP COLUMN alerta_codigo';
            END IF;

            -- Nova coluna: unidade_destino_sigla
            SELECT COUNT(*) INTO v_col_count FROM user_tab_columns WHERE table_name = 'NOTIFICACAO_EMAIL' AND column_name = 'UNIDADE_DESTINO_SIGLA';
            IF v_col_count = 0 THEN
                EXECUTE IMMEDIATE 'ALTER TABLE NOTIFICACAO_EMAIL ADD (unidade_destino_sigla VARCHAR2(20) NULL)';
            END IF;

            -- Nova coluna: usuario_destino_titulo
            SELECT COUNT(*) INTO v_col_count FROM user_tab_columns WHERE table_name = 'NOTIFICACAO_EMAIL' AND column_name = 'USUARIO_DESTINO_TITULO';
            IF v_col_count = 0 THEN
                EXECUTE IMMEDIATE 'ALTER TABLE NOTIFICACAO_EMAIL ADD (usuario_destino_titulo VARCHAR2(12) NULL)';
            END IF;
        END;
    END IF;
END;
/

COMMENT ON TABLE NOTIFICACAO_EMAIL IS 'Caixa de saida de e-mails, para envio assincrono com retry e auditoria.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.codigo IS 'Identificador unico do e-mail.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.subprocesso_codigo IS 'Subprocesso associado ao evento que gerou o e-mail.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.tipo_notificacao IS 'Tipo de notificacao que originou o e-mail.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.usuario_destino_titulo IS 'Usuario destinatario quando a notificacao for pessoal.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.destinatario IS 'Endereco de e-mail de destino.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.assunto IS 'Assunto do e-mail.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.corpo_html IS 'Corpo do e-mail (html).';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.situacao IS 'Situacao do envio: PENDENTE, ENVIANDO, ENVIADO, FALHA_TEMPORARIA ou FALHA_DEFINITIVA.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.tentativas IS 'Quantidade de tentativas de envio realizadas.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.proxima_tentativa_em IS 'Data/hora a partir da qual o worker pode tentar reenviar.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.data_hora_criacao IS 'Data/hora de criacao do registro de outbox.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.data_hora_envio IS 'Data/hora do envio bem-sucedido.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.ultimo_erro IS 'Ultimo erro registrado durante tentativa de envio.';
COMMENT ON COLUMN NOTIFICACAO_EMAIL.chave_idempotencia IS 'Chave unica para evitar duplicidade de e-mail em reprocessamentos.';

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM user_indexes WHERE index_name = 'IX_NOTIF_EMAIL_FILA';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX ix_notif_email_fila ON NOTIFICACAO_EMAIL (situacao, proxima_tentativa_em, data_hora_criacao)';
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM user_indexes WHERE index_name = 'IX_NOTIF_EMAIL_SUBPROC_SIT';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX ix_notif_email_subproc_sit ON NOTIFICACAO_EMAIL (subprocesso_codigo, situacao)';
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM user_indexes WHERE index_name = 'IX_NOTIF_EMAIL_ALERTA';
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'DROP INDEX ix_notif_email_alerta';
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM user_indexes WHERE index_name = 'IX_NOTIF_EMAIL_USUARIO';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX ix_notif_email_usuario ON NOTIFICACAO_EMAIL (usuario_destino_titulo)';
    END IF;
END;
/

-- #################################################################
-- GARANTE PARAMETROS ESSENCIAIS DO SISTEMA
-- #################################################################
DECLARE
    PROCEDURE garantir_parametro(p_chave VARCHAR2, p_desc VARCHAR2, p_valor VARCHAR2) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM parametro WHERE chave = p_chave;
        IF v_count = 0 THEN
            INSERT INTO parametro (chave, descricao, valor) VALUES (p_chave, p_desc, p_valor);
        END IF;
    END;
BEGIN
    garantir_parametro('DIAS_INATIVACAO_PROCESSO', 'Dias para inativacao de processos', '30');
    garantir_parametro('DIAS_ALERTA_NOVO', 'Dias para indicacao de alerta como novo', '3');
    garantir_parametro('TEMA_ESCURO', 'Habilitar tema escuro global', 'false');
END;
/
