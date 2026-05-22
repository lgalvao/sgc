# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-21.spec.ts >> CDU-21 - Finalizar processo de mapeamento ou de revisão >> Cenario 3: ADMIN finaliza processo com sucesso
- Location: e2e/cdu-21.spec.ts:79:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('tbl-notificacoes').locator('tbody tr').filter({ hasText: 'Finalização do processo Mapeamento CDU-21 1779407767743' }).filter({ hasText: 'secao_221@tre-pe.jus.br' }).filter({ hasText: 'Finalização de processo' }).first()
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('tbl-notificacoes').locator('tbody tr').filter({ hasText: 'Finalização do processo Mapeamento CDU-21 1779407767743' }).filter({ hasText: 'secao_221@tre-pe.jus.br' }).filter({ hasText: 'Finalização de processo' }).first()

```

```yaml
- heading "SGC" [level=1]
- link "Pular para o conteúdo principal":
  - /url: "#main-content"
- navigation:
  - link "SGC":
    - /url: /painel
  - button "Toggle navigation"
- button "Voltar"
- navigation "breadcrumb":
  - list:
    - listitem:
      - link "Início":
        - /url: /painel
    - listitem: › Notificações
- main:
  - heading "Notificações" [level=2]
  - button "Atualizar"
  - table:
    - rowgroup:
      - row "Destinatário Click to sort ascending Tipo Click to sort ascending Assunto Click to sort ascending Situação Click to sort ascending Quando Click to sort ascending":
        - columnheader "Destinatário Click to sort ascending"
        - columnheader "Tipo Click to sort ascending"
        - columnheader "Assunto Click to sort ascending"
        - columnheader "Situação Click to sort ascending"
        - columnheader "Quando Click to sort ascending"
        - columnheader
    - rowgroup:
      - row "SECAO_321 Mapa disponibilizado Mapa de competências disponibilizado - SECAO_321 Falha Definitiva 21/05/2026 20:11 Detalhes Ver conteúdo do e-mail Tentar reenviar e-mail":
        - cell "SECAO_321"
        - cell "Mapa disponibilizado"
        - cell "Mapa de competências disponibilizado - SECAO_321"
        - cell "Falha Definitiva"
        - cell "21/05/2026 20:11"
        - cell "Detalhes Ver conteúdo do e-mail Tentar reenviar e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
          - button "Tentar reenviar e-mail"
      - row "SECRETARIA_2 Finalização do processo Finalização do processo Mapeamento CDU-21 1779407767743 em unidades subordinadas Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "SECRETARIA_2"
        - cell "Finalização do processo"
        - cell "Finalização do processo Mapeamento CDU-21 1779407767743 em unidades subordinadas"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "ADMIN Finalização do processo Finalização do processo Mapeamento CDU-21 1779407767743 em unidades subordinadas Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "ADMIN"
        - cell "Finalização do processo"
        - cell "Finalização do processo Mapeamento CDU-21 1779407767743 em unidades subordinadas"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "ADMIN Finalização do processo Finalização do processo Mapeamento CDU-21 1779407767743 em unidades subordinadas Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "ADMIN"
        - cell "Finalização do processo"
        - cell "Finalização do processo Mapeamento CDU-21 1779407767743 em unidades subordinadas"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "COORD_22 Finalização do processo Finalização do processo Mapeamento CDU-21 1779407767743 em unidades subordinadas Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "COORD_22"
        - cell "Finalização do processo"
        - cell "Finalização do processo Mapeamento CDU-21 1779407767743 em unidades subordinadas"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "SECRETARIA_2 Finalização do processo Finalização do processo Mapeamento CDU-21 1779407767743 Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "SECRETARIA_2"
        - cell "Finalização do processo"
        - cell "Finalização do processo Mapeamento CDU-21 1779407767743"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "SECAO_221 Finalização do processo Finalização do processo Mapeamento CDU-21 1779407767743 Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "SECAO_221"
        - cell "Finalização do processo"
        - cell "Finalização do processo Mapeamento CDU-21 1779407767743"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "SECRETARIA_2 Início do processo Início de processo de mapeamento de competências em unidades subordinadas Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "SECRETARIA_2"
        - cell "Início do processo"
        - cell "Início de processo de mapeamento de competências em unidades subordinadas"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "COORD_22 Início do processo Início de processo de mapeamento de competências em unidades subordinadas Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "COORD_22"
        - cell "Início do processo"
        - cell "Início de processo de mapeamento de competências em unidades subordinadas"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "ADMIN Início do processo Início de processo de mapeamento de competências em unidades subordinadas Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "ADMIN"
        - cell "Início do processo"
        - cell "Início de processo de mapeamento de competências em unidades subordinadas"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "ADMIN Início do processo Início de processo de mapeamento de competências em unidades subordinadas Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "ADMIN"
        - cell "Início do processo"
        - cell "Início de processo de mapeamento de competências em unidades subordinadas"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "SECAO_221 Início do processo Início de processo de mapeamento de competências Enviado 21/05/2026 20:56 Detalhes Ver conteúdo do e-mail":
        - cell "SECAO_221"
        - cell "Início do processo"
        - cell "Início de processo de mapeamento de competências"
        - cell "Enviado"
        - cell "21/05/2026 20:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
      - row "SECAO_311 Cadastro homologado Cadastro de atividades homologado Enviado 20/05/2026 05:56 Detalhes Ver conteúdo do e-mail":
        - cell "SECAO_311"
        - cell "Cadastro homologado"
        - cell "Cadastro de atividades homologado"
        - cell "Enviado"
        - cell "20/05/2026 05:56"
        - cell "Detalhes Ver conteúdo do e-mail":
          - button "Detalhes"
          - button "Ver conteúdo do e-mail"
