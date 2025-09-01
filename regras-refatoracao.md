# Guia de Refatoração e Desenvolvimento

Este é um documento vivo que sumariza as lições aprendidas e as boas práticas observadas durante o processo de desenvolvimento, com foco em estratégias para modificação de código, depuração e resolução de problemas.

## 1. Entendimento do Contexto Existente

*   **Análise Profunda:** Antes de qualquer alteração, dedique tempo para entender a arquitetura existente, os padrões de código, as convenções de nomenclatura e o fluxo de dados. Isso inclui ler arquivos relacionados, testes e configurações.
*   **Mapeamento de Dependências:** Compreenda como os diferentes módulos e componentes interagem. Use ferramentas como `glob` e `search_file_content` para mapear as dependências e encontrar onde as funções ou componentes são utilizados.
*   **Rede de Segurança com Testes:** Se a funcionalidade a ser alterada já possui testes, execute-os para garantir que estão passando e que servirão como uma rede de segurança. Se não houver testes, considere criar testes de alto nível antes de refatorar.

## 2. Estratégias de Modificação de Código

### A Estratégia Mais Segura: Ler, Modificar, Escrever

Para modificações complexas, múltiplas ou quando a sensibilidade do `replace` é um problema, a abordagem mais robusta e recomendada é:

1.  **Ler o arquivo inteiro** com `read_file`.
2.  **Fazer as modificações no conteúdo em memória** (usando métodos de string ou manipulação de código).
3.  **Escrever o conteúdo modificado de volta** com `write_file`, sobrescrevendo o arquivo original.

### Ferramenta `replace` (Para Alterações Cirúrgicas)

A ferramenta `replace` é poderosa para alterações granulares, mas exige precisão absoluta. Use-a quando a mudança for pequena e bem definida.

*   **Precisão Extrema:** O argumento `old_string` deve ser **exatamente** igual ao texto no arquivo, incluindo quebras de linha, espaços e indentação.
*   **Obtenha o Conteúdo Real:** Sempre use `read_file` imediatamente antes de um `replace` para copiar o `old_string` diretamente do arquivo.
*   **Contexto é fundamental:** Inclua contexto suficiente (pelo menos 3 linhas antes e depois) no `old_string` para garantir que a correspondência seja única.
*   **Múltiplas Substituições:** Se precisar substituir múltiplas ocorrências, o valor de `expected_replacements` deve ser **exato**.

## 3. Verificação e Validação

Após cada alteração significativa, execute os seguintes comandos nesta ordem para garantir a qualidade e a correção do código.

1.  **Linting (`npm run lint`):** Verifica o estilo do código e captura erros comuns. É a verificação mais rápida e deve ser a primeira.
2.  **Verificação de Tipos (`npm run typecheck`):** Analisa o código TypeScript em busca de erros de tipo. Erros em arquivos de teste (`.spec.ts`) muitas vezes indicam que os *mocks* precisam ser atualizados para refletir as mudanças no código de produção.
3.  **Testes Unitários (`npm run test`):** Executa a suíte de testes para validar a lógica da aplicação. Para agilizar, você pode rodar testes para um arquivo específico: `npm run test -- nome-do-arquivo.spec.ts`.

## 4. Fluxo de Trabalho de Refatoração Recomendado

1.  **Compreensão:** Inicie com a análise descrita na Seção 1.
2.  **Implementação Cautelosa:** Aplique uma pequena e lógica alteração usando a estratégia da Seção 2.
3.  **Verificação Contínua:** Execute o ciclo de validação da Seção 3 (`lint`, `typecheck`, `test`). Corrija os problemas antes de prosseguir.
4.  **Repetição:** Volte ao passo 2 e continue fazendo pequenas alterações e verificações até que a refatoração esteja completa.
5.  **Commit Atômico:** Agrupe as alterações em um commit lógico e bem descrito. Use `git status` e `git diff HEAD` para revisar tudo antes de comitar. Uma boa mensagem de commit explica o "porquê" da mudança, não apenas o "o quê".

## 5. Princípios e Mentalidade

*   **Seja Pragmático:** Não hesite em usar a abordagem "Ler, Modificar, Escrever" em vez de insistir em um `replace` que não funciona. O importante é progredir de forma segura.
*   **Reconheça e Adapte-se:** Se uma estratégia não está funcionando, pare, analise o motivo e mude a abordagem.
*   **Comunicação:** Se estiver em dúvida sobre o comportamento esperado ou a melhor forma de implementar algo, confirme o entendimento com o usuário.