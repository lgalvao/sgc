# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: jornada.spec.ts >> Jornada do Ciclo de Vida Completo do SGC >> Fase 3: Ciclo de Revisão e Manutenção
- Location: e2e/jornada.spec.ts:37:5

# Error details

```
Error: expect(page).toHaveURL(expected) failed

Expected pattern: /\/painel(?:\?.*)?$/
Received string:  "http://localhost:5173/processo/cadastro"
Timeout: 5000ms

Call log:
  - Expect "toHaveURL" with timeout 5000ms
    9 × unexpected value "http://localhost:5173/processo/cadastro"

```

# Page snapshot

```yaml
- generic [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - main [ref=e6]:
      - generic [ref=e10]:
        - heading "SGC" [level=1] [ref=e11]
        - paragraph [ref=e12]: Sistema de Gestão de Competências
        - generic [ref=e13]:
          - group [ref=e14]:
            - generic [ref=e15]:
              - generic [ref=e16]: 
              - text: Título eleitoral *
            - textbox "Título eleitoral" [active] [ref=e17]:
              - /placeholder: Digite seu título
          - group [ref=e18]:
            - generic [ref=e19]:
              - generic [ref=e20]: 
              - text: Senha *
            - group [ref=e21]:
              - textbox "Senha" [ref=e22]:
                - /placeholder: Digite sua senha
              - button "Mostrar senha" [ref=e23] [cursor=pointer]:
                - generic [ref=e24]: 
          - button "Entrar" [ref=e25] [cursor=pointer]:
            - generic [ref=e26]: 
            - text: Entrar
```

# Test source

