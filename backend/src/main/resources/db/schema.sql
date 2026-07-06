create schema if not exists sgc;

-- Tabelas físicas que eram VIEWs no Oracle, mas no H2 de testes são tabelas simples
create table if not exists sgc.vw_usuario
(
    titulo
    varchar
(
    12
) not null,
    matricula varchar
(
    8
),
    nome varchar
(
    255
),
    email varchar
(
    255
),
    ramal varchar
(
    20
),
    unidade_lot_codigo bigint,
    unidade_comp_codigo bigint,
    primary key
(
    titulo
)
    );

create table if not exists sgc.administrador
(
    usuario_titulo
    varchar
(
    12
) not null,
    primary key
(
    usuario_titulo
)
    );

create table if not exists sgc.vw_unidade
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    nome
    varchar
(
    255
),
    sigla varchar
(
    20
),
    matricula_titular varchar
(
    8
),
    titulo_titular varchar
(
    12
),
    data_inicio_titularidade timestamp
(
    6
),
    tipo varchar
(
    20
) check
(
    tipo
    in
(
    'OPERACIONAL',
    'INTERMEDIARIA',
    'INTEROPERACIONAL',
    'SEM_EQUIPE',
    'RAIZ'
)),
    situacao varchar
(
    20
) check
(
    situacao
    in
(
    'ATIVA',
    'INATIVA'
)),
    unidade_superior_codigo bigint,
    primary key
(
    codigo
)
    );

create table if not exists sgc.unidade_mapa
(
    unidade_codigo
    bigint
    not
    null,
    mapa_vigente_codigo
    bigint
    not
    null,
    primary
    key
(
    unidade_codigo
)
    );

create table if not exists sgc.vw_usuario_perfil_unidade
(
    usuario_titulo
    varchar
(
    12
) not null,
    unidade_codigo bigint not null,
    perfil varchar
(
    255
) check
(
    perfil
    in
(
    'ADMIN',
    'GESTOR',
    'CHEFE',
    'SERVIDOR'
)),
    primary key
(
    usuario_titulo,
    unidade_codigo,
    perfil
)
    );

create table if not exists sgc.vw_responsabilidade
(
    unidade_codigo
    bigint
    not
    null,
    usuario_matricula
    varchar
(
    8
),
    usuario_titulo varchar
(
    12
) not null,
    tipo varchar
(
    30
),
    data_inicio timestamp
(
    6
),
    data_fim timestamp
(
    6
),
    primary key
(
    unidade_codigo
)
    );

create table if not exists sgc.processo
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    data_criacao
    timestamp
(
    6
) not null,
    data_finalizacao timestamp
(
    6
),
    data_limite timestamp
(
    6
) not null,
    situacao varchar
(
    20
) not null check
(
    situacao
    in
(
    'CRIADO',
    'EM_ANDAMENTO',
    'FINALIZADO'
)),
    tipo varchar
(
    20
) not null check
(
    tipo
    in
(
    'MAPEAMENTO',
    'REVISAO',
    'DIAGNOSTICO'
)),
    descricao varchar
(
    255
) not null,
    primary key
(
    codigo
)
    );

create table if not exists sgc.mapa
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    subprocesso_codigo
    bigint
    not
    null,
    data_hora_disponibilizado
    timestamp
(
    6
),
    data_hora_homologado timestamp
(
    6
),
    observacoes_disponibilizacao varchar
(
    1000
),
    sugestoes varchar
(
    1000
),
    primary key
(
    codigo
)
    );

create table if not exists sgc.subprocesso
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    data_limite_etapa1
    timestamp
(
    6
),
    data_limite_etapa2 timestamp
(
    6
),
    data_fim_etapa1 timestamp
(
    6
),
    data_fim_etapa2 timestamp
(
    6
),
    processo_codigo bigint not null,
    unidade_codigo bigint not null,
    situacao varchar
