# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 02 - Painel principal >> Captura painel GESTOR
- Location: e2e/captura.spec.ts:516:9

# Error details

```
Error: write EPIPE
```

```
Error: page.evaluate: Target page, context or browser has been closed
```

# Test source

```ts
  105 |     const viewportAtual = {largura: viewport.width, altura: viewport.height};
  106 | 
  107 |     if (!baseUrlMetadata) {
  108 |         baseUrlMetadata = extrairBaseUrl(url);
  109 |     }
  110 |     viewportPadraoMetadata ??= viewportAtual;
  111 | 
  112 |     const nomeArquivo = `${categoria}--${nome}.png`;
  113 |     const caminhoCompleto = path.join(SCREENSHOTS_DIR, nomeArquivo);
  114 | 
  115 |     const scrollOriginal = await prepararPaginaParaCaptura(page, isFullPage);
  116 |     await aguardarInterfaceEstavelParaCaptura(page);
  117 | 
  118 |     try {
  119 |         await page.screenshot({
  120 |             path: caminhoCompleto,
  121 |             fullPage: isFullPage
  122 |         });
  123 |     } finally {
  124 |         await restaurarScrollAposCaptura(page, scrollOriginal);
  125 |     }
  126 | 
  127 |     const metadata: CapturaMetadata = {
  128 |         ordem: capturasMetadata.length + 1,
  129 |         arquivo: nomeArquivo,
  130 |         categoria,
  131 |         nome,
  132 |         rota,
  133 |         titulo
  134 |     };
  135 | 
  136 |     if (opcoes?.tags && opcoes.tags.length > 0) {
  137 |         metadata.tags = opcoes.tags;
  138 |     }
  139 |     if (viewportPadraoMetadata.largura !== viewportAtual.largura || viewportPadraoMetadata.altura !== viewportAtual.altura) {
  140 |         metadata.viewport = viewportAtual;
  141 |     }
  142 |     if (opcoes?.extra && Object.keys(opcoes.extra).length > 0) {
  143 |         metadata.contexto = opcoes.extra;
  144 |     }
  145 | 
  146 |     capturasMetadata.push(metadata);
  147 | 
  148 | }
  149 | 
  150 | function extrairRota(url: string): string {
  151 |     try {
  152 |         const urlObj = new URL(url);
  153 |         return urlObj.pathname + urlObj.search;
  154 |     } catch {
  155 |         return url;
  156 |     }
  157 | }
  158 | 
  159 | function extrairBaseUrl(url: string): string {
  160 |     try {
  161 |         return new URL(url).origin;
  162 |     } catch {
  163 |         return '';
  164 |     }
  165 | }
  166 | 
  167 | function montarDocumentoCapturasMetadata(): DocumentoCapturasMetadata {
  168 |     return {
  169 |         versaoEsquema: 1,
  170 |         geradoEm: new Date().toISOString(),
  171 |         baseUrl: baseUrlMetadata,
  172 |         viewportPadrao: viewportPadraoMetadata ?? {largura: 0, altura: 0},
  173 |         capturas: capturasMetadata
  174 |     };
  175 | }
  176 | 
  177 | async function prepararPaginaParaCaptura(
  178 |     page: Page,
  179 |     isFullPage: boolean
  180 | ): Promise<{ x: number; y: number } | null> {
  181 |     if (!isFullPage) {
  182 |         return null;
  183 |     }
  184 | 
  185 |     return page.evaluate(() => {
  186 |         const estiloId = 'sgc-captura-fullpage-style';
  187 |         if (!document.getElementById(estiloId)) {
  188 |             const estilo = document.createElement('style');
  189 |             estilo.id = estiloId;
  190 |             estilo.textContent = '.navbar.sticky-top { position: static !important; top: auto !important; }';
  191 |             document.head.appendChild(estilo);
  192 |         }
  193 | 
  194 |         const scrollOriginal = {x: globalThis.scrollX, y: globalThis.scrollY};
  195 |         globalThis.scrollTo(0, 0);
  196 |         return scrollOriginal;
  197 |     });
  198 | }
  199 | 
  200 | async function restaurarScrollAposCaptura(
  201 |     page: Page,
  202 |     scrollOriginal: { x: number; y: number } | null
  203 | ): Promise<void> {
  204 |     if (!scrollOriginal || (scrollOriginal.x === 0 && scrollOriginal.y === 0)) {
> 205 |         await page.evaluate(() => {
      |                    ^ Error: page.evaluate: Target page, context or browser has been closed
  206 |             document.getElementById('sgc-captura-fullpage-style')?.remove();
  207 |         });
  208 |         return;
  209 |     }
  210 | 
  211 |     await page.evaluate(({x, y}) => {
  212 |         document.getElementById('sgc-captura-fullpage-style')?.remove();
  213 |         globalThis.scrollTo(x, y);
  214 |     }, scrollOriginal);
  215 | }
  216 | 
  217 | async function aguardarPinturaEstavel(page: Page, quadros = 2): Promise<void> {
  218 |     for (let indice = 0; indice < quadros; indice += 1) {
  219 |         await page.evaluate(() => new Promise<void>((resolve) => {
  220 |             globalThis.requestAnimationFrame(() => resolve());
  221 |         }));
  222 |     }
  223 | }
  224 | 
  225 | async function aguardarInterfaceEstavelParaCaptura(page: Page): Promise<void> {
  226 |     await aguardarPinturaEstavel(page);
  227 |     await aguardarModaisEstaveis(page);
  228 |     await aguardarPinturaEstavel(page);
  229 | }
  230 | 
  231 | async function aguardarModaisEstaveis(page: Page): Promise<void> {
  232 |     const duracaoMs = await page.evaluate(() => {
  233 |         const seletores = [
  234 |             '.modal.show',
  235 |             '.modal.show .modal-dialog',
  236 |             '.modal-backdrop.show',
  237 |             '[role="dialog"]:not([aria-hidden="true"])',
  238 |         ];
  239 | 
  240 |         const elementos = Array.from(document.querySelectorAll<HTMLElement>(seletores.join(',')))
  241 |             .filter((elemento) => {
  242 |                 const estilo = globalThis.getComputedStyle(elemento);
  243 |                 return estilo.display !== 'none' && estilo.visibility !== 'hidden';
  244 |             });
  245 | 
  246 |         if (elementos.length === 0) {
  247 |             return 0;
  248 |         }
  249 | 
  250 |         function converterTempoParaMs(valor: string): number {
  251 |             const valorTratado = valor.trim();
  252 |             if (valorTratado.endsWith('ms')) {
  253 |                 return Number.parseFloat(valorTratado) || 0;
  254 |             }
  255 |             if (valorTratado.endsWith('s')) {
  256 |                 return (Number.parseFloat(valorTratado) || 0) * 1000;
  257 |             }
  258 |             return Number.parseFloat(valorTratado) || 0;
  259 |         }
  260 | 
  261 |         function maiorTempo(listaCss: string): number {
  262 |             return Math.max(
  263 |                 0,
  264 |                 ...listaCss.split(',').map((valor) => converterTempoParaMs(valor))
  265 |             );
  266 |         }
  267 | 
  268 |         return Math.max(
  269 |             0,
  270 |             ...elementos.map((elemento) => {
  271 |                 const estilo = globalThis.getComputedStyle(elemento);
  272 |                 return maiorTempo(estilo.transitionDuration)
  273 |                     + maiorTempo(estilo.transitionDelay)
  274 |                     + maiorTempo(estilo.animationDuration)
  275 |                     + maiorTempo(estilo.animationDelay);
  276 |             })
  277 |         );
  278 |     });
  279 | 
  280 |     if (duracaoMs <= 0) {
  281 |         return;
  282 |     }
  283 | 
  284 |     await page.waitForTimeout(Math.min(Math.ceil(duracaoMs) + 50, 1000));
  285 | }
  286 | 
  287 | function registrarProcessoParaCleanup(cleanup: ProcessoCleanup, codigo: number): void {
  288 |     expect(codigo, 'Código de processo inválido para cleanup').toBeGreaterThan(0);
  289 |     cleanup.registrar(codigo);
  290 | }
  291 | 
  292 | async function criarProcessoMapeamentoIniciadoPorFixture(
  293 |     request: APIRequestContext,
  294 |     cleanup: ReturnType<typeof useProcessoCleanup>,
  295 |     descricao: string,
  296 |     unidadeSigla: string
  297 | ): Promise<number> {
  298 |     const response = await request.post('/e2e/fixtures/processo-mapeamento', {
  299 |         data: {
  300 |             descricao,
  301 |             unidadeSigla,
  302 |             iniciar: true,
  303 |             diasLimite: 30
  304 |         }
  305 |     });
```