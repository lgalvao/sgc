-- Script para apagar TODOS os dados de processos no SGC (Oracle)
-- Este script replica a lógica de exclusão completa do sistema, mas aplicada a todos os registros.
-- USE COM CAUTELA: Esta operação é irreversível.

DECLARE
    v_count INTEGER;
BEGIN
    -- 1. Notificações de e-mail vinculadas a subprocessos
    DELETE FROM sgc.notificacao_email WHERE subprocesso_codigo IS NOT NULL;

    -- 2. Alertas vinculados a processos
    DELETE FROM sgc.alerta_usuario WHERE alerta_codigo IN (SELECT codigo FROM sgc.alerta WHERE processo_codigo IS NOT NULL);
    DELETE FROM sgc.alerta WHERE processo_codigo IS NOT NULL;

    -- 3. Módulo de Diagnóstico (Tabelas opcionais)
    -- Avaliação Servidor
    SELECT count(*) INTO v_count FROM user_tables WHERE table_name = 'AVALIACAO_SERVIDOR';
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'DELETE FROM sgc.avaliacao_servidor';
    END IF;

    -- Situação de Capacitação
    SELECT count(*) INTO v_count FROM user_tables WHERE table_name = 'SITUACAO_CAPACITACAO';
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'DELETE FROM sgc.situacao_capacitacao';
    END IF;

    -- Diagnóstico
    SELECT count(*) INTO v_count FROM user_tables WHERE table_name = 'DIAGNOSTICO';
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'DELETE FROM sgc.diagnostico';
    END IF;

    -- 4. Itens do Mapa (Atividades, Competências, Conhecimentos)
    DELETE FROM sgc.conhecimento;
    DELETE FROM sgc.competencia_atividade;
    DELETE FROM sgc.unidade_mapa; -- Limpa o vínculo de "mapa vigente" das unidades
    DELETE FROM sgc.atividade;
    DELETE FROM sgc.competencia;
    DELETE FROM sgc.mapa;

    -- 5. Fluxo de Subprocessos e Processos
    DELETE FROM sgc.analise;
    DELETE FROM sgc.movimentacao;
    DELETE FROM sgc.subprocesso;
    DELETE FROM sgc.unidade_processo;
    DELETE FROM sgc.processo;

    COMMIT;
END;