(
    50
) check
(
    situacao
    in
(
    'NAO_INICIADO',
    'MAPEAMENTO_CADASTRO_EM_ANDAMENTO',
    'MAPEAMENTO_CADASTRO_DISPONIBILIZADO',
    'MAPEAMENTO_CADASTRO_HOMOLOGADO',
    'MAPEAMENTO_MAPA_CRIADO',
    'MAPEAMENTO_MAPA_DISPONIBILIZADO',
    'MAPEAMENTO_MAPA_COM_SUGESTOES',
    'MAPEAMENTO_MAPA_VALIDADO',
    'MAPEAMENTO_MAPA_HOMOLOGADO',
    'REVISAO_CADASTRO_EM_ANDAMENTO',
    'REVISAO_CADASTRO_DISPONIBILIZADA',
    'REVISAO_CADASTRO_HOMOLOGADA',
    'REVISAO_MAPA_AJUSTADO',
    'REVISAO_MAPA_DISPONIBILIZADO',
    'REVISAO_MAPA_COM_SUGESTOES',
    'REVISAO_MAPA_VALIDADO',
    'REVISAO_MAPA_HOMOLOGADO',
    'DIAGNOSTICO_EM_ANDAMENTO',
    'DIAGNOSTICO_CONCLUIDO',
    'DIAGNOSTICO_HOMOLOGADO'
)) not null,
    primary key
(
    codigo
)
    );

create table if not exists sgc.alerta
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    data_hora
    timestamp
(
    6
) not null,
    processo_codigo bigint,
    unidade_origem_codigo bigint not null,
    unidade_destino_codigo bigint,
    usuario_destino_titulo varchar
(
    12
),
    descricao varchar
(
    255
) not null,
    primary key
(
    codigo
),
    constraint ck_alerta_destino check
(
    unidade_destino_codigo
    is
    not
    null
    or
    usuario_destino_titulo
    is
    not
    null
)
    );

create table if not exists sgc.alerta_usuario
(
    alerta_codigo
    bigint
    not
    null,
    data_hora_leitura
    timestamp
(
    6
),
    usuario_titulo varchar
(
    12
) not null,
    primary key
(
    alerta_codigo,
    usuario_titulo
)
    );

create table if not exists sgc.analise
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    data_hora
    timestamp
(
    6
) not null,
    subprocesso_codigo bigint not null,
    acao varchar
(
    25
) not null check
(
    acao
    in
(
    'ACEITE_MAPEAMENTO',
    'DEVOLUCAO_MAPEAMENTO',
    'ACEITE_REVISAO',
    'DEVOLUCAO_REVISAO',
    'ACEITE_DIAGNOSTICO',
    'DEVOLUCAO_DIAGNOSTICO',
    'HOMOLOGACAO_DIAGNOSTICO'
)),
    tipo varchar
(
    20
) not null check
(
    tipo
    in
(
    'CADASTRO',
    'VALIDACAO',
    'DIAGNOSTICO'
)),
    unidade_codigo bigint not null,
    usuario_titulo varchar
(
    12
) not null,
    motivo varchar
(
    200
),
    observacoes varchar
(
    500
),
    primary key
(
    codigo
)
    );

create table if not exists sgc.atividade
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    mapa_codigo
    bigint
    not
    null,
    descricao
    varchar
(
    255
) not null,
    primary key
(
    codigo
)
    );

create table if not exists sgc.atribuicao_temporaria
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    data_inicio
    timestamp
(
    6
) not null,
    data_termino timestamp
(
    6
) not null,
    unidade_codigo bigint not null,
    usuario_titulo varchar
(
    12
) not null,
    usuario_matricula varchar
(
    8
) not null,
    justificativa varchar
(
    500
),
    primary key
(
    codigo
)
    );

create table if not exists sgc.competencia
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    mapa_codigo
    bigint
    not
    null,
    descricao
    varchar
(
    255
) not null,
    primary key
(
    codigo
)
    );

create table if not exists sgc.competencia_atividade
(
    atividade_codigo
    bigint
    not
    null,
    competencia_codigo
    bigint
    not
    null,
    primary
    key
(
    atividade_codigo,
    competencia_codigo
)
    );

create table if not exists sgc.conhecimento
(
    atividade_codigo
    bigint
    not
    null,
    codigo
    bigint
    generated
    by
    default as
    identity,
    descricao
    varchar
(
    255
) not null,
    primary key
(
    codigo
)
    );

