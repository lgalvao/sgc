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
**Problema:** Falha ao criar processo para `ASSESSORIA_21` (unidade ocupada pelo `CDU-05`).
**Solução:** Atualizado para usar `ASSESSORIA_22`. (Mantido da iteração anterior).

### CDU-07, CDU-08 e CDU-09 - Conflito de Unidades
**Problema:**
1.  `CDU-07` falhava ao usar `ASSESSORIA_22`, pois esta unidade ficava ocupada ("Em andamento") após a execução do `CDU-06`.
2.  `CDU-08` e `CDU-09` falhavam ao usar `ASSESSORIA_21`, que permanecia ocupada após o `CDU-05`.
O sistema bloqueia a criação de novos processos para unidades que já participam de um processo em andamento, causando falhas de redirecionamento (permanecendo na tela de cadastro) e timeouts.

**Solução:**
1.  **Isolamento de Unidades:** Cada teste E2E crítico agora utiliza uma unidade exclusiva para evitar conflitos de estado.
    *   `CDU-07` agora utiliza `SECAO_211` (Unidade 15).
    *   `CDU-08` agora utiliza `SECAO_212` (Unidade 16).
    *   `CDU-09` agora utiliza `SECAO_221` (Unidade 18).
2.  **Atualização de Helpers:** Adicionados os usuários chefes correspondentes (`CHEFE_SECAO_211`, `CHEFE_SECAO_212`, `CHEFE_SECAO_221`) ao arquivo `e2e/helpers/auth.ts`.
3.  **Atualização dos Testes:** Os arquivos `cdu-07.spec.ts`, `cdu-08.spec.ts` e `cdu-09.spec.ts` foram refatorados para usar as novas unidades e expandir os nós corretos da árvore (`SECRETARIA_2`, `COORD_21`, `COORD_22`).

**Resultado Esperado:**
Eliminação das falhas em cascata causadas por unidades presas em processos anteriores. Cada teste deve rodar de forma independente com sua própria unidade e usuário.
