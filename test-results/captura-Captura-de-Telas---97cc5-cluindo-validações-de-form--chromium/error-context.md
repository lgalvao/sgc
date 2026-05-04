# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 04 - Subprocesso e Atividades >> Captura fluxo completo de atividades (incluindo validações de form)
- Location: e2e\captura.spec.ts:727:9

# Error details

```
Error: page.evaluate: Target page, context or browser has been closed
```

```
Error: page.title: Target page, context or browser has been closed
```

# Test source

```ts
  141 |                             });
  142 |                         }
  143 |                         return JSON.stringify(a);
  144 |                     }
  145 |                     return String(a);
  146 |                 }).join(' ');
  147 |             } catch {
  148 |                 expandedArgs = text; // Fallback
  149 |             }
  150 | 
  151 |             if (monitoramentoAtivoNoPlaywright()
  152 |                 && (text.includes('[http] inicio') || text.includes('[http] fim'))) {
  153 |                 return;
  154 |             }
  155 | 
  156 |             if (monitoramentoAtivoNoPlaywright()
  157 |                 && text.includes('[http] erro')
  158 |                 && ehErroHttpMonitoradoEsperado(argsExpandidos)) {
  159 |                 return;
  160 |             }
  161 | 
  162 |             if (type === 'error'
  163 |                 && text.includes('%cerror')
  164 |                 && Date.now() - ultimoRuidoAutenticacaoDetalhesEm < 2_000
  165 |                 && (expandedArgs.includes('"name":"AxiosError"') || expandedArgs === 'null')) {
  166 |                 return;
  167 |             }
  168 | 
  169 |             const logEntry = `[${type.toUpperCase()}] ${expandedArgs || text}`;
  170 |             logs.push(logEntry);
  171 | 
  172 |             // Map Playwright console types to logger methods
  173 |             if (type === 'error') {
  174 |                 logger.error(`[BROWSER ${type.toUpperCase()}] ${expandedArgs || text}`);
  175 |             } else if (type === 'warning' || type === 'warn') {
  176 |                 logger.warn(`[BROWSER ${type.toUpperCase()}] ${expandedArgs || text}`);
  177 |             } else {
  178 |                 logger.info(`[BROWSER ${type.toUpperCase()}] ${expandedArgs || text}`);
  179 |             }
  180 |         });
  181 | 
  182 |         // Listener para erros não tratados da página
  183 |         page.on('pageerror', error => {
  184 |             const stack = error && error.stack ? error.stack : error;
  185 |             logs.push(`[UNCAUGHT ERROR] ${stack}`);
  186 |             logger.error(`[BROWSER UNCAUGHT ERROR] ${stack}`);
  187 |         });
  188 | 
  189 |         // Listener para falhas de rede (4xx, 5xx)
  190 |         page.on('response', async response => {
  191 |             if (response.status() >= 400) {
  192 |                 if (ehErroEsperadoAutenticacao(response.url(), response.status())) {
  193 |                     return;
  194 |                 }
  195 | 
  196 |                 let body: string;
  197 |                 try {
  198 |                     body = await response.text();
  199 |                 } catch {
  200 |                     body = '[Erro ao ler corpo]';
  201 |                 }
  202 | 
  203 |                 if (ehErroEsperadoImportacaoDuplicada(response.url(), response.status(), response.request().method(), body)) {
  204 |                     logger.info(`[NETWORK EXPECTED] ${response.status()} ${response.request().method()} ${response.url()}`);
  205 |                     return;
  206 |                 }
  207 | 
  208 |                 if (ehErroValidacaoEsperado(response.status(), body)) {
  209 |                     logger.info(`[NETWORK EXPECTED] ${response.status()} ${response.request().method()} ${response.url()}`);
  210 |                     return;
  211 |                 }
  212 | 
  213 |                 if (ehRuidoAutenticacaoEmDetalhes(response.url(), response.status(), response.request().method())) {
  214 |                     ultimoRuidoAutenticacaoDetalhesEm = Date.now();
  215 |                     logger.info(`[NETWORK EXPECTED] ${response.status()} ${response.request().method()} ${response.url()}`);
  216 |                     return;
  217 |                 }
  218 | 
  219 |                 const networkLog = `[NETWORK ERROR] ${response.status()} ${response.request().method()} ${response.url()}`;
  220 |                 logs.push(networkLog);
  221 |                 logger.warn(networkLog);
  222 | 
  223 |                 if (body && body.length < 2000) {
  224 |                     logs.push(`[NETWORK BODY] ${body}`);
  225 |                     logger.info(`[NETWORK BODY] ${body}`);
  226 |                 } else if (body) {
  227 |                     logs.push(`[NETWORK BODY] ${body.substring(0, 500)}...`);
  228 |                     logger.info(`[NETWORK BODY] ${body.substring(0, 500)}...`);
  229 |                 }
  230 |             }
  231 |         });
  232 | 
  233 |         await use(page);
  234 | 
  235 |         // Se o teste falhar, gera o arquivo error-context.md
  236 |         if (testInfo.status !== testInfo.expectedStatus && testInfo.status !== 'skipped') {
  237 |             const contextContent = [
  238 |                 `# Contexto de Erro: ${testInfo.title}`,
  239 |                 '',
  240 |                 `**URL:** ${page.url()}`,
> 241 |                 `**Título:** ${await page.title()}`,
      |                                           ^ Error: page.title: Target page, context or browser has been closed
  242 |                 '',
  243 |                 '## Logs do Navegador',
  244 |                 '```',
  245 |                 ...logs,
  246 |                 '```',
  247 |                 '',
  248 |                 '## HTML Page snapshot (Body)',
  249 |                 '```html',
  250 |                 await page.evaluate(() => document.body.innerHTML.substring(0, 5000)),
  251 |                 '```'
  252 |             ].join('\n');
  253 | 
  254 |             if (!fs.existsSync(testInfo.outputDir)) {
  255 |                 fs.mkdirSync(testInfo.outputDir, {recursive: true});
  256 |             }
  257 |             const filePath = path.join(testInfo.outputDir, 'error-context.md');
  258 |             fs.writeFileSync(filePath, contextContent);
  259 | 
  260 |             // Registra o arquivo como um anexo para aparecer no report do Playwright
  261 |             testInfo.attachments.push({
  262 |                 name: 'error-context',
  263 |                 contentType: 'text/markdown',
  264 |                 path: filePath
  265 |             });
  266 |         }
  267 |     },
  268 | 
  269 |     cleanupAutomatico: async ({request}, use) => {
  270 |         const cleanup = useProcessoCleanup();
  271 |         await use(cleanup);
  272 |         await cleanup.limpar(request);
  273 |     },
  274 | });
  275 | 
  276 | export {expect} from '@playwright/test';
  277 | 
```