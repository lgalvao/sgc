-- Unidades
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES
(2, 'Secretaria de Informática e Comunicações', 'STIC', 'INTEROPERACIONAL', NULL),
(3, 'Secretaria de Gestao de Pessoas', 'SGP', 'INTERMEDIARIA', 2),
(4, 'Coordenadoria de Educação Especial', 'COEDE', 'INTERMEDIARIA', 3),
(5, 'Seção Magistrados e Requisitados', 'SEMARE', 'OPERACIONAL', 4),
(6, 'Coordenadoria de Sistemas', 'COSIS', 'INTERMEDIARIA', 2),
(7, 'Coordenadoria de Suporte e Infraestrutura', 'COSINF', 'INTERMEDIARIA', 2),
(8, 'Seção de Desenvolvimento de Sistemas', 'SEDESENV', 'OPERACIONAL', 6),
(9, 'Seção de Dados e Inteligência Artificial', 'SEDIA', 'OPERACIONAL', 6),
(10, 'Seção de Sistemas Eleitorais', 'SESEL', 'OPERACIONAL', 6),
(11, 'Seção de Infraestrutura', 'SENIC', 'OPERACIONAL', 7),
(14, 'Coordenadoria Jurídica', 'COJUR', 'INTERMEDIARIA', 2),
(12, 'Seção Jurídica', 'SEJUR', 'OPERACIONAL', 14),
(13, 'Seção de Processos', 'SEPRO', 'OPERACIONAL', 14),
(15, 'Seção de Documentação', 'SEDOC', 'OPERACIONAL', 2),
-- Unidades para CDU14IntegrationTest
(100, 'ADMIN-UNIT', 'ADMIN-UNIT', 'INTEROPERACIONAL', NULL),
(101, 'GESTOR-UNIT', 'GESTOR-UNIT', 'INTERMEDIARIA', 100),
(102, 'SUB-UNIT', 'SUB-UNIT', 'OPERACIONAL', 101);

-- Unidades do mock
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (201, 'Seção de Desenvolvimento Organizacional e Capacitação', 'SEDOCAP', 'INTEROPERACIONAL', NULL);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (202, 'Secretaria de Informática e Comunicações', 'STIC', 'INTEROPERACIONAL', 201);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (203, 'Secretaria de Gestao de Pessoas', 'SGP', 'INTERMEDIARIA', 201);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (204, 'Coordenadoria de Educação Especial', 'COEDE', 'INTERMEDIARIA', 203);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (205, 'Seção Magistrados e Requisitados', 'SEMARE', 'OPERACIONAL', 204);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (206, 'Coordenadoria de Sistemas', 'COSIS', 'INTERMEDIARIA', 202);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (207, 'Coordenadoria de Suporte e Infraestrutura', 'COSINF', 'INTERMEDIARIA', 202);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (208, 'Seção de Desenvolvimento de Sistemas', 'SEDESENV', 'OPERACIONAL', 206);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (209, 'Seção de Dados e Inteligência Artificial', 'SEDIA', 'OPERACIONAL', 206);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (210, 'Seção de Sistemas Eleitorais', 'SESEL', 'OPERACIONAL', 206);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (211, 'Seção de Infraestrutura', 'SENIC', 'OPERACIONAL', 207);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (214, 'Coordenadoria Jurídica', 'COJUR', 'INTERMEDIARIA', 202);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (212, 'Seção Jurídica', 'SEJUR', 'OPERACIONAL', 214);
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (213, 'Seção de Processos', 'SEPRO', 'OPERACIONAL', 214);


