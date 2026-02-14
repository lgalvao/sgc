# VIEW-03 - VW_UNIDADE - Unidades Organizacionais do Sistema

## Finalidade

Esta view é a **principal fonte de informações sobre unidades organizacionais** no SGC. Ela consolida dados do SGRH sobre unidades do TRE-PE, enriquecendo-os com informações sobre titularidade, classificação hierárquica (tipo), situação operacional, e estrutura organizacional. É a base para toda a árvore de unidades do sistema, incluindo uma unidade virtual raiz (ADMIN) que não existe no SGRH.

## Origem dos Dados

**Sistema de Gestão de Recursos Humanos (SRH2):**
- `SRH2.UNIDADE_TSE`: Unidades organizacionais
- `SRH2.LOTACAO`: Lotações de servidores
- `SRH2.QFC_OCUP_COM`: Ocupações de cargos comissionados
- `SRH2.QFC_VAGAS_COM`: Vagas de cargos comissionados
- `SRH2.SERVIDOR`: Dados dos servidores

**Views do Sistema:**
- `VW_ZONA_RESP_CENTRAL`: Para determinar hierarquia de CAEs

## Estrutura da View

| Coluna | Tipo | Descrição | Origem |
|--------|------|-----------|--------|
| `codigo` | NUMBER | Código único da unidade (PK) | `UNIDADE_TSE.CD` ou 1 (ADMIN) |
| `nome` | VARCHAR2(255) | Nome completo da unidade | `UNIDADE_TSE.DS` |
| `sigla` | VARCHAR2(20) | Sigla da unidade | `UNIDADE_TSE.SIGLA_UNID_TSE` |
| `matricula_titular` | VARCHAR2(8) | Matrícula do servidor titular da unidade | Processado de `QFC_OCUP_COM` |
| `titulo_titular` | VARCHAR2(12) | Título de eleitor do titular | `SERVIDOR.NUM_TIT_ELE` |
| `data_inicio_titularidade` | DATE | Data de início da titularidade atual | `QFC_OCUP_COM.DT_INGRESSO` |
| `tipo` | VARCHAR2(20) | Classificação da unidade | Calculado (ver RN-VIEW03-03) |
| `situacao` | VARCHAR2(20) | Situação operacional | Derivado de `UNIDADE_TSE.SIT_UNID` |
| `unidade_superior_codigo` | NUMBER | Código da unidade hierarquicamente superior | Processado (ver RN-VIEW03-04) |

## Regras de Negócio

### RN-VIEW03-01: Unidade Virtual Raiz (ADMIN)

O sistema cria uma unidade virtual que não existe no SGRH:

```sql
SELECT 1 AS codigo,
       'UNIDADE RAIZ ADMINISTRATIVA' AS nome,
       'ADMIN' AS sigla,
       NULL AS matricula_titular,
       NULL AS titulo_titular,
       NULL AS data_inicio_titularidade,
       'RAIZ' AS tipo,
       'ATIVA' AS situacao,
       NULL AS unidade_superior_codigo,
       0 AS qtd_servidores_lotados,
       0 AS qtd_unidades_filhas
FROM DUAL
```

**Características da unidade ADMIN:**
- Código fixo: 1
- Não possui titular
- Não possui unidade superior (é a raiz da árvore)
- Tipo especial: 'RAIZ'
- Sempre ativa
- Quantidade de servidores e unidades filhas = 0

**Justificativa:** Esta unidade serve como ponto de partida para todos os processos de mapeamento, revisão e diagnóstico. Usuários com perfil ADMIN atuam no contexto desta unidade.

### RN-VIEW03-02: Filtragem de Unidades do SGRH

A view exclui as seguintes unidades do SGRH para evitar duplicações e inconsistências:

```sql
WHERE cd NOT IN (1, 6, 19, 37, 634, 635, 637)
```

**Justificativa:**
- Código 1 é reservado para a unidade virtual ADMIN
- Códigos 6, 19, 37, 634, 635, 637 são unidades que seriam mapeadas incorretamente para a unidade ADMIN (código 1) se não fossem filtradas

### RN-VIEW03-03: Determinação do Tipo da Unidade

O tipo da unidade é calculado através de uma lógica complexa que considera múltiplos fatores:

#### Unidades Extintas
```sql
WHEN sit_unid LIKE 'E%' THEN ''
```
Unidades extintas não recebem classificação de tipo.

