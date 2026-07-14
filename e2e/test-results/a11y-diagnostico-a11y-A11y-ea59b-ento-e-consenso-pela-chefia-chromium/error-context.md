# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: a11y/diagnostico-a11y.spec.ts >> A11y - Diagnóstico de Competências >> Captura monitoramento e consenso pela chefia
- Location: e2e/a11y/diagnostico-a11y.spec.ts:60:5

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
+               "bgColor": "#212529",
+               "contrastRatio": 3.11,
+               "expectedContrastRatio": "4.5:1",
+               "fgColor": "#6a7178",
+               "fontSize": "12.0pt (16px)",
+               "fontWeight": "normal",
+               "messageKey": null,
+             },
+             "id": "color-contrast",
+             "impact": "serious",
+             "message": "Element has insufficient color contrast of 3.11 (foreground color: #6a7178, background color: #212529, font size: 12.0pt (16px), font weight: normal). Expected contrast ratio of 4.5:1",
+             "relatedNodes": Array [
+               Object {
+                 "html": "<body>",
+                 "target": Array [
+                   "body",
+                 ],
+               },
+             ],
+           },
+         ],
+         "failureSummary": "Fix any of the following:
+   Element has insufficient color contrast of 3.11 (foreground color: #6a7178, background color: #212529, font size: 12.0pt (16px), font weight: normal). Expected contrast ratio of 4.5:1",
+         "html": "<button class=\"btn btn-outline-secondary\" type=\"button\" data-testid=\"btn-historico-analise-subprocesso\">Histórico de análise</button>",
+         "impact": "serious",
+         "none": Array [],
+         "target": Array [
+           ".page-header__actions > .btn-outline-secondary.btn",
+         ],
+       },
+       Object {
+         "all": Array [],
+         "any": Array [
+           Object {
+             "data": Object {
+               "bgColor": "#212529",
+               "contrastRatio": 4.37,
+               "expectedContrastRatio": "4.5:1",
+               "fgColor": "#798a9f",
+               "fontSize": "12.0pt (16px)",
+               "fontWeight": "normal",
+               "messageKey": null,
+             },
+             "id": "color-contrast",
+             "impact": "serious",
+             "message": "Element has insufficient color contrast of 4.37 (foreground color: #798a9f, background color: #212529, font size: 12.0pt (16px), font weight: normal). Expected contrast ratio of 4.5:1",
+             "relatedNodes": Array [
+               Object {
+                 "html": "<div class=\"card h-100\" data-testid=\"header-subprocesso-details-resp\">",
+                 "target": Array [
+                   ".col-md-6:nth-child(2) > .h-100.card",
+                 ],
+               },
+             ],
+           },
+         ],
+         "failureSummary": "Fix any of the following:
+   Element has insufficient color contrast of 4.37 (foreground color: #798a9f, background color: #212529, font size: 12.0pt (16px), font weight: normal). Expected contrast ratio of 4.5:1",
+         "html": "<a href=\"mailto:ana.beatriz.albuquerque@tre-pe.jus.br\" class=\"link-discreto\">ana.beatriz.albuquerque@tre-pe.jus.br</a>",
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

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - generic [ref=e5]:
      - generic [ref=e6]:
        - navigation [ref=e7]:
          - generic [ref=e8]:
            - link "SGC" [ref=e9] [cursor=pointer]:
              - /url: /painel
            - generic [ref=e10]:
              - list [ref=e11]:
                - listitem [ref=e12]:
                  - link "Painel" [ref=e13] [cursor=pointer]:
                    - /url: /painel
                    - generic [ref=e14]: 
                    - text: Painel
                - listitem [ref=e15]:
                  - link "Minha unidade" [ref=e16] [cursor=pointer]:
                    - /url: /unidade/4
                    - generic [ref=e17]: 
                    - text: Minha unidade
                - listitem [ref=e18]:
                  - link "Histórico" [ref=e19] [cursor=pointer]:
                    - /url: /historico
                    - generic [ref=e20]: 
                    - text: Histórico
              - list [ref=e21]:
                - listitem [ref=e22]:
                  - link "CHEFE - ASSESSORIA_12" [ref=e23] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e24]:
                      - generic [ref=e25]: 
                      - generic [ref=e26]: CHEFE - ASSESSORIA_12
                - listitem "Ativar modo escuro" [ref=e27]:
                  - link "Ativar modo escuro" [ref=e28] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e29]: Ativar modo escuro
                    - generic [ref=e30]: 
                - listitem "Sair" [ref=e31]:
                  - link "Sair" [ref=e32] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e33]: Sair
                    - generic [ref=e34]: 
        - generic [ref=e37]:
          - button "Voltar" [ref=e38] [cursor=pointer]:
            - generic [ref=e39]: 
          - navigation "breadcrumb" [ref=e40]:
            - list [ref=e41]:
              - listitem [ref=e42]:
                - link "Início" [ref=e43] [cursor=pointer]:
                  - /url: /painel
                  - generic [ref=e44]: 
                  - generic [ref=e45]: Início
              - listitem [ref=e46]:
                - text: ›
                - generic [ref=e47]: ASSESSORIA_12
      - main [ref=e48]:
        - generic [ref=e50]:
          - generic [ref=e51]:
            - generic [ref=e53]:
              - generic [ref=e54]:
                - heading "ASSESSORIA_12" [level=2] [ref=e55]
                - paragraph [ref=e56]: Assessoria 12
              - generic [ref=e57]:
                - button "Histórico de análise" [ref=e58] [cursor=pointer]
                - button "Concluir diagnóstico" [ref=e59] [cursor=pointer]
            - generic [ref=e60]:
              - generic [ref=e63]:
                - paragraph [ref=e64]:
                  - strong [ref=e65]: "Processo:"
                  - text: Proc diagnostico monitoramento 1783990477841
                - paragraph [ref=e66]: Situação:Em andamento
                - paragraph [ref=e67]: Localização atual:ASSESSORIA_12
                - paragraph [ref=e68]: Prazo para conclusão da etapa atual:12/08/2026
              - generic [ref=e71]:
                - paragraph [ref=e72]:
                  - strong [ref=e73]: "Titular:"
                  - text: Ana Beatriz de Albuquerque e Souza
                - paragraph [ref=e74]:
                  - generic [ref=e75]:
                    - generic [ref=e76]: 
                    - text: "2004"
                  - generic [ref=e77]:
                    - generic [ref=e78]: 
                    - link "ana.beatriz.albuquerque@tre-pe.jus.br" [ref=e79] [cursor=pointer]:
                      - /url: mailto:ana.beatriz.albuquerque@tre-pe.jus.br
          - generic [ref=e84]:
            - heading "Situação de capacitação" [level=4] [ref=e85]:
              - generic [ref=e86]: 
              - generic [ref=e87]: Situação de capacitação
            - paragraph [ref=e88]: Preenchimento da situação de capacitação dos servidores da unidade
          - table [ref=e93]:
            - rowgroup [ref=e94]:
              - row "Servidor Situação Ações" [ref=e95]:
                - columnheader "Servidor" [ref=e96]
                - columnheader "Situação" [ref=e97]
                - columnheader "Ações" [ref=e98]
            - rowgroup [ref=e99]:
              - row "Jon Lord Autoavaliação não iniciada Ações" [ref=e100]:
                - cell "Jon Lord" [ref=e101]
                - cell "Autoavaliação não iniciada" [ref=e102]:
                  - generic [ref=e103]: Autoavaliação não iniciada
                - cell "Ações" [ref=e104]:
                  - button "Ações" [ref=e106] [cursor=pointer]
              - row "Luiz Fernando Cavalcanti de Moura Autoavaliação não iniciada Ações" [ref=e107]:
                - cell "Luiz Fernando Cavalcanti de Moura" [ref=e108]
                - cell "Autoavaliação não iniciada" [ref=e109]:
                  - generic [ref=e110]: Autoavaliação não iniciada
                - cell "Ações" [ref=e111]:
                  - button "Ações" [ref=e113] [cursor=pointer]
              - row "João Guilherme de Albuquerque Maranhão Autoavaliação concluída Ações" [ref=e114]:
                - cell "João Guilherme de Albuquerque Maranhão" [ref=e115]
                - cell "Autoavaliação concluída" [ref=e116]:
                  - generic [ref=e117]: Autoavaliação concluída
                - cell "Ações" [ref=e118]:
                  - button "Ações" [ref=e120] [cursor=pointer]
              - row "Maria Eduarda Cavalcanti de Alencar Autoavaliação não iniciada Ações" [ref=e121]:
                - cell "Maria Eduarda Cavalcanti de Alencar" [ref=e122]
                - cell "Autoavaliação não iniciada" [ref=e123]:
                  - generic [ref=e124]: Autoavaliação não iniciada
                - cell "Ações" [ref=e125]:
                  - button "Ações" [ref=e127] [cursor=pointer]
          - generic [ref=e128]:
            - heading "Movimentações" [level=4] [ref=e129]
            - table [ref=e131]:
              - rowgroup [ref=e132]:
                - row "Data/hora Origem Destino Descrição" [ref=e133]:
                  - columnheader "Data/hora" [ref=e134]
                  - columnheader "Origem" [ref=e135]
                  - columnheader "Destino" [ref=e136]
                  - columnheader "Descrição" [ref=e137]
              - rowgroup [ref=e138]:
                - row "13/07/2026 21:54 ADMIN ASSESSORIA_12 Processo de diagnóstico iniciado" [ref=e139]:
                  - cell "13/07/2026 21:54" [ref=e140]
                  - cell "ADMIN" [ref=e141]
                  - cell "ASSESSORIA_12" [ref=e142]
                  - cell "Processo de diagnóstico iniciado" [ref=e143]
      - contentinfo [ref=e144]:
        - generic [ref=e145]:
          - generic [ref=e146]: Versão 1.3.7
          - generic [ref=e147]: © SESEL/COSIS/TRE-PE
  - button "Enviar feedback" [ref=e148] [cursor=pointer]:
    - generic [ref=e149]: 
  - text:         
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