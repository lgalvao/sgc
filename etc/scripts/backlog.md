# Backlog de Evolução: Toolkit de Automação (etc/scripts)

Este documento registra ideias e melhorias futuras para o toolkit do SGC, após a conclusão das Fases 1 e 2 do Plano de
Evolução.

## 1. Refinamentos de Auto-Fix

- **Melhoria no `@Nullable` (Java):** Evoluir a heurística de `java-auditar-null.js` para analisar o fluxo de dados (
  data flow) simples, identificando variáveis que recebem retorno de métodos anotados com `@Nullable` ou resultados de
  `findById`.
- **Auto-Fix de Test-IDs Duplicados:** Implementar renomeação automática em `test-ids-duplicados.js`. Estratégia
  sugerida: prefixar o ID com o nome do componente (ex: `Cadastro-salvar-btn`) e oferecer um modo `--dry-run` com diff
  visual.
- **Ordenação de Textos:** Adicionar funcionalidade ao `mensagens-analisar.js --fix` para ordenar as chaves dentro de
  cada categoria no `textos.ts` em ordem alfabética.

## 2. Padronização e Infraestrutura

- **JSON Schema:** Definir um schema formal (JSON Schema) para as saídas estruturadas de todos os scripts. Isso
  garantirá que o Dashboard de QA e outros consumidores tenham um contrato estável.
- **Refinamento de UX de Erro:** Implementar um tratador de erros global em `sgc.js` que formate stack traces de forma
  limpa e sugira soluções baseadas no código de erro ou contexto (similar ao que o `projeto doctor` faz).
- **Zod para Parsing de Args:** Substituir os loops manuais de `parseArgs` por uma biblioteca leve de validação de
  schema (como `zod` ou `commander` avançado) para garantir tipos corretos nos argumentos.

## 3. Novas Auditorias

- **Auditoria de Performance E2E:** Script para analisar os tempos de execução do Playwright em `test-results` e
  identificar testes "flaky" ou gargalos de navegação.
- **Sincronia de Enums:** Auditoria para garantir que Enums de domínio no Backend tenham uma representação de tipo
  correspondente (ou objeto de constantes) no Frontend.

## 4. Integração de QA

- **Snapshot Incremental:** Otimizar `qa snapshot coletar` para executar apenas as auditorias afetadas pelos arquivos
  alterados no commit atual (usando `git diff`).
- **Trend Analysis CLI:** Novo comando `sgc qa tendências` para comparar o snapshot atual com o anterior via terminal,
  destacando melhoras/pioras sem precisar abrir o Dashboard.
