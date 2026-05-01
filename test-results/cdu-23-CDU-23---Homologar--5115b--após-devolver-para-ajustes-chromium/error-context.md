# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-23.spec.ts >> CDU-23 - Homologar cadastros em bloco após devolução >> Cenario 1: ADMIN não pode homologar em bloco após devolver para ajustes
- Location: e2e/cdu-23.spec.ts:161:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByRole('row', { name: /SECAO_221.*Devolvido para ajustes/i })
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByRole('row', { name: /SECAO_221.*Devolvido para ajustes/i })

```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
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
                - link "Unidades" [ref=e15] [cursor=pointer]:
                  - /url: /unidades
                  - generic [ref=e16]: 
                  - text: Unidades
              - listitem [ref=e17]:
                - link "Relatórios" [ref=e18] [cursor=pointer]:
                  - /url: /relatorios
                  - generic [ref=e19]: 
                  - text: Relatórios
              - listitem [ref=e20]:
                - link "Histórico" [ref=e21] [cursor=pointer]:
                  - /url: /historico
                  - generic [ref=e22]: 
                  - text: Histórico
            - list [ref=e23]:
              - listitem [ref=e24]:
                - link "ADMIN" [ref=e25] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e26]:
                    - generic [ref=e27]: 
                    - generic [ref=e28]: ADMIN
              - listitem "Notificações" [ref=e29]:
                - link "Notificações" [ref=e30] [cursor=pointer]:
                  - /url: /administracao/notificacoes
                  - generic [ref=e31]: Notificações
                  - generic [ref=e32]: 
              - listitem "Configurações" [ref=e33]:
                - link "Configurações" [ref=e34] [cursor=pointer]:
                  - /url: /configuracoes
                  - generic [ref=e35]: Configurações
                  - generic [ref=e36]: 
              - listitem "Administradores do sistema" [ref=e37]:
                - link "Administradores" [ref=e38] [cursor=pointer]:
                  - /url: /administradores
                  - generic [ref=e39]: Administradores
                  - generic [ref=e40]: 
              - listitem [ref=e41]:
                - generic [ref=e42]:
                  - button "Ações Especiais" [ref=e43] [cursor=pointer]:
                    - generic [ref=e44]: Ações Especiais
                    - generic [ref=e45]: 
                  - text: 
              - listitem "Sair" [ref=e46]:
                - link "Sair" [ref=e47] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e48]: Sair
                  - generic [ref=e49]: 
      - generic [ref=e52]:
        - button "Voltar" [ref=e53] [cursor=pointer]:
          - generic [ref=e54]: 
        - navigation "breadcrumb" [ref=e55]:
          - list [ref=e56]:
            - listitem [ref=e57]:
              - link "Início" [ref=e58] [cursor=pointer]:
                - /url: /painel
                - generic [ref=e59]: 
                - generic [ref=e60]: Início
            - listitem [ref=e61]:
              - text: ›
              - generic [ref=e62]: Detalhes do processo
      - main [ref=e63]:
        - generic [ref=e65]:
          - generic [ref=e66]:
            - generic [ref=e67]:
              - heading "Mapeamento CDU-23 Devolucao 1777675404579" [level=2] [ref=e68]
              - paragraph [ref=e69]:
                - generic [ref=e70]:
                  - generic [ref=e71]:
                    - strong [ref=e72]: "Tipo:"
                    - text: Mapeamento
                  - generic [ref=e73]:
                    - strong [ref=e74]: "Situação:"
                    - text: Em andamento
            - generic [ref=e75]:
              - button "Finalizar" [disabled]
              - button "Ações em bloco" [ref=e77] [cursor=pointer]
          - table [ref=e80]:
            - rowgroup [ref=e85]:
              - row "Unidade participante Situação Data limite" [ref=e86]:
                - columnheader "Unidade participante" [ref=e87]
                - columnheader "Situação" [ref=e88]
                - columnheader "Data limite" [ref=e89]
            - rowgroup [ref=e90]:
              - row "SECAO_221 - Seção 221 Cadastro disponibilizado 31/05/2026" [ref=e91] [cursor=pointer]:
                - cell "SECAO_221 - Seção 221" [ref=e92]:
                  - generic [ref=e95]: SECAO_221 - Seção 221
                - cell "Cadastro disponibilizado" [ref=e96]
                - cell "31/05/2026" [ref=e97]
      - contentinfo [ref=e98]:
        - generic [ref=e99]:
          - generic [ref=e100]: Versão 1.0.0
          - generic [ref=e101]: © SESEL/COSIS/TRE-PE
```

# Test source

