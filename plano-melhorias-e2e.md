# Plano de Melhorias E2E

## Objetivo

Elevar a confiabilidade, previsibilidade e manutenibilidade da suíte E2E, reduzindo flakiness, ruído de logs e acoplamento desnecessário entre cenários.

## Estado Atual Consolidado

### Infraestrutura e paralelismo

- A arquitetura E2E foi ajustada para usar:
  - frontend único;
  - backend isolado por worker.
- O nível de paralelismo comprovado de forma estável hoje é **2 workers**.
- A mensagem de lifecycle foi simplificada para descrever a infra real, sem jargão de worker:
  - `>>> Infra E2E no ar. Frontend: 5173. Backend: 10000.`

### Documentação e regras

- O arquivo [etc/docs/regras-e2e.md](/Users/leonardo/sgc/etc/docs/regras-e2e.md) foi enriquecido com aprendizados práticos desta sessão:
  - quando depurar com `1 worker`;
  - como validar paralelismo depois;
  - uso correto de fixtures de backend;
  - distinção entre toast e alert;
  - antipadrões a evitar;
  - classificação de ruído vs falha real.

### Notificações e ruído de logs

- O listener central em [e2e/fixtures/base.ts](/Users/leonardo/sgc/e2e/fixtures/base.ts) foi ajustado para filtrar o ruído esperado de autenticação:
  - `401` de `/api/usuarios/autorizar` não é mais logado como erro relevante de navegador/rede.
- Validação feita:
  - `smoke` de autenticação continuou verde;
  - o ruído do browser `Failed to load resource: 401` para esse endpoint específico sumiu.
- O warning de backend para credencial inválida foi mantido, porque é comportamento real do cenário negativo.

### Smoke e captura de telas já corrigidos

- [e2e/smoke.spec.ts](/Users/leonardo/sgc/e2e/smoke.spec.ts)
  - seção de relatórios atualizada para a UI nova;
  - parte relevante de waits fixos removida e trocada por expectativas explícitas;
  - vários `btn-logout` crus substituídos por `fazerLogout`;
  - expectativas incorretas ajustadas:
    - gestão de unidades não abre modal em "Criar atribuição"; navega para a view de atribuição;
    - histórico agora valida `tbl-processos`, não `locator('table')`.
- [e2e/captura-telas.spec.ts](/Users/leonardo/sgc/e2e/captura-telas.spec.ts)
  - relatórios alinhados à UI nova;
  - trecho instável do fluxo intermediário do gestor em mapa removido temporariamente com `TODO`;
  - fixtures de backend já estão sendo usadas para reduzir preparação em trechos da seção 03.

### Fixtures de backend adicionadas

- Foram adicionados endpoints E2E para preparar estados mais profundos:
  - `POST /e2e/fixtures/processo-mapeamento-com-mapa-disponibilizado`
  - `POST /e2e/fixtures/processo-mapeamento-com-mapa-validado`
- Arquivos envolvidos:
  - [backend/src/main/java/sgc/e2e/E2eController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/e2e/E2eController.java)
  - [backend/src/test/java/sgc/e2e/E2eFixtureEndpointTest.java](/Users/leonardo/sgc/backend/src/test/java/sgc/e2e/E2eFixtureEndpointTest.java)

## Diagnóstico Atual

Os problemas mais relevantes restantes hoje não são de performance da máquina nem de timeout "verdadeiro". O padrão observado é:

- expectativa antiga de UI;
- ação que mudou de modal para navegação;
- corrida semântica após mutação (`iniciar`, `disponibilizar`, etc.);
- uso de `waitForTimeout()` para esconder sincronização incorreta;
- alguns cenários longos ainda acoplados demais à UI.

Em especial:

- quando o Playwright estoura `20s`, em vários casos o problema real é:
  - o teste entrou em estado errado;
  - a página/contexto foi encerrado no timeout global;
  - o stack termina mostrando `page.goto` como efeito colateral.

## Evidências Importantes Desta Sessão

### Falhas semânticas já identificadas e corrigidas

