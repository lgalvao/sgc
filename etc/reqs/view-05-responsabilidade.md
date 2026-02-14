# VIEW-05 - VW_RESPONSABILIDADE - Responsabilidades de Unidades

## Finalidade

Esta view consolida todas as formas de responsabilidade sobre unidades organizacionais, identificando quem são os responsáveis efetivos por cada unidade em um determinado momento. Integra três fontes de responsabilidade: titularidade formal (do SGRH), substituição temporária (do SGRH) e atribuição temporária de responsabilidade (do próprio SGC), estabelecendo uma hierarquia de precedência entre elas.

## Origem dos Dados

**Sistema de Gestão de Recursos Humanos (SRH2):**
- `SRH2.QFC_OCUP_COM`: Ocupações de cargos comissionados (titulares)
- `SRH2.QFC_SUBST_COM`: Substituições de cargos comissionados
- `SRH2.SERVIDOR`: Dados dos servidores substitutos

**Tabelas do SGC:**
- `ATRIBUICAO_TEMPORARIA`: Atribuições temporárias cadastradas no sistema

**Views do Sistema:**
- `VW_UNIDADE`: Unidades e seus titulares formais

## Estrutura da View

| Coluna | Tipo | Descrição | Origem |
|--------|------|-----------|--------|
| `unidade_codigo` | NUMBER | Código da unidade organizacional | `VW_UNIDADE.codigo` |
| `usuario_matricula` | VARCHAR2(8) | Matrícula do responsável atual | Consolidado (ver RN-VIEW05-02) |
| `usuario_titulo` | VARCHAR2(12) | Título de eleitor do responsável atual | Consolidado (ver RN-VIEW05-02) |
| `tipo` | VARCHAR2(30) | Tipo de responsabilidade | Calculado (ver RN-VIEW05-03) |
| `data_inicio` | DATE | Data de início da responsabilidade | Consolidado (ver RN-VIEW05-02) |
| `data_fim` | DATE | Data de término da responsabilidade (NULL se indefinida) | Consolidado (ver RN-VIEW05-02) |

## Regras de Negócio

### RN-VIEW05-01: Unidades Elegíveis

A view considera apenas unidades que podem ter responsáveis:

```sql
FROM (
    SELECT codigo, matricula_titular, titulo_titular, data_inicio_titularidade
    FROM vw_unidade
    WHERE situacao = 'ATIVA'
      AND tipo IN ('OPERACIONAL', 'INTEROPERACIONAL', 'INTERMEDIARIA')
) u
```

**Critérios de elegibilidade:**
- Situação: `ATIVA` (unidades inativas não têm responsáveis)
- Tipo: `OPERACIONAL`, `INTEROPERACIONAL` ou `INTERMEDIARIA`

**Exclusões:**
- Unidade `RAIZ` (ADMIN): Não tem responsável individual
- Unidades `SEM_EQUIPE`: Não participam de processos, logo não precisam de responsável
- Unidades `INATIVA`: Extintas ou desativadas

### RN-VIEW05-02: Hierarquia de Precedência das Responsabilidades

O responsável efetivo de uma unidade é determinado por ordem de precedência usando `COALESCE`:

```sql
usuario_matricula = COALESCE(
    a.usuario_matricula,      -- 1ª prioridade: Atribuição Temporária
    s.mat_serv_com_subs,      -- 2ª prioridade: Substituto
    u.matricula_titular        -- 3ª prioridade: Titular
)

usuario_titulo = COALESCE(
    a.usuario_titulo,          -- 1ª prioridade: Atribuição Temporária
    s.num_tit_ele,             -- 2ª prioridade: Substituto
    u.titulo_titular           -- 3ª prioridade: Titular
)
```

**Ordem de precedência:**

1. **ATRIBUIÇÃO TEMPORÁRIA** (maior prioridade)
   - Cadastrada no SGC através da tabela `ATRIBUICAO_TEMPORARIA`
   - Vigente no momento atual
   - Sobrepõe qualquer outra forma de responsabilidade

2. **SUBSTITUIÇÃO** (prioridade intermediária)
   - Cadastrada no SGRH através de `QFC_SUBST_COM`
   - Vigente no momento atual
   - Sobrepõe apenas a titularidade formal

3. **TITULARIDADE** (menor prioridade)
   - Titular formal da unidade conforme `VW_UNIDADE`
   - Sempre presente para unidades elegíveis
   - É a responsabilidade padrão quando não há substituições ou atribuições

