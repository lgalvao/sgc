# Plano de Trabalho — Adição de Test-IDs e Solidificação dos E2E

**Status: Em andamento. A refatoração da camada de helpers foi concluída, e a análise para o Grupo 2 foi iniciada.**

## Objetivo

Tornar os testes E2E mais resilientes adicionando `data-testid` onde for necessário, atualizando as constantes de testes e utilizando uma arquitetura de helpers robusta e semântica.

## Escopo

- Inserir `data-testid` em componentes e views, seguindo a ordem de prioridade.
- Atualizar `e2e/cdu/helpers/dados/constantes-teste.ts` com as novas chaves.
- Garantir que os helpers da Camada 2 utilizem a nova arquitetura de utilitários (`localizarElemento`, `clicarElemento`, `preencherCampo`).
- Validar todas as mudanças com a suíte de testes para garantir ausência de regressões.

## Prioridade (Ordem de Execução)

1.  **[Concluído]** Modais críticos (Confirmar / Cancelar / Fechar).
2.  **[Em Análise]** Botões de ação de alto impacto (Disponibilizar, Registrar aceite, Devolver, Homologar, Validar).
3.  Inputs de observação em modais (devolução/validação).
4.  Botões CRUD em cards de competência/atividade (Criar/Editar/Excluir).
5.  Botões do painel/processos (Criar processo, Iniciar, Finalizar, Aceitar em bloco).
6.  Títulos e contêineres úteis (`subprocesso-header`, `processo-info`, `mapa-card`).

## Resumo do Trabalho Realizado

- **Grupo 1 (Modais Críticos):** `data-testid`s foram adicionados aos componentes de modal no código-fonte.
- **Refatoração da Camada de Helpers:** Uma refatoração completa da camada de ações (`e2e/cdu/helpers/acoes/`) foi executada. 
  - Os utilitários iniciais (`localizarPorTestIdOuRole`) foram substituídos por um conjunto mais poderoso em `e2e/cdu/helpers/utils/refactoring-utils.ts` (`localizarElemento`, `clicarElemento`, `preencherCampo`).
  - Todos os principais arquivos de ações (`acoes-atividades.ts`, `acoes-modais.ts`, `acoes-processo.ts`) agora usam esses novos utilitários, eliminando código repetitivo e complexidade.

## Checklist de Status

- [x] `data-testid` adicionados nos componentes do **Grupo 1** (modais críticos).
- [x] Constantes correspondentes adicionadas em `constantes-teste.ts`.
- [x] Helpers utilitários avançados (`localizarElemento`, etc.) criados em `refactoring-utils.ts`.
- [x] Helpers de ações (`acoes-*.ts`) refatorados para usar os novos utilitários.
- [ ] Execução da suíte de testes completa para validar a refatoração.
- [ ] Análise e implementação dos `data-testid`s para o **Grupo 2**.

## Próximos Passos

1.  **Analisar Código-Fonte (Grupo 2):** Identificar os arquivos `.vue` e as linhas exatas onde os botões de ação de alto impacto (Disponibilizar, Registrar aceite, Devolver, etc.) estão localizados para adicionar `data-testid`s.
2.  **Implementar `data-testid`s:** Adicionar os atributos aos componentes Vue identificados.
3.  **Atualizar Constantes:** Adicionar os novos `data-testid`s ao arquivo `e2e/cdu/helpers/dados/constantes-teste.ts`.
4.  **Validar:** Executar a suíte de testes completa (`npx playwright test`) para garantir que as mudanças não introduziram regressões e que os novos seletores são utilizados corretamente.
5.  **Abrir PR:** Criar um Pull Request com as alterações no código-fonte e nos testes, referenciando este plano.