1. `smoke` seção 11:
   - "Criar atribuição" não abre `dialog`;
   - o comportamento correto é navegar para a página de atribuição temporária.

2. `smoke` seção 12:
   - `locator('table')` ficou ambíguo porque a tela expõe mais de uma tabela;
   - o seletor correto é `getByTestId('tbl-processos')`.

3. `smoke` seção 05:
   - o fluxo de mapa estava maior do que precisava por usar unidade com hierarquia mais profunda;
   - foi simplificado para `ASSESSORIA_12`, com apenas um gestor;
   - havia também corrida após `iniciar processo`, corrigida com espera explícita pelo retorno ao painel.

### Situação atual do `smoke`

As falhas anteriores nas seções de painel GESTOR/CHEFE foram eliminadas.

Correção aplicada:

- o helper [e2e/helpers/helpers-processos.ts](/Users/leonardo/sgc/e2e/helpers/helpers-processos.ts) deixou de depender apenas de `waitForURL('/painel')`;
- agora o fluxo de início espera uma pós-condição observável mais forte:
  - retorno ao painel;
  - presença da linha do processo;
  - situação `Em andamento` na tabela.

Também foi extraído um helper semântico para confirmar o modal de início quando a captura da tela do dialog ainda é necessária.

### Execuções relevantes já feitas

- `smoke` autenticação:
  - verde após filtro de ruído de auth.
- `smoke` seção de relatórios:
  - verde.
- `captura-telas` seção de relatórios:
  - verde.
- `smoke` seção 05 isolada:
  - **1 passed (18.4s)** em `/tmp/e2e-smoke-mapa-2.txt`
- `smoke` completo após reforço dos helpers:
  - **18/18 verde** em `/tmp/e2e-smoke-continue.txt`
- `captura-telas` completo após fixture homologada:
  - **21/21 verde** em `/tmp/e2e-captura-continue-2.txt`
- bloco `cdu-0*.spec.ts` após correções semânticas:
  - **43 passed** em `/tmp/e2e-bloco-cdu-0.txt`
- `lint` na raiz:
  - voltou a executar com `npm run lint` após restaurar dependências do `package.json`
  - a execução ainda expõe backlog antigo de warnings na suíte E2E (condicionais em testes, testes sem asserções e alguns usos de `force` em helpers legados)

## Fase 1 - Estabilização Imediata

### Já concluído

1. Redução de ruído esperado de autenticação no logger E2E.
2. Simplificação da mensagem de lifecycle.
3. Atualização dos testes de relatórios para a UI nova.
4. Substituição parcial de `waitForTimeout()` por expectativas explícitas no `smoke`.
5. Substituição parcial de `btn-logout` direto por `fazerLogout`.
6. Correção de expectativas erradas em:
   - gestão de unidades;
   - histórico;
   - fluxo de mapa com hierarquia desnecessariamente profunda.
7. `smoke.spec.ts` fechado completamente verde em `workers=1`.
8. `helpers-processos.ts` reforçado com helpers semânticos para:
   - iniciar processo com validação da linha no painel;
   - confirmar o modal de início sem repetir seletor cru nos testes.
9. `captura-telas.spec.ts` parcialmente limpo:
   - parte dos fluxos de início/logout migrada para helpers;
   - vários waits funcionais de modal e navegação trocados por expectativas explícitas.

### Ainda falta concluir

1. Continuar removendo `waitForTimeout()` substituível do `captura-telas.spec.ts`.
2. Validar `captura-telas.spec.ts` completo em `workers=1` e classificar falhas/ruídos restantes.
3. Endereçar o backlog estrutural indicado pelo lint:
   - `playwright/no-conditional-in-test`;
   - `playwright/expect-expect`;
   - `playwright/no-force-option` em helpers legados.

## Fase 2 - Robustez de Helpers e Fixtures

### Oportunidades já identificadas

1. Fortalecer o helper [e2e/helpers/helpers-processos.ts](/Users/leonardo/sgc/e2e/helpers/helpers-processos.ts):
   - após `iniciar: true`, garantir estado final observável antes de devolver o controle.
   - status: **concluído para o fluxo de início do processo no painel**.

