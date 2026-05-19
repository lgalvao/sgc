# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-19.spec.ts >> CDU-19 - Apresentar sugestões e pré-preenchimento >> Cenario 1: CHEFE apresenta sugestões com sucesso
- Location: e2e\cdu-19.spec.ts:94:5

# Error details

```
Test timeout of 20000ms exceeded.
```

```
Error: locator.click: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('btn-sugestoes-mapa-confirmar')
    - locator resolved to <button type="button" aria-busy="false" data-v-4f1e08f8="" class="btn btn-success" data-testid="btn-sugestoes-mapa-confirmar">…</button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not stable
    - retrying click action
    - waiting 20ms
    - waiting for element to be visible, enabled and stable
    - element is not stable
  - retrying click action
    - waiting 100ms
    - waiting for element to be visible, enabled and stable
  - element was detached from the DOM, retrying
    - locator resolved to <button type="button" aria-busy="false" data-v-4f1e08f8="" class="btn btn-success" data-testid="btn-sugestoes-mapa-confirmar">…</button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
      - waiting 100ms
    35 × waiting for element to be visible, enabled and stable
       - element is not visible
     - retrying click action
       - waiting 500ms

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
                    - /url: /unidade/18
                    - generic [ref=e17]: 
                    - text: Minha unidade
                - listitem [ref=e18]:
                  - link "Histórico" [ref=e19] [cursor=pointer]:
                    - /url: /historico
                    - generic [ref=e20]: 
                    - text: Histórico
              - list [ref=e21]:
                - listitem [ref=e22]:
                  - link "CHEFE - SECAO_221" [ref=e23] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e24]:
                      - generic [ref=e25]: 
                      - generic [ref=e26]: CHEFE - SECAO_221
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
                - link "SECAO_221" [ref=e47] [cursor=pointer]:
                  - /url: /processo/400/SECAO_221
                  - generic [ref=e48]: SECAO_221
              - listitem [ref=e49]:
                - text: ›
                - generic [ref=e50]: Mapa de competências
      - main [ref=e51]:
        - generic [ref=e52]:
          - generic [ref=e53]:
            - generic [ref=e54]:
              - heading "Mapa de competências técnicas" [level=2] [ref=e55]
              - paragraph [ref=e56]:
                - generic [ref=e57]: SECAO_221
            - generic [ref=e59]:
              - button "Histórico de análise" [ref=e60] [cursor=pointer]:
                - generic [ref=e61]: 
                - text: Histórico de análise
              - button "Ações" [ref=e63] [cursor=pointer]
          - generic [ref=e66]:
            - heading "Competência fixture - 400" [level=4] [ref=e68]:
              - strong [ref=e69]: Competência fixture - 400
            - generic [ref=e73]:
              - generic [ref=e74]: Atividade fixture - 400
              - list [ref=e76]:
                - listitem [ref=e77]: Conhecimento fixture - 400
      - contentinfo [ref=e78]:
        - generic [ref=e79]:
          - generic [ref=e80]: Versão 1.0.4
          - generic [ref=e81]: © SESEL/COSIS/TRE-PE
  - button "Enviar feedback" [ref=e82] [cursor=pointer]:
    - generic [ref=e83]: 
  - text:                 
```

# Test source

