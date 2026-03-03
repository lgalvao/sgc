# Relatório de Execução de Testes E2E (Playwright)

## Resumo dos Testes

### Bateria 1 (cdu-01 a cdu-05)
*   **Resultados:** 34 testes passaram, 1 ignorado (skipped).

### Bateria 2 (cdu-06 a cdu-10)
*   **Resultados:** 12 testes passaram.

### Bateria 3 (cdu-11 a cdu-15)
*   **Resultados:** 8 testes passaram, 4 falharam, 1 não executado.
*   **Falhas:**
    *   **CDU-11 (Cenário 3: Visualizar processo finalizado):** Timeout (15s) ao tentar clicar no texto do processo mapeado na tabela `tbl-processos`.
    *   **CDU-13 (Cenário 6: CHEFE vê processo "Em andamento" para ajustes):** Timeout (15s) no botão de logout. Elemento `div.orchestrator-container` interceptou eventos de ponteiro.
    *   **CDU-14 (Cenário 1: Preparação: Mapeamento completo e finalizado):** Timeout (15s) esperando modal `mdl-criar-competencia` desaparecer após salvar (`toBeHidden` falhou, recebeu `visible`).
    *   **CDU-15 (Cenários CDU-15: Fluxo completo de manutenção do mapa pelo ADMIN):** Erro: "Target page, context or browser has been closed" esperando botão no modal criar competência (`toBeVisible`).

### Bateria 4 (cdu-16 a cdu-20)
*   **Resultados:** 22 testes passaram, 1 falhou.
*   **Falhas:**
    *   **CDU-20 (Fluxo completo de validação de mapa > 3. ADMIN disponibiliza Mapa):** Timeout (15s) tentando clicar no checkbox de uma atividade dentro de um cartão. O locator não conseguiu estabilizar (`element is not stable`).

### Bateria 5 (cdu-21 a cdu-25)
*   **Resultados:** 32 testes passaram, 1 falhou.
*   **Falhas:**
    *   **CDU-24 (Fluxo completo: De criação de processo à disponibilização em bloco):** Timeout (2s) esperando o botão "Disponibilizar.*Bloco" ficar habilitado (`toBeEnabled` falhou, recebeu timeout, elemento não encontrado).

### Bateria 6 (cdu-26 a cdu-30)
*   **Resultados:** 22 testes passaram.

### Bateria 7 (cdu-31 a cdu-36)
*   **Resultados:** 10 testes passaram, 1 falhou, 4 não executados (por causa de falha em setup de preparação).
*   **Falhas:**
    *   **CDU-33 (Preparação 0: Criar e finalizar Mapeamento):** Timeout (15s) esperando o clique no card de mapa de visualização (`card-subprocesso-mapa-visualizacao`).

## Análise e Sugestões de Correções

1.  **CDU-11 (Timeout na tabela):** O teste está tentando clicar em uma linha que pode não estar aparecendo. O texto do processo pode estar diferente, ou ele não carregou a tempo.
    *   **Sugestão:** Aumentar o timeout de espera ou verificar se a listagem de processos realmente contém o processo antes de tentar clicar nele, e usar uma forma mais resiliente para identificar as linhas.

2.  **CDU-13 (Intercepção por Overlay):** Ocorreu ao clicar em Logout. Um elemento modal/toast (`orchestrator-container` com botão close) cobriu o botão de logout impedindo o clique.
    *   **Sugestão:** Utilizar `.click({ force: true })` no logout ou fechar explicitamente qualquer toaster visível ou aguardar que toasters/notificações sumam da tela através de limpeza explícita antes do clique de logout (parece que `limparNotificacoes` não está funcionando totalmente).

3.  **CDU-14 e CDU-15 (Modal Criar Competência):** Falhas em testes ligados a criação e manipulação do modal `mdl-criar-competencia`. Em uma das falhas o modal não fechou, na outra falhou logo depois.
    *   **Sugestão:** Provavelmente o clique em "Salvar" não efetivou e ele continuou aberto mostrando erros, ou houve alguma lentidão e ele demorou para fechar (requer um `expect().toBeHidden()` melhor dimensionado), ou precisa esperar por uma mensagem de sucesso após salvar.

4.  **CDU-20 (Elemento não estável em `helpers-mapas.ts`):** O checkbox dentro do `card-atividade` não ficou estável para clique.
    *   **Sugestão:** O checkbox pode estar animando, usar `{ force: true }` no `click()` do checkbox do modal de competências, ou focar no elemento antes.

5.  **CDU-24 (Botão de Disponibilizar em Bloco):** O elemento foi procurado por texto "Disponibilizar.*Bloco" com timeout curto de 2 segundos. O elemento pode nem estar em tela.
    *   **Sugestão:** O timeout customizado de 2000ms (`await expect(btnDisponibilizar).toBeEnabled({ timeout: 2000 })`?) no arquivo cdu-24 está muito curto. Remover o limite customizado ou aumentar para 15s.

6.  **CDU-33 (Timeout de card):** Timeout ao clicar no `card-subprocesso-mapa-visualizacao` em `helpers-mapas.ts`. O card pode não ter o data-testid esperado nesse contexto, ou o status do subprocesso mudou e o tipo do card mudou (ex: de visualização para edição ou vice versa).
    *   **Sugestão:** Verificar se o card existe usando um selector mais flexível, ou verificar o estado do processo naquele momento para garantir se o card de "visualização" é o card correto em exibição.

## Conclusão
Muitos problemas têm origem em pequenas questões de Timing/Overlay do Playwright (toasters bloqueando tela, checkboxes com animação impedindo clique) e timeouts que podem estar curtos para operações completas. A principal correção global seria aprimorar o helper de limpar notificações e checar a resiliência no `helpers-mapas.ts`.