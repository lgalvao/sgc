# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-10.spec.ts >> CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos >> 1.3 Cenário adicional: disponibilizar revisão sem mudanças usando checkbox
- Location: e2e/cdu-10.spec.ts:81:5

# Error details

```
Error: expect(locator).toBeDisabled() failed

Locator: getByTestId('btn-cad-atividades-disponibilizar')
Expected: disabled
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeDisabled" with timeout 5000ms
  - waiting for getByTestId('btn-cad-atividades-disponibilizar')

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
        - button "Voltar" [ref=e33] [cursor=pointer]:
          - generic [ref=e34]: 
        - navigation "breadcrumb" [ref=e35]:
          - list [ref=e36]:
            - listitem [ref=e37]:
              - link "Início" [ref=e38] [cursor=pointer]:
                - /url: /painel
                - generic [ref=e39]: 
                - generic [ref=e40]: Início
            - listitem [ref=e41]:
              - text: ›
              - link "SECAO_212" [ref=e42] [cursor=pointer]:
                - /url: /processo/403/SECAO_212
            - listitem [ref=e43]:
              - text: ›
              - generic [ref=e44]: Atividades e conhecimentos
      - main [ref=e45]:
        - generic [ref=e46]:
          - generic [ref=e47]:
            - generic [ref=e48]:
              - heading "Atividades e conhecimentos" [level=2] [ref=e49]
              - paragraph [ref=e50]: SECAO_212
            - generic [ref=e51]:
              - button "Histórico de análise" [ref=e53] [cursor=pointer]:
                - generic [ref=e54]: 
                - text: Histórico de análise
              - generic [ref=e55]:
                - button "Impacto no mapa" [ref=e56] [cursor=pointer]:
                  - generic [ref=e57]: 
                  - text: Impacto no mapa
                - button "Importar" [ref=e58] [cursor=pointer]:
                  - generic [ref=e59]: 
                  - text: Importar
          - generic [ref=e61]:
            - checkbox "Disponibilização sem mudanças" [ref=e62]
            - generic [ref=e63]: Disponibilização sem mudanças
          - generic [ref=e64]:
            - textbox "Nova atividade" [ref=e66]
            - button "Adicionar atividade" [ref=e68] [cursor=pointer]:
              - generic [ref=e69]: 
          - generic [ref=e72]:
            - 'heading "Editar Remover atividade: Atividade origem B - 402 Atividade origem B - 402" [level=4] [ref=e73]':
              - generic [ref=e74]:
                - generic [ref=e75]:
                  - button "Editar" [ref=e76] [cursor=pointer]:
                    - generic [ref=e77]: 
                  - 'button "Remover atividade: Atividade origem B - 402" [ref=e78] [cursor=pointer]':
                    - generic [ref=e79]: 
                - strong [ref=e81]: Atividade origem B - 402
            - generic [ref=e82]:
              - generic [ref=e84]:
                - textbox "Novo conhecimento" [ref=e86]
                - button "Adicionar conhecimento" [ref=e88] [cursor=pointer]:
                  - generic [ref=e89]: 
              - generic [ref=e91]:
                - generic [ref=e92]:
                  - button "Editar" [ref=e93] [cursor=pointer]:
                    - generic [ref=e94]: 
                  - 'button "Remover conhecimento: Conhecimento B - 402" [ref=e95] [cursor=pointer]':
                    - generic [ref=e96]: 
                - generic [ref=e97]: Conhecimento B - 402
          - generic [ref=e100]:
            - 'heading "Editar Remover atividade: Atividade origem A - 402 Atividade origem A - 402" [level=4] [ref=e101]':
              - generic [ref=e102]:
                - generic [ref=e103]:
                  - button "Editar" [ref=e104] [cursor=pointer]:
                    - generic [ref=e105]: 
                  - 'button "Remover atividade: Atividade origem A - 402" [ref=e106] [cursor=pointer]':
                    - generic [ref=e107]: 
                - strong [ref=e109]: Atividade origem A - 402
            - generic [ref=e110]:
              - generic [ref=e112]:
                - textbox "Novo conhecimento" [ref=e114]
                - button "Adicionar conhecimento" [ref=e116] [cursor=pointer]:
                  - generic [ref=e117]: 
              - generic [ref=e119]:
                - generic [ref=e120]:
                  - button "Editar" [ref=e121] [cursor=pointer]:
                    - generic [ref=e122]: 
                  - 'button "Remover conhecimento: Conhecimento A - 402" [ref=e123] [cursor=pointer]':
                    - generic [ref=e124]: 
                - generic [ref=e125]: Conhecimento A - 402
      - contentinfo [ref=e126]:
        - generic [ref=e127]:
          - generic [ref=e128]: Versão 1.0.0
          - generic [ref=e129]: © SESEL/COSIS/TRE-PE
  - text:  
```

# Test source

