# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-08.spec.ts >> CDU-08 - Manter cadastro de atividades e conhecimentos >> Cenário 1: Processo de Mapeamento (Fluxo completo + Importação + Auto-save)
- Location: e2e/cdu-08.spec.ts:16:5

# Error details

```
Test timeout of 20000ms exceeded.
```

```
Error: locator.waitFor: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('btn-cad-atividades-disponibilizar') to be visible

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
                  - /url: /unidade/3
                  - generic [ref=e16]: 
                  - text: Minha unidade
              - listitem [ref=e17]:
                - link "Histórico" [ref=e18] [cursor=pointer]:
                  - /url: /historico
                  - generic [ref=e19]: 
                  - text: Histórico
            - list [ref=e20]:
              - listitem [ref=e21]:
                - link "CHEFE - ASSESSORIA_11" [ref=e22] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e23]:
                    - generic [ref=e24]: 
                    - generic [ref=e25]: CHEFE - ASSESSORIA_11
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
              - link "ASSESSORIA_11" [ref=e42] [cursor=pointer]:
                - /url: /processo/402/ASSESSORIA_11
            - listitem [ref=e43]:
              - text: ›
              - generic [ref=e44]: Atividades e conhecimentos
      - main [ref=e45]:
        - generic [ref=e46]:
          - generic [ref=e47]:
            - generic [ref=e48]:
              - heading "Atividades e conhecimentos" [level=2] [ref=e49]
              - paragraph [ref=e50]: ASSESSORIA_11
            - generic [ref=e51]:
              - button "Histórico de análise" [ref=e53] [cursor=pointer]:
                - generic [ref=e54]: 
                - text: Histórico de análise
              - button "Importar" [ref=e56] [cursor=pointer]:
                - generic [ref=e57]: 
                - text: Importar
          - generic [ref=e58]:
            - textbox "Nova atividade" [ref=e60]
            - button "Adicionar atividade" [ref=e62] [cursor=pointer]:
              - generic [ref=e63]: 
          - region "Estado vazio" [ref=e64]:
            - generic [ref=e66]: 
            - heading "Lista de atividades" [level=2] [ref=e67]
            - paragraph [ref=e68]: Não há atividades cadastradas. Use o campo acima para adicionar uma atividade ou importe de outro processo.
      - contentinfo [ref=e69]:
        - generic [ref=e70]:
          - generic [ref=e71]: Versão 1.0.0
          - generic [ref=e72]: © SESEL/COSIS/TRE-PE
  - text:  
```

# Test source