#### Unidades sem Unidades Filhas
Para unidades que não possuem subordinadas (`qtd_unidades_filhas = 0`):

```sql
WHEN nvl(l.qtd_servidores, 0) < 2 THEN 'SEM_EQUIPE'
ELSE 'OPERACIONAL'
```

- **SEM_EQUIPE**: Menos de 2 servidores lotados
- **OPERACIONAL**: 2 ou mais servidores lotados

**Observação importante:** A contagem de servidores inclui não apenas os servidores diretamente lotados, mas também servidores de unidades filhas que sejam únicas em suas respectivas unidades (ver RN-VIEW03-06).

#### Unidades com Unidades Filhas

Para unidades com subordinadas, a classificação depende se as filhas têm servidores ou subfilhas:

```sql
WHEN NOT EXISTS (
    SELECT 1 FROM tb_unidade uf
    WHERE uf.cod_unid_super = u.cd
      AND ((SELECT count(1) FROM srh2.lotacao 
            WHERE dt_fim_lotacao IS NULL 
              AND cod_unid_tse = uf.cd) > 1
       OR EXISTS (SELECT 1 FROM tb_unidade 
                  WHERE cod_unid_super = uf.cd))
) THEN 'OPERACIONAL'
```

Se **nenhuma** unidade filha tiver mais de 1 servidor OU tiver subunidades → **OPERACIONAL**

```sql
WHEN nvl(l.qtd_servidores, 0) > 1 THEN 'INTEROPERACIONAL'
ELSE 'INTERMEDIARIA'
```

Se alguma filha tem >1 servidor ou tem subunidades:
- **INTEROPERACIONAL**: A unidade atual tem mais de 1 servidor lotado
- **INTERMEDIARIA**: A unidade atual tem no máximo 1 servidor lotado (apenas o titular)

#### Resumo dos Tipos

| Tipo | Definição | Perfis Aplicáveis |
|------|-----------|-------------------|
| `RAIZ` | Unidade virtual ADMIN | ADMIN |
| `SEM_EQUIPE` | Unidade sem subordinadas e menos de 2 servidores | Nenhum (não participa de processos) |
| `OPERACIONAL` | Unidade-folha com 2+ servidores OU unidade com filhas sem servidores/subfilhas | CHEFE, SERVIDOR |
| `INTEROPERACIONAL` | Unidade com filhas operacionais E 2+ servidores próprios | GESTOR, CHEFE, SERVIDOR |
| `INTERMEDIARIA` | Unidade com filhas operacionais mas apenas titular | GESTOR |

### RN-VIEW03-04: Determinação da Unidade Superior

A hierarquia das unidades é ajustada através de regras especiais:

```sql
CASE 
    WHEN sigla_unid_tse LIKE 'CAE%' AND sit_unid NOT LIKE 'E%' 
        THEN (SELECT codigo_zona_resp FROM vw_zona_resp_central WHERE codigo_central = cd)
    WHEN cod_unid_super IN (6, 19, 37, 634, 635, 637) 
        THEN 1
    ELSE cod_unid_super 
END AS cod_unid_super
```

**Regra 1 - Centrais de Atendimento (CAE):**
- Unidades CAE têm como superior a zona eleitoral sob sua responsabilidade
- Busca em `VW_ZONA_RESP_CENTRAL` pelo `codigo_zona_resp`
- Se não houver zona atribuída, retorna NULL

**Regra 2 - Unidades Especiais:**
- Unidades com superior nos códigos 6, 19, 37, 634, 635, 637 são remapeadas para a unidade ADMIN (código 1)
- Torna essas unidades filhas diretas da raiz administrativa

**Regra 3 - Demais Unidades:**
- Mantêm a hierarquia original do SGRH (`cod_unid_super`)

### RN-VIEW03-05: Determinação do Titular

O titular da unidade é identificado através das tabelas de cargos comissionados:

```sql
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
  AND nvl(c.titular_com, 0) = 1
```

**Critérios para ser titular:**
1. Ter ocupação de cargo comissionado (`qfc_ocup_com`)
2. Cargo não dispensado (`dt_dispensa IS NULL`)
3. Marcado como titular (`titular_com = 1`)
4. Lotação ativa na unidade (`dt_fim_lotacao IS NULL`)
5. Vaga de comissionado ativa

**Importante:** Uma unidade pode não ter titular se:
- Não houver cargo comissionado associado
- O cargo estiver vago
- O titular estiver afastado/dispensado

