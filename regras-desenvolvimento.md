# Regras e Boas Práticas para Refatoração e Desenvolvimento

Este documento sumariza as lições aprendidas e as boas práticas observadas durante o processo de desenvolvimento, com foco em estratégias para modificação de código, depuração e resolução de problemas.

## 1. Entendimento do Contexto Existente

*   **Análise Profunda:** Antes de qualquer alteração, dedique tempo para entender a arquitetura existente, os padrões de código, as convenções de nomenclatura e o fluxo de dados. Isso inclui ler arquivos relacionados, testes existentes e configurações.
*   **Identificação de Dependências:** Compreenda como os diferentes módulos e componentes interagem. Use ferramentas de busca e listagem de diretórios para mapear as dependências.
*   **Verificação de Testes Existentes:** Se a funcionalidade a ser alterada já possui testes, execute-os para garantir que servem como uma rede de segurança. Se não houver, considere criar testes antes de refatorar.

## 2. Estratégias de Modificação de Código

### Ferramenta `replace`

A ferramenta `replace` é poderosa para alterações granulares, mas exige precisão absoluta.

*   **Precisão Extrema:** O argumento `old_string` deve ser **exatamente** igual ao texto no arquivo, incluindo quebras de linha (`\r\n` vs `\n`), espaços e indentação.
*   **Obtenha o Conteúdo Real:** Sempre use `read_file` imediatamente antes de um `replace` para copiar o `old_string` diretamente do arquivo.
*   **Contexto é Chave:** Inclua contexto suficiente (pelo menos 3 linhas antes e depois) no `old_string` para garantir que a correspondência seja única.
*   **Múltiplas Substituições:** Se precisar substituir múltiplas ocorrências, o valor de `expected_replacements` deve ser **exato**.

### Ferramenta `write_file`

*   **Uso Ideal:** Perfeita para criar novos arquivos ou para sobrescrever completamente um arquivo quando a manipulação com `replace` se torna inviável.

### Abordagem Robusta: Ler, Modificar, Escrever

Quando um `replace` falha repetidamente (erro "0 occurrences found"), não insista. A estratégia mais robusta é:
1.  **Ler o arquivo inteiro** com `read_file`.
2.  **Fazer as modificações no conteúdo em memória** (usando métodos de string).
3.  **Escrever o conteúdo modificado de volta** com `write_file`.

Esta abordagem é a mais recomendada para modificações complexas, múltiplas ou quando a sensibilidade do `replace` é um problema.

## 3. Depuração e Testes

### Verificação de Tipos (`typecheck`)

*   **Ponto de Partida:** Sempre comece com `npm run typecheck` após mudanças significativas.
*   **Análise de Erros:** Analise os erros de cima para baixo. Erros em arquivos de teste (`.spec.ts`) geralmente indicam que os mocks precisam ser atualizados para refletir as mudanças no código de produção.

### Testes Unitários (Vitest/JSDOM)

*   **Mocks e Reatividade:**
    *   **Problema:** Mocks de stores Pinia ou `ref`s podem não ser reativos em testes, causando erros como `Cannot call trigger on an empty DOMWrapper.`
    *   **Solução:**
        *   **Ordem de Mocking:** Configure mocks de `localStorage` ou outros objetos globais *antes* de inicializar as stores que dependem deles.
        *   **Reatividade Explícita:** Defina o valor de `ref`s mockados *antes* de montar o componente. Use `await wrapper.vm.$nextTick()` para forçar a re-renderização após mudanças.
*   **Depuração:** Use `console.log` para inspecionar o estado dos mocks e do componente durante o teste.

### Testes End-to-End (Playwright)

*   **Inconsistências de Ambiente:** Erros que ocorrem apenas no Playwright (e não no Vitest ou no navegador) podem indicar problemas específicos do ambiente de teste.
*   **Depuração Direta:** Use as ferramentas do Playwright (`browser_navigate`, `browser_snapshot`) para interagir com a aplicação e verificar o comportamento real da UI.

## 4. Abordagem e Mentalidade

*   **Priorize a Funcionalidade:** O objetivo principal é garantir que a aplicação funcione corretamente.
*   **Seja Pragmático:** Não hesite em usar workarounds (como a abordagem "Ler, Modificar, Escrever" em vez de `replace`) para progredir, documentando a decisão.
*   **Reconheça e Adapte-se:** Se uma estratégia não está funcionando, mude a abordagem.
*   **Documente Lições:** Mantenha um registro dos problemas resolvidos para referência futura.
*   **Comunicação:** Se estiver em dúvida, peça ajuda ou confirme o entendimento com o usuário.
