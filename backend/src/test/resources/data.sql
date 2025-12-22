DELETE FROM SGC.MOVIMENTACAO;
DELETE FROM SGC.ALERTA_USUARIO;
DELETE FROM SGC.ALERTA;
DELETE FROM SGC.ANALISE;
DELETE FROM SGC.NOTIFICACAO;
DELETE FROM SGC.UNIDADE_PROCESSO;
DELETE FROM SGC.SUBPROCESSO;
DELETE FROM SGC.PROCESSO;

DELETE FROM SGC.CONHECIMENTO;
DELETE FROM SGC.COMPETENCIA_ATIVIDADE;
DELETE FROM SGC.ATIVIDADE;

DELETE FROM SGC.ATRIBUICAO_TEMPORARIA;
DELETE FROM SGC.COMPETENCIA;
DELETE FROM SGC.VW_USUARIO_PERFIL_UNIDADE;
DELETE FROM SGC.ADMINISTRADOR;
DELETE FROM SGC.VW_VINCULACAO_UNIDADE;

DELETE FROM SGC.UNIDADE_MAPA;
DELETE FROM SGC.VW_USUARIO;
DELETE FROM SGC.VW_UNIDADE;
DELETE FROM SGC.MAPA;

DELETE FROM SGC.PARAMETRO;