**Justificativa da hierarquia:**
- Atribuições temporárias permitem ao ADMIN designar responsáveis em casos excepcionais (afastamentos, vacâncias)
- Substituições formais do SGRH têm precedência sobre titularidade por serem decisões oficiais
- Titularidade é a forma normal e permanente de responsabilidade

### RN-VIEW05-03: Tipos de Responsabilidade

O tipo de responsabilidade indica a origem da designação:

```sql
tipo = CASE
    WHEN a.usuario_matricula IS NOT NULL THEN 'ATRIBUICAO_TEMPORARIA'
    WHEN s.mat_serv_com_subs IS NOT NULL THEN 'SUBSTITUTO'
    ELSE 'TITULAR' 
END
```

**Valores possíveis:**

| Tipo | Descrição | Origem |
|------|-----------|--------|
| `TITULAR` | Responsável por titularidade formal do cargo | `VW_UNIDADE` |
| `SUBSTITUTO` | Responsável por substituição formal no SGRH | `QFC_SUBST_COM` |
| `ATRIBUICAO_TEMPORARIA` | Responsável por atribuição no SGC | `ATRIBUICAO_TEMPORARIA` |

### RN-VIEW05-04: Vigência das Responsabilidades

#### Atribuição Temporária

```sql
LEFT JOIN (
    SELECT unidade_codigo, usuario_matricula, usuario_titulo, data_inicio, data_termino
    FROM ATRIBUICAO_TEMPORARIA
    WHERE SYSDATE BETWEEN TRUNC(data_inicio) AND TRUNC(data_termino + 1)
) a ON u.codigo = a.unidade_codigo
```

**Regras:**
- Vigente se data atual está entre `data_inicio` e `data_termino`
- `data_termino + 1` inclui todo o último dia do período
- `TRUNC` remove horário, considerando apenas datas
- Apenas uma atribuição vigente por unidade (constraint ou validação deve garantir)

#### Substituição

```sql
LEFT JOIN (
    SELECT sub.mat_servidor, sub.mat_serv_com_subs, s.num_tit_ele, 
           sub.dt_ini_subst, sub.dt_fim_subst
    FROM srh2.qfc_ocup_com c
    JOIN srh2.qfc_subst_com sub
        ON c.mat_servidor = sub.mat_servidor 
       AND c.dt_ingresso = sub.dt_ingresso 
       AND c.tp_ocup_com = sub.tp_ocup_com
    JOIN srh2.servidor s ON sub.mat_serv_com_subs = s.mat_servidor
    WHERE nvl(c.titular_com, 0) = 1
      AND SYSDATE BETWEEN TRUNC(sub.dt_ini_subst) AND TRUNC(sub.dt_fim_subst + 1)
) s ON u.matricula_titular = s.mat_servidor
```

**Critérios:**
- Substitui apenas titulares (`c.titular_com = 1`)
- Vigente se data atual está no período de substituição
- Vinculada ao cargo do titular, não à unidade diretamente
- Mesma regra de `+ 1` no fim do período

#### Titularidade

```sql
data_inicio = COALESCE(
    a.data_inicio,                   -- Data início da atribuição
    s.dt_ini_subst,                  -- Data início da substituição
    u.data_inicio_titularidade        -- Data ingresso na titularidade
)

data_fim = COALESCE(
    a.data_termino,                  -- Data fim da atribuição
    s.dt_fim_subst                   -- Data fim da substituição
    -- NULL para titularidade (indefinida)
)
```

**Características:**
- Titularidade não tem data de término definida
- `data_fim` é NULL para responsabilidades de titulares sem substituição ou atribuição
- `data_inicio` para titularidade vem de `data_inicio_titularidade` em `VW_UNIDADE`

### RN-VIEW05-05: Unidades sem Responsável

Unidades elegíveis sem titular definido em `VW_UNIDADE`:

**Comportamento:**
- Aparecem na view com todos os campos de usuário NULL
- `tipo` será NULL
- `data_inicio` e `data_fim` serão NULL
- Isso indica uma situação irregular que deve ser corrigida

**Causas possíveis:**
- Cargo vago aguardando nomeação
- Dados inconsistentes no SGRH
- Unidade recém-criada ainda sem titular designado

## Casos de Uso da View

### CU-VIEW05-01: Determinação de Perfis GESTOR e CHEFE