2. Extrair helpers compartilhados de fixtures hoje duplicados em `captura-telas`.

3. Continuar trocando preparo profundo por fixture de backend quando:
   - o objetivo do teste não é validar todo o workflow pela UI;
   - o custo de setup estiver empurrando o teste para o limite de `20s`.
   - status: **aplicado em `cdu-08.spec.ts` para setup de mapeamento/revisão e processo base de importação**.
4. Considerar helpers semânticos adicionais para modais recorrentes em `captura-telas`:
   - análise de cadastro;
   - validação/homologação de mapa;
   - disponibilização de cadastro.

### Descobertas recentes na rodada por blocos

1. O texto estável pós-disponibilização de cadastro não é mais `Cadastro de atividades disponibilizado` na UI atual.
   - O frontend hoje publica o toast `Disponibilizado com sucesso.` antes de retornar ao painel.
   - Os testes do bloco `cdu-0` foram atualizados para validar o toast atual e/ou o estado final no painel, conforme `etc/docs/regras-e2e.md`.

2. `cdu-08.spec.ts` estourava o limite de `20s` por depender demais de setup via UI.
   - A correção foi migrar o preparo do processo de origem finalizado e dos processos alvo para fixtures backend.
   - Depois disso, o bloco `cdu-0` ficou verde sem adicionar timeout e sem `force`.

3. Em ações em bloco, as pré-condições documentadas em `etc/reqs` precisam prevalecer sobre suposições do teste.
   - Os CDUs de aceite/homologação em bloco devem espelhar o mesmo encadeamento de aceite/homologação usado em fluxos como `cdu-21`, `cdu-25` e `cdu-26`.
5. Manter fixtures alinhadas à UI real:
   - a fixture `mapa validado` não garante mais que o ADMIN verá ação de homologação no `MapaVisualizacaoView`;
   - para capturas de finalização, o estado útil agora é `mapa homologado`, com processo ainda `EM_ANDAMENTO`.

## Fase 3 - Limpeza Estrutural do `captura-telas`

### Situação

O arquivo ainda concentra muitos `waitForTimeout()` válidos para estabilização visual, mas também carrega vários waits que são apenas sincronização pobre.

### Próximos alvos concretos

1. Trocar `btn-logout` cru por `fazerLogout`.
2. Garantir `expect(page).toHaveURL(/\/painel/)` após cada `btn-iniciar-processo-confirmar` que antecede troca de usuário.
3. Trocar waits de modal por `expect(dialog).toBeVisible()`.
4. Reduzir waits de 300/500 ms onde a prontidão do DOM já pode ser observada diretamente.
5. Revisar waits de navegação/menu hoje usados apenas para estabilização visual e substituí-los por URL ou heading observável quando possível.
6. Separar explicitamente waits visuais legítimos de waits funcionais:
   - hover e scroll para screenshot podem permanecer;
   - transição de estado, modal e navegação não devem depender de timeout fixo.

## Riscos e Cuidados

- Não aumentar timeout para mascarar defeito semântico.
- Não usar assertiva vaga com "ou" para esconder estado indefinido.
- Não silenciar logs reais só para a saída ficar bonita.
- Não assumir que um `waitForURL('/painel')` sempre basta quando o teste vai trocar de usuário imediatamente depois.

## Próximos Passos Recomendados

1. Rerodar e estabilizar `captura-telas.spec.ts` completo em `workers=1`.
2. Aplicar o mesmo padrão de correção no `captura-telas`:
   - pós-condição de `iniciar processo`;
   - logout via helper;
   - remoção adicional de waits cegos.
3. Extrair helpers compartilhados para fixtures de backend hoje duplicadas.
4. Planejar uma fase dedicada de saneamento dos warnings do lint na suíte E2E, priorizando:
   - remoção de condicionais em testes;
   - reforço de asserções em cenários hoje muito “fotográficos”;
   - eliminação de `force` em helpers legados.