create table if not exists sgc.movimentacao
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    data_hora
    timestamp
(
    6
) not null,
    subprocesso_codigo bigint not null,
    unidade_destino_codigo bigint not null,
    unidade_origem_codigo bigint not null,
    usuario_titulo varchar
(
    12
) not null,
    descricao varchar
(
    255
),
    primary key
(
    codigo
)
    );

create table if not exists sgc.notificacao_email
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    subprocesso_codigo
    bigint,
    tipo_notificacao
    varchar
(
    80
),
    unidade_destino_sigla varchar
(
    20
),
    unidade_origem_sigla varchar
(
    20
),
    usuario_destino_titulo varchar
(
    12
),
    destinatario varchar
(
    255
) not null,
    assunto varchar
(
    500
) not null,
    corpo_html clob not null,
    situacao varchar
(
    30
) default 'PENDENTE' not null check
(
    situacao
    in
(
    'PENDENTE',
    'ENVIANDO',
    'ENVIADO',
    'FALHA_TEMPORARIA',
    'FALHA_DEFINITIVA'
)),
    tentativas integer default 0 not null check
(
    tentativas
    >=
    0
),
    proxima_tentativa_em timestamp
(
    6
),
    data_hora_criacao timestamp
(
    6
) default current_timestamp not null,
    data_hora_envio timestamp
(
    6
),
    ultimo_erro varchar
(
    2000
),
    chave_idempotencia varchar
(
    255
) not null,
    primary key
(
    codigo
),
    unique
(
    chave_idempotencia
)
    );

create table if not exists sgc.configuracao
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    chave
    varchar
(
    50
) not null,
    descricao varchar
(
    255
),
    valor varchar
(
    255
) not null,
    primary key
(
    codigo
)
    );

create table if not exists sgc.unidade_processo
(
    processo_codigo
    bigint
    not
    null,
    unidade_codigo
    bigint
    not
    null,
    nome
    varchar
(
    255
),
    sigla varchar
(
    20
),
    matricula_titular varchar
(
    8
),
    titulo_titular varchar
(
    12
),
    data_inicio_titularidade timestamp
(
    6
),
    tipo varchar
(
    20
) check
(
    tipo
    in
(
    'OPERACIONAL',
    'INTERMEDIARIA',
    'INTEROPERACIONAL',
    'SEM_EQUIPE'
)),
    situacao varchar
(
    20
),
    unidade_superior_codigo bigint,
    primary key
(
    processo_codigo,
    unidade_codigo
)
    );

create table if not exists sgc.servidor_processo
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    processo_codigo
    bigint
    not
    null,
    unidade_codigo
    bigint
    not
    null,
    usuario_titulo
    varchar
(
    12
) not null,
    matricula varchar
(
    8
),
    nome varchar
(
    255
) not null,
    email varchar
(
    255
) not null,
    constraint pk_servidor_processo primary key
(
    codigo
),
    constraint uk_servidor_processo unique
(
    processo_codigo,
    unidade_codigo,
    usuario_titulo
)
    );


-- Foreign Keys (H2 compatible)
alter table if exists sgc.alerta
    add constraint fk_alerta_processo foreign key (processo_codigo) references sgc.processo;
alter table if exists sgc.alerta_usuario
    add constraint fk_alerta_usuario_alerta foreign key (alerta_codigo) references sgc.alerta;
alter table if exists sgc.alerta_usuario
    add constraint fk_alerta_usuario_usuario foreign key (usuario_titulo) references sgc.vw_usuario;
alter table if exists sgc.analise
    add constraint fk_analise_subprocesso foreign key (subprocesso_codigo) references sgc.subprocesso;
alter table if exists sgc.atividade
    add constraint fk_atividade_mapa foreign key (mapa_codigo) references sgc.mapa;
alter table if exists sgc.atribuicao_temporaria
    add constraint fk_atribuicao_temporaria_unidade foreign key (unidade_codigo) references sgc.vw_unidade;