```ts
  1   | // noinspection JSUnusedLocalSymbols
  2   | 
  3   | import {expect, test} from './fixtures/complete-fixtures.js';
  4   | import {login, USUARIOS} from './helpers/helpers-auth.js';
  5   | import * as AtividadeHelpers from './helpers/helpers-atividades.js';
  6   | import {fazerLogout} from './helpers/helpers-navegacao.js';
  7   | import {criarProcessoFinalizadoFixture, criarProcessoFixture} from './fixtures/index.js';
  8   | import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
  9   | 
  10  | test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
  11  |     const UNIDADE_ALVO = 'ASSESSORIA_11';
  12  |     const UNIDADE_ORIGEM = 'ASSESSORIA_12';
  13  |     const CHEFE_UNIDADE = USUARIOS.CHEFE_ASSESSORIA_11.titulo;
  14  |     const SENHA_CHEFE = USUARIOS.CHEFE_ASSESSORIA_11.senha;
  15  | 
  16  |     test('Cenário 1: Processo de Mapeamento (Fluxo completo + Importação + Auto-save)', async ({
  17  |                                                                                         _resetAutomatico,
  18  |                                                                                         page,
  19  |                                                                                         request,
  20  |                                                                                         _autenticadoComoAdmin
  21  | }) => {
  22  |         const timestamp = Date.now();
  23  |         const descricaoProcesso = `Processo CDU-08 Map ${timestamp}`;
  24  |         const processoOrigemDescricao = `Processo base FINALIZADO ${timestamp}`;
  25  |         const processoOrigem2Descricao = `Processo base FINALIZADO 2 ${timestamp}`;
  26  |         let processoOrigemId: number;
  27  |         let atividadeA = '';
  28  |         let atividadeB = '';
  29  | 
  30  |         await test.step('1. Setup: Criar processos origem e Mapeamento alvo', async () => {
  31  |             
  32  |             // Criar processos finalizados via Fixture (para importação)
  33  |             const procOrigem = await criarProcessoFinalizadoFixture(request, {
  34  |                 unidade: UNIDADE_ORIGEM,
  35  |                 descricao: processoOrigemDescricao
  36  |             });
  37  |             processoOrigemId = procOrigem.codigo;
  38  | 
  39  |             await criarProcessoFinalizadoFixture(request, {
  40  |                 unidade: 'ASSESSORIA_21',
  41  |                 descricao: processoOrigem2Descricao
  42  |             });
  43  | 
  44  |             await criarProcessoFixture(request, {
  45  |                 unidade: UNIDADE_ALVO,
  46  |                 descricao: descricaoProcesso,
  47  |                 iniciar: true,
  48  |                 diasLimite: 30
  49  |             });
  50  | 
  51  |             await fazerLogout(page);
  52  |         });
  53  | 
  54  |         await test.step('2. Acessar tela de Atividades', async () => {
  55  |             await login(page, CHEFE_UNIDADE, SENHA_CHEFE);
  56  |             await page.goto('/painel');
  57  |             await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();
  58  |             await acessarDetalhesProcesso(page, descricaoProcesso);
  59  |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}(?:\?.*)?$`));
  60  |             await AtividadeHelpers.navegarParaCadastro(page);
  61  |         });
  62  | 
  63  |         await test.step('2.1 Verificar estado inicial do mapeamento', async () => {
  64  |             await expect(page.getByTestId('cad-atividades-empty-state')).toBeVisible();
  65  |             const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
> 66  |             await btnDisponibilizar.waitFor({ state: 'visible' });
      |                                     ^ Error: locator.waitFor: Target page, context or browser has been closed
  67  |             await expect(btnDisponibilizar).toBeDisabled();
  68  |         });
  69  | 
  70  |         await test.step('3. Importar atividades (Fluxo múltiplo e Negativo)', async () => {
  71  |             atividadeA = `Atividade origem A - ${processoOrigemId}`;
  72  |             atividadeB = `Atividade origem B - ${processoOrigemId}`;
  73  |             
  74  |             await AtividadeHelpers.verificarOpcoesImportacaoVazia(page, [
  75  |                 { processo: processoOrigemDescricao, unidades: [UNIDADE_ORIGEM] },
  76  |                 { processo: processoOrigem2Descricao, unidades: ['ASSESSORIA_21'] }
  77  |             ]);
  78  | 
  79  |             await AtividadeHelpers.importarAtividadesVazia(page, processoOrigemDescricao, UNIDADE_ORIGEM, [atividadeA, atividadeB]);
  80  | 
  81  |             // A importação deve habilitar a disponibilização imediatamente
  82  |             await AtividadeHelpers.verificarBotaoDisponibilizar(page, true);
  83  | 
  84  |             // Tentar importar de novo com atividade já existente:
  85  |             // o modal deve fechar (sem erro) e um aviso deve ser exibido
  86  |             await AtividadeHelpers.importarAtividadesComAvisoDuplicidade(page, processoOrigemDescricao, UNIDADE_ORIGEM, [atividadeA]);
  87  |             await expect(page.getByText(/não foram importadas/i).first()).toBeVisible();
  88  |         });
  89  | 
  90  |         const atividadeManual = `Atividade manual ${timestamp}`;
  91  | 
  92  |         await test.step('4. Flexibilidade de Fluxo, Cadastro manual e Validar auto-save', async () => {
  93  |             await AtividadeHelpers.adicionarAtividade(page, atividadeManual);
  94  | 
  95  |             const atividadeManual2 = `Atividade manual 2 ${timestamp}`;
  96  |             await AtividadeHelpers.adicionarAtividade(page, atividadeManual2);
  97  | 
  98  |             const conhecimento1 = `Conhecimento manual ${timestamp}`;
  99  |             await AtividadeHelpers.adicionarConhecimento(page, atividadeManual, conhecimento1);
  100 |             const conhecimento2 = `Conhecimento manual 2 ${timestamp}`;
  101 |             await AtividadeHelpers.adicionarConhecimento(page, atividadeManual2, conhecimento2);
  102 | 
  103 |             // Recarregar a página para atestar que os dados estão sendo persistidos
  104 |             await page.reload();
  105 | 
  106 |             // Ao recarregar, tudo o que foi inserido precisa estar lá
  107 |             await expect(page.getByText(atividadeManual, { exact: true }).first()).toBeVisible();
  108 |             await expect(page.getByText(atividadeManual2, { exact: true }).first()).toBeVisible();
  109 |             await expect(page.locator('.group-conhecimento', { hasText: conhecimento1 }).first()).toBeVisible();
  110 |             await expect(page.locator('.group-conhecimento', { hasText: conhecimento2 }).first()).toBeVisible();
  111 |             await expect(page.getByText(atividadeA, { exact: true }).first()).toBeVisible();
  112 |         });
  113 | 
  114 |         await test.step('5. Editar e Remover (Com cancelamentos visuais)', async () => {
  115 |             const atividadeEditada = `${atividadeManual} EDITADA`;
  116 |             const atividadeCancelada = `${atividadeManual} CANCELADA`;
  117 |             
  118 |             await AtividadeHelpers.cancelarEdicaoAtividade(page, atividadeManual, atividadeCancelada);
  119 |             await AtividadeHelpers.editarAtividade(page, atividadeManual, atividadeEditada);
  120 | 
  121 |             const conhecimento1 = `Conhecimento manual ${timestamp}`;
  122 |             const conhecimentoCancelado = `${conhecimento1} CANCELADO`;
  123 |             const conhecimento1Editado = `${conhecimento1} EDITADO`;
  124 | 
  125 |             await AtividadeHelpers.cancelarEdicaoConhecimento(page, atividadeEditada, conhecimento1, conhecimentoCancelado);
  126 |             // Editar de fato
  127 |             await AtividadeHelpers.editarConhecimento(page, atividadeEditada, conhecimento1, conhecimento1Editado);
  128 | 
  129 |             // CDU-08 Item 20: após salvar edição de conhecimento, botões Editar e Remover voltam a aparecer
  130 |             const cardPosEdicao = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeEditada)});
  131 |             const linhaConhPosEdicao = cardPosEdicao.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimento1Editado});
  132 |             await linhaConhPosEdicao.hover();
  133 |             await expect(linhaConhPosEdicao.getByTestId('btn-editar-conhecimento')).toBeVisible();
  134 |             await expect(linhaConhPosEdicao.getByTestId('btn-remover-conhecimento')).toBeVisible();
  135 | 
  136 |             await AtividadeHelpers.removerConhecimento(page, atividadeEditada, conhecimento1Editado);
  137 |             
  138 |             await AtividadeHelpers.removerAtividade(page, atividadeEditada);
  139 |         });
  140 | 
  141 |         await test.step('6. Verificar ausência de Botão de Impacto', async () => {
  142 |             await AtividadeHelpers.verificarBotaoImpactoAusenteEdicao(page);
  143 |         });
  144 | 
  145 |         await test.step('7. Disponibilizar', async () => {
  146 |             await AtividadeHelpers.disponibilizarCadastro(page);
  147 |             await expect(page).toHaveURL(/\/painel/);
  148 |         });
  149 |     });
  150 | 
  151 |     test('Cenário 2: Processo de Revisão (Botão impacto)', async ({_resetAutomatico, page, request}) => {
  152 |         const timestamp = Date.now();
  153 |         const descricao = `Processo CDU-08 Rev ${timestamp}`;
  154 |         const UNIDADE_REVISAO = 'ASSESSORIA_12';
  155 |         const CHEFE_REVISAO = USUARIOS.CHEFE_ASSESSORIA_12.titulo;
  156 |         const SENHA_REVISAO = USUARIOS.CHEFE_ASSESSORIA_12.senha;
  157 | 
  158 |         await test.step('Setup: Criar processo de Revisão', async () => {
  159 |             await criarProcessoFixture(request, {
  160 |                 unidade: UNIDADE_REVISAO,
  161 |                 descricao,
  162 |                 tipo: 'REVISAO',
  163 |                 iniciar: true,
  164 |                 diasLimite: 30
  165 |             });
  166 |         });
```