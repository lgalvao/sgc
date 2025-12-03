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

### CDU-06 e CDU-07 - Detalhar processo e subprocesso
**Problema:** Os testes falhavam na criação de processos com a mensagem implícita de que a unidade já participava de um processo ativo (causando falha no redirecionamento para o painel). Isso ocorria porque os testes utilizavam a unidade `ASSESSORIA_21`, que fica com um processo ativo (`EM_ANDAMENTO`) ao final da execução do teste `CDU-05`.
**Solução:**
1.  Adicionado o usuário `CHEFE_ASSESSORIA_22` (Jimi Hendrix) em `e2e/helpers/auth.ts`.
2.  Atualizado `e2e/cdu-06.spec.ts` para utilizar a unidade `ASSESSORIA_22` no primeiro caso de teste.
3.  Atualizado `e2e/cdu-07.spec.ts` para utilizar a unidade `ASSESSORIA_22` e o respectivo chefe.
**Resultado Esperado:** Os testes devem passar pois utilizarão uma unidade livre, evitando conflitos com o estado deixado pelo `CDU-05`.
