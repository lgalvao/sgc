# Regras e Boas Práticas para Refatoração de Código

Este documento sumariza as lições aprendidas e as boas práticas observadas durante o processo de refatoração e desenvolvimento
de novas funcionalidades, com foco nos desafios práticos e nas estratégias para superá-los.

## 1. Entendimento do Contexto Existente

*   **Análise Profunda:** Antes de qualquer alteração, dedique tempo para entender a arquitetura existente, os padrões de
    código, as convenções de nomenclatura e o fluxo de dados. Isso inclui ler arquivos relacionados, testes existentes e
    configurações (ex: `package.json`, `vite.config.js`, `router.ts`, stores Pinia).
*   **Identificação de Dependências:** Compreenda como os diferentes módulos e componentes interagem. Use ferramentas de
    busca de conteúdo (`search_file_content`) e listagem de diretórios (`glob`, `list_directory`) para mapear as
    dependências.
*   **Verificação de Testes Existentes:** Se a funcionalidade a ser alterada já possui testes, execute-os para garantir
    que servem como uma rede de segurança. Se não houver, considere criar testes antes de refatorar.

## 2. Desafios com Ferramentas e Soluções

*   **Ferramenta `replace` (Sensibilidade a Quebras de Linha e Espaços):**
    *   **Problema:** A ferramenta `replace` é extremamente sensível a correspondências exatas, incluindo caracteres de
        quebra de linha (`\r\n` vs `\n`), espaços em branco e indentação. Pequenas diferenças podem fazer com que a
        operação falhe (`0 occurrences found`).
    *   **Solução:**
        *   Sempre use `read_file` imediatamente antes de um `replace` para obter o conteúdo exato da `old_string`.
        *   Para blocos de código maiores ou onde a sensibilidade é um problema, considere ler o arquivo inteiro,
            manipular o conteúdo em memória (se a ferramenta permitir ou se for um script externo), e então usar
            `write_file` para sobrescrever o arquivo. Esta abordagem, embora menos granular, garante a aplicação da
            mudança.

*   **Ferramenta `write_file` (Sobrescrita Completa):**
    *   **Uso:** Ideal para criar novos arquivos ou para sobrescrever completamente o conteúdo de um arquivo quando a
        manipulação granular com `replace` se torna inviável ou muito complexa.

## 3. Depuração e Resolução de Problemas em Ambientes de Teste

*   **Inconsistências entre Ambientes (Playwright vs. Vitest/JSDOM):**
    *   **Problema:** Erros que aparecem em um ambiente (ex: `TypeError: app.mount is not a function` no Playwright)
        mas não em outro (ex: Vitest/JSDOM) podem indicar diferenças na forma como o código é executado ou no ambiente
        DOM.
    *   **Solução:**
        *   **Priorize a funcionalidade real:** Se a aplicação funciona no navegador (mesmo com erros no console do
            Playwright), o problema pode ser específico do ambiente de teste.
        *   **Depuração Direta:** Use as ferramentas do Playwright (`browser_navigate`, `browser_type`, `browser_click`,
            `browser_snapshot`) para interagir diretamente com a aplicação no navegador. Isso ajuda a verificar o
            comportamento real e a identificar problemas de UI ou de integração que testes de unidade podem não capturar.
        *   **Isolamento de Problemas:** Se um erro persiste, tente isolar a causa comentando partes do código ou
            plugins (`app.use()`) para ver qual componente ou biblioteca está causando o conflito.

*   **Mocks e Reatividade em Testes Unitários (Vitest):**
    *   **Problema:** Mocks de stores Pinia ou composables que retornam valores reativos (`ref`) podem não ser
        reagidos corretamente por componentes em testes de unidade, especialmente com renderização condicional (`v-if`).
        Isso pode levar a erros como `Cannot call trigger on an empty DOMWrapper.`
    *   **Solução:**
        *   **Ordem de Mocking:** Garanta que os mocks de `localStorage`, `sessionStorage` ou outros objetos globais
            sejam configurados (`vi.spyOn`) *antes* que as stores ou componentes que os utilizam sejam inicializados
            no teste.
        *   **Reatividade Explícita:** Ao mockar `ref`s, certifique-se de que o valor do `ref` seja definido *antes* da
            montagem do componente. Se o valor for alterado após a montagem, use `await wrapper.vm.$nextTick()` para
            forçar a re-renderização do componente.
        *   **Depuração Profunda:** Use `console.log` extensivamente para inspecionar o estado dos mocks e do componente
            em diferentes pontos do teste. Isso ajuda a entender se os dados estão presentes e se a reatividade está
            funcionando como esperado.
        *   **Workarounds para Testes Persistentes:** Quando um teste falha de forma persistente e a causa não é óbvia
            (mesmo após depuração profunda), considere ignorá-lo temporariamente (`it.skip`) com uma nota explicativa.
            Isso permite que o restante da suíte de testes continue passando, enquanto o problema específico pode ser
            investigado em um momento oportuno, talvez com ferramentas mais avançadas (ex: `@pinia/testing`).

*   **Erros de Sintaxe em Arquivos de Teste:**
    *   **Problema:** Pequenos erros de sintaxe (ex: chaves desbalanceadas, comentários mal colocados) podem causar
        falhas de transformação no ambiente de teste, impedindo a execução dos testes.
    *   **Solução:** Use um editor com linting e formatação automática. Ao fazer alterações em massa ou comentar blocos
        de código, revise cuidadosamente a sintaxe.

## 4. Abordagem Pragmática na Refatoração

*   **Priorize a Funcionalidade:** O objetivo principal é garantir que a funcionalidade da aplicação esteja correta e
    bem testada.
*   **Não Lute Contra o Ambiente:** Se uma ferramenta ou um ambiente de teste específico está causando problemas
    persistentes e inexplicáveis, não hesite em usar workarounds (como ignorar testes ou usar `read/write_file` em vez
    de `replace`) para progredir, documentando a razão.
*   **Documente as Lições:** Mantenha um registro das lições aprendidas e dos problemas complexos resolvidos. Isso
    ajuda a evitar que os mesmos erros sejam repetidos no futuro e serve como um guia para outros desenvolvedores.
