-- Clear existing data (if any) to avoid duplicates
TRUNCATE TABLE sgc.atribuicao CASCADE;
TRUNCATE TABLE sgc.processo_unidade CASCADE;
TRUNCATE TABLE sgc.processo CASCADE;
TRUNCATE TABLE sgc.servidor_perfil CASCADE;
TRUNCATE TABLE sgc.servidor CASCADE;
TRUNCATE TABLE sgc.unidade CASCADE;

-- Insert test data
DO
$$
    DECLARE
        v_stic_id       UUID;
        v_sedoc_id      UUID;
        v_sgp_id        UUID;
        v_sj_id         UUID;
        v_cosis_id      UUID;
        v_sesel_id      UUID;
        v_error_message TEXT;
        v_error_detail  TEXT;
        v_error_hint    TEXT;
        v_error_context TEXT;
    BEGIN
        -- Inserir unidade principais
        INSERT INTO sgc.unidade (id, descricao, nome, unidade_superior_id)
        VALUES (gen_random_uuid(), 'Secretaria de Tecnologia da Informação e Comunicações', 'STIC', NULL)
        RETURNING id INTO v_stic_id;

        INSERT INTO sgc.unidade (id, descricao, nome, unidade_superior_id)
        VALUES (gen_random_uuid(), 'Seção de Desenvolvimento Organizacional e Capacitação', 'SEDOC', NULL)
        RETURNING id INTO v_sedoc_id;

        INSERT INTO sgc.unidade (id, descricao, nome, unidade_superior_id)
        VALUES (gen_random_uuid(), 'Secretaria de Gestão de Pessoas', 'SGP', NULL)
        RETURNING id INTO v_sgp_id;

        INSERT INTO sgc.unidade (id, descricao, nome, unidade_superior_id)
        VALUES (gen_random_uuid(), 'Secretaria Judiciária', 'SJ', NULL)
        RETURNING id INTO v_sj_id;

        -- Inserir unidade subordinadas
        INSERT INTO sgc.unidade (id, descricao, nome, unidade_superior_id)
        VALUES (gen_random_uuid(), 'Coordenadoria de Sistemas', 'COSIS', v_stic_id)
        RETURNING id INTO v_cosis_id;

        INSERT INTO sgc.unidade (id, descricao, nome, unidade_superior_id)
        VALUES (gen_random_uuid(), 'Seção de Sistemas Eleitorais', 'SESEL', v_cosis_id)
        RETURNING id INTO v_sesel_id;

        -- Inserir servidores de teste
        IF v_sedoc_id IS NOT NULL AND v_sgp_id IS NOT NULL AND v_sj_id IS NOT NULL THEN
            -- Servidores SEDOC
            -- Password is 'senha123' hashed with BCrypt
            INSERT INTO sgc.servidor (id, nome, email, cargo, unidade_id, senha)
            VALUES (gen_random_uuid(), 'João Silva', 'joao.silva@tre-pe.jus.br', 'Analista de Sistemas', v_sedoc_id,
                    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgAxG9eKSImjw2W'),
                   (gen_random_uuid(), 'Maria Santos', 'maria.santos@tre-pe.jus.br', 'Analista de Sistemas', v_sedoc_id,
                    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgAxG9eKSImjw2W'),
                   (gen_random_uuid(), 'Pedro Oliveira', 'pedro.oliveira@tre-pe.jus.br', 'Analista de Sistemas',
                    v_sedoc_id, '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgAxG9eKSImjw2W'),

                   -- Servidores SGP
                   (gen_random_uuid(), 'Ana Costa', 'ana.costa@tre-pe.jus.br', 'Analista de RH', v_sgp_id,
                    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgAxG9eKSImjw2W'),
                   (gen_random_uuid(), 'Carlos Souza', 'carlos.souza@tre-pe.jus.br', 'Analista de RH', v_sgp_id,
                    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgAxG9eKSImjw2W'),
                   (gen_random_uuid(), 'Juliana Lima', 'juliana.lima@tre-pe.jus.br', 'Analista de RH', v_sgp_id,
                    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgAxG9eKSImjw2W'),

                   -- Servidores SJ
                   (gen_random_uuid(), 'Roberto Alves', 'roberto.alves@tre-pe.jus.br', 'Analista Judiciário', v_sj_id,
                    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgAxG9eKSImjw2W'),
                   (gen_random_uuid(), 'Fernanda Martins', 'fernanda.martins@tre-pe.jus.br', 'Analista Judiciário',
                    v_sj_id, '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgAxG9eKSImjw2W'),
                   (gen_random_uuid(), 'Lucas Pereira', 'lucas.pereira@tre-pe.jus.br', 'Analista Judiciário', v_sj_id,
                    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgAxG9eKSImjw2W');

            -- Inserir perfis dos servidores
            WITH servidores AS (SELECT id, unidade_id
                                FROM sgc.servidor
                                WHERE unidade_id IN (v_sedoc_id, v_sgp_id, v_sj_id))
            INSERT
            INTO sgc.servidor_perfil (servidor_id, perfil)
            SELECT id, 'SERVIDOR'
            FROM servidores;

            -- Adicionar perfil SEDOC para os servidores da SEDOC
            INSERT INTO sgc.servidor_perfil (servidor_id, perfil)
            SELECT id, 'SEDOC'
            FROM sgc.servidor
            WHERE unidade_id = v_sedoc_id;

            -- Adicionar perfil GESTOR para o primeiro servidor de cada unidade
            INSERT INTO sgc.servidor_perfil (servidor_id, perfil)
            SELECT id, 'GESTOR'
            FROM (SELECT id, unidade_id, ROW_NUMBER() OVER (PARTITION BY unidade_id ORDER BY id) as rn
                  FROM sgc.servidor
                  WHERE unidade_id IN (v_sedoc_id, v_sgp_id, v_sj_id)) ranked
            WHERE rn = 1;

            -- Insert test processo with all required fields
            WITH inserted_processos AS (
                INSERT INTO sgc.processo (id, nome, datahora_limite, situacao)
                    VALUES (gen_random_uuid(),
                            'Processo de Mapeamento de Competências - 2025',
                            CURRENT_DATE + 30, 'CRIADO'),
                           (gen_random_uuid(),
                            'Processo de Teste 2',
                            CURRENT_DATE + 60,
                            'EM_ANDAMENTO'),
                           (gen_random_uuid(),
                            'Processo de Teste 3',
                            CURRENT_DATE + 90, 'CRIADO')
                    RETURNING id, nome, situacao)
            -- Insert participacoes for each processo
            INSERT
            INTO sgc.processo_unidade
            (id,
             unidade_id,
             processo_id,
             situacao,
             datahora_inicio_registro,
             datahora_fim_registro,
             datahora_validacao,
             datahora_homologacao)

            SELECT gen_random_uuid(),
                   u.id,
                   p.id,
                   CASE
                       WHEN u.id = v_sedoc_id THEN 'EM_ANDAMENTO'
                       WHEN u.id = v_sgp_id THEN 'EM_ANDAMENTO'
                       ELSE 'NAO_INICIADA'
                       END,
                   CURRENT_TIMESTAMP - (random() * INTERVAL '30 days'),
                   CASE
                       WHEN u.id = v_sedoc_id THEN CURRENT_TIMESTAMP
                       END,
                   CASE
                       WHEN u.id = v_sedoc_id THEN CURRENT_TIMESTAMP
                       END,
                   CASE
                       WHEN u.id = v_sedoc_id THEN CURRENT_TIMESTAMP
                       END
            FROM inserted_processos p
                     CROSS JOIN sgc.unidade u
            WHERE u.id IN (v_sedoc_id, v_sgp_id, v_sj_id)
            ORDER BY p.id, u.id;

            -- Insert atribuicoes (relating processo with servidores and unidade)
            WITH processo_unidade AS (SELECT p.id as processo_id, pu.unidade_id
                                      FROM sgc.processo p
                                               JOIN sgc.processo_unidade pu ON p.id = pu.processo_id
                                      WHERE pu.unidade_id IN (v_sedoc_id, v_sgp_id, v_sj_id))
            INSERT
            INTO sgc.atribuicao (id,
                                 processo_id,
                                 servidor_id,
                                 unidade_id,
                                 data_inicio,
                                 data_termino,
                                 justificativa)
            SELECT gen_random_uuid(),
                   pu.processo_id,
                   s.id,
                   pu.unidade_id,
                   CURRENT_DATE,
                   CURRENT_DATE + INTERVAL '30 days',
                   'Atribuição automática para teste'
            FROM processo_unidade pu
                     JOIN sgc.servidor s ON s.unidade_id = pu.unidade_id
            WHERE s.id IN (SELECT id
                           FROM sgc.servidor
                           WHERE unidade_id = pu.unidade_id
                           ORDER BY id
                           LIMIT 1)
            ON CONFLICT DO NOTHING;

        ELSE
            RAISE EXCEPTION 'Erro: Algumas unidade não foram criadas corretamente. v_sedoc_id=%, v_sgp_id=%, v_sj_id=%',
                v_sedoc_id, v_sgp_id, v_sj_id;
        END IF;

        RAISE NOTICE 'Dados de teste carregados com sucesso!';

    EXCEPTION
        WHEN OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_message = MESSAGE_TEXT,
                v_error_detail = PG_EXCEPTION_DETAIL,
                v_error_hint = PG_EXCEPTION_HINT,
                v_error_context = PG_EXCEPTION_CONTEXT;

            RAISE EXCEPTION 'Erro durante o carregamento dos dados de teste: %
Detalhe: %
Dica: %
Contexto: %',
                v_error_message, v_error_detail, v_error_hint, v_error_context;
    END
$$;