-- Script Oracle puro para montar o mapa SESEL completo em homologacao.
-- Arquivo gerado a partir de mapa-sesel.md e da massa do H2, com usuarios reais de homologacao.

DECLARE
    v_processo_codigo NUMBER;
    v_subprocesso_codigo NUMBER;
    v_mapa_codigo NUMBER;
    v_unidade_codigo NUMBER;
    v_unidade_superior_codigo NUMBER;
    v_unidade_nome VARCHAR2(255);
    v_unidade_sigla VARCHAR2(20);
    v_matricula_titular VARCHAR2(8);
    v_titulo_titular VARCHAR2(12);
    v_data_inicio_titularidade DATE;
    v_tipo_unidade VARCHAR2(20);
    v_situacao_unidade VARCHAR2(20);
    v_admin_1 VARCHAR2(12);
    v_admin_2 VARCHAR2(12);
    v_gestor VARCHAR2(12);
    v_chefe VARCHAR2(12);
    v_servidor VARCHAR2(12);
    v_email_gestor VARCHAR2(255);
    v_email_chefe VARCHAR2(255);
    v_email_servidor VARCHAR2(255);
    v_agora TIMESTAMP := SYSTIMESTAMP;
    v_descricao_processo CONSTANT VARCHAR2(255) := 'Mapa SESEL - Exemplo Oracle';
    TYPE t_codigo_por_ordem IS TABLE OF NUMBER INDEX BY PLS_INTEGER;
    v_competencias t_codigo_por_ordem;
    v_atividades t_codigo_por_ordem;

    PROCEDURE limpar_dependentes_processo(p_codigo NUMBER) IS
    BEGIN
        DELETE FROM SGC.NOTIFICACAO_EMAIL WHERE SUBPROCESSO_CODIGO IN (
            SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
        );
        DELETE FROM SGC.ALERTA_USUARIO WHERE ALERTA_CODIGO IN (
            SELECT codigo FROM SGC.ALERTA WHERE PROCESSO_CODIGO = p_codigo
        );
        DELETE FROM SGC.ALERTA WHERE PROCESSO_CODIGO = p_codigo;
        DELETE FROM SGC.CONHECIMENTO WHERE ATIVIDADE_CODIGO IN (
            SELECT codigo FROM SGC.ATIVIDADE WHERE MAPA_CODIGO IN (
                SELECT codigo FROM SGC.MAPA WHERE SUBPROCESSO_CODIGO IN (
                    SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
                )
            )
        );
        DELETE FROM SGC.COMPETENCIA_ATIVIDADE WHERE ATIVIDADE_CODIGO IN (
            SELECT codigo FROM SGC.ATIVIDADE WHERE MAPA_CODIGO IN (
                SELECT codigo FROM SGC.MAPA WHERE SUBPROCESSO_CODIGO IN (
                    SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
                )
            )
        );
        DELETE FROM SGC.COMPETENCIA_ATIVIDADE WHERE COMPETENCIA_CODIGO IN (
            SELECT codigo FROM SGC.COMPETENCIA WHERE MAPA_CODIGO IN (
                SELECT codigo FROM SGC.MAPA WHERE SUBPROCESSO_CODIGO IN (
                    SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
                )
            )
        );
        DELETE FROM SGC.UNIDADE_MAPA WHERE MAPA_VIGENTE_CODIGO IN (
            SELECT codigo FROM SGC.MAPA WHERE SUBPROCESSO_CODIGO IN (
                SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
            )
        );
        DELETE FROM SGC.ATIVIDADE WHERE MAPA_CODIGO IN (
            SELECT codigo FROM SGC.MAPA WHERE SUBPROCESSO_CODIGO IN (
                SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
            )
        );
        DELETE FROM SGC.COMPETENCIA WHERE MAPA_CODIGO IN (
            SELECT codigo FROM SGC.MAPA WHERE SUBPROCESSO_CODIGO IN (
                SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
            )
        );
        DELETE FROM SGC.MAPA WHERE SUBPROCESSO_CODIGO IN (
            SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
        );
        DELETE FROM SGC.ANALISE WHERE SUBPROCESSO_CODIGO IN (
            SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
        );
        DELETE FROM SGC.MOVIMENTACAO WHERE SUBPROCESSO_CODIGO IN (
            SELECT codigo FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo
        );
        DELETE FROM SGC.SUBPROCESSO WHERE PROCESSO_CODIGO = p_codigo;
        DELETE FROM SGC.UNIDADE_PROCESSO WHERE PROCESSO_CODIGO = p_codigo;
    END;
