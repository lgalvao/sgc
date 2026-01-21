-- #################################################################
-- SCRIPT DDL ORACLE PARA CRIAÇÃO DAS VIEWS DO MODELO DE DADOS SGC
-- #################################################################

-- Permissões necessárias no SRH2

GRANT SELECT ON SRH2.UNIDADE_TSE TO SGC;
GRANT SELECT ON SRH2.LOTACAO TO SGC;
GRANT SELECT ON SRH2.QFC_OCUP_COM TO SGC;
GRANT SELECT ON SRH2.QFC_VAGAS_COM TO SGC;
GRANT SELECT ON SRH2.SERVIDOR TO SGC;
GRANT SELECT ON SRH2.LOT_RAMAIS_SERVIDORES TO SGC;
GRANT SELECT ON SRH2.QFC_SUBST_COM TO SGC;

-- Permissões necessárias no CORAU (SIGMA

GRANT SELECT ON CORAU.RESP_CENTRAL TO SGC;
GRANT SELECT ON CORAU.EVENTO TO SGC;
GRANT SELECT ON CORAU.CT_CENTRAL TO SGC;
GRANT SELECT ON CORAU.CT_ZONA TO SGC;

-- 1. View VW_VINCULACAO_UNIDADE

CREATE OR REPLACE VIEW VW_VINCULACAO_UNIDADE (unidade_atual_codigo, unidade_anterior_codigo, demais_unidades_historicas) AS
WITH HistoricoCompleto AS (
  -- 1. CTE: Encontra o caminho completo da raiz até a atual
  SELECT
    t.CD,
    t.COD_UNID_TSE_ANT,
    -- Constrói o caminho completo da raiz para a atual
    LTRIM(SYS_CONNECT_BY_PATH(t.CD, '->'), '->') AS Caminho_Completo,
    LEVEL AS Nivel
  FROM
    SRH2.UNIDADE_TSE t
  START WITH
    t.COD_UNID_TSE_ANT IS NULL
  CONNECT BY NOCYCLE
    PRIOR t.CD = t.COD_UNID_TSE_ANT
),
HistoricoExtinto AS (
  -- 2. CTE: Processa o histórico para tokenização e LISTAGG
  SELECT
    h.CD,
    h.COD_UNID_TSE_ANT,
    h.Nivel,
    CASE 
        -- A string histórica só é gerada se o nível for maior que 2 (i.e., existe mais que Atual e Antecessor)
        WHEN h.Nivel > 2 THEN 
            REGEXP_SUBSTR(
                h.Caminho_Completo, 
                '^(.*?)->[^>]+->[^>]+$', -- Expressão para isolar o histórico
                1, 1, 'i', 1
            )
        END AS Historico_String_Pura
  FROM HistoricoCompleto h
)
SELECT
    u.CD AS unidade_atual_codigo,
    u.COD_UNID_TSE_ANT AS unidade_anterior_codigo,
    (
        -- 3. Subconsulta para tokenização, inversão e concatenação (EXECUTADA SOMENTE SE HOUVER HISTÓRICO)
        SELECT
            LISTAGG(
                LTRIM(REGEXP_SUBSTR(he.Historico_String_Pura, '[^-]+', 1, LEVEL), ' >'),
                ', '
            ) WITHIN GROUP (ORDER BY LEVEL DESC)
        FROM DUAL
        CONNECT BY LEVEL <= REGEXP_COUNT(he.Historico_String_Pura, '->') + 1
    ) AS demais_unidades_historicas
FROM
    SRH2.UNIDADE_TSE u -- Tabela Principal (274 registros)
LEFT JOIN
    HistoricoExtinto he ON u.CD = he.CD
WHERE
    -- FILTRO DE ATIVIDADE CONFORME SOLICITADO
    u.SIT_UNID = 'C' OR u.SIT_UNID LIKE 'O%'
ORDER BY
    u.CD;


-- 2. View VW_ZONA_RESP_CENTRAL

CREATE OR REPLACE VIEW VW_ZONA_RESP_CENTRAL (codigo_central, sigla_central, codigo_zona_resp, sigla_zona_resp, data_inicio_resp, data_fim_resp) AS
select uni_c.cd as codigo_central, uni_c.sigla_unid_tse as sigla_central,
       uni_z.cd as codigo_zona_resp, uni_z.sigla_unid_tse as sigla_zona_resp,
       r.datainicio as data_inicio_resp, r.datatermino as data_fim_resp
from (
    select * from srh2.unidade_tse where sigla_unid_tse like 'CAE%' and sit_unid not like 'E%'
) uni_c
join corau.ct_central c on uni_c.sigla_unid_tse = substr(c.sigla, 1, 5)
left join (
    select e.id, e.datainicio, e.datatermino, rc.central_id, rc.zona_id
    from corau.evento e
    join corau.resp_central rc on e.id = rc.id
    where sysdate between trunc(e.datainicio) and trunc(e.datatermino + 1)
) r on c.id = r.central_id
left join corau.ct_zona z on r.zona_id = z.id
left join (
    select * from srh2.unidade_tse where num_ze is not null and sit_unid not like 'E%'
) uni_z on z.numero = uni_z.num_ze


-- 3. View VW_UNIDADE

CREATE OR REPLACE VIEW VW_UNIDADE (codigo, nome, sigla, matricula_titular, titulo_titular, data_inicio_titularidade, tipo, situacao, unidade_superior_codigo) AS
WITH tb_unidade AS (
    select cd, ds, sigla_unid_tse, sit_unid, 
           case when sigla_unid_tse like 'CAE%' and sit_unid not like 'E%' then (select codigo_zona_resp from vw_zona_resp_central where codigo_central = cd)
                when cod_unid_super in (6, 19, 37, 634, 635, 637) then 1
                else cod_unid_super end as cod_unid_super
    from srh2.unidade_tse where cd not in (1, 6, 19, 37, 634, 635, 637)
)
SELECT codigo, nome, sigla, matricula_titular, titulo_titular, data_inicio_titularidade, tipo, situacao, unidade_superior_codigo
FROM (
    select u.cd as codigo, u.ds as nome, u.sigla_unid_tse as sigla, c.mat_servidor as matricula_titular, c.num_tit_ele as titulo_titular, c.dt_ingresso as data_inicio_titularidade,
           case when u.sit_unid like 'E%' then ''
                when nvl(p.qtd_unidades_filhas, 0) = 0 then case when nvl(l.qtd_servidores, 0) < 2 then 'SEM_EQUIPE' else 'OPERACIONAL' end
                when not exists (select 1 from tb_unidade uf 
                                 where uf.cod_unid_super = u.cd 
                                   and ((select count(1) from srh2.lotacao 
                                        where dt_fim_lotacao is null 
                                          and cod_unid_tse = uf.cd) > 1
                                       or exists (select 1 from tb_unidade where cod_unid_super = uf.cd))) then 'OPERACIONAL'
                when nvl(l.qtd_servidores, 0) > 1 then 'INTEROPERACIONAL'
                else 'INTERMEDIARIA'
           end as tipo,
           case when u.sit_unid like 'E%' then 'INATIVA' else 'ATIVA' end as situacao, u.cod_unid_super as unidade_superior_codigo,
           nvl(l.qtd_servidores, 0) as qtd_servidores_lotados, nvl(p.qtd_unidades_filhas, 0) as qtd_unidades_filhas
    from tb_unidade u
    left join (
        select l1.cod_unid_tse, count(1) + nvl((select sum(DECODE(qtd_servidores, 1, 1, 0))
                                                from (
                                                    select l2.cod_unid_tse, u2.cod_unid_super, count(1) as qtd_servidores
                                                    from srh2.lotacao l2
                                                    join tb_unidade u2 on l2.cod_unid_tse = u2.cd
                                                    where l2.dt_fim_lotacao is null
                                                    and not exists (select 1 from tb_unidade where cod_unid_super = u2.cd and sit_unid not like 'E%')
                                                    group by l2.cod_unid_tse, u2.cod_unid_super
                                                )
                                                where cod_unid_super = l1.cod_unid_tse), 0) as qtd_servidores
        from srh2.lotacao l1 where l1.dt_fim_lotacao is null
        group by l1.cod_unid_tse
    ) l on u.cd = l.cod_unid_tse
    left join (
        select cod_unid_super, count(1) as qtd_unidades_filhas
        from tb_unidade
        where cod_unid_super is not null and sit_unid not like 'E%'
        group by cod_unid_super
    ) p on u.cd = p.cod_unid_super
    left join (
        select v.num_vaga_com, l.cod_unid_tse, s.mat_servidor, s.num_tit_ele, c.dt_ingresso,
               c.cod_comissionado, v.nom_atu_com, v.vago
        from srh2.qfc_ocup_com c
        join srh2.qfc_vagas_com v on c.cod_comissionado = v.cod_comissionado and 
                                     c.nome_com = v.nome_com and c.num_vaga_com = v.num_vaga_com
        join srh2.lotacao l on c.mat_servidor = l.mat_servidor and l.dt_fim_lotacao is null
        join srh2.servidor s on c.mat_servidor = s.mat_servidor
        where c.dt_dispensa is null
        and nvl(c.titular_com, 0) = 1
    ) c on u.cd = c.cod_unid_tse
    union
    select 1, 'UNIDADE RAIZ ADMINISTRATIVA', 'ADMIN', null, null, null, 'RAIZ', 'ATIVA', null, 0, 0 from dual
);


-- 4. View VW_USUARIO

CREATE OR REPLACE VIEW VW_USUARIO (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo) AS
select s.num_tit_ele as titulo, s.mat_servidor as matricula, s.nom as nome, s.e_mail as email,
       r.ramal_servidor as ramal, l.cod_unid_tse as unidade_lot_codigo,
       (select decode(tipo, 'SEM_EQUIPE', 
                            decode(unidade_superior_codigo,
                                   1, case when sigla = 'GP' then (select codigo from vw_unidade 
                                                                   where sigla = 'ASPRE'
                                                                     and tipo = 'OPERACIONAL')
                                           else (select codigo from vw_unidade 
                                                 where sigla = 'SEDOC'
                                                   and tipo = 'OPERACIONAL') end,
                                   unidade_superior_codigo),
                            codigo)
        from vw_unidade where codigo = l.cod_unid_tse) as unidade_comp_codigo
from srh2.servidor s
join srh2.lotacao l on s.mat_servidor = l.mat_servidor and l.dt_fim_lotacao is null
left join srh2.lot_ramais_servidores r on s.mat_servidor = r.mat_servidor and l.cod_unid_tse = r.unid_lot and r.ramal_principal = 1;


-- 5. View VW_RESPONSABILIDADE

CREATE OR REPLACE VIEW VW_RESPONSABILIDADE (unidade_codigo, usuario_matricula, usuario_titulo, tipo, data_inicio, data_fim) AS
select u.codigo as unidade_codigo, 
       coalesce(a.usuario_matricula, s.mat_serv_com_subs, u.matricula_titular) as usuario_matricula,
       coalesce(a.usuario_titulo, s.num_tit_ele, u.titulo_titular) as usuario_titulo,
       case when a.usuario_matricula is not null then 'ATRIBUICAO_TEMPORARIA'
            when s.mat_serv_com_subs is not null then 'SUBSTITUTO'
            else 'TITULAR' end as tipo,
       coalesce(a.data_inicio, s.dt_ini_subst, u.data_inicio_titularidade) as data_inicio,
       coalesce(a.data_termino, s.dt_fim_subst) as data_fim
from (
    select codigo, matricula_titular, titulo_titular, data_inicio_titularidade
    from vw_unidade where situacao = 'ATIVA' and tipo in ('OPERACIONAL', 'INTEROPERACIONAL', 'INTERMEDIARIA')
) u
left join (
    select sub.mat_servidor, sub.mat_serv_com_subs, s.num_tit_ele, sub.dt_ini_subst, sub.dt_fim_subst
    from srh2.qfc_ocup_com c 
    join srh2.qfc_subst_com sub on c.mat_servidor = sub.mat_servidor and c.dt_ingresso = sub.dt_ingresso and c.tp_ocup_com = sub.tp_ocup_com
    join srh2.servidor s on sub.mat_serv_com_subs = s.mat_servidor
    where nvl(c.titular_com, 0) = 1 and sysdate between trunc(sub.dt_ini_subst) and trunc(sub.dt_fim_subst + 1)
) s on u.matricula_titular = s.mat_servidor
left join (
    select unidade_codigo, usuario_matricula, usuario_titulo, data_inicio, data_termino 
    from ATRIBUICAO_TEMPORARIA
    where sysdate between trunc(data_inicio) and trunc(data_termino + 1)
) a on u.codigo = a.unidade_codigo;


-- 6. View VW_USUARIO_PERFIL_UNIDADE

CREATE OR REPLACE VIEW VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) AS
select usuario_titulo, perfil, unidade_codigo from (
    select a.usuario_titulo, 'ADMIN' as perfil, 1 as unidade_codigo 
    from administrador a join vw_usuario u on u.titulo = a.usuario_titulo
    union
    select r.usuario_titulo, 'GESTOR' as perfil, r.unidade_codigo
    from vw_responsabilidade r
    join vw_unidade u on r.unidade_codigo = u.codigo and u.tipo in ('INTERMEDIARIA', 'INTEROPERACIONAL')
    union
    select r.usuario_titulo, 'CHEFE' as perfil, r.unidade_codigo
    from vw_responsabilidade r
    join vw_unidade u on r.unidade_codigo = u.codigo and u.tipo in ('INTEROPERACIONAL', 'OPERACIONAL')
    union
    select usu.titulo as usuario_titulo, 'SERVIDOR' as perfil, uni.codigo as unidade_codigo
    from vw_usuario usu join vw_unidade uni on usu.unidade_comp_codigo = uni.codigo
    where usu.titulo <> uni.titulo_titular
);
