# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: a11y/jornada-a11y.spec.ts >> Captura de Telas - Sistema SGC >> 10 - Gestão de Subprocessos >> Captura modais de gestão de subprocesso
- Location: e2e/a11y/jornada-a11y.spec.ts:1148:9

# Error details

```
Error: expect(received).toEqual(expected) // deep equality

- Expected  -  1
+ Received  + 93

- Array []
+ Array [
+   Object {
+     "description": "Ensure the contrast between foreground and background colors meets WCAG 2 AA minimum contrast ratio thresholds",
+     "help": "Elements must meet minimum color contrast ratio thresholds",
+     "helpUrl": "https://dequeuniversity.com/rules/axe/4.12/color-contrast?application=playwright",
+     "id": "color-contrast",
+     "impact": "serious",
+     "nodes": Array [
+       Object {
+         "all": Array [],
+         "any": Array [
+           Object {
+             "data": Object {
+               "bgColor": "#2b3035",
+               "contrastRatio": 4.19,
+               "expectedContrastRatio": "4.5:1",
+               "fgColor": "#8c9198",
+               "fontSize": "10.2pt (13.6px)",
+               "fontWeight": "normal",
+               "messageKey": null,
+             },
+             "id": "color-contrast",
+             "impact": "serious",
+             "message": "Element has insufficient color contrast of 4.19 (foreground color: #8c9198, background color: #2b3035, font size: 10.2pt (13.6px), font weight: normal). Expected contrast ratio of 4.5:1",
+             "relatedNodes": Array [
+               Object {
+                 "html": "<div class=\"bg-body-tertiary border-bottom\">",
+                 "target": Array [
+                   ".border-bottom.bg-body-tertiary",
+                 ],
+               },
+             ],
+           },
+         ],
+         "failureSummary": "Fix any of the following:
+   Element has insufficient color contrast of 4.19 (foreground color: #8c9198, background color: #2b3035, font size: 10.2pt (13.6px), font weight: normal). Expected contrast ratio of 4.5:1",
+         "html": "<span data-v-267ee191=\"\">Detalhes do processo</span>",
+         "impact": "serious",
+         "none": Array [],
+         "target": Array [
+           "a[href=\"/processo/403\"] > span[data-v-267ee191=\"\"]",
+         ],
+       },
+       Object {
+         "all": Array [],
+         "any": Array [
+           Object {
+             "data": Object {
+               "bgColor": "#212529",
+               "contrastRatio": 4.31,
+               "expectedContrastRatio": "4.5:1",
+               "fgColor": "#78899e",
+               "fontSize": "12.0pt (16px)",
+               "fontWeight": "normal",
+               "messageKey": null,
+             },
+             "id": "color-contrast",
+             "impact": "serious",
+             "message": "Element has insufficient color contrast of 4.31 (foreground color: #78899e, background color: #212529, font size: 12.0pt (16px), font weight: normal). Expected contrast ratio of 4.5:1",
+             "relatedNodes": Array [
+               Object {
+                 "html": "<div class=\"card h-100\" data-testid=\"header-subprocesso-details-resp\">",
+                 "target": Array [
+                   ".col-md-6:nth-child(2) > .card.h-100",
+                 ],
+               },
+             ],
+           },
+         ],
+         "failureSummary": "Fix any of the following:
+   Element has insufficient color contrast of 4.31 (foreground color: #78899e, background color: #212529, font size: 12.0pt (16px), font weight: normal). Expected contrast ratio of 4.5:1",
+         "html": "<a href=\"mailto:jimi.hendrix@tre-pe.jus.br\" class=\"link-discreto\">jimi.hendrix@tre-pe.jus.br</a>",
+         "impact": "serious",
+         "none": Array [],
+         "target": Array [
+           ".link-discreto",
+         ],
+       },
+     ],
+     "tags": Array [
+       "cat.color",
+       "wcag2aa",
+       "wcag143",
+       "TTv5",
+       "TT13.c",
+       "EN-301-549",
+       "EN-9.1.4.3",
+       "ACT",
+       "RGAAv4",
+       "RGAA-3.2.1",
+     ],
+   },
+ ]
```

# Test source

