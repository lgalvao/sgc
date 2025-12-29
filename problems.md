# Relatório de Problemas Conhecidos - Verificação de Qualidade

## Status Geral
A verificação de qualidade foi realizada em todo o sistema. A maioria dos componentes está em conformidade, mas persistem problemas na execução dos testes End-to-End (E2E).

## 1. Backend
- **Status:** ✅ Passou
- Todos os testes unitários e de integração passaram (`./gradlew :backend:test`).
- Build bem-sucedido.

## 2. Frontend
- **Status:** ✅ Passou
- **Lint:** Sem erros (corrigidos problemas de acessibilidade em `TreeRowItem.vue` e `VisMapa.vue`).
- **Typecheck:** Sem erros.
- **Testes Unitários:** Todos passando (`vitest`).
- **Correções Realizadas:**
  - Importação de componentes `BDropdown` e `BDropdownItem` em `CadAtividades.vue` para resolver warnings do Vue.
  - Ajustes de acessibilidade (listeners de teclado e labels) em componentes diversos.

## 3. Testes End-to-End (E2E)
- **Status:** ⚠️ Falhas Residuais
- **Problema Principal:** Interação com elementos de UI complexos (Dropdowns e Toasts).
  - Testes do **CDU-13** falham ao tentar clicar em "Histórico de análise" dentro do dropdown "Mais ações". O Playwright não consegue interagir com o item do menu, possivelmente devido à animação ou estrutura do BootstrapVueNext.
  - Testes de Logout apresentavam falhas intermitentes devido a Toasts cobrindo o botão de sair. Foi implementado um workaround no helper `fazerLogout` para remover esses elementos do DOM, o que mitigou parte dos erros, mas a estabilidade total ainda não foi alcançada.
- **Causa Raiz:** A automação E2E precisa de refinamento para lidar melhor com a assincronicidade e visibilidade de elementos flutuantes (popups, dropdowns) do framework de UI utilizado.

## Recomendações Futuras
1. Adicionar `data-testid` explícitos nos gatilhos de dropdowns (o botão principal) para facilitar a seleção nos testes.
2. Revisar a estratégia de espera nos helpers E2E para garantir que animações de abertura de menu sejam concluídas antes da interação.
3. Investigar timeouts em testes de visibilidade (`toBeVisible`) que podem indicar lentidão na renderização em ambiente de teste.