```ts
  15  | 
  16  | test.describe.serial('CDU-19 - Validar mapa de competências', () => {
  17  |     const UNIDADE_ALVO = 'SECAO_221';
  18  | 
  19  |     const timestamp = Date.now();
  20  |     const descProcesso = `Mapeamento CDU-19 ${timestamp}`;
  21  | 
  22  |     test('Setup data', async ({_resetAutomatico, request}) => {
  23  |         const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
  24  |             descricao: descProcesso,
  25  |             unidade: UNIDADE_ALVO
  26  |         });
  27  |         validarProcessoFixture(processo, descProcesso);
  28  |     });
  29  | 
  30  |     // TESTES PRINCIPAIS - CDU-19
  31  | 
  32  |     test('Cenários CDU-19: Fluxo completo de validação do mapa pelo CHEFE', async ({
  33  |                                                                                        _resetAutomatico,
  34  |                                                                                        page,
  35  |                                                                                        _autenticadoComoChefeSecao221
  36  |                                                                                    }) => {
  37  |         // Cenario 1: Navegação para visualização do mapa
  38  |         await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
  39  |         await acessarDetalhesProcesso(page, descProcesso);
  40  | 
  41  |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
  42  | 
  43  |         await navegarParaMapa(page);
  44  |         await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
  45  |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  46  | 
  47  |         // Cenario 2: Cancelar validação
  48  |         await abrirValidacaoMapa(page);
  49  |         const modal = page.getByRole('dialog');
  50  |         await expect(modal).toBeVisible();
  51  |         await expect(modal.getByText(/Confirma a validação/i)).toBeVisible();
  52  | 
  53  |         await page.getByTestId('btn-validar-mapa-cancelar').click();
  54  |         await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
  55  |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  56  | 
  57  |         // Cenario 3: Validar com sucesso
  58  |         await abrirValidacaoMapa(page);
  59  |         await expect(modal).toBeVisible();
  60  |         await page.getByTestId('btn-validar-mapa-confirmar').click();
  61  | 
  62  |         await verificarPaginaPainel(page);
  63  |         await acessarDetalhesProcesso(page, descProcesso);
  64  |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa validado/i);
  65  | 
  66  |         // CDU-19 Passo 5.4/5.5: verificar movimentação registrada no subprocesso com data/hora
  67  |         await navegarParaSubprocesso(page, UNIDADE_ALVO);
  68  |         const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
  69  |             .locator('tr', {hasText: /Mapa validado/i})
  70  |             .first();
  71  |         await expect(linhaMovimentacao).toBeVisible();
  72  |         await expect(linhaMovimentacao).toContainText(/\d{2}\/\d{2}\/\d{4}/);
  73  |         await expect(linhaMovimentacao).toContainText(/SECAO_221/i);
  74  |     });
  75  | });
  76  | 
  77  | test.describe.serial('CDU-19 - Apresentar sugestões e pré-preenchimento', () => {
  78  |     const UNIDADE_ALVO = 'SECAO_221';
  79  |     const GESTOR_SUPERIOR = USUARIOS.GESTOR_COORD_22;
  80  | 
  81  |     const timestamp = Date.now();
  82  |     const descProcesso = `Mapeamento CDU-19 Sugestoes ${timestamp}`;
  83  |     const TEXTO_SUGESTAO = 'Sugestão de ajuste na competência técnica';
  84  | 
  85  |     test('Setup data', async ({_resetAutomatico, request}) => {
  86  |         await resetDatabase(request);
  87  |         const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
  88  |             descricao: descProcesso,
  89  |             unidade: UNIDADE_ALVO
  90  |         });
  91  |         validarProcessoFixture(processo, descProcesso);
  92  |     });
  93  | 
  94  |     test('Cenario 1: CHEFE apresenta sugestões com sucesso', async ({
  95  |                                                                         _resetAutomatico,
  96  |                                                                         page,
  97  |                                                                         _autenticadoComoChefeSecao221
  98  |                                                                     }) => {
  99  |         await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
  100 |         await acessarDetalhesProcesso(page, descProcesso);
  101 | 
  102 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
  103 |         await navegarParaMapa(page);
  104 | 
  105 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  106 | 
  107 |         // Modal abre sem pré-preenchimento (mapa novo, sem sugestões anteriores)
  108 |         await abrirSugestoesMapa(page);
  109 |         const modal = page.getByRole('dialog');
  110 |         await expect(modal).toBeVisible();
  111 |         await expect(page.getByTestId('inp-sugestoes-mapa-texto')).toHaveText('');
  112 | 
  113 |         // Preenche e confirma
  114 |         await page.getByTestId('inp-sugestoes-mapa-texto').fill(TEXTO_SUGESTAO);
> 115 |         await page.getByTestId('btn-sugestoes-mapa-confirmar').click();
      |                                                                ^ Error: locator.click: Target page, context or browser has been closed
  116 | 
  117 |         await verificarPaginaPainel(page);
  118 |         await acessarDetalhesProcesso(page, descProcesso);
  119 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
  120 |     });
  121 | 
  122 |     test('Cenario 1b: GESTOR superior vê alerta de sugestões no painel', async ({_resetAutomatico, page}) => {
  123 |         // CDU-19 Passo 4.5: sistema cria alerta para unidade superior (COORD_22 acima de SECAO_221)
  124 |         await login(page, GESTOR_SUPERIOR.titulo, GESTOR_SUPERIOR.senha);
  125 | 
  126 |         const tabelaAlertas = page.getByTestId('tbl-alertas');
  127 |         const linhaAlerta = tabelaAlertas.locator('tr', {hasText: descProcesso}).first();
  128 |         await expect(linhaAlerta).toBeVisible();
  129 |         await expect(linhaAlerta).toContainText(/SECAO_221/i);
  130 |         await expect(linhaAlerta).toContainText(/\d{2}\/\d{2}\/\d{4}/);
  131 |     });
  132 | 
  133 |     test('Cenario 2: GESTOR devolve mapa para ajustes', async ({_resetAutomatico, page}) => {
  134 |         await login(page, GESTOR_SUPERIOR.titulo, GESTOR_SUPERIOR.senha);
  135 |         await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
  136 |         await navegarParaMapa(page);
  137 | 
  138 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  139 |         await abrirDevolucaoMapa(page);
  140 |         await page.getByTestId('inp-devolucao-mapa-obs').fill('Necessário rever competências');
  141 |         await page.getByTestId('btn-devolucao-mapa-confirmar').click();
  142 | 
  143 |         await verificarPaginaPainel(page);
  144 | 
  145 |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  146 |         await acessarDetalhesProcesso(page, descProcesso);
  147 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
  148 |         await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  149 |         await navegarParaMapa(page);
  150 |         await esperarMapaSomenteLeitura(page);
  151 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  152 |         await abrirSugestoesMapa(page);
  153 |         await expect(page.getByTestId('inp-sugestoes-mapa-texto')).toContainText(TEXTO_SUGESTAO);
  154 |         await page.getByTestId('btn-sugestoes-mapa-cancelar').click();
  155 |     });
  156 | 
  157 |     test('Cenario 3: CHEFE reabre modal com pré-preenchimento das sugestões anteriores', async ({
  158 |                                                                                                     _resetAutomatico,
  159 |                                                                                                     page,
  160 |                                                                                                     _autenticadoComoChefeSecao221
  161 |                                                                                                 }) => {
  162 |         await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
  163 |         await acessarDetalhesProcesso(page, descProcesso);
  164 | 
  165 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
  166 |         await navegarParaMapa(page);
  167 | 
  168 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  169 | 
  170 |         await abrirSugestoesMapa(page);
  171 |         await expect(page.getByTestId('inp-sugestoes-mapa-texto')).toContainText(TEXTO_SUGESTAO);
  172 | 
  173 |         // Cancela sem alterar o estado
  174 |         await page.getByTestId('btn-sugestoes-mapa-cancelar').click();
  175 |     });
  176 | });
  177 | 
```