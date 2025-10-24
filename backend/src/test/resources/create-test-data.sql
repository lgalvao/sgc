-- Unidades
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES
(1, 'Secretaria de Documentação', 'SEDOC', 'INTEROPERACIONAL', NULL),
(100, 'ADMIN-UNIT', 'ADMIN-UNIT', 'INTEROPERACIONAL', NULL),
(101, 'GESTOR-UNIT', 'GESTOR-UNIT', 'INTERMEDIARIA', 100),
(102, 'SUB-UNIT', 'SUB-UNIT', 'OPERACIONAL', 101);

-- Usuários
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES
(111111111111, 'Admin Teste', 'admin.teste@tre-pe.jus.br', '1111', 100),
(222222222222, 'Gestor Teste', 'gestor.teste@tre-pe.jus.br', '2222', 101),
(333333333333, 'Chefe Teste', 'chefe.teste@tre-pe.jus.br', '3333', 102);

-- Perfis
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES
(111111111111, 'ADMIN'),
(222222222222, 'GESTOR'),
(333333333333, 'CHEFE');

-- Mapa Vigente (ID 200)
INSERT INTO SGC.MAPA (codigo, sugestoes) VALUES (200, 'Sugestões para o mapa vigente');
INSERT INTO SGC.COMPETENCIA (codigo, descricao, mapa_codigo) VALUES (300, 'Competência Vigente', 200);
INSERT INTO SGC.ATIVIDADE (codigo, descricao, mapa_codigo) VALUES (400, 'Atividade Vigente', 200);
INSERT INTO SGC.CONHECIMENTO (codigo, descricao, atividade_codigo) VALUES (500, 'Conhecimento Vigente', 400);
INSERT INTO SGC.COMPETENCIA_ATIVIDADE (competencia_codigo, atividade_codigo) VALUES (300, 400);

-- Mapa de Revisão (ID 201), cópia do Vigente
INSERT INTO SGC.MAPA (codigo, sugestoes) VALUES (201, 'Sugestões para o mapa de revisão');
INSERT INTO SGC.COMPETENCIA (codigo, descricao, mapa_codigo) VALUES (301, 'Competência Vigente', 201); -- Descrição igual
INSERT INTO SGC.ATIVIDADE (codigo, descricao, mapa_codigo) VALUES (401, 'Atividade Vigente', 201);   -- Descrição igual
INSERT INTO SGC.CONHECIMENTO (codigo, descricao, atividade_codigo) VALUES (501, 'Conhecimento Vigente', 401); -- Descrição igual
INSERT INTO SGC.COMPETENCIA_ATIVIDADE (competencia_codigo, atividade_codigo) VALUES (301, 401);

-- Associar o mapa vigente à unidade
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo, data_vigencia) VALUES (102, 200, '2023-01-01 00:00:00');
