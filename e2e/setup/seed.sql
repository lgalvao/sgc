INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (1, 'Seção de Desenvolvimento e Capacitação', 'SEDOC', 'INTEROPERACIONAL', 'ATIVA', NULL);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (2, 'Secretaria 1', 'SECRETARIA_1', 'INTEROPERACIONAL', 'ATIVA', 1);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (3, 'Assessoria 11', 'ASSESSORIA_11', 'OPERACIONAL', 'ATIVA', 2);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (4, 'Assessoria 12', 'ASSESSORIA_12', 'OPERACIONAL', 'ATIVA', 2);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (5, 'Coordenadoria 11', 'COORD_11', 'INTERMEDIARIA', 'ATIVA', 2);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (6, 'Seção 111', 'SECAO_111', 'OPERACIONAL', 'ATIVA', 5);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (7, 'Seção 112', 'SECAO_112', 'OPERACIONAL', 'ATIVA', 5);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (8, 'Seção 113', 'SECAO_113', 'OPERACIONAL', 'ATIVA', 5);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (9, 'Coordenadoria 12', 'COORD_12', 'INTERMEDIARIA', 'ATIVA', 2);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (10, 'Seção 121', 'SECAO_121', 'OPERACIONAL', 'ATIVA', 9);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (11, 'Secretaria 2', 'SECRETARIA_2', 'INTEROPERACIONAL', 'ATIVA', 1);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (12, 'Assessoria 21', 'ASSESSORIA_21', 'OPERACIONAL', 'ATIVA', 11);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (13, 'Assessoria 22', 'ASSESSORIA_22', 'OPERACIONAL', 'ATIVA', 11);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (14, 'Coordenadoria 21', 'COORD_21', 'INTERMEDIARIA', 'ATIVA', 11);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (15, 'Seção 211', 'SECAO_211', 'OPERACIONAL', 'ATIVA', 14);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (16, 'Seção 212', 'SECAO_212', 'OPERACIONAL', 'ATIVA', 14);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (17, 'Coordenadoria 22', 'COORD_22', 'INTERMEDIARIA', 'ATIVA', 11);
INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (18, 'Seção 221', 'SECAO_221', 'OPERACIONAL', 'ATIVA', 17);

-- Users with full details (titulo, matricula, nome, email, ramal, unidade_lot, unidade_comp)
INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('111111', '00111111', 'ADMIN_SEDOC_E_CHEFE_SEDOC', 'admin_sedoc_e_chefe_sedoc@tre-pe.jus.br', '2001', 1, 1);
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

-- Inserir Mapa vigente para Assessoria 12 (Unit 4) para testes de Revisão
-- Processo 99
INSERT INTO sgc.processo (codigo, data_criacao, data_finalizacao, data_limite, descricao, situacao, tipo)
VALUES (99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Processo 99', 'FINALIZADO', 'MAPEAMENTO');

-- UnidadeProcesso (Unit 4)
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao)
VALUES (99, 4, 'CONCLUIDA');

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


-- Atualizar titulares das unidades depois da criação dos usuários (com matrícula e data inicio)
UPDATE sgc.vw_unidade
SET titulo_titular           = '111111',
    matricula_titular        = '00111111',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 1;

UPDATE sgc.vw_unidade
SET titulo_titular           = '202020',
    matricula_titular        = '00202020',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 2;

UPDATE sgc.vw_unidade
SET titulo_titular           = '555555',
    matricula_titular        = '00555555',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 3;

UPDATE sgc.vw_unidade
SET titulo_titular           = '151515',
    matricula_titular        = '00151515',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 4;

UPDATE sgc.vw_unidade
SET titulo_titular           = '222222',
    matricula_titular        = '00222222',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 5;

UPDATE sgc.vw_unidade
SET titulo_titular           = '333333',
    matricula_titular        = '00333333',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 6;

UPDATE sgc.vw_unidade
SET titulo_titular           = '444444',
    matricula_titular        = '00444444',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 7;

UPDATE sgc.vw_unidade
SET titulo_titular           = '303030',
    matricula_titular        = '00303030',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 8;

UPDATE sgc.vw_unidade
SET titulo_titular           = '222223',
    matricula_titular        = '00222223',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 9;

UPDATE sgc.vw_unidade
SET titulo_titular           = '171717',
    matricula_titular        = '00171717',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 10;

UPDATE sgc.vw_unidade
SET titulo_titular           = '212121',
    matricula_titular        = '00212121',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 11;

UPDATE sgc.vw_unidade
SET titulo_titular           = '777777',
    matricula_titular        = '00777777',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 12;

UPDATE sgc.vw_unidade
SET titulo_titular           = '888888',
    matricula_titular        = '00888888',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 13;

UPDATE sgc.vw_unidade
SET titulo_titular           = '999999',
    matricula_titular        = '00999999',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 14;

UPDATE sgc.vw_unidade
SET titulo_titular           = '101010',
    matricula_titular        = '00101010',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 15;

UPDATE sgc.vw_unidade
SET titulo_titular           = '181818',
    matricula_titular        = '00181818',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 16;

