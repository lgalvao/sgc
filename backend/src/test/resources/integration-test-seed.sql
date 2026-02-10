-- ================================================================================
-- SEED PARA TESTES DE INTEGRAÇÃO V2
-- ================================================================================
-- 
-- Este arquivo contém APENAS dados NÃO mantidos pelo sistema SGC:
--   - Unidades (VW_UNIDADE)
--   - Usuários (VW_USUARIO)
--   - Perfis de Usuários (VW_USUARIO_PERFIL_UNIDADE)
--   - Responsabilidades (VW_RESPONSABILIDADE)
--   - Parâmetros do Sistema
--
-- Dados MANTIDOS pelo sistema (Processos, Subprocessos, Mapas, etc.) devem ser
-- criados programaticamente em cada teste para garantir independência.
--
-- Última atualização: 2026-02-09
-- ================================================================================

-- ================================================================================
-- UNIDADES
-- ================================================================================
-- Hierarquia de Unidades para Testes
--
-- SEDOC (1)
--   ├── SECRETARIA_1 (2)
--   │   ├── ASSESSORIA_11 (3)
--   │   ├── ASSESSORIA_12 (4)
--   │   ├── COORD_11 (5)
--   │   │   ├── SECAO_111 (6)
--   │   │   ├── SECAO_112 (7)
--   │   │   └── SECAO_113 (8)
--   │   └── COORD_12 (9)
--   │       └── SECAO_121 (10)
--   └── SECRETARIA_2 (11)
--       ├── ASSESSORIA_21 (12)
--       ├── ASSESSORIA_22 (13)
--       ├── COORD_21 (14)
--       │   ├── SECAO_211 (15)
--       │   └── SECAO_212 (16)
--       └── COORD_22 (17)
--           └── SECAO_221 (18)

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (1, 'Seção de Desenvolvimento e Capacitação', 'SEDOC', 'INTEROPERACIONAL', 'ATIVA', NULL, '111111', '00111111', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (2, 'Secretaria 1', 'SECRETARIA_1', 'INTEROPERACIONAL', 'ATIVA', 1, '202020', '00202020', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (3, 'Assessoria 11', 'ASSESSORIA_11', 'OPERACIONAL', 'ATIVA', 2, '555555', '00555555', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (4, 'Assessoria 12', 'ASSESSORIA_12', 'OPERACIONAL', 'ATIVA', 2, '151515', '00151515', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (5, 'Coordenadoria 11', 'COORD_11', 'INTERMEDIARIA', 'ATIVA', 2, '222222', '00222222', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (6, 'Seção 111', 'SECAO_111', 'OPERACIONAL', 'ATIVA', 5, '333333', '00333333', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (7, 'Seção 112', 'SECAO_112', 'OPERACIONAL', 'ATIVA', 5, '444444', '00444444', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (8, 'Seção 113', 'SECAO_113', 'OPERACIONAL', 'ATIVA', 5, '303030', '00303030', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (9, 'Coordenadoria 12', 'COORD_12', 'INTERMEDIARIA', 'ATIVA', 2, '222223', '00222223', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (10, 'Seção 121', 'SECAO_121', 'OPERACIONAL', 'ATIVA', 9, '171717', '00171717', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (11, 'Secretaria 2', 'SECRETARIA_2', 'INTEROPERACIONAL', 'ATIVA', 1, '212121', '00212121', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (12, 'Assessoria 21', 'ASSESSORIA_21', 'OPERACIONAL', 'ATIVA', 11, '777777', '00777777', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (13, 'Assessoria 22', 'ASSESSORIA_22', 'OPERACIONAL', 'ATIVA', 11, '888888', '00888888', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (14, 'Coordenadoria 21', 'COORD_21', 'INTERMEDIARIA', 'ATIVA', 11, '999999', '00999999', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (15, 'Seção 211', 'SECAO_211', 'OPERACIONAL', 'ATIVA', 14, '101010', '00101010', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (16, 'Seção 212', 'SECAO_212', 'OPERACIONAL', 'ATIVA', 14, '181818', '00181818', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (17, 'Coordenadoria 22', 'COORD_22', 'INTERMEDIARIA', 'ATIVA', 11, '131313', '00131313', CURRENT_TIMESTAMP);

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
VALUES (18, 'Seção 221', 'SECAO_221', 'OPERACIONAL', 'ATIVA', 17, '141414', '00141414', CURRENT_TIMESTAMP);

-- ================================================================================
-- USUÁRIOS
-- ================================================================================

-- Administradores
-- Admin principal para testes V2 (usado por @WithMockAdmin)
INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('111111111111', '11111111', 'Admin Teste V2', 'admin.teste.v2@tre-pe.jus.br', '9999', 1, 1);

-- Outros administradores
INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('111111', '00111111', 'ADMIN_SEDOC_E_CHEFE_SEDOC', 'admin_sedoc_e_chefe_sedoc@tre-pe.jus.br', '2001', 1, 1);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('191919', '00191919', 'ADMIN_1_PERFIL', 'admin_unico@tre-pe.jus.br', '2002', 1, 1);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('666666', '00666666', 'Robert Plant', 'robert.plant@tre-pe.jus.br', '2008', 1, 1);

-- Gestores
INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('222222', '00222222', 'GESTOR_COORD_11', 'gestor_coord_11@tre-pe.jus.br', '2005', 5, 5);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('999999', '00999999', 'Roger Waters', 'roger.waters@tre-pe.jus.br', '2014', 14, 14);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('222223', '00222223', 'Ringo Starr', 'ringo.starr@tre-pe.jus.br', '2009', 9, 9);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('131313', '00131313', 'Mick Jagger', 'mick.jagger@tre-pe.jus.br', '2017', 17, 17);

-- Chefes
INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('333333', '00333333', 'CHEFE_SECAO_111', 'chefe_secao_111@tre-pe.jus.br', '2006', 6, 6);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('444444', '00444444', 'SERVIDOR_SECAO_111_E_CHEFE_SECAO_112',
        'servidor_secao_111_e_chefe_secao_112@tre-pe.jus.br', '2007', 6, 6);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('555555', '00555555', 'David Bowie', 'david.bowie@tre-pe.jus.br', '2003', 3, 3);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('777777', '00777777', 'Janis Joplin', 'janis.joplin@tre-pe.jus.br', '2012', 12, 12);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('888888', '00888888', 'Jimi Hendrix', 'jimi.hendrix@tre-pe.jus.br', '2013', 13, 13);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('101010', '00101010', 'Debbie Harry', 'debbie.harry@tre-pe.jus.br', '2015', 15, 15);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('141414', '00141414', 'Tina Turner', 'tina.turner@tre-pe.jus.br', '2018', 18, 18);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('151515', '00151515', 'Axl Rose', 'axl.rose@tre-pe.jus.br', '2004', 4, 4);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('171717', '00171717', 'Lemmy Kilmister', 'lemmy.kilmister@tre-pe.jus.br', '2010', 10, 10);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('181818', '00181818', 'Pete Townshend', 'pete.townshend@tre-pe.jus.br', '2026', 16, 16);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('202020', '00202020', 'John Lennon', 'john.lennon@tre-pe.jus.br', '2020', 2, 2);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('212121', '00212121', 'George Harrison', 'george.harrison@tre-pe.jus.br', '2021', 11, 11);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('303030', '00303030', 'Alice Cooper', 'alice.cooper@tre-pe.jus.br', '2031', 8, 8);

-- Servidores
INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('121212', '00121212', 'Steven Tyler', 'steven.tyler@tre-pe.jus.br', '2016', 16, 16);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('232323', '00232323', 'Bon Jovi', 'bon.jovi@tre-pe.jus.br', '2023', 3, 3);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('242424', '00242424', 'Jon Lord', 'jon.lord@tre-pe.jus.br', '2024', 4, 4);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('252525', '00252525', 'Bruce Springsteen', 'bruce.springsteen@tre-pe.jus.br', '2025', 7, 7);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('262626', '00262626', 'Chuck Berry', 'chuck.berry@tre-pe.jus.br', '2030', 10, 10);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('272727', '00272727', 'Elton John', 'elton.john@tre-pe.jus.br', '2027', 12, 12);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('282828', '00282828', 'Eric Clapton', 'eric.clapton@tre-pe.jus.br', '2028', 15, 15);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('292929', '00292929', 'Flea', 'flea@tre-pe.jus.br', '2029', 18, 18);

-- ================================================================================
-- PERFIS DE USUÁRIOS
-- ================================================================================

-- Perfis ADMIN
-- Perfil para Admin Teste V2 (usado por @WithMockAdmin)
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('111111111111', 'ADMIN', 1);

-- Outros perfis ADMIN
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('111111', 'ADMIN', 1);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('111111', 'CHEFE', 1);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('191919', 'ADMIN', 1);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('666666', 'ADMIN', 1);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('666666', 'SERVIDOR', 1);

-- Perfis GESTOR
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('222222', 'GESTOR', 5);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('999999', 'GESTOR', 14);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('222223', 'GESTOR', 9);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('131313', 'GESTOR', 17);

-- Perfis CHEFE
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('333333', 'CHEFE', 6);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('444444', 'SERVIDOR', 6);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('444444', 'CHEFE', 7);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('555555', 'CHEFE', 3);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('777777', 'CHEFE', 12);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('888888', 'CHEFE', 13);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('101010', 'CHEFE', 15);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('141414', 'CHEFE', 18);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('151515', 'CHEFE', 4);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('171717', 'CHEFE', 10);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('181818', 'CHEFE', 16);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('202020', 'CHEFE', 2);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('212121', 'CHEFE', 11);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('303030', 'CHEFE', 8);

-- Perfis SERVIDOR
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('121212', 'SERVIDOR', 16);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('232323', 'SERVIDOR', 3);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('242424', 'SERVIDOR', 4);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('252525', 'SERVIDOR', 7);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('262626', 'SERVIDOR', 10);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('272727', 'SERVIDOR', 12);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('282828', 'SERVIDOR', 15);

INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('292929', 'SERVIDOR', 18);

-- ================================================================================
-- RESPONSABILIDADES (Titulares das Unidades)
-- ================================================================================

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

-- ================================================================================
-- PARÂMETROS DO SISTEMA
-- ================================================================================

INSERT INTO sgc.parametro (codigo, chave, descricao, valor)
VALUES (1, 'DIAS_INATIVACAO_PROCESSO', 'Dias para inativação de processos', '30');

INSERT INTO sgc.parametro (codigo, chave, descricao, valor)
VALUES (2, 'DIAS_ALERTA_NOVO', 'Dias para indicação de alerta como novo', '3');

-- ================================================================================
-- FIM DO SEED
-- ================================================================================
