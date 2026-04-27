# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-10.spec.ts >> CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos >> 1.3 Cenário adicional: disponibilizar revisão sem mudanças usando checkbox
- Location: e2e\cdu-10.spec.ts:80:5

# Error details

```
Error: expect(locator).toHaveText(expected) failed

Locator: getByTestId('subprocesso-header__txt-situacao')
Expected pattern: /Não iniciado/i
Received string:  "Revisão em andamento"
Timeout: 5000ms

Call log:
  - Expect "toHaveText" with timeout 5000ms
  - waiting for getByTestId('subprocesso-header__txt-situacao')
    9 × locator resolved to <span data-v-aac515c3="" data-testid="subprocesso-header__txt-situacao">Revisão em andamento</span>
      - unexpected value "Revisão em andamento"

```

# Page snapshot

```yaml
- generic [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - generic [ref=e5]:
      - navigation [ref=e6]:
        - generic [ref=e7]:
          - link "SGC" [ref=e8] [cursor=pointer]:
            - /url: /painel
          - generic [ref=e9]:
            - list [ref=e10]:
              - listitem [ref=e11]:
                - link "Painel" [ref=e12] [cursor=pointer]:
                  - /url: /painel
                  - generic [ref=e13]: 
                  - text: Painel
              - listitem [ref=e14]:
                - link "Minha unidade" [ref=e15] [cursor=pointer]:
                  - /url: /unidade/16
                  - generic [ref=e16]: 
                  - text: Minha unidade
              - listitem [ref=e17]:
                - link "Histórico" [ref=e18] [cursor=pointer]:
                  - /url: /historico
                  - generic [ref=e19]: 
                  - text: Histórico
            - list [ref=e20]:
              - listitem [ref=e21]:
                - link "CHEFE - SECAO_212" [ref=e22] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e23]:
                    - generic [ref=e24]: 
                    - generic [ref=e25]: CHEFE - SECAO_212
              - listitem "Sair" [ref=e26]:
                - link "Sair" [ref=e27] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e28]: Sair
                  - generic [ref=e29]: 
      - generic [ref=e32]:
        - button "Voltar" [active] [ref=e33] [cursor=pointer]:
          - generic [ref=e34]: 
        - tooltip "Voltar" [ref=e35]:
          - generic [ref=e38]: Voltar
        - navigation "breadcrumb" [ref=e39]:
          - list [ref=e40]:
            - listitem [ref=e41]:
              - link "Início" [ref=e42] [cursor=pointer]:
                - /url: /painel
                - generic [ref=e43]: 
                - generic [ref=e44]: Início
            - listitem [ref=e45]:
              - text: ›
              - generic [ref=e46]: SECAO_212
      - main [ref=e47]:
        - generic [ref=e49]:
          - generic [ref=e50]:
            - generic [ref=e52]:
              - heading "SECAO_212" [level=2] [ref=e53]
              - paragraph [ref=e54]: Seção 212
            - generic [ref=e56]:
              - paragraph [ref=e57]:
                - strong [ref=e58]: "Processo:"
                - text: Rev 10 sem mudancas 1777309839852
              - paragraph [ref=e59]: Situação:Revisão em andamento
              - paragraph [ref=e60]: Localização atual:SECAO_212
              - paragraph [ref=e61]: Prazo para conclusão da etapa atual:27/05/2026
              - paragraph [ref=e62]:
                - strong [ref=e63]: "Titular:"
                - text: Pete Townshend
              - paragraph [ref=e64]:
                - generic [ref=e65]:
                  - generic [ref=e66]: 
                  - text: "2026"
                - generic [ref=e67]:
                  - generic [ref=e68]: 
                  - link "pete.townshend@tre-pe.jus.br" [ref=e69] [cursor=pointer]:
                    - /url: mailto:pete.townshend@tre-pe.jus.br
          - generic [ref=e70]:
            - button "Atividades e conhecimentos Cadastro de atividades e conhecimentos da unidade" [ref=e72] [cursor=pointer]:
              - generic [ref=e74]:
                - heading "Atividades e conhecimentos" [level=4] [ref=e75]
                - paragraph [ref=e76]: Cadastro de atividades e conhecimentos da unidade
            - generic [ref=e80]:
              - heading "Mapa de competências" [level=4] [ref=e81]
              - paragraph [ref=e82]: Mapa de competências técnicas da unidade
          - generic [ref=e83]:
            - heading "Movimentações" [level=4] [ref=e84]
            - table [ref=e86]:
              - rowgroup [ref=e87]:
                - row "Data/hora Origem Destino Descrição" [ref=e88]:
                  - columnheader "Data/hora" [ref=e89]
                  - columnheader "Origem" [ref=e90]
                  - columnheader "Destino" [ref=e91]
                  - columnheader "Descrição" [ref=e92]
              - rowgroup [ref=e93]:
                - row "27/04/2026 14:10 ADMIN SECAO_212 Processo iniciado" [ref=e94]:
                  - cell "27/04/2026 14:10" [ref=e95]
                  - cell "ADMIN" [ref=e96]
                  - cell "SECAO_212" [ref=e97]
                  - cell "Processo iniciado" [ref=e98]
      - contentinfo [ref=e99]:
        - generic [ref=e100]:
          - generic [ref=e101]: Versão 1.0.0
          - generic [ref=e102]: © SESEL/COSIS/TRE-PE
  - text: 
```

