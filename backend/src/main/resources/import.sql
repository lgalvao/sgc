-- Unidades (nível raiz e intermediárias primeiro)
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (2, 'Secretaria de Informática e Comunicações', 'STIC', 'INTEROPERACIONAL', NULL);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (100, 'ADMIN-UNIT', 'ADMIN-UNIT', 'INTEROPERACIONAL', NULL);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (200, 'Secretaria de Gestao de Pessoas', 'SGP', 'INTERMEDIARIA', NULL);

-- Unidades intermediárias (dependem de raiz)
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (3, 'Coordenadoria de Administracao', 'COAD', 'INTERMEDIARIA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (6, 'Coordenadoria de Sistemas', 'COSIS', 'INTERMEDIARIA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (7, 'Coordenadoria de Suporte e Infraestrutura', 'COSINF', 'INTERMEDIARIA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (14, 'Coordenadoria Jurídica', 'COJUR', 'INTERMEDIARIA', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (101, 'GESTOR-UNIT', 'GESTOR-UNIT', 'INTERMEDIARIA', 100);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (201, 'Coordenadoria de Atenção ao Servidor', 'CAS', 'INTEROPERACIONAL', 200);

-- Unidades intermediárias (dependem de outras intermediárias)
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (4, 'Coordenadoria de Educação Especial', 'COEDE', 'INTERMEDIARIA', 3);

-- Unidades operacionais (dependem de intermediárias)
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (5, 'Seção Magistrados e Requisitados', 'SEMARE', 'OPERACIONAL', 4);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (8, 'Seção de Desenvolvimento de Sistemas', 'SEDESENV', 'OPERACIONAL', 6);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (9, 'Seção de Dados e Inteligência Artificial', 'SEDIA', 'OPERACIONAL', 6);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (10, 'Seção de Sistemas Eleitorais', 'SESEL', 'OPERACIONAL', 6);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (11, 'Seção de Infraestrutura', 'SENIC', 'OPERACIONAL', 7);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (12, 'Seção Jurídica', 'SEJUR', 'OPERACIONAL', 14);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (13, 'Seção de Processos', 'SEPRO', 'OPERACIONAL', 14);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (15, 'Seção de Documentação', 'SEDOC', 'OPERACIONAL', 2);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (102, 'SUB-UNIT', 'SUB-UNIT', 'OPERACIONAL', 101);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (202, 'Seção de Atenção ao Servidor', 'SAS', 'OPERACIONAL', 201);

-- Usuários
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (1, 'Ana Paula Souza', 'ana.souza@tre-pe.jus.br', '1234', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (2, 'Carlos Henrique Lima', 'carlos.lima@tre-pe.jus.br', '2345', 3);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (3, 'Fernanda Oliveira', 'fernanda.oliveira@tre-pe.jus.br', '3456', 8);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (4, 'João Batista Silva', 'joao.silva@tre-pe.jus.br', '4567', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (5, 'Marina Dias', 'marina.dias@tre-pe.jus.br', '5678', 5);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (6, 'Ricardo Alves', 'ricardo.alves@tre-pe.jus.br', '6789', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (7, 'Zeca Silva', 'zeca.gado@tre-pe.jus.br', '7001', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (8, 'Paulo Horta', 'paulo.horta@tre-pe.jus.br', '7002', 8);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (9, 'Giuseppe Corleone', 'giuseppe.corleone@tre-pe.jus.br', '7003', 8);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (10, 'Paula Gonçalves', 'paula.goncalves@tre-pe.jus.br', '7004', 9);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (11, 'Herman Greely', 'herman.greely@tre-pe.jus.br', '7005', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (12, 'Taís Condida', 'tais.condida@tre-pe.jus.br', '7006', 11);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (13, 'Mike Smith', 'mike.smith@tre-pe.jus.br', '7007', 11);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (14, 'Maroca Silva', 'maroca.silva@tre-pe.jus.br', '7008', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (15, 'Roberto Santos', 'roberto.santos@tre-pe.jus.br', '7009', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (16, 'Luciana Pereira', 'luciana.pereira@tre-pe.jus.br', '7010', 6);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (17, 'Fernando Costa', 'fernando.costa@tre-pe.jus.br', '7011', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (18, 'Amanda Rodrigues', 'amanda.rodrigues@tre-pe.jus.br', '7012', 14);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (19, 'Diego Fernandes', 'diego.fernandes@tre-pe.jus.br', '7013', 6);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (20, 'Juliana Almeida', 'juliana.almeida@tre-pe.jus.br', '7014', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (21, 'Rafael Moreira', 'rafael.moreira@tre-pe.jus.br', '7015', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (22, 'Camila Barbosa', 'camila.barbosa@tre-pe.jus.br', '7016', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (23, 'Thiago Carvalho', 'thiago.carvalho@tre-pe.jus.br', '7017', 14);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (24, 'Patrícia Lima', 'patricia.lima@tre-pe.jus.br', '7018', 6);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (25, 'Lucas Mendes', 'lucas.mendes@tre-pe.jus.br', '7019', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (26, 'Beatriz Santos', 'beatriz.santos@tre-pe.jus.br', '7020', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (27, 'Gustavo Oliveira', 'gustavo.oliveira@tre-pe.jus.br', '7021', 10);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (28, 'Carolina Souza', 'carolina.souza@tre-pe.jus.br', '7022', 14);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (29, 'Bruno Rodrigues', 'bruno.rodrigues@tre-pe.jus.br', '7023', 6);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (30, 'Mariana Costa', 'mariana.costa@tre-pe.jus.br', '7024', 2), -- Usuarios para CDU14IntegrationTest (111111111111, 'Admin Teste', 'admin.teste@tre-pe.jus.br', '1111', 100);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (222222222222, 'Gestor Teste', 'gestor.teste@tre-pe.jus.br', '2222', 101);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (333333333333, 'Chefe Teste', 'chefe.teste@tre-pe.jus.br', '3333', 102);

-- Processos de teste para E2E
-- Nota: Processos em situação CRIADO sem unidades para não bloquear testes
INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite) VALUES ('Mapeamento de competências - 2025', 'MAPEAMENTO', 'CRIADO', CURRENT_DATE, CURRENT_DATE + 365);

INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite) VALUES ('Revisão de mapa de competências STIC - 2024', 'REVISAO', 'EM_ANDAMENTO', CURRENT_DATE - 150, CURRENT_DATE + 60);

INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite) VALUES ('Mapeamento inicial - 2025', 'MAPEAMENTO', 'CRIADO', CURRENT_DATE - 30, CURRENT_DATE + 300);

-- Processo para CDU-05: usa ADMIN-UNIT (livre) em vez de STIC (bloqueada)
INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite) VALUES ('Processo teste revisão CDU-05', 'REVISAO', 'CRIADO', CURRENT_DATE, CURRENT_DATE + 60);

-- Processos EM_ANDAMENTO para testes de filtragem por unidade
INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite) VALUES ('Processo ADMIN-UNIT - Fora da STIC', 'MAPEAMENTO', 'EM_ANDAMENTO', CURRENT_DATE, CURRENT_DATE + 180);

-- Unidades participantes dos processos
-- Processo 2: STIC (código 2)
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo, nome, sigla, tipo, unidade_superior_codigo) 
  VALUES (2, 2, 'Secretaria de Informática e Comunicações', 'STIC', 'INTEROPERACIONAL', NULL);
-- Processo 4: ADMIN-UNIT (código 100) - para CDU-05
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo, nome, sigla, tipo, unidade_superior_codigo) 
  VALUES (4, 100, 'ADMIN-UNIT', 'ADMIN-UNIT', 'INTEROPERACIONAL', NULL);
-- Processo 5: ADMIN-UNIT (código 100)
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo, nome, sigla, tipo, unidade_superior_codigo) 
  VALUES (5, 100, 'ADMIN-UNIT', 'ADMIN-UNIT', 'INTEROPERACIONAL', NULL);

-- Usuários adicionais para testes E2E (antes dos perfis!)
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (777, 'Chefe STIC Teste', 'chefe.stic@tre-pe.jus.br', '7777', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (999999999999, 'Usuario Multi Perfil', 'multi.perfil@tre-pe.jus.br', '9999', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (123456789012, 'João Silva', 'joao.silva@tre-pe.jus.br', '8001', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (987654321098, 'Maria Santos', 'maria.santos@tre-pe.jus.br', '8002', 2);
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (111222333444, 'Pedro Oliveira', 'pedro.oliveira@tre-pe.jus.br', '8003', 2);

-- ============================================================================
-- DADOS DE TESTE PARA PROCESSOS DE REVISAO E DIAGNOSTICO
-- ============================================================================

-- 1. Criar um processo de MAPEAMENTO FINALIZADO para gerar mapas vigentes
INSERT INTO SGC.PROCESSO (codigo, descricao, tipo, situacao, data_criacao, data_finalizacao, data_limite) 
VALUES (100, 'Mapeamento Base 2024 - FINALIZADO', 'MAPEAMENTO', 'FINALIZADO', CURRENT_DATE - 180, CURRENT_DATE - 30, CURRENT_DATE - 60);

-- 2. Unidades participantes do processo 100
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo, nome, sigla, tipo, unidade_superior_codigo) 
VALUES (100, 8, 'Seção de Desenvolvimento de Sistemas', 'SEDESENV', 'OPERACIONAL', 6);

INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo, nome, sigla, tipo, unidade_superior_codigo) 
VALUES (100, 9, 'Seção de Dados e Inteligência Artificial', 'SEDIA', 'OPERACIONAL', 6);

INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo, nome, sigla, tipo, unidade_superior_codigo) 
VALUES (100, 10, 'Seção de Sistemas Eleitorais', 'SESEL', 'OPERACIONAL', 6);

-- 3. Criar mapas para essas unidades
INSERT INTO SGC.MAPA (codigo) VALUES (1001);
INSERT INTO SGC.MAPA (codigo) VALUES (1002);
INSERT INTO SGC.MAPA (codigo) VALUES (1003);

-- 4. Criar subprocessos para as unidades (ligando ao processo 100 e aos mapas)
INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, situacao, data_limite_etapa1, data_fim_etapa1, data_fim_etapa2) 
VALUES (1001, 100, 8, 1001, 'HOMOLOGADO', CURRENT_DATE - 150, CURRENT_DATE - 120, CURRENT_DATE - 90);

INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, situacao, data_limite_etapa1, data_fim_etapa1, data_fim_etapa2) 
VALUES (1002, 100, 9, 1002, 'HOMOLOGADO', CURRENT_DATE - 150, CURRENT_DATE - 120, CURRENT_DATE - 90);

INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, mapa_codigo, situacao, data_limite_etapa1, data_fim_etapa1, data_fim_etapa2) 
VALUES (1003, 100, 10, 1003, 'HOMOLOGADO', CURRENT_DATE - 150, CURRENT_DATE - 120, CURRENT_DATE - 90);

-- 5. Configurar mapas vigentes para as unidades (tabela UNIDADE_MAPA)
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES (8, 1001);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES (9, 1002);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES (10, 1003);

-- 6. Adicionar competências aos mapas para torná-los mais realistas
INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao, tipo, nivel, essencial) 
VALUES (10001, 1001, 'Desenvolvimento em Java', 'TECNICA', 'AVANCADO', TRUE);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao, tipo, nivel, essencial) 
VALUES (10002, 1001, 'Desenvolvimento em Vue.js', 'TECNICA', 'INTERMEDIARIO', TRUE);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao, tipo, nivel, essencial) 
VALUES (10003, 1002, 'Análise de Dados', 'TECNICA', 'AVANCADO', TRUE);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao, tipo, nivel, essencial) 
VALUES (10004, 1002, 'Machine Learning', 'TECNICA', 'INTERMEDIARIO', FALSE);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao, tipo, nivel, essencial) 
VALUES (10005, 1003, 'Segurança da Informação', 'TECNICA', 'AVANCADO', TRUE);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao, tipo, nivel, essencial) 
VALUES (10006, 1003, 'Gestão de Projetos', 'COMPORTAMENTAL', 'INTERMEDIARIO', TRUE);

