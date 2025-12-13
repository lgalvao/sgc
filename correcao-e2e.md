# Correção de Testes E2E

## Diagnóstico Inicial

Executei a suíte de testes E2E e identifiquei 3 falhas principais:

1.  `e2e/cdu-10.spec.ts`: Falha no "Cenario 3: Devolução e Histórico de Análise". O elemento de status não foi encontrado ou houve erro de acesso ("Access Denied").
2.  `e2e/cdu-10.spec.ts`: Falha no "Cenario 4: Verificar que histórico foi excluído...". O teste não conseguia encontrar a linha "Seção 221" na tabela de unidades.
3.  `e2e/cdu-15.spec.ts`: Falha no "CT-06: Navegar para Disponibilização". O teste esperava verificar um badge na mesma página, mas ocorria redirecionamento.

## Correções Aplicadas

### 1. Correção em `e2e/cdu-15.spec.ts`

-   **Problema:** O teste falhava ao verificar elemento na página errada após ação de disponibilizar.
-   **Solução:** Atualizei a expectativa para aguardar o redirecionamento para `/painel` e verificar a mensagem de sucesso no painel.

### 2. Correção em `e2e/cdu-10.spec.ts` (Cenario 3 e 5)

-   **Problema:** Navegação instável na tabela de processos devido a seletores genéricos, levando ao clique em processos errados (ex: "Processo Seed 99").
-   **Solução:** Substituí seletores `page.locator('tr', {has: ...})` por `page.getByRole('cell', {name: ..., exact: true}).click()`, garantindo o clique no processo correto.

### 3. Correção em `e2e/cdu-10.spec.ts` (Cenario 4)

-   **Problema:** Além da navegação incorreta, o teste falhava ao tentar encontrar a unidade "Seção 221" dentro do detalhe do processo, pois a árvore de unidades estava colapsada ou a página não carregava a tempo.
-   **Solução:** Substituí a navegação via clique na tabela (que era propensa a erros) por navegação direta via URL (`/processo/${processoRevisaoId}`) e adicionei uma espera explícita pela visibilidade da tabela (`expect(page.getByRole('table')).toBeVisible()`) antes de tentar interagir com as linhas. Isso estabilizou o teste.

## Resumo

-   **CDU-15:** Corrigido.
-   **CDU-10:** Corrigido (Cenarios 3, 4 e 5 estabilizados).

Limpeza de arquivos temporários (`e2e/server.log`, `test-results/`) foi realizada para evitar poluição do repositório.
