INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (1, 'Tribunal Regional Eleitoral', 'TRE', 'INTEROPERACIONAL', 'ATIVA', NULL);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (2, 'Secretaria de Informática e Comunicações', 'STIC', 'INTEROPERACIONAL', 'ATIVA', NULL);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (100, 'ADMIN-UNIT', 'ADMIN-UNIT', 'INTEROPERACIONAL', 'ATIVA', NULL);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (200, 'Secretaria de Gestao de Pessoas', 'SGP', 'INTERMEDIARIA', 'ATIVA', NULL);

INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (3, 'Coordenadoria de Administracao', 'COAD', 'INTERMEDIARIA', 'ATIVA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (6, 'Coordenadoria de Sistemas', 'COSIS', 'INTERMEDIARIA', 'ATIVA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (7, 'Coordenadoria de Suporte e Infraestrutura', 'COSINF', 'INTERMEDIARIA', 'ATIVA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (14, 'Coordenadoria Jurídica', 'COJUR', 'INTERMEDIARIA', 'ATIVA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (101, 'GESTOR-UNIT', 'GESTOR-UNIT', 'INTERMEDIARIA', 'ATIVA', 100);

INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (201, 'Coordenadoria de Atenção ao Servidor', 'CAS', 'INTEROPERACIONAL', 'ATIVA', 200);

INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (4, 'Coordenadoria de Educação Especial', 'COEDE', 'INTERMEDIARIA', 'ATIVA', 3);

INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (5, 'Seção Magistrados e Requisitados', 'SEMARE', 'OPERACIONAL', 'ATIVA', 4);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (8, 'Seção de Desenvolvimento de Sistemas', 'SEDESENV', 'OPERACIONAL', 'ATIVA', 6);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (9, 'Seção de Dados e Inteligência Artificial', 'SEDIA', 'OPERACIONAL', 'ATIVA', 6);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (10, 'Seção de Sistemas Eleitorais', 'SESEL', 'OPERACIONAL', 'ATIVA', 6);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (11, 'Seção de Infraestrutura', 'SENIC', 'OPERACIONAL', 'ATIVA', 7);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (12, 'Seção Jurídica', 'SEJUR', 'OPERACIONAL', 'ATIVA', 14);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (13, 'Seção de Processos', 'SEPRO', 'OPERACIONAL', 'ATIVA', 14);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (15, 'Seção de Documentação', 'SEDOC', 'OPERACIONAL', 'ATIVA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (102, 'SUB-UNIT', 'SUB-UNIT', 'OPERACIONAL', 'ATIVA', 101);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (202, 'Seção de Atenção ao Servidor', 'SAS', 'OPERACIONAL', 'ATIVA', 201);

INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (900, 'CDU04-UNIT', 'CDU04-UNIT', 'OPERACIONAL', 'ATIVA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (901, 'CDU05-REV-UNIT', 'CDU05-REV-UNIT', 'OPERACIONAL', 'ATIVA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (902, 'CDU05-SUB-UNIT', 'CDU05-SUB-UNIT', 'OPERACIONAL', 'ATIVA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (903, 'CDU05-ALERT-UNIT', 'CDU05-ALERT-UNIT', 'OPERACIONAL', 'ATIVA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo)
VALUES (904, 'CDU05-READONLY-UNIT', 'CDU05-READONLY-UNIT', 'OPERACIONAL', 'ATIVA', 2);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (1, 'Ana Paula Souza', 'ana.souza@tre-pe.jus.br', '1234', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (2, 'Carlos Henrique Lima', 'carlos.lima@tre-pe.jus.br', '2345', 3);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (3, 'Fernanda Oliveira', 'fernanda.oliveira@tre-pe.jus.br', '3456', 8);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (4, 'João Batista Silva', 'joao.silva@tre-pe.jus.br', '4567', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (5, 'Marina Dias', 'marina.dias@tre-pe.jus.br', '5678', 5);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (6, 'Ricardo Alves', 'ricardo.alves@tre-pe.jus.br', '6789', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (7, 'Zeca Silva', 'zeca.gado@tre-pe.jus.br', '7001', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (8, 'Paulo Horta', 'paulo.horta@tre-pe.jus.br', '7002', 8);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (9, 'Giuseppe Corleone', 'giuseppe.corleone@tre-pe.jus.br', '7003', 8);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (10, 'Paula Gonçalves', 'paula.goncalves@tre-pe.jus.br', '7004', 9);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (11, 'Herman Greely', 'herman.greely@tre-pe.jus.br', '7005', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (12, 'Taís Condida', 'tais.condida@tre-pe.jus.br', '7006', 11);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (13, 'Mike Smith', 'mike.smith@tre-pe.jus.br', '7007', 11);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (14, 'Maroca Silva', 'maroca.silva@tre-pe.jus.br', '7008', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (15, 'Roberto Santos', 'roberto.santos@tre-pe.jus.br', '7009', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (16, 'Luciana Pereira', 'luciana.pereira@tre-pe.jus.br', '7010', 6);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (17, 'Fernando Costa', 'fernando.costa@tre-pe.jus.br', '7011', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (18, 'Amanda Rodrigues', 'amanda.rodrigues@tre-pe.jus.br', '7012', 14);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (19, 'Diego Fernandes', 'diego.fernandes@tre-pe.jus.br', '7013', 6);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (20, 'Juliana Almeida', 'juliana.almeida@tre-pe.jus.br', '7014', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (21, 'Rafael Moreira', 'rafael.moreira@tre-pe.jus.br', '7015', 2);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (22, 'Camila Barbosa', 'camila.barbosa@tre-pe.jus.br', '7016', 10);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (23, 'Thiago Carvalho', 'thiago.carvalho@tre-pe.jus.br', '7017', 14);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (24, 'Patrícia Lima', 'patricia.lima@tre-pe.jus.br', '7018', 6);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (25, 'Lucas Mendes', 'lucas.mendes@tre-pe.jus.br', '7019', 2);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (26, 'Beatriz Santos', 'beatriz.santos@tre-pe.jus.br', '7020', 2);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (27, 'Gustavo Oliveira', 'gustavo.oliveira@tre-pe.jus.br', '7021', 10);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (28, 'Carolina Souza', 'carolina.souza@tre-pe.jus.br', '7022', 14);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (29, 'Bruno Rodrigues', 'bruno.rodrigues@tre-pe.jus.br', '7023', 6);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (30, 'Mariana Costa', 'mariana.costa@tre-pe.jus.br', '7024',
        2);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (111111111111, 'Admin Teste', 'admin.teste@tre-pe.jus.br', '1111', 100);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (222222222222, 'Gestor Teste', 'gestor.teste@tre-pe.jus.br', '2222', 101);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (666666666666, 'Gestor COSIS', 'gestor.cosis@tre-pe.jus.br', '6666', 6);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (333333333333, 'Chefe Teste', 'chefe.teste@tre-pe.jus.br', '3333', 8);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (121212121212, 'Chefe SEJUR Teste', 'chefe.sejur@tre-pe.jus.br', '1212', 12);

INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite)
VALUES ('Mapeamento de competências - 2025', 'MAPEAMENTO', 'CRIADO', CURRENT_DATE, CURRENT_DATE + 365);

INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite)
VALUES ('Revisão de mapa de competências STIC - 2024', 'REVISAO', 'EM_ANDAMENTO', CURRENT_DATE - 150,
        CURRENT_DATE + 60);

INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite)
VALUES ('Mapeamento inicial - 2025', 'MAPEAMENTO', 'CRIADO', CURRENT_DATE - 30, CURRENT_DATE + 300);

INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite)
VALUES ('Processo teste revisão CDU-05', 'REVISAO', 'CRIADO', CURRENT_DATE, CURRENT_DATE + 60);

INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite)
VALUES ('Processo ADMIN-UNIT - Fora da STIC', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_DATE, CURRENT_DATE + 180);

INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (2, 2);
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (4, 100);
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (5, 9);

INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (777, 'Chefe STIC Teste', 'chefe.stic@tre-pe.jus.br', '7777', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (999999999999, 'Usuario Multi Perfil', 'multi.perfil@tre-pe.jus.br', '9999', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (123456789012, 'João Silva', 'joao.silva@tre-pe.jus.br', '8001', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (987654321098, 'Maria Santos', 'maria.santos@tre-pe.jus.br', '8002', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo)
VALUES (111222333444, 'Pedro Oliveira', 'pedro.oliveira@tre-pe.jus.br', '8003', 2);

INSERT INTO SGC.PROCESSO (codigo, descricao, tipo, situacao, data_criacao, data_finalizacao, data_limite)
VALUES (100, 'Mapeamento Base 2024 - FINALIZADO', 'MAPEAMENTO', 'FINALIZADO', CURRENT_DATE - 180, CURRENT_DATE - 30,
        CURRENT_DATE - 60);

INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (100, 8);
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (100, 9);
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (100, 10);
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (100, 102);

INSERT INTO SGC.MAPA (codigo)
VALUES (1001);
INSERT INTO SGC.MAPA (codigo)
VALUES (1002);
INSERT INTO SGC.MAPA (codigo)
VALUES (1003);
INSERT INTO SGC.MAPA (codigo)
VALUES (1004);

INSERT INTO SGC.MAPA (codigo)
VALUES (201);

INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, situacao_id, data_limite_etapa1,
                             data_fim_etapa1, data_limite_etapa2, data_fim_etapa2)
VALUES (1001, 100, 8, 1001, 'MAPA_HOMOLOGADO', CURRENT_DATE - 150, CURRENT_DATE - 120, CURRENT_DATE - 100,
        CURRENT_DATE - 90);

INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, situacao_id, data_limite_etapa1,
                             data_fim_etapa1, data_limite_etapa2, data_fim_etapa2)
VALUES (1002, 100, 9, 1002, 'MAPA_HOMOLOGADO', CURRENT_DATE - 150, CURRENT_DATE - 120, CURRENT_DATE - 100,
        CURRENT_DATE - 90);

INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, situacao_id, data_limite_etapa1,
                             data_fim_etapa1, data_limite_etapa2, data_fim_etapa2)
VALUES (1003, 100, 10, 1003, 'MAPA_HOMOLOGADO', CURRENT_DATE - 150, CURRENT_DATE - 120, CURRENT_DATE - 100,
        CURRENT_DATE - 90);

INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, situacao_id, data_limite_etapa1,
                             data_fim_etapa1, data_limite_etapa2, data_fim_etapa2)
VALUES (1004, 100, 102, 1004, 'MAPA_HOMOLOGADO', CURRENT_DATE - 150, CURRENT_DATE - 120, CURRENT_DATE - 100,
        CURRENT_DATE - 90);

INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo)
VALUES (9, 1002);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo)
VALUES (10, 1003);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo)
VALUES (102, 1004);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo)
VALUES (100, 1004); -- ADMIN-UNIT também tem mapa vigente para testes CDU-05
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo)
VALUES (101, 1004); -- GESTOR-UNIT também tem mapa vigente para testes CDU-05
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo)
VALUES (901, 1004); -- CDU05-REV-UNIT tem mapa vigente
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo)
VALUES (902, 1004); -- CDU05-SUB-UNIT tem mapa vigente
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo)
VALUES (903, 1004); -- CDU05-ALERT-UNIT tem mapa vigente
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo)
VALUES (904, 1004); -- CDU05-READONLY-UNIT tem mapa vigente

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES (10001, 1001, 'Desenvolvimento em Java');

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES (10002, 1001, 'Desenvolvimento em Vue.js');

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES (10003, 1002, 'Análise de Dados');

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES (10004, 1002, 'Machine Learning');

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES (10005, 1003, 'Segurança da Informação');

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES (10006, 1003, 'Gestão de Projetos');

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES (10007, 1004, 'Gestão Administrativa');

