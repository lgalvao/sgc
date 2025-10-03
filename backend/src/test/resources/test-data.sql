-- Arquivo de inicialização de dados para PostgreSQL (test-data.sql)
-- Insere dados básicos usados nos testes

-- Processos
INSERT INTO PROCESSO (codigo, data_criacao, data_finalizacao, data_limite, descricao, situacao, tipo) VALUES (1, TIMESTAMP '2025-10-02 12:00:00', NULL, DATE '2025-10-09', 'Processo de Mapeamento Exemplo', 'CRIADO', 'MAPEAMENTO');
INSERT INTO PROCESSO (codigo, data_criacao, data_finalizacao, data_limite, descricao, situacao, tipo) VALUES (2, TIMESTAMP '2025-09-25 09:00:00', NULL, DATE '2025-10-05', 'Processo de Revisão 1', 'EM_ANDAMENTO', 'REVISAO');
INSERT INTO PROCESSO (codigo, data_criacao, data_finalizacao, data_limite, descricao, situacao, tipo) VALUES (3, TIMESTAMP '2025-09-20 14:00:00', TIMESTAMP '2025-09-28 16:00:00', DATE '2025-09-30', 'Processo Finalizado', 'FINALIZADO', 'MAPEAMENTO');

-- Unidades
INSERT INTO UNIDADE (codigo, nome, sigla, titular_titulo, tipo, situacao, unidade_superior_codigo) VALUES (10, 'Diretoria de Exemplo', 'DEX', NULL, 'OPERACIONAL', 'ATIVA', NULL);
INSERT INTO UNIDADE (codigo, nome, sigla, titular_titulo, tipo, situacao, unidade_superior_codigo) VALUES (11, 'Seção de Testes', 'ST', NULL, 'OPERACIONAL', 'ATIVA', NULL);
INSERT INTO UNIDADE (codigo, nome, sigla, titular_titulo, tipo, situacao, unidade_superior_codigo) VALUES (100, 'Unidade A', 'UA', NULL, 'OPERACIONAL', 'ATIVA', NULL);
INSERT INTO UNIDADE (codigo, nome, sigla, titular_titulo, tipo, situacao, unidade_superior_codigo) VALUES (101, 'Unidade B', 'UB', NULL, 'OPERACIONAL', 'ATIVA', NULL);

-- Usuários
INSERT INTO USUARIO (titulo, nome, email, ramal, unidade_codigo) VALUES ('USR001', 'João Exemplo', 'joao.exemplo@exemplo.gov.br', '1234', 10);
INSERT INTO USUARIO (titulo, nome, email, ramal, unidade_codigo) VALUES ('USR002', 'Maria Teste', 'maria.teste@exemplo.gov.br', '5678', 11);
INSERT INTO USUARIO (titulo, nome, email, ramal, unidade_codigo) VALUES ('USR100', 'Usuario Teste 100', 'user100@example.com', '1000', 100);
INSERT INTO USUARIO (titulo, nome, email, ramal, unidade_codigo) VALUES ('USR101', 'Usuario Teste 101', 'user101@example.com', '1001', 101);

-- Mapas
INSERT INTO MAPA (codigo, data_hora_disponibilizado, observacoes_disponibilizacao, sugestoes_apresentadas, data_hora_homologado) VALUES (100, TIMESTAMP '2025-10-02 12:00:00', 'Mapa inicial de exemplo', NULL, NULL);

-- Atividades
INSERT INTO ATIVIDADE (codigo, mapa_codigo, descricao) VALUES (1000, 100, 'Atividade de exemplo 1');
INSERT INTO ATIVIDADE (codigo, mapa_codigo, descricao) VALUES (1001, 100, 'Atividade de exemplo 2');

-- Competências
INSERT INTO COMPETENCIA (codigo, mapa_codigo, descricao) VALUES (2000, 100, 'Competência A');
INSERT INTO COMPETENCIA (codigo, mapa_codigo, descricao) VALUES (2001, 100, 'Competência B');

-- Competencia_Atividade
INSERT INTO COMPETENCIA_ATIVIDADE (atividade_codigo, competencia_codigo) VALUES (1000, 2000);
INSERT INTO COMPETENCIA_ATIVIDADE (atividade_codigo, competencia_codigo) VALUES (1001, 2001);

-- Conhecimentos
INSERT INTO CONHECIMENTO (codigo, atividade_codigo, descricao) VALUES (3000, 1000, 'Conhecimento X necessário');
INSERT INTO CONHECIMENTO (codigo, atividade_codigo, descricao) VALUES (3001, 1001, 'Conhecimento Y necessário');

-- Subprocessos
INSERT INTO SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, data_limite_etapa1, data_fim_etapa1, data_limite_etapa2, data_fim_etapa2, situacao_id) VALUES (4000, 1, 10, 100, DATE '2025-10-09', NULL, NULL, NULL, 'CRIADO');
INSERT INTO SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, data_limite_etapa1, data_fim_etapa1, data_limite_etapa2, data_fim_etapa2, situacao_id) VALUES (4001, 1, 100, NULL, DATE '2025-10-10', NULL, NULL, NULL, 'CRIADO'); -- Changed codigo from 1000 to 4001
INSERT INTO SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, data_limite_etapa1, data_fim_etapa1, data_limite_etapa2, data_fim_etapa2, situacao_id) VALUES (1001, 2, 101, NULL, DATE '2025-10-05', NULL, NULL, NULL, 'EM_ANDAMENTO');

-- Alertas (para USR100)
INSERT INTO ALERTA (codigo, descricao, data_criacao, usuario_destino_titulo, processo_codigo, subprocesso_codigo, tipo) VALUES (1, 'Alerta para USR100', TIMESTAMP '2025-10-02 11:00:00', 'USR100', 1, 1000, 'PROCESSO_CRIADO');