# Guia para Correção de Testes E2E

Este guia documenta as lições aprendidas durante a depuração dos testes E2E do CDU-01, com o objetivo de acelerar a resolução de problemas semelhantes no futuro.

## 1. O Problema: Testes de Login Falhando Inexplicavelmente

Os testes E2E, especialmente o `cdu-01.spec.ts`, estavam falhando consistentemente com timeouts e erros de "elemento não encontrado". As tentativas iniciais de correção, focadas em problemas de ambiente e configuração do Playwright, não tiveram sucesso.

## 2. A Causa Raiz: Desalinhamento de Dados em Múltiplas Camadas

A investigação aprofundada revelou que a causa raiz não era um único erro, mas um desalinhamento de dados em três locais críticos:

1.  **`backend/src/main/resources/data.sql`**: O script de seed do banco de dados para o ambiente de teste.
2.  **`backend/src/main/java/sgc/sgrh/SgrhService.java`**: O mock do serviço SGRH, que retorna dados de usuário e unidade.
3.  **`frontend/e2e/helpers/dados/constantes-teste.ts`**: As constantes de dados de teste usadas pelos testes E2E.

O problema específico era que o `SgrhService` mockado retornava um `PerfilDto` com um `unidadeCodigo` que não existia no `data.sql`. Isso causava um erro de "Unidade não encontrada" no backend, o que impedia o processo de login de ser concluído com sucesso e, por consequência, a página do painel de ser exibida.

## 3. O Processo de Login Multi-Etapas

A depuração também revelou que o processo de login não é uma única etapa de autenticação, mas um fluxo de várias etapas:

1.  **Autenticação**: O usuário envia o título e a senha. O backend (atualmente mockado) sempre retorna sucesso.
2.  **Autorização**: O frontend solicita os pares `Perfil-Unidade` do usuário. É nesta etapa que o erro "Unidade não encontrada" ocorria.
3.  **Seleção de Perfil**: Se o usuário tiver mais de um par `Perfil-Unidade`, o frontend exibe um seletor para que o usuário escolha com qual perfil e unidade deseja acessar o sistema.
4.  **Entrada**: O frontend envia a escolha do usuário para o backend, que finaliza o processo de login e redireciona para o painel.

## 4. Lições Aprendidas e Passos para a Correção

1.  **Verifique o Alinhamento de Dados Primeiro**: Antes de assumir que o problema é com o ambiente ou com a configuração do Playwright, verifique se os dados de teste estão consistentes em todas as camadas (banco de dados, mocks de serviço e constantes de teste do frontend).
2.  **Comece pelo Erro do Backend**: Se um teste E2E está falhando, verifique os logs do backend em busca de erros. Um erro no backend, como o "Unidade não encontrada", é um forte indicador da causa raiz do problema.
3.  **Entenda o Fluxo de Negócio**: Certifique-se de entender o fluxo de negócio completo que está sendo testado. No caso do login, a falha em entender o processo de autorização multi-etapas levou a uma depuração ineficiente.
4.  **Crie um Teste de Linha de Base**: Se os testes existentes são complexos e estão falhando, crie um novo teste E2E, o mais simples possível, para verificar se o ambiente de teste está funcionando corretamente. Isso ajuda a isolar o problema.
5.  **Documente o que Você Aprendeu**: Ao resolver um problema complexo, documente as lições aprendidas. Isso economizará tempo para você e para outros no futuro.

## 5. Resumo da Correção Aplicada

A correção para os testes do CDU-01 envolveu:

1.  **Alinhar o `SgrhService`**: Modificar o `SgrhService.java` para que os métodos `buscarPerfisUsuario` e `criarUnidadesMock` retornem dados que sejam consistentes com o `data.sql`.
2.  **Alinhar as Constantes de Teste**: Modificar o `constantes-teste.ts` para que o objeto `PERFIS` use os mesmos dados alinhados.
3.  **Corrigir os Testes Unitários**: Modificar o `SgrhServiceTest.java` para que os testes unitários passem com os novos dados mockados.

Ao seguir estas diretrizes, a depuração de futuros testes E2E deve ser um processo muito mais rápido e eficiente.
