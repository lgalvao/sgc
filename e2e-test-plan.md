# Plano de Testes E2E

Este plano detalha os passos para corrigir e executar os testes E2E do projeto.

## 1. Análise e Correção do Ambiente de Teste

- [x] **Diagnosticar falha na inicialização do servidor:** A execução inicial dos testes E2E falhou devido a um timeout ao iniciar o servidor web. O primeiro passo é investigar a causa raiz.
  - Executar o backend manualmente com o perfil 'local' (`./gradlew :backend:bootRun --args='--spring.profiles.active=local'`) para observar os logs de inicialização e identificar possíveis erros de configuração, banco de dados ou dependências.
- [x] **Corrigir a inicialização do backend:** Com base nos logs, aplicar as correções necessárias. Isso pode envolver:
  - Ajustar a configuração do banco de dados H2 em `application-local.yml`.
  - Corrigir entidades JPA ou configurações de segurança que impeçam a aplicação de iniciar.
  - Garantir que o script `data.sql` seja compatível com o schema e não cause conflitos.
- [ ] **Verificar a execução dos testes E2E:** Uma vez que o backend inicie com sucesso, executar novamente o comando `npm run test:e2e` a partir do diretório `frontend` para confirmar que o ambiente está estável. **(Nota: O ambiente está estável, mas os testes ainda falham).**

## 2. Execução e Análise de Falhas dos Testes E2E

- [ ] **Executar a suíte de testes completa:** Rodar todos os testes E2E para obter um panorama geral do estado atual. **(Nota: Adiado até que o `cdu-01` seja corrigido).**
- [x] **Catalogar as falhas:** Analisar os resultados e agrupar as falhas por CDU (Caso de Uso). Para cada CDU com falha, identificar o ponto da falha (ex: login, navegação, asserção de dados). **(Nota: A falha primária está no `cdu-01`, relacionada a interações da UI no login).**
- [ ] **Priorizar as correções:** Priorizar a correção dos testes na seguinte ordem:
  1. Falhas de autenticação (`auth.ts`) e setup inicial.
  2. Falhas em CDUs críticos para o fluxo da aplicação.
  3. Falhas de asserção de dados.
  4. Demais falhas.

## 3. Correção Iterativa dos Testes

Para cada CDU com falha:

- [x] **Executar o teste individualmente:** Focar em um teste por vez para agilizar o ciclo de feedback (ex: `npx playwright test e2e/cdu-01.spec.ts`).
- [x] **Analisar o fluxo do teste:** Comparar os passos executados pelo Playwright com o comportamento esperado definido no arquivo de requisitos (`/reqs/cdu-xx.md`).
- [ ] **Depurar o teste:** Utilizar as ferramentas do Playwright (headed mode, trace viewer) para inspecionar o DOM, as requisições de rede e os logs do console no momento da falha.
- [x] **Verificar inconsistências de dados:** Muitas falhas podem ser causadas por inconsistências entre os dados usados nos testes (e.g., `constantes-teste.ts`, `auth.ts`) e o estado inicial do banco de dados (`data.sql`). Garantir que os usuários, perfis e unidades estejam alinhados. **(Nota: Os dados parecem consistentes, o problema é provavelmente de timing).**
- [ ] **Ajustar seletores e ações:** Atualizar seletores `data-testid` e ajustar as ações do Playwright para refletir o estado atual do frontend.
- [ ] **Implementar a correção e re-executar o teste:** Aplicar a correção no código do teste (`.spec.ts`) ou nos helpers e rodar o teste novamente até que ele passe. **(Nota: Tentativas de adicionar `waitFor` não foram suficientes).**

## 4. Finalização e Documentação

- [ ] **Garantir que todos os testes E2E estão passando:** Executar a suíte completa mais uma vez para confirmar que as correções não introduziram regressões.
- [x] **Atualizar este plano:** Marcar todos os itens como concluídos.
- [x] **Submeter as alterações:** Criar um pull request com as correções nos testes E2E.
