# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-04.spec.ts >> CDU-04 - Iniciar processo >> Deve iniciar um processo e validar criação de subprocessos e alertas
- Location: e2e/cdu-04.spec.ts:40:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('tbl-alertas').locator('tr').filter({ hasText: 'CDU-04 Iniciar - 1780528136149' }).filter({ hasText: 'Início do processo em unidade(s) subordinada(s)' })
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('tbl-alertas').locator('tr').filter({ hasText: 'CDU-04 Iniciar - 1780528136149' }).filter({ hasText: 'Início do processo em unidade(s) subordinada(s)' })

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
        - /url: /unidade/2
    - listitem:
      - link "Relatórios":
        - /url: /relatorios
    - listitem:
      - link "Histórico":
        - /url: /historico
  - list:
    - listitem:
      - link "GESTOR - SECRETARIA_1":
        - /url: "#"
    - listitem "Ativar modo escuro":
      - link "Ativar modo escuro":
        - /url: "#"
    - listitem "Sair":
      - link "Sair":
        - /url: "#"
- main:
  - heading "Painel" [level=1]
  - heading "Processos" [level=2]
  - table:
    - rowgroup:
      - row "Descrição Click to sort descending Tipo Click to sort ascending Unidades Situação Click to sort ascending":
        - columnheader "Descrição Click to sort descending"
        - columnheader "Tipo Click to sort ascending"
        - columnheader "Unidades"
        - columnheader "Situação Click to sort ascending"
    - rowgroup:
      - row "CDU-04 Iniciar - 1780528136149 Mapeamento SECRETARIA_1 Em andamento":
        - cell "CDU-04 Iniciar - 1780528136149"
        - cell "Mapeamento"
        - cell "SECRETARIA_1"
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
      - row "03/06/2026 20:08 Início do processo CDU-04 Iniciar - 1780528136149 ADMIN":
        - cell "03/06/2026 20:08"
        - cell "Início do processo"
        - cell "CDU-04 Iniciar - 1780528136149"
        - cell "ADMIN"