- contentinfo: Versão 1.1.0 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  1  | import {expect, type Locator, type Page} from '@playwright/test';
  2  | 
  3  | export interface CriteriosNotificacaoAdmin {
  4  |     assunto: string | RegExp;
  5  |     destinatario?: string | RegExp;
  6  |     tipo?: string | RegExp;
  7  |     situacao?: string | RegExp;
  8  |     trechoCorpo?: string | RegExp;
  9  | }
  10 | 
  11 | export async function abrirNotificacoesAdmin(page: Page): Promise<Locator> {
  12 |     const linkNotificacoes = page.getByTestId('nav-link-notificacoes');
  13 | 
  14 |     if (!(await linkNotificacoes.isVisible())) {
  15 |         const botaoNavbar = page.locator('.navbar-toggler:visible').first();
  16 |         await expect(botaoNavbar).toBeVisible();
  17 |         await botaoNavbar.click();
  18 |         await expect(linkNotificacoes).toBeVisible();
  19 |     }
  20 | 
  21 |     await linkNotificacoes.click();
  22 |     await expect(page).toHaveURL(/\/administracao\/notificacoes/);
  23 | 
  24 |     const tabela = page.getByTestId('tbl-notificacoes');
  25 |     await expect(tabela).toBeVisible();
  26 |     await page.getByTestId('btn-notificacoes-atualizar').click();
  27 |     await expect(tabela).toBeVisible();
  28 |     return tabela;
  29 | }
  30 | 
  31 | export async function verificarNotificacaoAdmin(page: Page, criterios: CriteriosNotificacaoAdmin): Promise<void> {
  32 |     const tabela = await abrirNotificacoesAdmin(page);
  33 | 
  34 |     let linha = tabela.locator('tbody tr');
  35 |     linha = linha.filter({hasText: criterios.assunto});
  36 |     if (criterios.destinatario) linha = linha.filter({hasText: criterios.destinatario});
  37 |     if (criterios.tipo) linha = linha.filter({hasText: criterios.tipo});
  38 |     if (criterios.situacao) linha = linha.filter({hasText: criterios.situacao});
  39 | 
  40 |     const linhaEncontrada = linha.first();
> 41 |     await expect(linhaEncontrada).toBeVisible();
     |                                   ^ Error: expect(locator).toBeVisible() failed
  42 | 
  43 |     if (!criterios.trechoCorpo) return;
  44 | 
  45 |     await linhaEncontrada.locator('[data-testid^="btn-preview-"]').first().click();
  46 |     const modal = page.getByTestId('modal-preview-email');
  47 |     await expect(modal).toBeVisible();
  48 |     const iframe = modal.frameLocator('[data-testid="iframe-preview-email"]');
  49 |     await expect(iframe.locator('body')).toContainText(criterios.trechoCorpo);
  50 |     await page.getByTestId('btn-fechar-preview-email').click();
  51 |     await expect(modal).toBeHidden();
  52 | }
  53 | 
```