alter table if exists sgc.atribuicao_temporaria
    add constraint fk_atribuicao_temporaria_usuario foreign key (usuario_titulo) references sgc.vw_usuario;
alter table if exists sgc.servidor_processo
    add constraint fk_servidor_processo foreign key (processo_codigo) references sgc.processo;
alter table if exists sgc.competencia
    add constraint fk_competencia_mapa foreign key (mapa_codigo) references sgc.mapa;
alter table if exists sgc.competencia_atividade
    add constraint fk_competencia_atividade_atividade foreign key (atividade_codigo) references sgc.atividade;
alter table if exists sgc.competencia_atividade
    add constraint fk_competencia_atividade_competencia foreign key (competencia_codigo) references sgc.competencia;
alter table if exists sgc.conhecimento
    add constraint fk_conhecimento_atividade foreign key (atividade_codigo) references sgc.atividade;
alter table if exists sgc.mapa
    add constraint fk_mapa_subprocesso foreign key (subprocesso_codigo) references sgc.subprocesso;
alter table if exists sgc.movimentacao
    add constraint fk_movimentacao_subprocesso foreign key (subprocesso_codigo) references sgc.subprocesso;
alter table if exists sgc.movimentacao
    add constraint fk_movimentacao_unidade_destino foreign key (unidade_destino_codigo) references sgc.vw_unidade;
alter table if exists sgc.movimentacao
    add constraint fk_movimentacao_unidade_origem foreign key (unidade_origem_codigo) references sgc.vw_unidade;
alter table if exists sgc.movimentacao
    add constraint fk_movimentacao_usuario foreign key (usuario_titulo) references sgc.vw_usuario;
alter table if exists sgc.notificacao_email
    add constraint fk_notif_email_subprocesso foreign key (subprocesso_codigo) references sgc.subprocesso;
alter table if exists sgc.subprocesso
    add constraint fk_subprocesso_processo foreign key (processo_codigo) references sgc.processo;
alter table if exists sgc.subprocesso
    add constraint fk_subprocesso_unidade foreign key (unidade_codigo) references sgc.vw_unidade;
alter table if exists sgc.unidade_processo
    add constraint fk_up_processo foreign key (processo_codigo) references sgc.processo;
-- FKs entre VIEWs externas removidas para evitar dependência circular nos dados de teste
-- alter table if exists sgc.vw_unidade
--     add constraint fk_unidade_titular foreign key (titulo_titular) references sgc.vw_usuario;
alter table if exists sgc.vw_unidade
    add constraint fk_unidade_superior foreign key (unidade_superior_codigo) references sgc.vw_unidade;
-- alter table if exists sgc.vw_usuario
--     add constraint fk_usuario_unidade_lot foreign key (unidade_lot_codigo) references sgc.vw_unidade;
alter table if exists sgc.vw_usuario_perfil_unidade
    add constraint fk_usuario_perfil_usuario foreign key (usuario_titulo) references sgc.vw_usuario;
alter table if exists sgc.vw_usuario_perfil_unidade
    add constraint fk_usuario_perfil_unidade foreign key (unidade_codigo) references sgc.vw_unidade;
alter table if exists sgc.vw_responsabilidade
    add constraint fk_responsabilidade_unidade foreign key (unidade_codigo) references sgc.vw_unidade;
alter table if exists sgc.vw_responsabilidade
    add constraint fk_responsabilidade_usuario foreign key (usuario_titulo) references sgc.vw_usuario;

-- Tabelas do Módulo de Diagnóstico
create table if not exists sgc.diagnostico
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    subprocesso_codigo
    bigint
    not
    null
    unique,
    data_conclusao
    timestamp
(
    6
),
    justificativa_conclusao text,
    constraint pk_diagnostico primary key
(
    codigo
),
    constraint fk_diagnostico_subprocesso foreign key
(
    subprocesso_codigo
) references sgc.subprocesso
    );

create table if not exists sgc.avaliacao_servidor
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    diagnostico_codigo
    bigint
    not
    null,
    servidor_titulo
    varchar
(
    12
) not null,
    servidor_nome_snapshot varchar
