-- #################################################################
-- SCRIPT DDL ORACLE PARA CRIAÇÃO DAS VIEWS DO MODELO DE DADOS SGC (VERSÃO OTIMIZADA)
-- #################################################################

-- 1. View VW_VINCULACAO_UNIDADE
-- Otimização: Uso de Recursive CTE em vez de SYS_CONNECT_BY_PATH + RegEx.
CREATE OR REPLACE VIEW VW_VINCULACAO_UNIDADE_2
            (unidade_atual_codigo, unidade_anterior_codigo, demais_unidades_historicas) AS
WITH Hierarquia (unidade_atual, unidade_anterior, cd_iteracao, proximo_pai, nivel) AS (
    SELECT cd AS unidade_atual,
           cod_unid_tse_ant AS unidade_anterior,
           cd AS cd_iteracao,
           cod_unid_tse_ant AS proximo_pai,
           1 AS nivel
    FROM SRH2.UNIDADE_TSE
    WHERE SIT_UNID = 'C' OR SIT_UNID LIKE 'O%'
    UNION ALL
    SELECT h.unidade_atual,
           h.unidade_anterior,
           u.cd AS cd_iteracao,
           u.cod_unid_tse_ant AS proximo_pai,
           h.nivel + 1
    FROM Hierarquia h
    JOIN SRH2.UNIDADE_TSE u ON u.cd = h.proximo_pai
)
SELECT unidade_atual AS unidade_atual_codigo,
       unidade_anterior AS unidade_anterior_codigo,
       (SELECT LISTAGG(cd_iteracao, ', ') WITHIN GROUP (ORDER BY nivel ASC)
        FROM Hierarquia h2
        WHERE h2.unidade_atual = h1.unidade_atual AND h2.nivel >= 3) AS demais_unidades_historicas
FROM (SELECT DISTINCT unidade_atual, unidade_anterior FROM Hierarquia) h1
ORDER BY unidade_atual;


-- 2. View VW_ZONA_RESP_CENTRAL
-- Otimização: Filtro temporal sargable (sem trunc na coluna) e junção por LIKE para uso de índice.
CREATE OR REPLACE VIEW VW_ZONA_RESP_CENTRAL_2
            (codigo_central, sigla_central, codigo_zona_resp, sigla_zona_resp, data_inicio_resp, data_fim_resp) AS
select uni_c.cd             as codigo_central,
       uni_c.sigla_unid_tse as sigla_central,
       uni_z.cd             as codigo_zona_resp,
       uni_z.sigla_unid_tse as sigla_zona_resp,
       r.datainicio         as data_inicio_resp,
       r.datatermino        as data_fim_resp
from (select cd, sigla_unid_tse from srh2.unidade_tse where sigla_unid_tse like 'CAE%' and sit_unid not like 'E%') uni_c
         join corau.ct_central c on c.sigla like uni_c.sigla_unid_tse || '%'
         left join (
            select rc.central_id, rc.zona_id, e.datainicio, e.datatermino
            from corau.evento e
            join corau.resp_central rc on e.id = rc.id
            -- OTIMIZACAO SARGABLE: e.datainicio < trunc(sysdate)+1 AND e.datatermino >= trunc(sysdate)
            where e.datainicio < trunc(sysdate) + 1 and e.datatermino >= trunc(sysdate)
         ) r on c.id = r.central_id
         left join corau.ct_zona z on r.zona_id = z.id
         left join (select cd, num_ze, sigla_unid_tse from srh2.unidade_tse where num_ze is not null and sit_unid not like 'E%') uni_z
                   on z.numero = uni_z.num_ze;


-- 3. View VW_UNIDADE
-- Otimização: Substituição de subqueries correlacionadas por CTEs de agregação (Group By).
CREATE OR REPLACE VIEW VW_UNIDADE_2
            (codigo, nome, sigla, matricula_titular, titulo_titular, data_inicio_titularidade, tipo, situacao,
             unidade_superior_codigo)
