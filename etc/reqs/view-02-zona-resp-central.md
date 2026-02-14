# VIEW-02 - VW_ZONA_RESP_CENTRAL - Responsabilidade de Centrais por Zonas Eleitorais

## Finalidade

Esta view estabelece o mapeamento entre as Centrais de Atendimento ao Eleitor (CAEs) do TRE-PE e as Zonas Eleitorais sob sua responsabilidade, incluindo a vigência temporal dessa responsabilidade. É fundamental para identificar quais zonas estão sob jurisdição de cada central em um determinado momento, permitindo a correta atribuição de processos e responsabilidades no sistema.

## Origem dos Dados

A view integra dados de duas fontes principais:

**Sistema de Gestão de Recursos Humanos (SRH2):**
- `SRH2.UNIDADE_TSE`: Unidades organizacionais (CAEs e Zonas)

**Sistema CORAU (SIGMA):**
- `CORAU.CT_CENTRAL`: Cadastro de centrais de atendimento
- `CORAU.CT_ZONA`: Cadastro de zonas eleitorais
- `CORAU.EVENTO`: Eventos de mudança de responsabilidade
- `CORAU.RESP_CENTRAL`: Relacionamento entre eventos, centrais e zonas

## Estrutura da View

| Coluna | Tipo | Descrição | Origem |
|--------|------|-----------|--------|
| `codigo_central` | NUMBER | Código da Central de Atendimento ao Eleitor (CAE) | `UNIDADE_TSE.CD` |
| `sigla_central` | VARCHAR2 | Sigla da central (formato: CAE + identificador, ex: CAE01) | `UNIDADE_TSE.SIGLA_UNID_TSE` |
| `codigo_zona_resp` | NUMBER | Código da Zona Eleitoral sob responsabilidade da central | `UNIDADE_TSE.CD` (zona) |
| `sigla_zona_resp` | VARCHAR2 | Sigla da zona eleitoral | `UNIDADE_TSE.SIGLA_UNID_TSE` (zona) |
| `data_inicio_resp` | DATE | Data de início da responsabilidade da central sobre a zona | `EVENTO.DATAINICIO` |
| `data_fim_resp` | DATE | Data de término da responsabilidade da central sobre a zona | `EVENTO.DATATERMINO` |

## Regras de Negócio

### RN-VIEW02-01: Identificação de Centrais de Atendimento

Uma unidade é considerada Central de Atendimento ao Eleitor (CAE) quando atende aos seguintes critérios:

- Sigla no formato `CAE%` (começa com 'CAE')
- Situação (`SIT_UNID`) diferente de 'E%' (não está extinta)

**Justificativa:** As CAEs são unidades especializadas no atendimento ao eleitor e têm jurisdição sobre um conjunto de zonas eleitorais.

### RN-VIEW02-02: Mapeamento entre SGRH e CORAU

O relacionamento entre os sistemas é estabelecido através da correspondência de siglas:

```sql
uni_c.sigla_unid_tse = substr(c.sigla, 1, 5)
```

**Detalhamento:**
- A sigla da central no CORAU pode conter caracteres adicionais além dos 5 primeiros
- Os primeiros 5 caracteres da sigla no CORAU devem corresponder exatamente à `SIGLA_UNID_TSE` no SGRH
- Exemplo: 'CAE01' (SGRH) corresponde a 'CAE01-PE' (CORAU)

### RN-VIEW02-03: Vigência da Responsabilidade

A responsabilidade de uma central sobre uma zona é considerada vigente quando:

```sql
SYSDATE BETWEEN TRUNC(e.datainicio) AND TRUNC(e.datatermino + 1)
```

**Detalhes importantes:**
- `TRUNC` remove a parte de hora das datas, considerando apenas o dia
- A data de término tem `+ 1` adicionado, incluindo todo o último dia no período
- Eventos fora da vigência atual não aparecem na view
- Se não houver evento vigente para uma central, as colunas de zona e datas ficarão NULL

**Exemplo:**
- Evento com `datainicio = 01/01/2024` e `datatermino = 31/12/2024`
- Vigente entre 01/01/2024 00:00:00 e 31/12/2024 23:59:59
- Consulta em 15/06/2024: retorna o evento
- Consulta em 01/01/2025: não retorna o evento

### RN-VIEW02-04: Identificação de Zonas Eleitorais

