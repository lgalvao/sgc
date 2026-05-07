-- #################################################################
-- SCRIPT DDL ORACLE PARA CRIACAO DAS VIEWS DO MODELO DE DADOS SGC
-- VERSAO OTIMIZADA - CODEX
-- #################################################################
--
-- Objetivo desta versao:
-- * manter os mesmos nomes e colunas das views atuais;
-- * reduzir regex, subconsultas correlacionadas e reprocessamento de VW_UNIDADE_2;
-- * tornar filtros temporais mais amigaveis a indices, sem TRUNC nas colunas.

-- 1. View VW_VINCULACAO_UNIDADE_2
-- Otimizacao: substitui SYS_CONNECT_BY_PATH + REGEXP por caminhada hierarquica direta
-- a partir de cada unidade atual. LISTAGG ignora NULL, preservando NULL quando nao
-- existem historicos alem da unidade anterior.

CREATE OR REPLACE VIEW VW_VINCULACAO_UNIDADE_2
            (unidade_atual_codigo, unidade_anterior_codigo, demais_unidades_historicas) AS
WITH hierarquia AS (
    SELECT CONNECT_BY_ROOT u.cd               AS unidade_atual_codigo,
           CONNECT_BY_ROOT u.cod_unid_tse_ant AS unidade_anterior_codigo,
           u.cd                               AS unidade_historica_codigo,
           LEVEL                              AS nivel
    FROM srh2.unidade_tse u
    START WITH u.sit_unid = 'C'
            OR u.sit_unid LIKE 'O%'
    CONNECT BY NOCYCLE PRIOR u.cod_unid_tse_ant = u.cd
)
SELECT unidade_atual_codigo,
       unidade_anterior_codigo,
       LISTAGG(
               CASE WHEN nivel >= 3 THEN unidade_historica_codigo END,
               ', '
       ) WITHIN GROUP (ORDER BY nivel) AS demais_unidades_historicas
FROM hierarquia
GROUP BY unidade_atual_codigo, unidade_anterior_codigo;


-- 2. View VW_ZONA_RESP_CENTRAL_2
-- Otimizacao: remove SELECT * intermediario e usa predicados temporais sargable.
-- A condicao abaixo equivale ao intervalo legado baseado em TRUNC:
-- sysdate between trunc(datainicio) and trunc(datatermino + 1).

CREATE OR REPLACE VIEW VW_ZONA_RESP_CENTRAL_2
            (codigo_central, sigla_central, codigo_zona_resp, sigla_zona_resp, data_inicio_resp, data_fim_resp) AS
SELECT uni_c.cd             AS codigo_central,
       uni_c.sigla_unid_tse AS sigla_central,
       uni_z.cd             AS codigo_zona_resp,
       uni_z.sigla_unid_tse AS sigla_zona_resp,
       r.datainicio         AS data_inicio_resp,
       r.datatermino        AS data_fim_resp
FROM (
         SELECT cd, sigla_unid_tse
         FROM srh2.unidade_tse
         WHERE sigla_unid_tse LIKE 'CAE%'
           AND sit_unid NOT LIKE 'E%'
     ) uni_c
         JOIN corau.ct_central c
              ON uni_c.sigla_unid_tse = SUBSTR(c.sigla, 1, 5)
         LEFT JOIN (
             SELECT e.datainicio,
                    e.datatermino,
                    rc.central_id,
                    rc.zona_id
             FROM corau.evento e
                      JOIN corau.resp_central rc
                           ON e.id = rc.id
             WHERE e.datainicio < TRUNC(SYSDATE) + 1
               AND e.datatermino >= TRUNC(SYSDATE)
         ) r
                   ON c.id = r.central_id
         LEFT JOIN corau.ct_zona z
                   ON r.zona_id = z.id
         LEFT JOIN (
             SELECT cd, sigla_unid_tse, num_ze
             FROM srh2.unidade_tse
             WHERE num_ze IS NOT NULL
               AND sit_unid NOT LIKE 'E%'
         ) uni_z
                   ON z.numero = uni_z.num_ze;


-- 3. View VW_UNIDADE_2
-- Otimizacao: calcula lotacao, quantidade de filhas, filhas complexas e titulares uma
-- unica vez em CTEs agregadas. A regra de classificacao de tipo segue a view original.