```ts
  6   |     adicionarAtividade,
  7   |     adicionarConhecimento,
  8   |     disponibilizarCadastro,
  9   |     navegarParaCadastro
  10  | } from './helpers/helpers-atividades.js';
  11  | import {
  12  |     abrirHistoricoAnalise,
  13  |     abrirAcaoCadastroDevolver,
  14  |     acessarSubprocessoChefeDireto,
  15  |     acessarSubprocessoGestor,
  16  |     fecharHistoricoAnalise,
  17  | } from './helpers/helpers-analise.js';
  18  | import {limparNotificacoes, verificarPaginaPainel,} from './helpers/helpers-navegacao.js';
  19  | import {TEXTOS} from '../frontend/src/constants/textos.js';
  20  | 
  21  | async function verificarPaginaSubprocesso(page: Page, unidade: string) {
  22  |     await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}(?:/)?(?:\?.*)?$`));
  23  | }
  24  | 
  25  | test.describe.serial('CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
  26  |     const UNIDADE_ALVO = 'SECAO_221';
  27  |     const timestamp = Date.now();
  28  |     const descProcessoRevisao = `Rev 10 ${timestamp}`;
  29  |     const descProcessoRevisaoSemMudancas = `Rev 10 sem mudancas ${timestamp}`;
  30  | 
  31  |     test('1. Setup: Preparar processo de revisão e atividades iniciais', async ({_resetAutomatico, request, page}) => {
  32  |         // Criar processo mapeamento finalizado (gera mapa vigente)
  33  |         await criarProcessoFinalizadoFixture(request, {
  34  |             unidade: UNIDADE_ALVO,
  35  |             descricao: `Base map 10 ${timestamp}`
  36  |         });
  37  | 
  38  |         // Criar processo de revisão
  39  |         const processo = await criarProcessoFixture(request, {
  40  |             descricao: descProcessoRevisao,
  41  |             tipo: 'REVISAO',
  42  |             unidade: UNIDADE_ALVO,
  43  |             iniciar: true
  44  |         });
  45  |         expect(processo.codigo).toBeGreaterThan(0);
  46  | 
  47  |         // Chefe revisa atividades (muda situação para EM_ANDAMENTO)
  48  |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  49  |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  50  |         await navegarParaCadastro(page);
  51  |         await adicionarAtividade(page, `Atividade revisão nova ${timestamp}`);
  52  |         await adicionarConhecimento(page, `Atividade revisão nova ${timestamp}`, 'Conhecimento revisão');
  53  |         await page.getByTestId('btn-nav-voltar').click();
  54  |         await verificarPaginaSubprocesso(page, UNIDADE_ALVO);
  55  |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
  56  |     });
  57  | 
  58  |     test('1.1 Cenário adicional: primeiro acesso direto ao cadastro carrega o subprocesso', async ({_resetAutomatico, page}) => {
  59  |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  60  |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  61  |         await navegarParaCadastro(page);
  62  | 
  63  |         await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO, level: 2})).toBeVisible();
  64  |         await expect(page.getByTestId('inp-nova-atividade')).toBeVisible();
  65  |         await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeVisible();
  66  |     });
  67  | 
  68  |     test('1.2 Cenário adicional: checkbox fica desabilitado quando houver mudanças no cadastro', async ({_resetAutomatico, page}) => {
  69  |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  70  |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  71  |         await navegarParaCadastro(page);
  72  | 
  73  |         const checkboxSemMudancas = page.getByTestId('chk-disponibilizacao-sem-mudancas');
  74  |         const botaoDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  75  | 
  76  |         await expect(checkboxSemMudancas).toBeVisible();
  77  |         await expect(checkboxSemMudancas).toBeDisabled();
  78  |         await expect(botaoDisponibilizar).toBeEnabled();
  79  |     });
  80  | 
  81  |     test('1.3 Cenário adicional: disponibilizar revisão sem mudanças usando checkbox', async ({_resetAutomatico, request, page}) => {
  82  |         const unidadeSemMudancas = 'SECAO_212';
  83  | 
  84  |         await criarProcessoFinalizadoFixture(request, {
  85  |             unidade: unidadeSemMudancas,
  86  |             descricao: `Base map 10 sem mudancas ${timestamp}`
  87  |         });
  88  | 
  89  |         const processoSemMudancas = await criarProcessoFixture(request, {
  90  |             descricao: descProcessoRevisaoSemMudancas,
  91  |             tipo: 'REVISAO',
  92  |             unidade: unidadeSemMudancas,
  93  |             iniciar: true
  94  |         });
  95  |         expect(processoSemMudancas.codigo).toBeGreaterThan(0);
  96  | 
  97  |         await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
  98  |         await acessarSubprocessoChefeDireto(page, descProcessoRevisaoSemMudancas, unidadeSemMudancas);
  99  |         await navegarParaCadastro(page);
  100 | 
  101 |         const checkboxSemMudancas = page.getByTestId('chk-disponibilizacao-sem-mudancas');
  102 |         await expect(checkboxSemMudancas).toBeVisible();
  103 |         await expect(checkboxSemMudancas).toBeEnabled();
  104 | 
  105 |         const botaoDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
> 106 |         await expect(botaoDisponibilizar).toBeDisabled();
      |                                           ^ Error: expect(locator).toBeDisabled() failed
  107 | 
  108 |         await checkboxSemMudancas.check();
  109 |         await expect(checkboxSemMudancas).toBeChecked();
  110 |         await expect(botaoDisponibilizar).toBeEnabled();
  111 | 
  112 |         await page.getByTestId('btn-nav-voltar').click();
  113 |         await verificarPaginaSubprocesso(page, unidadeSemMudancas);
  114 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
  115 | 
  116 |         await navegarParaCadastro(page);
  117 |         await expect(botaoDisponibilizar).toBeEnabled();
  118 |         await expect(checkboxSemMudancas).toBeChecked();
  119 |         await checkboxSemMudancas.uncheck();
  120 |         await expect(checkboxSemMudancas).not.toBeChecked();
  121 |         await expect(botaoDisponibilizar).toBeDisabled();
  122 | 
  123 |         await page.getByTestId('btn-nav-voltar').click();
  124 |         await verificarPaginaSubprocesso(page, unidadeSemMudancas);
  125 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Não iniciado/i);
  126 | 
  127 |         await navegarParaCadastro(page);
  128 |         await expect(checkboxSemMudancas).toBeEnabled();
  129 |         await checkboxSemMudancas.check();
  130 |         await expect(botaoDisponibilizar).toBeEnabled();
  131 | 
  132 |         await botaoDisponibilizar.click();
  133 |         await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
  134 |         await page.getByTestId('btn-confirmar-disponibilizacao').click();
  135 | 
  136 |         await expect(page.getByText(/disponibilizada?|Disponibilizado/i).first()).toBeVisible();
  137 |         await verificarPaginaPainel(page);
  138 |     });
  139 | 
  140 |     test('2. Cenário 1: Validação - Atividade sem conhecimento', async ({_resetAutomatico, page}) => {
  141 |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  142 |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  143 |         await navegarParaCadastro(page);
  144 | 
  145 |         const atividadeIncompleta = `Atividade incompleta ${timestamp}`;
  146 |         await adicionarAtividade(page, atividadeIncompleta);
  147 | 
  148 |         const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  149 |         await btnDisponibilizar.click();
  150 |         await expect(page.getByText(TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO)).toBeVisible();
  151 | 
  152 |         await adicionarConhecimento(page, atividadeIncompleta, 'Conhecimento corretivo');
  153 |         await limparNotificacoes(page);
  154 |         await btnDisponibilizar.click();
  155 |         await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
  156 |         await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();
  157 |     });
  158 | 
  159 |     test('3. Cenário 2: Caminho feliz - Disponibilizar revisão', async ({_resetAutomatico, page}) => {
  160 |         await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  161 |         await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
  162 |         await navegarParaCadastro(page);
  163 |         await limparNotificacoes(page);
  164 |         const botaoDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  165 |         if (await botaoDisponibilizar.isDisabled()) {
  166 |             const checkboxSemMudancas = page.getByTestId('chk-disponibilizacao-sem-mudancas');
  167 |             if (await checkboxSemMudancas.count() > 0) {
  168 |                 await expect(checkboxSemMudancas).toBeVisible();
  169 |                 await expect(checkboxSemMudancas).toBeEnabled();
  170 |                 await checkboxSemMudancas.check();
  171 |                 await expect(checkboxSemMudancas).toBeChecked();
  172 |             }
  173 |         }
  174 |         if (await botaoDisponibilizar.isDisabled()) {
  175 |             const atividadeExtra = `Atividade revisão complementar ${Date.now()}`;
  176 |             await adicionarAtividade(page, atividadeExtra);
  177 |             await adicionarConhecimento(page, atividadeExtra, 'Conhecimento complementar');
  178 |         }
  179 |         await expect(botaoDisponibilizar).toBeEnabled();
  180 |         await botaoDisponibilizar.click();
  181 |         const modalConfirmacao = page.getByRole('dialog');
  182 |         await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TITULO)).toBeVisible();
  183 |         await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TEXTO)).toBeVisible();
  184 |         await page.getByTestId('btn-confirmar-disponibilizacao').click();
  185 | 
  186 |         await expect(page.getByText(/disponibilizada?|Disponibilizado/i).first()).toBeVisible();
  187 |         await verificarPaginaPainel(page);
  188 | 
  189 |         // Verificar alerta para o gestor superior
  190 |         await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
  191 |         await expect(page.getByTestId('tbl-alertas')).toContainText(TEXTOS.alerta.SUCESSO_REVISAO_DISPONIBILIZADA(UNIDADE_ALVO));
  192 | 
  193 |         await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
  194 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
  195 |         await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.REVISAO_CADASTRO_DISPONIBILIZADA);
  196 |     });
  197 | 
  198 |     test('4. Cenário 3: Devolução e Histórico', async ({_resetAutomatico, page}) => {
  199 |         await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
  200 |         await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
  201 |         await navegarParaCadastro(page);
  202 |         await (await abrirAcaoCadastroDevolver(page)).click();
  203 |         const motivoDevolucao = 'Necessário revisar os conhecimentos técnicos.';
  204 |         await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivoDevolucao);
  205 |         await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
  206 |         await verificarPaginaPainel(page);
```