Uma unidade é considerada Zona Eleitoral quando:

- Possui `NUM_ZE` não nulo (número da zona eleitoral)
- Situação (`SIT_UNID`) diferente de 'E%' (não está extinta)

O mapeamento entre CORAU e SGRH para zonas é feito por:

```sql
z.numero = uni_z.num_ze
```

Onde `z.numero` é o número da zona no CORAU e `uni_z.num_ze` é o número da zona no SGRH.

### RN-VIEW02-05: Centrais sem Zona Atribuída

Centrais que não possuem zona atribuída no momento da consulta terão:

- `codigo_zona_resp`: NULL
- `sigla_zona_resp`: NULL
- `data_inicio_resp`: NULL
- `data_fim_resp`: NULL

Isso pode ocorrer quando:
1. A central foi recém-criada e ainda não teve zonas atribuídas
2. Todas as responsabilidades anteriores expiraram
3. A central está em processo de reestruturação

## Casos de Uso da View

### CU-VIEW02-01: Determinação da Unidade Superior para CAEs

**Contexto:** Na view `VW_UNIDADE`, as Centrais de Atendimento ao Eleitor (CAEs) têm sua unidade superior definida dinamicamente como a zona eleitoral sob sua responsabilidade.

**Implementação em VW_UNIDADE:**
```sql
CASE 
    WHEN sigla_unid_tse LIKE 'CAE%' AND sit_unid NOT LIKE 'E%' 
    THEN (SELECT codigo_zona_resp 
          FROM vw_zona_resp_central 
          WHERE codigo_central = cd)
    -- outros casos...
END AS cod_unid_super
```

**Justificativa:** As CAEs não seguem a hierarquia administrativa tradicional, sendo subordinadas operacionalmente às zonas que atendem.

### CU-VIEW02-02: Validação de Atribuição de Processos

**Contexto:** Ao criar ou atribuir processos de mapeamento/revisão/diagnóstico envolvendo CAEs, é necessário validar se a zona sob responsabilidade da central está incluída no processo.

**Aplicação:** 
- Se uma CAE é incluída em um processo, a zona sob sua responsabilidade deve estar incluída
- Se uma zona é removida de um processo, todas as CAEs sob sua responsabilidade devem ser removidas ou reatribuídas

### CU-VIEW02-03: Relatórios de Distribuição Territorial

**Contexto:** Geração de relatórios que mostram a distribuição das responsabilidades territoriais das centrais de atendimento.

**Exemplo de consulta:**
```sql
SELECT sigla_central,
       COUNT(codigo_zona_resp) AS qtd_zonas,
       MIN(data_inicio_resp) AS primeira_atribuicao,
       MAX(data_fim_resp) AS ultima_atribuicao
FROM VW_ZONA_RESP_CENTRAL
WHERE data_inicio_resp IS NOT NULL
GROUP BY sigla_central
ORDER BY qtd_zonas DESC;
```

### CU-VIEW02-04: Auditoria de Mudanças de Jurisdição

**Contexto:** Identificar quando houve mudanças na responsabilidade de zonas entre centrais ao longo do tempo.

**Nota:** Como a view mostra apenas a situação vigente atual, para auditoria histórica completa seria necessário consultar diretamente as tabelas do CORAU sem o filtro de vigência.

## Relação com Outras Views e Tabelas

### Integração com VW_UNIDADE

Esta view é **essencial** para a correta construção de `VW_UNIDADE`, especificamente para determinar a hierarquia das CAEs:

- `VW_UNIDADE` consulta `VW_ZONA_RESP_CENTRAL` para obter o `codigo_zona_resp`
- Este código se torna o `unidade_superior_codigo` da CAE
- Sem esta view, as CAEs não teriam posição correta na árvore de unidades

### Dependência de VW_RESPONSABILIDADE e VW_USUARIO_PERFIL_UNIDADE

Indiretamente, esta view afeta:
- A determinação de responsáveis por CAEs (através da estrutura hierárquica)
- A atribuição de perfis a usuários lotados em CAEs
- A validação de acessos e permissões relacionados a CAEs e suas zonas

## Dependências

### Permissões Necessárias no SGRH

```sql
GRANT SELECT ON SRH2.UNIDADE_TSE TO SGC;
```