INSERT INTO SGC.ATIVIDADE (codigo, mapa_codigo, descricao)
VALUES (30000, 1004, 'Realizar atendimento presencial');

INSERT INTO SGC.CONHECIMENTO (codigo, atividade_codigo, descricao)
VALUES (40000, 30000, 'Atendimento ao público');

INSERT INTO SGC.COMPETENCIA_ATIVIDADE (atividade_codigo, competencia_codigo)
VALUES (30000, 10007);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES (20001, 201, 'Gestão Administrativa');

INSERT INTO SGC.ATIVIDADE (codigo, mapa_codigo, descricao)
VALUES (30001, 201, 'Realizar atendimento presencial');

INSERT INTO SGC.CONHECIMENTO (codigo, atividade_codigo, descricao)
VALUES (40001, 30001, 'Atendimento ao público');

INSERT INTO SGC.COMPETENCIA_ATIVIDADE (atividade_codigo, competencia_codigo)
VALUES (30001, 20001);

INSERT INTO SGC.USUARIO (titulo_eleitoral, nome, unidade_codigo)
VALUES (50001, 'João da Silva', 8);

INSERT INTO SGC.USUARIO (titulo_eleitoral, nome, unidade_codigo)
VALUES (50002, 'Maria Oliveira', 8);