**Contexto:** Após login, sistema determina se usuário tem perfil GESTOR ou CHEFE através de `VW_USUARIO_PERFIL_UNIDADE`.

**Implementação em VW_USUARIO_PERFIL_UNIDADE:**

```sql
-- Perfil GESTOR
SELECT r.usuario_titulo, 'GESTOR' as perfil, r.unidade_codigo
FROM vw_responsabilidade r
JOIN vw_unidade u ON r.unidade_codigo = u.codigo 
WHERE u.tipo IN ('INTERMEDIARIA', 'INTEROPERACIONAL')

-- Perfil CHEFE
SELECT r.usuario_titulo, 'CHEFE' as perfil, r.unidade_codigo
FROM vw_responsabilidade r
JOIN vw_unidade u ON r.unidade_codigo = u.codigo 
WHERE u.tipo IN ('INTEROPERACIONAL', 'OPERACIONAL')
```

**Dinâmica:**
- Titular, substituto ou atribuído temporariamente → pode ter perfil GESTOR/CHEFE
- Mudança de responsável → perfis são automaticamente reatribuídos
- Fim de substituição/atribuição → responsabilidade volta ao titular

### CU-VIEW05-02: Notificação de Responsáveis

**Contexto:** Sistema precisa notificar o responsável atual de uma unidade.

**Implementação:**
```sql
SELECT u.nome, u.email, r.tipo
FROM VW_RESPONSABILIDADE r
JOIN VW_USUARIO u ON r.usuario_titulo = u.titulo
WHERE r.unidade_codigo = :codigo_unidade
  AND r.data_fim IS NULL;  -- Responsabilidade atual (sem data fim ou ainda vigente)
```

**Uso em alertas e notificações:**
- Enviar e-mail ao responsável quando subprocesso chega na unidade
- Notificar sobre prazos próximos ao vencimento
- Alertar sobre pendências de validação

### CU-VIEW05-03: Validação de Ações em Subprocessos

**Contexto:** Validar se usuário pode realizar ações (cadastro, validação, etc.) em um subprocesso.

**Implementação:**
```sql
-- Verificar se usuário é responsável pela unidade do subprocesso
SELECT COUNT(*) > 0 AS pode_agir
FROM SUBPROCESSO s
JOIN VW_RESPONSABILIDADE r 
    ON s.unidade_codigo = r.unidade_codigo
WHERE s.codigo = :codigo_subprocesso
  AND r.usuario_titulo = :titulo_usuario_logado
  AND (r.data_fim IS NULL OR r.data_fim >= SYSDATE);
```

**Regras:**
- Apenas responsável atual pode agir em nome da unidade
- Responsabilidade expirada não autoriza ação
- Tipo de responsabilidade (titular/substituto/atribuição) é irrelevante para autorização

### CU-VIEW05-04: Auditoria de Mudanças de Responsabilidade

**Contexto:** Rastrear quando e como mudaram os responsáveis por uma unidade.

**Limitação:** A view mostra apenas o estado atual. Para auditoria completa:

**Dados históricos disponíveis:**
- `ATRIBUICAO_TEMPORARIA`: Histórico completo de atribuições (inclusive expiradas)
- `QFC_SUBST_COM` (SGRH): Histórico de substituições
- `VW_VINCULACAO_UNIDADE`: Histórico de mudanças organizacionais

**Consulta de histórico de atribuições:**
```sql
SELECT a.unidade_codigo, a.usuario_titulo, u.nome,
       a.data_inicio, a.data_termino, a.justificativa
FROM ATRIBUICAO_TEMPORARIA a
JOIN VW_USUARIO u ON a.usuario_titulo = u.titulo
WHERE a.unidade_codigo = :codigo_unidade
ORDER BY a.data_inicio DESC;
```

### CU-VIEW05-05: Relatório de Responsabilidades

**Contexto:** Gerar relatório consolidado de responsabilidades do tribunal.

**Exemplo de consulta:**
```sql
SELECT u.sigla, u.nome, u.tipo,
       us.nome AS responsavel,
       r.tipo AS tipo_responsabilidade,
       r.data_inicio,
       r.data_fim
FROM VW_UNIDADE u
LEFT JOIN VW_RESPONSABILIDADE r ON u.codigo = r.unidade_codigo
LEFT JOIN VW_USUARIO us ON r.usuario_titulo = us.titulo
WHERE u.situacao = 'ATIVA'
  AND u.tipo IN ('OPERACIONAL', 'INTEROPERACIONAL', 'INTERMEDIARIA')
ORDER BY u.sigla;
```