- contentinfo: Versão 1.2.0 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  29  |         await expect(page).toHaveURL(/\/processo\/cadastro/);
  30  | 
  31  |         await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
  32  |         await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
  33  |         await page.getByTestId('btn-arvore-expand-COORD_11').click();
  34  | 
  35  |         const checkboxUnidadeSemResponsavel = page.getByTestId('chk-arvore-unidade-SECAO_SEM_RESP');
  36  |         await expect(checkboxUnidadeSemResponsavel).toBeVisible();
  37  |         await expect(checkboxUnidadeSemResponsavel).toBeDisabled();
  38  |     });
  39  | 
  40  |     test('Deve iniciar um processo e validar criação de subprocessos e alertas', async ({
  41  |                                                                                             _resetAutomatico,
  42  |                                                                                             page,
  43  |                                                                                             browser,
  44  |                                                                                             _autenticadoComoAdmin
  45  |                                                                                         }) => {
  46  |         const descricao = `CDU-04 Iniciar - ${Date.now()}`;
  47  |         await criarProcesso(page, {
  48  |             descricao: descricao,
  49  |             tipo: 'MAPEAMENTO',
  50  |             diasLimite: 15,
  51  |             unidade: ['SECRETARIA_1', 'ASSESSORIA_11'], // Interoperacional + Operacional
  52  |             expandir: ['SECRETARIA_1'],
  53  |             iniciar: false
  54  |         });
  55  | 
  56  |         await acessarDetalhesProcesso(page, descricao);
  57  |         await esperarPaginaCadastroProcesso(page);
  58  |         const codProcesso = await extrairProcessoCodigo(page);
  59  | 
  60  |         const dataLimiteStr = await page.getByTestId('inp-processo-data-limite').inputValue();
  61  | 
  62  |         await page.getByTestId('btn-processo-iniciar-rodape').click();
  63  |         const modal = page.getByRole('dialog');
  64  |         await expect(modal.getByText(TEXTOS.processo.cadastro.INICIAR_CONFIRMACAO)).toBeVisible();
  65  |         await confirmarInicioProcessoPeloDialogo(page, {
  66  |             descricao,
  67  |             tipo: 'MAPEAMENTO',
  68  |             unidadesComEquipePropriaParticipantes: ['SECRETARIA_1']
  69  |         });
  70  | 
  71  |         await verificarToast(page, TEXTOS.sucesso.PROCESSO_INICIADO);
  72  |         await verificarProcessoTabela(page, {
  73  |             descricao,
  74  |             situacao: 'Em andamento',
  75  |             tipo: 'Mapeamento'
  76  |         });
  77  | 
  78  |         await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descricao)}).click();
  79  |         await esperarPaginaDetalhesProcesso(page, codProcesso);
  80  | 
  81  |         await verificarDetalhesProcesso(page, {
  82  |             descricao: descricao,
  83  |             tipo: 'Mapeamento',
  84  |             situacao: 'Em andamento'
  85  |         });
  86  | 
  87  |         const linhaSecretaria1 = page.locator('tr', {hasText: 'SECRETARIA_1'}).first();
  88  |         await expect(linhaSecretaria1).toContainText('Não iniciado');
  89  |         await expect(linhaSecretaria1).toContainText(dataLimiteStr.split('-').reverse().join('/'));
  90  | 
  91  |         const linhaSecao111 = page.locator('tr', {hasText: 'SECAO_111'}).first();
  92  |         await expect(linhaSecao111).toContainText('Não iniciado');
  93  |         await expect(linhaSecao111).toContainText(dataLimiteStr.split('-').reverse().join('/'));
  94  | 
  95  |         await linhaSecretaria1.click();
  96  |         await esperarPaginaSubprocesso(page, 'SECRETARIA_1');
  97  |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText('Não iniciado');
  98  | 
  99  |         const timeline = page.getByTestId('tbl-movimentacoes');
  100 |         await expect(timeline.getByText(TEXTOS.movimentacao.PROCESSO_INICIADO)).toBeVisible();
  101 | 
  102 |         // John lennon (SECRETARIA_1) deve receber alertas tanto como Chefe quanto como Gestor
  103 | 
  104 |         // 7.1. Como CHEFE (Unidade operacional)
  105 |         const contextoChefeSec1 = await browser.newContext();
  106 |         const paginaChefeSec1 = await contextoChefeSec1.newPage();
  107 |         await loginComPerfil(paginaChefeSec1, USUARIOS.CHEFE_SECRETARIA_1.titulo, USUARIOS.CHEFE_SECRETARIA_1.senha, USUARIOS.CHEFE_SECRETARIA_1.perfil);
  108 | 
  109 |         const tabelaAlertasSec1Chefe = paginaChefeSec1.getByTestId('tbl-alertas');
  110 |         await expect(tabelaAlertasSec1Chefe.locator('tr', {hasText: descricao})
  111 |             .filter({hasText: 'Início do processo'})
  112 |             .filter({hasNotText: 'subordinada'})
  113 |         ).toBeVisible();
  114 | 
  115 |         await paginaChefeSec1.getByTestId('tbl-processos').locator('tr', {hasText: descricao}).click();
  116 |         await paginaChefeSec1.locator('tr', {hasText: 'SECRETARIA_1'}).first().click();
  117 |         await esperarPaginaSubprocesso(paginaChefeSec1, 'SECRETARIA_1');
  118 |         await expect(paginaChefeSec1.getByTestId('subprocesso-header__txt-situacao')).toHaveText('Não iniciado');
  119 |         await contextoChefeSec1.close();
  120 | 
  121 |         // 7.2. Como GESTOR (Unidade intermediária) - deve ver alerta sobre subordinada (ASSESSORIA_11)
  122 |         const contextoGestorSec1 = await browser.newContext();
  123 |         const paginaGestorSec1 = await contextoGestorSec1.newPage();
  124 |         await loginComPerfil(paginaGestorSec1, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
  125 | 
  126 |         const tabelaAlertasSec1Gestor = paginaGestorSec1.getByTestId('tbl-alertas');
  127 |         await expect(tabelaAlertasSec1Gestor.locator('tr', {hasText: descricao})
  128 |             .filter({hasText: 'Início do processo em unidade(s) subordinada(s)'})
> 129 |         ).toBeVisible();
      |           ^ Error: expect(locator).toBeVisible() failed
  130 |         await contextoGestorSec1.close();
  131 | 
  132 |         await verificarNotificacaoAdmin(page, {
  133 |             destinatario: 'SECRETARIA_1',
  134 |             assunto: 'Início de processo de mapeamento de competências',
  135 |             tipo: 'Início do processo',
  136 |             trechoCorpo: new RegExp(`Comunicamos o início do processo\\s+${descricao}\\s+para a sua unidade`, 'i')
  137 |         });
  138 |     });
  139 | });
  140 | 
```