-- 7. Criar servidores para as unidades 8, 9, 10 (necessário para DIAGNOSTICO)
INSERT INTO SGC.SERVIDOR (titulo_eleitoral, nome, cargo, unidade_codigo) 
VALUES (50001, 'João da Silva', 'Analista de Sistemas', 8);

INSERT INTO SGC.SERVIDOR (titulo_eleitoral, nome, cargo, unidade_codigo) 
VALUES (50002, 'Maria Oliveira', 'Desenvolvedora', 8);

INSERT INTO SGC.SERVIDOR (titulo_eleitoral, nome, cargo, unidade_codigo) 
VALUES (50003, 'Pedro Santos', 'Analista de Dados', 9);

INSERT INTO SGC.SERVIDOR (titulo_eleitoral, nome, cargo, unidade_codigo) 
VALUES (50004, 'Ana Costa', 'Cientista de Dados', 9);

INSERT INTO SGC.SERVIDOR (titulo_eleitoral, nome, cargo, unidade_codigo) 
VALUES (50005, 'Carlos Pereira', 'Especialista em Segurança', 10);

INSERT INTO SGC.SERVIDOR (titulo_eleitoral, nome, cargo, unidade_codigo) 
VALUES (50006, 'Juliana Lima', 'Gerente de Projetos', 10);

-- ============================================================================
-- RESUMO DOS DADOS CRIADOS:
-- - Processo 100: MAPEAMENTO FINALIZADO (SEDESENV, SEDIA, SESEL)
-- - Mapas vigentes: 1001 (SEDESENV), 1002 (SEDIA), 1003 (SESEL)
-- - Servidores cadastrados nas 3 unidades
-- 
-- AGORA É POSSÍVEL:
-- - Criar processo REVISAO incluindo unidades 8, 9, 10 (têm mapa vigente)
-- - Criar processo DIAGNOSTICO incluindo unidades 8, 9, 10 (têm mapa + servidores)
-- ============================================================================


-- Perfis dos usuários (AUTORIZAÇÕES)
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (1, 'SERVIDOR');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (2, 'CHEFE');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (3, 'CHEFE');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (6, 'ADMIN');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (8, 'GESTOR');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (777, 'CHEFE');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (111111111111, 'ADMIN');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (222222222222, 'GESTOR');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (333333333333, 'CHEFE');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (999999999999, 'ADMIN');
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (999999999999, 'GESTOR');