### RN-VIEW03-06: Contagem de Servidores

A contagem de servidores de uma unidade é complexa e inclui dois grupos:

**Grupo 1 - Servidores Diretamente Lotados:**
```sql
SELECT l1.cod_unid_tse, COUNT(1) + nvl(...ajuste..., 0) as qtd_servidores
FROM srh2.lotacao l1
WHERE l1.dt_fim_lotacao IS NULL
GROUP BY l1.cod_unid_tse
```

**Grupo 2 - Servidores Únicos de Filhas sem Subfilhas:**
```sql
SELECT DECODE(qtd_servidores, 1, 1, 0)
FROM (
    SELECT l2.cod_unid_tse, u2.cod_unid_super, count(1) as qtd_servidores
    FROM srh2.lotacao l2
    JOIN tb_unidade u2 ON l2.cod_unid_tse = u2.cd
    WHERE l2.dt_fim_lotacao IS NULL
      AND NOT EXISTS (SELECT 1 FROM tb_unidade WHERE cod_unid_super = u2.cd)
    GROUP BY l2.cod_unid_tse, u2.cod_unid_super
)
WHERE cod_unid_super = [unidade_atual]
```

**Lógica do Grupo 2:**
- Se uma unidade filha tem exatamente 1 servidor
- E essa filha não tem subunidades próprias
- Então esse servidor é contado também para a unidade superior

**Justificativa:** Um servidor único em uma unidade-folha é considerado como parte da equipe da unidade superior para fins de classificação.

### RN-VIEW03-07: Situação da Unidade

A situação é derivada diretamente do campo `SIT_UNID` do SGRH:

```sql
CASE 
    WHEN sit_unid LIKE 'E%' THEN 'INATIVA' 
    ELSE 'ATIVA' 
END
```

- **ATIVA**: Qualquer situação exceto extintas
- **INATIVA**: Unidades com código de situação começando com 'E' (extintas)

Unidades inativas não participam de processos e não aparecem na árvore de unidades ativa.

## Casos de Uso da View

### CU-VIEW03-01: Construção da Árvore de Unidades

**Contexto:** Tela "Unidades" (perfil ADMIN) e "Minha unidade" (outros perfis).

**Implementação:**
```sql
SELECT codigo, nome, sigla, tipo, situacao, unidade_superior_codigo
FROM VW_UNIDADE
WHERE situacao = 'ATIVA'
START WITH unidade_superior_codigo IS NULL
CONNECT BY PRIOR codigo = unidade_superior_codigo
ORDER SIBLINGS BY sigla;
```

**Resultado:** Árvore hierárquica completa iniciando em ADMIN (código 1).

### CU-VIEW03-02: Validação de Perfis de Usuário

**Contexto:** Determinação de quais perfis um usuário pode ter (ver VW_USUARIO_PERFIL_UNIDADE).

**Aplicação:**
- **GESTOR**: `tipo IN ('INTERMEDIARIA', 'INTEROPERACIONAL')`
- **CHEFE**: `tipo IN ('INTEROPERACIONAL', 'OPERACIONAL')`
- **SERVIDOR**: Qualquer unidade com `tipo` diferente de 'SEM_EQUIPE' e 'RAIZ'

### CU-VIEW03-03: Seleção de Unidades para Processos

**Contexto:** Ao criar um processo, selecionar quais unidades participarão.

**Regras:**
- Processos de Mapeamento/Revisão: Incluem unidades `OPERACIONAL` e `INTEROPERACIONAL`
- Processos de Diagnóstico: Incluem unidades `OPERACIONAL` e `INTEROPERACIONAL`
- Unidades `INTERMEDIARIA` não cadastram atividades, apenas validam
- Unidades `SEM_EQUIPE` não participam
- Unidade `ADMIN` (RAIZ) participa sempre como ponto de partida e homologação

### CU-VIEW03-04: Snapshot de Unidades em Processo

**Contexto:** Ao iniciar um processo, os dados das unidades são copiados para `UNIDADE_PROCESSO`.

**Implementação:**
```sql
INSERT INTO UNIDADE_PROCESSO (
    processo_codigo, unidade_codigo, nome, sigla, 
    matricula_titular, titulo_titular, data_inicio_titularidade,
    tipo, situacao, unidade_superior_codigo
)
SELECT :processo_codigo, codigo, nome, sigla,
       matricula_titular, titulo_titular, data_inicio_titularidade,
       tipo, situacao, unidade_superior_codigo
FROM VW_UNIDADE
WHERE codigo IN (:lista_unidades_selecionadas);
```