BEGIN
    -- Seed puro do exemplo SESEL.
    -- A limpeza oficial do processo fica na tela de administracao de homologacao.
    SELECT codigo, nome, sigla, matricula_titular, titulo_titular, data_inicio_titularidade, tipo, situacao, unidade_superior_codigo
    INTO v_unidade_codigo, v_unidade_nome, v_unidade_sigla, v_matricula_titular, v_titulo_titular, v_data_inicio_titularidade, v_tipo_unidade, v_situacao_unidade, v_unidade_superior_codigo
    FROM (
        SELECT codigo, nome, sigla, matricula_titular, titulo_titular, data_inicio_titularidade, tipo, situacao, unidade_superior_codigo
        FROM SGC.VW_UNIDADE
        WHERE sigla = 'SESEL'
        ORDER BY codigo DESC
    )
    WHERE ROWNUM = 1;
    SELECT titulo INTO v_admin_1 FROM (SELECT DISTINCT titulo FROM SGC.VW_USUARIO WHERE titulo = '039703250884' ORDER BY titulo) WHERE ROWNUM = 1;
    SELECT titulo INTO v_admin_2 FROM (SELECT DISTINCT titulo FROM SGC.VW_USUARIO WHERE titulo = '025545511252' ORDER BY titulo) WHERE ROWNUM = 1;
    SELECT titulo INTO v_gestor FROM (SELECT DISTINCT titulo FROM SGC.VW_USUARIO WHERE titulo = '050290130841' ORDER BY titulo) WHERE ROWNUM = 1;
    SELECT titulo INTO v_chefe FROM (SELECT DISTINCT titulo FROM SGC.VW_USUARIO WHERE titulo = '045098980809' ORDER BY titulo) WHERE ROWNUM = 1;
    SELECT titulo INTO v_servidor FROM (SELECT DISTINCT titulo FROM SGC.VW_USUARIO WHERE titulo = '072233370809' ORDER BY titulo) WHERE ROWNUM = 1;
    SELECT email INTO v_email_gestor FROM (SELECT DISTINCT email FROM SGC.VW_USUARIO WHERE titulo = v_gestor ORDER BY email) WHERE ROWNUM = 1;
    SELECT email INTO v_email_chefe FROM (SELECT DISTINCT email FROM SGC.VW_USUARIO WHERE titulo = v_chefe ORDER BY email) WHERE ROWNUM = 1;
    SELECT email INTO v_email_servidor FROM (SELECT DISTINCT email FROM SGC.VW_USUARIO WHERE titulo = v_servidor ORDER BY email) WHERE ROWNUM = 1;

    MERGE INTO SGC.ADMINISTRADOR a
    USING (SELECT v_admin_1 AS usuario_titulo FROM dual) src
    ON (a.usuario_titulo = src.usuario_titulo)
    WHEN NOT MATCHED THEN INSERT (usuario_titulo) VALUES (src.usuario_titulo);
    MERGE INTO SGC.ADMINISTRADOR a
    USING (SELECT v_admin_2 AS usuario_titulo FROM dual) src
    ON (a.usuario_titulo = src.usuario_titulo)
    WHEN NOT MATCHED THEN INSERT (usuario_titulo) VALUES (src.usuario_titulo);

    BEGIN
        SELECT codigo INTO v_processo_codigo
        FROM (
            SELECT codigo
            FROM SGC.PROCESSO
            WHERE descricao = v_descricao_processo
            ORDER BY codigo DESC
        )
        WHERE ROWNUM = 1;

        limpar_dependentes_processo(v_processo_codigo);

        UPDATE SGC.PROCESSO
        SET data_criacao = v_agora - INTERVAL '4' DAY,
            data_finalizacao = v_agora - INTERVAL '1' DAY,
            data_limite = SYSDATE + 30,
            descricao = v_descricao_processo,
            situacao = 'FINALIZADO',
            tipo = 'MAPEAMENTO'
        WHERE codigo = v_processo_codigo;

        FOR r IN (
            SELECT codigo
            FROM SGC.PROCESSO
            WHERE descricao = v_descricao_processo
              AND codigo <> v_processo_codigo
        ) LOOP
            limpar_dependentes_processo(r.codigo);
            DELETE FROM SGC.PROCESSO WHERE codigo = r.codigo;
        END LOOP;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            INSERT INTO SGC.PROCESSO (data_criacao, data_finalizacao, data_limite, descricao, situacao, tipo)
            VALUES (v_agora - INTERVAL '4' DAY, v_agora - INTERVAL '1' DAY, SYSDATE + 30, v_descricao_processo, 'FINALIZADO', 'MAPEAMENTO')
            RETURNING codigo INTO v_processo_codigo;
    END;

    INSERT INTO SGC.UNIDADE_PROCESSO (
        processo_codigo, unidade_codigo, nome, sigla, matricula_titular, titulo_titular,
        data_inicio_titularidade, tipo, situacao, unidade_superior_codigo
    ) VALUES (
        v_processo_codigo, v_unidade_codigo, v_unidade_nome, v_unidade_sigla, v_matricula_titular, v_titulo_titular,
        v_data_inicio_titularidade, v_tipo_unidade, v_situacao_unidade, v_unidade_superior_codigo
    );

    INSERT INTO SGC.SUBPROCESSO (
        processo_codigo, unidade_codigo, data_limite_etapa1, data_fim_etapa1, data_limite_etapa2, data_fim_etapa2, situacao
    ) VALUES (
        v_processo_codigo, v_unidade_codigo, SYSDATE + 14, v_agora - INTERVAL '1' DAY, NULL, NULL, 'MAPEAMENTO_MAPA_HOMOLOGADO'
    ) RETURNING codigo INTO v_subprocesso_codigo;

    INSERT INTO SGC.MAPA (
        subprocesso_codigo, data_hora_disponibilizado, observacoes_disponibilizacao, sugestoes, data_hora_homologado
    ) VALUES (
        v_subprocesso_codigo, v_agora - INTERVAL '2' DAY, 'Mapa SESEL disponibilizado para validação', NULL, v_agora - INTERVAL '1' DAY
    ) RETURNING codigo INTO v_mapa_codigo;

    DELETE FROM SGC.UNIDADE_MAPA WHERE UNIDADE_CODIGO = v_unidade_codigo;
    INSERT INTO SGC.UNIDADE_MAPA (unidade_codigo, mapa_vigente_codigo) VALUES (v_unidade_codigo, v_mapa_codigo);

    -- Competencias e atividades do exemplo completo.
    FOR r IN (
        SELECT 1 AS ord, 'Prestar suporte técnico às etapas do processo eleitoral' AS descricao FROM dual
        UNION ALL         SELECT 2 AS ord, 'Gerenciar sistemas eleitorais informatizados' AS descricao FROM dual
        UNION ALL         SELECT 3 AS ord, 'Gerenciar simulados eleitorais' AS descricao FROM dual
        UNION ALL         SELECT 4 AS ord, 'Elaborar roteiros técnicos de sistemas eleitorais' AS descricao FROM dual
        UNION ALL         SELECT 5 AS ord, 'Elaborar e ministrar treinamentos eleitorais' AS descricao FROM dual
        UNION ALL         SELECT 6 AS ord, 'Gerenciar projetos de desenvolvimento de sistemas informatizados' AS descricao FROM dual
        UNION ALL         SELECT 7 AS ord, 'Gerenciar a configuração e o ambiente de desenvolvimento de sistemas' AS descricao FROM dual
        UNION ALL         SELECT 8 AS ord, 'Realizar levantamento de requisitos de sistemas informatizados' AS descricao FROM dual
        UNION ALL         SELECT 9 AS ord, 'Realizar análise e projeto de sistemas informatizados' AS descricao FROM dual
        UNION ALL         SELECT 10 AS ord, 'Implementar serviços e sistemas informatizados' AS descricao FROM dual
        UNION ALL         SELECT 11 AS ord, 'Aplicar padrões de design, usabilidade, acessibilidade, privacidade e segurança' AS descricao FROM dual
        UNION ALL         SELECT 12 AS ord, 'Coordenar a implantação de sistemas informatizados' AS descricao FROM dual
        UNION ALL         SELECT 13 AS ord, 'Testar sistemas informatizados' AS descricao FROM dual
        UNION ALL         SELECT 14 AS ord, 'Prestar suporte técnico especializado aos sistemas' AS descricao FROM dual
        UNION ALL         SELECT 15 AS ord, 'Atuar como integrante demandante no planejamento de contratações' AS descricao FROM dual
        UNION ALL         SELECT 16 AS ord, 'Atuar como integrante técnico no planejamento de contratações' AS descricao FROM dual
        UNION ALL         SELECT 17 AS ord, 'Atuar como fiscal técnico na execução de contratações' AS descricao FROM dual
    ) LOOP
        INSERT INTO SGC.COMPETENCIA (mapa_codigo, descricao) VALUES (v_mapa_codigo, r.descricao) RETURNING codigo INTO v_competencias(r.ord);
    END LOOP;

    FOR r IN (
        SELECT 1 AS ord, 1 AS competencia_ord, 'Realizar a preparação, carga e lacração das urnas eletrônicas para os pleitos.' AS descricao FROM dual
        UNION ALL         SELECT 2 AS ord, 1 AS competencia_ord, 'Coordenar o suporte técnico às Juntas Eleitorais e locais de votação no dia da eleição.' AS descricao FROM dual
        UNION ALL         SELECT 3 AS ord, 1 AS competencia_ord, 'Executar procedimentos de contingência.' AS descricao FROM dual
        UNION ALL         SELECT 4 AS ord, 2 AS competencia_ord, 'Monitorar o desempenho e a disponibilidade dos sistemas de votação e totalização.' AS descricao FROM dual
        UNION ALL         SELECT 5 AS ord, 2 AS competencia_ord, 'Configurar parâmetros de segurança e acesso aos sistemas eleitorais.' AS descricao FROM dual
        UNION ALL         SELECT 6 AS ord, 2 AS competencia_ord, 'Realizar a transmissão e o processamento de arquivos de resultado.' AS descricao FROM dual
        UNION ALL         SELECT 7 AS ord, 3 AS competencia_ord, 'Planejar e executar testes de estresse e carga nos sistemas de totalização.' AS descricao FROM dual
        UNION ALL         SELECT 8 AS ord, 3 AS competencia_ord, 'Simular cenários de falha de hardware e rede para validar planos de contingência.' AS descricao FROM dual
        UNION ALL         SELECT 9 AS ord, 3 AS competencia_ord, 'Coordenar simulados regionais envolvendo técnicos de diversas unidades.' AS descricao FROM dual
        UNION ALL         SELECT 10 AS ord, 4 AS competencia_ord, 'Desenvolver guias passo a passo para operação de sistemas por técnicos de urna.' AS descricao FROM dual
        UNION ALL         SELECT 11 AS ord, 4 AS competencia_ord, 'Criar manuais de procedimentos para solução de problemas técnicos recorrentes.' AS descricao FROM dual
        UNION ALL         SELECT 12 AS ord, 4 AS competencia_ord, 'Documentar as configurações específicas do ambiente para cada ciclo eleitoral.' AS descricao FROM dual
        UNION ALL         SELECT 13 AS ord, 5 AS competencia_ord, 'Capacitar técnicos de suporte e mesários na operação das urnas e sistemas.' AS descricao FROM dual
        UNION ALL         SELECT 14 AS ord, 5 AS competencia_ord, 'Produzir videoaulas e materiais didáticos sobre novas funcionalidades dos sistemas.' AS descricao FROM dual
        UNION ALL         SELECT 15 AS ord, 5 AS competencia_ord, 'Avaliar o nível de retenção de conhecimento dos participantes.' AS descricao FROM dual
        UNION ALL         SELECT 16 AS ord, 6 AS competencia_ord, 'Definir cronogramas e marcos de entrega para novos módulos do SGC.' AS descricao FROM dual
        UNION ALL         SELECT 17 AS ord, 6 AS competencia_ord, 'Alocar recursos humanos e técnicos para sprints de desenvolvimento.' AS descricao FROM dual
        UNION ALL         SELECT 18 AS ord, 6 AS competencia_ord, 'Realizar reuniões de acompanhamento e retrospectivas.' AS descricao FROM dual
        UNION ALL         SELECT 19 AS ord, 7 AS competencia_ord, 'Manter a integridade do repositório central de código-fonte.' AS descricao FROM dual
        UNION ALL         SELECT 20 AS ord, 7 AS competencia_ord, 'Configurar e manter pipelines de integração e entrega contínua.' AS descricao FROM dual
        UNION ALL         SELECT 21 AS ord, 7 AS competencia_ord, 'Gerenciar ambientes virtualizados para desenvolvimento e homologação.' AS descricao FROM dual
        UNION ALL         SELECT 22 AS ord, 8 AS competencia_ord, 'Conduzir entrevistas com usuários finais para elicitar necessidades.' AS descricao FROM dual
        UNION ALL         SELECT 23 AS ord, 8 AS competencia_ord, 'Mapear processos de negócio e identificar pontos de automação.' AS descricao FROM dual
        UNION ALL         SELECT 24 AS ord, 8 AS competencia_ord, 'Documentar requisitos funcionais e não-funcionais detalhadamente.' AS descricao FROM dual
        UNION ALL         SELECT 25 AS ord, 9 AS competencia_ord, 'Desenhar diagramas de arquitetura de software e modelos de dados.' AS descricao FROM dual
        UNION ALL         SELECT 26 AS ord, 9 AS competencia_ord, 'Definir interfaces de integração entre módulos.' AS descricao FROM dual
        UNION ALL         SELECT 27 AS ord, 9 AS competencia_ord, 'Escolher padrões de projeto adequados para a solução.' AS descricao FROM dual
        UNION ALL         SELECT 28 AS ord, 10 AS competencia_ord, 'Desenvolver código backend.' AS descricao FROM dual
        UNION ALL         SELECT 29 AS ord, 10 AS competencia_ord, 'Desenvolver interfaces frontend.' AS descricao FROM dual
        UNION ALL         SELECT 30 AS ord, 10 AS competencia_ord, 'Realizar refatoração de código legado.' AS descricao FROM dual
        UNION ALL         SELECT 31 AS ord, 11 AS competencia_ord, 'Garantir a acessibilidade das interfaces do sistema.' AS descricao FROM dual
        UNION ALL         SELECT 32 AS ord, 11 AS competencia_ord, 'Implementar proteção contra ataques cibernéticos comuns.' AS descricao FROM dual
        UNION ALL         SELECT 33 AS ord, 11 AS competencia_ord, 'Configurar o sistema para conformidade com a proteção de dados.' AS descricao FROM dual
        UNION ALL         SELECT 34 AS ord, 12 AS competencia_ord, 'Planejar janelas de manutenção e comunicação de indisponibilidade.' AS descricao FROM dual
        UNION ALL         SELECT 35 AS ord, 12 AS competencia_ord, 'Executar scripts de migração de banco de dados.' AS descricao FROM dual
        UNION ALL         SELECT 36 AS ord, 12 AS competencia_ord, 'Verificar a estabilidade do sistema pós-implantação.' AS descricao FROM dual
        UNION ALL         SELECT 37 AS ord, 13 AS competencia_ord, 'Executar baterias de testes automatizados de ponta a ponta.' AS descricao FROM dual
        UNION ALL         SELECT 38 AS ord, 13 AS competencia_ord, 'Desenvolver testes unitários e de integração.' AS descricao FROM dual
        UNION ALL         SELECT 39 AS ord, 13 AS competencia_ord, 'Reportar e monitorar o ciclo de vida de bugs.' AS descricao FROM dual
        UNION ALL         SELECT 40 AS ord, 14 AS competencia_ord, 'Atuar na resolução de incidentes complexos escalonados.' AS descricao FROM dual
        UNION ALL         SELECT 41 AS ord, 14 AS competencia_ord, 'Analisar logs de erro para identificar falhas.' AS descricao FROM dual
        UNION ALL         SELECT 42 AS ord, 14 AS competencia_ord, 'Desenvolver correções paliativas urgentes.' AS descricao FROM dual
        UNION ALL         SELECT 43 AS ord, 15 AS competencia_ord, 'Elaborar estudos técnicos preliminares.' AS descricao FROM dual
        UNION ALL         SELECT 44 AS ord, 15 AS competencia_ord, 'Definir o objeto e a justificativa da contratação.' AS descricao FROM dual
        UNION ALL         SELECT 45 AS ord, 16 AS competencia_ord, 'Escrever as especificações técnicas no Termo de Referência.' AS descricao FROM dual
        UNION ALL         SELECT 46 AS ord, 16 AS competencia_ord, 'Definir níveis mínimos de serviço.' AS descricao FROM dual
        UNION ALL         SELECT 47 AS ord, 16 AS competencia_ord, 'Analisar propostas técnicas em processos licitatórios.' AS descricao FROM dual
        UNION ALL         SELECT 48 AS ord, 17 AS competencia_ord, 'Acompanhar a execução técnica garantindo os níveis de serviço.' AS descricao FROM dual
        UNION ALL         SELECT 49 AS ord, 17 AS competencia_ord, 'Validar entregas e atestar notas fiscais tecnicamente.' AS descricao FROM dual
        UNION ALL         SELECT 50 AS ord, 17 AS competencia_ord, 'Gerenciar riscos contratuais técnicos.' AS descricao FROM dual
    ) LOOP
        INSERT INTO SGC.ATIVIDADE (mapa_codigo, descricao) VALUES (v_mapa_codigo, r.descricao) RETURNING codigo INTO v_atividades(r.ord);
        INSERT INTO SGC.COMPETENCIA_ATIVIDADE (atividade_codigo, competencia_codigo) VALUES (v_atividades(r.ord), v_competencias(r.competencia_ord));
    END LOOP;

    FOR r IN (
        SELECT 1 AS atividade_ord, 'Funcionamento da Urna Eletrônica' AS descricao FROM dual
        UNION ALL         SELECT 1 AS atividade_ord, 'Arquitetura da Urna Eletrônica' AS descricao FROM dual
        UNION ALL         SELECT 1 AS atividade_ord, 'Sistema de Gerenciamento de Dados Aplicativos e Interface com a Urna Eletrônica' AS descricao FROM dual
        UNION ALL         SELECT 2 AS atividade_ord, 'Legislação sobre suporte técnico em eleições' AS descricao FROM dual
        UNION ALL         SELECT 2 AS atividade_ord, 'Normas de suporte técnico em eleições' AS descricao FROM dual
        UNION ALL         SELECT 2 AS atividade_ord, 'Fluxos de comunicação técnica' AS descricao FROM dual
        UNION ALL         SELECT 2 AS atividade_ord, 'Protocolos de escalonamento de chamados' AS descricao FROM dual
        UNION ALL         SELECT 3 AS atividade_ord, 'Protocolos de contingência de hardware da Urna Eletrônica' AS descricao FROM dual
        UNION ALL         SELECT 3 AS atividade_ord, 'Protocolos de contingência de software da Urna Eletrônica' AS descricao FROM dual
        UNION ALL         SELECT 3 AS atividade_ord, 'Normas para votação manual' AS descricao FROM dual
        UNION ALL         SELECT 3 AS atividade_ord, 'Procedimentos de recuperação de dados de votação' AS descricao FROM dual
        UNION ALL         SELECT 4 AS atividade_ord, 'Arquitetura de sistemas eleitorais distribuídos' AS descricao FROM dual
        UNION ALL         SELECT 4 AS atividade_ord, 'Ferramentas de monitoramento de infraestrutura de TI' AS descricao FROM dual
        UNION ALL         SELECT 5 AS atividade_ord, 'Gerenciamento de chaves digitais' AS descricao FROM dual
        UNION ALL         SELECT 5 AS atividade_ord, 'Gerenciamento de certificados digitais' AS descricao FROM dual
        UNION ALL         SELECT 5 AS atividade_ord, 'Políticas de controle de acesso do Tribunal Superior Eleitoral' AS descricao FROM dual
        UNION ALL         SELECT 5 AS atividade_ord, 'Normas de auditoria do Tribunal Superior Eleitoral' AS descricao FROM dual
        UNION ALL         SELECT 6 AS atividade_ord, 'Protocolos de transmissão de dados via rede segura JE-Connect' AS descricao FROM dual
        UNION ALL         SELECT 6 AS atividade_ord, 'Estrutura de arquivos do Boletim de Urna' AS descricao FROM dual
        UNION ALL         SELECT 6 AS atividade_ord, 'Estrutura de arquivos do Registro Digital de Voto' AS descricao FROM dual
        UNION ALL         SELECT 7 AS atividade_ord, 'Metodologia de execução de testes de carga' AS descricao FROM dual
        UNION ALL         SELECT 7 AS atividade_ord, 'Ferramentas de simulação de tráfego de dados' AS descricao FROM dual
        UNION ALL         SELECT 8 AS atividade_ord, 'Técnicas de simulação de falhas de hardware' AS descricao FROM dual
        UNION ALL         SELECT 8 AS atividade_ord, 'Arquitetura de rede do Tribunal' AS descricao FROM dual
        UNION ALL         SELECT 9 AS atividade_ord, 'Gestão de equipes técnicas' AS descricao FROM dual
        UNION ALL         SELECT 9 AS atividade_ord, 'Logística de eventos técnicos eleitorais' AS descricao FROM dual
        UNION ALL         SELECT 10 AS atividade_ord, 'Técnicas de redação técnica' AS descricao FROM dual
        UNION ALL         SELECT 10 AS atividade_ord, 'Simplificação de linguagem técnica' AS descricao FROM dual
        UNION ALL         SELECT 10 AS atividade_ord, 'Princípios de experiência do usuário para manuais' AS descricao FROM dual
        UNION ALL         SELECT 11 AS atividade_ord, 'Metodologia de análise de causa raiz' AS descricao FROM dual
        UNION ALL         SELECT 11 AS atividade_ord, 'Histórico de problemas técnicos de pleitos anteriores' AS descricao FROM dual
        UNION ALL         SELECT 12 AS atividade_ord, 'Padrões de documentação técnica de infraestrutura' AS descricao FROM dual
        UNION ALL         SELECT 13 AS atividade_ord, 'Didática para treinamentos operacionais' AS descricao FROM dual
        UNION ALL         SELECT 13 AS atividade_ord, 'Conteúdo do Manual do Mesário' AS descricao FROM dual
        UNION ALL         SELECT 13 AS atividade_ord, 'Legislação eleitoral aplicada ao mesário' AS descricao FROM dual
        UNION ALL         SELECT 14 AS atividade_ord, 'Ferramentas de edição de vídeo' AS descricao FROM dual
        UNION ALL         SELECT 14 AS atividade_ord, 'Técnicas de design instrucional' AS descricao FROM dual
        UNION ALL         SELECT 15 AS atividade_ord, 'Técnicas de avaliação de aprendizagem' AS descricao FROM dual
        UNION ALL         SELECT 16 AS atividade_ord, 'Técnicas de estimativa de esforço de software' AS descricao FROM dual
        UNION ALL         SELECT 16 AS atividade_ord, 'Uso de gráficos de Gantt' AS descricao FROM dual
        UNION ALL         SELECT 16 AS atividade_ord, 'Uso de gráficos Burndown' AS descricao FROM dual
        UNION ALL         SELECT 17 AS atividade_ord, 'Técnicas de planejamento de capacidade' AS descricao FROM dual
        UNION ALL         SELECT 17 AS atividade_ord, 'Mapeamento de competências técnicas da equipe' AS descricao FROM dual
        UNION ALL         SELECT 18 AS atividade_ord, 'Facilitação de reuniões diárias do Scrum' AS descricao FROM dual
        UNION ALL         SELECT 18 AS atividade_ord, 'Técnicas de retrospectiva de sprint' AS descricao FROM dual
        UNION ALL         SELECT 19 AS atividade_ord, 'Administração de servidores Git' AS descricao FROM dual
        UNION ALL         SELECT 19 AS atividade_ord, 'Estratégias de ramificação de código GitFlow' AS descricao FROM dual
        UNION ALL         SELECT 19 AS atividade_ord, 'Estratégias de desenvolvimento baseado em tronco' AS descricao FROM dual
        UNION ALL         SELECT 20 AS atividade_ord, 'Sintaxe YAML para configurações de pipeline' AS descricao FROM dual
        UNION ALL         SELECT 20 AS atividade_ord, 'Uso do Jenkins' AS descricao FROM dual
        UNION ALL         SELECT 20 AS atividade_ord, 'Uso do GitLab CI' AS descricao FROM dual
        UNION ALL         SELECT 21 AS atividade_ord, 'Uso de Docker' AS descricao FROM dual
        UNION ALL         SELECT 21 AS atividade_ord, 'Orquestração de containers com Docker Compose' AS descricao FROM dual
        UNION ALL         SELECT 21 AS atividade_ord, 'Princípios de Infraestrutura como Código' AS descricao FROM dual
        UNION ALL         SELECT 22 AS atividade_ord, 'Técnicas de entrevista de requisitos' AS descricao FROM dual
        UNION ALL         SELECT 22 AS atividade_ord, 'Técnicas de brainstorming de requisitos' AS descricao FROM dual
        UNION ALL         SELECT 23 AS atividade_ord, 'Notação BPMN 2.0' AS descricao FROM dual
        UNION ALL         SELECT 23 AS atividade_ord, 'Técnicas de análise de lacunas de processos' AS descricao FROM dual
        UNION ALL         SELECT 24 AS atividade_ord, 'Escrita de Histórias de Usuário' AS descricao FROM dual
        UNION ALL         SELECT 24 AS atividade_ord, 'Definição de Critérios de Aceitação' AS descricao FROM dual
        UNION ALL         SELECT 25 AS atividade_ord, 'Modelagem Entidade Relacionamento' AS descricao FROM dual
        UNION ALL         SELECT 25 AS atividade_ord, 'Diagramas de Classe da UML' AS descricao FROM dual
        UNION ALL         SELECT 25 AS atividade_ord, 'Diagramas de Sequência da UML' AS descricao FROM dual
        UNION ALL         SELECT 26 AS atividade_ord, 'Padrões de arquitetura RESTful' AS descricao FROM dual
        UNION ALL         SELECT 26 AS atividade_ord, 'Documentação de APIs com OpenAPI' AS descricao FROM dual
        UNION ALL         SELECT 27 AS atividade_ord, 'Padrões de projeto GoF' AS descricao FROM dual
        UNION ALL         SELECT 27 AS atividade_ord, 'Princípios SOLID de programação orientada a objetos' AS descricao FROM dual
        UNION ALL         SELECT 28 AS atividade_ord, 'Linguagem de programação Java 17' AS descricao FROM dual
        UNION ALL         SELECT 28 AS atividade_ord, 'Framework Spring Boot' AS descricao FROM dual
        UNION ALL         SELECT 28 AS atividade_ord, 'Framework Hibernate' AS descricao FROM dual
        UNION ALL         SELECT 28 AS atividade_ord, 'Otimização de consultas SQL no banco de dados' AS descricao FROM dual
        UNION ALL         SELECT 29 AS atividade_ord, 'Framework Vue.js 3' AS descricao FROM dual
        UNION ALL         SELECT 29 AS atividade_ord, 'Linguagem TypeScript' AS descricao FROM dual
        UNION ALL         SELECT 29 AS atividade_ord, 'Técnicas de CSS Flexbox' AS descricao FROM dual
        UNION ALL         SELECT 29 AS atividade_ord, 'Técnicas de CSS Grid' AS descricao FROM dual
        UNION ALL         SELECT 30 AS atividade_ord, 'Técnicas de refatoração de código' AS descricao FROM dual
        UNION ALL         SELECT 30 AS atividade_ord, 'Identificação de códigos com cheiro ruim' AS descricao FROM dual
        UNION ALL         SELECT 31 AS atividade_ord, 'Normas do Modelo de Acessibilidade em Governo Eletrônico' AS descricao FROM dual
        UNION ALL         SELECT 31 AS atividade_ord, 'Diretrizes de Acessibilidade para Conteúdo Web 2.1' AS descricao FROM dual
        UNION ALL         SELECT 31 AS atividade_ord, 'Ferramentas de validação de acessibilidade digital' AS descricao FROM dual
        UNION ALL         SELECT 32 AS atividade_ord, 'Segurança em APIs com padrão OAuth2' AS descricao FROM dual
        UNION ALL         SELECT 32 AS atividade_ord, 'Segurança em APIs com padrão JWT' AS descricao FROM dual
        UNION ALL         SELECT 32 AS atividade_ord, 'Prevenção contra ataque de Injeção de SQL' AS descricao FROM dual
        UNION ALL         SELECT 32 AS atividade_ord, 'Prevenção contra ataque de Script entre sites' AS descricao FROM dual
        UNION ALL         SELECT 33 AS atividade_ord, 'Diretrizes da Lei Geral de Proteção de Dados' AS descricao FROM dual
        UNION ALL         SELECT 33 AS atividade_ord, 'Técnicas de anonimização de dados pessoais' AS descricao FROM dual
        UNION ALL         SELECT 34 AS atividade_ord, 'Técnicas de gerenciamento de partes interessadas' AS descricao FROM dual
        UNION ALL         SELECT 35 AS atividade_ord, 'Uso da ferramenta Flyway' AS descricao FROM dual
        UNION ALL         SELECT 35 AS atividade_ord, 'Uso da ferramenta Liquibase' AS descricao FROM dual
        UNION ALL         SELECT 36 AS atividade_ord, 'Análise de verificações de saúde do sistema' AS descricao FROM dual
        UNION ALL         SELECT 37 AS atividade_ord, 'Uso da ferramenta Playwright' AS descricao FROM dual
        UNION ALL         SELECT 38 AS atividade_ord, 'Uso do framework JUnit' AS descricao FROM dual
        UNION ALL         SELECT 38 AS atividade_ord, 'Uso da biblioteca Mockito' AS descricao FROM dual
        UNION ALL         SELECT 38 AS atividade_ord, 'Uso da ferramenta Vitest' AS descricao FROM dual
        UNION ALL         SELECT 39 AS atividade_ord, 'Técnicas de gestão de defeitos de software' AS descricao FROM dual
        UNION ALL         SELECT 40 AS atividade_ord, 'Análise de despejos de memória Heap' AS descricao FROM dual
        UNION ALL         SELECT 40 AS atividade_ord, 'Análise de despejos de execução Thread' AS descricao FROM dual
        UNION ALL         SELECT 41 AS atividade_ord, 'Uso da pilha de ferramentas ELK' AS descricao FROM dual
        UNION ALL         SELECT 42 AS atividade_ord, 'Gestão de patches de software em produção' AS descricao FROM dual
        UNION ALL         SELECT 43 AS atividade_ord, 'Instruções Normativas de contratação de TI da Secretaria de Governo Digital' AS descricao FROM dual
        UNION ALL         SELECT 44 AS atividade_ord, 'Técnicas de pesquisa de mercado de tecnologia' AS descricao FROM dual
        UNION ALL         SELECT 44 AS atividade_ord, 'Análise de soluções tecnológicas alternativas' AS descricao FROM dual
        UNION ALL         SELECT 45 AS atividade_ord, 'Técnicas de definição de requisitos técnicos de TI' AS descricao FROM dual
        UNION ALL         SELECT 46 AS atividade_ord, 'Definição de indicadores de Acordo de Nível de Serviço' AS descricao FROM dual
        UNION ALL         SELECT 47 AS atividade_ord, 'Técnicas de julgamento de conformidade técnica em licitações' AS descricao FROM dual
        UNION ALL         SELECT 48 AS atividade_ord, 'Técnicas de gestão de contratos administrativos de TI' AS descricao FROM dual
        UNION ALL         SELECT 49 AS atividade_ord, 'Normas de recebimento provisório de serviços de TI' AS descricao FROM dual
        UNION ALL         SELECT 49 AS atividade_ord, 'Normas de recebimento definitivo de serviços de TI' AS descricao FROM dual
        UNION ALL         SELECT 50 AS atividade_ord, 'Técnicas de análise de matriz de riscos contratuais' AS descricao FROM dual
    ) LOOP
        INSERT INTO SGC.CONHECIMENTO (atividade_codigo, descricao) VALUES (v_atividades(r.atividade_ord), r.descricao);
    END LOOP;

    -- Movimentacoes do fluxo finalizado, usando os títulos reais de homologacao.
    INSERT INTO SGC.MOVIMENTACAO (subprocesso_codigo, data_hora, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, descricao)
    VALUES (v_subprocesso_codigo, v_agora - INTERVAL '4' DAY, v_unidade_codigo, v_unidade_codigo, v_servidor, 'Processo iniciado para o mapa SESEL');
    INSERT INTO SGC.MOVIMENTACAO (subprocesso_codigo, data_hora, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, descricao)
    VALUES (v_subprocesso_codigo, v_agora - INTERVAL '3' DAY, v_unidade_codigo, COALESCE(v_unidade_superior_codigo, v_unidade_codigo), v_gestor, 'Cadastro do mapa disponibilizado para análise');
    INSERT INTO SGC.MOVIMENTACAO (subprocesso_codigo, data_hora, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, descricao)
    VALUES (v_subprocesso_codigo, v_agora - INTERVAL '2' DAY, COALESCE(v_unidade_superior_codigo, v_unidade_codigo), v_unidade_codigo, v_chefe, 'Mapa disponibilizado para validação');
    INSERT INTO SGC.MOVIMENTACAO (subprocesso_codigo, data_hora, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, descricao)
    VALUES (v_subprocesso_codigo, v_agora - INTERVAL '1' DAY, v_unidade_codigo, v_unidade_codigo, v_admin_1, 'Mapa SESEL homologado');

    -- Alertas para os destinatarios corretos.
    INSERT INTO SGC.ALERTA (processo_codigo, data_hora, unidade_origem_codigo, unidade_destino_codigo, usuario_destino_titulo, descricao)
    VALUES (v_processo_codigo, v_agora - INTERVAL '1' DAY, COALESCE(v_unidade_superior_codigo, v_unidade_codigo), v_unidade_codigo, v_gestor, 'Mapa SESEL homologado');
    INSERT INTO SGC.ALERTA (processo_codigo, data_hora, unidade_origem_codigo, unidade_destino_codigo, usuario_destino_titulo, descricao)
    VALUES (v_processo_codigo, v_agora - INTERVAL '2' DAY, COALESCE(v_unidade_superior_codigo, v_unidade_codigo), v_unidade_codigo, v_chefe, 'Mapa SESEL disponível para validação');
    INSERT INTO SGC.ALERTA_USUARIO (alerta_codigo, usuario_titulo, data_hora_leitura)
    SELECT a.codigo, v_gestor, v_agora - INTERVAL '6' HOUR FROM SGC.ALERTA a WHERE a.processo_codigo = v_processo_codigo AND a.descricao = 'Mapa SESEL homologado';
    INSERT INTO SGC.ALERTA_USUARIO (alerta_codigo, usuario_titulo, data_hora_leitura)
    SELECT a.codigo, v_chefe, v_agora - INTERVAL '12' HOUR FROM SGC.ALERTA a WHERE a.processo_codigo = v_processo_codigo AND a.descricao = 'Mapa SESEL disponível para validação';

    -- Outbox de notificacoes coerentes com o fluxo.
    INSERT INTO SGC.NOTIFICACAO_EMAIL (
        subprocesso_codigo, tipo_notificacao, usuario_destino_titulo, unidade_destino_sigla, destinatario,
        assunto, corpo_html, situacao, tentativas, proxima_tentativa_em, data_hora_criacao, data_hora_envio, ultimo_erro, chave_idempotencia
    ) VALUES (
        v_subprocesso_codigo, 'MAPA_DISPONIBILIZADO', v_gestor, v_unidade_sigla, v_email_gestor,
        'SGC: Mapa de competencias disponibilizado - SESEL',
        '<p>Mapa disponibilizado para validacao da unidade SESEL.</p>',
        'ENVIADO', 1, NULL, v_agora - INTERVAL '2' DAY, v_agora - INTERVAL '1' DAY, NULL,
        'mapa-sesel-oracle-' || v_processo_codigo || '-01-disponibilizado'
    );
    INSERT INTO SGC.NOTIFICACAO_EMAIL (
        subprocesso_codigo, tipo_notificacao, usuario_destino_titulo, unidade_destino_sigla, destinatario,
        assunto, corpo_html, situacao, tentativas, proxima_tentativa_em, data_hora_criacao, data_hora_envio, ultimo_erro, chave_idempotencia
    ) VALUES (
        v_subprocesso_codigo, 'MAPA_HOMOLOGADO', v_chefe, v_unidade_sigla, v_email_chefe,
        'SGC: Mapa de competencias homologado - SESEL',
        '<p>Mapa homologado com sucesso.</p>',
        'FALHA_DEFINITIVA', 5, NULL, v_agora - INTERVAL '1' DAY, NULL, 'Falha simulada no Oracle: caixa postal indisponivel',
        'mapa-sesel-oracle-' || v_processo_codigo || '-02-homologado'
    );

    COMMIT;
END;
/

