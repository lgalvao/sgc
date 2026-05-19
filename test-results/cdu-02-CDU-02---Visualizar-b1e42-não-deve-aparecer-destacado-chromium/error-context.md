# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-02.spec.ts >> CDU-02 - Visualizar painel >> Alertas no painel >> Alerta expirado pelo prazo configurado não deve aparecer destacado
- Location: e2e\cdu-02.spec.ts:326:9

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('row-alerta-5')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('row-alerta-5')

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
      - link "Unidades":
        - /url: /unidades
    - listitem:
      - link "Relatórios":
        - /url: /relatorios
    - listitem:
      - link "Histórico":
        - /url: /historico
  - list:
    - listitem:
      - link "ADMIN":
        - /url: "#"
    - listitem "Notificações":
      - link "Notificações":
        - /url: /administracao/notificacoes
    - listitem "Configurações":
      - link "Configurações":
        - /url: /configuracoes
    - listitem "Administradores do sistema":
      - link "Administradores":
        - /url: /administradores
    - listitem "Ativar modo escuro":
      - link "Ativar modo escuro":
        - /url: "#"
    - listitem:
      - button "Ações especiais"
    - listitem "Sair":
      - link "Sair":
        - /url: "#"
- main:
  - heading "Painel" [level=1]
  - heading "Processos" [level=2]
  - button "Criar processo"
  - table:
    - rowgroup:
      - row "Descrição Click to sort descending Tipo Click to sort ascending Unidades Situação Click to sort ascending":
        - columnheader "Descrição Click to sort descending"
        - columnheader "Tipo Click to sort ascending"
        - columnheader "Unidades"
        - columnheader "Situação Click to sort ascending"
    - rowgroup:
      - row "Mapeamento Secão 311 Mapeamento COORD_31 Em andamento":
        - cell "Mapeamento Secão 311"
        - cell "Mapeamento"
        - cell "COORD_31"
        - cell "Em andamento"
      - row "Mapeamento Secão 321 Mapeamento SECAO_321 Em andamento":
        - cell "Mapeamento Secão 321"
        - cell "Mapeamento"
        - cell "SECAO_321"
        - cell "Em andamento"
      - row "Processo 99 Mapeamento ASSESSORIA_12 Finalizado":
        - cell "Processo 99"
        - cell "Mapeamento"
        - cell "ASSESSORIA_12"
        - cell "Finalizado"
      - row "Processo Seed 200 Mapeamento SECRETARIA_1 Finalizado":
        - cell "Processo Seed 200"
        - cell "Mapeamento"
        - cell "SECRETARIA_1"
        - cell "Finalizado"
  - heading "Alertas" [level=2]
  - table:
    - rowgroup:
      - row "Data/Hora Descrição Processo Origem":
        - columnheader "Data/Hora"
        - columnheader "Descrição"
        - columnheader "Processo"
        - columnheader "Origem"
    - rowgroup:
      - 'row "17/05/2026 23:03 Não lido: Mapa de competências homologado Processo 99 SECRETARIA_1"':
        - cell "17/05/2026 23:03"
        - 'cell "Não lido: Mapa de competências homologado"'
        - cell "Processo 99"
        - cell "SECRETARIA_1"
      - 'row "17/05/2026 18:03 Não lido: Cadastro homologado Mapeamento Secão 311 ADMIN"':
        - cell "17/05/2026 18:03"
        - 'cell "Não lido: Cadastro homologado"'
        - cell "Mapeamento Secão 311"
        - cell "ADMIN"
      - 'row "17/05/2026 00:03 Não lido: Mapa de competências homologado Processo Seed 200 SECRETARIA_1"':
        - cell "17/05/2026 00:03"
        - 'cell "Não lido: Mapa de competências homologado"'
        - cell "Processo Seed 200"
        - cell "SECRETARIA_1"
      - 'row "14/05/2026 06:03 Não lido: Alerta antigo seed 201 Processo Histórico Seed 201 SECRETARIA_1"':
        - cell "14/05/2026 06:03"
        - 'cell "Não lido: Alerta antigo seed 201"'
        - cell "Processo Histórico Seed 201"
        - cell "SECRETARIA_1"