(
    255
),
    competencia_codigo bigint not null,
    autoimportancia int check
(
    autoimportancia
    between
    0
    and
    6
),
    autodominio int check
(
    autodominio
    between
    0
    and
    6
),
    chefia_importancia int check
(
    chefia_importancia
    between
    0
    and
    6
),
    chefia_dominio int check
(
    chefia_dominio
    between
    0
    and
    6
),
    consenso_importancia int check
(
    consenso_importancia
    between
    0
    and
    6
),
    consenso_dominio int check
(
    consenso_dominio
    between
    0
    and
    6
),
    importancia int check
(
    importancia
    between
    0
    and
    6
),
    dominio int check
(
    dominio
    between
    0
    and
    6
),
    gap int,
    observacoes text,
    situacao_servidor varchar
(
    50
) not null check
(
    situacao_servidor
    in
(
    'AUTOAVALIACAO_NAO_INICIADA',
    'AUTOAVALIACAO_CONCLUIDA',
    'CONSENSO_CRIADO',
    'CONSENSO_APROVADO',
    'AVALIACAO_IMPOSSIBILITADA'
)),
    situacao_servidor_anterior varchar
(
    50
) check
(
    situacao_servidor_anterior
    in
(
    'AUTOAVALIACAO_NAO_INICIADA',
    'AUTOAVALIACAO_CONCLUIDA',
    'CONSENSO_CRIADO',
    'CONSENSO_APROVADO',
    'AVALIACAO_IMPOSSIBILITADA'
)),
    constraint pk_avaliacao_servidor primary key
(
    codigo
),
    constraint fk_avaliacao_diagnostico foreign key
(
    diagnostico_codigo
) references sgc.diagnostico,
    constraint fk_avaliacao_servidor foreign key
(
    servidor_titulo
) references sgc.vw_usuario,
    constraint fk_avaliacao_competencia foreign key
(
    competencia_codigo
) references sgc.competencia,
    constraint uk_avaliacao_servidor unique
(
    diagnostico_codigo,
    servidor_titulo,
    competencia_codigo
)
    );

create table if not exists sgc.situacao_capacitacao
(
    codigo
    bigint
    generated
    by
    default as
    identity,
    diagnostico_codigo
    bigint
    not
    null,
    servidor_titulo
    varchar
(
    12
) not null,
    servidor_nome_snapshot varchar
(
    255
),
    unidade_codigo_snapshot bigint,
    unidade_sigla_snapshot varchar
(
    20
),
    unidade_nome_snapshot varchar
(
    255
),
    competencia_codigo bigint not null,
    situacao_capacitacao varchar
(
    2
) check
(
    situacao_capacitacao
    in
(
    'NA',
    'AC',
    'EC',
    'C',
    'I'
)),
    constraint pk_situacao_capacitacao primary key
(
    codigo
),
    constraint fk_situacao_capacitacao_diagnostico foreign key
(
    diagnostico_codigo
) references sgc.diagnostico,
    constraint fk_situacao_capacitacao_servidor foreign key
(
    servidor_titulo
) references sgc.vw_usuario,
    constraint fk_situacao_capacitacao_competencia foreign key
(
    competencia_codigo
) references sgc.competencia,
    constraint uk_situacao_capacitacao unique
(
    diagnostico_codigo,
    servidor_titulo,
    competencia_codigo
)
    );

-- Índices de performance para otimização de consultas e junções
create index if not exists idx_diagnostico_subprocesso on sgc.diagnostico (subprocesso_codigo);
create index if not exists idx_avaliacao_diagnostico on sgc.avaliacao_servidor (diagnostico_codigo);
create index if not exists idx_avaliacao_servidor on sgc.avaliacao_servidor (servidor_titulo);
create index if not exists idx_avaliacao_competencia on sgc.avaliacao_servidor (competencia_codigo);
create index if not exists idx_situacao_capacitacao_diagnostico on sgc.situacao_capacitacao (diagnostico_codigo);
create index if not exists idx_situacao_capacitacao_servidor on sgc.situacao_capacitacao (servidor_titulo);
create index if not exists idx_situacao_capacitacao_competencia on sgc.situacao_capacitacao (competencia_codigo);

