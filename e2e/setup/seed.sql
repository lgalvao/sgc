INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (1, 'Seção de Desenvolvimento e Capacitação', 'SEDOC', 'INTEROPERACIONAL', 'ATIVA', NULL);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (2, 'Secretaria 1', 'SECRETARIA_1', 'INTEROPERACIONAL', 'ATIVA', 1);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (3, 'Assessoria 11', 'ASSESSORIA_11', 'OPERACIONAL', 'ATIVA', 2);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (4, 'Assessoria 12', 'ASSESSORIA_12', 'OPERACIONAL', 'ATIVA', 2);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (5, 'Coordenadoria 11', 'COORD_11', 'INTERMEDIARIA', 'ATIVA', 2);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (6, 'Seção 111', 'SECAO_111', 'OPERACIONAL', 'ATIVA', 5);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (7, 'Seção 112', 'SECAO_112', 'OPERACIONAL', 'ATIVA', 5);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (8, 'Seção 113', 'SECAO_113', 'OPERACIONAL', 'ATIVA', 5);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (9, 'Coordenadoria 12', 'COORD_12', 'INTERMEDIARIA', 'ATIVA', 2);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (10, 'Seção 121', 'SECAO_121', 'OPERACIONAL', 'ATIVA', 9);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (11, 'Secretaria 2', 'SECRETARIA_2', 'INTEROPERACIONAL', 'ATIVA', 1);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (12, 'Assessoria 21', 'ASSESSORIA_21', 'OPERACIONAL', 'ATIVA', 11);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (13, 'Assessoria 22', 'ASSESSORIA_22', 'OPERACIONAL', 'ATIVA', 11);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (14, 'Coordenadoria 21', 'COORD_21', 'INTERMEDIARIA', 'ATIVA', 11);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (15, 'Seção 211', 'SECAO_211', 'OPERACIONAL', 'ATIVA', 14);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (16, 'Seção 212', 'SECAO_212', 'OPERACIONAL', 'ATIVA', 14);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (17, 'Coordenadoria 22', 'COORD_22', 'INTERMEDIARIA', 'ATIVA', 11);
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (18, 'Seção 221', 'SECAO_221', 'OPERACIONAL', 'ATIVA', 17);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('111111', 'ADMIN_SEDOC_E_CHEFE_SEDOC', 'admin_sedoc_e_chefe_sedoc@tre-pe.jus.br', NULL, 1);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('111111', 'ADMIN', 1);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('111111', 'CHEFE', 1);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('191919', 'ADMIN_1_PERFIL', 'admin_unico@tre-pe.jus.br', NULL, 1);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('191919', 'ADMIN', 1);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('222222', 'GESTOR_COORD_11', 'gestor_coord_11@tre-pe.jus.br', NULL, 5);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('222222', 'GESTOR', 5);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('333333', 'CHEFE_SECAO_111', 'chefe_secao_111@tre-pe.jus.br', NULL, 6);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('333333', 'CHEFE', 6);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('444444', 'SERVIDOR_SECAO_111_E_CHEFE_SECAO_112', 'servidor_secao_111_e_chefe_secao_112@tre-pe.jus.br', NULL, 6);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('444444', 'SERVIDOR', 6);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('444444', 'CHEFE', 7);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('555555', 'David Bowie', 'david.bowie@tre-pe.jus.br', NULL, 3);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('555555', 'CHEFE', 3);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('666666', 'Robert Plant', 'robert.plant@tre-pe.jus.br', NULL, 1);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('666666', 'ADMIN', 1);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('666666', 'SERVIDOR', 1);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('777777', 'Janis Joplin', 'janis.joplin@tre-pe.jus.br', NULL, 12);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('777777', 'CHEFE', 12);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('888888', 'Jimi Hendrix', 'jimi.hendrix@tre-pe.jus.br', NULL, 13);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('888888', 'CHEFE', 13);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('999999', 'Roger Waters', 'roger.waters@tre-pe.jus.br', NULL, 14);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('999999', 'GESTOR', 14);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('101010', 'Debbie Harry', 'debbie.harry@tre-pe.jus.br', NULL, 15);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('101010', 'CHEFE', 15);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('121212', 'Steven Tyler', 'steven.tyler@tre-pe.jus.br', NULL, 16);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('121212', 'SERVIDOR', 16);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('131313', 'Mick Jagger', 'mick.jagger@tre-pe.jus.br', NULL, 17);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('131313', 'GESTOR', 17);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('141414', 'Tina Turner', 'tina.turner@tre-pe.jus.br', NULL, 18);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('141414', 'CHEFE', 18);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('151515', 'Axl Rose', 'axl.rose@tre-pe.jus.br', NULL, 4);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('151515', 'CHEFE', 4);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('171717', 'Lemmy Kilmister', 'lemmy.kilmister@tre-pe.jus.br', NULL, 10);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('171717', 'CHEFE', 10);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('181818', 'Pete Townshend', 'pete.townshend@tre-pe.jus.br', NULL, 16);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('181818', 'CHEFE', 16);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('202020', 'John Lennon', 'john.lennon@tre-pe.jus.br', NULL, 2);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('202020', 'CHEFE', 2);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('212121', 'George Harrison', 'george.harrison@tre-pe.jus.br', NULL, 11);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('212121', 'CHEFE', 11);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('222223', 'Ringo Starr', 'ringo.starr@tre-pe.jus.br', NULL, 9);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('222223', 'GESTOR', 9);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('232323', 'Bon Jovi', 'bon.jovi@tre-pe.jus.br', NULL, 3);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('232323', 'SERVIDOR', 3);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('242424', 'Jon Lord', 'jon.lord@tre-pe.jus.br', NULL, 4);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('242424', 'SERVIDOR', 4);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('252525', 'Bruce Springsteen', 'bruce.springsteen@tre-pe.jus.br', NULL, 7);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('252525', 'SERVIDOR', 7);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('262626', 'Chuck Berry', 'chuck.berry@tre-pe.jus.br', NULL, 10);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('262626', 'SERVIDOR', 10);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('272727', 'Elton John', 'elton.john@tre-pe.jus.br', NULL, 12);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('272727', 'SERVIDOR', 12);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('282828', 'Eric Clapton', 'eric.clapton@tre-pe.jus.br', NULL, 15);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('282828', 'SERVIDOR', 15);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('292929', 'Flea', 'flea@tre-pe.jus.br', NULL, 18);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('292929', 'SERVIDOR', 18);