# Test source

```ts
  23  | 
  24  | test.describe.serial('CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
  25  |     const UNIDADE_ALVO = 'SECAO_221';
  26  |     const timestamp = Date.now();
  27  |     const descProcessoRevisao = `Rev 10 ${timestamp}`;
  28  |     const descProcessoRevisaoSemMudancas = `Rev 10 sem mudancas ${timestamp}`;
  29  | 
  30  |     test('1. Setup: Preparar processo de revisão e atividades iniciais', async ({_resetAutomatico, request, page}) => {
  31  |         // Criar processo mapeamento finalizado (gera mapa vigente)
  32  |         await criarProcessoFinalizadoFixture(request, {
  33  |             unidade: UNIDADE_ALVO,
  34  |             descricao: `Base map 10 ${timestamp}`
  35  |         });
  36  | 
  37  |         // Criar processo de revisão
  38  |         const processo = await criarProcessoFixture(request, {
  39  |             descricao: descProcessoRevisao,
  40  |             tipo: 'REVISAO',
  41  |             unidade: UNIDADE_ALVO,
  42  |             iniciar: true
  43  |         });
  44  |         expect(processo.codigo).toBeGreaterThan(0);
  45  | 
  46  |         // Chefe revisa atividades (muda situação para EM_ANDAMENTO)
  47  |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  48  |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  49  |         await navegarParaAtividades(page);
  50  |         await adicionarAtividade(page, `Atividade revisão nova ${timestamp}`);
  51  |         await adicionarConhecimento(page, `Atividade revisão nova ${timestamp}`, 'Conhecimento revisão');
  52  |         await page.getByTestId('btn-nav-voltar').click();
  53  |         await verificarPaginaSubprocesso(page, UNIDADE_ALVO);
  54  |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
  55  |     });
  56  | 
  57  |     test('1.1 Cenário adicional: primeiro acesso direto ao cadastro carrega o subprocesso', async ({_resetAutomatico, page}) => {
  58  |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  59  |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  60  |         await navegarParaAtividades(page);
  61  | 
  62  |         await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO, level: 2})).toBeVisible();
  63  |         await expect(page.getByTestId('inp-nova-atividade')).toBeVisible();
  64  |         await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeVisible();
  65  |     });
  66  | 
  67  |     test('1.2 Cenário adicional: checkbox fica desabilitado quando houver mudanças no cadastro', async ({_resetAutomatico, page}) => {
  68  |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  69  |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  70  |         await navegarParaAtividades(page);
  71  | 
  72  |         const checkboxSemMudancas = page.getByTestId('chk-disponibilizacao-sem-mudancas');
  73  |         const botaoDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  74  | 
  75  |         await expect(checkboxSemMudancas).toBeVisible();
  76  |         await expect(checkboxSemMudancas).toBeDisabled();
  77  |         await expect(botaoDisponibilizar).toBeEnabled();
  78  |     });
  79  | 
  80  |     test('1.3 Cenário adicional: disponibilizar revisão sem mudanças usando checkbox', async ({_resetAutomatico, request, page}) => {
  81  |         const unidadeSemMudancas = 'SECAO_212';
  82  | 
  83  |         await criarProcessoFinalizadoFixture(request, {
  84  |             unidade: unidadeSemMudancas,
  85  |             descricao: `Base map 10 sem mudancas ${timestamp}`
  86  |         });
  87  | 
  88  |         const processoSemMudancas = await criarProcessoFixture(request, {
  89  |             descricao: descProcessoRevisaoSemMudancas,
  90  |             tipo: 'REVISAO',
  91  |             unidade: unidadeSemMudancas,
  92  |             iniciar: true
  93  |         });
  94  |         expect(processoSemMudancas.codigo).toBeGreaterThan(0);
  95  | 
  96  |         await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
  97  |         await acessarSubprocessoChefeDireto(page, descProcessoRevisaoSemMudancas, unidadeSemMudancas);
  98  |         await navegarParaAtividades(page);
  99  | 
  100 |         const checkboxSemMudancas = page.getByTestId('chk-disponibilizacao-sem-mudancas');
  101 |         await expect(checkboxSemMudancas).toBeVisible();
  102 |         await expect(checkboxSemMudancas).toBeEnabled();
  103 | 
  104 |         const botaoDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  105 |         await botaoDisponibilizar.click();
  106 |         await expect(page.getByText(TEXTOS.atividades.ERRO_REVISAO_SEM_ALTERACAO)).toBeVisible();
  107 | 
  108 |         await checkboxSemMudancas.check();
  109 |         await expect(checkboxSemMudancas).toBeChecked();
  110 |         await expect(botaoDisponibilizar).toBeEnabled();
  111 | 
  112 |         await page.getByTestId('btn-nav-voltar').click();
  113 |         await verificarPaginaSubprocesso(page, unidadeSemMudancas);
  114 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
  115 | 
  116 |         await navegarParaAtividades(page);
  117 |         await expect(checkboxSemMudancas).not.toBeChecked();
  118 |         await botaoDisponibilizar.click();
  119 |         await expect(page.getByText(TEXTOS.atividades.ERRO_REVISAO_SEM_ALTERACAO)).toBeVisible();
  120 | 
  121 |         await page.getByTestId('btn-nav-voltar').click();
  122 |         await verificarPaginaSubprocesso(page, unidadeSemMudancas);
> 123 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Não iniciado/i);
      |                                                                            ^ Error: expect(locator).toHaveText(expected) failed
  124 | 
  125 |         await navegarParaAtividades(page);
  126 |         await expect(checkboxSemMudancas).toBeEnabled();
  127 |         await checkboxSemMudancas.check();
  128 |         await expect(botaoDisponibilizar).toBeEnabled();
  129 | 
  130 |         await botaoDisponibilizar.click();
  131 |         await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
  132 |         await page.getByTestId('btn-confirmar-disponibilizacao').click();
  133 | 
  134 |         await expect(page.getByText(/disponibilizada?|Disponibilizado/i).first()).toBeVisible();
  135 |         await verificarPaginaPainel(page);
  136 |     });
  137 | 
  138 |     test('2. Cenário 1: Validação - Atividade sem conhecimento', async ({_resetAutomatico, page}) => {
  139 |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  140 |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  141 |         await navegarParaAtividades(page);
  142 | 
  143 |         const atividadeIncompleta = `Atividade incompleta ${timestamp}`;
  144 |         await adicionarAtividade(page, atividadeIncompleta);
  145 | 
  146 |         const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  147 |         await btnDisponibilizar.click();
  148 |         await expect(page.getByText(TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO)).toBeVisible();
  149 | 
  150 |         await adicionarConhecimento(page, atividadeIncompleta, 'Conhecimento corretivo');
  151 |         await limparNotificacoes(page);
  152 |         await btnDisponibilizar.click();
  153 |         await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
  154 |         await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();
  155 |     });
  156 | 
  157 |     test('3. Cenário 2: Caminho feliz - Disponibilizar revisão', async ({_resetAutomatico, page}) => {
  158 |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  159 |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  160 |         await navegarParaAtividades(page);
  161 |         await limparNotificacoes(page);
  162 |         const botaoDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  163 |         if (await botaoDisponibilizar.isDisabled()) {
  164 |             const checkboxSemMudancas = page.getByTestId('chk-disponibilizacao-sem-mudancas');
  165 |             if (await checkboxSemMudancas.count() > 0) {
  166 |                 await expect(checkboxSemMudancas).toBeVisible();
  167 |                 await expect(checkboxSemMudancas).toBeEnabled();
  168 |                 await checkboxSemMudancas.check();
  169 |                 await expect(checkboxSemMudancas).toBeChecked();
  170 |             }
  171 |         }
  172 |         if (await botaoDisponibilizar.isDisabled()) {
  173 |             const atividadeExtra = `Atividade revisão complementar ${Date.now()}`;
  174 |             await adicionarAtividade(page, atividadeExtra);
  175 |             await adicionarConhecimento(page, atividadeExtra, 'Conhecimento complementar');
  176 |         }
  177 |         await expect(botaoDisponibilizar).toBeEnabled();
  178 |         await botaoDisponibilizar.click();
  179 |         const modalConfirmacao = page.getByRole('dialog');
  180 |         await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TITULO)).toBeVisible();
  181 |         await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TEXTO)).toBeVisible();
  182 |         await page.getByTestId('btn-confirmar-disponibilizacao').click();
  183 | 
  184 |         await expect(page.getByText(/disponibilizada?|Disponibilizado/i).first()).toBeVisible();
  185 |         await verificarPaginaPainel(page);
  186 | 
  187 |         // Verificar alerta para o gestor superior
  188 |         await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
  189 |         await expect(page.getByTestId('tbl-alertas')).toContainText(TEXTOS.alerta.SUCESSO_REVISAO_DISPONIBILIZADA(UNIDADE_ALVO));
  190 | 
  191 |         await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
  192 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
  193 |         await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.REVISAO_CADASTRO_DISPONIBILIZADA);
  194 |     });
  195 | 
  196 |     test('4. Cenário 3: Devolução e Histórico', async ({_resetAutomatico, page}) => {
  197 |         await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
  198 |         await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
  199 |         await navegarParaAtividades(page);
  200 |         await page.getByTestId('btn-acao-devolver').click();
  201 |         const motivoDevolucao = 'Necessário revisar os conhecimentos técnicos.';
  202 |         await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivoDevolucao);
  203 |         await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
  204 |         await verificarPaginaPainel(page);
  205 | 
  206 |         // Verificar movimentação de devolução
  207 |         await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
  208 |         await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.REVISAO_CADASTRO_DEVOLVIDA);
  209 | 
  210 |         // Verificar alerta para o chefe da unidade
  211 |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  212 |         await limparNotificacoes(page);
  213 |         await expect(page.getByTestId('tbl-alertas').locator('tr', { hasText: TEXTOS.alerta.REVISAO_DEVOLVIDA(UNIDADE_ALVO) })).toBeVisible();
  214 | 
  215 |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  216 |         await navegarParaAtividades(page);
  217 |         const modal = await abrirHistoricoAnalise(page);
  218 |         await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
  219 |         await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivoDevolucao);
  220 |         await fecharHistoricoAnalise(page);
  221 | 
  222 |         await disponibilizarCadastro(page);
  223 |         await verificarPaginaPainel(page);
```