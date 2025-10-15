-- Unidades
INSERT INTO SGC.UNIDADE (ID, NOME, SIGLA, TIPO, EMAIL, UNIDADE_SUPERIOR_ID) VALUES
(1, 'Seção de Desenvolvimento Organizacional e Capacitação', 'SEDOC', 'ADMINISTRATIVA', NULL, NULL),
(2, 'Secretaria de Informática e Comunicações', 'STIC', 'INTEROPERACIONAL', NULL, 1),
(3, 'Secretaria de Gestao de Pessoas', 'SGP', 'INTERMEDIARIA', NULL, 1),
(4, 'Coordenadoria de Educação Especial', 'COEDE', 'INTERMEDIARIA', NULL, 3),
(5, 'Seção Magistrados e Requisitados', 'SEMARE', 'OPERACIONAL', NULL, 4),
(6, 'Coordenadoria de Sistemas', 'COSIS', 'INTERMEDIARIA', NULL, 2),
(7, 'Coordenadoria de Suporte e Infraestrutura', 'COSINF', 'INTERMEDIARIA', NULL, 2),
(8, 'Seção de Desenvolvimento de Sistemas', 'SEDESENV', 'OPERACIONAL', NULL, 6),
(9, 'Seção de Dados e Inteligência Artificial', 'SEDIA', 'OPERACIONAL', NULL, 6),
(10, 'Seção de Sistemas Eleitorais', 'SESEL', 'OPERACIONAL', NULL, 6),
(11, 'Seção de Infraestrutura', 'SENIC', 'OPERACIONAL', NULL, 7),
(12, 'Seção Jurídica', 'SEJUR', 'OPERACIONAL', NULL, 14),
(13, 'Seção de Processos', 'SEPRO', 'OPERACIONAL', NULL, 14),
(14, 'Coordenadoria Jurídica', 'COJUR', 'INTERMEDIARIA', NULL, 2);

-- Usuários
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, UNIDADE_ID) VALUES
(1, 'Ana Paula Souza', 'ana.souza@tre-pe.jus.br', '1234', 10),
(2, 'Carlos Henrique Lima', 'carlos.lima@tre-pe.jus.br', '2345', 3),
(3, 'Fernanda Oliveira', 'fernanda.oliveira@tre-pe.jus.br', '3456', 8),
(4, 'João Batista Silva', 'joao.silva@tre-pe.jus.br', '4567', 10),
(5, 'Marina Dias', 'marina.dias@tre-pe.jus.br', '5678', 5),
(6, 'Ricardo Alves', 'ricardo.alves@tre-pe.jus.br', '6789', 1),
(7, 'Zeca Silva', 'zeca.gado@tre-pe.jus.br', '7001', 1),
(8, 'Paulo Horta', 'paulo.horta@tre-pe.jus.br', '7002', 8),
(9, 'Giuseppe Corleone', 'giuseppe.corleone@tre-pe.jus.br', '7003', 8),
(10, 'Paula Gonçalves', 'paula.goncalves@tre-pe.jus.br', '7004', 9),
(11, 'Herman Greely', 'herman.greely@tre-pe.jus.br', '7005', 10),
(12, 'Taís Condida', 'tais.condida@tre-pe.jus.br', '7006', 11),
(13, 'Mike Smith', 'mike.smith@tre-pe.jus.br', '7007', 11),
(14, 'Maroca Silva', 'maroca.silva@tre-pe.jus.br', '7008', 2),
(15, 'Roberto Santos', 'roberto.santos@tre-pe.jus.br', '7009', 14),
(16, 'Luciana Pereira', 'luciana.pereira@tre-pe.jus.br', '7010', 6),
(17, 'Fernando Costa', 'fernando.costa@tre-pe.jus.br', '7011', 10),
(18, 'Amanda Rodrigues', 'amanda.rodrigues@tre-pe.jus.br', '7012', 14),
(19, 'Diego Fernandes', 'diego.fernandes@tre-pe.jus.br', '7013', 6),
(20, 'Juliana Almeida', 'juliana.almeida@tre-pe.jus.br', '7014', 2),
(21, 'Rafael Moreira', 'rafael.moreira@tre-pe.jus.br', '7015', 1),
(22, 'Camila Barbosa', 'camila.barbosa@tre-pe.jus.br', '7016', 10),
(23, 'Thiago Carvalho', 'thiago.carvalho@tre-pe.jus.br', '7017', 14),
(24, 'Patrícia Lima', 'patricia.lima@tre-pe.jus.br', '7018', 6),
(25, 'Lucas Mendes', 'lucas.mendes@tre-pe.jus.br', '7019', 2),
(26, 'Beatriz Santos', 'beatriz.santos@tre-pe.jus.br', '7020', 1),
(27, 'Gustavo Oliveira', 'gustavo.oliveira@tre-pe.jus.br', '7021', 10),
(28, 'Carolina Souza', 'carolina.souza@tre-pe.jus.br', '7022', 14),
(29, 'Bruno Rodrigues', 'bruno.rodrigues@tre-pe.jus.br', '7023', 6),
(30, 'Mariana Costa', 'mariana.costa@tre-pe.jus.br', '7024', 2);

