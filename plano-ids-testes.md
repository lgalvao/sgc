# Plano de Trabalho — Adição de Test-IDs e Solidificação dos E2E

Objetivo
- Tornar os testes E2E mais resilientes adicionando test-ids onde for necessário, atualizando as constantes de testes e ajustando helpers para usar selectors robustos.
- Fornecer um roteiro claro com etapas, responsáveis, critérios de aceite e rollback.

Escopo
- Inserir `data-testid` em componentes e views prioritários (modais, botões de ação críticos e campos de observação).
- Atualizar [`e2e/cdu/helpers/dados/constantes-teste.ts`](e2e/cdu/helpers/dados/constantes-teste.ts:1) com novas chaves.
- Ajustar helpers (Camada 2) para preferir test-ids e manter fallbacks por texto quando necessário.
- Reexecutar a suíte impactada e validar ausência de flakes.

Prioridade (ordem de execução)
1. Modais críticos (Confirmar / Cancelar / Fechar) em `src/components/*Modal.vue` e em `src/views/*` que abrem modais.
2. Botões de ação de alto impacto (Disponibilizar, Registrar aceite, Devolver, Homologar, Validar).
3. Inputs de observação em modais (devolução/validação).
4. Botões CRUD em cards de competência/atividade (Criar/Editar/Excluir).
5. Botões do painel/processos (Criar processo, Iniciar, Finalizar, Aceitar em bloco).
6. Títulos e contêineres úteis (`subprocesso-header`, `processo-info`, `mapa-card`).

Resumo do que já foi feito (status parcial — Grupo 1: Modais críticos)
- Escopo executado:
  - Adição de `data-testid` em modais críticos e botões principais.
  - Atualização de constantes de testes com novas chaves.
  - Implementação de helpers utilitários para priorizar test-ids com fallback (primeira onda).
  - Atualização de helpers de ações de modais para usar os utilitários com fallback.

- Arquivos modificados (principais):
  - Componentes UI (modais):
    - [`src/components/BaseModal.vue`](src/components/BaseModal.vue:32)
    - [`src/components/CriarCompetenciaModal.vue`](src/components/CriarCompetenciaModal.vue:31)
    - [`src/components/DisponibilizarMapaModal.vue`](src/components/DisponibilizarMapaModal.vue:15)
    - [`src/components/ImportarAtividadesModal.vue`](src/components/ImportarAtividadesModal.vue:26)
    - [`src/components/SistemaNotificacoesModal.vue`](src/components/SistemaNotificacoesModal.vue:15)
    - [`src/components/SubprocessoModal.vue`](src/components/SubprocessoModal.vue:25)
    - [`src/components/AcoesEmBlocoModal.vue`](src/components/AcoesEmBlocoModal.vue:15)
    - [`src/components/HistoricoAnaliseModal.vue`](src/components/HistoricoAnaliseModal.vue:20)
    - (modais já com test-ids mantidos: [`src/components/AceitarMapaModal.vue`](src/components/AceitarMapaModal.vue:1), [`src/components/ImpactoMapaModal.vue`](src/components/ImpactoMapaModal.vue:1))
  - Test helpers / constantes:
    - [`e2e/cdu/helpers/dados/constantes-teste.ts`](e2e/cdu/helpers/dados/constantes-teste.ts:1) — novas chaves adicionadas (`BTN_MODAL_FECHAR`, `INPUT_DATA_LIMITE`, `SELECT_PROCESSO`, etc.)
    - [`e2e/cdu/helpers/utils/utils.ts`](e2e/cdu/helpers/utils/utils.ts:1) — novos utilitários:
      - `localizarPorTestIdOuRole(...)`
      - `clicarPorTestIdOuRole(...)`
    - [`e2e/cdu/helpers/utils/index.ts`](e2e/cdu/helpers/utils/index.ts:1) — export dos utilitários.
    - [`e2e/cdu/helpers/acoes/acoes-modais.ts`](e2e/cdu/helpers/acoes/acoes-modais.ts:1) — funções de interação com modais atualizadas para priorizar test-ids com fallback.

- O que foi priorizado no trabalho:
  - Consistência de nomes (prefixos `btn-`, `input-`, `modal-`, `select-`).
  - Helpers que tentam `getByTestId` e, caso não encontrem, fazem fallback para `getByRole`, `getByText` ou `locator(...)`.
  - PRs pequenos: as mudanças foram aplicadas em pequenos commits por grupo (modais primeiro) para facilitar rollback.

Checklist atualizado (status atual)
- [x] `data-testid` adicionados nos componentes do Grupo 1 (modais críticos).
- [x] Constante correspondente adicionada em [`e2e/cdu/helpers/dados/constantes-teste.ts`](e2e/cdu/helpers/dados/constantes-teste.ts:1).
- [x] Helpers utilitários adicionados (`localizarPorTestIdOuRole`, `clicarPorTestIdOuRole`) e exportados.
- [x] Atualização inicial de helpers (ex.: [`e2e/cdu/helpers/acoes/acoes-modais.ts`](e2e/cdu/helpers/acoes/acoes-modais.ts:1)) para usar os utilitários com fallback.
- [ ] Testes locais rodando: `npx playwright test --last-failed` OK — pendente (recomenda-se execução local).
- [ ] Descrição do PR inclui lista de arquivos alterados e motivos — pendente (pronto para gerar).

Próximos passos recomendados
1. Executar a suíte de testes localmente para validar as mudanças:
   - npx playwright test --grep "área-alterada" --workers=1 --headed
   - npx playwright test --last-failed
   - npx playwright test (run completo para verificar regressões)
2. Propagar as mudanças dos utilitários para outros helpers (Camada 2):
   - Atualizar [`e2e/cdu/helpers/verificacoes/verificacoes-ui.ts`](e2e/cdu/helpers/verificacoes/verificacoes-ui.ts:1) para preferir `getByTestId(SELETORES.X)` onde aplicável.
   - Revisar [`e2e/cdu/helpers/navegacao/navegacao.ts`](e2e/cdu/helpers/navegacao/navegacao.ts:1) e demais ações para usar os utilitários.
3. Rodar a suíte 2x consecutivas (pipeline/CI ou local) para garantir que não haja flakes.
4. Abrir PR pequeno (Grupo 1 — Modais):
   - Incluir descrição clara, lista de arquivos alterados e instruções para QA (comandos de execução).
5. Caso surjam falhas intermitentes, aplicar mitigação conservadora:
   - Reverter PR se necessário.
   - Aplicar mudança apenas nos helpers e reavaliar.
6. Planejar próximo grupo (Botões de ação de alto impacto) e repetir o fluxo: aplicar test-ids, atualizar constantes, adaptar helpers, rodar testes, PR.

Notas de QA / Observações
- Priorizar execução local antes do PR para identificar eventuais seletores não cobertos.
- Os utilitários implementados mantêm comportamento tolerante (procuram test-id e usam fallbacks), reduzindo risco de regressões imediatas.
- Ao abrir PR, indicar explicitamente quais specs foram executados manualmente/visualmente.

Anexo: comandos úteis
- Rodar testes afetados (debug/headful):
  - npx playwright test --grep "área-alterada" --workers=1 --headed
- Rodar últimos falhos:
  - npx playwright test --last-failed
- Rodar tudo:
  - npx playwright test