CREATE OR REPLACE VIEW VW_UNIDADE_2
            (codigo, nome, sigla, matricula_titular, titulo_titular, data_inicio_titularidade, tipo, situacao,
             unidade_superior_codigo)
AS
WITH zona_responsavel AS (
    SELECT codigo_central,
           MIN(codigo_zona_resp) AS codigo_zona_resp
    FROM vw_zona_resp_central_2
    GROUP BY codigo_central
),
tb_unidade AS (
    SELECT u.cd,
           u.ds,
           u.sigla_unid_tse,
           u.sit_unid,
           CASE
               WHEN u.sigla_unid_tse LIKE 'CAE%' AND u.sit_unid NOT LIKE 'E%' THEN zr.codigo_zona_resp
               WHEN u.cod_unid_super IN (6, 19, 37, 634, 635, 637) THEN 1
               ELSE u.cod_unid_super
           END AS cod_unid_super
    FROM srh2.unidade_tse u
             LEFT JOIN zona_responsavel zr
                       ON zr.codigo_central = u.cd
    WHERE u.cd NOT IN (1, 6, 19, 37, 634, 635, 637)
),
lotacao_direta AS (
    SELECT cod_unid_tse,
           COUNT(1) AS qtd_servidores
    FROM srh2.lotacao
    WHERE dt_fim_lotacao IS NULL
    GROUP BY cod_unid_tse
),
filhas_ativas AS (
    SELECT cod_unid_super,
           COUNT(1) AS qtd_unidades_filhas
    FROM tb_unidade
    WHERE cod_unid_super IS NOT NULL
      AND sit_unid NOT LIKE 'E%'
    GROUP BY cod_unid_super
),
filhas_folha_com_um_servidor AS (
    SELECT uf.cod_unid_super,
           SUM(CASE WHEN ld.qtd_servidores = 1 THEN 1 ELSE 0 END) AS qtd_servidores
    FROM tb_unidade uf
             JOIN lotacao_direta ld
                  ON ld.cod_unid_tse = uf.cd
    WHERE NOT EXISTS (
        SELECT 1
        FROM tb_unidade neta
        WHERE neta.cod_unid_super = uf.cd
    )
    GROUP BY uf.cod_unid_super
),
lotacao_calculada AS (
    SELECT ld.cod_unid_tse,
           ld.qtd_servidores + NVL(ff.qtd_servidores, 0) AS qtd_servidores
    FROM lotacao_direta ld
             LEFT JOIN filhas_folha_com_um_servidor ff
                       ON ff.cod_unid_super = ld.cod_unid_tse
),
filhas_complexas AS (
    SELECT uf.cod_unid_super,
           COUNT(1) AS qtd_filhas_complexas
    FROM tb_unidade uf
             LEFT JOIN lotacao_direta ld
                       ON ld.cod_unid_tse = uf.cd
    WHERE NVL(ld.qtd_servidores, 0) > 1
       OR EXISTS (
        SELECT 1
        FROM tb_unidade neta
        WHERE neta.cod_unid_super = uf.cd
    )
    GROUP BY uf.cod_unid_super
),
titulares AS (
    SELECT l.cod_unid_tse,
           s.mat_servidor,
           s.num_tit_ele,
           c.dt_ingresso
    FROM srh2.qfc_ocup_com c
             JOIN srh2.qfc_vagas_com v
                  ON c.cod_comissionado = v.cod_comissionado
                 AND c.nome_com = v.nome_com
                 AND c.num_vaga_com = v.num_vaga_com
             JOIN srh2.lotacao l
                  ON c.mat_servidor = l.mat_servidor
                 AND l.dt_fim_lotacao IS NULL
             JOIN srh2.servidor s
                  ON c.mat_servidor = s.mat_servidor
    WHERE c.dt_dispensa IS NULL
      AND NVL(c.titular_com, 0) = 1
)
SELECT codigo,
       nome,
       sigla,
       matricula_titular,
       titulo_titular,
       data_inicio_titularidade,
       tipo,
       situacao,
       unidade_superior_codigo