-- Perfis
INSERT INTO SGC.USUARIO_PERFIS (USUARIO_TITULO_ELEITORAL, PERFIS) VALUES
(1, 'SERVIDOR'),
(2, 'CHEFE'),
(3, 'CHEFE'),
(4, 'CHEFE'),
(5, 'CHEFE'),
(6, 'CHEFE'),
(7, 'CHEFE'),
(8, 'GESTOR'),
(9, 'CHEFE'),
(10, 'CHEFE'),
(11, 'SERVIDOR'),
(12, 'CHEFE'),
(13, 'GESTOR'),
(14, 'SERVIDOR'),
(15, 'CHEFE'),
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
(30, 'SERVIDOR');

-- Mapas
INSERT INTO SGC.MAPA (ID, NOME, DESCRICAO, DATA_CRIACAO) VALUES
(1, 'Mapa de Competências Técnicas', 'Mapa de competências técnicas para as unidades da SGP', '2023-01-10'),
(2, 'Mapa de Competências Comportamentais', 'Mapa de competências comportamentais para as unidades da SGP', '2023-01-10'),
(3, 'Mapa de Competências Gerenciais', 'Mapa de competências gerenciais para as unidades da SGP', '2023-01-10');

-- Processos
INSERT INTO SGC.PROCESSO (ID, TIPO, NUMERO_PROCESSO, SITUACAO, DATA_CRIACAO, MAPA_ATUAL_ID, UNIDADE_RESPONSAVEL_ID) VALUES
(1, 'MAPEAMENTO', 'PRO-2023-001', 'EM_ANDAMENTO', '2023-02-01', 1, 1),
(2, 'REVISAO', 'PRO-2023-002', 'CONCLUIDO', '2023-03-15', 2, 1),
(3, 'DIAGNOSTICO', 'PRO-2023-003', 'EM_ANDAMENTO', '2023-04-20', 3, 2);

-- Subprocessos
INSERT INTO SGC.SUBPROCESSO (ID, PROCESSO_ID, UNIDADE_ID, MAPA_ID, SITUACAO, DATA_LIMITE_ETAPA1, DATA_CONCLUSAO) VALUES
(1, 1, 3, 1, 'EM_ELABORACAO', '2023-03-01', NULL),
(2, 1, 4, 1, 'EM_ELABORACAO', '2023-03-01', NULL),
(3, 2, 3, 2, 'CONCLUIDO', '2023-04-15', '2023-04-10'),
(4, 3, 6, 3, 'AGUARDANDO_VALIDACAO', '2023-05-20', NULL),
(5, 3, 7, 3, 'EM_ANALISE', '2023-05-20', NULL);

-- Atividades
INSERT INTO SGC.ATIVIDADE (ID, MAPA_ID, DESCRICAO) VALUES
(1, 1, 'Realizar levantamento de necessidades de treinamento'),
(2, 1, 'Elaborar plano de capacitação anual'),
(3, 2, 'Conduzir avaliação de desempenho'),
(4, 2, 'Aplicar feedback 360 graus'),
(5, 3, 'Desenvolver plano de sucessão'),
(6, 3, 'Mapear competências de liderança');

-- Conhecimentos
INSERT INTO SGC.CONHECIMENTO (ID, DESCRICAO, ATIVIDADE_ID) VALUES
(1, 'Técnicas de levantamento de necessidades', 1),
(2, 'Metodologias de elaboração de planos de capacitação', 2),
(3, 'Ferramentas de avaliação de desempenho', 3),
(4, 'Técnicas de feedback', 4),
(5, 'Modelos de plano de sucessão', 5),
(6, 'Frameworks de competências de liderança', 6);

-- Competências
INSERT INTO SGC.COMPETENCIA (ID, DESCRICAO, MAPA_ID) VALUES
(1, 'Análise de Dados', 1),
(2, 'Planejamento Estratégico', 1),
(3, 'Comunicação Interpessoal', 2),
(4, 'Feedback Efetivo', 2),
(5, 'Visão Sistêmica', 3),
(6, 'Liderança Inspiradora', 3);

-- Competência-Atividade
INSERT INTO SGC.COMPETENCIA_ATIVIDADE (COMPETENCIA_ID, ATIVIDADE_ID) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6);

-- Alertas
INSERT INTO SGC.ALERTA (ID, TITULO, DESCRICAO, TIPO, DATA_CRIACAO) VALUES
(1, 'Prazo para elaboração do mapa de competências', 'O prazo para elaboração do mapa de competências da unidade SGP está próximo do fim.', 'AVISO', '2023-02-20'),
(2, 'Validação de mapa de competências pendente', 'O mapa de competências da unidade COSIS aguarda sua validação.', 'PENDENCIA', '2023-05-22');

-- Alerta-Usuário
INSERT INTO SGC.ALERTA_USUARIO (ALERTA_ID, USUARIO_TITULO_ELEITORAL, DATA_CIENCIA) VALUES
(1, 2, NULL),
(2, 16, NULL);

-- Análises
INSERT INTO SGC.ANALISE (ID, SUBPROCESSO_ID, DATA_HORA, TIPO_ACAO, DESCRICAO, UNIDADE_SIGLA, USUARIO_NOME) VALUES
(1, 4, '2023-05-21 10:00:00', 'DEVOLUCAO', 'O mapa de competências precisa de ajustes na descrição das atividades.', 'SGP', 'Carlos Henrique Lima'),
(2, 5, '2023-05-23 15:30:00', 'ACEITE', 'O mapa de competências está de acordo com o esperado.', 'SGP', 'Carlos Henrique Lima');