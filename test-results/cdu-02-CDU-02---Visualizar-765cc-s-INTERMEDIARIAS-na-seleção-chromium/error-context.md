# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-02.spec.ts >> CDU-02 - Visualizar painel >> Como ADMIN >> Não deve incluir unidades INTERMEDIARIAS na seleção
- Location: e2e\cdu-02.spec.ts:114:9

# Error details

```
Error: locator.fill: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('inp-login-usuario')

```

```
Error: page.title: Target page, context or browser has been closed
```

```
Error: browserContext.close: Target page, context or browser has been closed
```

# Test source

```ts
  1   | import {test as base} from '@playwright/test';
  2   | // Use the project's centralized logger for consistent formatting
  3   | import logger from '../../frontend/src/utils/logger.js';
  4   | import {useProcessoCleanup} from '../hooks/hooks-limpeza.js';
  5   | import * as fs from 'node:fs';
  6   | import * as path from 'node:path';
  7   | 
  8   | function monitoramentoAtivoNoPlaywright(): boolean {
  9   |     return process.env.SGC_MONITORAMENTO === 'on';
  10  | }
  11  | 
  12  | function obterBaseUrlWorker(_workerIndex: number): string {
  13  |     const portaFrontend = Number.parseInt(process.env.E2E_FRONTEND_PORT || '5173', 10);
  14  |     return `http://localhost:${portaFrontend}`;
  15  | }
  16  | 
  17  | function ehErroEsperadoAutenticacao(url: string, status?: number): boolean {
  18  |     return status === 401 && url.endsWith('/api/usuarios/login');
  19  | }
  20  | 
  21  | function ehErroEsperadoImportacaoDuplicada(url: string, status?: number, method?: string, body?: string): boolean {
  22  |     return status === 422
  23  |         && method === 'POST'
  24  |         && /\/api\/subprocessos\/\d+\/importar-atividades$/.test(url)
  25  |         && !!body
  26  |         && body.includes('"code":"VALIDACAO"')
  27  |         && body.includes('já existentes no cadastro');
  28  | }
  29  | 
  30  | function ehRuidoAutenticacaoEmDetalhes(url: string, status?: number, method?: string): boolean {
  31  |     return status === 401
  32  |         && method === 'GET'
  33  |         && /\/api\/processos\/\d+\/detalhes$/.test(url);
  34  | }
  35  | 
  36  | function ehErroHttpMonitoradoEsperado(args: unknown[]): boolean {
  37  |     return args.some(arg => {
  38  |         if (!arg || typeof arg !== 'object') {
  39  |             return false;
  40  |         }
  41  | 
  42  |         const payload = arg as {url?: string; status?: number; metodo?: string};
  43  |         return payload.url === '/usuarios/login'
  44  |             && payload.status === 401
  45  |             && String(payload.metodo || '').toUpperCase() === 'POST';
  46  |     });
  47  | }
  48  | 
  49  | export const test = base.extend<{
  50  |     cleanupAutomatico: ReturnType<typeof useProcessoCleanup>;
  51  | }>({
  52  |     context: async ({browser}, use, testInfo) => {
  53  |         const baseURL = obterBaseUrlWorker(testInfo.parallelIndex);
  54  |         const context = await browser.newContext({
  55  |             baseURL,
  56  |             extraHTTPHeaders: {
  57  |                 'x-e2e-worker': String(testInfo.parallelIndex),
  58  |                 ...(monitoramentoAtivoNoPlaywright() ? {'X-Monitoramento-Ativo': 'true'} : {})
  59  |             }
  60  |         });
  61  |         await use(context);
> 62  |         await context.close();
      |         ^ Error: browserContext.close: Target page, context or browser has been closed
  63  |     },
  64  | 
  65  |     request: async ({playwright}, use, testInfo) => {
  66  |         const baseURL = obterBaseUrlWorker(testInfo.parallelIndex);
  67  |         const request = await playwright.request.newContext({
  68  |             baseURL,
  69  |             extraHTTPHeaders: {
  70  |                 'x-e2e-worker': String(testInfo.parallelIndex),
  71  |                 ...(monitoramentoAtivoNoPlaywright() ? {'X-Monitoramento-Ativo': 'true'} : {})
  72  |             }
  73  |         });
  74  |         await use(request);
  75  |         await request.dispose();
  76  |     },
  77  | 
  78  |     page: async ({page}, use, testInfo) => {
  79  |         let ultimoRuidoAutenticacaoDetalhesEm = 0;
  80  |         const logs: string[] = [];
  81  | 
  82  |         if (monitoramentoAtivoNoPlaywright()) {
  83  |             await page.addInitScript(() => {
  84  |                 window.sessionStorage.setItem('sgc.monitoramento.ativo', 'true');
  85  |             });
  86  |         }
  87  | 
  88  |         // Listener para logs do console
  89  |         page.on('console', async msg => {
  90  |             const text = msg.text();
  91  |             const locationUrl = msg.location().url || '';
  92  |             // Filtrar logs de conexão do Vite para reduzir ruído
  93  |             if (text.includes('[vite] connecting...') || text.includes('[vite] connected.')) {
  94  |                 return;
  95  |             }
  96  | 
  97  |             if (text.includes('Failed to load resource: the server responded with a status of 401')
  98  |                 && locationUrl.endsWith('/api/usuarios/login')) {
  99  |                 return;
  100 |             }
  101 | 
  102 |             if (text.includes('Failed to load resource: the server responded with a status of 422')
  103 |                 && /\/api\/subprocessos\/\d+\/importar-atividades$/.test(locationUrl)) {
  104 |                 return;
  105 |             }
  106 | 
  107 |             if (text.includes('Failed to load resource: the server responded with a status of 401')
  108 |                 && /\/api\/processos\/\d+\/detalhes$/.test(locationUrl)) {
  109 |                 return;
  110 |             }
  111 | 
  112 |             const type = String(msg.type());
  113 | 
  114 |             // Tenta expandir argumentos se forem objetos (ex: AxiosError)
  115 |             let expandedArgs: string;
  116 |             let argsExpandidos: unknown[] = [];
  117 |             try {
  118 |                 argsExpandidos = await Promise.all(msg.args().map(arg => arg.jsonValue().catch(() => null)));
  119 |                 expandedArgs = argsExpandidos.map(a => {
  120 |                     if (a && typeof a === 'object') {
  121 |                         const objeto = a as {
  122 |                             code?: string;
  123 |                             isAxiosError?: boolean;
  124 |                             message?: string;
  125 |                             config?: {url?: string; method?: string};
  126 |                             response?: {status?: number; data?: unknown};
  127 |                         };
  128 |                         // Se for um erro do Axios, tenta extrair detalhes úteis
  129 |                         if (objeto.code === 'ERR_BAD_REQUEST'
  130 |                             || objeto.isAxiosError
  131 |                             || (objeto.config && objeto.response)) {
  132 |                             return JSON.stringify({
  133 |                                 message: objeto.message,
  134 |                                 url: objeto.config?.url,
  135 |                                 method: objeto.config?.method,
  136 |                                 status: objeto.response?.status,
  137 |                                 responseData: objeto.response?.data
  138 |                             });
  139 |                         }
  140 |                         return JSON.stringify(a);
  141 |                     }
  142 |                     return String(a);
  143 |                 }).join(' ');
  144 |             } catch {
  145 |                 expandedArgs = text; // Fallback
  146 |             }
  147 | 
  148 |             if (monitoramentoAtivoNoPlaywright()
  149 |                 && (text.includes('[http] inicio') || text.includes('[http] fim'))) {
  150 |                 return;
  151 |             }
  152 | 
  153 |             if (monitoramentoAtivoNoPlaywright()
  154 |                 && text.includes('[http] erro')
  155 |                 && ehErroHttpMonitoradoEsperado(argsExpandidos)) {
  156 |                 return;
  157 |             }
  158 | 
  159 |             if (type === 'error'
  160 |                 && text.includes('%cerror')
  161 |                 && Date.now() - ultimoRuidoAutenticacaoDetalhesEm < 2_000
  162 |                 && (expandedArgs.includes('"name":"AxiosError"') || expandedArgs === 'null')) {
```