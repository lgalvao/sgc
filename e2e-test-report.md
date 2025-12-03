# Relatório de Execução dos Testes E2E

## Resumo

*   **Total de Testes:** 29
*   **Aprovados:** 21
*   **Falhas:** 5
*   **Pulados:** 3
*   **Duração Total:** ~3.6m

## Detalhes das Falhas

### CDU-06 - Detalhar processo
**Caso de Teste:** Deve exibir detalhes do processo para ADMIN
**Erro:** Falha na asserção de URL.
**Detalhes:**
```
Error: expect(page).toHaveURL(expected) failed
Expected pattern: /\/painel/
Received string: "http://localhost:5173/processo/cadastro"
```
**Arquivo:** `e2e/cdu-06.spec.ts:8:9`

### CDU-07 - Detalhar subprocesso
**Caso de Teste:** Deve exibir detalhes do subprocesso para CHEFE
**Erro:** Falha na asserção de URL.
**Detalhes:**
```
Error: expect(page).toHaveURL(expected) failed
Expected pattern: /\/painel/
Received string: "http://localhost:5173/processo/cadastro"
```
**Arquivo:** `e2e/cdu-07.spec.ts:10:9`

### CDU-08 - Manter cadastro de atividades e conhecimentos
**Caso de Teste:** Cenário 1: Processo de Mapeamento (Fluxo Completo + Importação) -> 1. Setup: Criar Processo de Mapeamento
**Erro:** Falha na asserção de URL.
**Detalhes:**
```
Error: expect(page).toHaveURL(expected) failed
Expected pattern: /\/painel/
Received string: "http://localhost:5173/processo/cadastro"
```
**Arquivo:** `e2e/cdu-08.spec.ts:11:9`

### CDU-08 - Manter cadastro de atividades e conhecimentos
**Caso de Teste:** Cenário 2: Processo de Revisão (Botão Impacto) -> Setup: Criar Processo de Revisão
**Erro:** Falha na asserção de URL.
**Detalhes:**
```
Error: expect(page).toHaveURL(expected) failed
Expected pattern: /\/painel/
Received string: "http://localhost:5173/processo/cadastro"
```
**Arquivo:** `e2e/cdu-08.spec.ts:126:9`

### CDU-09 - Disponibilizar cadastro de atividades e conhecimentos
**Caso de Teste:** Preparacao: Admin cria e inicia processo
**Erro:** Timeout do teste excedido.
**Detalhes:**
```
Test timeout of 15000ms exceeded.
Error: locator.click: Test timeout of 15000ms exceeded.
waiting for locator('tr').filter({ has: getByText('Processo CDU-09 1764798092556') })
```
**Arquivo:** `e2e/cdu-09.spec.ts:29:9`

## Testes Pulados

Os seguintes testes no arquivo `cdu-09.spec.ts` foram pulados, provavelmente devido à falha no teste de preparação:

1.  Cenario 1: Validacao - Atividade sem conhecimento
2.  Cenario 2: Fluxo Feliz - Disponibilizar Cadastro
3.  Cenario 3: Devolucao e Historico de Analise

## Correções Aplicadas

### CDU-06 - Detalhar processo
**Problema:** Falha persistente ao criar processo para `ASSESSORIA_22`. Embora `ASSESSORIA_22` seja tecnicamente diferente de `ASSESSORIA_21` (usada no `CDU-05`), ambas residem na mesma árvore hierárquica (`SECRETARIA_2`). O processo de revisão do `CDU-05` pode estar causando efeitos colaterais em toda a ramificação da `SECRETARIA_2`.

**Solução:**
1.  **Isolamento Completo de Ramificação:** O teste foi movido para a ramificação da `SECRETARIA_1`, que é totalmente independente da execução do `CDU-05`.
    *   `CDU-06` agora utiliza `ASSESSORIA_12` (Unidade 4, sob `SECRETARIA_1`).
2.  **Atualização de Helpers:** Adicionado o usuário chefe correspondente (`CHEFE_ASSESSORIA_12`) ao arquivo `e2e/helpers/auth.ts`.
3.  **Atualização do Teste:** O arquivo `cdu-06.spec.ts` foi atualizado para usar `ASSESSORIA_12` e expandir a árvore `SECRETARIA_1`.

### CDU-07 - Detalhar subprocesso
**Problema:** Similar ao CDU-06, o uso de `SECAO_211` (sob `SECRETARIA_2`) estava sujeito a conflitos ou estados inconsistentes deixados pelo `CDU-05`.

**Solução:**
1.  **Isolamento Completo de Ramificação:** O teste foi movido para a ramificação da `SECRETARIA_1`.
    *   `CDU-07` agora utiliza `SECAO_121` (Unidade 10, sob `SECRETARIA_1` -> `COORD_12`).
2.  **Atualização de Helpers:** Adicionado o usuário chefe correspondente (`CHEFE_SECAO_121`) ao arquivo `e2e/helpers/auth.ts`.
3.  **Atualização do Teste:** O arquivo `cdu-07.spec.ts` foi atualizado para usar `SECAO_121` e expandir a árvore `SECRETARIA_1` -> `COORD_12`.

### CDU-08 e CDU-09 - Conflito de Unidades
**Status:** Os testes `CDU-08` e `CDU-09` ainda utilizam unidades sob a árvore `SECRETARIA_2` (`SECAO_212` e `SECAO_221`). Se as falhas persistirem por motivos semelhantes, recomenda-se mover também estes testes para a árvore `SECRETARIA_1` (ex: `SECAO_112`, `SECAO_113`) ou `SECRETARIA_1` -> `COORD_12` (ex: `SECAO_121` se não conflitar).

**Resultado Esperado:**
Eliminação das falhas de criação de processo nos CDUs 06 e 07 ao utilizar uma hierarquia de unidades (Secretaria 1) que não é tocada pelo teste anterior (CDU-05), garantindo um estado limpo para a execução.
