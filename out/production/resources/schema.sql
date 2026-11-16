-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS sgc;

-- Drop tables if they exist (in reverse order of dependency)
DROP TABLE IF EXISTS sgc.conhecimento CASCADE;
DROP TABLE IF EXISTS sgc.atividade CASCADE;
DROP TABLE IF EXISTS sgc.competencia CASCADE;
DROP TABLE IF EXISTS sgc.mapa_unidade CASCADE;
DROP TABLE IF EXISTS sgc.notificacao CASCADE;
DROP TABLE IF EXISTS sgc.processo_unidade CASCADE;
DROP TABLE IF EXISTS sgc.evento_historico CASCADE;
DROP TABLE IF EXISTS sgc.atribuicao CASCADE;
DROP TABLE IF EXISTS sgc.servidor_perfil CASCADE;
DROP TABLE IF EXISTS sgc.servidor CASCADE;
DROP TABLE IF EXISTS sgc.unidade CASCADE;
DROP TABLE IF EXISTS sgc.processo CASCADE;
DROP TABLE IF EXISTS sgc.mapa CASCADE;
DROP TABLE IF EXISTS sgc.historico CASCADE;

-- Step 1: Create tables without foreign key constraints
CREATE TABLE sgc.historico (
    id uuid NOT NULL PRIMARY KEY
);

CREATE TABLE sgc.mapa (
    id uuid NOT NULL PRIMARY KEY,
    datahora_geracao timestamp(6)
);

CREATE TABLE sgc.processo (
    id uuid NOT NULL PRIMARY KEY,
    datahora_limite date,
    nome varchar(255) NOT NULL,
    situacao varchar(50),
    historico_id uuid
);

CREATE TABLE sgc.unidade (
    id uuid NOT NULL PRIMARY KEY,
    descricao varchar(200) NOT NULL,
    nome varchar(100) NOT NULL,
    substituto_id uuid,
    titular_id uuid,
    unidade_superior_id uuid
);

CREATE TABLE sgc.servidor (
    id uuid NOT NULL PRIMARY KEY,
    cargo varchar(100) NOT NULL,
    email varchar(100) NOT NULL,
    nome varchar(100) NOT NULL,
    senha varchar(100) NOT NULL,
    unidade_id uuid
);

CREATE TABLE sgc.servidor_perfil (
    servidor_id uuid NOT NULL,
    perfil varchar(50) NOT NULL
);

CREATE TABLE sgc.atribuicao (
    id uuid NOT NULL PRIMARY KEY,
    data_inicio date NOT NULL,
    data_termino date,
    justificativa varchar(500),
    processo_id uuid NOT NULL,
    servidor_id uuid NOT NULL,
    unidade_id uuid NOT NULL
);

CREATE TABLE sgc.evento_historico (
    id uuid NOT NULL PRIMARY KEY,
    datahora timestamp(6),
    detalhes text,
    tipo varchar(50),
    historico_id uuid,
    unidade_id uuid
);

CREATE TABLE sgc.mapa_unidade (
    id uuid NOT NULL PRIMARY KEY,
    mapa_id uuid,
    unidade_id uuid NOT NULL
);

CREATE TABLE sgc.competencia (
    id uuid NOT NULL PRIMARY KEY,
    descricao varchar(255) NOT NULL,
    detalhamento text,
    mapa_unidade_id uuid
);

CREATE TABLE sgc.notificacao (
    id uuid NOT NULL PRIMARY KEY,
    assunto varchar(255) NOT NULL,
    conteudo text,
    datahora_envio timestamp(6),
    tipo varchar(50),
    processo_id uuid,
    unidade_id uuid
);

CREATE TABLE sgc.processo_unidade
(
    id uuid NOT NULL PRIMARY KEY,
    datahora_fim_registro timestamp(6),
    datahora_homologacao timestamp(6),
    datahora_inicio_registro timestamp(6),
    datahora_validacao timestamp(6),
    situacao varchar(50),
    processo_id uuid NOT NULL,
    unidade_id uuid NOT NULL
);

