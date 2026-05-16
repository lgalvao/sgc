# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 11 - Histórico de Análise e Devolução >> Captura modal de histórico e fluxo de devolução de cadastro
- Location: e2e/captura.spec.ts:1518:9

# Error details

```
Error: page.evaluate: Execution context was destroyed, most likely because of a navigation.
```

# Test source

```ts
  132 |         await page.screenshot({
  133 |             path: caminhoCompleto,
  134 |             fullPage: isFullPage
  135 |         });
  136 |     } finally {
  137 |         await restaurarScrollAposCaptura(page, scrollOriginal);
  138 |     }
  139 | 
  140 |     const metadata: CapturaMetadata = {
  141 |         ordem: capturasMetadata.length + 1,
  142 |         arquivo: nomeArquivo,
  143 |         categoria,
  144 |         nome,
  145 |         rota,
  146 |         titulo
  147 |     };
  148 | 
  149 |     if (opcoes?.tags && opcoes.tags.length > 0) {
  150 |         metadata.tags = opcoes.tags;
  151 |     }
  152 |     if (viewportPadraoMetadata.largura !== viewportAtual.largura || viewportPadraoMetadata.altura !== viewportAtual.altura) {
  153 |         metadata.viewport = viewportAtual;
  154 |     }
  155 |     if (opcoes?.extra && Object.keys(opcoes.extra).length > 0) {
  156 |         metadata.contexto = opcoes.extra;
  157 |     }
  158 | 
  159 |     capturasMetadata.push(metadata);
  160 | 
  161 | }
  162 | 
  163 | function extrairRota(url: string): string {
  164 |     try {
  165 |         const urlObj = new URL(url);
  166 |         return urlObj.pathname + urlObj.search;
  167 |     } catch {
  168 |         return url;
  169 |     }
  170 | }
  171 | 
  172 | function extrairBaseUrl(url: string): string {
  173 |     try {
  174 |         return new URL(url).origin;
  175 |     } catch {
  176 |         return '';
  177 |     }
  178 | }
  179 | 
  180 | function montarDocumentoCapturasMetadata(): DocumentoCapturasMetadata {
  181 |     return {
  182 |         versaoEsquema: 1,
  183 |         geradoEm: new Date().toISOString(),
  184 |         baseUrl: baseUrlMetadata,
  185 |         viewportPadrao: viewportPadraoMetadata ?? {largura: 0, altura: 0},
  186 |         capturas: capturasMetadata
  187 |     };
  188 | }
  189 | 
  190 | async function prepararPaginaParaCaptura(
  191 |     page: Page,
  192 |     isFullPage: boolean
  193 | ): Promise<{ x: number; y: number } | null> {
  194 |     if (!isFullPage) {
  195 |         return null;
  196 |     }
  197 | 
  198 |     return page.evaluate(() => {
  199 |         const estiloId = 'sgc-captura-fullpage-style';
  200 |         if (!document.getElementById(estiloId)) {
  201 |             const estilo = document.createElement('style');
  202 |             estilo.id = estiloId;
  203 |             estilo.textContent = '.navbar.sticky-top { position: static !important; top: auto !important; }';
  204 |             document.head.appendChild(estilo);
  205 |         }
  206 | 
  207 |         const scrollOriginal = {x: globalThis.scrollX, y: globalThis.scrollY};
  208 |         globalThis.scrollTo(0, 0);
  209 |         return scrollOriginal;
  210 |     });
  211 | }
  212 | 
  213 | async function restaurarScrollAposCaptura(
  214 |     page: Page,
  215 |     scrollOriginal: { x: number; y: number } | null
  216 | ): Promise<void> {
  217 |     if (!scrollOriginal || (scrollOriginal.x === 0 && scrollOriginal.y === 0)) {
  218 |         await page.evaluate(() => {
  219 |             document.getElementById('sgc-captura-fullpage-style')?.remove();
  220 |         });
  221 |         return;
  222 |     }
  223 | 
  224 |     await page.evaluate(({x, y}) => {
  225 |         document.getElementById('sgc-captura-fullpage-style')?.remove();
  226 |         globalThis.scrollTo(x, y);
  227 |     }, scrollOriginal);
  228 | }
  229 | 
  230 | async function aguardarPinturaEstavel(page: Page, quadros = 2): Promise<void> {
  231 |     for (let indice = 0; indice < quadros; indice += 1) {
> 232 |         await page.evaluate(() => new Promise<void>((resolve) => {
      |                    ^ Error: page.evaluate: Execution context was destroyed, most likely because of a navigation.
  233 |             globalThis.requestAnimationFrame(() => resolve());
  234 |         }));
  235 |     }
  236 | }
  237 | 
  238 | async function aguardarInterfaceEstavelParaCaptura(page: Page): Promise<void> {
  239 |     await aguardarPinturaEstavel(page);
  240 |     await aguardarModaisEstaveis(page);
  241 |     await aguardarPinturaEstavel(page);
  242 | }
  243 | 
  244 | async function aguardarModaisEstaveis(page: Page): Promise<void> {
  245 |     const duracaoMs = await page.evaluate(() => {
  246 |         const seletores = [
  247 |             '.modal.show',
  248 |             '.modal.show .modal-dialog',
  249 |             '.modal-backdrop.show',
  250 |             '[role="dialog"]:not([aria-hidden="true"])',
  251 |         ];
  252 | 
  253 |         const elementos = Array.from(document.querySelectorAll<HTMLElement>(seletores.join(',')))
  254 |             .filter((elemento) => {
  255 |                 const estilo = globalThis.getComputedStyle(elemento);
  256 |                 return estilo.display !== 'none' && estilo.visibility !== 'hidden';
  257 |             });
  258 | 
  259 |         if (elementos.length === 0) {
  260 |             return 0;
  261 |         }
  262 | 
  263 |         function converterTempoParaMs(valor: string): number {
  264 |             const valorTratado = valor.trim();
  265 |             if (valorTratado.endsWith('ms')) {
  266 |                 return Number.parseFloat(valorTratado) || 0;
  267 |             }
  268 |             if (valorTratado.endsWith('s')) {
  269 |                 return (Number.parseFloat(valorTratado) || 0) * 1000;
  270 |             }
  271 |             return Number.parseFloat(valorTratado) || 0;
  272 |         }
  273 | 
  274 |         function maiorTempo(listaCss: string): number {
  275 |             return Math.max(
  276 |                 0,
  277 |                 ...listaCss.split(',').map((valor) => converterTempoParaMs(valor))
  278 |             );
  279 |         }
  280 | 
  281 |         return Math.max(
  282 |             0,
  283 |             ...elementos.map((elemento) => {
  284 |                 const estilo = globalThis.getComputedStyle(elemento);
  285 |                 return maiorTempo(estilo.transitionDuration)
  286 |                     + maiorTempo(estilo.transitionDelay)
  287 |                     + maiorTempo(estilo.animationDuration)
  288 |                     + maiorTempo(estilo.animationDelay);
  289 |             })
  290 |         );
  291 |     });
  292 | 
  293 |     if (duracaoMs <= 0) {
  294 |         return;
  295 |     }
  296 | 
  297 |     await page.waitForTimeout(Math.min(Math.ceil(duracaoMs) + 50, 1000));
  298 | }
  299 | 
  300 | function registrarProcessoParaCleanup(cleanup: ProcessoCleanup, codigo: number): void {
  301 |     expect(codigo, 'Código de processo inválido para cleanup').toBeGreaterThan(0);
  302 |     cleanup.registrar(codigo);
  303 | }
  304 | 
  305 | async function criarProcessoMapeamentoIniciadoPorFixture(
  306 |     request: APIRequestContext,
  307 |     cleanup: ReturnType<typeof useProcessoCleanup>,
  308 |     descricao: string,
  309 |     unidadeSigla: string
  310 | ): Promise<number> {
  311 |     const response = await request.post('/e2e/fixtures/processo-mapeamento', {
  312 |         data: {
  313 |             descricao,
  314 |             unidadeSigla,
  315 |             iniciar: true,
  316 |             diasLimite: 30
  317 |         }
  318 |     });
  319 | 
  320 |     if (!response.ok()) {
  321 |         throw new Error(`Falha ao criar fixture de processo iniciado: ${response.status()} ${await response.text()}`);
  322 |     }
  323 |     const processo = await response.json() as { codigo: number };
  324 |     cleanup.registrar(processo.codigo);
  325 |     return processo.codigo;
  326 | }
  327 | 
  328 | async function criarProcessoMapeamentoComMapaDisponibilizadoPorFixture(
  329 |     request: APIRequestContext,
  330 |     cleanup: ReturnType<typeof useProcessoCleanup>,
  331 |     descricao: string,
  332 |     unidadeSigla: string
```