-- Indices para otimização de performance (evitar full table scan em FKs)
create index if not exists idx_atividade_mapa on sgc.atividade (mapa_codigo);
create index if not exists idx_competencia_mapa on sgc.competencia (mapa_codigo);
create index if not exists idx_conhecimento_atividade on sgc.conhecimento (atividade_codigo);
create index if not exists idx_mapa_subprocesso on sgc.mapa (subprocesso_codigo);
create index if not exists idx_analise_subprocesso on sgc.analise (subprocesso_codigo);
create index if not exists idx_movimentacao_subprocesso on sgc.movimentacao (subprocesso_codigo);
create index if not exists idx_competencia_atividade_inv on sgc.competencia_atividade (competencia_codigo);
create index if not exists idx_subprocesso_processo on sgc.subprocesso (processo_codigo);
create index if not exists idx_subprocesso_unidade on sgc.subprocesso (unidade_codigo);
create index if not exists idx_alerta_unidade_destino on sgc.alerta (unidade_destino_codigo);
create index if not exists idx_alerta_processo on sgc.alerta (processo_codigo);
create index if not exists idx_alerta_usuario_usuario on sgc.alerta_usuario (usuario_titulo);
create index if not exists idx_unidade_processo_unidade on sgc.unidade_processo (unidade_codigo);
create index if not exists idx_notif_email_fila on sgc.notificacao_email (situacao, proxima_tentativa_em, data_hora_criacao);
create index if not exists idx_notif_email_subproc_sit on sgc.notificacao_email (subprocesso_codigo, situacao);
create index if not exists idx_notif_email_usuario on sgc.notificacao_email (usuario_destino_titulo);

alter table if exists sgc.avaliacao_servidor add column if not exists servidor_nome_snapshot varchar (255);
alter table if exists sgc.avaliacao_servidor add column if not exists autoimportancia int;
alter table if exists sgc.avaliacao_servidor add column if not exists autodominio int;
alter table if exists sgc.avaliacao_servidor add column if not exists chefia_importancia int;
alter table if exists sgc.avaliacao_servidor add column if not exists chefia_dominio int;
alter table if exists sgc.avaliacao_servidor add column if not exists consenso_importancia int;
alter table if exists sgc.avaliacao_servidor add column if not exists consenso_dominio int;
alter table if exists sgc.situacao_capacitacao add column if not exists servidor_nome_snapshot varchar (255);
alter table if exists sgc.situacao_capacitacao add column if not exists unidade_codigo_snapshot bigint;
alter table if exists sgc.situacao_capacitacao add column if not exists unidade_sigla_snapshot varchar (20);
alter table if exists sgc.situacao_capacitacao add column if not exists unidade_nome_snapshot varchar (255);
alter table if exists sgc.situacao_capacitacao alter column situacao_capacitacao drop not null;

-- Feedback (perfil hom)
create table if not exists sgc.FEEDBACK
(
    id
    uuid
    not
    null,
    tipo
    varchar
(
    20
) not null,
    nota varchar
(
    2000
) not null,
    metadata_json clob,
    caminho_screenshot varchar
(
    500
),
    usuario_id varchar
(
    100
) not null,
    usuario_nome varchar
(
    200
) not null,
    enviado_em timestamp with time zone not null,
                             rota varchar (500) not null,
    status varchar
(
    20
) default 'NOVO' not null,
    constraint pk_FEEDBACK primary key
(
    id
),
    constraint ck_feedback_tipo check
(
    tipo
    in
(
    'BUG',
    'SUGESTAO',
    'QUESTAO',
    'ELOGIO'
)),
    constraint ck_feedback_status check
(
    status
    in
(
    'NOVO',
    'REVISADO',
    'RESOLVIDO',
    'DESCARTADO'
))
    );

create index if not exists idx_feedback_status on sgc.FEEDBACK (status);
create index if not exists idx_feedback_usuario on sgc.FEEDBACK (usuario_id);
create index if not exists idx_feedback_data on sgc.FEEDBACK (enviado_em);