CREATE TABLE sgc.atividade (
    id uuid NOT NULL PRIMARY KEY,
    descricao varchar(255) NOT NULL,
    competencia_id uuid,
    processo_unidade_id uuid
);

CREATE TABLE sgc.conhecimento (
    id uuid NOT NULL PRIMARY KEY,
    descricao varchar(255) NOT NULL,
    tipo varchar(50),
    atividade_id uuid
);

-- Step 2: Add constraints after all tables are created
-- Processo constraints
ALTER TABLE sgc.processo
    ADD CONSTRAINT ck_processo_situacao 
        CHECK ((situacao)::text = ANY (
            (ARRAY['CRIADO'::character varying, 'EM_ANDAMENTO'::character varying, 
                  'APTO'::character varying, 'FINALIZADO'::character varying])::text[]
        )),
    ADD CONSTRAINT uk_processo_historico UNIQUE (historico_id),
    ADD CONSTRAINT fk_processo_historico 
        FOREIGN KEY (historico_id) REFERENCES sgc.historico(id);

-- Unidade constraints
ALTER TABLE sgc.unidade
    ADD CONSTRAINT uk_unidade_nome UNIQUE (nome),
    ADD CONSTRAINT fk_unidade_servidor_substituto 
        FOREIGN KEY (substituto_id) REFERENCES sgc.servidor(id) DEFERRABLE INITIALLY DEFERRED,
    ADD CONSTRAINT fk_unidade_servidor_titular 
        FOREIGN KEY (titular_id) REFERENCES sgc.servidor(id) DEFERRABLE INITIALLY DEFERRED,
    ADD CONSTRAINT fk_unidade_unidade_superior 
        FOREIGN KEY (unidade_superior_id) REFERENCES sgc.unidade(id);

-- Servidor constraints
ALTER TABLE sgc.servidor
    ADD CONSTRAINT uk_servidor_email UNIQUE (email),
    ADD CONSTRAINT fk_servidor_unidade 
        FOREIGN KEY (unidade_id) REFERENCES sgc.unidade(id) DEFERRABLE INITIALLY DEFERRED;

-- Servidor Perfil constraints
ALTER TABLE sgc.servidor_perfil
    ADD CONSTRAINT ck_servidor_perfil_perfil 
        CHECK ((perfil)::text = ANY (
            (ARRAY['SEDOC'::character varying, 'GESTOR'::character varying, 
                  'CHEFE'::character varying, 'SERVIDOR'::character varying])::text[]
        )),
    ADD CONSTRAINT uk_servidor_perfil UNIQUE (servidor_id, perfil),
    ADD CONSTRAINT fk_servidor_perfil_servidor 
        FOREIGN KEY (servidor_id) REFERENCES sgc.servidor(id);

-- Atribuicao constraints
ALTER TABLE sgc.atribuicao
    ADD CONSTRAINT uk_atribuicao_servidor_processo UNIQUE (servidor_id, processo_id),
    ADD CONSTRAINT fk_atribuicao_processo 
        FOREIGN KEY (processo_id) REFERENCES sgc.processo(id),
    ADD CONSTRAINT fk_atribuicao_servidor 
        FOREIGN KEY (servidor_id) REFERENCES sgc.servidor(id),
    ADD CONSTRAINT fk_atribuicao_unidade 
        FOREIGN KEY (unidade_id) REFERENCES sgc.unidade(id);

