# Relatório de Testes Automatizados

**Data:** 20/01/2026, 13:26:47
**Sistema:** Windows_NT 10.0.26100

## Resumo Executivo

| Teste | Status | Duração (s) |
| :--- | :---: | :---: |
| Backend - Testes Unitários | ✅ Sucesso | 60.91s |
| Backend - Testes de Integração | ✅ Sucesso | 116.83s |
| Frontend - Testes Unitários | ❌ Falha | 69.83s |
| E2E - Playwright | ❌ Falha | 528.05s |

**Status Geral:** 🔴 REPROVADO

## Detalhes da Execução

### Backend - Testes Unitários

- **Comando:** `gradlew.bat unitTest`
- **Diretório:** `backend`
- **Status:** ✅ Sucesso

<details>
<summary>Ver Logs de Saída</summary>

```text
Calculating task graph as no cached configuration is available for tasks: unitTest
> Task :backend:processResources UP-TO-DATE
> Task :backend:processTestResources UP-TO-DATE
> Task :backend:compileJava UP-TO-DATE
> Task :backend:classes UP-TO-DATE
> Task :backend:compileTestJava UP-TO-DATE
> Task :backend:testClasses UP-TO-DATE
> Task :backend:unitTest
  Results: SUCCESS
  Total:     975 tests run
  ✓ Passed:  975
  ✗ Failed:  0
  ○ Ignored: 0
  Time:     48.237s

Testes mais lentos (> 1s):
  - 5558ms: sgc.arquitetura.ArchConsistencyTest > controllers_should_not_access_repositories
  - 3927ms: sgc.ControllersServicesCoverageTest > deveListarAtividades()

BUILD SUCCESSFUL in 1m
5 actionable tasks: 1 executed, 4 up-to-date
Configuration cache entry stored.

```

</details>

---

### Backend - Testes de Integração

- **Comando:** `gradlew.bat integrationTest`
- **Diretório:** `backend`
- **Status:** ✅ Sucesso

<details>
<summary>Ver Logs de Saída</summary>

```text
Calculating task graph as no cached configuration is available for tasks: integrationTest
> Task :backend:processResources UP-TO-DATE
> Task :backend:processTestResources UP-TO-DATE
> Task :backend:compileJava UP-TO-DATE
> Task :backend:classes UP-TO-DATE
> Task :backend:compileTestJava UP-TO-DATE
> Task :backend:testClasses UP-TO-DATE
> Task :backend:integrationTest
  Results: SUCCESS
  Total:     268 tests run
  ✓ Passed:  268
  ✗ Failed:  0
  ○ Ignored: 0
  Time:     111.278s

BUILD SUCCESSFUL in 1m 56s
5 actionable tasks: 1 executed, 4 up-to-date
Configuration cache entry stored.

```

</details>

---

### Frontend - Testes Unitários

- **Comando:** `npm run test:unit`
- **Diretório:** `frontend`
- **Status:** ❌ Falha

<details>
<summary>Ver Logs de Saída</summary>