FROM (
         SELECT u.cd                                                           AS codigo,
                u.ds                                                           AS nome,
                u.sigla_unid_tse                                               AS sigla,
                t.mat_servidor                                                 AS matricula_titular,
                t.num_tit_ele                                                  AS titulo_titular,
                t.dt_ingresso                                                  AS data_inicio_titularidade,
                CASE
                    WHEN u.sit_unid LIKE 'E%' THEN ''
                    WHEN NVL(fa.qtd_unidades_filhas, 0) = 0 THEN
                        CASE
                            WHEN NVL(lc.qtd_servidores, 0) < 2 THEN 'SEM_EQUIPE'
                            ELSE 'OPERACIONAL'
                        END
                    WHEN NVL(fc.qtd_filhas_complexas, 0) = 0 THEN 'OPERACIONAL'
                    WHEN NVL(lc.qtd_servidores, 0) > 1 THEN 'INTEROPERACIONAL'
                    ELSE 'INTERMEDIARIA'
                END                                                            AS tipo,
                CASE WHEN u.sit_unid LIKE 'E%' THEN 'INATIVA' ELSE 'ATIVA' END AS situacao,
                u.cod_unid_super                                               AS unidade_superior_codigo
         FROM tb_unidade u
                  LEFT JOIN lotacao_calculada lc
                            ON lc.cod_unid_tse = u.cd
                  LEFT JOIN filhas_ativas fa
                            ON fa.cod_unid_super = u.cd
                  LEFT JOIN filhas_complexas fc
                            ON fc.cod_unid_super = u.cd
                  LEFT JOIN titulares t
                            ON t.cod_unid_tse = u.cd
         UNION
         SELECT 1,
                'UNIDADE RAIZ ADMINISTRATIVA',
                'ADMIN',
                NULL,
                NULL,
                NULL,
                'RAIZ',
                'ATIVA',
                NULL
         FROM dual
     );


-- 4. View VW_USUARIO_2
-- Otimizacao: substitui subconsulta por linha contra VW_UNIDADE_2 por joins sobre CTE
-- materializavel pelo otimizador. O literal legado 'SEM EQUIPE' foi mantido para
-- preservar a semantica da view original.

CREATE OR REPLACE VIEW VW_USUARIO_2 (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo) AS
WITH unidade AS (
    SELECT codigo,
           sigla,
           tipo,
           unidade_superior_codigo
    FROM vw_unidade_2
),
unidade_apoio AS (
    SELECT MIN(CASE WHEN sigla = 'ASPRE' AND tipo = 'OPERACIONAL' THEN codigo END) AS codigo_aspre,
           MIN(CASE WHEN sigla = 'SEDOC' AND tipo = 'OPERACIONAL' THEN codigo END) AS codigo_sedoc
    FROM unidade
)
SELECT s.num_tit_ele    AS titulo,
       s.mat_servidor   AS matricula,
       s.nom            AS nome,
       s.e_mail         AS email,
       r.ramal_servidor AS ramal,
       l.cod_unid_tse   AS unidade_lot_codigo,
       CASE
           WHEN u.tipo = 'SEM EQUIPE' THEN
               CASE
                   WHEN u.unidade_superior_codigo = 1 THEN
                       CASE
                           WHEN u.sigla = 'GP' THEN ua.codigo_aspre
                           ELSE ua.codigo_sedoc
                       END
                   ELSE u.unidade_superior_codigo
               END
           ELSE u.codigo
       END              AS unidade_comp_codigo
FROM srh2.servidor s
         JOIN srh2.lotacao l
              ON s.mat_servidor = l.mat_servidor
             AND l.dt_fim_lotacao IS NULL
         LEFT JOIN unidade u
                   ON u.codigo = l.cod_unid_tse
         CROSS JOIN unidade_apoio ua
         OUTER APPLY (
             SELECT ramal_servidor
             FROM (
                      SELECT ramal_servidor
                      FROM srh2.lot_ramais_servidores
                      WHERE mat_servidor = s.mat_servidor
                        AND unid_lot = l.cod_unid_tse
                        AND ramal_principal = 1
                      ORDER BY dt_ini_lotacao DESC
                  )
             WHERE ROWNUM = 1
         ) r;


-- 5. View VW_RESPONSABILIDADE_2
-- Otimizacao: reutiliza VW_UNIDADE_2 otimizada e torna os filtros de vigencia sargable.