-- Evento Historico constraints
ALTER TABLE sgc.evento_historico
    ADD CONSTRAINT ck_evento_historico_tipo 
        CHECK ((tipo)::text = ANY (
            (ARRAY['CRIACAO_PROCESSO'::character varying, 'INICIO_PROCESSO'::character varying,
                  'INICIO_CADASTRO'::character varying, 'FINALIZACAO_CADASTRO'::character varying,
                  'ATRIBUICAO_TEMPORARIA'::character varying, 'VALIDACAO'::character varying,
                  'DEVOLUCAO'::character varying, 'HOMOLOGACAO'::character varying,
                  'GERACAO_MAPA'::character varying])::text[]
        )),
    ADD CONSTRAINT fk_evento_historico_historico 
        FOREIGN KEY (historico_id) REFERENCES sgc.historico(id),
    ADD CONSTRAINT fk_evento_historico_unidade 
        FOREIGN KEY (unidade_id) REFERENCES sgc.unidade(id);

-- Mapa Unidade constraints
ALTER TABLE sgc.mapa_unidade
    ADD CONSTRAINT uk_mapa_unidade_mapa_unidade UNIQUE (mapa_id, unidade_id),
    ADD CONSTRAINT fk_mapa_unidade_mapa 
        FOREIGN KEY (mapa_id) REFERENCES sgc.mapa(id),
    ADD CONSTRAINT fk_mapa_unidade_unidade 
        FOREIGN KEY (unidade_id) REFERENCES sgc.unidade(id);

-- Competencia constraints
ALTER TABLE sgc.competencia
    ADD CONSTRAINT fk_competencia_mapa_unidade 
        FOREIGN KEY (mapa_unidade_id) REFERENCES sgc.mapa_unidade(id);

-- Notificacao constraints
ALTER TABLE sgc.notificacao
    ADD CONSTRAINT ck_notificacao_tipo 
        CHECK ((tipo)::text = ANY (
            (ARRAY['INICIO_PROCESSO'::character varying, 'LEMBRETE_PRAZO'::character varying, 
                  'VALIDACAO_PENDENTE'::character varying, 'DEVOLUCAO'::character varying, 
                  'ATRIBUICAO_TEMPORARIA'::character varying])::text[]
        )),
    ADD CONSTRAINT fk_notificacao_processo 
        FOREIGN KEY (processo_id) REFERENCES sgc.processo(id),
    ADD CONSTRAINT fk_notificacao_unidade 
        FOREIGN KEY (unidade_id) REFERENCES sgc.unidade(id);

-- processo_unidade constraints
ALTER TABLE sgc.processo_unidade
    ADD CONSTRAINT ck_processo_unidade_situacao
        CHECK ((situacao)::text = ANY (
            (ARRAY['NAO_INICIADA'::character varying, 'EM_ANDAMENTO'::character varying, 
                  'FINALIZADA'::character varying, 'VALIDADA'::character varying, 
                  'HOMOLOGADA'::character varying])::text[]
        )),
    ADD CONSTRAINT uk_processo_unidade_processo_unidade UNIQUE (processo_id, unidade_id),
    ADD CONSTRAINT fk_processo_unidade_processo
        FOREIGN KEY (processo_id) REFERENCES sgc.processo(id),
    ADD CONSTRAINT fk_processo_unidade_unidade
        FOREIGN KEY (unidade_id) REFERENCES sgc.unidade(id);

-- Atividade constraints
ALTER TABLE sgc.atividade
    ADD CONSTRAINT fk_atividade_competencia 
        FOREIGN KEY (competencia_id) REFERENCES sgc.competencia(id),
    ADD CONSTRAINT fk_atividade_processo_unidade
        FOREIGN KEY (processo_unidade_id) REFERENCES sgc.processo_unidade (id);

-- Conhecimento constraints
ALTER TABLE sgc.conhecimento
    ADD CONSTRAINT ck_conhecimento_tipo 
        CHECK ((tipo)::text = ANY (
            (ARRAY['TECNICO'::character varying, 'GERENCIAL'::character varying, 
                  'COMPORTAMENTAL'::character varying])::text[]
        )),
    ADD CONSTRAINT fk_conhecimento_atividade 
        FOREIGN KEY (atividade_id) REFERENCES sgc.atividade(id);