UPDATE sgc.vw_unidade
SET titulo_titular           = '131313',
    matricula_titular        = '00131313',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 17;

UPDATE sgc.vw_unidade
SET titulo_titular           = '141414',
    matricula_titular        = '00141414',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 18;

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
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao)
VALUES (200, 2, 'CONCLUIDA');

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

-- Unidade Isolada para Teste de Diagnóstico (Garante 100% de conclusão com Mock User)

INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo)
VALUES (99, 'Unit Test Diag', 'UNIT_TEST_DIAG', 'OPERACIONAL', 'ATIVA', 11);

INSERT INTO sgc.vw_usuario (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
VALUES ('123456789012', '56789012', 'Usuario Diagnostico Mock', 'mock.diagnostico@tre-pe.jus.br', '9999', 99, 99);
INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, perfil, unidade_codigo)
VALUES ('123456789012', 'CHEFE', 99);

UPDATE sgc.vw_unidade
SET titulo_titular           = '123456789012',
    matricula_titular        = '56789012',
    data_inicio_titularidade = CURRENT_TIMESTAMP
WHERE codigo = 99;

-- Phase 1: Enhanced Seed Data

-- Processo 300: Mapeamento em andamento na hierarquia SECRETARIA_1
INSERT INTO sgc.processo (codigo, data_criacao, data_finalizacao, data_limite, descricao, situacao, tipo)
VALUES (300, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 'Mapeamento Geral 2026 - Secretaria 1', 'EM_ANDAMENTO', 'MAPEAMENTO');

-- UnidadeProcesso entries (Units 2, 3, 4, 5, 6, 7, 8)
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (300, 2, 'EM_ANDAMENTO'); -- SECRETARIA_1
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (300, 3, 'EM_ANDAMENTO'); -- ASSESSORIA_11
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (300, 4, 'AGUARDANDO');   -- ASSESSORIA_12
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (300, 5, 'EM_ANDAMENTO'); -- COORD_11
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (300, 6, 'EM_ANDAMENTO'); -- SECAO_111
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (300, 7, 'EM_ANDAMENTO'); -- SECAO_112
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (300, 8, 'AGUARDANDO');   -- SECAO_113

-- Subprocesso Unit 3 (ASSESSORIA_11): MAPEAMENTO_CADASTRO_EM_ANDAMENTO
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (303, 300, 3, 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO', CURRENT_TIMESTAMP);

-- Subprocesso Unit 6 (SECAO_111): MAPEAMENTO_MAPA_DISPONIBILIZADO
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (306, 300, 6, 'MAPEAMENTO_MAPA_DISPONIBILIZADO', CURRENT_TIMESTAMP);
-- Map for Unit 6
INSERT INTO sgc.mapa (codigo, subprocesso_codigo, data_hora_disponibilizado, data_hora_homologado, sugestoes)
VALUES (306, 306, CURRENT_TIMESTAMP, NULL, NULL);

-- Subprocesso Unit 7 (SECAO_112): MAPEAMENTO_MAPA_COM_SUGESTOES
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (307, 300, 7, 'MAPEAMENTO_MAPA_COM_SUGESTOES', CURRENT_TIMESTAMP);
-- Map for Unit 7 with suggestions
INSERT INTO sgc.mapa (codigo, subprocesso_codigo, data_hora_disponibilizado, data_hora_homologado, sugestoes)
VALUES (307, 307, CURRENT_TIMESTAMP, NULL, 'Revisar a descrição das atividades 1 e 2. Estão genéricas demais.');

-- Subprocesso Unit 5 (COORD_11): MAPEAMENTO_CADASTRO_HOMOLOGADO
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (305, 300, 5, 'MAPEAMENTO_CADASTRO_HOMOLOGADO', CURRENT_TIMESTAMP);

-- Processo 400: Diagnóstico de Competências 2026
INSERT INTO sgc.processo (codigo, data_criacao, data_finalizacao, data_limite, descricao, situacao, tipo)
VALUES (400, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 'Diagnóstico de Competências 2026', 'EM_ANDAMENTO', 'DIAGNOSTICO');

-- UnidadeProcesso (Units 6, 7, 8, 10)
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (400, 6, 'EM_ANDAMENTO'); -- SECAO_111
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (400, 7, 'EM_ANDAMENTO'); -- SECAO_112
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (400, 8, 'AGUARDANDO');   -- SECAO_113
INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo, situacao) VALUES (400, 10, 'AGUARDANDO');  -- SECAO_121

-- Subprocesso Unit 6 (SECAO_111): DIAGNOSTICO_MONITORAMENTO
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (406, 400, 6, 'DIAGNOSTICO_MONITORAMENTO', CURRENT_TIMESTAMP);

-- Subprocesso Unit 7 (SECAO_112): DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (407, 400, 7, 'DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO', CURRENT_TIMESTAMP);

-- Subprocesso Unit 8 (SECAO_113): NAO_INICIADO
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (408, 400, 8, 'NAO_INICIADO', CURRENT_TIMESTAMP);

-- Subprocesso Unit 10 (SECAO_121): NAO_INICIADO
INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES (410, 400, 10, 'NAO_INICIADO', CURRENT_TIMESTAMP);