AS
WITH tb_unidade_base AS (
    select cd, ds, sigla_unid_tse, sit_unid, cod_unid_super,
           case when sigla_unid_tse like 'CAE%' and sit_unid not like 'E%' then 'S' else 'N' end as is_central
    from srh2.unidade_tse
    where cd not in (1, 6, 19, 37, 634, 635, 637)
),
tb_unidade AS (
    select b.cd, b.ds, b.sigla_unid_tse, b.sit_unid,
           case
               when b.is_central = 'S' then (select codigo_zona_resp from vw_zona_resp_central where codigo_central = b.cd)
               when b.cod_unid_super in (6, 19, 37, 634, 635, 637) then 1
               else b.cod_unid_super 
           end as cod_unid_super
    from tb_unidade_base b
),
agregacao_lotacao AS (
    select cod_unid_tse, count(1) as qtd_servidores
    from srh2.lotacao
    where dt_fim_lotacao is null
    group by cod_unid_tse
),
agregacao_filhas AS (
    select cod_unid_super, count(1) as qtd_unidades_filhas
    from tb_unidade
    where cod_unid_super is not null and sit_unid not like 'E%'
    group by cod_unid_super
),
agregacao_servidores_total AS (
    select u.cd as cod_unid_tse,
           nvl(al.qtd_servidores, 0) + nvl(f.soma_filhos_especificos, 0) as qtd_servidores_calculado
    from tb_unidade u
    left join agregacao_lotacao al on u.cd = al.cod_unid_tse
    left join (
        select u2.cod_unid_super, count(1) as soma_filhos_especificos
        from tb_unidade u2
        join agregacao_lotacao al2 on u2.cd = al2.cod_unid_tse
        where al2.qtd_servidores = 1
          and not exists (select 1 from tb_unidade u3 where u3.cod_unid_super = u2.cd and u3.sit_unid not like 'E%')
        group by u2.cod_unid_super
    ) f on u.cd = f.cod_unid_super
),
agregacao_filhas_complexas AS (
    select u_pai.cd as cod_unid_pai, count(1) as qtd_filhas_complexas
    from tb_unidade u_pai
    join tb_unidade u_filha on u_pai.cd = u_filha.cod_unid_super
    left join agregacao_lotacao al on u_filha.cd = al.cod_unid_tse
    left join agregacao_filhas af on u_filha.cd = af.cod_unid_super
    where nvl(al.qtd_servidores, 0) > 1 or nvl(af.qtd_unidades_filhas, 0) > 0
    group by u_pai.cd
),
titulares AS (
    select v.num_vaga_com, l.cod_unid_tse, s.mat_servidor, s.num_tit_ele, c.dt_ingresso,
           c.cod_comissionado, v.nom_atu_com, v.vago
    from srh2.qfc_ocup_com c
    join srh2.qfc_vagas_com v on c.cod_comissionado = v.cod_comissionado and c.nome_com = v.nome_com and c.num_vaga_com = v.num_vaga_com
    join srh2.lotacao l on c.mat_servidor = l.mat_servidor and l.dt_fim_lotacao is null
    join srh2.servidor s on c.mat_servidor = s.mat_servidor
    where c.dt_dispensa is null and nvl(c.titular_com, 0) = 1
)
SELECT codigo, nome, sigla, matricula_titular, titulo_titular, data_inicio_titularidade, tipo, situacao, unidade_superior_codigo
FROM (
    select u.cd as codigo, u.ds as nome, u.sigla_unid_tse as sigla,
           c.mat_servidor as matricula_titular, c.num_tit_ele as titulo_titular, c.dt_ingresso as data_inicio_titularidade,
           case
               when u.sit_unid like 'E%' then ''
               when nvl(p.qtd_unidades_filhas, 0) = 0 then (case when nvl(ast.qtd_servidores_calculado, 0) < 2 then 'SEM_EQUIPE' else 'OPERACIONAL' end)
               when nvl(pcomplex.qtd_filhas_complexas, 0) = 0 then 'OPERACIONAL'
               when nvl(ast.qtd_servidores_calculado, 0) > 1 then 'INTEROPERACIONAL'
               else 'INTERMEDIARIA'
           end as tipo,
           case when u.sit_unid like 'E%' then 'INATIVA' else 'ATIVA' end as situacao,
           u.cod_unid_super as unidade_superior_codigo
    from tb_unidade u
    left join agregacao_servidores_total ast on u.cd = ast.cod_unid_tse
    left join agregacao_filhas p on u.cd = p.cod_unid_super
    left join agregacao_filhas_complexas pcomplex on u.cd = pcomplex.cod_unid_pai
    left join titulares c on u.cd = c.cod_unid_tse
    union all
    select 1, 'UNIDADE RAIZ ADMINISTRATIVA', 'ADMIN', null, null, null, 'RAIZ', 'ATIVA', null from dual
);


-- 4. View VW_USUARIO
-- Otimização: Uso de CTE para evitar recalcular VW_UNIDADE para cada linha.
CREATE OR REPLACE VIEW VW_USUARIO_2 (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo) AS
WITH UnidadesPre (codigo, sigla, tipo, unidade_superior_codigo) AS (
    select codigo, sigla, tipo, unidade_superior_codigo from vw_unidade
)
select s.num_tit_ele                   as titulo,
       s.mat_servidor                  as matricula,
       s.nom                           as nome,
       s.e_mail                        as email,
       r.ramal_servidor                as ramal,
       l.cod_unid_tse                  as unidade_lot_codigo,
       (select decode(u.tipo, 'SEM_EQUIPE',
                      decode(u.unidade_superior_codigo,
                             1, case
                                    when u.sigla = 'GP' then (select codigo from UnidadesPre where sigla = 'ASPRE' and tipo = 'OPERACIONAL')
                                    else (select codigo from UnidadesPre where sigla = 'SEDOC' and tipo = 'OPERACIONAL') end,
                             u.unidade_superior_codigo),
                      u.codigo)
        from UnidadesPre u
        where u.codigo = l.cod_unid_tse) as unidade_comp_codigo
