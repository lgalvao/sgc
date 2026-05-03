# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: jornada-geral-semantic.spec.ts >> Jornada geral semântica - mapeamento e revisão ponta a ponta >> Fase 1 - ADMIN cria e inicia o processo de mapeamento da seção
- Location: e2e/jornada-geral-semantic.spec.ts:60:5

# Error details

```
Error: locator.click: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('btn-painel-criar-processo')

```

# Test source

```ts
  1   | import {expect, type Locator, type Page} from '@playwright/test';
  2   | import {TEXTOS} from '../../frontend/src/constants/textos.js';
  3   | 
  4   | const ROTULOS_TIPO_PROCESSO = {
  5   |     MAPEAMENTO: 'Mapeamento',
  6   |     REVISAO: 'Revisão',
  7   |     DIAGNOSTICO: 'Diagnóstico'
  8   | } as const;
  9   | 
  10  | type TipoProcesso = keyof typeof ROTULOS_TIPO_PROCESSO;
  11  | 
  12  | async function buscarUnidadeNaArvore(page: Page, siglaUnidade: string): Promise<void> {
  13  |     const busca = page.getByRole('searchbox', {name: 'Buscar unidade por sigla'});
  14  |     await expect(busca).toBeVisible();
  15  |     await busca.fill(siglaUnidade);
  16  | }
  17  | 
  18  | /**
  19  |  * Calcula uma data limite N dias no futuro
  20  |  */
  21  | export function calcularDataLimite(dias: number): string {
  22  |     const dataLimite = new Date();
  23  |     dataLimite.setDate(dataLimite.getDate() + dias);
  24  |     const ano = dataLimite.getFullYear();
  25  |     const mes = String(dataLimite.getMonth() + 1).padStart(2, '0');
  26  |     const dia = String(dataLimite.getDate()).padStart(2, '0');
  27  |     return `${ano}-${mes}-${dia}`;
  28  | }
  29  | 
  30  | /**
  31  |  * Cria um processo através da UI
  32  |  */
  33  | export async function criarProcesso(page: Page, options: {
  34  |     descricao: string;
  35  |     tipo: TipoProcesso;
  36  |     diasLimite?: number;
  37  |     unidade: string | string[];
  38  |     expandir?: string[];
  39  |     iniciar?: boolean;
  40  | }): Promise<void> {
  41  |     const dias = options.diasLimite ?? 30;
  42  |     await page.getByTestId('btn-painel-criar-processo').click();
  43  |     await expect(page).toHaveURL(/\/processo\/cadastro/);
  44  | 
  45  |     await page.getByTestId('inp-processo-descricao').fill(options.descricao);
  46  |     await page.getByTestId('sel-processo-tipo').selectOption(options.tipo);
  47  |     await page.getByTestId('inp-processo-data-limite').fill(calcularDataLimite(dias));
  48  | 
  49  |     await expect(page.getByText(TEXTOS.unidade.CARREGANDO)).toBeHidden();
  50  |     if (options.expandir) {
  51  |         for (const sigla of options.expandir) {
  52  |             await page.getByTestId(`btn-arvore-expand-${sigla}`).click();
  53  |         }
  54  |     }
  55  | 
  56  |     const unidades = Array.isArray(options.unidade) ? options.unidade : [options.unidade];
  57  |     for (const u of unidades) {
  58  |         const checkbox = page.getByTestId(`chk-arvore-unidade-${u}`);
  59  |         await expect(checkbox).toBeVisible();
  60  |         await expect(checkbox).toBeEnabled();
  61  |         // Só marca se não estiver marcado (pode já estar marcado se o pai foi selecionado)
  62  |         if (!await checkbox.isChecked()) {
  63  |             await checkbox.check();
  64  |         }
  65  |     }
  66  | 
  67  |     if (options.iniciar) {
  68  |         await iniciarProcessoPeloCadastro(page, {
  69  |             descricao: options.descricao,
  70  |             tipo: options.tipo
  71  |         });
  72  |     } else {
  73  |         const botaoSalvar = page.getByTestId('btn-processo-salvar-rodape');
  74  |         await botaoSalvar.scrollIntoViewIfNeeded();
  75  |         await expect(botaoSalvar).toBeInViewport();
  76  |         await botaoSalvar.click();
  77  |         await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
  78  |     }
  79  | }
  80  | 
  81  | /**
  82  |  * Cria um processo pela UI em formato semântico:
  83  |  * - recebe sempre um array em `unidades`
  84  |  * - expande a árvore automaticamente até encontrar as unidades
  85  |  * - apenas cria o processo, sem iniciá-lo
  86  |  */
  87  | export async function criarProcessoSimples(page: Page, options: {
  88  |     descricao: string;
  89  |     tipo: TipoProcesso;
  90  |     diasLimite?: number;
  91  |     unidades: string[];
  92  | }): Promise<void> {
  93  |     const dias = options.diasLimite ?? 30;
> 94  |     await page.getByTestId('btn-painel-criar-processo').click();
      |                                                         ^ Error: locator.click: Target page, context or browser has been closed
  95  |     await expect(page).toHaveURL(/\/processo\/cadastro/);
  96  | 
  97  |     await page.getByTestId('inp-processo-descricao').fill(options.descricao);
  98  |     await page.getByTestId('sel-processo-tipo').selectOption(options.tipo);
  99  |     await page.getByTestId('inp-processo-data-limite').fill(calcularDataLimite(dias));
  100 | 
  101 |     await expect(page.getByText(TEXTOS.unidade.CARREGANDO)).toBeHidden();
  102 | 
  103 |     for (const siglaUnidade of options.unidades) {
  104 |         await buscarUnidadeNaArvore(page, siglaUnidade);
  105 | 
  106 |         const checkbox = page.getByTestId(`chk-arvore-unidade-${siglaUnidade}`);
  107 |         await expect(checkbox).toBeVisible();
  108 |         await expect(checkbox).toBeEnabled();
  109 | 
  110 |         if (!await checkbox.isChecked()) {
  111 |             await checkbox.check();
  112 |         }
  113 |     }
  114 | 
  115 |     const botaoSalvar = page.getByTestId('btn-processo-salvar-rodape');
  116 |     await botaoSalvar.scrollIntoViewIfNeeded();
  117 |     await expect(botaoSalvar).toBeInViewport();
  118 |     await botaoSalvar.click();
  119 |     await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
  120 | }
  121 | 
  122 | /**
  123 |  * Verifica os cabeçalhos obrigatórios da tabela de processos
  124 |  */
  125 | 
  126 | export async function verificarCabecalhosTabelaProcessos(page: Page, compacto = false): Promise<void> {
  127 |     const tabela = page.getByTestId('tbl-processos');
  128 |     await expect(tabela).toBeVisible();
  129 | 
  130 |     const rotuloUnidades = compacto ? 'Unidades' : 'Unidades participantes';
  131 |     const cabecalhosEsperados = ['Descrição', 'Tipo', rotuloUnidades, 'Situação'];
  132 | 
  133 |     for (const cabecalho of cabecalhosEsperados) {
  134 |         await expect(tabela.locator('th', {hasText: cabecalho}).first()).toBeVisible();
  135 |     }
  136 | }
  137 | 
  138 | /**
  139 |  * Verifica que um processo aparece na tabela com situação e tipo corretos
  140 |  */
  141 | export async function verificarProcessoTabela(page: Page, options: {
  142 |     descricao: string;
  143 |     situacao: string;
  144 |     tipo: string;
  145 |     unidadesParticipantes?: string[];
  146 | }): Promise<void> {
  147 |     const tabela = page.locator('[data-testid="tbl-processos"]');
  148 |     await expect(tabela).toBeVisible();
  149 |     
  150 |     // Localizar a linha que contém a descrição do processo
  151 |     const linhaProcesso = tabela.locator('tr').filter({hasText: options.descricao}).first();
  152 |     await expect(linhaProcesso).toBeVisible();
  153 |     await expect(linhaProcesso.getByText(new RegExp(options.situacao, 'i'))).toBeVisible();
  154 |     await expect(linhaProcesso.getByText(new RegExp(`^${options.tipo}$`, 'i'))).toBeVisible();
  155 | 
  156 |     if (options.unidadesParticipantes) {
  157 |         for (const unidade of options.unidadesParticipantes) {
  158 |             await expect(linhaProcesso.getByText(unidade)).toBeVisible();
  159 |         }
  160 |     }
  161 | }
  162 | 
  163 | export async function aguardarProcessoNoPainel(page: Page, options: {
  164 |     descricao: string;
  165 |     situacao: string;
  166 |     tipo: TipoProcesso;
  167 |     unidadesParticipantes?: string[];
  168 | }): Promise<void> {
  169 |     await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
  170 |     await verificarProcessoTabela(page, {
  171 |         descricao: options.descricao,
  172 |         situacao: options.situacao,
  173 |         tipo: ROTULOS_TIPO_PROCESSO[options.tipo],
  174 |         unidadesParticipantes: options.unidadesParticipantes
  175 |     });
  176 | }
  177 | 
  178 | export async function iniciarProcessoPeloCadastro(page: Page, options: {
  179 |     descricao: string;
  180 |     tipo: TipoProcesso;
  181 |     unidadesParticipantes?: string[];
  182 | }): Promise<void> {
  183 |     await page.getByTestId('btn-processo-iniciar-rodape').click();
  184 |     await confirmarInicioProcessoPeloDialogo(page, options);
  185 | }
  186 | 
  187 | /**
  188 |  * Inicia o processo já aberto na tela de cadastro/detalhes.
  189 |  * Versão semântica: não exige informar o tipo novamente.
  190 |  */
  191 | export async function iniciarProcesso(page: Page, descricao: string): Promise<void> {
  192 |     await page.getByTestId('btn-processo-iniciar-rodape').click();
  193 | 
  194 |     const dialog = page.getByRole('dialog');
```