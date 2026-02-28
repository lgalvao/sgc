# VIEW-01 - VW_VINCULACAO_UNIDADE - Vinculação e Histórico de Unidades

## Finalidade

Esta view fornece o mapeamento histórico das unidades organizacionais do TRE-PE, registrando as vinculações e sucessões
entre unidades ao longo do tempo. É fundamental para rastreabilidade de mudanças organizacionais, permitindo identificar
qual unidade atual sucedeu quais unidades anteriores, e quais foram as demais unidades que fizeram parte desse histórico
de transformações.

## Origem dos Dados

**Tabela principal:** `SRH2.UNIDADE_TSE`

A view utiliza apenas a tabela de unidades do Sistema de Gestão de Recursos Humanos (SGRH), processando os dados de
maneira hierárquica através de consultas recursivas (CONNECT BY).

## Estrutura da View

| Coluna                       | Tipo     | Descrição                                                                                                                                                                 | Origem                                                      |
|------------------------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| `unidade_atual_codigo`       | NUMBER   | Código da unidade atual (ativa ou operacional)                                                                                                                            | `UNIDADE_TSE.CD`                                            |
| `unidade_anterior_codigo`    | NUMBER   | Código da unidade que precedeu diretamente a unidade atual                                                                                                                | `UNIDADE_TSE.COD_UNID_TSE_ANT`                              |
| `demais_unidades_historicas` | VARCHAR2 | Lista textual das demais unidades históricas que antecederam a unidade anterior, em ordem cronológica inversa (da mais recente para a mais antiga), separadas por vírgula | Processamento via CTE com `SYS_CONNECT_BY_PATH` e `LISTAGG` |

## Regras de Negócio

### RN-VIEW01-01: Filtragem de Unidades Ativas

A view inclui apenas unidades que estejam em situação ativa ou operacional, conforme os seguintes critérios:

- Unidades com `SIT_UNID = 'C'` (ativa/corrente)
- Unidades com `SIT_UNID` iniciando com 'O' (operacional)
- Unidades extintas (código iniciando com 'E') são excluídas

**Justificativa:** Apenas unidades em funcionamento têm relevância para os processos de mapeamento, revisão e
diagnóstico de competências.

### RN-VIEW01-02: Construção do Caminho Histórico

O histórico de vinculações é construído através de três etapas implementadas via CTEs (Common Table Expressions):

**Etapa 1 - CTE HistoricoCompleto:**

- Constrói o caminho completo da raiz até a unidade atual
- Utiliza `START WITH COD_UNID_TSE_ANT IS NULL` para identificar unidades raiz
- Aplica `CONNECT BY NOCYCLE PRIOR CD = COD_UNID_TSE_ANT` para navegar a hierarquia
- Gera uma string com todas as unidades no caminho, separadas por '->'
- Registra o nível hierárquico de cada unidade

**Etapa 2 - CTE HistoricoExtinto:**

- Processa apenas unidades com nível > 2 (que possuem histórico além da unidade atual e imediatamente anterior)
- Extrai a porção do caminho que exclui a unidade atual e a unidade anterior
- Utiliza expressão regular `^(.*?)->[^>]+->[^>]+$` para isolar o histórico

**Etapa 3 - Consulta Principal:**

- Tokeniza a string histórica separando por '->'
- Inverte a ordem para apresentar da mais recente para a mais antiga
- Concatena os códigos com vírgula usando `LISTAGG`

### RN-VIEW01-03: Casos Especiais de Histórico

**Unidades com nível 1 (raiz):**

- `unidade_anterior_codigo`: NULL
- `demais_unidades_historicas`: NULL

**Unidades com nível 2 (um nível abaixo da raiz):**

- `unidade_anterior_codigo`: código da unidade raiz
- `demais_unidades_historicas`: NULL

**Unidades com nível > 2:**

- `unidade_anterior_codigo`: código da unidade imediatamente anterior
- `demais_unidades_historicas`: lista de códigos das demais unidades no caminho histórico

### RN-VIEW01-04: Ordem de Apresentação

Os registros são ordenados por `unidade_atual_codigo` em ordem crescente para facilitar a localização de unidades
específicas.

## Casos de Uso da View

### CU-VIEW01-01: Rastreamento de Mudanças Organizacionais

**Contexto:** Quando uma unidade é reestruturada ou extinta, é necessário identificar qual unidade atual assumiu suas
atribuições.

**Exemplo prático:**