from srh2.servidor s
join srh2.lotacao l on s.mat_servidor = l.mat_servidor and l.dt_fim_lotacao is null
outer apply (select ramal_servidor
             from (select ramal_servidor
                   from srh2.lot_ramais_servidores
                   where mat_servidor = s.mat_servidor
                     and unid_lot = l.cod_unid_tse
                     and ramal_principal = 1
                   order by dt_ini_lotacao desc)
             where rownum = 1) r;


-- 5. View VW_RESPONSABILIDADE
-- Otimização: Filtros temporais sargable.
CREATE OR REPLACE VIEW VW_RESPONSABILIDADE_2
            (unidade_codigo, usuario_matricula, usuario_titulo, tipo, data_inicio, data_fim) AS
select u.codigo                                                                as unidade_codigo,
       coalesce(a.usuario_matricula, s.mat_serv_com_subs, u.matricula_titular) as usuario_matricula,
       coalesce(a.usuario_titulo, s.num_tit_ele, u.titulo_titular)             as usuario_titulo,
       case
           when a.usuario_matricula is not null then 'ATRIBUICAO_TEMPORARIA'
           when s.mat_serv_com_subs is not null then 'SUBSTITUTO'
           else 'TITULAR' end                                                  as tipo,
       coalesce(a.data_inicio, s.dt_ini_subst, u.data_inicio_titularidade)     as data_inicio,
       coalesce(a.data_termino, s.dt_fim_subst)                                as data_fim
from (select codigo, matricula_titular, titulo_titular, data_inicio_titularidade
      from vw_unidade
      where situacao = 'ATIVA'
        and tipo in ('OPERACIONAL', 'INTEROPERACIONAL', 'INTERMEDIARIA')) u
         left join (select sub.mat_servidor, sub.mat_serv_com_subs, s.num_tit_ele, sub.dt_ini_subst, sub.dt_fim_subst
                    from srh2.qfc_ocup_com c
                             join srh2.qfc_subst_com sub
                                  on c.mat_servidor = sub.mat_servidor and c.dt_ingresso = sub.dt_ingresso and
                                     c.tp_ocup_com = sub.tp_ocup_com
                             join srh2.servidor s on sub.mat_serv_com_subs = s.mat_servidor
                    where nvl(c.titular_com, 0) = 1
                      -- OTIMIZACAO SARGABLE
                      and sub.dt_ini_subst < trunc(sysdate) + 1 and sub.dt_fim_subst >= trunc(sysdate) ) s
                   on u.matricula_titular = s.mat_servidor
         left join (select unidade_codigo, usuario_matricula, usuario_titulo, data_inicio, data_termino
                    from ATRIBUICAO_TEMPORARIA
                    -- OTIMIZACAO SARGABLE
                    where data_inicio < trunc(sysdate) + 1 and data_termino >= trunc(sysdate) ) a
                   on u.codigo = a.unidade_codigo;


-- 6. View VW_USUARIO_PERFIL_UNIDADE
-- Otimização: Uso de UNION ALL para evitar sorteio/deduplicação desnecessária.
CREATE OR REPLACE VIEW VW_USUARIO_PERFIL_UNIDADE_2 (usuario_titulo, perfil, unidade_codigo) AS
select usuario_titulo, perfil, unidade_codigo
from (select a.usuario_titulo, 'ADMIN' as perfil, 1 as unidade_codigo
      from administrador a
               join vw_usuario u on u.titulo = a.usuario_titulo
      union all
      select r.usuario_titulo, 'GESTOR' as perfil, r.unidade_codigo
      from vw_responsabilidade r
               join vw_unidade u on r.unidade_codigo = u.codigo and u.tipo in ('INTERMEDIARIA', 'INTEROPERACIONAL')
      union all
      select r.usuario_titulo, 'CHEFE' as perfil, r.unidade_codigo
      from vw_responsabilidade r
               join vw_unidade u on r.unidade_codigo = u.codigo and u.tipo in ('INTEROPERACIONAL', 'OPERACIONAL')
      union all
      select usu.titulo as usuario_titulo, 'SERVIDOR' as perfil, uni.codigo as unidade_codigo
      from vw_usuario usu
               join vw_unidade uni on usu.unidade_comp_codigo = uni.codigo
      where usu.titulo <> uni.titulo_titular);
