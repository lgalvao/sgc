-- Data for CDU03IntegrationTest
INSERT INTO SGC.UNIDADE (codigo, nome, sigla, situacao, tipo) VALUES (1, 'Unidade Teste 1', 'UT1', 'ATIVA', 'OPERACIONAL');
INSERT INTO SGC.UNIDADE (codigo, nome, sigla, situacao, tipo) VALUES (2, 'Unidade Teste 2', 'UT2', 'ATIVA', 'OPERACIONAL');

-- Mock user for @WithMockUser(roles="ADMIN")
INSERT INTO SGC.USUARIO (titulo, nome, email) VALUES ('user', 'Test User', 'user@test.com');
INSERT INTO SGC.ADMINISTRADOR (usuario_titulo) VALUES ('user');