- contentinfo: Versão 1.0.4 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  233 |             });
  234 | 
  235 |             await page.goto('/painel');
  236 |             const linhaAdmin = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso}).first();
  237 |             await expect(linhaAdmin).toBeVisible();
  238 |             await linhaAdmin.click();
  239 |             await expect(page).toHaveURL(/\/processo\/\d+(?:\?.*)?$/);
  240 | 
  241 |             await fazerLogout(page);
  242 |             await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
  243 | 
  244 |             const linhaGestor = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso}).first();
  245 |             await expect(linhaGestor).toBeVisible();
  246 |             await linhaGestor.click();
  247 |             await expect(page).toHaveURL(/\/processo\/\d+(?:\?.*)?$/);
  248 | 
  249 |             await fazerLogout(page);
  250 |             await login(page, USUARIOS.CHEFE_SECAO_111.titulo, USUARIOS.CHEFE_SECAO_111.senha);
  251 | 
  252 |             const linhaChefe = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso}).first();
  253 |             await expect(linhaChefe).toBeVisible();
  254 |             await linhaChefe.click();
  255 |             await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_111(?:\?.*)?$/);
  256 |         });
  257 |     });
  258 | 
  259 |     test.describe('Como GESTOR', () => {
  260 |         test('Deve validar visualização, alertas e ordenação', async ({
  261 |                                                                           _resetAutomatico,
  262 |                                                                           page,
  263 |                                                                           _autenticadoComoGestor
  264 |                                                                       }) => {
  265 |             await test.step('Verificar restrições de botões e mensagens de tabela vazia', async () => {
  266 |                 await expect(page.getByTestId('btn-painel-criar-processo')).toBeHidden();
  267 |                 // A tabela não é renderizada se estiver vazia (EmptyState substitui)
  268 |                 await expect(page.getByTestId('empty-state-processos')).toBeVisible();
  269 |             });
  270 | 
  271 |             await test.step('Verificar tabela de alertas vazia', async () => {
  272 |                 await expect(page.getByTestId('empty-state-alertas')).toBeVisible();
  273 |                 await expect(page.getByTestId('empty-state-alertas')).toContainText(/Sem alertas/i);
  274 |             });
  275 |         });
  276 |     });
  277 | 
  278 |     test.describe('Alertas no painel', () => {
  279 |         test('Deve exibir campos, manter ordenação fixa e marcar alerta como lido na segunda visualização', async ({
  280 |                                                                                                                        _resetAutomatico,
  281 |                                                                                                                        page,
  282 |                                                                                                                        _autenticadoComoAdmin
  283 |                                                                                                                    }) => {
  284 |             const descricaoProcesso = `Processo alerta painel - ${Date.now()}`;
  285 | 
  286 |             await criarProcesso(page, {
  287 |                 descricao: descricaoProcesso,
  288 |                 tipo: 'MAPEAMENTO',
  289 |                 diasLimite: 30,
  290 |                 unidade: 'ASSESSORIA_11',
  291 |                 expandir: ['SECRETARIA_1'],
  292 |                 iniciar: true
  293 |             });
  294 | 
  295 |             await fazerLogout(page);
  296 |             await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
  297 | 
  298 |             const tabelaAlertas = page.getByTestId('tbl-alertas');
  299 |             await expect(tabelaAlertas).toBeVisible();
  300 | 
  301 |             await expect(tabelaAlertas.locator('th', {hasText: 'Data/Hora'}).first()).toBeVisible();
  302 |             await expect(tabelaAlertas.locator('th', {hasText: 'Descrição'}).first()).toBeVisible();
  303 |             await expect(tabelaAlertas.locator('th', {hasText: 'Processo'}).first()).toBeVisible();
  304 |             await expect(tabelaAlertas.locator('th', {hasText: 'Origem'}).first()).toBeVisible();
  305 | 
  306 |             const linhaAlerta = tabelaAlertas.locator('tr', {hasText: descricaoProcesso})
  307 |                 .filter({hasText: 'Início do processo'})
  308 |                 .first();
  309 |             await expect(linhaAlerta).toBeVisible();
  310 |             await expect(linhaAlerta).toContainText(/\d{2}\/\d{2}\/\d{4}\s+\d{2}:\d{2}/);
  311 |             await expect(linhaAlerta).toHaveClass(/fw-bold/);
  312 | 
  313 |             const cabecalhoDataHora = tabelaAlertas.locator('th', {hasText: 'Data/Hora'}).first();
  314 |             await expect(cabecalhoDataHora).not.toHaveAttribute('aria-sort', /ascending|descending/);
  315 |             await cabecalhoDataHora.click();
  316 |             await expect(cabecalhoDataHora).not.toHaveAttribute('aria-sort', /ascending|descending/);
  317 | 
  318 |             await page.reload();
  319 |             const linhaAlertaLida = page.getByTestId('tbl-alertas').locator('tr', {hasText: descricaoProcesso})
  320 |                 .filter({hasText: 'Início do processo'})
  321 |                 .first();
  322 |             await expect(linhaAlertaLida).toBeVisible();
  323 |             await expect(linhaAlertaLida).not.toHaveClass(/fw-bold/);
  324 |         });
  325 | 
  326 |         test('Alerta expirado pelo prazo configurado não deve aparecer destacado', async ({
  327 |                                                                                               _resetAutomatico,
  328 |                                                                                               page,
  329 |                                                                                               _autenticadoComoAdmin
  330 |                                                                                           }) => {
  331 |             const linhaAlertaAntigo = page.getByTestId('row-alerta-5');
  332 | 
> 333 |             await expect(linhaAlertaAntigo).toBeVisible();
      |                                             ^ Error: expect(locator).toBeVisible() failed
  334 |             await expect(linhaAlertaAntigo).toContainText('Alerta antigo seed 201');
  335 |             await expect(linhaAlertaAntigo).not.toHaveClass(/fw-bold/);
  336 |         });
  337 |     });
  338 | 
  339 |     test.describe('Visibilidade de alertas por perfil', () => {
  340 |         test('SERVIDOR não deve ver alertas de unidade', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  341 |             const descricao = `Proc servidor alertas - ${Date.now()}`;
  342 |             await criarProcesso(page, {
  343 |                 descricao,
  344 |                 tipo: 'MAPEAMENTO',
  345 |                 diasLimite: 30,
  346 |                 unidade: 'SECAO_113',
  347 |                 expandir: ['SECRETARIA_1', 'COORD_11'],
  348 |                 iniciar: true
  349 |             });
  350 | 
  351 |             await fazerLogout(page);
  352 |             await login(page, USUARIOS.SERVIDOR.titulo, USUARIOS.SERVIDOR.senha);
  353 | 
  354 |             // SERVIDOR vê apenas alertas pessoais, não alertas de unidade
  355 |             const tblAlertas = page.getByTestId('tbl-alertas');
  356 |             const linhaAlerta = tblAlertas.locator('tr', {hasText: descricao}).first();
  357 |             await expect(linhaAlerta).toBeHidden();
  358 |         });
  359 |     });
  360 | });
  361 | 
```