**Justificativa:** Preserva o estado das unidades no momento do início do processo, mesmo que posteriormente haja mudanças organizacionais.

### CU-VIEW03-05: Breadcrumb de Navegação

**Contexto:** Exibir o caminho hierárquico de uma unidade (ex: "ADMIN > Presidência > SESEL > COSIS").

**Implementação:**
```sql
SELECT LISTAGG(sigla, ' > ') WITHIN GROUP (ORDER BY LEVEL DESC)
FROM VW_UNIDADE
START WITH codigo = :codigo_unidade_atual
CONNECT BY codigo = PRIOR unidade_superior_codigo;
```

### CU-VIEW03-06: Validação de Hierarquia em Movimentações

**Contexto:** Validar se uma movimentação de subprocesso está seguindo a hierarquia correta.

**Aplicação:**
- Movimentação para superior: `unidade_destino_codigo = unidade_superior_codigo`
- Movimentação para subordinada: Verificar se `unidade_destino` tem `unidade_superior_codigo = unidade_origem`

## Relação com Outras Views e Tabelas

### Dependências de Outras Views

**VW_ZONA_RESP_CENTRAL:**
- Essencial para determinar `unidade_superior_codigo` de CAEs
- Consultada durante a construção de `tb_unidade` (CTE interna)

### Views que Dependem de VW_UNIDADE

**VW_USUARIO:**
- Usa `VW_UNIDADE` para determinar `unidade_comp_codigo` (unidade de competência)
- Aplica regras especiais para unidades `SEM_EQUIPE`

**VW_RESPONSABILIDADE:**
- Filtra unidades por tipo: `tipo IN ('OPERACIONAL', 'INTEROPERACIONAL', 'INTERMEDIARIA')`
- Usa `matricula_titular` e `titulo_titular`

**VW_USUARIO_PERFIL_UNIDADE:**
- Usa `tipo` para determinar perfis GESTOR e CHEFE
- Filtra por `situacao = 'ATIVA'`

### Tabelas que Usam VW_UNIDADE

**UNIDADE_PROCESSO:**
- Snapshot dos dados de `VW_UNIDADE` no momento de criação do processo

**ATRIBUICAO_TEMPORARIA:**
- Valida `unidade_codigo` contra códigos existentes em `VW_UNIDADE`

**ALERTA, NOTIFICACAO, MOVIMENTACAO, ANALISE:**
- Referenciam códigos de unidades que devem existir em `VW_UNIDADE`

## Dependências

### Permissões Necessárias

```sql
GRANT SELECT ON SRH2.UNIDADE_TSE TO SGC;
GRANT SELECT ON SRH2.LOTACAO TO SGC;
GRANT SELECT ON SRH2.QFC_OCUP_COM TO SGC;
GRANT SELECT ON SRH2.QFC_VAGAS_COM TO SGC;
GRANT SELECT ON SRH2.SERVIDOR TO SGC;
```

### Tabelas de Origem

- `SRH2.UNIDADE_TSE`: Dados base das unidades
- `SRH2.LOTACAO`: Lotações de servidores
- `SRH2.QFC_OCUP_COM`: Ocupações de cargos
- `SRH2.QFC_VAGAS_COM`: Vagas de cargos
- `SRH2.SERVIDOR`: Dados dos servidores

### Views Necessárias

- `VW_ZONA_RESP_CENTRAL`: Para hierarquia de CAEs

## Considerações de Performance

### Otimização de CTEs

A view utiliza uma CTE `tb_unidade` para pré-processar a hierarquia:

```sql
WITH tb_unidade AS (
    SELECT cd, ds, sigla_unid_tse, sit_unid, 
           CASE ... END AS cod_unid_super
    FROM srh2.unidade_tse 
    WHERE cd NOT IN (1, 6, 19, 37, 634, 635, 637)
)
```

**Benefício:** A CTE é executada uma vez e reutilizada em múltiplas subconsultas, evitando leituras repetidas da tabela `UNIDADE_TSE`.

### Complexidade das Subconsultas

A view contém várias subconsultas correlacionadas que podem impactar performance:

1. Contagem de servidores lotados (com subconsulta aninhada)
2. Contagem de unidades filhas
3. Identificação do titular (com múltiplas junções)

