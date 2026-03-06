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

### Falhas ainda em aberto no `smoke`

Na última execução completa registrada em `/tmp/e2e-smoke-full-3.txt`, ainda há falhas nas seções:

1. `02 - Painel Principal › Captura painel GESTOR`
2. `02 - Painel Principal › Captura painel CHEFE`

Diagnóstico provável:

- ambos usam `criarProcesso(..., iniciar: true)` seguido de troca de usuário quase imediata;
- o helper [e2e/helpers/helpers-processos.ts](/Users/leonardo/sgc/e2e/helpers/helpers-processos.ts) espera `waitForURL('/painel')`, mas isso não está garantindo, sozinho, que o fluxo de início do processo terminou de se estabilizar para o próximo usuário;
- o efeito observado é o mesmo padrão de corrida já corrigido na seção 05.

### Execuções relevantes já feitas

- `smoke` autenticação:
  - verde após filtro de ruído de auth.
- `smoke` seção de relatórios:
  - verde.
- `captura-telas` seção de relatórios:
  - verde.
- `smoke` seção 05 isolada:
  - **1 passed (18.4s)** em `/tmp/e2e-smoke-mapa-2.txt`
- `smoke` completo mais recente:
  - ainda com falhas nas seções 02 (gestor e chefe) em `/tmp/e2e-smoke-full-3.txt`

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

### Ainda falta concluir

1. Fechar o `smoke.spec.ts` completamente verde.
2. Remover as corridas restantes nos cenários de painel GESTOR/CHEFE.
3. Revisar `criarProcesso(..., iniciar: true)` para garantir pós-condição mais forte.
4. Continuar removendo `waitForTimeout()` substituível do `smoke`.
5. Aplicar o mesmo padrão de limpeza ao `captura-telas.spec.ts`.

## Fase 2 - Robustez de Helpers e Fixtures

### Oportunidades já identificadas

1. Fortalecer o helper [e2e/helpers/helpers-processos.ts](/Users/leonardo/sgc/e2e/helpers/helpers-processos.ts):
   - após `iniciar: true`, garantir estado final observável antes de devolver o controle.

2. Extrair helpers compartilhados de fixtures hoje duplicados em `captura-telas`.

3. Continuar trocando preparo profundo por fixture de backend quando:
   - o objetivo do teste não é validar todo o workflow pela UI;
   - o custo de setup estiver empurrando o teste para o limite de `20s`.

## Fase 3 - Limpeza Estrutural do `captura-telas`

### Situação

O arquivo ainda concentra muitos `waitForTimeout()` válidos para estabilização visual, mas também carrega vários waits que são apenas sincronização pobre.

### Próximos alvos concretos

1. Trocar `btn-logout` cru por `fazerLogout`.
2. Garantir `expect(page).toHaveURL(/\/painel/)` após cada `btn-iniciar-processo-confirmar` que antecede troca de usuário.
3. Trocar waits de modal por `expect(dialog).toBeVisible()`.
4. Reduzir waits de 300/500 ms onde a prontidão do DOM já pode ser observada diretamente.

## Riscos e Cuidados

- Não aumentar timeout para mascarar defeito semântico.
- Não usar assertiva vaga com "ou" para esconder estado indefinido.
- Não silenciar logs reais só para a saída ficar bonita.
- Não assumir que um `waitForURL('/painel')` sempre basta quando o teste vai trocar de usuário imediatamente depois.

## Próximos Passos Recomendados

1. Corrigir no `smoke` as seções 02 (GESTOR e CHEFE), provavelmente reforçando o helper `criarProcesso` para o caso `iniciar: true`.
2. Rerodar o `smoke` completo até fechar verde.
3. Aplicar o mesmo padrão de correção no `captura-telas`:
   - pós-condição de `iniciar processo`;
   - logout via helper;
   - remoção adicional de waits cegos.
4. Extrair helpers compartilhados para fixtures de backend hoje duplicadas.
