-- Script de limpeza de dados transacionais para testes E2E
-- Mantém estrutura do schema e dados de referência (seed.sql)
-- Remove apenas dados criados durante os testes

-- Desabilitar verificação de foreign keys temporariamente
SET REFERENTIAL_INTEGRITY FALSE;

-- Limpar dados transacionais (ordem inversa das dependências)
DELETE FROM sgc.alerta_usuario;
DELETE FROM sgc.alerta;
DELETE FROM sgc.analise;
DELETE FROM sgc.movimentacao;
DELETE FROM sgc.notificacao;
DELETE FROM sgc.competencia_atividade;
DELETE FROM sgc.conhecimento;
DELETE FROM sgc.atividade;
DELETE FROM sgc.competencia;
DELETE FROM sgc.subprocesso;
DELETE FROM sgc.mapa WHERE codigo NOT IN (SELECT mapa_vigente_codigo FROM sgc.unidade WHERE mapa_vigente_codigo IS NOT NULL);
DELETE FROM sgc.processo;
DELETE FROM sgc.unidade_processo;
DELETE FROM sgc.atribuicao_temporaria;

-- Reabilitar verificação de foreign keys
SET REFERENTIAL_INTEGRITY TRUE;

-- Reset de sequences (IDs) para valores iniciais
-- Isso garante que os IDs sejam previsíveis entre execuções
ALTER SEQUENCE sgc.processo_seq RESTART WITH 1;
ALTER SEQUENCE sgc.subprocesso_seq RESTART WITH 1;
ALTER SEQUENCE sgc.mapa_seq RESTART WITH 100;  -- Começar em 100 para não conflitar com mapas do seed
ALTER SEQUENCE sgc.atividade_seq RESTART WITH 1;
ALTER SEQUENCE sgc.competencia_seq RESTART WITH 1;
ALTER SEQUENCE sgc.conhecimento_seq RESTART WITH 1;
ALTER SEQUENCE sgc.alerta_seq RESTART WITH 1;
ALTER SEQUENCE sgc.analise_seq RESTART WITH 1;
ALTER SEQUENCE sgc.movimentacao_seq RESTART WITH 1;
ALTER SEQUENCE sgc.notificacao_seq RESTART WITH 1;
ALTER SEQUENCE sgc.unidade_processo_seq RESTART WITH 1;
ALTER SEQUENCE sgc.atribuicao_temporaria_seq RESTART WITH 1;