### Permissões Necessárias no CORAU (SIGMA)

```sql
GRANT SELECT ON CORAU.RESP_CENTRAL TO SGC;
GRANT SELECT ON CORAU.EVENTO TO SGC;
GRANT SELECT ON CORAU.CT_CENTRAL TO SGC;
GRANT SELECT ON CORAU.CT_ZONA TO SGC;
```

### Tabelas de Origem

**Do SGRH:**
- `SRH2.UNIDADE_TSE`: Unidades organizacionais

**Do CORAU:**
- `CORAU.CT_CENTRAL`: Cadastro de centrais
- `CORAU.CT_ZONA`: Cadastro de zonas
- `CORAU.EVENTO`: Eventos de mudança
- `CORAU.RESP_CENTRAL`: Vinculação evento-central-zona

## Considerações de Performance

### Otimização de Junções

A view utiliza múltiplas junções (LEFT JOIN) entre sistemas diferentes (SGRH e CORAU). Para otimização:

1. **Índices recomendados:**
   - `UNIDADE_TSE.SIGLA_UNID_TSE`
   - `UNIDADE_TSE.NUM_ZE`
   - `CT_CENTRAL.SIGLA`
   - `EVENTO.DATAINICIO`, `EVENTO.DATATERMINO`

2. **Filtros aplicados cedo:**
   - Centrais: filtro `sigla_unid_tse LIKE 'CAE%'` em subconsulta
   - Zonas: filtro `num_ze IS NOT NULL` em subconsulta
   - Eventos: filtro de vigência na subconsulta de eventos

### Impacto em VW_UNIDADE

Como `VW_UNIDADE` consulta esta view para cada CAE, é crítico que `VW_ZONA_RESP_CENTRAL` tenha boa performance. A consulta é executada uma vez por CAE durante a montagem de `VW_UNIDADE`.

## Exemplo de Registros

```
+----------------+---------------+------------------+------------------+-------------------+-----------------+
| codigo_central | sigla_central | codigo_zona_resp | sigla_zona_resp  | data_inicio_resp  | data_fim_resp   |
+----------------+---------------+------------------+------------------+-------------------+-----------------+
| 1001           | CAE01         | 2001             | 1ZE              | 01/01/2024        | 31/12/2024      |
| 1002           | CAE02         | 2005             | 5ZE              | 01/06/2024        | 30/06/2025      |
| 1003           | CAE03         | NULL             | NULL             | NULL              | NULL            |
| 1004           | CAE04         | 2010             | 10ZE             | 15/03/2024        | 31/12/2024      |
+----------------+---------------+------------------+------------------+-------------------+-----------------+
```

**Interpretação:**
- CAE01: Responsável pela 1ª Zona Eleitoral durante todo o ano de 2024
- CAE02: Responsável pela 5ª Zona, com vigência de junho/2024 a junho/2025
- CAE03: Sem zona atribuída no momento (pode estar inativa ou em reestruturação)
- CAE04: Responsável pela 10ª Zona do meio de março até final de 2024

## Notas de Implementação

### Integração entre Sistemas

Esta view é um exemplo crítico de integração entre dois sistemas corporativos (SGRH e CORAU/SIGMA). Mudanças em qualquer um dos sistemas podem afetar os resultados:

**Mudanças no SGRH:**
- Criação/extinção de CAEs ou Zonas
- Alteração de siglas
- Mudança de situação de unidades

**Mudanças no CORAU:**
- Novos eventos de responsabilidade
- Alteração de datas de vigência
- Mudanças nos cadastros de centrais ou zonas

### Sincronização de Dados

Como os dados vêm de sistemas diferentes, podem existir inconsistências temporárias:

- Zona cadastrada no CORAU mas não no SGRH (ou vice-versa)
- Siglas diferentes entre os sistemas
- Divergências nos números de zonas

Estas inconsistências resultarão em registros com colunas NULL e devem ser tratadas pelos processos de sincronização entre SGRH e CORAU.

### Periodicidade de Atualização

A view reflete sempre o estado atual dos dados (tempo real), mas:

- Os eventos de responsabilidade devem ser atualizados no CORAU quando houver mudanças
- A vigência é calculada com base em `SYSDATE`, então muda automaticamente quando eventos expiram
- Não há cache ou materialização, todas as consultas são executadas em tempo real