```sql
-- Identificar a unidade atual que sucedeu uma unidade histórica
SELECT unidade_atual_codigo, unidade_anterior_codigo, demais_unidades_historicas
FROM VW_VINCULACAO_UNIDADE
WHERE unidade_anterior_codigo = 123
   OR '123' IN (
       SELECT TRIM(REGEXP_SUBSTR(demais_unidades_historicas, '[^,]+', 1, LEVEL))
       FROM DUAL
       CONNECT BY LEVEL <= REGEXP_COUNT(demais_unidades_historicas, ',') + 1
   );
```

### CU-VIEW01-02: Auditoria de Processos Históricos

**Contexto:** Em processos antigos de mapeamento ou revisão, as unidades participantes podem ter sido extintas ou
reestruturadas. Esta view permite identificar as unidades atuais correspondentes.

**Aplicação:** Quando um usuário consulta o histórico de processos finalizados em unidades que não existem mais, o
sistema pode usar esta view para mapear para a unidade atual correspondente.

### CU-VIEW01-03: Migração de Dados e Documentos

**Contexto:** Quando documentos, mapas de competências ou outros dados de uma unidade histórica precisam ser
transferidos para a unidade sucessora.

**Aplicação:** Durante processos de migração de dados, o sistema pode utilizar esta view para garantir que todos os
documentos e registros sejam associados à unidade correta na estrutura atual.

## Relação com Outras Views e Tabelas

### Uso em Conjunto com VW_UNIDADE

A view `VW_VINCULACAO_UNIDADE` complementa `VW_UNIDADE`, fornecendo a dimensão temporal que não está presente na view de
unidades. Enquanto `VW_UNIDADE` mostra o estado atual das unidades, `VW_VINCULACAO_UNIDADE` registra as transformações
históricas.

### Integração com Processos do Sistema

Embora não seja utilizada diretamente nos casos de uso principais dos processos de mapeamento, revisão e diagnóstico,
esta view é importante para:

1. **Relatórios históricos** que necessitam consolidar dados de unidades que foram reestruturadas
2. **Auditoria** de mudanças organizacionais ao longo do tempo
3. **Manutenção** de integridade referencial em situações de reestruturação

## Dependências

### Permissões Necessárias

```sql
GRANT SELECT ON SRH2.UNIDADE_TSE TO SGC;
```

### Tabelas de Origem

- `SRH2.UNIDADE_TSE`: Tabela de unidades organizacionais do SGRH

## Considerações de Performance

### Otimização de Consultas Recursivas

A view utiliza `CONNECT BY NOCYCLE` para prevenir loops infinitos em casos de dados inconsistentes onde possa haver
referências circulares na hierarquia de unidades.

### Complexidade Computacional

O processamento hierárquico e a construção de strings podem ter impacto em performance para grandes volumes de dados.
Para consultas frequentes que não necessitem do histórico completo, considere:

1. Consultar apenas `unidade_atual_codigo` e `unidade_anterior_codigo`
2. Aplicar filtros WHERE antes de processar o histórico completo
3. Utilizar índices na coluna `CD` e `COD_UNID_TSE_ANT` da tabela `UNIDADE_TSE`

## Exemplo de Registros

```
+-----------------------+---------------------------+-------------------------------+
| unidade_atual_codigo  | unidade_anterior_codigo  | demais_unidades_historicas   |
+-----------------------+---------------------------+-------------------------------+
| 1                     | NULL                      | NULL                          |
| 100                   | 1                         | NULL                          |
| 250                   | 200                       | 100, 50                       |
| 300                   | 250                       | 200, 100, 50                  |
+-----------------------+---------------------------+-------------------------------+
```

**Interpretação:**

- Unidade 1: Unidade raiz, sem predecessores
- Unidade 100: Diretamente subordinada à raiz (código 1)
- Unidade 250: Sucedeu a unidade 200, que por sua vez sucedeu as unidades 100 e 50 (nesta ordem)
- Unidade 300: Sucedeu a unidade 250, com histórico completo de 200, 100 e 50

## Notas de Implementação

### Versão do Oracle

A view utiliza funções específicas do Oracle Database:

- `SYS_CONNECT_BY_PATH`: Disponível a partir do Oracle 9i
- `LISTAGG`: Disponível a partir do Oracle 11g Release 2
- `REGEXP_SUBSTR` e `REGEXP_COUNT`: Disponíveis a partir do Oracle 10g

### Manutenção

Em caso de mudanças na estrutura de vinculação de unidades no SGRH, esta view refletirá automaticamente as mudanças,
pois lê diretamente da tabela `UNIDADE_TSE`. Não é necessária manutenção adicional além de garantir que as permissões de
acesso permaneçam válidas.