-- Usuários
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES
(1, 'Ana Paula Souza', 'ana.souza@tre-pe.jus.br', '1234', 10),
(2, 'Carlos Henrique Lima', 'carlos.lima@tre-pe.jus.br', '2345', 3),
(3, 'Fernanda Oliveira', 'fernanda.oliveira@tre-pe.jus.br', '3456', 8),
(4, 'João Batista Silva', 'joao.silva@tre-pe.jus.br', '4567', 10),
(5, 'Marina Dias', 'marina.dias@tre-pe.jus.br', '5678', 5),
(6, 'Ricardo Alves', 'ricardo.alves@tre-pe.jus.br', '6789', 2),
(7, 'Zeca Silva', 'zeca.gado@tre-pe.jus.br', '7001', 2),
(8, 'Paulo Horta', 'paulo.horta@tre-pe.jus.br', '7002', 8),
(9, 'Giuseppe Corleone', 'giuseppe.corleone@tre-pe.jus.br', '7003', 8),
(10, 'Paula Gonçalves', 'paula.goncalves@tre-pe.jus.br', '7004', 9),
(11, 'Herman Greely', 'herman.greely@tre-pe.jus.br', '7005', 10),
(12, 'Taís Condida', 'tais.condida@tre-pe.jus.br', '7006', 11),
(13, 'Mike Smith', 'mike.smith@tre-pe.jus.br', '7007', 11),
(14, 'Maroca Silva', 'maroca.silva@tre-pe.jus.br', '7008', 2),
(15, 'Roberto Santos', 'roberto.santos@tre-pe.jus.br', '7009', 2),
(16, 'Luciana Pereira', 'luciana.pereira@tre-pe.jus.br', '7010', 6),
(17, 'Fernando Costa', 'fernando.costa@tre-pe.jus.br', '7011', 10),
(18, 'Amanda Rodrigues', 'amanda.rodrigues@tre-pe.jus.br', '7012', 14),
(19, 'Diego Fernandes', 'diego.fernandes@tre-pe.jus.br', '7013', 6),
(20, 'Juliana Almeida', 'juliana.almeida@tre-pe.jus.br', '7014', 2),
(21, 'Rafael Moreira', 'rafael.moreira@tre-pe.jus.br', '7015', 2),
(22, 'Camila Barbosa', 'camila.barbosa@tre-pe.jus.br', '7016', 10),
(23, 'Thiago Carvalho', 'thiago.carvalho@tre-pe.jus.br', '7017', 14),
(24, 'Patrícia Lima', 'patricia.lima@tre-pe.jus.br', '7018', 6),
(25, 'Lucas Mendes', 'lucas.mendes@tre-pe.jus.br', '7019', 2),
(26, 'Beatriz Santos', 'beatriz.santos@tre-pe.jus.br', '7020', 2),
(27, 'Gustavo Oliveira', 'gustavo.oliveira@tre-pe.jus.br', '7021', 10),
(28, 'Carolina Souza', 'carolina.souza@tre-pe.jus.br', '7022', 14),
(29, 'Bruno Rodrigues', 'bruno.rodrigues@tre-pe.jus.br', '7023', 6),
(30, 'Mariana Costa', 'mariana.costa@tre-pe.jus.br', '7024', 2),
-- Usuarios para CDU14IntegrationTest
(111111111111, 'Admin Teste', 'admin.teste@tre-pe.jus.br', '1111', 100),
(222222222222, 'Gestor Teste', 'gestor.teste@tre-pe.jus.br', '2222', 101),
(333333333333, 'Chefe Teste', 'chefe.teste@tre-pe.jus.br', '3333', 102);

