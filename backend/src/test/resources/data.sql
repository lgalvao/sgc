-- Unidades
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES
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