```ts
  72  |         await expect(modal).toHaveClass(/show/);
  73  |         await expect(modal.getByText(TEXTOS.acaoBloco.homologar.TITULO_CADASTRO)).toBeVisible();
  74  |         await expect(modal.getByText(TEXTOS.acaoBloco.homologar.TEXTO_CADASTRO)).toBeVisible();
  75  |         await expect(modal.locator('table')).toBeVisible();
  76  |         await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
  77  |         await expect(modal.getByRole('button', {name: TEXTOS.acaoBloco.homologar.BOTAO})).toBeVisible();
  78  |         await modal.getByRole('button', {name: /Cancelar/i}).click();
  79  | 
  80  |         await expect(modal).not.toHaveClass(/show/);
  81  |         await expect(page.getByTestId('processo-info')).toBeVisible();
  82  |     });
  83  | 
  84  |     test('Cenario 2: ADMIN confirma homologação em bloco e permanece na tela', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  85  |         await acessarDetalhesProcesso(page, descProcesso);
  86  |         const btnHomologar = await obterAcaoBloco(page, 'btn-processo-homologar-bloco');
  87  |         await expect(btnHomologar).toBeVisible();
  88  |         await btnHomologar.click();
  89  | 
  90  |         const modal = page.locator('#modal-acao-bloco');
  91  |         await expect(modal).toHaveClass(/show/);
  92  |         await modal.getByRole('button', {name: TEXTOS.acaoBloco.homologar.BOTAO}).click();
  93  | 
  94  |         await expect(page.getByText(TEXTOS.sucesso.CADASTROS_HOMOLOGADOS_EM_BLOCO).first()).toBeVisible();
  95  | 
  96  |         await expect(page).toHaveURL(/\/processo\/\d+(?:\?.*)?$/);
  97  |         await expect(page.getByTestId('processo-info')).toBeVisible();
  98  |         await expect(page.getByTestId('app-alert')).toContainText(TEXTOS.sucesso.CADASTROS_HOMOLOGADOS_EM_BLOCO);
  99  |         await expect(btnHomologar).toBeDisabled();
  100 |         await expect(page.getByRole('row', {name: /SECAO_221 - Seção 221 Cadastro homologado/i})).toBeVisible();
  101 |     });
  102 | 
  103 |     test('Cenario 3: Homologação em bloco registra movimentação e alerta com data/hora', async ({
  104 |         _resetAutomatico,
  105 |         page,
  106 |         _autenticadoComoAdmin
  107 |     }) => {
  108 |         // Processo da suíte já foi homologado no Cenario 2 — verificar movimentação e alerta
  109 |         await acessarDetalhesProcesso(page, descProcesso);
  110 |         await navegarParaSubprocesso(page, UNIDADE_1);
  111 | 
  112 |         const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
  113 |             .locator('tr', {hasText: /Cadastro homologado/i})
  114 |             .first();
  115 |         await expect(linhaMovimentacao).toBeVisible();
  116 |         await expect(linhaMovimentacao).toContainText(/\d{2}\/\d{2}\/\d{4}/);
  117 |         await expect(linhaMovimentacao).toContainText('ADMIN');
  118 | 
  119 |         // Verificar alerta para o chefe da unidade do subprocesso (SECAO_221)
  120 |         await fazerLogout(page);
  121 |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  122 | 
  123 |         const tabelaAlertas = page.getByTestId('tbl-alertas');
  124 |         const linhaAlerta = tabelaAlertas.locator('tr', {hasText: descProcesso})
  125 |             .filter({hasText: /homologado/i})
  126 |             .first();
  127 |         await expect(linhaAlerta).toBeVisible();
  128 |         await expect(linhaAlerta).toContainText(/SECAO_221/i);
  129 |         await expect(linhaAlerta).toContainText(/homologado/i);
  130 |         await expect(linhaAlerta).toContainText(/\d{2}\/\d{2}\/\d{4}/);
  131 |     });
  132 | });
  133 | 
  134 | test.describe.serial('CDU-23 - Homologar cadastros em bloco após devolução', () => {
  135 |     const UNIDADE_1 = 'SECAO_221';
  136 |     const timestamp = Date.now();
  137 |     const descProcesso = `Mapeamento CDU-23 Devolucao ${timestamp}`;
  138 | 
  139 |     test('Setup data', async ({_resetAutomatico, request}) => {
  140 |         await resetDatabase(request);
  141 |         const processo = await criarProcessoCadastroDisponibilizadoFixture(request, {
  142 |             descricao: descProcesso,
  143 |             unidade: UNIDADE_1
  144 |         });
  145 |         validarProcessoFixture(processo, descProcesso);
  146 |     });
  147 | 
  148 |     test('Setup aceites', async ({_resetAutomatico, page, _autenticadoComoGestorCoord22}) => {
  149 |         await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
  150 |         await navegarParaCadastro(page);
  151 |         await aceitarCadastroMapeamento(page);
  152 | 
  153 |         await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
  154 |         await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
  155 |         await navegarParaCadastro(page);
  156 |         await aceitarCadastroMapeamento(page);
  157 | 
  158 |         await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
  159 |     });
  160 | 
  161 |     test('Cenario 1: ADMIN não pode homologar em bloco após devolver para ajustes', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  162 |         await acessarDetalhesProcesso(page, descProcesso);
  163 |         await navegarParaSubprocesso(page, UNIDADE_1);
  164 |         await navegarParaCadastro(page);
  165 |         await devolverCadastroMapeamento(page, 'Ajustar cadastro antes da homologação');
  166 | 
  167 |         await acessarDetalhesProcesso(page, descProcesso);
  168 |         await page.reload();
  169 |         await expect(page.getByTestId('processo-info')).toBeVisible();
  170 |         
  171 |         // Garante que o estado do subprocesso foi atualizado na UI antes de verificar botões de ação em bloco
> 172 |         await expect(page.getByRole('row', {name: new RegExp(`${UNIDADE_1}.*Devolvido para ajustes`, 'i')})).toBeVisible();
      |                                                                                                              ^ Error: expect(locator).toBeVisible() failed
  173 | 
  174 |         const btnHomologar = await obterAcaoBloco(page, 'btn-processo-homologar-bloco');
  175 |         await expect(btnHomologar).toBeDisabled();
  176 |     });
  177 | });
  178 | 
```