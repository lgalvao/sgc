-- Deleta os dados existentes para garantir um estado limpo
DELETE FROM sgc.alerta;
DELETE FROM sgc.subprocesso;
DELETE FROM sgc.unidade_processo;
DELETE FROM sgc.processo;

-- Insere os processos necessários para os testes de visibilidade
-- Processo 1 (Raiz): Visível para GESTOR da Unidade Raiz e ADMIN
INSERT INTO sgc.processo (codigo, descricao, tipo, situacao, data_criacao, data_limite) VALUES (100, 'Processo da Raiz', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + 30);
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo) VALUES (100, 2);

-- Processo 2 (Filha 1): Visível para GESTOR da Unidade Raiz, CHEFE da Filha 1 e ADMIN
INSERT INTO sgc.processo (codigo, descricao, tipo, situacao, data_criacao, data_limite) VALUES (101, 'Processo da Filha 1', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + 30);
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo) VALUES (101, 6);

-- Processo 3 (Neta 1): Visível para GESTOR da Raiz, CHEFE da Filha 1 e ADMIN
INSERT INTO sgc.processo (codigo, descricao, tipo, situacao, data_criacao, data_limite) VALUES (102, 'Processo da Neta 1', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + 30);
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo) VALUES (102, 8);

-- Processo 4 (Criado): Visível apenas para ADMIN
INSERT INTO sgc.processo (codigo, descricao, tipo, situacao, data_criacao, data_limite) VALUES (103, 'Processo Criado', 'MAPEAMENTO', 'CRIADO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + 30);
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo) VALUES (103, 2);
