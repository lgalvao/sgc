## Testes E2E (Playwright)

Os testes end-to-end (E2E) são implementados utilizando o Playwright e estão localizados no diretório `spec/`. Eles garantem que os fluxos críticos da aplicação funcionem corretamente do ponto de vista do usuário.

- **Localização**: Todos os testes E2E estão no diretório `spec/`.
- **Estrutura**: Cada arquivo `.spec.ts` contém um conjunto de testes relacionados a uma funcionalidade específica (ex: `login.spec.ts` para o fluxo de login, `cad-atividades.spec.ts` para o cadastro de atividades).
- **Autenticação**: O login é realizado uma vez antes de cada teste ou suíte de testes, utilizando a função auxiliar `login` de `utils/auth.ts`, garantindo que os testes operem em um estado autenticado.
- **Ferramentas de Automação (MCP)**: As interações com o navegador para os testes E2E são realizadas através das ferramentas de automação do Playwright (referidas internamente como "MCP"). Estas ferramentas permitem simular ações do usuário como cliques, digitação e navegação.
- **Estratégias Comuns de Depuração**:
    - **Logs do Console**: Verifique as mensagens do console do navegador durante a execução do teste para identificar erros ou avisos.
    - **Snapshots da Página**: Utilize `browser_snapshot()` para capturar a árvore de acessibilidade da página em um determinado ponto do teste, o que ajuda a entender a estrutura atual do DOM.
    - **Conteúdo do DOM**: Em casos de elementos que não são encontrados, use `page.evaluate(() => document.body.innerHTML)` para inspecionar o conteúdo HTML completo da página e verificar se o elemento ou texto esperado está realmente presente.
    - **Requisições de Rede**: Monitore as requisições de rede para garantir que os dados mockados estão sendo carregados corretamente e que não há erros de rede.
    - **Asserções Robustas**: Prefira `toContainText()` para verificar a presença de texto em elementos, especialmente em casos onde a visibilidade pode ser transitória ou afetada por outros elementos. Use `toBeVisible()` quando a visibilidade explícita for crucial.
- **Boas práticas específicas (após refatoração)**:
  - Use o botão "Voltar" global nos testes (role/button com nome "Voltar"), não data-testids locais.
  - Prefira rotas nomeadas e seletores `data-testid` estáveis para formulários e botões.
  - Endpoints atualizados conforme a lista acima; não usar mais endpoints legados ou redirects.
- **timeouts**: Os testes E2E devem ter timeouts curtos, de no máximo 5000ms, pois não há backend e está tudo mockado. Se um teste não passar, o problema geralmente estará em outro lugar, não no timeout. Além disso, para elementos dinâmicos (como modais ou notificações que aparecem e desaparecem rapidamente), pode ser necessário o uso de métodos de asserção mais robustos (ex: `toContainText` em vez de `toBeVisible`) para garantir a detecção correta.