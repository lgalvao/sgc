# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-12.spec.ts >> CDU-12 - Verificar impactos no mapa de competências >> Passo 3.2: Verificação pelo GESTOR na tela de Visualização
- Location: e2e/cdu-12.spec.ts:72:5

# Error details

```
Error: expect(page).toHaveURL(expected) failed

Expected pattern: /\/cadastro(?:\?.*)?$/
Received string:  "http://localhost:5173/processo/401/SECAO_121?codSubprocesso=401"
Timeout: 5000ms

Call log:
  - Expect "toHaveURL" with timeout 5000ms
    14 × unexpected value "http://localhost:5173/processo/401/SECAO_121?codSubprocesso=401"

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
      - link "Minha unidade":
        - /url: /unidade/9
    - listitem:
      - link "Relatórios":
        - /url: /relatorios
    - listitem:
      - link "Histórico":
        - /url: /historico
  - list:
    - listitem:
      - link "GESTOR - COORD_12":
        - /url: "#"
    - listitem "Ativar modo escuro":
      - link "Ativar modo escuro":
        - /url: "#"
    - listitem "Sair":
      - link "Sair":
        - /url: "#"
- button "Voltar"
- navigation "breadcrumb":
  - list:
    - listitem:
      - link "Início":
        - /url: /painel
    - listitem:
      - text: ›
      - link "Detalhes do processo":
        - /url: /processo/401
    - listitem: › SECAO_121
- main:
  - heading "SECAO_121" [level=2]
  - paragraph: Seção 121
  - paragraph:
    - strong: "Processo:"
    - text: Revisão CDU-12 1778971406673
  - paragraph: Situação:Revisão em andamento
  - paragraph: Localização atual:SECAO_121
  - paragraph: Prazo para conclusão da etapa atual:15/06/2026
  - paragraph:
    - strong: "Titular:"
    - text: Lemmy Kilmister
  - paragraph:
    - text: "2010"
    - link "lemmy.kilmister@tre-pe.jus.br":
      - /url: mailto:lemmy.kilmister@tre-pe.jus.br
  - heading "Atividades e conhecimentos" [level=4]
  - paragraph: Cadastro de atividades e conhecimentos da unidade
  - heading "Mapa de competências" [level=4]
  - paragraph: Mapa de competências técnicas da unidade
  - heading "Movimentações" [level=4]
  - table:
    - rowgroup:
      - row "Data/hora Origem Destino Descrição":
        - columnheader "Data/hora"
        - columnheader "Origem"
        - columnheader "Destino"
        - columnheader "Descrição"
    - rowgroup:
      - row "16/05/2026 19:43 ADMIN SECAO_121 Processo iniciado":
        - cell "16/05/2026 19:43"
        - cell "ADMIN"
        - cell "SECAO_121"
        - cell "Processo iniciado"
- contentinfo: Versão 1.0.4 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  1   | import {expect, type Page} from '@playwright/test';
  2   | import {limparNotificacoes, verificarPaginaPainel, verificarToast} from './helpers-navegacao.js';
  3   | import {TEXTOS} from '../../frontend/src/constants/textos.js';
  4   | 
  5   | async function obterBotaoDisponibilizarCadastro(page: Page) {
  6   |     const dropdown = page.getByTestId('btn-cadastro-acoes');
  7   |     if (await dropdown.count() > 0) {
  8   |         await dropdown.click();
  9   |         return page.getByTestId('btn-cadastro-acao-disponibilizar');
  10  |     }
  11  | 
  12  |     return page.getByTestId('btn-cad-atividades-disponibilizar');
  13  | }
  14  | 
  15  | export async function esperarTelaAtividades(page: Page) {
  16  |     await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
  17  | }
  18  | 
  19  | export async function esperarAtividadesEditaveis(page: Page) {
  20  |     await expect(page.getByTestId('inp-nova-atividade')).toBeVisible();
  21  |     await expect(page.getByTestId('btn-adicionar-atividade')).toBeVisible();
  22  |     await expect(await obterBotaoDisponibilizarCadastro(page)).toBeVisible();
  23  | }
  24  | 
  25  | export async function esperarSemAcoesEdicaoCadastro(page: Page) {
  26  |     const input = page.getByTestId('inp-nova-atividade');
  27  |     const botao = page.getByTestId('btn-adicionar-atividade');
  28  | 
  29  |     // Se o elemento estiver no DOM (caso do perfil que PODE editar, como o Chefe), deve estar desabilitado.
  30  |     // Se não estiver no DOM (caso do perfil que NUNCA edita, como o Admin), deve estar oculto.
  31  |     if (await input.isVisible()) {
  32  |         await expect(input).toBeDisabled();
  33  |         await expect(botao).toBeDisabled();
  34  |     } else {
  35  |         await expect(input).toBeHidden();
  36  |         await expect(botao).toBeHidden();
  37  |     }
  38  | 
  39  |     const botaoImportar = page.getByTestId('btn-cad-atividades-importar');
  40  |     if (await botaoImportar.count() > 0) {
  41  |         await expect(botaoImportar).toBeVisible();
  42  |         await expect(botaoImportar).toBeDisabled();
  43  |     }
  44  | }
  45  | 
  46  | export async function esperarAtividadesSomenteLeitura(page: Page) {
  47  |     await esperarTelaAtividades(page);
  48  |     await esperarSemAcoesEdicaoCadastro(page);
  49  | }
  50  | 
  51  | export async function navegarParaCadastro(page: Page) {
  52  |     if (/\/cadastro(?:\?.*)?$/.test(page.url())) {
  53  |         await esperarTelaAtividades(page);
  54  |         return;
  55  |     }
  56  | 
  57  |     const card = page.getByTestId('card-subprocesso-atividades');
  58  |     await expect(card).toBeVisible();
  59  |     await card.click();
> 60  |     await expect(page).toHaveURL(/\/cadastro(?:\?.*)?$/);
      |                        ^ Error: expect(page).toHaveURL(expected) failed
  61  |     await esperarTelaAtividades(page);
  62  | }
  63  | 
  64  | 
  65  | export async function adicionarAtividade(page: Page, descricao: string) {
  66  |     await page.getByTestId('inp-nova-atividade').fill(descricao);
  67  |     await expect(page.getByTestId('btn-adicionar-atividade')).toBeEnabled();
  68  |     await page.getByTestId('btn-adicionar-atividade').click();
  69  |     await expect(page.getByText(descricao, {exact: true})).toBeVisible();
  70  | }
  71  | 
  72  | export async function adicionarConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimentoDescricao: string) {
  73  |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
  74  |     await card.getByTestId('inp-novo-conhecimento').fill(conhecimentoDescricao);
  75  | 
  76  |     const responsePromise = page.waitForResponse(resp => resp.url().includes('/conhecimentos') && resp.status() === 201);
  77  |     await card.getByTestId('btn-adicionar-conhecimento').click();
  78  |     await responsePromise;
  79  | 
  80  |     await expect(card.getByText(conhecimentoDescricao)).toBeVisible();
  81  | }
  82  | 
  83  | export async function editarAtividade(page: Page, descricaoAtual: string | RegExp, novaDescricao: string) {
  84  |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(descricaoAtual)});
  85  |     const editButton = card.getByTestId('btn-editar-atividade');
  86  |     await editButton.click();
  87  | 
  88  |     const input = page.getByTestId('inp-editar-atividade');
  89  |     await input.waitFor({state: 'visible'});
  90  |     await input.fill(novaDescricao);
  91  | 
  92  |     await page.getByTestId('btn-salvar-edicao-atividade').click();
  93  |     await expect(page.getByText(novaDescricao)).toBeVisible();
  94  | }
  95  | 
  96  | export async function cancelarEdicaoAtividade(page: Page, descricaoAtual: string | RegExp, textoCancelado: string) {
  97  |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(descricaoAtual)});
  98  |     const editButton = card.getByTestId('btn-editar-atividade');
  99  | 
  100 |     await editButton.click();
  101 | 
  102 |     const input = page.getByTestId('inp-editar-atividade');
  103 |     await input.waitFor({state: 'visible'});
  104 |     await input.fill(textoCancelado);
  105 | 
  106 |     await page.getByTestId('btn-cancelar-edicao-atividade').click();
  107 |     await expect(page.getByText(descricaoAtual)).toBeVisible();
  108 | }
  109 | 
  110 | export async function removerAtividade(page: Page, descricao: string | RegExp) {
  111 |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(descricao)});
  112 |     const row = card.getByTestId('cad-atividades__hover-row');
  113 |     const btnRemover = card.getByTestId('btn-remover-atividade');
  114 | 
  115 |     await row.hover();
  116 |     await expect(btnRemover).toBeVisible();
  117 |     await btnRemover.click();
  118 | 
  119 |     const dialog = page.getByRole('dialog');
  120 |     await expect(dialog.getByText(TEXTOS.atividades.MODAL_REMOVER_ATIVIDADE_TEXTO)).toBeVisible();
  121 |     await page.getByTestId('btn-modal-confirmacao-confirmar').click();
  122 |     await expect(page.getByText(descricao)).toBeHidden();
  123 | }
  124 | 
  125 | export async function editarConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimentoAtual: string, novoConhecimento: string) {
  126 |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
  127 |     const linhaConhecimento = card.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimentoAtual});
  128 |     const btnEditar = linhaConhecimento.getByTestId('btn-editar-conhecimento');
  129 | 
  130 |     await linhaConhecimento.hover();
  131 |     await expect(btnEditar).toBeVisible();
  132 |     await btnEditar.click();
  133 | 
  134 |     const input = card.getByTestId('inp-editar-conhecimento');
  135 |     await input.waitFor({state: 'visible'});
  136 |     await input.fill(novoConhecimento);
  137 |     await card.getByTestId('btn-salvar-edicao-conhecimento').click();
  138 | 
  139 |     await expect(card.getByText(novoConhecimento)).toBeVisible();
  140 | }
  141 | 
  142 | export async function cancelarEdicaoConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimentoAtual: string, textoCancelado: string) {
  143 |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
  144 |     const linhaConhecimento = card.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimentoAtual});
  145 |     const btnEditar = linhaConhecimento.getByTestId('btn-editar-conhecimento');
  146 | 
  147 |     await linhaConhecimento.hover();
  148 |     await expect(btnEditar).toBeVisible();
  149 |     await btnEditar.click();
  150 | 
  151 |     const input = card.getByTestId('inp-editar-conhecimento');
  152 |     await input.waitFor({state: 'visible'});
  153 |     await input.fill(textoCancelado);
  154 | 
  155 |     await card.getByTestId('btn-cancelar-edicao-conhecimento').click();
  156 |     await expect(card.getByText(conhecimentoAtual)).toBeVisible();
  157 | }
  158 | 
  159 | export async function removerConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimento: string) {
  160 |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
```