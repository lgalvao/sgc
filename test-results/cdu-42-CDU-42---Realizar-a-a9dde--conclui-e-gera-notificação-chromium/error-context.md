# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-42.spec.ts >> CDU-42 - Realizar autoavaliação >> SERVIDOR preenche a autoavaliação, conclui e gera notificação
- Location: e2e/cdu-42.spec.ts:11:5

# Error details

```
Error: expect(locator).toContainText(expected) failed

Locator: getByTestId('app-alert')
Expected substring: "Preencha importância e domínio para todas as competências."
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toContainText" with timeout 5000ms
  - waiting for getByTestId('app-alert')

```

```yaml
- heading "SGC" [level=1]
- link "Pular para o conteúdo principal":
  - /url: "#main-content"
- navigation:
  - link "SGC":
    - /url: /painel
  - list:
    - listitem:
      - link "Painel":
        - /url: /painel
    - listitem:
      - link "Minha unidade":
        - /url: /unidade/4
    - listitem:
      - link "Histórico":
        - /url: /historico
  - list:
    - listitem:
      - link "SERVIDOR - ASSESSORIA_12":
        - /url: "#"
    - listitem "Ativar modo escuro":
      - link "Ativar modo escuro":
        - /url: "#"
    - listitem "Sair":
      - link "Sair":
        - /url: "#"
- button "Voltar"
- navigation "breadcrumb":
  - list:
    - listitem:
      - link "Início":
        - /url: /painel
    - listitem: › Autoavaliação - Diagnóstico
- main:
  - heading "Autoavaliação de Competências" [level=1]
  - strong: ASSESSORIA_12
  - text: "- Assessoria 12 EM_ANDAMENTO"
  - button "Voltar"
  - strong: Autoavaliação de Competências
  - text: "Escala: NA, 1, 2, 3, 4, 5 e 6"
  - table:
    - rowgroup:
      - row "Codigo Competência Importância Domínio":
        - columnheader "Codigo"
        - columnheader "Competência"
        - columnheader "Importância"
        - columnheader "Domínio"
    - rowgroup:
      - row "4000 Competência Técnica Seed 99 Atividade e conhecimentos NA NA":
        - cell "4000"
        - cell "Competência Técnica Seed 99 Atividade e conhecimentos":
          - text: Competência Técnica Seed 99
          - button "Atividade e conhecimentos"
        - cell "NA":
          - combobox:
            - option "NA"
            - option "1"
            - option "2"
            - option "3"
            - option "4"
            - option "5"
            - option "6"
        - cell "NA":
          - combobox:
            - option "NA"
            - option "1"
            - option "2"
            - option "3"
            - option "4"
            - option "5"
            - option "6"
  - button "Concluir autoavaliação"
- contentinfo: Versão 1.2.0 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
- dialog "Concluir autoavaliação":
  - heading "Concluir autoavaliação" [level=5]
  - button "Close"
  - paragraph: Confirma a conclusão da autoavaliação? Você não poderá alterar os valores após confirmar.
  - button "Cancelar"
  - button "Concluir"
```

# Test source

```ts
  1  | import {expect, test} from './fixtures/complete-fixtures.js';
  2  | import {criarProcessoFixture} from './fixtures/index.js';
  3  | import {abrirCardDiagnostico, preencherAutoavaliacaoCompleta} from './helpers/helpers-diagnostico.js';
  4  | import {login} from './helpers/helpers-auth.js';
  5  | import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
  6  | 
  7  | const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
  8  | const UNIDADE = 'ASSESSORIA_12';
  9  | 
  10 | test.describe('CDU-42 - Realizar autoavaliação', () => {
  11 |     test('SERVIDOR preenche a autoavaliação, conclui e gera notificação', async ({
  12 |         _resetAutomatico,
  13 |         page,
  14 |         request
  15 |     }) => {
  16 |         const descricao = `Diagnóstico CDU-42 ${Date.now()}`;
  17 |         const processo = await criarProcessoFixture(request, {
  18 |             descricao,
  19 |             tipo: 'DIAGNOSTICO',
  20 |             unidade: UNIDADE,
  21 |             iniciar: true,
  22 |             diasLimite: 30
  23 |         });
  24 | 
  25 |         await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
  26 |         await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
  27 | 
  28 |         await abrirCardDiagnostico(page, 'card-subprocesso-diagnostico', /\/diagnostico\/\d+\/ASSESSORIA_12\/autoavaliacao/);
  29 |         await expect(page.getByRole('heading', {name: /Autoavaliação de Competências/i})).toBeVisible();
  30 | 
  31 |         await page.getByTestId('btn-concluir-autoavaliacao').click();
> 32 |         await expect(page.getByTestId('app-alert')).toContainText('Preencha importância e domínio para todas as competências.');
     |                                                     ^ Error: expect(locator).toContainText(expected) failed
  33 | 
  34 |         const url = new URL(page.url());
  35 |         const codSubprocesso = Number(url.pathname.split('/')[2]);
  36 |         await preencherAutoavaliacaoCompleta(page, codSubprocesso);
  37 | 
  38 |         await page.getByTestId('btn-concluir-autoavaliacao').click();
  39 |         await expect(page.getByRole('dialog')).toContainText('Confirma a conclusão da autoavaliação?');
  40 |         await Promise.all([
  41 |             page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao/concluir`) && res.ok()),
  42 |             page.getByTestId('btn-confirmar-concluir').click()
  43 |         ]);
  44 | 
  45 |         await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}(?:\\?.*)?$`));
  46 |         await expect(page.getByTestId('app-alert')).toContainText('Autoavaliação concluída');
  47 | 
  48 |         await login(page, '191919', 'senha');
  49 |         await verificarNotificacaoAdmin(page, {
  50 |             destinatario: UNIDADE,
  51 |             assunto: 'Autoavaliação',
  52 |             tipo: 'Autoavaliação concluída'
  53 |         });
  54 |     });
  55 | });
  56 | 
```