INSERT INTO SGC.USUARIO (titulo_eleitoral, nome, unidade_codigo)
VALUES (50003, 'Pedro Santos', 9);

INSERT INTO SGC.USUARIO (titulo_eleitoral, nome, unidade_codigo)
VALUES (50004, 'Ana Costa', 9);

INSERT INTO SGC.USUARIO (titulo_eleitoral, nome, unidade_codigo)
VALUES (50005, 'Carlos Pereira', 10);

INSERT INTO SGC.usuario (titulo_eleitoral, nome, unidade_codigo)
VALUES (50006, 'Juliana Lima', 10);

INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (1, 'SERVIDOR');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (2, 'CHEFE');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (3, 'CHEFE');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (6, 'ADMIN');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (8, 'GESTOR');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (777, 'CHEFE');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (111111111111, 'ADMIN');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (222222222222, 'GESTOR');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (333333333333, 'CHEFE');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (121212121212, 'CHEFE');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (666666666666, 'GESTOR');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (999999999999, 'ADMIN');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil)
VALUES (999999999999, 'GESTOR');

UPDATE SGC.UNIDADE
SET titular_titulo = 777
WHERE codigo = 2; -- STIC
UPDATE SGC.UNIDADE
SET titular_titulo = 666666666666
WHERE codigo = 6; -- COSIS
UPDATE SGC.UNIDADE
SET titular_titulo = 2
WHERE codigo = 3; -- COAD
UPDATE SGC.UNIDADE
SET titular_titulo = 333333333333
WHERE codigo = 8; -- SEDESENV (for CDU09 tests)
UPDATE SGC.UNIDADE
SET titular_titulo = 333333333333
WHERE codigo = 9; -- SEDIA (for CDU19 tests)
UPDATE SGC.UNIDADE
SET titular_titulo = 333333333333
WHERE codigo = 10; -- SESEL (for CDU10 tests)
UPDATE SGC.UNIDADE
SET titular_titulo = 12
WHERE codigo = 11; -- SENIC
UPDATE SGC.UNIDADE
SET titular_titulo = 121212121212
WHERE codigo = 12; -- SEJUR
UPDATE SGC.UNIDADE
SET titular_titulo = 111111111111
WHERE codigo = 100; -- ADMIN-UNIT
UPDATE SGC.UNIDADE
SET titular_titulo = 222222222222
WHERE codigo = 101; -- GESTOR-UNIT
UPDATE SGC.UNIDADE
SET titular_titulo = 333333333333
WHERE codigo = 102; -- SUB-UNIT

