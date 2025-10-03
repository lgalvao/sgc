-- Fixture para testes de integração do endpoint GET /api/subprocessos/{id}
-- Cria processo, unidade, usuário, mapa, subprocesso, movimentações, atividades e conhecimentos.
-- Tornar idempotente via MERGE

MERGE INTO PROCESSO (codigo, descricao, tipo, situacao, data_criacao) KEY(codigo)
VALUES (1, 'Processo Teste', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_TIMESTAMP);

MERGE INTO UNIDADE (codigo, nome, sigla, titular_titulo, tipo, situacao, unidade_superior_codigo) KEY(codigo)
VALUES (10, 'Unidade X', 'UX', '0001', 'OPERACIONAL', 'ATIVA', NULL);

MERGE INTO USUARIO (titulo, nome, email, ramal, unidade_codigo) KEY(titulo)
VALUES ('0001', 'Titular X', 'titular@exemplo', '1234', 10);

MERGE INTO MAPA (codigo, data_hora_disponibilizado, observacoes_disponibilizacao, sugestoes_apresentadas, data_hora_homologado) KEY(codigo)
VALUES (100, NULL, NULL, NULL, NULL);

MERGE INTO SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, data_limite_etapa1, data_fim_etapa1, data_limite_etapa2, data_fim_etapa2, situacao_id) KEY(codigo)
VALUES (1, 1, 10, 100, '2025-12-31', NULL, NULL, NULL, 'EM_ANDAMENTO');

-- Movimentações: mais recente deve vir primeiro (ordenadas por data_hora desc)
MERGE INTO MOVIMENTACAO (codigo, subprocesso_codigo, data_hora, unidade_origem_codigo, unidade_destino_codigo, descricao) KEY(codigo)
VALUES (501, 1, '2025-10-02T12:00:00', 10, 11, 'ENCAMINHADO');

MERGE INTO MOVIMENTACAO (codigo, subprocesso_codigo, data_hora, unidade_origem_codigo, unidade_destino_codigo, descricao) KEY(codigo)
VALUES (500, 1, '2025-10-01T10:00:00', NULL, 10, 'INÍCIO_DO_MAPEAMENTO');

MERGE INTO ATIVIDADE (codigo, mapa_codigo, descricao) KEY(codigo)
VALUES (200, 100, 'Atividade A');

MERGE INTO CONHECIMENTO (codigo, atividade_codigo, descricao) KEY(codigo)
VALUES (300, 200, 'Conhecimento 1');