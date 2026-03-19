INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (1, 'Administração', 'ADMIN', 'RAIZ', 'ATIVA', NULL, '111111', '00111111', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (2, 'Secretaria 1', 'SECRETARIA_1', 'INTEROPERACIONAL', 'ATIVA', 1, '202020', '00202020', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (3, 'Assessoria 11', 'ASSESSORIA_11', 'OPERACIONAL', 'ATIVA', 2, '555555', '00555555', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (4, 'Assessoria 12', 'ASSESSORIA_12', 'OPERACIONAL', 'ATIVA', 2, '151515', '00151515', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (5, 'Coordenadoria 11', 'COORD_11', 'INTERMEDIARIA', 'ATIVA', 2, '222222', '00222222', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (6, 'Seção 111', 'SECAO_111', 'OPERACIONAL', 'ATIVA', 5, '333333', '00333333', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (7, 'Seção 112', 'SECAO_112', 'OPERACIONAL', 'ATIVA', 5, '444444', '00444444', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (8, 'Seção 113', 'SECAO_113', 'OPERACIONAL', 'ATIVA', 5, '303030', '00303030', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (9, 'Coordenadoria 12', 'COORD_12', 'INTERMEDIARIA', 'ATIVA', 2, '222223', '00222223', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (10, 'Seção 121', 'SECAO_121', 'OPERACIONAL', 'ATIVA', 9, '171717', '00171717', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (11, 'Secretaria 2', 'SECRETARIA_2', 'INTEROPERACIONAL', 'ATIVA', 1, '212121', '00212121', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (12, 'Assessoria 21', 'ASSESSORIA_21', 'OPERACIONAL', 'ATIVA', 11, '777777', '00777777', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (13, 'Assessoria 22', 'ASSESSORIA_22', 'OPERACIONAL', 'ATIVA', 11, '888888', '00888888', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (14, 'Coordenadoria 21', 'COORD_21', 'INTERMEDIARIA', 'ATIVA', 11, '999999', '00999999', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (15, 'Seção 211', 'SECAO_211', 'OPERACIONAL', 'ATIVA', 14, '101010', '00101010', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (16, 'Seção 212', 'SECAO_212', 'OPERACIONAL', 'ATIVA', 14, '181818', '00181818', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (17, 'Coordenadoria 22', 'COORD_22', 'INTERMEDIARIA', 'ATIVA', 11, '131313', '00131313', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (18, 'Seção 221', 'SECAO_221', 'OPERACIONAL', 'ATIVA', 17, '141414', '00141414', CURRENT_TIMESTAMP);

-- Nova Subárvore (Secretaria 3)
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (30, 'Secretaria 3', 'SECRETARIA_3', 'INTEROPERACIONAL', 'ATIVA', 1, '300001', '00300001', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (31, 'Assessoria 31', 'ASSESSORIA_31', 'OPERACIONAL', 'ATIVA', 30, '310001', '00310001', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (32, 'Coordenadoria 31', 'COORD_31', 'INTERMEDIARIA', 'ATIVA', 30, '320001', '00320001', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (33, 'Seção 311', 'SECAO_311', 'OPERACIONAL', 'ATIVA', 32, '330001', '00330001', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (34, 'Assessoria 32', 'ASSESSORIA_32', 'OPERACIONAL', 'ATIVA', 30, '340001', '00340001', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (35, 'Coordenadoria 32', 'COORD_32', 'INTERMEDIARIA', 'ATIVA', 30, '350001', '00350001', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (36, 'Seção 321', 'SECAO_321', 'OPERACIONAL', 'ATIVA', 35, '360001', '00360001', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (37, 'Seção 322', 'SECAO_322', 'OPERACIONAL', 'ATIVA', 35, '370001', '00370001', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (38, 'Seção 323', 'SECAO_323', 'OPERACIONAL', 'ATIVA', 35, '380001', '00380001', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (39, 'Seção 324', 'SECAO_324', 'OPERACIONAL', 'ATIVA', 35, '390001', '00390001', CURRENT_TIMESTAMP);

-- Users with full details (titulo, matricula, nome, email, ramal, unidade_lot, unidade_comp)
INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('111111', '00111111', 'ADMIN_E_CHEFE_SEDOC', 'admin_e_chefe_sedoc@tre-pe.jus.br', '2001', 1, 1);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('111111', 'ADMIN', 1);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('111111', 'CHEFE', 1);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('191919', '00191919', 'ADMIN_1_PERFIL', 'admin_unico@tre-pe.jus.br', '2002', 1, 1);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('191919', 'ADMIN', 1);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('222222', '00222222', 'GESTOR_COORD_11', 'gestor_coord_11@tre-pe.jus.br', '2005', 5, 5);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('222222', 'GESTOR', 5);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('333333', '00333333', 'CHEFE_SECAO_111', 'chefe_secao_111@tre-pe.jus.br', '2006', 6, 6);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('333333', 'CHEFE', 6);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('444444', '00444444', 'SERVIDOR_SECAO_111_E_CHEFE_SECAO_112',
        'servidor_secao_111_e_chefe_secao_112@tre-pe.jus.br', '2007', 6, 6);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('444444', 'SERVIDOR', 6);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('444444', 'CHEFE', 7);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('555555', '00555555', 'David Bowie', 'david.bowie@tre-pe.jus.br', '2003', 3, 3);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('555555', 'CHEFE', 3);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('666666', '00666666', 'Robert Plant', 'robert.plant@tre-pe.jus.br', '2008', 1, 1);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('666666', 'ADMIN', 1);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('666666', 'SERVIDOR', 1);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('777777', '00777777', 'Janis Joplin', 'janis.joplin@tre-pe.jus.br', '2012', 12, 12);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('777777', 'CHEFE', 12);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('888888', '00888888', 'Jimi Hendrix', 'jimi.hendrix@tre-pe.jus.br', '2013', 13, 13);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('888888', 'CHEFE', 13);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('999999', '00999999', 'Roger Waters', 'roger.waters@tre-pe.jus.br', '2014', 14, 14);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('999999', 'GESTOR', 14);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('101010', '00101010', 'Debbie Harry', 'debbie.harry@tre-pe.jus.br', '2015', 15, 15);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('101010', 'CHEFE', 15);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('121212', '00121212', 'Steven Tyler', 'steven.tyler@tre-pe.jus.br', '2016', 16, 16);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('121212', 'SERVIDOR', 16);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('131313', '00131313', 'Mick Jagger', 'mick.jagger@tre-pe.jus.br', '2017', 17, 17);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('131313', 'GESTOR', 17);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('141414', '00141414', 'Tina Turner', 'tina.turner@tre-pe.jus.br', '2018', 18, 18);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('141414', 'CHEFE', 18);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('151515', '00151515', 'Axl Rose', 'axl.rose@tre-pe.jus.br', '2004', 4, 4);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('151515', 'CHEFE', 4);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('171717', '00171717', 'Lemmy Kilmister', 'lemmy.kilmister@tre-pe.jus.br', '2010', 10, 10);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('171717', 'CHEFE', 10);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('181818', '00181818', 'Pete Townshend', 'pete.townshend@tre-pe.jus.br', '2026', 16, 16);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('181818', 'CHEFE', 16);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('202020', '00202020', 'John Lennon', 'john.lennon@tre-pe.jus.br', '2020', 2, 2);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('202020', 'CHEFE', 2);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('202020', 'GESTOR', 2);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('212121', '00212121', 'George Harrison', 'george.harrison@tre-pe.jus.br', '2021', 11, 11);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('212121', 'CHEFE', 11);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('222223', '00222223', 'Ringo Starr', 'ringo.starr@tre-pe.jus.br', '2009', 9, 9);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('222223', 'GESTOR', 9);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('232323', '00232323', 'Bon Jovi', 'bon.jovi@tre-pe.jus.br', '2023', 3, 3);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('232323', 'SERVIDOR', 3);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('242424', '00242424', 'Jon Lord', 'jon.lord@tre-pe.jus.br', '2024', 4, 4);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('242424', 'SERVIDOR', 4);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('252525', '00252525', 'Bruce Springsteen', 'bruce.springsteen@tre-pe.jus.br', '2025', 7, 7);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('252525', 'SERVIDOR', 7);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('262626', '00262626', 'Chuck Berry', 'chuck.berry@tre-pe.jus.br', '2030', 10, 10);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('262626', 'SERVIDOR', 10);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('272727', '00272727', 'Elton John', 'elton.john@tre-pe.jus.br', '2027', 12, 12);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('272727', 'SERVIDOR', 12);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('282828', '00282828', 'Eric Clapton', 'eric.clapton@tre-pe.jus.br', '2028', 15, 15);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('282828', 'SERVIDOR', 15);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('292929', '00292929', 'Flea', 'flea@tre-pe.jus.br', '2029', 18, 18);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('292929', 'SERVIDOR', 18);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('303030', '00303030', 'Alice Cooper', 'alice.cooper@tre-pe.jus.br', '2031', 8, 8);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('303030', 'CHEFE', 8);

-- Usuários da Secretaria 3
INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('300001', '00300001', 'CHEFE_SECRETARIA_3', 'chefe_sec3@tre-pe.jus.br', '3001', 30, 30);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('300001', 'CHEFE', 30);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('300001', 'GESTOR', 30);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('310001', '00310001', 'CHEFE_ASSESSORIA_31', 'chefe_ass31@tre-pe.jus.br', '3101', 31, 31);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('310001', 'CHEFE', 31);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('320001', '00320001', 'GESTOR_COORD_31', 'gestor_coord31@tre-pe.jus.br', '3201', 32, 32);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('320001', 'GESTOR', 32);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('330001', '00330001', 'CHEFE_SECAO_311', 'chefe_sec311@tre-pe.jus.br', '3301', 33, 33);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('330001', 'CHEFE', 33);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('340001', '00340001', 'CHEFE_ASSESSORIA_32', 'chefe_ass32@tre-pe.jus.br', '3401', 34, 34);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('340001', 'CHEFE', 34);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('350001', '00350001', 'GESTOR_COORD_32', 'gestor_coord32@tre-pe.jus.br', '3501', 35, 35);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('350001', 'GESTOR', 35);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('360001', '00360001', 'CHEFE_SECAO_321', 'chefe_sec321@tre-pe.jus.br', '3601', 36, 36);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('360001', 'CHEFE', 36);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('370001', '00370001', 'CHEFE_SECAO_322', 'chefe_sec322@tre-pe.jus.br', '3701', 37, 37);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('370001', 'CHEFE', 37);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('380001', '00380001', 'CHEFE_SECAO_323', 'chefe_sec323@tre-pe.jus.br', '3801', 38, 38);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('380001', 'CHEFE', 38);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('390001', '00390001', 'CHEFE_SECAO_324', 'chefe_sec324@tre-pe.jus.br', '3901', 39, 39);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('390001', 'CHEFE', 39);

-- Administradores do Sistema (necessário para listar em configurações)
INSERT INTO sgc.administrador (usuario_titulo)
VALUES ('191919');
INSERT INTO sgc.administrador (usuario_titulo)
VALUES ('111111');

-- Inserir Mapa vigente para Assessoria 12 (Unit 4) para testes de Revisão
-- Processo 99
INSERT INTO sgc.processo (codigo, data_criacao, data_finalizacao, data_limite, descricao, situacao, tipo)
VALUES (99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Processo 99', 'FINALIZADO', 'MAPEAMENTO');

-- UnidadeProcesso (Unit 4)
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao, nome, sigla, tipo, matricula_titular,
                                  titulo_titular, data_inicio_titularidade, unidade_superior_codigo)
VALUES (99, 4, 'CONCLUIDA', 'Assessoria 12', 'ASSESSORIA_12', 'OPERACIONAL', '00151515', '151515', CURRENT_TIMESTAMP,
        2);

-- Subprocesso 99
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (99, 99, 4, 'MAPEAMENTO_MAPA_HOMOLOGADO', CURRENT_TIMESTAMP);

-- Mapa (agora vinculado ao Subprocesso e sem sugestoes_apresentadas)
INSERT INTO sgc.mapa (codigo, subprocesso_codigo, data_hora_disponibilizado, data_hora_homologado, sugestoes)
VALUES (99, 99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

-- Vincular o mapa à unidade via tabela de associação

INSERT INTO sgc.unidade_mapa (unidade_codigo, mapa_vigente_codigo)
VALUES (4, 99);

-- Atividades e conhecimentos para o mapa 99 (ASSESSORIA_12)
-- Necessário para testes de revisão (CDU-08, CDU-10)
INSERT INTO sgc.atividade (codigo, descricao, mapa_codigo)
VALUES (9901, 'Atividade Seed 1', 99);

INSERT INTO sgc.conhecimento (codigo, descricao, atividade_codigo)
VALUES (990101, 'Conhecimento Seed 1.1', 9901);

INSERT INTO sgc.atividade (codigo, descricao, mapa_codigo)
VALUES (9902, 'Atividade Seed 2', 99);

INSERT INTO sgc.conhecimento (codigo, descricao, atividade_codigo)
VALUES (990201, 'Conhecimento Seed 2.1', 9902);

-- Competências para o mapa 99 (ASSESSORIA_12)
-- Necessário para testes de CDU-18 (Visualização de mapa)
INSERT INTO sgc.competencia (codigo, descricao, mapa_codigo)
VALUES (99001, 'Competência Técnica Seed 99', 99);

-- Vincular atividades à competência
INSERT INTO sgc.competencia_atividade (atividade_codigo, competencia_codigo)
VALUES (9901, 99001);
INSERT INTO sgc.competencia_atividade (atividade_codigo, competencia_codigo)
VALUES (9902, 99001);


-- Parâmetros do sistema
INSERT INTO sgc.parametro (codigo, chave, descricao, valor)
VALUES (1, 'DIAS_INATIVACAO_PROCESSO', 'Dias para inativação de processos', '30');
INSERT INTO sgc.parametro (codigo, chave, descricao, valor)
VALUES (2, 'DIAS_ALERTA_NOVO', 'Dias para indicação de alerta como novo', '3');

-- Dados para teste de Importação (CDU-08)
-- Processo 200
INSERT INTO sgc.processo (codigo, data_criacao, data_finalizacao, data_limite, descricao, situacao, tipo)
VALUES (200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Processo Seed 200', 'FINALIZADO', 'MAPEAMENTO');

-- UnidadeProcesso (Unit 2)
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao, nome, sigla, tipo, matricula_titular,
                                  titulo_titular, data_inicio_titularidade, unidade_superior_codigo)
VALUES (200, 2, 'CONCLUIDA', 'Secretaria 1', 'SECRETARIA_1', 'INTEROPERACIONAL', '00202020', '202020',
        CURRENT_TIMESTAMP, 1);

-- Subprocesso 200
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (200, 200, 2, 'MAPEAMENTO_MAPA_HOMOLOGADO', CURRENT_TIMESTAMP);

-- Mapa para SECRETARIA_1 (Unidade 2)
INSERT INTO sgc.mapa (codigo, subprocesso_codigo, data_hora_disponibilizado, data_hora_homologado, sugestoes)
VALUES (200, 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

-- Vincular o mapa à unidade

INSERT INTO sgc.unidade_mapa (unidade_codigo, mapa_vigente_codigo)
VALUES (2, 200);

-- Atividade para SECRETARIA_1
INSERT INTO sgc.atividade (codigo, descricao, mapa_codigo)
VALUES (2001, 'Atividade 1', 200);

-- Conhecimento para Atividade 1
INSERT INTO sgc.conhecimento (codigo, descricao, atividade_codigo)
VALUES (200101, 'Conhecimento 1', 2001);

-- Competência para o mapa 200 (SECRETARIA_1)
-- Necessário para testes de CDU-18 (Visualização de mapa)
INSERT INTO sgc.competencia (codigo, descricao, mapa_codigo)
VALUES (200001, 'Competência Técnica 1', 200);

-- Vincular atividade à competência
INSERT INTO sgc.competencia_atividade (atividade_codigo, competencia_codigo)
VALUES (2001, 200001);

-- -------------------------------------------------------------------------------------------------
-- NOVOS PROCESSOS PARA SECRETARIA 3
-- -------------------------------------------------------------------------------------------------

-- Processo 301: SECAO_311 (Cadastro de Atividades Homologado)
INSERT INTO sgc.processo (codigo, data_criacao, data_limite, descricao, situacao, tipo)
VALUES (301, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30' DAY, 'Mapeamento Secão 311', 'EM_ANDAMENTO', 'MAPEAMENTO');

INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao, nome, sigla, tipo, matricula_titular, titulo_titular, data_inicio_titularidade, unidade_superior_codigo)
VALUES (301, 33, 'EM_ANDAMENTO', 'Seção 311', 'SECAO_311', 'OPERACIONAL', '00330001', '330001', CURRENT_TIMESTAMP, 32);

INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (301, 301, 33, 'MAPEAMENTO_CADASTRO_HOMOLOGADO', CURRENT_TIMESTAMP + INTERVAL '10' DAY);

INSERT INTO sgc.mapa (codigo, subprocesso_codigo, data_hora_disponibilizado, data_hora_homologado)
VALUES (301, 301, CURRENT_TIMESTAMP - INTERVAL '2' DAY, NULL);

INSERT INTO sgc.atividade (codigo, descricao, mapa_codigo) VALUES (3011, 'Gestão de Frequência', 301);
INSERT INTO sgc.conhecimento (codigo, descricao, atividade_codigo) VALUES (301101, 'Normas de Pessoal', 3011);
INSERT INTO sgc.conhecimento (codigo, descricao, atividade_codigo) VALUES (301102, 'Sistema de Ponto Eletrônico', 3011);

INSERT INTO sgc.atividade (codigo, descricao, mapa_codigo) VALUES (3012, 'Elaboração de Relatórios', 301);
INSERT INTO sgc.conhecimento (codigo, descricao, atividade_codigo) VALUES (301201, 'Ferramentas de BI', 3012);
INSERT INTO sgc.conhecimento (codigo, descricao, atividade_codigo) VALUES (301202, 'Técnicas de Redação Oficial', 3012);

-- Processo 302: SECAO_321 (Mapa Disponibilizado)
INSERT INTO sgc.processo (codigo, data_criacao, data_limite, descricao, situacao, tipo)
VALUES (302, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30' DAY, 'Mapeamento Secão 321', 'EM_ANDAMENTO', 'MAPEAMENTO');

INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao, nome, sigla, tipo, matricula_titular, titulo_titular, data_inicio_titularidade, unidade_superior_codigo)
VALUES (302, 36, 'EM_ANDAMENTO', 'Seção 321', 'SECAO_321', 'OPERACIONAL', '00360001', '360001', CURRENT_TIMESTAMP, 35);

INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (302, 302, 36, 'MAPEAMENTO_MAPA_DISPONIBILIZADO', CURRENT_TIMESTAMP + INTERVAL '15' DAY);

INSERT INTO sgc.mapa (codigo, subprocesso_codigo, data_hora_disponibilizado, data_hora_homologado)
VALUES (302, 302, CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL);

INSERT INTO sgc.atividade (codigo, descricao, mapa_codigo) VALUES (3021, 'Análise de Processos Licitatórios', 302);
INSERT INTO sgc.conhecimento (codigo, descricao, atividade_codigo) VALUES (302101, 'Lei de Licitações', 3021);

INSERT INTO sgc.competencia (codigo, descricao, mapa_codigo) VALUES (30201, 'Competência Técnica Secão 321', 302);
INSERT INTO sgc.competencia_atividade (atividade_codigo, competencia_codigo) VALUES (3021, 30201);

-- Unidade Isolada para Teste de Diagnóstico (Garante 100% de conclusão com Mock User)

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular,
                            matricula_titular, data_inicio_titularidade)
VALUES (99, 'Unit Test Diag', 'UNIT_TEST_DIAG', 'OPERACIONAL', 'ATIVA', 11, '123456789012', '56789012',
        CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('123456789012', '56789012', 'Usuario Diagnostico Mock', 'mock.diagnostico@tre-pe.jus.br', '9999', 99, 99);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('123456789012', 'CHEFE', 99);

-- Phase 1 & 2 (Removed because they were locking units used in E2E tests)

-- -------------------------------------------------------------------------------------------------
-- VW_RESPONSABILIDADE (Inserido ao final para garantir integridade referencial)
-- -------------------------------------------------------------------------------------------------
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (1, '111111', '00111111', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (2, '202020', '00202020', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (3, '555555', '00555555', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (4, '151515', '00151515', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (5, '222222', '00222222', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (6, '333333', '00333333', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (7, '444444', '00444444', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (8, '303030', '00303030', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (9, '222223', '00222223', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (10, '171717', '00171717', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (11, '212121', '00212121', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (12, '777777', '00777777', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (13, '888888', '00888888', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (14, '999999', '00999999', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (15, '101010', '00101010', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (16, '181818', '00181818', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (17, '131313', '00131313', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (18, '141414', '00141414', 'TITULAR', CURRENT_TIMESTAMP);

-- Responsabilidades da Secretaria 3
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (30, '300001', '00300001', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (31, '310001', '00310001', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (32, '320001', '00320001', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (33, '330001', '00330001', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (34, '340001', '00340001', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (35, '350001', '00350001', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (36, '360001', '00360001', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (37, '370001', '00370001', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (38, '380001', '00380001', 'TITULAR', CURRENT_TIMESTAMP);
INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (39, '390001', '00390001', 'TITULAR', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
VALUES (99, '123456789012', '56789012', 'TITULAR', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('212121', 'GESTOR', 11);

-- -------------------------------------------------------------------------------------------------
-- MOVIMENTAÇÕES (Logs de histórico dos processos)
-- -------------------------------------------------------------------------------------------------
-- Subprocesso 99 (Unidade 4 - ASSESSORIA_12)
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo,
                              data_hora, descricao)
VALUES (1, 99, 4, 4, '151515', CURRENT_TIMESTAMP - INTERVAL '5' DAY, 'Processo iniciado');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo,
                              data_hora, descricao)
VALUES (2, 99, 4, 2, '151515', CURRENT_TIMESTAMP - INTERVAL '4' DAY, 'Disponibilização do cadastro de atividades');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo,
                              data_hora, descricao)
VALUES (3, 99, 2, 4, '202020', CURRENT_TIMESTAMP - INTERVAL '3' DAY, 'Disponibilização do mapa de competências para validação');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo,
                              data_hora, descricao)
VALUES (4, 99, 4, 2, '151515', CURRENT_TIMESTAMP - INTERVAL '2' DAY, 'Validação do mapa de competências');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo,
                              data_hora, descricao)
VALUES (5, 99, 2, 1, '202020', CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'Mapa de competências homologado');

-- Subprocesso 200 (Unidade 2 - SECRETARIA_1)
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo,
                              data_hora, descricao)
VALUES (6, 200, 2, 2, '202020', CURRENT_TIMESTAMP - INTERVAL '10' DAY, 'Processo iniciado');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo,
                              data_hora, descricao)
VALUES (7, 200, 2, 1, '202020', CURRENT_TIMESTAMP - INTERVAL '8' DAY, 'Disponibilização do cadastro de atividades');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo,
                              data_hora, descricao)
VALUES (8, 200, 1, 2, '111111', CURRENT_TIMESTAMP - INTERVAL '5' DAY,
        'Disponibilização do mapa de competências para validação');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo,
                              data_hora, descricao)
VALUES (9, 200, 2, 1, '202020', CURRENT_TIMESTAMP - INTERVAL '2' DAY, 'Mapa de competências homologado');

-- Movimentações Secretaria 3
-- Processo 301 (33 < 32)
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao)
VALUES (30, 301, 33, 33, '330001', CURRENT_TIMESTAMP - INTERVAL '4' DAY, 'Processo iniciado');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao)
VALUES (31, 301, 33, 32, '330001', CURRENT_TIMESTAMP - INTERVAL '3' DAY, 'Disponibilização do cadastro de atividades');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao)
VALUES (32, 301, 32, 1, '320001', CURRENT_TIMESTAMP - INTERVAL '2' DAY, 'Cadastro aceito');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao)
VALUES (33, 301, 1, 1, '111111', CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'Cadastro homologado');

-- Processo 302 (36 < 35 < 30)
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao)
VALUES (34, 302, 36, 36, '360001', CURRENT_TIMESTAMP - INTERVAL '6' DAY, 'Processo iniciado');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao)
VALUES (35, 302, 36, 35, '360001', CURRENT_TIMESTAMP - INTERVAL '5' DAY, 'Disponibilização do cadastro de atividades');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao)
VALUES (36, 302, 35, 30, '350001', CURRENT_TIMESTAMP - INTERVAL '4' DAY, 'Cadastro aceito');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao)
VALUES (37, 302, 30, 30, '300001', CURRENT_TIMESTAMP - INTERVAL '3' DAY, 'Cadastro homologado');
INSERT INTO sgc.movimentacao (codigo, subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao)
VALUES (38, 302, 1, 36, '111111', CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'Disponibilização do mapa de competências para validação');

-- -------------------------------------------------------------------------------------------------
-- ALERTAS (Notificações correspondentes às movimentações)
-- -------------------------------------------------------------------------------------------------
-- Alertas para Unidade 1 (ADMIN) - Processos Homologados
INSERT INTO sgc.alerta (codigo, data_hora, processo_codigo, unidade_origem_codigo, unidade_destino_codigo, descricao)
VALUES (1, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 99, 2, 1, 'Mapa de competências homologado');
INSERT INTO sgc.alerta (codigo, data_hora, processo_codigo, unidade_origem_codigo, unidade_destino_codigo, descricao)
VALUES (2, CURRENT_TIMESTAMP - INTERVAL '2' DAY, 200, 2, 1, 'Mapa de competências homologado');
INSERT INTO sgc.alerta (codigo, data_hora, processo_codigo, unidade_origem_codigo, unidade_destino_codigo, descricao)
VALUES (3, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 301, 1, 1, 'Cadastro homologado');

-- Alerta para Unidade 36 (SECAO_321) - Mapa Disponibilizado para Validação
INSERT INTO sgc.alerta (codigo, data_hora, processo_codigo, unidade_origem_codigo, unidade_destino_codigo, descricao)
VALUES (4, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 302, 1, 36, 'Disponibilização do mapa de competências para validação');

-- Associações de Alertas aos Usuários (Chefe/Gestor)
-- Para os alertas da Unidade 1 (Usuário 111111)
INSERT INTO sgc.alerta_usuario (alerta_codigo, usuario_titulo, data_hora_leitura) VALUES (1, '111111', NULL);
INSERT INTO sgc.alerta_usuario (alerta_codigo, usuario_titulo, data_hora_leitura) VALUES (2, '111111', NULL);
INSERT INTO sgc.alerta_usuario (alerta_codigo, usuario_titulo, data_hora_leitura) VALUES (3, '111111', NULL);

-- Para o alerta da Unidade 36 (Usuário 360001)
INSERT INTO sgc.alerta_usuario (alerta_codigo, usuario_titulo, data_hora_leitura) VALUES (4, '360001', NULL);

-- Reset identity sequences to prevent ID conflicts with test data
-- This ensures auto-generated IDs start above the manually inserted ones
ALTER TABLE sgc.processo
    ALTER COLUMN codigo RESTART WITH 400;
ALTER TABLE sgc.subprocesso
    ALTER COLUMN codigo RESTART WITH 400;
ALTER TABLE sgc.mapa
    ALTER COLUMN codigo RESTART WITH 400;
ALTER TABLE sgc.atividade
    ALTER COLUMN codigo RESTART WITH 4000;
ALTER TABLE sgc.conhecimento
    ALTER COLUMN codigo RESTART WITH 400000;
ALTER TABLE sgc.competencia
    ALTER COLUMN codigo RESTART WITH 4000;
ALTER TABLE sgc.movimentacao
    ALTER COLUMN codigo RESTART WITH 50;
ALTER TABLE sgc.alerta
    ALTER COLUMN codigo RESTART WITH 10;

