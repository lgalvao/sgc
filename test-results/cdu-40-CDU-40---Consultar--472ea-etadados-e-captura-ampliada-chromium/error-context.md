# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-40.spec.ts >> CDU-40 - Consultar feedbacks enviados >> ADMIN consulta feedbacks, visualiza detalhes, metadados e captura ampliada
- Location: e2e/cdu-40.spec.ts:4:5

# Error details

```
Error: expect(locator).toContainText(expected) failed

Locator: getByTestId('modal-detalhes-feedback')
Expected substring: "/painel?aba=alertas"
Received string:    "Detalhes do feedbackTipo SugestãoUsuárioAdministrador 1 (191919)Rota/painelEnviado em03/06/2026 19:15CapturaDescriçãoMelhorar o fluxo do painel.MetadadosChaveValorRota/painelAcessoADMIN - SECRETARIA_1Resolução1440x900Título da páginaPainelIdiomapt-BRNavegadorChrome no macOSFechar"
Timeout: 5000ms

Call log:
  - Expect "toContainText" with timeout 5000ms
  - waiting for getByTestId('modal-detalhes-feedback')
    14 × locator resolved to <div role="dialog" tabindex="-1" data-testid="modal-detalhes-feedback" id="BootstrapVueNext__ID__v-0-0-0__modal___" aria-labelledby="BootstrapVueNext__ID__v-0-0-0__modal___-label" aria-describedby="BootstrapVueNext__ID__v-0-0-0__modal___-body" class="modal fade show stack-position-0 stack-inverse-position-0">…</div>
       - unexpected value "Detalhes do feedbackTipo SugestãoUsuárioAdministrador 1 (191919)Rota/painelEnviado em03/06/2026 19:15CapturaDescriçãoMelhorar o fluxo do painel.MetadadosChaveValorRota/painelAcessoADMIN - SECRETARIA_1Resolução1440x900Título da páginaPainelIdiomapt-BRNavegadorChrome no macOSFechar"

```

```yaml
- dialog "Detalhes do feedback":
  - heading "Detalhes do feedback" [level=5]
  - button "Close"
  - term: Tipo
  - definition:  Sugestão
  - term: Usuário
  - definition: Administrador 1 (191919)
  - term: Rota
  - definition:
    - code: /painel
  - term: Enviado em
  - definition: 03/06/2026 19:15
  - term: Captura
  - definition:
    - button "Ampliar captura de tela":
      - img "Captura de tela do feedback"
  - term: Descrição
  - definition:
    - paragraph:
      - strong: Melhorar
      - text: o fluxo do painel.
  - term: Metadados
  - definition:
    - table:
      - rowgroup:
        - row "Chave Valor":
          - columnheader "Chave"
          - columnheader "Valor"
      - rowgroup:
        - row "Rota /painel":
          - cell "Rota"
          - cell "/painel"
        - row "Acesso ADMIN - SECRETARIA_1":
          - cell "Acesso"
          - cell "ADMIN - SECRETARIA_1"
        - row "Resolução 1440x900":
          - cell "Resolução"
          - cell "1440x900"
        - row "Título da página Painel":
          - cell "Título da página"
          - cell "Painel"
        - row "Idioma pt-BR":
          - cell "Idioma"
          - cell "pt-BR"
        - row "Navegador Chrome no macOS":
          - cell "Navegador"
          - cell "Chrome no macOS"
  - button "Fechar"
```

# Test source

```ts
  1  | import {expect, test} from './fixtures/complete-fixtures.js';
  2  | 
  3  | test.describe('CDU-40 - Consultar feedbacks enviados', () => {
  4  |     test('ADMIN consulta feedbacks, visualiza detalhes, metadados e captura ampliada', async ({
  5  |         _resetAutomatico,
  6  |         page,
  7  |         request,
  8  |         _autenticadoComoAdmin
  9  |     }) => {
  10 |         const resposta = await request.post('/e2e/fixtures/feedback', {
  11 |             data: {
  12 |                 tipo: 'SUGESTAO',
  13 |                 nota: '<p><strong>Melhorar</strong> o fluxo do painel.</p>',
  14 |                 rota: '/painel',
  15 |                 metadataJson: JSON.stringify({
  16 |                     rotaCaminho: '/painel',
  17 |                     rotaQuery: '?aba=alertas',
  18 |                     perfilAtivo: 'ADMIN',
  19 |                     unidadeAtiva: 'SECRETARIA_1',
  20 |                     larguraTela: 1440,
  21 |                     alturaTela: 900,
  22 |                     tituloPagina: 'Painel',
  23 |                     idioma: 'pt-BR',
  24 |                     userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Chrome/136.0.0.0 Safari/537.36'
  25 |                 }),
  26 |                 comScreenshot: true,
  27 |                 usuarioCodigo: '191919',
  28 |                 usuarioNome: 'Administrador 1'
  29 |             }
  30 |         });
  31 |         expect(resposta.ok()).toBeTruthy();
  32 |         const feedback = await resposta.json();
  33 | 
  34 |         await page.goto('/administracao/feedbacks');
  35 |         await expect(page.getByRole('heading', {name: /Feedbacks/i})).toBeVisible();
  36 |         await expect(page.getByTestId('tbl-feedbacks')).toBeVisible();
  37 |         await expect(page.getByTestId('btn-feedbacks-atualizar')).toBeVisible();
  38 | 
  39 |         await page.getByTestId(`btn-feedback-detalhes-${feedback.codigo}`).click();
  40 |         const modal = page.getByTestId('modal-detalhes-feedback');
  41 |         await expect(modal).toBeVisible();
  42 |         await expect(modal).toContainText('Administrador 1 (191919)');
  43 |         await expect(modal).toContainText('/painel');
  44 |         await expect(modal).toContainText('Melhorar o fluxo do painel.');
  45 |         await expect(modal).toContainText('Rota');
> 46 |         await expect(modal).toContainText('/painel?aba=alertas');
     |                             ^ Error: expect(locator).toContainText(expected) failed
  47 |         await expect(modal).toContainText('Acesso');
  48 |         await expect(modal).toContainText('ADMIN / SECRETARIA_1');
  49 |         await expect(modal).toContainText('Resolução');
  50 |         await expect(modal).toContainText('1440 x 900');
  51 |         await expect(page.getByTestId('img-feedback-captura')).toBeVisible();
  52 | 
  53 |         await page.getByTestId('btn-feedback-ampliar-captura').click();
  54 |         await expect(page.getByTestId('modal-imagem-ampliada')).toBeVisible();
  55 |         await expect(page.getByTestId('modal-imagem-ampliada').locator('img')).toBeVisible();
  56 |     });
  57 | });
  58 | 
```