**Recomendações:**
- Materializar a view se consultas forem muito frequentes
- Criar índices em `LOTACAO.cod_unid_tse`, `LOTACAO.dt_fim_lotacao`
- Índice composto em `QFC_OCUP_COM(mat_servidor, dt_dispensa, titular_com)`

### Impacto de VW_ZONA_RESP_CENTRAL

Cada CAE executa uma subconsulta em `VW_ZONA_RESP_CENTRAL`. Para otimizar:

- Garantir que `VW_ZONA_RESP_CENTRAL` tenha boa performance
- Considerar materialização de ambas as views se houver muitas CAEs

## Exemplo de Registros

```
+--------+-------------------------+--------+--------------------+----------------+---------------------------+-----------------+----------+-----------+
| codigo | nome                    | sigla  | matricula_titular  | titulo_titular | data_inicio_titularidade  | tipo            | situacao | unid_sup  |
+--------+-------------------------+--------+--------------------+----------------+---------------------------+-----------------+----------+-----------+
| 1      | UNIDADE RAIZ ADM        | ADMIN  | NULL               | NULL           | NULL                      | RAIZ            | ATIVA    | NULL      |
| 10     | Presidência             | PRES   | 00012345           | 001234567890   | 01/01/2023                | INTERMEDIARIA   | ATIVA    | 1         |
| 100    | Secret Adm e Orc        | SAO    | 00023456           | 002345678901   | 15/06/2022                | INTEROPERACIONAL| ATIVA    | 10        |
| 150    | Coord de Orçamento      | COOR   | 00034567           | 003456789012   | 20/03/2023                | INTERMEDIARIA   | ATIVA    | 100       |
| 200    | Seção de Controle Orc   | SECO   | 00045678           | 004567890123   | 10/08/2023                | OPERACIONAL     | ATIVA    | 150       |
| 201    | Seção de Planejamento   | SEPL   | NULL               | NULL           | NULL                      | SEM_EQUIPE      | ATIVA    | 150       |
| 1001   | CAE Recife              | CAE01  | 00056789           | 005678901234   | 05/02/2024                | OPERACIONAL     | ATIVA    | 2001      |
+--------+-------------------------+--------+--------------------+----------------+---------------------------+-----------------+----------+-----------+
```

**Interpretação:**
- **Código 1 (ADMIN)**: Unidade raiz virtual, sem titular nem superior
- **Código 10 (PRES)**: Intermediária, subordinada à ADMIN
- **Código 100 (SAO)**: Interoperacional (tem filhas e servidores próprios), subordinada à Presidência
- **Código 150 (COOR)**: Intermediária, subordinada à SAO
- **Código 200 (SECO)**: Operacional (folha com servidores), subordinada à COOR
- **Código 201 (SEPL)**: Sem equipe (< 2 servidores), não participa de processos
- **Código 1001 (CAE01)**: Operacional, mas subordinada à zona 2001 (não segue hierarquia administrativa)

## Notas de Implementação

### Manutenção e Sincronização

A view reflete sempre o estado atual do SGRH. Mudanças no SGRH se refletem imediatamente:

**Mudanças que afetam a view:**
- Criação/extinção de unidades
- Mudança de lotações (afeta contagem de servidores e tipo)
- Nomeação/dispensa de titulares (afeta titular e data de titularidade)
- Reestruturação hierárquica (afeta `unidade_superior_codigo`)

**Processos em andamento:**
- Processos ativos usam o snapshot em `UNIDADE_PROCESSO`
- Mudanças no SGRH não afetam processos já iniciados
- Novos processos verão as mudanças

### Consistência de Dados

**Possíveis inconsistências:**
1. Unidade sem titular definido (campos NULL)
2. CAE sem zona atribuída (superior NULL)
3. Contagens de servidores divergentes (devido a atualizações assíncronas)

**Tratamento:**
- O sistema deve tolerar unidades sem titular
- Validações de negócio devem verificar tipos antes de atribuir ações
- Logs de auditoria devem registrar estados inconsistentes

### Versioning e Auditoria

Para auditoria de mudanças na estrutura organizacional:

1. **Snapshot em processos**: `UNIDADE_PROCESSO` preserva estado histórico
2. **Logs de mudanças**: Considerar trigger na `UNIDADE_TSE` do SGRH
3. **Histórico de vinculações**: `VW_VINCULACAO_UNIDADE` complementa com histórico de sucessões