**Informações extraídas:**
- Unidades com responsável definido
- Unidades sem responsável (situação irregular)
- Tipos de responsabilidade predominantes
- Responsabilidades temporárias vigentes

### CU-VIEW05-06: Substituições e Férias

**Contexto:** Gerenciar responsabilidades durante períodos de ausência do titular.

**Fluxos possíveis:**

**Substituição formal (SGRH):**
1. Titular entra em férias/afastamento
2. SGRH registra substituição em `QFC_SUBST_COM`
3. Substituto aparece automaticamente em `VW_RESPONSABILIDADE`
4. Substituto ganha perfis GESTOR/CHEFE automaticamente
5. Ao final, titular reassume automaticamente

**Atribuição temporária (SGC):**
1. Situação excepcional não coberta por substituição formal
2. ADMIN cadastra em `ATRIBUICAO_TEMPORARIA`
3. Atribuído aparece em `VW_RESPONSABILIDADE` (prioridade maior que substituto)
4. Ao final do período, retorna à situação anterior (substituto ou titular)

## Relação com Outras Views e Tabelas

### Views que Dependem de VW_RESPONSABILIDADE

**VW_USUARIO_PERFIL_UNIDADE:**
- Principal consumidor da view
- Determina perfis GESTOR e CHEFE com base nas responsabilidades
- Filtrada por tipo de unidade

### Tabelas e Views Consultadas

**VW_UNIDADE:**
- Fonte dos dados de titularidade
- Filtro de unidades elegíveis
- Informações sobre tipo da unidade

**ATRIBUICAO_TEMPORARIA:**
- Fonte de responsabilidades atribuídas no SGC
- Atualizada por perfil ADMIN
- Filtrada por vigência

**SGRH (QFC_OCUP_COM, QFC_SUBST_COM):**
- Fonte de titularidades e substituições formais
- Somente leitura
- Filtradas por vigência

**VW_USUARIO:**
- Indiretamente (via VW_USUARIO_PERFIL_UNIDADE)
- Para obter dados completos do responsável

## Dependências

### Permissões Necessárias

```sql
GRANT SELECT ON SRH2.QFC_OCUP_COM TO SGC;
GRANT SELECT ON SRH2.QFC_SUBST_COM TO SGC;
GRANT SELECT ON SRH2.SERVIDOR TO SGC;
```

### Tabelas e Views de Origem

**Do SGRH:**
- `SRH2.QFC_OCUP_COM`: Ocupações de cargos
- `SRH2.QFC_SUBST_COM`: Substituições
- `SRH2.SERVIDOR`: Dados dos servidores

**Do SGC:**
- `ATRIBUICAO_TEMPORARIA`: Atribuições temporárias
- `VW_UNIDADE`: Unidades e titulares

## Considerações de Performance

### Otimização de Joins

A view utiliza três LEFT JOINs com subconsultas:

```sql
FROM vw_unidade u
LEFT JOIN (... atribuição ...) a
LEFT JOIN (... substituição ...) s  
```

**Subconsultas pré-filtradas:**
- Atribuições: filtro de vigência reduz registros antes do JOIN
- Substituições: filtros de vigência e `titular_com = 1` reduzem registros

**Benefício:** Menos registros para processar nos JOINs principais

### Índices Recomendados

```sql
-- Na tabela ATRIBUICAO_TEMPORARIA
CREATE INDEX idx_atrib_temp_vigencia 
ON ATRIBUICAO_TEMPORARIA(unidade_codigo, data_inicio, data_termino);

-- No SGRH (se possível)
CREATE INDEX idx_subst_com_vigencia
ON QFC_SUBST_COM(mat_servidor, dt_ini_subst, dt_fim_subst)
WHERE dt_fim_subst IS NOT NULL;
```

### Consultas Frequentes

A view é consultada principalmente através de `VW_USUARIO_PERFIL_UNIDADE`:

- **Login:** Determinação de perfis (1 vez por login)
- **Validações:** Verificação de responsabilidade (múltiplas vezes por ação)
- **Notificações:** Identificação de destinatários (múltiplas vezes por dia)

**Recomendação:** Considerar materialização de `VW_USUARIO_PERFIL_UNIDADE` (que depende desta view) se necessário.

