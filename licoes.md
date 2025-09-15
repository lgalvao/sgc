# Lições Aprendidas no Processo de Depuração dos Testes E2E

O processo de extensão e correção dos testes para o `CDU-01` foi um exercício de depuração profundo que revelou tanto fragilidades nos testes quanto bugs na aplicação. As seguintes lições foram aprendidas e devem ser consideradas para futuros desenvolvimentos.

### 1. Falhas em Cascata Geralmente Apontam para uma Causa Raiz Compartilhada

Uma grande quantidade de testes falhando simultaneamente com o mesmo erro (neste caso, `TimeoutError` ao esperar por uma URL) não deve ser tratada como falhas individuais. O padrão indicou um problema fundamental na inicialização ou autenticação da aplicação no ambiente de teste. A correção de uma única alteração no `Login.vue` resolveu dezenas de falhas, confirmando essa abordagem.

### 2. Não Confie Cegamente em Documentação ou Requisitos para Seletores

Os testes falharam repetidamente porque os seletores (`getByLabel`, `getByText`, `getByTestId`) não correspondiam exatamente à implementação.

- **Solução:** Quando um seletor não for encontrado, use `console.log(await page.content())` dentro do teste para inspecionar o HTML real que o Playwright está vendo. Esta é a fonte da verdade e a maneira mais rápida de encontrar o seletor correto (seja um `title`, `aria-label`, `data-testid` ou o texto visível).

### 3. Isole o Problema Antes de Tentar Corrigir

Fazer várias alterações em múltiplos arquivos (`.spec.ts`, `auxiliares`, `constantes`) tornou a depuração inicial quase impossível. O progresso só foi alcançado após:

1.  **Reverter** o arquivo de teste principal ao seu estado original e funcional.
2.  Verificar que os arquivos auxiliares modificados não quebraram os testes existentes.
3.  Usar `test.only` para focar em um único teste falho por vez.

Essa abordagem de isolamento é crucial para identificar a causa raiz de forma eficiente.

### 4. Testes Podem Revelar Bugs Reais na Aplicação

A hipótese inicial era que os testes estavam errados. No entanto, a investigação provou que os testes estavam, na verdade, descobrindo bugs reais no `Login.vue`:

- A senha não era validada.
- A mensagem de erro para um usuário inexistente não correspondia à especificação.

Um teste que falha é um sinal de que algo está errado, e esse algo pode ser a própria aplicação.

### 5. Entenda a Arquitetura de Teste do Projeto

O projeto possuía múltiplos helpers de autenticação em locais diferentes (`spec/cdu/auxiliares-teste.ts` e `spec/utils/auth.ts`). Não ter conhecimento dessa duplicação poderia levar a correções ineficazes. É importante ter uma visão geral das ferramentas e padrões de teste existentes no projeto antes de adicionar ou modificar testes.
