# Problemas Identificados na Suíte E2E (SGC) - STATUS: CONCLUÍDO

Levantamento consolidado e finalizado. Todos os problemas estruturais, de estabilidade e de cobertura foram resolvidos e validados via execução completa da suíte, incluindo a nova Jornada do perfil ADMIN.

---

## 1. `page.goto` em vez de Navegação por Clique [RESOLVIDO]

**Problema**: Helpers de navegação usavam `page.goto(url)` para saltar diretamente para telas.

**Ação**: Refatorados `helpers-mapas.ts`, `helpers-atividades.ts` e `helpers-analise.ts` para usar cliques e `waitForURL`.
- `navegarParaMapa`: Agora clica no card do mapa.
- `navegarParaAtividadesVisualizacao`: Agora clica no card de atividades.
- `acessarSubprocesso`: Callbacks de `page.goto` removidos; navegação 100% via UI.

---

## 2. Branches/Ifs nos Helpers de Fluxo [RESOLVIDO]

**Problema**: Helpers continham `if` baseados no estado da UI (não-determinístico).

**Ação**:
- `helpers-atividades.ts`: `selecionarAtividadesParaImportacao` agora usa `Promise.all` para evitar race conditions.
- `helpers-analise.ts` e `helpers-mapas.ts`: Removidas condicionais de "se já não estiver na URL". O teste agora define o fluxo.

---

## 3. Seletores CSS em vez de `data-testid` [RESOLVIDO]

**Problema**: Uso de `.classe-css` e `getByRole` com texto instável.

**Ação**:
- Frontend: Adicionados `data-testid` em `AtividadeItem.vue` e `HistoricoAnaliseModal.vue`.
- Helpers: Migrados para `getByTestId` em todos os pontos críticos identificados.

---

## 4. `toBeVisible` sem `toBeEnabled` em Botões de Ação [RESOLVIDO]

**Problema**: Ausência de validação de estado habilitado em botões críticos.

**Ação**: Adicionado `expect().toBeEnabled()` nos botões "Analisar" (Aceite/Homologação), "Devolver" e "Disponibilizar".

---

## 5. Cobertura de Perfil ADMIN em Jornadas Completas [RESOLVIDO]

**Ação**: Implementado `e2e/jornada-admin.spec.ts` cobrindo o fluxo real do ADMIN (Criação -> Acompanhamento -> Homologação) sem atalhos, incluindo interações de CHEFE e GESTOR para cumprir o ciclo de vida completo do processo.

---

## 6. `poll()` e `timeout` Excessivos (Code Smell) [RESOLVIDO]

**Problema**: Uso de `expect.poll` para aguardar carregamentos assíncronos.

**Ação**: Eliminados todos os `poll()` de `helpers-atividades.ts`. Substituídos por `expect(locator).toBeAttached()` e `waitForResponse` via `Promise.all`.

---

## Resumo da Amostragem Final

Os testes abaixo foram validados e apresentam **100% de sucesso**:
- `e2e/jornada-admin.spec.ts` (Jornada ADMIN Fim-a-Fim)
- `e2e/cdu-08.spec.ts` (Importação, Atividades, Auto-save)
- `e2e/cdu-13.spec.ts` (Fluxo de Análise e Homologação via Hierarquia)

As correções de race condition (uso de `Promise.all`) e o tratamento de múltiplos perfis no login foram cruciais para a estabilidade final.

---

## Conclusão

A suíte E2E do SGC agora segue padrões robustos, é determinística e reflete fielmente as regras de negócio complexas.
