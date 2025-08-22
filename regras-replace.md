# Regras para o uso da ferramenta `replace`

## 1. Precisão Extrema do `old_string`:
*   O `old_string` deve ser **exatamente** igual ao texto no arquivo, incluindo todos os espaços em branco, quebras de linha, indentação e caracteres ocultos. Qualquer diferença mínima resultará em falha (`0 occurrences found`).
*   Sempre use `read_file` imediatamente antes de um `replace` para copiar o `old_string` diretamente do arquivo, garantindo a precisão.
*   Inclua contexto suficiente (pelo menos 3 linhas antes e depois) no `old_string` para garantir que ele seja único no arquivo, especialmente em arquivos grandes ou com código repetitivo.

## 2. `expected_replacements`:
*   Se você espera substituir apenas uma ocorrência, não especifique `expected_replacements` (o padrão é 1).
*   Se você espera substituir múltiplas ocorrências, o valor de `expected_replacements` deve ser **exato**. Uma contagem incorreta resultará em falha.
*   A ferramenta `replace` não suporta flags de regex (como `g` para substituição global). Se você precisa de substituições globais ou baseadas em regex, a abordagem de "ler, modificar em memória, escrever" é mais adequada.

## 3. Evitar Loops de "Failed to Edit":
*   Quando um `replace` falha repetidamente com "Failed to edit, 0 occurrences found", isso indica que o `old_string` não está sendo encontrado.
*   **Não persista** em tentar o mesmo `replace` com pequenas variações. Isso leva a loops improdutivos.
*   A melhor estratégia é:
    *   **Re-ler o arquivo (`read_file`)** para obter o estado atual e o `old_string` exato.
    *   **Reavaliar a estratégia:** Se o `replace` com contexto não funcionar, considere a abordagem de "ler o arquivo inteiro, fazer as modificações em memória (usando métodos de string do Python como `replace()`), e depois escrever o conteúdo modificado de volta no arquivo (`write_file`)". Esta é a abordagem mais robusta para modificações complexas ou múltiplas.

# Lidando com Erros de `typecheck` e Testes:

## 1. Análise de Erros de `typecheck`:
*   Sempre comece com `npm run typecheck` (ou equivalente) após mudanças significativas.
*   Analise os erros de cima para baixo, pois erros iniciais podem causar uma cascata de outros erros.
*   Preste atenção aos arquivos e linhas indicadas.
*   Erros em arquivos de teste (`.spec.ts`) geralmente indicam que os mocks ou dados de teste precisam ser atualizados para refletir as mudanças nas interfaces ou enums do código de produção.

## 2. Execução de Testes:
*   Após resolver os erros de `typecheck`, execute os testes (`npx vitest` ou `npm test`).
*   Verifique se todos os testes passam. Testes falhos indicam regressões ou que a nova funcionalidade não está se comportando como esperado.
*   Esteja ciente de que `stderr` em testes pode conter mensagens informativas (como validações de erro esperadas) e não necessariamente indicam uma falha no teste.

# Comunicação e Resiliência:

*   **Reconhecer e Corrigir Erros:** É crucial reconhecer quando uma estratégia não está funcionando (como o loop de `replace`) e mudar a abordagem.
*   **Pedir Ajuda/Confirmar:** Se estiver em dúvida ou preso, como aconteceu, é válido pedir ao usuário para reavaliar ou fornecer mais contexto.