```ts
  22  | }
  23  | 
  24  | /**
  25  |  * Cria um processo através da UI
  26  |  */
  27  | export async function criarProcesso(page: Page, options: {
  28  |     descricao: string;
  29  |     tipo: TipoProcesso;
  30  |     diasLimite?: number;
  31  |     unidade: string | string[];
  32  |     expandir?: string[];
  33  |     iniciar?: boolean;
  34  | }): Promise<void> {
  35  |     const dias = options.diasLimite ?? 30;
  36  |     await page.getByTestId('btn-painel-criar-processo').click();
  37  |     await expect(page).toHaveURL(/\/processo\/cadastro/);
  38  | 
  39  |     await page.getByTestId('inp-processo-descricao').fill(options.descricao);
  40  |     await page.getByTestId('sel-processo-tipo').selectOption(options.tipo);
  41  |     await page.getByTestId('inp-processo-data-limite').fill(calcularDataLimite(dias));
  42  | 
  43  |     await expect(page.getByText(TEXTOS.unidade.CARREGANDO)).toBeHidden();
  44  |     if (options.expandir) {
  45  |         for (const sigla of options.expandir) {
  46  |             await page.getByTestId(`btn-arvore-expand-${sigla}`).click();
  47  |         }
  48  |     }
  49  | 
  50  |     const unidades = Array.isArray(options.unidade) ? options.unidade : [options.unidade];
  51  |     for (const u of unidades) {
  52  |         const checkbox = page.getByTestId(`chk-arvore-unidade-${u}`);
  53  |         await expect(checkbox).toBeVisible();
  54  |         await expect(checkbox).toBeEnabled();
  55  |         // Só marca se não estiver marcado (pode já estar marcado se o pai foi selecionado)
  56  |         if (!await checkbox.isChecked()) {
  57  |             await checkbox.check();
  58  |         }
  59  |     }
  60  | 
  61  |     if (options.iniciar) {
  62  |         await iniciarProcessoPeloCadastro(page, {
  63  |             descricao: options.descricao,
  64  |             tipo: options.tipo
  65  |         });
  66  |     } else {
  67  |         const botaoSalvar = page.getByTestId('btn-processo-salvar');
  68  |         await botaoSalvar.scrollIntoViewIfNeeded();
  69  |         await expect(botaoSalvar).toBeInViewport();
  70  |         await botaoSalvar.click();
  71  |         await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
  72  |     }
  73  | }
  74  | 
  75  | /**
  76  |  * Verifica os cabeçalhos obrigatórios da tabela de processos
  77  |  */
  78  | 
  79  | export async function verificarCabecalhosTabelaProcessos(page: Page, compacto = false): Promise<void> {
  80  |     const tabela = page.getByTestId('tbl-processos');
  81  |     await expect(tabela).toBeVisible();
  82  | 
  83  |     const rotuloUnidades = compacto ? 'Unidades' : 'Unidades participantes';
  84  |     const cabecalhosEsperados = ['Descrição', 'Tipo', rotuloUnidades, 'Situação'];
  85  | 
  86  |     for (const cabecalho of cabecalhosEsperados) {
  87  |         await expect(tabela.locator('th', {hasText: cabecalho}).first()).toBeVisible();
  88  |     }
  89  | }
  90  | 
  91  | /**
  92  |  * Verifica que um processo aparece na tabela com situação e tipo corretos
  93  |  */
  94  | export async function verificarProcessoNaTabela(page: Page, options: {
  95  |     descricao: string;
  96  |     situacao: string;
  97  |     tipo: string;
  98  |     unidadesParticipantes?: string[];
  99  | }): Promise<void> {
  100 |     const tabela = page.locator('[data-testid="tbl-processos"]');
  101 |     await expect(tabela).toBeVisible();
  102 |     
  103 |     // Localizar a linha que contém a descrição do processo
  104 |     const linhaProcesso = tabela.locator('tr').filter({hasText: options.descricao}).first();
  105 |     await expect(linhaProcesso).toBeVisible();
  106 |     await expect(linhaProcesso.getByText(new RegExp(options.situacao, 'i'))).toBeVisible();
  107 |     await expect(linhaProcesso.getByText(new RegExp(`^${options.tipo}$`, 'i'))).toBeVisible();
  108 | 
  109 |     if (options.unidadesParticipantes) {
  110 |         for (const unidade of options.unidadesParticipantes) {
  111 |             await expect(linhaProcesso.getByText(unidade)).toBeVisible();
  112 |         }
  113 |     }
  114 | }
  115 | 
  116 | export async function aguardarProcessoNoPainel(page: Page, options: {
  117 |     descricao: string;
  118 |     situacao: string;
  119 |     tipo: TipoProcesso;
  120 |     unidadesParticipantes?: string[];
  121 | }): Promise<void> {
> 122 |     await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
      |                        ^ Error: expect(page).toHaveURL(expected) failed
  123 |     await verificarProcessoNaTabela(page, {
  124 |         descricao: options.descricao,
  125 |         situacao: options.situacao,
  126 |         tipo: ROTULOS_TIPO_PROCESSO[options.tipo],
  127 |         unidadesParticipantes: options.unidadesParticipantes
  128 |     });
  129 | }
  130 | 
  131 | export async function iniciarProcessoPeloCadastro(page: Page, options: {
  132 |     descricao: string;
  133 |     tipo: TipoProcesso;
  134 |     unidadesParticipantes?: string[];
  135 | }): Promise<void> {
  136 |     await page.getByTestId('btn-processo-iniciar').click();
  137 |     await confirmarInicioProcessoPeloDialogo(page, options);
  138 | }
  139 | 
  140 | export async function confirmarInicioProcessoPeloDialogo(page: Page, options: {
  141 |     descricao: string;
  142 |     tipo: TipoProcesso;
  143 |     unidadesParticipantes?: string[];
  144 | }): Promise<void> {
  145 |     const dialog = page.getByRole('dialog');
  146 |     await expect(dialog).toBeVisible();
  147 |     await dialog.getByTestId('btn-iniciar-processo-confirmar').click();
  148 |     await aguardarProcessoNoPainel(page, {
  149 |         descricao: options.descricao,
  150 |         situacao: 'Em andamento',
  151 |         tipo: options.tipo,
  152 |         unidadesParticipantes: options.unidadesParticipantes
  153 |     });
  154 | }
  155 | 
  156 | export interface UnidadeParticipante {
  157 |     sigla: string;
  158 |     situacao: string;
  159 |     dataLimite: string | RegExp;
  160 | }
  161 | 
  162 | export async function verificarDetalhesProcesso(page: Page, dados: {
  163 |     descricao: string,
  164 |     tipo: string,
  165 |     situacao: 'Criado' | 'Em andamento' | 'Finalizado'
  166 | }) {
  167 |     // Aguardar carregamento dos detalhes
  168 |     await expect(page.getByText(TEXTOS.processo.CARREGANDO_DETALHES).first()).toBeHidden();
  169 | 
  170 |     // Verificar descrição usando o test-id existente
  171 |     await expect(page.getByTestId('processo-info')).toHaveText(dados.descricao);
  172 | 
  173 |     // Verificar tipo e situação usando getByText
  174 |     await expect(page.getByText(`${TEXTOS.processo.INFO_TIPO}: ${dados.tipo}`)).toBeVisible();
  175 |     await expect(page.getByText(`${TEXTOS.processo.INFO_SITUACAO}: ${dados.situacao}`)).toBeVisible();
  176 | }
  177 | 
  178 | export async function verificarUnidadeParticipante(page: Page, unidade: UnidadeParticipante) {
  179 |     const row = page.locator('tr').filter({hasText: new RegExp(String.raw`^\s*${unidade.sigla}\b`, 'i')}).first();
  180 |     await expect(row).toBeVisible();
  181 |     await expect(row).toContainText(unidade.situacao);
  182 | 
  183 |     if (unidade.dataLimite instanceof RegExp) {
  184 |         await expect(row).toHaveText(unidade.dataLimite);
  185 |     } else {
  186 |         await expect(row).toContainText(unidade.dataLimite);
  187 |     }
  188 | }
  189 | 
  190 | export async function verificarDetalhesSubprocesso(page: Page, dados: {
  191 |     sigla: string,
  192 |     nomeUnidade?: string,
  193 |     situacao: string,
  194 |     prazo?: string | RegExp,
  195 |     localizacao?: string,
  196 |     titular?: string,
  197 |     ramalTitular?: string,
  198 |     emailTitular?: string,
  199 |     responsavel?: string,
  200 |     tipoResponsabilidade?: string,
  201 |     ramalResponsavel?: string,
  202 |     emailResponsavel?: string
  203 | }) {
  204 |     const header = page.getByTestId('header-subprocesso');
  205 |     await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toContainText(dados.sigla);
  206 |     if (dados.nomeUnidade) {
  207 |         await expect(header).toContainText(dados.nomeUnidade);
  208 |     }
  209 |     if (dados.titular) {
  210 |         await expect(page.getByText(`Titular: ${dados.titular}`).first()).toBeVisible();
  211 |     }
  212 |     if (dados.ramalTitular) {
  213 |         await expect(header.getByText(dados.ramalTitular)).toBeVisible();
  214 |     }
  215 |     if (dados.emailTitular) {
  216 |         await expect(header.getByRole('link', {name: dados.emailTitular})).toBeVisible();
  217 |     }
  218 |     if (dados.responsavel) {
  219 |         await expect(page.getByText(`Responsável: ${dados.responsavel}`).first()).toBeVisible();
  220 |     }
  221 |     if (dados.tipoResponsabilidade) {
  222 |         await expect(page.getByText(`- ${dados.tipoResponsabilidade}`).first()).toBeVisible();
```