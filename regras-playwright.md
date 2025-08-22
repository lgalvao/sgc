## Testes E2E (Playwright)

Os testes end-to-end (E2E) são implementados utilizando o Playwright e estão localizados no diretório `spec/`. Eles
garantem que os fluxos críticos da aplicação funcionem corretamente do ponto de vista do usuário.

- **Localização**: Todos os testes E2E estão no diretório `spec/`.
- **Estrutura**: Cada arquivo `.spec.ts` testa uma funcionalidade específica (ex: `login.spec.ts` para o fluxo de login,
  `cad-atividades.spec.ts` para o cadastro de atividades).
- **Autenticação**: O login é realizado uma vez antes de cada teste ou suíte, utilizando a função auxiliar `login` de
  `utils/auth.ts`, garantindo que os testes operem em um estado autenticado.
- **Estratégias Comuns de Depuração**:
    - **Logs do Console**: Verifique as mensagens do console do navegador durante a execução do teste para identificar
      erros ou avisos.
    - **Asserções Robustas**: Prefira `toContainText()` para verificar a presença de texto em elementos, especialmente
      em casos onde a visibilidade pode ser transitória ou afetada por outros elementos. Use `toBeVisible()` quando a
      visibilidade explícita for crucial.
- **Boas práticas específicas (após refatoração)**:
    - Use o botão "Voltar" global nos testes (role/button com nome "Voltar"), não data-testids locais.
    - Prefira rotas nomeadas e seletores `data-testid` estáveis para formulários e botões.
    - Endpoints atualizados conforme a lista acima; não usar mais endpoints legados ou redirects.
- **Timeouts**: Os testes E2E devem ter timeouts curtos, de no máximo 5000ms, pois não há backend e está tudo mockado.
  Se um teste não passar, o problema geralmente estará em outro lugar, não no timeout.
- **Estabilidade do Ambiente**: Antes de executar testes E2E, certifique-se de que o servidor de desenvolvimento (Vite)
  esteja estável e a aplicação carregue sem erros críticos no console do navegador (ex: `TypeError: app.mount is not a function`).
  Problemas no servidor ou na inicialização da aplicação podem levar a falhas inconsistentes nos testes E2E.
- **Seletores Robustos**: Prefira seletores baseados em `role` (ex: `page.getByRole('button', { name: 'Salvar' })`) ou
  `label` (ex: `page.getByLabel('Dias para inativação de processos:')`) para interagir com elementos da UI.
  Evite seletores baseados em classes CSS ou IDs gerados dinamicamente, pois são menos estáveis.
