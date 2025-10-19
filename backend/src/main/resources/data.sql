-- Deletes para garantir a limpeza do banco antes de inserir novos dados
DELETE FROM SGC.USUARIO_PERFIL;
DELETE FROM SGC.USUARIO;
DELETE FROM SGC.UNIDADE;

-- Unidade Raiz (Tribunal)
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES
(1, 'TRIBUNAL REGIONAL ELEITORAL DE PERNAMBUCO', 'TRE-PE', 'INTEROPERACIONAL', NULL);

-- Unidades Principais
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES
(2, 'Secretaria de Gestão de Pessoas', 'SGP', 'INTERMEDIARIA', 1),
(3, 'Secretaria de Tecnologia da Informação', 'STIC', 'INTERMEDIARIA', 1);

-- Subunidades da SGP
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES
(4, 'Coordenadoria de Desenvolvimento', 'CODES', 'OPERACIONAL', 2),
(5, 'Seção de Lotação e Desempenho', 'SELDES', 'OPERACIONAL', 4);

-- Subunidades da STIC
INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES
(6, 'Coordenadoria de Sistemas', 'COSIS', 'OPERACIONAL', 3),
(7, 'Seção de Sistemas Eleitorais', 'SESEL', 'OPERACIONAL', 6),
(8, 'Seção de Desenvolvimento', 'SEDES', 'OPERACIONAL', 6),
(9, 'Seção de Dados e IA', 'SEDIA', 'OPERACIONAL', 6);

-- Usuários
-- Adicionando um usuário ADMIN para a unidade raiz e GESTOR para STIC
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES
(1, 'Admin Geral', 'admin.geral@tre-pe.jus.br', '0001', 1),
(2, 'Gestor STIC', 'gestor.stic@tre-pe.jus.br', '0002', 3),
(3, 'Chefe CODES', 'chefe.codes@tre-pe.jus.br', '0003', 4),
(4, 'Servidor SELDES', 'servidor.seldes@tre-pe.jus.br', '0004', 5),
(5, 'Chefe SESEL', 'chefe.sesel@tre-pe.jus.br', '0005', 7),
(6, 'Servidor SEDES', 'servidor.sedes@tre-pe.jus.br', '0006', 8),
(7, 'Chefe SEDIA', 'chefe.sedia@tre-pe.jus.br', '0007', 9);

-- Perfis dos Usuários
INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES
(1, 'ADMIN'),
(2, 'GESTOR'),
(3, 'CHEFE'),
(4, 'SERVIDOR'),
(5, 'CHEFE'),
(6, 'SERVIDOR'),
(7, 'CHEFE');