## Exemplo de Registros

```
+--------------+-------------------+-----------------+------------------------+-------------+-------------+
| unid_codigo  | usuario_matricula | usuario_titulo  | tipo                   | data_inicio | data_fim    |
+--------------+-------------------+-----------------+------------------------+-------------+-------------+
| 100          | 00012345          | 001234567890    | TITULAR                | 01/01/2023  | NULL        |
| 150          | 00023456          | 002345678901    | SUBSTITUTO             | 01/12/2024  | 31/12/2024  |
| 200          | 00034567          | 003456789012    | ATRIBUICAO_TEMPORARIA  | 15/11/2024  | 15/02/2025  |
| 250          | 00045678          | 004567890123    | TITULAR                | 20/03/2023  | NULL        |
| 300          | NULL              | NULL            | NULL                   | NULL        | NULL        |
+--------------+-------------------+-----------------+------------------------+-------------+-------------+
```

**Interpretação:**

1. **Unidade 100:**
   - Responsável: Titular (matrícula 00012345)
   - Início da titularidade: 01/01/2023
   - Sem prazo de término (titularidade indefinida)
   - Sem substituições ou atribuições vigentes

2. **Unidade 150:**
   - Responsável: Substituto (matrícula 00023456)
   - Período de substituição: Dezembro/2024
   - Há um titular (não mostrado), mas está sendo substituído
   - Após 31/12/2024, titular reassume automaticamente

3. **Unidade 200:**
   - Responsável: Atribuição temporária (matrícula 00034567)
   - Período: 15/11/2024 a 15/02/2025
   - Pode haver titular E substituto, mas atribuição tem precedência
   - Após 15/02/2025, responsabilidade volta ao substituto (se houver) ou titular

4. **Unidade 250:**
   - Responsável: Titular (matrícula 00045678)
   - Início da titularidade: 20/03/2023
   - Situação normal sem substituições ou atribuições

5. **Unidade 300:**
   - SEM RESPONSÁVEL DEFINIDO
   - Situação irregular que precisa correção
   - Pode ser cargo vago ou dados inconsistentes no SGRH

## Notas de Implementação

### Sincronização entre Sistemas

A view integra dados de dois sistemas:

**SGRH (externo):**
- Titularidades: Sempre sincronizadas via `VW_UNIDADE`
- Substituições: Atualizadas em tempo real
- Fonte autoritativa para dados formais

**SGC (interno):**
- Atribuições temporárias: Controladas pelo próprio sistema
- Cadastradas por perfil ADMIN
- Sobrepõem dados do SGRH quando necessário

### Validações de Integridade

**Garantias fornecidas:**
- Unidades elegíveis sempre têm entrada na view (mesmo que com responsável NULL)
- Apenas uma responsabilidade vigente por unidade por vez
- Precedência clara entre tipos de responsabilidade

**Validações necessárias:**
- Sistema deve validar que não existam múltiplas atribuições temporárias vigentes para mesma unidade
- Sistema deve validar que datas de início < datas de término
- Sistema deve validar que atribuições temporárias não se sobreponham

### Mudanças Dinâmicas

**Transições automáticas:**

Às 00:00 de cada dia, responsabilidades podem mudar automaticamente:

1. Atribuição temporária expira → Responsabilidade passa a substituto (se houver) ou titular
2. Substituição expira → Responsabilidade volta ao titular
3. Nova atribuição/substituição inicia → Assume a responsabilidade

**Impactos no sistema:**
- Perfis de usuários mudam automaticamente
- Notificações devem ir para novo responsável
- Histórico em `ANALISE`, `MOVIMENTACAO` preserva usuário que realizou ação

### Casos Especiais

**Múltiplas responsabilidades:**
- Usuário pode ser responsável por várias unidades simultaneamente
- Cada unidade tem apenas um responsável
- Após login, usuário seleciona qual unidade/perfil quer usar

**Responsabilidade circular (improvável mas possível):**
- A (substituto de B) e B (substituto de A)
- Sistema deve prevenir ou alertar sobre esta situação
- Validação deve ser feita em `ATRIBUICAO_TEMPORARIA`

**Unidade sem tipo elegível:**
- Se unidade mudar de tipo (ex: tornar-se SEM_EQUIPE)
- Desaparece da view
- Responsável perde perfil GESTOR/CHEFE para aquela unidade
