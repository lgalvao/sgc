# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 03 - Fluxo de Processo >> Captura validacao do mapa
- Location: e2e/captura.spec.ts:662:9

# Error details

```
Error: page.waitForTimeout: Target page, context or browser has been closed
```

# Test source

```ts
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
  205 |         await page.evaluate(() => {
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
> 284 |     await page.waitForTimeout(Math.min(Math.ceil(duracaoMs) + 50, 1000));
      |                ^ Error: page.waitForTimeout: Target page, context or browser has been closed
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
  306 | 
  307 |     if (!response.ok()) {
  308 |         throw new Error(`Falha ao criar fixture de processo iniciado: ${response.status()} ${await response.text()}`);
  309 |     }
  310 |     const processo = await response.json() as { codigo: number };
  311 |     cleanup.registrar(processo.codigo);
  312 |     return processo.codigo;
  313 | }
  314 | 
  315 | async function criarProcessoMapeamentoComMapaDisponibilizadoPorFixture(
  316 |     request: APIRequestContext,
  317 |     cleanup: ReturnType<typeof useProcessoCleanup>,
  318 |     descricao: string,
  319 |     unidadeSigla: string
  320 | ): Promise<number> {
  321 |     const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-disponibilizado', {
  322 |         data: {
  323 |             descricao,
  324 |             unidadeSigla,
  325 |             iniciar: true,
  326 |             diasLimite: 30
  327 |         }
  328 |     });
  329 | 
  330 |     if (!response.ok()) {
  331 |         throw new Error(`Falha ao criar fixture de mapa disponibilizado: ${response.status()} ${await response.text()}`);
  332 |     }
  333 |     const processo = await response.json() as { codigo: number };
  334 |     cleanup.registrar(processo.codigo);
  335 |     return processo.codigo;
  336 | }
  337 | 
  338 | async function criarProcessoMapeamentoComCadastroDisponibilizadoPorFixture(
  339 |     request: APIRequestContext,
  340 |     cleanup: ReturnType<typeof useProcessoCleanup>,
  341 |     descricao: string,
  342 |     unidadeSigla: string
  343 | ): Promise<number> {
  344 |     const response = await request.post('/e2e/fixtures/processo-mapeamento-com-cadastro-disponibilizado', {
  345 |         data: {
  346 |             descricao,
  347 |             unidadeSigla,
  348 |             iniciar: true,
  349 |             diasLimite: 30
  350 |         }
  351 |     });
  352 | 
  353 |     if (!response.ok()) {
  354 |         throw new Error(`Falha ao criar fixture de cadastro disponibilizado: ${response.status()} ${await response.text()}`);
  355 |     }
  356 |     const processo = await response.json() as { codigo: number };
  357 |     cleanup.registrar(processo.codigo);
  358 |     return processo.codigo;
  359 | }
  360 | 
  361 | async function criarProcessoMapeamentoComMapaHomologadoPorFixture(
  362 |     request: APIRequestContext,
  363 |     cleanup: ReturnType<typeof useProcessoCleanup>,
  364 |     descricao: string,
  365 |     unidadeSigla: string
  366 | ): Promise<number> {
  367 |     const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-homologado', {
  368 |         data: {
  369 |             descricao,
  370 |             unidadeSigla,
  371 |             iniciar: true,
  372 |             diasLimite: 30
  373 |         }
  374 |     });
  375 | 
  376 |     if (!response.ok()) {
  377 |         throw new Error(`Falha ao criar fixture de mapa homologado: ${response.status()} ${await response.text()}`);
  378 |     }
  379 |     const processo = await response.json() as { codigo: number };
  380 |     cleanup.registrar(processo.codigo);
  381 |     return processo.codigo;
  382 | }
  383 | 
  384 | test.describe('Captura de Telas - Sistema SGC', () => {
```