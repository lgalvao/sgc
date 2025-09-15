# Lições Aprendidas no Processo de Depuração dos Testes E2E

O processo de extensão e correção dos testes E2E foi um exercício de depuração profundo que revelou tanto fragilidades nos testes quanto bugs na aplicação. As seguintes lições foram aprendidas através de múltiplas sessões de correção e devem ser consideradas para futuros desenvolvimentos.

### 1. Falhas em Cascata Geralmente Apontam para uma Causa Raiz Compartilhada

Uma grande quantidade de testes falhando simultaneamente com o mesmo erro (neste caso, `TimeoutError` ao esperar por uma URL) não deve ser tratada como falhas individuais. O padrão indicou um problema fundamental na inicialização ou autenticação da aplicação no ambiente de teste. A correção de uma única alteração no `Login.vue` resolveu dezenas de falhas, confirmando essa abordagem.

### 2. Não Confie Cegamente em Documentação ou Requisitos para Seletores

Os testes falharam repetidamente porque os seletores (`getByLabel`, `getByText`, `getByTestId`, `getByRole`) não correspondiam exatamente à implementação ou ao estado real do DOM.

- **Solução:** Quando um seletor não for encontrado, use `console.log(await page.content())` ou `console.log(await page.locator('body').innerHTML())` dentro do teste para inspecionar o HTML real que o Playwright está vendo. Esta é a fonte da verdade e a maneira mais rápida de encontrar o seletor correto (seja um `title`, `aria-label`, `data-testid` ou o texto visível).
- **`element(s) not found` pode ser enganoso:** Mesmo que o `innerHTML` mostre o elemento, o Playwright pode reportá-lo como não encontrado se ele não estiver visível, interativo ou se o contexto do locator estiver incorreto (ex: procurando um elemento em uma parte do DOM onde ele não existe).

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

### 6. Especificidade dos Seletores e `strict mode violation`

Seletores muito amplos podem levar a erros de `strict mode violation` no Playwright, onde múltiplos elementos correspondem ao seletor e o Playwright não sabe qual usar. 

- **Solução:** Sempre que possível, utilize seletores mais específicos, encadeando-os a partir de um elemento pai único (ex: `page.getByTestId('tabela-processos').getByRole('row', { name: /texto do processo/ })`). Isso garante que o Playwright encontre o elemento exato desejado.

### 7. Validação de Expectativas de Navegação por Perfil e Contexto

As expectativas de URL e de elementos visíveis na página podem variar drasticamente dependendo do perfil do usuário logado e do contexto da ação. 

- **Exemplo:** Um `GESTOR` pode ser direcionado para `/processo/<id>` (detalhes do processo) ao clicar em uma unidade na árvore, enquanto um `SERVIDOR` pode ir para `/processo/<id>/<slug>` (detalhes do subprocesso). As asserções de URL e de visibilidade de elementos (ex: `SUBPROCESSO_HEADER` vs `PROCESSO_INFO`) devem refletir essa lógica de negócio específica para cada perfil e cenário.

### 8. Sincronização com o Estado da Aplicação (`networkidle`)

Mesmo com os retries automáticos do Playwright, páginas dinâmicas podem apresentar problemas de timing. 

- **Solução:** Utilize `await page.waitForLoadState('networkidle');` após navegações ou interações que resultem em carregamento de dados ou mudanças significativas na UI. Isso garante que a página esteja completamente carregada e sem atividades de rede pendentes antes que os testes tentem interagir com novos elementos, reduzindo a flakiness.

### 9. Consistência dos Dados Mockados com o Cenário de Teste

As falhas nos testes frequentemente decorrem de um desencontro entre as suposições do teste e os dados reais nos arquivos de mock (`.json`). 

- **Solução:** Antes de escrever ou depurar testes, revise cuidadosamente os arquivos de mock (`processos.json`, `unidades.json`, `alertas.json`, etc.) para confirmar que os dados necessários para o cenário de teste (ex: um processo com status 'Criado', uma unidade específica em uma hierarquia) realmente existem e estão configurados conforme o esperado. Ajuste os seletores e as expectativas do teste para corresponder aos dados mockados existentes.

### 10. Caracteres Corrompidos Podem Causar Falhas em Cascata

Caracteres corrompidos ou problemas de encoding em arquivos de constantes podem causar falhas massivas nos testes, mesmo que o problema seja localizado em uma única linha.

- **Solução:** Quando múltiplos testes falharem com erros relacionados a constantes ou imports, verifique primeiro a integridade dos arquivos de constantes. Use ferramentas de diff ou inspeção de caracteres para identificar caracteres não-ASCII ou corrompidos que podem estar causando problemas de parsing.

### 11. Seletores Genéricos vs. Específicos para Elementos Dinâmicos

Elementos com IDs ou atributos gerados dinamicamente (ex: `chk-UNIDADE-123`) podem não existir no momento da execução do teste, causando falhas.

- **Solução:** Para elementos como checkboxes em listas dinâmicas, prefira seletores genéricos baseados em tipo (`input[type="checkbox"]`) ou classes CSS, em vez de IDs específicos. Combine com seletores de contexto para maior precisão (ex: `page.locator('.unidades-container input[type="checkbox"]').first()`).

### 12. Validação de Formatos de Data em Inputs HTML

Inputs HTML do tipo `date` esperam formato ISO (YYYY-MM-DD), não o formato brasileiro (DD/MM/YYYY) comumente usado na interface.

- **Solução:** Sempre use formato ISO (YYYY-MM-DD) ao preencher campos de data em testes, mesmo que a interface exiba o formato localizado. O Playwright interage diretamente com o elemento HTML, não com a representação visual.

### 13. Implementação de Funcionalidades Faltantes Durante os Testes

Testes podem revelar que funcionalidades esperadas (como edição ou remoção de processos) não estão implementadas na aplicação.

- **Solução:** Quando testes falharem por funcionalidades ausentes, implemente a funcionalidade mínima necessária na aplicação (componente Vue, método na store, etc.) em vez de modificar o teste. Isso garante que a aplicação atenda aos requisitos especificados nos testes.

### 14. Ambiguidade de Seletores com Múltiplos Elementos Similares

Quando há múltiplos elementos com o mesmo texto ou role (ex: dois botões "Remover"), o Playwright pode falhar com `strict mode violation`.

- **Solução:** Use seletores mais específicos baseados no contexto (ex: `.modal.show .btn-danger` para o botão dentro do modal) ou combine seletores (ex: `page.locator('form').getByRole('button', { name: 'Remover' })` para o botão no formulário).

### 15. Expectativas de URL Devem Refletir o Comportamento Real da Aplicação

Testes podem falhar se as expectativas de navegação não corresponderem à lógica de negócio implementada na aplicação.

- **Solução:** Analise o código da aplicação (especialmente funções de navegação e roteamento) para entender o comportamento real antes de definir expectativas de URL nos testes. Por exemplo, processos "Criado" podem ir para `/processo/cadastro?idProcesso=X` em vez de `/processo/X`.