INSERT INTO SGC.PROCESSO (codigo, descricao, tipo, situacao, data_criacao, data_limite)
VALUES (200, 'Processo da Raiz CDU02', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_DATE, CURRENT_DATE + 30);
INSERT INTO SGC.PROCESSO (codigo, descricao, tipo, situacao, data_criacao, data_limite)
VALUES (201, 'Processo da Filha 1 CDU02', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_DATE, CURRENT_DATE + 30);
INSERT INTO SGC.PROCESSO (codigo, descricao, tipo, situacao, data_criacao, data_limite)
VALUES (202, 'Processo da Neta 1 CDU02', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_DATE, CURRENT_DATE + 30);
INSERT INTO SGC.PROCESSO (codigo, descricao, tipo, situacao, data_criacao, data_limite)
VALUES (203, 'Processo Criado CDU02', 'MAPEAMENTO', 'CRIADO', CURRENT_DATE, CURRENT_DATE + 30);
INSERT INTO SGC.PROCESSO (codigo, descricao, tipo, situacao, data_criacao, data_limite)
VALUES (205, 'Processo ADMIN-UNIT - Fora da STIC', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_DATE, CURRENT_DATE + 30);

INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (200, 2);
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (201, 6);
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (202, 8);
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (203, 2);
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (205, 100);

INSERT INTO SGC.ALERTA (descricao, processo_codigo, usuario_destino_titulo, data_hora)
VALUES ('Alerta para Gestor', 200, 8, CURRENT_TIMESTAMP);
INSERT INTO SGC.ALERTA (descricao, processo_codigo, unidade_destino_codigo, data_hora)
VALUES ('Alerta para Unidade Filha 1', 201, 6, CURRENT_TIMESTAMP);