```text

> sgc@1.0.0 test:unit
> vitest --run --reporter=dot --no-color


 RUN  v4.0.17 C:/sgc/frontend

······························································································································································································································································································································································································································································································································································································································································································································································································································································································································································································································································································································Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
xx

⎯⎯⎯⎯⎯⎯⎯ Failed Tests 2 ⎯⎯⎯⎯⎯⎯⎯

 FAIL  src/components/__tests__/Acessibilidade.spec.ts > Verificação de Acessibilidade (Axe) > EmptyState deve ser acessível
Error: expect(received).toHaveNoViolations(expected)

Expected the HTML found at $('.h5') to have no violations:

<p data-v-f262d0f3="" class="h5 fw-normal mb-2" data-testid="empty-state-title">Título de Teste</p>

Received:

"All page content should be contained by landmarks (region)"

Fix any of the following:
  Some page content is not contained by landmarks

You can find more information on this issue here: 
https://dequeuniversity.com/rules/axe/4.11/region?application=axeAPI

Expected the HTML found at $('.small') to have no violations:

<p data-v-f262d0f3="" class="small mb-3" data-testid="empty-state-description">Descrição de teste</p>

Received:

"All page content should be contained by landmarks (region)"

Fix any of the following:
  Some page content is not contained by landmarks

You can find more information on this issue here: 
https://dequeuniversity.com/rules/axe/4.11/region?application=axeAPI
 ❯ Module.checkA11y src/test-utils/a11yTestHelpers.ts:13:19
     11| export async function checkA11y(container: HTMLElement, options?: any)…
     12|   const results = await axe(container, options);
     13|   expect(results).toHaveNoViolations();
       |                   ^
     14| }
     15| 
 ❯ src/components/__tests__/Acessibilidade.spec.ts:30:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[1/2]⎯

 FAIL  src/components/__tests__/Acessibilidade.spec.ts > Verificação de Acessibilidade (Axe) > BarraNavegacao deve ser acessível
Error: expect(received).toHaveNoViolations(expected)

Expected the HTML found at $('button') to have no violations:

<button data-v-bb3975c5="" class="btn btn-lg btn-outline-secondary btn-voltar" type="button" data-testid="btn-nav-voltar"><i data-v-bb3975c5="" class="bi bi-arrow-left-circle"></i></button>

Received:

"Buttons must have discernible text (button-name)"

Fix any of the following:
  Element does not have inner text that is visible to screen readers
  aria-label attribute does not exist or is empty
  aria-labelledby attribute does not exist, references elements that do not exist or references elements that are empty
  Element has no title attribute
  Element does not have an implicit (wrapped) <label>
  Element does not have an explicit <label>
  Element's default semantics were not overridden with role="none" or role="presentation"

You can find more information on this issue here: 
https://dequeuniversity.com/rules/axe/4.11/button-name?application=axeAPI
 ❯ Module.checkA11y src/test-utils/a11yTestHelpers.ts:13:19
     11| export async function checkA11y(container: HTMLElement, options?: any)…
     12|   const results = await axe(container, options);
     13|   expect(results).toHaveNoViolations();
       |                   ^
     14| }
     15| 
 ❯ src/components/__tests__/Acessibilidade.spec.ts:36:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[2/2]⎯


 Test Files  1 failed | 97 passed (98)
      Tests  2 failed | 1086 passed (1088)
   Start at  13:16:55
   Duration  64.08s (transform 29.35s, setup 130.42s, import 131.72s, tests 44.73s, environment 423.24s)


```

</details>

---

### E2E - Playwright

- **Comando:** `npx playwright test`
- **Diretório:** `.`
- **Status:** ❌ Falha

<details>
<summary>Ver Logs de Saída</summary>