-- Usuários do mock
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES
(201, 'Ana Paula Souza', 'ana.souza@tre-pe.jus.br', '1234', 210),
(202, 'Carlos Henrique Lima', 'carlos.lima@tre-pe.jus.br', '2345', 203),
(203, 'Fernanda Oliveira', 'fernanda.oliveira@tre-pe.jus.br', '3456', 208),
(204, 'João Batista Silva', 'joao.silva@tre-pe.jus.br', '4567', 210),
(205, 'Marina Dias', 'marina.dias@tre-pe.jus.br', '5678', 205),
(206, 'Ricardo Alves', 'ricardo.alves@tre-pe.jus.br', '6789', 201),
(207, 'Zeca Silva', 'zeca.gado@tre-pe.jus.br', '7001', 201),
(208, 'Paulo Horta', 'paulo.horta@tre-pe.jus.br', '7002', 208),
(209, 'Giuseppe Corleone', 'giuseppe.corleone@tre-pe.jus.br', '7003', 208),
(210, 'Paula Gonçalves', 'paula.goncalves@tre-pe.jus.br', '7004', 209),
(211, 'Herman Greely', 'herman.greely@tre-pe.jus.br', '7005', 210),
(212, 'Taís Condida', 'tais.condida@tre-pe.jus.br', '7006', 211),
(213, 'Mike Smith', 'mike.smith@tre-pe.jus.br', '7007', 211),
(214, 'Maroca Silva', 'maroca.silva@tre-pe.jus.br', '7008', 202),
(215, 'Roberto Santos', 'roberto.santos@tre-pe.jus.br', '7009', 214),
(216, 'Luciana Pereira', 'luciana.pereira@tre-pe.jus.br', '7010', 206),
(217, 'Fernando Costa', 'fernando.costa@tre-pe.jus.br', '7011', 210),
(218, 'Amanda Rodrigues', 'amanda.rodrigues@tre-pe.jus.br', '7012', 214),
(219, 'Diego Fernandes', 'diego.fernandes@tre-pe.jus.br', '7013', 206),
(220, 'Juliana Almeida', 'juliana.almeida@tre-pe.jus.br', '7014', 202),
(221, 'Rafael Moreira', 'rafael.moreira@tre-pe.jus.br', '7015', 201),
(222, 'Camila Barbosa', 'camila.barbosa@tre-pe.jus.br', '7016', 210),
(223, 'Thiago Carvalho', 'thiago.carvalho@tre-pe.jus.br', '7017', 214),
(224, 'Patrícia Lima', 'patricia.lima@tre-pe.jus.br', '7018', 206),
(225, 'Lucas Mendes', 'lucas.mendes@tre-pe.jus.br', '7019', 202),
(226, 'Beatriz Santos', 'beatriz.santos@tre-pe.jus.br', '7020', 201),
(227, 'Gustavo Oliveira', 'gustavo.oliveira@tre-pe.jus.br', '7021', 210),
(228, 'Carolina Souza', 'carolina.souza@tre-pe.jus.br', '7022', 214),
(229, 'Bruno Rodrigues', 'bruno.rodrigues@tre-pe.jus.br', '7023', 206),
(230, 'Mariana Costa', 'mariana.costa@tre-pe.jus.br', '7024', 202);


-- Perfis
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES
(1, 'SERVIDOR'),
(2, 'CHEFE'),
(3, 'CHEFE'),
(4, 'CHEFE'),
(5, 'CHEFE'),
(6, 'ADMIN'),
(7, 'CHEFE'),
(8, 'GESTOR'),
(9, 'CHEFE'),
(10, 'CHEFE'),
(11, 'SERVIDOR'),
(12, 'CHEFE'),
(13, 'GESTOR'),
(14, 'SERVIDOR'),
(15, 'SERVIDOR'),
(16, 'CHEFE'),
(17, 'CHEFE'),
(18, 'SERVIDOR'),
(19, 'SERVIDOR'),
(20, 'SERVIDOR'),
(21, 'SERVIDOR'),
(22, 'SERVIDOR'),
(23, 'SERVIDOR'),
(24, 'SERVIDOR'),
(25, 'SERVIDOR'),
(26, 'SERVIDOR'),
(27, 'SERVIDOR'),
(28, 'SERVIDOR'),
(29, 'SERVIDOR'),
(30, 'SERVIDOR'),
-- Perfis para CDU14IntegrationTest
(111111111111, 'ADMIN'),
(222222222222, 'GESTOR'),
(333333333333, 'CHEFE');

-- Usuário com múltiplos perfis para teste E2E
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES
(999999999999, 'Usuario Multi Perfil', 'multi.perfil@tre-pe.jus.br', '9999', 2); -- Associado à STIC

INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES
(999999999999, 'ADMIN'),
(999999999999, 'GESTOR');

-- Processos
INSERT INTO SGC.PROCESSO (codigo, data_criacao, data_limite, descricao, situacao, tipo) VALUES
(1, '2024-01-01 10:00:00', '2025-12-31 23:59:59', 'Processo de Teste Criado', 'CRIADO', 'MAPEAMENTO'),
(2, '2024-02-01 11:00:00', '2025-11-30 23:59:59', 'Processo de Teste Em Andamento', 'EM_ANDAMENTO', 'REVISAO');