```ts
  1   | import {AxeBuilder} from '@axe-core/playwright';
  2   | import {expect, type Page} from '@playwright/test';
  3   | 
  4   | type ContextoCaptura = Record<string, unknown>;
  5   | 
  6   | export interface OpcoesCapturaTela {
  7   |     fullPage?: boolean;
  8   |     extra?: ContextoCaptura;
  9   |     tags?: string[];
  10  |     auditarAcessibilidade?: boolean;
  11  | }
  12  | 
  13  | export async function capturarCheckpointA11y(page: Page, opcoes?: OpcoesCapturaTela): Promise<void> {
  14  |     await aguardarInterfaceEstavelParaCaptura(page);
  15  | 
  16  |     if (deveAuditarCheckpoint(opcoes)) {
  17  |         await auditarAcessibilidadeNosTemas(page);
  18  |     }
  19  | }
  20  | 
  21  | function deveAuditarCheckpoint(opcoes?: OpcoesCapturaTela): boolean {
  22  |     if (opcoes?.auditarAcessibilidade !== undefined) {
  23  |         return opcoes.auditarAcessibilidade;
  24  |     }
  25  | 
  26  |     if (opcoes?.fullPage) {
  27  |         return true;
  28  |     }
  29  | 
  30  |     const tags = new Set(opcoes?.tags ?? []);
  31  |     return ['modal', 'menu', 'dropdown', 'historico', 'a11y'].some((tag) => tags.has(tag));
  32  | }
  33  | 
  34  | export async function auditarAcessibilidadeNosTemas(page: Page): Promise<void> {
  35  |     const temaOriginal = await obterTemaAtual(page);
  36  | 
  37  |     await auditarAcessibilidade(page);
  38  |     await definirTemaTemporario(page, 'dark');
  39  |     await auditarAcessibilidade(page);
  40  |     await definirTemaTemporario(page, temaOriginal);
  41  | }
  42  | 
  43  | async function auditarAcessibilidade(page: Page): Promise<void> {
  44  |     const accessibilityScanResults = await new AxeBuilder({page})
  45  |         .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22a', 'wcag22aa'])
  46  |         // Toasters are transient overlays and not part of the stable checkpoint surface.
  47  |         .exclude('.toast')
  48  |         .disableRules(['list'])
  49  |         .analyze();
  50  | 
> 51  |     expect(accessibilityScanResults.violations).toEqual([]);
      |                                                 ^ Error: expect(received).toEqual(expected) // deep equality
  52  | }
  53  | 
  54  | async function obterTemaAtual(page: Page): Promise<'light' | 'dark'> {
  55  |     return page.evaluate(() => {
  56  |         return document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light';
  57  |     });
  58  | }
  59  | 
  60  | async function definirTemaTemporario(page: Page, tema: 'light' | 'dark'): Promise<void> {
  61  |     await page.evaluate((temaAtual) => {
  62  |         document.documentElement.setAttribute('data-bs-theme', temaAtual);
  63  |         document.documentElement.style.colorScheme = temaAtual;
  64  |     }, tema);
  65  |     await aguardarInterfaceEstavelParaCaptura(page);
  66  | }
  67  | 
  68  | export async function aguardarPinturaEstavel(page: Page, quadros = 2): Promise<void> {
  69  |     for (let indice = 0; indice < quadros; indice += 1) {
  70  |         await page.evaluate(() => new Promise<void>((resolve) => {
  71  |             globalThis.requestAnimationFrame(() => resolve());
  72  |         }));
  73  |     }
  74  | }
  75  | 
  76  | async function aguardarInterfaceEstavelParaCaptura(page: Page): Promise<void> {
  77  |     await aguardarPinturaEstavel(page);
  78  |     await aguardarModaisEstaveis(page);
  79  |     await aguardarTransicoesEspecificas(page);
  80  |     await aguardarPinturaEstavel(page);
  81  | }
  82  | 
  83  | async function aguardarModaisEstaveis(page: Page): Promise<void> {
  84  |     const duracaoMs = await page.evaluate(() => {
  85  |         const seletores = [
  86  |             '.modal.show',
  87  |             '.modal.show .modal-dialog',
  88  |             '.modal-backdrop.show',
  89  |             '[role="dialog"]:not([aria-hidden="true"])',
  90  |             '.tooltip.show',
  91  |             '.tooltip',
  92  |             '.dropdown-menu.show',
  93  |             '.dropdown-menu',
  94  |         ];
  95  | 
  96  |         const elementos = Array.from(document.querySelectorAll<HTMLElement>(seletores.join(',')))
  97  |             .filter((elemento) => {
  98  |                 const estilo = globalThis.getComputedStyle(elemento);
  99  |                 return estilo.display !== 'none' && estilo.visibility !== 'hidden';
  100 |             });
  101 | 
  102 |         if (elementos.length === 0) {
  103 |             return 0;
  104 |         }
  105 | 
  106 |         function converterTempoParaMs(valor: string): number {
  107 |             const valorTratado = valor.trim();
  108 |             if (valorTratado.endsWith('ms')) {
  109 |                 return Number.parseFloat(valorTratado) || 0;
  110 |             }
  111 |             if (valorTratado.endsWith('s')) {
  112 |                 return (Number.parseFloat(valorTratado) || 0) * 1000;
  113 |             }
  114 |             return Number.parseFloat(valorTratado) || 0;
  115 |         }
  116 | 
  117 |         function maiorTempo(listaCss: string): number {
  118 |             return Math.max(
  119 |                 0,
  120 |                 ...listaCss.split(',').map((valor) => converterTempoParaMs(valor))
  121 |             );
  122 |         }
  123 | 
  124 |         return Math.max(
  125 |             0,
  126 |             ...elementos.map((elemento) => {
  127 |                 const estilo = globalThis.getComputedStyle(elemento);
  128 |                 return maiorTempo(estilo.transitionDuration)
  129 |                     + maiorTempo(estilo.transitionDelay)
  130 |                     + maiorTempo(estilo.animationDuration)
  131 |                     + maiorTempo(estilo.animationDelay);
  132 |             })
  133 |         );
  134 |     });
  135 | 
  136 |     if (duracaoMs <= 0) {
  137 |         return;
  138 |     }
  139 | 
  140 |     await page.waitForTimeout(Math.min(Math.ceil(duracaoMs) + 50, 1000));
  141 | }
  142 | 
  143 | async function aguardarTransicoesEspecificas(page: Page): Promise<void> {
  144 |     const duracaoMs = await page.evaluate(() => {
  145 |         const seletores = [
  146 |             '.login-autorizacao-enter-active',
  147 |             '.login-autorizacao-leave-active',
  148 |             '.tree-row-transition-enter-active',
  149 |             '.tree-row-transition-leave-active',
  150 |         ];
  151 | 
```