```text
... (Log truncado - mostrando últimos 20k caracteres) ...
:
      - waiting for getByTestId('btn-abrir-criar-competencia')


       at helpers\helpers-mapas.ts:17

      15 |         await btnEmpty.click();
      16 |     } else {
    > 17 |         await page.getByTestId('btn-abrir-criar-competencia').click();
         |                                                               ^
      18 |     }
      19 |     await expect(page.getByTestId('mdl-criar-competencia')).toBeVisible();
      20 | }
        at abrirModalCriarCompetencia (C:\sgc\e2e\helpers\helpers-mapas.ts:17:63)
        at criarCompetencia (C:\sgc\e2e\helpers\helpers-mapas.ts:23:5)
        at C:\sgc\e2e\cdu-25.spec.ts:116:9

    attachment #1: screenshot (image/png) ──────────────────────────────────────────────────────────
    test-results\cdu-25-CDU-25---Aceitar-va-b261e-mologa-cadastro-e-cria-mapa-chromium\test-failed-1.png
    ────────────────────────────────────────────────────────────────────────────────────────────────

    Error Context: test-results\cdu-25-CDU-25---Aceitar-va-b261e-mologa-cadastro-e-cria-mapa-chromium\error-context.md


ium] › e2e\cdu-25.spec.ts:121:9 › CDU-25 - Aceitar validação de mapas em bloco › Preparacao 4: Chefe valida o mapa
ium] › e2e\cdu-25.spec.ts:138:9 › CDU-25 - Aceitar validação de mapas em bloco › Cenario 1: GESTOR acessa processo com mapa validado
ium] › e2e\cdu-25.spec.ts:153:9 › CDU-25 - Aceitar validação de mapas em bloco › Cenario 2: GESTOR abre modal de aceite de mapa em bloco
ium] › e2e\cdu-26.spec.ts:60:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 1: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 40 criado.

[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 40 iniciado para 1 unidade(s).

[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 40

ium] › e2e\cdu-26.spec.ts:84:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 2: Chefe disponibiliza cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-20T16:25:35.466229300Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-20T16:25:35.697356Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-20T16:25:35.751810900Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-20T16:25:35.780187Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-20T16:25:35.910185300Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-20T16:25:35.931194400Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:35, timestamp=2026-01-20T16:25:36.027109100Z

[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:51 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO

ium] › e2e\cdu-26.spec.ts:100:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 3: Admin homologa cadastro e cria mapa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-20T16:25:37.047390900Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Homologar cadastro, resource=Subprocesso:35, timestamp=2026-01-20T16:25:37.674110200Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-20T16:25:37.695369700Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-20T16:25:37.741803Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-20T16:25:37.898712500Z

ium] › e2e\cdu-26.spec.ts:100:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 3: Admin homologa cadastro e cria mapa 

    Test timeout of 15000ms exceeded.

    Error: locator.click: Test timeout of 15000ms exceeded.
    Call log:
      - waiting for getByTestId('btn-abrir-criar-competencia')


       at helpers\helpers-mapas.ts:17

      15 |         await btnEmpty.click();
      16 |     } else {
    > 17 |         await page.getByTestId('btn-abrir-criar-competencia').click();
         |                                                               ^
      18 |     }
      19 |     await expect(page.getByTestId('mdl-criar-competencia')).toBeVisible();
      20 | }
        at abrirModalCriarCompetencia (C:\sgc\e2e\helpers\helpers-mapas.ts:17:63)
        at criarCompetencia (C:\sgc\e2e\helpers\helpers-mapas.ts:23:5)
        at C:\sgc\e2e\cdu-26.spec.ts:113:9

    attachment #1: screenshot (image/png) ──────────────────────────────────────────────────────────
    test-results\cdu-26-CDU-26---Homologar--3ad6b-mologa-cadastro-e-cria-mapa-chromium\test-failed-1.png
    ────────────────────────────────────────────────────────────────────────────────────────────────

    Error Context: test-results\cdu-26-CDU-26---Homologar--3ad6b-mologa-cadastro-e-cria-mapa-chromium\error-context.md


ium] › e2e\cdu-26.spec.ts:118:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 4: Chefe valida o mapa
ium] › e2e\cdu-26.spec.ts:135:9 › CDU-26 - Homologar validação de mapas em bloco › Cenario 1: ADMIN visualiza botão Homologar Mapa em Bloco
ium] › e2e\cdu-26.spec.ts:150:9 › CDU-26 - Homologar validação de mapas em bloco › Cenario 2: ADMIN abre modal de homologação de mapa em bloco
ium] › e2e\cdu-26.spec.ts:172:9 › CDU-26 - Homologar validação de mapas em bloco › Cenario 3: Cancelar homologação de mapa em bloco
ium] › e2e\cdu-27.spec.ts:48:9 › CDU-27 - Alterar data limite de subprocesso › Preparacao: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 41 criado.

[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 41 iniciado para 1 unidade(s).

[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 41

ium] › e2e\cdu-27.spec.ts:76:9 › CDU-27 - Alterar data limite de subprocesso › Cenario 1: ADMIN navega para detalhes do subprocesso
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:36, timestamp=2026-01-20T16:26:04.519434900Z

ium] › e2e\cdu-27.spec.ts:88:9 › CDU-27 - Alterar data limite de subprocesso › Cenario 2: ADMIN visualiza botão Alterar data limite
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:36, timestamp=2026-01-20T16:26:05.913072100Z

ium] › e2e\cdu-28.spec.ts:32:9 › CDU-28 - Manter atribuição temporária › Cenario 1: ADMIN acessa menu de Unidades
ium] › e2e\cdu-28.spec.ts:50:9 › CDU-28 - Manter atribuição temporária › Cenario 2: ADMIN seleciona unidade na árvore
ium] › e2e\cdu-28.spec.ts:80:9 › CDU-28 - Manter atribuição temporária › Cenario 3: Verificar botão de criar atribuição
ium] › e2e\cdu-29.spec.ts:32:9 › CDU-29 - Consultar histórico de processos › Cenario 1: ADMIN navega para página de histórico
ium] › e2e\cdu-29.spec.ts:45:9 › CDU-29 - Consultar histórico de processos › Cenario 2: GESTOR pode acessar histórico
ium] › e2e\cdu-29.spec.ts:56:9 › CDU-29 - Consultar histórico de processos › Cenario 3: CHEFE pode acessar histórico
ium] › e2e\cdu-29.spec.ts:71:9 › CDU-29 - Consultar histórico de processos › Cenario 4: Tabela apresenta colunas corretas
ium] › e2e\cdu-30.spec.ts:31:9 › CDU-30 - Manter Administradores › Cenario 1: ADMIN acessa página de configurações
ium] › e2e\cdu-30.spec.ts:44:9 › CDU-30 - Manter Administradores › Cenario 2: Página de configurações contém seção de administradores
ium] › e2e\cdu-30.spec.ts:73:9 › CDU-30 - Manter Administradores › Cenario 3: Lista de administradores é exibida
ium] › e2e\cdu-31.spec.ts:28:9 › CDU-31 - Configurar sistema › Cenario 1: ADMIN navega para configurações
ium] › e2e\cdu-31.spec.ts:45:9 › CDU-31 - Configurar sistema › Cenario 2: Tela exibe configurações editáveis
ium] › e2e\cdu-31.spec.ts:65:9 › CDU-31 - Configurar sistema › Cenario 3: ADMIN salva configurações com sucesso
ium] › e2e\cdu-32.spec.ts:50:9 › CDU-32 - Reabrir cadastro › Preparacao 1: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 42 criado.

[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 42 iniciado para 1 unidade(s).

[WebServer] INFO  s.p.l.EventoProcessoListener.enviarEmailProcessoIniciado:236 - E-mail enviado para unidade SECAO_221

ium] › e2e\cdu-32.spec.ts:74:9 › CDU-32 - Reabrir cadastro › Preparacao 2: Chefe disponibiliza cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-20T16:26:21.565116500Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-20T16:26:21.770212600Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-20T16:26:21.826791900Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-20T16:26:21.853872200Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-20T16:26:22.010821600Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-20T16:26:22.032975100Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:37, timestamp=2026-01-20T16:26:22.132082800Z

[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:51 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO

ium] › e2e\cdu-32.spec.ts:94:9 › CDU-32 - Reabrir cadastro › Cenario 1: ADMIN navega para subprocesso disponibilizado
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-20T16:26:23.428449800Z

ium] › e2e\cdu-32.spec.ts:105:9 › CDU-32 - Reabrir cadastro › Cenario 2: ADMIN visualiza botão Reabrir cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-20T16:26:24.465738200Z

ium] › e2e\cdu-32.spec.ts:120:9 › CDU-32 - Reabrir cadastro › Cenario 3: ADMIN abre modal de reabertura de cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-20T16:26:25.382497900Z

ium] › e2e\cdu-32.spec.ts:140:9 › CDU-32 - Reabrir cadastro › Cenario 4: Botão confirmar desabilitado sem justificativa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-20T16:26:26.541665300Z

ium] › e2e\cdu-33.spec.ts:50:9 › CDU-33 - Reabrir revisão de cadastro › Preparacao 1: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 43 criado.

[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 43 iniciado para 1 unidade(s).

[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 43

ium] › e2e\cdu-33.spec.ts:74:9 › CDU-33 - Reabrir revisão de cadastro › Preparacao 2: Chefe disponibiliza revisão de cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-20T16:26:29.654981800Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-20T16:26:29.875099300Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-20T16:26:29.923478300Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-20T16:26:29.947185Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-20T16:26:30.090021100Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-20T16:26:30.107244800Z

[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:38, timestamp=2026-01-20T16:26:30.226173600Z

[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:51 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO

ium] › e2e\cdu-33.spec.ts:94:9 › CDU-33 - Reabrir revisão de cadastro › Cenario 1: ADMIN navega para subprocesso de revisão
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-20T16:26:31.247205Z

ium] › e2e\cdu-33.spec.ts:104:9 › CDU-33 - Reabrir revisão de cadastro › Cenario 2: ADMIN visualiza botão Reabrir Revisão
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-20T16:26:32.163812700Z

ium] › e2e\cdu-33.spec.ts:119:9 › CDU-33 - Reabrir revisão de cadastro › Cenario 3: ADMIN abre modal de reabertura de revisão
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-20T16:26:33.077557300Z

ium] › e2e\cdu-34.spec.ts:44:9 › CDU-34 - Enviar lembrete de prazo › Preparacao: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 44 criado.

[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 44 iniciado para 1 unidade(s).

[WebServer] INFO  s.p.l.EventoProcessoListener.enviarEmailProcessoIniciado:236 - E-mail enviado para unidade SECAO_221

ium] › e2e\cdu-34.spec.ts:72:9 › CDU-34 - Enviar lembrete de prazo › Cenario 1: ADMIN navega para detalhes do processo
ium] › e2e\cdu-34.spec.ts:81:9 › CDU-34 - Enviar lembrete de prazo › Cenario 2: Verificar indicadores de prazo
ium] › e2e\cdu-34.spec.ts:98:9 › CDU-34 - Enviar lembrete de prazo › Cenario 3: Verificar opção de enviar lembrete
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-20T16:26:37.627209500Z

ium] › e2e\cdu-35.spec.ts:28:9 › CDU-35 - Gerar relatório de andamento › Cenario 1: ADMIN navega para página de relatórios
ium] › e2e\cdu-35.spec.ts:38:9 › CDU-35 - Gerar relatório de andamento › Cenario 2: Página exibe card de relatório de andamento
ium] › e2e\cdu-35.spec.ts:48:9 › CDU-35 - Gerar relatório de andamento › Cenario 3: Abrir modal de Andamento Geral
ium] › e2e\cdu-35.spec.ts:62:9 › CDU-35 - Gerar relatório de andamento › Cenario 4: Modal contém tabela de dados
ium] › e2e\cdu-35.spec.ts:77:9 › CDU-35 - Gerar relatório de andamento › Cenario 5: Botão de exportação está disponível
ium] › e2e\cdu-35.spec.ts:88:9 › CDU-35 - Gerar relatório de andamento › Cenario 6: Filtros estão disponíveis
ium] › e2e\cdu-36.spec.ts:28:9 › CDU-36 - Gerar relatório de mapas › Cenario 1: ADMIN navega para página de relatórios
ium] › e2e\cdu-36.spec.ts:38:9 › CDU-36 - Gerar relatório de mapas › Cenario 2: Página exibe card de relatório de mapas
ium] › e2e\cdu-36.spec.ts:48:9 › CDU-36 - Gerar relatório de mapas › Cenario 3: Abrir modal de Mapas Vigentes
ium] › e2e\cdu-36.spec.ts:62:9 › CDU-36 - Gerar relatório de mapas › Cenario 4: Botão de exportação está disponível
[1A[2K  15 failed
    [chromium] › e2e\cdu-05.spec.ts:142:9 › CDU-05 - Iniciar processo de revisao › Fase 1.5: ADMIN adiciona competências e disponibiliza mapa 
    [chromium] › e2e\cdu-10.spec.ts:117:9 › CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos › Preparacao 4: Admin adiciona competências e disponibiliza mapa 
    [chromium] › e2e\cdu-11.spec.ts:186:9 › CDU-11 - Visualizar cadastro de atividades e conhecimentos › Cenario 3: Visualizar processo finalizado 
    [chromium] › e2e\cdu-12.spec.ts:47:9 › CDU-12 - Verificar impactos no mapa de competências › Preparacao 1: Setup Mapeamento (Atividades, Competências, Homologação) 
    [chromium] › e2e\cdu-13.spec.ts:209:9 › CDU-13 - Analisar cadastro de atividades e conhecimentos › Cenario 7: GESTOR registra aceite SEM observação 
    [chromium] › e2e\cdu-14.spec.ts:97:9 › CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos › Preparacao 0.3: GESTOR aceita cadastro 
    [chromium] › e2e\cdu-15.spec.ts:56:9 › CDU-15 - Manter mapa de competências › Preparacao: Criar processo e homologar cadastro de atividades 
    [chromium] › e2e\cdu-16.spec.ts:117:9 › CDU-16 - Ajustar mapa de competências › Preparacao 4: Admin cria competências e disponibiliza mapa 
    [chromium] › e2e\cdu-17.spec.ts:103:9 › CDU-17 - Disponibilizar mapa de competências › Preparacao 4: Admin cria competências com todas as atividades associadas 
    [chromium] › e2e\cdu-19.spec.ts:97:9 › CDU-19 - Validar mapa de competências › Preparacao 4: Admin cria competências e disponibiliza mapa 
    [chromium] › e2e\cdu-20.spec.ts:105:9 › CDU-20 - Analisar validação de mapa de competências › Preparacao 4: Admin cria competências e disponibiliza mapa 
    [chromium] › e2e\cdu-21.spec.ts:105:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Preparacao 4: Admin cria competências e disponibiliza mapa 
    [chromium] › e2e\cdu-24.spec.ts:94:9 › CDU-24 - Disponibilizar mapas em bloco › Preparacao 3: Admin homologa cadastro e cria competências 
    [chromium] › e2e\cdu-25.spec.ts:103:9 › CDU-25 - Aceitar validação de mapas em bloco › Preparacao 3: Admin homologa cadastro e cria mapa 
    [chromium] › e2e\cdu-26.spec.ts:100:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 3: Admin homologa cadastro e cria mapa 
  85 did not run
  140 passed (8.6m)

```

</details>

---

