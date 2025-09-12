## Padrões e Boas Práticas para Testes E2E (Playwright)

Este documento consolida as regras, padrões e lições aprendidas para a criação e manutenção de testes E2E com Playwright no projeto.

### 0. Como/quando executar os testes
 
* Use `npx playwright test` para rodar todos os testes. 
* Para rodar um teste específico, use `npx playwright test spec/nome-do-teste.spec.ts`.
* Não rodar todo os testes frequentemente. Foque em testes específicos durante o desenvolvimento.
* Use `npx playwright test --last-failed` quando estiver corrigindo problemas identificados nos testes.

### 1. Estrutura e Configuração

*   **Localização**: Todos os testes E2E estão no diretório `spec/`.
*   **Estrutura de Arquivos**: Cada arquivo `.spec.ts` deve testar uma funcionalidade específica (ex: `login.spec.ts`, `cad-atividades.spec.ts`).
*   **Servidor**: O servidor de desenvolvimento já está rodando em segundo plano. Não tente executá-lo novamente.
*   **Timeouts**: Os testes devem ter timeouts curtos (máximo de 5000ms). Como o backend é mockado, falhas de timeout geralmente indicam problemas no seletor ou na lógica do teste, não na performance. **Não altere timeouts.**

### 2. Seletores e Interações

*   **Prefira `data-testid`**: Use sempre `data-testid` para selecionar elementos interativos como formulários e botões. Se necessário, adicione-os ao código-fonte da aplicação.
*   **Evite Seletores Frágeis**: Não use seletores baseados em classes CSS ou IDs gerados dinamicamente.
*   **Seletores Precisos São Cruciais**: Evite seletores ambíguos. A violação do "strict mode" do Playwright é um sinal claro de imprecisão. Use `RegExp` ou seletores estruturais para garantir a seleção do elemento correto.

### 3. Lógica de Teste e Asserções

*   **Prefira Testes Atômicos**: Testes grandes que cobrem múltiplos cenários são frágeis, lentos e difíceis de depurar. Crie testes concisos que validem aspectos específicos.
*   **Espere Ativamente, não Use Tempos Fixos**: Nunca use esperas com tempo fixo (`waitForTimeout`). Para elementos assíncronos (animações, notificações), espere por um estado específico (ex: `expect(locator).toBeHidden()`).
*   **Validação vs. Especificação**: Sempre compare os testes com a especificação do CDU para garantir cobertura total. Adicione testes para cada item do fluxo principal e, se necessário, use comentários para referenciar o item (ex: `// item 9.1`).

### 4. Dados Mockados

*   **Entenda os Dados**: Antes de escrever asserções, leia os arquivos de mock (`.json`) em `src/mocks/` para entender o estado inicial dos dados.
*   **Trabalhe com os Dados Existentes**: Não tente interceptar ou mockar dados em tempo de execução. As manipulações nos testes devem refletir um impacto real nos dados mockados existentes.
*   **Adição de Dados**: Se for necessário, adicione novos dados diretamente aos arquivos canônicos em `src/mocks/`. Garanta que os IDs sejam únicos.
*   **Compatibilidade de Tipos**: Ao usar dados mockados em código tipado, garanta que o `type cast` corresponda à interface de destino, prestando atenção a propriedades opcionais (`?`).

### 5. Padrões de Código e Funções Auxiliares

*   **Funções Auxiliares**: Crie funções auxiliares para ações repetitivas (ex: `login`, `createCompetencia`). Isso reduz a duplicação, facilita a manutenção e torna os testes mais legíveis.
*   **Autenticação**: Use a função `login` de `spec/utils/auth.ts` antes de cada suíte. Utilize IDs de servidores válidos que existam nos mocks.
*   **Modais e Confirmações**:
    *   Sempre aguarde a abertura do modal antes de interagir.
    *   Use seletores específicos para os botões dentro do modal.
    *   Verifique o fechamento do modal e o resultado da ação (confirmação ou cancelamento).
*   **Notificações (Toasts)**:
    *   Use uma função auxiliar dedicada (ex: `waitForNotification`) que espere a notificação aparecer e depois desaparecer.
    *   A função deve ser flexível para aceitar diferentes tipos de seletores.
*   **Elementos Dinâmicos**: Para elementos que podem não existir (ex: uma lista vazia), use verificações condicionais (`if (await locator.count() > 0)`) ao invés de asserções que possam falhar.