CREATE OR REPLACE VIEW VW_RESPONSABILIDADE_2
            (unidade_codigo, usuario_matricula, usuario_titulo, tipo, data_inicio, data_fim) AS
SELECT u.codigo                                                                AS unidade_codigo,
       COALESCE(a.usuario_matricula, s.mat_serv_com_subs, u.matricula_titular) AS usuario_matricula,
       COALESCE(a.usuario_titulo, s.num_tit_ele, u.titulo_titular)             AS usuario_titulo,
       CASE
           WHEN a.usuario_matricula IS NOT NULL THEN 'ATRIBUICAO_TEMPORARIA'
           WHEN s.mat_serv_com_subs IS NOT NULL THEN 'SUBSTITUTO'
           ELSE 'TITULAR'
       END                                                                     AS tipo,
       COALESCE(a.data_inicio, s.dt_ini_subst, u.data_inicio_titularidade)     AS data_inicio,
       COALESCE(a.data_termino, s.dt_fim_subst)                                AS data_fim
FROM (
         SELECT codigo, matricula_titular, titulo_titular, data_inicio_titularidade
         FROM vw_unidade_2
         WHERE situacao = 'ATIVA'
           AND tipo IN ('OPERACIONAL', 'INTEROPERACIONAL', 'INTERMEDIARIA')
     ) u
         LEFT JOIN (
             SELECT sub.mat_servidor,
                    sub.mat_serv_com_subs,
                    s.num_tit_ele,
                    sub.dt_ini_subst,
                    sub.dt_fim_subst
             FROM srh2.qfc_ocup_com c
                      JOIN srh2.qfc_subst_com sub
                           ON c.mat_servidor = sub.mat_servidor
                          AND c.dt_ingresso = sub.dt_ingresso
                          AND c.tp_ocup_com = sub.tp_ocup_com
                      JOIN srh2.servidor s
                           ON sub.mat_serv_com_subs = s.mat_servidor
             WHERE NVL(c.titular_com, 0) = 1
               AND sub.dt_ini_subst < TRUNC(SYSDATE) + 1
               AND sub.dt_fim_subst >= TRUNC(SYSDATE)
         ) s
                   ON u.matricula_titular = s.mat_servidor
         LEFT JOIN (
             SELECT unidade_codigo,
                    usuario_matricula,
                    usuario_titulo,
                    data_inicio,
                    data_termino
             FROM atribuicao_temporaria
             WHERE data_inicio < TRUNC(SYSDATE) + 1
               AND data_termino >= TRUNC(SYSDATE)
         ) a
                   ON u.codigo = a.unidade_codigo;


-- 6. View VW_USUARIO_PERFIL_UNIDADE_2
-- Otimizacao: depende das views acima ja otimizadas. UNION foi preservado para manter
-- a deduplicacao da view original.

CREATE OR REPLACE VIEW VW_USUARIO_PERFIL_UNIDADE_2 (usuario_titulo, perfil, unidade_codigo) AS
SELECT usuario_titulo, perfil, unidade_codigo
FROM (
         SELECT a.usuario_titulo, 'ADMIN' AS perfil, 1 AS unidade_codigo
         FROM administrador a
                  JOIN vw_usuario_2 u
                       ON u.titulo = a.usuario_titulo
         UNION
         SELECT r.usuario_titulo, 'GESTOR' AS perfil, r.unidade_codigo
         FROM vw_responsabilidade_2 r
                  JOIN vw_unidade_2 u
                       ON r.unidade_codigo = u.codigo
                      AND u.tipo IN ('INTERMEDIARIA', 'INTEROPERACIONAL')
         UNION
         SELECT r.usuario_titulo, 'CHEFE' AS perfil, r.unidade_codigo
         FROM vw_responsabilidade_2 r
                  JOIN vw_unidade_2 u
                       ON r.unidade_codigo = u.codigo
                      AND u.tipo IN ('INTEROPERACIONAL', 'OPERACIONAL')
         UNION
         SELECT usu.titulo AS usuario_titulo, 'SERVIDOR' AS perfil, uni.codigo AS unidade_codigo
         FROM vw_usuario_2 usu
                  JOIN vw_unidade_2 uni
                       ON usu.unidade_comp_codigo = uni.codigo
         WHERE usu.titulo <> uni.titulo_titular
     );