-- -------------------------------------------------------------------------------------------------
-- VW_UNIDADE - simulada como tabela no H2
-- -------------------------------------------------------------------------------------------------
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('1', 'Tribunal Regional Eleitoral', 'TRE', 'INTEROPERACIONAL', 'ATIVA', NULL, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('2', 'Secretaria de Informática e Comunicações', 'STIC', 'INTEROPERACIONAL', 'ATIVA', NULL, '6');
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('100', 'ADMIN-UNIT', 'ADMIN-UNIT', 'INTEROPERACIONAL', 'ATIVA', NULL, '7');
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('200', 'Secretaria de Gestao de Pessoas', 'SGP', 'INTERMEDIARIA', 'ATIVA', NULL, '2');

INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('3', 'Coordenadoria de Administracao', 'COAD', 'INTERMEDIARIA', 'ATIVA', 2, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('6', 'Coordenadoria de Sistemas', 'COSIS', 'INTERMEDIARIA', 'ATIVA', 2, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('7', 'Coordenadoria de Suporte e Infraestrutura', 'COSINF', 'INTERMEDIARIA', 'ATIVA', 2, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('14', 'Coordenadoria Jurídica', 'COJUR', 'INTERMEDIARIA', 'ATIVA', 2, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('101', 'GESTOR-UNIT', 'GESTOR-UNIT', 'INTERMEDIARIA', 'ATIVA', 100, NULL);

INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('201', 'Coordenadoria de Atenção ao Servidor', 'CAS', 'INTEROPERACIONAL', 'ATIVA', 200, NULL);

INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('4', 'Coordenadoria de Educação Especial', 'COEDE', 'INTERMEDIARIA', 'ATIVA', 3, NULL);

INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('5', 'Seção Magistrados e Requisitados', 'SEMARE', 'OPERACIONAL', 'ATIVA', 4, '5');
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('8', 'Seção de Desenvolvimento de Sistemas', 'SEDESENV', 'OPERACIONAL', 'ATIVA', 6, '3');
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('9', 'Seção de Dados e Inteligência Artificial', 'SEDIA', 'OPERACIONAL', 'ATIVA', 6, '10');
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('10', 'Seção de Sistemas Eleitorais', 'SESEL', 'OPERACIONAL', 'ATIVA', 6, '1');
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('11', 'Seção de Infraestrutura', 'SENIC', 'OPERACIONAL', 'ATIVA', 7, '12');
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('12', 'Seção Jurídica', 'SEJUR', 'OPERACIONAL', 'ATIVA', 14, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('13', 'Seção de Processos', 'SEPRO', 'OPERACIONAL', 'ATIVA', 14, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('15', 'Seção de Documentação', 'SEDOC', 'OPERACIONAL', 'ATIVA', 2, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('102', 'SUB-UNIT', 'SUB-UNIT', 'OPERACIONAL', 'ATIVA', 101, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('202', 'Seção de Atenção ao Servidor', 'SAS', 'OPERACIONAL', 'ATIVA', 201, NULL);

-- Unidades para testes específicos
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('900', 'CDU04-UNIT', 'CDU04-UNIT', 'OPERACIONAL', 'ATIVA', 2, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('901', 'CDU05-REV-UNIT', 'CDU05-REV-UNIT', 'OPERACIONAL', 'ATIVA', 2, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('902', 'CDU05-SUB-UNIT', 'CDU05-SUB-UNIT', 'OPERACIONAL', 'ATIVA', 2, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('903', 'CDU05-ALERT-UNIT', 'CDU05-ALERT-UNIT', 'OPERACIONAL', 'ATIVA', 2, NULL);
INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular)
VALUES ('904', 'CDU05-READONLY-UNIT', 'CDU05-READONLY-UNIT', 'OPERACIONAL', 'ATIVA', 2, NULL);

-- -------------------------------------------------------------------------------------------------
-- UNIDADE_MAPA (relaciona unidades com mapas vigentes)
-- -------------------------------------------------------------------------------------------------
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES ('8', 1001);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES ('9', 1002);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES ('10', 1003);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES ('102', 1004);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES ('900', 1004);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES ('901', 1004);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES ('902', 1004);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES ('903', 1004);
INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES ('904', 1004);

-- -------------------------------------------------------------------------------------------------
-- VW_USUARIO - simulada como tabela no H2
-- -------------------------------------------------------------------------------------------------
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('1', 'Ana Paula Souza', 'ana.souza@tre-pe.jus.br', '1234', 10, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('2', 'Carlos Henrique Lima', 'carlos.lima@tre-pe.jus.br', '2345', 200, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('3', 'Fernanda Oliveira', 'fernanda.oliveira@tre-pe.jus.br', '3456', 8, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('4', 'João Batista Silva', 'joao.silva@tre-pe.jus.br', '4567', 10, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('5', 'Marina Dias', 'marina.dias@tre-pe.jus.br', '5678', 5, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('6', 'Ricardo Alves', 'ricardo.alves@tre-pe.jus.br', '6789', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('7', 'Zeca Silva', 'zeca.gado@tre-pe.jus.br', '7001', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('8', 'Paulo Horta', 'paulo.horta@tre-pe.jus.br', '7002', 8, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('9', 'Giuseppe Corleone', 'giuseppe.corleone@tre-pe.jus.br', '7003', 8, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('10', 'Paula Gonçalves', 'paula.goncalves@tre-pe.jus.br', '7004', 9, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('11', 'Herman Greely', 'herman.greely@tre-pe.jus.br', '7005', 10, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('12', 'Taís Condida', 'tais.condida@tre-pe.jus.br', '7006', 11, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('13', 'Mike Smith', 'mike.smith@tre-pe.jus.br', '7007', 11, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('14', 'Maroca Silva', 'maroca.silva@tre-pe.jus.br', '7008', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('15', 'Roberto Santos', 'roberto.santos@tre-pe.jus.br', '7009', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('16', 'Luciana Pereira', 'luciana.pereira@tre-pe.jus.br', '7010', 6, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('17', 'Fernando Costa', 'fernando.costa@tre-pe.jus.br', '7011', 10, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('18', 'Amanda Rodrigues', 'amanda.rodrigues@tre-pe.jus.br', '7012', 14, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('19', 'Diego Fernandes', 'diego.fernandes@tre-pe.jus.br', '7013', 6, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('20', 'Juliana Almeida', 'juliana.almeida@tre-pe.jus.br', '7014', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('21', 'Rafael Moreira', 'rafael.moreira@tre-pe.jus.br', '7015', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('22', 'Camila Barbosa', 'camila.barbosa@tre-pe.jus.br', '7016', 10, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('23', 'Thiago Carvalho', 'thiago.carvalho@tre-pe.jus.br', '7017', 14, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('24', 'Patrícia Lima', 'patricia.lima@tre-pe.jus.br', '7018', 6, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('25', 'Lucas Mendes', 'lucas.mendes@tre-pe.jus.br', '7019', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('26', 'Beatriz Santos', 'beatriz.santos@tre-pe.jus.br', '7020', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('27', 'Gustavo Oliveira', 'gustavo.oliveira@tre-pe.jus.br', '7021', 10, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('28', 'Carolina Souza', 'carolina.souza@tre-pe.jus.br', '7022', 14, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('29', 'Bruno Rodrigues', 'bruno.rodrigues@tre-pe.jus.br', '7023', 6, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('30', 'Mariana Costa', 'mariana.costa@tre-pe.jus.br', '7024', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('111111111111', 'Admin Teste', 'admin.teste@tre-pe.jus.br', '1111', 100, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('222222222222', 'Gestor Teste', 'gestor.teste@tre-pe.jus.br', '2222', 101, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('666666666666', 'Gestor COSIS', 'gestor.cosis@tre-pe.jus.br', '6666', 6, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('333333333333', 'Chefe Teste', 'chefe.teste@tre-pe.jus.br', '3333', 8, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('121212121212', 'Chefe SEJUR Teste', 'chefe.sejur@tre-pe.jus.br', '1212', 12, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('777', 'Chefe STIC Teste', 'chefe.stic@tre-pe.jus.br', '7777', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('999999999999', 'Usuario Multi Perfil', 'multi.perfil@tre-pe.jus.br', '9999', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('123456789012', 'João Silva', 'joao.silva@tre-pe.jus.br', '8001', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('987654321098', 'Maria Santos', 'maria.santos@tre-pe.jus.br', '8002', 2, '');
INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA)
VALUES ('111222333444', 'Pedro Oliveira', 'pedro.oliveira@tre-pe.jus.br', '8003', 2, '');
INSERT INTO SGC.VW_USUARIO (titulo, nome, unidade_lot_codigo, MATRICULA)
VALUES ('50001', 'João da Silva', 8, '');
INSERT INTO SGC.VW_USUARIO (titulo, nome, unidade_lot_codigo, MATRICULA)
VALUES ('50002', 'Maria Oliveira', 8, '');
INSERT INTO SGC.VW_USUARIO (titulo, nome, unidade_lot_codigo, MATRICULA)
VALUES ('50003', 'Pedro Santos', 9, '');
INSERT INTO SGC.VW_USUARIO (titulo, nome, unidade_lot_codigo, MATRICULA)
VALUES ('50004', 'Ana Costa', 9, '');
INSERT INTO SGC.VW_USUARIO (titulo, nome, unidade_lot_codigo, MATRICULA)
VALUES ('50005', 'Carlos Pereira', 10, '');
INSERT INTO SGC.VW_USUARIO (titulo, nome, unidade_lot_codigo, MATRICULA)
VALUES ('50006', 'Juliana Lima', 10, '');

-- -------------------------------------------------------------------------------------------------
-- PERFIS DE USUÁRIO (VW_USUARIO_PERFIL_UNIDADE - sem ID autoincrementado)
-- -------------------------------------------------------------------------------------------------
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('1', 'SERVIDOR', 10);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('2', 'CHEFE', 200);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('3', 'CHEFE', 8);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('6', 'ADMIN', 2);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('8', 'GESTOR', 8);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('777', 'CHEFE', 2);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('111111111111', 'ADMIN', 100);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('222222222222', 'GESTOR', 101);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('333333333333', 'CHEFE', 9);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('121212121212', 'CHEFE', 12);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('666666666666', 'GESTOR', 6);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('999999999999', 'ADMIN', 2);
INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES ('999999999999', 'GESTOR', 2);

-- -------------------------------------------------------------------------------------------------
-- ADMINISTRADORES (perfis ADMIN gerenciados pelo SGC)
-- -------------------------------------------------------------------------------------------------
INSERT INTO SGC.ADMINISTRADOR (usuario_titulo) VALUES ('6');
INSERT INTO SGC.ADMINISTRADOR (usuario_titulo) VALUES ('111111111111');
INSERT INTO SGC.ADMINISTRADOR (usuario_titulo) VALUES ('999999999999');

-- -------------------------------------------------------------------------------------------------
-- MAPAS, COMPETÊNCIAS, ATIVIDADES (DADOS BASE PARA REVISÃO)
-- -------------------------------------------------------------------------------------------------
-- OBS: Competências e atividades são inseridas junto com seus mapas correspondentes mais abaixo

INSERT INTO SGC.PROCESSO (codigo, descricao, situacao, data_criacao, tipo)
VALUES ('50000', 'Processo Teste A', 'EM_ANDAMENTO', CURRENT_TIMESTAMP, 'MAPEAMENTO');
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo)
VALUES ('50000', 8);
INSERT INTO SGC.ALERTA (codigo, processo_codigo, usuario_destino_titulo, descricao, data_hora)
VALUES ('70000', 50000, 50001, 'Alerta de teste para processo A', CURRENT_TIMESTAMP);

INSERT INTO SGC.PROCESSO (codigo, descricao, situacao, data_criacao, tipo)
VALUES ('50001', 'Processo Teste B', 'FINALIZADO', CURRENT_TIMESTAMP, 'MAPEAMENTO');
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo)
VALUES ('50001', 9);
INSERT INTO SGC.ALERTA (codigo, processo_codigo, usuario_destino_titulo, descricao, data_hora)
VALUES ('70001', 50001, 50003, 'Alerta de teste para processo B', CURRENT_TIMESTAMP);

-- Alertas específicos para CDU-02
INSERT INTO SGC.ALERTA (codigo, processo_codigo, usuario_destino_titulo, descricao, data_hora)
VALUES ('70002', 50000, '8', 'Alerta para Gestor', CURRENT_TIMESTAMP);

INSERT INTO SGC.ALERTA (codigo, processo_codigo, unidade_destino_codigo, descricao, data_hora)
VALUES ('70003', 50000, 6, 'Alerta para Unidade Filha 1', CURRENT_TIMESTAMP);

INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES ('60000', 50000, 8, 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO', CURRENT_TIMESTAMP);

INSERT INTO SGC.MAPA (codigo, subprocesso_codigo) VALUES (1001, 60000);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES ('10001', 1001, 'Desenvolvimento em Java');
INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES ('10002', 1001, 'Desenvolvimento em Vue.js');

INSERT INTO SGC.MOVIMENTACAO (codigo, subprocesso_codigo, usuario_titulo, descricao, data_hora)
VALUES ('80000', 60000, '50001', 'INICIADO', CURRENT_TIMESTAMP);

-- DADOS ADICIONAIS PARA CDU-17 e CDU-19 e CDU-02
-- Processo 1700 (para CDU-17)
INSERT INTO SGC.PROCESSO (codigo, descricao, situacao, data_criacao, tipo, data_limite)
VALUES ('1700', 'Processo para CDU-17', 'EM_ANDAMENTO', CURRENT_TIMESTAMP, 'MAPEAMENTO', '2025-12-31 23:59:59');
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo)
VALUES ('1700', 8);

-- Mapa e Subprocesso para CDU-17
INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES ('1700', 1700, 8, 'REVISAO_CADASTRO_HOMOLOGADA', '2025-12-31 23:59:59');

INSERT INTO SGC.MAPA (codigo, subprocesso_codigo)
VALUES (1700, 1700);

INSERT INTO SGC.ATIVIDADE (codigo, mapa_codigo, descricao)
VALUES ('17001', 1700, 'Atividade Teste CDU-17');
INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES ('17001', 1700, 'Competência Teste CDU-17');
INSERT INTO SGC.COMPETENCIA_ATIVIDADE (atividade_codigo, competencia_codigo)
VALUES ('17001', 17001);

-- Processo CRIADO (para CDU-02 teste de Admin)
INSERT INTO SGC.PROCESSO (codigo, descricao, situacao, data_criacao, tipo, data_limite)
VALUES ('2000', 'Processo Criado', 'CRIADO', CURRENT_TIMESTAMP, 'MAPEAMENTO', '2025-12-31 23:59:59');
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo)
VALUES ('2000', 8);

-- Vincula Mapa 1001 ao Subprocesso 60000 (já existente, Processo 50000 - EM ANDAMENTO)
UPDATE SGC.MAPA SET subprocesso_codigo = 60000 WHERE codigo = 1001;

-- Processo 50002 (FINALIZADO) para abrigar subprocessos históricos que não devem bloquear novos processos
INSERT INTO SGC.PROCESSO (codigo, descricao, situacao, data_criacao, tipo, data_limite)
VALUES ('50002', 'Processo Histórico Finalizado', 'FINALIZADO', CURRENT_TIMESTAMP, 'MAPEAMENTO', CURRENT_TIMESTAMP);

-- Mapa 1002 (Unidade 9) -> Processo 50001 (FINALIZADO)
INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES ('60002', 50001, 9, 'DIAGNOSTICO_CONCLUIDO', CURRENT_TIMESTAMP);

INSERT INTO SGC.MAPA (codigo, subprocesso_codigo) VALUES (1002, 60002);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES ('10003', 1002, 'Análise de Dados');
INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES ('10004', 1002, 'Machine Learning');

-- Mapa 1003 (Unidade 10) -> Processo 50002 (FINALIZADO)
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES ('50002', 10);
INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES ('60003', 50002, 10, 'MAPEAMENTO_MAPA_HOMOLOGADO', CURRENT_TIMESTAMP);

INSERT INTO SGC.MAPA (codigo, subprocesso_codigo) VALUES (1003, 60003);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES ('10005', 1003, 'Segurança da Informação');
INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES ('10006', 1003, 'Gestão de Projetos');

-- Mapa 1004 (Unidade 102) -> Processo 50002 (FINALIZADO)
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES ('50002', 102);
INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES ('60004', 50002, 102, 'MAPEAMENTO_MAPA_HOMOLOGADO', CURRENT_TIMESTAMP);

INSERT INTO SGC.MAPA (codigo, subprocesso_codigo) VALUES (1004, 60004);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES ('10007', 1004, 'Gestão Administrativa');

INSERT INTO SGC.ATIVIDADE (codigo, mapa_codigo, descricao)
VALUES ('30000', 1004, 'Realizar atendimento presencial');
INSERT INTO SGC.CONHECIMENTO (codigo, atividade_codigo, descricao)
VALUES ('40000', 30000, 'Atendimento ao público');
INSERT INTO SGC.COMPETENCIA_ATIVIDADE (atividade_codigo, competencia_codigo)
VALUES ('30000', 10007);

-- Mapa 201 (Unidade 201) -> Processo 50002 (FINALIZADO)
INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES ('50002', 201);
INSERT INTO SGC.SUBPROCESSO (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1)
VALUES ('60201', 50002, 201, 'MAPEAMENTO_MAPA_HOMOLOGADO', CURRENT_TIMESTAMP);

INSERT INTO SGC.MAPA (codigo, subprocesso_codigo) VALUES (201, 60201);

INSERT INTO SGC.COMPETENCIA (codigo, mapa_codigo, descricao)
VALUES ('20001', 201, 'Gestão Administrativa');
INSERT INTO SGC.ATIVIDADE (codigo, mapa_codigo, descricao)
VALUES ('30001', 201, 'Realizar atendimento presencial');
INSERT INTO SGC.CONHECIMENTO (codigo, atividade_codigo, descricao)
VALUES ('40001', 30001, 'Atendimento ao público');
INSERT INTO SGC.COMPETENCIA_ATIVIDADE (atividade_codigo, competencia_codigo)
VALUES ('30001', 20001);

-- -------------------------------------------------------------------------------------------------
-- RESTART SEQUENCES para evitar conflitos de ID em testes
-- -------------------------------------------------------------------------------------------------
ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 1000;
ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 60000;
ALTER TABLE SGC.MAPA ALTER COLUMN CODIGO RESTART WITH 2000;
ALTER TABLE SGC.SUBPROCESSO ALTER COLUMN CODIGO RESTART WITH 70000;
ALTER TABLE SGC.COMPETENCIA ALTER COLUMN CODIGO RESTART WITH 30000;
ALTER TABLE SGC.ATIVIDADE ALTER COLUMN CODIGO RESTART WITH 50000;
ALTER TABLE SGC.CONHECIMENTO ALTER COLUMN CODIGO RESTART WITH 50000;
ALTER TABLE SGC.NOTIFICACAO ALTER COLUMN CODIGO RESTART WITH 1000;
ALTER TABLE SGC.ALERTA ALTER COLUMN CODIGO RESTART WITH 80000;
ALTER TABLE SGC.MOVIMENTACAO ALTER COLUMN CODIGO RESTART WITH 90000;
ALTER TABLE SGC.ANALISE ALTER COLUMN CODIGO RESTART WITH 1000;