-- Inserir Mapa vigente para Assessoria 12 (Unit 4) para testes de Revisão
INSERT INTO sgc.mapa (codigo, data_hora_disponibilizado, data_hora_homologado, unidade_codigo, sugestoes_apresentadas) VALUES (99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4, false);
UPDATE sgc.unidade SET mapa_vigente_codigo = 99, data_vigencia_mapa_atual = CURRENT_TIMESTAMP WHERE codigo = 4;

-- Atualizar titulares das unidades depois da criação dos usuários
UPDATE sgc.unidade SET titular_titulo = '111111' WHERE codigo = 1;
UPDATE sgc.unidade SET titular_titulo = '202020' WHERE codigo = 2;
UPDATE sgc.unidade SET titular_titulo = '555555' WHERE codigo = 3;
UPDATE sgc.unidade SET titular_titulo = '151515' WHERE codigo = 4;
UPDATE sgc.unidade SET titular_titulo = '222222' WHERE codigo = 5;
UPDATE sgc.unidade SET titular_titulo = '333333' WHERE codigo = 6;
UPDATE sgc.unidade SET titular_titulo = '444444' WHERE codigo = 7;
UPDATE sgc.unidade SET titular_titulo = '222223' WHERE codigo = 9;
UPDATE sgc.unidade SET titular_titulo = '171717' WHERE codigo = 10;
UPDATE sgc.unidade SET titular_titulo = '212121' WHERE codigo = 11;
UPDATE sgc.unidade SET titular_titulo = '777777' WHERE codigo = 12;
UPDATE sgc.unidade SET titular_titulo = '888888' WHERE codigo = 13;
UPDATE sgc.unidade SET titular_titulo = '999999' WHERE codigo = 14;
UPDATE sgc.unidade SET titular_titulo = '101010' WHERE codigo = 15;
UPDATE sgc.unidade SET titular_titulo = '181818' WHERE codigo = 16;
UPDATE sgc.unidade SET titular_titulo = '131313' WHERE codigo = 17;
UPDATE sgc.unidade SET titular_titulo = '141414' WHERE codigo = 18;

-- Dados para teste de Importação (CDU-08)
-- Mapa para SECRETARIA_1 (Unidade 2)
INSERT INTO sgc.mapa (codigo, data_hora_disponibilizado, data_hora_homologado, unidade_codigo, sugestoes_apresentadas) VALUES (200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, false);
UPDATE sgc.unidade SET mapa_vigente_codigo = 200, data_vigencia_mapa_atual = CURRENT_TIMESTAMP WHERE codigo = 2;

-- Atividade para SECRETARIA_1
INSERT INTO sgc.atividade (codigo, descricao, mapa_codigo) VALUES (2001, 'Atividade 1', 200);

-- Conhecimento para Atividade 1
INSERT INTO sgc.conhecimento (codigo, descricao, atividade_codigo) VALUES (200101, 'Conhecimento 1', 2001);

-- Unidade Isolada para Teste de Diagnóstico (Garante 100% de conclusão com Mock User)
INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (99, 'Unit Test Diag', 'UNIT_TEST_DIAG', 'OPERACIONAL', 'ATIVA', 11);

INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES ('123456789012', 'Usuario Diagnostico Mock', 'mock.diagnostico@tre-pe.jus.br', NULL, 99);
INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES ('123456789012', 'CHEFE', 99);
UPDATE sgc.unidade SET titular_titulo = '123456789012' WHERE codigo = 99;
